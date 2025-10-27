# FTV-002: Role Management Page

**ID**: FTV-IOC-004-002  
**Componente**: `RoleManagementPage`  
**Tipo**: Page Component  
**Ruta**: `/admin/roles`  
**Sprint**: Sprint 3 (IOC-004)  
**Technical Design**: TD-IOC-004-User-Role-Management-claude.md  
**Fecha Creación**: 2025-10-27  
**Estado**: ✅ Ready for Implementation

---

## 1. Propósito y Contexto

### 1.1. Propósito
Página de administración de roles del sistema que permite crear, editar, eliminar roles y gestionar permisos asociados a cada rol.

### 1.2. Caso de Uso Principal
**Actor**: Administrador (ROLE_ADMIN)

**Flujo**:
1. Admin accede a `/admin/roles`
2. Ve lista de roles con conteo de usuarios y permisos asignados
3. Puede crear nuevo rol, editar existente, o eliminar (si no está en uso)
4. Puede asignar/remover permisos a roles
5. Ve resumen de cuántos usuarios tienen cada rol

### 1.3. Ubicación en la App
```
Dashboard
└── Admin Section
    ├── User Management
    └── Role Management ← ESTA PÁGINA
        └── Permission Management
```

---

## 2. Especificación Visual

### 2.1. Wireframe ASCII

```
┌─────────────────────────────────────────────────────────────────────┐
│ IOC Platform - Role Management                      [Profile ▾]     │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  Roles                                                                │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                                       │
│  ┌─────────────────────────────────────────────┐  [+ New Role]     │
│  │ 🔍 Search roles...                          │                    │
│  └─────────────────────────────────────────────┘                    │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │ ┌─────────────────────────────────────────────────────────┐   │ │
│  │ │ 👑 ADMIN                                    [Edit] [Del] │   │ │
│  │ │                                                           │   │ │
│  │ │ Acceso administrativo total al sistema                   │   │ │
│  │ │                                                           │   │ │
│  │ │ 👥 5 users                                                │   │ │
│  │ │ 🔑 13 permissions:                                        │   │ │
│  │ │    USER_READ, USER_WRITE, ROLE_READ, ROLE_WRITE...      │   │ │
│  │ │                                                           │   │ │
│  │ │ [Manage Permissions]                                      │   │ │
│  │ └─────────────────────────────────────────────────────────┘   │ │
│  │                                                                 │ │
│  │ ┌─────────────────────────────────────────────────────────┐   │ │
│  │ │ 👔 GERENTE                                  [Edit] [Del] │   │ │
│  │ │                                                           │   │ │
│  │ │ Gestión operativa y supervisión                          │   │ │
│  │ │                                                           │   │ │
│  │ │ 👥 12 users                                               │   │ │
│  │ │ 🔑 10 permissions:                                        │   │ │
│  │ │    USER_READ, DASHBOARD_VIEW, KPI_VIEW...                │   │ │
│  │ │                                                           │   │ │
│  │ │ [Manage Permissions]                                      │   │ │
│  │ └─────────────────────────────────────────────────────────┘   │ │
│  │                                                                 │ │
│  │ ┌─────────────────────────────────────────────────────────┐   │ │
│  │ │ 📊 ANALISTA                                 [Edit] [Del] │   │ │
│  │ │                                                           │   │ │
│  │ │ Acceso estándar para análisis y consultas                │   │ │
│  │ │                                                           │   │ │
│  │ │ 👥 45 users                                               │   │ │
│  │ │ 🔑 4 permissions:                                         │   │ │
│  │ │    DASHBOARD_VIEW, KPI_VIEW, REPORT_VIEW...              │   │ │
│  │ │                                                           │   │ │
│  │ │ [Manage Permissions]                                      │   │ │
│  │ └─────────────────────────────────────────────────────────┘   │ │
│  └───────────────────────────────────────────────────────────────┘ │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2. Estados Visuales

**Estado: Loading**
```typescript
<RoleCardSkeleton count={3} />
```

**Estado: Empty**
```typescript
<EmptyState
  icon="👑"
  title="No roles found"
  description="Create your first role to get started"
  action={<Button>Create Role</Button>}
/>
```

**Estado: Error (Delete con usuarios asignados)**
```typescript
<AlertDialog
  title="Cannot Delete Role"
  description="This role is assigned to 12 users. Remove all user assignments before deleting."
  variant="error"
/>
```

---

## 3. Jerarquía de Componentes

```
<RoleManagementPage>
  ├── <PageHeader>
  │   ├── <Typography variant="h1">Roles</Typography>
  │   └── <Button>+ New Role</Button>
  ├── <SearchInput placeholder="Search roles..." />
  ├── <RoleList>
  │   └── <RoleCard> (×N)
  │       ├── <RoleHeader>
  │       │   ├── <RoleIcon />
  │       │   ├── <RoleName />
  │       │   └── <ActionMenu>
  │       ├── <RoleDescription />
  │       ├── <RoleStats>
  │       │   ├── <UserCount />
  │       │   └── <PermissionCount />
  │       ├── <PermissionBadgeList />
  │       └── <Button>Manage Permissions</Button>
  ├── <RoleFormModal isOpen={showForm} />
  └── <PermissionAssignmentModal isOpen={showPermModal} />
