package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para DashboardController.
 * Prueba el controller con el servicio real de Metabase.
 */
@AutoConfigureMockMvc
@DisplayName("DashboardController Integration Tests")
class DashboardControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/dashboards/{id} - 200 OK con URL firmada")
    void shouldReturn200WithSignedUrlForAuthenticatedUser() throws Exception {
        // Dashboard ID 1 está configurado en application-test.properties
        mockMvc.perform(get("/api/v1/dashboards/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.signedUrl").exists())
            .andExpect(jsonPath("$.signedUrl", startsWith("http://localhost:3000/embed/dashboard/")))
            .andExpect(jsonPath("$.signedUrl", containsString("bordered=true")))
            .andExpect(jsonPath("$.dashboardId").value(1))
            .andExpect(jsonPath("$.expiresInMinutes").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/dashboards/{id} - 401 Unauthorized sin autenticación")
    void shouldReturn401ForUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/dashboards/{id} - 400 Bad Request para IDs inválidos")
    void shouldReturn400ForInvalidIds() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/0"))
            .andExpect(status().isBadRequest());
        
        mockMvc.perform(get("/api/v1/dashboards/9999999"))
            .andExpect(status().isBadRequest());
    }
}
