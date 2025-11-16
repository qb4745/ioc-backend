# Backend API Brief - IOC Platform: RBAC Implementation
**Versión**: 2025-11-16 14:30:00  
**Alcance**: Feature: User Role-Based Access Control (IOC-004)  
**Stack Backend**: Spring Boot 3 + Java 21 + PostgreSQL (Supabase)  
**Contrato Format**: TypeScript (para Frontend)

---

## 1. Metadatos de Generación

**Generado**: 2025-11-16T14:30:00Z  
**Controllers Analizados**:
- `AdminUserController.java`
- `RoleController.java`
- `PermissionController.java`
- `AssignmentController.java`

**Estadísticas**:
- Controllers escaneados: 4
- Endpoints encontrados: 19
- Endpoints con seguridad: 19 (100%)
- Advertencias detectadas: 0

**Fuentes de Información**:
- Código fuente: `src/main/java/com/cambiaso/ioc/controller/admin/`
- Technical Design: `.gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md`
- Project Summary: `.gemini/project-summary.md`

---

## 2. Política de Seguridad Global

### Mecanismo de Autenticación
- **Provider**: Supabase GoTrue
- **Token Format**: JWT en header `Authorization: Bearer <token>`
- **Validación Backend**: Spring Security OAuth2 Resource Server
- **JWKS Endpoint**: `https://[SUPABASE_URL]/auth/v1/jwks`

### Matriz de Acceso por Endpoint

| Endpoint | Método | Auth | Roles | Rate Limit |
|----------|--------|------|-------|------------|
| `/api/v1/admin/users` | GET | ✅ | ADMIN | - |
| `/api/v1/admin/users/{id}` | GET | ✅ | ADMIN | - |
| `/api/v1/admin/users` | POST | ✅ | ADMIN | - |
| `/api/v1/admin/users/{id}` | PUT | ✅ | ADMIN | - |
| `/api/v1/admin/users/{id}` | DELETE | ✅ | ADMIN | - |
| `/api/v1/admin/roles` | GET | ✅ | ADMIN | - |
| `/api/v1/admin/roles/{id}` | GET | ✅ | ADMIN | - |
| `/api/v1/admin/roles` | POST | ✅ | ADMIN | - |
| `/api/v1/admin/roles/{id}` | PUT | ✅ | ADMIN | - |
| `/api/v1/admin/roles/{id}` | DELETE | ✅ | ADMIN | - |
| `/api/v1/admin/permissions` | GET | ✅ | ADMIN | - |
| `/api/v1/admin/permissions/{id}` | GET | ✅ | ADMIN | - |
| `/api/v1/admin/permissions` | POST | ✅ | ADMIN | - |
| `/api/v1/admin/permissions/{id}` | PUT | ✅ | ADMIN | - |
| `/api/v1/admin/permissions/{id}` | DELETE | ✅ | ADMIN | - |
| `/api/v1/admin/assignments/users/{userId}/roles/{roleId}` | POST | ✅ | ADMIN | - |
| `/api/v1/admin/assignments/users/{userId}/roles/{roleId}` | DELETE | ✅ | ADMIN | - |
| `/api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}` | POST | ✅ | ADMIN | - |
| `/api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}` | DELETE | ✅ | ADMIN | - |

---

## 3. Contratos de API

### 3.1. Gestión de Usuarios (AdminUserController)

#### `GET /api/v1/admin/users`

**Propósito**: Buscar y listar usuarios del sistema con paginación y filtros opcionales

**Controller**: `AdminUserController.java`  
**Método Java**: `search`  
**BSS Relacionado**: `IOC-004` (RBAC Implementation)

##### Request

```typescript
// Query Params
interface QueryParams {
  search?: string;           // Búsqueda por email o nombre
  plantaId?: number;         // Filtrar por planta específica
  isActive?: boolean;        // Filtrar por estado activo/inactivo
  page?: number;             // Default: 0
  size?: number;             // Default: 20, max: 100
  sort?: string;             // Ejemplos: "email,asc" o "createdAt,desc"
}
```

##### Response (200 OK)

```typescript
interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
  numberOfElements: number;
  empty: boolean;
}

interface UsuarioResponse {
  id: number;                  // Long (Java)
  email: string;               // Unique, case-insensitive
  fullName: string;            // Computed: "primerNombre segundoNombre primerApellido segundoApellido"
  plantaId: number | null;     // FK to plantas table
  plantaCode: string | null;   // Código de la planta
  plantaName: string | null;   // Nombre de la planta
  centroCosto: string | null;  // Max 50 chars
  fechaContrato: string | null; // ISO 8601 date (YYYY-MM-DD)
  isActive: boolean;           // Default: true
  createdAt: string;           // ISO 8601 timestamp
  updatedAt: string;           // ISO 8601 timestamp
  roles: string[];             // Array of role names
}

// Response type
type SearchUsersResponse = PageResponse<UsuarioResponse>;
```

##### Error Responses

- **400 Bad Request**: Parámetros de paginación inválidos (tamaño negativo, etc.)
- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **500 Internal Server Error**: Error interno del servidor

##### Validaciones

**Query Parameters**:
- `page`: Mínimo 0 (se clampea automáticamente)
- `size`: Entre 1 y 100 (se clampea automáticamente)
- `search`: Búsqueda case-insensitive en email y nombre completo
- `plantaId`: Debe existir en tabla plantas
- `isActive`: Boolean true/false

##### Notas de Implementación

- Implementado en `AdminUserController.search()`
- Paginación automática con límites: min=1, max=100
- Búsqueda full-text en email y nombres
- Join con tabla plantas para incluir código y nombre
- Join con user_roles y roles para incluir lista de roles
- Cacheo: No aplicable (datos dinámicos)
- Logging: INFO level en búsquedas

