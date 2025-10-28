# FTV-001: User Management Page

**ID**: FTV-IOC-004-001  
**Componente**: `UserManagementPage`  
**Tipo**: Page Component  
**Ruta**: `/admin/users`  
**Sprint**: Sprint 3 (IOC-004)  
**Technical Design**: TD-IOC-004-User-Role-Management-claude.md  
**Fecha Creación**: 2025-10-27  
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
```typescript
// Mostrar skeleton loader en tabla
<UserListSkeleton rows={20} />
```

**Estado: Empty**
```typescript
// Sin usuarios (caso raro, al menos hay ADMIN)
<EmptyState
  icon="👥"
  title="No users found"
  description="No users match your search criteria"
  action={<Button>Clear Filters</Button>}
/>
```

**Estado: Error**
```typescript
// Error al cargar usuarios
<ErrorState
  message="Failed to load users"
  retry={() => refetch()}
/>
```

### 2.3. Responsive Behavior

**Desktop (>1024px)**: Tabla completa con todas las columnas  
**Tablet (768px - 1024px)**: Tabla con columnas esenciales (Name, Email, Roles, Actions)  
**Mobile (<768px)**: Vista de cards, filtros colapsables

---

## 3. Jerarquía de Componentes

### 3.1. Árbol de Componentes

```
<UserManagementPage>
  ├── <PageHeader>
  │   ├── <Typography variant="h1">Users</Typography>
  │   └── <Button variant="primary">+ New User</Button>
  ├── <UserFilters>
  │   ├── <SearchInput placeholder="Search..." />
  │   ├── <PlantaSelect />
  │   └── <StatusSelect />
  ├── <UserList>
  │   ├── <UserTable>
  │   │   └── <UserRow> (×N)
  │   │       ├── <UserAvatar />
  │   │       ├── <RoleBadge> (×M)
  │   │       └── <ActionMenu>
  │   │           ├── <MenuItem>Edit</MenuItem>
  │   │           ├── <MenuItem>Manage Roles</MenuItem>
  │   │           └── <MenuItem>Delete</MenuItem>
  │   └── <Pagination />
  ├── <UserFormModal isOpen={showForm} />
  └── <RoleAssignmentModal isOpen={showRoleModal} />
</UserManagementPage>
```

### 3.2. Componentes Hijo Generados

- `ftv-user-list.md` (FTV-IOC-004-002)
- `ftv-user-form-modal.md` (FTV-IOC-004-003)
- `ftv-role-assignment-modal.md` (FTV-IOC-004-004)

### 3.3. Componentes Reutilizados

- `<PageHeader>` - Layout component
- `<SearchInput>` - UI component
- `<Button>` - UI component
- `<Select>` - UI component
- `<Pagination>` - UI component
- `<EmptyState>` - UI component
- `<ErrorState>` - UI component

---

## 4. Props y API del Componente

### 4.1. Props Interface

```typescript
interface UserManagementPageProps {
  // No recibe props externas (es una página/route)
}
```

### 4.2. State Management

```typescript
interface UserManagementState {
  // Datos
  users: User[];
  totalUsers: number;
  totalPages: number;
  
  // Filtros y paginación
  searchQuery: string;
  selectedPlantaId: number | null;
  activeFilter: boolean | null;
  currentPage: number;
  pageSize: number;
  
  // UI state
  isLoading: boolean;
  error: string | null;
  showUserForm: boolean;
  showRoleModal: boolean;
  selectedUserId: number | null;
  formMode: 'create' | 'edit';
}

// Usando Zustand o Context
const useUserManagementStore = create<UserManagementState>((set) => ({
  users: [],
  totalUsers: 0,
  totalPages: 0,
  searchQuery: '',
  selectedPlantaId: null,
  activeFilter: null,
  currentPage: 0,
  pageSize: 20,
  isLoading: false,
  error: null,
  showUserForm: false,
  showRoleModal: false,
  selectedUserId: null,
  formMode: 'create',
  
  // Actions
  setSearchQuery: (query) => set({ searchQuery: query, currentPage: 0 }),
  setPlantaFilter: (plantaId) => set({ selectedPlantaId: plantaId, currentPage: 0 }),
  setPage: (page) => set({ currentPage: page }),
  openUserForm: (mode, userId?) => set({ 
    showUserForm: true, 
    formMode: mode, 
    selectedUserId: userId ?? null 
  }),
  closeUserForm: () => set({ showUserForm: false, selectedUserId: null }),
  openRoleModal: (userId) => set({ showRoleModal: true, selectedUserId: userId }),
  closeRoleModal: () => set({ showRoleModal: false, selectedUserId: null }),
}));
```

