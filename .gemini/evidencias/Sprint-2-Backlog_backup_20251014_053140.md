# Sprint 2 – Sprint Backlog (Desatando la Interactividad Analítica)

## Metadata del Sprint

**Proyecto:** Plataforma IOC - Sistema de Business Intelligence  
**Sprint:** Sprint 2 - Interactividad Analítica  
**Período:** 6 de Octubre - 25 de Octubre, 2025  
**Duración:** 3 semanas (15 días hábiles)  
**Equipo:**
- **Product Owner:** Boris Arriagada
- **Scrum Master:** Jaime Vicencio
- **Development Team:** Boris Arriagada (Backend/Frontend), Jaime Vicencio (Frontend/Backend)

**Festivos/No laborables:** Ninguno durante este período

**Velocity del Equipo:** ~10 SP/semana (basado en Sprint 1: 41 SP en 4 semanas)  
**Capacidad del Sprint:** 21 Story Points

---

## 🎯 SPRINT GOAL

**"Transformar el dashboard estático en una herramienta de análisis interactiva donde el usuario final pueda filtrar datos por línea, período y taller, además de comparar desempeño entre turnos y líneas para investigar patrones de rendimiento y exportar resultados."**

Al finalizar este Sprint, los gerentes y supervisores podrán:
- Aplicar filtros dinámicos a los dashboards para análisis específicos
- Comparar métricas entre diferentes turnos, líneas y períodos
- Exportar visualizaciones y datos en formato PDF/Excel para reportes
- Identificar patrones de rendimiento y oportunidades de mejora mediante comparativas interactivas

**Valor de Negocio:** Habilitar análisis profundo y toma de decisiones basada en comparativas, eliminando la necesidad de exportar a herramientas externas.

---

## Historias Comprometidas

| ID | Título | Tipo | Feature | Prioridad | SP | Asignado | Estado |
| :--- | :--- | :--- | :--- | :--- | :-: | :--- | :--- |
| IOC-008 | Como gerente, quiero comparar el desempeño entre turnos y líneas para identificar patrones y oportunidades de mejora. | Historia de Usuario | Análisis Comparativo | Crítica | 13 | Boris | 📋 Backlog |
| IOC-012 | Como supervisor-analista, quiero filtrar datos por línea, periodo y taller para realizar análisis detallados y específicos. | Historia de Usuario | Filtrado Dinámico | Crítica | 8 | Jaime | 📋 Backlog |

**Total Story Points Comprometidos:** 21 SP

---

## Criterios de Aceptación

### **IOC-008: Como gerente, quiero comparar el desempeño entre turnos y líneas para identificar patrones y oportunidades de mejora.**

**Contexto:** Los gerentes necesitan identificar qué turnos o líneas tienen mejor desempeño para replicar mejores prácticas y detectar áreas problemáticas.

**Criterios de Aceptación:**

✅ **Comparativa generada correctamente**
- **Dado** un gerente autenticado en el dashboard de comparativas
- **Cuando** selecciona dos o más entidades (turnos/líneas) y un rango de fechas
- **Entonces** el sistema muestra gráficos comparativos lado a lado con métricas clave (producción, eficiencia, defectos) en menos de 3 segundos

✅ **Datos insuficientes manejados apropiadamente**
- **Dado** un gerente que intenta generar una comparativa
- **Cuando** no existen datos suficientes para alguna de las entidades seleccionadas
- **Entonces** el sistema muestra un mensaje claro indicando "Datos insuficientes para [entidad]: Se requieren al menos 5 registros para generar comparativa" y sugiere ampliar el rango de fechas

✅ **Exportar resultados de comparativa**
- **Dado** un gerente visualizando una comparativa exitosa
- **Cuando** hace clic en "Exportar" y selecciona formato (PDF o Excel)
- **Entonces** el sistema genera y descarga un archivo con:
  - Gráficos comparativos (en PDF)
  - Tabla de datos con métricas detalladas
  - Metadatos (fecha de generación, filtros aplicados, usuario)
  - Descarga completada en menos de 5 segundos

✅ **Actualización dinámica sin recarga**
- **Dado** un gerente en la vista de comparativas
- **Cuando** modifica los filtros (cambia entidades, ajusta fechas, selecciona métricas)
- **Entonces** el sistema actualiza los gráficos sin recargar toda la página, mostrando un indicador de carga sutil durante la actualización (máximo 2 segundos)

---

### **IOC-012: Como supervisor-analista, quiero filtrar datos por línea, periodo y taller para realizar análisis detallados y específicos.**

**Contexto:** Los supervisores necesitan enfocarse en datos específicos de su área de responsabilidad sin ruido de otras líneas o períodos.

**Criterios de Aceptación:**

✅ **Aplicación de filtros exitosa**
- **Dado** un supervisor-analista autenticado en un dashboard
- **Cuando** selecciona filtros (línea específica, rango de fechas, taller) y hace clic en "Aplicar"
- **Entonces** todos los gráficos y KPIs del dashboard se actualizan mostrando solo los datos que coinciden con los filtros, y se muestra un badge visual indicando "Filtros activos: 3"

✅ **Ningún dato tras aplicar filtro**
- **Dado** un supervisor que ha aplicado filtros muy restrictivos
- **Cuando** la combinación de filtros no arroja resultados (ej. línea X en fecha donde no operó)
- **Entonces** el sistema muestra un mensaje informativo: "No se encontraron datos para los filtros seleccionados. Intenta ampliar el rango de fechas o seleccionar otra línea" sin mostrar gráficos vacíos

