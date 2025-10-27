# FTV-003: Permission Management Page

**ID**: FTV-IOC-004-003  
**Componente**: `PermissionManagementPage`  
**Tipo**: Page Component  
**Ruta**: `/admin/permissions`  
**Sprint**: Sprint 3 (IOC-004)  
**Technical Design**: TD-IOC-004-User-Role-Management-claude.md  
**Fecha Creación**: 2025-10-27  
**Estado**: ✅ Ready for Implementation

---

## 1. Propósito y Contexto

### 1.1. Propósito
Catálogo de permisos del sistema que permite crear, editar y eliminar permisos granulares que luego se asignan a roles.

### 1.2. Caso de Uso Principal
**Actor**: Administrador (ROLE_ADMIN)

**Flujo**:
1. Admin accede a `/admin/permissions`
2. Ve grid de permisos disponibles con descripciones
3. Puede crear nuevo permiso, editar existente, o eliminar (si no está asignado a roles)
4. Los permisos se agrupan por categoría (USER, ROLE, DASHBOARD, etc.)

---

## 2. Especificación Visual

### 2.1. Wireframe ASCII

```
┌─────────────────────────────────────────────────────────────────────┐
│ IOC Platform - Permission Management                [Profile ▾]     │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  Permissions                                                          │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                                       │
│  ┌─────────────────────────────────────────────┐  [+ New Permission]│
│  │ 🔍 Search permissions...                    │                    │
│  └─────────────────────────────────────────────┘                    │
│                                                                       │
│  Category: [All ▾]                                                   │
│                                                                       │
│  USER MANAGEMENT                                                     │
│  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐          │
│  │ 🔑 USER_READ   │ │ 🔑 USER_WRITE  │ │ 🔑 USER_DELETE │          │
│  │                │ │                │ │                │          │
│  │ Read user data │ │ Create/edit    │ │ Delete users   │          │
│  │                │ │ users          │ │                │          │
│  │ [Edit] [Del]   │ │ [Edit] [Del]   │ │ [Edit] [Del]   │          │
│  └────────────────┘ └────────────────┘ └────────────────┘          │
│                                                                       │
│  ROLE MANAGEMENT                                                     │
│  ┌────────────────┐ ┌────────────────┐                              │
│  │ 🔑 ROLE_READ   │ │ 🔑 ROLE_WRITE  │                              │
│  │                │ │                │                              │
│  │ Read roles     │ │ Create/edit    │                              │
│  │                │ │ roles          │                              │
│  │ [Edit] [Del]   │ │ [Edit] [Del]   │                              │
│  └────────────────┘ └────────────────┘                              │
│                                                                       │
│  DASHBOARD                                                           │
│  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐          │
│  │ 🔑 DASH_VIEW   │ │ 🔑 DASH_EDIT   │ │ 🔑 KPI_VIEW    │          │
│  │ ...            │ │ ...            │ │ ...            │          │
│  └────────────────┘ └────────────────┘ └────────────────┘          │
│                                                                       │
│  Showing 13 permissions                                              │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2. Estados Visuales

**Estado: Empty Category**
```typescript
<EmptyState
  icon="🔑"
  title="No permissions in this category"
  description="Select another category or create a new permission"
/>
```

**Estado: Error (Delete con roles asignados)**
```typescript
<AlertDialog
  title="Cannot Delete Permission"
  description="This permission is assigned to 3 roles. Remove all role assignments first."
  variant="error"
/>
```

---

## 3. Jerarquía de Componentes

```
<PermissionManagementPage>
  ├── <PageHeader>
  │   └── <Button>+ New Permission</Button>
  ├── <SearchInput />
  ├── <CategoryFilter>
  │   └── <Select options={categories} />
  ├── <PermissionGrid>
  │   ├── <CategorySection> (×N)
  │   │   ├── <CategoryTitle />
  │   │   └── <PermissionCard> (×M)
  │   │       ├── <PermissionIcon />
  │   │       ├── <PermissionName />
  │   │       ├── <PermissionDescription />
  │   │       └── <ActionMenu>
  ├── <PermissionFormModal isOpen={showForm} />
  └── <ConfirmDeleteDialog isOpen={showConfirm} />
</PermissionManagementPage>
```

---

## 4. Props y API del Componente

### 4.1. State Management

```typescript
interface PermissionManagementState {
  permissions: Permission[];
  searchQuery: string;
  categoryFilter: string | null;
  isLoading: boolean;
  error: string | null;
  showPermForm: boolean;
  showConfirmDelete: boolean;
  selectedPermId: number | null;
  formMode: 'create' | 'edit';
}

