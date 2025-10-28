# FTV-IOC-004-006: Permission Assignment Modal

**ID**: FTV-IOC-004-006  
**Componente**: `PermissionAssignmentModal`  
**Tipo**: Modal Component  
**Sprint**: Sprint 3 (IOC-004)  
**Fecha**: 2025-10-28  
**Estado**: ✅ Ready for Implementation

---

## 1. Propósito y Contexto

### 1.1. Propósito
Modal para asignar y remover permisos de un rol específico de forma interactiva.

### 1.2. Flujo de Usuario
1. Admin selecciona "Manage Permissions" en un rol
2. Modal muestra permisos disponibles agrupados por categoría
3. Permisos ya asignados aparecen marcados
4. Admin marca/desmarca permisos
5. Cambios se aplican mediante API idempotente
6. Modal se cierra y datos se actualizan

---

## 2. Especificación Visual

### 2.1. Wireframe ASCII

```
┌──────────────────────────────────────────────────┐
│ [X] Manage Permissions - GERENTE                 │
├──────────────────────────────────────────────────┤
│                                                  │
│  Assign or remove permissions for this role:    │
│                                                  │
│  User Management                                 │
│  ┌────────────────────────────────────────────┐ │
│  │ ☑ VIEW_USERS                               │ │
│  │ ☐ MANAGE_USERS                             │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  Data & Analytics                                │
│  ┌────────────────────────────────────────────┐ │
│  │ ☑ VIEW_DASHBOARD                           │ │
│  │ ☑ VIEW_REPORTS                             │ │
│  │ ☐ EXPORT_DATA                              │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  ETL Operations                                  │
│  ┌────────────────────────────────────────────┐ │
│  │ ☑ UPLOAD_FILES                             │ │
│  │ ☐ MANAGE_ETL                               │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  [Close]                                         │
│                                                  │
└──────────────────────────────────────────────────┘
```

---

## 3. Especificación Técnica

### 3.1. Props Interface

```typescript
interface PermissionAssignmentModalProps {
  isOpen: boolean;
  onClose: () => void;
  roleId: number | null;
  onSuccess?: () => void;
}
```

### 3.2. Permission Categories

```typescript
// src/constants/permissions.ts
export const PERMISSION_CATEGORIES = {
  USER_MANAGEMENT: {
    label: "Gestión de Usuarios",
    permissions: ["VIEW_USERS", "MANAGE_USERS", "MANAGE_ROLES", "MANAGE_PERMISSIONS"],
  },
  DATA_ANALYTICS: {
    label: "Datos y Análisis",
    permissions: ["VIEW_DASHBOARD", "VIEW_REPORTS", "VIEW_ANALYTICS", "EXPORT_DATA"],
  },
  ETL_OPERATIONS: {
    label: "Operaciones ETL",
    permissions: ["UPLOAD_FILES", "MANAGE_ETL", "VIEW_ETL_LOGS"],
  },
  PLANT_MANAGEMENT: {
    label: "Gestión de Plantas",
    permissions: ["VIEW_PLANTAS", "MANAGE_PLANTAS"],
  },
} as const;
```

### 3.3. State Management

```typescript
import { useOne, useList, useCustomMutation } from "@refinedev/core";
import { useState, useEffect, useMemo } from "react";

const PermissionAssignmentModal: React.FC<PermissionAssignmentModalProps> = ({
  isOpen,
  onClose,
  roleId,
  onSuccess,
}) => {
  // Cargar datos del rol
  const { data: roleData, isLoading: isLoadingRole } = useOne<RoleResponse>({
    resource: "admin/roles",
    id: roleId || "",
    queryOptions: {
      enabled: !!roleId && isOpen,
    },
  });

  // Cargar todos los permisos disponibles
  const { data: permissionsData, isLoading: isLoadingPermissions } = useList<PermissionResponse>({
    resource: "admin/permissions",
    pagination: { current: 1, pageSize: 100 },
    queryOptions: {
      enabled: isOpen,
    },
  });

  // Mutations
  const { mutate: assignPermission, isLoading: isAssigning } = useCustomMutation();
  const { mutate: removePermission, isLoading: isRemoving } = useCustomMutation();

  // Estado local de permisos asignados
  const [assignedPermissions, setAssignedPermissions] = useState<string[]>([]);

  // Inicializar permisos asignados
  useEffect(() => {
    if (roleData?.data?.permissions) {
      setAssignedPermissions(roleData.data.permissions);
    }
  }, [roleData]);

  // Agrupar permisos por categoría
  const groupedPermissions = useMemo(() => {
    const allPermissions = permissionsData?.data || [];
    const grouped: Record<string, PermissionResponse[]> = {};

    Object.entries(PERMISSION_CATEGORIES).forEach(([key, category]) => {
      grouped[key] = allPermissions.filter((p) => category.permissions.includes(p.name));
    });

    // Permisos sin categoría
    const categorizedNames = Object.values(PERMISSION_CATEGORIES).flatMap((c) => c.permissions);
    grouped.OTHERS = allPermissions.filter((p) => !categorizedNames.includes(p.name));

    return grouped;
  }, [permissionsData]);

  const role = roleData?.data;
  const isLoading = isLoadingRole || isLoadingPermissions;
  const isMutating = isAssigning || isRemoving;
};
```

