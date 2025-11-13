package com.cambiaso.ioc.service.ai;

import com.cambiaso.ioc.AbstractIntegrationTest;
import com.cambiaso.ioc.dto.ai.DashboardExplanationRequest;
import com.cambiaso.ioc.dto.ai.DashboardExplanationResponse;
import com.cambiaso.ioc.dto.analytics.*;
import com.cambiaso.ioc.exception.GeminiApiException;
import com.cambiaso.ioc.exception.GeminiRateLimitException;
import com.cambiaso.ioc.exception.GeminiTimeoutException;
import com.cambiaso.ioc.persistence.repository.DashboardAnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests para DashboardExplanationService.
 *
 * Cobertura:
 * - Flujo completo con cache miss
 * - Cache hit scenario
 * - Cálculo de TTL dinámico
 * - Anonimización de PII
 * - Manejo de errores de Gemini
 * - Parsing de respuestas
 * - Construcción de cache keys
 */
class DashboardExplanationServiceTest extends AbstractIntegrationTest {

    @Autowired
    private DashboardExplanationService service;

    @MockBean
    private DashboardAnalyticsRepository analyticsRepository;

    @MockBean
    private GeminiApiClient geminiClient;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setup() {
        // Limpiar cache antes de cada test
        if (cacheManager.getCache("aiExplanations") != null) {
            cacheManager.getCache("aiExplanations").clear();
        }
    }

