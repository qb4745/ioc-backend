# FTV-IOC-004-009: Empty State Component

**ID**: FTV-IOC-004-009  
**Componente**: `EmptyState`  
**Tipo**: Reusable UI Component  
**Technical Design**: TD-IOC-004-User-Role-Management-claude.md
**Sprint**: Sprint 3 (IOC-004)  
**Fecha**: 2025-10-28  
**Estado**: ✅ Ready for Implementation

---

## 1. Propósito y Contexto

### 1.1. Propósito
Componente reutilizable para mostrar estados vacíos con ilustración, mensaje contextualizado en español chileno y acción opcional.

### 1.2. Casos de Uso
- Sin resultados de búsqueda
- Sin usuarios registrados
- Sin roles configurados
- Sin permisos disponibles
- Cualquier lista vacía en la aplicación

---

## 2. Especificación Visual

### 2.1. Wireframe ASCII

```
┌─────────────────────────────────────────┐
│                                         │
│              [  ÍCONO  ]                │
│                                         │
│       No se encontraron usuarios        │
│                                         │
│   No hay usuarios que coincidan con tu  │
│     búsqueda. Intenta ajustar los       │
│              filtros.                   │
│                                         │
│          [ Limpiar Filtros ]            │
│                                         │
└─────────────────────────────────────────┘
```

---

## 3. Especificación Técnica

### 3.1. Props Interface

```typescript
interface EmptyStateProps {
  icon?: React.ReactNode;
  title: string;
  description?: string;
  action?: React.ReactNode;
  size?: "sm" | "md" | "lg";
  className?: string;
}
```

### 3.2. Size Mapping

```typescript
const SIZE_CONFIG = {
  sm: {
    container: "py-8",
    icon: "w-12 h-12",
    title: "text-lg",
    description: "text-sm",
  },
  md: {
    container: "py-12",
    icon: "w-16 h-16",
    title: "text-xl",
    description: "text-base",
  },
  lg: {
    container: "py-16",
    icon: "w-20 h-20",
    title: "text-2xl",
    description: "text-lg",
  },
} as const;
```

### 3.3. Estructura del Componente

```typescript
// src/components/common/EmptyState.tsx
import React from "react";

interface EmptyStateProps {
  icon?: React.ReactNode;
  title: string;
  description?: string;
  action?: React.ReactNode;
  size?: "sm" | "md" | "lg";
  className?: string;
}

const SIZE_CONFIG = {
  sm: {
    container: "py-8",
    icon: "w-12 h-12",
    title: "text-lg",
    description: "text-sm",
  },
  md: {
    container: "py-12",
    icon: "w-16 h-16",
    title: "text-xl",
    description: "text-base",
  },
  lg: {
    container: "py-16",
    icon: "w-20 h-20",
    title: "text-2xl",
    description: "text-lg",
  },
} as const;

export const EmptyState: React.FC<EmptyStateProps> = ({
  icon,
  title,
  description,
  action,
  size = "md",
  className = "",
}) => {
  const config = SIZE_CONFIG[size];

  return (
    <div
      className={`
        ${config.container}
        bg-white dark:bg-gray-800
        rounded-lg shadow-theme-sm
        flex flex-col items-center justify-center
        text-center
        ${className}
      `}
    >
      {/* Icon */}
      {icon && (
        <div className={`${config.icon} text-gray-400 dark:text-gray-500 mb-4`}>
          {icon}
        </div>
      )}

      {/* Title */}
      <h3
        className={`
          ${config.title}
          font-semibold
          text-gray-900 dark:text-white
          mb-2
        `}
      >
        {title}
      </h3>

      {/* Description */}
      {description && (
        <p
          className={`
            ${config.description}
            text-gray-600 dark:text-gray-400
            max-w-md
            mb-6
          `}
        >
          {description}
        </p>
      )}

      {/* Action */}
      {action && <div className="mt-2">{action}</div>}
    </div>
  );
};

export default React.memo(EmptyState);
```

---

## 4. Ejemplos de Uso

### 4.1. Sin Usuarios

```typescript
import { UsersIcon } from "@heroicons/react/outline";
import { EmptyState } from "@/components/common/EmptyState";
import { Button } from "@/components/ui/Button";

<EmptyState
  icon={<UsersIcon />}
  title="No se encontraron usuarios"
  description="No hay usuarios que coincidan con tu búsqueda. Intenta ajustar los filtros."
  action={
    <Button onClick={clearFilters} variant="secondary">
      Limpiar Filtros
    </Button>
  }
/>
```

