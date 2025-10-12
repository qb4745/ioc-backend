# Blueprint: Refactorización de Frontend para Sincronización con API de Ingesta

Este documento detalla el plan de acción para refactorizar la vista de Ingesta de Datos del frontend, alineándola con la API asíncrona y robusta del backend.

---
## Archivo: `package.json`
**Resumen de Cambios:**
1.  Añadir la dependencia `axios` (si no existe) para gestionar las peticiones HTTP.
2.  (Opcional, recomendado) Añadir `react-query` o similar si se decide gestionar el polling con queries.

**Justificación:**
Se necesita un cliente HTTP robusto para manejar la comunicación con la API, incluyendo la configuración de interceptores para la autenticación JWT. Una librería de queries puede simplificar polling y estados.

---
## Archivo: `src/types/api.ts` (Nuevo o a actualizar)
**Resumen de Cambios:**
1.  Centralizar todas las definiciones de tipos de la API.
2.  Definir interfaces para los DTOs de respuesta y error del backend, con estados como uniones literales.

**Código a Crear/Actualizar:**
```typescript
// DTOs de Éxito
export type EtlStatus = 'INICIADO' | 'EXITO' | 'FALLO';

export interface StartProcessResponse {
  jobId: string;
  fileName: string;
  status: EtlStatus; // "INICIADO"
}

export interface EtlJobStatusDto {
  jobId: string;
  fileName: string;
  status: EtlStatus; // "INICIADO" | "EXITO" | "FALLO"
  details: string | null;
  minDate: string | null; // YYYY-MM-DD
  maxDate: string | null; // YYYY-MM-DD
  createdAt: string | null; // ISO datetime
  finishedAt: string | null; // ISO datetime
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
  error: 'Unauthorized' | string;
  message: string;
  path: string;
}

export interface ForbiddenError {
  timestamp: string;
  status: 403;
  error: 'Forbidden' | string;
  message: string;
  path: string;
}

export type AnyApiError = StandardError | UnauthorizedError | ForbiddenError;

export interface ThrownApiError extends Error {
  status?: number;
  payload?: AnyApiError | unknown;
}

// Estado local para gestionar los trabajos en la UI
export interface EtlJob extends EtlJobStatusDto {
  // Extensión para estados propios de UI si se requieren a futuro
}
```
**Justificación:**
Centralizar los tipos de la API asegura consistencia, previene errores y permite exhaustividad en el manejo de estados y errores.

---
## Archivo: `src/utils/apiError.ts` (Nuevo)
**Resumen de Cambios:**
1.  Crear un helper reutilizable para mapear errores del backend a mensajes de UI y para manejar `401/403`.

**Código a Crear:**
```typescript
import type { AnyApiError, ThrownApiError } from '../types/api';

export function getApiErrorMessage(err: unknown): string {
  const e = err as ThrownApiError | undefined;
  const status = e?.status;
  const payload = (e?.payload as AnyApiError | any) ?? ({} as any);
  const backendMsg: string | undefined = (payload as any)?.message;

  if (status === 401 || status === 403) return backendMsg || 'Sesión expirada o acceso denegado.';
  if (status === 409) return backendMsg || 'Este archivo ya ha sido procesado.';
  if (status === 413) return backendMsg || 'El archivo es demasiado grande.';
  if (status === 429) return backendMsg || 'Demasiadas solicitudes. Intenta más tarde.';
  if (status === 400) return backendMsg || 'Solicitud inválida.';
  if (status && status >= 500) return backendMsg || 'Error interno del servidor.';
  return backendMsg || e?.message || 'Error desconocido';
}
```
**Justificación:**
Evita duplicación de lógica y garantiza un manejo homogéneo de mensajes de error en toda la app.

---
## Archivo: `src/services/api.ts` (Nuevo o a actualizar)
**Resumen de Cambios:**
1.  Crear un servicio centralizado para llamadas a la API con `axios`.
2.  Configurar `baseURL` y un interceptor de request para inyectar JWT desde Supabase y/o `localStorage`.
3.  Añadir interceptor de respuesta para actuar en `401/403` (logout/redirección).
4.  Implementar funciones para iniciar ETL y consultar estado. No fijar manualmente `Content-Type` para `FormData`.

**Código a Crear/Actualizar:**
```typescript
import axios from 'axios';
import { supabase } from '../lib/supabaseClient';
import type { AnyApiError, EtlJobStatusDto, StartProcessResponse, ThrownApiError } from '../types/api';

const api = axios.create({
  baseURL: (import.meta.env.VITE_API_BASE_URL as string | undefined) || 'http://localhost:8080',
});

// Interceptor: inyectar JWT
api.interceptors.request.use(async (config) => {
  try {
    const { data } = await supabase.auth.getSession();
    const token = data.session?.access_token;
    if (token) {
      config.headers = config.headers || {};
      (config.headers as any).Authorization = `Bearer ${token}`;
    }
  } catch {/* noop */}
  // Fallback a localStorage si aplica
  if (!(config.headers as any)?.Authorization) {
    const ls = localStorage.getItem('jwt') || localStorage.getItem('auth_token');
    if (ls) {
      config.headers = config.headers || {};
      (config.headers as any).Authorization = `Bearer ${ls}`;
    }
  }
  (config.headers as any).Accept = 'application/json';
  return config;
});

// Interceptor: respuesta (manejo 401/403)
api.interceptors.response.use(
  (res) => res,
  async (error) => {
    if (axios.isAxiosError(error) && error.response) {
      const status = error.response.status;
      if (status === 401 || status === 403) {
        try { await supabase.auth.signOut(); } catch {/* noop */}
        try { window.location.assign('/signin'); } catch {/* noop */}
      }
      const e: ThrownApiError = new Error((error.response.data as AnyApiError)?.message || error.message);
      e.status = status;
      e.payload = error.response.data as AnyApiError;
      return Promise.reject(e);
    }
    return Promise.reject(error);
  }
);

export async function startEtlProcess(file: File): Promise<StartProcessResponse> {
  const form = new FormData();
  form.append('file', file);
  const res = await api.post<StartProcessResponse>('/api/etl/start-process', form /* no Content-Type manual */);
  return res.data;
}

export async function getJobStatus(jobId: string): Promise<EtlJobStatusDto> {
  const res = await api.get<EtlJobStatusDto>(`/api/etl/jobs/${encodeURIComponent(jobId)}/status`);
  return res.data;
}
```
**Justificación:**
Obtiene el token de la fuente real (Supabase) con fallback, evita errores de `multipart` fijando el boundary manualmente, y centraliza la reacción ante `401/403`.

