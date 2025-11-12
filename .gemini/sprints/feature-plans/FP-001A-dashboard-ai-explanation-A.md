# Feature Plan (Alternativa A - MVP): Explicar Dashboard con Gemini AI

> ID: FP-001A
> Título: Explicación de Dashboard (Botón Global) - MVP
> Autor: Equipo de Producto
> Fecha: 2025-11-11
> Versión: 0.4-REFINED (Post-evaluación con todos los ajustes aplicados)
> Estado: READY FOR IMPLEMENTATION
> Changelog:
> - 2025-11-11 v0.4: Refinamiento final post-evaluación (aclaración de pre-requisitos operacionales, presupuesto, usuarios)
> - 2025-11-11 v0.3: Ajustes críticos post-evaluación (JSON response, cache inteligente, loading phases, testing strategy)
> - 2025-11-11 v0.2: Aplicadas mejoras críticas + importantes (timeout, DTOs, RBAC, etc.)
> - 2025-11-11 v0.1: Versión inicial

---

## 📑 ÍNDICE

- [0. Pre-requisitos (BLOQUEAN INICIO)](#0-pre-requisitos-bloquean-inicio)
- [1. Objetivo](#1-objetivo)
- [2. Justificación](#2-por-qué-esta-alternativa)
- [3. UX / Ubicación del botón](#3-ux--ubicación-del-botón)
- [4. Flujo de datos](#4-flujo-de-datos-alto-nivel)
- [5. API Backend](#5-api-backend)
  - [5.1. DTOs Backend](#51-dtos-backend-record-classes)
  - [5.2. Manejo de Errores](#52-manejo-de-errores-detallado)
- [6. Queries SQL](#6-queries-sql-para-construir-datos-resumidos)
- [7. Prompt Template](#7-prompt-template-ejemplo)
- [8. Implementación - Tareas](#8-implementación---tareas-detalladas-mvp)
  - [8.1. Estrategia de Testing](#81-estrategia-de-testing)
  - [8.2. Métricas y Monitoreo](#82-métricas-y-monitoreo)
- [9. Configuración y variables](#9-configuración-y-variables-necesarias)
- [10. Consideraciones importantes](#10-consideraciones-importantes)
- [11. Checklist pre-desarrollo](#11-checklist-pre-desarrollo-acción-requerida)
- [12. Contexto de negocio](#12-contexto-de-negocio-mínimo-requerido)
- [13. Métricas de éxito](#13-métricas-de-éxito-mvp)
- [14. Próximos pasos](#14-próximo-paso-recomendado)
- [15. DECISIÓN DE PERSISTENCIA (DOCUMENTADA)](#15-decision-de-persistencia-documentada)

---

## 0. PRE-REQUISITOS (BLOQUEAN INICIO)

⚠️ **Antes de comenzar la implementación, asegurar**:

- [x] ✅ **GEMINI_API_KEY obtenida**: https://makersuite.google.com/app/apikey
      - Límites free tier: 15 RPM, 1,500 RPD, 1M TPM
      - ⚠️ **Free tier OK para <= 3 usuarios simultáneos**
      - Plan de migración a paid tier: $1.50/mes para 10K requests
      
- [ ] ✅ **Actualización de datos confirmada** (CRÍTICO para cache strategy):
      - ❓ **¿Los datos se actualizan en tiempo real o batch diario?**
      - Si TIEMPO REAL → cache corto (30min para todo)
      - Si BATCH DIARIO → cache inteligente (24h histórico, 30min actual día)
      - **⚠️ ACCIÓN REQUERIDA**: Confirmar con equipo de datos antes de empezar
      
- [ ] ✅ **Presupuesto para paid tier confirmado** (si es necesario):
      - Free tier: 15 RPM límite → se agota con 3 usuarios concurrentes
      - ❓ **¿Hay presupuesto de ~$2-5/mes para migrar a paid tier?**
      - **⚠️ Si NO hay presupuesto** → Limitar feature solo a ROLE_ADMIN permanentemente
      - **⚠️ Si SÍ hay presupuesto** → Rollout gradual a todos los usuarios (ver sección 8.3)
      
- [ ] ✅ **Usuarios simultáneos esperados confirmados**:
      - ❓ **¿Cuántos usuarios usarán la feature simultáneamente en el peor caso?**
      - <= 3 usuarios → Free tier suficiente
      - 3-10 usuarios → Paid tier necesario ($1.50/mes)
      - >10 usuarios → Considerar queue system + paid tier
      - **⚠️ ACCIÓN REQUERIDA**: Estimar con product owner
      
- [ ] ✅ **Contexto de negocio completado** (ver sección 12):
      - industria, unidad_cantidad, objetivo_mensual_unidades
      - horario_turno_dia, horario_turno_noche
      - ⚠️ Sin esto, Gemini generará explicaciones genéricas sin valor de negocio
      
- [x] ✅ **Componente frontend identificado**:
      - **Componente**: `DashboardEmbed.tsx`
      - **Ruta**: `src/components/DashboardEmbed.tsx`
      - **Páginas que lo usan**: 
        - `src/pages/DashboardsPage.tsx` (dashboard operacional)
        - `src/pages/GerencialDashboardPage.tsx` (dashboard gerencial)
      - **Ubicación iframe**: `<iframe src={iframeUrl} width="100%" height="100%" .../>`
      
- [x] ✅ **Decisión de tecnología de persistencia (DECIDIDO)**:
      - **Opción elegida:** `NamedParameterJdbcTemplate` (implementación de `JdbcTemplate` con parámetros nombrados).
      - **Motivación:** Control total del SQL para queries agregadas complejas (COALESCE, STRING_AGG, agregaciones por periodo), rendimiento predecible y facilidad para construir payloads compactos para Gemini. Permite filtros dinámicos con parámetros nombrados y evita concatenación insegura.
      - **Patrón recomendado:** enfoque híbrido — mantener JPA para entidades CRUD existentes y usar `NamedParameterJdbcTemplate` exclusivamente para consultas analíticas y agregaciones del feature FP-001A.
      - **Ubicación propuesta:** `com.cambiaso.ioc.repository.analytics` + DTOs en `com.cambiaso.ioc.dto.analytics`.
      - **Acción:** Implementar `DashboardAnalyticsRepository` que inyecte `NamedParameterJdbcTemplate` y devuelva DTOs (TotalsDto, TopOperarioDto, TurnoDistributionDto).

- [x] ✅ **Decisión de formato de respuesta**: **JSON** (parsing 100% confiable vs Markdown ~70%)

**🔴 Si falta alguno → NO empezar implementación** (riesgo de bloquearse mid-work)

---

## 1. Objetivo

Implementar un MVP que permita a usuarios generar una explicación en lenguaje natural del dashboard incrustado en Metabase mediante un único botón "✨ Explicar Dashboard" ubicado fuera del iframe. Esta explicación se genera usando Gemini (API), a partir de agregados SQL calculados en el backend.

Alcance limitado: solo Alternativa A (Botón Global). No intentamos leer el DOM del iframe ni capturar clicks en gráficos individuales.

---

## 2. Justificación

- Evita problemas de same-origin policy con iframes.
- No requiere cambios en Metabase ni metadata manual inicial.
- Entrega valor rápido y testable (implementación rápida: 2-4 días).
- Fácil de instrumentar con cache y rate-limits para controlar costos.

---

## 3. UX / Ubicación del botón

- Ubicar el botón en la esquina superior derecha del contenedor que muestra el iframe de Metabase (flotante o en la barra superior del contenedor).
- Al hacer clic se abre un pequeño modal que permite confirmar/ajustar el período (fecha inicio/fin) y ejecutar la generación.
- Mientras se genera, mostrar loading y luego mostrar un modal grande con la explicación en Markdown (renderizado con `react-markdown`).

Mock simple:

[Período: Último mes ▼]  [✨ Explicar Dashboard]

---

## 4. Flujo de datos (alto nivel)

1. Usuario clic en botón -> frontend envía POST a `/api/v1/ai/explain-dashboard` con `dashboardId`, `fechaInicio`, `fechaFin`, `filtros` opcionales.
2. Backend valida permisos y rate-limit.
3. Backend busca en cache (Caffeine) usando clave `(dashboardId, fechaInicio, fechaFin, filtros)`.
   - Si hit: devuelve resultado cacheado.
   - Si miss: ejecuta queries agregadas en la base de datos para ese período.
4. Backend construye prompt (system + contexto negocio + datos agregados) y llama a Gemini API (WebClient).
5. Gemini devuelve texto Markdown; backend audita y cachea (TTL 5 minutos), retorna al frontend.
6. Frontend renderiza Markdown en modal.

---

## 5. API Backend

### Endpoint

POST /api/v1/ai/explain-dashboard

Request (JSON):

```json
{
  "dashboardId": 5,
  "fechaInicio": "2025-06-01",
  "fechaFin": "2025-06-30",
  "filtros": {"turno": "Día"} // opcional
}
```

Response (200) - **FORMATO JSON** (cambiado de Markdown para parsing confiable):

```json
{
  "resumenEjecutivo": "En junio 2025 se produjeron 145,320 unidades (32.4 toneladas)...",
  "keyPoints": [
    "Top 3 operarios concentran el 31% de producción total",
    "Turno día supera al nocturno en 20% de eficiencia",
    "MAQ-001 procesó 35% del volumen, sugiriendo dependencia operacional"
  ],
  "insightsAccionables": [
    "Considerar capacitación para operarios con <5000 unidades/mes",
    "Evaluar mantenimiento preventivo en MAQ-001 por alto uso"
  ],
  "alertas": [
    "3 operarios sin registros en últimos 7 días",
    "SKU 45003 con 0 producción en junio"
  ],
  "metadata": {
    "dashboardId": 5,
    "titulo": "Producción por Operario",
    "fechaInicio": "2025-06-01",
    "fechaFin": "2025-06-30",
    "filtrosAplicados": {"turno": "Día"}
  },
  "generadoAt": "2025-11-11T22:00:00Z",
  "fromCache": false,
  "tokensUsados": 1245,
  "cacheTTLSeconds": 86400
}
```

---

### 5.1. DTOs Backend (Record Classes)

**DashboardExplanationRequest.java**:
```java
package com.cambiaso.ioc.dto.ai;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Map;

public record DashboardExplanationRequest(
    @NotNull(message = "Dashboard ID es requerido")
    @Min(value = 1, message = "Dashboard ID debe ser positivo")
    Integer dashboardId,
    
    @NotNull(message = "Fecha inicio es requerida")
    @PastOrPresent(message = "Fecha inicio no puede ser futura")
    LocalDate fechaInicio,
    
    @NotNull(message = "Fecha fin es requerida")
    @PastOrPresent(message = "Fecha fin no puede ser futura")
    LocalDate fechaFin,
    
    Map<String, Object> filtros // opcional
) {
    // Validación custom
    public DashboardExplanationRequest {
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("fechaFin debe ser >= fechaInicio");
        }
        if (fechaInicio.plusMonths(12).isBefore(fechaFin)) {
            throw new IllegalArgumentException("Rango máximo: 12 meses");
        }
    }
}
```

**DashboardExplanationResponse.java** (ACTUALIZADO - JSON response):
```java
package com.cambiaso.ioc.dto.ai;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record DashboardExplanationResponse(
    String resumenEjecutivo,
    List<String> keyPoints,
    List<String> insightsAccionables,
    List<String> alertas,  // NUEVO: alertas opcionales
    DashboardMetadata metadata,
    Instant generadoAt,
    Boolean fromCache,
    Integer tokensUsados,
    Integer cacheTTLSeconds  // NUEVO: TTL dinámico informado
) {}

public record DashboardMetadata(
    Integer dashboardId,
    String titulo,
    java.time.LocalDate fechaInicio,
    java.time.LocalDate fechaFin,
    Map<String, Object> filtrosAplicados
) {}
```

**GeminiJsonResponse.java** (NUEVO - para parsing de JSON de Gemini):
```java
package com.cambiaso.ioc.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Estructura esperada de la respuesta JSON de Gemini.
 * Gemini debe devolver ÚNICAMENTE este JSON, sin texto adicional.
 */
public record GeminiJsonResponse(
    @JsonProperty("resumen_ejecutivo") String resumenEjecutivo,
    @JsonProperty("key_points") List<String> keyPoints,
    @JsonProperty("insights") List<String> insights,
    @JsonProperty("alertas") List<String> alertas  // opcional
) {
    // Constructor compacto con validación
    public GeminiJsonResponse {
        if (resumenEjecutivo == null || resumenEjecutivo.isBlank()) {
            throw new IllegalArgumentException("resumen_ejecutivo es requerido");
        }
        if (keyPoints == null || keyPoints.isEmpty()) {
            throw new IllegalArgumentException("key_points debe tener al menos 1 elemento");
        }
        if (insights == null) {
            insights = List.of();
        }
        if (alertas == null) {
            alertas = List.of();
        }
    }
}
```

---

### 5.2. Manejo de Errores Detallado

**400 Bad Request** - Validación fallida:
```json
{
  "error": "VALIDATION_ERROR",
  "message": "fechaFin debe ser >= fechaInicio",
  "timestamp": "2025-11-11T22:00:00Z"
}
```

**401 Unauthorized** - JWT inválido/expirado:
```json
{
  "error": "UNAUTHORIZED",
  "message": "Token JWT inválido o expirado",
  "timestamp": "2025-11-11T22:00:00Z"
}
```

**403 Forbidden** - Sin permisos para dashboard:
```json
{
  "error": "FORBIDDEN",
  "message": "No tienes permisos para acceder al dashboard 5",
  "timestamp": "2025-11-11T22:00:00Z"
}
```

**429 Too Many Requests** - Rate limit excedido:
```json
{
  "error": "RATE_LIMIT_EXCEEDED",
  "message": "Máximo 5 explicaciones por minuto. Intenta en 45 segundos.",
  "retryAfter": 45,
  "timestamp": "2025-11-11T22:00:00Z"
}
```

**503 Service Unavailable** - Gemini API caída:
```json
{
  "error": "AI_SERVICE_UNAVAILABLE",
  "message": "Servicio de IA temporalmente no disponible. Intenta más tarde.",
  "timestamp": "2025-11-11T22:00:00Z"
}
```

**504 Gateway Timeout** - Timeout de 90s excedido:
```json
{
  "error": "AI_TIMEOUT",
  "message": "La generación tardó demasiado. Intenta con un período más corto.",
  "timestamp": "2025-11-11T22:00:00Z"
}
```

**Frontend debe manejar**:
- 429: Mostrar countdown "Intenta en {retryAfter}s"
- 503/504: Mostrar "Intenta más tarde" con botón de reintentar
- 400: Mostrar mensaje de error específico

---

## 6. Queries SQL (para construir datos resumidos)

Se ejecutarán agregaciones y se devolverán datos en estructuras compactas (JSON) para el prompt.

Query 1 - Totales generales:

```sql
SELECT
  COUNT(DISTINCT maquinista_fk) AS total_operarios,
  COUNT(DISTINCT maquina_fk) AS total_maquinas,
  SUM(cantidad) AS produccion_total,
  SUM(peso_neto) AS peso_total_kg,
  MIN(fecha_contabilizacion) AS fecha_inicio,
  MAX(fecha_contabilizacion) AS fecha_fin,
  COUNT(DISTINCT numero_pallet) AS total_pallets
FROM fact_production
WHERE fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin;
```

Query 2 - Top operarios:

```sql
SELECT
  dm.codigo_maquinista,
  dm.nombre_completo,
  COUNT(DISTINCT fp.fecha_contabilizacion) as dias_trabajados,
  SUM(fp.cantidad) as total_unidades,
  SUM(fp.peso_neto) as total_kg,
  ROUND(
    SUM(fp.cantidad)::numeric / 
    NULLIF(COUNT(DISTINCT fp.fecha_contabilizacion), 0),
    2
  ) as promedio_unidades_por_dia
FROM fact_production fp
JOIN dim_maquinista dm ON fp.maquinista_fk = dm.id
WHERE fp.fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
GROUP BY dm.id, dm.codigo_maquinista, dm.nombre_completo
ORDER BY total_unidades DESC
LIMIT 10;
```

Query 3 - Distribución por turno:

```sql
SELECT turno, SUM(cantidad) AS unidades, COUNT(DISTINCT maquinista_fk) AS operarios
FROM fact_production
WHERE fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
GROUP BY turno;
```

Query 4 - Top máquinas:

```sql
SELECT dm.codigo_maquina, dm.nombre_maquina, SUM(fp.cantidad) as total_unidades
FROM fact_production fp
JOIN dim_maquina dm ON fp.maquina_fk = dm.id
WHERE fp.fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
GROUP BY dm.id, dm.codigo_maquina, dm.nombre_maquina
ORDER BY total_unidades DESC
LIMIT 5;
```

Query 5 - Tendencia diaria:

```sql
SELECT fecha_contabilizacion, SUM(cantidad) as produccion_dia
FROM fact_production
WHERE fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
GROUP BY fecha_contabilizacion
ORDER BY fecha_contabilizacion;
```

---

## 7. Prompt Template (ACTUALIZADO - JSON Response)

### System Prompt (contexto negocio + modelo de datos)

```
Eres un analista de datos que explica dashboards de producción de la empresa Cambiaso.

MODELO DE DATOS:
- fact_production: fecha_contabilizacion, cantidad, peso_neto, maquina_fk, maquinista_fk, turno, material_sku, material_descripcion, numero_pallet, numero_log
- dim_maquinista: id, codigo_maquinista, nombre_completo
- dim_maquina: id, codigo_maquina, nombre_maquina

OBJETIVO: 
Generar un resumen ejecutivo (2-3 líneas), 3-5 key points y 1-3 insights accionables.

⚠️ IMPORTANTE: Responde ÚNICAMENTE con JSON válido en este formato EXACTO:

{
  "resumen_ejecutivo": "[2-3 líneas concisas con métricas principales]",
  "key_points": [
    "Hallazgo cuantitativo 1",
    "Tendencia o patrón 2",
    "Dato relevante 3"
  ],
  "insights": [
    "Recomendación accionable 1",
    "Acción prioritaria 2"
  ],
  "alertas": [
    "Alerta 1 (opcional, solo si detectas anomalías)",
    "Alerta 2 (opcional)"
  ]
}

REGLAS ESTRICTAS:
- NO agregues texto fuera del JSON
- NO uses markdown, emojis o formato especial
- Números con separadores de miles (ej: 15,234)
- Incluye unidades (unidades, kg, %, días)
- Si no hay alertas, usa array vacío: "alertas": []
- key_points: mínimo 3, máximo 5
- insights: mínimo 1, máximo 3
```

### User Prompt (datos agregados)

```
Dashboard: Producción por Operario
Periodo: 2025-06-01 a 2025-06-30

TOTALES:
- Producción total: 145,320 unidades
- Peso total: 32,340 kg
- Operarios activos: 18
- Máquinas utilizadas: 6
- Pallets procesados: 1,234

TOP 10 OPERARIOS (nombre, total_unidades, dias_trabajados, promedio_unidades_por_dia):
1. Carlos González: 15,234 unidades, 23 días, 662.35 unidades/día
2. María Pérez: 12,890 unidades, 22 días, 585.91 unidades/día
3. Juan Martínez: 11,450 unidades, 21 días, 545.24 unidades/día
...

DISTRIBUCIÓN POR TURNO:
- Día: 82,000 unidades (56.4%)
- Noche: 63,320 unidades (43.6%)

TOP MÁQUINAS:
1. MAQ-001 (Envasadora Principal): 50,862 unidades (35.0%)
2. MAQ-003 (Empacadora Secundaria): 29,064 unidades (20.0%)
...

TENDENCIA DIARIA (últimos 7 días):
- 2025-06-24: 5,234 unidades
- 2025-06-25: 7,890 unidades (pico: +50.8% vs promedio)
- 2025-06-26: 4,890 unidades
...

ALERTAS DETECTADAS (si aplican):
- Operarios inactivos: 3 (sin registros en últimos 7 días)
- SKUs sin producción: SKU 45003 (0 unidades en junio)

Genera el análisis en JSON siguiendo el formato exacto del system prompt.
```

### Parsing de Respuesta (Backend) - JSON PARSING

**DashboardExplanationService.java** debe incluir:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonMappingException;

@Service
@Slf4j
public class DashboardExplanationService {
    
    private final ObjectMapper objectMapper;
    private final GeminiApiClient geminiClient;
    
    @Autowired
    public DashboardExplanationService(
        ObjectMapper objectMapper,
        GeminiApiClient geminiClient
    ) {
        this.objectMapper = objectMapper;
        this.geminiClient = geminiClient;
    }
    
    /**
     * Parsea la respuesta JSON de Gemini.
     * Si el parsing falla, retorna fallback.
     */
    private DashboardExplanationResponse parseGeminiResponse(
        String jsonResponse, 
        DashboardMetadata metadata,
        int cacheTTLSeconds
    ) {
        try {
            // Limpiar posible texto adicional antes/después del JSON
            String cleanJson = extractJsonFromResponse(jsonResponse);
            
            // Parsear con Jackson
            GeminiJsonResponse geminiResponse = objectMapper.readValue(
                cleanJson, 
                GeminiJsonResponse.class
            );
            
            // Validación automática en el constructor compacto de GeminiJsonResponse
            
            return new DashboardExplanationResponse(
                geminiResponse.resumenEjecutivo(),
                geminiResponse.keyPoints(),
                geminiResponse.insights(),
                geminiResponse.alertas(),
                metadata,
                Instant.now(),
                false,
                estimateTokens(jsonResponse),
                cacheTTLSeconds
            );
            
        } catch (JsonMappingException e) {
            log.error("Error parsing Gemini JSON response (mapping): {}", e.getMessage());
            return createFallbackResponse(metadata, "Error de formato en respuesta de IA");
        } catch (Exception e) {
            log.error("Error parsing Gemini JSON response: {}", e.getMessage(), e);
            return createFallbackResponse(metadata, "Error procesando respuesta de IA");
        }
    }
    
    /**
     * Extrae el JSON de la respuesta, eliminando texto adicional si existe.
     * Gemini a veces devuelve: "Aquí está el análisis:\n{...}"
     */
    private String extractJsonFromResponse(String response) {
        // Buscar el primer { y el último }
        int startIndex = response.indexOf('{');
        int endIndex = response.lastIndexOf('}');
        
        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            log.warn("No se encontró JSON válido en respuesta de Gemini");
            return response; // Intentar parsear de todos modos
        }
        
        return response.substring(startIndex, endIndex + 1);
    }
    
    private DashboardExplanationResponse createFallbackResponse(
        DashboardMetadata metadata,
        String errorMessage
    ) {
        return new DashboardExplanationResponse(
            "No se pudo generar el análisis automático. " + errorMessage,
            List.of("Intenta nuevamente en unos momentos"),
            List.of(),
            List.of("Error en el servicio de IA"),
            metadata,
            Instant.now(),
            false,
            0,
            300 // Cache fallback 5 minutos
        );
    }
    
    private int estimateTokens(String text) {
        // Estimación: ~4 caracteres = 1 token (aproximado)
        return text.length() / 4;
    }
    
    /**
     * Calcula TTL de cache dinámicamente basado en frescura de datos.
     * OPTIMIZACIÓN CRÍTICA: Evita llamadas innecesarias a Gemini.
     */
    public int calculateCacheTTL(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDate today = LocalDate.now();
        
        // Datos históricos (no incluyen hoy): cache 24 horas
        if (fechaFin.isBefore(today)) {
            log.debug("Cache TTL: 24h (datos históricos)");
            return 86400; // 24 horas
        }
        
        // Datos actuales (incluyen hoy): cache 30 minutos
        if (fechaFin.equals(today) || fechaFin.isAfter(today)) {
            log.debug("Cache TTL: 30min (datos actuales/futuros)");
            return 1800; // 30 minutos
        }
        
        // Fallback
        return 300; // 5 minutos
    }
}
```

---

## 8. Implementación - Tareas detalladas (MVP)

Estimación total: **5-7 días** (backend + frontend + tests + ajustes)

Backend (3 días):
- [ ] Crear `GeminiApiClient` usando Spring WebClient (timeout 90s). Manejo de 429/503 con retry.
- [ ] Crear `DashboardExplanationController` con:
      - Endpoint POST `/api/v1/ai/explain-dashboard`
      - `@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER')")` 
      - `@RateLimiter(name = "aiExplanation")`
      - Validación adicional: usuario puede acceder a ese dashboard específico
      - **Logging de latencia por fase** (queries, prompt build, Gemini call)
- [ ] Implementar `DashboardAnalyticsRepository` con las queries usando **NamedParameterJdbcTemplate** (en lugar de JPA para estas consultas analíticas)
- [ ] Implementar `DashboardExplanationService` que:
  - Valida permisos del usuario
  - **Calcula TTL dinámico** con `calculateCacheTTL()`
  - Busca/guarda en cache (Caffeine) con TTL dinámico
  - Construye prompt (system + contexto negocio + datos agregados)
  - Llama a `GeminiApiClient` con timeout y retry
  - **Parsea JSON con Jackson** usando `parseGeminiResponse()`
  - Audita request (userId, dashboardId, tokensUsados, fromCache, cacheTTL)
  - **Registra métricas** (Counter, Timer, ver sección 8.2)
- [ ] Configurar properties: `gemini.api-key`, `gemini.model`, `gemini.timeout=90s`, `gemini.retry`, `cache.ttl-*`
- [ ] Crear **DTOs**: `GeminiJsonResponse`, `DashboardExplanationResponse` (actualizado) y DTOs analíticos (`TotalsDto`, `TopOperarioDto`, `TurnoDistributionDto`)
- [ ] **Tests unitarios** (ver sección 8.1)
- [ ] **Tests de integración** (ver sección 8.1)

Frontend (2 días):
- [ ] Instalar dependencias:
      ```bash
      npm install @tanstack/react-query@^5.0.0  # Para manejo de estado async
      ```
- [ ] Crear `components/dashboard/ExplainDashboardButton.tsx`:
      - Botón flotante en esquina superior derecha
      - **Loading phases con mensajes** (ver código abajo)
      - Modal de selección de período (opcional)
- [ ] Crear `components/dashboard/ExplanationModal.tsx`:
      - Renderiza JSON response (NO usa react-markdown)
      - Secciones: Resumen, Key Points, Insights, Alertas
      - Botones: Copiar, Cerrar, Regenerar
      - Muestra metadata (período, fromCache, cacheTTL)
- [ ] **Modificar `src/components/DashboardEmbed.tsx`**:
      - Agregar wrapper `<div className="relative">` alrededor del iframe
      - Incluir `<ExplainDashboardButton dashboardId={dashboardId} />` como hijo
      - Posición: absolute top-4 right-4
- [ ] Modificar `src/pages/DashboardsPage.tsx` y `src/pages/GerencialDashboardPage.tsx`:
      - Verificar que pasan `dashboardId` correctamente a `DashboardEmbed`
- [ ] Crear `src/services/aiExplanationService.ts` con llamada a API
- [ ] **Manejo de estados de loading con phases**:
```typescript
// ExplainDashboardButton.tsx - LOADING PHASES IMPLEMENTATION
const LOADING_PHASES = [
  { time: 0, message: "📊 Consultando datos de producción..." },
  { time: 15000, message: "🔍 Analizando métricas y tendencias..." },
  { time: 30000, message: "🤖 Generando insights con IA..." },
  { time: 60000, message: "✨ Finalizando análisis..." }
];

const [currentPhase, setCurrentPhase] = useState(0);
const [isLoading, setIsLoading] = useState(false);

useEffect(() => {
  if (!isLoading) return;
  
  const intervals = LOADING_PHASES.map((phase, index) => 
    setTimeout(() => setCurrentPhase(index), phase.time)
  );
  
  return () => intervals.forEach(clearTimeout);
}, [isLoading]);

// Render
{isLoading && (
  <div className="flex items-center gap-2">
    <Loader2 className="animate-spin" />
    <span>{LOADING_PHASES[currentPhase].message}</span>
  </div>
)}
```
- [ ] **Manejo de errores con react-hot-toast**:
      - 429: Countdown "Intenta en {retryAfter}s"
      - 503/504: Botón de reintentar
      - 400: Mensaje de error específico
- [ ] **Tests de componentes** (ver sección 8.1)

Testing y despliegue (2 días):
- [ ] **Tests unitarios backend** (parsing JSON, cache TTL, validaciones)
- [ ] **Tests de integración backend** (endpoint completo, rate limiting)
- [ ] **Tests de componentes frontend** (loading, modal, errores)
- [ ] **Tests manuales** (ver checklist sección 8.1)
- [ ] Iterar prompts hasta obtener calidad consistente (3-5 iteraciones)
- [ ] Deploy a staging y validar con 2-3 usuarios
- [ ] Habilitar **solo a ROLE_ADMIN** inicialmente

---

### 8.1. Estrategia de Testing (NUEVO)

#### Backend - Unit Tests

```java
package com.cambiaso.ioc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.*;

@DisplayName("DashboardExplanationService - Unit Tests")
class DashboardExplanationServiceTest {
    
    @Test
    @DisplayName("Parsing de JSON válido debe retornar respuesta correcta")
    void whenGeminiReturnsValidJson_thenParseCorrectly() {
        String mockJsonResponse = """
            {
              "resumen_ejecutivo": "En junio 2025 se produjeron 145,320 unidades",
              "key_points": [
                "Top 3 operarios concentran 31% del total",
                "Turno día supera al nocturno en 20%"
              ],
              "insights": [
                "Considerar capacitación para operarios con bajo rendimiento"
              ],
              "alertas": [
                "3 operarios sin registros en últimos 7 días"
              ]
            }
            """;
        
        DashboardExplanationResponse response = service.parseGeminiResponse(
            mockJsonResponse, 
            testMetadata,
            3600
        );
        
        assertThat(response.resumenEjecutivo()).contains("145,320 unidades");
        assertThat(response.keyPoints()).hasSize(2);
        assertThat(response.insightsAccionables()).hasSize(1);
        assertThat(response.alertas()).hasSize(1);
        assertThat(response.fromCache()).isFalse();
        assertThat(response.cacheTTLSeconds()).isEqualTo(3600);
    }
    
    @Test
    @DisplayName("JSON con texto adicional debe ser limpiado y parseado")
    void whenGeminiReturnsJsonWithExtraText_thenExtractAndParse() {
        String responseWithExtraText = """
            Aquí está el análisis solicitado:
            
            {
              "resumen_ejecutivo": "Test resumen",
              "key_points": ["Punto 1"],
              "insights": ["Insight 1"],
              "alertas": []
            }
            
            Espero que sea útil.
            """;
        
        DashboardExplanationResponse response = service.parseGeminiResponse(
            responseWithExtraText,
            testMetadata,
            1800
        );
        
        assertThat(response.resumenEjecutivo()).isEqualTo("Test resumen");
        assertThat(response.keyPoints()).containsExactly("Punto 1");
    }
    
    @Test
    @DisplayName("JSON malformado debe retornar fallback")
    void whenGeminiReturnsMalformedJson_thenReturnFallback() {
        String malformedJson = "This is not valid JSON";
        
        DashboardExplanationResponse response = service.parseGeminiResponse(
            malformedJson,
            testMetadata,
            300
        );
        
        assertThat(response.resumenEjecutivo()).contains("No se pudo generar");
        assertThat(response.keyPoints()).hasSize(1);
        assertThat(response.keyPoints().get(0)).contains("Intenta nuevamente");
        assertThat(response.alertas()).contains("Error en el servicio de IA");
    }
    
    @Test
    @DisplayName("Cache TTL debe ser 24h para datos históricos")
    void whenDataIsHistorical_thenCacheTTLIs24Hours() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate lastWeek = LocalDate.now().minusDays(7);
        
        int ttl = service.calculateCacheTTL(lastWeek, yesterday);
        
        assertThat(ttl).isEqualTo(86400); // 24 horas
    }
    
    @Test
    @DisplayName("Cache TTL debe ser 30min para datos actuales")
    void whenDataIncludesToday_thenCacheTTLIs30Minutes() {
        LocalDate today = LocalDate.now();
        LocalDate lastWeek = LocalDate.now().minusDays(7);
        
        int ttl = service.calculateCacheTTL(lastWeek, today);
        
        assertThat(ttl).isEqualTo(1800); // 30 minutos
    }
}
```

#### Backend - Integration Tests

```java
package com.cambiaso.ioc.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("DashboardExplanation - Integration Tests")
class DashboardExplanationIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private GeminiApiClient geminiClient;
    
    private static final String VALID_JSON_RESPONSE = """
        {
          "resumen_ejecutivo": "Test resumen ejecutivo",
          "key_points": ["Punto 1", "Punto 2"],
          "insights": ["Insight 1"],
          "alertas": []
        }
        """;
    
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Request válido debe retornar 200 con explicación")
    void whenValidRequest_thenReturn200WithExplanation() throws Exception {
        when(geminiClient.generateContent(any()))
            .thenReturn(VALID_JSON_RESPONSE);
        
        mockMvc.perform(post("/api/v1/ai/explain-dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "dashboardId": 5,
                        "fechaInicio": "2025-06-01",
                        "fechaFin": "2025-06-30"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resumenEjecutivo").value("Test resumen ejecutivo"))
            .andExpect(jsonPath("$.keyPoints").isArray())
            .andExpect(jsonPath("$.keyPoints.length()").value(2))
            .andExpect(jsonPath("$.fromCache").value(false))
            .andExpect(jsonPath("$.cacheTTLSeconds").exists());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("6 requests rápidas deben activar rate limiting")
    void whenRateLimitExceeded_thenReturn429() throws Exception {
        when(geminiClient.generateContent(any()))
            .thenReturn(VALID_JSON_RESPONSE);
        
        String requestBody = """
            {
                "dashboardId": 5,
                "fechaInicio": "2025-06-01",
                "fechaFin": "2025-06-30"
            }
            """;
        
        // Hacer 6 requests rápidas (límite es 5/min)
        for (int i = 0; i < 6; i++) {
            var result = mockMvc.perform(
                post("/api/v1/ai/explain-dashboard")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            );
            
            if (i < 5) {
                result.andExpect(status().isOk());
            } else {
                result.andExpect(status().isTooManyRequests())
                      .andExpect(jsonPath("$.error").value("RATE_LIMIT_EXCEEDED"))
                      .andExpect(jsonPath("$.retryAfter").exists());
            }
        }
    }
    
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Validación debe fallar si fechaFin < fechaInicio")
    void whenInvalidDateRange_thenReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/ai/explain-dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "dashboardId": 5,
                        "fechaInicio": "2025-06-30",
                        "fechaFin": "2025-06-01"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("fechaFin")));
    }
}
```

#### Frontend - Component Tests

```typescript
// ExplainDashboardButton.test.tsx
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { server } from '@/test/mocks/server';
import { http, HttpResponse } from 'msw';
import { ExplainDashboardButton } from './ExplainDashboardButton';

describe('ExplainDashboardButton', () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } }
  });
  
  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  );
  
  it('muestra loading phases durante generación', async () => {
    const user = userEvent.setup();
    
    // Mock con delay para ver phases
    server.use(
      http.post('/api/v1/ai/explain-dashboard', async () => {
        await new Promise(resolve => setTimeout(resolve, 20000));
        return HttpResponse.json(mockSuccessResponse);
      })
    );
    
    render(<ExplainDashboardButton dashboardId={5} />, { wrapper });
    
    const button = screen.getByText('Explicar Dashboard');
    await user.click(button);
    
    // Verificar primera fase
    expect(screen.getByText(/Consultando datos/)).toBeInTheDocument();
    
    // Avanzar 15 segundos
    await waitFor(() => {
      expect(screen.getByText(/Analizando métricas/)).toBeInTheDocument();
    }, { timeout: 16000 });
  });
  
  it('muestra modal con explicación al recibir respuesta', async () => {
    const user = userEvent.setup();
    
    server.use(
      http.post('/api/v1/ai/explain-dashboard', () => {
        return HttpResponse.json({
          resumenEjecutivo: 'Resumen de prueba',
          keyPoints: ['Punto 1', 'Punto 2'],
          insightsAccionables: ['Insight 1'],
          alertas: [],
          metadata: { dashboardId: 5, titulo: 'Test' },
          generadoAt: '2025-11-11T22:00:00Z',
          fromCache: false,
          tokensUsados: 1000,
          cacheTTLSeconds: 3600
        });
      })
    );
    
    render(<ExplainDashboardButton dashboardId={5} />, { wrapper });
    
    await user.click(screen.getByText('Explicar Dashboard'));
    
    await waitFor(() => {
      expect(screen.getByText('Análisis del Dashboard')).toBeInTheDocument();
      expect(screen.getByText('Resumen de prueba')).toBeInTheDocument();
      expect(screen.getByText('Punto 1')).toBeInTheDocument();
    });
  });
  
  it('maneja error 429 con countdown', async () => {
    const user = userEvent.setup();
    
    server.use(
      http.post('/api/v1/ai/explain-dashboard', () => {
        return HttpResponse.json(
          {
            error: 'RATE_LIMIT_EXCEEDED',
            message: 'Límite alcanzado',
            retryAfter: 45
          },
          { status: 429 }
        );
      })
    );
    
    render(<ExplainDashboardButton dashboardId={5} />, { wrapper });
    
    await user.click(screen.getByText('Explicar Dashboard'));
    
    await waitFor(() => {
      expect(screen.getByText(/Intenta en 45 segundos/)).toBeInTheDocument();
    });
  });
});
```

#### Checklist de Tests Manuales

```markdown
## Checklist de Tests Manuales - FP-001A

### 🟢 Casos Happy Path
- [ ] Dashboard ID 5, último mes → Explicación coherente con JSON parseado
- [ ] Mismo request 2 veces → Segunda vez fromCache: true, cacheTTLSeconds informado
- [ ] Request histórico (mes pasado) → cacheTTLSeconds = 86400 (24h)
- [ ] Request actual (incluye hoy) → cacheTTLSeconds = 1800 (30min)
- [ ] Loading phases se muestran correctamente (0s, 15s, 30s, 60s)
- [ ] Modal muestra: resumen, key points, insights, alertas (si existen)

### 🔴 Casos de Error
- [ ] 6 requests en 1 minuto → 429 con retryAfter y countdown visible
- [ ] Gemini retorna JSON inválido → Fallback visible con mensaje claro
- [ ] fechaFin < fechaInicio → 400 con mensaje de validación
- [ ] Rango > 12 meses → 400 con mensaje
- [ ] Sin autenticación → 401
- [ ] Usuario sin permisos → 403

### ⚡ Performance
- [ ] Request con datos de 1 mes → Completado en < 60s (p95)
- [ ] Cache hit (<24h histórico) → Respuesta instantánea (< 200ms)
- [ ] Cold start en Render → Primera request < 120s

### 🎨 UX
- [ ] Botón flotante visible en esquina superior derecha de DashboardEmbed
- [ ] Loading phases se actualizan cada 15 segundos
- [ ] Modal responsivo y se cierra correctamente
- [ ] JSON se renderiza como texto legible (no raw JSON)
- [ ] Botón "Copiar" copia texto formateado

### 📊 Calidad de Explicación
- [ ] Resumen ejecutivo coherente (2-3 líneas, métricas correctas)
- [ ] Key points cuantitativos (3-5, con números verificados contra BD)
- [ ] Insights accionables (1-3, recomendaciones concretas)
- [ ] No inventa datos (verificar contra queries SQL)
- [ ] Detecta alertas reales (operarios inactivos, SKUs sin producción)
```

---

### 8.2. Métricas y Monitoreo (NUEVO - Opcional)

```java
package com.cambiaso.ioc.service;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Service;

@Service
public class DashboardExplanationService {
    
    private final Counter explanationsGenerated;
    private final Counter explanationsFromCache;
    private final Timer explanationDuration;
    private final Counter geminiErrors;
    private final Counter tokensConsumed;
    
    @Autowired
    public DashboardExplanationService(MeterRegistry registry, ...) {
        this.explanationsGenerated = Counter.builder("explanations.generated.total")
            .description("Total de explicaciones generadas")
            .tag("source", "gemini")
            .register(registry);
            
        this.explanationsFromCache = Counter.builder("explanations.cache.hit.total")
            .description("Explicaciones servidas desde cache")
            .register(registry);
            
        this.explanationDuration = Timer.builder("explanations.duration.seconds")
            .description("Tiempo de generación de explicación")
            .register(registry);
            
        this.geminiErrors = Counter.builder("explanations.errors.total")
            .description("Errores en generación")
            .tag("type", "gemini_api")
            .register(registry);
            
        this.tokensConsumed = Counter.builder("explanations.tokens.consumed.total")
            .description("Tokens consumidos de Gemini API")
            .register(registry);
    }
    
    public DashboardExplanationResponse explain(DashboardExplanationRequest request) {
        Timer.Sample sample = Timer.start();
        
        try {
            // ... lógica de explicación
            
            if (fromCache) {
                explanationsFromCache.increment();
            } else {
                explanationsGenerated.increment();
                tokensConsumed.increment(response.tokensUsados());
            }
            
            return response;
            
        } catch (Exception e) {
            geminiErrors.increment();
            throw e;
        } finally {
            sample.stop(explanationDuration);
        }
    }
}
```

**Dashboard de Grafana sugerido** (queries Prometheus):
```promql
# Requests por minuto
rate(explanations_generated_total[1m])

# Cache hit rate (%)
(rate(explanations_cache_hit_total[5m]) / 
 (rate(explanations_generated_total[5m]) + rate(explanations_cache_hit_total[5m]))) * 100

# Latencia p95
histogram_quantile(0.95, rate(explanations_duration_seconds_bucket[5m]))

# Tokens consumidos (proyección de costos)
rate(explanations_tokens_consumed_total[1h]) * 3600  # tokens/hora

# Tasa de error
rate(explanations_errors_total[5m])
```

---

## 9. Configuración y variables necesarias

- `GEMINI_API_KEY` (env) - clave de API de Google AI Studio
- `gemini.model` (application.yml) - modelo a usar: `gemini-1.5-flash`
- `gemini.timeout` - **90s** (aumentado de 30s para evitar timeouts)
- `gemini.retry` - configuración de reintentos para errores 503/504
- Resilience4j rate limiter: `aiExplanation` limitada a 5 req/min por usuario

Snippet `application.yml` (ACTUALIZADO):

```yaml
gemini:
  api-key: ${GEMINI_API_KEY}
  model: gemini-1.5-flash
  timeout: 90s
  retry:
    maxAttempts: 2
    backoffMs: 1000
    
resilience4j:
  ratelimiter:
    instances:
      aiExplanation:
        limitForPeriod: 5
        limitRefreshPeriod: 60s
        timeoutDuration: 0s
        
spring:
  cache:
    caffeine:
      spec: maximumSize=100,expireAfterWrite=24h  # TTL máximo
    cache-names:
      - dashboard-explanations

# Cache configuration (TTL dinámico calculado en runtime)
cache:
  dashboard-explanations:
    ttl-historical: 86400     # 24h para datos pasados
    ttl-current: 1800         # 30min para datos actuales
    ttl-default: 300          # 5min fallback
    maxSize: 100

# Feature flags (rollout gradual)
features:
  ai-explanations:
    enabled: true
    allowed-roles: [ROLE_ADMIN]  # Fase 1: Solo admins
    # Fase 2: [ROLE_ADMIN, ROLE_MANAGER]
    # Fase 3: [ROLE_ADMIN, ROLE_MANAGER, ROLE_USER]
```

---

## 10. Consideraciones importantes

### Timeouts y Retries

⚠️ **Gemini 1.5 Flash puede tardar 10-60s** con prompts largos (>1000 tokens).

**Estrategia**:
- Timeout: **90s** (suficiente para p99 de respuestas)
- Retry: 2 intentos con backoff 1s (solo para errores 503/504)
- NO reintentar 429 (rate limit) automáticamente
- **Frontend: Loading phases cada 15s** para mejorar UX percibida

**Render Deployment**:
- Cold start agrega 20-30s adicionales en Render free tier
- Primera request post-inactividad puede tardar hasta 120s total
- Considerar endpoint warmup opcional: `GET /api/v1/ai/warmup` (return 200 OK)

### Control de Datos y Cache

- Gemini no "ve" gráficos: siempre enviar datos agregados explícitos
- **Cache inteligente**: TTL dinámico según frescura de datos (24h histórico, 30min actual)
- Control de tokens: JSON es más compacto que Markdown (~30% menos tokens)
- Seguridad: no enviar datos sensibles (PII) si no es necesario; auditar prompts enviados
- Rate limits del free tier: cache + rate limiter + mensajes claros de error

### Formato de Respuesta: JSON vs Markdown

**✅ Decisión: JSON** (parsing 100% confiable con Jackson)

Ventajas:
- Parsing garantizado sin regex frágil
- Validación automática de estructura con records
- Menos tokens consumidos
- Control total en frontend del formato visual

Desventaja:
- Frontend debe formatear (pero simple: bullets + secciones)

### Seguridad y RBAC

- Endpoint protegido con `@PreAuthorize` para roles ADMIN, MANAGER, USER
- Validación adicional: verificar que usuario tiene permisos para ese dashboard específico
- Reutilizar `DashboardSecurityService` si existe en el proyecto
- O implementar check manual contra configuración de dashboards permitidos
- **Rollout gradual**: Fase 1 solo ADMIN, Fase 2 + MANAGER, Fase 3 GA

### Free Tier Limits y Migración a Paid

**Límites actuales (Free Tier)**:
- 15 RPM (requests por minuto)
- 1,500 RPD (requests por día)
- 1M TPM (tokens por minuto)

**Escenario límite**:
- 3 usuarios simultáneos × 5 req/min = 15 RPM → **LÍMITE ALCANZADO**
- ⚠️ Free tier OK para **<= 3 usuarios concurrentes**

**Plan de migración a Paid Tier**:
- Gemini 1.5 Flash: $0.075 / 1M tokens input, $0.30 / 1M tokens output
- Estimación: 2000 tokens/request = **$0.00015/request**
- 10,000 requests/mes = **$1.50/mes** (muy económico)
- **Umbral de migración**: >5 usuarios concurrentes o >500 requests/día

---

## 11. Checklist pre-desarrollo (acción requerida)

- [x] Confirmar: queremos Alternativa A (Botón Global) ✅
- [x] **Formato de respuesta: JSON** (más confiable que Markdown) ✅
- [x] **Componente frontend identificado**: `DashboardEmbed.tsx` ✅
- [ ] Proveer contexto de negocio básico (ver sección 12) para integrar en System Prompt
- [x] Obtener `GEMINI_API_KEY` (Makersuite) ✅
- [ ] Decidir si anonimizar nombres de operarios (recomendado: NO para valor interno)
- [x] **Decisión de persistencia tomada: `NamedParameterJdbcTemplate` seleccionado para queries analíticas** ✅
- [ ] Habilitar solo a **ROLE_ADMIN** inicialmente (feature flag)

---

## 12. Contexto de negocio mínimo requerido (para System Prompt)

He externalizado el contexto de negocio en un archivo YAML para facilitar su edición y reutilización por el backend.

- Archivo: `.gemini/sprints/feature-plans/FP-001A-context.yaml`

Por favor completa ese YAML con los valores reales del negocio. Contiene campos para:
- Metadatos de la empresa (nombre, industria, descripción)
- Productos/SKUs (sku, nombre, objetivo de share)
- Métricas clave (qué significa `cantidad`, objetivos mensuales y diarios)
- Turnos y jornadas (horarios y expectativas)
- Máquinas críticas y tipos
- Política de anonimización de operarios (PII)
- Metadata por dashboard (títulos, graficos sugeridos)
- Reglas de data-quality (nulls, outliers)
- Guidelines para prompts de Gemini (estructura obligatoria, tokens)
- Políticas operacionales (cache TTL, rate limits)

Cuando completes el YAML puedo:
1. Integrarlo en el System Prompt que usará el backend (reemplazar placeholders).
2. Generar automáticamente la versión definitiva del System Prompt y ofrecértela para revisión.

(El YAML ya fue creado en la ruta indicada: `.gemini/sprints/feature-plans/FP-001A-context.yaml`)
---

## 13. Métricas de éxito (MVP)

- Tiempo promedio para obtener explicación: **< 60s** (p95) - ajustado de 8s
- Cache hit rate: **>= 60%** en primeras 2 semanas (aumentado por cache inteligente)
- Adoption: >= 30% de usuarios ADMIN usan la función al menos 1 vez/semana
- Feedback útil: >= 70% (si implementas botones 👍👎)
- **Parsing success rate: >= 95%** (JSON vs ~70% con Markdown regex)
- **Error rate: < 5%** (4xx + 5xx + Gemini API failures)

---

## 14. Próximo paso recomendado

### ✅ APROBADO PARA IMPLEMENTACIÓN

**Pre-implementación completado**:
- [x] GEMINI_API_KEY disponible
- [x] Componente frontend identificado (`DashboardEmbed.tsx`)
- [x] Decisión tomada: JSON response (no Markdown)
- [x] Ajustes críticos aplicados al plan

**Siguiente acción inmediata**:

1. **Completar contexto de negocio** (YAML sección 12) - 30 min
2. **Iniciar implementación backend** (día 1-3):
   - GeminiApiClient con JSON parsing
   - DashboardExplanationService con cache inteligente
   - DTOs actualizados (GeminiJsonResponse)
   - Tests unitarios
3. **Implementación frontend** (día 4-5):
   - Modificar DashboardEmbed.tsx (agregar botón)
   - ExplainDashboardButton con loading phases
   - ExplanationModal con renderizado de JSON
4. **Testing y ajustes** (día 6-7):
   - Tests de integración
   - Iteración de prompts
   - Deploy a staging
   - Validación con usuarios ADMIN

**Estimación final**: 5-7 días para MVP production-ready 🚀

---

## 15. DECISIÓN DE PERSISTENCIA (DOCUMENTADA)

Resumen ejecutivo:
- Decisión tomada: usar `NamedParameterJdbcTemplate` para todas las consultas analíticas necesarias para FP-001A.
- Mantener JPA/Hibernate para el resto del dominio (entidades CRUD, relaciones, repositorios existentes).

Razonamiento técnico (breve):
- Permite control fino del SQL necesario para construir prompts compactos y precisos.
- Facilita queries parametrizadas (evita concatenación), mejor manejo de COALESCE/NULLS y agregaciones complejas.
- Se integra fácilmente en el proyecto actual (ya usa `JdbcTemplate` y tiene DataSource/Hikari configurados).

Impacto en el plan:
- Cambia: las queries analíticas se implementarán en un nuevo repositorio `DashboardAnalyticsRepository` usando `NamedParameterJdbcTemplate` y se mapearán a DTOs en `com.cambiaso.ioc.dto.analytics`.
- No cambia: el modelo de datos existente ni los repositorios JPA actuales.

Pasos mínimos para la implementación (tácticos):
1. Crear `DashboardAnalyticsRepository` en `com.cambiaso.ioc.repository.analytics` que inyecte `NamedParameterJdbcTemplate`.
2. Implementar métodos: `fetchTotals`, `fetchTopOperarios`, `fetchTurnoDistribution`, `fetchTendenciaDiaria` (si aplica).
3. Crear DTOs (records) en `com.cambiaso.ioc.dto.analytics` y RowMappers.
4. Integrar el repository en `DashboardExplanationService` para construir el prompt.
5. Añadir tests unitarios (mock JdbcTemplate) e integration tests (Testcontainers/Postgres) para validar SQL y mapeo.
6. Monitorizar latencias y ajustar índices si las consultas no cumplen objetivos de p95.

Notas operativas:
- Spring Boot autoconfigura `NamedParameterJdbcTemplate` si hay un `DataSource`. Si no, exponer manualmente un bean con `new NamedParameterJdbcTemplate(dataSource)`.
- Considerar uso de réplicas de lectura o materialized views si las aggregaciones son costosas a producción.

