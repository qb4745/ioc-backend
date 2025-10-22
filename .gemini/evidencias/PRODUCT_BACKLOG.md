# 📋 PRODUCT BACKLOG - PLATAFORMA IOC
## Sistema de Business Intelligence con Metabase + Gestión de Datos

**Framework:** Scrum  
**Duración Sprints:** Sprint 1 (4 semanas) | Sprint 2 (3 semanas) | Sprint 3 (4 semanas)  
**Visualización:** Tablero Kanban  
**WIP Limit:** 3 tareas simultáneas por persona  
**Última Actualización:** 13 de Octubre, 2025
**Product Owner:** Boris Arriagada  
**Scrum Master:** Jaime Vicencio  
**Development Team:** Boris Arriagada, Jaime Vicencio

---

## 🎯 ÉPICAS DEL PROYECTO

| ID | Nombre | Objetivo | Estado |
|----|--------|----------|--------|
| **EP-01** | **Ingesta y Validación de Datos** | Permitir al administrador cargar archivos CSV de producción con validación automática, monitoreo de estado y gestión de errores detallada | 🟢 Activo |
| **EP-02** | **Contenido Analítico y Visualizaciones** | Proporcionar herramientas completas para crear, gestionar y organizar dashboards, gráficos y KPIs utilizando la integración con Metabase | 🟢 Activo |
| **EP-03** | **Acceso, Seguridad y Gestión de Usuarios** | Implementar un sistema robusto de gestión de usuarios, roles y permisos con control granular de acceso a dashboards y funcionalidades | 🟢 Activo |

---

## 📊 CONFIGURATION KANBAN BOARD

### Columnas y WIP Limits

| Columna | WIP Limit | Definición | Políticas de "Done" |
|---------|-----------|------------|---------------------|
| **Backlog** | ∞ | Todas las historias priorizadas pendientes de iniciar | Historia refinada con criterios de aceptación claros |
| **In Progress** | 6 items | Desarrollo activo en curso | Código en rama feature + tests unitarios iniciados |
| **Done** | ∞ | Completado y desplegado | DoD completado + integrado en main + documentado |

### WIP Limits por Rol
- **Frontend Developer:** Máximo 3 tareas simultáneas
- **Backend Developer:** Máximo 3 tareas simultáneas  
- **QA Engineer:** Máximo 4 tareas simultáneas (revisión)
- **Product Owner:** Sin límite (priorización)

---

## ✅ DEFINITION OF READY (DoR)

Toda historia debe cumplir estos 5 criterios antes de moverse a "Ready":

1. **Historia de Usuario Completa:** Formato "Como [rol], quiero [acción] para [beneficio]" con descripción clara del valor de negocio
2. **Criterios de Aceptación Definidos:** Mínimo 3 criterios medibles en formato Given-When-Then o checklist específico
3. **Dependencias Identificadas:** Todas las dependencias técnicas o de negocio están documentadas y resueltas o planificadas
4. **Estimación Completada:** Story Points asignados mediante Planning Poker con consenso del equipo
5. **Diseño y Assets Disponibles:** Mockups, wireframes o especificaciones técnicas necesarias están listos y accesibles

---

## ✅ DEFINITION OF DONE (DoD)

Toda historia debe cumplir estos 5 criterios antes de moverse a "Done":

1. **Código Completado y Revisado:** Código implementado, testeado localmente, PR aprobado por al menos 1 reviewer, y merge a main completado
2. **Tests Implementados y Pasando:** Tests unitarios con cobertura mínima 70%, tests de integración para flujos críticos, todos los tests en CI/CD pasan
3. **Documentación Actualizada:** README actualizado si aplica, código comentado en secciones complejas, documentación técnica en `/docs` si es nueva funcionalidad
4. **Sin Errores de Linting:** ESLint y TypeScript compilando sin errores, código formateado según estándares del proyecto
5. **Validación de QA Completada:** Funcionalidad testeada en entorno de staging, criterios de aceptación verificados, UX/UI aprobado por PO o diseñador

---

## 📋 PRODUCT BACKLOG ITEMS

### SPRINT 1: El Ciclo de Valor Completo (Fundación y Visualización)
**Duración:** 4 semanas (8 Sept - 4 Oct 2025)  
**Sprint Goal:** Al final de este Sprint, un Administrador podrá iniciar sesión, cargar datos de producción, y un Gerente podrá ver inmediatamente un dashboard con gráficos y KPIs que reflejen esa información.

