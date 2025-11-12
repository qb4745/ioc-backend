# 🔍 REPORTE DE EVALUACIÓN: Feature Plan FP-001A

**Documento Evaluado**: FP-001A-dashboard-ai-explanation-A.md  
**Fecha de Evaluación**: 2025-11-11  
**Evaluador**: AI Self-Evaluation System  
**Versión del Metaprompt**: 1.0

---

## FASE 1: CONTEXTO Y PREPARACIÓN

### 🔍 Identificación de Salida

**Salida detectada**:
- **Tipo**: Feature Plan (documento técnico-funcional)
- **Nombre**: "Feature Plan (Alternativa A - MVP): Explicar Dashboard con Gemini AI"
- **ID**: FP-001A
- **Ubicación**: `.gemini/sprints/feature-plans/FP-001A-dashboard-ai-explanation-A.md`
- **Tamaño**: ~400 líneas, 14 secciones
- **Estado**: DRAFT - Ready for implementation
- **Propósito**: Especificación técnica para implementar botón de explicación de dashboards con IA

**Contexto del proyecto**:
- **Proyecto**: IOC Backend (Inteligencia Operacional Cambiaso)
- **Stack**: Spring Boot 3.5.5 + React 19 + PostgreSQL + Metabase
- **Feature Nueva**: Integración con Gemini API para generar explicaciones automáticas
- **Audiencia**: Equipo de desarrollo (backend + frontend)

✅ **Salida identificada correctamente**

---

## FASE 2: EVALUACIÓN SISTEMÁTICA

### 📋 EVALUACIÓN: COMPLETITUD

**Criterio**: ¿El Feature Plan incluye todo lo necesario para implementar la feature?

#### Elementos Esperados en un Feature Plan

- ✅ Objetivo claro y alcance definido
- ✅ Justificación de la solución (sección 2)
- ✅ Diseño de UX/UI (sección 3)
- ✅ Flujo de datos técnico (sección 4)
- ✅ Especificación de API (sección 5)
- ✅ Queries SQL detalladas (sección 6)
- ✅ Prompts para Gemini (sección 7)
- ✅ Plan de implementación con tareas (sección 8)
- ✅ Configuración necesaria (sección 9)
- ✅ Consideraciones técnicas (sección 10)
- ✅ Checklist pre-desarrollo (sección 11)
- ✅ Métricas de éxito (sección 13)
- ⚠️ **DTOs backend** (mencionados pero no especificados completamente)
- ❌ **Casos de error específicos** (solo mencionados códigos HTTP, sin detalles)
- ❌ **Estrategia de testing** (solo dice "test manual", no hay tests unitarios/integración)
- ❌ **Mockups visuales** (solo texto descriptivo, sin wireframes)
- ❌ **Estimación de esfuerzo por tarea** (solo total "2-4 días", sin breakdown)
- ❌ **Dependencias de otras features/PRs**

#### Hallazgos

**Elementos faltantes** [NIVEL]:

1. **[IMPORTANTE 🟡]** Falta especificación completa de DTOs
   - Ubicación: Sección 5 (API Backend)
   - Problema: Menciona request/response JSON pero no hay clases Java
   - Debería incluir:
     ```java
     // DashboardExplanationRequest.java
     public record DashboardExplanationRequest(
         @NotNull @Min(1) Integer dashboardId,
         @NotNull LocalDate fechaInicio,
         @NotNull LocalDate fechaFin,
         Map<String, Object> filtros
     ) {}
     
     // DashboardExplanationResponse.java
     public record DashboardExplanationResponse(
         String explicacionCompleta,
         String resumenEjecutivo,
         List<String> keyPoints,
         List<String> insightsAccionables,
         DashboardMetadata metadata,
         Instant generadoAt,
         Boolean fromCache
     ) {}
     ```
   - Impacto: Desarrollador debe inferir estructura, posible inconsistencia

2. **[IMPORTANTE 🟡]** Manejo de errores no detallado
   - Ubicación: Sección 5
   - Problema: Solo lista códigos (400, 401, 429, 503) sin bodies de respuesta
   - Debería incluir:
     ```json
     // 429 Too Many Requests
     {
       "error": "RATE_LIMIT_EXCEEDED",
       "message": "Máximo 5 explicaciones por minuto. Intenta en 45 segundos.",
       "retryAfter": 45,
       "timestamp": "2025-11-11T22:00:00Z"
     }
     
     // 503 Gemini Unavailable
     {
       "error": "AI_SERVICE_UNAVAILABLE", 
       "message": "Servicio de IA temporalmente no disponible.",
       "timestamp": "2025-11-11T22:00:00Z"
     }
     ```
   - Impacto: Frontend no sabe qué mostrar al usuario

3. **[MEJORA 🔵]** Sin estrategia de testing clara
   - Ubicación: Sección 8 (solo dice "test manual")
   - Problema: No especifica tests unitarios, de integración, ni E2E
   - Debería incluir:
     ```
     Tests Backend:
     - Unit: GeminiApiClientTest (mock HTTP)
     - Unit: DashboardExplanationServiceTest (mock cache + API)
     - Integration: DashboardExplanationControllerTest (Testcontainers)
     
     Tests Frontend:
     - Unit: ExplainDashboardButton.test.tsx (mock API)
     - Unit: ExplanationModal.test.tsx (render Markdown)
     - E2E: dashboard-explanation.spec.ts (Playwright/Cypress)
     ```
   - Impacto: Calidad del código no garantizada

