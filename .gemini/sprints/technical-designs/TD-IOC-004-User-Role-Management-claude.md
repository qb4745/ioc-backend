# Technical Design: Gestión de Usuarios, Roles y Permisos (IOC-004)

## Metadata
- ID: TD-IOC-004
- Feature Plan: .gemini/sprints/feature-plans/FP-IOC-004-User-Role-Management.md
- Sprint: Sprint 3
- Autor: Tech Lead (IA)
- Reviewers: Backend Lead, Frontend Lead, DevOps
- Estado:
  - [x] Draft
  - [ ] En Revisión
  - [ ] Aprobado
  - [ ] Implementado
- Fecha Creación: 2025-10-22
- Última Actualización: 2025-10-22
- Versión: 1.0

---

## 1. Resumen Ejecutivo

### 1.1. Propósito de Este Documento
Este documento define el cómo técnico para implementar la feature “Gestión de Usuarios, Roles y Permisos”, basada en el Feature Plan FP-IOC-004. Cubre arquitectura, modelo de datos, contratos API, seguridad, testing y plan de implementación.

### 1.2. Alcance Técnico
- Arquitectura de componentes backend y consideraciones frontend
- Modelo de datos en Postgres (Supabase) con migraciones idempotentes
- Contratos de API REST completos
- Estrategia de seguridad (JWT + RBAC + RLS Supabase)
- Plan de pruebas (unidad, integración, seguridad)
- Plan de despliegue y rollback

Fuera de alcance: auto-provisioning de usuarios desde eventos Supabase (Sprint 4), auditoría avanzada y asignación directa de permisos a usuario.

### 1.3. Decisiones Arquitectónicas Clave
- Identidad: usar supabase_user_id (UUID) como referencia externa y app_users.id (BIGSERIAL) como PK interna
- Email case-insensitive: CITEXT con unique
- RBAC local: roles y permisos gestionados en DB local; Spring Security mapea roles a authorities ROLE_*
- RLS en Supabase: habilitada en tablas críticas y reforzada con funciones has_role/has_permission
- CRUD admin-only: endpoints bajo ROLE_ADMIN, con excepciones de lectura según reglas

Alternativas consideradas: usar solo claims del JWT para permisos (rechazada por falta de dinamismo), usar UUID como PK para app_users (posible, se prioriza BIGSERIAL por simplicidad e historiales existentes).

---

## 2. Arquitectura de la Solución

### 2.1. Diagrama de Alto Nivel (texto)
- Frontend Admin: rutas /admin/users, /admin/roles, /admin/permissions consumen API REST con JWT
- Backend Spring Boot:
  - Controllers: AdminUserController, RoleController, PermissionController, AssignmentController
  - Services: UserAdminService, RoleService, PermissionService, AssignmentService
  - Persistence: Repositories JPA para AppUser, Role, Permission, UserRole, RolePermission, Planta
  - Security: filtro JWT existente + JwtAuthenticationConverter que expone ROLE_* y PERM_* como GrantedAuthorities
- DB Supabase (Postgres): tablas 3FN, RLS, funciones has_role/has_permission y vistas vw_users_with_roles, vw_user_permissions, vw_role_summary

### 2.1. Diagrama de Arquitectura de Alto Nivel