### 3.4. Handlers

```typescript
const handleTogglePermission = async (permissionName: string) => {
  if (!roleId) return;

  const allPermissions = permissionsData?.data || [];
  const permission = allPermissions.find((p) => p.name === permissionName);
  if (!permission) return;

  const isCurrentlyAssigned = assignedPermissions.includes(permissionName);

  if (isCurrentlyAssigned) {
    // Remover permiso
    removePermission(
      {
        url: `/api/v1/admin/assignments/roles/${roleId}/permissions/${permission.id}`,
        method: "delete",
        values: {},
      },
      {
        onSuccess: () => {
          setAssignedPermissions((prev) => prev.filter((p) => p !== permissionName));
          toast.success(`Permission ${permissionName} removed`);
          onSuccess?.();
        },
        onError: () => {
          toast.error(`Failed to remove permission ${permissionName}`);
        },
      }
    );
  } else {
    // Asignar permiso
    assignPermission(
      {
        url: `/api/v1/admin/assignments/roles/${roleId}/permissions/${permission.id}`,
        method: "post",
        values: {},
      },
      {
        onSuccess: () => {
          setAssignedPermissions((prev) => [...prev, permissionName]);
          toast.success(`Permission ${permissionName} assigned`);
          onSuccess?.();
        },
        onError: () => {
          toast.error(`Failed to assign permission ${permissionName}`);
        },
      }
    );
  }
};

const handleClose = () => {
  setAssignedPermissions([]);
  onClose();
};
```

### 3.5. Estructura del Componente

```typescript
import React, { useState, useEffect, useMemo } from "react";
import { useOne, useList, useCustomMutation } from "@refinedev/core";
import { toast } from "react-hot-toast";
import { KeyIcon } from "@heroicons/react/outline";

import { Modal } from "@/components/ui/Modal";
import { Button } from "@/components/ui/Button";
import { Checkbox } from "@/components/ui/Checkbox";
import { PERMISSION_CATEGORIES } from "@/constants/permissions";

import type { RoleResponse, PermissionResponse } from "@/types";

export const PermissionAssignmentModal: React.FC<PermissionAssignmentModalProps> = ({
  isOpen,
  onClose,
  roleId,
  onSuccess,
}) => {
  // State management (ver sección 3.3)
  // Handlers (ver sección 3.4)

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title={role ? `Manage Permissions - ${role.name}` : "Manage Permissions"}
      size="lg"
    >
      {isLoading ? (
        <div className="p-8 text-center">
          <p className="text-gray-500 dark:text-gray-400">Loading permissions...</p>
        </div>
      ) : (
        <div className="space-y-6">
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Asigne o elimine permisos para este rol:
          </p>

          {/* Permisos agrupados por categoría */}
          <div className="space-y-6 max-h-[500px] overflow-y-auto pr-2">
            {Object.entries(PERMISSION_CATEGORIES).map(([key, category]) => {
              const categoryPermissions = groupedPermissions[key] || [];
              if (categoryPermissions.length === 0) return null;

              return (
                <div key={key}>
                  <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">
                    Gestión de Usuarios
                  </h3>
                  <div className="space-y-2">
                    {categoryPermissions.map((permission) => {
                      const isAssigned = assignedPermissions.includes(permission.name);

                      return (
                        <div
                          key={permission.id}
                          className={`
                            p-3 rounded-lg border cursor-pointer transition-all
                            ${
                              isAssigned
                                ? "border-brand-500 bg-brand-50 dark:bg-brand-900/20"
                                : "border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600"
                            }
                          `}
                          onClick={() => !isMutating && handleTogglePermission(permission.name)}
                        >
                          <div className="flex items-start gap-3">
                            <Checkbox
                              checked={isAssigned}
                              onChange={() => handleTogglePermission(permission.name)}
                              disabled={isMutating}
                            />
                            <div className="flex-1">
                              <div className="flex items-center gap-2">
                                <KeyIcon className="w-4 h-4 text-gray-400" />
                                <span className="text-sm font-mono font-medium text-gray-900 dark:text-white">
                                  {permission.name}
                                </span>
                              </div>
                              {permission.description && (
                                <p className="text-xs text-gray-600 dark:text-gray-400 mt-1 ml-6">
                                  {permission.description}
                                </p>
                              )}
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              );
            })}

            {/* Permisos sin categoría */}
            {groupedPermissions.OTHERS && groupedPermissions.OTHERS.length > 0 && (
              <div>
                <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">
                  Otros Permisos
                </h3>
                <div className="space-y-2">
                  {groupedPermissions.OTHERS.map((permission) => {
                    const isAssigned = assignedPermissions.includes(permission.name);

                    return (
                      <div
                        key={permission.id}
                        className={`
                          p-3 rounded-lg border cursor-pointer transition-all
                          ${
                            isAssigned
                              ? "border-brand-500 bg-brand-50 dark:bg-brand-900/20"
                              : "border-gray-200 dark:border-gray-700"
                          }
                        `}
                        onClick={() => !isMutating && handleTogglePermission(permission.name)}
                      >
                        <div className="flex items-start gap-3">
                          <Checkbox
                            checked={isAssigned}
                            onChange={() => handleTogglePermission(permission.name)}
                            disabled={isMutating}
                          />
                          <span className="text-sm font-mono font-medium text-gray-900 dark:text-white">
                            {permission.name}
                          </span>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}
          </div>

          {/* Info */}
          <div className="p-3 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg">
            <p className="text-xs text-blue-800 dark:text-blue-200">
              💡 Los cambios se aplican inmediatamente. Todos los usuarios con este rol serán afectados.
            </p>
          </div>

          {/* Botones */}
          <div className="flex justify-end pt-4 border-t border-gray-200 dark:border-gray-700">
            <Button onClick={handleClose} variant="primary">
              Cerrar
            </Button>
          </div>
        </div>
      )}
    </Modal>
  );
};
```

