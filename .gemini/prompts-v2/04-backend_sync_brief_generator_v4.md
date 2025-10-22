# Prompt Mejorado: Backend Sync Brief Generator (v4) - IOC Platform

## 1. CONFIGURACIÓN (PARA EL HUMANO)

**Propósito**: Este prompt analiza las Fichas Técnicas de Vista (FTVs) del proyecto IOC y genera un brief de sincronización validado para el equipo Backend (Spring Boot).

**Acción Requerida**: Ninguna. Audita automáticamente el directorio de blueprints.

**Modo de Uso**: Copia y pega en Gemini CLI.

---

## 2. CONTEXTO DEL PROYECTO (MAPA MENTAL DE LA IA)

### Stack Tecnológico Target

**Frontend**: React 19 + TypeScript + Vite + Tailwind CSS  
**Backend**: Spring Boot 3 + Java 21 + PostgreSQL  
**Auth**: Supabase (JWT Provider)  
**Analytics**: Metabase (AWS EC2)

### Arquitectura de Información

```
@.gemini/blueprints/       ← Fuente primaria (FTVs)
@.gemini/sprints/          ← Contexto de negocio (backlog)
@.gemini/sprints/backend_sync_brief.md ← Archivo de salida
```

---

## 3. MANDATO OPERATIVO (PARA LA IA)

**Tu Rol**: Lead Software Architect especializado en contratos de API para arquitecturas desacopladas.

**Tu Misión**: Generar un "Backend Sync Brief" que traduzca requisitos de UI en especificaciones de API en formato TypeScript, listas para implementación en Spring Boot.

---

## 4. PROTOCOLO DE EJECUCIÓN (MANDATORIO)

### **Fase 1: Análisis y Extracción**

#### Acción 1.1: Escaneo de Fuentes
- Identifica todos los archivos `.md` en `@.gemini/blueprints/`
- Extrae TODOS los bloques que contengan:
  - `Endpoint:` o `API Endpoint:`
  - Bloques de código TypeScript/interfaces
  - Secciones "Contrato de API" o "Backend Requirements"

#### Acción 1.2: Normalización
Para cada endpoint encontrado, extrae:

```typescript
{
  method: "GET" | "POST" | "PUT" | "DELETE",
  path: string,                    // ej: /api/v1/etl/upload
  purpose: string,                 // Descripción del negocio
  source_ftv: string[],            // Archivos de origen
  auth_required: boolean,          // Default: true
  roles: string[],                 // ej: ["ADMIN", "ANALYST"]
  request: {
    params?: Record<string, any>,  // Path params
    query?: Record<string, any>,   // Query params
    body?: Record<string, any>     // Request body
  },
  response: {
    success: Record<string, any>,  // Response 200
    errors: Record<number, string> // Códigos de error
  }
}
```

---

### **Fase 2: Validación y Fusión**

#### Regla 2.1: Detección de Conflictos

**Si dos FTVs definen el mismo endpoint (`method` + `path`), aplicar:**

1. **Conflicto Tipo A (Tipos Incompatibles)**:
   ```typescript
   // FTV-1 dice:
   totalUsers: number
   
   // FTV-2 dice:
   totalUsers: string
   
   ❌ DETENER → Reportar conflicto para resolución humana
   ```

2. **Conflicto Tipo B (Campos Opcionales vs Requeridos)**:
   ```typescript
   // FTV-1:
   email?: string
   
   // FTV-2:
   email: string
   
   ✅ FUSIONAR → Marcar como requerido (el más restrictivo gana)
   ```

3. **Caso C (Propiedades Adicionales)**:
   ```typescript
   // FTV-1 tiene: { id, name }
   // FTV-2 tiene: { id, name, email }
   
   ✅ FUSIONAR → Resultado: { id, name, email }
   ```

#### Regla 2.2: Validación de Completitud

**Para cada endpoint, verificar que se haya especificado:**

- [ ] Método HTTP y ruta
- [ ] Propósito/descripción de negocio
- [ ] Roles permitidos (si `auth_required = true`)
- [ ] Estructura de respuesta exitosa (200/201)
- [ ] Al menos 2 códigos de error (401, 500 mínimos)
- [ ] Tipos TypeScript explícitos (sin `any` o `unknown`)

