# Ficha Técnica de Vista: [Nombre del Componente]

## Metadata
- **ID**: FTV-[XXX]
- **Technical Design**: [TD-XXX-nombre.md]
- **Componente**: `[NombreComponente]`
- **Tipo**: [Page | Layout | Feature Component | UI Component]
- **Ruta** (si aplica): `/[ruta]`
- **Sprint**: Sprint [X]
- **Estado**: 
  - [ ] Draft
  - [ ] En Revisión
  - [ ] Aprobado
  - [ ] Implementado
- **Autor**: [Nombre o IA]
- **Fecha Creación**: [YYYY-MM-DD]
- **Última Actualización**: [YYYY-MM-DD]

---

## 1. Propósito del Componente

### 1.1. Descripción

[Descripción breve de qué hace este componente y por qué existe]

**Caso de Uso Principal**:
```
Como [tipo de usuario],
Cuando [contexto/acción],
Entonces veo/interactúo con [este componente],
Para [objetivo]
```

### 1.2. Ubicación en la Aplicación

**Jerarquía de Navegación**:
```
[Página Padre] → [Sección] → [Este Componente]

Ejemplo:
Dashboard → Panel Admin → Ingesta de Datos → DataUploadDropzone
```

**Rutas**:
- Ruta Principal: `/[ruta]`
- Rutas Relacionadas: `/[otra-ruta]`

---

## 2. Especificación Visual

### 2.1. Wireframe / Descripción Visual

```
┌─────────────────────────────────────────────────────────────┐
│ [NombreComponente]                                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  [Descripción visual en ASCII o referencia a Figma]        │
│                                                             │
│  ┌──────────────────────────────────────────┐             │
│  │ [Elemento hijo 1]                        │             │
│  └──────────────────────────────────────────┘             │
│                                                             │
│  ┌──────────────────────────────────────────┐             │
│  │ [Elemento hijo 2]                        │             │
│  └──────────────────────────────────────────┘             │
│                                                             │
│  [Botón Acción]  [Botón Cancelar]                         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Referencias de Diseño**:
- Figma: [URL si existe]
- Screenshot: [Ubicación]
- Inspiración: [Componente similar en otra app]

---

### 2.2. Estados Visuales

| Estado | Descripción Visual | Cuándo Ocurre |
|--------|-------------------|---------------|
| **Inicial** | [Descripción] | Al montar el componente |
| **Cargando** | [Descripción] | Durante fetch de datos |
| **Con Datos** | [Descripción] | Después de cargar exitosamente |
| **Vacío** | [Descripción] | Sin datos para mostrar |
| **Error** | [Descripción] | Fallo en carga de datos |
| **Deshabilitado** | [Descripción] | Usuario sin permisos o condición no cumplida |
| **[Otros]** | [Descripción] | [Condición] |

---

### 2.3. Responsive Design

| Breakpoint | Comportamiento |
|------------|----------------|
| **Desktop (≥1024px)** | [Descripción del layout] |
| **Tablet (768-1023px)** | [Cambios en el layout] |
| **Mobile (≤767px)** | [Adaptaciones para móvil] |

**Prioridades Mobile-First**:
- [ ] [Elemento crítico 1]
- [ ] [Elemento crítico 2]
- [ ] [Elemento que se oculta en mobile]

---

## 3. Jerarquía de Componentes

### 3.1. Árbol de Componentes

```typescript
<[ComponentePrincipal]>
  ├─ <[ComponenteHijo1]>
  │   ├─ <[Nieto1]>
  │   └─ <[Nieto2]>
  ├─ <[ComponenteHijo2]>
  │   └─ <[Nieto3]>
  └─ <[ComponenteHijo3]>
```

**Ejemplo Concreto**:
```tsx
<DataUploadPage>
  ├─ <PageHeader title="Ingesta de Datos" />
  ├─ <DataUploadDropzone>
  │   ├─ <FilePreview lines={14} />
  │   ├─ <FileValidationResult />
  │   └─ <UploadButton disabled={!isValid} />
  └─ <UploadHistoryTable />
