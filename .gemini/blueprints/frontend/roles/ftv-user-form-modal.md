# FTV-004: User Form Modal

**ID**: FTV-IOC-004-004  
**Componente**: `UserFormModal`  
**Tipo**: Feature Component (Modal)  
**Padre**: `UserManagementPage`  
**Sprint**: Sprint 3 (IOC-004)  
**Technical Design**: TD-IOC-004-User-Role-Management-claude.md  
**Fecha Creación**: 2025-10-27  
**Estado**: ✅ Ready for Implementation

---

## 1. Propósito y Contexto

### 1.1. Propósito
Modal que permite crear un nuevo usuario o editar uno existente con validación en tiempo real y manejo de errores específicos del backend.

### 1.2. Casos de Uso

**Modo CREATE**:
1. Admin click "New User" → Modal se abre vacío
2. Completa formulario (todos los campos requeridos)
3. Submit → POST /api/v1/admin/users
4. Success → Cierra modal, refresh lista, toast

**Modo EDIT**:
1. Admin click "Edit" en UserRow → Modal se abre con datos precargados
2. Modifica campos (email y supabaseUserId no editables)
3. Submit → PUT /api/v1/admin/users/{id}
4. Success → Actualiza lista, cierra modal

---

## 2. Especificación Visual

### 2.1. Wireframe ASCII (Modo CREATE)

```
┌────────────────────────────────────────────────────────────┐
│ Create User                                          [×]   │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  Email *                                                   │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ user@example.com                                     │ │
│  └──────────────────────────────────────────────────────┘ │
│  ℹ️  Must be unique in the system                         │
│                                                            │
│  Supabase User ID *                                        │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ 123e4567-e89b-12d3-a456-426614174000                 │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  First Name *                                              │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ Juan                                                  │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  Second Name                                               │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ Carlos                                                │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  Last Name *                                               │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ Pérez                                                 │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  Second Last Name                                          │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ García                                                │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  Plant                                                     │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ Lima ▾                                                │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  Cost Center                                               │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ CC-001                                                │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  Contract Date                                             │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ 2025-01-15                              📅            │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  Roles (optional)                                          │
│  ☐ ADMIN    ☐ GERENTE    ☑ ANALISTA                      │
│                                                            │
├────────────────────────────────────────────────────────────┤
│                                       [Cancel]  [Create]  │
└────────────────────────────────────────────────────────────┘
```

### 2.2. Estados Visuales

**Estado: Loading (Submitting)**
```typescript
<Button disabled loading>
  Creating...
</Button>
```

**Estado: Error en campo específico**
```typescript
<FormField error="This email is already registered">
  <Input value={email} />
</FormField>
```

**Estado: Success**
```typescript
// Modal se cierra automáticamente
// Toast aparece: "User created successfully"
```

---

## 3. Props y API del Componente

### 3.1. Props Interface

```typescript
interface UserFormModalProps {
  isOpen: boolean;
  mode: 'create' | 'edit';
  userId?: number;                    // Required en modo edit
  onClose: () => void;
  onSuccess?: (user: UsuarioResponse) => void;
}
```

### 3.2. Form State

```typescript
interface UserFormData {
  email: string;
  supabaseUserId: string;
  primerNombre: string;
  segundoNombre?: string;
  primerApellido: string;
  segundoApellido?: string;
  plantaId?: number;
  centroCosto?: string;
  fechaContrato?: Date;
  roles?: string[];
}

// Usando React Hook Form
const {
  register,
  handleSubmit,
  formState: { errors, isSubmitting },
  reset,
  setError
} = useForm<UserFormData>({
  resolver: zodResolver(userFormSchema)
});
```

---

## 4. Validaciones

### 4.1. Schema de Validación

```typescript
const userFormSchema = z.object({
  email: z.string()
    .email('Invalid email format')
    .min(5, 'Email is too short')
    .max(255, 'Email is too long'),
  
  supabaseUserId: z.string()
    .uuid('Must be a valid UUID')
    .refine(async (val) => {
      // Validación async: verificar si UUID ya existe (solo en create)
      if (mode === 'create') {
        const exists = await checkSupabaseIdExists(val);
        return !exists;
      }
      return true;
    }, 'This Supabase ID is already registered'),
  
  primerNombre: z.string()
    .min(2, 'First name must be at least 2 characters')
    .max(100, 'First name is too long'),
  
  segundoNombre: z.string().max(100).optional(),
  
  primerApellido: z.string()
    .min(2, 'Last name must be at least 2 characters')
    .max(100, 'Last name is too long'),
  
  segundoApellido: z.string().max(100).optional(),
  
  plantaId: z.number().positive().optional(),
  
  centroCosto: z.string().max(50).optional(),
  
  fechaContrato: z.date().optional(),
  
  roles: z.array(z.string()).optional()
});
```

