# FTV-IOC-004-002: Role Management Page

**ID**: FTV-IOC-004-002  
**Componente**: `RoleManagementPage`  
**Tipo**: Page Component  
**Ruta**: `/admin/roles`  
**Sprint**: Sprint 3 (IOC-004)  
**Fecha**: 2025-10-28  
**Estado**: ✅ Ready for Implementation

---

## 1. Propósito y Contexto

### 1.1. Propósito
Página de administración de roles del sistema que permite crear, editar, eliminar roles y gestionar sus permisos asociados.

### 1.2. Caso de Uso Principal
**Actor**: Administrador (ROLE_ADMIN)

**Flujo**:
1. Admin accede a `/admin/roles`
2. Ve lista de roles con cantidad de usuarios y permisos
3. Puede crear nuevo rol, editar nombre/descripción, o eliminar
4. Puede asignar/remover permisos a roles mediante modal
5. Sistema previene eliminar roles en uso

### 1.3. Ubicación en la App
```
Dashboard
└── Admin Section
    ├── User Management
    └── Role Management ← ESTA PÁGINA
        └── Permission Management
```

**Restricción de Acceso**: Solo usuarios con `ROLE_ADMIN`

---

## 2. Especificación Visual

### 2.1. Wireframe ASCII

```
┌─────────────────────────────────────────────────────────────────┐
│ IOC Platform - Role Management                      [Profile ▾] │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  Roles                                            [+ New Role]   │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                                   │
│  ┌────────────────────────────────────────────┐  [🔄 Refresh]   │
│  │ 🔍 Search roles...                         │                  │
│  └────────────────────────────────────────────┘                  │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Name      │ Description      │ Users │ Perms │ Actions  │   │
│  ├─────────────────────────────────────────────────────────┤   │
│  │ 🔵 ADMIN  │ Full system     │  5    │  12   │ 🔐 ✏️ 🗑️│   │
│  │ 🟢 GERENTE│ Plant manager   │  15   │   8   │ 🔐 ✏️ 🗑️│   │
│  │ 🟡 ANALISTA│ Data analyst   │  25   │   4   │ 🔐 ✏️ 🗑️│   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                   │
│  Total: 3 roles                                                  │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2. Estados Visuales

**Estado: Loading**
```tsx
<div className="bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm p-8">
  <div className="animate-pulse space-y-4">
    {[...Array(3)].map((_, i) => (
      <div key={i} className="h-16 bg-gray-200 dark:bg-gray-700 rounded"></div>
    ))}
  </div>
</div>
```

**Estado: Empty**
```tsx
<EmptyState
  icon={<ShieldCheckIcon className="w-16 h-16 text-gray-400" />}
  title="No roles configured"
  description="Create your first role to start organizing permissions."
  action={
    <Button onClick={handleCreateRole} variant="primary">
      Create First Role
    </Button>
  }
/>
```

**Estado: Error al Eliminar Rol en Uso**
```tsx
// Toast notification
toast.error("Cannot delete role. It's assigned to 15 users. Remove users first.");
```

---

## 3. Especificación Técnica

### 3.1. Types

```typescript
// src/types/role.types.ts
export interface RoleResponse {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
  usersCount: number;
  permissions: string[];
}

export interface RoleRequest {
  name: string;
  description?: string;
}