```

---

### 3.2. Componentes Hijo (Nuevos)

Lista de componentes hijo que deben crearse específicamente para este componente:

| Componente | Tipo | Propósito | FTV |
|------------|------|-----------|-----|
| `[NombreHijo1]` | UI Component | [Propósito] | `ftv-[nombre-hijo1].md` |
| `[NombreHijo2]` | UI Component | [Propósito] | `ftv-[nombre-hijo2].md` |

---

### 3.3. Componentes Reutilizados

Componentes existentes que se usan en este componente:

| Componente | Librería | Propósito |
|------------|----------|-----------|
| `Button` | shadcn/ui | Botones de acción |
| `Card` | shadcn/ui | Contenedor con estilo |
| `Spinner` | Custom (components/ui) | Indicador de carga |
| `[Otro]` | [Librería] | [Propósito] |

---

## 4. Props y API del Componente

### 4.1. Props

```typescript
interface [NombreComponente]Props {
  // Props requeridas
  id: string;                    // Identificador único
  data: [TipoDato];              // Datos principales
  onAction: (params: ActionParams) => void;  // Callback principal
  
  // Props opcionales
  className?: string;            // Clases CSS adicionales
  disabled?: boolean;            // Deshabilitar interacción
  variant?: 'default' | 'compact' | 'full';  // Variante visual
  
  // Props de configuración
  config?: {
    maxItems?: number;
    showPreview?: boolean;
  };
  
  // Callbacks opcionales
  onSuccess?: (result: Result) => void;
  onError?: (error: Error) => void;
  onCancel?: () => void;
}
```

**Validación de Props**:
```typescript
// Usar Zod o PropTypes
const propsSchema = z.object({
  id: z.string().min(1),
  data: z.array(z.object({ /* ... */ })),
  onAction: z.function(),
  disabled: z.boolean().optional(),
  // ...
});
```

---

### 4.2. Valores por Defecto

```typescript
const defaultProps: Partial<[NombreComponente]Props> = {
  disabled: false,
  variant: 'default',
  config: {
    maxItems: 10,
    showPreview: true
  }
};
```

---

### 4.3. Ejemplos de Uso

**Uso Básico**:
```tsx
<[NombreComponente]
  id="unique-id"
  data={myData}
  onAction={handleAction}
/>
```

**Uso Avanzado**:
```tsx
<[NombreComponente]
  id="advanced-example"
  data={filteredData}
  onAction={handleAction}
  onSuccess={(result) => {
    toast.success('Operación exitosa');
    navigate('/success');
  }}
  onError={(error) => {
    toast.error(error.message);
  }}
  disabled={!hasPermission}
  variant="compact"
  config={{
    maxItems: 20,
    showPreview: false
  }}
  className="custom-class"
/>
```

---

## 5. Estado Interno

### 5.1. State Management

**Tipo de Estado**:
- [ ] Estado Local (useState)
- [ ] Estado Global (Zustand/Redux)
- [ ] Server State (TanStack Query)
- [ ] URL State (React Router)

**Variables de Estado**:

```typescript
// Estado local
const [isLoading, setIsLoading] = useState(false);
const [error, setError] = useState<Error | null>(null);
const [selectedItem, setSelectedItem] = useState<Item | null>(null);
const [formData, setFormData] = useState<FormData>({
  field1: '',
  field2: 0
});

// Estado derivado (useMemo)
const filteredItems = useMemo(() => {
  return items.filter(item => item.status === 'active');
}, [items]);

// Estado de UI
const [isModalOpen, setIsModalOpen] = useState(false);
const [activeTab, setActiveTab] = useState<'tab1' | 'tab2'>('tab1');
```

---

### 5.2. Máquina de Estados (Si Aplica)

**Estados Posibles**: `IDLE` | `LOADING` | `SUCCESS` | `ERROR`

```typescript
type ComponentState = 
  | { status: 'IDLE' }
  | { status: 'LOADING' }
  | { status: 'SUCCESS'; data: Data }
  | { status: 'ERROR'; error: Error };

const [state, setState] = useState<ComponentState>({ status: 'IDLE' });

