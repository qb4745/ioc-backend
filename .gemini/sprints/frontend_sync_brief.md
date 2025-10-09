# Frontend Sync Brief: API del Backend (v2025-09-21)

## 1. Resumen Ejecutivo
Este documento es la especificación oficial de la API del backend, generada desde el código fuente (controladores y DTOs). Sirve como contrato de integración para que el equipo de Frontend consuma los endpoints de forma segura y consistente.

## 2. Política de Autenticación
- Mecanismo: Todos los endpoints protegidos requieren un JWT en la cabecera `Authorization: Bearer <token>`.
- Errores: Las peticiones sin token válido a endpoints protegidos devolverán `401 Unauthorized` o `403 Forbidden`.

### 2.1 Esquemas de Error (payloads estandarizados)
- Errores generales (validez de archivo, conflicto de job, argumentos inválidos, tamaño de archivo, fallos internos)
  - Fuente: GlobalExceptionHandler (com.cambiaso.ioc.exception)
  ```typescript
  // Usado en: 400, 409, 413, 500
  interface StandardError {
    error: string;     // "VALIDATION_ERROR" | "INVALID_ARGUMENT" | "CONFLICT" | "FILE_TOO_LARGE" | "INTERNAL_ERROR"
    message: string;   // detalle humano-legible
    timestamp: string; // ISO-8601 con offset (OffsetDateTime)
  }
  ```
- Errores de seguridad (autenticación/autorización)
  - Fuente: SecurityExceptionHandler (com.cambiaso.ioc.security)
  ```typescript
  // 401 Unauthorized
  interface UnauthorizedError {
    timestamp: string; // ISO LocalDateTime
    status: 401;
    error: "Unauthorized";
    message: string;   // p.ej. "Authentication failed: <detalle>"
    path: string;      // ruta solicitada
  }

  // 403 Forbidden
  interface ForbiddenError {
    timestamp: string; // ISO LocalDateTime
    status: 403;
    error: "Forbidden";
    message: string;   // p.ej. "You do not have permission to access this resource."
    path: string;      // ruta solicitada
  }
  ```

---

## 3. API Endpoints Disponibles

### Endpoint: [POST] /api/etl/start-process
- Descripción: Inicia un proceso ETL para el archivo CSV subido. Valida, calcula hash para idempotencia, crea el job y dispara el procesamiento asíncrono.
- Protegido: Sí (requiere estar autenticado; `@PreAuthorize("isAuthenticated()")`).
- Consumes: `multipart/form-data` (parte `file` como CSV)
- Produces: `application/json`
- Cabeceras requeridas:
  - `Authorization: Bearer <token>`
- Parámetros de petición:
  - Path Variables: N/A
  - Query Params: N/A
  - Multipart (form-data):
    - `file`: binary (CSV) – requerido. Tamaño máximo permitido 50MB. Extensión esperada `.csv`. Content-Type aceptados: `text/csv`, `application/csv`, `text/plain`.
- Contrato de Petición (Request Body):
  ```typescript
  // N/A (se envía como multipart/form-data con una parte llamada "file")
  ```
- Contrato de Respuesta (202 Accepted):
  ```typescript
  interface StartProcessResponse {
    jobId: string;     // UUID
    fileName: string;  // nombre de archivo original
    status: string;    // "INICIADO"
  }
  ```
- Códigos de estado adicionales:
  - 202 Accepted: Proceso aceptado y disparado en background (respuesta mostrada arriba).
  - 400 Bad Request: Archivo vacío, tamaño > 50MB, extensión inválida o Content-Type inválido. Payload: StandardError.
  - 409 Conflict: El archivo ya fue procesado previamente (idempotencia por hash). Payload: StandardError.
  - 413 Payload Too Large: Límite de tamaño superado. Payload: StandardError (error = "FILE_TOO_LARGE").
  - 429 Too Many Requests: Rate limit.
  - 401/403: Autenticación/autorización inválida. Payload: UnauthorizedError / ForbiddenError.
  - 5xx: Error interno inesperado. Payload: StandardError.
- Rate Limiting:
  - Cabeceras devueltas: `RateLimit-Limit`, `RateLimit-Remaining`, `RateLimit-Reset`, `Retry-After` (RFC 9331).
- Ejemplo curl:
  ```bash
  curl -X POST "http://<host>/api/etl/start-process" \
    -H "Authorization: Bearer <token>" \
    -H "Accept: application/json" \
    -F "file=@/ruta/al/archivo.csv;type=text/csv"
  ```

---

### Endpoint: [GET] /api/etl/jobs/{jobId}/status
- Descripción: Recupera el estado del job ETL.
- Protegido: Sí (requiere estar autenticado; `@PreAuthorize("isAuthenticated()")`).
- Consumes: `*/*`
- Produces: `application/json`
- Cabeceras requeridas:
  - `Authorization: Bearer <token>`
- Parámetros de petición:
  - Path Variables:
    - `{jobId}`: `string` (UUID) – requerido.
  - Query Params: N/A
- Contrato de Petición (Request Body):
  ```typescript
  // N/A
  ```
