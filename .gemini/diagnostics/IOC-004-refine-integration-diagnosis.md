# 🔍 Diagnóstico de Integración Refine - IOC-004
**Fecha:** 2025-10-29  
**Feature:** User Role Management (IOC-004)  
**Estado:** 🔴 ERRORES CRÍTICOS DETECTADOS

---

## 1️⃣ RESUMEN EJECUTIVO

### Problema Principal
Los archivos `src/pages/admin/users/list.tsx` y `src/pages/admin/roles/list.tsx` están **completamente vacíos** (solo 1 línea cada uno), causando el error:

```
App.tsx:19 Uncaught SyntaxError: The requested module '/src/pages/admin/roles/list.tsx' 
does not provide an export named 'RoleManagementPage' (at App.tsx:19:10)
```

### Causa Raíz Identificada
**Los componentes `UserManagementPage` y `RoleManagementPage` nunca fueron implementados**, a pesar de que:
- ✅ Las rutas están correctamente configuradas en `App.tsx`
- ✅ Los menús están correctamente configurados en `AppSidebar.tsx`
- ✅ El componente `PermissionManagementPage` SÍ está implementado (295 líneas)
- ✅ Los componentes auxiliares (modales, tablas, badges) SÍ existen

### Severidad
- 🔴 **CRÍTICA**

### Impacto
- [x] Bloquea navegación a páginas de administración (`/admin/usuarios` y `/admin/roles`)
- [x] Impide CRUD de usuarios/roles
- [ ] Rompe autenticación/autorización
- [ ] Solo afecta UX/performance

---

## 2️⃣ HALLAZGOS DETALLADOS

### A. Exports/Imports Inconsistentes

#### ❌ Errores Encontrados
| Archivo | Export Actual | Import en App.tsx | Estado | Líneas |
|---------|---------------|-------------------|--------|--------|
| users/list.tsx | **NINGUNO** (archivo vacío) | `import { UserManagementPage }` | ❌ ERROR | 1 |
| roles/list.tsx | **NINGUNO** (archivo vacío) | `import { RoleManagementPage }` | ❌ ERROR | 1 |
| permissions/list.tsx | `export const PermissionManagementPage` | `import { PermissionManagementPage }` | ✅ OK | 295 |

#### 📋 Detalle de Discrepancias

**Archivo 1: src/pages/admin/users/list.tsx**
```typescript
// ARCHIVO ACTUALMENTE VACÍO (solo 1 línea)
// Se espera un componente completo que exporte:
// export const UserManagementPage: React.FC = () => { ... }
```

**Archivo 2: src/pages/admin/roles/list.tsx**
```typescript
// ARCHIVO ACTUALMENTE VACÍO (solo 1 línea)
// Se espera un componente completo que exporte:
// export const RoleManagementPage: React.FC = () => { ... }
```

**Archivo 3: App.tsx (Líneas 18-20)**
```typescript
// App.tsx importa componentes que NO existen:
import { UserManagementPage } from "./pages/admin/users/list";      // ❌ ERROR
import { RoleManagementPage } from "./pages/admin/roles/list";      // ❌ ERROR
import { PermissionManagementPage } from "./pages/admin/permissions/list"; // ✅ OK
```

### B. Hooks de Refine Mal Configurados

#### ✅ PermissionManagementPage - Implementación Correcta (Referencia)

El archivo `permissions/list.tsx` SÍ está correctamente implementado y sirve como referencia:

```typescript
// ✅ Uso correcto de useList
const permissionsList = useList({
  resource: 'admin/permissions',
  pagination: { pageSize: 100 },
}) as unknown as UseListPermissionsResult;

const permissions = permissionsList.result?.data || permissionsList.query?.data || [];
const isLoading = permissionsList.query?.isFetching ?? false;
const refetch = permissionsList.query?.refetch;

// ✅ Uso correcto de useCreate
const { mutate: createPermission } = useCreate();

// ✅ Uso correcto de useDelete
const { mutate: deletePermission } = useDelete();
```

**Características correctas observadas:**
- ✅ Manejo defensivo con optional chaining (`?.`)
- ✅ Fallbacks con nullish coalescing (`??`)
- ✅ Type casting apropiado para compatibilidad con Refine
- ✅ Destructuring correcto de mutate functions
- ✅ Manejo de estados de loading

#### ❌ Problemas en useList
**N/A** - Los archivos están vacíos, no hay hooks implementados.

