## Metadatos de la Ficha Técnica
*   **ID:** `FTV-3/4`
*   **Vista:** `Contenido Analítico`
---

# Ficha Técnica de Vista: Contenido Analítico (v1.4)

## 1. Misión y Flujo de Usuario
*   **Misión de la Vista:** Empoderar al administrador con herramientas para crear, configurar y organizar los artefactos de BI (Dashboards, Gráficos, KPIs) que serán consumidos por los usuarios finales.
*   **Flujo de Usuario Clave:**
    1.  El administrador navega a "Contenido Analítico" (`AnalyticsContentPage`).
    2.  Selecciona la pestaña "Gestión de Gráficos" y abre el `ChartBuilderModal` para crear un gráfico.
    3.  Navega a "Diseño de Dashboards", selecciona un dashboard y arrastra el nuevo gráfico desde el `AvailableChartsPanel` al `DashboardLayoutEditor`.
    4.  Guarda la disposición.
    5.  Navega a "Gestión de KPIs" y ajusta los umbrales en la `KpiSettingsTable`.

## 2. Arquitectura de Componentes
Esta vista se renderiza dentro del `AppLayout` y utilizará un componente de Pestañas (`Tabs`) para organizar sus tres sub-secciones.

### A. Componentes Reutilizados
*   **Del Proyecto (`@src/`):** `PageBreadcrumb`, `Table`, `Modal`, `Button`, `Input`, `Switch`, `Dropdown`, `Alert`.
*   **Librerías Externas:** `react-dnd`.

### B. Nuevos Componentes a Crear
#### B.1. Componente de Página (Orquestador)
*   **Nombre:** `AnalyticsContentPage.tsx`
*   **Responsabilidad:** Orquestar las tres sub-vistas, gestionar el estado de la pestaña activa y los datos compartidos (ej. lista de gráficos).

#### B.2. Componentes de UI (Reutilizables)
*   **Nombre:** `DashboardLayoutEditor.tsx`
*   **Responsabilidad:** Proveer un lienzo interactivo donde se pueden arrastrar, soltar y redimensionar componentes de gráficos.
*   **Interfaz de Props (Props Interface):**
    ```typescript
    interface DashboardLayoutEditorProps {
      dashboardId: string;
      layout: LayoutItem[];
      onLayoutChange: (newLayout: LayoutItem[]) => void;
      "data-testid"?: string;
    }
    ```
*   **Nombre:** `ChartBuilderModal.tsx`
*   **Responsabilidad:** Un formulario modal multi-paso para guiar al administrador en la creación o edición de un gráfico.
*   **Interfaz de Props (Props Interface):**
    ```typescript
    interface ChartBuilderModalProps {
      isOpen: boolean;
      onClose: () => void;
      chartId?: string;
      "data-testid"?: string;
    }
    ```
*   **Nombre:** `KpiSettingsTable.tsx`
*   **Responsabilidad:** Mostrar la lista de KPIs con sus controles para activar alertas y definir umbrales.
*   **Interfaz de Props (Props Interface):**
    ```typescript
    interface KpiSettingsTableProps {
      kpis: KpiData[];
      onUpdateKpi: (kpiId: string, newValues: Partial<KpiData>) => void;
      "data-testid"?: string;
    }
    ```

## 3. Estrategia de Estado y Efectos Secundarios
*   **Gestión de Estado:** El estado de la pestaña activa se gestionará en `AnalyticsContentPage`. Cada sub-sección gestionará su propio estado de datos, carga y error con `useState`.
*   **Efectos Secundarios (`useEffect`):** Cada sub-sección usará `useEffect` para cargar sus datos respectivos de la API al ser activada.

## 4. Copy Aprobado por Marca (Ejemplos)
*   **Título Principal de la Vista:** "Gestión de Contenido Analítico"
*   **Título de Pestaña:** "Diseño de Dashboards", "Gestión de Gráficos", "Configuración de KPIs"
*   **Texto del Botón Principal:** "Crear Nuevo Gráfico"
*   **Mensaje de Éxito:** "La configuración del dashboard ha sido guardada, manteniendo nuestro compromiso con la calidad."

## 5. Manejo de Casos Límite y Estados Vacíos
*   **Estado de Carga:** Mientras los datos de las tablas (`ChartsDataTable`, `KpiSettingsTable`) se están cargando, se mostrarán `SkeletonLoaders` en lugar de las filas.
*   **Estado de Error:** Si la carga de datos para cualquier sub-sección falla, se mostrará un `Alert` de tipo `error` dentro de la pestaña correspondiente.
*   **Estado Vacío (Sin Gráficos/KPIs):** Si no hay gráficos o KPIs creados, la tabla correspondiente mostrará un estado vacío con un mensaje y un botón de acción principal. Ej: "No hay gráficos creados. [Crear tu Primer Gráfico]".
*   **Guardado Fallido:** Si la acción de guardar la disposición del dashboard (`DashboardLayoutEditor`) falla, se debe mostrar una notificación (`Toast` o `Alert`) de error sin que el usuario pierda los cambios realizados en el lienzo.

## 6. Dependencias de Datos y Contratos API
*   **Endpoints Consumidos:**
    *   `GET /api/admin/charts`: Obtiene la lista de todos los gráficos definidos.
    *   `POST /api/admin/charts`: Crea un nuevo gráfico.
    *   `PUT /api/admin/charts/{chartId}`: Actualiza un gráfico existente.
    *   `GET /api/admin/kpis`: Obtiene la lista de KPIs.
    *   `PUT /api/admin/kpis/{kpiId}`: Actualiza un KPI.
    *   `GET /api/admin/dashboards/{dashboardId}`: Obtiene la configuración de un dashboard.
    *   `PUT /api/admin/dashboards/{dashboardId}`: Guarda la disposición de un dashboard.
*   **Contrato de Datos (Tipos Clave):**
    ```typescript
    // Para la tabla de KPIs
    type KpiData = {
      id: string;
      name: string;
      description: string;
      alertsActive: boolean;
      threshold: number;
    };

    // Para la tabla de Gráficos
    type ChartData = {
      id: string;
      title: string;
      type: 'bar' | 'line' | 'pie';
      metric: string;
      dimension: string;
    };

    // Para la disposición del dashboard
    type LayoutItem = {
      chartId: string;
      x: number;
      y: number;
      width: number;
      height: number;
    };
    ```

## 7. Criterios de Aceptación Técnicos (CAT)
*   **CAT-1 (Carga de Pestañas):** Al cambiar de pestaña, la tabla correspondiente debe mostrar un estado de carga (`SkeletonLoader`) hasta recibir los datos.
*   **CAT-2 (Error de Datos):** Si la API para una pestaña falla, se debe mostrar un `Alert` de error dentro del panel de esa pestaña, sin afectar a las demás.
*   **CAT-3 (Estado Vacío):** Si no hay datos para una tabla (ej. sin gráficos), se debe mostrar el estado vacío con el botón de acción correspondiente.
*   **CAT-4 (Persistencia de Layout):** Después de guardar la disposición en `DashboardLayoutEditor`, al recargar la página, la disposición de los gráficos debe ser la misma que se guardó.
*   **CAT-5 (Validación de Formulario):** El `ChartBuilderModal` debe tener validación en el lado del cliente para campos obligatorios antes de permitir el envío.
