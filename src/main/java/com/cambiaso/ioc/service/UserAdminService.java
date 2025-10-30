package com.cambiaso.ioc.service;

import com.cambiaso.ioc.dto.request.UsuarioCreateRequest;
import com.cambiaso.ioc.dto.request.UsuarioUpdateRequest;
import com.cambiaso.ioc.exception.ResourceConflictException;
import com.cambiaso.ioc.exception.ResourceNotFoundException;
import com.cambiaso.ioc.mapper.UsuarioMapper;
import com.cambiaso.ioc.persistence.entity.AppUser;
import com.cambiaso.ioc.persistence.entity.Planta;
import com.cambiaso.ioc.persistence.repository.AppUserRepository;
import com.cambiaso.ioc.persistence.repository.AppUserSearchRepository;
import com.cambiaso.ioc.persistence.repository.PlantaRepository;
import com.cambiaso.ioc.persistence.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserAdminService {

    private final AppUserRepository appUserRepository;
    private final AppUserSearchRepository appUserSearchRepository;
    private final PlantaRepository plantaRepository;
    private final UserRoleRepository userRoleRepository;
    private final UsuarioMapper usuarioMapper;
    private final SupabaseAuthService supabaseAuthService;

    @Transactional(readOnly = true)
    public Page<com.cambiaso.ioc.dto.response.UsuarioResponse> search(String search, Integer plantaId, Boolean isActive, Pageable pageable) {
        Page<AppUser> page = appUserSearchRepository.search((search == null || search.isBlank()) ? null : search.trim(), plantaId, isActive, pageable);
        return page.map(u -> usuarioMapper.toResponse(u, userRoleRepository.findRoleNamesByUserId(u.getId())));
    }

    @Transactional(readOnly = true)
    public com.cambiaso.ioc.dto.response.UsuarioResponse getById(Long id) {
        AppUser u = appUserRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        List<String> roles = userRoleRepository.findRoleNamesByUserId(id);
        return usuarioMapper.toResponse(u, roles);
    }

    public com.cambiaso.ioc.dto.response.UsuarioResponse create(UsuarioCreateRequest req) {
        String email = req.getEmail().trim();

        // Validar que el email no exista
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new ResourceConflictException("Email already exists: " + email);
        }

        UUID supabaseId;
        boolean createdInSupabase = false;

        // NUEVO FLUJO: Si viene password, crear en Supabase automáticamente
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            log.info("Creating user in Supabase with password for email: {}", email);
            try {
                supabaseId = supabaseAuthService.createSupabaseUser(email, req.getPassword());
                createdInSupabase = true;
            } catch (Exception e) {
                log.error("Failed to create Supabase account for email: {}", email, e);
                throw new RuntimeException("Failed to create Supabase account: " + e.getMessage(), e);
            }
        }
        // FLUJO LEGACY: Si viene supabaseUserId, usarlo (deprecado)
        else if (req.getSupabaseUserId() != null) {
            log.warn("Using deprecated supabaseUserId field for user creation: {}", email);
            supabaseId = req.getSupabaseUserId();
            if (appUserRepository.existsBySupabaseUserId(supabaseId)) {
                throw new ResourceConflictException("Supabase user id already exists: " + supabaseId);
            }
        }
        // ERROR: Debe venir uno de los dos
        else {
            throw new IllegalArgumentException("Either password or supabaseUserId must be provided");
        }

        // Crear usuario en la BD
        AppUser u = new AppUser();
        u.setEmail(email);
        u.setSupabaseUserId(supabaseId);
        u.setPrimerNombre(req.getPrimerNombre());
        u.setSegundoNombre(req.getSegundoNombre());
        u.setPrimerApellido(req.getPrimerApellido());
        u.setSegundoApellido(req.getSegundoApellido());
        u.setCentroCosto(req.getCentroCosto());
        u.setFechaContrato(req.getFechaContrato());
        u.setActive(true);
        u.setCreatedAt(OffsetDateTime.now());
        u.setUpdatedAt(OffsetDateTime.now());

        if (req.getPlantaId() != null) {
            Planta p = plantaRepository.findById(req.getPlantaId()).orElse(null);
            u.setPlanta(p);
        }

        try {
            AppUser saved = appUserRepository.save(u);
            log.info("Successfully created user in database with ID: {}", saved.getId());
            return usuarioMapper.toResponse(saved, List.of());
        } catch (Exception e) {
            log.error("Failed to save user to database, rolling back", e);
            // Rollback: eliminar usuario de Supabase si falló la BD y lo creamos nosotros
            if (createdInSupabase) {
                try {
                    supabaseAuthService.deleteSupabaseUser(supabaseId);
                    log.info("Successfully rolled back Supabase user creation");
                } catch (Exception rollbackError) {
                    log.error("Failed to rollback Supabase user creation for ID: {}", supabaseId, rollbackError);
                }
            }
            throw e;
        }
    }

    public com.cambiaso.ioc.dto.response.UsuarioResponse update(Long id, UsuarioUpdateRequest req) {
        AppUser u = appUserRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        u.setPrimerNombre(req.getPrimerNombre());
        u.setSegundoNombre(req.getSegundoNombre());
        u.setPrimerApellido(req.getPrimerApellido());
        u.setSegundoApellido(req.getSegundoApellido());
        u.setCentroCosto(req.getCentroCosto());
        u.setFechaContrato(req.getFechaContrato());
        if (req.getIsActive() != null) u.setActive(req.getIsActive());
        u.setUpdatedAt(OffsetDateTime.now());

        if (req.getPlantaId() != null) {
            Planta p = plantaRepository.findById(req.getPlantaId()).orElse(null);
            u.setPlanta(p);
        } else {
            u.setPlanta(null);
        }

        AppUser saved = appUserRepository.save(u);
        List<String> roles = userRoleRepository.findRoleNamesByUserId(id);
        return usuarioMapper.toResponse(saved, roles);
    }

    public void delete(Long id) {
        AppUser u = appUserRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        // soft delete
        u.setActive(false);
        u.setDeletedAt(OffsetDateTime.now());
        u.setUpdatedAt(OffsetDateTime.now());
        appUserRepository.save(u);
    }
}