### 4.2. Validación en Tiempo Real

```typescript
// Validar email mientras el usuario escribe (debounced)
const debouncedEmailCheck = useMemo(
  () => debounce(async (email: string) => {
    if (mode === 'edit') return; // No validar en edit
    
    try {
      const exists = await checkEmailExists(email);
      if (exists) {
        setError('email', {
          type: 'manual',
          message: 'This email is already registered'
        });
      }
    } catch (err) {
      // Silently fail, backend will validate on submit
    }
  }, 500),
  [mode]
);

useEffect(() => {
  const email = watch('email');
  if (email && email.includes('@')) {
    debouncedEmailCheck(email);
  }
}, [watch('email')]);
```

---

## 5. Lógica de Negocio

### 5.1. Cargar Datos en Modo Edit

```typescript
const { data: userData, isLoading: isLoadingUser } = useQuery({
  queryKey: ['user', userId],
  queryFn: () => fetchUserById(userId!),
  enabled: mode === 'edit' && !!userId,
  onSuccess: (data) => {
    reset({
      email: data.email,
      supabaseUserId: data.supabaseUserId,
      primerNombre: data.primerNombre,
      segundoNombre: data.segundoNombre,
      primerApellido: data.primerApellido,
      segundoApellido: data.segundoApellido,
      plantaId: data.plantaId,
      centroCosto: data.centroCosto,
      fechaContrato: data.fechaContrato ? new Date(data.fechaContrato) : undefined,
      roles: data.roles
    });
  }
});
```

### 5.2. Submit Handler

```typescript
const onSubmit = async (data: UserFormData) => {
  try {
    if (mode === 'create') {
      const newUser = await createUser({
        ...data,
        fechaContrato: data.fechaContrato?.toISOString().split('T')[0]
      });
      toast.success('User created successfully');
      onSuccess?.(newUser);
    } else {
      const updatedUser = await updateUser(userId!, {
        primerNombre: data.primerNombre,
        segundoNombre: data.segundoNombre,
        primerApellido: data.primerApellido,
        segundoApellido: data.segundoApellido,
        plantaId: data.plantaId,
        centroCosto: data.centroCosto,
        fechaContrato: data.fechaContrato?.toISOString().split('T')[0],
        isActive: true
      });
      toast.success('User updated successfully');
      onSuccess?.(updatedUser);
    }
    
    onClose();
  } catch (error: any) {
    handleSubmitError(error);
  }
};
```

### 5.3. Manejo de Errores del Backend

```typescript
const handleSubmitError = (error: ApiError) => {
  if (error.status === 409) {
    // Conflicto: email o supabaseUserId duplicado
    if (error.message.includes('email')) {
      setError('email', {
        type: 'manual',
        message: 'This email is already registered'
      });
    } else if (error.message.includes('supabase')) {
      setError('supabaseUserId', {
        type: 'manual',
        message: 'This Supabase ID is already registered'
      });
    }
  } else if (error.status === 400) {
    // Errores de validación genéricos
    toast.error(error.message);
  } else {
    toast.error('Failed to save user. Please try again.');
  }
};
```

---

## 6. Interacciones de Usuario

### 6.1. Flujo de Creación

```
1. Modal abre → Focus en campo Email
2. Usuario completa email → Validación async (debounced)
3. Si válido → Continúa a siguiente campo
4. Si inválido → Muestra error debajo del input
5. Usuario completa UUID → Validación formato UUID
6. Usuario completa nombres (requeridos)
7. Usuario selecciona planta (opcional)
8. Usuario selecciona roles (opcional)
9. Click "Create" → Validación completa
10. Si valid → POST al backend
11. Success → Modal cierra, lista refresh, toast
12. Error → Muestra error, mantiene datos
```

### 6.2. Atajos de Teclado

