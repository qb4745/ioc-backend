# Backend Sync Brief - IOC-004: User Role Management
**Versión**: 2025-10-27 (v1.0)  
**Stack Target**: Spring Boot 3.5.5 + Java 21 + PostgreSQL (Supabase)  
**Contrato Format**: TypeScript (para referencia Frontend)  
**Estado**: ✅ Implementado y Validado (Score: 94/100)

---

## 1. Metadatos de Generación

**Generado**: 2025-10-27T00:00:00Z  
**Fuentes Analizadas**:
- `.gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md`
- `.gemini/validation/IOC-004-blueprint-validation.md`
- Implementación verificada en `src/main/java/com/cambiaso/ioc/controller/admin/*`

**Estadísticas**:
- Endpoints implementados: 17
- Controllers: 4 (AdminUserController, RoleController, PermissionController, AssignmentController)
- Conflictos detectados: 0
- Advertencias: 1 (OpenAPI/Swagger pendiente de añadir)

---

## 2. Política de Seguridad Global

### Mecanismo de Autenticación
- **Provider**: Supabase GoTrue
- **Token Format**: JWT en header `Authorization: Bearer <token>`
- **Validación Backend**: Spring Security como Resource Server
- **JWKS Endpoint**: `https://[SUPABASE_URL]/auth/v1/.well-known/jwks.json`
- **Claims Extraídos**: `sub` (UUID del usuario), `realm_access.roles`, `roles`

### Autorización
- **Mapeo de Roles**: Claims `realm_access.roles` y `roles` → `ROLE_ADMIN`, `ROLE_GERENTE`, `ROLE_ANALISTA`
- **Política General**: Todos los endpoints admin requieren `ROLE_ADMIN`
- **RLS en Supabase**: Políticas habilitadas en tablas `app_users`, `roles`, `permissions`, `user_roles`, `role_permissions`

### Matriz de Acceso por Endpoint

| Endpoint | Método | Auth | Roles Requeridos | Rate Limit |
|----------|--------|------|------------------|------------|
| `/api/v1/admin/users` | GET | ✅ | ADMIN | 100/min |
| `/api/v1/admin/users` | POST | ✅ | ADMIN | 20/min |
| `/api/v1/admin/users/{id}` | GET | ✅ | ADMIN | 100/min |
| `/api/v1/admin/users/{id}` | PUT | ✅ | ADMIN | 50/min |
| `/api/v1/admin/users/{id}` | DELETE | ✅ | ADMIN | 10/min |
| `/api/v1/admin/roles` | GET | ✅ | ADMIN | 100/min |
| `/api/v1/admin/roles` | POST | ✅ | ADMIN | 20/min |
| `/api/v1/admin/roles/{id}` | GET | ✅ | ADMIN | 100/min |
| `/api/v1/admin/roles/{id}` | PUT | ✅ | ADMIN | 50/min |
| `/api/v1/admin/roles/{id}` | DELETE | ✅ | ADMIN | 10/min |
| `/api/v1/admin/permissions` | GET | ✅ | ADMIN | 100/min |
| `/api/v1/admin/permissions` | POST | ✅ | ADMIN | 20/min |
| `/api/v1/admin/permissions/{id}` | GET | ✅ | ADMIN | 100/min |
| `/api/v1/admin/permissions/{id}` | PUT | ✅ | ADMIN | 50/min |
| `/api/v1/admin/permissions/{id}` | DELETE | ✅ | ADMIN | 10/min |
| `/api/v1/admin/assignments/users/{userId}/roles/{roleId}` | POST | ✅ | ADMIN | 30/min |
| `/api/v1/admin/assignments/users/{userId}/roles/{roleId}` | DELETE | ✅ | ADMIN | 30/min |
| `/api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}` | POST | ✅ | ADMIN | 30/min |
| `/api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}` | DELETE | ✅ | ADMIN | 30/min |

---

## 3. Contratos de API

### 3.1. Gestión de Usuarios

#### `POST /api/v1/admin/users`

**Propósito**: Crear un nuevo usuario en el sistema con información de perfil y asignación opcional de roles.

**Controlador**: `AdminUserController.create()`

##### Request

