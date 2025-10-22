### **Checklist de Cobertura de Historias de Usuario del MVP Core (Actualizado)**

| ID | Enunciado de la Historia de Usuario | Sprint 1: Fundación y Visualización | Sprint 2: Interactividad y Filtros | Sprint 3: Gobernanza y Administración |
| :---- | :---- | :---- | :---- | :---- |
| **IOC-001** | Administrador puede cargar y validar archivos de producción. | ✅ |  |  |
| **IOC-006** | Gerente puede visualizar un dashboard con KPIs actualizados. | ✅ |  |  |
| **IOC-021** | Usuario puede iniciar sesión en la plataforma. | ✅ |  |  |
| **IOC-022** | Usuario puede cerrar sesión de forma segura. | ✅ |  |  |
| **IOC-023** | El sistema tiene un Layout y Rutas Protegidas. | ✅ |  |  |
| **IOC-008** | Gerente puede comparar el desempeño entre entidades. |  | ✅ |  |
| **IOC-012** | Supervisor-Analista puede filtrar datos en el dashboard. |  | ✅ |  |
| **IOC-002** | Administrador puede validar datos cargados automáticamente. |  |  | ✅ |
| **IOC-003** | Administrador puede gestionar usuarios, roles y permisos. |  |  | ✅ |
| **IOC-019** | Administrador puede crear y gestionar gráficos. |  |  | ✅ |
| **IOC-020** | Administrador puede diseñar la disposición del dashboard. |  |  | ✅ |

---

### **Historias de Usuario Fuera del MVP Core (Post-MVP)**

| ID | Enunciado de la Historia de Usuario | Estado |
| :---- | :---- | :---- |
| **IOC-004** | Administrador puede gestionar usuarios y roles | Post-MVP |
| **IOC-005** | Gerente puede recibir alertas en caso de desviaciones | Post-MVP |
| **IOC-007** | Gerente puede recibir alertas automáticas | Post-MVP |
| **IOC-009** | Gerente puede analizar tendencias históricas | Post-MVP |
| **IOC-010** | Gerente puede personalizar dashboards | Post-MVP |
| **IOC-011** | Gerente puede acceder desde dispositivos móviles | Post-MVP |
| **IOC-013** | Supervisor-Analista puede reportar incidencias | Post-MVP |
| **IOC-014** | Supervisor-Analista puede descargar reportes en PDF o Excel | Post-MVP |
| **IOC-015** | Supervisor-Analista puede exportar datos para análisis avanzados | Post-MVP |
| **IOC-016** | Supervisor-Analista puede definir filtros personalizados | Post-MVP |
| **IOC-017** | Supervisor-Analista puede participar en pruebas de usabilidad | Post-MVP |
| **IOC-018** | Supervisor-Analista puede monitorizar indicadores en tiempo real | Post-MVP |

---

### **Análisis de la Tabla y Justificación (Actualizado Octubre 2025)**

1. **Cobertura del MVP:** El plan de 3 Sprints cubre **11 historias de usuario** definidas como MVP Core (Sprint 1-3), con **12 historias adicionales** planificadas para Post-MVP.

2. **Lógica Incremental:**
   * **Sprint 1 (5 historias):** Entrega un **ciclo de valor completo de extremo a extremo**. Construye seguridad (login/logout), ingesta de datos (carga CSV) y **primera visualización funcional** del dashboard. El resultado es una aplicación que ya entrega valor visible a gerentes.
   * **Sprint 2 (2 historias):** Enriquece la experiencia analítica. Convierte el dashboard estático en una herramienta interactiva con filtros dinámicos y capacidades de comparación entre entidades.
   * **Sprint 3 (4 historias):** Entrega capacidad de auto-gestión y sostenibilidad. Permite que administradores configuren validaciones automáticas, gestionen usuarios/roles/permisos, y diseñen la estructura de dashboards sin intervención técnica.

**Conclusión:** Esta planificación prioriza la entrega temprana de valor tangible (dashboard funcional en Sprint 1), seguida por mejoras en usabilidad (Sprint 2) y capacidades administrativas (Sprint 3).

---

### **Diseño Detallado de Sprints (Actualizado con CSV)**

#### **Sprint 1: El Ciclo de Valor Completo (Fundación y Visualización)**

**Duración:** **4 Semanas** (8 Septiembre - 4 Octubre 2025)  
**Objetivo del Sprint (Sprint Goal):** Al final de este Sprint, un Administrador podrá iniciar sesión, cargar datos de producción, y un Gerente podrá ver inmediatamente un dashboard con gráficos y KPIs que reflejen esa información.

**Incremento Funcional Demostrable:** Una aplicación web con autenticación completa, un panel de administración para subir archivos CSV validados, y una vista de Dashboard que muestra gráficos y KPIs que leen datos de Supabase en tiempo real.

**Historias de Usuario Comprometidas:**