---

## 5. Lógica de Negocio y Reglas

### 5.1. Reglas de Negocio

1. **Acceso Restringido**: Solo usuarios con `ROLE_ADMIN` pueden acceder
2. **Soft Delete**: Delete no elimina físicamente, establece `deleted_at`
3. **Búsqueda**: Case-insensitive, busca en `email`, `primer_nombre`, `primer_apellido`
4. **Paginación**: Máximo 100 usuarios por página (backend clamp)
5. **Validación Email**: Debe ser único en el sistema

### 5.2. Validaciones

```typescript
// Validación de permisos en route guard
const RequireAdmin: React.FC<{children: React.ReactNode}> = ({children}) => {
  const { user } = useAuth();
  
  if (!user?.roles.includes('ADMIN')) {
    return <Navigate to="/unauthorized" />;
  }
  
  return <>{children}</>;
};

// En App.tsx
<Route path="/admin/users" element={
  <RequireAdmin>
    <UserManagementPage />
  </RequireAdmin>
} />
```

### 5.3. Transformaciones de Datos

```typescript
// Transform backend response to UI model
function mapUserResponse(dto: UsuarioResponse): User {
  return {
    id: dto.id,
    email: dto.email,
    fullName: dto.fullName,
    planta: dto.plantaId ? {
      id: dto.plantaId,
      code: dto.plantaCode!,
      name: dto.plantaName!
    } : null,
    centroCosto: dto.centroCosto,
    fechaContrato: dto.fechaContrato ? new Date(dto.fechaContrato) : null,
    isActive: dto.isActive,
    roles: dto.roles,
    createdAt: new Date(dto.createdAt),
    updatedAt: new Date(dto.updatedAt)
  };
}
```

---

## 6. Interacciones de Usuario

### 6.1. Eventos de Usuario

```typescript
interface UserInteractions {
  // Búsqueda y filtros
  onSearchChange: (query: string) => void;
  onPlantaFilterChange: (plantaId: number | null) => void;
  onStatusFilterChange: (isActive: boolean | null) => void;
  onRefresh: () => void;
  
  // Paginación
  onPageChange: (page: number) => void;
  
  // Acciones CRUD
  onCreateUser: () => void;
  onEditUser: (userId: number) => void;
  onDeleteUser: (userId: number) => void;
  onManageRoles: (userId: number) => void;
}
```

### 6.2. Flujos de Interacción

**Flujo: Crear Usuario**
```
1. Click "New User" → Abre UserFormModal (mode: create)
2. Completa formulario → Valida campos
3. Submit → POST /api/v1/admin/users
4. Success → Cierra modal, refresh lista, toast success
5. Error → Muestra error en modal, mantiene datos
```

**Flujo: Editar Usuario**
```
1. Click "Edit" en ActionMenu → Carga datos del usuario
2. Abre UserFormModal (mode: edit, prefilled)
3. Modifica campos → Valida
4. Submit → PUT /api/v1/admin/users/{id}
5. Success → Actualiza en lista, cierra modal, toast
```

**Flujo: Eliminar Usuario**
```
1. Click "Delete" → Muestra confirmación
2. Confirma → DELETE /api/v1/admin/users/{id}
3. Success → Remueve de lista, toast "User deleted"
4. Error → Toast error
```

