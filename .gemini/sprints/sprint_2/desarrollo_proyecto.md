# DESARROLLO DEL PROYECTO APT - FASE 2

## Plataforma de Inteligencia Operacional Cambiaso (IOC)

**Proyecto:** Sistema de Business Intelligence con Metabase + Gestión de Datos  
**Institución:** AIEP  
**Programa:** Analista Programador de Aplicaciones de Gestión (APT)  
**Fase:** Fase 2 - Desarrollo del Proyecto  
**Período:** Septiembre - Octubre 2025  
**Equipo:**

- **Product Owner y Desarrollador:** Boris Arriagada
- **Scrum Master y Desarrollador:** Jaime Vicencio

---

## 1. RESUMEN AVANCE PROYECTO APT

### 1.1 Resumen de Avance del Proyecto

El proyecto **Inteligencia Operacional Cambiaso (IOC)** ha completado exitosamente el **Sprint 1** (8 septiembre - 4 octubre 2025) alcanzando un **100% de cumplimiento** de los objetivos comprometidos. El sprint tuvo una duración de 4 semanas calendáricas con 19 días hábiles de trabajo (excluyendo los festivos 18 y 19 de septiembre). Se implementó el ciclo de valor completo de extremo a extremo, permitiendo que un usuario pueda autenticarse en la plataforma, cargar datos de producción mediante archivos CSV y visualizar inmediatamente dashboards interactivos con KPIs actualizados.

#### Actividades Realizadas y Objetivos Cumplidos

**1. Implementación del Sistema de Autenticación y Autorización (IOC-021, IOC-022, IOC-023)**

Se desarrolló e implementó un sistema robusto de autenticación utilizando **Supabase Auth** integrado con **Spring Security 6** y tokens JWT. Las actividades incluyeron:

- **Frontend (React + TypeScript):** Creación de componentes de autenticación (SignIn, SignUp) con validación de formularios usando React Hook Form y Zod. Integración completa con Supabase Auth incluyendo manejo de estados de sesión, persistencia de tokens y redirección condicional por roles.

- **Backend (Spring Boot 3.5.5):** Configuración de Spring Security con filtros personalizados (`JwtRequestFilter`) para validar tokens en cada petición. Implementación de `AuthController` y `AuthService` para gestionar el login y generación de JWT. Configuración de CORS para permitir comunicación segura entre frontend y backend.

- **Sistema de Rutas Protegidas:** Desarrollo del componente `ProtectedRoute` que valida la sesión del usuario antes de permitir acceso a vistas administrativas. Implementación de `AppLayout` con navegación dinámica según el rol del usuario (Administrador vs Gerente).

- **Cierre de Sesión Seguro:** Lógica robusta de `signOut` con invalidación completa de sesión, limpieza de tokens y prevención de acceso mediante historial del navegador.

**Cómo se cumplió:** Se siguió una arquitectura de autenticación moderna con separación de responsabilidades. El frontend maneja la experiencia de usuario y la comunicación con Supabase, mientras el backend valida independientemente los tokens JWT para asegurar que cada petición a la API esté autorizada. Se implementaron 6 tareas técnicas (FE-TASK-01 a FE-TASK-06, BE-TASK-01 a BE-TASK-03) con pruebas exhaustivas de flujos de autenticación exitosos y fallidos.

**2. Pipeline ETL Completo para Ingesta y Validación de Datos (IOC-001)**

Se construyó un pipeline ETL (Extract, Transform, Load) completamente funcional y robusto que permite a los administradores cargar archivos CSV de producción con validación automática, procesamiento asíncrono y monitoreo de estado en tiempo real. Las actividades incluyeron:

- **Arquitectura del Pipeline:** Diseño e implementación de 4 servicios especializados:
  
  - `EtlJobService`: Gobernanza de jobs con gestión de estados y prevención de duplicados
  - `EtlProcessingService`: Procesamiento asíncrono con `@Async` para evitar bloqueos
  - `ParserService`: Validación y parseo de CSV con de-duplicación intra-archivo
  - `DataSyncService`: Persistencia transaccional con manejo de errores y rollback automático