```typescript
// Content-Type: application/json
interface UsuarioCreateRequest {
  email: string;                  // Email único (CITEXT, case-insensitive)
  primerNombre: string;           // Primer nombre (requerido)
  segundoNombre?: string;         // Segundo nombre (opcional)
  primerApellido: string;         // Primer apellido (requerido)
  segundoApellido?: string;       // Segundo apellido (opcional)
  plantaId?: number;              // ID de la planta asignada (FK a plantas)
  centroCosto?: string;           // Centro de costo (max 50 chars)
  fechaContrato?: string;         // Fecha ISO 8601 (YYYY-MM-DD)
  supabaseUserId: string;         // UUID del usuario en Supabase Auth (requerido, único)
  roles?: string[];               // Array de nombres de roles (opcional, ej: ["ADMIN"])
}
```

**Validaciones**:
- `email`: formato válido, único en la base de datos
- `supabaseUserId`: formato UUID válido, único
- `primerNombre` y `primerApellido`: requeridos, no vacíos
- `centroCosto`: máximo 50 caracteres
- `fechaContrato`: formato de fecha válido si se proporciona

##### Response (201 Created)

```typescript
interface UsuarioResponse {
  id: number;                     // ID interno del usuario
  email: string;
  fullName: string;               // Nombre completo calculado (concatenación de nombres)
  plantaId?: number;
  plantaCode?: string;            // Código de la planta
  plantaName?: string;            // Nombre de la planta
  centroCosto?: string;
  fechaContrato?: string;         // ISO 8601
  isActive: boolean;              // true por defecto
  createdAt: string;              // ISO 8601 timestamp
  updatedAt: string;              // ISO 8601 timestamp
  roles: string[];                // Array de nombres de roles asignados
}

// Headers
Location: /api/v1/admin/users/{id}
```

##### Error Responses

- **400 Bad Request**: Errores de validación (email inválido, campos requeridos faltantes)
- **401 Unauthorized**: Token JWT de Supabase inválido o expirado
- **403 Forbidden**: Usuario sin rol ADMIN
- **409 Conflict**: Email o supabaseUserId ya existen en la base de datos
- **500 Internal Server Error**: Error al crear usuario

##### Notas de Implementación

- Soft delete implementado: usuarios eliminados tienen `deleted_at` no nulo
- La columna `fullName` se calcula en el mapper (concatena nombres no nulos)
- Si se proporciona `plantaId`, se valida que exista en la tabla `plantas`
- Los roles se asignan después de crear el usuario (transacción atómica)
- Logging obligatorio: crear usuario con email (sin PII en INFO level)

---

#### `GET /api/v1/admin/users`

**Propósito**: Listar usuarios con búsqueda, filtros y paginación.

**Controlador**: `AdminUserController.search()`

##### Request

```typescript
// Query Params
interface UserSearchParams {
  search?: string;                // Búsqueda por nombre o email (opcional)
  plantaId?: number;              // Filtro por planta
  isActive?: boolean;             // Filtro por estado activo/inactivo
  page?: number;                  // Número de página (0-indexed, default: 0)
  size?: number;                  // Tamaño de página (default: 20, max: 100)
  sort?: string;                  // Campo de ordenamiento (ej: "email,asc")
}
```

##### Response (200 OK)

```typescript
interface UserListResponse {
  content: UsuarioResponse[];     // Array de usuarios
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
    };
  };
  totalElements: number;          // Total de registros
  totalPages: number;             // Total de páginas
  last: boolean;                  // Es la última página
  first: boolean;                 // Es la primera página
  numberOfElements: number;       // Elementos en esta página
  size: number;
  number: number;
}
```

##### Error Responses

- **401 Unauthorized**: Token inválido
- **403 Forbidden**: Usuario sin rol ADMIN
- **500 Internal Server Error**: Error al consultar base de datos

##### Notas de Implementación

- La búsqueda (`search`) busca en `email`, `primer_nombre`, `primer_apellido` (case-insensitive)
- El page size está limitado (clamped) a un máximo de 100 por performance
- Se usa `AppUserSearchRepositoryImpl` con criteria API para filtros dinámicos
- Los usuarios con `deleted_at` no nulo NO se devuelven en listados

---

#### `GET /api/v1/admin/users/{id}`

**Propósito**: Obtener detalles completos de un usuario específico.

**Controlador**: `AdminUserController.getById()`

##### Request

```typescript
// Path Params
interface PathParams {
  id: number;                     // ID del usuario
}
```

##### Response (200 OK)

```typescript
// Devuelve: UsuarioResponse (ver estructura arriba)
```

##### Error Responses

