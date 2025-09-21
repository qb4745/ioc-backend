## Metadatos de la Ficha Técnica
*   **ID:** `FTV-2/4`
*   **Vista:** `Ingesta de Datos`
---

# Ficha Técnica de Vista: Ingesta de Datos (v1.4)

## 1. Misión y Flujo de Usuario
*   **Misión de la Vista:** Permitir al administrador cargar archivos de producción CSV de manera segura, proporcionando feedback claro e inmediato sobre el resultado del proceso de validación e ingesta.
*   **Flujo de Usuario Clave:**
    1.  El administrador navega a la sección "Ingesta de Datos" (`DataIngestionPage`).
    2.  Arrastra un archivo CSV al componente `DataUploadDropzone`.
    3.  El sistema muestra el estado de carga.
    4.  Al finalizar, se muestra un `Alert` de éxito o error. Si hay error, el `UploadHistoryTable` muestra la fila fallida con un botón para ver detalles.
    5.  El administrador hace clic en "Ver Detalles" para abrir el `ErrorLogModal`.

## 2. Arquitectura de Componentes
Esta vista se renderiza dentro del `AppLayout` y se compone de la siguiente jerarquía:

### A. Componentes Reutilizados
*   **Del Proyecto (`@src/`):** `PageBreadcrumb`, `Alert`, `Table`, `Modal`, `Button`, `FileInput`.

### B. Nuevos Componentes a Crear
#### B.1. Componente de Página (Orquestador)
*   **Nombre:** `DataIngestionPage.tsx`
*   **Responsabilidad:** Orquestar la vista, gestionar el estado de carga/error del archivo, la respuesta de la API y los datos del historial.

#### B.2. Componentes de UI (Reutilizables)
*   **Nombre:** `DataUploadDropzone.tsx`
*   **Responsabilidad:** Gestionar la selección de archivos y mostrar el estado visual correspondiente.
*   **Interfaz de Props (Props Interface):**
    ```typescript
    interface DataUploadDropzoneProps {
      onFileSelect: (file: File) => void;
      isUploading: boolean;
      "data-testid"?: string;
    }
    ```
*   **Nombre:** `UploadHistoryTable.tsx`
*   **Responsabilidad:** Mostrar una tabla paginada con el historial de cargas.
*   **Interfaz de Props (Props Interface):**
    ```typescript
    interface UploadHistoryTableProps {
      uploads: UploadRecord[];
      onViewDetails: (uploadId: string) => void;
      "data-testid"?: string;
    }
    ```
*   **Nombre:** `ErrorLogModal.tsx`
*   **Responsabilidad:** Mostrar en un modal una lista detallada de los errores de validación.
*   **Interfaz de Props (Props Interface):**
    ```typescript
    interface ErrorLogModalProps {
      isOpen: boolean;
      onClose: () => void;
      errors: string[];
      "data-testid"?: string;
    }
    ```

## 3. Estrategia de Estado y Efectos Secundarios
*   **Gestión de Estado:** El estado principal (`uploading`, `error`, `history`, `selectedFile`) se gestionará en `DataIngestionPage.tsx` con `useState`.
*   **Efectos Secundarios (`useEffect`):** Un `useEffect` se usará para obtener el historial de cargas al montar la página. La función de carga de archivos manejará la lógica asíncrona y actualizará los estados de `uploading` y `error`.

## 4. Copy Aprobado por Marca (Ejemplos)
*   **Título Principal de la Vista:** "Centro de Ingesta de Datos de Producción"
*   **Texto del Botón Principal:** "Seleccionar Archivo"
*   **Mensaje de Éxito:** "Archivo procesado con éxito. Los datos han sido integrados con la calidad y confianza de siempre."
*   **Título del Modal de Error:** "Detalle de Errores en la Carga"

## 5. Manejo de Casos Límite y Estados Vacíos
*   **Estado de Carga:** Mientras `uploading` es `true`, el `DataUploadDropzone` debe mostrar un estado de carga (ej. un `Spinner`) y el botón de carga debe estar deshabilitado.
*   **Estado de Error de Red:** Si la subida del archivo falla por un problema de red, se debe mostrar un `Alert` de tipo `error` con el mensaje: "Error de conexión. No se pudo cargar el archivo."
*   **Estado Vacío (Sin Historial):** Si no hay registros de cargas previas, el `UploadHistoryTable` debe mostrar un estado vacío con un mensaje como: "Aún no se han realizado cargas de datos. Utilice el panel superior para cargar su primer archivo."
*   **Archivo Inválido (Frontend):** El `DataUploadDropzone` debe validar la extensión del archivo y mostrar un mensaje de error si no es `.csv` antes de intentar subirlo.

## 6. Dependencias de Datos y Contratos API
*   **Endpoints Consumidos:**
    *   `POST /api/admin/data/upload`: Para subir el archivo CSV.
    *   `GET /api/admin/data/history`: Para obtener el historial de cargas.
    *   `GET /api/admin/data/history/{uploadId}/errors`: Para obtener los errores de una carga específica.
*   **Contrato de Datos (Tipos Clave):**
    ```typescript
    // Para el historial de cargas
    type UploadRecord = {
      id: string;
      fileName: string;
      uploadDate: string; // ISO 8601
      status: 'Éxito' | 'Fallo';
      user: string;
      errorCount: number;
    };

    // Para la respuesta del endpoint de subida
    interface UploadResponse {
      success: boolean;
      message: string;
      errors?: string[];
    }
    ```

## 7. Criterios de Aceptación Técnicos (CAT)
*   **CAT-1 (Validación Frontend):** Al seleccionar un archivo, si la extensión no es `.csv`, la subida debe bloquearse y mostrarse un error.
*   **CAT-2 (Carga):** Durante la subida, el `Dropzone` debe mostrar un `Spinner` y el botón de subida debe estar en estado `disabled`.
*   **CAT-3 (Éxito de Subida):** Con una respuesta 200 OK del backend, se debe mostrar un `Alert` de éxito y la tabla de historial debe actualizarse.
*   **CAT-4 (Error de Subida):** Con una respuesta de error del backend (ej. 400 por validación), se debe mostrar un `Alert` de error con el mensaje de la API.
*   **CAT-5 (Historial Vacío):** Si la API de historial devuelve un array vacío, la tabla debe renderizar su estado vacío definido.