- **Manejo de Concurrencia:** Implementación de **Advisory Locks** de PostgreSQL para serializar ejecuciones concurrentes de ETL con rangos de fecha superpuestos, previniendo colisiones de datos.

- **Sistema de Reintentos:** Mecanismo de reintentos con backoff exponencial (3 intentos con delays de 1s, 2s, 4s) para absorber colisiones transitorias de restricciones UNIQUE en la base de datos.

- **Monitoreo y Resiliencia:**
  
  - `EtlJobWatchdog`: Job programado (@Scheduled) que detecta automáticamente jobs en estados intermedios por más de 1 hora y los marca como "FALLO"
  - Instrumentación completa con **Micrometer**: contadores de registros procesados, temporizadores de duración de jobs, gauges de memoria utilizada
  - HealthIndicator personalizado (`/actuator/health/etl`) que expone el estado de integridad del sistema ETL

- **Optimización de Rendimiento:** Resolución del impedimento crítico IMP-009 donde el procesamiento inicial tomaba 4+ minutos para 17,000 registros. Mediante la implementación de:
  
  - Caché de dimensiones (líneas, turnos, máquinas) para evitar queries repetitivas
  - JDBC Batching para inserción masiva en lotes de 100 registros
  - **Resultado:** Reducción de 4 minutos a menos de 30 segundos (mejora del 87.5%)

- **Integridad de Datos:** Fortalecimiento del esquema de base de datos con índice UNIQUE compuesto sobre la clave de negocio natural (fecha, línea, turno, máquina) y verificadores de arranque (@PostConstruct) que validan la ausencia de duplicados al iniciar la aplicación.

- **Frontend Interactivo:** Página de Ingesta de Datos (`DataIngestionPage.tsx`) con:
  
  - Drag & drop para carga de archivos
  - Sistema de sondeo (polling cada 2 segundos) para monitorear progreso en tiempo real
  - Tabla histórica con estados visuales (Pending, Processing, Completed, Failed)
  - Manejo de errores con mensajes específicos y sugerencias de corrección

**Cómo se cumplió:** Se implementaron 29 tareas técnicas (FE-TASK-07 a FE-TASK-15, BE-TASK-04 a BE-TASK-16) con un enfoque iterativo. Se identificaron y resolvieron 4 impedimentos críticos durante el desarrollo (IMP-008: clave compuesta, IMP-009: rendimiento ETL). Se aplicaron patrones de diseño empresariales como Command Pattern para jobs, Strategy Pattern para parsing, y Transaction Script para sincronización de datos. Todas las funcionalidades fueron validadas contra los 4 criterios de aceptación definidos en la historia de usuario.

**3. Integración Completa con Metabase para Visualización de Dashboards (IOC-006)**

Se logró la integración exitosa de **Metabase Open Source** embebido en el frontend de React, permitiendo a los gerentes visualizar dashboards interactivos con KPIs actualizados en tiempo real. Las actividades incluyeron:

- **Backend - Servicio de Embedding:** Implementación de `MetabaseEmbeddingService` que:
  
  - Genera tokens JWT firmados con la secret key de Metabase
  - Valida autorización por roles (solo usuarios con permisos pueden acceder a dashboards específicos)
  - Construye URLs firmadas con parámetros de embedding
  - Registra auditoría de todos los accesos mediante `DashboardAuditService`

- **API RESTful Segura:** Creación de `DashboardController` con endpoint `GET /api/v1/dashboards/{dashboardId}` protegido por Spring Security, que retorna URLs firmadas válidas por 10 minutos.

- **Configuración de Seguridad:** Ajuste de `SecurityConfig` para modificar la Política de Seguridad de Contenido (CSP) y permitir que iframes de Metabase se rendericen correctamente sin violaciones de seguridad.

- **Conversión de Autenticación:** Implementación de `JwtAuthenticationConverter` que convierte el JWT de Supabase en el objeto `CustomUserDetails` utilizado por Spring Security, habilitando filtrado por atributos de usuario.

