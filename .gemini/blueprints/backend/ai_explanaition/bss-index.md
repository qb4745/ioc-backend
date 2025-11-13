# BSS Index - Dashboard AI Explanation Implementation

## Resumen del Proyecto
Implementación de la funcionalidad de explicaciones de dashboard generadas por IA usando Google Gemini API.

**Feature**: FP-001A - Dashboard AI Explanation  
**Technical Design**: TD-001A

---

## Componentes Implementados

### ✅ BSS-001: DashboardAnalyticsRepository
**Archivo**: `src/main/java/com/cambiaso/ioc/persistence/repository/DashboardAnalyticsRepository.java`  
**Test**: `src/test/java/com/cambiaso/ioc/persistence/repository/DashboardAnalyticsRepositoryTest.java`

**Responsabilidad**: Repositorio para consultas analíticas agregadas sobre producción.

**Métodos principales**:
- `fetchTotals(fechaInicio, fechaFin)` - Totales agregados
- `fetchTopOperarios(fechaInicio, fechaFin)` - Top 10 operarios por producción
- `fetchDistribucionTurno(fechaInicio, fechaFin)` - Distribución por turno
- `fetchTopMaquinas(fechaInicio, fechaFin)` - Top 10 máquinas
- `fetchTendenciaDiaria(fechaInicio, fechaFin)` - Serie temporal diaria

**Lecciones aprendidas**:
- Uso de `NamedParameterJdbcTemplate` para queries optimizadas sin overhead JPA
- Validación de rangos de fecha (máximo 12 meses)
- Manejo de casos edge: datos vacíos, fechas nulas
- Row mappers eficientes para DTOs

---

### ✅ BSS-002: GeminiApiClient
**Archivo**: `src/main/java/com/cambiaso/ioc/service/ai/GeminiApiClient.java`  
**Test**: `src/test/java/com/cambiaso/ioc/service/ai/GeminiApiClientTest.java`

**Responsabilidad**: Cliente HTTP para interactuar con Google Gemini API.

**Características principales**:
- Timeout configurable (90 segundos por defecto)
- Retry automático con backoff exponencial (max 2 intentos)
- Manejo de rate limiting (429 → GeminiRateLimitException)
- Timeout detection (→ GeminiTimeoutException)
- Parsing de respuestas JSON con limpieza de markdown

**Lecciones aprendidas**:
- WebClient reactivo con Mono para timeouts precisos
- Retry strategy: `Retry.backoff(maxAttempts, Duration.ofMillis(initialBackoff))`
- Limpieza de respuestas: remover ```json y ``` que a veces incluye Gemini
- Manejo de errores HTTP específicos (400, 429, 500, 503)
- Tests con WireMock para simular respuestas de API externa

---

### ✅ BSS-003: DashboardExplanationService
**Archivo**: `src/main/java/com/cambiaso/ioc/service/ai/DashboardExplanationService.java`  
**Test**: `src/test/java/com/cambiaso/ioc/service/ai/DashboardExplanationServiceTest.java`

**Responsabilidad**: Servicio orquestador que coordina consultas, prompts, cache y llamadas a IA.

**Flujo de 8 fases**:
1. Verificar cache (key compuesta)
2. Consultar datos agregados (5 queries SQL paralelas)
3. Anonimizar PII si configurado
4. Construir prompt estructurado (system + context + data)
5. Invocar Gemini API
6. Parsear respuesta JSON
7. Guardar en cache con TTL dinámico
8. Auditar request (logs + métricas)

**Cache TTL dinámico**:
- Datos históricos (fechaFin < hoy): 24 horas
- Datos actuales (fechaFin >= hoy): 30 minutos
- Fallback: 5 minutos

**Lecciones aprendidas**:
- Cache key con hash SHA-256 de dashboardId + fechas + filtros
- Anonimización de PII: reemplazar nombres por "Operario_1", "Operario_2"
- Prompts desde archivos externos (`classpath:prompts/system-prompt.txt`, `context.yaml`)
- Métricas con Micrometer: timers, counters, summaries
- Logging estructurado para auditoría

