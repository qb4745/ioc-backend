# API Brief - IOC Backend (Completo)

**Proyecto:** Sistema de Indicadores Operacionales Cambiaso  
**Backend:** Spring Boot 3.x + PostgreSQL + Supabase Auth  
**Generado:** 2025-11-17  
**Base URL:** `https://api.ioc-cambiaso.com` (Producción) | `http://localhost:8080` (Local)

---

## 📋 Índice de Endpoints

### Módulos Principales
1. [ETL Controller](#1-etl-controller) - Procesamiento de archivos TXT
2. [Dashboard Controller](#2-dashboard-controller) - Embedding de Metabase
3. [AI Explanation Controller](#3-ai-explanation-controller) - Explicaciones con IA
4. [User Controller](#4-user-controller) - Perfil de usuario
5. [Admin User Controller](#5-admin-user-controller) - Administración de usuarios
6. [Role Controller](#6-role-controller) - Gestión de roles
7. [Permission Controller](#7-permission-controller) - Gestión de permisos
8. [Assignment Controller](#8-assignment-controller) - Asignación de roles y permisos

---

## 🔐 Autenticación

**Tipo:** Bearer Token (JWT de Supabase)

**Header requerido en todos los endpoints (excepto actuator):**
```http
Authorization: Bearer <JWT_TOKEN>
```

**Roles disponibles:**
- `ROLE_ADMIN` - Acceso completo a endpoints administrativos
- `ROLE_USER` - Acceso a endpoints públicos autenticados

**Rate Limiting:**
- `dashboardAccess`: Configurado en resilience4j
- `aiExplanation`: 10 requests/min por usuario

---

## 1. ETL Controller

**Base Path:** `/api/etl`

### 1.1 Iniciar Proceso ETL

**Endpoint:** `POST /api/etl/start-process`  
**Autenticación:** Requerida (cualquier usuario autenticado)  
**Descripción:** Procesa un archivo TXT con datos de producción de forma asíncrona

**Request:**
```http
POST /api/etl/start-process
Content-Type: multipart/form-data
Authorization: Bearer <token>

file: <archivo.txt>
```

**Validaciones:**
- Archivo no vacío
- Tamaño máximo: 50MB
- Extensión: `.txt`
- Content-type: `text/plain` (preferido)
- Validación de hash para idempotencia

**Response Success (202 Accepted):**
```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "fileName": "produccion_2025_11.txt",
  "status": "INICIADO"
}
```

**Response Error (409 Conflict):**
```json
{
  "message": "This file has already been processed. Job ID: 550e8400-e29b-41d4-a716-446655440000"
}
```

**Response Error (400 Bad Request):**
```json
{
  "message": "File size exceeds maximum allowed limit of 50MB"
}
```

---

### 1.2 Consultar Estado de Job

**Endpoint:** `GET /api/etl/jobs/{jobId}/status`  
**Autenticación:** Requerida  
**Descripción:** Obtiene el estado actual de un proceso ETL

**Request:**
```http
GET /api/etl/jobs/550e8400-e29b-41d4-a716-446655440000/status
Authorization: Bearer <token>
```

**Response Success (200 OK):**
```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "fileName": "produccion_2025_11.txt",
  "status": "COMPLETADO",
  "details": "Procesadas 15,234 líneas correctamente",
  "minDate": "2025-11-01",
  "maxDate": "2025-11-30",
  "createdAt": "2025-11-17T14:30:00Z",
  "finishedAt": "2025-11-17T14:32:15Z"
}
```

**Posibles Estados:**
- `INICIADO` - Job creado, en cola
- `PROCESANDO` - Archivo siendo procesado
- `COMPLETADO` - Proceso exitoso
- `FALLIDO` - Error durante procesamiento

**Response Error (404 Not Found):**
```json
{}
```

---

## 2. Dashboard Controller

**Base Path:** `/api/v1/dashboards`

### 2.1 Obtener URL de Dashboard

**Endpoint:** `GET /api/v1/dashboards/{dashboardId}`  
**Autenticación:** Requerida  
**Rate Limit:** `dashboardAccess`  
**Descripción:** Genera URL firmada para incrustar dashboard de Metabase

**Request:**
```http
GET /api/v1/dashboards/5
Authorization: Bearer <token>
```

**Path Parameters:**
- `dashboardId` (integer, required) - ID del dashboard (1-999999)

**Validaciones:**
- dashboardId entre 1 y 999,999
- Usuario debe tener acceso al dashboard (verificado por RBAC)

**Response Success (200 OK):**
```json
{
  "signedUrl": "https://metabase.ioc.com/embed/dashboard/eyJhbGc...",
  "expiresInMinutes": 10,
  "dashboardId": 5
}
```

**Response Error (403 Forbidden):**
```json
{
  "error": "Access denied to dashboard 5"
}
```

**Response Error (400 Bad Request):**
```json
{
  "dashboardId": "Dashboard ID must be at least 1"
}
```

---

## 3. AI Explanation Controller

**Base Path:** `/api/v1/ai`

### 3.1 Explicar Dashboard (POST)

**Endpoint:** `POST /api/v1/ai/explain`  
**Aliases:** `POST /api/v1/ai/explain-dashboard`  
**Autenticación:** Requerida  
**Rate Limit:** `aiExplanation` (10 req/min)  
**Descripción:** Genera explicación ejecutiva de un dashboard usando IA (Gemini)

**Request:**
```http
POST /api/v1/ai/explain
Content-Type: application/json
Authorization: Bearer <token>

{
  "dashboardId": 5,
  "fechaInicio": "2025-06-01",
  "fechaFin": "2025-06-30",
  "filtros": {
    "turno": "DIA",
    "planta": "P1"
  }
}
```

**Request Body:**
```typescript
{
  dashboardId: number;      // Required, 1-999999
  fechaInicio: string;      // Required, formato ISO (yyyy-MM-dd)
  fechaFin: string;         // Required, formato ISO (yyyy-MM-dd)
  filtros?: {               // Optional
    [key: string]: string;
  }
}
```

**Validaciones:**
- dashboardId positivo y requerido
- fechaInicio y fechaFin no nulos
- Rango máximo: 12 meses
- Usuario debe tener acceso al dashboard

**Response Success (200 OK):**
```json
{
  "resumenEjecutivo": "Durante junio 2025, la producción alcanzó 45,234 unidades con una eficiencia promedio del 87.3%. Se observó un incremento del 12% respecto al mes anterior.",
  "keyPoints": [
    "Producción total: 45,234 unidades (+12% vs mayo)",
    "Eficiencia promedio: 87.3%",
    "Turno más productivo: DIA (58% del total)",
    "Tiempo de inactividad: 3.2% (dentro del objetivo <5%)"
  ],
  "insightsAccionables": [
    "Considerar aumentar capacidad en turno DIA dado su alto rendimiento",
    "Investigar causa de baja eficiencia en máquina M-105 (78.2%)",
    "Replicar mejores prácticas del operario TOP-001 al resto del equipo"
  ],
  "alertas": [
    "Máquina M-105 muestra eficiencia 10% por debajo del promedio",
    "Incremento de rechazos en última semana (+5%)"
  ],
  "dashboardId": 5,
  "dashboardTitulo": "Dashboard Producción General",
  "fechaInicio": "2025-06-01",
  "fechaFin": "2025-06-30",
  "filtrosAplicados": {
    "turno": "DIA",
    "planta": "P1"
  },
  "generadoEn": "2025-11-17T14:35:22Z",
  "fromCache": false,
  "tokensUsados": 1247,
  "cacheTTLSeconds": 540
}
```

**Response Error (400 Bad Request):**
```json
{
  "resumenEjecutivo": "Invalid date range: fechaFin must be after fechaInicio",
  "keyPoints": [],
  "insightsAccionables": [],
  "alertas": ["Error: Invalid date range: fechaFin must be after fechaInicio"],
  "dashboardId": 5,
  "dashboardTitulo": "Dashboard 5",
  "fechaInicio": "2025-06-30",
  "fechaFin": "2025-06-01",
  "filtrosAplicados": {},
  "generadoEn": "2025-11-17T14:35:22Z",
  "fromCache": false,
  "tokensUsados": 0,
  "cacheTTLSeconds": 300
}
```

**Response Error (408 Request Timeout):**
```json
{
  "resumenEjecutivo": "Request timeout - try again later",
  // ... estructura similar con alertas de error
}
```

**Response Error (429 Too Many Requests):**
```json
{
  "resumenEjecutivo": "Rate limit exceeded - please wait",
  // ... estructura similar
}
```

**Response Error (503 Service Unavailable):**
```json
{
  "resumenEjecutivo": "AI service temporarily unavailable",
  // ... estructura similar
}
```

---

### 3.2 Explicar Dashboard (GET)

**Endpoint:** `GET /api/v1/ai/explain/{dashboardId}`  
**Autenticación:** Requerida  
**Rate Limit:** `aiExplanation`  
**Descripción:** Genera explicación usando query parameters (alternativa sin body JSON)

**Request:**
```http
GET /api/v1/ai/explain/5?fechaInicio=2025-06-01&fechaFin=2025-06-30&turno=DIA&planta=P1
Authorization: Bearer <token>
```

**Query Parameters:**
- `fechaInicio` (string, required) - Formato ISO (yyyy-MM-dd)
- `fechaFin` (string, required) - Formato ISO (yyyy-MM-dd)
- Cualquier otro parámetro se interpreta como filtro

**Response:** Misma estructura que POST `/api/v1/ai/explain`

---

### 3.3 Health Check AI Service

**Endpoint:** `GET /api/v1/ai/health`  
**Autenticación:** Requerida  
**Descripción:** Verifica disponibilidad del servicio de IA

**Request:**
```http
GET /api/v1/ai/health
Authorization: Bearer <token>
```

**Response Success (200 OK):**
```json
{
  "service": "AI Explanation Service",
  "status": "UP",
  "timestamp": "2025-11-17"
}
```

---

## 4. User Controller

**Base Path:** `/api/v1/users`

### 4.1 Obtener Perfil Actual

**Endpoint:** `GET /api/v1/users/me`  
**Autenticación:** Requerida  
**Descripción:** Obtiene el perfil del usuario autenticado con sus roles

**Request:**
```http
GET /api/v1/users/me
Authorization: Bearer <token>
```

**Response Success (200 OK):**
```json
{
  "id": 42,
  "email": "juan.perez@cambiaso.com",
  "fullName": "Juan Carlos Pérez González",
  "plantaId": 1,
  "plantaCode": "P1",
  "plantaName": "Planta Santiago",
  "centroCosto": "CC-001",
  "fechaContrato": "2024-03-15",
  "isActive": true,
  "createdAt": "2024-03-15T10:00:00Z",
  "updatedAt": "2025-11-17T14:00:00Z",
  "roles": ["ROLE_USER", "ROLE_SUPERVISOR"]
}
```

**Response Error (401 Unauthorized):**
```http
HTTP/1.1 401 Unauthorized
```

**Response Error (403 Forbidden):**
```http
HTTP/1.1 403 Forbidden
```
(Usuario existe pero está inactivo)

**Response Error (404 Not Found):**
```http
HTTP/1.1 404 Not Found
```
(Usuario no existe en la base de datos local)

---

## 5. Admin User Controller

**Base Path:** `/api/v1/admin/users`  
**Autorización:** `ROLE_ADMIN` requerido en todos los endpoints

### 5.1 Buscar Usuarios

**Endpoint:** `GET /api/v1/admin/users`  
**Descripción:** Busca y lista usuarios con paginación

**Request:**
```http
GET /api/v1/admin/users?search=juan&plantaId=1&isActive=true&page=0&size=20&sort=email,asc
Authorization: Bearer <token>
```

**Query Parameters:**
- `search` (string, optional) - Búsqueda por email o nombre
- `plantaId` (integer, optional) - Filtrar por planta
- `isActive` (boolean, optional) - Filtrar por estado activo
- `page` (integer, optional, default=0) - Número de página
- `size` (integer, optional, default=20, max=100) - Tamaño de página
- `sort` (string, optional) - Ordenamiento (ej: `email,asc`, `createdAt,desc`)

**Response Success (200 OK):**
```json
{
  "content": [
    {
      "id": 42,
      "email": "juan.perez@cambiaso.com",
      "fullName": "Juan Carlos Pérez González",
      "plantaId": 1,
      "plantaCode": "P1",
      "plantaName": "Planta Santiago",
      "centroCosto": "CC-001",
      "fechaContrato": "2024-03-15",
      "isActive": true,
      "createdAt": "2024-03-15T10:00:00Z",
      "updatedAt": "2025-11-17T14:00:00Z",
      "roles": ["ROLE_USER"]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 156,
  "totalPages": 8,
  "last": false,
  "first": true,
  "numberOfElements": 20,
  "size": 20,
  "number": 0,
  "empty": false
}
```

---

### 5.2 Obtener Usuario por ID

**Endpoint:** `GET /api/v1/admin/users/{id}`  
**Descripción:** Obtiene un usuario específico por su ID

**Request:**
```http
GET /api/v1/admin/users/42
Authorization: Bearer <token>
```

**Response Success (200 OK):**
```json
{
  "id": 42,
  "email": "juan.perez@cambiaso.com",
  "fullName": "Juan Carlos Pérez González",
  "plantaId": 1,
  "plantaCode": "P1",
  "plantaName": "Planta Santiago",
  "centroCosto": "CC-001",
  "fechaContrato": "2024-03-15",
  "isActive": true,
  "createdAt": "2024-03-15T10:00:00Z",
  "updatedAt": "2025-11-17T14:00:00Z",
  "roles": ["ROLE_USER", "ROLE_SUPERVISOR"]
}
```

**Response Error (404 Not Found):**
```json
{
  "error": "User not found with id: 42"
}
```

---

### 5.3 Crear Usuario

**Endpoint:** `POST /api/v1/admin/users`  
**Descripción:** Crea un nuevo usuario (crea automáticamente en Supabase Auth)

**Request:**
```http
POST /api/v1/admin/users
Content-Type: application/json
Authorization: Bearer <token>

{
  "email": "maria.gomez@cambiaso.com",
  "password": "TempPass123!",
  "primerNombre": "María",
  "segundoNombre": "Isabel",
  "primerApellido": "Gómez",
  "segundoApellido": "Rojas",
  "plantaId": 1,
  "centroCosto": "CC-002",
  "fechaContrato": "2025-11-17",
  "roles": ["ROLE_USER"]
}
```

**Request Body:**
```typescript
{
  email: string;              // Required, unique, formato email
  password?: string;          // Optional, min 6 caracteres (si se omite, se genera auto)
  supabaseUserId?: UUID;      // Deprecated, se crea automáticamente
  primerNombre: string;       // Required
  segundoNombre?: string;     // Optional
  primerApellido: string;     // Required
  segundoApellido?: string;   // Optional
  plantaId?: number;          // Optional
  centroCosto?: string;       // Optional, max 50 caracteres
  fechaContrato?: string;     // Optional, formato ISO
  roles?: string[];           // Optional, lista de roles
}
```

**Response Success (201 Created):**
```http
HTTP/1.1 201 Created
Location: /api/v1/admin/users/43

{
  "id": 43,
  "email": "maria.gomez@cambiaso.com",
  "fullName": "María Isabel Gómez Rojas",
  "plantaId": 1,
  "plantaCode": "P1",
  "plantaName": "Planta Santiago",
  "centroCosto": "CC-002",
  "fechaContrato": "2025-11-17",
  "isActive": true,
  "createdAt": "2025-11-17T15:00:00Z",
  "updatedAt": "2025-11-17T15:00:00Z",
  "roles": ["ROLE_USER"]
}
```

**Response Error (400 Bad Request):**
```json
{
  "email": "Email already exists",
  "password": "Password must be at least 6 characters"
}
```

---

### 5.4 Actualizar Usuario

**Endpoint:** `PUT /api/v1/admin/users/{id}`  
**Descripción:** Actualiza los datos de un usuario existente

**Request:**
```http
PUT /api/v1/admin/users/42
Content-Type: application/json
Authorization: Bearer <token>

{
  "primerNombre": "Juan Carlos",
  "segundoNombre": null,
  "primerApellido": "Pérez",
  "segundoApellido": "González",
  "plantaId": 2,
  "centroCosto": "CC-003",
  "fechaContrato": "2024-03-15",
  "isActive": true
}
```

**Request Body:**
```typescript
{
  primerNombre: string;       // Required
  segundoNombre?: string;     // Optional
  primerApellido: string;     // Required
  segundoApellido?: string;   // Optional
  plantaId?: number;          // Optional (null para quitar)
  centroCosto?: string;       // Optional, max 50 caracteres
  fechaContrato?: string;     // Optional, formato ISO
  isActive?: boolean;         // Optional
}
```

**Response Success (200 OK):**
```json
{
  "id": 42,
  "email": "juan.perez@cambiaso.com",
  "fullName": "Juan Carlos Pérez González",
  "plantaId": 2,
  "plantaCode": "P2",
  "plantaName": "Planta Valparaíso",
  "centroCosto": "CC-003",
  "fechaContrato": "2024-03-15",
  "isActive": true,
  "createdAt": "2024-03-15T10:00:00Z",
  "updatedAt": "2025-11-17T15:30:00Z",
  "roles": ["ROLE_USER"]
}
```

---

### 5.5 Eliminar Usuario

**Endpoint:** `DELETE /api/v1/admin/users/{id}`  
**Descripción:** Elimina un usuario (soft delete - marca como inactivo)

**Request:**
```http
DELETE /api/v1/admin/users/42
Authorization: Bearer <token>
```

**Response Success (204 No Content):**
```http
HTTP/1.1 204 No Content
```

**Response Error (404 Not Found):**
```json
{
  "error": "User not found with id: 42"
}
```

---

## 6. Role Controller

**Base Path:** `/api/v1/admin/roles`  
**Autorización:** `ROLE_ADMIN` requerido en todos los endpoints

### 6.1 Buscar Roles

**Endpoint:** `GET /api/v1/admin/roles`  
**Descripción:** Lista roles con información de usuarios y permisos

**Request:**
```http
GET /api/v1/admin/roles?search=admin&page=0&size=20
Authorization: Bearer <token>
```

**Query Parameters:**
- `search` (string, optional) - Búsqueda por nombre o descripción
- `page` (integer, optional, default=0)
- `size` (integer, optional, default=20, max=100)
- `sort` (string, optional)

**Response Success (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "ROLE_ADMIN",
      "description": "Administrador del sistema con acceso completo",
      "usersCount": 5,
      "permissions": [
        "dashboard:view",
        "dashboard:edit",
        "users:manage",
        "roles:manage"
      ]
    },
    {
      "id": 2,
      "name": "ROLE_USER",
      "description": "Usuario estándar con acceso de solo lectura",
      "usersCount": 150,
      "permissions": [
        "dashboard:view"
      ]
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

---

### 6.2 Obtener Role por ID

**Endpoint:** `GET /api/v1/admin/roles/{id}`  
**Descripción:** Obtiene un rol específico con detalles completos

**Request:**
```http
GET /api/v1/admin/roles/1
Authorization: Bearer <token>
```

**Response Success (200 OK):**
```json
{
  "id": 1,
  "name": "ROLE_ADMIN",
  "description": "Administrador del sistema con acceso completo",
  "usersCount": 5,
  "permissions": [
    "dashboard:view",
    "dashboard:edit",
    "users:manage",
    "roles:manage",
    "permissions:manage"
  ]
}
```

---

### 6.3 Crear Role

**Endpoint:** `POST /api/v1/admin/roles`  
**Descripción:** Crea un nuevo rol

**Request:**
```http
POST /api/v1/admin/roles
Content-Type: application/json
Authorization: Bearer <token>

{
  "name": "ROLE_SUPERVISOR",
  "description": "Supervisor de planta con acceso a reportes"
}
```

**Request Body:**
```typescript
{
  name: string;         // Required, max 100 caracteres
  description?: string; // Optional, max 255 caracteres
}
```

**Response Success (201 Created):**
```http
HTTP/1.1 201 Created
Location: /api/v1/admin/roles/3

{
  "id": 3,
  "name": "ROLE_SUPERVISOR",
  "description": "Supervisor de planta con acceso a reportes",
  "usersCount": 0,
  "permissions": []
}
```

**Response Error (400 Bad Request):**
```json
{
  "name": "Role name already exists"
}
```

---

### 6.4 Actualizar Role

**Endpoint:** `PUT /api/v1/admin/roles/{id}`  
**Descripción:** Actualiza un rol existente

**Request:**
```http
PUT /api/v1/admin/roles/3
Content-Type: application/json
Authorization: Bearer <token>

{
  "name": "ROLE_SUPERVISOR",
  "description": "Supervisor de planta con acceso extendido a reportes y dashboards"
}
```

**Response Success (200 OK):**
```json
{
  "id": 3,
  "name": "ROLE_SUPERVISOR",
  "description": "Supervisor de planta con acceso extendido a reportes y dashboards",
  "usersCount": 12,
  "permissions": ["dashboard:view"]
}
```

---

### 6.5 Eliminar Role

**Endpoint:** `DELETE /api/v1/admin/roles/{id}`  
**Descripción:** Elimina un rol (solo si no tiene usuarios asignados)

**Request:**
```http
DELETE /api/v1/admin/roles/3
Authorization: Bearer <token>
```

**Response Success (204 No Content):**
```http
HTTP/1.1 204 No Content
```

**Response Error (409 Conflict):**
```json
{
  "error": "Cannot delete role with assigned users"
}
```

---

## 7. Permission Controller

**Base Path:** `/api/v1/admin/permissions`  
**Autorización:** `ROLE_ADMIN` requerido en todos los endpoints

### 7.1 Buscar Permisos

**Endpoint:** `GET /api/v1/admin/permissions`  
**Descripción:** Lista permisos disponibles

**Request:**
```http
GET /api/v1/admin/permissions?search=dashboard&page=0&size=20
Authorization: Bearer <token>
```

**Query Parameters:**
- `search` (string, optional)
- `page` (integer, optional, default=0)
- `size` (integer, optional, default=20, max=100)

**Response Success (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "dashboard:view",
      "description": "Ver dashboards"
    },
    {
      "id": 2,
      "name": "dashboard:edit",
      "description": "Editar dashboards"
    },
    {
      "id": 3,
      "name": "users:manage",
      "description": "Gestionar usuarios"
    }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

---

### 7.2 Obtener Permiso por ID

**Endpoint:** `GET /api/v1/admin/permissions/{id}`

**Request:**
```http
GET /api/v1/admin/permissions/1
Authorization: Bearer <token>
```

**Response Success (200 OK):**
```json
{
  "id": 1,
  "name": "dashboard:view",
  "description": "Ver dashboards"
}
```

---

### 7.3 Crear Permiso

**Endpoint:** `POST /api/v1/admin/permissions`

**Request:**
```http
POST /api/v1/admin/permissions
Content-Type: application/json
Authorization: Bearer <token>

{
  "name": "reports:export",
  "description": "Exportar reportes a PDF/Excel"
}
```

**Request Body:**
```typescript
{
  name: string;         // Required, max 150 caracteres
  description?: string; // Optional, max 255 caracteres
}
```

**Response Success (201 Created):**
```http
HTTP/1.1 201 Created
Location: /api/v1/admin/permissions/10

{
  "id": 10,
  "name": "reports:export",
  "description": "Exportar reportes a PDF/Excel"
}
```

---

### 7.4 Actualizar Permiso

**Endpoint:** `PUT /api/v1/admin/permissions/{id}`

**Request:**
```http
PUT /api/v1/admin/permissions/10
Content-Type: application/json
Authorization: Bearer <token>

{
  "name": "reports:export",
  "description": "Exportar reportes a múltiples formatos (PDF, Excel, CSV)"
}
```

**Response Success (200 OK):**
```json
{
  "id": 10,
  "name": "reports:export",
  "description": "Exportar reportes a múltiples formatos (PDF, Excel, CSV)"
}
```

---

### 7.5 Eliminar Permiso

**Endpoint:** `DELETE /api/v1/admin/permissions/{id}`

**Request:**
```http
DELETE /api/v1/admin/permissions/10
Authorization: Bearer <token>
```

**Response Success (204 No Content):**
```http
HTTP/1.1 204 No Content
```

---

## 8. Assignment Controller

**Base Path:** `/api/v1/admin/assignments`  
**Autorización:** `ROLE_ADMIN` requerido en todos los endpoints

### 8.1 Asignar Rol a Usuario

**Endpoint:** `POST /api/v1/admin/assignments/users/{userId}/roles/{roleId}`  
**Descripción:** Asigna un rol a un usuario

**Request:**
```http
POST /api/v1/admin/assignments/users/42/roles/3?assignedBy=1
Authorization: Bearer <token>
```

**Path Parameters:**
- `userId` (integer, required) - ID del usuario
- `roleId` (integer, required) - ID del rol

**Query Parameters:**
- `assignedBy` (integer, optional) - ID del admin que asigna

**Response Success (200 OK):**
```http
HTTP/1.1 200 OK
```

**Response Error (404 Not Found):**
```json
{
  "error": "User or Role not found"
}
```

**Response Error (409 Conflict):**
```json
{
  "error": "User already has this role"
}
```

---

### 8.2 Remover Rol de Usuario

**Endpoint:** `DELETE /api/v1/admin/assignments/users/{userId}/roles/{roleId}`  
**Descripción:** Remueve un rol de un usuario

**Request:**
```http
DELETE /api/v1/admin/assignments/users/42/roles/3
Authorization: Bearer <token>
```

**Response Success (204 No Content):**
```http
HTTP/1.1 204 No Content
```

---

### 8.3 Asignar Permiso a Rol

**Endpoint:** `POST /api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}`  
**Descripción:** Asigna un permiso a un rol

**Request:**
```http
POST /api/v1/admin/assignments/roles/3/permissions/10
Authorization: Bearer <token>
```

**Path Parameters:**
- `roleId` (integer, required) - ID del rol
- `permissionId` (integer, required) - ID del permiso

**Response Success (200 OK):**
```http
HTTP/1.1 200 OK
```

**Response Error (409 Conflict):**
```json
{
  "error": "Role already has this permission"
}
```

---

### 8.4 Remover Permiso de Rol

**Endpoint:** `DELETE /api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}`  
**Descripción:** Remueve un permiso de un rol

**Request:**
```http
DELETE /api/v1/admin/assignments/roles/3/permissions/10
Authorization: Bearer <token>
```

**Response Success (204 No Content):**
```http
HTTP/1.1 204 No Content
```

---

## 9. Actuator Endpoints

**Base Path:** `/actuator`  
**Autenticación:** No requerida (algunos endpoints pueden requerir auth según configuración)

### 9.1 Health Check

**Endpoint:** `GET /actuator/health`  
**Descripción:** Verifica el estado general de la aplicación

**Request:**
```http
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500068036608,
        "free": 250034018304,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

### 9.2 Lista de Métricas Disponibles

**Endpoint:** `GET /actuator/metrics`  
**Descripción:** Devuelve la lista de nombres de todas las métricas disponibles en el sistema

**Request:**
```http
GET /actuator/metrics
Accept: application/vnd.spring-boot.actuator.v3+json
```

**Response:**
```json
{
  "names": [
    "application.ready.time",
    "application.started.time",
    "cache.evictions",
    "cache.gets",
    "cache.puts",
    "etl.jobs.active",
    "etl.jobs.stuck",
    "hikaricp.connections.active",
    "hikaricp.connections.idle",
    "http.server.requests",
    "jvm.memory.used",
    "jvm.gc.pause",
    "metabase.dashboard.access",
    "resilience4j.ratelimiter.available.permissions",
    "spring.security.authentications",
    "system.cpu.usage",
    "tomcat.sessions.active.current"
    // ... más de 130 métricas disponibles
  ]
}
```

**Nota Importante:** Este endpoint solo devuelve los **nombres** de las métricas. Para obtener el **valor real** de una métrica específica, debes llamar a `/actuator/metrics/{metricName}`.

---

### 9.3 Obtener Valor de Métrica Específica

**Endpoint:** `GET /actuator/metrics/{metricName}`  
**Descripción:** Obtiene el valor actual y detalles de una métrica específica

**Métricas Clave del Sistema:**

#### 9.3.1 Conexiones de Base de Datos (HikariCP)

**Request:**
```http
GET /actuator/metrics/hikaricp.connections.active
```

**Response:**
```json
{
  "name": "hikaricp.connections.active",
  "description": "Active connections in the HikariCP connection pool",
  "baseUnit": "connections",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 8.0
    }
  ],
  "availableTags": [
    {
      "tag": "pool",
      "values": ["HikariPool-1"]
    }
  ]
}
```

**Otras métricas de HikariCP:**
- `hikaricp.connections.idle` - Conexiones inactivas
- `hikaricp.connections.max` - Máximo de conexiones configurado
- `hikaricp.connections.min` - Mínimo de conexiones configurado
- `hikaricp.connections.pending` - Conexiones pendientes
- `hikaricp.connections.timeout` - Timeouts de conexión
- `hikaricp.connections.usage` - Tiempo de uso de conexiones

---

#### 9.3.2 Jobs ETL

**Request:**
```http
GET /actuator/metrics/etl.jobs.active
```

**Response:**
```json
{
  "name": "etl.jobs.active",
  "description": "Number of currently active ETL jobs",
  "baseUnit": "jobs",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 3.0
    }
  ]
}
```

**Request:**
```http
GET /actuator/metrics/etl.jobs.stuck
```

**Response:**
```json
{
  "name": "etl.jobs.stuck",
  "description": "Number of stuck ETL jobs (running for >30min)",
  "baseUnit": "jobs",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 0.0
    }
  ]
}
```

---

#### 9.3.3 Rate Limiter (AI Explanation)

**Request:**
```http
GET /actuator/metrics/resilience4j.ratelimiter.available.permissions
```

**Response:**
```json
{
  "name": "resilience4j.ratelimiter.available.permissions",
  "description": "Available permissions for rate limiter",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 7.0
    }
  ],
  "availableTags": [
    {
      "tag": "name",
      "values": ["aiExplanation", "dashboardAccess"]
    }
  ]
}
```

**Para filtrar por rate limiter específico:**
```http
GET /actuator/metrics/resilience4j.ratelimiter.available.permissions?tag=name:aiExplanation
```

**Otras métricas de Resilience4j:**
- `resilience4j.ratelimiter.waiting_threads` - Threads esperando permisos
- `resilience4j.circuitbreaker.state` - Estado del circuit breaker (0=closed, 1=open, 2=half-open)
- `resilience4j.circuitbreaker.failure.rate` - Tasa de fallos

---

#### 9.3.4 Acceso a Dashboards de Metabase

**Request:**
```http
GET /actuator/metrics/metabase.dashboard.access
```

**Response:**
```json
{
  "name": "metabase.dashboard.access",
  "description": "Dashboard access requests",
  "baseUnit": "requests",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 1247.0
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 18.456
    },
    {
      "statistic": "MAX",
      "value": 0.245
    }
  ],
  "availableTags": [
    {
      "tag": "dashboard",
      "values": ["5", "12", "23"]
    },
    {
      "tag": "status",
      "values": ["success", "error"]
    }
  ]
}
```

**Request:**
```http
GET /actuator/metrics/metabase.dashboard.request.duration
```

**Response:**
```json
{
  "name": "metabase.dashboard.request.duration",
  "description": "Duration of Metabase dashboard requests",
  "baseUnit": "seconds",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 1247.0
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 18.456
    },
    {
      "statistic": "MAX",
      "value": 0.245
    },
    {
      "statistic": "MEAN",
      "value": 0.0148
    }
  ]
}
```

---

#### 9.3.5 Solicitudes HTTP

**Request:**
```http
GET /actuator/metrics/http.server.requests
```

**Response:**
```json
{
  "name": "http.server.requests",
  "description": "HTTP server requests",
  "baseUnit": "seconds",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 15247.0
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 234.567
    },
    {
      "statistic": "MAX",
      "value": 2.345
    }
  ],
  "availableTags": [
    {
      "tag": "exception",
      "values": ["None", "IllegalArgumentException"]
    },
    {
      "tag": "method",
      "values": ["GET", "POST", "PUT", "DELETE"]
    },
    {
      "tag": "outcome",
      "values": ["SUCCESS", "CLIENT_ERROR", "SERVER_ERROR"]
    },
    {
      "tag": "status",
      "values": ["200", "201", "400", "401", "403", "404", "500"]
    },
    {
      "tag": "uri",
      "values": ["/api/v1/dashboards/{dashboardId}", "/api/etl/start-process", "/api/v1/users/me"]
    }
  ]
}
```

**Filtrar por endpoint específico:**
```http
GET /actuator/metrics/http.server.requests?tag=uri:/api/v1/dashboards/{dashboardId}&tag=status:200
```

**Request:**
```http
GET /actuator/metrics/http.server.requests.active
```

**Response:**
```json
{
  "name": "http.server.requests.active",
  "description": "Number of active HTTP requests",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 5.0
    }
  ]
}
```

---

#### 9.3.6 Autenticación y Seguridad

**Request:**
```http
GET /actuator/metrics/spring.security.authentications
```

**Response:**
```json
{
  "name": "spring.security.authentications",
  "description": "Spring Security authentication attempts",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 8456.0
    }
  ],
  "availableTags": [
    {
      "tag": "result",
      "values": ["success", "failure"]
    },
    {
      "tag": "type",
      "values": ["jwt"]
    }
  ]
}
```

**Filtrar autenticaciones fallidas:**
```http
GET /actuator/metrics/spring.security.authentications?tag=result:failure
```

**Request:**
```http
GET /actuator/metrics/spring.security.http.secured.requests
```

**Response:**
```json
{
  "name": "spring.security.http.secured.requests",
  "description": "Secured HTTP requests",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 15234.0
    }
  ],
  "availableTags": [
    {
      "tag": "result",
      "values": ["allowed", "denied"]
    }
  ]
}
```

---

#### 9.3.7 Caché (Caffeine)

**Request:**
```http
GET /actuator/metrics/cache.gets
```

**Response:**
```json
{
  "name": "cache.gets",
  "description": "Cache get operations",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 2456.0
    }
  ],
  "availableTags": [
    {
      "tag": "cache",
      "values": ["aiExplanationCache"]
    },
    {
      "tag": "result",
      "values": ["hit", "miss"]
    }
  ]
}
```

**Ver cache hits vs misses:**
```http
GET /actuator/metrics/cache.gets?tag=cache:aiExplanationCache&tag=result:hit
GET /actuator/metrics/cache.gets?tag=cache:aiExplanationCache&tag=result:miss
```

**Otras métricas de caché:**
- `cache.puts` - Elementos añadidos al caché
- `cache.evictions` - Elementos removidos del caché
- `cache.size` - Tamaño actual del caché
- `cache.eviction.weight` - Peso total de elementos evictados

---

#### 9.3.8 JVM y Memoria

**Request:**
```http
GET /actuator/metrics/jvm.memory.used
```

**Response:**
```json
{
  "name": "jvm.memory.used",
  "description": "The amount of used memory",
  "baseUnit": "bytes",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 524288000.0
    }
  ],
  "availableTags": [
    {
      "tag": "area",
      "values": ["heap", "nonheap"]
    },
    {
      "tag": "id",
      "values": ["G1 Eden Space", "G1 Old Gen", "Metaspace"]
    }
  ]
}
```

**Filtrar solo heap memory:**
```http
GET /actuator/metrics/jvm.memory.used?tag=area:heap
```

**Request:**
```http
GET /actuator/metrics/jvm.gc.pause
```

**Response:**
```json
{
  "name": "jvm.gc.pause",
  "description": "Time spent in GC pause",
  "baseUnit": "seconds",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 145.0
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 2.456
    },
    {
      "statistic": "MAX",
      "value": 0.089
    }
  ],
  "availableTags": [
    {
      "tag": "action",
      "values": ["end of minor GC", "end of major GC"]
    },
    {
      "tag": "cause",
      "values": ["Allocation Failure", "G1 Evacuation Pause"]
    }
  ]
}
```

**Otras métricas de JVM:**
- `jvm.threads.live` - Threads activos
- `jvm.threads.daemon` - Threads daemon
- `jvm.threads.peak` - Pico de threads
- `jvm.gc.memory.allocated` - Memoria asignada por GC
- `jvm.gc.memory.promoted` - Memoria promovida a Old Gen

---

#### 9.3.9 Sistema Operativo

**Request:**
```http
GET /actuator/metrics/system.cpu.usage
```

**Response:**
```json
{
  "name": "system.cpu.usage",
  "description": "The recent CPU usage of the system",
  "baseUnit": "percent",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 0.42
    }
  ]
}
```

**Request:**
```http
GET /actuator/metrics/process.cpu.usage
```

**Response:**
```json
{
  "name": "process.cpu.usage",
  "description": "The recent CPU usage of the JVM process",
  "baseUnit": "percent",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 0.18
    }
  ]
}
```

**Otras métricas del sistema:**
- `system.cpu.count` - Número de CPUs
- `system.load.average.1m` - Load average (1 minuto)
- `process.uptime` - Uptime del proceso
- `process.files.open` - Archivos abiertos
- `disk.free` - Espacio libre en disco
- `disk.total` - Espacio total en disco

---

#### 9.3.10 Thread Pool Executor

**Request:**
```http
GET /actuator/metrics/executor.active
```

**Response:**
```json
{
  "name": "executor.active",
  "description": "The approximate number of threads that are actively executing tasks",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 3.0
    }
  ],
  "availableTags": [
    {
      "tag": "name",
      "values": ["etlTaskExecutor"]
    }
  ]
}
```

**Otras métricas del executor:**
- `executor.completed` - Tareas completadas
- `executor.pool.size` - Tamaño actual del pool
- `executor.pool.core` - Tamaño core del pool
- `executor.pool.max` - Tamaño máximo del pool
- `executor.queued` - Tareas en cola
- `executor.queue.remaining` - Capacidad restante en cola

---

#### 9.3.11 Sesiones de Tomcat

**Request:**
```http
GET /actuator/metrics/tomcat.sessions.active.current
```

**Response:**
```json
{
  "name": "tomcat.sessions.active.current",
  "description": "Current number of active sessions",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 15.0
    }
  ]
}
```

**Otras métricas de Tomcat:**
- `tomcat.sessions.active.max` - Máximo de sesiones activas
- `tomcat.sessions.created` - Sesiones creadas
- `tomcat.sessions.expired` - Sesiones expiradas
- `tomcat.sessions.rejected` - Sesiones rechazadas

---

#### 9.3.12 Tiempo de Inicio de Aplicación

**Request:**
```http
GET /actuator/metrics/application.started.time
```

**Response:**
```json
{
  "name": "application.started.time",
  "description": "Time taken to start the application",
  "baseUnit": "seconds",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 12.456
    }
  ]
}
```

**Request:**
```http
GET /actuator/metrics/application.ready.time
```

**Response:**
```json
{
  "name": "application.ready.time",
  "description": "Time taken for the application to be ready to service requests",
  "baseUnit": "seconds",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 13.124
    }
  ]
}
```

---

### 9.4 Prometheus Metrics

**Endpoint:** `GET /actuator/prometheus`  
**Descripción:** Exporta todas las métricas en formato Prometheus para scraping
**Accept:** `text/plain;version=0.0.4;charset=utf-8`

**Request:**
```http
GET /actuator/prometheus
```

**Response (formato texto):**
```text
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="G1 Eden Space",} 5.24288E8
jvm_memory_used_bytes{area="heap",id="G1 Old Gen",} 1.048576E8
# HELP hikaricp_connections_active Active connections
# TYPE hikaricp_connections_active gauge
hikaricp_connections_active{pool="HikariPool-1",} 8.0
# HELP http_server_requests_seconds HTTP server requests
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/api/v1/users/me",} 1247.0
http_server_requests_seconds_sum{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/api/v1/users/me",} 18.456
# HELP etl_jobs_active_total Number of currently active ETL jobs
# TYPE etl_jobs_active_total gauge
etl_jobs_active_total 3.0
# ... más métricas en formato Prometheus
```

**Uso:** Este endpoint es ideal para integración con Prometheus + Grafana para monitoreo en tiempo real.

---

### 9.5 Info Endpoint

**Endpoint:** `GET /actuator/info`  
**Descripción:** Información general de la aplicación

**Request:**
```http
GET /actuator/info
```

**Response:**
```json
{
  "app": {
    "name": "ioc-backend",
    "version": "1.0.0",
    "description": "Sistema de Indicadores Operacionales Cambiaso"
  },
  "build": {
    "artifact": "ioc-backend",
    "name": "ioc-backend",
    "time": "2025-11-17T10:30:00.000Z",
    "version": "1.0.0",
    "group": "com.cambiaso"
  }
}
```

---

## 📊 Uso Práctico de Métricas

### Monitoreo de Performance

**1. Verificar estado de conexiones de BD:**
```bash
# Ver conexiones activas
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active

# Ver conexiones idle
curl http://localhost:8080/actuator/metrics/hikaricp.connections.idle

# Ver si hay timeouts
curl http://localhost:8080/actuator/metrics/hikaricp.connections.timeout
```

**2. Monitorear jobs ETL:**
```bash
# Jobs actualmente procesando
curl http://localhost:8080/actuator/metrics/etl.jobs.active

# Jobs atascados (>30min)
curl http://localhost:8080/actuator/metrics/etl.jobs.stuck
```

**3. Verificar rate limiting:**
```bash
# Permisos disponibles para AI Explanation
curl "http://localhost:8080/actuator/metrics/resilience4j.ratelimiter.available.permissions?tag=name:aiExplanation"
```

**4. Analizar requests HTTP:**
```bash
# Requests totales
curl http://localhost:8080/actuator/metrics/http.server.requests

# Requests con error 500
curl "http://localhost:8080/actuator/metrics/http.server.requests?tag=status:500"

# Tiempo promedio del endpoint de dashboards
curl "http://localhost:8080/actuator/metrics/http.server.requests?tag=uri:/api/v1/dashboards/{dashboardId}&tag=status:200"
```

**5. Verificar caché:**
```bash
# Cache hits
curl "http://localhost:8080/actuator/metrics/cache.gets?tag=cache:aiExplanationCache&tag=result:hit"

# Cache misses
curl "http://localhost:8080/actuator/metrics/cache.gets?tag=cache:aiExplanationCache&tag=result:miss"

# Calcular hit rate = hits / (hits + misses)
```

**6. Monitorear memoria:**
```bash
# Memoria heap usada
curl "http://localhost:8080/actuator/metrics/jvm.memory.used?tag=area:heap"

# Memoria máxima
curl "http://localhost:8080/actuator/metrics/jvm.memory.max?tag=area:heap"

# Calcular % usado = used / max * 100
```

### Alertas Recomendadas

**Configurar alertas en Prometheus/Grafana cuando:**

1. **Conexiones de BD:**
   - `hikaricp.connections.active > 18` (90% del max de 20)
   - `hikaricp.connections.timeout > 0`

2. **Jobs ETL:**
   - `etl.jobs.stuck > 0`
   - `etl.jobs.active > 10`

3. **Rate Limiting:**
   - `resilience4j.ratelimiter.waiting_threads > 5`

4. **HTTP:**
   - `http.server.requests` con `status:500` aumentando
   - `http.server.requests.active > 50`

5. **Memoria:**
   - `jvm.memory.used / jvm.memory.max > 0.85` (85% uso)
   - `jvm.gc.pause` con tiempo promedio > 1 segundo

6. **CPU:**
   - `system.cpu.usage > 0.8` (80%)
   - `process.cpu.usage > 0.6` (60%)

---
