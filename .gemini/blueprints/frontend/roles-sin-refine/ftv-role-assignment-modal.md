# FTV-005: Role Assignment Modal

**ID**: FTV-IOC-004-005  
**Componente**: `RoleAssignmentModal`  
**Tipo**: Feature Component (Modal)  
**Padre**: `UserManagementPage`  
**Sprint**: Sprint 3 (IOC-004)  
**Technical Design**: TD-IOC-004-User-Role-Management-claude.md  
**Fecha Creación**: 2025-10-27  
**Estado**: ✅ Ready for Implementation

---

## 1. Propósito y Contexto

### 1.1. Propósito
Modal que permite asignar/remover múltiples roles a un usuario específico con actualización optimista y feedback instantáneo.

### 1.2. Caso de Uso Principal
**Actor**: Administrador (ROLE_ADMIN)

**Flujo**:
1. Admin click "Manage Roles" en UserRow
2. Modal muestra usuario y roles actuales
3. Admin toggle roles (on/off)
4. Cada cambio se aplica inmediatamente (optimistic update)
5. Backend confirma o rollback si error
6. Admin cierra modal cuando termina

---

## 2. Especificación Visual

### 2.1. Wireframe ASCII

```
┌────────────────────────────────────────────────────────────┐
│ Manage Roles - Juan Pérez                           [×]   │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  👤 Juan Pérez (juan@ioc.com)                             │
│                                                            │
│  Available Roles                                           │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                            │
│  ┌────────────────────────────────────────────────────┐   │
│  │ ☑ ADMIN                                            │   │
│  │                                                     │   │
│  │   Acceso administrativo total al sistema           │   │
│  │   13 permissions                                    │   │
│  └────────────────────────────────────────────────────┘   │
│                                                            │
│  ┌────────────────────────────────────────────────────┐   │
│  │ ☐ GERENTE                                          │   │
│  │                                                     │   │
│  │   Gestión operativa y supervisión                  │   │
│  │   10 permissions                                    │   │
│  └────────────────────────────────────────────────────┘   │
│                                                            │
│  ┌────────────────────────────────────────────────────┐   │
│  │ ☑ ANALISTA                                         │   │
│  │                                                     │   │
│  │   Acceso estándar para análisis y consultas        │   │
│  │   4 permissions                                     │   │
│  └────────────────────────────────────────────────────┘   │
│                                                            │
│  ℹ️  Changes are applied immediately                      │
│                                                            │
├────────────────────────────────────────────────────────────┤
│                                                  [Close]  │
└────────────────────────────────────────────────────────────┘
```

### 2.2. Estados Visuales

**Estado: Loading Initial Data**
```typescript
<Dialog>
  <Skeleton height={60} count={3} />
</Dialog>
```

**Estado: Optimistic Update (Toggle ON)**
```typescript
<RoleCard checked loading>
  <Spinner size="small" />
  GERENTE
</RoleCard>
```

**Estado: Error (Rollback)**
```typescript
<Toast variant="error">
  Failed to assign GERENTE role
</Toast>
// Checkbox vuelve a estado anterior
```

---

## 3. Props y API del Componente

### 3.1. Props Interface

```typescript
interface RoleAssignmentModalProps {
  isOpen: boolean;
  userId: number;
  userName: string;
  userEmail: string;
  onClose: () => void;
}
```

### 3.2. State Management

```typescript
interface RoleAssignmentState {
  availableRoles: Role[];
  assignedRoleIds: Set<number>;
  loadingRoles: Set<number>;        // Roles en proceso de asignación/remoción
  error: string | null;
}

interface Role {
  id: number;
  name: string;
  description?: string;
  permissionsCount: number;
}
```

---

## 4. Lógica de Negocio

### 4.1. Cargar Datos Iniciales

```typescript
const useRoleAssignment = (userId: number, isOpen: boolean) => {
  // Cargar roles disponibles
  const { data: roles } = useQuery({
    queryKey: ['roles'],
    queryFn: fetchRoles,
    enabled: isOpen
  });
  
  // Cargar roles actuales del usuario
  const { data: user } = useQuery({
    queryKey: ['user', userId],
    queryFn: () => fetchUserById(userId),
    enabled: isOpen
  });
  
  const assignedRoleIds = useMemo(() => {
    if (!user || !roles) return new Set<number>();
    
    return new Set(
      roles.content
        .filter(role => user.roles.includes(role.name))
        .map(role => role.id)
    );
  }, [user, roles]);
  
  return { roles: roles?.content || [], assignedRoleIds };
};
```

### 4.2. Toggle Role (Optimistic Update)