export interface RoleListResponse {
  content: RoleResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
```

### 3.2. Zod Schema

```typescript
// src/schemas/role.schema.ts
import { z } from "zod";

export const roleSchema = z.object({
  name: z
    .string()
    .min(2, "Role name must be at least 2 characters")
    .max(50, "Role name must not exceed 50 characters")
    .regex(/^[A-Z_]+$/, "Role name must be uppercase with underscores only")
    .trim(),
  description: z
    .string()
    .max(200, "Description must not exceed 200 characters")
    .optional(),
});

export type RoleFormData = z.infer<typeof roleSchema>;
```

### 3.3. State Management

```typescript
import { useTable, useDelete } from "@refinedev/core";
import { useForm } from "@refinedev/react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { roleSchema } from "@/schemas/role.schema";

// Hook principal para la tabla
const {
  tableQueryResult,
  current,
  setCurrent,
  pageSize,
  filters,
  setFilters,
} = useTable<RoleResponse>({
  resource: "admin/roles",
  pagination: {
    current: 1,
    pageSize: 20,
  },
  sorters: {
    initial: [{ field: "name", order: "asc" }],
  },
});

// Hook para eliminación
const { mutate: deleteRole, isLoading: isDeleting } = useDelete<RoleResponse>();

// Hook para formulario
const {
  refineCore: { onFinish, formLoading },
  register,
  handleSubmit,
  formState: { errors },
  reset,
} = useForm<RoleFormData>({
  refineCoreProps: {
    resource: "admin/roles",
    action: "create",
  },
  resolver: zodResolver(roleSchema),
});

// Estado para modales
const [showCreateModal, setShowCreateModal] = useState(false);
const [showEditModal, setShowEditModal] = useState(false);
const [showPermissionModal, setShowPermissionModal] = useState(false);
const [selectedRoleId, setSelectedRoleId] = useState<number | null>(null);
const [searchQuery, setSearchQuery] = useState("");
```

### 3.4. Handlers

```typescript
// Buscar roles
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

// Crear rol
const handleCreateRole = () => {
  reset();
  setShowCreateModal(true);
};

// Editar rol
const handleEditRole = (roleId: number) => {
  setSelectedRoleId(roleId);
  // Cargar datos del rol en el formulario
  setShowEditModal(true);
};

// Eliminar rol
const handleDeleteRole = (roleId: number, roleName: string, usersCount: number) => {
  // Verificar si es un rol del sistema
  if (SYSTEM_ROLES.includes(roleName)) {
    toast.error("No se pueden eliminar roles del sistema. Están protegidos.");
    return;
  }

  if (usersCount > 0) {
    toast.error(
      `No se puede eliminar este rol. Está asignado a ${usersCount} usuario(s). Elimine los usuarios primero.`
    );
    return;
  }

  if (confirm("¿Está seguro de que desea eliminar este rol?")) {
    deleteRole(
      {
        resource: "admin/roles",
        id: roleId,
      },
      {
        onSuccess: () => {
          toast.success("Rol eliminado exitosamente");
        },
        onError: (error) => {
          if (error.statusCode === 409) {
            toast.error("El rol está en uso y no puede ser eliminado");
          } else {
            toast.error("Error al eliminar el rol");
          }
        },
      }
    );
  }
};

// Gestionar permisos
const handleManagePermissions = (roleId: number) => {
  setSelectedRoleId(roleId);
  setShowPermissionModal(true);
};

// Refrescar datos
const handleRefresh = () => {
  tableQueryResult.refetch();
};
```

### 3.5. Estructura del Componente

```typescript
// src/pages/admin/roles/list.tsx
import React, { useState, useMemo } from "react";
import { useTable, useDelete } from "@refinedev/core";
import { useForm } from "@refinedev/react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { ShieldCheckIcon, PlusIcon, RefreshIcon, SearchIcon } from "@heroicons/react/outline";
import { debounce } from "lodash-es";
import { toast } from "react-hot-toast";

import { RoleBadge } from "@/components/roles/RoleBadge";
import { PermissionAssignmentModal } from "@/components/roles/PermissionAssignmentModal";
import { EmptyState } from "@/components/common/EmptyState";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Modal } from "@/components/ui/Modal";

import { roleSchema, type RoleFormData } from "@/schemas/role.schema";
import type { RoleResponse } from "@/types/role.types";

