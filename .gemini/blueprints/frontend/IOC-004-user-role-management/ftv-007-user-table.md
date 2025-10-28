# FTV-IOC-004-007: User Table Component

**ID**: FTV-IOC-004-007  
**Componente**: `UserTable`  
**Tipo**: Reusable Component  
**Technical Design**: TD-IOC-004-User-Role-Management-claude.md
**Sprint**: Sprint 3 (IOC-004)  
**Fecha**: 2025-10-28  
**Estado**: ✅ Ready for Implementation

---

## 1. Propósito y Contexto

### 1.1. Propósito
Componente reutilizable de tabla para mostrar usuarios con columnas estándar, acciones inline y diseño responsivo adaptado al contexto chileno.

### 1.2. Características
- Tabla responsive con layout adaptativo (desktop/mobile)
- Muestra roles con badges coloridos
- Acciones contextuales (Edit, Manage Roles, Delete)
- Indicador de estado activo/inactivo
- Skeleton loader durante carga
- Empty state cuando no hay datos

---

## 2. Especificación Visual

### 2.1. Wireframe ASCII (Desktop)

```
┌─────────────────────────────────────────────────────────────────────┐
│ Nombre          │ Correo           │ Planta │ Roles      │ Estado │ Acciones│
├─────────────────────────────────────────────────────────────────────┤
│ Juan Pérez      │ juan@ioc.com     │ Lima   │ ADMIN      │ ● Activo│ ⚙️ 👥 🗑️ │
│ María García    │ maria@ioc.com    │ Callao │ GERENTE    │ ● Activo│ ⚙️ 👥 🗑️ │
│ Pedro López     │ pedro@ioc.com    │ Lima   │ ANALISTA   │ ○ Inactivo│ ⚙️ 👥 🗑️ │
│ Ana Rodríguez   │ ana@ioc.com      │ —      │ ANALISTA   │ ● Activo│ ⚙️ 👥 🗑️ │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2. Wireframe ASCII (Mobile - Cards)

```
┌────────────────────────────────────────┐
│ Juan Pérez                             │
│ juan@ioc.com                           │
│                                        │
│ 🏭 Lima    │ ADMIN    │ ● Activo      │
│                                        │
│ [Editar] [Gestionar Roles] [Eliminar] │
└────────────────────────────────────────┘

┌────────────────────────────────────────┐
│ María García                           │
│ maria@ioc.com                          │
│                                        │
│ 🏭 Callao  │ GERENTE  │ ● Activo      │
│                                        │
│ [Editar] [Gestionar Roles] [Eliminar] │
└────────────────────────────────────────┘
```

---

## 3. Especificación Técnica

### 3.1. Props Interface

```typescript
interface UserTableProps {
  users: UsuarioResponse[];
  onEdit: (userId: number) => void;
  onDelete: (userId: number) => void;
  onManageRoles: (userId: number) => void;
  isDeleting?: boolean;
  isLoading?: boolean;
  className?: string;
}
```

### 3.2. Types

```typescript
// src/types/user.types.ts
export interface UsuarioResponse {
  id: number;
  email: string;
  primerNombre: string;
  segundoNombre?: string;
  primerApellido: string;
  segundoApellido?: string;
  fullName: string;
  plantaId?: number;
  plantaName?: string;
  centroCosto?: string;
  fechaContrato?: string;
  isActive: boolean;
  roles: string[];
  supabaseUserId: string;
  lastLoginAt?: string;
  createdAt: string;
  updatedAt: string;
}
```

### 3.3. Estructura del Componente

```typescript
// src/components/users/UserTable.tsx
import React from "react";
import { PencilIcon, TrashIcon, UserGroupIcon } from "@heroicons/react/outline";
import { RoleBadge } from "@/components/roles/RoleBadge";
import { Button } from "@/components/ui/Button";
import type { UsuarioResponse } from "@/types/user.types";

interface UserTableProps {
  users: UsuarioResponse[];
  onEdit: (userId: number) => void;
  onDelete: (userId: number) => void;
  onManageRoles: (userId: number) => void;
  isDeleting?: boolean;
  isLoading?: boolean;
  className?: string;
}

