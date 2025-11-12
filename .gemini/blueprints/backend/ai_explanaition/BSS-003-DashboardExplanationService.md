# Backend Service Specification: DashboardExplanationService

## Metadata
- **BSS ID**: BSS-003
- **Technical Design**: TD-001A (Dashboard AI Explanation)
- **Feature Plan**: FP-001A
- **Clase**: `DashboardExplanationService`
- **Tipo**: Service (Orchestration)
- **Package**: `com.cambiaso.ioc.service.ai`
- **Sprint**: Sprint 2
- **Fecha Creación**: 2025-11-12
- **Estado**: DRAFT

---

## 1. Propósito y Responsabilidades

### 1.1. Propósito
Servicio orquestador principal para la generación de explicaciones de dashboards con IA. Coordina la obtención de datos analíticos, construcción de prompts, invocación de Gemini, parsing de respuestas, caching y auditoría.

### 1.2. Responsabilidades
- Orquestar el flujo completo de generación de explicaciones (8 fases)
- Gestionar cache in-memory con TTL dinámico (histórico vs actual)
- Construir prompts estructurados (system + context + data + instructions)
- Invocar GeminiApiClient con manejo de errores
- Parsear y validar respuestas JSON de Gemini
- Sanitizar/anonimizar datos PII según configuración
- Generar logs de auditoría estructurados
- Registrar métricas de performance y uso
- Implementar fallback responses para errores de parsing

### 1.3. Ubicación en Arquitectura
```
AiExplanationController
         ↓
[DashboardExplanationService] ← Orquestador Principal
         ↓                  ↓
DashboardAnalyticsRepo   GeminiApiClient
         ↓                  ↓
    PostgreSQL         Gemini API
```

---

## 2. Interfaz Pública

### 2.1. Signature Completa

```java
package com.cambiaso.ioc.service.ai;

import com.cambiaso.ioc.dto.ai.DashboardExplanationRequest;
import com.cambiaso.ioc.dto.ai.DashboardExplanationResponse;

/**
 * Servicio para generar explicaciones de dashboards usando IA (Gemini).
 * Coordina consultas analíticas, prompts, cache y auditoría.
 */
@Service
public class DashboardExplanationService {

    /**
     * Genera una explicación ejecutiva del dashboard para el rango de fechas dado.
     * 
     * Flujo:
     * 1. Verificar cache (key compuesta por dashboard + fechas + filtros)
     * 2. Si miss: consultar datos agregados (5 queries SQL)
     * 3. Construir prompt estructurado con system + context + data
     * 4. Invocar Gemini API con timeout 90s
     * 5. Parsear respuesta JSON
     * 6. Guardar en cache con TTL dinámico
     * 7. Auditar request (logs estructurados + métricas)
     * 8. Retornar respuesta
     * 
     * @param request DTO con dashboardId, fechaInicio, fechaFin, filtros
     * @return Respuesta con resumen, keyPoints, insights, alertas y metadata
     * @throws IllegalArgumentException si validaciones de negocio fallan
     * @throws GeminiApiException si falla invocación a IA después de retries
     * @throws GeminiTimeoutException si excede timeout de 90s
     */
    public DashboardExplanationResponse explainDashboard(DashboardExplanationRequest request);
    
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
    public int calculateCacheTTL(LocalDate fechaInicio, LocalDate fechaFin);
}
```

---

## 3. Dependencias Inyectadas

### 3.1. Constructor Injection

```java
import com.cambiaso.ioc.repository.DashboardAnalyticsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;

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
    
    // Métodos...
}
```

### 3.2. Grafo de Dependencias
```
DashboardExplanationService
    ├── DashboardAnalyticsRepository
    ├── GeminiApiClient
    ├── ObjectMapper (Jackson)
    ├── CacheManager (Caffeine)
    └── MeterRegistry (Micrometer)
```

---

## 4. Reglas de Negocio

