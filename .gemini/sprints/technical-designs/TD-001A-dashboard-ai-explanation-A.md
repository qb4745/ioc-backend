# TD-001A: Dashboard AI Explanation

**Feature ID**: FP-001A  
**Sprint**: Sprint 1  
**Estado**: ✅ IMPLEMENTADO  
**Fecha**: Noviembre 2025

## 1. Resumen Ejecutivo

Implementar endpoint REST que genere explicaciones ejecutivas de dashboards usando Google Gemini AI, con cache inteligente, manejo robusto de errores y observabilidad completa.

## 2. Objetivos

### Funcionales
- ✅ Generar resúmenes ejecutivos de dashboards en lenguaje natural
- ✅ Proveer insights accionables basados en datos agregados
- ✅ Detectar alertas y anomalías automáticamente
- ✅ Soportar filtros personalizados por dashboard

### No Funcionales
- ✅ Timeout máximo: 90 segundos
- ✅ Rate limiting: 10 requests/min por usuario
- ✅ Cache con TTL dinámico (24h histórico, 30min actual)
- ✅ Disponibilidad: 99.5% (tolerancia a fallos de Gemini)
- ✅ Latencia P95: <5s (con cache hit)

## 3. Arquitectura de Solución

### 3.1 Componentes Implementados

```
┌─────────────────────────────────────────────────────────────┐
│                    AiExplanationController                   │
│  POST /api/v1/ai/explain                                    │
│  GET  /api/v1/ai/explain/{dashboardId}                      │
│  - Rate Limiting (Resilience4j)                             │
│  - Validación de acceso (DashboardAccessService)            │
│  - Manejo de errores (400/408/429/500/503)                  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│              DashboardExplanationService                     │
│  - Orquestación de 8 fases                                  │
│  - Cache dual (histórico/actual)                            │
│  - Construcción de prompts                                  │
│  - Anonimización PII                                        │
│  - Auditoría y métricas (Micrometer)                        │
└────────┬────────────────────────────────┬───────────────────┘
         │                                │
         ▼                                ▼
┌────────────────────┐          ┌─────────────────────────────┐
│ DashboardAnalytics │          │     GeminiApiClient         │
│    Repository      │          │  - WebClient (reactive)     │
│  - 5 queries SQL   │          │  - Retry exponencial        │
│  - JDBC Template   │          │  - Timeout 90s              │
│  - Validaciones    │          │  - Parsing robusto          │
└────────────────────┘          └─────────────────────────────┘
```

### 3.2 Flujo de Ejecución (8 Fases)

1. **Verificación de Cache** → CacheManager (Spring Cache)
2. **Consulta de Datos** → 5 queries SQL agregadas (totales, top operarios, turnos, máquinas, tendencia)
3. **Anonimización PII** → Opcional según configuración (`ai.explanation.send-pii`)
4. **Construcción de Prompt** → System prompt + Context YAML + Datos + Instrucciones
5. **Invocación Gemini** → API REST con timeout 90s y retries
6. **Parsing JSON** → Validación de estructura y fallback en errores
7. **Persistencia en Cache** → TTL dinámico según frescura de datos
8. **Auditoría** → Logs estructurados + Métricas Prometheus

## 4. Especificaciones de API

### 4.1 POST /api/v1/ai/explain

**Request Body**:
```json
{
  "dashboardId": 5,
  "fechaInicio": "2025-06-01",
  "fechaFin": "2025-06-30",
  "filtros": {
    "turno": "DIA",
    "maquina": "M001"
  }
}
```

**Response 200 OK**:
```json
{
  "resumenEjecutivo": "Análisis de producción junio 2025...",
  "keyPoints": [
    "Producción total: 125,430 unidades (+12% vs mayo)",
    "Top operario: Juan Pérez (8,450 unidades)",
    "Turno día concentra 68% de producción"
  ],
  "insightsAccionables": [
    "Considerar nivelación de turnos para optimizar capacidad",
    "Evaluar training para operarios de turno noche"
  ],
  "alertas": [
    "⚠️ Máquina M-003 con baja productividad (20% bajo promedio)"
  ],
  "dashboardId": 5,
  "dashboardTitulo": "Producción por Operario - Mensual",
  "fechaInicio": "2025-06-01",
  "fechaFin": "2025-06-30",
  "filtrosAplicados": {"turno": "DIA"},
  "generadoEn": "2025-11-14T02:30:45Z",
  "fromCache": false,
  "tokensUsados": 1250,
  "cacheTTLSeconds": 1800
}
```