---

#### `GET /api/v1/admin/users/{id}`

**Propósito**: Obtener los detalles completos de un usuario específico por su ID

**Controller**: `AdminUserController.java`  
**Método Java**: `getById`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Path Params
interface PathParams {
  id: number;          // ID del usuario (Long)
}
```

##### Response (200 OK)

```typescript
interface UsuarioResponse {
  id: number;
  email: string;
  fullName: string;
  plantaId: number | null;
  plantaCode: string | null;
  plantaName: string | null;
  centroCosto: string | null;
  fechaContrato: string | null; // ISO 8601 date
  isActive: boolean;
  createdAt: string;            // ISO 8601 timestamp
  updatedAt: string;            // ISO 8601 timestamp
  roles: string[];
}
```

##### Error Responses

- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Usuario no existe o fue eliminado (soft delete)
- **500 Internal Server Error**: Error interno del servidor

##### Validaciones

**Path Parameters**:
- `id`: Debe ser un número positivo

##### Notas de Implementación

- Implementado en `AdminUserController.getById()`
- Excluye usuarios soft-deleted (deleted_at IS NULL)
- Incluye información de planta y roles asociados
- Transacción: Read-only
- Logging: INFO level

---

#### `POST /api/v1/admin/users`

**Propósito**: Crear un nuevo usuario en el sistema y opcionalmente crear su cuenta en Supabase

**Controller**: `AdminUserController.java`  
**Método Java**: `create`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Request Body
interface UsuarioCreateRequest {
  email: string;                    // @NotBlank @Email @UniqueEmail
  password?: string;                // @Size(min=6) - Para crear usuario en Supabase
  supabaseUserId?: string;          // @ValidSupabaseUUID (DEPRECATED - backward compatibility)
  primerNombre: string;             // @NotBlank
  segundoNombre?: string;           // Optional
  primerApellido: string;           // @NotBlank
  segundoApellido?: string;         // Optional
  plantaId?: number;                // FK to plantas table
  centroCosto?: string;             // @Size(max=50)
  fechaContrato?: string;           // ISO 8601 date (YYYY-MM-DD)
  roles?: string[];                 // Array of role names to assign
}
```

##### Response (201 Created)

```typescript
interface UsuarioResponse {
  id: number;
  email: string;
  fullName: string;
  plantaId: number | null;
  plantaCode: string | null;
  plantaName: string | null;
  centroCosto: string | null;
  fechaContrato: string | null;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  roles: string[];
}

// Headers
Location: /api/v1/admin/users/{id}
```

##### Error Responses

- **400 Bad Request**: 
  - Validación fallida (email inválido, campos requeridos faltantes)
  - Password menor a 6 caracteres
  - Roles especificados no existen
  - PlantaId no existe
- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **409 Conflict**: 
  - Email ya existe en el sistema
  - SupabaseUserId ya existe (si se proporciona)
- **500 Internal Server Error**: Error interno del servidor o error en creación de Supabase

##### Validaciones

**Campos Requeridos**:
- `email`: No nulo, formato email válido, único en el sistema
- `primerNombre`: No nulo, no vacío
- `primerApellido`: No nulo, no vacío

**Validaciones de Negocio**:
- Email debe ser único (case-insensitive por CITEXT)
- Si se proporciona `password`, se crea usuario en Supabase automáticamente
- Si se proporciona `supabaseUserId`, debe ser UUID válido y único
- Roles en el array deben existir en la tabla roles
- PlantaId debe existir en la tabla plantas
- CentroCosto máximo 50 caracteres
- FechaContrato debe ser fecha válida

##### Notas de Implementación

- Implementado en `AdminUserController.create()`
- Transacción: `@Transactional` - atomicidad en creación + asignación de roles
- Si se proporciona password, crea usuario en Supabase y obtiene UUID automáticamente
- Campo supabaseUserId es DEPRECATED pero soportado por compatibilidad
- Asignación de roles es idempotente (ON CONFLICT DO NOTHING)
- Logging: INFO para creación exitosa, WARN para conflictos
- Auditoría: Registra quién creó el usuario

---

#### `PUT /api/v1/admin/users/{id}`

**Propósito**: Actualizar la información de un usuario existente

**Controller**: `AdminUserController.java`  
**Método Java**: `update`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Path Params
interface PathParams {
  id: number;          // ID del usuario a actualizar
}

// Request Body
interface UsuarioUpdateRequest {
  primerNombre: string;             // @NotBlank
  segundoNombre?: string;           // Optional
  primerApellido: string;           // @NotBlank
  segundoApellido?: string;         // Optional
  plantaId?: number | null;         // Nullable to unset
  centroCosto?: string;             // @Size(max=50)
  fechaContrato?: string;           // ISO 8601 date (YYYY-MM-DD)
  isActive?: boolean;               // Activar/desactivar usuario
}
```

##### Response (200 OK)

```typescript
interface UsuarioResponse {
  id: number;
  email: string;               // No editable
  fullName: string;
  plantaId: number | null;
  plantaCode: string | null;
  plantaName: string | null;
  centroCosto: string | null;
  fechaContrato: string | null;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;           // Actualizado automáticamente
  roles: string[];             // No se modifican en este endpoint
}
```

##### Error Responses

- **400 Bad Request**: Validación fallida (campos requeridos vacíos, formato inválido)
- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Usuario no existe
- **500 Internal Server Error**: Error interno del servidor

##### Validaciones

**Campos Requeridos**:
- `primerNombre`: No nulo, no vacío
- `primerApellido`: No nulo, no vacío

**Validaciones de Negocio**:
- PlantaId debe existir si se proporciona
- CentroCosto máximo 50 caracteres
- Email NO es editable (se ignora si se envía)
- SupabaseUserId NO es editable
- Roles NO se modifican aquí (usar endpoint de assignments)

##### Notas de Implementación

- Implementado en `AdminUserController.update()`
- Transacción: `@Transactional`
- Solo actualiza campos proporcionados
- Email y supabaseUserId son inmutables
- UpdatedAt se actualiza automáticamente
- Logging: INFO para actualización exitosa

---

#### `DELETE /api/v1/admin/users/{id}`

**Propósito**: Eliminar (soft delete) un usuario del sistema

**Controller**: `AdminUserController.java`  
**Método Java**: `delete`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Path Params
interface PathParams {
  id: number;          // ID del usuario a eliminar
}
```