4. **[MEJORA 🔵]** Sin mockups visuales
   - Ubicación: Sección 3 (UX)
   - Problema: Solo texto "esquina superior derecha", no hay imagen
   - Debería incluir: ASCII art o link a Figma
   - Impacto: Posible desalineación de expectativas UX

5. **[MEJORA 🔵]** Estimaciones no granulares
   - Ubicación: Sección 8
   - Problema: Dice "Backend (2 días)" sin breakdown por tarea
   - Debería incluir:
     ```
     Backend:
     - GeminiApiClient: 3h
     - DashboardExplanationController: 2h
     - DashboardDataService (5 queries): 4h
     - DashboardExplanationService: 3h
     - Tests unitarios: 2h
     - Configuración: 1h
     Total: 15h (~2 días)
     ```
   - Impacto: Difícil trackear progreso

6. **[OPCIONAL 🟢]** Sin análisis de dependencias
   - Problema: No menciona si requiere PRs previos
   - Impacto: Menor (asume todo está disponible)

### Score de Completitud

**Cálculo**: 100 - (0×15 + 2×8 + 3×3 + 1×1) = 100 - 26 = **74% COMPLETO** 🟡

---

### 🎯 EVALUACIÓN: PRECISIÓN

**Criterio**: ¿La información técnica es correcta y ejecutable?

#### Análisis

**Verificación de especificaciones técnicas**:

- ✅ Endpoint REST bien formado (`POST /api/v1/ai/explain-dashboard`)
- ✅ Queries SQL sintácticamente correctas
- ✅ Configuración `application.yml` válida
- ✅ Rate limiter config compatible con Resilience4j
- ⚠️ **Prompt template sin estructura de salida garantizada**
- ❌ **Timeout 30s puede ser insuficiente** (Gemini puede tardar más)
- ❌ **No especifica cómo parsear la respuesta de Gemini**
- ⚠️ Query 2 usa `AVG(fp.cantidad)` que puede ser engañoso (promedio de registros, no diario real)

#### Hallazgos

**Imprecisiones detectadas** [NIVEL]:

1. **[CRÍTICO 🔴]** Timeout de 30s puede causar timeouts frecuentes
   - Ubicación: Sección 9 (`gemini.timeout: 30s`)
   - Problema: Gemini 1.5 Flash puede tardar 10-60s con prompts largos
   - Solución: Cambiar a 60s o 90s + agregar retry con backoff
   - Verificación: Testing con prompts reales mostrará timeouts
   - Impacto: Feature fallará en producción con datos complejos

2. **[IMPORTANTE 🟡]** Sin especificación de parsing de respuesta Gemini
   - Ubicación: Sección 7 (Prompt Template)
   - Problema: Gemini puede devolver Markdown en formato variable
   - Solución: Especificar estructura JSON o usar delimitadores:
     ```
     System Prompt adicional:
     "Usa SIEMPRE este formato exacto:
     # 📊 RESUMEN EJECUTIVO
     [2-3 líneas]
     
     # 🔑 KEY POINTS
     • [punto 1]
     • [punto 2]
     
     # ⚡ INSIGHTS ACCIONABLES
     • [insight 1]"
     ```
   - Impacto: Respuestas inconsistentes, frontend puede fallar al parsear

3. **[IMPORTANTE 🟡]** Query AVG() engañosa
   - Ubicación: Sección 6, Query 2
   - Problema: `AVG(fp.cantidad)` promedia todos los registros, no unidades/día
   - Corrección:
     ```sql
     -- Cambiar:
     ROUND(AVG(fp.cantidad),2) as promedio_diario
     -- Por:
     ROUND(SUM(fp.cantidad) / NULLIF(COUNT(DISTINCT fp.fecha_contabilizacion), 0), 2) as promedio_unidades_por_dia
     ```
   - Impacto: Gemini recibirá métrica incorrecta

4. **[MEJORA 🔵]** Cache key no incluye userId
   - Ubicación: Sección 4 (dice cache por dashboardId + fechas)
   - Problema: Si dos usuarios piden mismo dashboard/período, comparten cache
   - Esto puede ser OK, pero si hay filtros por usuario (ej: centro_costos), será problema
   - Solución: Evaluar si el cache debe ser por usuario o global

5. **[MEJORA 🔵]** No especifica manejo de NULL en queries
   - Ubicación: Queries en sección 6
   - Problema: `maquinista_fk` es nullable, puede causar issues
   - Solución: Agregar `COALESCE(dm.nombre_completo, 'Sin asignar')`

### Score de Precisión

**Cálculo**: 100 - (1×15 + 2×8 + 3×3 + 0×1) = 100 - 40 = **60% PRECISO** 🟡

---

### 🔄 EVALUACIÓN: CONSISTENCIA

**Criterio**: ¿Hay contradicciones internas?

#### Análisis

**Consistencia entre secciones**:

- ✅ Sección 5 (API) alineada con sección 4 (Flujo de datos)
- ✅ Queries (sección 6) corresponden al User Prompt (sección 7)
- ✅ Configuración (sección 9) corresponde al endpoint (sección 5)
- ✅ Tareas (sección 8) cubren lo descrito en secciones anteriores
- ⚠️ **Contradicción**: Sección 8 dice "usar JdbcTemplate O Spring Data JPA", pero no decide cuál
- ⚠️ **Inconsistencia**: Sección 10 menciona "auditar prompts" pero no está en tareas (sección 8)

