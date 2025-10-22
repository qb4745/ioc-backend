# Sprint 1 – Sprint Backlog (El Ciclo de Valor Completo)

## Período del Sprint
- **Fechas:** 8 Septiembre 2025 – 3 Octubre 2025
- **Duración:** 4 semanas (excluyendo festivos 18-19 Septiembre)
- **Días hábiles:** 18 días

## Objetivo del Sprint
Entregar un ciclo de valor de extremo a extremo: un usuario podrá autenticarse, cargar datos de producción a través de un archivo CSV y visualizar inmediatamente un dashboard con KPIs y gráficos que reflejen esa nueva información.

## Historias Comprometidas
| ID | Título | Tipo | Feature | Prioridad | SP | Asignado | Estado |
| :--- | :--- | :--- | :--- | :--- | :-: | :--- | :--- |
| IOC-021 | Como usuario, quiero iniciar sesión en la plataforma para acceder a las funcionalidades que corresponden a mi rol. | Historia de Usuario | Autenticación | Crítica | 5 | Jaime | ✅ Terminada |
| IOC-022 | Como usuario, quiero cerrar sesión de forma segura para proteger mi cuenta cuando termino de usar la plataforma. | Historia de Usuario | Autenticación | Crítica | 2 | Jaime | ✅ Terminada |
| IOC-023 | Como usuario, quiero navegar entre las secciones principales de la aplicación para acceder fácilmente a las diferentes funcionalidades. | Historia de Usuario | Infraestructura Frontend | Crítica | 8 | Jaime | ✅ Terminada |
| IOC-001 | Como administrador, quiero cargar y validar archivos de producción para asegurar que solo datos fiables y de alta calidad se carguen en la base de datos. | Historia de Usuario | Ingesta de Datos | Crítica | 13 | Boris | ✅ Terminada |
| IOC-006 | Como gerente, quiero visualizar un dashboard con KPIs actualizados para tomar decisiones informadas sobre la operación de manera rápida y eficiente. | Historia de Usuario | Visualización de Datos | Crítica | 13 | Boris | ✅ Terminada |

### Criterios de Aceptación

**IOC-021: Como usuario, quiero iniciar sesión en la plataforma para acceder a las funcionalidades que corresponden a mi rol.**
- ✅ Login exitoso: Al ingresar credenciales correctas, el sistema inicia sesión y redirige al dashboard
- ✅ Login fallido: Al ingresar credenciales incorrectas, el sistema muestra un mensaje de error y no permite el acceso
- ✅ Redirección post-login (opcional): Un administrador puede ser redirigido a una vista de administración por defecto

**IOC-022: Como usuario, quiero cerrar sesión de forma segura para proteger mi cuenta cuando termino de usar la plataforma.**
- ✅ Cierre de sesión exitoso: Al hacer clic en 'Cerrar Sesión', el sistema finaliza la sesión y redirige a la página de login
- ✅ Invalidación de la sesión: Después de cerrar sesión, no se puede acceder a páginas protegidas usando el historial
- ✅ Visibilidad del botón de logout: El botón para cerrar sesión es claramente visible y accesible en todo momento

**IOC-023: Como usuario, quiero navegar entre las secciones principales de la aplicación para acceder fácilmente a las diferentes funcionalidades.**
- ✅ Navegación completa (Admin): Un administrador ve todas las opciones en la barra de navegación
- ✅ Navegación limitada (Gerente): Un gerente ve únicamente las opciones relevantes para su rol
- ✅ Protección de rutas: Un usuario sin permisos no puede acceder a una URL de administración directamente

**IOC-001: Como administrador, quiero cargar y validar archivos de producción para asegurar que solo datos fiables y de alta calidad se carguen en la base de datos.**
- ✅ Archivo CSV válido: Dado un archivo con formato correcto, al cargarlo, el sistema lo procesa sin error
- ✅ Archivo incorrecto: Dado un archivo con formato incorrecto, al intentar cargarlo, el sistema muestra un error claro
- ✅ Archivo duplicado: Dado un archivo ya cargado, al intentar cargarlo de nuevo, el sistema notifica al usuario y no lo procesa
- ✅ Archivo vacío: Dado un archivo CSV sin datos, al cargarlo, se notifica que está vacío y no se procesa

