('VIEW_PLANTAS', 'Ver información de plantas'),
('MANAGE_PLANTAS', 'Crear y editar plantas');
```

### 6.3. Edición de Permisos

**Campos editables**:
- ✅ `description` - Puede actualizarse en cualquier momento

**Campos readonly**:
- ❌ `name` - NO puede cambiarse después de creado (evita romper referencias)
- ❌ `id` - Auto-generado
- ❌ `createdAt` - Auto-generado
- ❌ `updatedAt` - Auto-actualizado

**Razón**: El `name` es usado como referencia en code y en asignaciones a roles. Cambiarlo rompería la integridad.

### 6.4. Eliminación de Permisos

**Reglas**:
1. Solo se puede eliminar si NO está asignado a ningún rol
2. Backend devuelve 409 Conflict si está asignado
3. Frontend muestra mensaje claro al usuario
4. Sugerencia: "Remove it from roles first"

### 6.5. Performance

- Lista completa sin paginación (típicamente < 100 permisos)
- Búsqueda con debounce de 300ms
- Refetch automático después de crear/editar/eliminar

---

## 7. Accesibilidad

### 7.1. ARIA Labels

```typescript
// Botones con labels descriptivos
<Button
  aria-label={`Edit permission ${permission.name}`}
  onClick={() => handleEditPermission(permission.id)}
>
  <PencilIcon />
</Button>

<Button
  aria-label={`Delete permission ${permission.name}`}
  onClick={() => handleDeletePermission(permission.id, permission.name)}
>
  <TrashIcon />
</Button>
```

### 7.2. Formularios

```typescript
// Labels explícitos
<label htmlFor="permission-name">Permission Name *</label>
<Input id="permission-name" {...register("name")} />

// Mensajes de error asociados
<Input
  aria-invalid={!!errors.name}
  aria-describedby={errors.name ? "name-error" : undefined}
/>
{errors.name && (
  <p id="name-error" role="alert">
    {errors.name.message}
  </p>
)}
```

### 7.3. Navegación por Teclado

- Tab order lógico: Search → New Permission → Table rows → Actions
- Enter para submit en formularios
- Escape para cerrar modales
- Focus visible en todos los elementos interactivos

---

## 8. Casos de Uso Especiales

### 8.1. Primera vez (sin permisos)

```typescript
// EmptyState con CTA para crear el primero
<EmptyState
  icon={<KeyIcon />}
  title="No permissions created yet"
  description="Create your first permission to start controlling access."
  action={
    <Button onClick={handleCreatePermission} variant="primary">
      Create First Permission
    </Button>
  }
/>
```

### 8.2. Búsqueda sin resultados

```typescript
// EmptyState con opción de limpiar búsqueda
<EmptyState
  icon={<SearchIcon />}
  title="No permissions found"
  description={`No permissions match "${searchQuery}"`}
  action={
    <Button onClick={handleClearSearch} variant="secondary">
      Clear Search
    </Button>
  }
/>
```

### 8.3. Error al cargar

```typescript
// Mensaje de error con botón de retry
<div className="text-center p-8">
  <p className="text-error-600">Failed to load permissions.</p>
  <Button onClick={handleRefresh} variant="outline" className="mt-4">
    Retry
  </Button>
</div>
```

---

**Generado por**: FTV Generator v2.0  
**Tiempo estimado**: 3-4 horas  
**Dependencias**: FTV-009 (EmptyState), Modal UI, Input UI
# FTV-IOC-004-003: Permission Management Page

**ID**: FTV-IOC-004-003  
**Componente**: `PermissionManagementPage`  
**Tipo**: Page Component  
**Ruta**: `/admin/permissions`  
**Sprint**: Sprint 3 (IOC-004)  
**Fecha**: 2025-10-28  
**Estado**: ✅ Ready for Implementation

---

## 1. Propósito y Contexto

### 1.1. Propósito
Página de administración de permisos del sistema que permite crear, editar y eliminar permisos granulares que luego se asignan a roles.

### 1.2. Caso de Uso Principal
**Actor**: Administrador (ROLE_ADMIN)

**Flujo**:
1. Admin accede a `/admin/permissions`
2. Ve lista completa de permisos disponibles en el sistema
3. Puede crear nuevo permiso con formato UPPERCASE_SNAKE_CASE
4. Puede editar descripción de permisos existentes
5. Puede eliminar permisos (sistema previene eliminar si están asignados a roles)

### 1.3. Ubicación en la App
```
Dashboard
└── Admin Section
    ├── User Management
    ├── Role Management
    └── Permission Management ← ESTA PÁGINA