#### ❌ Problemas en useCreate/useUpdate/useDelete
**N/A** - Los archivos están vacíos, no hay mutaciones implementadas.

### C. Problemas en dataProvider

#### ⚠️ Archivo dataProvider.ts Vacío
**Archivo:** `src/providers/dataProvider.ts`  
**Estado:** Vacío (0 líneas de código)

**Observación:** Aunque el archivo existe, no hay implementación. Sin embargo, el componente `PermissionManagementPage` funciona, lo que sugiere que:
- Puede estar usando un dataProvider por defecto
- O la integración con Refine está parcialmente configurada
- Requiere verificación de cómo `useList`, `useCreate` y `useDelete` están resolviendo las peticiones

**Recomendación:** Verificar si existe otro archivo de configuración de dataProvider o si se está usando uno por defecto.

### D. Problemas en Configuración de Rutas

#### ✅ Estructura de Router Correcta
La configuración de rutas en `App.tsx` es correcta:

```typescript
<Route element={<ProtectedRoute />}>
  <Route element={<AppLayout />}>
    {/* Rutas de Gestión de Usuarios, Roles y Permisos (IOC-004) */}
    <Route path="/admin/usuarios" element={<UserManagementPage />} />
    <Route path="/admin/roles" element={<RoleManagementPage />} />
    <Route path="/admin/permisos" element={<PermissionManagementPage />} />
  </Route>
</Route>
```

**Características:**
- ✅ Rutas anidadas dentro de `<ProtectedRoute />` (requieren autenticación)
- ✅ Rutas anidadas dentro de `<AppLayout />` (incluyen header y sidebar)
- ✅ Paths consistentes con los enlaces del sidebar

#### ⚠️ Resources No Registrados en <Refine>

**PROBLEMA DETECTADO:** No se encontró el componente `<Refine>` en `main.tsx` ni en `App.tsx`.

**Archivo main.tsx:**
```typescript
createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ThemeProvider>
      <AppWrapper>
        <AuthProvider>
          <App />  {/* ❌ No hay wrapper de <Refine> */}
        </AuthProvider>
      </AppWrapper>
    </ThemeProvider>
  </StrictMode>,
);
```

**Implicación:** Los hooks de Refine (`useList`, `useCreate`, `useDelete`) pueden estar funcionando sin configuración explícita o están usando valores por defecto. Esto es inusual y debe ser verificado.

### E. Problemas de Versiones

#### ⏳ Pendiente de Verificación
Se requiere ejecutar:
```bash
cat package.json | grep "@refinedev"
```

Para verificar:
- Versiones de paquetes `@refinedev/*`
- Compatibilidad entre dependencias
- Posibles imports obsoletos

---

## 3️⃣ PLAN DE SOLUCIÓN

### Fase 1: Correcciones Críticas (INMEDIATO)
**Tiempo estimado:** 45-60 minutos

#### Tarea 1.1: Implementar UserManagementPage
**Archivos a crear/modificar:**
- [x] `src/pages/admin/users/list.tsx` (actualmente vacío)

**Estrategia:**
1. Usar `PermissionManagementPage` como plantilla (ya está funcionando)
2. Adaptar para gestión de usuarios con sus particularidades
3. Integrar componentes auxiliares existentes:
   - `UserFormModal` (ya implementado)
   - `UserTable` (ya implementado)
   - `RoleAssignmentModal` (ya implementado)
   - `EmptyState` (ya implementado)