##### Response (204 No Content)

Sin body de respuesta.

##### Error Responses

- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Usuario no existe o ya fue eliminado
- **500 Internal Server Error**: Error interno del servidor

##### Validaciones

**Path Parameters**:
- `id`: Debe ser un número positivo

**Validaciones de Negocio**:
- No se puede eliminar el último usuario ADMIN del sistema
- Soft delete: establece deleted_at = NOW() y isActive = false
- Las asignaciones de roles se mantienen (histórico)

##### Notas de Implementación

- Implementado en `AdminUserController.delete()`
- Transacción: `@Transactional`
- Soft delete: no elimina físicamente el registro
- Establece deleted_at y isActive = false
- No elimina de Supabase (desacoplado)
- Logging: WARN level para auditoría de eliminaciones

---

### 3.2. Gestión de Roles (RoleController)

#### `GET /api/v1/admin/roles`

**Propósito**: Listar todos los roles del sistema con información de usuarios asignados y permisos

**Controller**: `RoleController.java`  
**Método Java**: `search`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Query Params
interface QueryParams {
  search?: string;           // Búsqueda por nombre o descripción del rol
  page?: number;             // Default: 0
  size?: number;             // Default: 20, max: 100
  sort?: string;             // Ejemplo: "name,asc"
}
```

##### Response (200 OK)

```typescript
interface RoleResponse {
  id: number;                  // Integer (Java)
  name: string;                // Unique, max 100 chars
  description: string | null;  // Max 255 chars
  usersCount: number;          // Conteo de usuarios con este rol
  permissions: string[];       // Array de nombres de permisos asociados
}

type SearchRolesResponse = PageResponse<RoleResponse>;
```

##### Error Responses

- **400 Bad Request**: Parámetros de paginación inválidos
- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **500 Internal Server Error**: Error interno del servidor

##### Validaciones

**Query Parameters**:
- `page`: Mínimo 0 (se clampea automáticamente)
- `size`: Entre 1 y 100 (se clampea automáticamente)
- `search`: Case-insensitive en name y description

##### Notas de Implementación

- Implementado en `RoleController.search()`
- Join con user_roles para conteo de usuarios
- Join con role_permissions y permissions para lista de permisos
- Puede usar vista vw_role_summary para optimización
- Paginación con límites automáticos
- Logging: INFO level

---

#### `GET /api/v1/admin/roles/{id}`

**Propósito**: Obtener los detalles completos de un rol específico

**Controller**: `RoleController.java`  
**Método Java**: `getById`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Path Params
interface PathParams {
  id: number;          // ID del rol (Integer)
}
```

##### Response (200 OK)

```typescript
interface RoleResponse {
  id: number;
  name: string;
  description: string | null;
  usersCount: number;
  permissions: string[];
}
```

##### Error Responses

- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Rol no existe
- **500 Internal Server Error**: Error interno del servidor

##### Notas de Implementación

- Implementado en `RoleController.getById()`
- Incluye conteo de usuarios y lista de permisos
- Read-only transaction
- Logging: INFO level

---

#### `POST /api/v1/admin/roles`

**Propósito**: Crear un nuevo rol en el sistema

**Controller**: `RoleController.java`  
**Método Java**: `create`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Request Body
interface RoleRequest {
  name: string;              // @NotBlank @Size(max=100)
  description?: string;      // @Size(max=255)
}
```

##### Response (201 Created)

```typescript
interface RoleResponse {
  id: number;
  name: string;
  description: string | null;
  usersCount: number;        // Always 0 for new role
  permissions: string[];     // Always empty array for new role
}

// Headers
Location: /api/v1/admin/roles/{id}
```

##### Error Responses

- **400 Bad Request**: 
  - Validación fallida (name vacío, excede tamaño máximo)
- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **409 Conflict**: Nombre del rol ya existe (unique constraint)
- **500 Internal Server Error**: Error interno del servidor

##### Validaciones

**Campos Requeridos**:
- `name`: No nulo, no vacío, máximo 100 caracteres, único

**Validaciones de Negocio**:
- Nombre del rol debe ser único (case-sensitive)
- Descripción es opcional, máximo 255 caracteres
- Convención: Nombres de roles en MAYÚSCULAS (ej: ADMIN, GERENTE, ANALISTA)

##### Notas de Implementación

- Implementado en `RoleController.create()`
- Transacción: `@Transactional`
- Nuevo rol no tiene usuarios ni permisos asignados inicialmente
- Logging: INFO para creación exitosa

---

#### `PUT /api/v1/admin/roles/{id}`

**Propósito**: Actualizar la información de un rol existente

**Controller**: `RoleController.java`  
**Método Java**: `update`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Path Params
interface PathParams {
  id: number;          // ID del rol a actualizar
}

// Request Body
interface RoleRequest {
  name: string;              // @NotBlank @Size(max=100)
  description?: string;      // @Size(max=255)
}
```