**Errores**:
- `400 BAD REQUEST`: Validaciones fallidas (fechas inválidas, rango >12 meses)
- `408 REQUEST TIMEOUT`: Gemini API timeout (>90s)
- `429 TOO MANY REQUESTS`: Rate limit excedido (API o app)
- `500 INTERNAL SERVER ERROR`: Error inesperado
- `503 SERVICE UNAVAILABLE`: Gemini API no disponible

### 4.2 GET /api/v1/ai/explain/{dashboardId}

Query params: `fechaInicio`, `fechaFin`, filtros opcionales.

**Ejemplo**:
```
GET /api/v1/ai/explain/5?fechaInicio=2025-06-01&fechaFin=2025-06-30&turno=DIA
```

Respuesta idéntica a POST (reutiliza misma lógica).

## 5. Modelo de Datos

### 5.1 DTOs Request
- `DashboardExplanationRequest` (dashboardId, fechaInicio, fechaFin, filtros)

### 5.2 DTOs Response
- `DashboardExplanationResponse` (resumen, keyPoints, insights, alertas, metadata)
- `GeminiJsonResponse` (DTO interno para parsing)

### 5.3 DTOs Analytics
- `TotalsDto` (totalRegistros, totalUnidades, pesoNetoTotal)
- `TopOperarioDto` (nombreCompleto, codigoMaquinista, totalUnidades, numRegistros)
- `TurnoDistributionDto` (turno, totalUnidades, numRegistros)
- `TopMachineDto` (maquinaNombre, maquinaCodigo, totalUnidades, numRegistros)
- `DailyTrendPoint` (fecha, totalUnidades, numRegistros)

### 5.4 Excepciones Custom
- `GeminiApiException` (error base)
- `GeminiTimeoutException` (timeout específico)
- `GeminiRateLimitException` (429 de Gemini)

## 6. Configuración Requerida

### application.properties
```properties
# Gemini API
gemini.api-key=${GEMINI_API_KEY}
gemini.model=gemini-1.5-flash
gemini.timeout.seconds=90
gemini.retry.max-attempts=2
gemini.retry.backoff.initial=500
gemini.retry.backoff.max=1500
gemini.base-url=https://generativelanguage.googleapis.com

# AI Explanation Feature
ai.explanation.send-pii=false
ai.explanation.cache-name-historical=aiExplanationsHistorical
ai.explanation.cache-name-current=aiExplanationsCurrent

# Resilience4j Rate Limiter
resilience4j.ratelimiter.instances.aiExplanation.limit-for-period=10
resilience4j.ratelimiter.instances.aiExplanation.limit-refresh-period=60s
resilience4j.ratelimiter.instances.aiExplanation.timeout-duration=0s
```

### Prompts Resources
- `src/main/resources/prompts/system-prompt.txt`
- `src/main/resources/prompts/context.yaml`

## 7. Estrategia de Cache

### 7.1 Cache Dual
- **aiExplanationsHistorical**: Datos pasados (fechaFin < hoy) → TTL 24h
- **aiExplanationsCurrent**: Datos actuales (fechaFin >= hoy) → TTL 30min

### 7.2 Cache Key
```
dashboard:{id}:fi:{fechaInicio}:ff:{fechaFin}:filters:{hash-SHA256}
```

### 7.3 Invalidación
- Automática por TTL
- Manual vía CacheManager (endpoint admin futuro)

## 8. Observabilidad

### 8.1 Logs Estructurados
```json
{
  "event": "AI_EXPLANATION",
  "dashboardId": 5,
  "fechaInicio": "2025-06-01",
  "fechaFin": "2025-06-30",
  "fromCache": false,
  "latencyMs": 3450,
  "queryLatencyMs": 120,
  "geminiLatencyMs": 3200,
  "tokens": 1250,
  "status": "SUCCESS"
}
```

### 8.2 Métricas Prometheus
- `ai.explanation.requests{outcome=success|timeout|error|rate_limited}`
- `ai.explanation.duration{phase=total|queries|gemini, cache=hit|miss}`
- `ai.explanation.cache{result=hit|miss, type=historical|current}`
- `ai.explanation.tokens` (summary)

