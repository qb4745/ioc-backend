### **El Prompt Mejorado (v2)**

```markdown
Tu misión es actuar como un Senior Product Manager con un fuerte background en UX, transformando los requerimientos de negocio del proyecto IOC en un diseño de interfaz coherente y accionable para la "Vista de Administrador".

**Fuentes de Verdad (Sources of Truth):**
Tu análisis debe basarse **exclusivamente** en los siguientes artefactos:
1.  `@.gemini/strategy/2_Product_Backlog.md`: Para extraer todas las funcionalidades del rol "Administrador".
2.  `@.gemini/strategy/1_Definicion_Proyecto.md`: Para comprender el propósito estratégico global del módulo de administración.

**Proceso de Trabajo (Tu Razonamiento):**
1.  **Extracción y Agrupación Temática:** Identifica todas las Historias de Usuario (HU) del backlog donde el actor es "Como Administrador". Agrúpalas por temas funcionales (ej. todo lo relacionado con usuarios, todo lo relacionado con gráficos, etc.).
2.  **Síntesis y Diseño de Flujo:** No solo listes las features. Piensa en el flujo de trabajo de un administrador. ¿Cómo se relacionan estas secciones? ¿Cuál es el orden lógico para presentarlas en una interfaz? Tu diseño de navegación debe reflejar este flujo de trabajo.
3.  **Identificación de Oportunidades (Gap Analysis):** Basado en los requerimientos explícitos, identifica y sugiere funcionalidades complementarias de bajo esfuerzo que mejorarían drásticamente la usabilidad (ej. un campo de búsqueda en una tabla de usuarios, un botón para "duplicar" un gráfico).
4.  **Generación del Documento:** Produce un único documento Markdown bien formateado que describa tu diseño propuesto, siguiendo la plantilla exacta que se detalla a continuación.

---
### **PLANTILLA DE SALIDA OBLIGATORIA**

# Diseño de la Vista de Administrador - Plataforma IOC (v2)

## 1. Racional de Diseño (Design Rationale)
[Comienza explicando tus decisiones de diseño de alto nivel. ¿Por qué agrupaste las secciones de la manera en que lo hiciste? ¿Qué principio guía la estructura de navegación? Ej: "El diseño se basa en el principio de separación de responsabilidades, agrupando la 'Gestión de Contenido' (lo que ven los usuarios) de la 'Gestión de Acceso' (quiénes son los usuarios). Esto simplifica el modelo mental del administrador..."]

## 2. Estructura de Navegación Propuesta (Menú Lateral)
[Presenta la navegación con descripciones que expliquen el propósito de cada sección.]
*   **Panel Principal (Dashboard):** Vista de bienvenida con métricas de uso de la plataforma y accesos directos.
*   **Ingesta de Datos:** Herramientas para la carga y monitorización de los datos de producción.
*   **Contenido Analítico:** Módulo para crear y configurar los elementos que consumen los usuarios finales (Dashboards, Gráficos, KPIs).
*   **Acceso y Seguridad:** Módulo para administrar el acceso a la plataforma (Usuarios, Roles y Permisos).
*   **Configuración:** (Sugerencia) Parámetros generales de la aplicación.

---

## 3. Detalle de Secciones y Componentes

### 3.1 Sección: [Nombre de la primera sección, ej: Contenido Analítico]
*   **Propósito:** Centralizar la creación y gestión de todos los elementos visuales que componen la experiencia de BI.
*   **Historias de Usuario Cubiertas:** `IOC-019a`, `IOC-019b`, `IOC-020a`, `IOC-020b`, `IOC-002a`, `IOC-002b`, `IOC-002c`
*   **Funcionalidades y Componentes Clave Sugeridos (Frontend):**
    *   **Sub-sección: Diseño de Dashboards:**
        *   `DashboardLayoutEditor`: Un componente de tipo "lienzo" con drag-and-drop.
        *   `AvailableChartsPanel`: Un panel lateral que lista los gráficos disponibles para arrastrar.
    *   **Sub-sección: Gestión de Gráficos:**
        *   `ChartsDataTable`: Una tabla paginada con opciones de Editar/Eliminar.
        *   `ChartBuilderForm`: Un formulario multi-paso para crear/editar un gráfico, con un componente de previsualización en tiempo real.
    *   **Sub-sección: Gestión de KPIs:**
        *   `KpiSettingsTable`: Una tabla con los KPIs, mostrando un componente `ToggleSwitch` para activar/desactivar alertas y un campo de entrada para el umbral.

### 3.2 Sección: [Nombre de la segunda sección, ej: Acceso y Seguridad]
*   **Propósito:** Administrar de forma segura el ciclo de vida de los usuarios y sus niveles de autorización.
*   **Historias de Usuario Cubiertas:** `IOC-003a` a `IOC-003g`
*   **Funcionalidades y Componentes Clave Sugeridos (Frontend):**
    *   **Sub-sección: Gestión de Usuarios:**
        *   `UsersDataTable`: Una tabla paginada con todos los usuarios.
        *   `UserCreateModal`: Un formulario modal para crear un nuevo usuario.
        *   `UserEditRoleDropdown`: Un componente de selección de rol dentro de la tabla o en un modal de edición.
    *   **Sub-sección: Gestión de Roles y Permisos:**
        *   `RolesTable`: Una tabla simple con los roles y un botón "Editar Permisos".
        *   `PermissionsMatrix`: Un componente que muestra una matriz de checkboxes (permisos vs. rol) para una asignación visual e intuitiva.

*(Continúa este patrón para todas las agrupaciones lógicas que identifiques)*

---

## 4. Oportunidades de Mejora Identificadas (Gap Analysis)
*   **Búsqueda y Filtrado:** Ninguna HU lo pide explícitamente, pero añadir un campo de búsqueda en la parte superior de las tablas de "Usuarios", "Gráficos" y "KPIs" es una mejora de usabilidad crítica y de bajo costo.
*   **Duplicar Gráfico/Dashboard:** Se sugiere añadir una acción "Duplicar" para permitir a los administradores crear nuevas visualizaciones a partir de plantillas existentes, acelerando su flujo de trabajo.
*   **Logs de Actividad:** Para la sección de "Ingesta de Datos", un log detallado de errores por fila en cargas fallidas sería invaluable para la depuración.
```