##### Response (200 OK)

```typescript
interface RoleResponse {
  id: number;
  name: string;
  description: string | null;
  usersCount: number;
  permissions: string[];
}
```

##### Error Responses

- **400 Bad Request**: Validación fallida
- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Rol no existe
- **409 Conflict**: Nuevo nombre ya existe en otro rol
- **500 Internal Server Error**: Error interno del servidor

##### Validaciones

**Campos Requeridos**:
- `name`: No nulo, no vacío, máximo 100 caracteres

**Validaciones de Negocio**:
- Nombre debe ser único si se cambia
- No se puede renombrar roles del sistema (ADMIN, GERENTE, ANALISTA) - opcional según política

##### Notas de Implementación

- Implementado en `RoleController.update()`
- Transacción: `@Transactional`
- Actualiza solo name y description
- Usuarios y permisos NO se modifican aquí
- Logging: INFO para actualización exitosa

---

#### `DELETE /api/v1/admin/roles/{id}`

**Propósito**: Eliminar un rol del sistema

**Controller**: `RoleController.java`  
**Método Java**: `delete`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Path Params
interface PathParams {
  id: number;          // ID del rol a eliminar
}
```

##### Response (204 No Content)

Sin body de respuesta.

##### Error Responses

- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Rol no existe
- **409 Conflict**: 
  - Rol está asignado a usuarios (no se puede eliminar)
  - Rol es un rol del sistema protegido
- **500 Internal Server Error**: Error interno del servidor

##### Validaciones

**Validaciones de Negocio**:
- No se puede eliminar si tiene usuarios asignados (usersCount > 0)
- No se puede eliminar roles del sistema: ADMIN, GERENTE, ANALISTA
- Eliminación física (hard delete) del rol y sus asociaciones con permisos

##### Notas de Implementación

- Implementado en `RoleController.delete()`
- Transacción: `@Transactional`
- Elimina en cascada role_permissions
- Valida que no haya user_roles asociados
- Logging: WARN level para auditoría

---

### 3.3. Gestión de Permisos (PermissionController)

#### `GET /api/v1/admin/permissions`

**Propósito**: Listar todos los permisos disponibles en el sistema

**Controller**: `PermissionController.java`  
**Método Java**: `search`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Query Params
interface QueryParams {
  search?: string;           // Búsqueda por nombre o descripción
  page?: number;             // Default: 0
  size?: number;             // Default: 20, max: 100
}
```

##### Response (200 OK)

```typescript
interface PermissionResponse {
  id: number;                  // Integer (Java)
  name: string;                // Unique, max 150 chars
  description: string | null;  // Max 255 chars
}

type SearchPermissionsResponse = PageResponse<PermissionResponse>;
```

##### Error Responses

- **400 Bad Request**: Parámetros de paginación inválidos
- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **500 Internal Server Error**: Error interno del servidor

##### Notas de Implementación

- Implementado en `PermissionController.search()`
- Búsqueda case-insensitive
- Paginación con límites automáticos (1-100)
- Logging: INFO level

---

#### `GET /api/v1/admin/permissions/{id}`

**Propósito**: Obtener los detalles de un permiso específico

**Controller**: `PermissionController.java`  
**Método Java**: `getById`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Path Params
interface PathParams {
  id: number;          // ID del permiso (Integer)
}
```

##### Response (200 OK)

```typescript
interface PermissionResponse {
  id: number;
  name: string;
  description: string | null;
}
```

##### Error Responses

- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Permiso no existe
- **500 Internal Server Error**: Error interno del servidor

---

#### `POST /api/v1/admin/permissions`

**Propósito**: Crear un nuevo permiso en el sistema

**Controller**: `PermissionController.java`  
**Método Java**: `create`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Request Body
interface PermissionRequest {
  name: string;              // @NotBlank @Size(max=150)
  description?: string;      // @Size(max=255)
}
```

##### Response (201 Created)

```typescript
interface PermissionResponse {
  id: number;
  name: string;
  description: string | null;
}

// Headers
Location: /api/v1/admin/permissions/{id}
```

##### Error Responses

- **400 Bad Request**: Validación fallida (name vacío o excede límite)
- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **409 Conflict**: Nombre del permiso ya existe (unique constraint)
- **500 Internal Server Error**: Error interno del servidor

##### Validaciones

**Campos Requeridos**:
- `name`: No nulo, no vacío, máximo 150 caracteres, único

**Validaciones de Negocio**:
- Nombre debe ser único
- Convención: usar snake_case (ej: read_users, write_dashboards)

##### Notas de Implementación

- Implementado en `PermissionController.create()`
- Transacción: `@Transactional`
- Logging: INFO para creación exitosa

---

#### `PUT /api/v1/admin/permissions/{id}`

**Propósito**: Actualizar la información de un permiso existente

**Controller**: `PermissionController.java`  
**Método Java**: `update`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Path Params
interface PathParams {
  id: number;          // ID del permiso a actualizar
}

// Request Body
interface PermissionRequest {
  name: string;              // @NotBlank @Size(max=150)
  description?: string;      // @Size(max=255)
}
```

##### Response (200 OK)

```typescript
interface PermissionResponse {
  id: number;
  name: string;
  description: string | null;
}
```

##### Error Responses

- **400 Bad Request**: Validación fallida
- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Permiso no existe
- **409 Conflict**: Nuevo nombre ya existe
- **500 Internal Server Error**: Error interno del servidor

---

#### `DELETE /api/v1/admin/permissions/{id}`

**Propósito**: Eliminar un permiso del sistema

**Controller**: `PermissionController.java`  
**Método Java**: `delete`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Path Params
interface PathParams {
  id: number;          // ID del permiso a eliminar
}
```