- Contrato de Respuesta (200 OK):
  ```typescript
  interface EtlJobStatusDto {
    jobId: string;             // UUID
    fileName: string;
    status: string;            // e.g., "INICIADO", "EXITO", "FALLO"
    details: string | null;    // detalles adicionales si aplica
    minDate: string | null;    // LocalDate ISO: YYYY-MM-DD
    maxDate: string | null;    // LocalDate ISO: YYYY-MM-DD
    createdAt: string | null;  // OffsetDateTime ISO: e.g., 2025-09-21T12:34:56Z
    finishedAt: string | null; // OffsetDateTime ISO
  }
  ```
- Códigos de estado adicionales:
  - 404 Not Found: Job no encontrado.
  - 429 Too Many Requests: Rate limit.
  - 401/403: Autenticación/autorización inválida. Payload: UnauthorizedError / ForbiddenError.
- Rate Limiting:
  - Cabeceras devueltas: `RateLimit-Limit`, `RateLimit-Remaining`, `RateLimit-Reset`, `Retry-After` (RFC 9331).
- Ejemplo curl:
  ```bash
  curl -X GET "http://<host>/api/etl/jobs/<job-uuid>/status" \
    -H "Authorization: Bearer <token>" \
    -H "Accept: application/json"
  ```

---

## 4. Referencia de DTOs (TypeScript)
```typescript
// Respuesta del POST /api/etl/start-process (202 Accepted)
export interface StartProcessResponse {
  jobId: string;     // UUID
  fileName: string;  // nombre de archivo original
  status: string;    // "INICIADO"
}

// Respuesta del GET /api/etl/jobs/{jobId}/status (200 OK)
export interface EtlJobStatusDto {
  jobId: string;             // UUID
  fileName: string;
  status: string;            // e.g., "INICIADO", "EXITO", "FALLO"
  details: string | null;    // detalles adicionales si aplica
  minDate: string | null;    // LocalDate ISO: YYYY-MM-DD
  maxDate: string | null;    // LocalDate ISO: YYYY-MM-DD
  createdAt: string | null;  // OffsetDateTime ISO (Z o con offset)
  finishedAt: string | null; // OffsetDateTime ISO
}
```

## 5. Formato y Convenciones
- Fechas como strings ISO-8601:
  - `LocalDate` -> `YYYY-MM-DD`
  - `OffsetDateTime` -> `YYYY-MM-DDTHH:mm:ss[.SSS]Z` o con offset
- Enums -> unir como string literal union (no aplica en los DTOs actuales).
- Nullabilidad: Campos opcionales o nulos en Java se representan como `T | null`.
- Colecciones: `List<T>`/`Set<T>` -> `T[]`.

## 6. Resumen y Próximos Pasos
- Backend: estos endpoints están implementados en el código de producción (EtlController) y protegidos por JWT; el rate limiting aplica a la ruta `/api/etl/**`.
- Frontend: usar este documento como fuente de verdad para la integración. Para subidas, enviar el archivo como `multipart/form-data` en la parte `file`.
## 7. Estado del Frontend y Plan de Integración (IOC-001)

*   **Estado Actual:** ✅ **UI Completada.** La vista de Ingesta de Datos (`DataIngestionPage`) y todos sus componentes asociados (`DataUploadDropzone`, `UploadHistoryTable`, etc.) están construidos y son funcionalmente completos, pero utilizan datos simulados (`mocks`).

*   **Trabajo Pendiente para la Integración:** Para completar la historia de usuario `IOC-001`, se deben realizar los siguientes pasos en el frontend:

    1.  **Crear un Servicio de API (`apiService.ts`):**
        *   **Acción:** Crear un nuevo archivo, por ejemplo `src/services/apiService.ts`.
        *   **Contenido:** Este archivo contendrá una instancia de `axios` (o `fetch`) preconfigurada para incluir la URL base del backend y para añadir automáticamente el token JWT de Supabase en la cabecera `Authorization` de cada petición.

    2.  **Implementar la Lógica de Subida de Archivos:**
        *   **Acción:** En `DataIngestionPage.tsx`, modificar la función `handleFileSelect`.
        *   **Lógica:**
            *   Llamar al nuevo `apiService` para hacer la petición `POST /api/etl/start-process` con el archivo.
            *   Al recibir la respuesta `202 Accepted`, guardar el `jobId` en el estado.
            *   Iniciar un mecanismo de *polling* (ej. `setInterval`) que llame periódicamente a `GET /api/etl/jobs/{jobId}/status`.

    3.  **Implementar la Lógica de Polling y Actualización de UI:**
        *   **Acción:** Crear una función que maneje el polling.
        *   **Lógica:**
            *   Cada X segundos, llamar al endpoint de estado del job.
            *   Cuando el estado del job cambie a "EXITO" o "FALLO", detener el polling.
            *   Actualizar la tabla `UploadHistoryTable` con la información recibida del backend.
            *   Mostrar una notificación de éxito o error (`toast`) con el mensaje apropiado.

    4.  **Manejo de Errores de API:**
        *   **Acción:** Envolver las llamadas de la API en bloques `try/catch`.
        *   **Lógica:** Capturar los errores (ej. 400, 409, 401) y mostrar un mensaje de error claro al usuario utilizando `toast.error()`.