✅ **Filtros inválidos prevenidos**
- **Dado** un supervisor configurando filtros
- **Cuando** intenta aplicar parámetros incorrectos (ej. fecha de inicio posterior a fecha fin, formato de fecha inválido)
- **Entonces** el sistema bloquea la aplicación, muestra un mensaje de error específico junto al campo problemático ("La fecha de inicio debe ser anterior a la fecha de fin") y resalta el campo en rojo

✅ **Reset de filtros y persistencia**
- **Dado** un supervisor con filtros activos
- **Cuando** hace clic en "Limpiar filtros"
- **Entonces** todos los campos de filtro vuelven a su estado por defecto y el dashboard muestra datos completos sin filtros
- **Y** los filtros aplicados persisten en la sesión del usuario (si recarga la página, los filtros se mantienen hasta que los limpie explícitamente)

✅ **Filtros con autocompletado**
- **Dado** un supervisor escribiendo en el campo de filtro "Línea"
- **Cuando** comienza a escribir (ej. "Línea 0")
- **Entonces** el sistema muestra sugerencias de autocompletado basadas en las líneas disponibles en la base de datos, facilitando la selección

---

## Checklist de Tareas Técnicas

| Nº | ID | Capa | Historia | Responsable | Descripción | Estado |
| :-: | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | FE-TASK-18 | Frontend | IOC-012 | Jaime | Crear componente `DashboardFilters.tsx` con campos de filtro (línea, período, taller) usando React Hook Form y Zod para validación | ⬜ Pendiente |
| 2 | FE-TASK-19 | Frontend | IOC-012 | Jaime | Implementar lógica de autocompletado en el campo "Línea" usando Combobox de Headless UI, conectado a endpoint `/api/v1/filters/lines` | ⬜ Pendiente |
| 3 | FE-TASK-20 | Frontend | IOC-012 | Jaime | Crear hook personalizado `useFilters()` para manejar estado de filtros, validación, aplicación y reset con persistencia en localStorage | ⬜ Pendiente |
| 4 | FE-TASK-21 | Frontend | IOC-012 | Jaime | Integrar `DashboardFilters` con `DashboardEmbed.tsx` para pasar parámetros de filtro a Metabase mediante URL params | ⬜ Pendiente |
| 5 | FE-TASK-22 | Frontend | IOC-012 | Jaime | Implementar badge visual "Filtros activos: X" y botón "Limpiar filtros" con animaciones de transición | ⬜ Pendiente |
| 6 | FE-TASK-23 | Frontend | IOC-012 | Jaime | Manejar casos edge: mostrar mensaje "No hay datos" cuando filtros no arrojan resultados, validar fechas inválidas con feedback en tiempo real | ⬜ Pendiente |
| 7 | BE-TASK-25 | Backend | IOC-012 | Jaime | Crear `FilterController.java` con endpoint GET `/api/v1/filters/lines` para obtener lista de líneas disponibles desde `dim_line` | ⬜ Pendiente |
| 8 | BE-TASK-26 | Backend | IOC-012 | Jaime | Crear `FilterController.java` con endpoint GET `/api/v1/filters/workshops` para obtener lista de talleres disponibles desde `dim_workshop` | ⬜ Pendiente |
| 9 | BE-TASK-27 | Backend | IOC-012 | Jaime | Implementar `FilterService.java` con caché (Caffeine, TTL 1 hora) para reducir queries repetitivas de líneas/talleres | ⬜ Pendiente |
| 10 | BE-TASK-28 | Backend | IOC-012 | Jaime | Modificar `MetabaseEmbeddingService.java` para aceptar parámetros de filtro y construir payload JWT con filtros (`locked_parameters`) | ⬜ Pendiente |
| 11 | BE-TASK-29 | Backend | IOC-012 | Jaime | Añadir validación de fechas en `FilterController`: fecha_inicio <= fecha_fin, rango máximo 365 días, formato ISO-8601 | ⬜ Pendiente |
| 12 | FE-TASK-24 | Frontend | IOC-008 | Boris | Crear página `ComparativeDashboardPage.tsx` con layout de selección de entidades (multi-select para turnos/líneas) y selector de rango de fechas | ⬜ Pendiente |
| 13 | FE-TASK-25 | Frontend | IOC-008 | Boris | Implementar hook `useComparative()` para gestionar estado de selección de entidades, llamadas a API `/api/v1/analytics/compare` y manejo de loading/error | ⬜ Pendiente |
| 14 | FE-TASK-26 | Frontend | IOC-008 | Boris | Crear componente `ComparativeChartsGrid.tsx` para renderizar gráficos lado a lado (Chart.js o Recharts) con métricas: producción, eficiencia, defectos | ⬜ Pendiente |
| 15 | FE-TASK-27 | Frontend | IOC-008 | Boris | Implementar lógica de manejo de "datos insuficientes": mostrar mensaje específico por entidad cuando no hay suficientes registros (< 5) | ⬜ Pendiente |
| 16 | FE-TASK-28 | Frontend | IOC-008 | Boris | Crear componente `ExportButton.tsx` con dropdown para seleccionar formato (PDF/Excel) y lógica de descarga llamando a `/api/v1/analytics/export` | ⬜ Pendiente |
| 17 | FE-TASK-29 | Frontend | IOC-008 | Boris | Implementar actualización dinámica: cuando el usuario modifica filtros, disparar re-fetch sin recargar página completa, mostrar skeleton loading | ⬜ Pendiente |
| 18 | FE-TASK-30 | Frontend | IOC-008 | Boris | Añadir tests E2E con Playwright para flujo completo: seleccionar entidades → visualizar comparativa → exportar PDF → verificar descarga | ⬜ Pendiente |
| 19 | BE-TASK-30 | Backend | IOC-008 | Boris | Crear `AnalyticsController.java` con endpoint POST `/api/v1/analytics/compare` recibiendo DTO con entidades (turnos/líneas) y rango de fechas | ⬜ Pendiente |
| 20 | BE-TASK-31 | Backend | IOC-008 | Boris | Implementar `ComparativeAnalyticsService.java` para ejecutar queries agregadas en `fact_production` agrupando por entidad seleccionada | ⬜ Pendiente |
| 21 | BE-TASK-32 | Backend | IOC-008 | Boris | Crear DTOs de respuesta: `ComparativeResultDTO` con lista de `EntityMetricsDTO` (nombre, producción_total, eficiencia_avg, defectos_total, num_registros) | ⬜ Pendiente |
| 22 | BE-TASK-33 | Backend | IOC-008 | Boris | Implementar validación de "datos suficientes": si una entidad tiene < 5 registros, incluir flag `insufficient_data: true` en el DTO de respuesta | ⬜ Pendiente |
| 23 | BE-TASK-34 | Backend | IOC-008 | Boris | Crear `ExportController.java` con endpoint POST `/api/v1/analytics/export` recibiendo datos de comparativa y formato (PDF/EXCEL) | ⬜ Pendiente |
| 24 | BE-TASK-35 | Backend | IOC-008 | Boris | Implementar `ReportExportService.java` usando Apache POI para Excel y iText/Flying Saucer para PDF, incluyendo gráficos y metadatos | ⬜ Pendiente |
| 25 | BE-TASK-36 | Backend | IOC-008 | Boris | Añadir dependencias Maven: Apache POI (5.2.3) para Excel, Flying Saucer (9.1.22) para PDF, configurar límite de tamaño de export (max 10,000 filas) | ⬜ Pendiente |
| 26 | BE-TASK-37 | Backend | IOC-008 | Boris | Optimizar queries de comparativa: añadir índices compuestos en `fact_production` (`date`, `line_id`, `shift_id`) para acelerar agregaciones | ⬜ Pendiente |
| 27 | BE-TASK-38 | Backend | IOC-008 | Boris | Implementar caché de resultados de comparativa con Caffeine (TTL 5 minutos, max 100 entradas) para reducir carga en BD con filtros repetidos | ⬜ Pendiente |
| 28 | BE-TASK-39 | Backend | IOC-008 | Boris | Añadir logging detallado y métricas Micrometer: contador de comparativas generadas, timer de duración de queries, gauge de tamaño de exports | ⬜ Pendiente |
| 29 | TEST-TASK-01 | Testing | IOC-012 | Jaime | Escribir tests unitarios para `FilterService`: validar respuestas de líneas/talleres, verificar comportamiento de caché (hit/miss) | ⬜ Pendiente |
| 30 | TEST-TASK-02 | Testing | IOC-012 | Jaime | Escribir tests de integración para `FilterController`: verificar endpoints GET retornan datos correctos, manejo de errores 404 si no hay datos | ⬜ Pendiente |
| 31 | TEST-TASK-03 | Testing | IOC-012 | Jaime | Escribir tests E2E con Playwright: aplicar filtros → verificar actualización de dashboard → limpiar filtros → verificar reset | ⬜ Pendiente |
| 32 | TEST-TASK-04 | Testing | IOC-008 | Boris | Escribir tests unitarios para `ComparativeAnalyticsService`: mockear repositorios, verificar cálculos de agregaciones, validar flag de datos insuficientes | ⬜ Pendiente |
| 33 | TEST-TASK-05 | Testing | IOC-008 | Boris | Escribir tests de integración para `AnalyticsController`: probar endpoint POST con datos válidos, inválidos, casos límite (0 entidades, 10 entidades) | ⬜ Pendiente |
| 34 | TEST-TASK-06 | Testing | IOC-008 | Boris | Escribir tests para `ReportExportService`: generar PDF/Excel con datos mock, verificar estructura del archivo, metadatos correctos | ⬜ Pendiente |
| 35 | OPS-TASK-01 | DevOps | IOC-008, IOC-012 | Jaime | Actualizar pipeline CI/CD para ejecutar nuevos tests E2E de filtros y comparativas antes de deployment a staging | ⬜ Pendiente |
| 36 | OPS-TASK-02 | DevOps | IOC-008 | Boris | Configurar límites de recursos para exportación: timeout 30s, max heap 512MB para generación de PDFs grandes, configurar en application.yml | ⬜ Pendiente |
| 37 | DOC-TASK-01 | Documentación | IOC-008, IOC-012 | Jaime | Actualizar `/docs/api-endpoints.md` con nuevos endpoints de filtros, comparativas y exportación incluyendo ejemplos de payloads | ⬜ Pendiente |
| 38 | DOC-TASK-02 | Documentación | IOC-008, IOC-012 | Boris | Crear guía de usuario `/docs/user-guide-filters-comparatives.md` con capturas de pantalla y ejemplos de uso para gerentes y supervisores | ⬜ Pendiente |

