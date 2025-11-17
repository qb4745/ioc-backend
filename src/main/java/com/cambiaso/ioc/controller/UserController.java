package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.dto.response.UsuarioResponse;
import com.cambiaso.ioc.mapper.UsuarioMapper;
import com.cambiaso.ioc.persistence.entity.AppUser;
import com.cambiaso.ioc.persistence.repository.AppUserRepository;
import com.cambiaso.ioc.persistence.repository.UserRoleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller público para operaciones de usuario autenticado.
 * No requiere roles administrativos, solo autenticación válida.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "Public user endpoints for authenticated users")
public class UserController {

    private final AppUserRepository appUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final UsuarioMapper usuarioMapper;

    /**
     * Obtiene el perfil del usuario autenticado actual.
     *
     * Este endpoint permite a cualquier usuario autenticado obtener su propio perfil
     * incluyendo sus roles asignados, sin necesidad de privilegios administrativos.
     *
     * @param jwt Token JWT del usuario autenticado (inyectado automáticamente por Spring Security)
     * @return Perfil del usuario con roles
     */
    @GetMapping("/me")
    @Operation(
        summary = "Get current user profile",
        description = "Returns the profile of the authenticated user including their assigned roles. " +
                      "This endpoint is accessible to any authenticated user without requiring admin privileges.",
        security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ResponseEntity<UsuarioResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        // Extraer supabaseUserId del subject del JWT
        String supabaseUserIdStr = jwt.getSubject();

        if (supabaseUserIdStr == null || supabaseUserIdStr.isBlank()) {
            log.warn("JWT subject (sub) is null or empty");
            return ResponseEntity.status(401).build();
        }

        UUID supabaseUserId;
        try {
            supabaseUserId = UUID.fromString(supabaseUserIdStr);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in JWT subject: {}", supabaseUserIdStr, e);
            return ResponseEntity.status(401).build();
        }

        // Buscar usuario en la base de datos
        AppUser user = appUserRepository.findBySupabaseUserId(supabaseUserId)
            .orElseGet(() -> {
                log.warn("User not found in database for supabaseUserId: {}", supabaseUserId);
                return null;
            });

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Verificar que el usuario esté activo
        if (!user.isActive()) {
            log.warn("Inactive user attempted to access profile: {}", user.getEmail());
            return ResponseEntity.status(403).build();
        }

        // Obtener roles del usuario
        List<String> roles = userRoleRepository.findRoleNamesByUserId(user.getId());

        log.debug("Retrieved profile for user: {} with roles: {}", user.getEmail(), roles);

        // Construir respuesta usando el mapper existente
        UsuarioResponse response = usuarioMapper.toResponse(user, roles);

        return ResponseEntity.ok(response);
    }
}

