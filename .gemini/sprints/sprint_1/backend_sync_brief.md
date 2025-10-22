# Backend Sync Brief (2025-09-19)

## 1. Metadatos del Documento
*   **Generado el:** 2025-09-19 12:00:00 UTC
*   **Fuentes Analizadas:**
    *   `01-FTV-panel-principal.md`
    *   `02-FTV-ingesta-de-datos.md`
    *   `03-FTV-contenido-analitico.md`
    *   `04-FTV-acceso-y-seguridad.md`

## 2. Resumen Ejecutivo
Este documento especifica los requisitos de la API consolidados desde la perspectiva del Frontend. Sirve como un contrato unificado para la implementación de los endpoints necesarios para las funcionalidades de administración.

## 3. Política de Seguridad Global
*   **Mecanismo:** El Frontend enviará un **JWT** en la cabecera `Authorization: Bearer <token>` en todas las peticiones a los endpoints listados a continuación. El Backend debe proteger todos estos endpoints, asegurando que solo usuarios con el rol de "Administrador" puedan acceder.

---

## 4. API Endpoints Requeridos

### Endpoint: `GET /api/admin/dashboard/summary`
*   **Propósito:** Obtener un resumen de métricas y datos de gráficos para poblar el Panel Principal de Administración.
*   **FTVs de Origen:** `01-FTV-panel-principal.md`
*   **Contrato de Petición (Request Body/Params):**
    ```typescript
    // N/A
    ```
*   **Contrato de Respuesta (Response Body - Consolidado):**
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

### Endpoint: `POST /api/admin/data/upload`
*   **Propósito:** Subir un archivo CSV de producción para su validación e ingesta.
*   **FTVs de Origen:** `02-FTV-ingesta-de-datos.md`
*   **Contrato de Petición (Request Body/Params):**
    ```typescript
    // FormData con una clave "file" que contiene el archivo CSV.
    ```
*   **Contrato de Respuesta (Response Body - Consolidado):**
    ```typescript
    interface UploadResponse {
      success: boolean;
      message: string;
      uploadId?: string; // ID para consultar errores si success es false
      errors?: string[]; // Errores genéricos si no hay uploadId
    }
    ```

### Endpoint: `GET /api/admin/data/history`
*   **Propósito:** Obtener una lista paginada del historial de archivos cargados.
*   **FTVs de Origen:** `02-FTV-ingesta-de-datos.md`
*   **Contrato de Petición (Request Body/Params):**
    ```typescript
    // Query Params: ?page=1&limit=10
    ```
*   **Contrato de Respuesta (Response Body - Consolidado):**
    ```typescript
    interface UploadHistoryResponse {
      data: {
        id: string;
        fileName: string;
        uploadDate: string; // ISO 8601
        status: 'Éxito' | 'Fallo';
        user: string;
        errorCount: number;
      }[];
      totalPages: number;
      currentPage: number;
    }
    ```

### Endpoint: `GET /api/admin/data/history/{uploadId}/errors`
*   **Propósito:** Obtener los detalles de los errores de validación para una carga fallida específica.
*   **FTVs de Origen:** `02-FTV-ingesta-de-datos.md`
*   **Contrato de Petición (Request Body/Params):**
    ```typescript
    // Path Param: uploadId (string)
    ```
*   **Contrato de Respuesta (Response Body - Consolidado):**
    ```typescript
    interface ErrorLogResponse {
      errors: string[];
    }
    ```

### Endpoint: `GET /api/admin/users`
*   **Propósito:** Obtener una lista paginada y con capacidad de búsqueda de todos los usuarios del sistema.
*   **FTVs de Origen:** `04-FTV-acceso-y-seguridad.md`
*   **Contrato de Petición (Request Body/Params):**
    ```typescript
    // Query Params: ?page=1&limit=10&search=query
    ```
*   **Contrato de Respuesta (Response Body - Consolidado):**
    ```typescript
    interface UserListResponse {
      data: {
        id: string;
        firstName: string;
        lastName: string;
        email: string;
        role: string;
      }[];
      totalPages: number;
      currentPage: number;
    }
    ```

### Endpoint: `PUT /api/admin/users/{userId}/role`
*   **Propósito:** Actualizar el rol de un usuario específico.
*   **FTVs de Origen:** `04-FTV-acceso-y-seguridad.md`
*   **Contrato de Petición (Request Body/Params):**
    ```typescript
    // Path Param: userId (string)
    interface UpdateUserRoleRequest {
      newRole: string;
    }
    ```
*   **Contrato de Respuesta (Response Body - Consolidado):**
    ```typescript
    // Devuelve el objeto de usuario actualizado.
    interface UserData {
      id: string;
      firstName: string;
      lastName: string;
      email: string;
      role: string;
    }
    ```

---

## 5. Resumen y Próximos Pasos
*   El equipo de Frontend utilizará estos contratos para desarrollar contra datos `mock`.
*   Se solicita al equipo de Backend que implemente estos endpoints y notifique cuando estén listos para la integración.
