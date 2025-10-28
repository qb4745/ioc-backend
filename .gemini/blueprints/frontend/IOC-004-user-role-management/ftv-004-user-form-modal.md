# FTV-IOC-004-004: User Form Modal

**ID**: FTV-IOC-004-004  
**Componente**: `UserFormModal`  
**Tipo**: Modal Component  
**Sprint**: Sprint 3 (IOC-004)  
**Fecha**: 2025-10-28  
**Estado**: ✅ Ready for Implementation

---

## 1. Propósito y Contexto

### 1.1. Propósito
Modal reutilizable para crear y editar usuarios con validación completa de formulario.

### 1.2. Modos de Operación
- **Modo Create**: Todos los campos habilitados, incluido email y supabaseUserId
- **Modo Edit**: Email y supabaseUserId readonly (no editables)

---

## 2. Especificación Visual

### 2.1. Wireframe ASCII

```
┌──────────────────────────────────────────────────┐
│ [X] Create User                                  │
├──────────────────────────────────────────────────┤
│                                                  │
│  Email *                                         │
│  ┌────────────────────────────────────────────┐ │
│  │ user@cambiaso.com                          │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  Supabase User ID *                              │
│  ┌────────────────────────────────────────────┐ │
│  │ 123e4567-e89b-12d3-a456-426614174000       │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  First Name *          Second Name               │
│  ┌────────────────┐    ┌────────────────────┐   │
│  │ Juan           │    │ Carlos             │   │
│  └────────────────┘    └────────────────────┘   │
│                                                  │
│  First Lastname *      Second Lastname           │
│  ┌────────────────┐    ┌────────────────────┐   │
│  │ Pérez          │    │ García             │   │
│  └────────────────┘    └────────────────────┘   │
│                                                  │
│  Plant                 Cost Center               │
│  ┌────────────────┐    ┌────────────────────┐   │
│  │ Lima ▾         │    │ CC-001             │   │
│  └────────────────┘    └────────────────────┘   │
│                                                  │
│  Contract Date         Status                    │
│  ┌────────────────┐    ┌────────────────────┐   │
│  │ 2024-01-15     │    │ ☑ Active           │   │
│  └────────────────┘    └────────────────────┘   │
│                                                  │
│  Initial Roles (optional)                        │
│  ┌────────────────────────────────────────────┐ │
│  │ ☐ ADMIN                                    │ │
│  │ ☑ GERENTE                                  │ │
│  │ ☐ ANALISTA                                 │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  [Cancel]                        [Create User]  │
│                                                  │
└──────────────────────────────────────────────────┘
```

---

## 3. Especificación Técnica

### 3.1. Props Interface

```typescript
// src/components/users/UserFormModal.tsx
interface UserFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  userId?: number;                    // Si está presente, modo EDIT
  onSuccess?: () => void;             // Callback tras crear/editar
}
```

### 3.2. Zod Schema

```typescript
// src/schemas/user.schema.ts
import { z } from "zod";

export const userFormSchema = z.object({
  email: z
    .string()
    .email("Invalid email format")
    .min(5, "Email must be at least 5 characters")
    .max(255, "Email must not exceed 255 characters")
    .toLowerCase()
    .trim(),
  
  supabaseUserId: z
    .string()
    .uuid("Invalid UUID format")
    .trim(),
  
  primerNombre: z
    .string()
    .min(2, "First name must be at least 2 characters")
    .max(100, "First name must not exceed 100 characters")
    .trim(),
  
  segundoNombre: z
    .string()
    .max(100, "Second name must not exceed 100 characters")
    .optional()
    .or(z.literal("")),
  
  primerApellido: z
    .string()
    .min(2, "First lastname must be at least 2 characters")
    .max(100, "First lastname must not exceed 100 characters")
    .trim(),
  
  segundoApellido: z
    .string()
    .max(100, "Second lastname must not exceed 100 characters")
    .optional()
    .or(z.literal("")),
  
  plantaId: z
    .number()
    .int()
    .positive()
    .optional()
    .nullable(),
  
  centroCosto: z
    .string()
    .max(50, "Cost center must not exceed 50 characters")
    .optional()
    .or(z.literal("")),
  
  fechaContrato: z
    .string()
    .regex(/^\d{4}-\d{2}-\d{2}$/, "Date must be in YYYY-MM-DD format")
    .optional()
    .or(z.literal("")),
  
  isActive: z
    .boolean()
    .default(true),
  
  roles: z
    .array(z.string())
    .optional(),
});

export type UserFormData = z.infer<typeof userFormSchema>;
```