// Transiciones
IDLE → LOADING (user clicks button)
LOADING → SUCCESS (fetch completes)
LOADING → ERROR (fetch fails)
ERROR → IDLE (user retries)
```

---

## 6. Lógica de Negocio

### 6.1. Reglas de Negocio

| Regla | Descripción | Validación |
|-------|-------------|------------|
| RN-1 | [Descripción de regla] | [Cómo se valida] |
| RN-2 | Solo admins pueden ver el botón "Eliminar" | `user.role === 'ADMIN'` |
| RN-3 | No se puede enviar formulario si hay errores | `errors.length === 0` |

---

### 6.2. Validaciones

**Validaciones de Input**:
```typescript
const validateForm = (data: FormData): ValidationErrors => {
  const errors: ValidationErrors = {};
  
  if (!data.field1 || data.field1.trim() === '') {
    errors.field1 = 'Campo requerido';
  }
  
  if (data.field2 < 0) {
    errors.field2 = 'Debe ser un número positivo';
  }
  
  return errors;
};
```

**Validaciones de Negocio**:
```typescript
const canSubmit = (data: FormData, user: User): boolean => {
  return (
    Object.keys(validateForm(data)).length === 0 &&
    user.hasPermission('CREATE') &&
    !isLoading
  );
};
```

---

### 6.3. Cálculos y Transformaciones

```typescript
// Transformar datos de API a formato de UI
const transformApiData = (apiData: ApiResponse): UiData => {
  return {
    id: apiData.id,
    displayName: `${apiData.firstName} ${apiData.lastName}`,
    formattedDate: formatDate(apiData.createdAt),
    // ...
  };
};

// Cálculos derivados
const totalAmount = items.reduce((sum, item) => sum + item.amount, 0);
const averageScore = scores.reduce((a, b) => a + b) / scores.length;
```

---

## 7. Interacciones de Usuario

### 7.1. Eventos de Usuario

| Acción del Usuario | Evento | Handler | Efecto |
|-------------------|--------|---------|--------|
| Click en botón "Guardar" | onClick | `handleSave()` | Envía datos al backend |
| Cambio en input | onChange | `handleInputChange()` | Actualiza estado del formulario |
| Submit de formulario | onSubmit | `handleSubmit()` | Valida y envía datos |
| Click fuera de modal | onClick (overlay) | `handleClose()` | Cierra el modal |
| Tecla "Escape" | onKeyDown | `handleEscape()` | Cierra diálogo |
| Drag and drop | onDrop | `handleFileDrop()` | Procesa archivo |

---

### 7.2. Flujos de Interacción Detallados

#### Flujo 1: [Nombre del Flujo Principal]

**Trigger**: Usuario hace clic en "Guardar"

**Pasos**:
```
1. Usuario hace clic en botón "Guardar"
   ↓
2. handleSave() se ejecuta
   ↓
3. Validar formulario con validateForm()
   ↓
4. ¿Errores?
   ├─ SÍ → Mostrar errores en UI, detener
   └─ NO → Continuar
   ↓
5. setIsLoading(true)
   ↓
6. Llamar a API: await api.saveData(formData)
   ↓
7. ¿Éxito?
   ├─ SÍ → 
   │   ├─ onSuccess(result)
   │   ├─ toast.success('Guardado')
   │   └─ navigate('/success')
   └─ NO →
       ├─ setError(error)
       ├─ toast.error(error.message)
       └─ setIsLoading(false)