- **401 Unauthorized**: Token inválido
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Usuario no existe o fue eliminado (soft delete)
- **500 Internal Server Error**: Error al consultar

---

#### `PUT /api/v1/admin/users/{id}`

**Propósito**: Actualizar información de un usuario existente.

**Controlador**: `AdminUserController.update()`

##### Request

```typescript
// Path Params
interface PathParams {
  id: number;
}

// Body
interface UsuarioUpdateRequest {
  primerNombre: string;           // Requerido
  segundoNombre?: string;
  primerApellido: string;         // Requerido
  segundoApellido?: string;
  plantaId?: number;              // null para desvincular de planta
  centroCosto?: string;
  fechaContrato?: string;         // ISO 8601
  isActive?: boolean;             // Cambiar estado activo
}
```

**Nota**: El email y `supabaseUserId` NO son editables tras la creación.

##### Response (200 OK)

```typescript
// Devuelve: UsuarioResponse actualizado
```

##### Error Responses

- **400 Bad Request**: Validación fallida
- **401 Unauthorized**: Token inválido
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Usuario no existe
- **500 Internal Server Error**: Error al actualizar

##### Notas de Implementación

- La columna `updated_at` se actualiza automáticamente (trigger SQL)
- Si `plantaId` es `null`, se desvincula al usuario de la planta
- Si `isActive` cambia a `false`, el usuario no podrá autenticarse (lógica futura)

---

#### `DELETE /api/v1/admin/users/{id}`

**Propósito**: Eliminar un usuario (soft delete).

**Controlador**: `AdminUserController.delete()`

##### Request

```typescript
// Path Params
interface PathParams {
  id: number;
}
```

##### Response (204 No Content)

Sin cuerpo de respuesta.

##### Error Responses

- **401 Unauthorized**: Token inválido
- **403 Forbidden**: Usuario sin rol ADMIN
- **404 Not Found**: Usuario no existe
- **500 Internal Server Error**: Error al eliminar

##### Notas de Implementación

- Soft delete: se establece `deleted_at = CURRENT_TIMESTAMP` y `is_active = false`
- Los roles asignados al usuario permanecen en `user_roles` (histórico)
- El usuario desaparece de listados y búsquedas tras el soft delete

---

### 3.2. Gestión de Roles

#### `POST /api/v1/admin/roles`

**Propósito**: Crear un nuevo rol en el sistema.

**Controlador**: `RoleController.create()`

##### Request

```typescript
interface RoleRequest {
  name: string;                   // Nombre único del rol (ej: "SUPERVISOR")
  description?: string;           // Descripción opcional
}
```

**Validaciones**:
- `name`: requerido, único (case-insensitive)

##### Response (201 Created)

```typescript
interface RoleResponse {
  id: number;
  name: string;
  description?: string;
  createdAt: string;              // ISO 8601
  updatedAt: string;
  usersCount: number;             // Cantidad de usuarios con este rol (default: 0)
  permissions: string[];          // Array de nombres de permisos asignados (default: [])
}

// Headers
Location: /api/v1/admin/roles/{id}
```

##### Error Responses

- **400 Bad Request**: Nombre vacío o inválido
- **401 Unauthorized**: Token inválido
- **403 Forbidden**: Usuario sin rol ADMIN
- **409 Conflict**: Nombre de rol ya existe
- **500 Internal Server Error**: Error al crear

##### Notas de Implementación

- Los roles seed (`ADMIN`, `GERENTE`, `ANALISTA`) ya están creados en la migración SQL
- El campo `usersCount` se calcula consultando `user_roles` (en `enrichResponse`)
- Los permisos se asignan tras crear el rol (vía `AssignmentController`)

---

#### `GET /api/v1/admin/roles`

**Propósito**: Listar todos los roles con detalles (usuarios asignados, permisos).

**Controlador**: `RoleController.search()`

##### Request

```typescript
// Query Params
interface RoleSearchParams {
  search?: string;                // Búsqueda por nombre (opcional)
  page?: number;                  // Default: 0
  size?: number;                  // Default: 20, max: 100
  sort?: string;
}
```

##### Response (200 OK)

```typescript
interface RoleListResponse {
  content: RoleResponse[];        // Array de roles con usersCount y permissions
  // ... paginación estándar (ver UserListResponse)
}
```

##### Error Responses

