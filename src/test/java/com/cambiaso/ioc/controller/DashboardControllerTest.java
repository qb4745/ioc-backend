package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.service.MetabaseEmbeddingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para DashboardController.
 *
 * NOTA: Usamos @SpringBootTest con @AutoConfigureMockMvc en lugar de @WebMvcTest
 * porque la aplicación tiene @EnableJpaRepositories en la clase principal,
 * lo cual hace imposible usar @WebMvcTest sin cargar toda la capa de persistencia.
 *
 * Esta configuración:
 * - Carga el contexto completo de Spring Boot
 * - Configura MockMvc automáticamente
 * - Mockea el servicio para evitar dependencias externas (Metabase)
 * - Usa H2 en memoria (configurado en application-test.properties)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetabaseEmbeddingService metabaseEmbeddingService;

    @BeforeEach
    void setUp() {
        when(metabaseEmbeddingService.getSignedDashboardUrl(eq(5), any()))
            .thenReturn("http://localhost:3000/embed/dashboard/abc123");
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn200WithSignedUrlForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/{dashboardId}", 5))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.signedUrl").value("http://localhost:3000/embed/dashboard/abc123"));

        verify(metabaseEmbeddingService, times(1)).getSignedDashboardUrl(eq(5), any());
    }

    @Test
    void shouldReturn401ForUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/{dashboardId}", 5))
            .andExpect(status().isUnauthorized());

        verify(metabaseEmbeddingService, never()).getSignedDashboardUrl(anyInt(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn400ForInvalidDashboardId() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/{dashboardId}", 0))
            .andExpect(status().isBadRequest());

        verify(metabaseEmbeddingService, never()).getSignedDashboardUrl(anyInt(), any());
    }
}