#### Hallazgos

**Inconsistencias detectadas** [NIVEL]:

1. **[MEJORA 🔵]** Tecnología de persistencia indecisa
   - Ubicación: Sección 8, tarea backend #3
   - Problema: "(usar `JdbcTemplate` o `Spring Data JDBC`/`JPA` con `@Query`)"
   - Solución: Decidir y especificar. Recomendación: JdbcTemplate para queries agregadas
   - Impacto: Desarrollador debe tomar decisión no documentada

2. **[MEJORA 🔵]** Auditoría mencionada pero no especificada
   - Ubicación: Sección 10 dice "auditar prompts enviados", sección 8 dice "audita request"
   - Problema: No hay tarea para crear tabla/entity de auditoría
   - Solución: Agregar tarea:
     ```
     - [ ] Crear AiExplanationAudit entity (userId, dashboardId, tokensUsados, tiempoRespuesta, fromCache)
     - [ ] Implementar logging en DashboardExplanationService
     ```
   - Impacto: Feature de auditoría no implementada

3. **[MEJORA 🔵]** TTL de cache inconsistente
   - Ubicación: Sección 4 dice "TTL 5 minutos", sección 8 también, pero no está en configuración (sección 9)
   - Solución: Agregar a application.yml:
     ```yaml
     spring:
       cache:
         caffeine:
           spec: maximumSize=100,expireAfterWrite=5m
     ```

### Score de Consistencia

**Cálculo**: 100 - (0×15 + 0×8 + 3×3 + 0×1) = 100 - 9 = **91% CONSISTENTE** 🟢

---

### 💡 EVALUACIÓN: CLARIDAD

**Criterio**: ¿Es fácil de entender para el equipo de desarrollo?

#### Análisis

**Estructura y organización**:

- ✅ Secciones numeradas y con títulos claros
- ✅ Orden lógico: Objetivo → Justificación → UX → Técnico → Implementación
- ✅ Uso apropiado de code blocks y ejemplos
- ✅ Lenguaje técnico pero accesible
- ⚠️ **Sin tabla de contenidos** (documento de 400 líneas)
- ⚠️ **Prompt template ocupa mucho espacio** sin delimitación clara
- ✅ Checklist clara en sección 11

**Lenguaje y terminología**:

- ✅ Consistente uso de "MVP", "Alternativa A"
- ✅ Términos técnicos bien definidos
- ⚠️ Sección 12 usa formato pregunta-respuesta poco claro

#### Hallazgos

**Problemas de claridad** [NIVEL]:

1. **[MEJORA 🔵]** Sin índice/TOC
   - Problema: 400 líneas sin navegación rápida
   - Solución: Agregar TOC al inicio:
     ```markdown
     ## 📑 ÍNDICE
     1. [Objetivo](#1-objetivo)
     2. [Justificación](#2-por-qué-esta-alternativa)
     ...
     14. [Próximos Pasos](#14-próximo-paso-recomendado)
     ```

2. **[MEJORA 🔵]** Prompts template sin delimitación visual clara
   - Ubicación: Sección 7
   - Problema: No está claro dónde termina System y empieza User prompt
   - Solución: Usar bloques separados con headers más claros

3. **[MEJORA 🔵]** Sección 12 formato confuso
   - Ubicación: Contexto de negocio
   - Problema: Lista de campos con "(ej: ...)" intercalados
   - Solución: Usar tabla:
     ```markdown
     | Campo | Ejemplo | ¿Completado? |
     |-------|---------|--------------|
     | industria | "Alimentos" | [ ] |
     | unidad_cantidad | "cajas" | [ ] |
     ```

### Score de Claridad

**Cálculo**: 100 - (0×15 + 0×8 + 3×3 + 0×1) = 100 - 9 = **91% CLARO** 🟢

---

### ⚡ EVALUACIÓN: ACCIONABILIDAD

**Criterio**: ¿El equipo puede empezar a implementar inmediatamente?

#### Análisis

**Ejecutabilidad**:

- ✅ Tareas específicas en checklist format
- ✅ Código SQL copy-pasteable
- ✅ Config YAML copy-pasteable
- ⚠️ **Depende de GEMINI_API_KEY** (no está, requiere acción externa)
- ⚠️ **Depende de contexto de negocio** (sección 12, no completado)
- ❌ **No hay componente frontend especificado para integrar el botón** (dice "componente que ya existe" sin nombrar archivo)
- ⚠️ **Sin priorización de tareas** (no dice qué hacer primero)

#### Hallazgos

**Blockers para acción** [NIVEL]:

1. **[IMPORTANTE 🟡]** No especifica dónde integrar el botón frontend
   - Ubicación: Sección 8, Frontend tareas
   - Problema: "Integrar en la vista que renderiza el iframe (componente que ya existe)"
   - No dice: ¿`DashboardView.tsx`? ¿`MetabaseEmbed.tsx`?
   - Solución: Especificar archivo exacto o investigar estructura frontend primero
   - Impacto: Desarrollador frontend debe buscar/investigar

2. **[IMPORTANTE 🟡]** Dependencias externas no resueltas
   - Problema: Requiere GEMINI_API_KEY + contexto negocio (sección 12)
   - Sección 11 lo marca como "acción requerida" pero no bloqueante
   - Solución: Mover a "Pre-requisitos" al inicio del documento
   - Impacto: Desarrollador puede empezar y bloquearse mid-work