**Si falta algún elemento crítico**: Reportar advertencia pero continuar con valores por defecto:

```typescript
// Defaults para elementos faltantes:
- auth_required: true
- roles: ["ADMIN"]  // Más restrictivo por seguridad
- errors: { 401: "No autorizado", 500: "Error interno" }
```

---

### **Fase 3: Generación del Brief**

**Acción**: Crear archivo `backend_sync_brief.md` en `@.gemini/sprints/` siguiendo la plantilla mejorada a continuación.

---

## 5. PLANTILLA DE SALIDA (v4) - OBLIGATORIA

```markdown
# Backend Sync Brief - IOC Platform
**Versión**: [YYYY-MM-DD HH:mm]  
**Stack Target**: Spring Boot 3 + Java 21  
**Contrato Format**: TypeScript (para referencia Frontend)

---

## 1. Metadatos de Generación

**Generado**: [Fecha y hora actual ISO 8601]  
**Fuentes Analizadas**:
- `ftv-admin-dashboard.md`
- `ftv-data-ingestion.md`
- `ftv-analytics-viewer.md`
- *(listar todos los procesados)*

**Estadísticas**:
- Endpoints encontrados: X
- Conflictos detectados: Y
- Advertencias: Z

---

## 2. Política de Seguridad Global

### Mecanismo de Autenticación
- **Provider**: Supabase GoTrue
- **Token Format**: JWT en header `Authorization: Bearer <token>`
- **Validación Backend**: Spring Security como Resource Server
- **JWKS Endpoint**: `https://[SUPABASE_URL]/auth/v1/jwks`

### Matriz de Acceso por Endpoint

| Endpoint | Método | Auth | Roles | Rate Limit |
|----------|--------|------|-------|------------|
| `/api/v1/dashboards/{id}` | GET | ✅ | ADMIN, ANALYST | 100/min |
| `/api/v1/etl/upload` | POST | ✅ | ADMIN | 10/hora |
| `/api/v1/etl/history` | GET | ✅ | ADMIN | 60/min |

---

## 3. Contratos de API

### 3.1. Visualización de Dashboards

#### `GET /api/v1/dashboards/{dashboardId}`

**Propósito**: Genera una URL firmada de corta duración para renderizar un dashboard de Metabase en un iframe.

**FTVs de Origen**: `ftv-analytics-viewer.md`, `ftv-admin-dashboard.md`

##### Request

```typescript
// Path Params
interface PathParams {
  dashboardId: number; // ID del dashboard en Metabase
}

// Query Params (Opcionales - para filtros dinámicos)
interface QueryParams {
  fecha_inicio?: string;  // ISO 8601 date
  fecha_fin?: string;     // ISO 8601 date
  region?: string;        // Código de región
}
```

##### Response (200 OK)

```typescript
interface DashboardResponse {
  iframeUrl: string;      // URL firmada con JWT de Metabase
  expiresAt: string;      // ISO 8601 timestamp
  dashboardTitle: string; // Título del dashboard
}
```

##### Error Responses

- **401 Unauthorized**: Token JWT de Supabase inválido o expirado
- **403 Forbidden**: Usuario sin permiso para este dashboard
- **404 Not Found**: Dashboard ID no existe en Metabase
- **429 Too Many Requests**: Rate limit excedido (100 req/min)
- **500 Internal Server Error**: Error al generar JWT de Metabase

##### Notas de Implementación

- Cachear metadatos del dashboard por 10 minutos
- TTL del JWT de Metabase: 10 minutos
- Validar que el usuario tenga acceso según su rol almacenado en PostgreSQL
- Logging obligatorio de accesos para auditoría

---

### 3.2. Ingesta de Datos (ETL)

#### `POST /api/v1/etl/upload`

**Propósito**: Inicia un proceso ETL asíncrono para un archivo cargado por el administrador.

**FTVs de Origen**: `ftv-data-ingestion.md`

##### Request

```typescript
// Content-Type: multipart/form-data
interface UploadRequest {
  file: File;              // CSV, XLSX (max 50MB)
  dataType: 'sales' | 'inventory' | 'logistics'; // Tipo de dataset
  overwriteExisting: boolean; // Default: false
}
```