</RoleManagementPage>
```

---

## 4. Props y API del Componente

### 4.1. State Management

```typescript
interface RoleManagementState {
  roles: RoleWithDetails[];
  searchQuery: string;
  isLoading: boolean;
  error: string | null;
  showRoleForm: boolean;
  showPermModal: boolean;
  selectedRoleId: number | null;
  formMode: 'create' | 'edit';
}

interface RoleWithDetails {
  id: number;
  name: string;
  description?: string;
  usersCount: number;
  permissions: string[];
  createdAt: string;
  updatedAt: string;
}
```

---

## 5. Integración con Backend

### 5.1. Endpoints Consumidos

**GET /api/v1/admin/roles**
```typescript
const fetchRoles = async (search?: string): Promise<RoleListResponse> => {
  const response = await api.get('/api/v1/admin/roles', {
    params: { search, size: 100 }
  });
  return response.data;
};
```

**POST /api/v1/admin/roles**
```typescript
interface RoleRequest {
  name: string;
  description?: string;
}

const createRole = async (data: RoleRequest): Promise<RoleResponse> => {
  const response = await api.post('/api/v1/admin/roles', data);
  return response.data;
};
```

**PUT /api/v1/admin/roles/{id}**
```typescript
const updateRole = async (id: number, data: RoleRequest): Promise<RoleResponse> => {
  const response = await api.put(`/api/v1/admin/roles/${id}`, data);
  return response.data;
};
```

**DELETE /api/v1/admin/roles/{id}**
```typescript
const deleteRole = async (id: number): Promise<void> => {
  await api.delete(`/api/v1/admin/roles/${id}`);
};
```

### 5.2. React Query Hooks

```typescript
const useRoles = (search?: string) => {
  return useQuery({
    queryKey: ['roles', search],
    queryFn: () => fetchRoles(search),
    staleTime: 60_000,
  });
};

const useDeleteRole = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: deleteRole,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      toast.success('Role deleted successfully');
    },
    onError: (error: ApiError) => {
      if (error.status === 409) {
        toast.error('Cannot delete role: it is assigned to users or has permissions');
      } else {
        toast.error('Failed to delete role');
      }
    }
  });
};
```

---

## 6. Manejo de Errores

### 6.1. Casos de Error Específicos

```typescript
// Error 409: Rol en uso
try {
  await deleteRole(roleId);
} catch (error) {
  if (error.status === 409) {
    showAlert({
      title: 'Cannot Delete Role',
      description: `This role is assigned to ${role.usersCount} users. Remove all assignments first.`,
      variant: 'error'
    });
  }
}

// Error 409: Nombre duplicado
try {
  await createRole({ name: 'ADMIN' });
} catch (error) {
  if (error.status === 409) {
    setFieldError('name', 'A role with this name already exists');
  }
}
```

---

## 7. Lógica de Negocio

### 7.1. Reglas

1. **Roles Seed Protegidos**: ADMIN, GERENTE, ANALISTA no se pueden eliminar (protección en backend)
2. **Nombre Único**: Case-insensitive
3. **Eliminación Condicional**: Solo si `usersCount === 0` y no tiene permisos
4. **Permisos**: Se asignan vía modal separado (POST/DELETE en `/assignments`)

### 7.2. Validaciones del Formulario

```typescript
const roleSchema = z.object({
  name: z.string()
    .min(3, 'Name must be at least 3 characters')
    .max(50, 'Name must be less than 50 characters')
    .regex(/^[A-Z_]+$/, 'Name must be uppercase with underscores only'),
  description: z.string().max(255).optional()
});
```

---

## 8. Testing

```typescript
describe('RoleManagementPage', () => {
  it('should display all roles with counts', async () => {
    mockUseRoles.mockReturnValue({
      data: {
        content: [
          { id: 1, name: 'ADMIN', usersCount: 5, permissions: ['USER_READ', 'USER_WRITE'] },
          { id: 2, name: 'GERENTE', usersCount: 12, permissions: ['USER_READ'] }
        ]
      },
      isLoading: false
    });
    
    render(<RoleManagementPage />);
    
    expect(screen.getByText('ADMIN')).toBeInTheDocument();
    expect(screen.getByText('5 users')).toBeInTheDocument();
    expect(screen.getByText('2 permissions')).toBeInTheDocument();
  });
  
  it('should prevent deleting role with users', async () => {
    const { user } = render(<RoleManagementPage />);
    
    const deleteBtn = screen.getAllByLabelText('Delete role')[0];
    await user.click(deleteBtn);
    
    // Confirmación
    await user.click(screen.getByText('Confirm'));
    
    await waitFor(() => {
      expect(screen.getByText(/Cannot delete role/i)).toBeInTheDocument();
    });
  });
});
```

---

## 9. Checklist de Implementación

- [ ] Crear `RoleManagementPage.tsx`
- [ ] Implementar `RoleCard` component
- [ ] Implementar `RoleFormModal`
- [ ] Implementar `PermissionAssignmentModal`
- [ ] Crear hooks de React Query
- [ ] Implementar búsqueda con debounce
- [ ] Añadir confirmación de delete
- [ ] Implementar manejo de errores 409
- [ ] Tests unitarios
- [ ] Tests E2E

---

**Próximo FTV**: `ftv-permission-management-page.md` (FTV-IOC-004-003)