- **Resiliencia y Performance:**
  
  - **Circuit Breaker con Resilience4j:** Configuración de circuit breaker para manejar caídas del servidor de Metabase (50% threshold de errores, 30 llamadas mínimas)
  - **Caché con Caffeine:** Implementación de caché de tokens con TTL de 5 minutos y máximo 100 entradas, reduciendo la carga en el servicio de embedding

- **Frontend - Componente de Embedding:** Desarrollo de `DashboardEmbed.tsx` que:
  
  - Solicita URL firmada al backend mediante axios
  - Renderiza iframe con el dashboard de Metabase
  - Maneja estados de carga, error y éxito con feedback visual
  - Implementa auto-refresh controlado para mantener datos actualizados

**Cómo se cumplió:** Se implementaron 8 tareas técnicas del backend (BE-TASK-17 a BE-TASK-24) siguiendo la documentación oficial de Metabase para JWT embedding. Se resolvieron 4 impedimentos técnicos críticos durante la integración:

- IMP-011: ECONNREFUSED entre frontend y backend (resuelto ajustando proxy de Vite)
- IMP-012: CORS bloqueando peticiones (resuelto configurando allowedOrigins)
- IMP-013: Bucle de renderizado infinito en dashboards (resuelto con useCallback)
- IMP-014: Tokens expirados causando 403 (resuelto con refresh automático)

Todas las funcionalidades fueron validadas exhaustivamente contra los 4 criterios de aceptación, incluyendo pruebas con datos reales cargados mediante el pipeline ETL.

**4. Localización y Rebranding Completo del Sistema**

Se realizó la localización completa de la interfaz de usuario al español y se aplicó el rebranding corporativo "Cambiaso" en toda la plataforma (FE-TASK-17), incluyendo:

- Traducción de todos los componentes, mensajes de error y notificaciones
- Actualización de logos, colores corporativos y tipografía
- Adaptación de formatos de fecha y número al estándar chileno

#### Logros Cuantitativos del Sprint 1

| Métrica                               | Resultado               |
| ------------------------------------- | ----------------------- |
| **Historias de Usuario Completadas**  | 5/5 (100%)              |
| **Story Points Entregados**           | 41/41 (100%)            |
| **Tareas Técnicas Implementadas**     | 40/40 (100%)            |
| **Criterios de Aceptación Cumplidos** | 18/18 (100%)            |
| **Impedimentos Identificados**        | 16                      |
| **Impedimentos Resueltos**            | 16/16 (100%)            |
| **Cobertura de Tests Unitarios**      | >70% (Backend)          |
| **Riesgos Mitigados**                 | 3 (R-002, R-004, R-008) |

#### Estado Actual: Sprint 2 en Progreso (Semana 2 de 3)

El equipo se encuentra actualmente en el **Sprint 2** (6 octubre - 24 octubre 2025) con el objetivo de "Desatar la Interactividad Analítica". Este sprint busca transformar los dashboards estáticos en herramientas de análisis interactivas mediante:

- **IOC-012:** Filtrado dinámico de datos por línea, período y taller
- **IOC-008:** Comparación de desempeño entre turnos y líneas con exportación en PDF/Excel

**Progreso Sprint 2 (al 14 octubre):**

- Historias completadas: 0/2 (0%)
- Story Points entregados: 5/21 (24%)
- Tareas técnicas de Semana 1 completadas: 17/17 (100%)
- Impedimentos resueltos: 5/7 (71%)
- Impedimentos activos: 2 (IMP-S2-006: Tests sin datos seed, IMP-S2-007: Rate limiting Supabase)

**Actividades realizadas en Sprint 2 (Semana 1):**

- Completados 2 spikes técnicos para validar integración de filtros con Metabase JWT y generación de PDFs con Flying Saucer
- Implementados controladores y servicios base para filtrado (`FilterController`, `FilterService` con caché Caffeine)
- Implementados controladores y servicios base para análisis comparativo (`AnalyticsController`, `ComparativeAnalyticsService`)
- Resuelto impedimento crítico IMP-S2-004: Query N+1 optimizado de 12 segundos a <2 segundos (mejora del 600%)