3. **[MEJORA 🔵]** Sin orden de implementación recomendado
   - Problema: Tareas en checklist sin números/prioridades
   - Solución: Ordenar como:
     ```
     PASO 1 (Backend Core): GeminiApiClient + Controller
     PASO 2 (Backend Data): DashboardDataService (queries)
     PASO 3 (Backend Logic): DashboardExplanationService
     PASO 4 (Frontend): Botón + Modal
     PASO 5 (Integration): Testing E2E
     ```

4. **[MEJORA 🔵]** No especifica cómo generar mock de Gemini para desarrollo
   - Problema: Desarrollador querrá testear sin gastar tokens
   - Solución: Agregar sección "Desarrollo Local sin API Key":
     ```java
     @Profile("dev")
     @Bean
     public GeminiApiClient mockGeminiClient() {
         return prompt -> "# MOCK RESPONSE\n...";
     }
     ```

### Score de Accionabilidad

**Cálculo**: 100 - (0×15 + 2×8 + 2×3 + 0×1) = 100 - 22 = **78% ACCIONABLE** 🟡

---

### 🔧 EVALUACIÓN: MANTENIBILIDAD

**Criterio**: ¿Será fácil actualizar este plan cuando cambien los requerimientos?

#### Análisis

**Estructura modular**:

- ✅ Secciones bien separadas
- ✅ Queries SQL en bloque independiente (fácil actualizar)
- ✅ Config separada de implementación
- ✅ Incluye metadata (ID, Fecha, Estado)
- ⚠️ **Prompts hardcodeados en sección 7** (si cambian, hay que editar doc + código)
- ⚠️ **No hay versionado** (solo dice "DRAFT", no hay v0.1, v0.2)

**Regenerabilidad**:

- ✅ Formato Markdown estándar
- ✅ Secciones reutilizables para otros Feature Plans
- ⚠️ No hay template o estructura replicable

#### Hallazgos

**Problemas de mantenibilidad** [NIVEL]:

1. **[MEJORA 🔵]** Prompts duplicados doc vs código
   - Problema: Sección 7 tiene prompts que luego van en Java
   - Si cambia el prompt, hay que actualizar en 2 lugares
   - Solución: 
     - Opción A: Prompts en archivos separados (`prompts/system-dashboard.txt`)
     - Opción B: Agregar nota "⚠️ Al implementar, estos prompts irán en archivos de configuración separados"

2. **[MEJORA 🔵]** Sin versionado semántico
   - Ubicación: Metadata al inicio
   - Problema: Solo dice "DRAFT", no v0.1
   - Solución: Cambiar a:
     ```
     > Versión: 0.1-DRAFT
     > Changelog:
     > - 2025-11-11: Versión inicial (0.1-DRAFT)
     ```

3. **[OPCIONAL 🟢]** Sin template reutilizable
   - Problema: Si hay FP-001B, FP-002, etc., no hay guía de estructura
   - Solución: Crear `feature-plan-template.md` en `.gemini/templates/`

### Score de Mantenibilidad

**Cálculo**: 100 - (0×15 + 0×8 + 2×3 + 1×1) = 100 - 7 = **93% MANTENIBLE** 🟢

---

### 🎨 EVALUACIÓN: CRITERIOS ESPECÍFICOS DEL CONTEXTO

**Criterio**: ¿Cumple con los requisitos de un Feature Plan para este proyecto?

#### Requerimientos del proyecto IOC (extraídos de project-summary.md):

- ✅ Integración con stack existente (Spring Boot, React, PostgreSQL)
- ✅ Usa patrones del proyecto (JWT auth, rate limiting con Resilience4j)
- ✅ Considera Metabase (no modifica iframe, respeta same-origin)
- ✅ Incluye observabilidad (menciona auditoría)
- ⚠️ **No menciona cómo se integra con sistema de permisos RBAC existente**
- ⚠️ **No considera deployment en Render** (timeouts de cold start, recursos limitados)

#### Hallazgos

**Cumplimiento de contexto** [NIVEL]:

1. **[IMPORTANTE 🟡]** Sin integración con RBAC
   - Problema: Proyecto tiene `@PreAuthorize` en endpoints, este plan no lo menciona
   - Ubicación: Sección 5 (API) solo dice "valida permisos" pero no específica cuáles
   - Solución: Agregar:
     ```java
     @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER')")
     // o específico:
     @PreAuthorize("@dashboardSecurityService.canAccessDashboard(#request.dashboardId, authentication)")
     ```
   - Impacto: Implementación insegura si no se considera

2. **[MEJORA 🔵]** Sin consideración de Render deployment
   - Problema: Render free tier tiene cold starts (10-30s)
   - Si timeout es 30s + cold start, primera request siempre falla
   - Solución: Agregar nota en sección 10:
     ```
     ⚠️ Render Deployment: 
     - Cold start puede tardar 20-30s
     - Primera request después de inactividad puede timeout
     - Considerar: endpoint de warmup o aumentar timeout a 90s
     ```

3. **[MEJORA 🔵]** No menciona Prometheus metrics
   - Problema: Proyecto usa Actuator + Prometheus, este endpoint debería tener métricas
   - Solución: Agregar en sección 8:
     ```java
     - [ ] Registrar métricas custom:
           - ai.explanation.requests.total (counter)
           - ai.explanation.duration (timer)
           - ai.explanation.cache.hit.ratio (gauge)
           - ai.explanation.tokens.used (counter)
     ```