```

**Código**:
```typescript
const handleSave = async () => {
  const errors = validateForm(formData);
  if (Object.keys(errors).length > 0) {
    setErrors(errors);
    return;
  }
  
  setIsLoading(true);
  setError(null);
  
  try {
    const result = await api.saveData(formData);
    onSuccess?.(result);
    toast.success('Datos guardados exitosamente');
    navigate('/success');
  } catch (error) {
    setError(error as Error);
    toast.error(error.message);
  } finally {
    setIsLoading(false);
  }
};
```

---

#### Flujo 2: [Otro Flujo Importante]

[Repetir estructura...]

---

### 7.3. Atajos de Teclado

| Tecla | Acción | Contexto |
|-------|--------|----------|
| `Escape` | Cerrar modal/diálogo | Cuando modal está abierto |
| `Enter` | Submit formulario | Cuando input tiene foco |
| `Ctrl/Cmd + S` | Guardar | En cualquier momento |
| `Tab` | Navegar entre campos | En formulario |

**Implementación**:
```typescript
useEffect(() => {
  const handleKeyDown = (e: KeyboardEvent) => {
    if (e.key === 'Escape') {
      handleClose();
    }
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
      e.preventDefault();
      handleSave();
    }
  };
  
  window.addEventListener('keydown', handleKeyDown);
  return () => window.removeEventListener('keydown', handleKeyDown);
}, []);
```

---

## 8. Integración con Backend

### 8.1. Endpoints Consumidos

#### Endpoint 1: [Nombre]

```typescript
GET /api/v1/[recurso]
```

**Propósito**: Obtener datos para poblar el componente

**Request**:
```typescript
// Query params
{
  page?: number;
  size?: number;
  filter?: string;
}
```

**Response (200 OK)**:
```typescript
{
  items: Array<{
    id: string;
    name: string;
    // ...
  }>;
  pagination: {
    total: number;
    page: number;
    size: number;
  };
}
```

**Manejo de Errores**:
- **401**: Redirigir a /login
- **403**: Mostrar mensaje "Sin permisos"
- **404**: Mostrar mensaje "No encontrado"
- **500**: Mostrar mensaje genérico de error

**Código**:
```typescript
const fetchData = async () => {
  setIsLoading(true);
  setError(null);
  
  try {
    const response = await api.get('/api/v1/recurso', {
      params: { page, size, filter }
    });
    setData(response.data.items);
  } catch (error) {
    if (error.status === 401) {
      navigate('/login');
    } else if (error.status === 403) {
      setError(new Error('No tienes permisos'));
    } else {
      setError(error);
    }
  } finally {
    setIsLoading(false);
  }
};
```

---

#### Endpoint 2: [Nombre]

```typescript
POST /api/v1/[recurso]
```

[Repetir estructura...]

---

### 8.2. Estrategia de Carga de Datos

**Trigger de Carga**:
- [ ] Al montar el componente (useEffect con [])
- [ ] Al cambiar parámetros (useEffect con [dependency])
- [ ] Manual (botón "Refrescar")
- [ ] Polling (cada X segundos)
- [ ] WebSocket (real-time)

**Implementación**:
```typescript
// Opción 1: Carga al montar
useEffect(() => {
  fetchData();
}, []);

// Opción 2: Con TanStack Query (recomendado)
const { data, isLoading, error } = useQuery({
  queryKey: ['recurso', { page, filter }],
  queryFn: () => api.getRecurso({ page, filter }),
  staleTime: 5 * 60 * 1000, // 5 minutos
  refetchOnWindowFocus: true
});
```

---

### 8.3. Optimistic Updates

**Cuándo aplicar**: Al crear/editar/eliminar items sin esperar confirmación del backend

```typescript
const handleDelete = async (id: string) => {
  // 1. Actualización optimista
  const previousItems = items;
  setItems(items.filter(item => item.id !== id));
  
  try {
    // 2. Llamar al backend
    await api.deleteItem(id);
    toast.success('Eliminado');
  } catch (error) {
    // 3. Rollback si falla
    setItems(previousItems);
    toast.error('Error al eliminar');
  }
};
```

---

## 9. Manejo de Errores

### 9.1. Tipos de Errores

| Tipo | Origen | UI | Acción del Usuario |
|------|--------|----|--------------------|
| **Validación** | Cliente | Mensaje bajo input | Corregir y reintentar |
| **Network** | Conexión | Toast de error + retry | Revisar conexión, retry |
| **401 Unauthorized** | Backend | Redirect a /login | Volver a autenticarse |
| **403 Forbidden** | Backend | Mensaje "Sin permisos" | Contactar admin |
| **404 Not Found** | Backend | Mensaje "No encontrado" | Volver atrás |
| **500 Server Error** | Backend | Mensaje genérico | Reportar a soporte |

---

### 9.2. Componentes de Error

**Error Boundary**:
```typescript
// Envolver componente con ErrorBoundary
<ErrorBoundary
  fallback={<ErrorFallback />}
  onError={(error) => logError(error)}
>
  <[NombreComponente] {...props} />
</ErrorBoundary>
```

**Error Inline**:
```tsx
{error && (
  <div className="alert alert-error" role="alert">
    <AlertCircle className="icon" />
    <span>{error.message}</span>
    <button onClick={handleRetry}>Reintentar</button>
  </div>
)}
```

**Error en Formulario**:
```tsx
<input
  className={errors.field1 ? 'input-error' : ''}
  aria-invalid={!!errors.field1}
  aria-describedby={errors.field1 ? 'field1-error' : undefined}