### 4.2. Sin Roles

```typescript
import { ShieldCheckIcon } from "@heroicons/react/outline";

<EmptyState
  icon={<ShieldCheckIcon />}
  title="No hay roles configurados"
  description="Crea tu primer rol para comenzar a organizar los permisos."
  action={
    <Button onClick={handleCreateRole} variant="primary">
      Crear Primer Rol
    </Button>
  }
  size="lg"
/>
```

### 4.3. Sin Permisos

```typescript
import { KeyIcon } from "@heroicons/react/outline";

<EmptyState
  icon={<KeyIcon />}
  title="No se encontraron permisos"
  description="Crea tu primer permiso para comenzar a controlar el acceso."
  action={
    <Button onClick={handleCreatePermission} variant="primary">
      Crear Primer Permiso
    </Button>
  }
/>
```

### 4.4. Sin Resultados de Búsqueda

```typescript
import { SearchIcon } from "@heroicons/react/outline";

<EmptyState
  icon={<SearchIcon />}
  title="Sin resultados"
  description={`No se encontraron resultados para "${searchQuery}"`}
  action={
    <Button onClick={() => setSearchQuery("")} variant="outline">
      Limpiar Búsqueda
    </Button>
  }
  size="sm"
/>
```

### 4.5. Con Emoji (Sin Librería de Iconos)

```typescript
<EmptyState
  icon={<span className="text-6xl">📭</span>}
  title="Sin notificaciones"
  description="¡Estás al día! Vuelve más tarde para ver actualizaciones."
/>
```

---

## 5. Variantes por Contexto

### 5.1. Lista Vacía Genérica

```typescript
<EmptyState
  title="Aún no hay nada aquí"
  description="Los elementos aparecerán aquí una vez que se agreguen."
/>
```

### 5.2. Error de Búsqueda

```typescript
<EmptyState
  icon={<ExclamationCircleIcon className="text-yellow-500" />}
  title="La búsqueda no arrojó resultados"
  description="Intenta con otras palabras clave o verifica la ortografía."
  action={<Button onClick={resetSearch}>Reiniciar Búsqueda</Button>}
/>
```

### 5.3. Feature No Disponible

```typescript
<EmptyState
  icon={<LockClosedIcon className="text-gray-400" />}
  title="Funcionalidad no disponible"
  description="Esta funcionalidad no está disponible en tu plan actual."
  action={<Button onClick={handleUpgrade}>Mejorar Plan</Button>}
/>
```

---

## 6. Validación y Tests

```typescript
describe("EmptyState", () => {
  it("renderiza título correctamente", () => {
    render(<EmptyState title="No se encontraron usuarios" />);
    expect(screen.getByText("No se encontraron usuarios")).toBeInTheDocument();
  });

  it("renderiza descripción cuando se proporciona", () => {
    render(
      <EmptyState
        title="Empty"
        description="This is a description"
      />
    );
    expect(screen.getByText("This is a description")).toBeInTheDocument();
  });

  it("renderiza acción cuando se proporciona", () => {
    render(
      <EmptyState
        title="Empty"
        action={<button>Click me</button>}
      />
    );
    expect(screen.getByText("Click me")).toBeInTheDocument();
  });

  it("aplica tamaño correcto", () => {
    const { container } = render(
      <EmptyState title="Empty" size="lg" />
    );
    expect(container.firstChild?.firstChild).toHaveClass("py-16");
  });

  it("renderiza icono cuando se proporciona", () => {
    render(
      <EmptyState
        title="Empty"
        icon={<div data-testid="custom-icon">Icon</div>}
      />
    );
    expect(screen.getByTestId("custom-icon")).toBeInTheDocument();
  });

  it("no renderiza descripción ni acción cuando no se proporcionan", () => {
    const { container } = render(<EmptyState title="Empty" />);
    expect(container.querySelector("p")).not.toBeInTheDocument();
  });
});
```

---

## 7. Accesibilidad

```tsx
// Estructura semántica
<div role="status" aria-live="polite">
  <EmptyState
    title="No results"
    description="Try adjusting your filters"
  />
</div>

// Con acción
<EmptyState
  title="No users"
  action={
    <Button
      onClick={handleCreate}
      aria-label="Create your first user"
    >
      Create User
    </Button>
  }
/>
```

---

## 8. Guía de Estilo

