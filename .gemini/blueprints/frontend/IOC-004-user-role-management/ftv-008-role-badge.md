# FTV-008: Role Badge Component

## 📋 Metadata

- **ID:** ftv-008-role-badge
- **Tipo:** Componente Reutilizable
- **Feature:** IOC-004 User Role Management
- **Technical Design:** TD-IOC-004-User-Role-Management-claude.md
- **Parent:** ftv-001, ftv-002, ftv-003, ftv-005
- **Versión:** 1.0
- **Fecha:** 2025-10-28
- **Autor:** Tech Lead (IA)
- **Estado:** ✅ Ready for Implementation

---

## 🎯 Propósito

Componente visual reutilizable para mostrar roles de usuario con colores consistentes adaptados al contexto chileno, iconos y estados. Debe ser accesible, responsivo y mantener la identidad visual del sistema IOC.

---

## 🔧 Props Interface

```typescript
interface RoleBadgeProps {
  /** Nombre del rol (ADMIN, GERENTE, ANALISTA) */
  role: string;
  
  /** Tamaño del badge */
  size?: 'sm' | 'md' | 'lg';
  
  /** Mostrar icono */
  showIcon?: boolean;
  
  /** Mostrar badge removible (con X) */
  removable?: boolean;
  
  /** Callback cuando se remueve el badge */
  onRemove?: (role: string) => void;
  
  /** Clase CSS adicional */
  className?: string;
  
  /** Deshabilitar badge */
  disabled?: boolean;
}
```

---

## 🎨 Diseño Visual

### Variantes por Rol

```typescript
const ROLE_VARIANTS = {
  ADMIN: {
    bg: 'bg-red-100 dark:bg-red-900',
    text: 'text-red-800 dark:text-red-200',
    border: 'border-red-200 dark:border-red-800',
    icon: Shield,
    label: 'Administrador'
  },
  GERENTE: {
    bg: 'bg-blue-100 dark:bg-blue-900',
    text: 'text-blue-800 dark:text-blue-200',
    border: 'border-blue-200 dark:border-blue-800',
    icon: UserCog,
    label: 'Gerente'
  },
  ANALISTA: {
    bg: 'bg-green-100 dark:bg-green-900',
    text: 'text-green-800 dark:text-green-200',
    border: 'border-green-200 dark:border-green-800',
    icon: User,
    label: 'Analista'
  },
  DEFAULT: {
    bg: 'bg-gray-100 dark:bg-gray-800',
    text: 'text-gray-800 dark:text-gray-200',
    border: 'border-gray-200 dark:border-gray-700',
    icon: HelpCircle,
    label: 'Desconocido'
  }
} as const;
```

### Tamaños

```typescript
const SIZE_VARIANTS = {
  sm: {
    padding: 'px-2 py-0.5',
    text: 'text-xs',
    iconSize: 12,
    gap: 'gap-1'
  },
  md: {
    padding: 'px-2.5 py-1',
    text: 'text-sm',
    iconSize: 14,
    gap: 'gap-1.5'
  },
  lg: {
    padding: 'px-3 py-1.5',
    text: 'text-base',
    iconSize: 16,
    gap: 'gap-2'
  }
} as const;
```

---

## 📐 Estructura del Componente

```tsx
import React from 'react';
import { Shield, UserCog, User, HelpCircle, X } from 'lucide-react';
import { cn } from '@/lib/utils';

export const RoleBadge: React.FC<RoleBadgeProps> = ({
  role,
  size = 'md',
  showIcon = true,
  removable = false,
  onRemove,
  className,
  disabled = false
}) => {
  const variant = ROLE_VARIANTS[role as keyof typeof ROLE_VARIANTS] || ROLE_VARIANTS.DEFAULT;
  const sizeConfig = SIZE_VARIANTS[size];
  const Icon = variant.icon;

  const handleRemove = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!disabled && onRemove) {
      onRemove(role);
    }
  };

  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full border font-medium transition-colors',
        sizeConfig.padding,
        sizeConfig.text,
        sizeConfig.gap,
        variant.bg,
        variant.text,
        variant.border,
        disabled && 'opacity-50 cursor-not-allowed',
        !disabled && removable && 'pr-1',
        className
      )}
      role="status"
      aria-label={`Rol: ${variant.label}`}
    >
      {showIcon && (
        <Icon 
          size={sizeConfig.iconSize} 
          className="flex-shrink-0"
          aria-hidden="true"
        />
      )}
      <span>{variant.label}</span>
      
      {removable && (
        <button
          type="button"
          onClick={handleRemove}
          disabled={disabled}
          className={cn(
            'ml-0.5 rounded-full p-0.5 transition-colors',
            'hover:bg-black/10 dark:hover:bg-white/10',
            'focus:outline-none focus:ring-2 focus:ring-offset-1',
            variant.text.replace('text-', 'focus:ring-'),
            disabled && 'cursor-not-allowed'
          )}
          aria-label={`Remover rol ${variant.label}`}
        >
          <X size={12} />
        </button>
      )}
    </span>
  );
};

RoleBadge.displayName = 'RoleBadge';
```

