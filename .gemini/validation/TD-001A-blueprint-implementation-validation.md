# VALIDADOR DE IMPLEMENTACIÓN DE BLUEPRINTS – TD-001A (Feature FP-001A)

Fecha: 2025-11-13
Ámbito: Backend – Explicación de Dashboard con Gemini (Alternativa A - MVP)
Modo de validación: Feature (múltiples BSS + M archivos)

Resumen ejecutivo
- Veredicto: Aprobado con observaciones menores
- Cobertura: 4 BSS implementados (Repo analítico, Cliente Gemini, Servicio de orquestación, Controller)
- Score global: 93/100
- Riesgos: verificación de acceso al dashboard (RBAC) no evidente a nivel de endpoint/servicio; divergencia menor de ruta API.

1) Artefactos usados (evidencia)
- Technical Design: `.gemini/sprints/technical-designs/TD-001A-dashboard-ai-explanation-A.md`
- Blueprints backend (BSS):
  - `.gemini/blueprints/backend/ai_explanaition/BSS-001-DashboardAnalyticsRepository.md`
  - `.gemini/blueprints/backend/ai_explanaition/BSS-002-GeminiApiClient.md`
  - `.gemini/blueprints/backend/ai_explanaition/BSS-003-DashboardExplanationService.md`
  - `.gemini/blueprints/backend/ai_explanaition/BSS-004-AiExplanationController.md`
  - Índice: `.gemini/blueprints/backend/ai_explanaition/bss-index.md`
- Implementación (clases principales):
  - Repo analítico: `src/main/java/com/cambiaso/ioc/persistence/repository/DashboardAnalyticsRepository.java`
  - Cliente Gemini: `src/main/java/com/cambiaso/ioc/service/ai/GeminiApiClient.java`
  - Servicio: `src/main/java/com/cambiaso/ioc/service/ai/DashboardExplanationService.java`
  - Controller: `src/main/java/com/cambiaso/ioc/controller/AiExplanationController.java`
  - DTOs AI: `src/main/java/com/cambiaso/ioc/dto/ai/*.java`
  - DTOs analíticos: `src/main/java/com/cambiaso/ioc/dto/analytics/*.java`
  - Recursos prompt: `src/main/resources/prompts/system-prompt.txt`, `src/main/resources/prompts/context.yaml`
  - Config: `src/main/resources/application.properties` (+ `CacheConfig`, `RateLimitingConfig`)
- Tests relevantes:
  - Controller: `src/test/java/com/cambiaso/ioc/controller/AiExplanationControllerTest.java`
  - Servicio: `src/test/java/com/cambiaso/ioc/service/ai/DashboardExplanationServiceTest.java`
  - Repositorio: `src/test/java/com/cambiaso/ioc/persistence/repository/DashboardAnalyticsRepositoryTest.java`
- Resultado de test suite (usuario): 190 tests, 0 fallos, 3 skip

2) Validación por componente (BSS)

BSS-001 – DashboardAnalyticsRepository (Score: 95/100)
- Conformidad: Alta. Usa `NamedParameterJdbcTemplate`. Métodos previstos: fetchTotals, fetchTopOperarios, fetchDistribucionTurno, fetchTopMaquinas, fetchTendenciaDiaria.
- Validaciones: rango de fechas (<=12 meses), orden y límites (top 10) OK.
- Manejo de errores: `@Repository` habilita traducción de excepciones (IllegalArgumentException -> InvalidDataAccessApiUsageException en tests), `EmptyResultDataAccessException` manejada en totales.
- Evidencia: firma y SQL presentes con agregaciones correctas; tests de orden, límite y validaciones.
- Observación menor: Considerar índices compuestos si filtros por turno/fecha se vuelven pesados (coincide con TD 7).

BSS-002 – GeminiApiClient (Score: 92/100)
- Conformidad: Alta. `WebClient` con timeout total 90s, retries con backoff (configurable), parse robusto de respuesta (JSON + fallback regex), manejo explícito de 429.
- Configuración: `gemini.api-key`, `gemini.model`, `gemini.timeout.seconds`, `gemini.retry.*`, `gemini.base-url` en `application.properties`.
- Utilities: `estimateTokens()` (~len/4) alineado al TD.
- Observaciones:
  - OK: extracción `candidates[0].content.parts[0].text` y fallback.
  - Mejora: cuando el JSON de la API cambia, convendría logging de payload parcial para debugging (mitigado por log actual).