```

**Restricción de Acceso**: Solo usuarios con `ROLE_ADMIN`

---

## 2. Especificación Visual

### 2.1. Wireframe ASCII

```
┌──────────────────────────────────────────────────────────────────┐
│ IOC Platform - Permission Management                 [Profile ▾] │
├──────────────────────────────────────────────────────────────────┤
│                                                                    │
│  Permissions                                    [+ New Permission]│
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                                    │
│  ┌──────────────────────────────────┐           [🔄 Refresh]     │
│  │ 🔍 Search permissions...         │                             │
│  └──────────────────────────────────┘                             │
│                                                                    │
│  ┌──────────────────────────────────────────────────────────┐    │
│  │ Name              │ Description           │ Actions      │    │
│  ├──────────────────────────────────────────────────────────┤    │
│  │ 🔑 VIEW_DASHBOARD │ Access dashboard      │ ✏️ 🗑️       │    │
│  │ 🔑 MANAGE_USERS   │ CRUD users            │ ✏️ 🗑️       │    │
│  │ 🔑 UPLOAD_FILES   │ Upload ETL files      │ ✏️ 🗑️       │    │
│  │ 🔑 VIEW_REPORTS   │ View analytics        │ ✏️ 🗑️       │    │
│  │ 🔑 MANAGE_ROLES   │ CRUD roles            │ ✏️ 🗑️       │    │
│  │ 🔑 EXPORT_DATA    │ Export to CSV/Excel   │ ✏️ 🗑️       │    │
│  └──────────────────────────────────────────────────────────┘    │
│                                                                    │
│  Total: 6 permissions                                             │
│                                                                    │
└──────────────────────────────────────────────────────────────────┘
```

### 2.2. Estados Visuales

**Estado: Loading**
```tsx
<div className="animate-pulse space-y-3">
  {[...Array(8)].map((_, i) => (
    <div key={i} className="h-12 bg-gray-200 dark:bg-gray-700 rounded"></div>
  ))}
</div>
```

**Estado: Empty (Sin permisos)**
```tsx
<EmptyState
  icon={<KeyIcon className="w-16 h-16 text-gray-400" />}
  title="No permissions found"
  description="Create your first permission to start controlling access."
  action={
    <Button onClick={handleCreatePermission} variant="primary">
      Create First Permission
    </Button>
  }
/>
```

**Estado: Error**
```tsx
<div className="rounded-lg bg-error-50 dark:bg-error-900/20 border border-error-200 dark:border-error-800 p-6">
  <p className="text-error-800 dark:text-error-200">Failed to load permissions. Please try again.</p>
  <Button onClick={refetch} variant="outline" className="mt-4">
    Retry
  </Button>
