# Blueprint: Refactorización de Frontend para Sincronización con API de Ingesta (v3.0)

Este documento detalla el plan de acción para refactorizar la vista de Ingesta de Datos y establecer una estrategia de calidad robusta, alineándola con la API asíncrona del backend. **Versión 3.0 incorpora una estrategia de pruebas completa.**

---
## 1. Plan de Implementación

### Archivo: `package.json`
*   **Resumen de Cambios:**
    1.  Se añadirá la dependencia `axios`.
    2.  Se añadirán las dependencias de desarrollo: `vitest`, `msw`, `@testing-library/react`.

*   **Justificación:**
    Se necesita `axios` para la comunicación con la API. `vitest`, `msw` y `Testing Library` son cruciales para crear pruebas unitarias y de integración robustas.

### Archivo: `src/types/api.ts` (Nuevo)
*   **Resumen de Cambios:**
    1.  Se definirán los tipos para los DTOs de éxito y error, usando uniones literales para `status`.

*   **Código a Crear:**
    ```typescript
    // DTOs de Éxito
    export type JobStatus = 'INICIADO' | 'EXITO' | 'FALLO';

    export interface StartProcessResponse {
      jobId: string;
      fileName: string;
      status: 'INICIADO';
    }

    export interface EtlJobStatusDto {
      jobId: string;
      fileName: string;
      status: JobStatus;
      details: string | null;
      minDate: string | null;
      maxDate: string | null;
      createdAt: string | null;
      finishedAt: string | null;
    }

    // DTOs de Error
    export interface StandardError {
      error: string;
      message: string;
      timestamp: string;
    }

    // Estado local para la UI
    export interface EtlJob extends EtlJobStatusDto {}
    ```

### Archivo: `src/utils/apiError.ts` (Nuevo)
*   **Resumen de Cambios:**
    1.  Se creará un helper para parsear errores de `axios` de forma consistente.

*   **Código a Crear:**
    ```typescript
    import axios from 'axios';

    interface ParsedApiError {
      status: number;
      message: string;
      code: string;
    }

    export const parseApiError = (error: unknown): ParsedApiError => {
      if (axios.isAxiosError(error) && error.response) {
        const { status, data } = error.response;
        const message = data?.message || 'Ocurrió un error inesperado.';
        const code = data?.error || 'UNKNOWN_ERROR';
        return { status, message, code };
      }
      return { status: 500, message: 'Error de red o respuesta no válida.', code: 'NETWORK_ERROR' };
    };
    ```

### Archivo: `src/services/apiService.ts` (Nuevo)
*   **Resumen de Cambios:**
    1.  Se configurará `axios` con interceptores de petición y respuesta.
    2.  El interceptor de respuesta manejará globalmente los errores `401/403`.

*   **Código a Crear:**
    ```typescript
    import axios from 'axios';
    import { useAuth } from '../hooks/useAuth';

    const apiClient = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    });

    apiClient.interceptors.request.use((config) => {
      const { session } = useAuth.getState();
      const token = session?.access_token;
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    apiClient.interceptors.response.use(
      (response) => response,
      (error) => {
        if (axios.isAxiosError(error) && error.response) {
          if (error.response.status === 401 || error.response.status === 403) {
            useAuth.getState().signOut();
            window.location.href = '/signin';
          }
        }
        return Promise.reject(error);
      }
    );
    // ... (resto de funciones: startEtlProcess, getJobStatus)
    ```

### Archivo: `src/pages/admin/DataIngestionPage.tsx`
*   **Resumen de Cambios:**
    1.  Se usará `useRef` para gestionar un único intervalo de sondeo.
    2.  Se manejarán los errores de la API a través del nuevo `parseApiError` helper.

*   **Justificación:**
    El uso de `useRef` para el intervalo de sondeo previene la creación de múltiples intervalos, solucionando el riesgo de fugas de memoria.

---
## 2. Estrategia de Pruebas y Calidad

### A. Pruebas Unitarias y de Integración (Vitest + MSW)
*   **Ubicación:** `src/services/tests/apiService.spec.ts`
    *   **Cobertura:**
        *   Verificar que el interceptor de petición añade correctamente el `Authorization header`.
        *   Simular una respuesta `401` y verificar que el interceptor de respuesta llama a la función de `signOut`.
        *   Probar la función `startEtlProcess`, simulando una respuesta `202` y verificando que los datos devueltos son correctos.
        *   Probar `parseApiError` con diferentes payloads de error (`409`, `413`, etc.) y verificar que la salida es la esperada.
*   **Ubicación:** `src/pages/admin/tests/DataIngestionPage.spec.tsx`
    *   **Cobertura:**
        *   Renderizar el componente y simular una subida de archivo exitosa usando MSW.
        *   Verificar que el estado de carga (`isUploading`) se activa y desactiva correctamente.
        *   Verificar que, tras una respuesta exitosa, el nuevo job aparece en la tabla.
        *   Simular una respuesta de sondeo que cambia el estado de 'INICIADO' a 'EXITO' y verificar que la UI se actualiza.

### B. Pruebas End-to-End (E2E) (Manual - Playwright/Cypress Opcional)
*   **Happy Path:**
    1.  Login.
    2.  Navegar a la página de ingesta.
    3.  Subir un archivo CSV válido.
    4.  **Verificar:** Se muestra el progreso, luego un toast de éxito, y la tabla se actualiza con el estado "INICIADO". Tras unos segundos, el estado cambia a "EXITO".
*   **Casos de Error:**
    1.  **Token Vencido:** Simular un token vencido (o eliminarlo manualmente) y realizar una subida. **Verificar:** La aplicación redirige al login.
    2.  **Archivo Duplicado (409):** Subir el mismo archivo dos veces. **Verificar:** Se muestra un toast de error "Este archivo ya ha sido procesado."
    3.  **Archivo Grande (413):** Intentar subir un archivo que exceda el límite del frontend. **Verificar:** El `Dropzone` muestra un mensaje de error y la petición no se envía.

### C. Checklist de QA Manual
*   **[ ]** **Flujo Visual:** El estado de la UI (idle, uploading, success, error) es claro y sigue el sistema de diseño.
*   **[ ]** **Controles Deshabilitados:** Todos los botones y controles interactivos en la página se deshabilitan correctamente durante la subida.
*   **[ ]** **Limpieza del Polling:** Navegar fuera de la página de ingesta mientras un job está en progreso y verificar (en la consola de red) que las peticiones de sondeo se detienen.
*   **[ ]** **Accesibilidad:** Se puede navegar por todo el flujo de subida usando solo el teclado. Los mensajes de error son leídos por lectores de pantalla.
*   **[ ]** **Formato de Fechas:** Las fechas en la tabla de historial se muestran en un formato localizado y legible.

### D. Pruebas de Aceptación de Usuario (UAT)
*   **[ ]** Un usuario no técnico puede completar el flujo de subida de un archivo válido sin necesidad de instrucciones.
*   **[ ]** Los mensajes de error (archivo inválido, duplicado, etc.) son claros y comprensibles para un usuario no técnico.
