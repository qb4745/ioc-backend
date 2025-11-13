package com.cambiaso.ioc.service.ai;

import com.cambiaso.ioc.dto.ai.DashboardExplanationRequest;
import com.cambiaso.ioc.dto.ai.DashboardExplanationResponse;
import com.cambiaso.ioc.dto.ai.GeminiJsonResponse;
import com.cambiaso.ioc.dto.analytics.*;
import com.cambiaso.ioc.exception.GeminiRateLimitException;
import com.cambiaso.ioc.exception.GeminiTimeoutException;
import com.cambiaso.ioc.persistence.repository.DashboardAnalyticsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Servicio para generar explicaciones de dashboards usando IA (Gemini).
 * Coordina consultas analíticas, prompts, cache y auditoría.
 *
 * BSS-003: DashboardExplanationService
 * Feature: FP-001A - Dashboard AI Explanation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardExplanationService {

    private final DashboardAnalyticsRepository analyticsRepository;
    private final GeminiApiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;
    private final MeterRegistry meterRegistry;

    @Value("classpath:prompts/system-prompt.txt")
    private Resource systemPromptResource;

    @Value("classpath:prompts/context.yaml")
    private Resource contextResource;

    @Value("${ai.explanation.send-pii:false}")
    private boolean sendPiiToGemini;

    @Value("${ai.explanation.cache-name:aiExplanations}")
    private String cacheName;

    /**
     * Record interno para agrupar datos analíticos.
     */
    private record AnalyticsData(
        TotalsDto totals,
        List<TopOperarioDto> topOperarios,
        List<TurnoDistributionDto> distribucionTurno,
        List<TopMachineDto> topMaquinas,
        List<DailyTrendPoint> tendenciaDiaria
    ) {}

    /**
     * Genera una explicación ejecutiva del dashboard para el rango de fechas dado.
     *
     * Flujo de 8 fases:
     * 1. Verificar cache (key compuesta por dashboard + fechas + filtros)
     * 2. Si miss: consultar datos agregados (5 queries SQL)
     * 3. Anonimizar PII si configurado
     * 4. Construir prompt estructurado con system + context + data
     * 5. Invocar Gemini API con timeout 90s
     * 6. Parsear respuesta JSON
     * 7. Guardar en cache con TTL dinámico
     * 8. Auditar request (logs estructurados + métricas)
     *
     * @param request DTO con dashboardId, fechaInicio, fechaFin, filtros
     * @return Respuesta con resumen, keyPoints, insights, alertas y metadata
     * @throws IllegalArgumentException si validaciones de negocio fallan
     * @throws com.cambiaso.ioc.exception.GeminiApiException si falla invocación a IA después de retries
     * @throws GeminiTimeoutException si excede timeout de 90s
     */
    public DashboardExplanationResponse explainDashboard(DashboardExplanationRequest request) {
        long startTime = System.currentTimeMillis();
        Timer.Sample timerSample = Timer.start(meterRegistry);

        log.info("Starting AI explanation generation for dashboard: {}, range: {} to {}",
            request.dashboardId(), request.fechaInicio(), request.fechaFin());

        try {
            // FASE 1: Verificar cache
            String cacheKey = buildCacheKey(request);
            Cache cache = cacheManager.getCache(cacheName);

            if (cache != null) {
                Cache.ValueWrapper cached = cache.get(cacheKey);
                if (cached != null) {
                    log.info("Cache HIT for key: {}", cacheKey);
                    meterRegistry.counter("ai.explanation.cache", "result", "hit").increment();

                    DashboardExplanationResponse response = (DashboardExplanationResponse) cached.get();
                    timerSample.stop(Timer.builder("ai.explanation.duration")
                        .tag("phase", "total")
                        .tag("cache", "hit")
                        .register(meterRegistry));

                    return response.withFromCache(true);
                }
            }

            log.info("Cache MISS - Starting data aggregation and AI generation");
            meterRegistry.counter("ai.explanation.cache", "result", "miss").increment();

            // FASE 2: Consultar datos agregados
            long queryStart = System.currentTimeMillis();
            AnalyticsData data = fetchAnalyticsData(request);
            long queryDuration = System.currentTimeMillis() - queryStart;

            meterRegistry.timer("ai.explanation.duration", "phase", "queries")
                .record(queryDuration, TimeUnit.MILLISECONDS);
            log.debug("Queries completed in {}ms", queryDuration);

            // FASE 3: Anonimizar PII si configurado
            if (!sendPiiToGemini) {
                data = anonymizeData(data);
                log.debug("PII anonymization applied");
            }

            // FASE 4: Construir prompt
            String prompt = buildPrompt(request, data);
            int estimatedTokens = geminiClient.estimateTokens(prompt);
            log.debug("Prompt built - Length: {} chars, Estimated tokens: {}",
                prompt.length(), estimatedTokens);

            // FASE 5: Invocar Gemini
            long geminiStart = System.currentTimeMillis();
            String geminiResponse = geminiClient.callGemini(prompt);
            long geminiDuration = System.currentTimeMillis() - geminiStart;

            meterRegistry.timer("ai.explanation.duration", "phase", "gemini")
                .record(geminiDuration, TimeUnit.MILLISECONDS);
            log.info("Gemini API call completed in {}ms", geminiDuration);

            // FASE 6: Parsear respuesta JSON
            DashboardExplanationResponse response = parseGeminiResponse(
                geminiResponse,
                request,
                estimatedTokens,
                calculateCacheTTL(request.fechaInicio(), request.fechaFin())
            );

            // FASE 7: Guardar en cache
            if (cache != null) {
                cache.put(cacheKey, response);
                log.debug("Response cached with key: {}", cacheKey);
            }

            // FASE 8: Auditoría
            long totalDuration = System.currentTimeMillis() - startTime;
            logAudit(request, response, queryDuration, geminiDuration, totalDuration, "SUCCESS");

            timerSample.stop(Timer.builder("ai.explanation.duration")
                .tag("phase", "total")
                .tag("cache", "miss")
                .tag("outcome", "success")
                .register(meterRegistry));

            meterRegistry.counter("ai.explanation.requests", "outcome", "success").increment();
            meterRegistry.summary("ai.explanation.tokens").record(estimatedTokens);

            return response;

        } catch (GeminiTimeoutException e) {
            logAuditError(request, "TIMEOUT", e);
            meterRegistry.counter("ai.explanation.requests", "outcome", "timeout").increment();
            throw e;

        } catch (GeminiRateLimitException e) {
            logAuditError(request, "RATE_LIMIT", e);
            meterRegistry.counter("ai.explanation.requests", "outcome", "rate_limited").increment();
            throw e;

        } catch (IOException e) {
            logAuditError(request, "IO_ERROR", e);
            meterRegistry.counter("ai.explanation.requests", "outcome", "error").increment();
            throw new RuntimeException("Failed to load prompt resources", e);

        } catch (Exception e) {
            logAuditError(request, "ERROR", e);
            meterRegistry.counter("ai.explanation.requests", "outcome", "error").increment();
            throw e;
        }
    }

    /**
     * Calcula TTL de cache dinámicamente basado en frescura de datos.
     *
     * Estrategia:
     * - Datos históricos (fechaFin < hoy): 24 horas (86400s)
     * - Datos actuales (incluyen hoy): 30 minutos (1800s)
     * - Fallback: 5 minutos (300s)
     *
     * @param fechaInicio Fecha inicial del rango
     * @param fechaFin Fecha final del rango
     * @return TTL en segundos
     */
    public int calculateCacheTTL(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDate today = LocalDate.now();

        if (fechaFin.isBefore(today)) {
            log.debug("Historical data detected - Cache TTL: 24h");
            return 86400; // 24 horas
        } else if (fechaFin.equals(today) || fechaFin.isAfter(today)) {
            log.debug("Current/future data detected - Cache TTL: 30min");
            return 1800; // 30 minutos
        }

        log.debug("Fallback TTL applied: 5min");
        return 300; // 5 minutos
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private AnalyticsData fetchAnalyticsData(DashboardExplanationRequest request) {
        LocalDate fi = request.fechaInicio();
        LocalDate ff = request.fechaFin();

        return new AnalyticsData(
            analyticsRepository.fetchTotals(fi, ff),
            analyticsRepository.fetchTopOperarios(fi, ff),
            analyticsRepository.fetchDistribucionTurno(fi, ff),
            analyticsRepository.fetchTopMaquinas(fi, ff),
            analyticsRepository.fetchTendenciaDiaria(fi, ff)
        );
    }

    private AnalyticsData anonymizeData(AnalyticsData data) {
        // Anonimizar nombres de operarios manteniendo orden
        List<TopOperarioDto> anonymizedOperarios = IntStream.range(0, data.topOperarios().size())
            .mapToObj(i -> new TopOperarioDto(
                "Operario #" + (i + 1),
                null, // Remove codigoMaquinista
                data.topOperarios().get(i).totalUnidades(),
                data.topOperarios().get(i).numRegistros()
            ))
            .toList();

        return new AnalyticsData(
            data.totals(),
            anonymizedOperarios,
            data.distribucionTurno(),
            data.topMaquinas(),
            data.tendenciaDiaria()
        );
    }

    private String buildPrompt(DashboardExplanationRequest request, AnalyticsData data)
        throws IOException {

        StringBuilder prompt = new StringBuilder();

        // 1. System Prompt
        String systemPrompt = loadResource(systemPromptResource);
        prompt.append(systemPrompt).append("\n\n");

        // 2. Context YAML
        String context = loadResource(contextResource);
        prompt.append("# CONTEXTO DE NEGOCIO\n");
        prompt.append(context).append("\n\n");

        // 3. Metadata del Dashboard
        prompt.append("# DASHBOARD ANALIZADO\n");
        prompt.append("ID: ").append(request.dashboardId()).append("\n");
        prompt.append("Título: Producción por Operario - Mensual\n");
        prompt.append("Rango: ").append(request.fechaInicio())
              .append(" a ").append(request.fechaFin()).append("\n");

        if (request.filtros() != null && !request.filtros().isEmpty()) {
            prompt.append("Filtros aplicados: ")
                  .append(formatFiltros(request.filtros())).append("\n");
        }
        prompt.append("\n");

        // 4. Datos Agregados
        prompt.append("# DATOS AGREGADOS\n\n");

        prompt.append("## TOTALES\n");
        prompt.append(formatTotals(data.totals())).append("\n\n");

        prompt.append("## TOP 10 OPERARIOS (por unidades)\n");
        prompt.append(formatTopOperarios(data.topOperarios())).append("\n\n");

        prompt.append("## DISTRIBUCIÓN POR TURNO\n");
        prompt.append(formatDistribucionTurno(data.distribucionTurno())).append("\n\n");

        prompt.append("## TOP 10 MÁQUINAS\n");
        prompt.append(formatTopMaquinas(data.topMaquinas())).append("\n\n");

        prompt.append("## TENDENCIA DIARIA\n");
        prompt.append(formatTendenciaDiaria(data.tendenciaDiaria())).append("\n\n");

        // 5. Instrucciones finales
        prompt.append("# INSTRUCCIONES\n");
        prompt.append("Genera el análisis en formato JSON estricto siguiendo el esquema definido en el system prompt.\n");
        prompt.append("Campos requeridos: resumenEjecutivo, keyPoints, insightsAccionables, alertas.\n");
        prompt.append("Usa lenguaje claro y ejecutivo. Enfócate en insights accionables.\n");

        return prompt.toString();
    }

    private String loadResource(Resource resource) throws IOException {
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String formatTotals(TotalsDto totals) {
        return String.format("""
            - Total registros: %,d
            - Unidades producidas: %s
            - Peso neto total: %s kg
            """,
            totals.totalRegistros(),
            totals.totalUnidades(),
            totals.pesoNetoTotal()
        );
    }

    private String formatTopOperarios(List<TopOperarioDto> operarios) {
        if (operarios.isEmpty()) {
            return "No hay datos de operarios.";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < operarios.size(); i++) {
            TopOperarioDto op = operarios.get(i);
            sb.append(String.format("%d. %s - %s unidades (%d registros)\n",
                i + 1,
                op.nombreCompleto(),
                op.totalUnidades(),
                op.numRegistros()
            ));
        }
        return sb.toString();
    }

    private String formatDistribucionTurno(List<TurnoDistributionDto> turnos) {
        if (turnos.isEmpty()) {
            return "No hay datos de turnos.";
        }

        StringBuilder sb = new StringBuilder();
        for (TurnoDistributionDto turno : turnos) {
            sb.append(String.format("- %s: %s unidades (%d registros)\n",
                turno.turno(),
                turno.totalUnidades(),
                turno.numRegistros()
            ));
        }
        return sb.toString();
    }

    private String formatTopMaquinas(List<TopMachineDto> maquinas) {
        if (maquinas.isEmpty()) {
            return "No hay datos de máquinas.";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maquinas.size(); i++) {
            TopMachineDto maq = maquinas.get(i);
            sb.append(String.format("%d. %s (%s) - %s unidades\n",
                i + 1,
                maq.maquinaNombre(),
                maq.maquinaCodigo(),
                maq.totalUnidades()
            ));
        }
        return sb.toString();
    }

    private String formatTendenciaDiaria(List<DailyTrendPoint> tendencia) {
        if (tendencia.isEmpty()) {
            return "No hay datos de tendencia.";
        }

        // Formato compacto para no saturar el prompt
        StringBuilder sb = new StringBuilder();
        sb.append("Primeros 7 días:\n");

        tendencia.stream().limit(7).forEach(point ->
            sb.append(String.format("  %s: %s unidades\n",
                point.fecha(), point.totalUnidades()))
        );

        if (tendencia.size() > 14) {
            sb.append("...\n");
            sb.append("Últimos 7 días:\n");
            tendencia.stream().skip(tendencia.size() - 7).forEach(point ->
                sb.append(String.format("  %s: %s unidades\n",
                    point.fecha(), point.totalUnidades()))
            );
        } else if (tendencia.size() > 7) {
            tendencia.stream().skip(7).forEach(point ->
                sb.append(String.format("  %s: %s unidades\n",
                    point.fecha(), point.totalUnidades()))
            );
        }

        return sb.toString();
    }

    private String formatFiltros(Map<String, String> filtros) {
        return filtros.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining(", "));
    }

    private DashboardExplanationResponse parseGeminiResponse(
        String geminiText,
        DashboardExplanationRequest request,
        int estimatedTokens,
        int cacheTTL
    ) {
        try {
            // Full parsing/validation logic; any exception should result in fallback
            try {
                // Extraer JSON de la respuesta (puede venir con texto extra)
                String jsonText;
                try {
                    jsonText = extractJsonFromText(geminiText);
                } catch (IllegalArgumentException iae) {
                    log.error("No JSON found in Gemini response: {}", iae.getMessage());
                    return createFallbackResponse(request, estimatedTokens, cacheTTL,
                        "Error al procesar la respuesta de IA. Por favor intenta nuevamente.");
                }

                // Parsear a estructura temporal
                GeminiJsonResponse geminiData = objectMapper.readValue(jsonText, GeminiJsonResponse.class);

                // Validar campos requeridos
                try {
                    validateGeminiResponse(geminiData);
                } catch (IllegalArgumentException iae) {
                    log.error("Gemini response validation failed: {}", iae.getMessage());
                    log.debug("Parsed Gemini JSON: {}", jsonText);
                    return createFallbackResponse(request, estimatedTokens, cacheTTL,
                        "Error al procesar la respuesta de IA. Por favor intenta nuevamente.");
                }

                // Construir respuesta final
                return new DashboardExplanationResponse(
                    geminiData.resumenEjecutivo(),
                    geminiData.keyPoints(),
                    geminiData.insightsAccionables(),
                    geminiData.alertas(),
                    request.dashboardId(),
                    "Producción por Operario - Mensual",
                    request.fechaInicio(),
                    request.fechaFin(),
                    request.filtros(),
                    Instant.now(),
                    false, // fromCache
                    estimatedTokens,
                    cacheTTL
                );

            } catch (JsonProcessingException e) {
                log.error("Failed to parse Gemini JSON response: {}", e.getMessage(), e);
                log.debug("Raw Gemini response: {}", geminiText);
                return createFallbackResponse(request, estimatedTokens, cacheTTL,
                    "Error al procesar la respuesta de IA. Por favor intenta nuevamente.");
            }
        } catch (Exception e) {
            // Any unexpected exception during parsing/validation should not bubble up
            log.error("Unexpected error while parsing Gemini response: {}", e.getMessage(), e);
            log.debug("Raw Gemini response: {}", geminiText);
            return createFallbackResponse(request, estimatedTokens, cacheTTL,
                "Error al procesar la respuesta de IA. Por favor intenta nuevamente.");
        }
    }

    private String extractJsonFromText(String text) {
        // Buscar primer { y último }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start == -1 || end == -1 || start >= end) {
            throw new IllegalArgumentException("No JSON object found in response");
        }

        return text.substring(start, end + 1);
    }

    private void validateGeminiResponse(GeminiJsonResponse response) {
        if (response.resumenEjecutivo() == null || response.resumenEjecutivo().isBlank()) {
            throw new IllegalArgumentException("resumenEjecutivo is required");
        }

        if (response.keyPoints() == null || response.keyPoints().isEmpty()) {
            throw new IllegalArgumentException("keyPoints must have at least 1 item");
        }

        if (response.insightsAccionables() == null) {
            throw new IllegalArgumentException("insightsAccionables is required");
        }

        if (response.alertas() == null) {
            throw new IllegalArgumentException("alertas is required");
        }
    }

    private DashboardExplanationResponse createFallbackResponse(
        DashboardExplanationRequest request,
        int tokens,
        int cacheTTL,
        String errorMessage
    ) {
        return new DashboardExplanationResponse(
            "No se pudo generar el análisis automático. " + errorMessage,
            List.of("Intenta nuevamente en unos momentos", "Si el problema persiste, contacta soporte"),
            List.of(),
            List.of("⚠️ Error en el servicio de IA"),
            request.dashboardId(),
            "Producción por Operario - Mensual",
            request.fechaInicio(),
            request.fechaFin(),
            request.filtros(),
            Instant.now(),
            false,
            tokens,
            cacheTTL
        );
    }

    private String buildCacheKey(DashboardExplanationRequest request) {
        // Hash de filtros ordenados
        String filtersHash = hashFiltros(request.filtros());

        return String.format("dashboard:%d:fi:%s:ff:%s:filters:%s",
            request.dashboardId(),
            request.fechaInicio(),
            request.fechaFin(),
            filtersHash
        );
    }

    private String hashFiltros(Map<String, String> filtros) {
        if (filtros == null || filtros.isEmpty()) {
            return "none";
        }

        try {
            // Ordenar alfabéticamente y serializar
            String sortedJson = objectMapper.writeValueAsString(
                new TreeMap<>(filtros)
            );

            // SHA-256
            byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(sortedJson.getBytes(StandardCharsets.UTF_8));

            // Hex string (primeros 12 caracteres)
            return HexFormat.of().formatHex(hash).substring(0, 12);

        } catch (Exception e) {
            log.warn("Failed to hash filtros, using fallback", e);
            return "error";
        }
    }

    private void logAudit(
        DashboardExplanationRequest request,
        DashboardExplanationResponse response,
        long queryDuration,
        long geminiDuration,
        long totalDuration,
        String status
    ) {
        String auditLog = String.format("""
            {
              "event": "AI_EXPLANATION",
              "dashboardId": %d,
              "fechaInicio": "%s",
              "fechaFin": "%s",
              "fromCache": %b,
              "latencyMs": %d,
              "queryLatencyMs": %d,
              "geminiLatencyMs": %d,
              "tokens": %d,
              "cacheTTL": %d,
              "status": "%s"
            }
            """,
            request.dashboardId(),
            request.fechaInicio(),
            request.fechaFin(),
            response.fromCache(),
            totalDuration,
            queryDuration,
            geminiDuration,
            response.tokensUsados(),
            response.cacheTTLSeconds(),
            status
        );

        log.info(auditLog);
    }

    private void logAuditError(
        DashboardExplanationRequest request,
        String status,
        Exception e
    ) {
        String auditLog = String.format("""
            {
              "event": "AI_EXPLANATION_ERROR",
              "dashboardId": %d,
              "fechaInicio": "%s",
              "fechaFin": "%s",
              "status": "%s",
              "error": "%s"
            }
            """,
            request.dashboardId(),
            request.fechaInicio(),
            request.fechaFin(),
            status,
            e.getMessage()
        );

        log.error(auditLog, e);
    }
}