### 1.2 Ajustes a Objetivos

**No se han realizado ajustes a los objetivos originales del proyecto.** Los objetivos específicos definidos en la Fase 1 se mantienen vigentes y están siendo cumplidos sistemáticamente:

**Objetivos Específicos del Proyecto (Mantenidos):**

1. **OE-1: Automatizar la Ingesta de Datos de Producción** ✅ CUMPLIDO (Sprint 1)
   
   - Implementado pipeline ETL completo con validación automática
   - Procesamiento asíncrono de archivos CSV con monitoreo en tiempo real
   - Validaciones robustas y manejo de duplicados

2. **OE-2: Centralizar la Información en una Base de Datos Relacional** ✅ CUMPLIDO (Sprint 1)
   
   - Modelo de datos estrella implementado en PostgreSQL (Supabase)
   - Tablas dimensionales: dim_line, dim_shift, dim_machine, dim_date
   - Tabla de hechos: fact_production con métricas de producción
   - Índices optimizados y restricciones de integridad

3. **OE-3: Proveer Dashboards Interactivos con KPIs en Tiempo Real** ✅ CUMPLIDO (Sprint 1) + 🔄 EN MEJORA (Sprint 2)
   
   - Integración completa con Metabase embebido
   - Dashboards con KPIs de producción, eficiencia y defectos
   - Sprint 2 agregará filtros interactivos y análisis comparativo

4. **OE-4: Garantizar Seguridad y Control de Acceso por Roles** ✅ CUMPLIDO (Sprint 1)
   
   - Autenticación con Supabase Auth + validación JWT en backend
   - Rutas protegidas con control de acceso por rol
   - Auditoría de accesos a dashboards

5. **OE-5: Optimizar la Agilidad en la Toma de Decisiones Operativas** 🔄 EN PROGRESO
   
   - Sprint 1 habilitó acceso inmediato a datos actualizados
   - Sprint 2 agregará capacidades de análisis comparativo para identificar patrones
   - Sprint 3 completará con gestión avanzada de usuarios y permisos

**Justificación:** El alcance del proyecto fue definido con precisión en la Fase 1 utilizando metodología Scrum. Los Product Backlog Items fueron refinados exhaustivamente con el cliente, y el equipo ha demostrado una velocidad estable que permite cumplir los compromisos de cada sprint sin necesidad de ajustar objetivos. La planificación conservadora con buffers del 30% ha permitido absorber imprevistos sin desviar el rumbo del proyecto.

### 1.3 Ajustes a Metodología

**Se realizó un ajuste menor en la metodología de trabajo durante el Sprint 2** en respuesta a lecciones aprendidas del Sprint 1:

#### Ajuste Implementado: Incorporación de Spikes Técnicos

**Contexto:** Durante el Sprint 1, se encontraron impedimentos técnicos (IMP-008, IMP-009) que requirieron investigación y experimentación significativas durante el tiempo de desarrollo, afectando la predictibilidad de la velocidad.

**Ajuste:** A partir del Sprint 2, se incorporaron **Spikes Técnicos** como actividades explícitas dentro del Sprint Backlog:

- **SPIKE-01:** Validar integración de filtros con Metabase JWT (2 días)
- **SPIKE-02:** Validar generación de PDFs con Flying Saucer (2 días)

**Beneficios del ajuste:**

- Reducción de incertidumbre técnica antes de comprometer Story Points
- Mejor visibilidad de riesgos técnicos en el Sprint Planning
- Decisiones Go/No-Go informadas sobre viabilidad de implementación
- Estimaciones más precisas de tareas de desarrollo

**Impacto:** Los spikes consumieron 2 Story Points del buffer del Sprint 2 (30% reservado = 9 SP), permitiendo validar ambas tecnologías críticas en los primeros 2 días del sprint sin afectar los compromisos del Sprint Goal.

#### Metodología Scrum Core (Mantenida sin cambios)