**Flujo: Asignar Roles**
```
1. Click "Manage Roles" → Abre RoleAssignmentModal
2. Muestra roles disponibles y asignados
3. Toggle roles → Optimistic update en UI
4. Cada cambio → POST/DELETE /api/v1/admin/assignments/users/{id}/roles/{roleId}
5. Error → Rollback optimistic, toast error
```

### 6.3. Atajos de Teclado

```typescript
const keyboardShortcuts = {
  'Ctrl+K': 'Focus search input',
  'Ctrl+N': 'Open new user form',
  'Ctrl+R': 'Refresh list',
  'Escape': 'Close active modal'
};
```

---

## 7. Integración con Backend

### 7.1. Endpoints Consumidos

**GET /api/v1/admin/users**
```typescript
const fetchUsers = async (params: {
  search?: string;
  plantaId?: number;
  isActive?: boolean;
  page: number;
  size: number;
}): Promise<UserListResponse> => {
  const response = await api.get('/api/v1/admin/users', { params });
  return response.data;
};
```

**POST /api/v1/admin/users**
```typescript
const createUser = async (data: UsuarioCreateRequest): Promise<UsuarioResponse> => {
  const response = await api.post('/api/v1/admin/users', data);
  return response.data;
};
```

**PUT /api/v1/admin/users/{id}**
```typescript
const updateUser = async (id: number, data: UsuarioUpdateRequest): Promise<UsuarioResponse> => {
  const response = await api.put(`/api/v1/admin/users/${id}`, data);
  return response.data;
};
```

**DELETE /api/v1/admin/users/{id}**
```typescript
const deleteUser = async (id: number): Promise<void> => {
  await api.delete(`/api/v1/admin/users/${id}`);
};
```

### 7.2. Estrategia de Carga de Datos

```typescript
// Using React Query
const useUsers = (filters: UserFilters) => {
  return useQuery({
    queryKey: ['users', filters],
    queryFn: () => fetchUsers(filters),
    staleTime: 30_000, // 30 segundos
    refetchOnWindowFocus: true
  });
};

// Mutations
const useCreateUser = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: createUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      toast.success('User created successfully');
    },
    onError: (error: ApiError) => {
      toast.error(error.message);
    }
  });
};
```

### 7.3. Optimistic Updates

```typescript
const useAssignRole = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ userId, roleId }: AssignRoleParams) => 
      assignRoleToUser(userId, roleId),
    
    // Optimistic update
    onMutate: async ({ userId, roleId }) => {
      await queryClient.cancelQueries({ queryKey: ['users'] });
      
      const previousUsers = queryClient.getQueryData<UserListResponse>(['users']);
      
      // Actualizar cache optimistamente
      queryClient.setQueryData<UserListResponse>(['users'], (old) => {
        if (!old) return old;
        return {
          ...old,
          content: old.content.map(user => 
            user.id === userId 
              ? { ...user, roles: [...user.roles, getRoleName(roleId)] }
              : user
          )
        };
      });
      
      return { previousUsers };
    },
    
    // Rollback en error
    onError: (err, variables, context) => {
      if (context?.previousUsers) {
        queryClient.setQueryData(['users'], context.previousUsers);
      }
      toast.error('Failed to assign role');
    },
    
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
    }
  });
};
```

---

## 8. Manejo de Errores

### 8.1. Estrategia de Manejo de Errores

```typescript
// Error types from backend
interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

// Error handler
const handleApiError = (error: ApiError): string => {
  const errorMessages: Record<number, string> = {
    400: 'Invalid data provided',
    401: 'You are not authenticated',
    403: 'You do not have permission to perform this action',
    404: 'User not found',
    409: 'Email or Supabase ID already exists',
    429: 'Too many requests, please try again later',
    500: 'An unexpected error occurred'
  };
  
  return errorMessages[error.status] || error.message;
};
```

### 8.2. Casos de Error Específicos

