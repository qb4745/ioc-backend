# Frontend Blueprints Index - IOC-004: User Role Management

**Feature**: User, Role & Permission Management  
**Technical Design**: TD-IOC-004-User-Role-Management-claude.md  
**Sprint**: Sprint 3  
**Fecha Generación**: 2025-10-27  
**Total FTVs**: 5

---

## 📋 Resumen Ejecutivo

Este índice lista todos los componentes frontend necesarios para implementar la gestión completa de usuarios, roles y permisos en la plataforma IOC. Los componentes cubren:

- ✅ Gestión de usuarios (CRUD completo)
- ✅ Gestión de roles y permisos
- ✅ Asignación dinámica de roles a usuarios
- ✅ Asignación de permisos a roles (vía modals)
- ✅ Búsqueda, filtrado y paginación
- ✅ Validación en tiempo real
- ✅ Optimistic updates para mejor UX

---

## 📄 Lista de FTVs Generados

### Pages (Rutas Principales)

#### FTV-001: User Management Page
**Archivo**: `ftv-user-management-page.md`  
**Componente**: `UserManagementPage`  
**Ruta**: `/admin/users`  
**Propósito**: Página principal de administración de usuarios con lista paginada, búsqueda, filtros y acciones CRUD.

**Responsabilidades**:
- Listar usuarios con paginación (max 100 por página)
- Búsqueda por nombre/email (debounced)
- Filtros por planta y estado activo
- Crear nuevo usuario
- Editar usuario existente
- Eliminar usuario (soft delete)
- Abrir modal de asignación de roles

**Endpoints Consumidos**:
- GET `/api/v1/admin/users` (con query params)
- POST `/api/v1/admin/users`
- PUT `/api/v1/admin/users/{id}`
- DELETE `/api/v1/admin/users/{id}`

**Testing**: Unit tests, integration tests, E2E (Playwright)

---

#### FTV-002: Role Management Page
**Archivo**: `ftv-role-management-page.md`  
**Componente**: `RoleManagementPage`  
**Ruta**: `/admin/roles`  
**Propósito**: Administración de roles del sistema con vista en cards mostrando usuarios asignados y permisos.

**Responsabilidades**:
- Listar roles con conteo de usuarios y permisos
- Crear nuevo rol
- Editar rol existente
- Eliminar rol (solo si no está en uso)
- Abrir modal de gestión de permisos por rol

**Endpoints Consumidos**:
- GET `/api/v1/admin/roles`
- POST `/api/v1/admin/roles`
- PUT `/api/v1/admin/roles/{id}`
- DELETE `/api/v1/admin/roles/{id}`

**Validaciones Especiales**:
- Protección contra eliminación de roles en uso (backend retorna 409)
- Validación de nombre único (case-insensitive)

---

#### FTV-003: Permission Management Page
**Archivo**: `ftv-permission-management-page.md`  
**Componente**: `PermissionManagementPage`  
**Ruta**: `/admin/permissions`  
**Propósito**: Catálogo de permisos granulares del sistema agrupados por categoría.

**Responsabilidades**:
- Listar permisos agrupados por categoría (USER, ROLE, DASHBOARD, etc.)
- Crear nuevo permiso
- Editar permiso existente
- Eliminar permiso (solo si no está asignado a roles)
- Filtrar por categoría

**Endpoints Consumidos**:
- GET `/api/v1/admin/permissions`
- POST `/api/v1/admin/permissions`
- PUT `/api/v1/admin/permissions/{id}`
- DELETE `/api/v1/admin/permissions/{id}`

**Lógica Especial**:
- Categorización automática por prefijo del nombre (ej: `USER_READ` → categoría `USER`)
- Naming convention enforced: `UPPERCASE_SNAKE_CASE`

---

### Feature Components (Modals)

#### FTV-004: User Form Modal
**Archivo**: `ftv-user-form-modal.md`  
**Componente**: `UserFormModal`  
**Tipo**: Modal Component  
**Propósito**: Formulario para crear/editar usuarios con validación en tiempo real.

**Modos de Operación**:
- **CREATE**: Formulario vacío, todos los campos editables
- **EDIT**: Formulario precargado, email y supabaseUserId bloqueados

**Campos del Formulario**:
- Email * (único, validación async)
- Supabase User ID * (UUID, validación async)
- Primer Nombre *
- Segundo Nombre (opcional)
- Primer Apellido *
- Segundo Apellido (opcional)
- Planta (select, opcional)
- Centro de Costo (opcional)
- Fecha de Contrato (date picker, opcional)
- Roles (checkboxes, opcional)

**Validaciones**:
- Schema Zod con validación de formato
- Validación async de email duplicado (debounced)
- Validación async de UUID duplicado
- Manejo de errores 409 (Conflict) del backend

**Tecnologías**:
- React Hook Form + Zod
- React Query para mutations
- Debounce para validaciones async

---

#### FTV-005: Role Assignment Modal
**Archivo**: `ftv-role-assignment-modal.md`  
**Componente**: `RoleAssignmentModal`  
**Tipo**: Modal Component  
**Propósito**: Asignar/remover roles a un usuario con optimistic updates.

**Responsabilidades**:
- Mostrar lista de roles disponibles con checkboxes
- Indicar roles actualmente asignados al usuario
- Toggle roles on/off con feedback instantáneo
- Optimistic updates (cambio inmediato en UI)
- Rollback automático si backend falla
- Mostrar loading state por cada role siendo procesado

**Endpoints Consumidos**:
- POST `/api/v1/admin/assignments/users/{userId}/roles/{roleId}`
- DELETE `/api/v1/admin/assignments/users/{userId}/roles/{roleId}`