### Score en Criterios Específicos

**Cálculo**: 100 - (0×15 + 1×8 + 2×3 + 0×1) = 100 - 14 = **86% CUMPLE CONTEXTO** 🟡

---

## FASE 3: CONSOLIDACIÓN Y PRIORIZACIÓN

### 📊 REPORTE CONSOLIDADO

**Feature Plan evaluado**: FP-001A - Dashboard AI Explanation (Alternativa A - MVP)  
**Fecha de evaluación**: 2025-11-11  
**Líneas totales**: ~400  
**Secciones**: 14

---

### Executive Summary

**Score General**: **79%** de calidad 🟡

| Dimensión | Score | Status | Issues |
|-----------|-------|--------|--------|
| Completitud | 74% | 🟡 | 6 (0🔴 2🟡 3🔵 1🟢) |
| Precisión | 60% | 🟡 | 5 (1🔴 2🟡 3🔵 0🟢) |
| Consistencia | 91% | 🟢 | 3 (0🔴 0🟡 3🔵 0🟢) |
| Claridad | 91% | 🟢 | 3 (0🔴 0🟡 3🔵 0🟢) |
| Accionabilidad | 78% | 🟡 | 4 (0🔴 2🟡 2🔵 0🟢) |
| Mantenibilidad | 93% | 🟢 | 3 (0🔴 0🟡 2🔵 1🟢) |
| Criterios Específicos | 86% | 🟡 | 3 (0🔴 1🟡 2🔵 0🟢) |

**Promedio**: (74+60+91+91+78+93+86) / 7 = **79%**

**Interpretación**:
- 🟢 90-100%: Excelente (3 dimensiones)
- 🟡 70-89%: Bueno, mejoras recomendadas (3 dimensiones)
- 🔴 <70%: Requiere trabajo (1 dimensión: Precisión 60%)

---

### Resumen de Hallazgos

- 🔴 **Críticos**: 1 issue (bloquea producción)
- 🟡 **Importantes**: 7 issues (mejoran significativamente)
- 🔵 **Mejoras**: 17 issues (optimizaciones)
- 🟢 **Opcionales**: 2 issues (nice to have)

**Total**: **27 oportunidades de mejora identificadas**

---

### Top 10 Prioridades (ordenadas por impacto)

1. 🔴 **PRECISIÓN** - Timeout 30s insuficiente para Gemini → cambiar a 60-90s
2. 🟡 **PRECISIÓN** - Sin especificación de parsing de respuesta Gemini (formato inconsistente)
3. 🟡 **PRECISIÓN** - Query AVG() engañosa (calcula mal promedio diario)
4. 🟡 **COMPLETITUD** - DTOs backend no especificados (DashboardExplanationRequest/Response)
5. 🟡 **COMPLETITUD** - Manejo de errores no detallado (sin response bodies 429, 503)
6. 🟡 **ACCIONABILIDAD** - No especifica componente frontend para integrar botón
7. 🟡 **ACCIONABILIDAD** - Dependencias externas (API Key, contexto negocio) no en pre-requisitos
8. 🟡 **CONTEXTO** - Sin integración con RBAC existente (@PreAuthorize faltante)
9. 🔵 **COMPLETITUD** - Sin estrategia de testing (unit/integration/E2E)
10. 🔵 **PRECISIÓN** - Cache key sin userId (puede causar conflictos)

---

### Fortalezas Detectadas

✅ **Queries SQL bien diseñadas** - Cubren todas las dimensiones de análisis (operarios, máquinas, turnos, tendencias)
✅ **Configuración completa** - application.yml snippet listo para usar
✅ **Alcance bien definido** - Muy claro qué es MVP y qué no (no clickear gráficos)
✅ **Prompts template útiles** - Buen punto de partida para Gemini
✅ **Checklist pre-desarrollo** - Ayuda a no olvidar pasos
✅ **Métricas de éxito claras** - Cuantificables (< 8s, >= 50% cache hit, >= 30% adoption)

---

### Debilidades Críticas

❌ **Timeout muy corto** - 30s causará fallas frecuentes
❌ **Sin manejo robusto de respuesta variable de Gemini** - Frontend romperá con formatos inesperados
❌ **Métricas SQL incorrectas** - Gemini recibirá datos engañosos

---

### Recomendación

🟡 **MEJORAR ANTES DE IMPLEMENTAR**

→ El Feature Plan es **útil y mayormente completo**, pero tiene **1 issue crítico y 7 importantes** que bloquearán la implementación o causarán bugs en producción.

→ **Riesgo de implementar as-is**:
- Alta probabilidad de timeouts (timeout 30s + Gemini tarda 40s = fallo)
- Respuestas inconsistentes de Gemini romperán frontend
- Métricas incorrectas confundirán a usuarios

→ **Esfuerzo de mejoras**: ~2-3 horas
→ **Score proyectado con mejoras críticas+importantes**: 79% → **92%** (+13 puntos)

---

## 🔧 PLAN DE MEJORAS PROPUESTO

### Correcciones Críticas 🔴 (OBLIGATORIAS)

#### Issue #1: Timeout insuficiente

**Ubicación**: Sección 9 (Configuración)

