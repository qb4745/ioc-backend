# Bitácora Técnica del Sprint 1

Este documento registra el desglose técnico de las implementaciones realizadas durante el Sprint 1. Sirve como una memoria técnica para futuras consultas, mantenimiento y onboarding de nuevos miembros al equipo.

---
<!-- COMIENZO DE LA PLANTILLA DE HISTORIA - NO MODIFICAR -->
## [ID de la Historia]: [Título de la Historia]

*   **Objetivo de Negocio (El "Para Qué"):** [Descripción concisa del valor que aporta la historia al usuario/negocio.]
*   **Criterios de Aceptación Clave:**
    *   [Criterio 1]
    *   [Criterio 2]
    *   ...

### Desglose Técnico de la Implementación

*   **Plan Aprobado:** [Resumen del plan de alto nivel que Gemini propuso antes de la implementación.]

*   **Archivos Creados/Modificados:**
    *   `ruta/al/archivo1.java` (Backend)
    *   `ruta/al/componente1.tsx` (Frontend)
    *   ...

*   **Resumen de la Implementación:**
    [Explicación técnica detallada pero concisa. Por ejemplo: "Se creó el `AuthController` en el backend con un endpoint POST `/api/auth/login` que utiliza el `AuthService` para validar credenciales contra Supabase. En caso de éxito, genera un JWT. En el frontend, el componente `LoginPage` ahora gestiona el estado del formulario y, al enviarlo, llama al endpoint, almacenando el JWT en `localStorage` y redirigiendo al usuario al dashboard principal."]

### Verificación y Pruebas Realizadas

*   **Pruebas Automatizadas:**
    *   **(Backend) Pruebas Unitarias:** [Describe brevemente qué se probó. Ej: "Se creó `AuthControllerTest` para verificar el endpoint `/api/auth/login` con credenciales válidas (esperando status 200 OK) y credenciales inválidas (esperando status 401 Unauthorized)."]
    *   **(Frontend) Pruebas de Componentes:** [Si aplica. Ej: "Se probó que el componente `LoginForm` maneja correctamente los estados de carga y error."]

*   **Pruebas Manuales (End-to-End):**
    *   **Caso de Prueba 1 (Happy Path):** [Describe el flujo. Ej: "1. Navegar a `/login`. 2. Ingresar un email y contraseña válidos. 3. Hacer clic en 'Iniciar Sesión'. **Resultado Esperado:** Redirección a `/dashboard` y almacenamiento del JWT."]
    *   **Caso de Prueba 2 (Caso de Error):** [Describe el flujo. Ej: "1. Navegar a `/login`. 2. Ingresar credenciales incorrectas. 3. Hacer clic en 'Iniciar Sesión'. **Resultado Esperado:** Aparición de un mensaje de error 'Credenciales incorrectas' y permanencia en la página de login."]

*   **Commit de Referencia (Squashed):**
    *   `hash-del-commit` - `tipo(ámbito): descripción corta (ID Historia) (#PR)`

---
<!-- FIN DE LA PLANTILLA DE HISTORIA -->

<!-- FIN DE LA PLANTILLA DE HISTORIA -->

<!-- NUEVAS ENTRADAS DE HISTORIAS SE AÑADEN AQUÍ -->
<!-- NUEVAS ENTRradas DE HISTORIAS SE AÑADEN AQUÍ -->
## IOC-001: Cargar y validar un archivo CSV con datos de producción

*   **Estado:** ✅ **Terminada (Backend)**
*   **Objetivo de Negocio (El "Para Qué"):** Permitir a los administradores cargar archivos de producción de forma segura y asíncrona, obteniendo feedback en tiempo real sobre el estado del procesamiento para agilizar la toma de decisiones.
*   **Criterios de Aceptación Clave:**
    *   La API permite subir un archivo CSV de forma segura.
    *   El sistema rechaza archivos duplicados para garantizar la idempotencia.
    *   El procesamiento del archivo es asíncrono; el cliente recibe una respuesta inmediata (`202 Accepted`).
    *   El cliente puede consultar el estado detallado de un job de carga en cualquier momento.
    *   El cliente recibe notificaciones en tiempo real sobre el progreso del job vía WebSockets.

### Desglose Técnico de la Implementación

*   **Plan Aprobado:** Se ejecutó el plan `task-ingesta-back-implementation-plan.md`, construyendo el sistema en capas:
    1.  **Persistencia:** Mapeo de entidades JPA y repositorios.
    2.  **Infraestructura:** Configuración de un pool de hilos (`@Async`) y WebSockets (STOMP) con seguridad integrada.
    3.  **Lógica de Negocio:** Implementación de servicios transaccionales para la gobernanza de jobs y la sincronización de datos.
    4.  **API:** Orquestación del flujo completo y exposición a través de endpoints REST seguros.