---

## 4. Integración con API

### 4.1. Endpoints Utilizados

```typescript
// POST /api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}
// Response: 200 OK (idempotente)

// DELETE /api/v1/admin/assignments/roles/{roleId}/permissions/{permissionId}
// Response: 200 OK (idempotente)
```

---

## 5. Notas de Implementación

### 5.1. Agrupación de Permisos
- Permisos agrupados por categoría mejoran UX
- Categorías definidas en constantes reutilizables
- Permisos sin categoría agrupados en "Others"

### 5.2. Performance
- Max height con scroll para evitar modal muy largo
- Optimistic updates en estado local
- Debounce opcional si hay muchos permisos (>50)

---

**Generado por**: FTV Generator v2.0  
**Tiempo estimado**: 4-5 horas  
**Dependencias**: UI components
# FTV-IOC-004-005: Role Assignment Modal

**ID**: FTV-IOC-004-005  
**Componente**: `RoleAssignmentModal`  
**Tipo**: Modal Component  
**Sprint**: Sprint 3 (IOC-004)  
**Fecha**: 2025-10-28  
**Estado**: ✅ Ready for Implementation

---

## 1. Propósito y Contexto

### 1.1. Propósito
Modal para asignar y remover roles de un usuario específico de forma interactiva.

### 1.2. Flujo de Usuario
1. Admin selecciona "Manage Roles" en un usuario
2. Modal muestra roles disponibles con checkboxes
3. Roles ya asignados aparecen marcados
4. Admin marca/desmarca roles
5. Cambios se aplican mediante API idempotente
6. Modal se cierra y tabla se actualiza

---

## 2. Especificación Visual

### 2.1. Wireframe ASCII

```
┌────────────────────────────────────────────────┐
│ [X] Manage Roles - Juan Pérez                  │
├────────────────────────────────────────────────┤
│                                                │
│  Assign or remove roles for this user:        │
│                                                │
│  ┌──────────────────────────────────────────┐ │
│  │ ☑ ADMIN                                  │ │
│  │   Full system access                     │ │
│  ├──────────────────────────────────────────┤ │
│  │ ☑ GERENTE                                │ │
│  │   Plant manager access                   │ │
│  ├──────────────────────────────────────────┤ │
│  │ ☐ ANALISTA                               │ │
│  │   Data analyst access                    │ │
│  └──────────────────────────────────────────┘ │
│                                                │
│  Changes are applied immediately              │
│                                                │
│  [Close]                                       │
│                                                │
└────────────────────────────────────────────────┘
```

---

## 3. Especificación Técnica

### 3.1. Props Interface