**Cambio**:
```yaml
# ANTES:
gemini:
  timeout: 30s

# DESPUÉS:
gemini:
  timeout: 90s
  retry:
    maxAttempts: 2
    backoffMs: 1000
```

**Agregar en sección 10**:
```markdown
### Timeouts y Retries

⚠️ **Gemini 1.5 Flash puede tardar 10-60s** con prompts largos (>1000 tokens).

**Estrategia**:
- Timeout: 90s (suficiente para p99 de respuestas)
- Retry: 2 intentos con backoff 1s (solo para errores 503/504)
- NO reintentar 429 (rate limit) automáticamente

**Render Deployment**:
- Cold start agrega 20-30s adicionales
- Primera request post-inactividad puede tardar hasta 120s total
- Considerar endpoint warmup: `GET /api/v1/ai/warmup` (return 200 OK)
```

**Impacto si NO se aplica**: 🔴 Feature fallará frecuentemente en producción

---

### Mejoras Importantes 🟡 (ALTAMENTE RECOMENDADAS)

#### Issue #2: Especificar parsing de respuesta Gemini

**Ubicación**: Sección 7 (Prompt Template)

**Agregar después del User prompt**:
```markdown
### Estructura de Salida Garantizada

Para asegurar parsing consistente, el System Prompt DEBE incluir:

```
IMPORTANTE: Responde SIEMPRE con esta estructura exacta:

# 📊 RESUMEN EJECUTIVO
[2-3 líneas máximo]

# 🔑 KEY POINTS
• [punto 1]
• [punto 2]
• [punto 3 a 5]

# ⚡ INSIGHTS ACCIONABLES
• [insight 1]
• [insight 2 a 3]

No agregues secciones adicionales. Usa EXACTAMENTE estos headers con emojis.
```

**Backend parsing** (agregar en DashboardExplanationService):
```java
private DashboardExplanationResponse parseGeminiResponse(String markdown, ...) {
    // Extraer secciones usando regex
    String resumen = extractSection(markdown, "# 📊 RESUMEN EJECUTIVO");
    List<String> keyPoints = extractBulletPoints(markdown, "# 🔑 KEY POINTS");
    List<String> insights = extractBulletPoints(markdown, "# ⚡ INSIGHTS ACCIONABLES");
    
    // Validar que todas las secciones existen
    if (resumen == null || keyPoints.isEmpty()) {
        log.warn("Gemini response malformed, using fallback");
        return createFallbackResponse();
    }
    
    return new DashboardExplanationResponse(markdown, resumen, keyPoints, insights, ...);
}

private String extractSection(String markdown, String header) {
    Pattern pattern = Pattern.compile(
        Pattern.quote(header) + "\\n([^#]+)",
        Pattern.MULTILINE
    );
    Matcher matcher = pattern.matcher(markdown);
    return matcher.find() ? matcher.group(1).trim() : null;
}
```
```

---

#### Issue #3: Corregir query AVG()

**Ubicación**: Sección 6, Query 2

**Cambio**:
```sql
-- ANTES (INCORRECTO):
ROUND(AVG(fp.cantidad),2) as promedio_diario

-- DESPUÉS (CORRECTO):
ROUND(
  SUM(fp.cantidad)::numeric / 
  NULLIF(COUNT(DISTINCT fp.fecha_contabilizacion), 0),
  2
) as promedio_unidades_por_dia
```

**Explicación**: `AVG(cantidad)` promedia todos los registros (pallets), no unidades por día. La corrección suma total de unidades y divide por días trabajados.

---

#### Issue #4: Especificar DTOs completos

**Ubicación**: Nueva sección 5.1 después de sección 5

**Agregar**:
```markdown
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

**DashboardExplanationResponse.java**:
```java
package com.cambiaso.ioc.dto.ai;

import java.time.Instant;
import java.util.List;

public record DashboardExplanationResponse(
    String explicacionCompleta,
    String resumenEjecutivo,
    List<String> keyPoints,
    List<String> insightsAccionables,
    DashboardMetadata metadata,
    Instant generadoAt,
    Boolean fromCache,
    Integer tokensUsados  // para tracking de costos
) {}

public record DashboardMetadata(
    Integer dashboardId,
    String titulo,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    Map<String, Object> filtrosAplicados
) {}
```

**ErrorResponse.java** (reutilizable):
```java
package com.cambiaso.ioc.dto.error;

import java.time.Instant;

public record ErrorResponse(
    String error,
    String message,
    Integer retryAfter, // solo para 429
    Instant timestamp
) {
    public static ErrorResponse rateLimitExceeded(int retryAfterSeconds) {
        return new ErrorResponse(
            "RATE_LIMIT_EXCEEDED",
            "Máximo 5 explicaciones por minuto. Intenta en " + retryAfterSeconds + " segundos.",
            retryAfterSeconds,
            Instant.now()
        );
    }
    
    public static ErrorResponse aiServiceUnavailable() {
        return new ErrorResponse(
            "AI_SERVICE_UNAVAILABLE",
            "Servicio de IA temporalmente no disponible. Intenta más tarde.",
            null,
            Instant.now()
        );
    }
}
```
```

---

#### Issue #5: Detallar manejo de errores

**Ubicación**: Sección 5, agregar subsección

**Agregar**:
```markdown
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
  "message": "La generación de explicación tardó demasiado. Intenta con un período más corto.",
  "timestamp": "2025-11-11T22:00:00Z"
}
```

