# Feature Plan: Gestión de Usuarios, Roles y Permisos (IOC-004)

## Metadata
- ID: FP-IOC-004
- Sprint: Sprint 3
- Prioridad: Alta
- Tipo: Nueva Feature
- Estimación: 8 Story Points (5-6 días)
- Asignado a: Backend + Frontend
- Estado:
  - [x] Planificación
  - [ ] En Diseño
  - [ ] Listo para Desarrollo
  - [ ] En Desarrollo
  - [ ] En Testing
  - [ ] Completado
- Fecha Creación: 2025-10-22
- Última Actualización: 2025-10-22

---

## 1. Contexto de Negocio

### 1.1. Problema a Resolver

Contexto Actual:
- El proyecto usa Supabase Auth (JWT) para autenticación y Spring Security en el backend. Existen entidades JPA `Role` y `Permission`, pero no hay un modelo persistente de “usuarios de aplicación” ni endpoints para gestionar usuarios, roles y permisos.
- La asignación de accesos se requiere para proteger información en dashboards y en endpoints críticos.

Problema Específico:
- No existe CRUD de usuarios y su asignación de roles.
- No existe CRUD de roles/permiso ni asociación rol-permiso formal en BD (solo entidades JPA base).
- No hay RLS/funciones de verificación de rol en Supabase para reforzar seguridad a nivel de fila.

Impacto del Problema:
- Usuarios afectados: Administradores y cualquier usuario que requiera acceso controlado.
- Frecuencia: Diaria.
- Severidad: Alta (riesgo de acceso indebido y bloqueo operativo).
- Coste de NO resolverlo: Accesos inconsistentes, fallas de cumplimiento y riesgo de exposición de datos.

Ejemplo de Caso de Uso:
- Usuario: Administrador
- Situación: Debe dar acceso a un nuevo Gerente y revocar acceso de un usuario saliente.
- Problema: No puede crear/editar/eliminar usuarios ni asignar roles.
- Impacto: Retrasos operativos y riesgo de accesos activos no autorizados.

---

### 1.2. Solución Propuesta

Qué vamos a construir (alto nivel):
- Módulo integral de Gestión de Usuarios, Roles y Permisos compatible con Supabase.
- Modelo de datos 3NF en Postgres (Supabase): `app_users`, `roles`, `permissions`, `role_permissions`, `user_roles` y funciones auxiliares de seguridad (`has_role`).
- En el diseño del modelo se sustituye el concepto previo de `departments`/`regions` por `plantas` (la fábrica a la que pertenece el usuario). Además se añade el campo `centro_costo` (nullable) y `fecha_contrato` (nullable, tipo DATE) en el perfil de usuario.
- Endpoints REST seguros para CRUD de usuarios (perfil de app), roles y permisos; y para asignación de roles a usuarios.
- Políticas de RLS para proteger lectura/escritura desde Supabase, permitiendo self-read y modificaciones solo para administradores.

Valor para el Usuario:
- Alta seguridad y control de accesos.
- Autogestión operativa sin intervención técnica.
- Trazabilidad de asignaciones y cambios.

Valor para el Negocio:
- Cumplimiento de mínimos de gobernanza de acceso.
- Reducción de riesgos y de incidencias por permisos.
- Base escalable para futuras features con permisos finos.

---

### 1.3. Alcance del MVP

✅ Dentro del Alcance (Sprint 3):
- [ ] Modelo 3NF y migraciones SQL en Supabase.
- [ ] CRUD de Roles y Permisos.
- [ ] Asociación Rol-Permiso.
- [ ] Perfil de Usuario de Aplicación vinculado a Supabase Auth y CRUD básico (admin).
- [ ] Asignación de Roles a Usuarios (CRUD asignaciones).
- [ ] RLS y función `has_role(text)` para checks desde SQL.
- [ ] Endpoints Backend protegidos por `ROLE_*`/`PERM_*` (Spring Security) y tests.

❌ Fuera del Alcance (futuro):
- [ ] Auto-provisioning completo desde eventos de Supabase (webhooks) → Sprint 4.
- [ ] UI avanzada de auditoría (historial) → Sprint 4/5.
- [ ] Asignación directa de permisos a usuario (sin rol) → backlog (si se requiere).

