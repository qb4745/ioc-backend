# Blueprint: Refactorización de Frontend para Sincronización con API de Ingesta (v4.0)

Este documento detalla el plan de acción para refactorizar la vista de Ingesta de Datos y establecer una estrategia de calidad robusta. **Versión 4.0 incorpora feedback de QA avanzado para máxima robustez, seguridad y mantenibilidad.**

---
## 1. Plan de Implementación

### Archivo: `package.json`
*   **Resumen de Cambios:**
    1.  Se añadirá la dependencia `axios`.
    2.  Se añadirán las dependencias de desarrollo: `vitest`, `msw`, `@testing-library/react`.

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
    1.  Se creará un helper robusto para parsear errores de `axios`.

*   **Código a Crear:**
    ```typescript
    import axios from 'axios';

    export interface ParsedApiError {
      status: number;
      message: string;
      code: string;
    }

    export const parseApiError = (error: unknown): ParsedApiError => {
      if (axios.isAxiosError(error)) {
        const status = error.response?.status ?? 0;
        const data = error.response?.data as any;
        const message =
          data?.message ||
          error.message ||
          'Unexpected error.';
        const code =
          data?.error ||
          (status === 0 ? 'NETWORK_ERROR' : 'UNKNOWN_ERROR');
        return { status, message, code };
      }
      return { status: 0, message: 'Network or unknown error.', code: 'NETWORK_ERROR' };
    };
    ```

### Archivo: `src/services/apiService.ts` (Nuevo)
*   **Resumen de Cambios:**
    1.  Se configurará `axios` con un sistema de "Token Provider" inyectable para desacoplarlo de React.
    2.  Se implementarán interceptores seguros para la inyección de tokens y el manejo global de errores 401/403.

*   **Código a Crear:**
    ```typescript
    import axios from 'axios';
    import type { StartProcessResponse, EtlJobStatusDto } from '../types/api';

    type TokenProvider = () => Promise<string | null> | string | null;

    let getToken: TokenProvider = () => {
      try {
        const raw = localStorage.getItem('supabase.auth.token');
        if (!raw) return null;
        const parsed = JSON.parse(raw);
        return parsed?.currentSession?.access_token ?? null;
      } catch {
        return null;
      }
    };

    export const setTokenProvider = (provider: TokenProvider) => {
      getToken = provider;
    };

    export const apiClient = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    });

    apiClient.interceptors.request.use(async (config) => {
      const token = await getToken();
      if (token) {
        config.headers = config.headers ?? {};
        (config.headers as any).Authorization = `Bearer ${token}`;
      }
      return config;
    });

    apiClient.interceptors.response.use(
      (response) => response,
      async (error) => {
        if (axios.isAxiosError(error) && error.response) {
          const s = error.response.status;
          if (s === 401 || s === 403) {
            // window.location.href = '/signin';
          }
        }
        return Promise.reject(error);
      }
    );

    export const startEtlProcess = async (
      file: File,
      onUploadProgress?: (progress: number) => void
    ): Promise<StartProcessResponse> => {
      const formData = new FormData();
      formData.append('file', file);

      const res = await apiClient.post('/api/etl/start-process', formData, {
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

    export const getJobStatus = async (jobId: string): Promise<EtlJobStatusDto> => {
      const res = await apiClient.get(`/api/etl/jobs/${jobId}/status`);
      return res.data as EtlJobStatusDto;
    };
    ```

### Archivo: `src/pages/admin/DataIngestionPage.tsx`
*   **Resumen de Cambios:**
    1.  Se implementará un `useEffect` robusto para el sondeo, que no cree intervalos múltiples.
    2.  Se usará el `parseApiError` helper para un manejo de errores consistente.

*   **Código de Referencia (Snippets):**
    ```tsx
    // useRef typing para intervalos de navegador
    const pollingIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

    // useEffect que crea un único intervalo y lee el estado de forma segura
    useEffect(() => {
      if (pollingIntervalRef.current) return;
      pollingIntervalRef.current = setInterval(() => {
        // Lógica de sondeo aquí, leyendo el estado más reciente
      }, 5000);
      return () => {
        if (pollingIntervalRef.current) clearInterval(pollingIntervalRef.current);
      };
    }, []);
    ```

### Archivo: `src/components/admin/DataUploadDropzone.tsx`
*   **Resumen de Cambios:**
    1.  Se añadirá validación de tamaño de archivo en el frontend.
    2.  Se mostrará una barra de progreso durante la subida.

*   **Código de Referencia (Snippet):**
    ```tsx
    // ...
    {isUploading && (
      <div className="w-full bg-gray-200 rounded-full h-2.5 dark:bg-gray-700 mt-4">
        <div className="bg-brand-500 h-2.5 rounded-full" style={{ width: `${uploadProgress}%` }}></div>
      </div>
    )}
    // ...
    ```
---
## 2. Estrategia de Pruebas y Calidad

### A. Pruebas Unitarias y de Integración (Vitest + MSW)
*   **`src/services/tests/apiService.spec.ts`:**
    *   Probar que el interceptor de petición añade el `Authorization header`.
    *   Probar que el interceptor de respuesta llama a `signOut` en `401`.
    *   Probar `startEtlProcess` y `getJobStatus` con respuestas mockeadas.
*   **`src/pages/admin/tests/DataIngestionPage.spec.tsx`:**
    *   Probar el flujo de subida completo, verificando los cambios de estado (carga, éxito, error) y la actualización de la tabla.

### B. Pruebas End-to-End (E2E) y Manuales
*   **Happy Path:** Verificar el flujo completo de subida exitosa.
*   **Casos de Error:** Verificar el comportamiento ante `401/403`, `409` (duplicado) y `413` (archivo grande).
*   **Checklist de QA Manual:**
    *   **[ ]** Flujo visual de estados (idle, uploading, success, error).
    *   **[ ]** Controles deshabilitados durante la subida.
    *   **[ ]** El sondeo se detiene al desmontar el componente.
    *   **[ ]** Accesibilidad (teclado y lector de pantalla).