/>
{errors.field1 && (
  <span id="field1-error" className="error-message" role="alert">
    {errors.field1}
  </span>
)}
```

---

### 9.3. Estrategia de Retry

```typescript
const fetchWithRetry = async (
  fn: () => Promise<any>,
  maxRetries = 3,
  delay = 1000
) => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fn();
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      await sleep(delay * Math.pow(2, i)); // Exponential backoff
    }
  }
};

// Uso
const data = await fetchWithRetry(() => api.getData());
```

---

## 10. Performance

### 10.1. Optimizaciones

| Optimización | Técnica | Impacto |
|--------------|---------|---------|
| **Memoización** | `useMemo` para cálculos costosos | Evita re-cálculos en cada render |
| **Callbacks** | `useCallback` para funciones | Evita re-creación de funciones |
| **Componentes** | `React.memo` para componentes hijo | Evita re-renders innecesarios |
| **Lazy Loading** | `React.lazy` para imports | Reduce bundle inicial |
| **Virtualización** | `react-window` para listas grandes | Renderiza solo items visibles |
| **Debounce** | `useDebouncedValue` para búsquedas | Reduce llamadas a API |

**Código**:
```typescript
// Memoización de cálculos
const expensiveValue = useMemo(() => {
  return items.reduce((acc, item) => {
    // cálculo costoso
    return acc + heavyComputation(item);
  }, 0);
}, [items]);

// Memoización de callbacks
const handleClick = useCallback((id: string) => {
  onItemClick(id);
}, [onItemClick]);

// Memoización de componentes
const ListItem = React.memo(({ item, onClick }) => {
  return <div onClick={() => onClick(item.id)}>{item.name}</div>;
});
```

---

### 10.2. Métricas Objetivo

| Métrica | Objetivo | Crítico |
|---------|----------|---------|
| **Tiempo de carga inicial** | < 1s | < 2s |
| **Tiempo de interacción** | < 100ms | < 300ms |
| **Re-renders por segundo** | < 10 | < 30 |
| **Tamaño del bundle** | < 50KB | < 100KB |

---

## 11. Accesibilidad (a11y)

### 11.1. Checklist de Accesibilidad

- [ ] **Semántica HTML**: Uso correcto de `<button>`, `<form>`, `<label>`, etc.
- [ ] **ARIA Labels**: Todos los elementos interactivos tienen labels
- [ ] **Navegación por Teclado**: Todo es accesible con Tab/Enter/Escape
- [ ] **Contraste**: Ratio mínimo 4.5:1 (WCAG AA)
- [ ] **Focus Visible**: Indicador visual cuando elemento tiene foco
- [ ] **Screen Reader**: Funciona con NVDA/JAWS/VoiceOver
- [ ] **Errores Anunciados**: Errores tienen `role="alert"`
- [ ] **Estados Comunicados**: Loading/disabled tienen ARIA states

---

### 11.2. ARIA Attributes

```tsx
<button
  aria-label="Guardar cambios"
  aria-busy={isLoading}
  aria-disabled={!canSubmit}
  onClick={handleSave}
>
  {isLoading ? <Spinner aria-hidden="true" /> : 'Guardar'}
</button>

<input
  id="email"
  type="email"
  aria-required="true"
  aria-invalid={!!errors.email}
  aria-describedby={errors.email ? 'email-error' : 'email-help'}
/>

<div id="email-error" role="alert" aria-live="polite">
  {errors.email}
</div>
```

---

### 11.3. Navegación por Teclado

**Orden de Tabulación**:
```
1. Input Field 1
2. Input Field 2
3. Botón "Guardar"
4. Botón "Cancelar"
5. Link "Ayuda"
```

**Focus Trap** (en modales):
```typescript
useFocusTrap(modalRef, isOpen);