##### Response (202 Accepted)

```typescript
interface UploadResponse {
  jobId: string;           // UUID del trabajo ETL
  status: 'PENDING';       // Estado inicial
  createdAt: string;       // ISO 8601
  estimatedDuration: number; // Segundos estimados
}
```

##### Error Responses

- **400 Bad Request**: Archivo inválido (formato, tamaño, columnas faltantes)
- **401 Unauthorized**: Token inválido
- **403 Forbidden**: Usuario no tiene rol ADMIN
- **413 Payload Too Large**: Archivo excede 50MB
- **429 Too Many Requests**: Máximo 10 cargas por hora
- **500 Internal Server Error**: Error al iniciar proceso ETL

##### Notas de Implementación

- Validar extensión del archivo en el backend (no confiar en MIME type)
- Almacenar archivo en bucket S3 antes de procesamiento
- Crear entrada en tabla `etl_jobs` con estado `PENDING`
- Lanzar procesamiento asíncrono con Spring `@Async`
- Enviar notificación al frontend vía WebSocket cuando termine (futuro)

---

#### `GET /api/v1/etl/history`

**Propósito**: Obtiene el historial de trabajos ETL del usuario actual.

**FTVs de Origen**: `ftv-data-ingestion.md`

##### Request

```typescript
// Query Params
interface HistoryQueryParams {
  page?: number;          // Default: 0
  size?: number;          // Default: 20, max: 100
  status?: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  sortBy?: 'createdAt' | 'fileName'; // Default: createdAt
  order?: 'asc' | 'desc'; // Default: desc
}
```

##### Response (200 OK)

```typescript
interface HistoryResponse {
  jobs: ETLJob[];
  pagination: {
    totalElements: number;
    totalPages: number;
    currentPage: number;
    pageSize: number;
  };
}

interface ETLJob {
  jobId: string;
  fileName: string;
  dataType: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  createdAt: string;      // ISO 8601
  completedAt?: string;   // ISO 8601, null si no ha terminado
  recordsProcessed?: number;
  errorMessage?: string;  // Solo si status === 'FAILED'
}
```

##### Error Responses

- **401 Unauthorized**: Token inválido
- **403 Forbidden**: Usuario sin permisos
- **500 Internal Server Error**: Error al consultar base de datos

---

#### `GET /api/v1/etl/jobs/{jobId}`

**Propósito**: Consulta el estado detallado de un trabajo ETL específico (para polling del frontend).

**FTVs de Origen**: `ftv-data-ingestion.md`

##### Request

```typescript
// Path Params
interface PathParams {
  jobId: string; // UUID del trabajo
}
```

##### Response (200 OK)

```typescript
interface JobStatusResponse {
  jobId: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  progress: {
    percentage: number;    // 0-100
    currentStep: string;   // ej: "Validando datos", "Insertando registros"
    recordsProcessed: number;
    recordsTotal?: number; // null hasta que se calcule
  };
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  result?: {
    recordsInserted: number;
    recordsUpdated: number;
    recordsSkipped: number;
    warnings: string[];    // Advertencias no críticas
  };
  error?: {
    message: string;
    code: string;          // ej: "INVALID_DATA", "DATABASE_ERROR"
    details: string;       // Stack trace o detalles técnicos
  };
}
```

##### Error Responses

- **401 Unauthorized**: Token inválido
- **403 Forbidden**: Usuario intentando acceder a job de otro usuario
- **404 Not Found**: Job ID no existe
- **500 Internal Server Error**: Error al consultar estado

##### Notas de Implementación

- El frontend hará polling cada 2 segundos mientras `status !== 'COMPLETED' && status !== 'FAILED'`
- Implementar cache de 1 segundo para el mismo `jobId` para prevenir sobrecarga
- Considerar migrar a WebSocket en Sprint 2 para actualización en tiempo real

---

## 4. Estructura de Errores Estándar

Todos los endpoints deben retornar errores en el siguiente formato:

```typescript
interface ErrorResponse {
  timestamp: string;        // ISO 8601
  status: number;           // Código HTTP
  error: string;            // Mensaje de error estándar
  message: string;          // Descripción detallada
  path: string;             // Ruta del endpoint
  traceId?: string;         // UUID para tracing (opcional)
}
```