| ID | Tipo | Título | Descripción | Prioridad | SP | Estado Kanban | Sprint |
|----|------|--------|-------------|-----------|----|--------------|----|
| **IOC-021** | 🎯 Story | Iniciar Sesión en la Plataforma | **Como** usuario, **quiero** iniciar sesión en la plataforma **para** acceder a las funcionalidades que corresponden a mi rol.<br><br>**Criterios de Aceptación:**<br>✅ Login exitoso: Al ingresar credenciales correctas, el sistema inicia sesión y redirige al dashboard<br>✅ Login fallido: Al ingresar credenciales incorrectas, el sistema muestra un mensaje de error y no permite el acceso<br>✅ Redirección post-login (opcional): Un administrador puede ser redirigido a una vista de administración por defecto | Must Have | 5 | In Progress | Sprint 1 |
| **IOC-022** | 🎯 Story | Cerrar Sesión de Forma Segura | **Como** usuario, **quiero** cerrar sesión de forma segura **para** proteger mi cuenta cuando termino de usar la plataforma.<br><br>**Criterios de Aceptación:**<br>✅ Cierre de sesión exitoso: Al hacer clic en 'Cerrar Sesión', el sistema finaliza la sesión y redirige a la página de login<br>✅ Invalidación de la sesión: Después de cerrar sesión, no se puede acceder a páginas protegidas usando el historial<br>✅ Visibilidad del botón de logout: El botón para cerrar sesión es claramente visible y accesible en todo momento | Must Have | 2 | In Progress | Sprint 1 |
| **IOC-023** | 🎯 Story | Layout Principal y Rutas Protegidas | **Como** usuario, **quiero** navegar entre las secciones principales de la aplicación **para** acceder fácilmente a las diferentes funcionalidades.<br><br>**Criterios de Aceptación:**<br>✅ Navegación completa (Admin): Un administrador ve todas las opciones en la barra de navegación<br>✅ Navegación limitada (Gerente): Un gerente ve únicamente las opciones relevantes para su rol<br>✅ Protección de rutas: Un usuario sin permisos no puede acceder a una URL de administración directamente | Must Have | 8 | In Progress | Sprint 1 |
| **IOC-001** | 🎯 Story | Cargar Archivo de Datos de Producción | **Como** administrador, **quiero** cargar y validar archivos de producción **para** asegurar que solo datos fiables y de alta calidad se carguen en la base de datos.<br><br>**Criterios de Aceptación:**<br>✅ Archivo CSV válido: Dado un archivo con formato correcto, al cargarlo, el sistema lo procesa sin error<br>✅ Archivo incorrecto: Dado un archivo con formato incorrecto, al intentar cargarlo, el sistema muestra un error claro<br>✅ Archivo duplicado: Dado un archivo ya cargado, al intentar cargarlo de nuevo, el sistema notifica al usuario y no lo procesa<br>✅ Archivo vacío: Dado un archivo CSV sin datos, al cargarlo, se notifica que está vacío y no se procesa | Must Have | 13 | Ready | Sprint 1 |
| **IOC-006** | 🎯 Story | Visualizar Dashboard de Producción | **Como** gerente, **quiero** visualizar un dashboard con KPIs actualizados **para** tomar decisiones informadas sobre la operación de manera rápida y eficiente.<br><br>**Criterios de Aceptación:**<br>✅ Dashboard carga exitosamente: Al acceder al dashboard con datos correctos, se muestran gráficos y métricas vigentes<br>✅ Aplicar filtros: Al seleccionar filtros, el dashboard actualiza la visualización según los criterios<br>✅ Manejo de errores: Si hay datos incompletos, se muestra un mensaje de error claro<br>✅ Actualización automática: El dashboard se refresca automáticamente para reflejar datos en tiempo real | Must Have | 13 | Ready | Sprint 1 |

---

### SPRINT 2: Desatando la Interactividad Analítica
**Duración:** 3 semanas (6 Oct - 25 Oct 2025)  
**Sprint Goal:** Al final de este Sprint, el dashboard estático se transformará en una herramienta de análisis interactiva, permitiendo al usuario final filtrar los datos y hacer comparativas para investigar el rendimiento.