// Implementación
const useFocusTrap = (ref: RefObject<HTMLElement>, isActive: boolean) => {
  useEffect(() => {
    if (!isActive) return;
    
    const element = ref.current;
    const focusableElements = element?.querySelectorAll(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    
    const firstElement = focusableElements?.[0] as HTMLElement;
    const lastElement = focusableElements?.[focusableElements.length - 1] as HTMLElement;
    
    firstElement?.focus();
    
    const handleTab = (e: KeyboardEvent) => {
      if (e.key !== 'Tab') return;
      
      if (e.shiftKey) {
        if (document.activeElement === firstElement) {
          e.preventDefault();
          lastElement?.focus();
        }
      } else {
        if (document.activeElement === lastElement) {
          e.preventDefault();
          firstElement?.focus();
        }
      }
    };
    
    element?.addEventListener('keydown', handleTab);
    return () => element?.removeEventListener('keydown', handleTab);
  }, [ref, isActive]);
};
```

---

## 12. Testing

### 12.1. Test Plan

| Tipo | Herramienta | Casos a Cubrir |
|------|-------------|----------------|
| **Unit** | Vitest | Funciones de lógica pura (validators, transformers) |
| **Component** | Testing Library | Renderizado, interacciones, estados |
| **Integration** | Testing Library + MSW | Flujos completos con API mocks |
| **Visual** | Storybook (manual) | Estados visuales |
| **a11y** | jest-axe | Validación WCAG |

---

### 12.2. Casos de Prueba

#### Test 1: Renderizado Básico

```typescript
// [NombreComponente].test.tsx
import { render, screen } from '@testing-library/react';
import { [NombreComponente] } from './[NombreComponente]';

describe('[NombreComponente]', () => {
  it('renderiza correctamente con props mínimas', () => {
    render(
      <[NombreComponente]
        id="test-id"
        data={mockData}
        onAction={vi.fn()}
      />
    );
    
    expect(screen.getByText('Título Esperado')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /guardar/i })).toBeInTheDocument();
  });
});
```

---

#### Test 2: Interacción de Usuario

```typescript
it('llama a onAction cuando se hace clic en guardar', async () => {
  const mockOnAction = vi.fn();
  const user = userEvent.setup();
  
  render(
    <[NombreComponente]
      id="test-id"
      data={mockData}
      onAction={mockOnAction}
    />
  );
  
  const saveButton = screen.getByRole('button', { name: /guardar/i });
  await user.click(saveButton);
  
  expect(mockOnAction).toHaveBeenCalledWith(
    expect.objectContaining({
      id: 'test-id',
      // ...
    })
  );
});
```

---

#### Test 3: Validación

```typescript
it('muestra error si campo requerido está vacío', async () => {
  const user = userEvent.setup();
  
  render(<[NombreComponente] {...defaultProps} />);
  
  const input = screen.getByLabelText(/nombre/i);
  const submitButton = screen.getByRole('button', { name: /guardar/i });
  
  await user.clear(input);
  await user.click(submitButton);
  
  expect(screen.getByRole('alert')).toHaveTextContent('Campo requerido');
  expect(mockOnAction).not.toHaveBeenCalled();
});
```

---

#### Test 4: Estados de Carga

```typescript
it('muestra spinner mientras está cargando', async () => {
  const { rerender } = render(
    <[NombreComponente] {...defaultProps} />
  );
  
  // Simular carga
  rerender(<[NombreComponente] {...defaultProps} isLoading={true} />);
  
  expect(screen.getByRole('status')).toBeInTheDocument();
  expect(screen.getByLabelText(/cargando/i)).toBeInTheDocument();
  
  // Termina carga
  rerender(<[NombreComponente] {...defaultProps} isLoading={false} />);
  
  expect(screen.queryByRole('status')).not.toBeInTheDocument();
});
```

---

#### Test 5: Accesibilidad

```typescript
import { axe, toHaveNoViolations } from 'jest-axe';
expect.extend(toHaveNoViolations);