BSS-003 – DashboardExplanationService (Score: 95/100)
- Conformidad: Muy buena. Flujo completo en 8 fases: cache lookup, consultas, anonimización PII condicional, construcción de prompt (system+context+datos), llamada a Gemini, parse-validación, cache put, auditoría y métricas.
- PII: cuando `ai.explanation.send-pii=false`, reemplaza nombres por "Operario #N" y omite códigos.
- Cache: construye key con hash SHA-256 de filtros ordenados; calcula TTL dinámico (24h histórico, 30m actual, 5m fallback) y ahora aplica TTL real mediante doble caché (historical/current).
- Observabilidad: logs de auditoría estructurados, Micrometer timers/counters/summary.
- Parsing robusto: extrae JSON del texto y valida campos requeridos; fallback controlado si hay errores, según TD 11.

BSS-004 – AiExplanationController (Score: 93/100)
- Endpoints:
  - POST `/api/v1/ai/explain` (en TD se menciona `/api/v1/ai/explain-dashboard`): leve divergencia de ruta. GET alternativo disponible.
- Validaciones: Bean Validation en DTO, handlers de errores para 400, 408, 429, 503, 500 devolviendo estructura consistente.
- Rate limiting: usa `@RateLimiter(name = "aiExplanation")` (Resilience4j) y la configuración de rate limiter se agregó en `application.properties`.
- Seguridad: exige autenticación (considerando configuración global de Security). No se observa en este componente la verificación de acceso a un dashboard específico (RBAC por dashboard), como sugiere el TD; puede estar resuelto en otra capa, pero aquí no es evidente.

3) API contract vs implementación
- Request DTO `DashboardExplanationRequest` con `dashboardId`, `fechaInicio`, `fechaFin`, `filtros`. Validaciones básicas aplicadas; rango máximo se valida en repositorio.
- Response `DashboardExplanationResponse`: `resumenEjecutivo`, `keyPoints`, `insightsAccionables`, `alertas`, `metadata` equivalente: `dashboardId`, `dashboardTitulo`, fechas, `filtrosAplicados`, `generadoEn`, `fromCache`, `tokensUsados`, `cacheTTLSeconds`.
- Divergencia menor de ruta: TD proponía `POST /api/v1/ai/explain-dashboard`; implementado `POST /api/v1/ai/explain` y `GET /api/v1/ai/explain/{dashboardId}`. Sugerido alinear contrato o documentar ambos.

4) Caching & performance
- Cache key: `dashboard:{id}:fi:{fi}:ff:{ff}:filters:{sha256-12}` (bien).
- TTL dinámico calculado y aplicado usando dos cachés: `aiExplanationsHistorical` (24h) y `aiExplanationsCurrent` (30min).
- Métricas: timers por fase (queries, gemini, total), counters de outcomes y cache hit/miss.

5) Seguridad & compliance
- JWT/Auth: El controller espera `Authentication` y depende de configuración de seguridad global.
- RBAC por dashboard: no se evidencia chequeo explícito (por ejemplo, lista de dashboards accesibles por rol/usuario o servicio de acceso). TD sugiere reutilizar lógica existente de embedding/roles; incluir validación en Service o Controller.
- PII: anonimización implementada; `send_pii_to_gemini=false` por defecto en `application.properties`.

6) Observabilidad
- Logging estructurado de auditoría con latencias y tokens.
- Micrometer: counters, timers, summary. Bien.

7) Testing – cobertura y calidad
- ControllerTest: cubre happy path, fromCache, GET con query params, mapeo de excepciones (408/429/503/400). Service mockeado.
- ServiceTest: cubre cache miss/hit, TTL computation, PII anonymization, errores Gemini (timeout/rate limit/API), parsing inválido y con campos faltantes, hash de filtros estable, manejo de datos vacíos.
- RepositoryTest: valida límites, ordenamientos y excepciones envueltas. Buena batería.
- Resultado general de tests (usuario): 190 ejecutados, 0 fallos, 3 skipped – adecuado para merge.