### 8.1. Títulos Recomendados
- ✅ "No se encontraron usuarios"
- ✅ "Sin resultados"
- ✅ "Aún no hay nada aquí"
- ❌ "¡Error!" (demasiado alarmante)
- ❌ "Vacío" (poco descriptivo)

### 8.2. Descripciones Recomendadas
- ✅ Ser específico: "No hay usuarios que coincidan con tu búsqueda"
- ✅ Sugerir acción: "Intenta ajustar los filtros"
- ✅ Ser positivo: "Los elementos aparecerán aquí una vez agregados"
- ❌ Ser vago: "Algo salió mal"
- ❌ Culpar al usuario: "No agregaste nada"

### 8.3. Acciones Recomendadas
- ✅ Botón primario para crear: "Crear Primer Elemento"
- ✅ Botón secundario para resetear: "Limpiar Filtros"
- ✅ Link para ayuda: "Aprender Más"
- ❌ Múltiples acciones confusas

---

## 9. Notas de Implementación

### 9.1. Performance
- Componente memoizado con `React.memo`
- Props simples para evitar re-renders
- No efectos secundarios

### 9.2. Flexibilidad
- Acepta cualquier ReactNode como icon
- Action puede ser cualquier componente
- Sizes configurables para diferentes contextos

### 9.3. Consistencia
- Usar siempre el mismo componente en toda la app
- Mantener mensajes consistentes en tono
- Iconos de Heroicons preferiblemente

---

**Generado por**: FTV Generator v2.0  
**Tiempo estimado**: 1-2 horas  
**Dependencias**: Ninguna (componente base)
# FTV-IOC-004-007: User Table Component

**ID**: FTV-IOC-004-007  
**Componente**: `UserTable`  
**Tipo**: Reusable Component  
**Sprint**: Sprint 3 (IOC-004)  
**Fecha**: 2025-10-27  
**Estado**: ✅ Ready for Implementation

---

## 1. Propósito y Contexto

### 1.1. Propósito
Componente reutilizable de tabla para mostrar usuarios con columnas estándar, acciones y responsive design.

### 1.2. Características
- Tabla responsive con layout adaptativo
- Muestra roles con badges
- Acciones contextuales (Edit, Manage Roles, Delete)
- Sorting visual en headers
- Estados de loading en acciones

---

## 2. Especificación Visual

### 2.1. Wireframe ASCII

```
┌─────────────────────────────────────────────────────────────┐
│ Name          │ Email          │ Plant  │ Roles      │ Actions│
├─────────────────────────────────────────────────────────────┤
│ Juan Pérez    │ juan@ioc.com   │ Lima   │ ADMIN      │ ⚙️ 🗑️ │
│ María García  │ maria@ioc.com  │ Callao │ GERENTE    │ ⚙️ 🗑️ │
│ Pedro López   │ pedro@ioc.com  │ Lima   │ ANALISTA   │ ⚙️ 🗑️ │
└─────────────────────────────────────────────────────────────┘
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
  className?: string;
}
```

### 3.2. Estructura del Componente

