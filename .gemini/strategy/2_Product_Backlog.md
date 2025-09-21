# Product Backlog - Proyecto Inteligencia Operacional Cambiaso (IOC)

Este documento contiene la lista completa y priorizada de Historias de Usuario, Tareas Técnicas y Épicas para el proyecto IOC. Sirve como la única fuente de verdad para todos los requerimientos del producto.

## Backlog General

| ID | Título | Feature | Prioridad | Asignado a | Sprint | SP | Criterios de Aceptación |
| :--- | :--- | :--- | :--- | :--- | :--- | :-: | :--- |
| **IOC-001** | Cargar y validar un archivo CSV con datos de producción | Ingesta de Datos | Crítica | Boris | Sprint 1 | 5 | - La interfaz permite seleccionar y subir un archivo CSV.<br>- El sistema rechaza archivos con formato incorrecto (ej. columnas faltantes).<br>- Se muestra una notificación de éxito o error al finalizar la carga. |
| **IOC-021** | Como Usuario, quiero iniciar sesión en la plataforma | Autenticación | Crítica | Jaime | Sprint 1 | 5 | - El usuario puede ingresar con email y contraseña.<br>- Se muestra un error si las credenciales son incorrectas.<br>- Tras el login exitoso, se redirige al dashboard principal. |
| **IOC-022** | Como Usuario, quiero cerrar mi sesión de forma segura | Autenticación | Crítica | Jaime | Sprint 1 | 3 | - Al hacer clic en "Cerrar Sesión", se redirige a la página de login.<br>- La sesión anterior queda invalidada (no se puede volver con el botón "Atrás"). |
| **IOC-023** | Construir el Layout Principal y las Rutas Protegidas | Infraestructura Frontend | Crítica | Jaime | Sprint 1 | 8 | - Se debe crear una estructura visual consistente (Navbar, Sidebar, etc.).<br>- Las rutas deben redirigir al login si el usuario no está autenticado.<br>- Las rutas de admin deben ser inaccesibles para roles no autorizados. |
| **IOC-006a**| Como Gerente, quiero visualizar un dashboard con sus gráficos y KPIs | Visualización de Datos | Crítica | Boris | Sprint 2 | 13 | - El sistema carga y muestra todos los gráficos y métricas configuradas.<br>- Se muestran indicadores de carga claros (ej. 'esqueletos' o 'spinners'). |
| **IOC-008a**| Como Gerente, quiero comparar el desempeño de múltiples entidades | Dashboard Interactivo | Alta | Boris | Sprint 3 | 8 | - Un gráfico incluye un control para 'Comparar por' (ej. Turno, Máquina).<br>- Al seleccionar una opción, el gráfico se redibuja para mostrar múltiples series de datos. |
| **IOC-012a**| Como Supervisor-Analista, quiero filtrar un dashboard por un rango de fechas | Dashboard Interactivo | Crítica | Boris | Sprint 3 | 5 | - La interfaz incluye un selector de fechas con opciones y rango personalizado.<br>- Al aplicar un filtro, todos los gráficos del dashboard se actualizan. |
| **IOC-012b**| Como Supervisor-Analista, quiero filtrar por categorías de producción | Dashboard Interactivo | Crítica | Jaime | Sprint 3 | 8 | - La interfaz presenta filtros desplegables para 'Línea', 'Máquina' y 'Turno'.<br>- Al seleccionar un valor y aplicar, todos los gráficos se actualizan. |
| **IOC-012c**| Como Supervisor-Analista, quiero restaurar todos los filtros a su estado por defecto | Dashboard Interactivo | Crítica | Boris | Sprint 3 | 2 | - Existe un botón visible 'Limpiar Filtros'.<br>- Al hacer clic, todos los filtros vuelven a su estado inicial y los gráficos se actualizan. |
| **IOC-002a**| Como Administrador, quiero ver la lista de KPIs del sistema | Configuración de KPIs | Alta | Jaime | Sprint 4 | 5 | - La interfaz muestra una tabla con Nombre del KPI, Descripción e interruptor de alertas 'Activas'/'Inactivas'. |
| **IOC-002b**| Como Administrador, quiero establecer un umbral numérico para un KPI | Configuración de KPIs | Alta | Boris | Sprint 4 | 5 | - El formulario de edición de KPI incluye un campo numérico para el 'Umbral'.<br>- El sistema valida que el valor sea un número y lo guarda. |
| **IOC-002c**| Como Administrador, quiero activar o desactivar las alertas para un KPI | Configuración de KPIs | Alta | Jaime | Sprint 4 | 3 | - La lista de KPIs muestra un interruptor (toggle) para las alertas de cada KPI.<br>- Si un KPI está 'Inactivo', el sistema no generará alertas para él. |
| **IOC-003a**| Como Administrador, quiero ver la lista de todos los usuarios del sistema | Gestión de Usuarios | Alta | Jaime | Sprint 4 | 5 | - La tabla muestra Nombre, Apellido, Email y Rol de cada usuario.<br>- La tabla está paginada si hay más de 20 usuarios. |
| **IOC-003b**| Como Administrador, quiero crear un nuevo usuario en el sistema | Gestión de Usuarios | Alta | Boris | Sprint 4 | 5 | - El formulario requiere Nombre, Apellido, Email y Rol.<br>- La contraseña se genera y se envía un enlace de bienvenida/restablecimiento.<br>- El email debe ser único. |
| **IOC-003c**| Como Administrador, quiero cambiar el rol de un usuario existente | Gestión de Usuarios | Alta | Jaime | Sprint 4 | 3 | - El formulario de edición permite cambiar el rol del usuario desde una lista desplegable.<br>- El cambio se refleja y los permisos se actualizan. |
| **IOC-003d**| Como Administrador, quiero ver la lista de roles y sus permisos | Gestión de Usuarios y Roles | Alta | Jaime | Sprint 4 | 5 | - La tabla de 'Gestión de Roles' muestra Nombre, Descripción y un botón para 'Ver/Editar Permisos'. |
| **IOC-003e**| Como Administrador, quiero crear un nuevo rol en el sistema | Gestión de Usuarios y Roles | Alta | Boris | Sprint 4 | 5 | - El formulario de creación requiere un Nombre de rol único.<br>- El nuevo rol se crea sin permisos por defecto. |
| **IOC-003f**| Como Administrador, quiero asignar o revocar permisos a un rol | Gestión de Usuarios y Roles | Crítica | Jaime | Sprint 4 | 5 | - La pantalla de edición de un rol muestra una lista de permisos disponibles con checkboxes para seleccionar/deseleccionar. |
| **IOC-003g**| Como Administrador, quiero eliminar un usuario del sistema | Gestión de Usuarios | Media | Boris | Sprint 4 | 2 | - Al hacer clic en 'Eliminar', se muestra un diálogo de confirmación.<br>- Tras confirmar, el usuario es eliminado y ya no puede iniciar sesión. |
| **IOC-019a**| Como Administrador, quiero crear una nueva definición de gráfico | Gestión de Gráficos | Alta | Boris | Sprint 4 | 8 | - La interfaz permite seleccionar Título, Tipo de Gráfico, Métrica y Dimensión.<br>- Se muestra una previsualización del gráfico mientras se configura. |
| **IOC-019b**| Como Administrador, quiero editar o eliminar un gráfico existente | Gestión de Gráficos | Alta | Jaime | Sprint 4 | 5 | - En la lista de gráficos, cada uno tiene opciones para 'Editar' y 'Eliminar'.<br>- Al editar, se abre el formulario de creación con los datos precargados. |
| **IOC-020a**| Como Administrador, quiero añadir o quitar gráficos de un dashboard | Diseño de Dashboards | Crítica | Boris | Sprint 4 | 5 | - La vista de diseño muestra una lista de 'Gráficos Disponibles' que se pueden arrastrar al lienzo del dashboard y quitar de él. |
| **IOC-020b**| Como Administrador, quiero organizar y guardar la disposición de los gráficos | Diseño de Dashboards | Crítica | Jaime | Sprint 4 | 5 | - En 'modo edición', los gráficos se pueden arrastrar y redimensionar.<br>- Al 'Guardar Disposición', la nueva configuración se guarda. |

## Resumen de Puntos por Sprint

| Sprint | Puntos de Historia Totales |
| :--- | :--- |
| Sprint 1 | 21 |
| Sprint 2 | 13 |
| Sprint 3 | 23 |
| Sprint 4 | 66 |