export const UserTable: React.FC<UserTableProps> = ({
  users,
  onEdit,
  onDelete,
  onManageRoles,
  isDeleting = false,
  isLoading = false,
  className = "",
}) => {
  if (isLoading) {
    return (
      <div className={`bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm overflow-hidden ${className}`}>
        <div className="p-6 space-y-3">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="animate-pulse">
              <div className="h-16 bg-gray-200 dark:bg-gray-700 rounded"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <>
      {/* Vista Desktop - Tabla */}
      <div className={`hidden lg:block bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm overflow-hidden ${className}`}>
        <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead className="bg-gray-50 dark:bg-gray-900">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Nombre
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Correo
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Planta
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Roles
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Estado
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Acciones
              </th>
            </tr>
          </thead>
          <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
            {users.map((user) => (
              <tr
                key={user.id}
                className="hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              >
                {/* Name */}
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 h-10 w-10 bg-brand-100 dark:bg-brand-900 rounded-full flex items-center justify-center">
                      <span className="text-brand-600 dark:text-brand-300 font-medium text-sm">
                        {user.primerNombre[0]}{user.primerApellido[0]}
                      </span>
                    </div>
                    <div className="ml-4">
                      <div className="text-sm font-medium text-gray-900 dark:text-white">
                        {user.fullName}
                      </div>
                    </div>
                  </div>
                </td>

                {/* Email */}
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-gray-600 dark:text-gray-400">
                    {user.email}
                  </div>
                </td>

                {/* Plant */}
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-gray-900 dark:text-white">
                    {user.plantaName || "—"}
                  </div>
                </td>

                {/* Roles */}
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex flex-wrap gap-1">
                    {user.roles.length > 0 ? (
                      user.roles.map((role) => (
                        <RoleBadge key={role} role={role} size="sm" />
                      ))
                    ) : (
                      <span className="text-sm text-gray-400">Sin roles</span>
                    )}
                  </div>
                </td>

                {/* Status */}
                <td className="px-6 py-4 whitespace-nowrap">
                  <span
                    className={`
                      inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium
                      ${
                        user.isActive
                          ? "bg-success-100 text-success-800 dark:bg-success-900 dark:text-success-200"
                          : "bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300"
                      }
                    `}
                  >
                    <span className={`w-1.5 h-1.5 rounded-full ${user.isActive ? "bg-success-500" : "bg-gray-400"}`} />
                    {user.isActive ? "Activo" : "Inactivo"}
                  </span>
                </td>

                {/* Actions */}
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div className="flex items-center justify-end gap-2">
                    <Button
                      onClick={() => onEdit(user.id)}
                      variant="ghost"
                      size="sm"
                      title="Editar usuario"
                      aria-label={`Editar usuario ${user.fullName}`}
                    >
                      <PencilIcon className="w-4 h-4" />
                    </Button>
                    <Button
                      onClick={() => onManageRoles(user.id)}
                      variant="ghost"
                      size="sm"
                      title="Gestionar roles"
                      aria-label={`Gestionar roles de ${user.fullName}`}
                    >
                      <UserGroupIcon className="w-4 h-4" />
                    </Button>
                    <Button
                      onClick={() => onDelete(user.id)}
                      variant="ghost"
                      size="sm"
                      disabled={isDeleting}
                      title="Eliminar usuario"
                      aria-label={`Eliminar usuario ${user.fullName}`}
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
      </div>

      {/* Vista Mobile - Cards */}
      <div className="lg:hidden space-y-4">
        {users.map((user) => (
          <div
            key={user.id}
            className="bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm p-4 border border-gray-200 dark:border-gray-700"
          >
            {/* Header */}
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-center gap-3">
                <div className="flex-shrink-0 h-12 w-12 bg-brand-100 dark:bg-brand-900 rounded-full flex items-center justify-center">
                  <span className="text-brand-600 dark:text-brand-300 font-medium">
                    {user.primerNombre[0]}{user.primerApellido[0]}
                  </span>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-gray-900 dark:text-white">
                    {user.fullName}
                  </h3>
                  <p className="text-xs text-gray-600 dark:text-gray-400">
                    {user.email}
                  </p>
                </div>
              </div>
              <span
                className={`
                  inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium
                  ${
                    user.isActive
                      ? "bg-success-100 text-success-800 dark:bg-success-900 dark:text-success-200"
                      : "bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300"
                  }
                `}
              >
                <span className={`w-1.5 h-1.5 rounded-full ${user.isActive ? "bg-success-500" : "bg-gray-400"}`} />
                {user.isActive ? "Activo" : "Inactivo"}
              </span>
            </div>

            {/* Info */}
            <div className="flex flex-wrap items-center gap-3 mb-3 text-xs text-gray-600 dark:text-gray-400">
              {user.plantaName && (
                <div className="flex items-center gap-1">
                  <span>🏭</span>
                  <span>{user.plantaName}</span>
                </div>
              )}
              <div className="flex flex-wrap gap-1">
                {user.roles.length > 0 ? (
                  user.roles.map((role) => (
                    <RoleBadge key={role} role={role} size="sm" />
                  ))
                ) : (
                  <span className="text-gray-400">Sin roles</span>
                )}
              </div>
            </div>

            {/* Actions */}
            <div className="flex gap-2 pt-3 border-t border-gray-200 dark:border-gray-700">
              <Button
                onClick={() => onEdit(user.id)}
                variant="outline"
                size="sm"
                className="flex-1"
                leftIcon={<PencilIcon className="w-4 h-4" />}
              >
                Editar
              </Button>
              <Button
                onClick={() => onManageRoles(user.id)}
                variant="outline"
                size="sm"
                className="flex-1"
                leftIcon={<UserGroupIcon className="w-4 h-4" />}
              >
                Roles
              </Button>
              <Button
                onClick={() => onDelete(user.id)}
                variant="outline"
                size="sm"
                disabled={isDeleting}
                className="text-error-600 hover:text-error-700 dark:text-error-400 dark:hover:text-error-300"
              >
                <TrashIcon className="w-4 h-4" />
              </Button>
            </div>
          </div>
        ))}
      </div>
    </>
  );
};

export default React.memo(UserTable);
```

---

## 4. Estados Visuales

### 4.1. Loading State

```typescript
// Skeleton loader mostrado automáticamente cuando isLoading=true
<div className="p-6 space-y-3">
  {[...Array(5)].map((_, i) => (
    <div key={i} className="animate-pulse">
      <div className="h-16 bg-gray-200 dark:bg-gray-700 rounded"></div>
    </div>
  ))}
</div>
```

### 4.2. Empty State

```typescript
// Manejado por el componente padre (UserManagementPage)
{users.length === 0 && !isLoading && (
  <EmptyState
    icon={<UsersIcon className="w-16 h-16" />}
    title="No se encontraron usuarios"
    description="No hay usuarios que coincidan con tu búsqueda."
    action={<Button onClick={clearFilters}>Limpiar Filtros</Button>}
  />
)}
```

---

## 5. Responsive Behavior

### 5.1. Breakpoints

- **Desktop (≥1024px)**: Tabla completa con todas las columnas
- **Mobile (<1024px)**: Vista de cards con información condensada

### 5.2. Clases Tailwind Responsivas

```typescript
// Ocultar tabla en mobile, mostrar en desktop
className="hidden lg:block"

// Mostrar cards en mobile, ocultar en desktop
className="lg:hidden"
```

---

## 6. Validación y Tests

### 6.1. Unit Tests

```typescript
describe("UserTable", () => {
  const mockUsers: UsuarioResponse[] = [
    {
      id: 1,
      email: "juan@ioc.com",
      primerNombre: "Juan",
      primerApellido: "Pérez",
      fullName: "Juan Pérez",
      plantaName: "Lima",
      isActive: true,
      roles: ["ADMIN"],
      supabaseUserId: "uuid-1",
      createdAt: "2024-01-01",
      updatedAt: "2024-01-01",
    },
  ];

  it("renderiza usuarios correctamente en tabla desktop", () => {
    render(
      <UserTable
        users={mockUsers}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
        onManageRoles={jest.fn()}
      />
    );

    expect(screen.getByText("Juan Pérez")).toBeInTheDocument();
    expect(screen.getByText("juan@ioc.com")).toBeInTheDocument();
    expect(screen.getByText("Lima")).toBeInTheDocument();
  });

  it("muestra badge de rol correctamente", () => {
    render(
      <UserTable
        users={mockUsers}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
        onManageRoles={jest.fn()}
      />
    );

    expect(screen.getAllByText("Administrador")).toHaveLength(2); // Desktop + Mobile
  });

  it("muestra estado activo/inactivo correctamente", () => {
    render(
      <UserTable
        users={mockUsers}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
        onManageRoles={jest.fn()}
      />
    );

    expect(screen.getAllByText("Active")).toHaveLength(2);
  });

  it("llama onEdit cuando se hace click en botón editar", async () => {
    const onEdit = jest.fn();
    const user = userEvent.setup();

    render(
      <UserTable
        users={mockUsers}
        onEdit={onEdit}
        onDelete={jest.fn()}
        onManageRoles={jest.fn()}
      />
    );

    const editButtons = screen.getAllByLabelText(/Edit user Juan Pérez/i);
    await user.click(editButtons[0]);

    expect(onEdit).toHaveBeenCalledWith(1);
  });

  it("llama onManageRoles cuando se hace click en botón roles", async () => {
    const onManageRoles = jest.fn();
    const user = userEvent.setup();

    render(
      <UserTable
        users={mockUsers}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
        onManageRoles={onManageRoles}
      />
    );

    const roleButtons = screen.getAllByLabelText(/Manage roles for Juan Pérez/i);
    await user.click(roleButtons[0]);

    expect(onManageRoles).toHaveBeenCalledWith(1);
  });

  it("llama onDelete cuando se hace click en botón eliminar", async () => {
    const onDelete = jest.fn();
    const user = userEvent.setup();

    render(
      <UserTable
        users={mockUsers}
        onEdit={jest.fn()}
        onDelete={onDelete}
        onManageRoles={jest.fn()}
      />
    );

    const deleteButtons = screen.getAllByLabelText(/Delete user Juan Pérez/i);
    await user.click(deleteButtons[0]);

    expect(onDelete).toHaveBeenCalledWith(1);
  });

  it("deshabilita botón delete durante isDeleting", () => {
    render(
      <UserTable
        users={mockUsers}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
        onManageRoles={jest.fn()}
        isDeleting={true}
      />
    );

    const deleteButtons = screen.getAllByLabelText(/Delete user/i);
    deleteButtons.forEach((btn) => {
      expect(btn).toBeDisabled();
    });
  });

  it("muestra skeleton loader cuando isLoading=true", () => {
    const { container } = render(
      <UserTable
        users={[]}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
        onManageRoles={jest.fn()}
        isLoading={true}
      />
    );

    expect(container.querySelector(".animate-pulse")).toBeInTheDocument();
  });

  it("muestra '—' cuando no hay planta asignada", () => {
    const userWithoutPlant = { ...mockUsers[0], plantaName: undefined };

    render(
      <UserTable
        users={[userWithoutPlant]}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
        onManageRoles={jest.fn()}
      />
    );

    expect(screen.getByText("—")).toBeInTheDocument();
  });

  it("muestra 'No roles' cuando usuario no tiene roles", () => {
    const userWithoutRoles = { ...mockUsers[0], roles: [] };

    render(
      <UserTable
        users={[userWithoutRoles]}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
        onManageRoles={jest.fn()}
      />
    );

    expect(screen.getAllByText("No roles")).toHaveLength(2);
  });
});
```

---

## 7. Accesibilidad

### 7.1. ARIA Labels

```typescript
// Botones con labels descriptivos
<Button
  aria-label={`Edit user ${user.fullName}`}
  onClick={() => onEdit(user.id)}
>
  <PencilIcon />
</Button>

// Tabla con headers semánticos
<thead>
  <tr>
    <th scope="col">Name</th>
    <th scope="col">Email</th>
  </tr>
</thead>
```

### 7.2. Navegación por Teclado

- Todos los botones son accesibles vía teclado
- Focus visible en todos los elementos interactivos
- Tab order lógico

---

## 8. Notas de Implementación

### 8.1. Performance

- Componente memoizado con `React.memo`
- Props callbacks deben ser memoizados en el componente padre
- Lista renderizada eficientemente con keys únicas

### 8.2. Iniciales del Avatar

```typescript
// Generar iniciales a partir del nombre
{user.primerNombre[0]}{user.primerApellido[0]}
// Ejemplo: "Juan Pérez" → "JP"
```

### 8.3. Colores de Estado

```typescript
// Active: verde success
bg-success-100 text-success-800 dark:bg-success-900 dark:text-success-200

// Inactive: gris neutral
bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300
```

---

**Generado por**: FTV Generator v2.0  
**Tiempo estimado**: 4-5 horas  
**Dependencias**: FTV-008 (RoleBadge), Button UI, Icons (Heroicons)
