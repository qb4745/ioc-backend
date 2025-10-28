# IOC-004: Gestión de Usuarios, Roles y Permisos - Índice de Vistas Técnicas Frontend (FTV)

## 📋 Descripción General

Este índice documenta todos los Frontend Technical Views (FTVs) creados para implementar la funcionalidad de Gestión de Usuarios, Roles y Permisos basada en el Technical Design TD-IOC-004.

**Diseño Técnico Base:** `.gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md`

---

## 🎯 Alcance del Feature

Implementar una interfaz administrativa completa para:
- Gestión de usuarios (CRUD + asignación de roles)
- Gestión de roles (CRUD + asignación de permisos)
- Gestión de permisos (CRUD + visualización)
- Visualización de relaciones y estados

---

## 📁 Estructura de FTVs

### Páginas Principales

1. **[ftv-001-user-management-page.md](./ftv-001-user-management-page.md)**
   - **Ruta:** `/admin/users`
   - **Descripción:** Página principal de gestión de usuarios con tabla, filtros, búsqueda y acciones CRUD
   - **Componentes hijos:** ftv-007, ftv-004, ftv-005, ftv-009
   - **APIs:** GET /api/v1/admin/users, DELETE /api/v1/admin/users/{id}

2. **[ftv-002-role-management-page.md](./ftv-002-role-management-page.md)**
   - **Ruta:** `/admin/roles`
   - **Descripción:** Página de gestión de roles con tabla, estadísticas y permisos asociados
   - **Componentes hijos:** ftv-006, ftv-008, ftv-009
   - **APIs:** GET /api/v1/admin/roles, DELETE /api/v1/admin/roles/{id}

3. **[ftv-003-permission-management-page.md](./ftv-003-permission-management-page.md)**
   - **Ruta:** `/admin/permissions`
   - **Descripción:** Página de gestión de permisos con visualización de roles asociados
   - **Componentes hijos:** ftv-009
   - **APIs:** GET /api/v1/admin/permissions, POST /api/v1/admin/permissions

### Componentes Modales

4. **[ftv-004-user-form-modal.md](./ftv-004-user-form-modal.md)**
   - **Tipo:** Modal de formulario
   - **Propósito:** Crear y editar usuarios
   - **Validaciones:** Correo, nombres, UUID Supabase, planta, centro de costo
   - **APIs:** POST /api/v1/admin/users, PUT /api/v1/admin/users/{id}

5. **[ftv-005-role-assignment-modal.md](./ftv-005-role-assignment-modal.md)**
   - **Tipo:** Modal de asignación
   - **Propósito:** Asignar/remover roles a usuarios
   - **Interacción:** Multi-select con checkboxes
   - **APIs:** POST /api/v1/admin/users/{id}/roles/{roleId}, DELETE /api/v1/admin/users/{id}/roles/{roleId}

6. **[ftv-006-permission-assignment-modal.md](./ftv-006-permission-assignment-modal.md)**
   - **Tipo:** Modal de asignación
   - **Propósito:** Asignar/remover permisos a roles
   - **Interacción:** Multi-select agrupado por categorías
   - **APIs:** POST /api/v1/admin/roles/{id}/permissions/{permissionId}, DELETE /api/v1/admin/roles/{id}/permissions/{permissionId}

### Componentes Reutilizables

7. **[ftv-007-user-table.md](./ftv-007-user-table.md)**
   - **Tipo:** Tabla de datos
   - **Características:** Paginación, ordenamiento, búsqueda, acciones inline
   - **Columnas:** Nombre, Correo, Planta, Roles, Estado, Acciones
   - **Responsivo:** Mobile-first con cards en móvil

8. **[ftv-008-role-badge.md](./ftv-008-role-badge.md)**
   - **Tipo:** Componente visual
   - **Propósito:** Mostrar roles con colores consistentes
   - **Variantes:** ADMIN (rojo), GERENTE (azul), ANALISTA (verde)
   - **Tamaños:** sm, md, lg

9. **[ftv-009-empty-state.md](./ftv-009-empty-state.md)**
   - **Tipo:** Componente de estado
   - **Propósito:** Mostrar estados vacíos con acciones sugeridas
   - **Variantes:** Sin usuarios, sin roles, sin permisos, sin resultados
   - **Accesibilidad:** ARIA labels y foco en CTA

---

## 🔗 Dependencias Entre Componentes

```
UserManagementPage (ftv-001)
├── UserTable (ftv-007)
│   ├── RoleBadge (ftv-008)
│   └── EmptyState (ftv-009)
├── UserFormModal (ftv-004)
└── RoleAssignmentModal (ftv-005)
    └── RoleBadge (ftv-008)

RoleManagementPage (ftv-002)
├── RoleBadge (ftv-008)
├── EmptyState (ftv-009)
└── PermissionAssignmentModal (ftv-006)

PermissionManagementPage (ftv-003)
├── EmptyState (ftv-009)
└── RoleBadge (ftv-008)
```

---

## 📡 Contratos de API Utilizados

