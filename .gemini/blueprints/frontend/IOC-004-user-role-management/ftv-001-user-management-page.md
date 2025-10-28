# FTV-IOC-004-001: User Management Page

**ID**: FTV-IOC-004-001  
**Componente**: `UserManagementPage`  
**Tipo**: Page Component  
**Ruta**: `/admin/users`  
**Sprint**: Sprint 3 (IOC-004)  
**Fecha**: 2025-10-27  
**Estado**: ✅ Ready for Implementation

---

## 1. Propósito y Contexto

### 1.1. Propósito
Página principal de administración de usuarios que permite a los administradores ver, crear, editar, eliminar y gestionar roles de usuarios del sistema IOC.

### 1.2. Caso de Uso Principal
**Actor**: Administrador (ROLE_ADMIN)

**Flujo**:
1. Admin accede a `/admin/users`
2. Ve lista paginada de usuarios con filtros de búsqueda
3. Puede crear nuevo usuario, editar existente, o eliminar (soft delete)
4. Puede asignar/remover roles a usuarios mediante modal dedicado
5. Puede filtrar por planta, estado activo, y buscar por nombre/email

### 1.3. Ubicación en la App
```
Dashboard
└── Admin Section
    └── User Management ← ESTA PÁGINA
        ├── Role Management
        └── Permission Management
```

**Restricción de Acceso**: Solo usuarios con `ROLE_ADMIN`

---

## 2. Especificación Visual

### 2.1. Wireframe ASCII

```
┌─────────────────────────────────────────────────────────────────────┐
│ IOC Platform - User Management                          [Profile ▾] │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  Users                                                                │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                                       │
│  ┌─────────────────────────────────────────────┐  [+ New User]     │
│  │ 🔍 Search by name or email...               │                    │
│  └─────────────────────────────────────────────┘                    │
│                                                                       │
│  Filters:                                                            │
│  [All Plants ▾]  [All Status ▾]                [🔄 Refresh]         │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │ Name          │ Email          │ Plant  │ Roles      │ Actions│ │
│  ├───────────────────────────────────────────────────────────────┤ │
│  │ Juan Pérez    │ juan@ioc.com   │ Lima   │ ADMIN      │ ⚙️ 🗑️  │ │
│  │ María García  │ maria@ioc.com  │ Callao │ GERENTE    │ ⚙️ 🗑️  │ │
│  │ Pedro López   │ pedro@ioc.com  │ Lima   │ ANALISTA   │ ⚙️ 🗑️  │ │
│  │ ...                                                            │ │
│  └───────────────────────────────────────────────────────────────┘ │
│                                                                       │
│  Showing 1-20 of 150 users          [◀ Prev]  1 2 3 ... 8  [Next ▶]│
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2. Estados Visuales

**Estado: Loading**
```tsx
// Skeleton loader mientras se cargan datos
<div className="animate-pulse space-y-4">
  <div className="h-12 bg-gray-200 dark:bg-gray-700 rounded"></div>
  <div className="h-12 bg-gray-200 dark:bg-gray-700 rounded"></div>
  {/* Repetir para 20 filas */}
</div>
```

**Estado: Empty (Sin resultados)**
```tsx
<EmptyState
  icon={<UsersIcon className="w-16 h-16 text-gray-400" />}
  title="No users found"
  description="No users match your search criteria. Try adjusting your filters."
  action={
    <Button onClick={clearFilters} variant="secondary">
      Clear Filters
    </Button>
  }
/>
```

**Estado: Error**
```tsx
<div className="rounded-lg bg-error-50 dark:bg-error-900/20 border border-error-200 dark:border-error-800 p-6">
  <p className="text-error-800 dark:text-error-200">Failed to load users. Please try again.</p>
  <Button onClick={refetch} variant="outline" className="mt-4">
    Retry
  </Button>
</div>
```

### 2.3. Responsive Behavior

- **Desktop (≥1024px)**: Tabla completa con todas las columnas
- **Tablet (768px-1023px)**: Ocultar columna "Plant", reducir acciones a iconos
- **Mobile (<768px)**: Vista de cards en lugar de tabla, filtros en drawer colapsable

---

## 3. Especificación Técnica

### 3.1. Props Interface

```typescript
// Este es un page component, no recibe props
// Toda la lógica viene de hooks de Refine y router
interface UserManagementPageProps {
  // No props - page component
}
```

### 3.2. State Management (Refine Hooks)

```typescript
import { useTable, useDelete, useNavigation } from "@refinedev/core";
import { useModalForm } from "@refinedev/react-hook-form";