</div>
```

### 2.3. Modal de Creación/Edición

```
┌──────────────────────────────────────────────────┐
│ [X] Create Permission                            │
├──────────────────────────────────────────────────┤
│                                                  │
│  Permission Name *                               │
│  ┌────────────────────────────────────────────┐ │
│  │ VIEW_DASHBOARD                             │ │
│  └────────────────────────────────────────────┘ │
│  ⓘ Must be UPPERCASE_SNAKE_CASE                │
│                                                  │
│  Description (optional)                          │
│  ┌────────────────────────────────────────────┐ │
│  │ Allows users to view the main dashboard   │ │
│  │                                            │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  [Cancel]                      [Create Permission]│
│                                                  │
└──────────────────────────────────────────────────┘
```

---

## 3. Especificación Técnica

### 3.1. Types

```typescript
// src/types/permission.types.ts
export interface PermissionResponse {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PermissionRequest {
  name: string;
  description?: string;
}

export interface PermissionListResponse {
  content: PermissionResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
```

### 3.2. Zod Schema

```typescript
// src/schemas/permission.schema.ts
import { z } from "zod";

export const permissionSchema = z.object({
  name: z
    .string()
    .min(3, "Permission name must be at least 3 characters")
    .max(100, "Permission name must not exceed 100 characters")
    .regex(
      /^[A-Z_]+$/,
      "Permission name must be uppercase with underscores only (e.g., VIEW_DASHBOARD)"
    )
    .trim(),
  
  description: z
    .string()
    .max(500, "Description must not exceed 500 characters")
    .optional()
    .or(z.literal("")),
});

export type PermissionFormData = z.infer<typeof permissionSchema>;
```

### 3.3. State Management

```typescript
import { useTable, useDelete } from "@refinedev/core";
import { useModalForm } from "@refinedev/react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useState, useMemo } from "react";
import { debounce } from "lodash-es";

const PermissionManagementPage: React.FC = () => {
  // Hook principal para la tabla de permisos
  const {
    tableQueryResult,
    filters,
    setFilters,
  } = useTable<PermissionResponse>({
    resource: "admin/permissions",
    pagination: { current: 1, pageSize: 50 },
    sorters: { initial: [{ field: "name", order: "asc" }] },
  });

  // Hook para eliminación
  const { mutate: deletePermission, isLoading: isDeleting } = useDelete<PermissionResponse>();

  // Hook para modal de crear
  const {
    modal: { show: showCreateModal, close: closeCreateModal, visible: createModalVisible },
    refineCore: { onFinish: onFinishCreate },
    register: registerCreate,
    handleSubmit: handleSubmitCreate,
    formState: { errors: createErrors },
    reset: resetCreate,
  } = useModalForm<PermissionFormData>({
    refineCoreProps: {
      resource: "admin/permissions",
      action: "create",
      onMutationSuccess: () => {
        toast.success("Permission created successfully");
        closeCreateModal();
        tableQueryResult.refetch();
      },
      onMutationError: (error) => {
        if (error.statusCode === 409) {
          toast.error("Permission name already exists");
        } else {
          toast.error("Failed to create permission");
        }
      },
    },
    resolver: zodResolver(permissionSchema),
    modalProps: {
      autoSubmitClose: true,
      autoResetForm: true,
    },
  });

  // Hook para modal de editar
  const {
    modal: { show: showEditModal, close: closeEditModal, visible: editModalVisible },
    refineCore: { onFinish: onFinishEdit, queryResult: editQueryResult },
    register: registerEdit,
    handleSubmit: handleSubmitEdit,
    formState: { errors: editErrors },
    reset: resetEdit,
  } = useModalForm<PermissionFormData>({
    refineCoreProps: {
      resource: "admin/permissions",
      action: "edit",
      onMutationSuccess: () => {
        toast.success("Permission updated successfully");
        closeEditModal();
        tableQueryResult.refetch();
      },
      onMutationError: () => {
        toast.error("Failed to update permission");
      },
    },
    resolver: zodResolver(permissionSchema),
    modalProps: {
      autoSubmitClose: true,
    },
  });

  // Estado local para búsqueda
  const [searchQuery, setSearchQuery] = useState("");

  const { data, isLoading, isError } = tableQueryResult;
  const permissions = data?.data || [];
};
```

### 3.4. Handlers

```typescript
// Buscar permisos (debounced)
const handleSearch = useMemo(
  () =>
    debounce((query: string) => {
      setFilters([
        {
          field: "search",
          operator: "contains",
          value: query,
        },
      ]);
    }, 300),
  [setFilters]
);

// Abrir modal de crear permiso
const handleCreatePermission = () => {
  resetCreate();
  showCreateModal();
};

// Abrir modal de editar permiso
const handleEditPermission = (permissionId: number) => {
  showEditModal(permissionId);
};

// Eliminar permiso (con confirmación)
const handleDeletePermission = (permissionId: number, permissionName: string) => {
  if (confirm(`¿Está seguro de que desea eliminar el permiso "${permissionName}"?`)) {
    deletePermission(
      {
        resource: "admin/permissions",
        id: permissionId,
      },
      {
        onSuccess: () => {
          toast.success("Permiso eliminado exitosamente");
          tableQueryResult.refetch();
        },
        onError: (error) => {
          if (error.statusCode === 409) {
            toast.error(
              "El permiso está asignado a roles y no puede ser eliminado. Elimínelo de los roles primero."
            );
          } else {
            toast.error("Error al eliminar el permiso");
          }
        },
      }
    );
  }
};

// Refrescar datos
const handleRefresh = () => {
  tableQueryResult.refetch();
};

// Limpiar búsqueda
const handleClearSearch = () => {
  setSearchQuery("");
  setFilters([]);
};
```

### 3.5. Estructura del Componente

```typescript
// src/pages/admin/permissions/list.tsx
import React, { useState, useMemo } from "react";
import { useTable, useDelete } from "@refinedev/core";
import { useModalForm } from "@refinedev/react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { PlusIcon, RefreshIcon, KeyIcon, SearchIcon, PencilIcon, TrashIcon } from "@heroicons/react/outline";
import { debounce } from "lodash-es";
import { toast } from "react-hot-toast";

import { EmptyState } from "@/components/common/EmptyState";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Modal } from "@/components/ui/Modal";

import { permissionSchema, type PermissionFormData } from "@/schemas/permission.schema";
import type { PermissionResponse } from "@/types/permission.types";

export const PermissionManagementPage: React.FC = () => {
  // State management (ver sección 3.3)
  // Handlers (ver sección 3.4)

  return (
    <div className="p-6 bg-gray-50 dark:bg-gray-900 min-h-screen">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-gray-800 dark:text-white">Permisos</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
            Administre permisos granulares del sistema
          </p>
        </div>
        <Button
          onClick={handleCreatePermission}
          variant="primary"
          leftIcon={<PlusIcon className="w-5 h-5" />}
        >
          Nuevo Permiso
        </Button>
      </div>

      {/* Búsqueda */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm p-4 mb-6">
        <div className="flex items-center gap-4">
          <div className="flex-1">
            <Input
              type="search"
              placeholder="Buscar permisos..."
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                handleSearch(e.target.value);
              }}
              leftIcon={<SearchIcon className="w-5 h-5 text-gray-400" />}
            />
          </div>
          <Button
            onClick={handleRefresh}
            variant="ghost"
            leftIcon={<RefreshIcon className="w-5 h-5" />}
          >
            Actualizar
          </Button>
        </div>
      </div>

      {/* Tabla de Permisos */}
      {isLoading ? (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm p-8">
          <div className="animate-pulse space-y-3">
            {[...Array(8)].map((_, i) => (
              <div key={i} className="h-12 bg-gray-200 dark:bg-gray-700 rounded"></div>
            ))}
          </div>
        </div>
      ) : isError ? (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm p-8 text-center">
          <p className="text-error-600 dark:text-error-400">Error al cargar permisos.</p>
          <Button onClick={handleRefresh} variant="outline" className="mt-4">
            Reintentar
          </Button>
        </div>
      ) : permissions.length === 0 ? (
        <EmptyState
          icon={<KeyIcon className="w-16 h-16 text-gray-400" />}
          title={searchQuery ? "No se encontraron permisos" : "Aún no hay permisos creados"}
          description={
            searchQuery
              ? `No hay permisos que coincidan con "${searchQuery}"`
              : "Cree su primer permiso para comenzar a controlar el acceso."
          }
          action={
            searchQuery ? (
              <Button onClick={handleClearSearch} variant="secondary">
                Limpiar Búsqueda
              </Button>
            ) : (
              <Button onClick={handleCreatePermission} variant="primary">
                Crear Primer Permiso
              </Button>
            )
          }
        />
      ) : (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
            <thead className="bg-gray-50 dark:bg-gray-900">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Name
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Description
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
              {permissions.map((permission) => (
                <tr key={permission.id} className="hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <KeyIcon className="w-5 h-5 text-brand-500 mr-3 flex-shrink-0" />
                      <span className="text-sm font-mono font-medium text-gray-900 dark:text-white">
                        {permission.name}
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                      {permission.description || "—"}
                    </p>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div className="flex items-center justify-end gap-2">
                      <Button
                        onClick={() => handleEditPermission(permission.id)}
                        variant="ghost"
                        size="sm"
                        title="Editar permiso"
                        aria-label={`Editar permiso ${permission.name}`}
                      >
                        <PencilIcon className="w-4 h-4" />
                      </Button>
                      <Button
                        onClick={() => handleDeletePermission(permission.id, permission.name)}
                        variant="ghost"
                        size="sm"
                        disabled={isDeleting}
                        title="Eliminar permiso"
                        aria-label={`Eliminar permiso ${permission.name}`}
                        className="text-error-600 hover:text-error-700 dark:text-error-400 dark:hover:text-error-300"
                      >
                        <TrashIcon className="w-4 h-4" />
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* Footer */}
          <div className="px-6 py-3 bg-gray-50 dark:bg-gray-900 border-t border-gray-200 dark:border-gray-700">
            <p className="text-sm text-gray-600 dark:text-gray-400">
              Total: {permissions.length} permiso{permissions.length !== 1 ? "s" : ""}
            </p>
          </div>
        </div>
      )}

      {/* Modal de Crear Permiso */}
      <Modal
        isOpen={createModalVisible}
        onClose={closeCreateModal}
        title="Create Permission"
        size="md"
      >
        <form onSubmit={handleSubmitCreate(onFinishCreate)} className="space-y-6">
          {/* Permission Name */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Permission Name *
            </label>
            <Input
              {...registerCreate("name")}
              type="text"
              placeholder="VIEW_DASHBOARD"
              error={createErrors.name?.message}
              className="font-mono"
            />
            {createErrors.name && (
              <p className="mt-1 text-sm text-error-600 dark:text-error-400">
                {createErrors.name.message}
              </p>
            )}
            <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
              ⓘ Debe estar en MAYÚSCULAS_CON_GUIONES_BAJOS (ej: VIEW_DASHBOARD, MANAGE_USERS)
            </p>
          </div>

          {/* Descripción */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Descripción (opcional)
            </label>
            <textarea
              {...registerCreate("description")}
              placeholder="Permite a los usuarios ver el panel principal"
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-brand-500 focus:border-brand-500 dark:bg-gray-700 dark:text-white"
            />
            {createErrors.description && (
              <p className="mt-1 text-sm text-error-600 dark:text-error-400">
                {createErrors.description.message}
              </p>
            )}
          </div>

          {/* Botones */}
          <div className="flex justify-end gap-3 pt-4 border-t border-gray-200 dark:border-gray-700">
            <Button onClick={closeCreateModal} variant="outline" type="button">
              Cancelar
            </Button>
            <Button type="submit" variant="primary">
              Crear Permiso
            </Button>
          </div>
        </form>
      </Modal>

      {/* Modal de Editar Permiso */}
      <Modal
        isOpen={editModalVisible}
        onClose={closeEditModal}
        title="Edit Permission"
        size="md"
      >
        {editQueryResult?.isLoading ? (
          <div className="p-8 text-center">
            <p className="text-gray-500 dark:text-gray-400">Loading permission data...</p>
          </div>
        ) : (
          <form onSubmit={handleSubmitEdit(onFinishEdit)} className="space-y-6">
            {/* Permission Name (readonly) */}
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Permission Name
              </label>
              <Input
                {...registerEdit("name")}
                type="text"
                disabled
                className="font-mono bg-gray-100 dark:bg-gray-700"
              />
              <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                ⓘ El nombre del permiso no puede ser modificado
              </p>
            </div>

            {/* Descripción */}
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Descripción
              </label>
              <textarea
                {...registerEdit("description")}
                placeholder="Permite a los usuarios ver el panel principal"
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-brand-500 focus:border-brand-500 dark:bg-gray-700 dark:text-white"
              />
              {editErrors.description && (
                <p className="mt-1 text-sm text-error-600 dark:text-error-400">
                  {editErrors.description.message}
                </p>
              )}
            </div>

            {/* Botones */}
            <div className="flex justify-end gap-3 pt-4 border-t border-gray-200 dark:border-gray-700">
              <Button onClick={closeEditModal} variant="outline" type="button">
                Cancelar
              </Button>
              <Button type="submit" variant="primary">
                Actualizar Permiso
              </Button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
};
```

---

## 4. Integración con API

### 4.1. Endpoints Utilizados

```typescript
// GET /api/v1/admin/permissions
// Query params: search, page, size, sort
// Response: PermissionListResponse
{
  "content": [
    {
      "id": 1,
      "name": "VIEW_DASHBOARD",
      "description": "Access dashboard",
      "createdAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-01-01T10:00:00Z"
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "size": 50,
  "number": 0
}

// POST /api/v1/admin/permissions
// Body: PermissionRequest
{
  "name": "VIEW_DASHBOARD",
  "description": "Access dashboard"
}
// Response: 201 Created + PermissionResponse
// Error: 409 Conflict si el nombre ya existe

// PUT /api/v1/admin/permissions/{id}
// Body: PermissionRequest (solo description es editable)
{
  "name": "VIEW_DASHBOARD",  // readonly, pero requerido
  "description": "Updated description"
}
// Response: 200 OK + PermissionResponse

// DELETE /api/v1/admin/permissions/{id}
// Response: 204 No Content
// Error: 409 Conflict si está asignado a roles
```

---

## 5. Validación y Tests

### 5.1. Unit Tests

```typescript
describe("PermissionManagementPage", () => {
  it("renderiza el título correctamente", () => {
    render(<PermissionManagementPage />);
    expect(screen.getByText("Permissions")).toBeInTheDocument();
  });

  it("muestra loading state durante carga", () => {
    render(<PermissionManagementPage />);
    expect(screen.getByRole("status")).toBeInTheDocument();
  });

  it("renderiza lista de permisos correctamente", async () => {
    const mockPermissions = [
      { id: 1, name: "VIEW_DASHBOARD", description: "Access dashboard" },
      { id: 2, name: "MANAGE_USERS", description: "CRUD users" },
    ];

    render(<PermissionManagementPage />);

    await waitFor(() => {
      expect(screen.getByText("VIEW_DASHBOARD")).toBeInTheDocument();
      expect(screen.getByText("MANAGE_USERS")).toBeInTheDocument();
    });
  });

  it("valida formato de nombre de permiso (UPPERCASE_SNAKE_CASE)", async () => {
    const user = userEvent.setup();
    render(<PermissionManagementPage />);
    
    await user.click(screen.getByText("New Permission"));
    await user.type(screen.getByLabelText(/Permission Name/i), "invalid-format");
    await user.click(screen.getByText("Create Permission"));
    
    expect(
      await screen.findByText(/uppercase with underscores only/i)
    ).toBeInTheDocument();
  });

  it("crea permiso exitosamente", async () => {
    const user = userEvent.setup();
    render(<PermissionManagementPage />);
    
    await user.click(screen.getByText("New Permission"));
    await user.type(screen.getByLabelText(/Permission Name/i), "VIEW_ANALYTICS");
    await user.type(screen.getByLabelText(/Description/i), "View analytics dashboard");
    await user.click(screen.getByText("Create Permission"));
    
    await waitFor(() => {
      expect(toast.success).toHaveBeenCalledWith("Permission created successfully");
    });
  });

  it("maneja error 409 al crear permiso duplicado", async () => {
    const user = userEvent.setup();
    mockCreatePermission.mockRejectedValue({ statusCode: 409 });
    
    render(<PermissionManagementPage />);
    await user.click(screen.getByText("New Permission"));
    await user.type(screen.getByLabelText(/Permission Name/i), "VIEW_DASHBOARD");
    await user.click(screen.getByText("Create Permission"));
    
    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Permission name already exists");
    });
  });

  it("edita descripción de permiso exitosamente", async () => {
    const user = userEvent.setup();
    render(<PermissionManagementPage />);
    
    await waitFor(() => screen.getByText("VIEW_DASHBOARD"));
    
    const editButtons = screen.getAllByLabelText(/Edit permission/i);
    await user.click(editButtons[0]);
    
    await waitFor(() => screen.getByText("Edit Permission"));
    
    const descriptionField = screen.getByLabelText(/Description/i);
    await user.clear(descriptionField);
    await user.type(descriptionField, "Updated description");
    await user.click(screen.getByText("Update Permission"));
    
    await waitFor(() => {
      expect(toast.success).toHaveBeenCalledWith("Permission updated successfully");
    });
  });

  it("maneja error 409 al intentar eliminar permiso asignado", async () => {
    const user = userEvent.setup();
    window.confirm = vi.fn(() => true);
    mockDeletePermission.mockRejectedValue({ statusCode: 409 });
    
    render(<PermissionManagementPage />);
    await waitFor(() => screen.getByRole("table"));
    
    const deleteButtons = screen.getAllByLabelText(/Delete permission/i);
    await user.click(deleteButtons[0]);
    
    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith(
        expect.stringContaining("assigned to roles")
      );
    });
  });

  it("filtra permisos por búsqueda", async () => {
    const user = userEvent.setup();
    render(<PermissionManagementPage />);
    
    await waitFor(() => screen.getByPlaceholderText("Search permissions..."));
    
    const searchInput = screen.getByPlaceholderText("Search permissions...");
    await user.type(searchInput, "MANAGE");
    
    // Debounce de 300ms
    await waitFor(
      () => {
        expect(screen.queryByText("VIEW_DASHBOARD")).not.toBeInTheDocument();
        expect(screen.getByText("MANAGE_USERS")).toBeInTheDocument();
      },
      { timeout: 500 }
    );
  });

  it("muestra empty state cuando no hay resultados de búsqueda", async () => {
    const user = userEvent.setup();
    render(<PermissionManagementPage />);
    
    const searchInput = screen.getByPlaceholderText("Search permissions...");
    await user.type(searchInput, "NONEXISTENT");
    
    await waitFor(() => {
      expect(screen.getByText(/No permissions match/i)).toBeInTheDocument();
      expect(screen.getByText("Clear Search")).toBeInTheDocument();
    });
  });

  it("limpia búsqueda al hacer click en Clear Search", async () => {
    const user = userEvent.setup();
    render(<PermissionManagementPage />);
    
    const searchInput = screen.getByPlaceholderText("Search permissions...");
    await user.type(searchInput, "NONEXISTENT");
    
    await waitFor(() => screen.getByText("Clear Search"));
    await user.click(screen.getByText("Clear Search"));
    
    expect(searchInput).toHaveValue("");
  });
});
```

---

## 6. Notas de Implementación

### 6.1. Convenciones de Nombres

**Formato**: `UPPERCASE_SNAKE_CASE`

**Ejemplos válidos**:
- `VIEW_DASHBOARD`
- `MANAGE_USERS`
- `UPLOAD_FILES`
- `EXPORT_DATA`
- `CREATE_REPORTS`
- `DELETE_RECORDS`

**Prefijos recomendados**:
- `VIEW_` - Permisos de solo lectura
- `MANAGE_` - Permisos CRUD completos
- `CREATE_` - Solo crear
- `UPDATE_` - Solo actualizar
- `DELETE_` - Solo eliminar
- `EXPORT_` - Exportar datos
- `IMPORT_` - Importar datos

### 6.2. Permisos Seed Recomendados

```sql
-- Autenticación y Dashboard
INSERT INTO permissions (name, description) VALUES
('VIEW_DASHBOARD', 'Access main dashboard'),
('VIEW_ANALYTICS', 'View analytics and charts');

-- Gestión de Usuarios
INSERT INTO permissions (name, description) VALUES
('VIEW_USERS', 'View user list'),
('MANAGE_USERS', 'Create, edit, and delete users'),
('MANAGE_ROLES', 'Create and assign roles'),
('MANAGE_PERMISSIONS', 'Create and assign permissions');

-- ETL y Datos
INSERT INTO permissions (name, description) VALUES
('UPLOAD_FILES', 'Upload ETL data files'),
('MANAGE_ETL', 'Configure and manage ETL pipelines'),
('VIEW_ETL_LOGS', 'View ETL execution logs');

-- Reportes
INSERT INTO permissions (name, description) VALUES
('VIEW_REPORTS', 'View generated reports'),
('EXPORT_DATA', 'Export data to CSV/Excel'),
('CREATE_REPORTS', 'Create custom reports');

-- Plantas
INSERT INTO permissions (name, description) VALUES
('VIEW_PLANTAS', 'Ver información de plantas'),
('MANAGE_PLANTAS', 'Crear y editar plantas');
````