```typescript
interface RoleAssignmentModalProps {
  isOpen: boolean;
  onClose: () => void;
  userId: number | null;
  onSuccess?: () => void;
}
```

### 3.2. State Management

```typescript
import { useOne, useList, useCustomMutation } from "@refinedev/core";
import { useState, useEffect } from "react";

const RoleAssignmentModal: React.FC<RoleAssignmentModalProps> = ({
  isOpen,
  onClose,
  userId,
  onSuccess,
}) => {
  // Cargar datos del usuario
  const { data: userData, isLoading: isLoadingUser } = useOne<UsuarioResponse>({
    resource: "admin/users",
    id: userId || "",
    queryOptions: {
      enabled: !!userId && isOpen,
    },
  });

  // Cargar todos los roles disponibles
  const { data: rolesData, isLoading: isLoadingRoles } = useList<RoleResponse>({
    resource: "admin/roles",
    pagination: { current: 1, pageSize: 50 },
    queryOptions: {
      enabled: isOpen,
    },
  });

  // Mutation para asignar rol
  const { mutate: assignRole, isLoading: isAssigning } = useCustomMutation();

  // Mutation para remover rol
  const { mutate: removeRole, isLoading: isRemoving } = useCustomMutation();

  // Estado local de roles asignados
  const [assignedRoles, setAssignedRoles] = useState<string[]>([]);

  // Inicializar roles asignados cuando carga el usuario
  useEffect(() => {
    if (userData?.data?.roles) {
      setAssignedRoles(userData.data.roles);
    }
  }, [userData]);

  const allRoles = rolesData?.data || [];
  const user = userData?.data;
  const isLoading = isLoadingUser || isLoadingRoles;
  const isMutating = isAssigning || isRemoving;
};
```

### 3.3. Handlers

```typescript
// Toggle de rol (asignar o remover)
const handleToggleRole = async (roleName: string) => {
  if (!userId) return;

  const isCurrentlyAssigned = assignedRoles.includes(roleName);
  
  // Encontrar el ID del rol por nombre
  const role = allRoles.find((r) => r.name === roleName);
  if (!role) return;

  if (isCurrentlyAssigned) {
    // Remover rol
    removeRole(
      {
        url: `/api/v1/admin/assignments/users/${userId}/roles/${role.id}`,
        method: "delete",
        values: {},
      },
      {
        onSuccess: () => {
          setAssignedRoles((prev) => prev.filter((r) => r !== roleName));
          toast.success(`Role ${roleName} removed`);
          onSuccess?.();
        },
        onError: () => {
          toast.error(`Failed to remove role ${roleName}`);
        },
      }
    );
  } else {
    // Asignar rol
    assignRole(
      {
        url: `/api/v1/admin/assignments/users/${userId}/roles/${role.id}`,
        method: "post",
        values: {},
      },
      {
        onSuccess: () => {
          setAssignedRoles((prev) => [...prev, roleName]);
          toast.success(`Role ${roleName} assigned`);
          onSuccess?.();
        },
        onError: () => {
          toast.error(`Failed to assign role ${roleName}`);
        },
      }
    );
  }
};

const handleClose = () => {
  setAssignedRoles([]);
  onClose();
};
```

### 3.4. Estructura del Componente