### Justificación Estratégica
*   **Propósito General**: Esta versión 2.0 eleva a la IA de un simple "recolector de requisitos" a un verdadero **"socio de diseño"**. El artefacto generado es mucho más rico, ya que no solo documenta el "qué", sino que también explica el "porqué" (Racional de Diseño) y anticipa el "cómo" (Componentes Sugeridos).

*   **Decisión de Diseño 1 (Exigir un Racional de Diseño)**: Al forzar a la IA a escribir la sección de "Design Rationale", estamos aplicando directamente el principio de **Justificación Mandatoria**. Esto la obliga a sintetizar la información y a tomar decisiones de diseño explícitas en lugar de simplemente agrupar. El resultado es un diseño más cohesivo y defendible.

*   **Decisión de Diseño 2 (Anticipar la Implementación)**: La adición de "Componentes Clave Sugeridos (Frontend)" es una mejora estratégica. Es un ejemplo de **instrucción "just-in-time"** que prepara el terreno para la siguiente fase. Este prompt de "Modo Planificación" ahora genera un artefacto que sirve de entrada directa y detallada para un futuro prompt de "Modo Implementación", creando un flujo de trabajo mucho más fluido y reduciendo la ambigüedad para el desarrollador.

*   **Decisión de Diseño 3 (Formalizar la Identificación de Oportunidades)**: Al crear una sección dedicada al "Gap Analysis", estamos incentivando a la IA a ir más allá de los requerimientos literales y a aplicar su conocimiento para **agregar valor proactivamente**. Esto transforma a la IA en un colaborador que puede ayudar a mejorar el producto, no solo a construirlo. Es una forma de aprovechar al máximo las capacidades del modelo.