| ID | Tipo | Título | Descripción | Prioridad | SP | Estado Kanban | Sprint |
|----|------|--------|-------------|-----------|----|--------------|----|
| **IOC-008** | 🎯 Story | Comparar Desempeño entre Entidades | **Como** gerente, **quiero** comparar el desempeño entre turnos y líneas **para** identificar patrones y oportunidades de mejora.<br><br>**Criterios de Aceptación:**<br>✅ Comparativa generada: Al aplicar filtros de rangos y entornos, los gráficos muestran la comparativa correcta<br>✅ Datos insuficientes: Al intentar visualizar una comparativa sin datos suficientes, se muestra un mensaje indicándolo<br>✅ Exportar resultados: Al solicitar una descarga, se genera un archivo PDF/Excel con los resultados<br>✅ Actualización dinámica: Al modificar filtros, los resultados se actualizan sin recargar toda la página | Must Have | 13 | Backlog | Sprint 2 |
| **IOC-012** | 🎯 Story | Filtrar Datos del Dashboard | **Como** supervisor-analista, **quiero** filtrar datos por línea, periodo y taller **para** realizar análisis detallados y específicos.<br><br>**Criterios de Aceptación:**<br>✅ Aplicación de filtros: Al seleccionar filtros, los datos y reportes se actualizan correctamente<br>✅ Ningún dato tras filtro: Si los filtros no arrojan resultados, se muestra un mensaje indicando que no hay datos<br>✅ Filtros inválidos: Si se aplican parámetros incorrectos, el sistema muestra un error y no aplica el filtro<br>✅ Reset de filtros: Al limpiar los filtros, los datos vuelven a mostrarse en su estado completo por defecto | Must Have | 8 | Backlog | Sprint 2 |

---

### SPRINT 3: El Poder de la Gobernanza
**Duración:** 4 semanas (27 Oct - 22 Nov 2025)  
**Sprint Goal:** Al final de este Sprint, el Administrador tendrá el control total sobre el contenido analítico y los usuarios, completando así todas las funcionalidades del MVP.

| ID | Tipo | Título | Descripción | Prioridad | SP | Estado Kanban | Sprint |
|----|------|--------|-------------|-----------|----|--------------|----|
| **IOC-002** | 🎯 Story | Validar datos cargados automáticamente | **Como** administrador, **quiero** validar datos cargados automáticamente **para** asegurar la calidad y precisión en los datos.<br><br>**Criterios de Aceptación:**<br>✅ Datos válidos: Al iniciar la validación de datos correctos, estos son aceptados sin errores<br>✅ Datos erróneos: Al validar datos con errores, el sistema los rechaza y muestra los errores<br>✅ Duplicados detectados: Durante la validación, el sistema marca y elimina datos duplicados<br>✅ Validación manual: Si la validación automática falla, se alerta al administrador para una revisión manual | Must Have | 8 | Backlog | Sprint 3 |
| **IOC-003** | 🎯 Story | Gestionar Usuarios, Roles y Permisos | **Como** administrador, **quiero** configurar parámetros y umbrales de KPIs **para** adaptar el sistema a las métricas de negocio que son más importantes para la empresa.<br><br>**Criterios de Aceptación:**<br>✅ Parámetros válidos: Al guardar parámetros dentro del rango permitido, estos se almacenan y aplican correctamente<br>✅ Parámetros inválidos: Al intentar guardar valores fuera de los límites, el sistema alerta sobre los valores no válidos<br>✅ KPIs activos: Al activar KPIs, se generan alertas si se exceden sus umbrales<br>✅ KPIs inactivos: Al desactivar KPIs, no se generan alertas para ellos | Must Have | 13 | Backlog | Sprint 3 |
| **IOC-019** | 🎯 Story | Gestionar Gráficos del Dashboard | **Como** administrador, **quiero** crear y gestionar las definiciones de los gráficos **para** controlar qué visualizaciones están disponibles en los dashboards.<br><br>**Criterios de Aceptación:**<br>✅ Creación exitosa: Al guardar un nuevo gráfico con datos válidos, este aparece en la lista de gráficos disponibles<br>✅ Modificación y eliminación: Al editar o eliminar un gráfico, la lista se actualiza para reflejar el cambio<br>✅ Validación de datos: Al intentar guardar un gráfico sin un campo obligatorio, el sistema muestra un error y no guarda | Must Have | 8 | Backlog | Sprint 3 |
| **IOC-020** | 🎯 Story | Diseñar Disposición del Dashboard | **Como** administrador, **quiero** diseñar la disposición del dashboard principal **para** controlar qué KPIs y gráficos son visibles para los usuarios finales.<br><br>**Criterios de Aceptación:**<br>✅ Acceso a la vista de diseño: La sección 'Diseño de Dashboard' carga una cuadrícula y la lista de componentes disponibles<br>✅ Añadir y quitar componentes: Al arrastrar componentes al lienzo o quitarlos, la interfaz refleja el cambio visualmente<br>✅ Guardado de la disposición: Al guardar la disposición, la nueva configuración se convierte en la vista por defecto para los usuarios | Must Have | 13 | Backlog | Sprint 3 |