##### Response (204 No Content)

Sin body de respuesta.

##### Error Responses

- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Permiso no existe
- **409 Conflict**: Permiso está asignado a roles
- **500 Internal Server Error**: Error interno del servidor

##### Validaciones

**Validaciones de Negocio**:
- No se puede eliminar si está asignado a algún rol
- Eliminación física (hard delete)

##### Notas de Implementación

- Implementado en `PermissionController.delete()`
- Transacción: `@Transactional`
- Valida que no existan role_permissions asociados
- Logging: WARN level para auditoría

---

### 3.4. Asignaciones (AssignmentController)

#### `POST /api/v1/admin/assignments/users/{userId}/roles/{roleId}`

**Propósito**: Asignar un rol a un usuario

**Controller**: `AssignmentController.java`  
**Método Java**: `assignRoleToUser`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Path Params
interface PathParams {
  userId: number;      // ID del usuario (Long)
  roleId: number;      // ID del rol (Integer)
}

// Query Params
interface QueryParams {
  assignedBy?: number; // ID del usuario que realiza la asignación (opcional)
}
```

##### Response (200 OK)

Sin body de respuesta (solo status 200).

##### Error Responses

- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Usuario o rol no existe
- **409 Conflict**: Asignación ya existe (operación es idempotente, retorna 200)
- **500 Internal Server Error**: Error interno del servidor

##### Validaciones

**Path Parameters**:
- `userId`: Debe existir y no estar soft-deleted
- `roleId`: Debe existir

**Validaciones de Negocio**:
- Usuario debe estar activo (isActive = true)
- Operación idempotente: si ya existe, no falla

##### Notas de Implementación

- Implementado en `AssignmentController.assignRoleToUser()`
- Transacción: `@Transactional`
- Operación idempotente (ON CONFLICT DO NOTHING en DB)
- Campo assigned_at se establece a NOW()
- Campo assigned_by_user_id opcional para auditoría
- Logging: INFO para asignación nueva, DEBUG si ya existía

---

#### `DELETE /api/v1/admin/assignments/users/{userId}/roles/{roleId}`

**Propósito**: Remover un rol de un usuario

**Controller**: `AssignmentController.java`  
**Método Java**: `removeRoleFromUser`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Path Params
interface PathParams {
  userId: number;      // ID del usuario (Long)
  roleId: number;      // ID del rol (Integer)
}
```

##### Response (204 No Content)

Sin body de respuesta.

##### Error Responses

- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Usuario, rol, o asignación no existe
- **409 Conflict**: No se puede remover el único rol ADMIN del último admin
- **500 Internal Server Error**: Error interno del servidor

##### Validaciones

**Validaciones de Negocio**:
- No se puede remover rol ADMIN del último usuario ADMIN del sistema
- Operación idempotente: si no existe, retorna 204

##### Notas de Implementación

- Implementado en `AssignmentController.removeRoleFromUser()`
- Transacción: `@Transactional`
- Validación de seguridad para evitar quedarse sin admins
- Logging: WARN para remoción de roles

---

#### `POST /api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}`

**Propósito**: Asignar un permiso a un rol

**Controller**: `AssignmentController.java`  
**Método Java**: `assignPermissionToRole`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Path Params
interface PathParams {
  roleId: number;         // ID del rol (Integer)
  permissionId: number;   // ID del permiso (Integer)
}
```

##### Response (200 OK)

Sin body de respuesta (solo status 200).

##### Error Responses

- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Rol o permiso no existe
- **409 Conflict**: Asignación ya existe (operación es idempotente, retorna 200)
- **500 Internal Server Error**: Error interno del servidor

##### Validaciones

**Path Parameters**:
- `roleId`: Debe existir
- `permissionId`: Debe existir

**Validaciones de Negocio**:
- Operación idempotente

##### Notas de Implementación

- Implementado en `AssignmentController.assignPermissionToRole()`
- Transacción: `@Transactional`
- Operación idempotente (ON CONFLICT DO NOTHING)
- Campo created_at se establece a NOW()
- Logging: INFO para asignación nueva

---

#### `DELETE /api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}`

**Propósito**: Remover un permiso de un rol

**Controller**: `AssignmentController.java`  
**Método Java**: `removePermissionFromRole`  
**BSS Relacionado**: `IOC-004`

##### Request

```typescript
// Path Params
interface PathParams {
  roleId: number;         // ID del rol (Integer)
  permissionId: number;   // ID del permiso (Integer)
}
```

##### Response (204 No Content)

Sin body de respuesta.

##### Error Responses

- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Rol, permiso, o asignación no existe
- **500 Internal Server Error**: Error interno del servidor

##### Validaciones

**Validaciones de Negocio**:
- Operación idempotente: si no existe, retorna 204

##### Notas de Implementación

- Implementado en `AssignmentController.removePermissionFromRole()`
- Transacción: `@Transactional`
- Operación idempotente
- Logging: WARN para remoción de permisos

---

## 4. Estructura de DTOs Completa

### Request DTOs

```typescript
// Usuarios
interface UsuarioCreateRequest {
  email: string;                    // String (Java) - Required, Email format, Unique
  password?: string;                // String (Java) - Min 6 chars, for Supabase creation
  supabaseUserId?: string;          // UUID (Java) - DEPRECATED, backward compatibility
  primerNombre: string;             // String (Java) - Required
  segundoNombre?: string;           // String (Java) - Optional
  primerApellido: string;           // String (Java) - Required
  segundoApellido?: string;         // String (Java) - Optional
  plantaId?: number;                // Integer (Java) - Optional FK
  centroCosto?: string;             // String (Java) - Max 50 chars
  fechaContrato?: string;           // LocalDate (Java) → ISO 8601 (YYYY-MM-DD)
  roles?: string[];                 // List<String> (Java) - Array of role names
}