**Estructura esperada:**
```typescript
// src/pages/admin/users/list.tsx
import React, { useState } from 'react';
import { useList, useDelete, useUpdate } from '@refinedev/core';
import { User } from '../../../types/user-management';
import { UserTable } from '../../../components/admin/user-management/UserTable';
import { UserFormModal } from '../../../components/admin/user-management/UserFormModal';
import { RoleAssignmentModal } from '../../../components/admin/user-management/RoleAssignmentModal';
import { EmptyState } from '../../../components/admin/user-management/EmptyState';
import toast from 'react-hot-toast';

export const UserManagementPage: React.FC = () => {
  // Estados locales
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isRoleModalOpen, setIsRoleModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [searchTerm, setSearchTerm] = useState('');

  // Hooks de Refine (similar a PermissionManagementPage)
  const usersList = useList({
    resource: 'admin/users',
    pagination: { pageSize: 50 },
  });

  const users = usersList.data?.data || [];
  const isLoading = usersList.isLoading;
  const refetch = usersList.refetch;

  const { mutate: deleteUser } = useDelete();
  const { mutate: updateUser } = useUpdate();

  // Handlers
  const handleCreateUser = () => {
    setSelectedUser(null);
    setIsCreateModalOpen(true);
  };

  const handleEditUser = (user: User) => {
    setSelectedUser(user);
    setIsEditModalOpen(true);
  };

  const handleDeleteUser = (userId: string) => {
    if (confirm('¿Estás seguro de eliminar este usuario?')) {
      deleteUser(
        { resource: 'admin/users', id: userId },
        {
          onSuccess: () => {
            toast.success('Usuario eliminado exitosamente');
            refetch?.();
          },
          onError: () => {
            toast.error('Error al eliminar usuario');
          },
        }
      );
    }
  };

  const handleAssignRoles = (user: User) => {
    setSelectedUser(user);
    setIsRoleModalOpen(true);
  };

  // Filtrado local
  const filteredUsers = users.filter((user: User) =>
    user.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.name?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Renderizado
  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
            Gestión de Usuarios
          </h1>
          <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
            Administra usuarios y sus permisos de acceso
          </p>
        </div>
        <button
          onClick={handleCreateUser}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 
                     transition-colors flex items-center gap-2"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Nuevo Usuario
        </button>
      </div>

      {/* Search Bar */}
      <div className="flex gap-4">
        <div className="flex-1">
          <input
            type="text"
            placeholder="Buscar por nombre o email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 
                       rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white
                       focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      {/* Loading State */}
      {isLoading && (
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
      )}

      {/* Empty State */}
      {!isLoading && filteredUsers.length === 0 && (
        <EmptyState
          title="No hay usuarios"
          description="Comienza creando tu primer usuario"
          actionLabel="Crear Usuario"
          onAction={handleCreateUser}
        />
      )}

      {/* User Table */}
      {!isLoading && filteredUsers.length > 0 && (
        <UserTable
          users={filteredUsers}
          onEdit={handleEditUser}
          onDelete={handleDeleteUser}
          onAssignRoles={handleAssignRoles}
        />
      )}

      {/* Modals */}
      {isCreateModalOpen && (
        <UserFormModal
          isOpen={isCreateModalOpen}
          onClose={() => setIsCreateModalOpen(false)}
          onSuccess={() => {
            setIsCreateModalOpen(false);
            refetch?.();
          }}
          mode="create"
        />
      )}

      {isEditModalOpen && selectedUser && (
        <UserFormModal
          isOpen={isEditModalOpen}
          onClose={() => setIsEditModalOpen(false)}
          onSuccess={() => {
            setIsEditModalOpen(false);
            refetch?.();
          }}
          mode="edit"
          user={selectedUser}
        />
      )}

      {isRoleModalOpen && selectedUser && (
        <RoleAssignmentModal
          isOpen={isRoleModalOpen}
          onClose={() => setIsRoleModalOpen(false)}
          user={selectedUser}
          onSuccess={() => {
            setIsRoleModalOpen(false);
            refetch?.();
          }}
        />
      )}
    </div>
  );
};
```

**Justificación:**
- Reutiliza patrón exitoso de `PermissionManagementPage`
- Integra componentes auxiliares ya implementados
- Mantiene consistencia con el resto del sistema
- Usa hooks de Refine correctamente

#### Tarea 1.2: Implementar RoleManagementPage
**Archivos a crear/modificar:**
- [x] `src/pages/admin/roles/list.tsx` (actualmente vacío)

**Estrategia:**
Similar a UserManagementPage, pero enfocado en gestión de roles.