---

### BACKLOG (Post-MVP - Sin Sprint Asignado)

| ID | Tipo | Título | Descripción | Prioridad | SP | Estado Kanban | Sprint |
|----|------|--------|-------------|-----------|----|--------------|----|
| **IOC-004** | 🎯 Story | Gestionar usuarios y roles | **Como** administrador, **quiero** gestionar usuarios y roles **para** controlar el acceso y proteger la información.<br><br>**Criterios de Aceptación:**<br>✅ Crear usuario válido: Al registrar un usuario con datos correctas, este es creado con sus roles asignados<br>✅ Modificar usuario válido: Al actualizar un usuario existente, los datos modificados se aplican correctamente<br>✅ Eliminar usuario: Al confirmar la eliminación de un usuario, este pierde el acceso inmediatamente<br>✅ Asignar roles con permisos: Al guardar roles para un usuario, sus accesos son controlados según los permisos definidos | Should Have | 8 | Backlog | Post-MVP |
| **IOC-005** | 🎯 Story | Recibir alertas en caso de desviaciones | **Como** gerente, **quiero** recibir alertas en caso de desviaciones **para** reaccionar rápidamente a eventos críticos.<br><br>**Criterios de Aceptación:**<br>✅ Alerta generada: Cuando un KPI supera su umbral, se envía una notificación al gestor correspondiente<br>✅ Alerta vista: Cuando el gestor consulta sus alertas, la alerta se marca como revisada<br>✅ Configurar alertas: Al guardar nuevos parámetros de aviso, el sistema los utiliza para futuras alertas<br>✅ Escalación: Si una alerta no es respondida en el tiempo definido, se eleva a instancias superiores | Should Have | 8 | Backlog | Post-MVP |
| **IOC-007** | 🎯 Story | Recibir alertas automáticas | **Como** gerente, **quiero** recibir alertas automáticas **para** detectar y reaccionar ante desviaciones críticas.<br><br>**Criterios de Aceptación:**<br>✅ Alerta enviada: Cuando el valor de un KPI excede el límite, se envía una notificación al destinatario<br>✅ Visualización de alertas: Al consultar el panel de alertas, estas son visibles y están ordenadas por prioridad<br>✅ Confirmar alerta: Cuando el usuario marca una alerta como revisada, su estado cambia y se registra la acción<br>✅ Escalación de alerta: Si una alerta no obtiene respuesta en el tiempo previsto, se notifica a un superior | Should Have | 8 | Backlog | Post-MVP |
| **IOC-009** | 🎯 Story | Analizar tendencias históricas | **Como** gerente, **quiero** analizar tendencias históricas **para** planificar y evaluar el rendimiento a largo plazo.<br><br>**Criterios de Aceptación:**<br>✅ Históricos mostrados: Al seleccionar un periodo, se visualizan correctamente los gráficos y análisis de tendencias<br>✅ Comparación temporal: Al cambiar el intervalo de tiempo, los gráficos adaptan la información al nuevo rango<br>✅ Exportar informe: Al solicitar la descarga, el informe del análisis se exporta sin errores<br>✅ Mensaje de falta de datos: Si no existen datos en el periodo seleccionado, se informa al usuario | Should Have | 8 | Backlog | Post-MVP |
| **IOC-010** | 🎯 Story | Personalizar dashboards | **Como** gerente, **quiero** personalizar dashboards **para** adaptar las vistas según necesidades particulares.<br><br>**Criterios de Aceptación:**<br>✅ Configuración guardada: Al guardar modificaciones de una vista, los cambios se aplican y reflejan correctamente<br>✅ Vista predeterminada: Al restaurar la configuración, el dashboard vuelve a su estado inicial<br>✅ Error en configuración: Al intentar guardar parámetros inválidos, se muestra un mensaje de error específico<br>✅ Compartir vistas: Cuando se comparte una configuración, el usuario receptor puede acceder a la vista personalizada | Should Have | 8 | Backlog | Post-MVP |
| **IOC-011** | 🎯 Story | Acceder desde dispositivos móviles | **Como** gerente, **quiero** acceder a la plataforma desde dispositivos móviles **para** consultar KPIs y datos en cualquier momento y lugar.<br><br>**Criterios de Aceptación:**<br>✅ Acceso desde móvil válido: Al iniciar sesión desde un dispositivo compatible, se muestra una interfaz optimizada<br>✅ Acceso móvil con fallas: Si hay problemas de conexión, se muestra un mensaje de error con posibles soluciones<br>✅ Seguridad en acceso móvil: Tras un login exitoso, el usuario accede solo a las funciones y datos autorizados<br>✅ Logout en móvil: Al cerrar sesión, esta finaliza y el usuario es redirigido a la pantalla de login | Could Have | 5 | Backlog | Post-MVP |
| **IOC-013** | 🎯 Story | Reportar incidencias | **Como** supervisor-analista, **quiero** reportar incidencias desde la plataforma **para** facilitar la comunicación y resolución rápida de problemas.<br><br>**Criterios de Aceptación:**<br>✅ Incidencia reportada: Al enviar un formulario con datos completos, el sistema guarda el reporte y notifica al equipo<br>✅ Campos obligatorios faltantes: Al intentar enviar un reporte incompleto, el sistema avisa sobre los campos faltantes<br>✅ Incidencia duplicada: Al reportar una incidencia similar a una existente, el sistema alerta sobre la posible duplicidad<br>✅ Incidencia cerrada: El estado de una incidencia resuelta se refleja correctamente al consultarla | Could Have | 5 | Backlog | Post-MVP |
| **IOC-014** | 🎯 Story | Descargar reportes en PDF o Excel | **Como** supervisor-analista, **quiero** descargar reportes en PDF o Excel **para** compartir información con otras áreas.<br><br>**Criterios de Aceptación:**<br>✅ Reporte generado correctamente: Al descargar un reporte, el archivo se genera en el formato y rango seleccionados<br>✅ Reporte con errores: Si hay un problema en la generación, se muestra un error y se sugiere reintentar<br>✅ Formato no soportado: Si se elige un formato no válido, el sistema bloquea la descarga y alerta<br>✅ Reporte personalizado: El reporte descargado refleja los filtros personalizados aplicados por el usuario | Should Have | 5 | Backlog | Post-MVP |
| **IOC-015** | 🎯 Story | Exportar datos para análisis avanzados | **Como** supervisor-analista, **quiero** exportar datos para análisis avanzados **para** personalizar informes según necesidades específicas.<br><br>**Criterios de Aceptación:**<br>✅ Exportación exitosa: Al ejecutar una exportación, el archivo se genera en el formato y con los campos solicitados<br>✅ Exportación abortada: Si ocurre un error durante la exportación, se muestra un mensaje de error<br>✅ Restricción de datos: Al intentar exportar datos sensibles, el sistema previene la acción y muestra un aviso<br>✅ Notificación post-exportación: Al completarse la exportación, el usuario recibe una confirmación de éxito | Should Have | 5 | Backlog | Post-MVP |
| **IOC-016** | 🎯 Story | Definir filtros personalizados | **Como** supervisor-analista, **quiero** definir filtros personalizados y consultas complejas **para** profundizar en el análisis para obtener insights.<br><br>**Criterios de Aceptación:**<br>✅ Filtros aplicados exitosamente: Al aplicar un filtro, los datos reflejan los criterios correctamente<br>✅ Consulta no válida: Si una consulta está mal estructurada, se muestra un error con guías para corregirla<br>✅ Guardar filtros personalizados: El sistema permite guardar un conjunto de filtros para uso futuro<br>✅ Eliminar filtros: El sistema permite eliminar filtros guardados y actualiza el estado | Could Have | 5 | Backlog | Post-MVP |
| **IOC-017** | 🎯 Story | Participar en pruebas de usabilidad | **Como** supervisor-analista, **quiero** participar en pruebas de usabilidad **para** asegurar una experiencia intuitiva y eficaz.<br><br>**Criterios de Aceptación:**<br>✅ Prueba con usuarios reales: El feedback recogido durante la prueba es documentado correctamente<br>✅ Identificación de bugs: Los errores encontrados son replicados, documentados y asignados para corrección<br>✅ Mejoras sugeridas: Las sugerencias de los usuarios son recogidas y priorizadas para futuras mejoras<br>✅ Prueba finalizada: Al concluir la prueba, se entrega un documento de resultados para su aprobación | Could Have | 3 | Backlog | Post-MVP |
| **IOC-018** | 🎯 Story | Monitorizar indicadores en tiempo real | **Como** supervisor-analista, **quiero** monitorizar indicadores clave en tiempo real **para** detectar desviaciones y tomar decisiones rápidas.<br><br>**Criterios de Aceptación:**<br>✅ Indicadores actualizados: Los KPIs en el panel reflejan el estado de los datos en tiempo real<br>✅ Alerta automática: Cuando un KPI supera su umbral, el sistema emite una alerta inmediata<br>✅ Dashboard responsive: El dashboard se visualiza de forma adecuada y rápida en dispositivos móviles<br>✅ Reporte de desviaciones: Se puede generar y descargar un informe con el historial y análisis de desviaciones | Should Have | 8 | Backlog | Post-MVP |

