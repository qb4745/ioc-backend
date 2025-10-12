# Blueprint: Refactorización de Frontend para Sincronización con API de Ingesta (v2.0)

Este documento detalla el plan de acción para refactorizar la vista de Ingesta de Datos del frontend, alineándola con la API asíncrona y robusta del backend. **Versión 2.0 incorpora feedback de QA para mayor robustez y mantenibilidad.**

---
## Archivo: `package.json`
**Resumen de Cambios:**
1.  Se añadirá la dependencia `axios`.
2.  Se añadirán las dependencias de desarrollo para pruebas: `vitest`, `msw` (Mock Service Worker).

**Justificación:**
Se necesita `axios` para la comunicación con la API. `vitest` y `msw` son cruciales para crear pruebas unitarias robustas para el servicio de API, permitiendo simular respuestas del backend y verificar el manejo de errores.

---
## Archivo: `src/types/api.ts` (Nuevo)
**Resumen de Cambios:**
1.  Se definirán los tipos para los DTOs de éxito y error, usando uniones literales para `status`.

**Código a Crear:**
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

export interface UnauthorizedError {
  timestamp: string;
  status: 401;
  error: "Unauthorized";
  message: string;
  path: string;
}

// Estado local para la UI
export interface EtlJob extends EtlJobStatusDto {}
```
**Justificación:**
Usar uniones literales para `JobStatus` proporciona type safety, previniendo errores por strings inválidos. Definir los DTOs de error permite un manejo de errores tipado y predecible.

---
## Archivo: `src/utils/apiError.ts` (Nuevo)
**Resumen de Cambios:**
1.  Se creará un helper para parsear errores de `axios` de forma consistente.

**Código a Crear:**
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
**Justificación:**
Centralizar el parseo de errores en un helper previene la duplicación de lógica `try/catch` y asegura que todos los errores de la API se manejen de forma uniforme en toda la aplicación.

---
## Archivo: `src/services/apiService.ts` (Nuevo)
**Resumen de Cambios:**
1.  Se configurará `axios` con interceptores de petición y respuesta.
2.  El interceptor de petición inyectará el token JWT desde el `AuthProvider`.
3.  El interceptor de respuesta manejará globalmente los errores `401/403`.
4.  Se eliminará la cabecera `Content-Type` manual en la subida de archivos.

**Código a Crear:**
```typescript
import axios from 'axios';
import { useAuth } from '../hooks/useAuth'; // Asumiendo que el hook provee el token

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
});

// Interceptor de Petición: Inyecta el token
apiClient.interceptors.request.use((config) => {
  const { session } = useAuth.getState(); // Acceso al estado de Zustand/Context
  const token = session?.access_token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor de Respuesta: Maneja errores globales
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error) && error.response) {
      if (error.response.status === 401 || error.response.status === 403) {
        // Lógica para forzar logout
        useAuth.getState().signOut(); // Ejemplo
        window.location.href = '/signin';
      }
    }
    return Promise.reject(error);
  }
);

export const startEtlProcess = async (file: File, onUploadProgress: (progress: number) => void): Promise<StartProcessResponse> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await apiClient.post('/api/etl/start-process', formData, {
    onUploadProgress: (progressEvent) => {
      const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
      onUploadProgress(percentCompleted);
    }
  });
  return response.data;
};

export const getJobStatus = async (jobId: string): Promise<EtlJobStatusDto> => {
  const response = await apiClient.get(`/api/etl/jobs/${jobId}/status`);
  return response.data;
};
```
**Justificación:**
Esta implementación es más robusta: obtiene el token de la fuente de verdad (`AuthProvider`), maneja la expiración de sesión de forma global y deja que el navegador establezca el `Content-Type` correcto para `FormData`, lo cual es la mejor práctica.

---
## Archivo: `src/pages/admin/DataIngestionPage.tsx`
**Resumen de Cambios:**
1.  Se usará `useRef` para gestionar un único intervalo de sondeo, evitando fugas de memoria.
2.  Se manejarán los errores de la API a través del nuevo `parseApiError` helper.
3.  Se pasará una función `onUploadProgress` al `DataUploadDropzone` para mostrar el progreso de la subida.

**Código Modificado:**
```typescript
// ...
import { parseApiError } from '../../utils/apiError';

const DataIngestionPage = () => {
  const [jobs, setJobs] = useState<EtlJob[]>([]);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const pollingIntervalRef = useRef<NodeJS.Timeout | null>(null);

  const pollJobs = useCallback(() => {
    // ... (lógica de sondeo que ahora se puede llamar de forma segura)
  }, [jobs]);

  useEffect(() => {
    // Iniciar un único intervalo al montar
    pollingIntervalRef.current = setInterval(pollJobs, 5000);
    // Limpiar el intervalo al desmontar
    return () => {
      if (pollingIntervalRef.current) clearInterval(pollingIntervalRef.current);
    };
  }, [pollJobs]);

  const handleFileSelect = async (file: File) => {
    setIsUploading(true);
    setUploadProgress(0);
    try {
      const response = await startEtlProcess(file, setUploadProgress);
      // ... (añadir job al estado)
    } catch (error) {
      const { status, message, code } = parseApiError(error);
      if (code === 'CONFLICT') {
        toast.error('Este archivo ya ha sido procesado.');
      } else {
        toast.error(`Error (${status}): ${message}`);
      }
    } finally {
      setIsUploading(false);
    }
  };
  // ...
};
```
**Justificación:**
El uso de `useRef` para el intervalo de sondeo previene la creación de múltiples intervalos, solucionando el riesgo de fugas de memoria. El manejo de errores a través del helper centraliza la lógica y la hace más predecible.

---
## Archivo: `src/components/admin/DataUploadDropzone.tsx`
**Resumen de Cambios:**
1.  Se añadirá una validación de tamaño de archivo en el frontend.
2.  Se mostrará una barra de progreso durante la subida.

**Código Modificado:**
```typescript
// ...
interface DataUploadDropzoneProps {
  // ...
  uploadProgress: number;
}

const DataUploadDropzone = ({ onFileSelect, isUploading, uploadProgress }: DataUploadDropzoneProps) => {
  // ...
  const onDrop = useCallback((acceptedFiles, fileRejections) => {
    if (acceptedFiles.length > 0) {
      const file = acceptedFiles[0];
      if (file.size > 50 * 1024 * 1024) { // 50MB
        setRejectionError('El archivo es demasiado grande. El máximo es 50MB.');
        return;
      }
      onFileSelect(file);
    }
    // ...
  }, [onFileSelect]);

  return (
    // ...
    {isUploading && (
      <div className="w-full bg-gray-200 rounded-full h-2.5 dark:bg-gray-700 mt-4">
        <div className="bg-brand-500 h-2.5 rounded-full" style={{ width: `${uploadProgress}%` }}></div>
      </div>
    )}
    // ...
  );
};
```
**Justificación:**
Validar el tamaño del archivo en el frontend (`client-side validation`) proporciona un feedback inmediato al usuario y evita una subida innecesaria que el backend rechazaría, mejorando la UX y ahorrando recursos.


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