Todos los elementos fundamentales de Scrum se mantienen según lo planificado:

- **Sprints de duración fija:** Sprint 1 (4 semanas), Sprint 2 (3 semanas), Sprint 3 (4 semanas)
- **Eventos Scrum:** Sprint Planning, Daily Scrum (diario), Sprint Review, Sprint Retrospective
- **Roles:** Product Owner (Boris), Scrum Master (Jaime), Development Team (ambos)
- **Artefactos:** Product Backlog, Sprint Backlog, Increment
- **Definition of Ready y Definition of Done:** Aplicados estrictamente en todas las historias
- **Límites WIP:** 3 tareas simultáneas por persona, 6 items en "In Progress"

**Métricas de adherencia:**

- Daily Scrums realizados: 19/19 en Sprint 1 (100%)
- Sprint Review exitoso: Sprint 1 completado con demostración al PO el 3 de octubre
- Sprint Retrospective documentada con lecciones aprendidas y acciones de mejora

**Impacto del ajuste:** La incorporación de spikes técnicos fue una mejora bien recibida por el equipo, permitiendo abordar incertidumbres técnicas de manera más efectiva. No se reportaron impactos negativos en la entrega de valor.

### 1.4 Evidencias de Avance

Las evidencias del desarrollo del proyecto están organizadas jerárquicamente en la carpeta `/evidencias_a_entregar` según se detalla en el documento `evidencias.md`. A continuación se describen las evidencias clave que demuestran el avance del proyecto:

#### A. Product Backlog y Planificación del Proyecto

**Archivo:** `02.-product_backlog.md` (ubicación: `.gemini/evidencias/PRODUCT_BACKLOG.md`)

**Descripción:** Documento maestro que contiene la visión completa del proyecto, incluyendo:

- 3 épicas principales del proyecto (Ingesta, Visualización, Seguridad)
- 15+ historias de usuario priorizadas con formato "Como [rol], quiero [acción] para [beneficio]"
- Criterios de aceptación detallados en formato Given-When-Then
- Estimación en Story Points mediante Planning Poker
- Definition of Ready y Definition of Done aplicados consistentemente
- Configuración del tablero Kanban con WIP limits

**Cómo demuestra el desarrollo:** Este documento evidencia la aplicación rigurosa de metodología Scrum con planificación detallada y orientada al valor de negocio. Cada historia incluye criterios de aceptación medibles que fueron utilizados como checklist durante el desarrollo y testing.

**Resguardo de calidad:** El Product Backlog fue refinado en sesiones conjuntas con el stakeholder (representado por el PO) y revisado en cada Sprint Planning para asegurar que las historias cumplieran con el DoR antes de ser comprometidas.

#### B. Sprint 1 Backlog y Seguimiento Detallado

**Archivo:** `01.-Sprint-1-Backlog.md` (ubicación: `.gemini/evidencias/Sprint-1-Backlog.md`)

**Descripción:** Backlog específico del Sprint 1 que incluye:

- 5 historias comprometidas (IOC-021, IOC-022, IOC-023, IOC-001, IOC-006) con 41 Story Points
- Desglose de 40 tareas técnicas granulares con responsables asignados
- Estado de completitud de cada tarea (100% completadas)
- Registro de dependencias técnicas identificadas (Metabase, Supabase Auth, Backend API, PostgreSQL)
- Catálogo de 16 impedimentos con descripción, impacto y resolución documentada
- Análisis de riesgos con probabilidad, impacto y estrategias de mitigación
- Lecciones aprendidas del sprint

**Cómo demuestra el desarrollo:** Este documento es la evidencia más detallada del trabajo técnico realizado. Cada tarea incluye su estado de completitud, y los impedimentos documentados muestran la capacidad del equipo para resolver problemas complejos de forma sistemática.

**Resguardo de calidad:** 

- Todas las tareas fueron revisadas en code review antes del merge
- Los impedimentos críticos (IMP-008, IMP-009) fueron escalados y resueltos con refactorización profunda
- Se implementaron tests unitarios con cobertura >70%
- Se aplicó el DoD estrictamente: código revisado, tests pasando, sin errores de linting, documentación actualizada