```
┌─────────────────────────────────────────────────────────────┐
│                       FRONTEND (React)                      │
│  ┌──────────────────┐        ┌──────────────────────┐     │
│  │ Vistas de Admin  │◄───────┤ Servicio API         │     │
│  │ (User/Role/Perm) │        │ (axios)              │     │
│  └──────────────────┘        └──────────────────────┘     │
└────────────────────────────────┬────────────────────────────┘
                                 │ HTTPS/REST + JWT (Supabase)
                                 │
┌────────────────────────────────▼────────────────────────────┐
│                       BACKEND (Spring Boot)                 │
│  ┌──────────────────┐        ┌──────────────────────┐     │
│  │ AdminController  │───────►│ User/Role/Perm       │     │
│  │ (REST API)       │        │ Services             │     │
│  └──────────────────┘        └──────────────────────┘     │
│           │                            │                   │
│           │ Security Filter            ▼                   │
│           ▼                   ┌──────────────────────┐     │
│  ┌──────────────────┐         │ Repositories (JPA)   │     │
│  │ JWT Validator    │         │                      │     │
│  └──────────────────┘         └──────────────────────┘     │
└────────────────────────────────┬────────────────────────────┘
                                 │
                                 ▼
              ┌──────────────────────────────────┐
              │ PostgreSQL (Supabase)            │
              │ ┌───────────┐  ┌───────────┐   │
              │ │ auth.users│  │ app_users │   │
              │ └─────▲─────┘  └─────┬─────┘   │
              │       └──────────────┘ 1:1     │
              │             ┌────────▼────────┐│
              │             │   user_roles    ││
              │             └────────┬────────┘│
              │             ┌────────▼────────┐│
              │             │      roles      ││
              │             └────────┬────────┘│
              │             ┌────────▼────────┐│
              │             │ role_permissions││
              │             └────────┬────────┘│
              │             ┌────────▼────────┐│
              │             │   permissions   ││
              │             └─────────────────┘│
              └──────────────────────────────────┘
```


### 2.2. Flujo de Datos Principal (Crear usuario y asignar roles)
1) Admin en UI completa formulario
2) Frontend envía POST /api/v1/admin/users con JWT
3) Security valida JWT y extrae sub (UUID Supabase)
4) Service valida unicidad email y supabase_user_id
5) Persiste app_user y asignaciones de roles (transacción)
6) Devuelve 201 con DTO de usuario + roles

Errores comunes: 409 si email/supabase_user_id ya existen, 404 si rol no existe, 400 validación.

### 2.3. Componentes y Responsabilidades (Backend)
- AdminUserController: CRUD usuarios y endpoints de asignación
- RoleController: CRUD roles
- PermissionController: CRUD permisos
- AssignmentController: asignar/remover roles a usuario
- UserAdminService: reglas de negocio de usuarios (validación, soft delete)
- RoleService / PermissionService: catálogos y asociaciones
- AssignmentService: operaciones M:N (idempotentes cuando aplique)
- Repositories JPA: acceso a datos con paginación y filtros

---

## 3. Modelo de Datos

Fuente: .gemini/sql/schema-user-role-management-unified.sql (v1.3)

### 3.1. Tablas Principales
- plantas: id SERIAL, code UNIQUE, name, address, timestamps
- app_users: id BIGSERIAL, supabase_user_id UUID UNIQUE NOT NULL, email CITEXT UNIQUE NOT NULL, primer/segundo nombre y apellido, planta_id FK, centro_costo VARCHAR(50) NULL, fecha_contrato DATE NULL, is_active, last_login_at, timestamps, deleted_at
- roles: id SERIAL, name UNIQUE, description, timestamps
- permissions: id SERIAL, name UNIQUE, description
- role_permissions: PK(role_id, permission_id), created_at
- user_roles: PK(user_id, role_id), assigned_at, assigned_by_user_id

Índices relevantes: por actividad, planta, centro_costo, nombre, claves foráneas y vistas agregadas. Extensión citext habilitada.

### 3.2. Funciones, RLS y Vistas
- Funciones: has_role(text), has_any_role(text[]), has_permission(text) con SECURITY DEFINER
- RLS habilitado en app_users, user_roles, roles, permissions, role_permissions
- Políticas admin-write y select para autenticados (self-read o por rol)
- Vistas: vw_users_with_roles, vw_user_permissions, vw_role_summary

### 3.3. Migraciones Condicionales
- Migrar departments/regions a plantas (idempotente)
- Renombrar roles legacy MANAGER→GERENTE, USER→ANALISTA
- Migrar tablas singulares a plurales (role, permission, user_role, role_permission)
- Migrar app_user con full_name a app_users con nombres separados

---

## 4. Contratos de API

Base path: /api/v1/admin
Seguridad: Bearer JWT requerido, salvo donde se indique
Autorización: ROLE_ADMIN para write; lecturas según política de negocio