8) Matriz de integración (resumen)
- Controller → Service: OK (contrato DTO/Response alineado). Manejo de errores propagados y mapeados a HTTP.
- Service → Repository: OK (5 consultas invocadas; DTOs consistentes).
- Service → GeminiApiClient: OK (timeout, retries, parse). Estimación de tokens.
- Service → Cache/Metrics/Prompts: OK (cache via Spring Cache; timers/counters; recursos `system-prompt.txt`, `context.yaml` cargados).
- Config → Runtime: `gemini.*` presentes; `ai.explanation.*` presentes; rate limiter `aiExplanation` ahora configurado.

9) Riesgos y gaps (accionables)
- R1 – RateLimiter Resilience4j para `aiExplanation` ✅ RESUELTO
  - Estado: COMPLETADO (2025-11-13)
  - Evidencia: agregado a `application.properties`:
    - `resilience4j.ratelimiter.instances.aiExplanation.limit-for-period=5`
    - `resilience4j.ratelimiter.instances.aiExplanation.limit-refresh-period=60s`
    - `resilience4j.ratelimiter.instances.aiExplanation.timeout-duration=0s`

- R2 – TTL dinámico aplicado en la caché ✅ RESUELTO
  - Estado: COMPLETADO (2025-11-13)
  - Solución: Implementada estrategia de doble caché (aiExplanationsHistorical / aiExplanationsCurrent)
  - Evidencia:
    - `src/main/java/com/cambiaso/ioc/config/CacheConfig.java`: definición con 2 caches Caffeine
    - `src/main/java/com/cambiaso/ioc/service/ai/DashboardExplanationService.java`: `selectCacheName(fechaFin)` y uso de la caché correspondiente para hit/miss y put.
    - `src/main/resources/application.properties`: nombres de caches configurables

- R3 – Divergencia de ruta del endpoint respecto al TD (Bajo)
  - Acción: Alinear documento TD (aceptar `/api/v1/ai/explain`) o agregar alias/controller mapping adicional `/explain-dashboard` para compatibilidad con frontend.
  - Estado: ✅ RESUELTO (2025-11-14)
  - Solución: Agregado alias en el controlador para soportar ambas rutas
  - Evidencia: `@PostMapping(path = {"/explain", "/explain-dashboard"})` en `AiExplanationController.java`

- R4 – Verificación centralizada de acceso a dashboards ✅ RESUELTO
  - Estado: COMPLETADO (2025-11-14)
  - Solución: Implementado servicio centralizado de verificación de acceso
  - Componentes creados:
    - `DashboardAccessService` (interfaz): Define contrato para verificación de acceso
    - `DashboardAccessServiceImpl`: Implementación con caché (60s), bypass para ROLE_ADMIN, auditoría y métricas
    - `GlobalExceptionHandler`: Mapea `DashboardAccessDeniedException` a HTTP 403
    - Integración en `DashboardExplanationService`: Verifica acceso antes de generar explicaciones (FASE 0)
    - Cache `dashboardAccess` agregada en `CacheConfig` con TTL de 60 segundos
  - Tests creados:
    - `DashboardAccessServiceImplTest`: Suite completa de tests unitarios (6 tests)
  - Evidencia:
    - `src/main/java/com/cambiaso/ioc/service/DashboardAccessService.java`
    - `src/main/java/com/cambiaso/ioc/service/impl/DashboardAccessServiceImpl.java`
    - `src/main/java/com/cambiaso/ioc/exception/GlobalExceptionHandler.java`
    - `src/main/java/com/cambiaso/ioc/config/CacheConfig.java`: cache `dashboardAccess` configurada
    - `src/main/java/com/cambiaso/ioc/service/ai/DashboardExplanationService.java`: integración FASE 0
    - `src/test/java/com/cambiaso/ioc/service/impl/DashboardAccessServiceImplTest.java`
  - Características implementadas:
    - Usa `metabase.dashboards[*].allowed-roles` como fuente de verdad
    - Bypass automático para usuarios con ROLE_ADMIN
    - Cache de decisiones por usuario:dashboard durante 60 segundos
    - Auditoría mediante `DashboardAuditService` de accesos permitidos/denegados
    - Métricas de Micrometer para observabilidad (dashboard.access con tags result/reason)
    - Política segura: dashboards no configurados = acceso denegado (403)
    - Respuesta HTTP 403 estructurada con timestamp, error y detalles