**Frontend debe manejar**:
- 429: Mostrar countdown "Intenta en {retryAfter}s"
- 503/504: Mostrar "Intenta más tarde" con botón de reintentar
- 400: Mostrar mensaje de error específico
```

---

#### Issue #6: Especificar componente frontend

**Ubicación**: Sección 8, tarea Frontend

**Cambiar**:
```markdown
Frontend (1-2 días):
- [ ] Instalar dependencias:
      npm install react-markdown@^9.0.0 remark-gfm@^4.0.0
- [ ] Crear `components/dashboard/ExplainDashboardButton.tsx`
- [ ] Crear `components/dashboard/ExplanationModal.tsx`
- [ ] Crear `components/dashboard/PeriodSelector.tsx` (selector de fechas)
- [ ] **Integrar en `src/pages/DashboardViewPage.tsx`** ← ESPECIFICADO
      (Investigar primero: buscar componente que renderiza iframe de Metabase)
      (Si no existe, crear nuevo `MetabaseDashboardContainer.tsx`)
- [ ] Agregar service: `src/services/aiExplanationService.ts`
- [ ] Manejar estados de loading/error con react-hot-toast
- [ ] Tests unitarios: `ExplainDashboardButton.test.tsx`, `ExplanationModal.test.tsx`
```

---

#### Issue #7: Mover dependencias a pre-requisitos

**Ubicación**: Nueva sección 0 (antes del objetivo)

**Agregar**:
```markdown
## 0. PRE-REQUISITOS (BLOQUEAN INICIO)

Antes de comenzar la implementación, asegurar:

- [ ] ✅ **GEMINI_API_KEY obtenida**: https://makersuite.google.com/app/apikey
      - Verificar límites free tier: 15 RPM, 1,500 RPD
      - Guardar en `.env` local y secretos de Render
      
- [ ] ✅ **Contexto de negocio completado** (ver sección 12):
      - industria, unidad_cantidad, objetivo_mensual_unidades
      - horario_turno_dia, horario_turno_noche
      - Sin esto, Gemini generará explicaciones genéricas sin valor
      
- [ ] ✅ **Componente frontend de dashboard identificado**:
      - Buscar archivo que renderiza iframe de Metabase
      - Confirmar que acepta props adicionales (para agregar botón)
      
- [ ] ⚠️ **Decisión de tecnología de persistencia**: JdbcTemplate vs JPA
      - Recomendación: **JdbcTemplate** (queries agregadas, no entities)

**Si falta alguno → NO empezar implementación**
```

---

#### Issue #8: Integración con RBAC

**Ubicación**: Sección 5, endpoint specification

**Cambiar**:
```java
// ANTES (IMPLÍCITO):
POST /api/v1/ai/explain-dashboard

// DESPUÉS (EXPLÍCITO):
@PostMapping("/explain-dashboard")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER')")
@RateLimiter(name = "aiExplanation")
public ResponseEntity<DashboardExplanationResponse> explainDashboard(...) {
    // Validación adicional: usuario puede acceder a este dashboard específico
    if (!dashboardSecurityService.canAccessDashboard(request.dashboardId(), auth)) {
        throw new AccessDeniedException("No tienes permisos para este dashboard");
    }
    ...
}
```

**Agregar en sección 8 (tareas backend)**:
```markdown
- [ ] Validar permisos de dashboard:
      - Reutilizar `DashboardSecurityService` existente (si existe)
      - O implementar check simple: validar que dashboardId está en lista permitida para rol del usuario
```

---

### Optimizaciones 🔵 (RECOMENDADAS)

#### Optimización #1: Agregar TOC

**Ubicación**: Después de metadata inicial

**Agregar**:
```markdown
## 📑 ÍNDICE

