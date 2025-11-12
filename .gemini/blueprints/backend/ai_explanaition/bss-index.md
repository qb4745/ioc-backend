# Índice de Backend Service Specifications (BSS)

Fecha de generación: 2025-11-12
Generado a partir de: `TD-001A-dashboard-ai-explanation-A.md` (FP-001A)

---

## Resumen
Este índice agrupa las especificaciones de servicio backend (BSS) generadas para la feature **FP-001A - Explicación de Dashboard con Gemini**. Cada entrada apunta al documento BSS correspondiente y resume su propósito, estado y puntos de verificación rápidos para implementación.

> Ubicación de los BSS:
> `.gemini/blueprints/backend/`

---

## Lista de BSS generados

1. BSS-001 — `BSS-001-DashboardAnalyticsRepository.md`
   - Ruta: `.gemini/blueprints/backend/BSS-001-DashboardAnalyticsRepository.md`
   - Tipo: Repository (NamedParameterJdbcTemplate)
   - Propósito: Consultas analíticas (Totales, Top operarios, Distribución por turno, Top máquinas, Tendencia diaria).
   - Estado: DRAFT
   - Implementación recomendada: métodos read-only mapeando a Records DTO; validación de rango de 12 meses.

2. BSS-002 — `BSS-002-GeminiApiClient.md`
   - Ruta: `.gemini/blueprints/backend/BSS-002-GeminiApiClient.md`
   - Tipo: External API Client (WebClient)
   - Propósito: Encapsular llamadas a Google Gemini (timeout 90s, retries, parsing)
   - Estado: DRAFT
   - Implementación recomendada: WebClient.Builder bean, excepciones personalizadas, tests con WireMock.

3. BSS-003 — `BSS-003-DashboardExplanationService.md`
   - Ruta: `.gemini/blueprints/backend/BSS-003-DashboardExplanationService.md`
   - Tipo: Service (Orquestación)
   - Propósito: Orquestar flujo (cache, queries, prompt build, llamada a Gemini, parseo, auditoría)
   - Estado: DRAFT
   - Implementación recomendada: cache Caffeine, métricas Micrometer, anonimización PII opcional.

4. BSS-004 — `BSS-004-AiExplanationController.md`
   - Ruta: `.gemini/blueprints/backend/BSS-004-AiExplanationController.md`
   - Tipo: REST Controller
   - Propósito: Exponer `POST /api/v1/ai/explain-dashboard` con validación, seguridad y rate limiting
   - Estado: DRAFT
   - Implementación recomendada: `@PreAuthorize`, Resilience4j rate limiter, manejo global de excepciones.

---

## Checklist rápido para comenzar la implementación
- [ ] Revisar pre-requisitos en `TD-001A` (datos, presupuesto Gemini, concurrencia esperada).
- [ ] Crear rama: `feature/fp-001A-ai-dashboard-explanation`.
- [ ] Implementar BSS-001 (repositorio) y tests unitarios primero.
- [ ] Verificar dependencia `commons-codec` (SHA-256). Ejecutar `mvn dependency:tree | grep commons-codec`.
- [ ] Implementar BSS-002 (GeminiApiClient) con WireMock tests.
- [ ] Implementar BSS-003 (servicio) y pruebas de integración con mocks.
- [ ] Implementar BSS-004 (controller) y pruebas MockMvc + seguridad.
- [ ] Ejecutar cobertura, lint y revisión de seguridad; realizar code review.

---

## Mapa TD -> BSS (cobertura)
- TD Sección 7 (Capa de Acceso a Datos) → BSS-001
- TD Sección 11 / 11.5 (Gemini Integration & snippets) → BSS-002
- TD Sección 4,5,8,10,15 (Arquitectura, Flujo, API, Caching, Observabilidad) → BSS-003
- TD Sección 8 (API Contract) + 17 (Plan implementación) → BSS-004

---

## QA / Validaciones sugeridas antes del PR
- Ejecutar tests unitarios e integración localmente.
- Probar endpoint con stub de Gemini (WireMock) y dataset pequeño.
- Validar métricas Micrometer y logs estructurados en modo local.
- Verificar que `GEMINI_API_KEY` no esté en el repo.
- Comprobar que las properties sugeridas se añadan a `application-*.properties`.

---

## Cómo generar más BSS automáticamente
Si quieres generar más BSS desde un TD existente, usa el prompt ` .gemini/prompts-v2/08-generate-backend-service-spec.md` (plantilla generadora). Flujo recomendado:
1. Asegurar que el TD contiene: modelo de datos, contratos de API y componentes backend.
2. Ejecutar el generador (herramienta interna) o crear BSS manualmente siguiendo las secciones de los BSS existentes.

---

## Notas finales
- Fecha: 2025-11-12
- Autor: Generado automáticamente a partir de `TD-001A-dashboard-ai-explanation-A.md`
- Estado global de BSS: Draft — revisar con Tech Lead y Data Team antes de merge.

---

> Si quieres, puedo:
> - Añadir checklist de PR/CI (comandos `mvn test`, `mvn -DskipTests=false verify`).
> - Crear los archivos `system-prompt.txt` y `context.yaml` como recursos iniciales para pruebas.
> - Generar un `README.md` en la carpeta con pasos de implementación.