10) Puntuación detallada
- BSS-001 (Repo): 95 – Conformidad alta, validaciones y pruebas sólidas.
- BSS-002 (Cliente Gemini): 92 – Timeouts/retries/parse robustos; buena configuración.
- BSS-003 (Servicio): 95 – Orquestación completa; TTL dinámico aplicado.
- BSS-004 (Controller): 93 – Endpoints y mapeo de errores correctos; rate limiter configurado.
- Score global (ponderado): 93/100.

11) Cobertura de requerimientos (TD → Implementación)
- 1 endpoint AI: Hecho (POST `/api/v1/ai/explain` + GET variante) [Divergencia menor de ruta]
- Capa de agregación SQL vía JDBC: Hecho (`DashboardAnalyticsRepository`).
- Caching con TTL dinámico: Hecho (doble caché aplicado y seleccionado por `fechaFin`).
- Integración Gemini con timeout 90s y retries: Hecho (`GeminiApiClient`).
- Sanitización PII configurable: Hecho (`send_pii_to_gemini=false`).
- Auditoría y métricas: Hecho (logs estructurados + Micrometer).

12) Cambios realizados (implementación R1 y R2)
- `src/main/resources/application.properties` (2025-11-13)
  - Añadidas propiedades de rate limiter para `aiExplanation`.
  - Añadidos nombres de caché `ai.explanation.cache-name-historical` y `ai.explanation.cache-name-current`.
- `src/main/java/com/cambiaso/ioc/config/CacheConfig.java` (2025-11-13)
  - Nuevo bean `CacheManager` con `aiExplanationsHistorical` (24h) y `aiExplanationsCurrent` (30min).
- `src/main/java/com/cambiaso/ioc/service/ai/DashboardExplanationService.java` (2025-11-13)
  - Añadido `selectCacheName(LocalDate fechaFin)` y uso de la caché correspondiente para hit/miss y put.
  - Métricas de caché diferenciadas por tipo (historical/current).

13) Próximos pasos (quick wins)
- Alinear ruta del endpoint o documentar el mapping efectivo.
- Añadir validación explícita de acceso a `dashboardId` en Service/Controller (retornar 403 al no cumplir).
- Documentar en README/Swagger el contrato final (request/response y casos de error).

14) Recomendación (ruta API) — Cambio mínimo y seguro
- Recomendación: Añadir un alias para mantener compatibilidad hacia atrás y evitar rupturas en clientes.
- Implementación mínima (una línea en `AiExplanationController`):

  ```java
  @PostMapping(path = {"/explain", "/explain-dashboard"})
  @RateLimiter(name = "aiExplanation")
  public ResponseEntity<DashboardExplanationResponse> explainDashboard(...) { ... }
  ```

- Beneficios:
  - Retrocompatibilidad inmediata para clientes que esperan `/explain` o `/explain-dashboard`.
  - Cambio reversible y de bajo riesgo.
  - Evita necesidad de coordinar despliegues inmediatos entre backend y frontend.

- Tests y docs:
  - Añadir un test que verifique POST a `/api/v1/ai/explain-dashboard` (paralelo al test existente para `/explain`).
  - Actualizar OpenAPI/Swagger si se desea que ambas rutas aparezcan en la spec.

Conclusión
- Añadir el alias es la forma más segura y de menor esfuerzo para resolver la divergencia R3; recomiendo aplicar este cambio inmediatamente o coordinar con frontend si se opta por migrar permanentemente la ruta.

---

ADICIÓN: Aceptación e implementación propuesta para R4 (comprobación centralizada de acceso)

Recibo — voy a analizar si podemos implementar la solución propuesta para R4 (comprobación centralizada de acceso a dashboards en la capa Service, con fallback a mapping en `metabase.dashboards[*].allowed-roles`, 403 en denegado, auditoría y cache de 60s). A continuación tienes un checklist de lo que voy a cubrir y el resultado.