**IOC-006: Como gerente, quiero visualizar un dashboard con KPIs actualizados para tomar decisiones informadas sobre la operación de manera rápida y eficiente.**
- ✅ Dashboard carga exitosamente: Al acceder al dashboard con datos correctos, se muestran gráficos y métricas vigentes
- ✅ Aplicar filtros: Al seleccionar filtros, el dashboard actualiza la visualización según los criterios
- ✅ Manejo de errores: Si hay datos incompletos, se muestra un mensaje de error claro
- ✅ Actualización automática: El dashboard se refresca automáticamente para reflejar datos en tiempo real

---

## Checklist de Tareas del Sprint

| Nº | ID | Capa | Historia | Responsable | Tarea Técnica | Estado |
| :-: | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | FE-TASK-01 | Frontend | IOC-021 | Jaime | Crear vistas y componentes para el flujo de autenticación (SignIn, SignUp, etc.). | ✅ **Terminada** |
| 2 | FE-TASK-02 | Frontend | IOC-021 | Jaime | Integrar vistas de autenticación con Supabase Auth y react-hot-toast. | ✅ **Terminada** |
| 3 | FE-TASK-03 | Frontend | IOC-022 | Jaime | Implementar la lógica de signOut robusta en UserDropdown y AccountPage. | ✅ **Terminada** |
| 4 | FE-TASK-04 | Frontend | IOC-023 | Jaime | Crear el componente ProtectedRoute para validar la sesión del usuario. | ✅ **Terminada** |
| 5 | FE-TASK-05 | Frontend | IOC-023 | Jaime | Crear el AppLayout principal (Sidebar, Header, etc.). | ✅ **Terminada** |
| 6 | FE-TASK-06 | Frontend | IOC-023 | Jaime | Limpiar App.tsx y AppSidebar.tsx de rutas y enlaces de demostración. | ✅ **Terminada** |
| 7 | FE-TASK-07 | Frontend | IOC-001 | Boris | Construir la UI de la página de Ingesta de Datos con datos simulados. | ✅ **Terminada** |
| 8 | FE-TASK-08 | Frontend | IOC-001 | Boris | Instalar axios y crear archivo de tipos src/types/api.ts. | ✅ **Terminada** |
| 9 | FE-TASK-09 | Frontend | IOC-001 | Boris | Crear src/services/apiService.ts con instancia de axios y tokenProvider. | ✅ **Terminada** |
| 10 | FE-TASK-10 | Frontend | IOC-001 | Boris | Implementar las funciones startEtlProcess y getJobStatus en apiService.ts. | ✅ **Terminada** |
| 11 | FE-TASK-11 | Frontend | IOC-001 | Boris | Crear un helper src/utils/apiError.ts para parsear errores del backend. | ✅ **Terminada** |
| 12 | FE-TASK-12 | Frontend | IOC-001 | Boris | Refactorizar DataIngestionPage.tsx para llamar a startEtlProcess al subir archivo. | ✅ **Terminada** |
| 13 | FE-TASK-13 | Frontend | IOC-001 | Boris | Implementar lógica de sondeo (setInterval) en DataIngestionPage.tsx. | ✅ **Terminada** |
| 14 | FE-TASK-14 | Frontend | IOC-001 | Boris | Implementar lógica de limpieza (clearInterval) en useEffect. | ✅ **Terminada** |
| 15 | FE-TASK-15 | Frontend | IOC-001 | Boris | Actualizar la UI de UploadHistoryTable con los datos reales obtenidos del sondeo. | ✅ **Terminada** |
| 16 | FE-TASK-17 | Frontend | IOC-001 | Jaime | Localizar la UI al español y aplicar rebranding "Cambiaso". | ✅ **Terminada** |
| 17 | BE-TASK-01 | Backend | IOC-021 | Jaime | Configurar Spring Security, CORS y dependencias de JWT. | ✅ **Terminada** |
| 18 | BE-TASK-02 | Backend | IOC-023 | Jaime | Implementar JwtRequestFilter para validar tokens en cada petición. | ✅ **Terminada** |
| 19 | BE-TASK-03 | Backend | IOC-021 | Jaime | Crear AuthController y AuthService para manejar el login y la generación de JWT. | ✅ **Terminada** |
| 20 | BE-TASK-04 | Backend | IOC-001 | Boris | Definir Entidades JPA (EtlJob, etc.) y Repositorios. | ✅ **Terminada** |
| 21 | BE-TASK-05 | Backend | IOC-001 | Boris | Crear EtlController con endpoints para start-process y status. | ✅ **Terminada** |
| 22 | BE-TASK-06 | Backend | IOC-001 | Boris | Implementar EtlJobService para la gobernanza de jobs (creación, idempotencia). | ✅ **Terminada** |
| 23 | BE-TASK-07 | Backend | IOC-001 | Boris | Implementar EtlProcessingService con lógica @Async para el procesamiento en segundo plano. | ✅ **Terminada** |
| 24 | BE-TASK-08 | Backend | IOC-001 | Boris | Implementar ParserService y DataSyncService para la validación y persistencia transaccional. | ✅ **Terminada** |
| 25 | BE-TASK-09 | Backend | IOC-001 | Boris | Implementar de-duplicación intra-archivo en ParserService para prevenir la ingesta de registros lógicamente idénticos. | ✅ **Terminada** |
| 26 | BE-TASK-10 | Backend | IOC-001 | Boris | Añadir un mecanismo de bloqueo pesimista (Advisory Lock) en DataSyncService para serializar ejecuciones concurrentes de ETL con rangos de fecha superpuestos. | ✅ **Terminada** |
| 27 | BE-TASK-11 | Backend | IOC-001 | Boris | Implementar un sistema de reintentos con backoff exponencial en DataSyncService para manejar y absorber colisiones transitorias de clave única (UNIQUE constraint). | ✅ **Terminada** |
| 28 | BE-TASK-12 | Backend | IOC-001 | Boris | Crear un EtlJobWatchdog programado para detectar y marcar automáticamente como "FALLO" los jobs que permanezcan en estados intermedios por un tiempo prolongado. | ✅ **Terminada** |
| 29 | BE-TASK-13 | Backend | IOC-001 | Boris | Instrumentar todo el pipeline ETL con métricas de Micrometer, incluyendo contadores, temporizadores y gauges para monitorear el rendimiento y la salud del proceso. | ✅ **Terminada** |
| 30 | BE-TASK-14 | Backend | IOC-001 | Boris | Desarrollar un HealthIndicator personalizado (/actuator/health/etl) que exponga el estado de la integridad de los datos y la configuración operacional del ETL. | ✅ **Terminada** |
| 31 | BE-TASK-15 | Backend | IOC-001 | Boris | Fortalecer el esquema de la base de datos añadiendo un índice UNIQUE sobre la clave de negocio natural y un índice de rendimiento en la columna de fecha de fact_production. | ✅ **Terminada** |
| 32 | BE-TASK-16 | Backend | IOC-001 | Boris | Implementar verificadores de arranque (@PostConstruct) para validar la integridad de la base de datos (ej. ausencia de duplicados) al iniciar la aplicación. | ✅ **Terminada** |
| 33 | BE-TASK-17 | Backend | IOC-006a | Jaime | Configurar MetabaseProperties.java: Crear la clase de configuración para cargar los ajustes de Metabase (URL, secret key, dashboards) desde application.properties. | ✅ **Terminada** |
| 34 | BE-TASK-18 | Backend | IOC-006a | Jaime | Implementar MetabaseEmbeddingService: Crear el servicio principal para la lógica de negocio, incluyendo la validación de autorización por roles y la generación de tokens JWT firmados para Metabase. | ✅ **Terminada** |
| 35 | BE-TASK-19 | Backend | IOC-006a | Jaime | Crear DashboardController: Exponer el endpoint seguro GET /api/v1/dashboards/{dashboardId} para que el frontend pueda solicitar las URLs firmadas. | ✅ **Terminada** |
| 36 | BE-TASK-20 | Backend | IOC-006a | Jaime | Implementar DashboardAuditService: Crear un servicio para registrar todos los intentos de acceso a dashboards (exitosos y fallidos) en los logs. | ✅ **Terminada** |
| 37 | BE-TASK-21 | Backend | IOC-006a | Jaime | Ajustar SecurityConfig para CSP: Modificar la Política de Seguridad de Contenido (Content-Security-Policy) para permitir que los iframes de Metabase se rendericen correctamente. | ✅ **Terminada** |
| 38 | BE-TASK-22 | Backend | IOC-006a | Jaime | Añadir JwtAuthenticationConverter: Configurar Spring Security para convertir el JWT de Supabase en el objeto CustomUserDetails, permitiendo el filtrado por atributos de usuario. | ✅ **Terminada** |
| 39 | BE-TASK-23 | Backend | IOC-006a | Jaime | Implementar Resiliencia (Circuit Breaker): Añadir y configurar Resilience4j en MetabaseEmbeddingService para manejar caídas del servidor de Metabase. | ✅ **Terminada** |
| 40 | BE-TASK-24 | Backend | IOC-006a | Jaime | Implementar Caché de Tokens: Añadir y configurar Caffeine para cachear los tokens de Metabase y mejorar el rendimiento. | ✅ **Terminada** |