interface UsuarioUpdateRequest {
  primerNombre: string;             // String (Java) - Required
  segundoNombre?: string;           // String (Java) - Optional
  primerApellido: string;           // String (Java) - Required
  segundoApellido?: string;         // String (Java) - Optional
  plantaId?: number | null;         // Integer (Java) - Nullable
  centroCosto?: string;             // String (Java) - Max 50 chars
  fechaContrato?: string;           // LocalDate (Java) → ISO 8601
  isActive?: boolean;               // Boolean (Java)
}

// Roles
interface RoleRequest {
  name: string;                     // String (Java) - Required, Max 100 chars, Unique
  description?: string;             // String (Java) - Max 255 chars
}

// Permisos
interface PermissionRequest {
  name: string;                     // String (Java) - Required, Max 150 chars, Unique
  description?: string;             // String (Java) - Max 255 chars
}
```

### Response DTOs

```typescript
// Usuarios
interface UsuarioResponse {
  id: number;                       // Long (Java)
  email: string;                    // String (Java)
  fullName: string;                 // String (Java) - Computed
  plantaId: number | null;          // Integer (Java)
  plantaCode: string | null;        // String (Java)
  plantaName: string | null;        // String (Java)
  centroCosto: string | null;       // String (Java)
  fechaContrato: string | null;     // LocalDate (Java) → ISO 8601
  isActive: boolean;                // Boolean (Java)
  createdAt: string;                // OffsetDateTime (Java) → ISO 8601
  updatedAt: string;                // OffsetDateTime (Java) → ISO 8601
  roles: string[];                  // List<String> (Java)
}

// Roles
interface RoleResponse {
  id: number;                       // Integer (Java)
  name: string;                     // String (Java)
  description: string | null;       // String (Java)
  usersCount: number;               // Integer (Java)
  permissions: string[];            // List<String> (Java)
}

// Permisos
interface PermissionResponse {
  id: number;                       // Integer (Java)
  name: string;                     // String (Java)
  description: string | null;       // String (Java)
}
```

---

## 5. Estructura de Errores Estándar

Todos los endpoints retornan errores en este formato:

```typescript
interface ErrorResponse {
  timestamp: string;        // ISO 8601
  status: number;           // Código HTTP
  error: string;            // Mensaje estándar HTTP
  message: string;          // Descripción detallada del error
  path: string;             // Ruta del endpoint que falló
  traceId?: string;         // UUID para tracing (si está configurado)
}
```

**Ejemplo - 400 Bad Request**:
```json
{
  "timestamp": "2025-11-16T14:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: email must be a valid email address",
  "path": "/api/v1/admin/users",
  "traceId": "abc-123-def-456"
}
```

**Ejemplo - 401 Unauthorized**:
```json
{
  "timestamp": "2025-11-16T14:30:00.000Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is invalid or expired",
  "path": "/api/v1/admin/users"
}
```

**Ejemplo - 403 Forbidden**:
```json
{
  "timestamp": "2025-11-16T14:30:00.000Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Required authority: ROLE_ADMIN",
  "path": "/api/v1/admin/users"
}
```

**Ejemplo - 404 Not Found**:
```json
{
  "timestamp": "2025-11-16T14:30:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "User with id 123 not found",
  "path": "/api/v1/admin/users/123"
}
```

**Ejemplo - 409 Conflict**:
```json
{
  "timestamp": "2025-11-16T14:30:00.000Z",
  "status": 409,
  "error": "Conflict",
  "message": "Email already exists: user@example.com",
  "path": "/api/v1/admin/users"
}
```

---

## 6. Mapeo de Tipos Java → TypeScript

| Tipo Java | Tipo TypeScript | Notas |
|-----------|----------------|-------|
| `String` | `string` | |
| `Integer` | `number` | Signed 32-bit |
| `Long` | `number` | Signed 64-bit (JavaScript safe up to 2^53-1) |
| `Boolean` | `boolean` | |
| `LocalDate` | `string` | ISO 8601 date (YYYY-MM-DD) |
| `LocalDateTime` | `string` | ISO 8601 timestamp (sin timezone) |
| `OffsetDateTime` | `string` | ISO 8601 timestamp con timezone |
| `UUID` | `string` | UUID formato estándar |
| `List<T>` | `T[]` | Array |
| `Map<K,V>` | `Record<K, V>` | |
| `enum` | `'value1' \| 'value2'` | Union type |
| `Page<T>` | `PageResponse<T>` | Ver estructura arriba |

---

## 7. Consideraciones de Integración

### 7.1. Autenticación

- El Frontend debe incluir JWT de Supabase en header `Authorization: Bearer <token>`
- Tokens se obtienen del cliente Supabase: `supabase.auth.getSession()`
- Interceptor Axios debe inyectar automáticamente el token en todas las requests
- El backend valida el JWT contra el JWKS de Supabase

**Ejemplo de configuración Axios**:
```typescript
import axios from 'axios';
import { supabase } from './supabase';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
});

api.interceptors.request.use(async (config) => {
  const { data: { session } } = await supabase.auth.getSession();
  if (session?.access_token) {
    config.headers.Authorization = `Bearer ${session.access_token}`;
  }
  return config;
});