```typescript
const useToggleRole = (userId: number) => {
  const queryClient = useQueryClient();
  const [loadingRoles, setLoadingRoles] = useState(new Set<number>());
  
  const assignRole = useMutation({
    mutationFn: ({ roleId }: { roleId: number }) => 
      assignRoleToUser(userId, roleId),
    
    onMutate: async ({ roleId }) => {
      // Marcar como loading
      setLoadingRoles(prev => new Set(prev).add(roleId));
      
      // Cancelar queries pendientes
      await queryClient.cancelQueries({ queryKey: ['user', userId] });
      
      // Snapshot del estado anterior
      const previousUser = queryClient.getQueryData(['user', userId]);
      
      // Actualizar cache optimistamente
      queryClient.setQueryData(['user', userId], (old: any) => ({
        ...old,
        roles: [...old.roles, getRoleName(roleId)]
      }));
      
      return { previousUser };
    },
    
    onError: (err, { roleId }, context) => {
      // Rollback
      if (context?.previousUser) {
        queryClient.setQueryData(['user', userId], context.previousUser);
      }
      toast.error(`Failed to assign ${getRoleName(roleId)}`);
    },
    
    onSettled: (data, error, { roleId }) => {
      setLoadingRoles(prev => {
        const next = new Set(prev);
        next.delete(roleId);
        return next;
      });
      queryClient.invalidateQueries({ queryKey: ['user', userId] });
    }
  });
  
  const removeRole = useMutation({
    mutationFn: ({ roleId }: { roleId: number }) => 
      removeRoleFromUser(userId, roleId),
    
    onMutate: async ({ roleId }) => {
      setLoadingRoles(prev => new Set(prev).add(roleId));
      await queryClient.cancelQueries({ queryKey: ['user', userId] });
      
      const previousUser = queryClient.getQueryData(['user', userId]);
      
      queryClient.setQueryData(['user', userId], (old: any) => ({
        ...old,
        roles: old.roles.filter((r: string) => r !== getRoleName(roleId))
      }));
      
      return { previousUser };
    },
    
    onError: (err, { roleId }, context) => {
      if (context?.previousUser) {
        queryClient.setQueryData(['user', userId], context.previousUser);
      }
      toast.error(`Failed to remove ${getRoleName(roleId)}`);
    },
    
    onSettled: (data, error, { roleId }) => {
      setLoadingRoles(prev => {
        const next = new Set(prev);
        next.delete(roleId);
        return next;
      });
      queryClient.invalidateQueries({ queryKey: ['user', userId] });
    }
  });
  
  const toggleRole = (roleId: number, isAssigned: boolean) => {
    if (isAssigned) {
      removeRole.mutate({ roleId });
    } else {
      assignRole.mutate({ roleId });
    }
  };
  
  return { toggleRole, loadingRoles };
};
```

---

## 5. Integración con Backend

### 5.1. Endpoints Consumidos

**POST /api/v1/admin/assignments/users/{userId}/roles/{roleId}**
```typescript
const assignRoleToUser = async (userId: number, roleId: number): Promise<void> => {
  await api.post(`/api/v1/admin/assignments/users/${userId}/roles/${roleId}`);
};
```

**DELETE /api/v1/admin/assignments/users/{userId}/roles/{roleId}**
```typescript
const removeRoleFromUser = async (userId: number, roleId: number): Promise<void> => {
  await api.delete(`/api/v1/admin/assignments/users/${userId}/roles/${roleId}`);
};
```

### 5.2. Idempotencia

Los endpoints son idempotentes (backend no arroja error si la asignación ya existe o no existe), por lo que podemos hacer múltiples clicks sin problemas.

---

## 6. Interacciones de Usuario

### 6.1. Flujo Principal

```
1. Modal abre → Carga roles y asignaciones actuales
2. Usuario ve lista de roles con checkboxes
3. Click en checkbox → Toggle optimista inmediato
4. Backend procesa → Éxito: mantiene estado
5. Backend falla → Rollback + toast error
6. Usuario puede hacer múltiples cambios sin esperar
7. Click "Close" → Modal cierra (cambios ya aplicados)
```

### 6.2. Manejo de Múltiples Clicks Rápidos

```typescript
// Prevenir múltiples requests simultáneos para el mismo role
const toggleRole = (roleId: number, isAssigned: boolean) => {
  if (loadingRoles.has(roleId)) {
    // Ya está procesando, ignorar
    return;
  }
  
  // Proceder con toggle
  if (isAssigned) {
    removeRole.mutate({ roleId });
  } else {
    assignRole.mutate({ roleId });
  }
};
```

---

## 7. Testing