Checklist (lo que revisaré)
- Verificar en el código existente si hay elementos reutilizables (props, servicios, excepciones, audit) que faciliten la implementación.
- Diagnosticar los puntos concretos donde integrar la comprobación (qué clases/ubicaciones).
- Señalar los cambios mínimos necesarios (sin aplicarlos).
- Enumerar riesgos/consideraciones y pruebas a añadir.
- Concluir si la solución es factible y cómo implementarla paso a paso.

Resumen ejecutivo (respuesta corta)
- Sí — la recomendación es factible y de bajo riesgo. El repositorio ya contiene infraestructura reutilizable (mapeo `MetabaseProperties` con `dashboards[*].allowed-roles`, `MetabaseEmbeddingService` con lógica/autorización comentada, `DashboardAuditService`, excepción `DashboardAccessDeniedException`, cachés y métricas). Implementar un `DashboardAccessService` (o exponer la verificación en un servicio existente) e invocarlo desde la capa Service (p. ej. `DashboardExplanationService`) para lanzar 403 en denegado es totalmente viable con cambios pequeños y localizados.

Evidencia relevante (qué encontré en el código)
- `MetabaseProperties` (config): existe una clase mapeando `metabase.dashboards[*]` con `allowedRoles` y `filters`. Eso da exactamente el mapping recomendado por TD.
- `application.properties` / test properties contienen entradas `metabase.dashboards[...]` con `allowed-roles`. Por tanto los datos de configuración ya están presentes en ambientes y tests.
- `MetabaseEmbeddingService`:
  - Es un servicio ya responsable de autorización para dashboards (método `getSignedDashboardUrl`).
  - Contiene `findDashboardConfig(...)` y una `checkAuthorization(...)` con la lógica por roles pero actualmente la parte que verifica roles está comentada (hay TODO que sugiere reactivar). Además hace audit y usa métricas. Esto significa que la lógica de autorización y audit ya existe, pero encapsulada y parcialmente desactivada.
- `DashboardAccessDeniedException` existe y el comentario dice que será manejada por un GlobalExceptionHandler (aunque no encontré un GlobalExceptionHandler con ese nombre en el repo).
- `DashboardAuditService` existe y `MetabaseEmbeddingService` lo usa para registrar accesos exitosos/denegados (audit + metrics).
- Caching: en el repo hay uso de Caffeine (`CacheConfig`, `spring.cache.*`) y `@Cacheable` en otras partes. Así que aplicar un cache breve es trivial.

Diagnóstico y opciones de integración (detallado)

1) Reutilizar `MetabaseEmbeddingService`
- Pros: ya hace `findConfig`, audit, métricas y contenía la verificación de roles (comentada). Reutilizarlo evita duplicar la lógica y mantiene una única fuente de verdad para la política.
- Limitación práctica: la verificación de roles dentro de `MetabaseEmbeddingService` está en un método `private checkAuthorization(...)`. Reutilizarla requeriría exponer una API pública (por ejemplo `boolean canAccess(Authentication, int dashboardId)` o un método `checkAccessOrThrow(...)`) o refactorizar para extraer la lógica autoritativa a un helper/bean compartido.
- Trabajo mínimo: añadir un método público en `MetabaseEmbeddingService` o extraer la lógica a un nuevo `DashboardAccessService` que ambos puedan usar. También reactivar la sección comentada de autorización (si se decide).

2) Implementar `DashboardAccessService` que use `MetabaseProperties`
- Pros: cambio pequeño y controlado; no hay que tocar `MetabaseEmbeddingService` si prefieres no tocarlo; fácil de testear. Puede leer `MetabaseProperties` (ya mapeada) y decidir.
- Comportamiento sugerido:
  - Bypass para `ROLE_ADMIN`.
  - Buscar dashboard config en `MetabaseProperties.dashboards` por id.
  - Comparar `authentication.getAuthorities()` con `config.getAllowedRoles()` (strings).
  - Registrar auditoría (usar `DashboardAuditService.logDashboardAccess(username, id, dashboardName, success)`).
  - Cachear la decisión por clave user:dashboard 60s (Caffeine or Spring `@Cacheable`).
- Trabajo mínimo: crear interfaz + impl + inyección en `DashboardExplanationService`.