---

## 📈 MÉTRICAS DEL BACKLOG

### Distribución por Sprint
- **Sprint 1 (4 semanas - 8 Sept a 4 Oct):** 5 items | 41 Story Points | **Fundación y Visualización**
- **Sprint 2 (3 semanas - 6 Oct a 25 Oct):** 2 items | 21 Story Points | **Interactividad Analítica**
- **Sprint 3 (4 semanas - 27 Oct a 22 Nov):** 4 items | 42 Story Points | **Gobernanza Completa**
- **Post-MVP:** 12 items | 77 Story Points | **Funcionalidades Avanzadas**

### Distribución por Épica
- **EP-01 (Ingesta y Validación de Datos):** 2 items | 21 Story Points (IOC-001, IOC-002)
- **EP-02 (Contenido Analítico y Visualizaciones):** 10 items | 85 Story Points (IOC-006, IOC-008, IOC-009, IOC-010, IOC-012, IOC-014, IOC-015, IOC-016, IOC-019, IOC-020)
- **EP-03 (Acceso, Seguridad y Gestión de Usuarios):** 11 items | 75 Story Points (IOC-003, IOC-004, IOC-005, IOC-007, IOC-011, IOC-013, IOC-017, IOC-018, IOC-021, IOC-022, IOC-023)