*   **Archivos Creados/Modificados:**
    *   `com.cambiaso.ioc.controller.EtlController`
    *   `com.cambiaso.ioc.service.*` (EtlProcessingService, EtlJobService, DataSyncService, NotificationService, ParserService)
    *   `com.cambiaso.ioc.persistence.entity.*` (EtlJob, FactProduction, etc.)
    *   `com.cambiaso.ioc.persistence.repository.*` (EtlJobRepository, etc.)
    *   `com.cambiaso.ioc.config.*` (AsyncConfig, WebSocketConfig, WebSocketSecurityConfig)
    *   `com.cambiaso.ioc.dto.*` (EtlJobStatusDto, NotificationPayload)
    *   `com.cambiaso.ioc.exception.*` (JobConflictException, etc.)
    *   `src/test/java/com/cambiaso/ioc/**` (Tests de integración y unitarios)

*   **Resumen de la Implementación:**
    Se ha construido un pipeline de ETL asincrónico y robusto. El `EtlController` recibe el archivo, valida su unicidad mediante un hash SHA-256 y crea un registro en `EtlJobService`. Inmediatamente, delega el procesamiento a `EtlProcessingService` en un hilo separado (`@Async`), devolviendo un `202 Accepted` con el `jobId`. El orquestador se encarga de parsear el archivo (con lógica "find-or-create" para dimensiones), validar la concurrencia con "window locking", sincronizar los datos de forma transaccional (`delete-insert`) y notificar cada paso del proceso (`PROCESANDO`, `EXITO`, `FALLO`) al usuario a través de `NotificationService` y WebSockets seguros.

### Verificación y Pruebas Realizadas

*   **Pruebas Automatizadas:**
    *   **(Backend) Pruebas de Integración y Unitarias:** Se ha creado una suite de pruebas exhaustiva que valida cada capa del sistema:
        *   `PersistenceLayerTest`: Valida todos los mapeos de entidades JPA y constraints de la base de datos con H2.
        *   `ParserServiceTest`: Asegura que el parser procesa correctamente el formato de archivo real, incluyendo la lógica "find-or-create".
        *   `NotificationServiceTest`: Verifica que las notificaciones se envían al destino y usuario correctos.
        *   `EtlJobServiceTest`: Valida la lógica de gobernanza, incluyendo la idempotencia por hash y las guardas de concurrencia por rango de fechas.
        *   `DataSyncServiceTest`: Confirma el comportamiento transaccional atómico, incluyendo el rollback exitoso en caso de fallo.
        *   `EtlControllerTest`: Prueba la capa de API con `MockMvc`, validando la seguridad de los endpoints, las respuestas HTTP correctas (202, 401, 409) y la invocación del flujo asíncrono.

*   **Pruebas Manuales (End-to-End):**
    *   **Caso de Prueba 1 (Happy Path):** 1. Obtener un JWT válido. 2. Enviar una petición `POST` a `/api/etl/start-process` con un archivo CSV válido. **Resultado Esperado:** Recibir una respuesta `202 Accepted` con el `jobId`. 3. Consultar el endpoint `GET /api/etl/jobs/{jobId}/status` y observar la transición de estado hasta `EXITO`.
    *   **Caso de Prueba 2 (Caso de Error - Duplicado):** 1. Enviar el mismo archivo CSV una segunda vez. **Resultado Esperado:** Recibir una respuesta `409 Conflict` con un mensaje indicando que el archivo ya fue procesado.

*   **Commit de Referencia (Squashed):**
    *   `[hash-del-commit-post-merge] - feat(etl): implementa pipeline de ingesta asincrónico (IOC-001) (#PR)`
---


---
## IOC-021: Como Usuario, quiero iniciar sesión en la plataforma
*   **Estado:** ✅ **Terminada**
*   **Objetivo de Negocio (El "Para Qué"):** Permitir a los usuarios acceder a la plataforma de forma segura mediante sus credenciales y ofrecer un mecanismo para recuperar su cuenta si olvidan la contraseña.
*   **Criterios de Aceptación Clave:**
    *   El usuario puede ingresar con email y contraseña.
    *   Se muestra un error si las credenciales son incorrectas.
    *   Tras el login exitoso, se redirige al dashboard principal.
    *   (Extendido) El usuario puede registrarse y solicitar un reseteo de contraseña.

### Desglose Técnico de la Implementación

*   **Plan Aprobado:** Seguir los blueprints para la autenticación con Supabase, implementando los formularios de `SignIn`, `SignUp`, `ResetPassword` y `UpdatePassword`.
*   **Archivos Creados/Modificados:**
    *   **Creados:** `src/lib/supabaseClient.ts`, `src/context/AuthProvider.tsx`, `src/components/auth/ResetPasswordForm.tsx`, `src/pages/AuthPages/ResetPassword.tsx`, `src/components/auth/UpdatePasswordForm.tsx`, `src/pages/AuthPages/UpdatePassword.tsx`.
    *   **Modificados:** `src/components/auth/SignInForm.tsx`, `src/components/auth/SignUpForm.tsx`, `src/App.tsx`.