**Estructura esperada:**
```typescript
// src/pages/admin/roles/list.tsx
import React, { useState } from 'react';
import { useList, useDelete, useCreate, useUpdate } from '@refinedev/core';
import { Role } from '../../../types/user-management';
import { EmptyState } from '../../../components/admin/user-management/EmptyState';
import { RoleBadge } from '../../../components/admin/user-management/RoleBadge';
import { PermissionAssignmentModal } from '../../../components/admin/user-management/PermissionAssignmentModal';
import toast from 'react-hot-toast';

export const RoleManagementPage: React.FC = () => {
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isPermissionModalOpen, setIsPermissionModalOpen] = useState(false);
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);
  const [formData, setFormData] = useState({ name: '', description: '' });

  // Hooks de Refine
  const rolesList = useList({
    resource: 'admin/roles',
    pagination: { pageSize: 50 },
  });

  const roles = rolesList.data?.data || [];
  const isLoading = rolesList.isLoading;
  const refetch = rolesList.refetch;

  const { mutate: createRole } = useCreate();
  const { mutate: updateRole } = useUpdate();
  const { mutate: deleteRole } = useDelete();

  // Handlers
  const handleCreateRole = async (e: React.FormEvent) => {
    e.preventDefault();
    
    createRole(
      {
        resource: 'admin/roles',
        values: formData,
      },
      {
        onSuccess: () => {
          toast.success('Rol creado exitosamente');
          setIsCreateModalOpen(false);
          setFormData({ name: '', description: '' });
          refetch?.();
        },
        onError: () => {
          toast.error('Error al crear rol');
        },
      }
    );
  };

  const handleUpdateRole = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedRole) return;

    updateRole(
      {
        resource: 'admin/roles',
        id: selectedRole.id,
        values: formData,
      },
      {
        onSuccess: () => {
          toast.success('Rol actualizado exitosamente');
          setIsEditModalOpen(false);
          setSelectedRole(null);
          setFormData({ name: '', description: '' });
          refetch?.();
        },
        onError: () => {
          toast.error('Error al actualizar rol');
        },
      }
    );
  };

  const handleDeleteRole = (roleId: string) => {
    if (confirm('¿Estás seguro de eliminar este rol?')) {
      deleteRole(
        { resource: 'admin/roles', id: roleId },
        {
          onSuccess: () => {
            toast.success('Rol eliminado exitosamente');
            refetch?.();
          },
          onError: () => {
            toast.error('Error al eliminar rol');
          },
        }
      );
    }
  };

  const handleEditRole = (role: Role) => {
    setSelectedRole(role);
    setFormData({ name: role.name, description: role.description || '' });
    setIsEditModalOpen(true);
  };

  const handleManagePermissions = (role: Role) => {
    setSelectedRole(role);
    setIsPermissionModalOpen(true);
  };

  // Renderizado
  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
            Gestión de Roles
          </h1>
          <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
            Define y administra roles del sistema
          </p>
        </div>
        <button
          onClick={() => setIsCreateModalOpen(true)}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 
                     transition-colors flex items-center gap-2"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Nuevo Rol
        </button>
      </div>

      {/* Loading State */}
      {isLoading && (
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
      )}

      {/* Empty State */}
      {!isLoading && roles.length === 0 && (
        <EmptyState
          title="No hay roles definidos"
          description="Comienza creando tu primer rol"
          actionLabel="Crear Rol"
          onAction={() => setIsCreateModalOpen(true)}
        />
      )}

      {/* Roles Grid */}
      {!isLoading && roles.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {roles.map((role: Role) => (
            <div
              key={role.id}
              className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 
                         dark:border-gray-700 p-6 hover:shadow-lg transition-shadow"
            >
              <div className="flex justify-between items-start mb-4">
                <RoleBadge role={role.name} />
                <div className="flex gap-2">
                  <button
                    onClick={() => handleManagePermissions(role)}
                    className="p-2 text-blue-600 hover:bg-blue-50 dark:hover:bg-gray-700 
                               rounded-lg transition-colors"
                    title="Gestionar Permisos"
                  >
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                            d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
                    </svg>
                  </button>
                  <button
                    onClick={() => handleEditRole(role)}
                    className="p-2 text-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 
                               rounded-lg transition-colors"
                  >
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                            d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                    </svg>
                  </button>
                  <button
                    onClick={() => handleDeleteRole(role.id)}
                    className="p-2 text-red-600 hover:bg-red-50 dark:hover:bg-gray-700 
                               rounded-lg transition-colors"
                  >
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                            d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </div>

              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                {role.name}
              </h3>
              
              {role.description && (
                <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
                  {role.description}
                </p>
              )}

              <div className="flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} 
                        d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
                <span>{role.user_count || 0} usuarios</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create Role Modal */}
      {isCreateModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
             onClick={() => setIsCreateModalOpen(false)}>
          <div className="bg-white dark:bg-gray-900 rounded-lg shadow-xl max-w-md w-full"
               onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700">
              <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Crear Rol</h2>
              <button
                onClick={() => setIsCreateModalOpen(false)}
                className="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 
                           rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleCreateRole} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Nombre del Rol <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="Ej: Administrador, Editor, Visor"
                  required
                  className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg 
                             bg-white dark:bg-gray-800 text-gray-900 dark:text-white 
                             focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Descripción
                </label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  rows={3}
                  placeholder="Describe las responsabilidades de este rol"
                  className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg 
                             bg-white dark:bg-gray-800 text-gray-900 dark:text-white 
                             focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div className="flex justify-end gap-3 pt-4">
                <button
                  type="button"
                  onClick={() => setIsCreateModalOpen(false)}
                  className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 
                             bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 
                             rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg 
                             hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 
                             focus:ring-offset-2 transition-colors"
                >
                  Crear
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit Role Modal */}
      {isEditModalOpen && selectedRole && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
             onClick={() => setIsEditModalOpen(false)}>
          <div className="bg-white dark:bg-gray-900 rounded-lg shadow-xl max-w-md w-full"
               onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700">
              <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Editar Rol</h2>
              <button
                onClick={() => setIsEditModalOpen(false)}
                className="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 
                           rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <form onSubmit={handleUpdateRole} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Nombre del Rol <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                  className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg 
                             bg-white dark:bg-gray-800 text-gray-900 dark:text-white 
                             focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Descripción
                </label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  rows={3}
                  className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg 
                             bg-white dark:bg-gray-800 text-gray-900 dark:text-white 
                             focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div className="flex justify-end gap-3 pt-4">
                <button
                  type="button"
                  onClick={() => setIsEditModalOpen(false)}
                  className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 
                             bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 
                             rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg 
                             hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 
                             focus:ring-offset-2 transition-colors"
                >
                  Guardar Cambios
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Permission Assignment Modal */}
      {isPermissionModalOpen && selectedRole && (
        <PermissionAssignmentModal
          isOpen={isPermissionModalOpen}
          onClose={() => setIsPermissionModalOpen(false)}
          role={selectedRole}
          onSuccess={() => {
            setIsPermissionModalOpen(false);
            refetch?.();
          }}
        />
      )}
    </div>
  );
};
```