- **401 Unauthorized**: Token inválido
- **403 Forbidden**: Usuario sin rol ADMIN
- **500 Internal Server Error**: Error al consultar

---

#### `GET /api/v1/admin/roles/{id}`

**Propósito**: Obtener detalles completos de un rol específico.

**Controlador**: `RoleController.getById()`

##### Request

```typescript
interface PathParams {
  id: number;
}
```

##### Response (200 OK)

```typescript
// Devuelve: RoleResponse con usersCount y permissions calculados
```

##### Error Responses

- **404 Not Found**: Rol no existe
- **401/403**: Autenticación/autorización fallida

---

#### `PUT /api/v1/admin/roles/{id}`

**Propósito**: Actualizar nombre y/o descripción de un rol.

**Controlador**: `RoleController.update()`

##### Request

```typescript
interface PathParams {
  id: number;
}

// Body
interface RoleRequest {
  name: string;
  description?: string;
}
```

##### Response (200 OK)

```typescript
// Devuelve: RoleResponse actualizado
```

##### Error Responses

- **400 Bad Request**: Validación fallida
- **404 Not Found**: Rol no existe
- **409 Conflict**: Nuevo nombre ya existe en otro rol

##### Notas de Implementación

- El nombre puede cambiarse solo si no genera conflicto con otro rol
- Los usuarios y permisos asignados al rol NO se modifican en este endpoint

---

#### `DELETE /api/v1/admin/roles/{id}`

**Propósito**: Eliminar un rol (solo si no está en uso).

**Controlador**: `RoleController.delete()`

##### Request

```typescript
interface PathParams {
  id: number;
}
```

##### Response (204 No Content)

Sin cuerpo.

##### Error Responses

- **404 Not Found**: Rol no existe
- **409 Conflict**: El rol está asignado a usuarios o tiene permisos asociados
- **401/403**: Autenticación/autorización fallida

##### Notas de Implementación

- Protección implementada: si `userRoleRepository.existsByIdRoleId(id)` devuelve `true`, se lanza `ResourceConflictException`
- Mismo check para `rolePermissionRepository.existsByRole_Id(id)`
- Los roles seed (`ADMIN`, `GERENTE`, `ANALISTA`) pueden protegerse con lógica adicional

---

### 3.3. Gestión de Permisos

#### `POST /api/v1/admin/permissions`

**Propósito**: Crear un nuevo permiso en el sistema.

**Controlador**: `PermissionController.create()`

##### Request

```typescript
interface PermissionRequest {
  name: string;                   // Nombre único (ej: "USER_READ", "DASHBOARD_EDIT")
  description?: string;
}
```

##### Response (201 Created)

```typescript
interface PermissionResponse {
  id: number;
  name: string;
  description?: string;
}

// Headers
Location: /api/v1/admin/permissions/{id}
```

##### Error Responses

- **400 Bad Request**: Nombre vacío
- **409 Conflict**: Nombre de permiso ya existe
- **401/403**: Autenticación/autorización fallida

---

#### `GET /api/v1/admin/permissions`

**Propósito**: Listar todos los permisos disponibles.

**Controlador**: `PermissionController.search()`

##### Request

```typescript
// Query Params
interface PermissionSearchParams {
  search?: string;
  page?: number;
  size?: number;
}
```

##### Response (200 OK)

```typescript
interface PermissionListResponse {
  content: PermissionResponse[];
  // ... paginación estándar
}
```

---

#### `GET /api/v1/admin/permissions/{id}`

**Propósito**: Obtener un permiso por ID.

**Controlador**: `PermissionController.getById()`

##### Request

```typescript
interface PathParams {
  id: number;
}
```

##### Response (200 OK)

```typescript
// Devuelve: PermissionResponse
```

---

#### `PUT /api/v1/admin/permissions/{id}`

**Propósito**: Actualizar un permiso existente.

**Controlador**: `PermissionController.update()`

##### Request

```typescript
interface PathParams {
  id: number;
}

// Body
interface PermissionRequest {
  name: string;
  description?: string;
}
```

##### Response (200 OK)

```typescript
// Devuelve: PermissionResponse actualizado
```

---

#### `DELETE /api/v1/admin/permissions/{id}`

**Propósito**: Eliminar un permiso (solo si no está asignado a roles).

**Controlador**: `PermissionController.delete()`

##### Request

```typescript
interface PathParams {
  id: number;
}
```

##### Response (204 No Content)

##### Error Responses

