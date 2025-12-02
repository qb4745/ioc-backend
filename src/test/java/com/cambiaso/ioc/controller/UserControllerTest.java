package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.AbstractIntegrationTest;
import com.cambiaso.ioc.dto.response.UsuarioResponse;
import com.cambiaso.ioc.mapper.UsuarioMapper;
import com.cambiaso.ioc.persistence.entity.AppUser;
import com.cambiaso.ioc.persistence.repository.AppUserRepository;
import com.cambiaso.ioc.persistence.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test unitario del UserController usando mocks.
 *
 * Sigue el patrón de Testing Strategy del proyecto:
 * - Extiende AbstractIntegrationTest para heredar configuración H2 y mocks globales
 * - Usa @AutoConfigureMockMvc para inyectar MockMvc
 * - Mockea repositorios y mapper para aislar la lógica del controller
 * - Transaccional con rollback automático
 *
 * Nota: Aunque usa mocks, es técnicamente un test de integración porque carga
 * el contexto completo de Spring. Para tests verdaderamente unitarios sin Spring,
 * considera usar solo Mockito sin anotaciones de Spring.
 */
@AutoConfigureMockMvc
class UserControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppUserRepository appUserRepository;

    @MockBean
    private UserRoleRepository userRoleRepository;

    @MockBean
    private UsuarioMapper usuarioMapper;

    private UUID testSupabaseUserId;
    private AppUser testUser;
    private UsuarioResponse testResponse;

    @BeforeEach
    void setUp() {
        testSupabaseUserId = UUID.randomUUID();

        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setSupabaseUserId(testSupabaseUserId);
        testUser.setEmail("test@example.com");
        testUser.setPrimerNombre("John");
        testUser.setPrimerApellido("Doe");
        testUser.setActive(true);
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser.setUpdatedAt(OffsetDateTime.now());

        testResponse = new UsuarioResponse();
        testResponse.setId(1L);
        testResponse.setEmail("test@example.com");
        testResponse.setFullName("John Doe");
        testResponse.setIsActive(true);
        testResponse.setRoles(Arrays.asList("ANALISTA", "GERENTE"));
    }

    @Test
    void getCurrentUser_withValidToken_returnsUserProfile() throws Exception {
        // Arrange
        List<String> roles = Arrays.asList("ANALISTA", "GERENTE");

        when(appUserRepository.findBySupabaseUserId(testSupabaseUserId))
            .thenReturn(Optional.of(testUser));
        when(userRoleRepository.findRoleNamesByUserId(1L))
            .thenReturn(roles);
        when(usuarioMapper.toResponse(testUser, roles))
            .thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/me")
                .with(jwt().jwt(builder -> builder
                    .subject(testSupabaseUserId.toString())
                    .claim("email", "test@example.com"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.fullName").value("John Doe"))
            .andExpect(jsonPath("$.isActive").value(true))
            .andExpect(jsonPath("$.roles").isArray())
            .andExpect(jsonPath("$.roles[0]").value("ANALISTA"))
            .andExpect(jsonPath("$.roles[1]").value("GERENTE"));
    }

    @Test
    void getCurrentUser_withAdminUser_returnsAdminProfile() throws Exception {
        // Arrange
        List<String> adminRoles = Arrays.asList("ADMIN", "ANALISTA", "GERENTE");
        testResponse.setRoles(adminRoles);

        when(appUserRepository.findBySupabaseUserId(testSupabaseUserId))
            .thenReturn(Optional.of(testUser));
        when(userRoleRepository.findRoleNamesByUserId(1L))
            .thenReturn(adminRoles);
        when(usuarioMapper.toResponse(testUser, adminRoles))
            .thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/me")
                .with(jwt().jwt(builder -> builder
                    .subject(testSupabaseUserId.toString())
                    .claim("email", "admin@example.com"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roles").isArray())
            .andExpect(jsonPath("$.roles.length()").value(3))
            .andExpect(jsonPath("$.roles[?(@=='ADMIN')]").exists());
    }

    @Test
    void getCurrentUser_userNotFound_returns404() throws Exception {
        // Arrange
        when(appUserRepository.findBySupabaseUserId(any(UUID.class)))
            .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/me")
                .with(jwt().jwt(builder -> builder
                    .subject(UUID.randomUUID().toString()))))
            .andExpect(status().isNotFound());
    }

    @Test
    void getCurrentUser_inactiveUser_returns403() throws Exception {
        // Arrange
        testUser.setActive(false);

        when(appUserRepository.findBySupabaseUserId(testSupabaseUserId))
            .thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/me")
                .with(jwt().jwt(builder -> builder
                    .subject(testSupabaseUserId.toString()))))
            .andExpect(status().isForbidden());
    }

    @Test
    void getCurrentUser_invalidUuidInToken_returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/me")
                .with(jwt().jwt(builder -> builder
                    .subject("invalid-uuid-format"))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUser_noSubjectInToken_returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/me")
                .with(jwt().jwt(builder -> builder
                    .claim("email", "test@example.com")))) // sin subject
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUser_userWithNoRoles_returnsEmptyRolesList() throws Exception {
        // Arrange
        List<String> emptyRoles = Collections.emptyList();
        testResponse.setRoles(emptyRoles);

        when(appUserRepository.findBySupabaseUserId(testSupabaseUserId))
            .thenReturn(Optional.of(testUser));
        when(userRoleRepository.findRoleNamesByUserId(1L))
            .thenReturn(emptyRoles);
        when(usuarioMapper.toResponse(testUser, emptyRoles))
            .thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/me")
                .with(jwt().jwt(builder -> builder
                    .subject(testSupabaseUserId.toString()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roles").isArray())
            .andExpect(jsonPath("$.roles").isEmpty());
    }

    @Test
    void getCurrentUser_withoutAuthentication_returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isUnauthorized());
    }
}
