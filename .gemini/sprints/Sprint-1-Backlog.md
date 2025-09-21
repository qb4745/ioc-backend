# Sprint 1 – Sprint Backlog (Flujo de Datos y Seguridad)

## Objetivo del Sprint
Entregar el esqueleto funcional del sistema: autenticación, layout y rutas protegidas, junto con el flujo de carga/validación de archivos CSV de producción de extremo a extremo en Supabase.

## Historias comprometidas
| ID | Título | Tipo | Feature | Prioridad | SP | Asignado | Estado |
| --- | --- | --- | --- | --- | ---: | --- | --- |
| IOC-001 | Cargar y validar un archivo CSV con datos de producción | Historia de Usuario | Ingesta de Datos | Crítica | 5 | Boris | Por Hacer |
| IOC-021 | Como Usuario, quiero iniciar sesión en la plataforma | Historia de Usuario | Autenticación | Crítica | 5 | Jaime | Por Hacer |
| IOC-022 | Como Usuario, quiero cerrar mi sesión de forma segura | Historia de Usuario | Autenticación | Crítica | 3 | Jaime | Por Hacer |
| IOC-023 | Construir el Layout Principal y las Rutas Protegidas | Tarea Técnica | Infraestructura Frontend | Crítica | 8 | Jaime | Por Hacer |

## Criterios de aceptación trazados
- IOC-001: - La interfaz permite seleccionar y subir un archivo CSV. - El sistema rechaza archivos con formato incorrecto (ej. columnas faltantes). - Se muestra una notificación de éxito o error al finalizar la carga.
- IOC-021: - El usuario puede ingresar con email y contraseña. - Se muestra un error si las credenciales son incorrectas. - Tras el login exitoso, se redirige al dashboard principal.
- IOC-022: - Al hacer clic en "Cerrar Sesión", se redirige a la página de login. - La sesión anterior queda invalidada (no se puede volver con el botón "Atrás").
- IOC-023: - Se debe crear una estructura visual consistente (Navbar, Sidebar, etc.). - Las rutas deben redirigir al login si el usuario no está autenticado. - Las rutas de admin deben ser inaccesibles para roles no autorizados.

## Tareas técnicas por historia
### IOC-001
- Backend: endpoint multipart para upload; validación de cabeceras y esquema; parse seguro; inserción en lotes con transacciones; resumen de errores.
- Backend: validadores de formato y negocio (fechas, IDs de máquina); manejo de duplicados (checksum o nombre+fecha); métricas y logs.
- Frontend: formulario de carga con estados (progreso/éxito/error); mensajes de validación; pruebas con archivos vacíos y malformados.
### IOC-021
- Frontend: pantalla de login; integración con Supabase Auth; manejo de errores y loading.
- Backend: verificación de token en la capa API (si aplica), CORS y endurecimiento básico de seguridad.
### IOC-022
- Frontend: acción de logout; limpieza de sesión/estado; redirección a login y verificación de acceso denegado post-logout.
- Backend: invalidación de sesión/token en servidor (si aplica); pruebas de acceso a rutas protegidas tras logout.
### IOC-023
- Frontend: layout persistente (Navbar/Sidebar/Contenido) y router; guard de rutas por rol con redirección a login/403.
- Frontend: estructura base (features/components/services/hooks) y placeholders para vistas futuras.

## Definition of Done (DoD) del sprint
- Criterios de aceptación verificados en ambiente de prueba con datos de ejemplo y casos de error.
- Código versionado con build/checks automáticos; endpoints y documentación mínima para el frontend.
- Rutas protegidas operativas; login/logout funcionales; carga de CSV valida/rechaza correctamente e inserta datos en Supabase.
- Demo de fin de sprint del flujo end-to-end: autenticación → carga CSV → confirmación y verificación en base de datos.

## Entregables del sprint
- Frontend: login/logout, layout y rutas protegidas; vista de carga de CSV con feedback.
- Backend: upload/validación/ingesta CSV; logs y mensajes de error consistentes; verificación de token (si aplica).
- Artefactos: registro de pruebas (válido/invalidos/duplicados); guía breve para probar el flujo.

## Métricas y capacidad
- Alcance: 4 historias, 21 SP.
- Seguimiento: burnup/burndown por historia; tiempo de ciclo por tarea; % de cargas rechazadas correctamente en pruebas.