- **409 Conflict**: El permiso está asignado a uno o más roles

---

### 3.4. Asignación de Roles y Permisos

#### `POST /api/v1/admin/assignments/users/{userId}/roles/{roleId}`

**Propósito**: Asignar un rol a un usuario (operación idempotente).

**Controlador**: `AssignmentController.assignRoleToUser()`

##### Request

```typescript
// Path Params
interface PathParams {
  userId: number;
  roleId: number;
}

// Query Params (opcional)
interface QueryParams {
  assignedBy?: number;            // ID del usuario que hace la asignación (para auditoría)
}
```

##### Response (200 OK)

Sin cuerpo (operación exitosa).

##### Error Responses

- **404 Not Found**: Usuario o rol no existen
- **401/403**: Autenticación/autorización fallida

##### Notas de Implementación

- Idempotente: si la asignación ya existe, no genera error (retorna 200)
- El campo `assigned_by_user_id` en `user_roles` se establece si se proporciona `assignedBy`
- El campo `assigned_at` se establece con `CURRENT_TIMESTAMP`

---

#### `DELETE /api/v1/admin/assignments/users/{userId}/roles/{roleId}`

**Propósito**: Remover un rol de un usuario (operación idempotente).

**Controlador**: `AssignmentController.removeRoleFromUser()`

##### Request

```typescript
interface PathParams {
  userId: number;
  roleId: number;
}
```

##### Response (204 No Content)

##### Error Responses

- **404 Not Found**: Usuario o rol no existen

##### Notas de Implementación

- Idempotente: si la asignación no existe, no genera error (retorna 204)
- Se elimina la fila correspondiente en `user_roles`

---

#### `POST /api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}`

**Propósito**: Asignar un permiso a un rol (idempotente).

**Controlador**: `AssignmentController.assignPermissionToRole()`

##### Request

```typescript
interface PathParams {
  roleId: number;
  permissionId: number;
}
```

##### Response (200 OK)

##### Error Responses

- **404 Not Found**: Rol o permiso no existen

---

#### `DELETE /api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}`

**Propósito**: Remover un permiso de un rol (idempotente).

**Controlador**: `AssignmentController.removePermissionFromRole()`

##### Request

```typescript
interface PathParams {
  roleId: number;
  permissionId: number;
}
```

##### Response (204 No Content)

##### Error Responses

- **404 Not Found**: Rol o permiso no existen

---

## 4. Estructura de Errores Estándar

Todos los endpoints siguen el formato de error definido en `GlobalExceptionHandler`:

```typescript
interface ErrorResponse {
  timestamp: string;              // ISO 8601
  status: number;                 // Código HTTP (400, 401, 403, 404, 409, 500)
  error: string;                  // Nombre del error (ej: "Bad Request", "Conflict")
  message: string;                // Descripción detallada del error
  path: string;                   // Ruta del endpoint que generó el error
  traceId?: string;               // UUID para tracing (opcional, si se implementa)
}
```

**Ejemplo de 409 Conflict**:
```json
{
  "timestamp": "2025-10-27T10:30:45.123Z",
  "status": 409,
  "error": "Conflict",
  "message": "Email already exists: admin@example.com",
  "path": "/api/v1/admin/users"
}
```

**Ejemplo de 404 Not Found**:
```json
{
  "timestamp": "2025-10-27T10:31:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "User not found: 123",
  "path": "/api/v1/admin/users/123"
}
```

---

## 5. Consideraciones Especiales

### 5.1. Integración con Supabase

- **Validación JWT**: El backend valida el JWT usando el JWKS de Supabase configurado en `SecurityConfig`
- **Extracción de Claims**: Se extrae `sub` (UUID del usuario) y se busca en `app_users.supabase_user_id`
- **Roles Locales**: Los roles NO se almacenan en Supabase Auth; se gestionan en PostgreSQL (tabla `user_roles`)
- **RLS en Supabase**: Las políticas RLS protegen accesos directos a la DB (ej: desde Metabase)
  - Funciones helper: `has_role(text)`, `has_any_role(text[])`, `has_permission(text)`
  - Vistas: `vw_users_with_roles`, `vw_user_permissions`, `vw_role_summary`

### 5.2. Validaciones y Constraints