interface Permission {
  id: number;
  name: string;
  description?: string;
}

// Categorías inferidas del nombre
const categories = [
  'USER',
  'ROLE',
  'PERMISSION',
  'DASHBOARD',
  'KPI',
  'REPORT',
  'PLANT'
];

function getCategoryFromName(name: string): string {
  const prefix = name.split('_')[0];
  return categories.includes(prefix) ? prefix : 'OTHER';
}
```

---

## 5. Integración con Backend

### 5.1. Endpoints Consumidos

**GET /api/v1/admin/permissions**
```typescript
const fetchPermissions = async (search?: string): Promise<PermissionListResponse> => {
  const response = await api.get('/api/v1/admin/permissions', {
    params: { search, size: 100 }
  });
  return response.data;
};
```

**POST /api/v1/admin/permissions**
```typescript
interface PermissionRequest {
  name: string;
  description?: string;
}

const createPermission = async (data: PermissionRequest): Promise<PermissionResponse> => {
  const response = await api.post('/api/v1/admin/permissions', data);
  return response.data;
};
```

**PUT /api/v1/admin/permissions/{id}**
```typescript
const updatePermission = async (id: number, data: PermissionRequest): Promise<PermissionResponse> => {
  const response = await api.put(`/api/v1/admin/permissions/${id}`, data);
  return response.data;
};
```

**DELETE /api/v1/admin/permissions/{id}**
```typescript
const deletePermission = async (id: number): Promise<void> => {
  await api.delete(`/api/v1/admin/permissions/${id}`);
};
```

---

## 6. Lógica de Negocio

### 6.1. Reglas

1. **Naming Convention**: UPPERCASE_SNAKE_CASE (ej: `USER_READ`, `DASHBOARD_EDIT`)
2. **Categorización**: Primera palabra del nombre define categoría
3. **Eliminación Condicional**: Solo si no está asignado a ningún rol (backend valida)
4. **Permisos Seed**: Los creados en migración SQL están protegidos

### 6.2. Validaciones

```typescript
const permissionSchema = z.object({
  name: z.string()
    .min(3, 'Name must be at least 3 characters')
    .max(150, 'Name must be less than 150 characters')
    .regex(/^[A-Z_]+$/, 'Name must be uppercase with underscores (e.g., USER_READ)'),
  description: z.string().max(255).optional()
});
```

### 6.3. Agrupación por Categoría

```typescript
function groupPermissionsByCategory(permissions: Permission[]): Record<string, Permission[]> {
  return permissions.reduce((acc, perm) => {
    const category = getCategoryFromName(perm.name);
    if (!acc[category]) acc[category] = [];
    acc[category].push(perm);
    return acc;
  }, {} as Record<string, Permission[]>);
}
```

---

## 7. Testing

```typescript
describe('PermissionManagementPage', () => {
  it('should group permissions by category', () => {
    const permissions = [
      { id: 1, name: 'USER_READ', description: 'Read users' },
      { id: 2, name: 'USER_WRITE', description: 'Write users' },
      { id: 3, name: 'DASHBOARD_VIEW', description: 'View dashboards' }
    ];
    
    mockUsePermissions.mockReturnValue({ data: { content: permissions }, isLoading: false });
    
    render(<PermissionManagementPage />);
    
    expect(screen.getByText('USER MANAGEMENT')).toBeInTheDocument();
    expect(screen.getByText('DASHBOARD')).toBeInTheDocument();
  });
  
  it('should prevent deleting permission assigned to roles', async () => {
    mockDeletePermission.mockRejectedValue({ status: 409 });
    
    const { user } = render(<PermissionManagementPage />);
    
    await user.click(screen.getByLabelText('Delete USER_READ'));
    await user.click(screen.getByText('Confirm'));
    
    await waitFor(() => {
      expect(screen.getByText(/assigned to.*roles/i)).toBeInTheDocument();
    });
  });
});
```

---

## 8. Checklist de Implementación

- [ ] Crear `PermissionManagementPage.tsx`
- [ ] Implementar `PermissionCard` component
- [ ] Implementar agrupación por categoría
- [ ] Implementar `PermissionFormModal`
- [ ] Crear hooks de React Query
- [ ] Implementar búsqueda y filtro por categoría
- [ ] Añadir confirmación de delete
- [ ] Implementar manejo de errores 409
- [ ] Tests unitarios
- [ ] Tests E2E

---

**Próximo FTV**: `ftv-user-form-modal.md` (FTV-IOC-004-004)