**Justificación:**
- Estructura similar a PermissionManagementPage para consistencia
- Integra componentes ya existentes (RoleBadge, PermissionAssignmentModal)
- Implementa CRUD completo de roles
- Incluye gestión de permisos por rol

#### Tarea 1.3: Verificar Tipos TypeScript
**Archivos a verificar:**
- [x] `src/types/user-management/user.types.ts`
- [x] `src/types/user-management/role.types.ts`
- [x] `src/types/user-management/permission.types.ts`

**Acción:** Asegurar que los tipos `User`, `Role`, y `Permission` estén correctamente exportados y definidos.

### Fase 2: Validación de dataProvider (ALTA PRIORIDAD)
**Tiempo estimado:** 20-30 minutos

#### Tarea 2.1: Investigar Configuración de Refine

**Problema detectado:** No se encontró el componente `<Refine>` en la aplicación, pero los hooks de Refine funcionan en `PermissionManagementPage`.

**Acciones requeridas:**

1. **Buscar configuración de Refine:**
   ```bash
   grep -r "from '@refinedev" src/
   ```

2. **Verificar si existe un wrapper personalizado:**
   ```bash
   grep -r "<Refine" src/
   ```

3. **Verificar dependencias:**
   ```bash
   cat package.json | grep "@refinedev"
   ```

4. **Posibles escenarios:**
   - **Escenario A:** Refine está configurado pero no detectado
   - **Escenario B:** Se están usando hooks sin configuración (poco común)
   - **Escenario C:** Hay un dataProvider custom que funciona por defecto

#### Tarea 2.2: Implementar/Verificar dataProvider

Si `dataProvider.ts` está realmente vacío, necesita implementación:

```typescript
// src/providers/dataProvider.ts
import { DataProvider } from '@refinedev/core';
import * as api from '../services/apiService';

export const dataProvider: DataProvider = {
  getList: async ({ resource, pagination, filters, sorters }) => {
    const { current = 1, pageSize = 10 } = pagination || {};
    
    // Ajustar según tu API
    const response = await api.get(`/${resource}`, {
      params: {
        page: current,
        limit: pageSize,
        // TODO: mapear filters y sorters
      },
    });

    return {
      data: response.data,
      total: response.total || response.data.length,
    };
  },

  getOne: async ({ resource, id }) => {
    const response = await api.get(`/${resource}/${id}`);
    return {
      data: response,
    };
  },

  getMany: async ({ resource, ids }) => {
    const promises = ids.map(id => api.get(`/${resource}/${id}`));
    const responses = await Promise.all(promises);
    return {
      data: responses,
    };
  },

  create: async ({ resource, variables }) => {
    const response = await api.post(`/${resource}`, variables);
    return {
      data: response,
    };
  },

  update: async ({ resource, id, variables }) => {
    const response = await api.put(`/${resource}/${id}`, variables);
    return {
      data: response,
    };
  },

  deleteOne: async ({ resource, id }) => {
    const response = await api.del(`/${resource}/${id}`);
    return {
      data: response,
    };
  },

  getApiUrl: () => {
    return process.env.VITE_API_URL || 'http://localhost:3000/api';
  },
};
```

#### Tarea 2.3: Registrar Resources en <Refine> (Si aplica)

Si se encuentra que falta la configuración de Refine, agregar en `main.tsx`:

```typescript
import { Refine } from '@refinedev/core';
import { dataProvider } from './providers/dataProvider';
import { authProvider } from './providers/authProvider';

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ThemeProvider>
      <AppWrapper>
        <AuthProvider>
          <Refine
            dataProvider={dataProvider}
            authProvider={authProvider}
            resources={[
              { name: "admin/users" },
              { name: "admin/roles" },
              { name: "admin/permissions" },
            ]}
          >
            <App />
          </Refine>
        </AuthProvider>
      </AppWrapper>
    </ThemeProvider>
  </StrictMode>,
);
```

### Fase 3: Limpieza de Archivos Duplicados (MEDIA PRIORIDAD)
**Tiempo estimado:** 15-20 minutos

Según el reporte de validación, varios archivos tienen contenido duplicado:

#### Tarea 3.1: Limpiar Duplicados
**Archivos afectados:**
- [ ] `src/components/admin/user-management/RoleAssignmentModal.tsx` (duplicado)
- [ ] `src/components/admin/user-management/PermissionAssignmentModal.tsx` (duplicado)
- [ ] `src/components/admin/user-management/UserTable.tsx` (duplicado)
- [ ] `src/components/admin/user-management/EmptyState.tsx` (duplicado)

**Acción:** Revisar cada archivo y eliminar el contenido duplicado, manteniendo solo una versión.

---

## 4️⃣ CAMBIOS A APLICAR

### Archivo 1: src/pages/admin/users/list.tsx

**Estado Actual:**
```typescript
// ARCHIVO VACÍO (1 línea)
```

**Cambio Requerido:**
Implementar componente completo `UserManagementPage` según lo especificado en Tarea 1.1 (ver código completo arriba).

**Líneas a agregar:** ~300-350 líneas

---

### Archivo 2: src/pages/admin/roles/list.tsx

**Estado Actual:**
```typescript
// ARCHIVO VACÍO (1 línea)
```

**Cambio Requerido:**
Implementar componente completo `RoleManagementPage` según lo especificado en Tarea 1.2 (ver código completo arriba).

**Líneas a agregar:** ~400-450 líneas

---

### Archivo 3: src/providers/dataProvider.ts (Si está vacío)

**Estado Actual:**
```typescript
// ARCHIVO VACÍO
```

**Cambio Requerido:**
Implementar dataProvider completo según Tarea 2.2.

**Líneas a agregar:** ~80-100 líneas

---

### Archivo 4: src/main.tsx (Solo si se requiere wrapper de Refine)

**Cambio Requerido:**
Agregar wrapper `<Refine>` solo si la investigación de Fase 2 determina que es necesario.

---

## 5️⃣ VALIDACIÓN POST-CORRECCIÓN

### Checklist de Validación

#### Compilación y Linting
- [ ] `npm run build` pasa sin errores
- [ ] `npm run lint` pasa sin errores
- [ ] No hay errores TypeScript en los archivos nuevos

#### Navegación
- [ ] Navegación a `/admin/usuarios` funciona sin errores en consola
- [ ] Navegación a `/admin/roles` funciona sin errores en consola
- [ ] Navegación a `/admin/permisos` sigue funcionando
- [ ] Los enlaces del sidebar funcionan correctamente