export default api;
```

### 7.2. Manejo de Errores

- Implementar interceptor de respuesta en Axios para manejo centralizado
- **Códigos 401**: Redirigir automáticamente a `/login`
- **Códigos 403**: Mostrar toast/modal "No tienes permisos para esta acción"
- **Códigos 404**: Mostrar mensaje "Recurso no encontrado"
- **Códigos 409**: Mostrar mensaje de conflicto específico (ej: "Email ya existe")
- **Códigos 500**: Mostrar mensaje genérico "Error del servidor. Intenta más tarde"

**Ejemplo de interceptor de errores**:
```typescript
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      window.location.href = '/login';
    } else if (error.response?.status === 403) {
      toast.error('No tienes permisos para esta acción');
    } else if (error.response?.status === 409) {
      toast.error(error.response.data.message);
    } else if (error.response?.status >= 500) {
      toast.error('Error del servidor. Intenta más tarde');
    }
    return Promise.reject(error);
  }
);
```

### 7.3. Paginación

- El backend usa Spring Data Pageable con estructura estándar
- Default page size: 20
- Max page size: 100 (clampeado automáticamente)
- Sort format: `field,direction` (ej: `email,asc` o `createdAt,desc`)

**Ejemplo de uso**:
```typescript
interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
}

const getUsers = async (params: PaginationParams = {}) => {
  const response = await api.get('/api/v1/admin/users', { params });
  return response.data;
};

// Uso
const usersPage = await getUsers({ page: 0, size: 20, sort: 'email,asc' });
```

### 7.4. Validaciones Client-Side

- Duplicar validaciones del backend en el frontend para feedback inmediato
- Usar Zod schemas que repliquen constraints de Jakarta Validation
- Validar antes de hacer request para mejor UX

**Ejemplo con Zod**:
```typescript
import { z } from 'zod';

export const usuarioCreateSchema = z.object({
  email: z.string().email('Email inválido').min(1, 'Email requerido'),
  password: z.string().min(6, 'Contraseña mínimo 6 caracteres').optional(),
  primerNombre: z.string().min(1, 'Primer nombre requerido'),
  segundoNombre: z.string().optional(),
  primerApellido: z.string().min(1, 'Primer apellido requerido'),
  segundoApellido: z.string().optional(),
  plantaId: z.number().int().positive().optional(),
  centroCosto: z.string().max(50, 'Máximo 50 caracteres').optional(),
  fechaContrato: z.string().regex(/^\d{4}-\d{2}-\d{2}$/).optional(),
  roles: z.array(z.string()).optional(),
});

export type UsuarioCreateInput = z.infer<typeof usuarioCreateSchema>;
```

---

## 8. Endpoints por Módulo (Resumen)

### Administración de Usuarios (`/api/v1/admin/users`)
| Método | Endpoint | Propósito |
|--------|----------|-----------|
| GET | `/api/v1/admin/users` | Listar usuarios con filtros |
| GET | `/api/v1/admin/users/{id}` | Obtener usuario por ID |
| POST | `/api/v1/admin/users` | Crear nuevo usuario |
| PUT | `/api/v1/admin/users/{id}` | Actualizar usuario |
| DELETE | `/api/v1/admin/users/{id}` | Eliminar usuario (soft delete) |

### Gestión de Roles (`/api/v1/admin/roles`)
| Método | Endpoint | Propósito |
|--------|----------|-----------|
| GET | `/api/v1/admin/roles` | Listar roles con detalles |
| GET | `/api/v1/admin/roles/{id}` | Obtener rol por ID |
| POST | `/api/v1/admin/roles` | Crear nuevo rol |
| PUT | `/api/v1/admin/roles/{id}` | Actualizar rol |
| DELETE | `/api/v1/admin/roles/{id}` | Eliminar rol |

### Gestión de Permisos (`/api/v1/admin/permissions`)
| Método | Endpoint | Propósito |
|--------|----------|-----------|
| GET | `/api/v1/admin/permissions` | Listar permisos |
| GET | `/api/v1/admin/permissions/{id}` | Obtener permiso por ID |
| POST | `/api/v1/admin/permissions` | Crear nuevo permiso |
| PUT | `/api/v1/admin/permissions/{id}` | Actualizar permiso |
| DELETE | `/api/v1/admin/permissions/{id}` | Eliminar permiso |

### Asignaciones (`/api/v1/admin/assignments`)
| Método | Endpoint | Propósito |
|--------|----------|-----------|
| POST | `/assignments/users/{userId}/roles/{roleId}` | Asignar rol a usuario |
| DELETE | `/assignments/users/{userId}/roles/{roleId}` | Remover rol de usuario |
| POST | `/assignments/roles/{roleId}/permissions/{permissionId}` | Asignar permiso a rol |
| DELETE | `/assignments/roles/{roleId}/permissions/{permissionId}` | Remover permiso de rol |

---

## 9. Próximos Pasos

### Para el Equipo Frontend ✅

1. **Revisar contratos de API** en este brief
2. **Actualizar `src/services/api.ts`** con tipos TypeScript generados
3. **Implementar Zod schemas** para validación client-side (ver sección 7.4)
4. **Crear servicios específicos**:
   - `src/services/userService.ts`
   - `src/services/roleService.ts`
   - `src/services/permissionService.ts`
   - `src/services/assignmentService.ts`
5. **Implementar hooks personalizados** con React Query:
   - `useUsers()`, `useUser(id)`, `useCreateUser()`, etc.
   - `useRoles()`, `useRole(id)`, etc.
6. **Implementar manejo de errores** según ErrorResponse (ver sección 5)
7. **Crear componentes UI** para las vistas de administración:
   - `/admin/users` - Tabla con filtros y paginación
   - `/admin/users/create` - Formulario de creación
   - `/admin/users/{id}/edit` - Formulario de edición
   - `/admin/roles` - Gestión de roles
   - `/admin/permissions` - Gestión de permisos
8. **Escribir tests con MSW** usando estos contratos

### Para el Equipo Backend 🔲

1. **Validar** que la información extraída sea correcta
2. **Completar JavaDoc** faltante en controllers y services
3. **Actualizar OpenAPI/Swagger** con ejemplos de request/response
4. **Notificar cambios** de endpoints al Frontend con anticipación
5. **Mantener sincronizado** este brief con el código (versionar cambios)

---

## 10. Advertencias y Limitaciones

### ⚠️ Campos Deprecados

- `UsuarioCreateRequest.supabaseUserId`: Este campo está DEPRECATED. Se mantiene por compatibilidad pero se recomienda usar `password` para crear automáticamente el usuario en Supabase.

### ⚠️ Operaciones Idempotentes

Los siguientes endpoints son idempotentes por diseño:
- `POST /api/v1/admin/assignments/users/{userId}/roles/{roleId}` - Si la asignación ya existe, retorna 200
- `DELETE /api/v1/admin/assignments/users/{userId}/roles/{roleId}` - Si la asignación no existe, retorna 204
- `POST /api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}` - Si la asignación ya existe, retorna 200
- `DELETE /api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}` - Si la asignación no existe, retorna 204

### ⚠️ Limitaciones de Paginación

- Tamaño máximo de página: 100 elementos
- El backend clampea automáticamente valores fuera de rango (1-100)
- Números de página negativos se convierten a 0

### ⚠️ Soft Delete

- Los usuarios eliminados tienen `deleted_at != NULL` y `isActive = false`
- No aparecen en listados ni búsquedas
- Las asignaciones de roles se mantienen para histórico
- No se elimina de Supabase automáticamente

### ⚠️ Seguridad

- **TODOS los endpoints requieren ROLE_ADMIN**
- No hay endpoints públicos en este módulo
- El token JWT debe incluir el claim de rol ADMIN
- La validación se hace en Spring Security, no en lógica de negocio

---

## 11. Ejemplos de Uso Completos

### Crear Usuario con Roles

```typescript
// Request
POST /api/v1/admin/users
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "email": "nuevo.usuario@cambiaso.cl",
  "password": "SecurePass123!",
  "primerNombre": "Juan",
  "segundoNombre": "Carlos",
  "primerApellido": "Pérez",
  "segundoApellido": "González",
  "plantaId": 2,
  "centroCosto": "CC-001",
  "fechaContrato": "2025-11-01",
  "roles": ["ANALISTA"]
}