### 4.1. Usuarios
- POST /users
  - Request: { email: string, primerNombre: string, segundoNombre?: string, primerApellido: string, segundoApellido?: string, plantaId?: number, centroCosto?: string, fechaContrato?: string(YYYY-MM-DD), supabaseUserId: string(UUID), roles?: string[] }
  - Validación: email requerido/formato; supabaseUserId UUID; nombres 1 y 3 requeridos; roles opcional
  - Responses:
    - 201: { id: number, email: string, fullName: string, plantaId?: number, centroCosto?: string, fechaContrato?: string, isActive: boolean, roles: string[] }
    - 400: errores de validación
    - 401/403: no autorizado
    - 409: email o supabaseUserId ya existen

- GET /users
  - Query: page, size, search, plantaId, isActive
  - 200: { items: UsuarioResponse[], pagination }

- GET /users/{id}
  - 200: UsuarioResponse
  - 404: no encontrado

- PUT /users/{id}
  - Body: campos editables (nombres, plantaId, centroCosto, fechaContrato, isActive)
  - 200: UsuarioResponse
  - 404/400

- DELETE /users/{id}
  - Soft delete: set deleted_at y is_active=false
  - 204

- POST /users/{id}/roles/{roleId}
  - 200: { roles: string[] }
  - 404 si role o user inexistente

- DELETE /users/{id}/roles/{roleId}
  - 204

### 4.2. Roles
- GET /roles: listado con conteos (opcionalmente vía vw_role_summary)
- POST /roles: { name, description? } → 201
- PUT /roles/{id}: 200
- DELETE /roles/{id}: 204 (si no referenciado o con lógica de seguridad)

### 4.3. Permisos
- GET /permissions
- POST /permissions: { name, description? } → 201
- PUT /permissions/{id}: 200
- DELETE /permissions/{id}: 204

### 4.4. Esquemas DTO (Backend)
- UsuarioCreateRequest, UsuarioResponse, RoleRequest/Response, PermissionRequest/Response
- Reglas: los nombres se devuelven también como nombre_completo calculado en el DTO para conveniencia UI

---

## 5. Seguridad y Autorización

- Autenticación: JWT emitido por Supabase; el backend confía en firma y extrae claims
- Identidad: claim sub (UUID) mapea a app_users.supabase_user_id; si no existe, el endpoint de creación de usuarios lo insertará; no se crea on-login en este sprint
- Autorización:
  - Roles: mapear roles a authorities ROLE_ADMIN, ROLE_GERENTE, ROLE_ANALISTA
  - Permisos: opcionalmente mapear a PERM_* para granularidad futura
  - Endpoints de escritura bajo hasAuthority('ROLE_ADMIN')
- RLS en Supabase: protege consultas directas desde servicios y BI; funciones has_role/has_permission permiten checks SQL
- Datos sensibles: email, nombres; logs sin PII en nivel INFO

Endurecimiento:
- Validación de entrada (Bean Validation)
- Paginación obligatoria en listados
- Límites de tamaño de request
- Idempotencia en asignaciones (ON CONFLICT DO NOTHING en DB y checks en Service)

---

## 6. Estrategia de Implementación