### 3.3. State Management

```typescript
import { useForm } from "@refinedev/react-hook-form";
import { useOne, useList } from "@refinedev/core";
import { zodResolver } from "@hookform/resolvers/zod";

const UserFormModal: React.FC<UserFormModalProps> = ({ isOpen, onClose, userId, onSuccess }) => {
  // Cargar usuario si está en modo EDIT
  const { data: userData, isLoading: isLoadingUser } = useOne<UsuarioResponse>({
    resource: "admin/users",
    id: userId || "",
    queryOptions: {
      enabled: !!userId,
    },
  });

  // Cargar plantas para el select
  const { data: plantasData } = useList<PlantaResponse>({
    resource: "plantas",
    pagination: { current: 1, pageSize: 100 },
  });

  // Cargar roles disponibles
  const { data: rolesData } = useList<RoleResponse>({
    resource: "admin/roles",
    pagination: { current: 1, pageSize: 50 },
  });

  // Hook de formulario con Refine
  const {
    refineCore: { onFinish, formLoading },
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
    setValue,
  } = useForm<UserFormData>({
    refineCoreProps: {
      resource: "admin/users",
      action: userId ? "edit" : "create",
      id: userId,
      onMutationSuccess: () => {
        toast.success(userId ? "User updated successfully" : "User created successfully");
        onSuccess?.();
        onClose();
      },
      onMutationError: (error) => {
        if (error.statusCode === 409) {
          toast.error("Email or Supabase User ID already exists");
        } else {
          toast.error("Failed to save user");
        }
      },
    },
    resolver: zodResolver(userFormSchema),
  });

  // Rellenar formulario en modo EDIT
  useEffect(() => {
    if (userData?.data) {
      reset({
        email: userData.data.email,
        supabaseUserId: userData.data.supabaseUserId,
        primerNombre: userData.data.primerNombre,
        segundoNombre: userData.data.segundoNombre || "",
        primerApellido: userData.data.primerApellido,
        segundoApellido: userData.data.segundoApellido || "",
        plantaId: userData.data.plantaId,
        centroCosto: userData.data.centroCosto || "",
        fechaContrato: userData.data.fechaContrato || "",
        isActive: userData.data.isActive,
        roles: userData.data.roles || [],
      });
    }
  }, [userData, reset]);

  const plantas = plantasData?.data || [];
  const roles = rolesData?.data || [];
  const isEditMode = !!userId;
};
```

### 3.4. Estructura del Componente