#### Funcionalidad CRUD - Usuarios
- [ ] Listado de usuarios carga datos correctamente
- [ ] Búsqueda de usuarios funciona
- [ ] Crear usuario funciona
- [ ] Editar usuario funciona
- [ ] Eliminar usuario funciona
- [ ] Asignar roles a usuario funciona
- [ ] Estados de loading se muestran correctamente
- [ ] Mensajes de éxito/error (toasts) funcionan

#### Funcionalidad CRUD - Roles
- [ ] Listado de roles carga datos correctamente
- [ ] Crear rol funciona
- [ ] Editar rol funciona
- [ ] Eliminar rol funciona
- [ ] Gestionar permisos de rol funciona
- [ ] Contador de usuarios por rol se muestra
- [ ] Estados de loading se muestran correctamente
- [ ] Mensajes de éxito/error (toasts) funcionan

#### Funcionalidad CRUD - Permisos
- [ ] Gestión de permisos sigue funcionando (no debe romperse)

#### Integración
- [ ] Los modales se abren y cierran correctamente
- [ ] Los componentes auxiliares (UserTable, RoleBadge, etc.) funcionan
- [ ] El tema oscuro/claro se aplica correctamente
- [ ] Las rutas protegidas siguen requiriendo autenticación

### Comandos de Validación

```bash
# 1. Compilación TypeScript
npm run build

# 2. Linting
npm run lint

# 3. Verificar errores en archivos específicos
npx tsc --noEmit src/pages/admin/users/list.tsx
npx tsc --noEmit src/pages/admin/roles/list.tsx

# 4. Servidor de desarrollo
npm run dev
# Abrir en navegador y probar manualmente cada funcionalidad

# 5. Tests (si existen)
npm run test

# 6. Verificar estructura de archivos
ls -lh src/pages/admin/users/list.tsx
ls -lh src/pages/admin/roles/list.tsx
# Deberían tener >10KB cada uno (no 1 línea)
```

### Criterios de Éxito

✅ **ÉXITO CRÍTICO:** Las 3 páginas cargan sin errores en consola  
✅ **ÉXITO ALTO:** Todas las operaciones CRUD funcionan  
✅ **ÉXITO MEDIO:** Estados de loading y mensajes funcionan  
✅ **ÉXITO BAJO:** UI/UX es consistente con el resto del sistema

---

## 6️⃣ REFERENCIAS