### 4.1. Cálculo de Cache TTL

| Condición | TTL | Justificación |
|-----------|-----|---------------|
| `fechaFin < today` | 24 horas | Datos históricos no cambian |
| `fechaFin >= today` | 30 minutos | Datos pueden actualizarse |
| Fallback | 5 minutos | Seguridad ante lógica inesperada |

### 4.2. Anonimización PII

Si `send-pii-to-gemini=false`:
- `nombreCompleto` → `"Operario #1"`, `"Operario #2"`, etc. (ordenados por unidades DESC)
- `usuarioSap` → Removido completamente
- `maquinaNombre` → Preservado (no es PII)

### 4.3. Construcción de Cache Key

```
Format: "dashboard:{id}:fi:{fechaInicio}:ff:{fechaFin}:filters:{sha256Hash}"

Ejemplo: "dashboard:5:fi:2025-06-01:ff:2025-06-30:filters:a3b2c1..."
```

Hash de filtros:
- Ordenar filtros alfabéticamente por clave
- Serializar a JSON compacto
- Aplicar SHA-256
- Tomar primeros 12 caracteres del hex

---

## 5. Implementación Detallada

### 5.1. Método Principal: explainDashboard

```java
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
        
    } catch (Exception e) {
        logAuditError(request, "ERROR", e);
        meterRegistry.counter("ai.explanation.requests", "outcome", "error").increment();
        throw e;
    }
}
```

### 5.2. Fetch Analytics Data (Helper)

```java
private record AnalyticsData(
    TotalsDto totals,
    List<TopOperarioDto> topOperarios,
    List<TurnoDistributionDto> distribucionTurno,
    List<TopMachineDto> topMaquinas,
    List<DailyTrendPoint> tendenciaDiaria
) {}

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
```

### 5.3. Anonymize Data

```java
private AnalyticsData anonymizeData(AnalyticsData data) {
    // Anonimizar nombres de operarios manteniendo orden
    List<TopOperarioDto> anonymizedOperarios = IntStream.range(0, data.topOperarios().size())
        .mapToObj(i -> new TopOperarioDto(
            "Operario #" + (i + 1),
            null, // Remove usuarioSap
            data.topOperarios().get(i).totalUnidades(),
            data.topOperarios().get(i).numRegistros(),
            data.topOperarios().get(i).eficienciaPromedio()
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
```

### 5.4. Build Prompt

```java
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
        - Unidades producidas: %,d
        - Defectos: %,d
        - Fallos: %,d
        - Eficiencia promedio: %.2f%%
        """, 
        totals.totalRegistros(),
        totals.totalUnidades(),
        totals.totalDefectos(),
        totals.totalFallos(),
        totals.eficienciaPromedio()
    );
}

private String formatTopOperarios(List<TopOperarioDto> operarios) {
    if (operarios.isEmpty()) {
        return "No hay datos de operarios.";
    }
    
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < operarios.size(); i++) {
        TopOperarioDto op = operarios.get(i);
        sb.append(String.format("%d. %s - %,d unidades (%.2f%% eficiencia, %d registros)\n",
            i + 1,
            op.nombreCompleto(),
            op.totalUnidades(),
            op.eficienciaPromedio(),
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
        sb.append(String.format("- %s: %,d unidades (%.2f%% eficiencia, %d registros)\n",
            turno.turno(),
            turno.totalUnidades(),
            turno.eficienciaPromedio(),
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
        sb.append(String.format("%d. %s (%s) - %,d unidades (%.2f%% eficiencia)\n",
            i + 1,
            maq.maquinaNombre(),
            maq.maquinaCodigo(),
            maq.totalUnidades(),
            maq.eficienciaPromedio()
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
        sb.append(String.format("  %s: %,d unidades\n", 
            point.fecha(), point.totalUnidades()))
    );
    
    if (tendencia.size() > 14) {
        sb.append("...\n");
        sb.append("Últimos 7 días:\n");
        tendencia.stream().skip(tendencia.size() - 7).forEach(point -> 
            sb.append(String.format("  %s: %,d unidades\n", 
                point.fecha(), point.totalUnidades()))
        );
    } else if (tendencia.size() > 7) {
        tendencia.stream().skip(7).forEach(point -> 
            sb.append(String.format("  %s: %,d unidades\n", 
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
```