---

## Progreso del Sprint

### Historias Completadas: 5/5 (100%)
- ✅ IOC-021: Como usuario, quiero iniciar sesión en la plataforma para acceder a las funcionalidades que corresponden a mi rol.
- ✅ IOC-022: Como usuario, quiero cerrar sesión de forma segura para proteger mi cuenta cuando termino de usar la plataforma.
- ✅ IOC-023: Como usuario, quiero navegar entre las secciones principales de la aplicación para acceder fácilmente a las diferentes funcionalidades.
- ✅ IOC-001: Como administrador, quiero cargar y validar archivos de producción para asegurar que solo datos fiables y de alta calidad se carguen en la base de datos.
- ✅ IOC-006: Como gerente, quiero visualizar un dashboard con KPIs actualizados para tomar decisiones informadas sobre la operación de manera rápida y eficiente.

### Story Points Completados: 41/41 (100%)
- Completados: 41 SP (IOC-021: 5 SP + IOC-022: 2 SP + IOC-023: 8 SP + IOC-001: 13 SP + IOC-006: 13 SP)

---

## Riesgos y Dependencias Identificados

### Dependencias Técnicas

1. **Metabase (Servicio Externo)**
   - **Impacto:** Crítico para IOC-006
   - **Descripción:** El dashboard de producción depende completamente de la disponibilidad, configuración y rendimiento de Metabase
   - **Mitigación Implementada:** 
     - Circuit Breaker con Resilience4j para manejar caídas del servidor
     - Caché de tokens con Caffeine para reducir dependencia en tiempo real
     - Manejo de errores robusto en el frontend