## Riesgos e impedimentos a monitorear
- Variabilidad del formato CSV (nombres de columnas/encoding) que afecte la validación e inserción en lote.
- Integración de autenticación y guard de rutas que bloquee pruebas end-to-end si no se alinea temprano.
- Latencia en inserciones si faltan índices/claves en tablas objetivo de Supabase.

## Plan de pruebas del sprint
- Casos happy-path y negativos por historia basados en criterios de aceptación.
- Pruebas manuales E2E: login → carga CSV (válido, malformado, vacío, duplicado) → verificación de persistencia/errores → logout.


## Desglose de Tareas Técnicas 

Para lograr el objetivo, desglosamos las historias en las siguientes tareas técnicas específicas para Backend y Frontend:

#### **A. Tareas de Autenticación y Estructura (Responsable: Jaime)**

| Historia | Capa | Tarea Técnica |
| :--- | :--- | :--- |
| `IOC-021` | Backend | **1.** Configurar `Spring Security` para la gestión de autenticación y autorización. |
| `IOC-021` | Backend | **2.** Crear un `AuthController` con endpoints para login (`/api/auth/login`). |
| `IOC-021` | Backend | **3.** Implementar un `AuthService` que se comunique con Supabase Auth para validar credenciales. |
| `IOC-021` | Backend | **4.** Implementar la generación de un token `JWT` (JSON Web Token) en caso de login exitoso. |
| `IOC-022` | Backend | **5.** Implementar un endpoint de logout (`/api/auth/logout`) que gestione la invalidación del token si es necesario (ej. blacklist). |
| `IOC-023` | Backend | **6.** Configurar el filtro de seguridad de Spring (`JwtRequestFilter`) para validar el JWT en cada petición a rutas protegidas. |
| `IOC-021` | Frontend | **7.** Crear la vista/componente de `LoginPage` con el formulario de email y contraseña. |
| `IOC-022` | Frontend | **8.** Implementar el `AuthService` en React para llamar a los endpoints de login/logout. |
| `IOC-023` | Frontend | **9.** Configurar `React Router` con la lógica de Rutas Públicas (`/login`) y Rutas Protegidas (`/admin`, `/dashboard`). |
| `IOC-023` | Frontend | **10.** Crear el componente `LayoutPrincipal` (con `Navbar`, `Sidebar` y área de contenido) que envuelva las rutas protegidas. |
| `IOC-022` | Frontend | **11.** Implementar la lógica para almacenar/eliminar el JWT de forma segura en el cliente (ej. `localStorage` o `sessionStorage`). |

#### **B. Tareas de Ingesta de Datos (Responsable: Boris)**

| Historia | Capa | Tarea Técnica |
| :--- | :--- | :--- |
| `IOC-001` | Backend | **1.** Crear la entidad JPA (`ProductionData`) que mapee la estructura de la tabla de datos de producción. |
| `IOC-001` | Backend | **2.** Crear el `DataIngestionController` con un endpoint `POST /api/admin/data/upload` que acepte un `MultipartFile`. |
| `IOC-001` | Backend | **3.** Implementar un `CsvProcessingService` para leer y parsear el archivo CSV (usando una librería como `OpenCSV`). |
| `IOC-001` | Backend | **4.** Dentro del servicio, implementar la lógica de validación de cabeceras y de datos a nivel de fila. |
| `IOC-001` | Backend | **5.** Crear un `ProductionDataRepository` (Spring Data JPA) para guardar los registros válidos en la base de datos. |
| `IOC-001` | Backend | **6.** Implementar un manejo de errores robusto que reporte qué filas fallaron y por qué. |
| `IOC-001` | Frontend | **7.** Crear la vista/componente `AdminDataUploadPage` dentro de una ruta protegida (`/admin/upload`). |
| `IOC-001` | Frontend | **8.** Construir el componente de UI para seleccionar un archivo (`<input type="file">`) y un botón para iniciar la carga. |
| `IOC-001` | Frontend | **9.** Implementar la llamada al servicio API del frontend que envíe el archivo al backend. |
| `IOC-001` | Frontend | **10.** Gestionar el estado de la UI durante la carga (mostrar un `spinner`) y mostrar notificaciones de éxito o error basadas en la respuesta del backend. |
