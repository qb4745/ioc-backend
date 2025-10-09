# Sprint 1 – Sprint Backlog (El Ciclo de Valor Completo)

## Objetivo del Sprint
Entregar un ciclo de valor de extremo a extremo: un usuario podrá autenticarse, cargar datos de producción a través de un archivo CSV y visualizar inmediatamente un dashboard con KPIs y gráficos que reflejen esa nueva información.

## Historias Comprometidas
| ID | Título | Tipo | Feature | Prioridad | SP | Asignado | Estado |
| :--- | :--- | :--- | :--- | :--- | :-: | :--- | :--- |
| IOC-021 | Como Usuario, quiero iniciar sesión en la plataforma | Historia de Usuario | Autenticación | Crítica | 5 | Jaime | ✅ **Terminada** |
| IOC-022 | Como Usuario, quiero cerrar mi sesión de forma segura | Historia de Usuario | Autenticación | Crítica | 3 | Jaime | ✅ **Terminada** |
| IOC-023 | Construir el Layout Principal y las Rutas Protegidas | Tarea Técnica | Infraestructura Frontend | Crítica | 8 | Jaime | ✅ **Terminada** |
| IOC-001 | Cargar y validar un archivo CSV con datos de producción | Historia de Usuario | Ingesta de Datos | Crítica | 5 | Boris | 🟡 **En Progreso** |
| IOC-006a | Como Gerente, quiero visualizar un dashboard con sus gráficos y KPIs | Historia de Usuario | Visualización de Datos | Crítica | 13 | Boris | ❌ **Por Hacer** |

## Checklist de Tareas del Sprint

| ID | Capa | Historia | Responsable | Tarea Técnica | Estado |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **BE-TASK-01** | Backend | `IOC-021` | Jaime | Configurar Spring Security, CORS y dependencias de JWT. | ✅ **Terminada** |
| **BE-TASK-02** | Backend | `IOC-023` | Jaime | Implementar `JwtRequestFilter` para validar tokens en cada petición. | ✅ **Terminada** |
| **BE-TASK-03** | Backend | `IOC-021` | Jaime | Crear `AuthController` y `AuthService` para manejar el login y la generación de JWT. | ✅ **Terminada** |
| **FE-TASK-01** | Frontend | `IOC-021` | Jaime | Crear vistas y componentes para el flujo de autenticación (SignIn, SignUp, etc.). | ✅ **Terminada** |
| **FE-TASK-02** | Frontend | `IOC-021` | Jaime | Integrar vistas de autenticación con Supabase Auth y `react-hot-toast`. | ✅ **Terminada** |
| **FE-TASK-03** | Frontend | `IOC-022` | Jaime | Implementar la lógica de `signOut` robusta en `UserDropdown` y `AccountPage`. | ✅ **Terminada** |
| **FE-TASK-04** | Frontend | `IOC-023` | Jaime | Crear el componente `ProtectedRoute` para validar la sesión del usuario. | ✅ **Terminada** |
| **FE-TASK-05** | Frontend | `IOC-023` | Jaime | Crear el `AppLayout` principal (Sidebar, Header, etc.). | ✅ **Terminada** |
| **FE-TASK-06** | Frontend | `IOC-023` | Jaime | Limpiar `App.tsx` y `AppSidebar.tsx` de rutas y enlaces de demostración. | ✅ **Terminada** |
| **BE-TASK-04** | Backend | `IOC-001` | Boris | Definir Entidades JPA (`EtlJob`, etc.) y Repositorios. | ✅ **Terminada** |
| **BE-TASK-05** | Backend | `IOC-001` | Boris | Crear `EtlController` con endpoints para `start-process` y `status`. | ✅ **Terminada** |
| **BE-TASK-06** | Backend | `IOC-001` | Boris | Implementar `EtlJobService` para la gobernanza de jobs (creación, idempotencia). | ✅ **Terminada** |
| **BE-TASK-07** | Backend | `IOC-001` | Boris | Implementar `EtlProcessingService` con lógica `@Async` para el procesamiento en segundo plano. | ✅ **Terminada** |
| **BE-TASK-08** | Backend | `IOC-001` | Boris | Implementar `ParserService` y `DataSyncService` para la validación y persistencia transaccional. | ✅ **Terminada** |
| **FE-TASK-07** | Frontend | `IOC-001` | Boris | Construir la UI de la página de Ingesta de Datos con datos simulados. | ✅ **Terminada** |
| **FE-TASK-08** | Frontend | `IOC-001` | Boris | **(Pendiente)** Instalar `axios` y crear archivo de tipos `src/types/api.ts`. | ❌ **Por Hacer** |
| **FE-TASK-09** | Frontend | `IOC-001` | Boris | **(Pendiente)** Crear `src/services/apiService.ts` con instancia de `axios` y `tokenProvider`. | ❌ **Por Hacer** |
| **FE-TASK-10** | Frontend | `IOC-001` | Boris | **(Pendiente)** Implementar las funciones `startEtlProcess` y `getJobStatus` en `apiService.ts`. | ❌ **Por Hacer** |
| **FE-TASK-11** | Frontend | `IOC-001` | Boris | **(Pendiente)** Crear un helper `src/utils/apiError.ts` para parsear errores del backend. | ❌ **Por Hacer** |
| **FE-TASK-12** | Frontend | `IOC-001` | Boris | **(Pendiente)** Refactorizar `DataIngestionPage.tsx` para llamar a `startEtlProcess` al subir archivo. | ❌ **Por Hacer** |
| **FE-TASK-13** | Frontend | `IOC-001` | Boris | **(Pendiente)** Implementar lógica de sondeo (`setInterval`) en `DataIngestionPage.tsx`. | ❌ **Por Hacer** |
| **FE-TASK-14** | Frontend | `IOC-001` | Boris | **(Pendiente)** Implementar lógica de limpieza (`clearInterval`) en `useEffect`. | ❌ **Por Hacer** |
| **FE-TASK-15** | Frontend | `IOC-001` | Boris | **(Pendiente)** Actualizar la UI de `UploadHistoryTable` con los datos reales obtenidos del sondeo. | ❌ **Por Hacer** |
| **FE-TASK-16** | Frontend | `IOC-001` | Boris | **(Pendiente)** Crear y configurar `.env` para la variable `VITE_API_BASE_URL`. | ❌ **Por Hacer** |

## Criterios de Aceptación Trazados
- **IOC-021:** El usuario puede ingresar con email/contraseña y recibe feedback de error.
- **IOC-022:** El logout invalida la sesión y previene el acceso con el historial del navegador.
- **IOC-023:** La aplicación tiene un layout consistente y las rutas están protegidas.
- **IOC-001:** La UI permite la carga de archivos CSV y da feedback. La integración con el backend está pendiente.
- **IOC-006a:** El dashboard debe mostrar gráficos y KPIs con estados de carga claros.

## Definition of Done (DoD) del Sprint
- Criterios de aceptación de todas las historias verificados.
- Código versionado con build/checks automáticos.
- Flujo completo operativo: login → carga CSV → visualización de datos en el dashboard → logout.
- Demo de fin de sprint del flujo end-to-end.