**Total de Tareas:** 38 tareas técnicas

---

## Progreso del Sprint

### Historias Completadas: 0/2 (0%)
- 📋 IOC-008: Como gerente, quiero comparar el desempeño entre turnos y líneas para identificar patrones y oportunidades de mejora.
- 📋 IOC-012: Como supervisor-analista, quiero filtrar datos por línea, periodo y taller para realizar análisis detallados y específicos.

### Story Points Completados: 0/21 (0%)
- Comprometidos: 21 SP
- Completados: 0 SP
- Restantes: 21 SP

### Distribución de Trabajo por Miembro

**Jaime Vicencio (IOC-012 - Filtrado):**
- Frontend: 6 tareas (FE-TASK-18 a FE-TASK-23)
- Backend: 5 tareas (BE-TASK-25 a BE-TASK-29)
- Testing: 3 tareas (TEST-TASK-01 a TEST-TASK-03)
- DevOps: 1 tarea (OPS-TASK-01)
- Documentación: 1 tarea (DOC-TASK-01)
- **Total: 16 tareas**

**Boris Arriagada (IOC-008 - Comparativas):**
- Frontend: 7 tareas (FE-TASK-24 a FE-TASK-30)
- Backend: 10 tareas (BE-TASK-30 a BE-TASK-39)
- Testing: 3 tareas (TEST-TASK-04 a TEST-TASK-06)
- DevOps: 1 tarea (OPS-TASK-02)
- Documentación: 1 tarea (DOC-TASK-02)
- **Total: 22 tareas**