// Response 201 Created
Location: /api/v1/admin/users/42

{
  "id": 42,
  "email": "nuevo.usuario@cambiaso.cl",
  "fullName": "Juan Carlos Pérez González",
  "plantaId": 2,
  "plantaCode": "SCL",
  "plantaName": "Santiago Centro",
  "centroCosto": "CC-001",
  "fechaContrato": "2025-11-01",
  "isActive": true,
  "createdAt": "2025-11-16T14:30:00.000Z",
  "updatedAt": "2025-11-16T14:30:00.000Z",
  "roles": ["ANALISTA"]
}
```

### Buscar Usuarios Activos de una Planta

```typescript
// Request
GET /api/v1/admin/users?plantaId=2&isActive=true&page=0&size=20&sort=email,asc
Authorization: Bearer <jwt-token>

// Response 200 OK
{
  "content": [
    {
      "id": 42,
      "email": "nuevo.usuario@cambiaso.cl",
      "fullName": "Juan Carlos Pérez González",
      "plantaId": 2,
      "plantaCode": "SCL",
      "plantaName": "Santiago Centro",
      "centroCosto": "CC-001",
      "fechaContrato": "2025-11-01",
      "isActive": true,
      "createdAt": "2025-11-16T14:30:00.000Z",
      "updatedAt": "2025-11-16T14:30:00.000Z",
      "roles": ["ANALISTA"]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": true, "unsorted": false, "empty": false },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "size": 20,
  "number": 0,
  "numberOfElements": 1,
  "empty": false
}
```

### Asignar Rol a Usuario

```typescript
// Request
POST /api/v1/admin/assignments/users/42/roles/2
Authorization: Bearer <jwt-token>

// Response 200 OK
(Sin body)
```

### Crear Rol con Permisos

```typescript
// 1. Crear el rol
POST /api/v1/admin/roles
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "name": "SUPERVISOR",
  "description": "Supervisor de planta con permisos de lectura"
}

// Response 201 Created
{
  "id": 5,
  "name": "SUPERVISOR",
  "description": "Supervisor de planta con permisos de lectura",
  "usersCount": 0,
  "permissions": []
}

// 2. Asignar permisos al rol
POST /api/v1/admin/assignments/roles/5/permissions/1
Authorization: Bearer <jwt-token>

POST /api/v1/admin/assignments/roles/5/permissions/3
Authorization: Bearer <jwt-token>

// 3. Verificar rol con permisos
GET /api/v1/admin/roles/5
Authorization: Bearer <jwt-token>

// Response 200 OK
{
  "id": 5,
  "name": "SUPERVISOR",
  "description": "Supervisor de planta con permisos de lectura",
  "usersCount": 0,
  "permissions": ["read_users", "read_dashboards"]
}
```

---

## 12. Changelog del Brief

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | 2025-11-16 | Versión inicial - Feature IOC-004 RBAC |

---

**Generado automáticamente por**: Backend API Brief Generator  
**Basado en**: Technical Design TD-IOC-004-User-Role-Management-claude.md  
**Comando**: Análisis de feature específica RBAC (IOC-004)