```typescript
describe('RoleAssignmentModal', () => {
  it('should show currently assigned roles as checked', async () => {
    mockFetchUser.mockResolvedValue({
      id: 1,
      roles: ['ADMIN', 'ANALISTA']
    });
    
    mockFetchRoles.mockResolvedValue({
      content: [
        { id: 1, name: 'ADMIN' },
        { id: 2, name: 'GERENTE' },
        { id: 3, name: 'ANALISTA' }
      ]
    });
    
    render(<RoleAssignmentModal isOpen userId={1} userName="Juan" userEmail="juan@ioc.com" onClose={jest.fn()} />);
    
    await waitFor(() => {
      const adminCheckbox = screen.getByLabelText('ADMIN');
      const gerenteCheckbox = screen.getByLabelText('GERENTE');
      const analistaCheckbox = screen.getByLabelText('ANALISTA');
      
      expect(adminCheckbox).toBeChecked();
      expect(gerenteCheckbox).not.toBeChecked();
      expect(analistaCheckbox).toBeChecked();
    });
  });
  
  it('should optimistically update on role toggle', async () => {
    const { user } = render(<RoleAssignmentModal isOpen userId={1} userName="Juan" userEmail="juan@ioc.com" onClose={jest.fn()} />);
    
    const gerenteCheckbox = screen.getByLabelText('GERENTE');
    expect(gerenteCheckbox).not.toBeChecked();
    
    await user.click(gerenteCheckbox);
    
    // Debe marcarse inmediatamente (optimistic)
    expect(gerenteCheckbox).toBeChecked();
    
    // Debe mostrar loading state
    expect(screen.getByTestId('role-2-loading')).toBeInTheDocument();
    
    // Esperar confirmación del backend
    await waitFor(() => {
      expect(mockAssignRole).toHaveBeenCalledWith(1, 2);
    });
  });
  
  it('should rollback on error', async () => {
    mockAssignRole.mockRejectedValue(new Error('Network error'));
    
    const { user } = render(<RoleAssignmentModal isOpen userId={1} userName="Juan" userEmail="juan@ioc.com" onClose={jest.fn()} />);
    
    const gerenteCheckbox = screen.getByLabelText('GERENTE');
    await user.click(gerenteCheckbox);
    
    // Esperar que el error dispare rollback
    await waitFor(() => {
      expect(gerenteCheckbox).not.toBeChecked();
      expect(screen.getByText(/Failed to assign GERENTE/i)).toBeInTheDocument();
    });
  });
});
```

---

## 8. Performance

### 8.1. Optimizaciones

```typescript
// Debounce múltiples toggles del mismo role
const debouncedToggle = useMemo(
  () => debounce((roleId: number, isAssigned: boolean) => {
    toggleRole(roleId, isAssigned);
  }, 150),
  [toggleRole]
);

// Memoizar lista de roles para evitar re-renders
const RoleCardMemo = React.memo(RoleCard);
```

---

## 9. Accesibilidad

```typescript
<Dialog
  open={isOpen}
  onClose={onClose}
  aria-labelledby="role-assignment-title"
>
  <DialogTitle id="role-assignment-title">
    Manage Roles - {userName}
  </DialogTitle>
  
  <div role="group" aria-label="Available roles">
    {roles.map(role => (
      <FormGroup key={role.id}>
        <Checkbox
          id={`role-${role.id}`}
          checked={assignedRoleIds.has(role.id)}
          onChange={() => handleToggle(role.id)}
          disabled={loadingRoles.has(role.id)}
          aria-busy={loadingRoles.has(role.id)}
          aria-label={`${role.name} role`}
        />
        <Label htmlFor={`role-${role.id}`}>
          <strong>{role.name}</strong>
          <span>{role.description}</span>
          <span aria-label={`${role.permissionsCount} permissions`}>
            🔑 {role.permissionsCount} permissions
          </span>
        </Label>
      </FormGroup>
    ))}
  </div>
  
  <DialogActions>
    <Button onClick={onClose}>Close</Button>
  </DialogActions>
</Dialog>
```

---

## 10. Checklist de Implementación

- [ ] Crear `RoleAssignmentModal.tsx`
- [ ] Implementar carga de roles y asignaciones actuales
- [ ] Implementar `useToggleRole` con optimistic updates
- [ ] Crear `RoleCard` component con checkbox
- [ ] Implementar loading state por role
- [ ] Implementar rollback en error
- [ ] Añadir toasts de feedback
- [ ] Prevenir múltiples clicks simultáneos
- [ ] Tests de optimistic updates
- [ ] Tests de rollback
- [ ] Tests de accesibilidad
- [ ] Performance optimization (memo, debounce)

---

## 11. Archivos a Crear

```
src/
├── components/
│   └── users/
│       ├── RoleAssignmentModal.tsx      ← ESTE COMPONENTE
│       └── RoleCard.tsx
└── hooks/
    └── roles/
        ├── useToggleRole.ts
        └── useRoleAssignment.ts
```

---

**FTV Completado** - Este es el último componente modal del flujo IOC-004.