Justificación del Alcance:
- Se enfoca en lo esencial: gestionar usuarios y roles con permisos, minimizando complejidad y permitiendo evolución incremental.

---

## 2. Análisis Técnico (Alto Nivel)

### 2.1. Componentes Afectados

Frontend:
- [ ] Nuevas vistas: Administración de Usuarios, Roles, Permisos.
- [ ] Componentes: UserList, UserForm, RoleList, RoleForm, PermissionList, AssignmentDialog.
- [ ] Rutas: `/admin/users`, `/admin/roles`, `/admin/permissions`.

Backend:
- [ ] Endpoints nuevos:
  - `GET/POST/PUT/DELETE /api/v1/admin/users`
  - `GET/POST/PUT/DELETE /api/v1/admin/roles`
  - `GET/POST/PUT/DELETE /api/v1/admin/permissions`
  - `POST/DELETE /api/v1/admin/users/{id}/roles/{roleId}` (asignación)
- [ ] Servicios: UserAdminService, RoleService, PermissionService, AssignmentService.
- [ ] Seguridad: uso de `JwtAuthenticationConverter` existente; controles por `hasAuthority('ROLE_ADMIN')` y permisos.

Base de Datos:
- [ ] Nuevas tablas: `app_users`, `user_roles`, `role_permissions` (si no existe), y funciones `has_role`.
- [ ] Migraciones requeridas: Sí (archivo SQL en `.gemini/sql/schema-user-role-management.sql`).

Integraciones Externas:
- Supabase Auth (JWT) para identidad; opcionalmente referencia a `auth.users`.

### 2.2. Dependencias Técnicas

Nuevas Librerías/Dependencias: No (usar stack actual de Spring Security y JPA).

Features/Sistemas Existentes Requeridos:
- Seguridad JWT vigente; entidades `Role` y `Permission` existentes.

Bloqueos Técnicos:
- Verificar mapeo de `CustomUserDetails.userId` (Long) vs UUID de Supabase; decisión: usar `app_users.id` (BIGSERIAL) como identificador interno y guardar `supabase_user_id` (UUID) como referencia.

### 2.3. Impacto en la Arquitectura

Cambios Arquitectónicos:
- [x] Cambios menores (endpoints + tablas + RLS + función de rol SQL).

Si hay cambios, describir:
- Persistencia de perfil local de usuario enlazando con Supabase Auth. El backend sigue validando JWT; los permisos se gestionan por roles locales.

---

## 3. Requisitos Funcionales

### 3.1. Historias de Usuario

Historia 1: Crear usuario válido
- Como Administrador, quiero registrar un usuario con datos correctos para que quede creado con roles asignados.
Criterios de Aceptación:
```gherkin
Escenario: Crear usuario y asignar roles
  Dado que soy ADMIN autenticado
  Cuando envío POST /api/v1/admin/users con email, nombre y roles ["MANAGER"]
  Entonces el sistema crea el usuario app y vincula supabase_user_id
  Y asigna los roles indicados
  Y responde 201 con el usuario y sus roles
```
Notas Técnicas:
- Validar unicidad de email y supabase_user_id.
- Si el usuario ya existe, retornar 409 o idempotencia controlada (configurable).

Historia 2: Modificar usuario válido
```gherkin
Escenario: Actualizar datos de un usuario
  Dado que soy ADMIN autenticado
  Cuando envío PUT /api/v1/admin/users/{id} con cambios válidos
  Entonces los datos se actualizan correctamente
  Y responde 200 con el usuario actualizado
```

Historia 3: Eliminar usuario (revocar acceso)
```gherkin
Escenario: Eliminar (soft delete) usuario
  Dado que soy ADMIN autenticado
  Cuando envío DELETE /api/v1/admin/users/{id}
  Entonces el usuario se marca eliminado y pierde acceso inmediatamente
  Y responde 204
```

Historia 4: Asignar roles con permisos
```gherkin
Escenario: Asignar roles a un usuario
  Dado que soy ADMIN autenticado
  Cuando envío POST /api/v1/admin/users/{id}/roles/{roleId}
  Entonces el usuario obtiene accesos según permisos del rol
  Y responde 200 con roles actuales
```

