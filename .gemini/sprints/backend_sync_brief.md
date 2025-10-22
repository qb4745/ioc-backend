# Backend Sync Brief - IOC Platform
**Versión**: 2025-10-10 13:00
**Stack Target**: Spring Boot 3 + Java 21
**Contrato Format**: TypeScript (para referencia Frontend)
**Fuente**: Análisis de código fuente (`/src/main/java`)

---

## 1. Metadatos de Generación

**Generado**: 2025-10-10T16:00:00Z
**Fuentes Analizadas**:
- `DashboardController.java`
- `EtlController.java`
- `EtlJobStatusDto.java`
- `MetabaseProperties.java`

**Estadísticas**:
- Endpoints encontrados: 3
- Configuraciones analizadas: 1
- Conflictos detectados: 0
- Advertencias: 0

---

## 2. Política de Seguridad Global

### Mecanismo de Autenticación
- **Provider**: Supabase GoTrue (inferido de `SecurityConfig`)
- **Token Format**: JWT en header `Authorization: Bearer <token>`
- **Validación Backend**: Spring Security como Resource Server

### Matriz de Acceso por Endpoint

| Endpoint | Método | Auth | Roles | Rate Limit |
|----------|--------|------|-------|------------|
| `/api/v1/dashboards/{dashboardId}` | GET | ✅ | `isAuthenticated()` | `dashboardAccess` |
| `/api/etl/start-process` | POST | ✅ | `isAuthenticated()` | N/A |
| `/api/etl/jobs/{jobId}/status` | GET | ✅ | `isAuthenticated()` | N/A |

---

## 3. Contratos de API

### 3.1. Dashboards

#### `GET /api/v1/dashboards/{dashboardId}`

**Propósito**: Genera una URL firmada de corta duración para renderizar un dashboard de Metabase en un iframe.
**Fuente**: `DashboardController.java`

##### Request

```typescript
// Path Params
interface GetDashboardUrlParams {
  dashboardId: number; // Validado con @Min(1) @Max(999999)
}
```

##### Response (200 OK)

```typescript
interface DashboardUrlResponse {
  signedUrl: string;
  expiresInMinutes: number;
  dashboardId: number;
}
```

##### Error Responses
- **401 Unauthorized**: Token JWT inválido o expirado.
- **403 Forbidden**: (Si se implementan roles específicos).
- **404 Not Found**: Si el `dashboardId` no está configurado.
- **429 Too Many Requests**: Rate limit excedido.
- **500 Internal Server Error**: Error al generar la URL.

---

### 3.2. Ingesta de Datos (ETL)

#### `POST /api/etl/start-process`

**Propósito**: Inicia un proceso ETL asíncrono para el archivo de texto (`.txt`) subido.
**Fuente**: `EtlController.java`

##### Request

```typescript
// Content-Type: multipart/form-data
interface StartEtlProcessRequest {
  file: File; // Archivo .txt, tamaño máximo 50MB
}
```

##### Response (202 Accepted)

```typescript
interface StartEtlProcessResponse {
  jobId: string; // UUID
  fileName: string;
  status: 'INICIADO';
}
```

##### Error Responses
- **400 Bad Request**: Archivo vacío, tamaño incorrecto o extensión no válida.
- **401/403**: No autenticado.
- **409 Conflict**: El archivo ya ha sido procesado (basado en hash).
- **500**: Error interno.

#### `GET /api/etl/jobs/{jobId}/status`

**Propósito**: Consulta el estado detallado de un trabajo ETL específico.
**Fuente**: `EtlController.java`, `EtlJobStatusDto.java`

##### Request

```typescript
// Path Params
interface GetJobStatusParams {
  jobId: string; // UUID
}
```

##### Response (200 OK)

```typescript
interface EtlJobStatusDto {
  jobId: string; // UUID
  fileName: string;
  status: string; // e.g., "INICIADO", "PROCESANDO", "EXITO", "FALLO"
  details: string | null;
  minDate: string | null; // Formato: YYYY-MM-DD
  maxDate: string | null; // Formato: YYYY-MM-DD
  createdAt: string; // ISO-8601 OffsetDateTime
  finishedAt: string | null; // ISO-8601 OffsetDateTime
}
```

##### Error Responses
- **401/403**: No autenticado.
- **404 Not Found**: El `jobId` no existe.
- **500**: Error interno.

---

## 4. Estructura de Configuración (`application.properties`)

### 4.1. Configuración de Metabase

**Propósito**: Define los parámetros de conexión a Metabase y la configuración de los dashboards que se pueden incrustar.
**Fuente**: `MetabaseProperties.java`

```properties
# URL base de la instancia de Metabase
metabase.site-url=http://54.232.229.228:3000

# Clave secreta de embedding (debe tener 64+ caracteres hexadecimales)
metabase.secret-key=${METABASE_SECRET_KEY}

# Expiración del token de embedding en minutos
metabase.token-expiration-minutes=10

# --- Lista de Dashboards ---
# (Se pueden definir múltiples dashboards usando el índice [0], [1], etc.)

# Ejemplo de Dashboard 1
metabase.dashboards[0].id=1
metabase.dashboards[0].name=Dashboard Gerencial
metabase.dashboards[0].description=Metricas ejecutivas y KPIs principales
metabase.dashboards[0].allowed-roles=ROLE_ADMIN,ROLE_MANAGER

# Filtros para el Dashboard 1 (opcional)
metabase.dashboards[0].filters[0].name=user_id
metabase.dashboards[0].filters[0].type=USER_ATTRIBUTE
metabase.dashboards[0].filters[0].source=userId

# Ejemplo de Dashboard 2
metabase.dashboards[1].id=3
metabase.dashboards[1].name=Dashboard Operacional Prueba
metabase.dashboards[1].description=Métricas operativas diarias de prueba
metabase.dashboards[1].allowed-roles=ROLE_USER,ROLE_ADMIN
```

---

## 5. Próximos Pasos

- **Frontend**: Usar estos contratos actualizados para la integración.
- **Backend**: No se requieren cambios, este brief refleja el estado actual del código y la configuración.