### Usuarios
- `GET /api/v1/admin/users` - Listar usuarios con filtros y paginación
- `POST /api/v1/admin/users` - Crear usuario
- `GET /api/v1/admin/users/{id}` - Obtener usuario por ID
- `PUT /api/v1/admin/users/{id}` - Actualizar usuario
- `DELETE /api/v1/admin/users/{id}` - Soft delete usuario
- `POST /api/v1/admin/users/{id}/roles/{roleId}` - Asignar rol
- `DELETE /api/v1/admin/users/{id}/roles/{roleId}` - Remover rol

### Roles
- `GET /api/v1/admin/roles` - Listar roles
- `POST /api/v1/admin/roles` - Crear rol
- `PUT /api/v1/admin/roles/{id}` - Actualizar rol
- `DELETE /api/v1/admin/roles/{id}` - Eliminar rol
- `POST /api/v1/admin/roles/{id}/permissions/{permissionId}` - Asignar permiso
- `DELETE /api/v1/admin/roles/{id}/permissions/{permissionId}` - Remover permiso

### Permisos
- `GET /api/v1/admin/permissions` - Listar permisos
- `POST /api/v1/admin/permissions` - Crear permiso
- `PUT /api/v1/admin/permissions/{id}` - Actualizar permiso
- `DELETE /api/v1/admin/permissions/{id}` - Eliminar permiso

---

## 🎨 Guías de Diseño

### Paleta de Colores (Roles)
- **ADMIN:** `bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200`
- **GERENTE:** `bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200`
- **ANALISTA:** `bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200`

### Estados
- **Activo:** Badge verde con punto
- **Inactivo:** Badge gris
- **Cargando:** Skeleton loaders
- **Error:** Mensaje con toast/alert rojo
- **Vacío:** Ilustración + mensaje + CTA

### Responsividad
- **Desktop (≥1024px):** Tablas completas con todas las columnas
- **Tablet (768-1023px):** Tablas con columnas esenciales
- **Mobile (<768px):** Cards apiladas con información condensada

---

## ♿ Accesibilidad

Todos los FTVs cumplen con:
- **WCAG 2.1 AA** mínimo
- **Navegación por teclado** completa
- **Screen readers** compatibles
- **Contraste de colores** validado
- **ARIA labels** descriptivos en español
- **Focus management** en modales

---

## 🧪 Testing Requerido

Cada FTV debe incluir:
1. **Unit Tests:** Componentes aislados (React Testing Library)
2. **Integration Tests:** Flujos completos de usuario
3. **Accessibility Tests:** axe-core / jest-axe
4. **Visual Regression:** Chromatic / Percy (opcional)
5. **E2E Tests:** Cypress / Playwright (críticos)

---

## 📦 Librerías y Dependencias

- **React 18+**
- **TypeScript 4.9+**
- **Tailwind CSS 3.x** (según tailadmin_config)
- **React Hook Form** (formularios)
- **Zod** (validación)
- **TanStack Query** (data fetching)
- **Axios** (HTTP client)
- **React Router v6** (routing)
- **Lucide React** (iconos)

---

## 🚀 Orden de Implementación Sugerido

1. **Fase 1 - Componentes base:**
   - ftv-008-role-badge
   - ftv-009-empty-state

2. **Fase 2 - Componentes de datos:**
   - ftv-007-user-table

3. **Fase 3 - Páginas simples:**
   - ftv-003-permission-management-page

4. **Fase 4 - Modales:**
   - ftv-004-user-form-modal
   - ftv-005-role-assignment-modal
   - ftv-006-permission-assignment-modal

5. **Fase 5 - Páginas complejas:**
   - ftv-001-user-management-page
   - ftv-002-role-management-page

---

## 📝 Notas de Implementación

### Seguridad
- Todos los endpoints requieren JWT token
- Solo usuarios con `ROLE_ADMIN` pueden acceder a estas vistas
- Validar permisos en frontend y backend

### Performance
- Implementar paginación server-side
- Lazy loading de modales
- Debounce en búsquedas (300ms)
- Cache de roles y permisos (raramente cambian)

### UX
- Confirmaciones para acciones destructivas
- Feedback inmediato (optimistic updates)
- Toast notifications para operaciones async
- Skeleton loaders durante carga
- Mensajes en español chileno

---

## 🔄 Versionado

- **Versión:** 1.0
- **Fecha:** 2025-10-28
- **Última Actualización:** 2025-10-28
- **Autor:** Tech Lead (IA)
- **Reviewers:** Frontend Lead, UX Designer

---

## ✅ Checklist de Completitud

- [x] Todos los FTVs documentados
- [x] Contratos de API mapeados
- [x] Dependencias identificadas
- [x] Guías de diseño especificadas
- [x] Requisitos de accesibilidad definidos
- [x] Orden de implementación sugerido
- [x] Localización en español chileno aplicada
- [x] Fechas y metadatos consistentes
- [ ] Revisión por Frontend Lead
- [ ] Aprobación por UX Designer
- [ ] Implementación iniciada

---

## 📚 Referencias

- **Technical Design:** `.gemini/sprints/technical-designs/TD-IOC-004-User-Role-Management-claude.md`
- **SQL Schema:** `.gemini/sql/schema-user-role-management-unified.sql`
- **Design System:** `.gemini/context/tailadmin_config.md`
- **Brand Manual:** `.gemini/context/manual_de_marca.md`