### 5.5. Parse Gemini Response

```java
private DashboardExplanationResponse parseGeminiResponse(
    String geminiText,
    DashboardExplanationRequest request,
    int estimatedTokens,
    int cacheTTL
) {
    try {
        // Extraer JSON de la respuesta (puede venir con texto extra)
        String jsonText = extractJsonFromText(geminiText);
        
        // Parsear a estructura temporal
        GeminiJsonResponse geminiData = objectMapper.readValue(jsonText, GeminiJsonResponse.class);
        
        // Validar campos requeridos
        validateGeminiResponse(geminiData);
        
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
        
        // Fallback response
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
```

### 5.6. Calculate Cache TTL

```java
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
```

### 5.7. Build Cache Key

```java
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
```

### 5.8. Audit Logging

```java
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
```

---

## 6. DTOs

### 6.1. GeminiJsonResponse (Internal)

```java
package com.cambiaso.ioc.dto.ai;

import java.util.List;

/**
 * Estructura esperada del JSON retornado por Gemini.
 */
public record GeminiJsonResponse(
    String resumenEjecutivo,
    List<String> keyPoints,
    List<String> insightsAccionables,
    List<String> alertas
) {}
```

---

## 7. Manejo de Errores

### 7.1. Estrategia de Errores

| Error | Origen | Acción |
|-------|--------|--------|
| `IllegalArgumentException` | Validación fechas | Retornar al controller → 400 |
| `GeminiTimeoutException` | Timeout 90s | Incrementar métrica, log audit → 504 al cliente |
| `GeminiRateLimitException` | API 429 | Incrementar métrica → 503 al cliente con Retry-After |
| `GeminiApiException` | Otros errores API | Log error → 503 al cliente |
| `JsonProcessingException` | Parsing fallido | Fallback response → 200 con mensaje error |
| `IOException` | Lectura resources | Log error → 500 |

### 7.2. Circuit Breaker (Futuro)

Considerar añadir Resilience4j Circuit Breaker si tasa de errores Gemini > 50% en 10 requests:
```java
@CircuitBreaker(name = "gemini", fallbackMethod = "fallbackExplain")
public DashboardExplanationResponse explainDashboard(DashboardExplanationRequest request) {
    // ...
}
```

---

## 8. Performance

### 8.1. Latency Budget

| Escenario | Target P50 | Target P95 | Max (timeout) |
|-----------|------------|------------|---------------|
| Cache Hit | 50ms | 150ms | - |
| Cache Miss (queries + Gemini) | 4s | 10s | 90s |

### 8.2. Optimizaciones
- Cache agresivo con TTL adaptativo
- Queries paralelas (futuro: CompletableFuture)
- Prompt compacto (tendencia resumida, no todos los días)
- Timeout agresivo para evitar bloqueos

---

## 9. Testing

### 9.1. Tests Unitarios