it('no tiene violaciones de accesibilidad', async () => {
  const { container } = render(<[NombreComponente] {...defaultProps} />);
  const results = await axe(container);
  expect(results).toHaveNoViolations();
});
```

---

### 12.3. Cobertura Objetivo

**Objetivo**: ≥ 80% de cobertura

- **Statements**: 85%
- **Branches**: 80%
- **Functions**: 90%
- **Lines**: 85%

---

## 13. Dependencias

### 13.1. Librerías Externas

| Librería | Versión | Propósito | Instalación |
|----------|---------|-----------|-------------|
| `react` | ^19.0.0 | Framework base | Ya instalada |
| `react-hook-form` | ^7.x | Manejo de formularios | `npm install react-hook-form` |
| `zod` | ^3.x | Validación de schemas | `npm install zod` |
| `@tanstack/react-query` | ^5.x | Server state | `npm install @tanstack/react-query` |
| `[otra]` | [version] | [propósito] | `npm install [otra]` |

---

### 13.2. Hooks Personalizados

| Hook | Ubicación | Propósito |
|------|-----------|-----------|
| `useAuth` | `hooks/useAuth.ts` | Acceso a usuario autenticado |
| `useApi` | `hooks/useApi.ts` | Cliente HTTP configurado |
| `useDebouncedValue` | `hooks/useDebouncedValue.ts` | Debounce de valores |
| `[otro]` | [ubicación] | [propósito] |

---

### 13.3. Servicios

| Servicio | Ubicación | Propósito |
|----------|-----------|-----------|
| `api` | `services/api.ts` | Cliente HTTP (axios/fetch) |
| `telemetry` | `services/telemetry.ts` | Envío de métricas |
| `[otro]` | [ubicación] | [propósito] |

---

## 14. Configuración y Feature Flags

### 14.1. Variables de Entorno

```bash
# .env.local
VITE_[FEATURE]_ENABLED=true
VITE_[FEATURE]_MAX_ITEMS=50
VITE_API_TIMEOUT=10000
```

### 14.2. Feature Flags

```typescript
const FEATURE_ENABLED = import.meta.env.VITE_[FEATURE]_ENABLED === 'true';

if (!FEATURE_ENABLED) {
  return <LegacyComponent {...props} />;
}

return <[NombreComponente] {...props} />;
```

---

## 15. Notas de Implementación

### 15.1. Consideraciones Especiales

- **[Consideración 1]**: [Descripción y por qué es importante]
- **[Consideración 2]**: [Descripción]
- **Degradación Graciosa**: Si la API falla, mostrar datos en cache y permitir modo offline limitado

### 15.2. Deuda Técnica Conocida

| Issue | Severidad | Razón | Plan de Resolución |
|-------|-----------|-------|-------------------|
| [Descripción] | Alta/Media/Baja | [Por qué existe] | [Cuándo/cómo resolverlo] |

### 15.3. TODOs

- [ ] [TODO 1: Descripción]
- [ ] [TODO 2: Descripción]

---

## 16. Checklist de Implementación

### Desarrollo
- [ ] Crear archivo del componente
- [ ] Implementar props interface
- [ ] Implementar estado interno
- [ ] Implementar lógica de negocio
- [ ] Implementar handlers de eventos
- [ ] Integrar con API (si aplica)
- [ ] Implementar manejo de errores
- [ ] Implementar estados visuales (loading, error, empty)
- [ ] Aplicar estilos (Tailwind/CSS)
- [ ] Optimizar performance (memo, callbacks)

### Accesibilidad
- [ ] Agregar ARIA labels
- [ ] Probar navegación por teclado
- [ ] Probar con screen reader
- [ ] Validar contraste de colores
- [ ] Agregar focus visible

### Testing
- [ ] Tests unitarios
- [ ] Tests de integración
- [ ] Tests de accesibilidad (axe)
- [ ] Testing manual en navegadores
- [ ] Testing responsive

### Documentación
- [ ] Comentarios en código (JSDoc)
- [ ] Storybook stories (si aplica)
- [ ] Actualizar esta FTV si hubo cambios

### Code Review
- [ ] Self-review
- [ ] Peer review
- [ ] QA review

---

## 17. Referencias

- **Technical Design**: `@.gemini/sprints/technical-designs/TD-[XXX]-[nombre].md`
- **Feature Plan**: `@.gemini/sprints/feature-plans/FP-[XXX]-[nombre].md`
- **Componentes Relacionados**: 
  - `ftv-[componente-relacionado-1].md`
  - `ftv-[componente-relacionado-2].md`
- **Figma**: [URL]
- **PRD/Spec**: [URL]

---

## 18. Changelog

| Versión | Fecha | Autor | Cambios |
|---------|-------|-------|---------|
| 1.0 | [YYYY-MM-DD] | [Nombre/IA] | Creación inicial |
| 1.1 | [YYYY-MM-DD] | [Nombre] | [Descripción de cambios] |

---

**FTV creada por**: [Nombre o "IA Blueprint Generator"]  
**Fecha**: [YYYY-MM-DD]  
**Última actualización**: [YYYY-MM-DD]
```

---
