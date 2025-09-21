## Metadatos de la Ficha Técnica
*   **ID:** `FTV-1/4`
*   **Vista:** `Panel Principal`
---

# Ficha Técnica de Vista: Panel Principal (v1.4)

## 1. Misión y Flujo de Usuario
*   **Misión de la Vista:** Proporcionar al administrador un resumen visual e inmediato del estado de la plataforma, permitiéndole identificar anomalías y acceder rápidamente a las secciones de gestión más relevantes.
*   **Flujo de Usuario Clave:**
    1.  El administrador inicia sesión y es dirigido a esta vista (`AdminDashboardPage`).
    2.  Visualiza las métricas clave en los componentes `MetricCard`.
    3.  Utiliza los `QuickAccessButton` para navegar a una sección específica.

## 2. Arquitectura de Componentes
Esta vista se renderiza dentro del `AppLayout` y se compone de la siguiente jerarquía:

### A. Componentes Reutilizados
*   **Del Proyecto (`@src/`):** `PageBreadcrumb`, `Button`, `LineChartOne`, `BarChartOne`, `Alert`.

### B. Nuevos Componentes a Crear
#### B.1. Componente de Página (Orquestador)
*   **Nombre:** `AdminDashboardPage.tsx`
*   **Responsabilidad:** Orquestar la vista, gestionar el estado de carga y error, obtener los datos para las métricas y gráficos, y manejar la navegación de los accesos directos.

#### B.2. Componentes de UI (Reutilizables)
*   **Nombre:** `MetricCard.tsx`
*   **Responsabilidad:** Mostrar una métrica individual (ej. "Usuarios Activos") con un título, un valor y un ícono.
*   **Interfaz de Props (Props Interface):**
    ```typescript
    interface MetricCardProps {
      title: string;
      value: string | number;
      icon: React.ReactNode;
      "data-testid"?: string;
    }
    ```
*   **Nombre:** `QuickAccessButton.tsx`
*   **Responsabilidad:** Un botón estilizado con un ícono y texto para los accesos directos.
*   **Interfaz de Props (Props Interface):**
    ```typescript
    interface QuickAccessButtonProps {
      title: string;
      icon: React.ReactNode;
      onClick: () => void;
      "data-testid"?: string;
    }
    ```

## 3. Estrategia de Estado y Efectos Secundarios
*   **Gestión de Estado:** El estado se gestionará en `AdminDashboardPage.tsx` usando `useState` para `metrics`, `chartData`, `loading` y `error`.
*   **Efectos Secundarios (`useEffect`):** Un `useEffect` se disparará al montar el componente para obtener todos los datos del dashboard desde la API del backend, actualizando los estados de `loading` y `error` según corresponda.

## 4. Copy Aprobado por Marca (Ejemplos)
*   **Título Principal de la Vista:** "Panel Principal de Administración"
*   **Texto del Botón Principal:** "Cargar Nuevo Archivo de Producción"
*   **Título de Métrica:** "Estado de la Última Carga"

## 5. Manejo de Casos Límite y Estados Vacíos
*   **Estado de Carga:** Mientras `loading` es `true`, los componentes `MetricCard` y los gráficos mostrarán un componente `SkeletonLoader` para indicar actividad.
*   **Estado de Error:** Si la llamada a la API falla, se mostrará un componente `Alert` de tipo `error` en la parte superior de la vista con un mensaje como: "No se pudo cargar la información del panel. Por favor, intente de nuevo."
*   **Estado Vacío (Sin Datos):** Si la API devuelve datos vacíos (ej. no hay usuarios, no hay cargas), los `MetricCard` mostrarán "0" o "N/A". Los gráficos mostrarán un estado vacío con el mensaje "No hay suficientes datos para mostrar este gráfico".

## 6. Dependencias de Datos y Contratos API
*   **Endpoint Consumido:** `GET /api/admin/dashboard/summary`
*   **Contrato de Datos (Respuesta Esperada):**
    ```typescript
    interface DashboardSummary {
      metrics: {
        activeUsers: number;
        lastUploadStatus: 'Éxito' | 'Fallo' | 'N/A';
        kpiAlerts: number;
      };
      charts: {
        userActivity: { date: string; count: number; }[];
        uploadHistory: { month: string; count: number; }[];
      };
    }
    ```

## 7. Criterios de Aceptación Técnicos (CAT)
*   **CAT-1 (Carga):** Al entrar en la vista, se debe mostrar un estado de carga (`SkeletonLoader`) mientras se esperan los datos de la API.
*   **CAT-2 (Éxito):** Con una respuesta 200 OK de la API, la vista debe renderizar los `MetricCard` y los gráficos con los datos recibidos.
*   **CAT-3 (Error):** Con una respuesta de error de la API (ej. 500), la vista debe ocultar los loaders y mostrar un `Alert` de error.
*   **CAT-4 (Vacío):** Si la API responde 200 OK pero con datos vacíos (ej. `activeUsers: 0`), los componentes deben renderizar su estado vacío definido.