    @Test
    void explainDashboard_cacheMiss_generatesExplanation() {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 30),
            Map.of()
        );

        // Mock repository responses
        when(analyticsRepository.fetchTotals(any(), any()))
            .thenReturn(new TotalsDto(100L, new BigDecimal("5000"), new BigDecimal("25000")));

        when(analyticsRepository.fetchTopOperarios(any(), any()))
            .thenReturn(List.of(
                new TopOperarioDto("Juan Pérez", 12345L, new BigDecimal("1500"), 30),
                new TopOperarioDto("María García", 12346L, new BigDecimal("1200"), 25)
            ));

        when(analyticsRepository.fetchDistribucionTurno(any(), any()))
            .thenReturn(List.of(
                new TurnoDistributionDto("Día", new BigDecimal("3000"), 60),
                new TurnoDistributionDto("Noche", new BigDecimal("2000"), 40)
            ));

        when(analyticsRepository.fetchTopMaquinas(any(), any()))
            .thenReturn(List.of(
                new TopMachineDto("Máquina A", "MA-001", new BigDecimal("2500"), 50),
                new TopMachineDto("Máquina B", "MA-002", new BigDecimal("2000"), 40)
            ));

        when(analyticsRepository.fetchTendenciaDiaria(any(), any()))
            .thenReturn(List.of(
                new DailyTrendPoint(LocalDate.of(2025, 6, 1), new BigDecimal("150"), 3),
                new DailyTrendPoint(LocalDate.of(2025, 6, 2), new BigDecimal("160"), 4)
            ));

        // Mock Gemini response
        String mockGeminiResponse = """
            {
              "resumenEjecutivo": "Producción estable con incremento del 5% respecto al mes anterior. Turnos balanceados.",
              "keyPoints": [
                "5000 unidades producidas en junio 2025",
                "Juan Pérez lidera con 1500 unidades (30% del total)",
                "Turno Día supera al turno Noche en 50%"
              ],
              "insightsAccionables": [
                "Replicar mejores prácticas de Juan Pérez al resto del equipo",
                "Analizar causas de menor productividad en turno Noche"
              ],
              "alertas": []
            }
            """;

        when(geminiClient.callGemini(anyString())).thenReturn(mockGeminiResponse);
        when(geminiClient.estimateTokens(anyString())).thenReturn(1500);

        // When
        DashboardExplanationResponse response = service.explainDashboard(request);

        // Then
        assertNotNull(response);
        assertFalse(response.fromCache());
        assertEquals("Producción estable con incremento del 5% respecto al mes anterior. Turnos balanceados.",
            response.resumenEjecutivo());
        assertEquals(3, response.keyPoints().size());
        assertEquals(2, response.insightsAccionables().size());
        assertTrue(response.alertas().isEmpty());
        assertEquals(5, response.dashboardId());
        assertEquals(LocalDate.of(2025, 6, 1), response.fechaInicio());
        assertEquals(LocalDate.of(2025, 6, 30), response.fechaFin());
        assertEquals(1500, response.tokensUsados());

        // Verify interactions
        verify(analyticsRepository).fetchTotals(any(), any());
        verify(analyticsRepository).fetchTopOperarios(any(), any());
        verify(analyticsRepository).fetchDistribucionTurno(any(), any());
        verify(analyticsRepository).fetchTopMaquinas(any(), any());
        verify(analyticsRepository).fetchTendenciaDiaria(any(), any());
        verify(geminiClient).callGemini(anyString());
    }

    @Test
    void explainDashboard_cacheHit_returnsFromCache() {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 30),
            Map.of()
        );

        // Mock para primer request
        mockRepositoryData();
        String mockGeminiResponse = """
            {
              "resumenEjecutivo": "Test resumen",
              "keyPoints": ["Punto 1"],
              "insightsAccionables": ["Insight 1"],
              "alertas": []
            }
            """;
        when(geminiClient.callGemini(anyString())).thenReturn(mockGeminiResponse);
        when(geminiClient.estimateTokens(anyString())).thenReturn(1000);

        // Primer request (cache miss)
        DashboardExplanationResponse firstResponse = service.explainDashboard(request);
        assertFalse(firstResponse.fromCache());

        // When - Segundo request (cache hit)
        DashboardExplanationResponse secondResponse = service.explainDashboard(request);

        // Then
        assertNotNull(secondResponse);
        assertTrue(secondResponse.fromCache());
        assertEquals(firstResponse.resumenEjecutivo(), secondResponse.resumenEjecutivo());

        // Gemini solo debe haberse llamado una vez (primer request)
        verify(geminiClient, times(1)).callGemini(anyString());
    }

    @Test
    void calculateCacheTTL_historicalData_returns24Hours() {
        // Given
        LocalDate fi = LocalDate.now().minusDays(30);
        LocalDate ff = LocalDate.now().minusDays(1);

        // When
        int ttl = service.calculateCacheTTL(fi, ff);

        // Then
        assertEquals(86400, ttl); // 24 hours
    }

    @Test
    void calculateCacheTTL_currentData_returns30Minutes() {
        // Given
        LocalDate fi = LocalDate.now().minusDays(7);
        LocalDate ff = LocalDate.now();

        // When
        int ttl = service.calculateCacheTTL(fi, ff);

        // Then
        assertEquals(1800, ttl); // 30 minutes
    }

    @Test
    void calculateCacheTTL_futureData_returns30Minutes() {
        // Given
        LocalDate fi = LocalDate.now();
        LocalDate ff = LocalDate.now().plusDays(7);

        // When
        int ttl = service.calculateCacheTTL(fi, ff);

        // Then
        assertEquals(1800, ttl); // 30 minutes
    }

    @Test
    void explainDashboard_withGeminiTimeout_throwsTimeoutException() {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 30),
            Map.of()
        );

        mockRepositoryData();
        when(geminiClient.callGemini(anyString()))
            .thenThrow(new GeminiTimeoutException("Timeout after 90s", null));

        // When/Then
        assertThrows(GeminiTimeoutException.class, () ->
            service.explainDashboard(request)
        );
    }

    @Test
    void explainDashboard_withGeminiRateLimit_throwsRateLimitException() {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 30),
            Map.of()
        );

        mockRepositoryData();
        when(geminiClient.callGemini(anyString()))
            .thenThrow(new GeminiRateLimitException("Rate limit exceeded"));

        // When/Then
        assertThrows(GeminiRateLimitException.class, () ->
            service.explainDashboard(request)
        );
    }

    @Test
    void explainDashboard_withInvalidGeminiJson_returnsFallbackResponse() {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 30),
            Map.of()
        );

        mockRepositoryData();
        when(geminiClient.callGemini(anyString()))
            .thenReturn("Invalid JSON response without proper structure");
        when(geminiClient.estimateTokens(anyString())).thenReturn(1000);

        // When
        DashboardExplanationResponse response = service.explainDashboard(request);

        // Then
        assertNotNull(response);
        assertTrue(response.resumenEjecutivo().contains("No se pudo generar el análisis automático"));
        assertTrue(response.alertas().stream().anyMatch(a -> a.contains("Error en el servicio de IA")));
    }

    @Test
    void explainDashboard_withMissingRequiredFields_returnsFallbackResponse() {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 30),
            Map.of()
        );

        mockRepositoryData();
        // JSON sin campo requerido 'keyPoints'
        String invalidResponse = """
            {
              "resumenEjecutivo": "Test",
              "insightsAccionables": [],
              "alertas": []
            }
            """;
        when(geminiClient.callGemini(anyString())).thenReturn(invalidResponse);
        when(geminiClient.estimateTokens(anyString())).thenReturn(1000);

        // When
        DashboardExplanationResponse response = service.explainDashboard(request);

        // Then
        assertNotNull(response);
        assertTrue(response.resumenEjecutivo().contains("No se pudo generar el análisis automático"));
    }

    @Test
    void explainDashboard_withFilters_generatesCacheKeyWithHash() {
        // Given
        Map<String, String> filters = Map.of(
            "turno", "Día",
            "maquina", "MA-001"
        );

        DashboardExplanationRequest request1 = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 30),
            filters
        );

        DashboardExplanationRequest request2 = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 30),
            Map.of("maquina", "MA-001", "turno", "Día") // Mismo orden diferente
        );

        mockRepositoryData();
        String mockResponse = """
            {
              "resumenEjecutivo": "Test",
              "keyPoints": ["1"],
              "insightsAccionables": [],
              "alertas": []
            }
            """;
        when(geminiClient.callGemini(anyString())).thenReturn(mockResponse);
        when(geminiClient.estimateTokens(anyString())).thenReturn(1000);

        // When
        DashboardExplanationResponse response1 = service.explainDashboard(request1);
        DashboardExplanationResponse response2 = service.explainDashboard(request2);

        // Then
        assertFalse(response1.fromCache());
        assertTrue(response2.fromCache()); // Mismo hash de filtros

        // Gemini solo llamado una vez
        verify(geminiClient, times(1)).callGemini(anyString());
    }

    @Test
    void explainDashboard_withDifferentFilters_generatesDifferentCacheKeys() {
        // Given
        DashboardExplanationRequest request1 = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 30),
            Map.of("turno", "Día")
        );

        DashboardExplanationRequest request2 = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 30),
            Map.of("turno", "Noche")
        );

        mockRepositoryData();
        String mockResponse = """
            {
              "resumenEjecutivo": "Test",
              "keyPoints": ["1"],
              "insightsAccionables": [],
              "alertas": []
            }
            """;
        when(geminiClient.callGemini(anyString())).thenReturn(mockResponse);
        when(geminiClient.estimateTokens(anyString())).thenReturn(1000);

        // When
        DashboardExplanationResponse response1 = service.explainDashboard(request1);
        DashboardExplanationResponse response2 = service.explainDashboard(request2);

        // Then
        assertFalse(response1.fromCache());
        assertFalse(response2.fromCache()); // Filtros diferentes

        // Gemini llamado dos veces
        verify(geminiClient, times(2)).callGemini(anyString());
    }

    @Test
    void explainDashboard_withGeminiApiException_propagatesException() {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 30),
            Map.of()
        );

        mockRepositoryData();
        when(geminiClient.callGemini(anyString()))
            .thenThrow(new GeminiApiException("API error"));

        // When/Then
        assertThrows(GeminiApiException.class, () ->
            service.explainDashboard(request)
        );
    }

    @Test
    void explainDashboard_withEmptyData_stillGeneratesResponse() {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
            5,
            LocalDate.of(2025, 6, 1),
            LocalDate.of(2025, 6, 30),
            Map.of()
        );

        // Mock con datos vacíos
        when(analyticsRepository.fetchTotals(any(), any()))
            .thenReturn(new TotalsDto(0L, BigDecimal.ZERO, BigDecimal.ZERO));
        when(analyticsRepository.fetchTopOperarios(any(), any())).thenReturn(List.of());
        when(analyticsRepository.fetchDistribucionTurno(any(), any())).thenReturn(List.of());
        when(analyticsRepository.fetchTopMaquinas(any(), any())).thenReturn(List.of());
        when(analyticsRepository.fetchTendenciaDiaria(any(), any())).thenReturn(List.of());

        String mockResponse = """
            {
              "resumenEjecutivo": "No hay datos de producción para el período seleccionado",
              "keyPoints": ["Sin registros de producción"],
              "insightsAccionables": ["Verificar conectividad con sistema SAP"],
              "alertas": ["⚠️ Sin datos de producción"]
            }
            """;
        when(geminiClient.callGemini(anyString())).thenReturn(mockResponse);
        when(geminiClient.estimateTokens(anyString())).thenReturn(500);

        // When
        DashboardExplanationResponse response = service.explainDashboard(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.keyPoints().size());
        assertEquals(1, response.alertas().size());
    }

    // Helper method
    private void mockRepositoryData() {
        when(analyticsRepository.fetchTotals(any(), any()))
            .thenReturn(new TotalsDto(100L, new BigDecimal("5000"), new BigDecimal("25000")));
        when(analyticsRepository.fetchTopOperarios(any(), any()))
            .thenReturn(List.of(new TopOperarioDto("Test", 123L, new BigDecimal("1000"), 10)));
        when(analyticsRepository.fetchDistribucionTurno(any(), any()))
            .thenReturn(List.of(new TurnoDistributionDto("Día", new BigDecimal("5000"), 100)));
        when(analyticsRepository.fetchTopMaquinas(any(), any()))
            .thenReturn(List.of(new TopMachineDto("Máquina A", "MA-001", new BigDecimal("2500"), 50)));
        when(analyticsRepository.fetchTendenciaDiaria(any(), any()))
            .thenReturn(List.of(new DailyTrendPoint(LocalDate.now(), new BigDecimal("150"), 3)));
    }
}