---
## Archivo: `src/pages/admin/DataIngestionPage.tsx`
**Resumen de Cambios:**
1.  Reemplazar la lógica simulada por llamadas al `api` real.
2.  Gestionar estado como lista de `EtlJob[]` y deshabilitar controles durante subida.
3.  Implementar polling con un único `setInterval` y cancelación en unmount; evitar recrearlo por cambios del array.
4.  (Opcional) Cargar historial inicial si existe endpoint de listado.
5.  Usar el helper `getApiErrorMessage` para mensajes consistentes.

**Código Modificado (fragmento):**
```typescript
// ...existing code...
import { startEtlProcess, getJobStatus } from '../../services/api';
import type { EtlJob, EtlStatus } from '../../types/api';
import { getApiErrorMessage } from '../../utils/apiError';

const DataIngestionPage = () => {
  const [jobs, setJobs] = useState<EtlJob[]>([]);
  const [isUploading, setIsUploading] = useState(false);
  const pollingRef = useRef<number | null>(null);

  // Polling único
  useEffect(() => {
    if (pollingRef.current) return; // ya existe
    pollingRef.current = window.setInterval(async () => {
      const inProgress = jobs.filter(j => j.status === 'INICIADO').map(j => j.jobId);
      await Promise.all(inProgress.map(async (id) => {
        try {
          const updated = await getJobStatus(id);
          if (updated.status !== 'INICIADO') {
            setJobs(prev => prev.map(j => j.jobId === id ? { ...j, ...updated } : j));
          }
        } catch {/* silenciar polling errors */}
      }));
    }, 4000);
    return () => { if (pollingRef.current) clearInterval(pollingRef.current); };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleFileSelect = async (file: File) => {
    setIsUploading(true);
    try {
      const resp = await startEtlProcess(file);
      setJobs(prev => [{
        jobId: resp.jobId,
        fileName: resp.fileName,
        status: resp.status as EtlStatus,
        details: null,
        minDate: null,
        maxDate: null,
        createdAt: new Date().toISOString(),
        finishedAt: null,
      }, ...prev]);
      toast.success('Proceso iniciado con éxito.');
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    } finally {
      setIsUploading(false);
    }
  };

  // ...existing code...
}
```
**Justificación:**
Previene fugas de intervalos y estados inconsistentes, y estandariza los mensajes de error. Mantiene la UI reactiva sin recrear múltiples timers.

---
## Archivo: `src/components/admin/UploadHistoryTable.tsx`
**Resumen de Cambios:**
1.  Actualizar props para aceptar `EtlJob[]`.
2.  Mostrar `status` real y mantener botón de detalles si `FALLO`.

**Código Modificado (fragmento):**
```typescript
// ...existing code...
import type { EtlJob } from '../../types/api';

interface UploadHistoryTableProps {
  jobs: EtlJob[];
  onViewDetails: (jobId: string) => void;
}

const UploadHistoryTable = ({ jobs, onViewDetails }: UploadHistoryTableProps) => {
  // ...existing code...
  <TableBody>
    {jobs.map((job) => (
      <TableRow key={job.jobId}>
        <TableCell className="px-4 py-3 truncate">{job.fileName}</TableCell>
        <TableCell className="px-4 py-3">{job.createdAt ? new Date(job.createdAt).toLocaleString() : '-'}</TableCell>
        <TableCell className="px-4 py-3">
          <span className={/* classes según estado */ ''}>
            {job.status}
          </span>
        </TableCell>
        <TableCell className="px-4 py-3">
          {job.status === 'FALLO' && (
            <Button variant="outline" size="sm" onClick={() => onViewDetails(job.jobId)}>
              Ver Detalles
            </Button>
          )}
        </TableCell>
      </TableRow>
    ))}
  </TableBody>
  // ...existing code...
}
```
**Justificación:**
Alinea la tabla con el modelo real de datos del backend.

---
## Consideraciones adicionales
- Validación previa del archivo: tamaño y extensión `.csv` para feedback temprano antes de un posible `413`.
- (Opcional) Historial inicial: si existe `GET /api/etl/jobs` o similar, poblar la tabla al montar.
- Cancelación: abortar peticiones y limpiar intervalos al desmontar.
- Calidad: ejecutar typecheck y linter tras los cambios.

---
## Asunciones
- El contrato de API expone `POST /api/etl/start-process` (202) y `GET /api/etl/jobs/{jobId}/status` (200).
- El JWT proviene de Supabase (`supabase.auth.getSession()`), con fallback a `localStorage` si aplica.
- Los estados del job son `INICIADO | EXITO | FALLO`.

---
## Pasos de verificación (Quality Gates)
- Compilar sin errores de TypeScript: PASS.
- Linter sin errores críticos: PASS.
- Flujo manual: subir CSV válido, ver job en estado `INICIADO`, transicionar a `EXITO`/`FALLO`, ver mensajes de error estandarizados.
