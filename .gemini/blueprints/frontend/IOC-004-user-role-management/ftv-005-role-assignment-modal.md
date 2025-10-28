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
Modal para asignar y remover roles de un usuario específico mediante checkboxes interactivos.

### 1.2. Flujo de Usuario
1. Admin selecciona "Manage Roles" en un usuario
2. Modal muestra todos los roles disponibles
3. Roles ya asignados aparecen marcados
4. Admin marca/desmarca roles
5. Cambios se aplican mediante API idempotente
6. Modal se cierra y datos se actualizan

---

## 2. Especificación Visual

### 2.1. Wireframe ASCII

```
┌──────────────────────────────────────────────────┐
│ [X] Manage Roles - Juan Pérez                    │
├──────────────────────────────────────────────────┤
│                                                  │
│  Assign or remove roles for this user:          │
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │ ☑ ADMIN                                    │ │
│  │   Full system administrator access         │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │ ☑ GERENTE                                  │ │
│  │   Manager with reporting capabilities      │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │ ☐ ANALISTA                                 │ │
│  │   Analyst with view-only access            │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  💡 Changes are applied immediately.            │
│                                                  │
│  [Close]                                         │
│                                                  │
└──────────────────────────────────────────────────┘
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

### 3.2. Types

```typescript
// src/types/role.types.ts
export interface RoleResponse {
  id: number;
  name: string;
  description?: string;
  userCount?: number;
  permissions?: string[];
}
```

### 3.3. State Management

```typescript
import { useOne, useList, useCustomMutation } from "@refinedev/core";
import { useState, useEffect } from "react";
import { toast } from "react-hot-toast";

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

  // Mutations para asignar/remover roles
  const { mutate: assignRole, isLoading: isAssigning } = useCustomMutation();
  const { mutate: removeRole, isLoading: isRemoving } = useCustomMutation();

  // Estado local de roles asignados
  const [assignedRoles, setAssignedRoles] = useState<string[]>([]);

  // Inicializar roles asignados cuando se carga el usuario
  useEffect(() => {
    if (userData?.data?.roles) {
      setAssignedRoles(userData.data.roles);
    }
  }, [userData]);

  const user = userData?.data;
  const allRoles = rolesData?.data || [];
  const isLoading = isLoadingUser || isLoadingRoles;
  const isMutating = isAssigning || isRemoving;
};
```

### 3.4. Handlers

```typescript
const handleToggleRole = async (roleName: string) => {
  if (!userId) return;

  const role = allRoles.find((r) => r.name === roleName);
  if (!role) return;

  const isCurrentlyAssigned = assignedRoles.includes(roleName);

  if (isCurrentlyAssigned) {
    // Remover rol
    removeRole(
      {
        url: `/api/v1/admin/users/${userId}/roles/${role.id}`,
        method: "delete",
        values: {},
      },
      {
        onSuccess: () => {
          setAssignedRoles((prev) => prev.filter((r) => r !== roleName));
          toast.success(`Role ${roleName} removed successfully`);
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
        url: `/api/v1/admin/users/${userId}/roles/${role.id}`,
        method: "post",
        values: {},
      },
      {
        onSuccess: () => {
          setAssignedRoles((prev) => [...prev, roleName]);
          toast.success(`Role ${roleName} assigned successfully`);
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

### 3.5. Estructura del Componente

```typescript
// src/components/users/RoleAssignmentModal.tsx
import React, { useState, useEffect } from "react";
import { useOne, useList, useCustomMutation } from "@refinedev/core";
import { toast } from "react-hot-toast";
import { ShieldCheckIcon } from "@heroicons/react/outline";

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
  // State management (ver sección 3.3)
  // Handlers (ver sección 3.4)

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title={user ? `Manage Roles - ${user.primerNombre} ${user.primerApellido}` : "Manage Roles"}
      size="md"
    >
      {isLoading ? (
        <div className="p-8 text-center">
          <p className="text-gray-500 dark:text-gray-400">Loading roles...</p>
        </div>
      ) : (
        <div className="space-y-6">
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Asigne o elimine roles para este usuario:
          </p>

          {/* Lista de roles */}
          <div className="space-y-3 max-h-[400px] overflow-y-auto pr-2">
            {allRoles.length === 0 ? (
              <div className="text-center py-8">
                <ShieldCheckIcon className="w-12 h-12 text-gray-400 mx-auto mb-3" />
                <p className="text-gray-500 dark:text-gray-400">No hay roles disponibles</p>
              </div>
            ) : (
              allRoles.map((role) => {
                const isAssigned = assignedRoles.includes(role.name);

                return (
                  <div
                    key={role.id}
                    className={`
                      p-4 rounded-lg border cursor-pointer transition-all
                      ${
                        isAssigned
                          ? "border-brand-500 bg-brand-50 dark:bg-brand-900/20"
                          : "border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600"
                      }
                      ${isMutating ? "opacity-50 cursor-not-allowed" : ""}
                    `}
                    onClick={() => !isMutating && handleToggleRole(role.name)}
                  >
                    <div className="flex items-start gap-3">
                      <Checkbox
                        checked={isAssigned}
                        onChange={() => handleToggleRole(role.name)}
                        disabled={isMutating}
                        className="mt-0.5"
                      />
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-1">
                          <RoleBadge role={role.name} size="sm" />
                        </div>
                        {role.description && (
                          <p className="text-xs text-gray-600 dark:text-gray-400 mt-1">
                            {role.description}
                          </p>
                        )}
                        {isAssigned && (
                          <span className="text-xs text-brand-600 dark:text-brand-400 font-medium">
                            Asignado
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })
            )}
          </div>

          {/* Info */}
          <div className="p-3 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg">
            <p className="text-xs text-blue-800 dark:text-blue-200">
              💡 Los cambios se aplican inmediatamente. Los permisos del usuario se actualizarán en tiempo real.
            </p>
          </div>

          {/* Botones */}
          <div className="flex justify-end pt-4 border-t border-gray-200 dark:border-gray-700">
            <Button onClick={handleClose} variant="primary" disabled={isMutating}>
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
// POST /api/v1/admin/users/{userId}/roles/{roleId}
// Response: 200 OK + { roles: string[] }
// Idempotente: asignar el mismo rol múltiples veces no genera error

// DELETE /api/v1/admin/users/{userId}/roles/{roleId}
// Response: 204 No Content
// Idempotente: remover un rol no asignado no genera error
```

---

## 5. Validación y Tests

### 5.1. Unit Tests

```typescript
describe("RoleAssignmentModal", () => {
  it("renderiza correctamente con usuario cargado", async () => {
    const mockUser = {
      id: 1,
      primerNombre: "Juan",
      primerApellido: "Pérez",
      roles: ["ADMIN"],
    };

    render(
      <RoleAssignmentModal
        isOpen={true}
        onClose={jest.fn()}
        userId={1}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/Manage Roles - Juan Pérez/i)).toBeInTheDocument();
    });
  });

  it("marca roles ya asignados al usuario", async () => {
    render(
      <RoleAssignmentModal
        isOpen={true}
        onClose={jest.fn()}
        userId={1}
      />
    );

    await waitFor(() => {
      const adminCheckbox = screen.getByLabelText(/ADMIN/i);
      expect(adminCheckbox).toBeChecked();
    });
  });

  it("asigna rol cuando se hace click en checkbox no marcado", async () => {
    const user = userEvent.setup();
    const onSuccess = jest.fn();

    render(
      <RoleAssignmentModal
        isOpen={true}
        onClose={jest.fn()}
        userId={1}
        onSuccess={onSuccess}
      />
    );

    await waitFor(() => screen.getByText(/GERENTE/i));
    await user.click(screen.getByLabelText(/GERENTE/i));

    await waitFor(() => {
      expect(onSuccess).toHaveBeenCalled();
      expect(toast.success).toHaveBeenCalledWith(
        expect.stringContaining("GERENTE assigned")
      );
    });
  });

  it("remueve rol cuando se hace click en checkbox marcado", async () => {
    const user = userEvent.setup();

    render(
      <RoleAssignmentModal
        isOpen={true}
        onClose={jest.fn()}
        userId={1}
      />
    );

    await waitFor(() => screen.getByLabelText(/ADMIN/i));
    await user.click(screen.getByLabelText(/ADMIN/i));

    await waitFor(() => {
      expect(toast.success).toHaveBeenCalledWith(
        expect.stringContaining("ADMIN removed")
      );
    });
  });

  it("muestra estado de loading mientras carga datos", () => {
    render(
      <RoleAssignmentModal
        isOpen={true}
        onClose={jest.fn()}
        userId={1}
      />
    );

    expect(screen.getByText(/Loading roles/i)).toBeInTheDocument();
  });

  it("deshabilita checkboxes durante mutación", async () => {
    const user = userEvent.setup();

    render(
      <RoleAssignmentModal
        isOpen={true}
        onClose={jest.fn()}
        userId={1}
      />
    );

    await waitFor(() => screen.getByLabelText(/GERENTE/i));
    
    const checkbox = screen.getByLabelText(/GERENTE/i);
    await user.click(checkbox);

    expect(checkbox).toBeDisabled();
  });
});
```

---

## 6. Notas de Implementación

### 6.1. Idempotencia
- Las APIs POST y DELETE son idempotentes
- No es necesario validar antes de asignar/remover
- Errores se manejan mediante toast

### 6.2. Actualización en Tiempo Real
- El estado local `assignedRoles` se actualiza optimistically
- `onSuccess` callback permite refrescar la tabla padre
- Toast feedback inmediato al usuario

### 6.3. UX Considerations
- Click en toda la card toggle el checkbox
- Feedback visual claro (borde brand en seleccionados)
- Loading state mientras muta
- Info box con explicación de comportamiento

---

## 7. Accesibilidad

```typescript
// Labels claros en checkboxes
<Checkbox
  id={`role-${role.id}`}
  aria-label={`Assign role ${role.name}`}
  checked={isAssigned}
/>

// Modal con título descriptivo
<Modal
  title="Manage Roles - Juan Pérez"
  aria-describedby="role-assignment-description"
>
  <p id="role-assignment-description">
    Assign or remove roles for this user
  </p>
</Modal>
```

---

**Generado por**: FTV Generator v2.0  
**Tiempo estimado**: 3-4 horas  
**Dependencias**: FTV-008 (RoleBadge), Modal UI, Checkbox UI