Dónde colocar la comprobación
- En `DashboardExplanationService` (capa Service) al inicio del flujo, antes de ejecutar queries/consumir Gemini. Razón: centralizar la seguridad de negocio y proteger cualquier invocador (no sólo el controlador).
- Después de comprobar, si no autorizado lanzar `DashboardAccessDeniedException` o `AccessDeniedException`. Con `DashboardAccessDeniedException` ya existente se mantiene coherencia con Metabase code and comments.

Mapeado a HTTP 403
- Si hay un GlobalExceptionHandler que ya maneja `DashboardAccessDeniedException` → perfecto (devuelve 403).
- Si no hay handler (no encontramos GlobalExceptionHandler), alternativas seguras:
  - Añadir en `AiExplanationController` un `@ExceptionHandler(DashboardAccessDeniedException.class)` que devuelva 403 y registro.
  - O añadir un `@ControllerAdvice` global (recomendado) que convierta `DashboardAccessDeniedException` → `ResponseEntity.status(403)`.
- Nota: Spring Security también puede mapear `AccessDeniedException`, pero usar la excepción de dominio (`DashboardAccessDeniedException`) facilita logs y auditoría.

Auditoría y métricas
- Reutilizar `DashboardAuditService` para registrar intento con `success=false` en denegado y `success=true` en permitidos. `MetabaseEmbeddingService` ya lo hace; replicar el mismo patrón (audit + `meterRegistry.counter(...)`) es coherente.
- También registrar un metric counter `ai.explanation.access_denied` o incrementar `ai.explanation.requests outcome=forbidden` en `MeterRegistry`.

Tests que hay que añadir/ajustar
- Unit tests para `DashboardAccessServiceImpl`: varios casos (user allowed, user denied, dashboard absent).
- Controller tests: forzar `@WithMockUser(roles="...")` con roles no permitidos y verificar 403 and that `explainDashboard` service is NOT called.
- Service tests: inject a mock `DashboardAccessService` and assert behavior.

Edge cases y puntos de atención
- Si `metabase.dashboards` no contiene el `dashboardId`, la política segura es DENY (403), no 404 — se evita exponer existencia de objetos sensibles.
- Asegurar que los roles en JWT/Authorites tengan el formato esperado (p. ej. `"ROLE_USER"`). Testear con `@WithMockUser(roles = {"USER"})` (Spring prefixa con ROLE_).
- Cambios en roles o mappings requieren invalidar cache; usar TTL corto (~60s) para garantizar eventual consistencia.
- Si `DashboardExplanationService` es invocado por otros canales (no solo el controller) la verificación en Service protege todos los caminos.
- Si se reutiliza `MetabaseEmbeddingService`, documentar y probar que su método público no altera side-effects (ej. auditoría doble).

Cambios mínimos necesarios (lista concreta)
- Crear `DashboardAccessService` (o exponer método público en `MetabaseEmbeddingService`).
- Implementación `DashboardAccessServiceImpl` que:
  - usa `MetabaseProperties` to get `allowedRoles`
  - verifica `authentication.getAuthorities()`
  - bypass `ROLE_ADMIN`
  - audit via `DashboardAuditService` when denied/success
  - cache decision 60s with Caffeine or `@Cacheable`
- Inyectar `DashboardAccessService` into `DashboardExplanationService` and call it at the top of `explainDashboard(...)`:
  - if false → throw `DashboardAccessDeniedException`
- Ensure there is a handler that maps `DashboardAccessDeniedException` -> 403 (global `@ControllerAdvice` or controller-level `@ExceptionHandler`).
- Add unit and integration tests.

Estimación esfuerzo y riesgos
- Esfuerzo (small): ~ 1–2 horas de dev para change & tests (si se implementa con `MetabaseProperties`); menos si se usa `MetabaseEmbeddingService` reusando internals (pero eso requiere refactor privado->público).
- Riesgos: baja probabilidad de breaking changes si implementas alias/handler correctamente; principal riesgo es mismatch entre nombres de roles (JWT vs allowedRoles strings) — validar con tests.
- Reversibilidad: alta (pequeños commits, localizados).

Ejemplo de integración (solo para clarificar, no aplicar)
- Pseudocódigo que ilustra la idea (no ejecutar):