---

## 🧪 Testing

### Unit Tests

```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import { RoleBadge } from './RoleBadge';

describe('RoleBadge', () => {
  it('renders with correct role label', () => {
    render(<RoleBadge role="ADMIN" />);
    expect(screen.getByText('Administrador')).toBeInTheDocument();
  });

  it('applies correct color variant for ADMIN role', () => {
    const { container } = render(<RoleBadge role="ADMIN" />);
    const badge = container.firstChild;
    expect(badge).toHaveClass('bg-red-100', 'text-red-800');
  });

  it('applies correct color variant for GERENTE role', () => {
    const { container } = render(<RoleBadge role="GERENTE" />);
    const badge = container.firstChild;
    expect(badge).toHaveClass('bg-blue-100', 'text-blue-800');
  });

  it('applies correct color variant for ANALISTA role', () => {
    const { container } = render(<RoleBadge role="ANALISTA" />);
    const badge = container.firstChild;
    expect(badge).toHaveClass('bg-green-100', 'text-green-800');
  });

  it('shows icon by default', () => {
    render(<RoleBadge role="ADMIN" />);
    const icon = screen.getByLabelText(/Rol: Administrador/i).querySelector('svg');
    expect(icon).toBeInTheDocument();
  });

  it('hides icon when showIcon is false', () => {
    render(<RoleBadge role="ADMIN" showIcon={false} />);
    const badge = screen.getByLabelText(/Rol: Administrador/i);
    expect(badge.querySelector('svg')).not.toBeInTheDocument();
  });

  it('applies correct size classes', () => {
    const { container: sm } = render(<RoleBadge role="ADMIN" size="sm" />);
    expect(sm.firstChild).toHaveClass('text-xs', 'px-2', 'py-0.5');

    const { container: lg } = render(<RoleBadge role="ADMIN" size="lg" />);
    expect(lg.firstChild).toHaveClass('text-base', 'px-3', 'py-1.5');
  });

  it('shows remove button when removable', () => {
    render(<RoleBadge role="ADMIN" removable onRemove={jest.fn()} />);
    expect(screen.getByLabelText(/Remover rol Administrador/i)).toBeInTheDocument();
  });

  it('calls onRemove when remove button is clicked', () => {
    const handleRemove = jest.fn();
    render(<RoleBadge role="ADMIN" removable onRemove={handleRemove} />);
    
    fireEvent.click(screen.getByLabelText(/Remover rol Administrador/i));
    expect(handleRemove).toHaveBeenCalledWith('ADMIN');
  });

  it('does not call onRemove when disabled', () => {
    const handleRemove = jest.fn();
    render(<RoleBadge role="ADMIN" removable onRemove={handleRemove} disabled />);
    
    fireEvent.click(screen.getByLabelText(/Remover rol Administrador/i));
    expect(handleRemove).not.toHaveBeenCalled();
  });

  it('applies disabled styles', () => {
    const { container } = render(<RoleBadge role="ADMIN" disabled />);
    expect(container.firstChild).toHaveClass('opacity-50', 'cursor-not-allowed');
  });

  it('handles unknown role gracefully', () => {
    render(<RoleBadge role="UNKNOWN_ROLE" />);
    expect(screen.getByText('Desconocido')).toBeInTheDocument();
  });

  it('applies custom className', () => {
    const { container } = render(<RoleBadge role="ADMIN" className="custom-class" />);
    expect(container.firstChild).toHaveClass('custom-class');
  });
});
```

### Accessibility Tests