### 8.3 Dashboards Recomendados
- Latencia P50/P95/P99 por fase
- Cache hit rate (histórico vs actual)
- Tasa de errores por tipo
- Tokens consumidos diarios

## 9. Seguridad

### 9.1 Autenticación
- Spring Security (JWT o session-based)
- `Authentication` inyectado en controller

### 9.2 Autorización
- Verificación de acceso al dashboard (`DashboardAccessService`)
- Logs con username auditado

### 9.3 Protección de PII
- Configuración `ai.explanation.send-pii=false` por defecto
- Anonimización: "Operario #1", "Operario #2" (sin nombres reales)
- No enviar códigos de maquinista a Gemini

### 9.4 Rate Limiting
- Aplicación: 10 req/min por usuario (Resilience4j)
- Gemini API: Manejo de 429 con backoff

## 10. Testing

### 10.1 Cobertura Implementada
- ✅ `AiExplanationControllerTest` (integration tests)
- ✅ `DashboardExplanationServiceTest` (integration tests)
- ✅ `GeminiApiClientTest` (unit tests con WireMock)
- ✅ `DashboardAnalyticsRepositoryTest` (integration con Testcontainers)

### 10.2 Casos Clave
- Happy path POST/GET
- Cache hit/miss
- Validaciones (rango >12 meses, fechas inválidas)
- Timeout de Gemini (simulado)
- Rate limit de Gemini (429)
- Respuesta malformada de Gemini (fallback)
- Anonimización de PII

## 11. Dependencias Técnicas

### 11.1 Spring
- Spring Web (REST controllers)
- Spring Cache (abstraction layer)
- Spring JDBC (NamedParameterJdbcTemplate)
- Spring Validation (Bean Validation)

### 11.2 Resilience4j
- Rate Limiter
- Retry mechanism

### 11.3 WebFlux
- WebClient (reactive HTTP client para Gemini)

### 11.4 Observabilidad
- Micrometer (métricas)
- SLF4J/Logback (logging)

### 11.5 Testing
- JUnit 5
- Mockito
- WireMock (Gemini API mock)
- Testcontainers (PostgreSQL)

## 12. Riesgos y Mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación Implementada |
|--------|--------------|---------|-------------------------|
| Gemini API down | Media | Alto | Timeout 90s + retries + fallback response |
| Latencia alta de Gemini | Alta | Medio | Cache dual + TTL optimizado |
| Rate limit Gemini | Media | Medio | Rate limiter app (10/min) + manejo 429 |
| Respuesta malformada | Baja | Medio | Parsing robusto + validación + fallback |
| Fuga de PII a Gemini | Baja | Crítico | Anonimización por defecto (`send-pii=false`) |

## 13. Roadmap Futuro

### Fase 2 (Próximo Sprint)
- [ ] Streaming de respuestas (Server-Sent Events)
- [ ] Soporte para múltiples dashboards en batch
- [ ] Cache distribuido (Redis)
- [ ] Endpoint admin para invalidación manual de cache
- [ ] Soporte multiidioma (i18n)

### Fase 3 (Futuro)
- [ ] Fine-tuning de modelo Gemini con datos históricos
- [ ] Comparación de períodos (MoM, YoY)
- [ ] Exportación PDF de reportes
- [ ] Webhooks para notificaciones de alertas

## 14. Referencias

- [BSS-001: DashboardAnalyticsRepository](../../blueprints/backend/ai_explanaition/BSS-001-DashboardAnalyticsRepository.md)
- [BSS-002: GeminiApiClient](../../blueprints/backend/ai_explanaition/BSS-002-GeminiApiClient.md)
- [BSS-003: DashboardExplanationService](../../blueprints/backend/ai_explanaition/BSS-003-DashboardExplanationService.md)
- [BSS-004: AiExplanationController](../../blueprints/backend/ai_explanaition/BSS-004-AiExplanationController.md)
- [Validation Report](../../validation/TD-001A-blueprint-implementation-validation.md)

---
**Aprobado por**: Equipo Técnico  
**Implementado por**: AI Agent  
**Fecha de implementación**: Noviembre 14, 2025  
**Estado de Tests**: ✅ PASSING (100% cobertura crítica)