*   **Resumen de la Implementación:** Se implementó el flujo completo de autenticación de Supabase. Se crearon y modificaron los componentes de formulario para manejar el registro, inicio de sesión y el ciclo completo de recuperación de contraseña. La lógica central de estado se maneja en `AuthProvider`.

### Verificación y Pruebas Realizadas

*   **Pruebas Manuales (End-to-End):**
    *   **Caso de Prueba 1 (Happy Path):** 1. Registrar un nuevo usuario. 2. Iniciar sesión con las nuevas credenciales. **Resultado Esperado:** Redirección exitosa al dashboard.
    *   **Caso de Prueba 2 (Caso de Error):** 1. Intentar iniciar sesión con una contraseña incorrecta. **Resultado Esperado:** Se muestra un mensaje de error.
    *   **Caso de Prueba 3 (Flujo de Reseteo):** 1. Solicitar reseteo de contraseña. 2. Usar el enlace del correo para establecer una nueva contraseña. 3. Iniciar sesión con la nueva contraseña. **Resultado Esperado:** Éxito en todos los pasos.

*   **Commits de Referencia:**
    *   `82cf3c2` - `feat: Integrate Supabase authentication`
    *   `ed34de8` - `refactor: Integrar SignUpForm con la lógica de Supabase`
    *   `3de3f75` - `feat(auth): Implementar flujo de actualización de contraseña`
---
## IOC-022: Como Usuario, quiero cerrar mi sesión de forma segura
*   **Estado:** ✅ **Terminada**
*   **Objetivo de Negocio (El "Para Qué"):** Proporcionar a los usuarios una forma clara y segura de finalizar su sesión para proteger su cuenta.
*   **Criterios de Aceptación Clave:**
    *   Al hacer clic en "Cerrar Sesión", se redirige a la página de login.
    *   La sesión anterior queda invalidada.

### Desglose Técnico de la Implementación

*   **Plan Aprobado:** Como parte del flujo de autenticación, se crearía una vista de cuenta de usuario con un botón para cerrar sesión.
*   **Archivos Creados/Modificados:**
    *   **Creados:** `src/pages/Account.tsx`.
*   **Resumen de la Implementación:** Se creó la página `Account.tsx`, accesible solo para usuarios autenticados. Esta página utiliza el `useAuth` hook para obtener la información del usuario y contiene un botón que llama a la función `supabase.auth.signOut()`. Tras el cierre de sesión exitoso, el usuario es redirigido a `/signin`.

### Verificación y Pruebas Realizadas

*   **Pruebas Manuales (End-to-End):**
    *   **Caso de Prueba 1 (Cierre de Sesión):** 1. Iniciar sesión. 2. Navegar a `/account`. 3. Hacer clic en "Sign Out". **Resultado Esperado:** Redirección a `/signin`.
    *   **Caso de Prueba 2 (Verificación de Invalidación):** 1. Después de cerrar sesión, intentar acceder a `/account` o a la ruta raíz (`/`). **Resultado Esperado:** Ser redirigido a `/signin` en ambos casos.

*   **Commit de Referencia:**
    *   `82cf3c2` - `feat: Integrate Supabase authentication` (Implementado como parte de la autenticación inicial)
---
## IOC-023: Construir el Layout Principal y las Rutas Protegidas
*   **Estado:** ✅ **Terminada**
*   **Objetivo de Negocio (El "Para Qué"):** Establecer la estructura visual principal de la aplicación y asegurar que solo los usuarios autenticados puedan acceder a las secciones protegidas.
*   **Criterios de Aceptación Clave:**
    *   Se debe crear una estructura visual consistente.
    *   Las rutas deben redirigir al login si el usuario no está autenticado.

### Desglose Técnico de la Implementación

*   **Plan Aprobado:** Crear un componente `ProtectedRoute` y un `AppLayout` consistentes. Limpiar el enrutador y el menú lateral para incluir únicamente las rutas relevantes para el MVP.
*   **Archivos Creados/Modificados:**
    *   **Creados:** `src/components/auth/ProtectedRoute.tsx`.
    *   **Modificados:** `src/App.tsx`, `src/layout/AppSidebar.tsx`.
*   **Resumen de la Implementación:** Se implementó el `ProtectedRoute` para validar la sesión del usuario. Se refactorizó `App.tsx` y `AppSidebar.tsx` para eliminar todas las rutas y enlaces de demostración de la plantilla, dejando una estructura de navegación limpia y enfocada en las funcionalidades del MVP.
*   **Commit de Referencia:** `90b10f5` - `refactor(layout): Limpiar rutas y enlaces de demostración`
---