```typescript
import React, { useEffect } from "react";
import { useForm } from "@refinedev/react-hook-form";
import { useOne, useList } from "@refinedev/core";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "react-hot-toast";

import { Modal } from "@/components/ui/Modal";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Checkbox } from "@/components/ui/Checkbox";

import { userFormSchema, type UserFormData } from "@/schemas/user.schema";
import type { UsuarioResponse, PlantaResponse, RoleResponse } from "@/types";

export const UserFormModal: React.FC<UserFormModalProps> = ({
  isOpen,
  onClose,
  userId,
  onSuccess,
}) => {
  // State management (ver sección 3.3)

  const onSubmit = async (data: UserFormData) => {
    await onFinish(data);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditMode ? "Edit User" : "Create User"}
      size="lg"
    >
      {isLoadingUser && isEditMode ? (
        <div className="p-8 text-center">
          <p className="text-gray-500 dark:text-gray-400">Loading user data...</p>
        </div>
      ) : (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          {/* Email */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Email *
            </label>
            <Input
              {...register("email")}
              type="email"
              placeholder="usuario@cambiaso.com"
              disabled={isEditMode}
              error={errors.email?.message}
            />
            {errors.email && (
              <p className="mt-1 text-sm text-error-600 dark:text-error-400">
                {errors.email.message}
              </p>
            )}
          </div>

          {/* Supabase User ID */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Supabase User ID *
            </label>
            <Input
              {...register("supabaseUserId")}
              type="text"
              placeholder="123e4567-e89b-12d3-a456-426614174000"
              disabled={isEditMode}
              error={errors.supabaseUserId?.message}
            />
            {errors.supabaseUserId && (
              <p className="mt-1 text-sm text-error-600 dark:text-error-400">
                {errors.supabaseUserId.message}
              </p>
            )}
          </div>

          {/* Nombres */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                First Name *
              </label>
              <Input
                {...register("primerNombre")}
                type="text"
                placeholder="Juan"
                error={errors.primerNombre?.message}
              />
              {errors.primerNombre && (
                <p className="mt-1 text-sm text-error-600 dark:text-error-400">
                  {errors.primerNombre.message}
                </p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Second Name
              </label>
              <Input
                {...register("segundoNombre")}
                type="text"
                placeholder="Carlos"
              />
            </div>
          </div>

          {/* Apellidos */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                First Lastname *
              </label>
              <Input
                {...register("primerApellido")}
                type="text"
                placeholder="Pérez"
                error={errors.primerApellido?.message}
              />
              {errors.primerApellido && (
                <p className="mt-1 text-sm text-error-600 dark:text-error-400">
                  {errors.primerApellido.message}
                </p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Second Lastname
              </label>
              <Input
                {...register("segundoApellido")}
                type="text"
                placeholder="García"
              />
            </div>
          </div>

          {/* Planta y Centro de Costo */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Planta
              </label>
              <Select
                {...register("plantaId", { valueAsNumber: true })}
                options={[
                  { value: "", label: "Sin planta asignada" },
                  ...plantas.map((p) => ({
                    value: p.id.toString(),
                    label: p.name,
                  })),
                ]}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Centro de Costo
              </label>
              <Input
                {...register("centroCosto")}
                type="text"
                placeholder="CC-001"
              />
            </div>
          </div>

          {/* Fecha de Contrato y Estado */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Fecha de Contrato
              </label>
              <Input
                {...register("fechaContrato")}
                type="date"
              />
            </div>

            <div className="flex items-center h-full pt-6">
              <Checkbox
                {...register("isActive")}
                label="Activo"
              />
            </div>
          </div>

          {/* Roles iniciales (solo en CREATE) */}
          {!isEditMode && (
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Roles Iniciales (opcional)
              </label>
              <div className="space-y-2 p-4 bg-gray-50 dark:bg-gray-900 rounded-lg">
                {roles.map((role) => (
                  <Checkbox
                    key={role.id}
                    {...register("roles")}
                    value={role.name}
                    label={role.name}
                  />
                ))}
              </div>
            </div>
          )}

          {/* Botones */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-200 dark:border-gray-700">
            <Button
              type="button"
              onClick={onClose}
              variant="outline"
              disabled={formLoading}
            >
              Cancelar
            </Button>
            <Button
              type="submit"
              variant="primary"
              disabled={formLoading}
              loading={formLoading}
            >
              {isEditMode ? "Actualizar Usuario" : "Crear Usuario"}
            </Button>
          </div>
        </form>
      )}
    </Modal>
  );
};
```

---

## 4. Validación y Tests

```typescript
describe("UserFormModal", () => {
  it("valida formato de email", async () => {
    const user = userEvent.setup();
    render(<UserFormModal isOpen={true} onClose={vi.fn()} />);
    
    await user.type(screen.getByLabelText(/email/i), "invalid-email");
    await user.click(screen.getByText("Create User"));
    
    expect(await screen.findByText("Invalid email format")).toBeInTheDocument();
  });

  it("valida formato UUID de Supabase", async () => {
    const user = userEvent.setup();
    render(<UserFormModal isOpen={true} onClose={vi.fn()} />);
    
    await user.type(screen.getByLabelText(/supabase user id/i), "not-a-uuid");
    await user.click(screen.getByText("Create User"));
    
    expect(await screen.findByText("Invalid UUID format")).toBeInTheDocument();
  });

  it("deshabilita email en modo EDIT", () => {
    render(<UserFormModal isOpen={true} onClose={vi.fn()} userId={1} />);
    
    const emailInput = screen.getByLabelText(/email/i);
    expect(emailInput).toBeDisabled();
  });
});
```

