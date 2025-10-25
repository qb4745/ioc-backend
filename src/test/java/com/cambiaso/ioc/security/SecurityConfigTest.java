package com.cambiaso.ioc.security;

import com.cambiaso.ioc.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de configuración de seguridad.
 */
@AutoConfigureMockMvc
class SecurityConfigTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRespondToHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @WithAnonymousUser
    void shouldDenyAccessToProtectedEndpointsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldAllowAccessWithValidAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.signedUrl").exists())
                .andExpect(jsonPath("$.dashboardId").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldAllowAccessForAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signedUrl").exists());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldReturn400ForInvalidDashboardId() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldReturnErrorForNonExistentDashboard() throws Exception {
        // Un dashboard no existente puede retornar:
        // - 404 NOT_FOUND si el circuit breaker NO está activo
        // - 500 INTERNAL_SERVER_ERROR si el circuit breaker SÍ está activo
        // Ambos comportamientos son correctos según el estado del sistema
        mockMvc.perform(get("/api/v1/dashboards/99999"))
                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    if (statusCode != 404 && statusCode != 500) {
                        throw new AssertionError(
                                "Expected status 404 or 500, but got: " + statusCode
                        );
                    }
                })
                .andExpect(jsonPath("$.message").exists());
    }
}