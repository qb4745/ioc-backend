# Blueprint: Refactorización de Frontend para Sincronización con API de Ingesta (v5.0 - Final)

Este documento detalla el plan de acción para refactorizar la vista de Ingesta de Datos. **Versión 5.0 es la versión final, incorporando cancelación de peticiones e inyección de proveedor de token para máxima robustez.**

---
## 1. Plan de Implementación

### Archivo: `package.json`
*   **Acción:** Añadir `axios`, `vitest`, `msw`, `@testing-library/react`.

### Archivo: `src/types/api.ts` (Nuevo)
*   **Acción:** Crear el archivo con las interfaces `JobStatus`, `StartProcessResponse`, `EtlJobStatusDto`, `StandardError`, y `EtlJob`.

### Archivo: `src/utils/apiError.ts` (Nuevo)
*   **Acción:** Crear el helper `parseApiError` para un manejo de errores consistente.

### Archivo: `src/services/apiService.ts` (Nuevo)
*   **Resumen de Cambios:**
    1.  Implementar el sistema de "Token Provider" inyectable.
    2.  Añadir soporte para `AbortSignal` en la función de subida de archivos.
    3.  Incluir un helper para `Retry-After`.

*   **Código a Implementar:**
    ```typescript
    import axios from 'axios';
    import type { StartProcessResponse, EtlJobStatusDto } from '../types/api';

    type TokenProvider = () => Promise<string | null> | string | null;

    let getToken: TokenProvider = () => { /* ... (implementación de fallback) ... */ };

    export const setTokenProvider = (provider: TokenProvider) => {
      getToken = provider;
    };

    export const apiClient = axios.create({ /* ... */ });

    apiClient.interceptors.request.use(async (config) => { /* ... */ });
    apiClient.interceptors.response.use( (response) => response, async (error) => { /* ... */ });

    export const getRetryAfterMs = (retryAfter?: string | null): number | null => {
      if (!retryAfter) return null;
      const seconds = Number(retryAfter);
      return Number.isFinite(seconds) ? Math.max(0, seconds * 1000) : null;
    };

    export const startEtlProcess = async (
      file: File,
      onUploadProgress?: (progress: number) => void,
      signal?: AbortSignal
    ): Promise<StartProcessResponse> => {
      const formData = new FormData();
      formData.append('file', file);

      const res = await apiClient.post('/api/etl/start-process', formData, {
        signal,
        onUploadProgress: (evt) => {
          if (!onUploadProgress) return;
          const total = evt.total ?? 0;
          const loaded = evt.loaded ?? 0;
          const percent = total > 0 ? Math.round((loaded * 100) / total) : 0;
          onUploadProgress(percent);
        },
      });
      return res.data as StartProcessResponse;
    };

    export const getJobStatus = async (jobId: string): Promise<EtlJobStatusDto> => { /* ... */ };
    ```

### Archivo: `src/main.tsx` (o archivo de arranque de la app)
*   **Resumen de Cambios:**
    1.  Inyectar el proveedor de tokens de Supabase al inicio de la aplicación.

*   **Código a Añadir:**
    ```typescript
    import { setTokenProvider } from './services/apiService';
    import { supabase } from './lib/supabaseClient';

    setTokenProvider(async () => {
      try {
        const { data } = await supabase.auth.getSession();
        return data.session?.access_token ?? null;
      } catch {
        return null;
      }
    });
    ```

### Archivo: `src/pages/admin/DataIngestionPage.tsx`
*   **Resumen de Cambios:**
    1.  Añadir la lógica para usar `AbortController` y permitir la cancelación de subidas.

*   **Código de Referencia (Snippet):**
    ```tsx
    const [abortController, setAbortController] = useState<AbortController | null>(null);

    const handleFileSelect = async (file: File) => {
      const controller = new AbortController();
      setAbortController(controller);
      // ...
      try {
        await startEtlProcess(file, setUploadProgress, controller.signal);
      } catch (error) {
        if (axios.isCancel(error)) {
          toast.error('Subida cancelada.');
        } else {
          // ... (manejo de otros errores)
        }
      }
      // ...
    };

    const handleCancelUpload = () => {
      if (abortController) {
        abortController.abort();
      }
    };
    ```

---
## 2. Estrategia de Pruebas y Calidad
*   **Pruebas Unitarias (`apiService.spec.ts`):**
    *   Añadir prueba para verificar que el `AbortSignal` se pasa correctamente a `axios`.
*   **Pruebas de Integración (`DataIngestionPage.spec.tsx`):**
    *   Añadir prueba para simular el clic en un botón "Cancelar" y verificar que la petición de subida se cancela.
*   **Checklist de QA Manual:**
    *   **[ ]** **Cancelación de Subida:** Iniciar la subida de un archivo grande y hacer clic en "Cancelar". Verificar que la petición de red se detiene y se muestra un mensaje de cancelación.