#### C. Daily Scrum Summaries - Sprint 1 y Sprint 2

**Archivos:** 

- `03.-daily_scrum_summary_sprint_1.md` (ubicación: `.gemini/sprints/sprint_1/DAILY_SCRUM_SUMMARY_SPRINT_1.md`)
- `03.-daily_scrum_summary_sprint_2.md` (ubicación: `.gemini/sprints/sprint_2/DAILY_SCRUM_SUMMARY_SPRINT_2.md`)

**Descripción:** Registro completo de todas las reuniones Daily Scrum realizadas durante los sprints, siguiendo el formato estándar de Scrum:

- **Round Robin:** Cada miembro responde "Qué hice ayer", "Qué haré hoy", "Tengo algún blocker"
- **Progreso del Sprint:** Historias completadas y Story Points acumulados
- **Blockers identificados:** Impedimentos detectados con ID único (IMP-XXX)
- **Parking Lot:** Temas a discutir fuera de la daily
- **Fecha de siguiente daily**

**Cómo demuestra el desarrollo:** Estas evidencias muestran la transparencia y disciplina del equipo en la ejecución de Scrum. Se realizaron 17 dailies durante el Sprint 1 sin faltas, y cada impedimento fue rastreado hasta su resolución. Los dailies del Sprint 2 muestran que el equipo está actualmente en la Semana 2 con buen ritmo de progreso.

**Resguardo de calidad:** Los dailies permitieron detectar impedimentos tempranamente:

- IMP-001 (Error 404 en /update-password) detectado día 4, resuelto día 5
- IMP-009 (ETL con rendimiento inaceptable) detectado día 11, resuelto día 12 con optimización completa
- Visibilidad diaria del progreso facilitó ajustes ágiles de prioridades

#### D. Gráficos Burndown de Sprints

**Archivos:**

- `05.-sprint1_burndown_REAL.png` (Sprint 1)
- `05.-sprint2_grafico_burndown.png` (Sprint 2)
- `05.-burndown_chart.ipynb` (Script Python para generación automatizada)

**Descripción:** Gráficos de burndown que muestran visualmente el progreso del sprint:

- Eje X: Días del sprint
- Eje Y: Story Points restantes
- Línea ideal (tendencia lineal) vs línea real (progreso del equipo)
- Sprint 1 completó los 41 SP comprometidos
- Sprint 2 muestra progreso de 5/21 SP al día 14

**Cómo demuestra el desarrollo:** Los burndowns son evidencia cuantitativa del ritmo de entrega. El Sprint 1 muestra una curva real que inicialmente está por debajo de la ideal (inicio lento por setup técnico) y luego se acelera en la segunda mitad (resolución de impedimentos y desarrollo fluido).

**Resguardo de calidad:** El burndown es generado mediante un notebook de Jupyter que lee datos reales del Sprint Backlog, eliminando manipulación manual y asegurando trazabilidad.

#### E. Impediment Log

**Archivos:**

- `04.-impediment_log_sprint_1.md`
- `04.-impediment_log_sprint_2.md`

**Descripción:** Registro estructurado de todos los impedimentos encontrados durante los sprints, incluyendo:

- ID único del impedimento
- Descripción del problema
- Impacto en el sprint (crítico, alto, medio, bajo)
- Responsable de la resolución
- Estado (activo, resuelto, mitigado)
- Fecha de detección y resolución
- Solución implementada

**Cómo demuestra el desarrollo:** Este log evidencia la madurez del equipo en la gestión de riesgos. Los 16 impedimentos del Sprint 1 fueron resueltos al 100%, con tiempos de resolución de 1-2 días para impedimentos críticos.

**Resguardo de calidad:** Los impedimentos críticos desencadenaron acciones de mejora documentadas:

- IMP-009 (rendimiento ETL) → Refactorización completa con caché y batching JDBC
- IMP-011 (ECONNREFUSED) → Ajuste de configuración de proxy Vite documentado para futuros desarrolladores

