package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.AbstractIntegrationTest;
import com.cambiaso.ioc.dto.ai.DashboardExplanationRequest;
import com.cambiaso.ioc.dto.ai.DashboardExplanationResponse;
import com.cambiaso.ioc.exception.GeminiApiException;
import com.cambiaso.ioc.exception.GeminiRateLimitException;
import com.cambiaso.ioc.exception.GeminiTimeoutException;
import com.cambiaso.ioc.service.ai.DashboardExplanationService;
import com.cambiaso.ioc.service.DashboardAccessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para AiExplanationController.
 *
 * BSS-004: AiExplanationController Test Suite
 * Feature: FP-001A - Dashboard AI Explanation
 *
 * Patrón 1: Test de Controller con contexto completo (AbstractIntegrationTest)
 * - Carga contexto Spring completo (H2, seguridad, servicios)
 * - Mockea solo DashboardExplanationService (servicio específico)
 * - Usa mocks globales de GlobalTestConfiguration (WebSocket, métricas)
 * - Transaccional con rollback automático
 *
 * Referencia: TESTING_STRATEGY.md - Patrón 1: Test de Controller Simple
 */
@AutoConfigureMockMvc
class AiExplanationControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * MockBean local - específico para este test
     * El servicio de explicaciones AI se mockea porque:
     * 1. Depende de Gemini API (servicio externo)
     * 2. No queremos hacer llamadas reales a APIs externas en tests
     * 3. Queremos controlar las respuestas para escenarios específicos
     */
    @MockBean
    private DashboardExplanationService explanationService;

    /**
     * MockBean para verificación de acceso a dashboards (R4)
     * Se mockea para permitir acceso en tests sin configurar metabase.dashboards
     */
    @MockBean
    private DashboardAccessService dashboardAccessService;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void explainDashboard_POST_success_returns200() throws Exception {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
                5,
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                Map.of("turno", "DIA")
        );

        DashboardExplanationResponse mockResponse = new DashboardExplanationResponse(
                "Producción estable en junio 2025",
                List.of("5000 unidades producidas", "Juan Pérez lidera con 30%"),
                List.of("Replicar mejores prácticas de top performers"),
                List.of(),
                5,
                "Producción por Operario - Mensual",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                Map.of("turno", "DIA"),
                Instant.now(),
                false,
                1500,
                1800
        );

        when(explanationService.explainDashboard(any(DashboardExplanationRequest.class)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/ai/explain")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumenEjecutivo").value("Producción estable en junio 2025"))
                .andExpect(jsonPath("$.keyPoints.length()").value(2))
                .andExpect(jsonPath("$.insightsAccionables.length()").value(1))
                .andExpect(jsonPath("$.alertas.length()").value(0))
                .andExpect(jsonPath("$.dashboardId").value(5))
                .andExpect(jsonPath("$.fromCache").value(false))
                .andExpect(jsonPath("$.tokensUsados").value(1500));

        verify(explanationService).explainDashboard(any(DashboardExplanationRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void explainDashboard_POST_fromCache_returns200() throws Exception {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
                5,
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                Map.of()
        );

        DashboardExplanationResponse mockResponse = new DashboardExplanationResponse(
                "Cached response",
                List.of("Cached data"),
                List.of(),
                List.of(),
                5,
                "Dashboard 5",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                Map.of(),
                Instant.now(),
                true,
                1200,
                1800
        );

        when(explanationService.explainDashboard(any(DashboardExplanationRequest.class)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/ai/explain")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCache").value(true))
                .andExpect(jsonPath("$.resumenEjecutivo").value("Cached response"));

        verify(explanationService).explainDashboard(any(DashboardExplanationRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void explainDashboard_GET_withQueryParams_returns200() throws Exception {
        // Given
        DashboardExplanationResponse mockResponse = new DashboardExplanationResponse(
                "GET request response",
                List.of("Data point 1"),
                List.of(),
                List.of(),
                10,
                "Dashboard 10",
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 31),
                Map.of("turno", "NOCHE"),
                Instant.now(),
                false,
                1000,
                1800
        );

        when(explanationService.explainDashboard(any(DashboardExplanationRequest.class)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/ai/explain/10")
                        .param("fechaInicio", "2025-07-01")
                        .param("fechaFin", "2025-07-31")
                        .param("turno", "NOCHE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dashboardId").value(10))
                .andExpect(jsonPath("$.resumenEjecutivo").value("GET request response"));

        verify(explanationService).explainDashboard(any(DashboardExplanationRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void explainDashboard_GeminiTimeout_returns408() throws Exception {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
                5,
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                Map.of()
        );

        when(explanationService.explainDashboard(any(DashboardExplanationRequest.class)))
                .thenThrow(new GeminiTimeoutException("Request timeout after 90s", null));

        // When & Then
        mockMvc.perform(post("/api/v1/ai/explain")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isRequestTimeout())
                .andExpect(jsonPath("$.resumenEjecutivo").value("Request timeout - try again later"))
                .andExpect(jsonPath("$.alertas[0]").value("Error: Request timeout - try again later"));

        verify(explanationService).explainDashboard(any(DashboardExplanationRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void explainDashboard_GeminiRateLimit_returns429() throws Exception {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
                5,
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                Map.of()
        );

        when(explanationService.explainDashboard(any(DashboardExplanationRequest.class)))
                .thenThrow(new GeminiRateLimitException("Rate limit exceeded"));

        // When & Then
        mockMvc.perform(post("/api/v1/ai/explain")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.resumenEjecutivo").value("Rate limit exceeded - please wait"));

        verify(explanationService).explainDashboard(any(DashboardExplanationRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void explainDashboard_GeminiApiException_returns503() throws Exception {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
                5,
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                Map.of()
        );

        when(explanationService.explainDashboard(any(DashboardExplanationRequest.class)))
                .thenThrow(new GeminiApiException("Gemini API unavailable"));

        // When & Then
        mockMvc.perform(post("/api/v1/ai/explain")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.resumenEjecutivo").value("AI service temporarily unavailable"));

        verify(explanationService).explainDashboard(any(DashboardExplanationRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void explainDashboard_InvalidDateRange_returns400() throws Exception {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
                5,
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                Map.of()
        );

        when(explanationService.explainDashboard(any(DashboardExplanationRequest.class)))
                .thenThrow(new IllegalArgumentException("Date range exceeds 12 months"));

        // When & Then
        mockMvc.perform(post("/api/v1/ai/explain")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resumenEjecutivo").value("Date range exceeds 12 months"));

        verify(explanationService).explainDashboard(any(DashboardExplanationRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void explainDashboard_UnexpectedException_returns500() throws Exception {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
                5,
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                Map.of()
        );

        when(explanationService.explainDashboard(any(DashboardExplanationRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(post("/api/v1/ai/explain")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.resumenEjecutivo").value("Internal server error"));

        verify(explanationService).explainDashboard(any(DashboardExplanationRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void explainDashboard_GET_missingRequiredParams_returns400() throws Exception {
        // When & Then - Missing fechaInicio
        // Spring validation lanza ConstraintViolationException que se convierte en 400
        mockMvc.perform(get("/api/v1/ai/explain/5")
                        .param("fechaFin", "2025-06-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameters"));

        // When & Then - Missing fechaFin
        mockMvc.perform(get("/api/v1/ai/explain/5")
                        .param("fechaInicio", "2025-06-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameters"));

        verifyNoInteractions(explanationService);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void explainDashboard_GET_invalidDashboardId_returns400() throws Exception {
        // When & Then - Dashboard ID too low
        mockMvc.perform(get("/api/v1/ai/explain/0")
                        .param("fechaInicio", "2025-06-01")
                        .param("fechaFin", "2025-06-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameters"));

        // When & Then - Dashboard ID too high
        mockMvc.perform(get("/api/v1/ai/explain/9999999")
                        .param("fechaInicio", "2025-06-01")
                        .param("fechaFin", "2025-06-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameters"));

        verifyNoInteractions(explanationService);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void healthCheck_returns200() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/ai/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("AI Explanation Service"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(explanationService);
    }

    @Test
    void explainDashboard_withoutAuthentication_returns401() throws Exception {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
                5,
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                Map.of()
        );

        // When & Then
        mockMvc.perform(post("/api/v1/ai/explain")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(explanationService);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void explainDashboard_GET_extractsFiltersCorrectly() throws Exception {
        // Given
        DashboardExplanationResponse mockResponse = new DashboardExplanationResponse(
                "Response with filters",
                List.of(),
                List.of(),
                List.of(),
                5,
                "Dashboard 5",
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 6, 30),
                Map.of("turno", "DIA", "maquina", "M001"),
                Instant.now(),
                false,
                800,
                1800
        );

        when(explanationService.explainDashboard(any(DashboardExplanationRequest.class)))
                .thenReturn(mockResponse);

        // When & Then
        // CORRECCIÓN: El JSON usa "filtrosAplicados", no "filtros"
        mockMvc.perform(get("/api/v1/ai/explain/5")
                        .param("fechaInicio", "2025-06-01")
                        .param("fechaFin", "2025-06-30")
                        .param("turno", "DIA")
                        .param("maquina", "M001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filtrosAplicados.turno").value("DIA"))
                .andExpect(jsonPath("$.filtrosAplicados.maquina").value("M001"));

        verify(explanationService).explainDashboard(any(DashboardExplanationRequest.class));
    }
}
