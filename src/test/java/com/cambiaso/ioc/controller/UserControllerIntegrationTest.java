package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.AbstractIntegrationTest;
import com.cambiaso.ioc.persistence.entity.AppUser;
import com.cambiaso.ioc.persistence.entity.Role;
import com.cambiaso.ioc.persistence.entity.UserRole;
import com.cambiaso.ioc.persistence.entity.UserRoleKey;
import com.cambiaso.ioc.persistence.repository.AppUserRepository;
import com.cambiaso.ioc.persistence.repository.RoleRepository;
import com.cambiaso.ioc.persistence.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para UserController.
 * Valida el endpoint /api/v1/users/me con datos reales en base de datos H2.
 *
 * Extiende AbstractIntegrationTest para heredar:
 * - Configuración H2 con tablas creadas automáticamente
 * - Perfil 'test' activado
 * - Mocks globales (NotificationService, SimpMessagingTemplate, etc.)
 * - Transaccionalidad con rollback automático
 */
@AutoConfigureMockMvc
class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private UUID analistaSupabaseId;
    private UUID adminSupabaseId;
    private AppUser analistaUser;
    private AppUser adminUser;

    @BeforeEach
    void setUp() {
        // Crear roles si no existen
        Role analistaRole = roleRepository.findByNameIgnoreCase("ANALISTA")
            .orElseGet(() -> {
                Role r = new Role();
                r.setName("ANALISTA");
                r.setDescription("Analista de datos");
                r.setCreatedAt(OffsetDateTime.now());
                return roleRepository.save(r);
            });

        Role adminRole = roleRepository.findByNameIgnoreCase("ADMIN")
            .orElseGet(() -> {
                Role r = new Role();
                r.setName("ADMIN");
                r.setDescription("Administrador del sistema");
                r.setCreatedAt(OffsetDateTime.now());
                return roleRepository.save(r);
            });

        Role gerenteRole = roleRepository.findByNameIgnoreCase("GERENTE")
            .orElseGet(() -> {
                Role r = new Role();
                r.setName("GERENTE");
                r.setDescription("Gerente");
                r.setCreatedAt(OffsetDateTime.now());
                return roleRepository.save(r);
            });

        // Crear usuario ANALISTA
        analistaSupabaseId = UUID.randomUUID();
        analistaUser = new AppUser();
        analistaUser.setSupabaseUserId(analistaSupabaseId);
        analistaUser.setEmail("analista@test.com");
        analistaUser.setPrimerNombre("María");
        analistaUser.setSegundoNombre("José");
        analistaUser.setPrimerApellido("González");
        analistaUser.setSegundoApellido("Silva");
        analistaUser.setCentroCosto("CC-001");
        analistaUser.setFechaContrato(LocalDate.of(2024, 1, 15));
        analistaUser.setActive(true);
        analistaUser.setCreatedAt(OffsetDateTime.now());
        analistaUser.setUpdatedAt(OffsetDateTime.now());
        analistaUser = appUserRepository.save(analistaUser);

        // Asignar rol ANALISTA
        UserRoleKey analistaKey = new UserRoleKey();
        analistaKey.setUserId(analistaUser.getId());
        analistaKey.setRoleId(analistaRole.getId());
        UserRole analistaUserRole = new UserRole();
        analistaUserRole.setId(analistaKey);
        analistaUserRole.setUser(analistaUser);
        analistaUserRole.setRole(analistaRole);
        analistaUserRole.setAssignedAt(OffsetDateTime.now());
        userRoleRepository.save(analistaUserRole);

        // Crear usuario ADMIN con múltiples roles
        adminSupabaseId = UUID.randomUUID();
        adminUser = new AppUser();
        adminUser.setSupabaseUserId(adminSupabaseId);
        adminUser.setEmail("admin@test.com");
        adminUser.setPrimerNombre("Carlos");
        adminUser.setPrimerApellido("Ramírez");
        adminUser.setCentroCosto("CC-ADMIN");
        adminUser.setActive(true);
        adminUser.setCreatedAt(OffsetDateTime.now());
        adminUser.setUpdatedAt(OffsetDateTime.now());
        adminUser = appUserRepository.save(adminUser);

        // Asignar múltiples roles al admin
        for (Role role : new Role[]{adminRole, analistaRole, gerenteRole}) {
            UserRoleKey key = new UserRoleKey();
            key.setUserId(adminUser.getId());
            key.setRoleId(role.getId());
            UserRole ur = new UserRole();
            ur.setId(key);
            ur.setUser(adminUser);
            ur.setRole(role);
            ur.setAssignedAt(OffsetDateTime.now());
            userRoleRepository.save(ur);
        }
    }

    @Test
    void getCurrentUser_analistaUser_returnsProfileWithAnalistaRole() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                .with(jwt().jwt(builder -> builder
                    .subject(analistaSupabaseId.toString())
                    .claim("email", "analista@test.com"))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(analistaUser.getId()))
            .andExpect(jsonPath("$.email").value("analista@test.com"))
            .andExpect(jsonPath("$.fullName").value("María José González Silva"))
            .andExpect(jsonPath("$.centroCosto").value("CC-001"))
            .andExpect(jsonPath("$.isActive").value(true))
            .andExpect(jsonPath("$.roles").isArray())
            .andExpect(jsonPath("$.roles", hasSize(1)))
            .andExpect(jsonPath("$.roles[0]").value("ANALISTA"));
    }

    @Test
    void getCurrentUser_adminUser_returnsProfileWithMultipleRoles() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                .with(jwt().jwt(builder -> builder
                    .subject(adminSupabaseId.toString())
                    .claim("email", "admin@test.com"))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(adminUser.getId()))
            .andExpect(jsonPath("$.email").value("admin@test.com"))
            .andExpect(jsonPath("$.fullName").value("Carlos Ramírez"))
            .andExpect(jsonPath("$.isActive").value(true))
            .andExpect(jsonPath("$.roles").isArray())
            .andExpect(jsonPath("$.roles", hasSize(3)))
            .andExpect(jsonPath("$.roles", containsInAnyOrder("ADMIN", "ANALISTA", "GERENTE")));
    }

    @Test
    void getCurrentUser_nonExistentUser_returns404() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/users/me")
                .with(jwt().jwt(builder -> builder
                    .subject(randomId.toString()))))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void getCurrentUser_inactiveUser_returns403() throws Exception {
        // Desactivar usuario
        analistaUser.setActive(false);
        appUserRepository.save(analistaUser);

        mockMvc.perform(get("/api/v1/users/me")
                .with(jwt().jwt(builder -> builder
                    .subject(analistaSupabaseId.toString()))))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    void getCurrentUser_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
}