### Distribución por Prioridad (MoSCoW)
- **Must Have:** 11 items (48%) | 104 SP - Todas las historias del MVP Core (Sprint 1-3)
- **Should Have:** 7 items (30%) | 54 SP - Funcionalidades importantes Post-MVP
- **Could Have:** 5 items (22%) | 23 SP - Mejoras opcionales Post-MVP
- **Won't Have:** 0 items (0%) | 0 SP

### Distribución por Tipo
- **🎯 User Story:** 23 items (100%)

### Velocidad Estimada y Riesgo
- **Sprint 1:** 41 SP en 4 semanas = **10.25 SP/semana** (Riesgo: Alto - Sprint fundacional con 5 historias críticas)
- **Sprint 2:** 21 SP en 3 semanas = **7 SP/semana** (Riesgo: Bajo - Solo 2 historias enfocadas)
- **Sprint 3:** 42 SP en 4 semanas = **10.5 SP/semana** (Riesgo: Medio - 4 historias de gobernanza complejas)

> ⚠️ **Alerta de Planificación:** Sprint 1 y Sprint 3 tienen cargas similares (~10 SP/semana). El Sprint 1 tiene más riesgo por ser fundacional con integración de Metabase y Supabase.

### Total del MVP Core
- **Historias MVP:** 11 items
- **Story Points MVP:** 104 SP
- **Duración Total:** 11 semanas (8 Sept - 22 Nov 2025)
- **Velocidad Promedio:** 9.45 SP/semana

---

## 🎯 ROADMAP DE RELEASES

### Release 1.0 (MVP Core) - 22 Nov 2025
**Objetivo:** Sistema funcional con features core end-to-end basado en 11 historias de usuario del MVP

**Funcionalidades Incluidas (Solo MVP Core):**

**Sprint 1 - Fundación y Visualización:**
- ✅ **IOC-021**: Iniciar Sesión en la Plataforma - Login seguro con autenticación
- ✅ **IOC-022**: Cerrar Sesión de Forma Segura - Logout y protección de sesión
- ✅ **IOC-023**: Layout Principal y Rutas Protegidas - Navegación y protección por roles
- ✅ **IOC-001**: Cargar Archivo de Datos de Producción - Validación y carga de CSV
- ✅ **IOC-006**: Visualizar Dashboard de Producción - Dashboard con gráficos y KPIs en tiempo real

**Sprint 2 - Interactividad Analítica:**
- ✅ **IOC-008**: Comparar Desempeño entre Entidades - Comparativas entre turnos y líneas con exportación
- ✅ **IOC-012**: Filtrar Datos del Dashboard - Filtros por línea, periodo y taller