- [0. Pre-requisitos](#0-pre-requisitos-bloquean-inicio)
- [1. Objetivo](#1-objetivo)
- [2. Justificación](#2-por-qué-esta-alternativa)
- [3. UX / Ubicación del botón](#3-ux--ubicación-del-botón)
- [4. Flujo de datos](#4-flujo-de-datos-alto-nivel)
- [5. API Backend](#5-api-backend)
  - [5.1. DTOs](#51-dtos-backend-record-classes)
  - [5.2. Manejo de Errores](#52-manejo-de-errores-detallado)
- [6. Queries SQL](#6-queries-sql-para-construir-datos-resumidos)
- [7. Prompt Template](#7-prompt-template-ejemplo)
- [8. Implementación - Tareas](#8-implementación---tareas-detalladas-mvp)
- [9. Configuración](#9-configuración-y-variables-necesarias)
- [10. Consideraciones](#10-consideraciones-importantes)
- [11. Checklist pre-desarrollo](#11-checklist-pre-desarrollo-acción-requerida)
- [12. Contexto de negocio](#12-contexto-de-negocio-mínimo-requerido)
- [13. Métricas de éxito](#13-métricas-de-éxito-mvp)
- [14. Próximos pasos](#14-próximo-paso-recomendado)

---
```

#### Optimización #2: Estrategia de testing

**Ubicación**: Nueva sección 8.1 después de tareas

**Agregar**:
```markdown
### 8.1. Estrategia de Testing

**Tests Backend**:

1. **Unit Tests**:
   - `GeminiApiClientTest`: Mock HTTP con WireMock
     - Test respuesta 200 OK
     - Test timeout (delay >90s)
     - Test 429 rate limit
     - Test 503 service unavailable
   
   - `DashboardExplanationServiceTest`: Mock Gemini + Cache
     - Test cache hit (no llama a Gemini)
     - Test cache miss (llama y cachea)
     - Test parsing de respuesta Gemini
     - Test fallback si parsing falla
   
   - `DashboardDataServiceTest`: Testcontainers PostgreSQL
     - Test queries con datos mock
     - Test manejo de NULLs (maquinista_fk nullable)
     - Test rangos de fechas edge cases

2. **Integration Tests**:
   - `DashboardExplanationControllerTest`: MockMvc + @SpringBootTest
     - Test endpoint con autenticación válida
     - Test 401 sin JWT
     - Test 403 sin permisos
     - Test 429 rate limit (hacer 6 requests)
     - Test validación de fechas (fin < inicio)

**Tests Frontend**:

1. **Unit Tests (Vitest + Testing Library)**:
   - `ExplainDashboardButton.test.tsx`:
     - Test render botón
     - Test click abre modal de período
     - Test loading state mientras llama API
     - Test error toast si API falla
   
   - `ExplanationModal.test.tsx`:
     - Test render Markdown correctamente
     - Test botón copiar
     - Test botón cerrar
     - Test muestra "Desde caché" si fromCache=true

2. **E2E Tests (Opcional - Playwright)**:
   - `dashboard-explanation.spec.ts`:
     - Test flujo completo: login → dashboard → click explicar → ver modal
     - Test rate limit (hacer 6 clicks rápidos)

**Coverage Target**: >= 80% líneas, >= 70% branches
```

#### Optimización #3: Métricas Prometheus

**Ubicación**: Sección 8, agregar tarea

**Agregar**:
```markdown
Backend (2 días):
...
- [ ] Registrar métricas custom (Micrometer):
      ```java
      @Autowired
      private MeterRegistry meterRegistry;
      
      // En DashboardExplanationService:
      Counter.builder("ai.explanation.requests.total")
          .tag("dashboard_id", String.valueOf(dashboardId))
          .tag("from_cache", String.valueOf(fromCache))
          .register(meterRegistry)
          .increment();
      
      Timer.builder("ai.explanation.duration")
          .tag("success", "true")
          .register(meterRegistry)
          .record(duration);
      
      Counter.builder("ai.explanation.tokens.used")
          .register(meterRegistry)
          .increment(tokensUsados);
      ```
```

#### Optimizaciones adicionales (4-10): Listadas pero no detalladas para brevedad

- #4: Mock de Gemini para desarrollo local sin API Key
- #5: Cache key incluir userId (evaluar necesidad)
- #6: Versionado semántico del Feature Plan (0.1-DRAFT)
- #7: Formatear sección 12 como tabla
- #8: Separar prompts a archivos de configuración
- #9: Considerar deployment Render (cold start, warmup endpoint)
- #10: Manejo de NULLs en queries SQL

---

### Mejoras Opcionales 🟢 (NICE TO HAVE)

1. **Template reutilizable**: Crear `.gemini/templates/feature-plan-template.md`
2. **Mockups visuales**: Agregar ASCII art o link a Figma

---

## FASE 4: INTERACCIÓN CON USUARIO

### 📋 RESUMEN PARA DECISIÓN

**Feature Plan FP-001A evaluado**:
- ✅ Útil y mayormente completo
- ⚠️ 1 issue crítico (timeout), 7 importantes
- 🎯 Score actual: **79%**
- 🎯 Score proyectado con mejoras: **92%**

**Riesgo de implementar sin mejoras**:
- 🔴 Alta probabilidad de timeouts en producción
- 🔴 Respuestas inconsistentes de Gemini romperán frontend
- 🔴 Métricas incorrectas confundirán a usuarios

**Esfuerzo de mejoras**:
- Solo críticas (1): ~30 min
- Críticas + importantes (8): ~2-3 horas
- Todas (27): ~5-6 horas

---

### 🎬 OPCIONES DISPONIBLES

**A) APLICAR TODAS LAS MEJORAS** ⭐ **RECOMENDADO**
- Tiempo: 5-6 horas
- Resultado: Feature Plan production-ready (92% score)
- Incluye: 1 crítico + 7 importantes + 17 optimizaciones + 2 opcionales

**B) SOLO CRÍTICAS + IMPORTANTES**
- Tiempo: 2-3 horas
- Resultado: Feature Plan implementable sin blockers (88% score)
- Incluye: 1 crítico + 7 importantes

**C) SOLO CRÍTICAS**
- Tiempo: 30 minutos
- Resultado: Evita fallas críticas pero quedan gaps (72% score)
- Incluye: Solo timeout fix

**D) MEJORAS PERSONALIZADAS**
- Tú eliges qué issues aplicar de la lista
- Puedo generar un plan a medida

**E) NO APLICAR MEJORAS (PROCEDER AS-IS)**
- Riesgo: Alto
- No recomendado para este caso

**V) VER DIFF DETALLADO**
- Mostrar cambios línea por línea antes de decidir

---

### ❓ TU DECISIÓN

¿Qué opción prefieres?

Responde con la letra (A/B/C/D/E/V) o describe qué mejoras específicas quieres aplicar.

**Ejemplo**:
```
"Opción B - Aplica críticas + importantes solamente"
```

O:
```
"Opción D - Solo aplica: #1 (timeout), #2 (parsing), #4 (DTOs), #8 (RBAC)"
```

---