// Hook principal para la tabla de usuarios
const {
  tableQueryResult,      // { data, isLoading, isError, refetch }
  current,               // Página actual
  setCurrent,            // Cambiar página
  pageSize,
  setPageSize,
  filters,
  setFilters,
  sorters,
  setSorters,
} = useTable<UsuarioResponse>({
  resource: "admin/users",
  pagination: {
    current: 1,
    pageSize: 20,
  },
  sorters: {
    initial: [{ field: "email", order: "asc" }],
  },
  filters: {
    permanent: [
      // Filtros que nunca se quitan (si aplica)
    ],
  },
});

// Hook para eliminación
const { mutate: deleteUser, isLoading: isDeleting } = useDelete<UsuarioResponse>();

// Hook para modal de crear/editar
const {
  modal: { show: showUserModal, close: closeUserModal, visible: userModalVisible },
  formState,
  register,
  handleSubmit,
  reset,
} = useModalForm<UsuarioCreateRequest, HttpError, UsuarioResponse>({
  refineCoreProps: {
    resource: "admin/users",
    action: "create", // o "edit"
  },
  modalProps: {
    autoSubmitClose: true,
    autoResetForm: true,
  },
});

// Estado local para filtros UI
const [searchQuery, setSearchQuery] = useState("");
const [selectedPlanta, setSelectedPlanta] = useState<number | null>(null);
const [selectedStatus, setSelectedStatus] = useState<boolean | null>(null);
const [showRoleModal, setShowRoleModal] = useState(false);
const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
```

### 3.3. Handlers

```typescript
// Aplicar búsqueda (debounced)
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
    }, 500),
  [setFilters]
);

// Aplicar filtro de planta
const handlePlantaChange = (plantaId: number | null) => {
  setSelectedPlanta(plantaId);
  setFilters((prev) => [
    ...prev.filter((f) => f.field !== "plantaId"),
    ...(plantaId ? [{ field: "plantaId", operator: "eq" as const, value: plantaId }] : []),
  ]);
};

// Aplicar filtro de estado
const handleStatusChange = (isActive: boolean | null) => {
  setSelectedStatus(isActive);
  setFilters((prev) => [
    ...prev.filter((f) => f.field !== "isActive"),
    ...(isActive !== null ? [{ field: "isActive", operator: "eq" as const, value: isActive }] : []),
  ]);
};

// Limpiar todos los filtros
const handleClearFilters = () => {
  setSearchQuery("");
  setSelectedPlanta(null);
  setSelectedStatus(null);
  setFilters([]);
};

// Abrir modal de crear usuario
const handleCreateUser = () => {
  showUserModal();
};

// Abrir modal de editar usuario
const handleEditUser = (userId: number) => {
  showUserModal(userId);
};

// Eliminar usuario (con confirmación)
const handleDeleteUser = (userId: number) => {
  if (confirm("¿Estás seguro de que deseas eliminar este usuario?")) {
    deleteUser(
      {
        resource: "admin/users",
        id: userId,
      },
      {
        onSuccess: () => {
          toast.success("Usuario eliminado exitosamente");
        },
        onError: () => {
          toast.error("Error al eliminar usuario");
        },
      }
    );
  }
};

// Abrir modal de asignación de roles
const handleManageRoles = (userId: number) => {
  setSelectedUserId(userId);
  setShowRoleModal(true);
};

// Refrescar datos
const handleRefresh = () => {
  tableQueryResult.refetch();
};
```

### 3.4. Estructura del Componente

```typescript
// src/pages/admin/users/list.tsx
import React, { useState, useMemo } from "react";
import { useTable, useDelete } from "@refinedev/core";
import { useModalForm } from "@refinedev/react-hook-form";
import { UsersIcon, PlusIcon, RefreshIcon, SearchIcon } from "@heroicons/react/outline";
import { debounce } from "lodash-es";

import { UserTable } from "@/components/users/UserTable";
import { UserFormModal } from "@/components/users/UserFormModal";
import { RoleAssignmentModal } from "@/components/users/RoleAssignmentModal";
import { EmptyState } from "@/components/common/EmptyState";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";

import type { UsuarioResponse, UsuarioCreateRequest } from "@/types/user.types";
import type { HttpError } from "@refinedev/core";