```typescript
const keyboardShortcuts = {
  'Escape': 'Close modal',
  'Ctrl+Enter': 'Submit form',
  'Tab': 'Navigate between fields'
};
```

---

## 7. Testing

```typescript
describe('UserFormModal', () => {
  describe('Create Mode', () => {
    it('should validate required fields', async () => {
      const { user } = render(<UserFormModal isOpen mode="create" onClose={jest.fn()} />);
      
      await user.click(screen.getByText('Create'));
      
      expect(screen.getByText(/email is required/i)).toBeInTheDocument();
      expect(screen.getByText(/first name is required/i)).toBeInTheDocument();
    });
    
    it('should show error on duplicate email', async () => {
      mockCreateUser.mockRejectedValue({ status: 409, message: 'Email already exists' });
      
      const { user } = render(<UserFormModal isOpen mode="create" onClose={jest.fn()} />);
      
      await user.type(screen.getByLabelText('Email'), 'existing@ioc.com');
      await user.type(screen.getByLabelText('First Name'), 'Juan');
      await user.type(screen.getByLabelText('Last Name'), 'Pérez');
      await user.type(screen.getByLabelText('Supabase User ID'), uuidv4());
      
      await user.click(screen.getByText('Create'));
      
      await waitFor(() => {
        expect(screen.getByText(/email is already registered/i)).toBeInTheDocument();
      });
    });
  });
  
  describe('Edit Mode', () => {
    it('should prefill form with user data', async () => {
      const userData = {
        id: 1,
        email: 'juan@ioc.com',
        primerNombre: 'Juan',
        primerApellido: 'Pérez'
      };
      
      mockFetchUser.mockResolvedValue(userData);
      
      render(<UserFormModal isOpen mode="edit" userId={1} onClose={jest.fn()} />);
      
      await waitFor(() => {
        expect(screen.getByDisplayValue('juan@ioc.com')).toBeInTheDocument();
        expect(screen.getByDisplayValue('Juan')).toBeInTheDocument();
      });
    });
    
    it('should disable email and uuid fields in edit mode', () => {
      render(<UserFormModal isOpen mode="edit" userId={1} onClose={jest.fn()} />);
      
      expect(screen.getByLabelText('Email')).toBeDisabled();
      expect(screen.getByLabelText('Supabase User ID')).toBeDisabled();
    });
  });
});
```

---

## 8. Accesibilidad

```typescript
<Dialog
  open={isOpen}
  onClose={onClose}
  aria-labelledby="user-form-title"
  aria-describedby="user-form-description"
>
  <DialogTitle id="user-form-title">
    {mode === 'create' ? 'Create User' : 'Edit User'}
  </DialogTitle>
  
  <form onSubmit={handleSubmit(onSubmit)}>
    <FormField>
      <Label htmlFor="email">Email *</Label>
      <Input
        id="email"
        {...register('email')}
        aria-required="true"
        aria-invalid={!!errors.email}
        aria-describedby={errors.email ? 'email-error' : undefined}
      />
      {errors.email && (
        <ErrorMessage id="email-error" role="alert">
          {errors.email.message}
        </ErrorMessage>
      )}
    </FormField>
    
    {/* ... más campos ... */}
    
    <DialogActions>
      <Button onClick={onClose} variant="secondary">
        Cancel
      </Button>
      <Button
        type="submit"
        disabled={isSubmitting}
        aria-busy={isSubmitting}
      >
        {isSubmitting ? 'Saving...' : mode === 'create' ? 'Create' : 'Save'}
      </Button>
    </DialogActions>
  </form>
</Dialog>
```

---

## 9. Checklist de Implementación

- [ ] Crear `UserFormModal.tsx`
- [ ] Implementar schema de validación con Zod
- [ ] Configurar React Hook Form
- [ ] Implementar modo CREATE
- [ ] Implementar modo EDIT con prefill
- [ ] Añadir validación async de email/UUID
- [ ] Implementar selector de planta (cargar de API)
- [ ] Implementar selector de roles con checkboxes
- [ ] Implementar date picker para fecha de contrato
- [ ] Manejo de errores 409 (duplicados)
- [ ] Tests unitarios completos
- [ ] Tests de accesibilidad
- [ ] Responsive design (mobile)

---

**Próximo FTV**: `ftv-role-assignment-modal.md` (FTV-IOC-004-005)