```typescript
import { axe } from 'jest-axe';

describe('RoleBadge Accessibility', () => {
  it('has no accessibility violations', async () => {
    const { container } = render(<RoleBadge role="ADMIN" />);
    const results = await axe(container);
    expect(results).toHaveNoViolations();
  });

  it('has proper ARIA label', () => {
    render(<RoleBadge role="ADMIN" />);
    expect(screen.getByRole('status')).toHaveAttribute('aria-label', 'Rol: Administrador');
  });

  it('remove button is keyboard accessible', () => {
    render(<RoleBadge role="ADMIN" removable onRemove={jest.fn()} />);
    const removeButton = screen.getByLabelText(/Remover rol Administrador/i);
    
    removeButton.focus();
    expect(removeButton).toHaveFocus();
  });

  it('remove button has focus ring', () => {
    render(<RoleBadge role="ADMIN" removable onRemove={jest.fn()} />);
    const removeButton = screen.getByLabelText(/Remover rol Administrador/i);
    
    expect(removeButton).toHaveClass('focus:ring-2');
  });
});
```

---

## ♿ Accesibilidad

### WCAG 2.1 Compliance

- ✅ **1.4.3 Contraste (AA):** Todos los colores cumplen ratio mínimo 4.5:1
- ✅ **2.1.1 Teclado:** Botón de remover accesible por teclado
- ✅ **2.4.7 Foco Visible:** Focus ring visible en botón de remover
- ✅ **4.1.2 Nombre, Rol, Valor:** ARIA labels descriptivos

### Características

- Rol semántico `status` para screen readers
- ARIA label descriptivo con nombre del rol
- Botón de remover con label específico
- Focus management en interacciones
- Iconos decorativos con `aria-hidden="true"`

---

## 📱 Responsive Design

El componente es naturalmente responsivo gracias a las unidades relativas:
- **Mobile:** Usar size="sm" para ahorrar espacio
- **Tablet:** Usar size="md" (default)
- **Desktop:** Usar size="md" o size="lg" según contexto

---

## 🎭 Casos de Uso

```tsx
// Básico
<RoleBadge role="ADMIN" />

// Con tamaño pequeño
<RoleBadge role="GERENTE" size="sm" />

// Sin icono
<RoleBadge role="ANALISTA" showIcon={false} />

// Removible
<RoleBadge 
  role="ADMIN" 
  removable 
  onRemove={(role) => console.log(`Removing ${role}`)} 
/>

// Deshabilitado
<RoleBadge role="GERENTE" disabled />

// Lista de roles
{user.roles.map(role => (
  <RoleBadge 
    key={role}
    role={role}
    size="sm"
    removable
    onRemove={handleRemoveRole}
  />
))}
```

---

## 🔄 Estados

1. **Default:** Muestra rol con color y icono
2. **Hover (removable):** Botón de remover visible con hover effect
3. **Focus (removable):** Focus ring visible en botón
4. **Disabled:** Opacidad reducida, no interactive
5. **Unknown role:** Fallback a variante DEFAULT gris

---

## 🎨 Variaciones de Diseño

### Dark Mode
- Automático mediante clases `dark:` de Tailwind
- Colores ajustados para mantener contraste

### Custom Roles
- Si se necesitan más roles en el futuro, agregar en `ROLE_VARIANTS`
- Mantener consistencia en la paleta de colores

---

## 📦 Dependencias

```json
{
  "lucide-react": "^0.263.1",
  "clsx": "^2.0.0",
  "tailwind-merge": "^2.0.0"
}
```

---

## 🚀 Implementación

**Archivo:** `src/components/ui/RoleBadge.tsx`

**Utilities necesarias:**

```typescript
// src/lib/utils.ts
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
```

---

## ✅ Checklist de Implementación

- [ ] Crear archivo `RoleBadge.tsx`
- [ ] Crear archivo `RoleBadge.test.tsx`
- [ ] Definir tipos TypeScript
- [ ] Implementar variantes de color
- [ ] Implementar tamaños
- [ ] Agregar funcionalidad removible
- [ ] Implementar dark mode
- [ ] Tests unitarios (>90% coverage)
- [ ] Tests de accesibilidad
- [ ] Validar contraste de colores
- [ ] Documentar en Storybook (opcional)
- [ ] Code review

---

## 📚 Referencias

- **Technical Design:** TD-IOC-004
- **Design System:** tailadmin_config.md
- **Iconos:** Lucide React
- **Accesibilidad:** WCAG 2.1 AA
