package com.cambiaso.ioc.security;
import com.cambiaso.ioc.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RoleService roleService;
    @Test
    void shouldAllowAccessWithAdminRole() throws Exception {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("sub", "user123")
                .claim("realm_access", Map.of("roles", List.of("ADMIN")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        mockMvc.perform(get("/api/admin/roles")
                        .with(jwt().jwt(jwt)))
                .andExpect(status().isOk());
    }
    @Test
    void shouldDenyAccessWithoutAdminRole() throws Exception {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("sub", "user456")
                .claim("realm_access", Map.of("roles", List.of("USER")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        mockMvc.perform(get("/api/admin/roles")
                        .with(jwt().jwt(jwt)))
                .andExpect(status().isForbidden());
    }
    @Test
    void shouldExtractRolesFromSimpleRolesClaim() throws Exception {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("sub", "user789")
                .claim("roles", List.of("ADMIN"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        mockMvc.perform(get("/api/admin/roles")
                        .with(jwt().jwt(jwt)))
                .andExpect(status().isOk());
    }
    @Test
    void shouldDenyAccessWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void shouldAllowAccessWithDirectAuthorities() throws Exception {
        mockMvc.perform(get("/api/admin/roles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }
    @Test
    void shouldDenyAccessWithInsufficientAuthorities() throws Exception {
        mockMvc.perform(get("/api/admin/roles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }
}