| ID | Título | Criterios de Aceptación Clave |
|---|---|---|
| **IOC-021** | Iniciar Sesión en la Plataforma | Login exitoso con credenciales correctas, manejo de errores para credenciales incorrectas, redirección post-login |
| **IOC-022** | Cerrar Sesión de Forma Segura | Cierre de sesión exitoso, invalidación de sesión, protección contra acceso con historial |
| **IOC-023** | Layout Principal y Rutas Protegidas | Navegación completa para Admin, navegación limitada para Gerente, protección de rutas por rol |
| **IOC-001** | Cargar Archivo de Datos de Producción | Procesamiento de CSV válido, rechazo de archivos incorrectos, detección de duplicados, manejo de archivos vacíos |
| **IOC-006** | Visualizar Dashboard de Producción | Dashboard carga con gráficos y métricas, aplicación de filtros, manejo de errores, actualización automática |

**Riesgos Identificados:**
- Alta carga de trabajo (5 historias en 4 semanas)
- Dependencia de integración con Metabase para visualizaciones
- Configuración inicial de Supabase y autenticación

---

#### **Sprint 2: Desatando la Interactividad Analítica**

**Duración:** **3 Semanas** (6 Octubre - 25 Octubre 2025)  
**Objetivo del Sprint (Sprint Goal):** Al final de este Sprint, el dashboard estático se transformará en una herramienta de análisis interactiva, permitiendo filtrar datos y realizar comparativas para investigar el rendimiento.

**Incremento Funcional Demostrable:** El dashboard cuenta con panel de filtros funcionales (fecha, turno, máquina, línea, taller). Al aplicar filtros, todos los gráficos y KPIs se actualizan dinámicamente. Los gerentes pueden comparar desempeño entre múltiples entidades y exportar resultados.

**Historias de Usuario Comprometidas:**

| ID | Título | Criterios de Aceptación Clave |
|---|---|---|
| **IOC-008** | Comparar Desempeño entre Entidades | Comparativa generada correctamente, manejo de datos insuficientes, exportación a PDF/Excel, actualización dinámica |
| **IOC-012** | Filtrar Datos del Dashboard | Aplicación de filtros por línea/periodo/taller, mensaje cuando no hay datos, validación de filtros inválidos, reset de filtros |

**Riesgos Identificados:**
- Complejidad en la sincronización de múltiples filtros
- Performance con grandes volúmenes de datos
- Integración con Metabase para filtros dinámicos

---

#### **Sprint 3: El Poder de la Gobernanza**

**Duración:** **4 Semanas** (27 Octubre - 22 Noviembre 2025)  
**Objetivo del Sprint (Sprint Goal):** Al final de este Sprint, el Administrador tendrá el control total sobre validación de datos, gestión de usuarios/roles, y configuración de visualizaciones, completando todas las funcionalidades del MVP.

**Incremento Funcional Demostrable:** Un panel de administración completo donde el Administrador puede configurar validaciones automáticas de datos, gestionar usuarios y permisos, crear/editar gráficos, y diseñar la disposición del dashboard que verán los usuarios finales.

**Historias de Usuario Comprometidas:**

| ID | Título | Criterios de Aceptación Clave |
|---|---|---|
| **IOC-002** | Validar datos cargados automáticamente | Aceptación de datos válidos, rechazo de datos erróneos con detalle, detección de duplicados, alerta para validación manual |
| **IOC-003** | Gestionar Usuarios, Roles y Permisos | Configuración de parámetros de KPIs, validación de rangos, activación/desactivación de alertas |
| **IOC-019** | Gestionar Gráficos del Dashboard | Creación de gráficos con validación, modificación y eliminación con actualización de lista |
| **IOC-020** | Diseñar Disposición del Dashboard | Acceso a vista de diseño, drag & drop de componentes, guardado de disposición como vista por defecto |

**Riesgos Identificados:**
- Complejidad en el sistema de permisos granulares
- Integración bidireccional con Metabase para gestión de gráficos
- Testing exhaustivo de validaciones automáticas

---

### **Roadmap Post-MVP (Release 1.1 - Enero 2026)**

**Objetivo:** Completar funcionalidades avanzadas de alertas, análisis y gestión

**Historias Planificadas:**
- **IOC-004**: Gestión completa de usuarios y roles con interfaz CRUD
- **IOC-005, IOC-007**: Sistema completo de alertas y notificaciones automáticas
- **IOC-009**: Análisis de tendencias históricas con comparativas temporales
- **IOC-010**: Personalización avanzada de dashboards por usuario
- **IOC-011**: Optimización para dispositivos móviles
- **IOC-013**: Sistema de reporte de incidencias
- **IOC-014, IOC-015**: Exportación avanzada en múltiples formatos
- **IOC-016**: Filtros personalizados y consultas complejas
- **IOC-017**: Programa de pruebas de usabilidad
- **IOC-018**: Monitoreo de indicadores en tiempo real

---

**Última Actualización:** 13 de Octubre, 2025  
**Fuente de Verdad:** `.gemini/evidencias/HISTORIAS_DE_USUARIO_2.csv`  
**Product Owner:** Boris Arriagada  
**Scrum Master:** Jaime Vicencio