#### F. Tablero Kanban y Gestión Visual

**Archivos:**

- `02.-kanban` - Link al tablero GitHub Projects: https://github.com/users/qb4745/projects/6 (Sprint 1)
- `02.-kanban` - Link al tablero GitHub Projects: https://github.com/users/qb4745/projects/7 (Sprint 2)
- `02.-tablero_kanban.png` (Screenshots de los tableros)

**Descripción:** Tablero Kanban digital configurado en GitHub Projects con:

- Columnas: Backlog, Ready, In Progress (WIP limit 6), Done
- Tarjetas con historias de usuario y tareas técnicas
- Etiquetas de prioridad (Crítica, Alta, Media, Baja)
- Asignación visual de responsables
- Filtros por sprint, tipo de tarea (frontend/backend), y estado

**Cómo demuestra el desarrollo:** El tablero Kanban es la herramienta de gestión en tiempo real del equipo. Las capturas de pantalla muestran el flujo de trabajo: todas las tarjetas del Sprint 1 están en la columna "Done", evidenciando el cumplimiento del 100%.

**Resguardo de calidad:** El WIP limit de 6 items en "In Progress" previene sobrecarga del equipo y asegura foco en completar tareas antes de iniciar nuevas. El tablero fue revisado diariamente en las Daily Scrums.

#### G. Retrospectiva del Sprint 1

**Archivo:** `06.-sprint_1_retrospective.md`

**Descripción:** Documento de retrospectiva del Sprint 1 siguiendo el formato "What went well, What didn't go well, Action items":

- Éxitos: Implementación exitosa de Circuit Breaker, procesamiento asíncrono, Advisory Locks
- Áreas de mejora: Necesidad de spikes técnicos, mejor gestión de dependencias externas
- Acciones: Incorporar spikes en Sprint 2, evaluar BD local Docker para evitar rate limiting

**Cómo demuestra el desarrollo:** La retrospectiva muestra la capacidad del equipo para reflexionar y mejorar continuamente. Las acciones identificadas fueron implementadas en Sprint 2 (spikes técnicos agregados al backlog).

**Resguardo de calidad:** La retrospectiva es un mecanismo formal de mejora continua. Las lecciones aprendidas documentadas se convierten en estándares para sprints futuros.

#### H. Demo del Sprint 1

**Archivo:** `07.-sprint_1_demo.md`

**Descripción:** Guión y resultados de la Sprint Review (demostración) del Sprint 1, incluyendo:

- Agenda de la demo
- Historias demostradas en vivo
- Feedback del Product Owner
- Métricas de completitud presentadas
- Próximos pasos

**Cómo demuestra el desarrollo:** La demo es la validación final de que el incremento de software funciona y cumple con los criterios de aceptación. El PO aprobó todas las historias demostradas.

**Resguardo de calidad:** Cada historia fue demostrada con datos reales:

- Autenticación con usuarios reales de Supabase
- Carga de archivo CSV real de producción (17k+ registros)
- Dashboard de Metabase mostrando KPIs derivados de los datos cargados

#### I. Esquema de Base de Datos y ERD

**Archivos:**

- `3.-ERD-2025-10-14-022739.png` (Diagrama Entidad-Relación actualizado)
- `3.-schema-actual.sql` (Script SQL del esquema completo)
- `3.-supabase-schema-bdyvzjpkycnekjrlqlfp.png` (Screenshot del esquema en Supabase)

**Descripción:** Modelo de datos en tercera forma normal (3FN) con arquitectura de estrella (star schema) para análisis BI:

- Tablas dimensionales: dim_line, dim_shift, dim_machine, dim_date
- Tabla de hechos: fact_production (métricas de producción por día/línea/turno/máquina)
- Tabla de control: etl_job (seguimiento de jobs ETL)
- Índices optimizados: UNIQUE en clave de negocio, índices en foreign keys

**Cómo demuestra el desarrollo:** El esquema de base de datos es la columna vertebral del sistema. Su diseño refleja análisis profundo de los requisitos de negocio y optimización para consultas analíticas.