DashboardAccessService.canAccess(auth, dashboardId):
- return true if auth contains ROLE_ADMIN
- find dashboard config by id from MetabaseProperties
- for each authority in auth, if allowedRoles contains authority -> return true
- audit denied -> return false

In DashboardExplanationService.explainDashboard(request):
- Authentication auth = SecurityContextHolder.getContext().getAuthentication()
- if (!dashboardAccessService.canAccess(auth, request.dashboardId())) throw new DashboardAccessDeniedException("Forbidden")
- continue...

ControllerAdvice (global) or controller handler:
- handle DashboardAccessDeniedException -> return 403 with structured body and audit log

Conclusión práctica
- Implementar la solución que propones es totalmente viable y recomendada. El repositorio ya contiene los bloques necesarios (config props, audit, excepciones) por lo que el trabajo es de bajo costo y alto impacto en seguridad.
- Recomendación de ejecución: crear DashboardAccessServiceImpl que use MetabaseProperties + Caffeine 60s + DashboardAuditService; inyectarlo en DashboardExplanationService y lanzar DashboardAccessDeniedException en denegado; añadir/confirmar handler que devuelva 403; añadir tests.

15) Cambios introducidos en la implementación de R1 y R2
A continuación se lista, por archivo, el conjunto completo de cambios aplicados al código para resolver R1 (Rate Limiter para `aiExplanation`) y R2 (TTL dinámico aplicado en caché) — esto incluye propiedades, configuración de caché y adaptaciones en el servicio.

- `src/main/resources/application.properties`
  - Añadidas propiedades de Rate Limiter para `aiExplanation`:
    - `resilience4j.ratelimiter.instances.aiExplanation.limit-for-period=5`
    - `resilience4j.ratelimiter.instances.aiExplanation.limit-refresh-period=60s`
    - `resilience4j.ratelimiter.instances.aiExplanation.timeout-duration=0s`
  - Añadidos nombres de caché configurables para AI Explanation:
    - `ai.explanation.cache-name-historical=aiExplanationsHistorical`
    - `ai.explanation.cache-name-current=aiExplanationsCurrent`
  - (Notas): se mantuvieron las propiedades `gemini.*` y `ai.explanation.send-pii` existentes; las nuevas propiedades son adiciones no disruptivas.

- `src/main/java/com/cambiaso/ioc/config/CacheConfig.java`
  - Nuevo bean `CacheManager` con dos cachés Caffeine pre-registradas:
    - `aiExplanationsHistorical` — `expireAfterWrite(24 hours)`, `maximumSize=5000`, `recordStats()`
    - `aiExplanationsCurrent` — `expireAfterWrite(30 minutes)`, `maximumSize=5000`, `recordStats()`
  - Objetivo: proporcionar expiración por clase de frescura (histórico vs actual) sin gestionar expiraciones por entrada manualmente.

- `src/main/java/com/cambiaso/ioc/service/ai/DashboardExplanationService.java`
  - Nuevas propiedades inyectadas (configurables):
    - `@Value("${ai.explanation.cache-name-historical:aiExplanationsHistorical}") private String cacheNameHistorical;`
    - `@Value("${ai.explanation.cache-name-current:aiExplanationsCurrent}") private String cacheNameCurrent;`
  - Nuevo método privado `selectCacheName(LocalDate fechaFin)` que devuelve `cacheNameHistorical` si `fechaFin < today` o `cacheNameCurrent` en caso contrario.
  - FASE 0 (nueva en el flujo): selección de caché basada en `fechaFin` y uso de la caché seleccionada para lookup/put; los counters/metrics ahora incluyen una etiqueta `type` indicando `aiExplanationsHistorical` o `aiExplanationsCurrent`.
  - `calculateCacheTTL(...)` se mantiene (devuelve TTL en segundos) y ahora la TTL semántica se refleja en la caché física mediante la selección de la cache apropiada.
  - Nota: la lógica de prompt, llamadas Gemini, parsing y fallback se mantuvieron sin cambios funcionales.

- `src/main/java/com/cambiaso/ioc/controller/AiExplanationController.java`
  - Cambiada la anotación del POST para exponer un alias y mantener retrocompatibilidad:
    - `@PostMapping(path = {"/explain", "/explain-dashboard"})`
  - Objetivo: resolver la divergencia R3 y permitir que clientes que llamen a `/explain-dashboard` continúen funcionando.