**Carga Balanceada:** La distribución es equilibrada considerando que IOC-008 (13 SP) es más compleja que IOC-012 (8 SP).

---

## Riesgos y Dependencias

### Dependencias Técnicas

1. **Metabase - Sistema de Filtros Parametrizados**
   - **Impacto:** Crítico para IOC-012
   - **Descripción:** Los filtros del dashboard dependen de que Metabase soporte parámetros dinámicos (`locked_parameters` en JWT)
   - **Estado:** 🟡 **A Validar**
   - **Mitigación:**
     - Investigar documentación de Metabase Embedding con filtros durante los primeros 2 días del sprint
     - Si Metabase no soporta filtros dinámicos, implementar solución alternativa: crear múltiples dashboards pre-filtrados
     - Plan B: Construir gráficos custom en el frontend usando Chart.js consultando directamente `/api/v1/analytics`

2. **Apache POI y Flying Saucer (Librerías de Exportación)**
   - **Impacto:** Alto para IOC-008 (funcionalidad de exportación)
   - **Descripción:** Generación de PDFs/Excel depende de librerías externas que pueden tener curva de aprendizaje
   - **Estado:** 🟡 **A Validar**
   - **Mitigación:**
     - Spike técnico de 4 horas en el día 1 para probar generación básica de PDF y Excel
     - Documentar ejemplos de código reutilizables
     - Alternativa: Usar librerías más simples como jsPDF (lado cliente) si las dependencias backend son problemáticas

3. **Rendimiento de Queries Agregadas**
   - **Impacto:** Alto para IOC-008
   - **Descripción:** Comparativas pueden requerir queries complejas con GROUP BY sobre tablas grandes
   - **Estado:** 🟢 **Bajo Control**
   - **Mitigación:**
     - Implementar índices compuestos en `fact_production` (BE-TASK-37)
     - Limitar rango de fechas máximo a 365 días
     - Caché con Caffeine para queries repetidas
     - Monitorear con Micrometer y optimizar si query > 2 segundos

4. **Integración con Dashboard Existente (IOC-006)**
   - **Impacto:** Crítico para IOC-012
   - **Descripción:** Los filtros deben integrarse sin romper el dashboard de producción del Sprint 1
   - **Estado:** 🟢 **Bajo Control**
   - **Mitigación:**
     - Hacer los filtros opcionales (dashboard funciona sin filtros)
     - Tests de regresión para verificar que dashboard base sigue funcionando
     - Branch feature separada con PR review riguroso antes de merge

5. **Dependencia entre Historias**
   - **IOC-008 puede usar IOC-012:** Las comparativas podrían beneficiarse de filtros, pero no es bloqueante
   - **Estrategia:** Desarrollar ambas historias en paralelo, integrar al final del sprint si hay tiempo

---

### Riesgos Identificados y Mitigaciones

#### 🔴 Riesgos Críticos

1. **Complejidad de Integración de Filtros con Metabase JWT**
   - **Probabilidad:** Media (40%)
   - **Impacto:** Crítico - Bloquea IOC-012
   - **Descripción:** La documentación de Metabase sobre filtros dinámicos en embedded dashboards puede ser incompleta o la funcionalidad puede no existir como esperamos
   - **Estado:** 🔴 **No Mitigado**
   - **Plan de Mitigación:**
     - **Día 1-2:** Spike técnico para validar factibilidad (4 horas)
     - **Si falla:** Implementar Plan B - construir gráficos custom en frontend con Recharts consumiendo API REST directa
     - **Escalación:** Si el Plan B se activa, notificar al PO inmediatamente (puede afectar el alcance del sprint)
   - **Responsable:** Jaime Vicencio

2. **Generación de PDFs con Gráficos Complejos**
   - **Probabilidad:** Media (35%)
   - **Impacto:** Alto - Reduce valor de IOC-008
   - **Descripción:** Flying Saucer puede tener limitaciones para renderizar gráficos HTML5/Canvas complejos en PDF
   - **Estado:** 🟡 **Parcialmente Mitigado**
   - **Plan de Mitigación:**
     - **Día 3:** Spike de 3 horas para validar generación de PDF con chart básico
     - **Alternativa 1:** Usar capturas de pantalla (Puppeteer/Selenium) del dashboard para incrustar en PDF
     - **Alternativa 2:** Exportar solo Excel con datos tabulares (más simple) y marcar PDF como "Could Have"
     - **Reducción de Alcance:** Si consume > 8 horas, priorizar Excel y dejar PDF para Sprint 3
   - **Responsable:** Boris Arriagada

#### 🟡 Riesgos Medios