**Resguardo de calidad:** 

- El esquema fue revisado y aprobado por el stakeholder en la Fase 1
- Índices UNIQUE previenen duplicados a nivel de base de datos
- Foreign keys con ON DELETE CASCADE aseguran integridad referencial
- Verificadores @PostConstruct validan integridad al arrancar la aplicación

#### J. Tests Automatizados

**Archivo:** `2.-tests.zip`

**Descripción:** Suite completa de tests automatizados:

- **Tests Unitarios (JUnit 5 + Mockito):** Cobertura >70% en servicios críticos (EtlJobService, ParserService, DataSyncService, MetabaseEmbeddingService)
- **Tests de Integración:** Validación de flujos completos con base de datos H2 in-memory
- **Tests E2E (Playwright - planificados Sprint 2):** Flujos de usuario completos

**Cómo demuestra el desarrollo:** Los tests son evidencia de ingeniería de software profesional. La cobertura >70% asegura que la lógica de negocio crítica está validada automáticamente.

**Resguardo de calidad:**

- Tests ejecutados en pipeline CI/CD (GitHub Actions) en cada push
- Tests de integración validan transacciones y rollback en escenarios de error
- Estrategia de testing definida: Unitarios para lógica, Integración para persistencia, E2E para UX

#### K. Registro de Riesgos

**Archivo:** `04.-risk_register.md`

**Descripción:** Registro de riesgos del proyecto con:

- ID único del riesgo
- Descripción y contexto
- Probabilidad (Muy Baja, Baja, Media, Alta)
- Impacto (Bajo, Medio, Alto, Crítico)
- Estado (Mitigado, Monitoreado, Aceptado)
- Estrategia de mitigación implementada
- Plan de contingencia

**Cómo demuestra el desarrollo:** El registro de riesgos muestra gestión proactiva de incertidumbre. Los 3 riesgos críticos identificados (R-002: Integración Metabase, R-004: Concurrencia ETL, R-008: Procesamiento asíncrono) fueron completamente mitigados durante el Sprint 1.

**Resguardo de calidad:** Cada riesgo crítico desencadenó implementación de controles técnicos:

- R-002 → Circuit Breaker + caché
- R-004 → Advisory Locks + índices UNIQUE
- R-008 → @Async + EtlJobWatchdog + polling en frontend

#### L. Cronograma del Proyecto

**Archivo:** `03.-cronograma.png`

**Descripción:** Diagrama de Gantt o cronograma visual que muestra:

- 3 sprints del proyecto con fechas de inicio y fin
- Hitos principales (Sprint Reviews, entregables)
- Días no laborables (18-19 septiembre)
- Estado actual (Sprint 2 Semana 2)

**Cómo demuestra el desarrollo:** El cronograma evidencia cumplimiento de plazos. El Sprint 1 se completó exactamente en la fecha comprometida (4 octubre 2025).

**Resguardo de calidad:** La planificación incluye buffers del 30% para absorber imprevistos sin afectar las fechas de entrega comprometidas con el cliente.

---

## Conclusión de la Sección

El proyecto IOC ha demostrado un avance sólido y medible durante la Fase 2, con un **Sprint 1 completado al 100%** y un **Sprint 2 en progreso con buen ritmo** (24% completado en la primera mitad del sprint). La metodología Scrum ha sido aplicada con disciplina, evidenciada por artefactos completos (Product Backlog, Sprint Backlogs, Burndowns, Dailies, Retrospectivas) y métricas cuantitativas de cumplimiento.

La calidad del desarrollo está respaldada por:

- Cobertura de tests >70%
- Resolución del 100% de impedimentos
- Mitigación de riesgos críticos
- Code reviews y DoD aplicado estrictamente
- Instrumentación con métricas (Micrometer, Actuator)

El equipo ha demostrado capacidad para adaptarse (incorporación de spikes técnicos en Sprint 2) y resolver problemas complejos (optimización ETL, integración Metabase JWT, manejo de concurrencia), manteniendo siempre el foco en entregar valor de negocio medible al stakeholder.