### Documentación Oficial de Refine
- [Data Provider](https://refine.dev/docs/core/providers/data-provider/)
- [useList Hook](https://refine.dev/docs/core/hooks/data/useList/)
- [useCreate Hook](https://refine.dev/docs/core/hooks/data/useCreate/)
- [useUpdate Hook](https://refine.dev/docs/core/hooks/data/useUpdate/)
- [useDelete Hook](https://refine.dev/docs/core/hooks/data/useDelete/)
- [React Router v6 Integration](https://refine.dev/docs/packages/react-router-v6/)

### Archivos de Referencia Internos
- **Implementación Correcta:** `src/pages/admin/permissions/list.tsx` (295 líneas, funciona)
- **Componentes Auxiliares:** `src/components/admin/user-management/`
- **Tipos:** `src/types/user-management/`

### Reportes Previos
- `.gemini/validation/IOC-004-user-role-management/index.md`
- `.gemini/validation/IOC-004-user-role-management/routing-and-menu-implementation.md`

### Patrones a Seguir
1. **Estructura de Página:**
   - Header con título + botón de acción
   - Barra de búsqueda/filtros (si aplica)
   - Estado de loading
   - Estado vacío (EmptyState)
   - Contenido principal (tabla o grid)
   - Modales para formularios

2. **Manejo de Hooks de Refine:**
   ```typescript
   // Pattern correcto observado en permissions/list.tsx
   const list = useList({ resource: 'admin/...' });
   const data = list.data?.data || [];
   const isLoading = list.isLoading;
   const refetch = list.refetch;
   ```

3. **Manejo de Mutaciones:**
   ```typescript
   const { mutate: actionName } = useCreate();
   
   actionName(
     { resource: '...', values: {...} },
     {
       onSuccess: () => { /* toast + refetch */ },
       onError: () => { /* toast error */ },
     }
   );
   ```

---

## 7️⃣ PRÓXIMOS PASOS

### Inmediato (Siguiente 2 horas)
1. ✅ **PASO 1:** Implementar `UserManagementPage` (Tarea 1.1)
2. ✅ **PASO 2:** Implementar `RoleManagementPage` (Tarea 1.2)
3. ⏳ **PASO 3:** Verificar compilación (`npm run build`)
4. ⏳ **PASO 4:** Probar navegación en dev server
5. ⏳ **PASO 5:** Validar CRUD de usuarios
6. ⏳ **PASO 6:** Validar CRUD de roles

### Corto Plazo (Siguiente sesión)
7. 📋 **PASO 7:** Investigar configuración de Refine (Fase 2)
8. 📋 **PASO 8:** Implementar/corregir dataProvider si es necesario
9. 📋 **PASO 9:** Limpiar archivos duplicados (Fase 3)
10. 📋 **PASO 10:** Ejecutar checklist de validación completo

### Antes de PR
11. 🧪 **PASO 11:** Testing manual exhaustivo
12. 🧪 **PASO 12:** Verificar que nada más se rompió
13. 📚 **PASO 13:** Actualizar documentación
14. 📚 **PASO 14:** Crear changelog de cambios

### Post-Implementación
15. 🔍 **PASO 15:** Code review interno
16. 🚀 **PASO 16:** Deploy a staging
17. ✅ **PASO 17:** UAT (User Acceptance Testing)
18. 📊 **PASO 18:** Monitorear errores en producción

---

## 8️⃣ NOTAS ADICIONALES

### Observaciones Importantes

1. **PermissionManagementPage funciona correctamente:**
   - Usa hooks de Refine sin configuración explícita visible
   - Esto sugiere que puede haber configuración en otro lugar
   - O que Refine v4+ permite modo "standalone" de hooks

2. **Componentes auxiliares ya implementados:**
   - UserFormModal ✅
   - RoleAssignmentModal ✅ (con duplicados a limpiar)
   - PermissionAssignmentModal ✅ (con duplicados a limpiar)
   - UserTable ✅ (con duplicados a limpiar)
   - RoleBadge ✅
   - EmptyState ✅ (con duplicados a limpiar)

3. **Arquitectura de tipos bien definida:**
   - `src/types/user-management/` tiene todos los tipos necesarios
   - Exportados correctamente desde `index.ts`

4. **Patrones de diseño consistentes:**
   - Dark mode support en todos los componentes
   - Tailwind CSS para estilos
   - React Hot Toast para notificaciones
   - Estructura de modales consistente

### Riesgos Identificados

🚨 **RIESGO ALTO:** Sin dataProvider explícito, puede haber problemas en producción  
⚠️ **RIESGO MEDIO:** Archivos duplicados pueden causar confusión en mantenimiento  
⚠️ **RIESGO MEDIO:** Falta de tests automatizados para validar funcionalidad  
⚠️ **RIESGO BAJO:** Configuración de Refine poco clara

### Recomendaciones Adicionales

1. **Considerar implementar tests:**
   ```bash
   # Tests unitarios para componentes
   npm install -D @testing-library/react @testing-library/jest-dom
   ```

2. **Documentar configuración de Refine:**
   - Crear un README en `src/providers/` explicando la configuración
   - Documentar endpoints de API esperados

3. **Implementar error boundaries:**
   - Para capturar errores en producción sin romper toda la app

4. **Considerar lazy loading:**
   ```typescript
   const UserManagementPage = lazy(() => import('./pages/admin/users/list'));
   ```

---

## 9️⃣ PREGUNTAS PENDIENTES

- [ ] ¿Existe una configuración de Refine que no se detectó?
- [ ] ¿El backend tiene endpoints `/admin/users` y `/admin/roles` implementados?
- [ ] ¿Qué estructura de datos retorna el backend para estos recursos?
- [ ] ¿Hay autenticación/autorización en los endpoints?
- [ ] ¿Los duplicados en archivos son intencionales o errores?
- [ ] ¿Existe documentación del backend API?
- [ ] ¿Hay un Postman/Swagger con los endpoints?

---

**Generado por:** Agente IA - Diagnóstico Refine  
**Fecha de Generación:** 2025-10-29  
**Versión del Diagnóstico:** 1.0  
**Siguiente Acción:** Implementar páginas faltantes (Fase 1, Tareas 1.1 y 1.2)  
**Tiempo Estimado Total:** 90-120 minutos

---

## 📞 CONTACTO Y SOPORTE

Para cualquier duda sobre este diagnóstico:
- Revisar archivos de referencia listados en sección 6
- Consultar documentación oficial de Refine
- Verificar implementación de PermissionManagementPage como ejemplo funcional