**Sprint 3 - Gobernanza y Administración:**
- ✅ **IOC-002**: Validar datos cargados automáticamente - Validación automática de calidad de datos
- ✅ **IOC-003**: Gestionar Usuarios, Roles y Permisos - Configuración de KPIs y umbrales
- ✅ **IOC-019**: Gestionar Gráficos del Dashboard - CRUD de definiciones de gráficos
- ✅ **IOC-020**: Diseñar Disposición del Dashboard - Editor drag & drop para layout del dashboard

**Criterios de Aceptación del Release:**
- Las 11 historias del MVP Core completadas con DoD
- Suite de tests unitarios con cobertura mínima 70%
- Documentación técnica completa
- Despliegue exitoso en producción y validado por stakeholders

**Total MVP Core:**
- 11 historias de usuario
- 104 Story Points
- 11 semanas de desarrollo (8 Sept - 22 Nov 2025)

---

### FASE 3: Presentación del Proyecto (24 Nov - 6 Dic 2025)
**Objetivo:** Presentación formal del proyecto y entrega final académica

- ✅ Preparación de presentación ejecutiva
- ✅ Documentación final del proyecto
- ✅ Demo funcional del sistema completo con las 11 historias MVP
- ✅ Entrega de código fuente y documentación técnica
- ✅ Presentación ante stakeholders y evaluadores

---

### Release 1.1 (Post-MVP Enhancements) - Enero 2026
**Objetivo:** Funcionalidades avanzadas de alertas, gestión y análisis

**Historias Post-MVP (12 historias - 77 Story Points):**

**Gestión Avanzada de Usuarios:**
- ✅ **IOC-004**: Gestionar usuarios y roles - CRUD completo de usuarios
- ✅ **IOC-011**: Acceder desde dispositivos móviles - Interfaz responsive mobile

**Sistema de Alertas y Notificaciones:**
- ✅ **IOC-005**: Recibir alertas en caso de desviaciones - Notificaciones por umbral
- ✅ **IOC-007**: Recibir alertas automáticas - Sistema de alertas automático con escalación

**Análisis Avanzado:**
- ✅ **IOC-009**: Analizar tendencias históricas - Análisis temporal y proyecciones
- ✅ **IOC-010**: Personalizar dashboards - Vistas personalizadas por usuario

**Reportes y Exportación:**
- ✅ **IOC-013**: Reportar incidencias - Sistema de tickets y seguimiento
- ✅ **IOC-014**: Descargar reportes en PDF o Excel - Exportación en múltiples formatos
- ✅ **IOC-015**: Exportar datos para análisis avanzados - Exportación personalizada de datos
- ✅ **IOC-016**: Definir filtros personalizados - Consultas complejas guardadas

**Calidad y Usabilidad:**
- ✅ **IOC-017**: Participar en pruebas de usabilidad - Programa de testing con usuarios
- ✅ **IOC-018**: Monitorizar indicadores en tiempo real - Dashboard de monitoreo en vivo

**Mejoras Adicionales:**
- ✅ Optimizaciones de performance
- ✅ Analytics de uso de la plataforma
- ✅ Mejoras de UX basadas en feedback de usuarios


---

## 📊 BURNDOWN CHART (Proyectado)

### Sprint 1 (8 Sept - 4 Oct) - Proyección de Story Points
```
Semana 1 (8-13 Sept): 33 SP → 26 SP (IOC-022 completado - 5 SP, IOC-023 completado - 2 SP)
Semana 2 (15-20 Sept): 26 SP → 13 SP (IOC-001 completado - 13 SP)
Semana 3 (22-27 Sept): 13 SP → 6 SP (IOC-014 en progreso - 7 SP)
Semana 4 (29 Sept-4 Oct): 6 SP → 0 SP (IOC-014 completado - 6 SP restantes)
```

**Estado Actual:** IOC-022 y IOC-023 en progreso, IOC-001 y IOC-014 en Ready

### Sprint 2 (6 Oct - 25 Oct) - Proyección de Story Points
```
Semana 1 (6-11 Oct): 42 SP → 26 SP (IOC-015 y IOC-016 completados - 16 SP)
Semana 2 (13-18 Oct): 26 SP → 13 SP (IOC-017 y IOC-018 completados - 13 SP) 
    ⭐ Entrega de avance de proyecto (13-18 Oct)
Semana 3 (20-25 Oct): 13 SP → 0 SP (IOC-019 y IOC-020 completados - 13 SP)
```

**Enfoque:** Funcionalidades de análisis interactivo y personalización del dashboard

