package com.cambiaso.ioc.service;

import com.cambiaso.ioc.dto.request.UsuarioCreateRequest;
import com.cambiaso.ioc.dto.request.UsuarioUpdateRequest;
import com.cambiaso.ioc.dto.response.UsuarioResponse;
import com.cambiaso.ioc.exception.ResourceConflictException;
import com.cambiaso.ioc.exception.ResourceNotFoundException;
import com.cambiaso.ioc.mapper.UsuarioMapper;
import com.cambiaso.ioc.persistence.entity.AppUser;
import com.cambiaso.ioc.persistence.entity.Planta;
import com.cambiaso.ioc.persistence.repository.AppUserRepository;
import com.cambiaso.ioc.persistence.repository.AppUserSearchRepository;
import com.cambiaso.ioc.persistence.repository.PlantaRepository;
import com.cambiaso.ioc.persistence.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private AppUserSearchRepository appUserSearchRepository;
    @Mock
    private PlantaRepository plantaRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UserAdminService userAdminService;

    @BeforeEach
    void setup() {
    }

    @Test
    void create_success() {
        UsuarioCreateRequest req = new UsuarioCreateRequest();
        req.setEmail("test@example.com");
        req.setSupabaseUserId(UUID.randomUUID());
        req.setPrimerNombre("John");
        req.setPrimerApellido("Doe");

        when(appUserRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(appUserRepository.existsBySupabaseUserId(any(UUID.class))).thenReturn(false);

        AppUser saved = new AppUser();
        saved.setId(42L);
        when(appUserRepository.save(any(AppUser.class))).thenReturn(saved);

        UsuarioResponse resp = new UsuarioResponse();
        resp.setId(42);
        when(usuarioMapper.toResponse(any(AppUser.class), anyList())).thenReturn(resp);

        UsuarioResponse result = userAdminService.create(req);
        assertNotNull(result);
        assertEquals(42, result.getId());
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void create_conflict_email() {
        UsuarioCreateRequest req = new UsuarioCreateRequest();
        req.setEmail("a@b.com");
        when(appUserRepository.existsByEmailIgnoreCase("a@b.com")).thenReturn(true);
        assertThrows(ResourceConflictException.class, () -> userAdminService.create(req));
    }

    @Test
    void getById_notFound() {
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userAdminService.getById(99L));
    }

    @Test
    void update_success() {
        AppUser existing = new AppUser();
        existing.setId(5L);
        existing.setPrimerNombre("Old");
        when(appUserRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(plantaRepository.findById(1)).thenReturn(Optional.of(new Planta()));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRoleRepository.findRoleNamesByUserId(5L)).thenReturn(List.of("R"));
        when(usuarioMapper.toResponse(any(AppUser.class), anyList())).thenReturn(new UsuarioResponse());

        UsuarioUpdateRequest req = new UsuarioUpdateRequest();
        req.setPrimerNombre("New");
        req.setPrimerApellido("Name");
        req.setPlantaId(1);

        UsuarioResponse out = userAdminService.update(5L, req);
        assertNotNull(out);
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void delete_softDeletes() {
        AppUser u = new AppUser();
        u.setId(7L);
        u.setActive(true);
        when(appUserRepository.findById(7L)).thenReturn(Optional.of(u));

        userAdminService.delete(7L);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        AppUser saved = captor.getValue();
        assertFalse(saved.isActive());
        assertNotNull(saved.getDeletedAt());
    }
}