2. **Supabase Auth (Servicio Externo)**
   - **Impacto:** Crítico para IOC-021, IOC-022, IOC-023
   - **Descripción:** Todo el sistema de autenticación depende de la disponibilidad de Supabase
   - **Mitigación Implementada:**
     - Manejo de errores y reintentos en el frontend
     - Tokens JWT con expiración controlada
     - Validación de sesión en múltiples capas

3. **Backend API REST**
   - **Impacto:** Crítico para IOC-001 y IOC-006
   - **Descripción:** El frontend depende de la disponibilidad y correcta operación de los endpoints del backend
   - **Mitigación Implementada:**
     - Health checks con Spring Boot Actuator
     - Manejo de timeouts y errores en axios
     - Feedback visual de estados de carga y error

4. **Base de Datos PostgreSQL**
   - **Impacto:** Crítico para IOC-001
   - **Descripción:** Todo el pipeline ETL y almacenamiento de datos depende de PostgreSQL
   - **Mitigación Implementada:**
     - Transacciones con rollback automático
     - Índices UNIQUE para prevenir duplicados
     - Verificadores de integridad al arranque

5. **Dependencia entre Historias**
   - **IOC-006 depende de IOC-001:** El dashboard necesita datos cargados para mostrar visualizaciones significativas
   - **IOC-023 depende de IOC-021:** Las rutas protegidas requieren autenticación funcionando

---

### Registro de Riesgos Scrum (Documento Vivo)
- Consulta el detalle completo de riesgos, mitigaciones, triggers y Risk Burndown en: `.gemini/evidencias/RISK_REGISTER_SCRUM.md`

---

### Lecciones Aprendidas del Sprint

- ✅ La implementación de Circuit Breaker fue clave para manejar dependencias externas
- ✅ El procesamiento asíncrono con polling proporciona mejor UX que esperas síncronas
- ✅ Los Advisory Locks previenen efectivamente problemas de concurrencia
- ✅ La instrumentación con Micrometer facilita el debugging y monitoreo
- 📝 Considerar implementar un modo offline básico para futuros sprints
- 📝 Evaluar alternativas de caché distribuido (Redis) si el volumen de usuarios crece