### 3.2. Casos de Uso Detallados

Caso 1: Alta de Usuario + Asignación
- Actor: Admin
- Precondiciones: Usuario autenticado como ADMIN; rol y permisos existentes.
- Flujo Normal:
  1) Admin crea perfil app_user (vinculado a Supabase uid)
  2) Admin asigna roles
  3) Backend valida y persiste; devuelve confirmación
- Alternativas:
  - Email ya existe → 409
  - Rol no existe → 404

### 3.3. Requisitos No Funcionales
- Seguridad: Endpoints bajo `ROLE_ADMIN`; RLS en tablas; logs de auditoría mínimos.
- Performance: Listados paginados; índices en claves de búsqueda/joins.
- Confiabilidad: Transacciones en asignaciones; validaciones y constraints fuertes en DB.

---

## 4. Diseño de Interfaz (UX/UI)

Wireframes ligeros:
- Users List: tabla con filtros, acciones crear/editar/eliminar, asignar roles.
- Roles List: CRUD de roles con permisos asociados.
- Permissions List: CRUD básico.

Flujo de Usuario (Admin):
- Admin → Admin Panel → Users → Create/Edit → Assign Roles → Save.

Estados UI:
- Loading, Empty, Error, Success, Confirm dialogs para destructive actions.

---

## 5. Contratos de API (Preliminares)

Endpoint: POST /api/v1/admin/users
- Request: { email, fullName, plantaId, centroCosto?, fechaContrato?, supabaseUserId, roles?: string[] }
- Response 201: { id, email, fullName, roles: string[] }
- Errores: 400/401/403/409

Endpoint: PUT /api/v1/admin/users/{id}
- Request: campos editables
- Response 200: usuario actualizado

Endpoint: DELETE /api/v1/admin/users/{id}
- Response 204

Endpoint: POST /api/v1/admin/users/{id}/roles/{roleId}
- Response 200: roles actuales

Endpoint: DELETE /api/v1/admin/users/{id}/roles/{roleId}
- Response 204

CRUD Roles/Permisos: GET/POST/PUT/DELETE /api/v1/admin/roles, /permissions

---

## 6. Modelo de Datos (Preliminar, 3NF)

Nuevas Entidades (3FN):
- `plantas` (id SERIAL PK, code VARCHAR UNIQUE, name VARCHAR, address TEXT, created_at, updated_at)
- `app_users` (id BIGSERIAL PK, supabase_user_id UUID UNIQUE NOT NULL, email CITEXT UNIQUE NOT NULL, full_name VARCHAR, planta_id FK → plantas.id NULL, centro_costo VARCHAR(50) NULL, fecha_contrato DATE NULL, is_active BOOLEAN, last_login_at TIMESTAMP, created_at, updated_at, deleted_at)
- `roles` (id SERIAL PK, name UNIQUE NOT NULL, description, timestamps)
- `permissions` (id SERIAL PK, name UNIQUE NOT NULL, description)
- `role_permissions` (role_id FK → roles.id, permission_id FK → permissions.id, PK(role_id,permission_id), created_at)
- `user_roles` (user_id FK → app_users.id, role_id FK → roles.id, PK(user_id,role_id), assigned_at, assigned_by_user_id)

Relaciones:
- `app_users` ↔ `roles` (M:N) vía `user_roles`.
- `roles` ↔ `permissions` (M:N) vía `role_permissions`.

Observaciones sobre 3FN:
- `plantas` centraliza la pertenencia organizacional y evita repetir nombre/dirección en cada usuario.
- `centro_costo` y `fecha_contrato` son atributos del perfil del usuario (no dependen de otros atributos) y por tanto permanecen en `app_users`.

Migración y verificación:
- Archivo de migración: `.gemini/sql/schema-user-role-management.sql` (crea `plantas`, `app_users` con `planta_id`, `centro_costo`, `fecha_contrato`, roles, permisos y relaciones M:N).
- Ejecutar en Supabase SQL Editor o con psql. El script incluye seeds idempotentes y consultas de verificación (tablas/índices) y emite notices al final.
- Verificar: `SELECT * FROM vw_users_with_roles LIMIT 10;` y revisar que `planta_code` y `fecha_contrato` se muestren correctamente.