---

### ✅ BSS-004: AiExplanationController
**Archivo**: `src/main/java/com/cambiaso/ioc/controller/AiExplanationController.java`  
**Test**: `src/test/java/com/cambiaso/ioc/controller/AiExplanationControllerTest.java`

**Responsabilidad**: Controlador REST que expone endpoints para generar explicaciones de IA.

**Endpoints**:
- `POST /api/v1/ai/explain` - Genera explicación con body JSON
- `GET /api/v1/ai/explain/{dashboardId}` - Genera explicación con query params
- `GET /api/v1/ai/health` - Health check del servicio

**Características principales**:
- Rate limiting: `@RateLimiter(name = "aiExplanation")`
- Validaciones: `@Valid`, `@Min`, `@Max`, `@NotNull`, `@DateTimeFormat`
- Manejo de errores HTTP específicos:
  - 400 Bad Request: Validaciones fallidas
  - 408 Request Timeout: Timeout de Gemini (>90s)
  - 429 Too Many Requests: Rate limit de Gemini
  - 500 Internal Server Error: Errores inesperados
  - 503 Service Unavailable: Gemini API no disponible
- Exception handlers: `@ExceptionHandler` para validaciones
- Extracción de filtros desde query params

**Implementación**:
```java
@PostMapping("/explain")
@RateLimiter(name = "aiExplanation")
public ResponseEntity<DashboardExplanationResponse> explainDashboard(
    @Valid @RequestBody DashboardExplanationRequest request,
    Authentication authentication
)
```

**Lecciones aprendidas**:
- `@WebMvcTest` carga solo el contexto web (controladores) sin toda la aplicación
- `MockMvc` permite tests de endpoints sin levantar servidor HTTP real
- `@WithMockUser` simula autenticación de Spring Security en tests
- Rate limiting con `@RateLimiter` de Resilience4j integrado en endpoints
- Exception handlers con `@ExceptionHandler` capturan errores específicos
- Validaciones automáticas: `@Valid`, `@Min`, `@Max`, `@NotNull`, `@DateTimeFormat`
- Query params automáticos: `@RequestParam Map<String, String>` captura todos los params
- `ResponseEntity.status(HttpStatus.XXX)` para códigos HTTP específicos
- CSRF token requerido en tests POST: `.with(csrf())`
- Constructores de excepciones: `GeminiTimeoutException(mensaje, causa)` requiere 2 args
- JSON assertions con `jsonPath()`: `$.campo` accede a propiedades
- Mock de servicios: `when(...).thenReturn(...)` o `.thenThrow(...)`
- Verification: `verify(service).method(...)` valida que se llamó al método
- `verifyNoInteractions(service)` para casos donde no debe llamarse al servicio

---

## Estructura de DTOs

### DashboardExplanationRequest
```java
public record DashboardExplanationRequest(
    Integer dashboardId,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    Map<String, String> filtros
)
```

### DashboardExplanationResponse
```java
public record DashboardExplanationResponse(
    String resumenEjecutivo,
    List<String> keyPoints,
    List<String> insightsAccionables,
    List<String> alertas,
    Integer dashboardId,
    String dashboardName,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    Map<String, String> filtros,
    Instant generatedAt,
    boolean fromCache,
    int tokensUsados,
    int cacheTTL
)
```

---

## Configuración Requerida

### application.properties
```properties
# Gemini API
gemini.api-key=${GEMINI_API_KEY}
gemini.model=gemini-1.5-flash
gemini.timeout.seconds=90
gemini.retry.max-attempts=2
gemini.retry.backoff.initial=500

# AI Explanation Service
ai.explanation.send-pii=false
ai.explanation.cache-name=aiExplanations

# Rate Limiter
resilience4j.ratelimiter.instances.aiExplanation.limit-for-period=10
resilience4j.ratelimiter.instances.aiExplanation.limit-refresh-period=60s
resilience4j.ratelimiter.instances.aiExplanation.timeout-duration=5s
```