```typescript
// Error al crear usuario con email duplicado (409)
try {
  await createUser(userData);
} catch (error) {
  if (error.status === 409) {
    // Mostrar error en el campo email del formulario
    setFieldError('email', 'This email is already registered');
  }
}

// Error de autenticación (401)
if (error.status === 401) {
  // Redirect to login
  navigate('/login');
}

// Error de permisos (403)
if (error.status === 403) {
  toast.error('You need administrator privileges');
  navigate('/dashboard');
}
```

---

## 9. Testing

### 9.1. Tests Unitarios

```typescript
describe('UserManagementPage', () => {
  it('should render page header with title', () => {
    render(<UserManagementPage />);
    expect(screen.getByText('Users')).toBeInTheDocument();
  });
  
  it('should show loading state while fetching users', () => {
    mockUseUsers.mockReturnValue({ isLoading: true });
    render(<UserManagementPage />);
    expect(screen.getByTestId('user-list-skeleton')).toBeInTheDocument();
  });
  
  it('should filter users by search query', async () => {
    render(<UserManagementPage />);
    const searchInput = screen.getByPlaceholderText('Search...');
    
    fireEvent.change(searchInput, { target: { value: 'Juan' } });
    
    await waitFor(() => {
      expect(mockFetchUsers).toHaveBeenCalledWith(
        expect.objectContaining({ search: 'Juan' })
      );
    });
  });
  
  it('should open create user modal on button click', () => {
    render(<UserManagementPage />);
    const newUserBtn = screen.getByText('+ New User');
    
    fireEvent.click(newUserBtn);
    
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByText('Create User')).toBeInTheDocument();
  });
});
```

### 9.2. Tests de Integración

```typescript
describe('UserManagementPage Integration', () => {
  it('should create a new user end-to-end', async () => {
    // Setup
    const { user } = renderWithAuth(<UserManagementPage />);
    
    // Click new user
    await user.click(screen.getByText('+ New User'));
    
    // Fill form
    await user.type(screen.getByLabelText('Email'), 'nuevo@ioc.com');
    await user.type(screen.getByLabelText('First Name'), 'Nuevo');
    await user.type(screen.getByLabelText('Last Name'), 'Usuario');
    
    // Submit
    await user.click(screen.getByText('Create'));
    
    // Verify API call
    await waitFor(() => {
      expect(mockCreateUser).toHaveBeenCalledWith(
        expect.objectContaining({
          email: 'nuevo@ioc.com',
          primerNombre: 'Nuevo',
          primerApellido: 'Usuario'
        })
      );
    });
    
    // Verify success message
    expect(screen.getByText('User created successfully')).toBeInTheDocument();
  });
});
```

### 9.3. Tests E2E (Playwright)

```typescript
test('Admin can manage users', async ({ page }) => {
  await page.goto('/admin/users');
  
  // Verify page loaded
  await expect(page.locator('h1')).toHaveText('Users');
  
  // Search for user
  await page.fill('input[placeholder*="Search"]', 'Juan');
  await page.waitForResponse(/\/api\/v1\/admin\/users/);
  
  // Verify filtered results
  const rows = page.locator('table tbody tr');
  await expect(rows).toContainText('Juan');
  
  // Create new user
  await page.click('text=New User');
  await page.fill('[name="email"]', 'test@ioc.com');
  await page.fill('[name="primerNombre"]', 'Test');
  await page.fill('[name="primerApellido"]', 'User');
  await page.click('button:has-text("Create")');
  
  // Verify success
  await expect(page.locator('.toast')).toContainText('created successfully');
});
```

---

## 10. Accesibilidad (a11y)

### 10.1. Requisitos WCAG 2.1 AA

```typescript
// ARIA labels
<SearchInput
  aria-label="Search users by name or email"
  placeholder="Search..."
/>

<Button
  aria-label="Create new user"
  onClick={onCreateUser}
>
  + New User
</Button>

// Table accessibility
<table role="table" aria-label="Users list">
  <thead>
    <tr role="row">
      <th role="columnheader" scope="col">Name</th>
      <th role="columnheader" scope="col">Email</th>
      {/* ... */}
    </tr>
  </thead>
  <tbody>
    {users.map(user => (
      <tr key={user.id} role="row">
        <td role="cell">{user.fullName}</td>
        <td role="cell">{user.email}</td>
        {/* ... */}
      </tr>
    ))}
  </tbody>
</table>
```