**Ejemplo**:
```json
{
  "timestamp": "2024-01-15T10:30:45.123Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Usuario sin permisos de administrador",
  "path": "/api/v1/etl/upload",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

---

## 5. Consideraciones Especiales

### 5.1. Integración con Supabase

- El backend debe validar el JWT usando el JWKS de Supabase
- Extraer `sub` (user ID) y `email` del token para auditoría
- Los roles se almacenan en PostgreSQL (tabla `user_roles`), NO en el JWT de Supabase

### 5.2. Integración con Metabase

- Usar librería `jjwt` para generar tokens JWT de Metabase
- Secret Key de Metabase debe estar en variable de entorno `METABASE_SECRET_KEY`
- URL base de Metabase en `METABASE_URL`

### 5.3. Rate Limiting

- Implementado con Resilience4j
- Límites por endpoint definidos en la tabla de la sección 2
- Respuesta 429 debe incluir header `Retry-After` en segundos

---

## 6. Próximos Pasos

### Para el Equipo Frontend
✅ Usar estos contratos para desarrollo contra datos mock  
✅ Actualizar el servicio `api.ts` con los tipos TypeScript  
✅ Implementar manejo de errores según `ErrorResponse`

### Para el Equipo Backend
🔲 Implementar endpoints según contratos  
🔲 Configurar Rate Limiting con Resilience4j  
🔲 Crear tests de integración para cada endpoint  
🔲 Documentar en Swagger/OpenAPI  
🔲 Notificar cuando estén listos para integración

---

## 7. Changelog del Brief

| Versión | Fecha | Cambios |
|---------|-------|---------|
| v4 | 2024-01-15 | Versión inicial con contratos de ETL y Dashboards |

---

**Generado automáticamente por**: Gemini CLI + Blueprint Analyzer v4
```

---

## 6. REGLAS DE EJECUCIÓN ADICIONALES (PARA LA IA)

### Regla 6.1: Priorización de Endpoints
Si encuentras más de 10 endpoints, prioriza en este orden:
1. Endpoints marcados como "Sprint Actual" en el backlog
2. Endpoints de autenticación críticos
3. Endpoints de visualización (dashboards)
4. Endpoints de ETL
5. Endpoints de configuración

### Regla 6.2: Manejo de Ambigüedades
Si una FTV no especifica:
- **Códigos de error**: Usar defaults (401, 500)
- **Tipos de datos**: Inferir del contexto o marcar como `unknown` con advertencia
- **Roles**: Asumir `["ADMIN"]` (más restrictivo)
- **Paginación**: Si retorna arrays, asumir paginación requerida

### Regla 6.3: Validación de Naming Conventions
Todos los endpoints deben seguir:
- Rutas: `/api/v1/recurso` (plural para colecciones)
- Path params: `{camelCase}`
- Query params: `snake_case` o `camelCase` consistente
- Response fields: `camelCase`

Si detectas inconsistencias, reportar pero no detener.

---

## 7. EJEMPLO DE SALIDA DE CONFLICTOS

Si se detectan conflictos, la IA debe generar este reporte ANTES del brief:

```markdown
⚠️ CONFLICTOS DETECTADOS - REQUIERE INTERVENCIÓN HUMANA

### Conflicto #1: Tipos Incompatibles
**Endpoint**: GET /api/v1/dashboards/{id}
**Campo**: `expiresAt`
**FTV-1** (`ftv-dashboard-viewer.md`): `expiresAt: number` (timestamp Unix)
**FTV-2** (`ftv-admin-dashboard.md`): `expiresAt: string` (ISO 8601)

🔧 **Acción Requerida**: Unificar tipo en ambas FTVs antes de regenerar brief.

---

### Conflicto #2: Endpoints Duplicados con Propósitos Diferentes
**Endpoint**: POST /api/v1/etl/upload
**FTV-1** (`ftv-data-ingestion.md`): Upload de archivos CSV
**FTV-2** (`ftv-bulk-import.md`): Upload de archivos JSON

🔧 **Acción Requerida**: Renombrar uno de los endpoints o fusionar funcionalidad.

---

❌ **GENERACIÓN DETENIDA**: Resolver conflictos y volver a ejecutar el prompt.
```

---