export const UserManagementPage: React.FC = () => {
  // Hooks de Refine
  const { tableQueryResult, current, setCurrent, pageSize, setPageSize, filters, setFilters } =
    useTable<UsuarioResponse>({
      resource: "admin/users",
      pagination: { current: 1, pageSize: 20 },
      sorters: { initial: [{ field: "email", order: "asc" }] },
    });

  const { mutate: deleteUser, isLoading: isDeleting } = useDelete<UsuarioResponse>();

  const {
    modal: { show: showUserModal, visible: userModalVisible },
  } = useModalForm<UsuarioCreateRequest, HttpError, UsuarioResponse>({
    refineCoreProps: { resource: "admin/users" },
  });

  // Estado local
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedPlanta, setSelectedPlanta] = useState<number | null>(null);
  const [selectedStatus, setSelectedStatus] = useState<boolean | null>(null);
  const [showRoleModal, setShowRoleModal] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);

  // Handlers (ver sección 3.3)

  // Datos de la tabla
  const { data, isLoading, isError } = tableQueryResult;
  const users = data?.data || [];
  const total = data?.total || 0;

  // Cargar plantas para filtro
  const { data: plantasData } = useList<PlantaResponse>({
    resource: "plantas",
    pagination: { current: 1, pageSize: 100 },
  });

  const plantas = plantasData?.data || [];

  return (
    <div className="p-6 bg-gray-50 dark:bg-gray-900 min-h-screen">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-gray-800 dark:text-white">Users</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
            Manage user accounts and permissions
          </p>
        </div>
        <Button
          onClick={handleCreateUser}
          variant="primary"
          leftIcon={<PlusIcon className="w-5 h-5" />}
        >
          New User
        </Button>
      </div>

      {/* Filtros */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* Búsqueda */}
          <div className="md:col-span-2">
            <Input
              type="search"
              placeholder="Search by name or email..."
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                handleSearch(e.target.value);
              }}
              leftIcon={<SearchIcon className="w-5 h-5 text-gray-400" />}
            />
          </div>

          {/* Filtro de Planta */}
          <Select
            value={selectedPlanta?.toString() || ""}
            onChange={(e) => handlePlantaChange(e.target.value ? Number(e.target.value) : null)}
            options={[
              { value: "", label: "All Plants" },
              ...plantas.map((p) => ({
                value: p.id.toString(),
                label: p.plantaName,
              })),
            ]}
          />

          {/* Filtro de Estado */}
          <Select
            value={selectedStatus === null ? "" : selectedStatus.toString()}
            onChange={(e) =>
              handleStatusChange(e.target.value === "" ? null : e.target.value === "true")
            }
            options={[
              { value: "", label: "All Status" },
              { value: "true", label: "Active" },
              { value: "false", label: "Inactive" },
            ]}
          />
        </div>

        {/* Botones de acción */}
        <div className="flex items-center gap-2 mt-4">
          <Button onClick={handleClearFilters} variant="outline" size="sm">
            Clear Filters
          </Button>
          <Button
            onClick={handleRefresh}
            variant="ghost"
            size="sm"
            leftIcon={<RefreshIcon className="w-4 h-4" />}
          >
            Refresh
          </Button>
        </div>
      </div>

      {/* Tabla de Usuarios */}
      {isLoading ? (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm p-8">
          {/* Skeleton loader */}
          <div className="animate-pulse space-y-4">
            {[...Array(10)].map((_, i) => (
              <div key={i} className="h-12 bg-gray-200 dark:bg-gray-700 rounded"></div>
            ))}
          </div>
        </div>
      ) : isError ? (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm p-8">
          <div className="text-center text-error-600 dark:text-error-400">
            <p>Failed to load users. Please try again.</p>
            <Button onClick={handleRefresh} variant="outline" className="mt-4">
              Retry
            </Button>
          </div>
        </div>
      ) : users.length === 0 ? (
        <EmptyState
          icon={<UsersIcon className="w-16 h-16 text-gray-400" />}
          title="No users found"
          description="No users match your search criteria."
          action={
            <Button onClick={handleClearFilters} variant="secondary">
              Clear Filters
            </Button>
          }
        />
      ) : (
        <>
          <UserTable
            users={users}
            onEdit={handleEditUser}
            onDelete={handleDeleteUser}
            onManageRoles={handleManageRoles}
            isDeleting={isDeleting}
          />

          {/* Paginación */}
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm p-4 mt-4">
            <div className="flex items-center justify-between">
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Showing {(current - 1) * pageSize + 1}-{Math.min(current * pageSize, total)} of{" "}
                {total} users
              </p>
              <div className="flex items-center gap-2">
                <Button
                  onClick={() => setCurrent(current - 1)}
                  disabled={current === 1}
                  variant="outline"
                  size="sm"
                >
                  Previous
                </Button>
                {/* Números de página */}
                <Button
                  onClick={() => setCurrent(current + 1)}
                  disabled={current * pageSize >= total}
                  variant="outline"
                  size="sm"
                >
                  Next
                </Button>
              </div>
            </div>
          </div>
        </>
      )}

      {/* Modales */}
      <UserFormModal isOpen={userModalVisible} onClose={closeUserModal} />
      <RoleAssignmentModal
        isOpen={showRoleModal}
        onClose={() => setShowRoleModal(false)}
        userId={selectedUserId}
      />
    </div>
  );
};
```

---

## 4. Integración con API

### 4.1. Endpoints Utilizados

```typescript
// GET /api/v1/admin/users
// Query params: search, plantaId, isActive, page, size, sort
// Response: UserListResponse (paginado)