```typescript
// src/components/users/UserTable.tsx
import React from "react";
import { PencilIcon, TrashIcon, UserGroupIcon } from "@heroicons/react/outline";
import { RoleBadge } from "@/components/roles/RoleBadge";
import { Button } from "@/components/ui/Button";
import type { UsuarioResponse } from "@/types/user.types";

export const UserTable: React.FC<UserTableProps> = ({
  users,
  onEdit,
  onDelete,
  onManageRoles,
  isDeleting = false,
  className = "",
}) => {
  return (
    <div className={`bg-white dark:bg-gray-800 rounded-lg shadow-theme-sm overflow-hidden ${className}`}>
      {/* Desktop Table */}
      <div className="hidden md:block overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead className="bg-gray-50 dark:bg-gray-900">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Name
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Email
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Plant
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Roles
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
            {users.map((user) => (
              <tr
                key={user.id}
                className="hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              >
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 h-10 w-10 bg-brand-100 dark:bg-brand-900 rounded-full flex items-center justify-center">
                      <span className="text-brand-600 dark:text-brand-400 font-semibold text-sm">
                        {user.fullName.charAt(0).toUpperCase()}
                      </span>
                    </div>
                    <div className="ml-4">
                      <div className="text-sm font-medium text-gray-900 dark:text-white">
                        {user.fullName}
                      </div>
                      {!user.isActive && (
                        <span className="text-xs text-gray-500 dark:text-gray-400">
                          (Inactive)
                        </span>
                      )}
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-gray-600 dark:text-gray-400">
                    {user.email}
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-gray-900 dark:text-white">
                    {user.plantaName || "—"}
                  </div>
                  {user.plantaCode && (
                    <div className="text-xs text-gray-500 dark:text-gray-400">
                      {user.plantaCode}
                    </div>
                  )}
                </td>
                <td className="px-6 py-4">
                  <div className="flex flex-wrap gap-1">
                    {user.roles && user.roles.length > 0 ? (
                      user.roles.map((role) => (
                        <RoleBadge key={role} name={role} size="sm" />
                      ))
                    ) : (
                      <span className="text-sm text-gray-400 dark:text-gray-500">
                        No roles
                      </span>
                    )}
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div className="flex items-center justify-end gap-2">
                    <Button
                      onClick={() => onEdit(user.id)}
                      variant="ghost"
                      size="sm"
                      title="Edit user"
                      leftIcon={<PencilIcon className="w-4 h-4" />}
                    >
                      <span className="hidden lg:inline">Edit</span>
                    </Button>
                    <Button
                      onClick={() => onManageRoles(user.id)}
                      variant="ghost"
                      size="sm"
                      title="Manage roles"
                      leftIcon={<UserGroupIcon className="w-4 h-4" />}
                    >
                      <span className="hidden lg:inline">Roles</span>
                    </Button>
                    <Button
                      onClick={() => onDelete(user.id)}
                      variant="ghost"
                      size="sm"
                      disabled={isDeleting}
                      title="Delete user"
                      className="text-error-600 hover:text-error-700 dark:text-error-400 dark:hover:text-error-300"
                      leftIcon={<TrashIcon className="w-4 h-4" />}
                    >
                      <span className="hidden lg:inline">Delete</span>
                    </Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Mobile Cards */}
      <div className="md:hidden divide-y divide-gray-200 dark:divide-gray-700">
        {users.map((user) => (
          <div key={user.id} className="p-4 hover:bg-gray-50 dark:hover:bg-gray-700">
            {/* User Info */}
            <div className="flex items-start gap-3 mb-3">
              <div className="flex-shrink-0 h-12 w-12 bg-brand-100 dark:bg-brand-900 rounded-full flex items-center justify-center">
                <span className="text-brand-600 dark:text-brand-400 font-semibold">
                  {user.fullName.charAt(0).toUpperCase()}
                </span>
              </div>
              <div className="flex-1 min-w-0">
                <div className="text-sm font-medium text-gray-900 dark:text-white truncate">
                  {user.fullName}
                </div>
                <div className="text-sm text-gray-600 dark:text-gray-400 truncate">
                  {user.email}
                </div>
                {user.plantaName && (
                  <div className="text-xs text-gray-500 dark:text-gray-500 mt-1">
                    📍 {user.plantaName}
                  </div>
                )}
              </div>
            </div>

            {/* Roles */}
            <div className="mb-3">
              <div className="flex flex-wrap gap-1">
                {user.roles && user.roles.length > 0 ? (
                  user.roles.map((role) => (
                    <RoleBadge key={role} name={role} size="sm" />
                  ))
                ) : (
                  <span className="text-xs text-gray-400 dark:text-gray-500">
                    No roles assigned
                  </span>
                )}
              </div>
            </div>

            {/* Actions */}
            <div className="flex items-center gap-2">
              <Button
                onClick={() => onEdit(user.id)}
                variant="outline"
                size="sm"
                className="flex-1"
                leftIcon={<PencilIcon className="w-4 h-4" />}
              >
                Edit
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
                className="text-error-600 border-error-600 hover:bg-error-50 dark:text-error-400 dark:border-error-400"
              >
                <TrashIcon className="w-4 h-4" />
              </Button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
```

---

## 4. Responsive Behavior

- **Desktop (≥768px)**: Tabla completa con todas las columnas
- **Mobile (<768px)**: Vista de cards verticales con botones adaptados

---

## 5. Notas de Implementación

### 5.1. Accesibilidad
- Botones con `title` para tooltips
- Indicador visual de usuario inactivo
- Alto contraste en modo oscuro

### 5.2. Performance
- Componente memoizado con `React.memo`
- Evitar re-renders innecesarios
- Virtualización opcional si >100 usuarios

---

**Generado por**: FTV Generator v2.0  
**Tiempo estimado**: 2-3 horas  
**Dependencias**: FTV-008 (RoleBadge), UI components