---

**Generado por**: FTV Generator v2.0  
**Tiempo estimado**: 5-6 horas  
**Dependencias**: UI components (Modal, Input, Select, Checkbox, Button)
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
Página de administración de permisos del sistema que permite crear, editar y eliminar permisos granulares.

### 1.2. Caso de Uso Principal
**Actor**: Administrador (ROLE_ADMIN)

**Flujo**:
1. Admin accede a `/admin/permissions`
2. Ve lista de permisos disponibles en el sistema
3. Puede crear nuevo permiso, editar existente, o eliminar
4. Sistema previene eliminar permisos asignados a roles

---

## 2. Especificación Visual

### 2.1. Wireframe ASCII

```
┌──────────────────────────────────────────────────────────────┐
│ IOC Platform - Permission Management             [Profile ▾] │
├──────────────────────────────────────────────────────────────┤
│                                                                │
│  Permissions                                  [+ New Permission]│
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                                │
│  ┌──────────────────────────────────┐         [🔄 Refresh]   │
│  │ 🔍 Search permissions...         │                         │
│  └──────────────────────────────────┘                         │
│                                                                │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Name              │ Description         │ Actions      │  │
│  ├────────────────────────────────────────────────────────┤  │
│  │ VIEW_DASHBOARD    │ Access dashboard    │ ✏️ 🗑️       │  │
│  │ MANAGE_USERS      │ CRUD users          │ ✏️ 🗑️       │  │
│  │ UPLOAD_FILES      │ Upload ETL files    │ ✏️ 🗑️       │  │
│  │ VIEW_REPORTS      │ View analytics      │ ✏️ 🗑️       │  │
│  │ MANAGE_ROLES      │ CRUD roles          │ ✏️ 🗑️       │  │
│  │ EXPORT_DATA       │ Export to CSV/Excel │ ✏️ 🗑️       │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                                │
│  Total: 6 permissions                                         │
│                                                                │
└──────────────────────────────────────────────────────────────┘
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
      "Permission name must be uppercase with underscores only"
    )
    .trim(),
  description: z
    .string()
    .max(500, "Description must not exceed 500 characters")
    .optional(),
});

export type PermissionFormData = z.infer<typeof permissionSchema>;
```

### 3.3. State Management

```typescript
import { useTable, useDelete } from "@refinedev/core";
import { useForm } from "@refinedev/react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

const {
  tableQueryResult,
  filters,
  setFilters,
} = useTable<PermissionResponse>({
  resource: "admin/permissions",
  pagination: { current: 1, pageSize: 50 },
  sorters: { initial: [{ field: "name", order: "asc" }] },
});

const { mutate: deletePermission, isLoading: isDeleting } = useDelete<PermissionResponse>();

const {
  refineCore: { onFinish, formLoading },
  register,
  handleSubmit,
  formState: { errors },
  reset,
} = useForm<PermissionFormData>({
  refineCoreProps: {
    resource: "admin/permissions",
    action: "create",
  },
  resolver: zodResolver(permissionSchema),
});

const [showCreateModal, setShowCreateModal] = useState(false);
const [showEditModal, setShowEditModal] = useState(false);
const [selectedPermissionId, setSelectedPermissionId] = useState<number | null>(null);
const [searchQuery, setSearchQuery] = useState("");
```

### 3.4. Handlers

```typescript
// Buscar permisos
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

// Crear permiso
const handleCreatePermission = () => {
  reset();
  setShowCreateModal(true);
};

// Eliminar permiso
const handleDeletePermission = (permissionId: number) => {
  if (confirm("Are you sure you want to delete this permission?")) {
    deletePermission(
      {
        resource: "admin/permissions",
        id: permissionId,
      },
      {
        onSuccess: () => {
          toast.success("Permission deleted successfully");
        },
        onError: (error) => {
          if (error.statusCode === 409) {
            toast.error("Permission is assigned to roles and cannot be deleted");
          } else {
            toast.error("Failed to delete permission");
          }
        },
      }
    );
  }
};
```