- **Email**: Tipo `CITEXT` en PostgreSQL (case-insensitive), único
- **UUID Supabase**: Único, requerido, formato UUID válido
- **Nombres**: `primerNombre` y `primerApellido` requeridos; `segundoNombre` y `segundoApellido` opcionales
- **Soft Delete**: Los usuarios eliminados tienen `deleted_at` no nulo y `is_active = false`

### 5.3. Paginación

- **Límite de Page Size**: El backend clampea el tamaño de página a un máximo de 100
- **Default**: `page=0`, `size=20`
- **Formato de Respuesta**: Spring Data Page estándar con `content`, `totalElements`, `totalPages`, etc.

### 5.4. Idempotencia

- **Asignaciones**: Los endpoints de asignación (POST) y remoción (DELETE) en `AssignmentController` son idempotentes
  - Si la asignación ya existe, POST retorna 200
  - Si la asignación no existe, DELETE retorna 204
- **Implementación**: Se usa `ON CONFLICT DO NOTHING` en queries o checks previos en el servicio

### 5.5. Auditoría

- **Campos de Auditoría**: `created_at`, `updated_at` en todas las tablas principales
- **Triggers SQL**: `update_updated_at_column` actualiza `updated_at` automáticamente en UPDATE
- **Asignaciones**: `user_roles.assigned_at` y `user_roles.assigned_by_user_id` permiten rastrear quién asignó roles
- **Logging**: Eventos críticos (crear usuario, asignar rol, eliminar) se loguean (sin PII en nivel INFO)

---

## 6. Estado de Implementación

### ✅ Completado

- Entidades JPA (AppUser, Role, Permission, UserRole, RolePermission, Planta)
- Repositorios JPA y custom search impl (AppUserSearchRepositoryImpl)
- Servicios de negocio (UserAdminService, RoleService, PermissionService, AssignmentService)
- Controllers admin con prefijo `/api/v1/admin`
- DTOs request/response con validaciones Bean Validation
- Mappers MapStruct (UsuarioMapper, RoleMapper, PermissionMapper)
- GlobalExceptionHandler con manejo de 400/401/403/404/409/500
- SecurityConfig con JWT → ROLE_* mapping
- Migración SQL unificada con RLS, funciones y vistas
- JPA Auditing config (`@EnableJpaAuditing`)
- Tests unitarios (services, mappers, validators)
- Tests de integración con Testcontainers
- Tests de controllers (MockMvc)

### ⚠️ Pendiente

- **OpenAPI/Swagger**: Añadir dependencia `springdoc-openapi-starter-webmvc-ui` y generar spec automático
- **Permission-level authorities**: Mapeo automático de permisos DB → `PERM_*` en GrantedAuthorities (opcional)
- **WebSocket notifications**: Notificaciones en tiempo real (fuera de scope IOC-004)

---

## 7. Próximos Pasos

### Para el Equipo Frontend
✅ Usar estos contratos TypeScript para desarrollo contra datos mock  
✅ Actualizar el servicio `api.ts` con los tipos definidos  
✅ Implementar manejo de errores según `ErrorResponse`  
✅ Implementar paginación en listados (page, size, totalPages)  
🔲 Consumir endpoints de asignación para gestión de roles (drag & drop, multi-select)

### Para el Equipo Backend
✅ Implementación completa según contratos  
✅ Tests unitarios y de integración pasando  
🔲 Añadir `springdoc-openapi` y generar `/v3/api-docs`  
🔲 Documentar en Swagger UI (`/swagger-ui.html`)  
🔲 Configurar rate limiting con Resilience4j (según matriz de acceso)  
🔲 Smoke test en entorno QA con token ADMIN real

### Para DevOps
✅ Migración SQL ejecutada en Supabase  
✅ Extensión `citext` habilitada  
✅ RLS políticas y funciones desplegadas  
🔲 Configurar variables de entorno en producción (`SUPABASE_URL`, `SUPABASE_JWKS`)  
🔲 Configurar rate limits en API Gateway (si aplica)  
🔲 Monitoreo de métricas (4xx/5xx por endpoint, latencia p95)

---

## 8. Changelog del Brief

| Versión | Fecha | Cambios |
|---------|-------|---------|
| v1.0 | 2025-10-27 | Versión inicial basada en TD IOC-004 y validation report |

---

**Generado automáticamente por**: GitHub Copilot + Blueprint Validator v2  
**Validación Score**: 94/100 (Excelente)  
**Build Status**: ✅ BUILD SUCCESS  
**Tests Status**: ✅ ALL TESTS PASSING