### Sprint 3 (27 Oct - 22 Nov) - Proyección de Story Points
```
Semana 1 (27 Oct-1 Nov): 45 SP → 34 SP (IOC-002, IOC-003, IOC-004 completados - 11 SP)
Semana 2 (3-8 Nov): 34 SP → 21 SP (IOC-005, IOC-006, IOC-007 completados - 13 SP)
Semana 3 (10-15 Nov): 21 SP → 5 SP (IOC-008, IOC-010 completados - 13 SP, IOC-009 en progreso - 3 SP)
Semana 4 (17-22 Nov): 5 SP → 0 SP (IOC-009, IOC-011 completados - 5 SP)
    ⭐ Entrega final del proyecto (17-22 Nov)
```

**Enfoque:** Sistema completo de gestión de usuarios, roles y permisos

---

## 🔍 DEFINICIONES Y GLOSARIO

### Roles del Proyecto
- **Administrador:** Usuario con permisos completos para gestionar usuarios, cargar datos y configurar KPIs
- **Gerente:** Usuario que visualiza dashboards ejecutivos, aplica filtros y exporta datos
- **Supervisor-Analista:** Usuario que reporta incidencias y monitorea su resolución
- **Usuario:** Cualquier usuario del sistema con acceso a funcionalidades básicas

### Conceptos Técnicos
- **Story Points (SP):** Unidad de estimación de complejidad relativa (1 SP = ~4-6 horas de trabajo)
- **WIP Limit:** Work In Progress Limit - Límite de tareas simultáneas para evitar sobrecarga
- **DoR:** Definition of Ready - Criterios que debe cumplir una historia para iniciar desarrollo
- **DoD:** Definition of Done - Criterios que debe cumplir una historia para considerarse completada
- **MoSCoW:** Must Have, Should Have, Could Have, Won't Have - Método de priorización

### Abreviaturas
- **CSV:** Comma-Separated Values (archivo de datos separados por comas)
- **KPI:** Key Performance Indicator (Indicador Clave de Desempeño)
- **CRUD:** Create, Read, Update, Delete (operaciones básicas de base de datos)
- **RF:** Requerimiento Funcional
- **NFR:** Non-Functional Requirement (Requerimiento No Funcional)
- **CA:** Criterio de Aceptación

---

## 📝 NOTAS Y DECISIONES

### Equipo del Proyecto
- **Product Owner:** Boris Arriagada
- **Scrum Master:** Jaime Vicencio
- **Development Team:** Boris Arriagada, Jaime Vicencio

### Decisiones Técnicas Clave
1. **Integración Metabase:** Uso de iframes embebidos con autenticación JWT para dashboards
2. **Validación CSV:** Librería `papaparse` para parsing + validación custom backend con esquema definido
3. **Testing:** Vitest para unit tests con cobertura mínima 70%
4. **State Management:** Context API + React Query para data fetching
5. **Autenticación:** Supabase Auth para gestión de usuarios y sesiones

### Alineación con CSV
- **Fuente de Verdad:** Archivo CSV "HISTORIAS DE USUARIO 2.0 - Historias de Usuario.csv"
- **Última Sincronización:** 13 de Octubre, 2025
- **Historias Totales:** 23 (100% alineadas con CSV)
- **Criterios de Aceptación:** Mantenidos exactamente como aparecen en el CSV con referencias a RF y NFR

### Riesgos Identificados
- ⚠️ **Sprint 2 Alta Velocidad:** 14 SP/semana puede ser exigente. Plan de mitigación: buffer de tiempo en IOC-020.
- ⚠️ **Dependencia de Metabase:** Cambios en API de Metabase pueden afectar integración. Mitigación: Abstracción en capa de servicio.
- ⚠️ **Performance CSV:** Archivos CSV muy grandes (>100MB) pueden saturar memoria. Mitigación: Procesamiento en chunks.

### Dependencias Externas
- Backend API para validación de CSV y persistencia de datos
- Metabase instance disponible y configurada (versión 0.47+)
- Supabase para autenticación y base de datos PostgreSQL
- Vercel para deployment frontend
- Servicios de email (SendGrid/AWS SES) para notificaciones Post-MVP

---

**Documento vivo - Actualizar después de cada Sprint Planning, Review y Retrospective**

_Última revisión: 13 de Octubre, 2025 - Product Owner: Boris Arriagada_  
_Scrum Master: Jaime Vicencio_  
_Fuente de verdad: HISTORIAS DE USUARIO 2.0 - Historias de Usuario.csv_