### 3.5. Estructura del Componente

```typescript
// src/pages/admin/permissions/list.tsx
import React, { useState, useMemo } from "react";
import { useTable, useDelete } from "@refinedev/core";
import { PlusIcon, RefreshIcon, KeyIcon } from "@heroicons/react/outline";
import { debounce } from "lodash-es";
import { toast } from "react-hot-toast";

import { EmptyState } from "@/components/common/EmptyState";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Modal } from "@/components/ui/Modal";

import type { PermissionResponse } from "@/types/permission.types";

export const PermissionManagementPage: React.FC = () => {
  const { tableQueryResult, filters, setFilters } = useTable<PermissionResponse>({
    resource: "admin/permissions",
    pagination: { current: 1, pageSize: 50 },
    sorters: { initial: [{ field: "name", order: "asc" }] },
  });

  const { mutate: deletePermission, isLoading: isDeleting } = useDelete<PermissionResponse>();

  const [searchQuery, setSearchQuery] = useState("");

  // Handlers

  const { data, isLoading, isError } = tableQueryResult;
  const permissions = data?.data || [];

  return (
    <div className="p-6 bg-gray-50 dark:bg-gray-900 min-h-screen">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-gray-800 dark:text-white">Permissions</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
            Manage granular system permissions
          </p>
        </div>
        <Button
          onClick={handleCreatePermission}
          variant="primary"
          leftIcon={<PlusIcon className="w-5 h-5" />}
        >
          New Permission
        </Button>
      </div>

      {/* Búsqueda */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm p-4 mb-6">
        <div className="flex items-center gap-4">
          <div className="flex-1">
            <Input
              type="search"
              placeholder="Search permissions..."
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                handleSearch(e.target.value);
              }}
              leftIcon={<SearchIcon className="w-5 h-5 text-gray-400" />}
            />
          </div>
          <Button
            onClick={() => tableQueryResult.refetch()}
            variant="ghost"
            leftIcon={<RefreshIcon className="w-5 h-5" />}
          >
            Refresh
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
          <p className="text-error-600 dark:text-error-400">Failed to load permissions.</p>
          <Button onClick={() => tableQueryResult.refetch()} variant="outline" className="mt-4">
            Retry
          </Button>
        </div>
      ) : permissions.length === 0 ? (
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
                <tr key={permission.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <KeyIcon className="w-5 h-5 text-brand-500 mr-2" />
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
                        title="Edit"
                      >
                        ✏️
                      </Button>
                      <Button
                        onClick={() => handleDeletePermission(permission.id)}
                        variant="ghost"
                        size="sm"
                        disabled={isDeleting}
                        title="Delete"
                      >
                        🗑️
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
              Total: {permissions.length} permission{permissions.length !== 1 ? "s" : ""}
            </p>
          </div>
        </div>
      )}
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

// POST /api/v1/admin/permissions
// Body: PermissionRequest
// Response: 201 Created + PermissionResponse

// PUT /api/v1/admin/permissions/{id}
// Body: PermissionRequest
// Response: 200 OK + PermissionResponse

// DELETE /api/v1/admin/permissions/{id}
// Response: 204 No Content (o 409 si está asignado)
```

---

## 5. Notas de Implementación

### 5.1. Convenciones de Nombres
- **Formato**: UPPERCASE_SNAKE_CASE
- **Ejemplos válidos**: `VIEW_DASHBOARD`, `MANAGE_USERS`, `EXPORT_DATA`
- **Prefijos comunes**: `VIEW_`, `MANAGE_`, `CREATE_`, `DELETE_`, `EXPORT_`

### 5.2. Permisos Seed Recomendados
```sql
-- Ya creados en migración SQL
VIEW_DASHBOARD
MANAGE_USERS
MANAGE_ROLES
MANAGE_PERMISSIONS
UPLOAD_FILES
VIEW_REPORTS
EXPORT_DATA
MANAGE_ETL
VIEW_ANALYTICS
MANAGE_PLANTAS
```

---

**Generado por**: FTV Generator v2.0  
**Tiempo estimado**: 3-4 horas  
**Dependencias**: FTV-009 (EmptyState)
