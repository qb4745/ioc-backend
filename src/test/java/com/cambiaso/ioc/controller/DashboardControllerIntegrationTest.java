package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.service.MetabaseEmbeddingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")  // ← Activar perfil test para usar H2 en memoria
@DisplayName("DashboardController Integration Tests")
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetabaseEmbeddingService metabaseEmbeddingService;

    @Test
    @WithMockUser // Simula un usuario autenticado (sin roles específicos)
    @DisplayName("GET /api/v1/dashboards/{id} - Should return 200 OK for authenticated user")
    void shouldReturn200ForAuthenticatedUser() throws Exception {
        // Given
        String signedUrl = "http://localhost:3000/embed/dashboard/mock-signed-token";
        when(metabaseEmbeddingService.getSignedDashboardUrl(anyInt(), any())).thenReturn(signedUrl);

        // When & Then
        mockMvc.perform(get("/api/v1/dashboards/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.signedUrl").value(signedUrl));
    }

    @Test
    @DisplayName("GET /api/v1/dashboards/{id} - Should return 401 Unauthorized for unauthenticated user")
    void shouldReturn401ForUnauthenticatedUser() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/dashboards/5"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/dashboards/{id} - Should return 400 Bad Request for invalid dashboard ID")
    void shouldReturn400ForInvalidDashboardId() throws Exception {
        // Test con un ID fuera del rango de validación del controlador
        mockMvc.perform(get("/api/v1/dashboards/0"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/dashboards/9999999"))
            .andExpect(status().isBadRequest());
    }
}