3. **Performance de Queries con Múltiples Agregaciones**
   - **Probabilidad:** Media (30%)
   - **Impacto:** Medio - UX degradada pero funcional
   - **Descripción:** Comparativas con 5+ entidades y rangos grandes pueden tardar > 5 segundos
   - **Estado:** 🟢 **Mitigado**
   - **Plan de Mitigación:**
     - Límites preventivos: máximo 5 entidades, máximo 365 días
     - Implementar índices compuestos (BE-TASK-37) antes de cualquier funcionalidad
     - Caché agresivo (TTL 5 min) para queries idénticas
     - Si query > 3 segundos, mostrar mensaje "Esto puede tardar un momento..." con progress spinner
   - **Métrica de Éxito:** 95% de queries < 3 segundos (medir con Micrometer)
   - **Responsable:** Boris Arriagada

4. **Inconsistencia de Datos en Caché vs Datos Reales**
   - **Probabilidad:** Baja (15%)
   - **Impacto:** Medio - Usuarios ven datos desactualizados
   - **Descripción:** Con TTL de 5 minutos en caché de comparativas, si se carga nuevo CSV, los resultados pueden estar obsoletos
   - **Estado:** 🟡 **Monitoreado**
   - **Plan de Mitigación:**
     - Documentar claramente el TTL en la UI: "Datos actualizados hace X minutos"
     - Botón "Refrescar" para invalidar caché manualmente
     - Evento de invalidación automática: cuando se completa un ETL job, limpiar caché de analytics
   - **Decisión:** Aceptar el trade-off de frescura por performance (5 min es aceptable para análisis históricos)
   - **Responsable:** Boris Arriagada

5. **Validación Insuficiente de Filtros Genera Errores en Metabase**
   - **Probabilidad:** Media (25%)
   - **Impacto:** Medio - Errores visibles al usuario
   - **Descripción:** Si pasamos parámetros inválidos a Metabase (fechas mal formateadas, IDs inexistentes), el iframe puede mostrar error genérico
   - **Estado:** 🟢 **Mitigado**
   - **Plan de Mitigación:**
     - Validación estricta en backend (BE-TASK-29): formato ISO-8601, existencia de IDs en BD
     - Validación en frontend con Zod: prevenir envío de datos inválidos
     - Manejo de errores en `DashboardEmbed.tsx`: capturar errores de iframe y mostrar mensaje amigable
     - Logging detallado: registrar todos los parámetros enviados a Metabase para debugging
   - **Responsable:** Jaime Vicencio

#### 🟢 Riesgos Bajos (Aceptados)

6. **Curva de Aprendizaje de Librerías de Exportación**
   - **Probabilidad:** Alta (60%)
   - **Impacto:** Bajo - Solo afecta tiempo de desarrollo
   - **Descripción:** Apache POI y Flying Saucer tienen APIs complejas, puede tomar tiempo dominarlas
   - **Estado:** ✅ **Aceptado**
   - **Mitigación:**
     - Presupuestar 6 horas de investigación/prueba en la estimación
     - Documentar ejemplos de código en Confluence para futuros desarrollos
     - Pair programming entre Boris y Jaime si hay bloqueos

7. **Autocompletado de Líneas Puede Ser Lento con Muchos Registros**
   - **Probabilidad:** Baja (10%)
   - **Impacto:** Bajo - Latencia apenas perceptible
   - **Descripción:** Si hay > 1000 líneas, el autocompletado puede ser lento
   - **Estado:** ✅ **Aceptado**
   - **Mitigación:**
     - Caché de 1 hora en backend para lista de líneas (BE-TASK-27)
     - Si el problema aparece, implementar paginación o búsqueda del lado servidor en Sprint 3

8. **Incompatibilidad de Formatos de Export entre Navegadores**
   - **Probabilidad:** Baja (10%)
   - **Impacto:** Bajo - Funciona en mayoría de casos
   - **Descripción:** Descarga de archivos puede comportarse diferente en Safari vs Chrome
   - **Estado:** ✅ **Aceptado**
   - **Mitigación:**
     - Usar headers HTTP estándar (`Content-Disposition: attachment`)
     - Testing manual en Chrome, Firefox, Safari antes de Sprint Review
     - Si hay problemas, documentar limitaciones conocidas

---

### Plan de Contingencia por Riesgo Crítico