Orden recomendado:
1) Ejecutar migración SQL unificada: .gemini/sql/schema-user-role-management-unified.sql
2) Crear entidades JPA y repositorios (AppUser, Role, Permission, UserRole, RolePermission, Planta)
3) Servicios y mapeos DTO
4) Controllers y validaciones (incluye paginación)
5) Security config: antMatchers/authorizeHttpRequests para /api/v1/admin/** bajo ROLE_ADMIN
6) Tests (unidad, integración, seguridad)
7) Semillas en DB ya provistas en SQL (roles y permisos)

Consideraciones de compatibilidad:
- El SQL contempla migración desde tablas legacy en singular y roles renombrados
- Emails en citext: asegurar dependencia/permiso de extensión en Supabase

---

## 7. Plan de Testing

Pruebas de Unidad (Backend):
- Servicios: creación de usuario, validación de unicidad, asignación/remoción de roles
- Repositorios: consultas por email, por supabase_user_id, paginación y filtros

Pruebas de Integración:
- Controllers: CRUD usuarios, roles, permisos (MockMvc/WebTestClient)
- Transaccionalidad: asignación de roles atomizada
- Seguridad: 401/403 con JWT inválido/sin ROLE_ADMIN

Pruebas de SQL/RLS (opcional en este sprint):
- Verificación de funciones has_role/has_permission via queries controladas
- Acceso a vistas vw_users_with_roles y vw_role_summary

Pruebas de Performance:
- Listados con 10k usuarios: paginación estable < 500ms local
- Índices efectivos en búsquedas por email, planta, actividad

---

## 8. Observabilidad y Auditoría

- Logging: eventos admin (crear, actualizar, eliminar usuario; asignación de rol)
- Métricas (si Micrometer): conteo de usuarios activos, roles por usuario, errores 4xx/5xx por endpoint
- Trazas: habilitar en entorno de QA para diagnósticos

---

## 9. Despliegue y Operación

Prerequisitos:
- Supabase: extensión citext, permisos para crear funciones y políticas RLS

Pasos de despliegue:
1) Ejecutar .gemini/sql/schema-user-role-management-unified.sql en Supabase (SQL Editor)
2) Deploy del backend (no requiere nuevas dependencias)
3) Probar endpoints admin con token ADMIN

Rollback:
- Revertir deploy del backend
- El SQL es idempotente; si se requiere revertir datos seed, eliminar entradas agregadas (roles/permisos) manualmente con cuidado

Riesgos y mitigaciones:
- Mapeo entre UUID Supabase y app_users: preparar script de backfill si faltan perfiles
- Cambios RLS: validar con cuentas de prueba antes de abrir a producción

---

## 10. Anexos

- SQL de esquema: .gemini/sql/schema-user-role-management-unified.sql
- Vistas útiles: vw_users_with_roles, vw_user_permissions, vw_role_summary
- Futuros (out of scope): auto-provisioning por webhooks Supabase, auditoría avanzada de cambios

---

## 11. Checklist de Implementación
- [ ] Ejecutar migración SQL en Supabase
- [ ] Crear entidades JPA y repositorios
- [ ] Implementar servicios y DTOs
- [ ] Implementar controllers y validaciones
- [ ] Configurar seguridad (ROLE_ADMIN para /api/v1/admin/**)
- [ ] Escribir pruebas de unidad e integración
- [ ] Validar RLS y funciones en Supabase
- [ ] Documentar endpoints en OpenAPI/Swagger
- [ ] Smoke test en QA

---

## 12. Archivos generados (blueprints / BSS)
A continuación se listan los artefactos que se generaron a partir de este Technical Design y que sirven como base para la implementación backend (BSS y blueprints). Estos archivos están dentro de la carpeta `.gemini/blueprints/backend` salvo el technical design que ya existe.

1. `.gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md` (este documento)
2. `.gemini/blueprints/backend/bss-admin-user-controller.md`
3. `.gemini/blueprints/backend/bss-role-controller.md`
4. `.gemini/blueprints/backend/bss-permission-controller.md`
5. `.gemini/blueprints/backend/bss-assignment-controller.md`
6. `.gemini/blueprints/backend/bss-user-admin-service.md`
7. `.gemini/blueprints/backend/bss-role-service.md`
8. `.gemini/blueprints/backend/bss-permission-service.md`
9. `.gemini/blueprints/backend/bss-assignment-service.md`
10. `.gemini/blueprints/backend/bss-app-user-repository.md`
11. `.gemini/blueprints/backend/bss-role-repository.md`
12. `.gemini/blueprints/backend/bss-permission-repository.md`
13. `.gemini/blueprints/backend/bss-user-role-repository.md`
14. `.gemini/blueprints/backend/bss-role-permission-repository.md`
15. `.gemini/blueprints/backend/bss-planta-repository.md`
16. `.gemini/blueprints/backend/bss-index-IOC-004.md`
17. `.gemini/blueprints/backend/bss-usuario-mapper.md`
18. `.gemini/blueprints/backend/bss-role-mapper.md`
19. `.gemini/blueprints/backend/bss-permission-mapper.md`
20. `.gemini/blueprints/backend/bss-dto-contracts.md`
21. `.gemini/blueprints/backend/bss-entities.md`

Notas:
- Todos los archivos están en formato Markdown y contienen especificaciones (interfaz pública, firmas, reglas de negocio, tests sugeridos).
- Úsalos como referencia para scaffolding del código en `src/main/java` y para generar OpenAPI/Swagger.

---