// DELETE /api/v1/admin/users/{id}
// Response: 204 No Content
```

### 4.2. Data Provider Config

```typescript
// src/providers/dataProvider.ts
import { DataProvider } from "@refinedev/core";
import axios from "@/utils/axios";

export const dataProvider: DataProvider = {
  getList: async ({ resource, pagination, filters, sorters }) => {
    const { current = 1, pageSize = 20 } = pagination || {};
    
    const params = new URLSearchParams({
      page: (current - 1).toString(),
      size: pageSize.toString(),
    });

    // Agregar filtros
    filters?.forEach((filter) => {
      if (filter.value !== null && filter.value !== undefined) {
        params.append(filter.field, filter.value.toString());
      }
    });

    // Agregar ordenamiento
    if (sorters && sorters.length > 0) {
      const sort = sorters.map((s) => `${s.field},${s.order}`).join(",");
      params.append("sort", sort);
    }

    const { data } = await axios.get(`/api/v1/${resource}?${params.toString()}`);

    return {
      data: data.content,
      total: data.totalElements,
    };
  },

  deleteOne: async ({ resource, id }) => {
    await axios.delete(`/api/v1/${resource}/${id}`);
    return { data: {} };
  },

  // ... otros métodos
};
```

---

## 5. Validación y Tests

### 5.1. Casos de Test

```typescript
// src/pages/admin/users/__tests__/list.test.tsx
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { UserManagementPage } from "../list";

describe("UserManagementPage", () => {
  it("renderiza el título correctamente", () => {
    render(<UserManagementPage />);
    expect(screen.getByText("Users")).toBeInTheDocument();
  });

  it("muestra skeleton loader mientras carga datos", () => {
    render(<UserManagementPage />);
    expect(screen.getByTestId("skeleton-loader")).toBeInTheDocument();
  });

  it("muestra la tabla de usuarios cuando los datos cargan", async () => {
    render(<UserManagementPage />);
    await waitFor(() => {
      expect(screen.getByRole("table")).toBeInTheDocument();
    });
  });

  it("abre modal al hacer click en 'New User'", async () => {
    const user = userEvent.setup();
    render(<UserManagementPage />);
    
    await user.click(screen.getByText("New User"));
    expect(screen.getByRole("dialog")).toBeInTheDocument();
  });

  it("filtra usuarios al escribir en búsqueda", async () => {
    const user = userEvent.setup();
    render(<UserManagementPage />);
    
    const searchInput = screen.getByPlaceholderText("Search by name or email...");
    await user.type(searchInput, "juan");
    
    // Verificar que se aplicó el filtro (debounced)
    await waitFor(() => {
      expect(mockSetFilters).toHaveBeenCalled();
    }, { timeout: 1000 });
  });

  it("confirma antes de eliminar usuario", async () => {
    const user = userEvent.setup();
    window.confirm = vi.fn(() => true);
    
    render(<UserManagementPage />);
    await waitFor(() => screen.getByRole("table"));
    
    await user.click(screen.getAllByRole("button", { name: /delete/i })[0]);
    expect(window.confirm).toHaveBeenCalledWith(
      expect.stringContaining("eliminar")
    );
  });
});
```

---

## 6. Accesibilidad

```tsx
// Atributos ARIA requeridos
<main role="main" aria-labelledby="page-title">
  <h1 id="page-title">Users</h1>
  
  <Input
    aria-label="Search users by name or email"
    aria-describedby="search-hint"
  />
  <span id="search-hint" className="sr-only">
    Type to filter users in real-time
  </span>

  <table role="table" aria-label="Users table">
    {/* ... */}
  </table>
</main>
```

---

## 7. Notas de Implementación

### 7.1. Performance
- Debounce en búsqueda (500ms)
- Paginación limitada a max 100 items por página
- Memoizar handlers con `useCallback`
- Virtualización si >100 items (opcional, usar react-window)

### 7.2. Seguridad
- Validar token JWT en cada request (axios interceptor)
- Redirigir a login si 401 Unauthorized
- Mostrar error si 403 Forbidden (sin ROLE_ADMIN)

### 7.3. UX
- Confirmación antes de eliminar
- Toast notifications en operaciones exitosas/fallidas
- Loading states en botones durante operaciones
- Preservar filtros en navegación (URL params opcional)

---

**Generado por**: FTV Generator v2.0  
**Tiempo estimado**: 6-8 horas  
**Dependencias**: FTV-004, FTV-005, FTV-007, FTV-009