### 10.2. Navegación por Teclado

- Tab order: Search → Filters → Table → Pagination → Actions
- Enter/Space: Activar botones y abrir modals
- Escape: Cerrar modals
- Arrow keys: Navegar por tabla

### 10.3. Screen Reader Support

```typescript
// Announce loading state
<div role="status" aria-live="polite" aria-atomic="true">
  {isLoading && 'Loading users...'}
</div>

// Announce actions
<button
  aria-label={`Delete user ${user.fullName}`}
  onClick={() => onDelete(user.id)}
>
  🗑️
</button>
```

---

## 11. Performance

### 11.1. Optimizaciones

```typescript
// Virtualized table for large datasets
import { useVirtualizer } from '@tanstack/react-virtual';

const rowVirtualizer = useVirtualizer({
  count: users.length,
  getScrollElement: () => parentRef.current,
  estimateSize: () => 60,
  overscan: 5
});

// Debounced search
const debouncedSearch = useMemo(
  () => debounce((query: string) => {
    setSearchQuery(query);
  }, 300),
  []
);
```

### 11.2. Métricas de Performance

- **LCP**: < 2.5s (First meaningful paint de la tabla)
- **FID**: < 100ms (Interacción con search input)
- **CLS**: < 0.1 (Sin layout shifts en carga)

---

## 12. Checklist de Implementación

### Fase 1: Estructura Básica
- [ ] Crear componente `UserManagementPage.tsx`
- [ ] Configurar ruta en React Router
- [ ] Implementar route guard `RequireAdmin`
- [ ] Crear layout básico con header y search

### Fase 2: Lista de Usuarios
- [ ] Implementar `useUsers` hook con React Query
- [ ] Crear `UserTable` component
- [ ] Implementar paginación
- [ ] Añadir filtros (planta, status)
- [ ] Implementar búsqueda con debounce

### Fase 3: CRUD Operations
- [ ] Implementar `UserFormModal` (create/edit)
- [ ] Implementar `useCreateUser` mutation
- [ ] Implementar `useUpdateUser` mutation
- [ ] Implementar `useDeleteUser` mutation
- [ ] Añadir confirmación de delete

### Fase 4: Role Management
- [ ] Implementar `RoleAssignmentModal`
- [ ] Implementar `useAssignRole` mutation
- [ ] Implementar `useRemoveRole` mutation
- [ ] Añadir optimistic updates

### Fase 5: Error Handling & UX
- [ ] Implementar manejo de errores global
- [ ] Añadir toasts de éxito/error
- [ ] Implementar loading states
- [ ] Implementar empty states

### Fase 6: Testing
- [ ] Unit tests (componente, hooks)
- [ ] Integration tests
- [ ] E2E tests (Playwright)
- [ ] Accessibility audit

### Fase 7: Polish
- [ ] Responsive design
- [ ] Keyboard shortcuts
- [ ] Performance optimizations
- [ ] Documentation

---

## 13. Archivos a Crear

```
src/
├── pages/
│   └── admin/
│       └── UserManagementPage.tsx          ← ESTE COMPONENTE
├── components/
│   └── users/
│       ├── UserList.tsx
│       ├── UserTable.tsx
│       ├── UserRow.tsx
│       ├── UserFormModal.tsx
│       ├── RoleAssignmentModal.tsx
│       └── UserFilters.tsx
├── hooks/
│   └── users/
│       ├── useUsers.ts
│       ├── useCreateUser.ts
│       ├── useUpdateUser.ts
│       ├── useDeleteUser.ts
│       └── useAssignRole.ts
├── services/
│   └── api/
│       └── usersApi.ts
└── types/
    └── user.types.ts
```

---

**Próximo FTV**: `ftv-user-list.md` (FTV-IOC-004-002)