- `.gemini/validation/TD-001A-blueprint-implementation-validation.md`
  - Actualizado el informe para marcar R1 y R2 como resueltos y añadir una sección (esta) con el registro de cambios aplicados, la evidencia y recomendaciones relacionadas.

Correcciones aplicadas (post-test failures) — 2025-11-13

Contexto: durante la ejecución de la suite de tests de integración del controlador `AiExplanationControllerTest` aparecieron fallos (500) y errores de compilación/IDE relacionados con el handler de excepciones y la ausencia de configuración de RateLimiter en los properties de test. Se investigó, se corrigió el código y se verificó mediante ejecución de tests.

Resumen rápido
- Síntoma: varios tests del controlador devolvían HTTP 500 en lugar de los códigos esperados (200/400/408/503). IDE marcaba "Cannot resolve symbol 'exceptions'" en `AiExplanationController.java`.
- Causa raíz: (1) referencia a una clase inexistente `io.github.resilience4j.core.exceptions.RequestNotPermitted` (handler duplicado incompatible con las dependencias actuales), y (2) falta de configuración de la instancia `aiExplanation` del rate limiter en los archivos de propiedades de test (`application-test.properties` y `application-pgtest.properties`).

Cambios realizados
- `src/main/java/com/cambiaso/ioc/controller/AiExplanationController.java`
  - Eliminado el `@ExceptionHandler` que apuntaba a `io.github.resilience4j.core.exceptions.RequestNotPermitted` y se mantuvo un único handler para `io.github.resilience4j.ratelimiter.RequestNotPermitted`. Esto elimina la referencia a un paquete/clase que no existe en el classpath y evita errores de compilación/IDE.

- `src/test/resources/application-test.properties` y `src/test/resources/application-pgtest.properties`
  - Añadida configuración permisiva para la instancia `aiExplanation` del rate limiter para el entorno de tests:
    ```properties
    resilience4j.ratelimiter.instances.aiExplanation.limit-for-period=1000
    resilience4j.ratelimiter.instances.aiExplanation.limit-refresh-period=1s
    resilience4j.ratelimiter.instances.aiExplanation.timeout-duration=0s
    ```
  - Objetivo: evitar que el rate limiter en tests cause excepciones no gestionadas durante el inicio o las llamadas a endpoints mockeados.

Verificación ejecutada
- Ejecutado: `mvn -DskipTests=false -Dtest=AiExplanationControllerTest test` (ejecución local en el entorno del repo)
- Resultado: Tests corridos: 13 — Failures: 0, Errors: 0, Skipped: 0
- Observaciones de log: se muestran handlers de parámetros faltantes/validaciones y logs de éxito esperados para los escenarios del test.

Comandos para reproducir localmente
- Ejecutar el test del controlador (rápido):

```bash
cd /mnt/ssd-480/repos/captone/ioc-backend
mvn -DskipTests=false -Dtest=AiExplanationControllerTest test
```

- Ejecutar todos los tests del proyecto (puede tardar más):

```bash
mvn test
```

Notas y recomendaciones
- Mantener en el repo la configuración del rate limiter para entornos de test (permisiva) evita falsos positivos en CI cuando se usan `@RateLimiter` con Resilience4j.
- Evitar referencias a clases de paquetes no presentes en el classpath (por ejemplo, `io.github.resilience4j.core.exceptions`) — en su lugar usar las clases provistas por las dependencias actuales (`io.github.resilience4j.ratelimiter.RequestNotPermitted`).
- Si se planea soportar múltiples versiones de Resilience4j con paquetes distintos, considerar un manejador genérico de excepciones o centralizar la conversión de excepciones en una capa común.

Registro de cambios (commits aplicados por acción manual en workspace)
- Editado `AiExplanationController.java` para eliminar el handler duplicado.
- Actualizados `application-test.properties` y `application-pgtest.properties` en `src/test/resources` para incluir `aiExplanation`.

---

(Se añadió este bloque el 2025-11-13 para documentar la corrección de fallos detectados en la validación y su verificación mediante tests.)