### Archivos de Prompts
- `src/main/resources/prompts/system-prompt.txt` - Prompt del sistema
- `src/main/resources/prompts/context.yaml` - Contexto del negocio

---

## Tests Implementados

### BSS-001: DashboardAnalyticsRepositoryTest
- ✅ fetchTotals - datos válidos
- ✅ fetchTotals - rango vacío
- ✅ fetchTotals - rango excede 12 meses
- ✅ fetchTopOperarios - top 10 con ranking
- ✅ fetchDistribucionTurno - suma 100%
- ✅ fetchTopMaquinas - ordenado por producción
- ✅ fetchTendenciaDiaria - serie temporal completa

### BSS-002: GeminiApiClientTest
- ✅ callGemini - respuesta exitosa
- ✅ callGemini - timeout después de 90s
- ✅ callGemini - rate limit 429
- ✅ callGemini - retry en error 500
- ✅ parseJsonResponse - limpieza de markdown
- ✅ estimateTokens - cálculo aproximado

### BSS-003: DashboardExplanationServiceTest
- ✅ explainDashboard - cache miss genera explicación
- ✅ explainDashboard - cache hit retorna inmediato
- ✅ explainDashboard - anonimiza PII
- ✅ explainDashboard - timeout propaga excepción
- ✅ explainDashboard - rate limit propaga excepción
- ✅ calculateCacheTTL - datos históricos 24h
- ✅ calculateCacheTTL - datos actuales 30min
- ✅ cache key diferente por filtros

### BSS-004: AiExplanationControllerTest
- ✅ POST /explain - respuesta exitosa 200
- ✅ POST /explain - respuesta desde cache
- ✅ GET /explain/{id} - con query params
- ✅ POST /explain - timeout retorna 408
- ✅ POST /explain - rate limit retorna 429
- ✅ POST /explain - Gemini API error retorna 503
- ✅ POST /explain - validación fallida retorna 400
- ✅ POST /explain - error inesperado retorna 500
- ✅ GET /explain - parámetros faltantes retorna 400
- ✅ GET /explain - dashboard ID inválido retorna 400
- ✅ GET /health - retorna 200
- ✅ POST /explain - sin autenticación retorna 401
- ✅ GET /explain - extrae filtros correctamente

---

## Métricas y Observabilidad

### Métricas Micrometer
- `ai.explanation.cache` (counter): hit/miss
- `ai.explanation.requests` (counter): success/timeout/rate_limited/error
- `ai.explanation.duration` (timer): total/queries/gemini
- `ai.explanation.tokens` (summary): distribución de tokens usados

### Logs Estructurados
```json
{
  "level": "INFO",
  "message": "AI explanation generated successfully",
  "dashboardId": 5,
  "fromCache": false,
  "tokensUsados": 1500,
  "user": "testuser",
  "duration": 2500
}
```

---

## Próximos Pasos

1. ✅ Ejecutar tests del BSS-004 (AiExplanationController)
2. ⬜ Actualizar lecciones aprendidas con resultados de tests
3. ⬜ Configurar rate limiter en application.properties si no existe
4. ⬜ Crear archivos de prompts si no existen
5. ⬜ Documentar endpoints en Swagger/OpenAPI
6. ⬜ Integración con frontend
7. ⬜ Tests de integración end-to-end

---

## Comandos Útiles

### Compilar
```bash
mvn clean compile
```

### Ejecutar tests
```bash
# Todos los tests de AI
mvn test -Dtest=*AI*,*Gemini*,*Dashboard*Explanation*

# Solo BSS-004
mvn test -Dtest=AiExplanationControllerTest
```

### Ejecutar la aplicación
```bash
export GEMINI_API_KEY=your-api-key
mvn spring-boot:run
```

---

**Última actualización**: 2025-11-12  
**Estado**: BSS-004 implementado, pendiente ejecutar tests