**UX Avanzada**:
- Cambios se aplican inmediatamente sin esperar confirmación
- Loading spinner individual por role
- Toast de error si falla + rollback
- Idempotencia garantizada (backend no arroja error en duplicados)
- Prevención de múltiples clicks simultáneos en el mismo role

**Testing**:
- Tests de optimistic updates
- Tests de rollback en error
- Tests de accesibilidad (ARIA labels, keyboard navigation)

---

## 🗂️ Estructura de Archivos Sugerida

```
src/
├── pages/
│   └── admin/
│       ├── UserManagementPage.tsx          (FTV-001)
│       ├── RoleManagementPage.tsx          (FTV-002)
│       └── PermissionManagementPage.tsx    (FTV-003)
│
├── components/
│   ├── users/
│   │   ├── UserList.tsx
│   │   ├── UserTable.tsx
│   │   ├── UserRow.tsx
│   │   ├── UserFormModal.tsx               (FTV-004)
│   │   ├── RoleAssignmentModal.tsx         (FTV-005)
│   │   └── UserFilters.tsx
│   │
│   ├── roles/
│   │   ├── RoleList.tsx
│   │   ├── RoleCard.tsx
│   │   ├── RoleFormModal.tsx
│   │   └── PermissionAssignmentModal.tsx
│   │
│   └── permissions/
│       ├── PermissionGrid.tsx
│       ├── PermissionCard.tsx
│       ├── PermissionFormModal.tsx
│       └── CategoryFilter.tsx
│
├── hooks/
│   ├── users/
│   │   ├── useUsers.ts
│   │   ├── useCreateUser.ts
│   │   ├── useUpdateUser.ts
│   │   ├── useDeleteUser.ts
│   │   └── useUserForm.ts
│   │
│   ├── roles/
│   │   ├── useRoles.ts
│   │   ├── useToggleRole.ts
│   │   └── useRoleAssignment.ts
│   │
│   └── permissions/
│       └── usePermissions.ts
│
├── services/
│   └── api/
│       ├── usersApi.ts
│       ├── rolesApi.ts
│       ├── permissionsApi.ts
│       └── assignmentsApi.ts
│
└── types/
    ├── user.types.ts
    ├── role.types.ts
    └── permission.types.ts
```

---

## 🔗 Dependencias entre Componentes

```
UserManagementPage (FTV-001)
├── UserFormModal (FTV-004)
└── RoleAssignmentModal (FTV-005)

RoleManagementPage (FTV-002)
└── PermissionAssignmentModal (similar a FTV-005)

PermissionManagementPage (FTV-003)
└── PermissionFormModal (similar a FTV-004)
```

---

## ✅ Checklist de Implementación (Orden Recomendado)

### Fase 1: Setup y Tipos (Día 1)
- [ ] Crear estructura de carpetas
- [ ] Definir tipos TypeScript (`user.types.ts`, `role.types.ts`, `permission.types.ts`)
- [ ] Crear servicios de API base (`usersApi.ts`, `rolesApi.ts`, etc.)
- [ ] Configurar React Query global

### Fase 2: Componentes Base (Día 2-3)
- [ ] Implementar `UserManagementPage` (FTV-001)
- [ ] Implementar `UserList` y `UserTable`
- [ ] Implementar hooks de React Query (`useUsers`, `useCreateUser`, etc.)
- [ ] Tests unitarios de hooks

### Fase 3: Formularios (Día 4-5)
- [ ] Implementar `UserFormModal` (FTV-004)
- [ ] Configurar React Hook Form + Zod
- [ ] Implementar validaciones async
- [ ] Tests del formulario

### Fase 4: Role Assignment (Día 6)
- [ ] Implementar `RoleAssignmentModal` (FTV-005)
- [ ] Implementar optimistic updates
- [ ] Implementar rollback en error
- [ ] Tests de optimistic updates

### Fase 5: Role & Permission Pages (Día 7-8)
- [ ] Implementar `RoleManagementPage` (FTV-002)
- [ ] Implementar `PermissionManagementPage` (FTV-003)
- [ ] Implementar modals de formularios
- [ ] Tests de integración

### Fase 6: Testing & Polish (Día 9-10)
- [ ] Tests E2E con Playwright
- [ ] Accessibility audit (WCAG 2.1 AA)
- [ ] Performance optimization
- [ ] Responsive design validation
- [ ] Error handling refinement

---

## 🧪 Estrategia de Testing

### Unit Tests
- Hooks de React Query (useUsers, useRoles, etc.)
- Validaciones de formulario (Zod schemas)
- Mappers de datos (DTO → UI models)

### Integration Tests
- Flujos completos (crear usuario → asignar rol → editar)
- Manejo de errores del backend
- Optimistic updates y rollback

### E2E Tests (Playwright)
- Smoke test: Admin puede crear usuario y asignar roles
- Happy path: Gestión completa de roles
- Error scenarios: Manejo de conflictos (409)

---

## 📚 Referencias

- **Backend Brief**: `.gemini/sprints/backend_sync_brief_IOC-004.md`
- **Technical Design**: `.gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md`
- **Validation Report**: `.gemini/validation/IOC-004-blueprint-validation.md`

---

## 🎯 Métricas de Éxito

- **Performance**: LCP < 2.5s, FID < 100ms
- **Accesibilidad**: WCAG 2.1 AA compliant
- **Cobertura de Tests**: > 80%
- **User Experience**: Feedback instantáneo en todas las acciones
- **Error Handling**: Mensajes claros y accionables para el usuario

---

**Generado automáticamente**: 2025-10-27  
**Estado**: ✅ Ready for Implementation