```typescript
import React, { useState, useEffect } from "react";
import { useOne, useList, useCustomMutation } from "@refinedev/core";
import { toast } from "react-hot-toast";

import { Modal } from "@/components/ui/Modal";
import { Button } from "@/components/ui/Button";
import { Checkbox } from "@/components/ui/Checkbox";
import { RoleBadge } from "@/components/roles/RoleBadge";

import type { UsuarioResponse, RoleResponse } from "@/types";

export const RoleAssignmentModal: React.FC<RoleAssignmentModalProps> = ({
  isOpen,
  onClose,
  userId,
  onSuccess,
}) => {
  // State management (ver sección 3.2)
  // Handlers (ver sección 3.3)

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title={user ? `Manage Roles - ${user.fullName}` : "Manage Roles"}
      size="md"
    >
      {isLoading ? (
        <div className="p-8 text-center">
          <p className="text-gray-500 dark:text-gray-400">Loading roles...</p>
        </div>
      ) : (
        <div className="space-y-4">
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Assign or remove roles for this user:
          </p>

          {/* Lista de roles */}
          <div className="space-y-2 max-h-96 overflow-y-auto">
            {allRoles.map((role) => {
              const isAssigned = assignedRoles.includes(role.name);

              return (
                <div
                  key={role.id}
                  className={`
                    p-4 rounded-lg border-2 cursor-pointer transition-all
                    ${
                      isAssigned
                        ? "border-brand-500 bg-brand-50 dark:bg-brand-900/20"
                        : "border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600"
                    }
                  `}
                  onClick={() => !isMutating && handleToggleRole(role.name)}
                >
                  <div className="flex items-start gap-3">
                    <Checkbox
                      checked={isAssigned}
                      onChange={() => handleToggleRole(role.name)}
                      disabled={isMutating}
                    />
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <RoleBadge name={role.name} />
                        {isAssigned && (
                          <span className="text-xs text-brand-600 dark:text-brand-400 font-medium">
                            Assigned
                          </span>
                        )}
                      </div>
                      {role.description && (
                        <p className="text-sm text-gray-600 dark:text-gray-400">
                          {role.description}
                        </p>
                      )}
                      {role.permissions.length > 0 && (
                        <p className="text-xs text-gray-500 dark:text-gray-500 mt-1">
                          {role.permissions.length} permission
                          {role.permissions.length !== 1 ? "s" : ""}
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Info */}
          <div className="p-3 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg">
            <p className="text-xs text-blue-800 dark:text-blue-200">
              💡 Changes are applied immediately. The user's permissions will update in real-time.
            </p>
          </div>

          {/* Botones */}
          <div className="flex justify-end pt-4 border-t border-gray-200 dark:border-gray-700">
            <Button onClick={handleClose} variant="primary">
              Close
            </Button>
          </div>
        </div>
      )}
    </Modal>
  );
};
```

---

## 4. Integración con API

### 4.1. Endpoints Utilizados

```typescript
// POST /api/v1/admin/assignments/users/{userId}/roles/{roleId}
// Response: 200 OK (idempotente)

// DELETE /api/v1/admin/assignments/users/{userId}/roles/{roleId}
// Response: 200 OK (idempotente)
```

### 4.2. Notas de Implementación
- Las operaciones son **idempotentes**: asignar un rol ya asignado no genera error
- Los cambios se aplican inmediatamente (no hay botón "Save")
- Optimistic UI: actualizar estado local antes de la respuesta del servidor
- Refrescar datos del usuario al cerrar el modal

---

## 5. Validación y Tests

```typescript
describe("RoleAssignmentModal", () => {
  it("muestra roles ya asignados como checked", async () => {
    const mockUser = {
      id: 1,
      fullName: "Juan Pérez",
      roles: ["ADMIN", "GERENTE"],
    };

    render(<RoleAssignmentModal isOpen={true} onClose={vi.fn()} userId={1} />);

    await waitFor(() => {
      expect(screen.getByLabelText("ADMIN")).toBeChecked();
      expect(screen.getByLabelText("GERENTE")).toBeChecked();
      expect(screen.getByLabelText("ANALISTA")).not.toBeChecked();
    });
  });

  it("asigna rol al hacer click", async () => {
    const user = userEvent.setup();
    render(<RoleAssignmentModal isOpen={true} onClose={vi.fn()} userId={1} />);

    await waitFor(() => screen.getByLabelText("ANALISTA"));
    await user.click(screen.getByLabelText("ANALISTA"));

    expect(mockAssignRole).toHaveBeenCalledWith(
      expect.objectContaining({
        url: "/api/v1/admin/assignments/users/1/roles/3",
        method: "post",
      })
    );
  });

  it("remueve rol al desmarcar", async () => {
    const user = userEvent.setup();
    render(<RoleAssignmentModal isOpen={true} onClose={vi.fn()} userId={1} />);

    await waitFor(() => screen.getByLabelText("ADMIN"));
    await user.click(screen.getByLabelText("ADMIN"));

    expect(mockRemoveRole).toHaveBeenCalledWith(
      expect.objectContaining({
        url: "/api/v1/admin/assignments/users/1/roles/1",
        method: "delete",
      })
    );
  });
});
```

---

## 6. Accesibilidad

```tsx
// Atributos ARIA requeridos
<div role="dialog" aria-labelledby="modal-title" aria-describedby="modal-description">
  <h2 id="modal-title">Manage Roles - {user.fullName}</h2>
  <p id="modal-description">Assign or remove roles for this user</p>

  <div role="group" aria-label="Available roles">
    {roles.map((role) => (
      <Checkbox
        key={role.id}
        aria-label={`${role.name}: ${role.description}`}
        checked={assignedRoles.includes(role.name)}
      />
    ))}
  </div>
</div>
```

---

**Generado por**: FTV Generator v2.0  
**Tiempo estimado**: 4-5 horas  
**Dependencias**: FTV-008 (RoleBadge), UI components
