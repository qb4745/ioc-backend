package com.cambiaso.ioc.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("DashboardController Integration Tests (Real Service)")
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser // Simula un usuario autenticado
    @DisplayName("GET /api/v1/dashboards/{id} - 200 OK con servicio real y URL firmada")
    void shouldReturn200WithSignedUrlForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.signedUrl").exists())
            .andExpect(jsonPath("$.signedUrl", startsWith("http://localhost:3000/embed/dashboard/")))
            .andExpect(jsonPath("$.signedUrl", containsString("bordered=true")))
            .andExpect(jsonPath("$.dashboardId").value(5))
            .andExpect(jsonPath("$.expiresInMinutes").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/dashboards/{id} - 401 Unauthorized sin autenticación")
    void shouldReturn401ForUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/5"))
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