**Si la integración con Metabase JWT falla (Riesgo #1):**

**Activación:** Día 2 del sprint, después del spike técnico, si se confirma que Metabase no soporta filtros dinámicos vía JWT.

**Acciones Inmediatas:**
1. **Hora 0:** Notificar al Product Owner (Boris) y Scrum Master (Jaime)
2. **Hora 1:** Reunión de emergencia para decidir entre:
   - **Opción A:** Implementar gráficos custom en frontend (Recharts + API REST) - +8 SP estimados
   - **Opción B:** Crear dashboards pre-filtrados en Metabase (uno por línea) - +5 SP estimados
   - **Opción C:** Reducir alcance: filtros básicos solo por fecha - mantener 8 SP
3. **Hora 2:** Actualizar Sprint Backlog con la opción elegida
4. **Día 3:** Comenzar implementación del Plan B

**Criterio de Éxito del Plan B:**
- Usuario puede filtrar dashboard por al menos fecha y una dimensión (línea o taller)
- Performance aceptable (< 3 segundos para aplicar filtros)
- DoD completo para funcionalidad reducida

**Impacto en Sprint Goal:**
- Sprint Goal se mantiene pero con alcance reducido
- Se acepta deuda técnica: filtros completos se mueven a Sprint 3

---

## Definition of Ready (DoR) - Verificación

Verificamos que ambas historias cumplen el DoR antes de iniciar el sprint:

### IOC-008: Comparar Desempeño entre Entidades

✅ **Historia de Usuario Completa:** "Como gerente, quiero comparar el desempeño entre turnos y líneas para identificar patrones y oportunidades de mejora" - Valor de negocio claro  
✅ **Criterios de Aceptación Definidos:** 4 criterios medibles en formato Given-When-Then  
✅ **Dependencias Identificadas:** Depende de datos de producción (IOC-001 completada en Sprint 1), librerías de exportación Apache POI/Flying Saucer  
✅ **Estimación Completada:** 13 SP acordados por el equipo  
✅ **Diseño y Assets Disponibles:** Wireframes de comparativas y mockups de exports disponibles en Figma

### IOC-012: Filtrar Datos del Dashboard

✅ **Historia de Usuario Completa:** "Como supervisor-analista, quiero filtrar datos por línea, periodo y taller para realizar análisis detallados y específicos" - Valor de negocio claro  
✅ **Criterios de Aceptación Definidos:** 5 criterios medibles incluyendo casos edge  
✅ **Dependencias Identificadas:** Depende de dashboard existente (IOC-006 completada), integración con Metabase JWT  
✅ **Estimación Completada:** 8 SP acordados por el equipo  
✅ **Diseño y Assets Disponibles:** Mockups de componente de filtros y estados de UI disponibles en Figma

**Ambas historias cumplen DoR ✅ - Sprint puede iniciarse**

---

## Definition of Done (DoD) - Checklist por Historia

Cada historia debe cumplir estos 5 criterios para moverse a "Done":

### IOC-008: Comparar Desempeño entre Entidades

**1. Código Completado y Revisado:**
- [ ] Página `ComparativeDashboardPage.tsx` implementada y funcionando
- [ ] Hook `useComparative()` con manejo completo de estado
- [ ] `AnalyticsController` y `ComparativeAnalyticsService` implementados
- [ ] `ReportExportService` generando PDFs y Excel correctamente
- [ ] Pull Request creado y aprobado por 1+ reviewer (mínimo Jaime revisa código de Boris)
- [ ] Merge a `main` completado sin conflictos

**2. Tests Implementados y Pasando:**
- [ ] Tests unitarios para `ComparativeAnalyticsService` (cobertura > 70%)
- [ ] Tests de integración para `AnalyticsController` (casos válidos, inválidos, límite)
- [ ] Tests para `ReportExportService` (generación de PDF/Excel)
- [ ] Test E2E con Playwright: flujo completo de comparativa + exportación
- [ ] Todos los tests pasan en CI/CD pipeline

**3. Documentación Actualizada:**
- [ ] `/docs/api-endpoints.md` actualizado con nuevos endpoints de comparativas y exportación
- [ ] `/docs/user-guide-filters-comparatives.md` creado con guía de usuario y capturas
- [ ] Código comentado en secciones complejas (algoritmos de agregación, generación de PDFs)
- [ ] README actualizado con instrucciones de instalación de Apache POI/Flying Saucer si aplica

**4. Sin Errores de Linting:**
- [ ] ESLint sin errores en código frontend
- [ ] TypeScript compilando sin errores
- [ ] Checkstyle/Spotless sin errores en código backend Java
- [ ] Código formateado según estándares del proyecto (Prettier para TS, Google Java Style)

**5. Validación de QA Completada:**
- [ ] Funcionalidad testeada manualmente en staging
- [ ] Los 4 criterios de aceptación verificados y documentados
- [ ] Performance validada: queries < 3 segundos en 95% de casos
- [ ] Exports (PDF/Excel) validados: descarga exitosa, formato correcto, metadatos incluidos
- [ ] UX/UI aprobado por Product Owner (Boris)
- [ ] Probado en Chrome, Firefox y Safari

---

### IOC-012: Filtrar Datos del Dashboard

**1. Código Completado y Revisado:**
- [ ] Componente `DashboardFilters.tsx` implementado con validación
- [ ] Hook `useFilters()` con lógica de persistencia en localStorage
- [ ] `FilterController` y `FilterService` implementados en backend
- [ ] Integración con `DashboardEmbed.tsx` funcionando (filtros pasan a Metabase)
- [ ] Pull Request creado y aprobado por 1+ reviewer (mínimo Boris revisa código de Jaime)
- [ ] Merge a `main` completado sin conflictos

**2. Tests Implementados y Pasando:**
- [ ] Tests unitarios para `FilterService` con verificación de caché (cobertura > 70%)
- [ ] Tests de integración para `FilterController` (endpoints GET de líneas/talleres)
- [ ] Test E2E con Playwright: aplicar filtros → verificar actualización → limpiar filtros
- [ ] Todos los tests pasan en CI/CD pipeline

**3. Documentación Actualizada:**
- [ ] `/docs/api-endpoints.md` actualizado con endpoints de filtros
- [ ] `/docs/user-guide-filters-comparatives.md` con sección de filtros y screenshots
- [ ] Código comentado en hook `useFilters()` (lógica de persistencia)
- [ ] README actualizado si hay nuevas dependencias

**4. Sin Errores de Linting:**
- [ ] ESLint sin errores en código frontend
- [ ] TypeScript compilando sin errores
- [ ] Checkstyle/Spotless sin errores en código backend Java
- [ ] Código formateado según estándares del proyecto

**5. Validación de QA Completada:**
- [ ] Funcionalidad testeada manualmente en staging
- [ ] Los 5 criterios de aceptación verificados (incluyendo autocompletado)
- [ ] Validación de fechas funcionando correctamente (mensajes de error claros)
- [ ] Persistencia de filtros validada (recargar página mantiene filtros)
- [ ] Mensaje "No hay datos" aparece correctamente cuando filtros no arrojan resultados
- [ ] UX/UI aprobado por Product Owner (Boris)
- [ ] Probado en Chrome, Firefox y Safari

---

## Calendario del Sprint (3 Semanas)

### Semana 1: Fundación y Spikes (6 - 10 Octubre)

**Lunes 6 Oct:**
- 🟢 Sprint Planning (2 horas): Refinamiento de historias, compromiso del equipo
- 🔴 **Spike IOC-012:** Jaime valida integración de filtros con Metabase JWT (4 horas)
- 🔴 **Spike IOC-008:** Boris valida generación de PDF con Flying Saucer (3 horas)
- Daily Standup (15 min)

**Martes 7 Oct:**
- Decisión Go/No-Go sobre spikes (9:00 AM)
- **Si spike IOC-012 OK:** Jaime inicia FE-TASK-18, FE-TASK-19
- **Si spike IOC-008 OK:** Boris inicia FE-TASK-24, FE-TASK-25
- Daily Standup (15 min)

**Miércoles 8 Oct:**
- Jaime: Continúa frontend de filtros (FE-TASK-20, FE-TASK-21)
- Boris: Continúa frontend de comparativas (FE-TASK-26, FE-TASK-27)
- Daily Standup (15 min)

**Jueves 9 Oct:**
- Jaime: Backend de filtros (BE-TASK-25, BE-TASK-26, BE-TASK-27)
- Boris: Backend de comparativas (BE-TASK-30, BE-TASK-31, BE-TASK-32)
- Daily Standup (15 min)

**Viernes 10 Oct:**
- Jaime: Finaliza integración frontend-backend IOC-012 (FE-TASK-22, FE-TASK-23)
- Boris: Backend de exportación (BE-TASK-33, BE-TASK-34, BE-TASK-35)
- Daily Standup (15 min)
- **Checkpoint:** ¿Ambas historias tienen funcionalidad básica E2E? Si no, re-priorizar

---

### Semana 2: Implementación Core (13 - 17 Octubre)

**Lunes 13 Oct:**
- Jaime: Validaciones y casos edge IOC-012 (BE-TASK-28, BE-TASK-29)
- Boris: Optimizaciones de performance IOC-008 (BE-TASK-36, BE-TASK-37, BE-TASK-38)
- Daily Standup (15 min)

**Martes 14 Oct:** ⬅️ **HOY ESTAMOS AQUÍ**
- Jaime: Comienza testing IOC-012 (TEST-TASK-01, TEST-TASK-02)
- Boris: Logging y métricas IOC-008 (BE-TASK-39)
- Daily Standup (15 min)

**Miércoles 15 Oct:**
- Jaime: Finaliza testing y crea PR para IOC-012 (TEST-TASK-03)
- Boris: Frontend final de IOC-008 (FE-TASK-28, FE-TASK-29, FE-TASK-30)
- Daily Standup (15 min)
- **Mid-Sprint Review:** Demo interna de IOC-012 (si está lista)

**Jueves 16 Oct:**
- Jaime: Code review del PR de Boris para IOC-008
- Boris: Testing IOC-008 (TEST-TASK-04, TEST-TASK-05, TEST-TASK-06)
- Daily Standup (15 min)

**Viernes 17 Oct:**
- Jaime: DevOps y documentación (OPS-TASK-01, DOC-TASK-01)
- Boris: Finaliza testing y crea PR para IOC-008
- Daily Standup (15 min)
- **Checkpoint:** ¿Ambas historias pasan tests? ¿PRs listos para merge?

---

### Semana 3: Testing, Refinamiento y Cierre (20 - 24 Octubre)

**Lunes 20 Oct:**
- Equipo: Merge de ambos PRs a `main`
- Equipo: Testing de regresión completo (verificar que nada se rompió)
- Daily Standup (15 min)

**Martes 21 Oct:**
- Equipo: Testing en staging, validación de criterios de aceptación
- Jaime: Ajustes finales en IOC-012 si se encuentran bugs
- Boris: Ajustes finales en IOC-008 si se encuentran bugs
- Daily Standup (15 min)

**Miércoles 22 Oct:**
- Equipo: Finaliza documentación (DOC-TASK-02, ajustes en DOC-TASK-01)
- Boris (PO): Valida que ambas historias cumplen DoD
- Daily Standup (15 min)

**Jueves 23 Oct:**
- Equipo: Preparación de Sprint Review (slides, demo environment)
- Equipo: Testing final en producción (smoke tests)
- Daily Standup (15 min)

**Viernes 24 Oct:**
- 🎉 **Sprint Review** (1.5 horas): Demo a stakeholders
- 🔄 **Sprint Retrospective** (1 hora): Qué funcionó, qué mejorar

---

**Sábado 25 Oct:**
- 📊 **Finalización del Sprint**
- Deployment final a producción si aún no se hizo
- Documentación de lecciones aprendidas

---

## Métricas y Objetivos del Sprint

### Objetivos de Performance

| Métrica | Objetivo | Medición |
|---------|----------|----------|
| **Tiempo de respuesta de filtros** | < 2 segundos en 95% de casos | Micrometer timer en `FilterService` |
| **Tiempo de respuesta de comparativas** | < 3 segundos en 95% de casos | Micrometer timer en `ComparativeAnalyticsService` |
| **Tiempo de generación de exports** | < 5 segundos para archivos < 1000 filas | Micrometer timer en `ReportExportService` |
| **Cobertura de tests** | > 70% en código nuevo | JaCoCo report |
| **Bugs en producción post-release** | 0 bugs críticos en primeros 7 días | Monitoreo post-sprint |

### Métricas de Calidad

| Aspecto | Objetivo | Validación |
|---------|----------|------------|
| **Code Review** | 100% del código revisado por par | GitHub PR reviews |
| **Linting** | 0 errores de ESLint/Checkstyle | CI/CD pipeline |
| **Documentación** | 100% de endpoints documentados | Revisión manual de `/docs` |
| **Accesibilidad** | Componentes de filtros navegables por teclado | Testing manual |
| **Compatibilidad** | Funciona en Chrome, Firefox, Safari | Testing cross-browser |

### Definición de Éxito del Sprint

El Sprint 2 será considerado exitoso si:

✅ **Ambas historias (IOC-008 y IOC-012) cumplen Definition of Done**  
✅ **Sprint Goal alcanzado:** Dashboard interactivo con filtros y comparativas funcionando  
✅ **Sin regresiones:** Funcionalidades del Sprint 1 siguen operando correctamente  
✅ **Performance dentro de objetivos:** Métricas de tiempo de respuesta cumplidas  
✅ **Stakeholders satisfechos:** Feedback positivo en Sprint Review

---

## Backlog Refinement para Sprint 3

Durante este sprint, el equipo debe refinar las historias del Sprint 3:

- **IOC-002:** Validar datos cargados automáticamente
- **IOC-003:** Gestionar Usuarios, Roles y Permisos
- **IOC-019:** Gestionar Gráficos del Dashboard
- **IOC-020:** Diseñar Disposición del Dashboard

**Actividad:** 1 sesión de refinement a mitad del sprint (Miércoles 15 Oct, 1 hora)

---

## Lecciones Aprendidas (A completar al final del Sprint)

### ✅ Qué Funcionó Bien

_[Se completará durante la Sprint Retrospective del 24 de Octubre]_

Ejemplos esperados:
- Spikes técnicos al inicio del sprint redujeron incertidumbre
- Desarrollo paralelo de ambas historias aceleró el progreso
- Code reviews tempranos detectaron problemas antes de QA

### 📝 Qué Mejorar

_[Se completará durante la Sprint Retrospective del 24 de Octubre]_

Ejemplos esperados:
- Necesitamos más tiempo de testing manual antes del merge
- La integración con Metabase requiere mejor documentación
- Considerar pair programming para tareas complejas de backend

### 💡 Ideas para Próximos Sprints

_[Se completará durante la Sprint Retrospective del 24 de Octubre]_

Ejemplos esperados:
- Automatizar más tests E2E para reducir testing manual
- Crear biblioteca de componentes reutilizables para filtros
- Implementar feature flags para releases más seguras

---

## Apéndices

### A. Stack Tecnológico del Sprint

**Frontend:**
- React 18 + TypeScript
- React Hook Form + Zod (validación de formularios)
- Headless UI (componentes de UI accesibles)
- Recharts o Chart.js (gráficos de comparativas)
- Playwright (testing E2E)
- Axios (HTTP client)

**Backend:**
- Spring Boot 3.x
- Spring Security (autenticación/autorización)
- Spring Data JPA (acceso a datos)
- PostgreSQL (base de datos)
- Apache POI 5.2.3 (generación de Excel)
- Flying Saucer 9.1.22 (generación de PDF)
- Resilience4j (circuit breaker)
- Caffeine (caché)
- Micrometer (métricas)

**DevOps:**
- GitHub Actions (CI/CD)
- Docker (contenedores)
- Vercel (frontend deployment)
- Railway/Render (backend deployment)

### B. Endpoints del Sprint

**Filtros (IOC-012):**
```
GET /api/v1/filters/lines
GET /api/v1/filters/workshops
POST /api/v1/dashboards/{dashboardId} (modificado para aceptar filtros)
```

**Comparativas (IOC-008):**
```
POST /api/v1/analytics/compare
POST /api/v1/analytics/export
```

### C. Modelo de Datos Relevante

**Tablas Utilizadas:**
- `fact_production`: Tabla principal de hechos (producción diaria)
- `dim_line`: Dimensión de líneas de producción
- `dim_shift`: Dimensión de turnos
- `dim_workshop`: Dimensión de talleres
- `dim_date`: Dimensión de fechas

**Índices Nuevos (BE-TASK-37):**
```sql
CREATE INDEX idx_fact_production_filters 
ON fact_production(date, line_id, shift_id);

CREATE INDEX idx_fact_production_comparatives
ON fact_production(line_id, shift_id, date);
```

### D. Contactos y Escalación

**Product Owner:** Boris Arriagada - boris@example.com  
**Scrum Master:** Jaime Vicencio - jaime@example.com  
**Tech Lead:** Boris Arriagada  
**Stakeholders:** Gerentes de Producción, Supervisores de Línea

**Escalación de Impedimentos:**
1. Daily Standup → Scrum Master
2. Si no se resuelve en 24h → Product Owner
3. Si bloquea Sprint Goal → Reunión de emergencia con stakeholders

---

**Documento Creado:** 14 de Octubre, 2025  
**Última Actualización:** 14 de Octubre, 2025  
**Versión:** 1.0  
**Estado:** 📋 En Progreso (Día 6 de 15)

---

## Firma de Compromiso del Sprint

**Equipo de Desarrollo:**
- [ ] Boris Arriagada - Development Team
- [ ] Jaime Vicencio - Development Team

**Acuerdo:**
El equipo se compromete a entregar las 2 historias (21 SP) cumpliendo el Definition of Done y alcanzando el Sprint Goal de transformar el dashboard en una herramienta interactiva de análisis.

**Fecha de Compromiso:** 6 de Octubre, 2025  
**Sprint Review Programada:** 24 de Octubre, 2025 - 15:00 hrs