export const RoleManagementPage: React.FC = () => {
  // Hooks (ver sección 3.3)
  const { tableQueryResult, filters, setFilters } = useTable<RoleResponse>({
    resource: "admin/roles",
  });

  const { mutate: deleteRole, isLoading: isDeleting } = useDelete<RoleResponse>();

  // Estado local
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showPermissionModal, setShowPermissionModal] = useState(false);
  const [selectedRoleId, setSelectedRoleId] = useState<number | null>(null);
  const [searchQuery, setSearchQuery] = useState("");

  // Handlers (ver sección 3.4)

  const { data, isLoading, isError } = tableQueryResult;
  const roles = data?.data || [];

  return (
    <div className="p-6 bg-gray-50 dark:bg-gray-900 min-h-screen">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-gray-800 dark:text-white">Roles</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
            Administre roles del sistema y sus permisos
          </p>
        </div>
        <Button
          onClick={handleCreateRole}
          variant="primary"
          leftIcon={<PlusIcon className="w-5 h-5" />}
        >
          Nuevo Rol
        </Button>
      </div>

      {/* Búsqueda */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm p-4 mb-6">
        <div className="flex items-center gap-4">
          <div className="flex-1">
            <Input
              type="search"
              placeholder="Buscar roles..."
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

      {/* Tabla de Roles */}
      {isLoading ? (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm p-8">
          <div className="animate-pulse space-y-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-16 bg-gray-200 dark:bg-gray-700 rounded"></div>
            ))}
          </div>
        </div>
      ) : isError ? (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm p-8 text-center">
          <p className="text-error-600 dark:text-error-400">Failed to load roles.</p>
          <Button onClick={handleRefresh} variant="outline" className="mt-4">
            Retry
          </Button>
        </div>
      ) : roles.length === 0 ? (
        <EmptyState
          icon={<ShieldCheckIcon className="w-16 h-16 text-gray-400" />}
          title="No se encontraron roles"
          description="Cree su primer rol para comenzar a organizar permisos."
          action={
            <Button onClick={handleCreateRole} variant="primary">
              Crear Primer Rol
            </Button>
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
                <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Users
                </th>
                <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Permissions
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
              {roles.map((role) => (
                <tr key={role.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <RoleBadge role={role.name} />
                  </td>
                  <td className="px-6 py-4">
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                      {role.description || "—"}
                    </p>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-center">
                    <span className="text-sm font-medium text-gray-900 dark:text-white">
                      {role.usersCount}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-center">
                    <span className="text-sm font-medium text-gray-900 dark:text-white">
                      {role.permissions.length}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div className="flex items-center justify-end gap-2">
                      <Button
                        onClick={() => handleManagePermissions(role.id)}
                        variant="ghost"
                        size="sm"
                        title="Gestionar Permisos"
                      >
                        🔐
                      </Button>
                      <Button
                        onClick={() => handleEditRole(role.id)}
                        variant="ghost"
                        size="sm"
                        title="Editar"
                      >
                        ✏️
                      </Button>
                      <Button
                        onClick={() => handleDeleteRole(role.id, role.name, role.usersCount)}
                        variant="ghost"
                        size="sm"
                        disabled={isDeleting}
                        title="Eliminar"
                      >
                        🗑️
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* Footer con total */}
          <div className="px-6 py-3 bg-gray-50 dark:bg-gray-900 border-t border-gray-200 dark:border-gray-700">
            <p className="text-sm text-gray-600 dark:text-gray-400">
              Total: {roles.length} rol{roles.length !== 1 ? "es" : ""}
            </p>
          </div>
        </div>
      )}

      {/* Modal de Permisos */}
      <PermissionAssignmentModal
        isOpen={showPermissionModal}
        onClose={() => setShowPermissionModal(false)}
        roleId={selectedRoleId}
      />
    </div>
  );
};
```

---

## 4. Integración con API

### 4.1. Endpoints Utilizados

```typescript
// GET /api/v1/admin/roles
// Query params: search, page, size, sort
// Response: RoleListResponse

// POST /api/v1/admin/roles
// Body: RoleRequest
// Response: 201 Created + RoleResponse

// PUT /api/v1/admin/roles/{id}
// Body: RoleRequest
// Response: 200 OK + RoleResponse

// DELETE /api/v1/admin/roles/{id}
// Response: 204 No Content (o 409 si está en uso)
```

### 4.2. Protección de Roles Seed

```typescript
// Añadir validación adicional para roles del sistema
const SYSTEM_ROLES = ["ADMIN", "GERENTE", "ANALISTA"];

const handleDeleteRole = (roleId: number, roleName: string, usersCount: number) => {
  // Verificar si es un rol del sistema
  if (SYSTEM_ROLES.includes(roleName)) {
    toast.error("No se pueden eliminar roles del sistema. Están protegidos.");
    return;
  }

  if (usersCount > 0) {
    toast.error(
      `No se puede eliminar este rol. Está asignado a ${usersCount} usuario(s). Elimine los usuarios primero.`
    );
    return;
  }

  // ...existing confirmation and delete logic
};
```

---

## 5. Validación y Tests

```typescript
describe("RoleManagementPage", () => {
  it("previene eliminar rol con usuarios asignados", async () => {
    const user = userEvent.setup();
    render(<RoleManagementPage />);
    
    await waitFor(() => screen.getByRole("table"));
    
    const roleWithUsers = screen.getByText("GERENTE").closest("tr");
    const deleteButton = within(roleWithUsers).getByTitle("Delete");
    
    await user.click(deleteButton);
    
    expect(toast.error).toHaveBeenCalledWith(
      expect.stringContaining("assigned to")
    );
  });

  it("permite eliminar rol sin usuarios", async () => {
    window.confirm = vi.fn(() => true);
    const user = userEvent.setup();
    render(<RoleManagementPage />);
    
    await waitFor(() => screen.getByRole("table"));
    
    // Simular rol sin usuarios (usersCount: 0)
    const deleteButton = screen.getAllByTitle("Delete")[0];
    await user.click(deleteButton);
    
    expect(window.confirm).toHaveBeenCalled();
  });
});
```

---

## 6. Notas de Implementación

### 6.1. Reglas de Negocio
- Roles seed (ADMIN, GERENTE, ANALISTA) pueden protegerse contra eliminación
- Nombre de rol debe ser único (case-insensitive)
- Formato recomendado: UPPERCASE_SNAKE_CASE
- No permitir eliminar roles con usuarios asignados

### 6.2. UX
- Badge con color distintivo por rol
- Confirmación antes de eliminar
- Mensaje claro si rol está en uso
- Contador de usuarios y permisos visible

---

**Generado por**: FTV Generator v2.0  
**Tiempo estimado**: 4-6 horas  
**Dependencias**: FTV-006, FTV-008, FTV-009