```java
@SpringBootTest
class DashboardExplanationServiceTest {

    @Autowired
    private DashboardExplanationService service;
    
    @MockBean
    private DashboardAnalyticsRepository analyticsRepository;
    
    @MockBean
    private GeminiApiClient geminiClient;
    
    @Test
    void explainDashboard_cacheMiss_generatesExplanation() {
        // Given
        DashboardExplanationRequest request = new DashboardExplanationRequest(
            5, LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30), Map.of()
        );
        
        when(analyticsRepository.fetchTotals(any(), any()))
            .thenReturn(new TotalsDto(100L, 5000L, 50L, 10L, new BigDecimal("95.5")));
        // ... más mocks
        
        String mockGeminiResponse = """
            {
              "resumenEjecutivo": "Producción estable...",
              "keyPoints": ["Punto 1", "Punto 2"],
              "insightsAccionables": ["Insight 1"],
              "alertas": []
            }
            """;
        when(geminiClient.callGemini(anyString())).thenReturn(mockGeminiResponse);
        
        // When
        DashboardExplanationResponse response = service.explainDashboard(request);
        
        // Then
        assertNotNull(response);
        assertEquals("Producción estable...", response.resumenEjecutivo());
        assertEquals(2, response.keyPoints().size());
        assertFalse(response.fromCache());
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
}
```

### 9.2. Tests de Integración

```java
@SpringBootTest
@Transactional
class DashboardExplanationServiceIntegrationTest {

    @Autowired
    private DashboardExplanationService service;
    
    @Test
    void explainDashboard_endToEnd_withRealDatabase() {
        // Given: datos reales en BD
        DashboardExplanationRequest request = new DashboardExplanationRequest(
            5, LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30), Map.of()
        );
        
        // When
        DashboardExplanationResponse response = service.explainDashboard(request);
        
        // Then
        assertNotNull(response);
        assertTrue(response.keyPoints().size() >= 1);
        // ... más assertions
    }
}
```

### 9.3. Cobertura Objetivo
- **Líneas**: 85%
- **Branches**: 80%
- **Métodos**: 95%

---

## 10. Observabilidad

### 10.1. Métricas Clave

| Métrica | Tipo | Tags | Descripción |
|---------|------|------|-------------|
| `ai.explanation.duration` | Timer | phase=total\|queries\|gemini, cache=hit\|miss | Duración por fase |
| `ai.explanation.requests` | Counter | outcome=success\|error\|timeout\|rate_limited | Total requests |
| `ai.explanation.cache` | Counter | result=hit\|miss | Cache effectiveness |
| `ai.explanation.tokens` | Summary | - | Tokens usados (distribución) |

---

## 11. Seguridad

### 11.1. Validaciones
- Request DTO validado con Bean Validation en controller
- Rango de fechas validado por repository (max 12 meses)
- Filtros validados contra whitelist (futuro)

### 11.2. PII Protection
- Anonimización opcional via flag `send-pii-to-gemini`
- Logs de auditoría NO incluyen datos PII

---

## 12. Configuración

### 12.1. application.properties

```properties
# AI Explanation
ai.explanation.send-pii=false
ai.explanation.cache-name=aiExplanations

# Prompts (resources)
# Archivos en: src/main/resources/prompts/
# - system-prompt.txt
# - context.yaml
```

---

## 13. Checklist de Implementación

- [ ] Crear clase `DashboardExplanationService`
- [ ] Crear record interno `AnalyticsData`
- [ ] Crear DTO `GeminiJsonResponse`
- [ ] Implementar método principal `explainDashboard` (8 fases)
- [ ] Implementar helpers (fetchAnalyticsData, buildPrompt, parseResponse)
- [ ] Implementar `calculateCacheTTL`
- [ ] Implementar `buildCacheKey` con hash SHA-256
- [ ] Implementar anonimización PII
- [ ] Añadir métricas Micrometer
- [ ] Añadir logs de auditoría estructurados
- [ ] Crear archivos de prompts (system-prompt.txt, context.yaml)
- [ ] Tests unitarios con mocks
- [ ] Tests integración con BD real
- [ ] Verificar cobertura >= 85%
- [ ] Code review

---

## 14. Referencias

- **Technical Design**: TD-001A-dashboard-ai-explanation-A.md
- **Feature Plan**: FP-001A-dashboard-ai-explanation-A.md
- **Prompts**: FP-001A-system-prompt.txt, FP-001A-context.yaml

---

**Fin BSS-003**

