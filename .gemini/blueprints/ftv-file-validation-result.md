# FTV-004: FileValidationResult (Componente UI)

## Metadata
- **ID**: FTV-004
- **Technical Design**: TD-001-validacion-carga-archivos-etl-v2.md
- **Tipo**: Feature Component
- **Ubicación**: `src/components/FileValidationResult.tsx`
- **Sprint**: Sprint 2
- **Fecha Creación**: 2025-10-19
- **Estado**: Draft
- **Dependencias**: FTV-001 (tipos de ValidationResult)

---

## 1. Propósito

### 1.1. Descripción
Componente que muestra el resultado de la validación de un archivo con un resumen visual (✅❌⚠), lista detallada de errores/warnings con sugerencias, y botones de acción ("Confirmar y Subir" o "Seleccionar otro archivo").

### 1.2. Caso de Uso
```
Como usuario que validó un archivo,
Cuando la validación termina,
Entonces veo un resumen claro de si pasó o falló y por qué,
Para saber si puedo continuar o debo corregir el archivo.
```

### 1.3. Ubicación en la App
- Mostrado en `DataIngestionPage` después de la validación
- Ubicado debajo de `FilePreview`
- Controla habilitación del botón "Confirmar y Subir"

---

## 2. Especificación Visual

### 2.1. Wireframe - Estado READY (Válido)

```
┌─ Resultado de Validación ──────────────────────────────────┐
│                                                            │
│  ✅ Archivo válido y listo para subir                     │
│                                                            │
│  ✅ Extensión: .txt                                       │
│  ✅ Tamaño: 15MB / 50MB                                   │
│  ✅ Codificación: UTF-8                                   │
│  ✅ Encabezados: 9/9 columnas coinciden                   │
│  ✅ Datos (10 líneas muestreadas): Sin errores            │
│                                                            │
│  [Confirmar y Subir]  [Cancelar]                          │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

### 2.2. Wireframe - Estado BLOCKED (Con Errores)

```
┌─ Resultado de Validación ──────────────────────────────────┐
│                                                            │
│  ❌ Archivo con errores - No se puede subir               │
│                                                            │
│  ❌ Extensión: .xlsx (esperado: .txt)                     │
│                                                            │
│  💡 Sugerencia:                                            │
│     Exporta el archivo como TXT delimitado por pipes (|)  │
│     desde Excel: Guardar como → Texto → Reemplazar tabs  │
│                                                            │
│  ---                                                       │
│                                                            │
│  ❌ Campo 'Cantidad' debe ser numérico (línea 5)          │
│     Valor encontrado: 'N/A'                               │
│                                                            │
│  💡 Reemplaza valores no numéricos con 0 o elimina la fila│
│                                                            │
│  [Seleccionar otro archivo]                               │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

### 2.3. Estados Visuales

| Estado | Color | Icono | Botones |
|---|---|---|---|
| READY (válido) | Verde | ✅ | Confirmar y Subir, Cancelar |
| BLOCKED (errores) | Rojo | ❌ | Seleccionar otro archivo |
| WARNING (advertencias) | Amarillo | ⚠️ | Confirmar (con warning), Cancelar |

---

## 3. Props y API

### 3.1. Interface de Props

```typescript
export interface FileValidationResultProps {
  result: ValidationResult;       // Resultado de validación
  onConfirm?: () => void;         // Callback al confirmar upload
  onCancel?: () => void;          // Callback al cancelar
  className?: string;
  showButtons?: boolean;          // Mostrar botones de acción (default: true)
}
```

### 3.2. Tipos Importados

```typescript
import type { 
  ValidationResult, 
  ValidationIssue, 
  ValidationSummary 
} from '@/types/file-validation.types';
```

### 3.3. Ejemplos de Uso

**Ejemplo 1: Básico**
```tsx
<FileValidationResult 
  result={validationResult}
  onConfirm={handleConfirmUpload}
  onCancel={handleCancel}
/>
```

**Ejemplo 2: Solo visualización (sin botones)**
```tsx
<FileValidationResult 
  result={validationResult}
  showButtons={false}
/>
```

---

## 4. Renderizado

### 4.1. Código JSX Completo

```tsx
import React from 'react';
import type { 
  ValidationResult, 
  ValidationIssue 
} from '@/types/file-validation.types';

export interface FileValidationResultProps {
  result: ValidationResult;
  onConfirm?: () => void;
  onCancel?: () => void;
  className?: string;
  showButtons?: boolean;
}

export function FileValidationResult({
  result,
  onConfirm,
  onCancel,
  className = '',
  showButtons = true
}: FileValidationResultProps) {
  const { isValid, errors, warnings, summary } = result;
  
  // Determinar color de tema
  const themeColor = isValid ? 'green' : 'red';
  const bgColor = isValid ? 'bg-green-50' : 'bg-red-50';
  const borderColor = isValid ? 'border-green-200' : 'border-red-200';
  const textColor = isValid ? 'text-green-700' : 'text-red-700';
  const buttonColor = isValid ? 'bg-green-600 hover:bg-green-700' : 'bg-gray-600 hover:bg-gray-700';
  
  return (
    <div 
      className={`border rounded-lg ${borderColor} ${className}`}
      role="region"
      aria-label="Resultado de validación"
    >
      {/* Header con status principal */}
      <div className={`${bgColor} px-4 py-3 border-b ${borderColor}`}>
        <div className={`flex items-center ${textColor} font-medium text-lg`}>
          <span className="text-2xl mr-2">
            {isValid ? '✅' : '❌'}
          </span>
          <span>
            {isValid 
              ? 'Archivo válido y listo para subir'
              : 'Archivo con errores - No se puede subir'
            }
          </span>
        </div>
      </div>
      
      {/* Resumen de validación */}
      <div className="p-4 space-y-2">
        <SummaryItem 
          icon="✅"
          label="Extensión"
          value={summary.extension === 'valid' ? '.txt' : 'inválida'}
          isValid={summary.extension === 'valid'}
        />
        
        <SummaryItem 
          icon="✅"
          label="Tamaño"
          value={`${(summary.size.current / 1024 / 1024).toFixed(1)}MB / ${(summary.size.max / 1024 / 1024).toFixed(0)}MB`}
          isValid={summary.size.valid}
        />
        
        <SummaryItem 
          icon="✅"
          label="Codificación"
          value={summary.encoding}
          isValid={true}
        />
        
        <SummaryItem 
          icon="✅"
          label="Encabezados"
          value={`${summary.headers.found.length}/${summary.headers.expected.length} columnas coinciden`}
          isValid={summary.headers.valid}
        />
        
        <SummaryItem 
          icon="✅"
          label={`Datos (${summary.dataSample.linesValidated} líneas muestreadas)`}
          value={summary.dataSample.errors === 0 ? 'Sin errores' : `${summary.dataSample.errors} errores`}
          isValid={summary.dataSample.errors === 0}
        />
      </div>
      
      {/* Lista de errores */}
      {errors.length > 0 && (
        <div className="px-4 pb-4">
          <div className="border-t pt-4">
            <h3 className="font-medium text-red-700 mb-3">
              Errores detectados ({errors.length})
            </h3>
            <div className="space-y-3">
              {errors.map((error, index) => (
                <IssueItem key={index} issue={error} type="error" />
              ))}
            </div>
          </div>
        </div>
      )}
      
      {/* Lista de warnings */}
      {warnings.length > 0 && (
        <div className="px-4 pb-4">
          <div className="border-t pt-4">
            <h3 className="font-medium text-yellow-700 mb-3">
              Advertencias ({warnings.length})
            </h3>
            <div className="space-y-3">
              {warnings.map((warning, index) => (
                <IssueItem key={index} issue={warning} type="warning" />
              ))}
            </div>
          </div>
        </div>
      )}
      
      {/* Botones de acción */}
      {showButtons && (
        <div className="px-4 py-3 bg-gray-50 border-t flex gap-3">
          {isValid ? (
            <>
              <button
                onClick={onConfirm}
                className={`flex-1 px-4 py-2 ${buttonColor} text-white rounded font-medium transition-colors`}
                aria-label="Confirmar y subir archivo"
              >
                Confirmar y Subir
              </button>
              <button
                onClick={onCancel}
                className="px-4 py-2 bg-gray-200 hover:bg-gray-300 text-gray-700 rounded font-medium transition-colors"
                aria-label="Cancelar"
              >
                Cancelar
              </button>
            </>
          ) : (
            <button
              onClick={onCancel}
              className="flex-1 px-4 py-2 bg-gray-600 hover:bg-gray-700 text-white rounded font-medium transition-colors"
              aria-label="Seleccionar otro archivo"
            >
              Seleccionar otro archivo
            </button>
          )}
        </div>
      )}
    </div>
  );
}

// Componente auxiliar: Item del resumen
interface SummaryItemProps {
  icon: string;
  label: string;
  value: string;
  isValid: boolean;
}

function SummaryItem({ icon, label, value, isValid }: SummaryItemProps) {
  const iconDisplay = isValid ? '✅' : '❌';
  const textColor = isValid ? 'text-gray-700' : 'text-red-700';
  
  return (
    <div className="flex items-center">
      <span className="text-xl mr-2">{iconDisplay}</span>
      <span className={`${textColor}`}>
        <span className="font-medium">{label}:</span> {value}
      </span>
    </div>
  );
}

// Componente auxiliar: Item de issue (error/warning)
interface IssueItemProps {
  issue: ValidationIssue;
  type: 'error' | 'warning';
}

function IssueItem({ issue, type }: IssueItemProps) {
  const bgColor = type === 'error' ? 'bg-red-50' : 'bg-yellow-50';
  const borderColor = type === 'error' ? 'border-red-200' : 'border-yellow-200';
  const textColor = type === 'error' ? 'text-red-800' : 'text-yellow-800';
  const icon = type === 'error' ? '❌' : '⚠️';
  
  return (
    <div className={`${bgColor} border ${borderColor} rounded p-3`}>
      <div className={`flex items-start ${textColor}`}>
        <span className="text-lg mr-2 mt-0.5">{icon}</span>
        <div className="flex-1">
          {/* Mensaje principal */}
          <div className="font-medium">
            {translateMessage(issue.message, issue)}
          </div>
          
          {/* Detalles adicionales */}
          {issue.value && (
            <div className="text-sm mt-1">
              Valor encontrado: <code className="bg-white px-1 rounded">{issue.value}</code>
            </div>
          )}
          
          {issue.line && (
            <div className="text-sm mt-1">
              Línea: {issue.line}
            </div>
          )}
          
          {/* Sugerencia */}
          {issue.suggestion && (
            <div className="mt-2 pt-2 border-t border-current/20">
              <div className="flex items-start text-sm">
                <span className="mr-1">💡</span>
                <span>{translateMessage(issue.suggestion, issue)}</span>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

// Helper: traducir mensajes i18n
function translateMessage(key: string, issue: ValidationIssue): string {
  // Mapeo básico (en producción usar librería i18n)
  const messages: Record<string, string> = {
    'val.ext.notAllowed': 'Solo se permiten archivos .txt',
    'val.file.empty': 'El archivo está vacío',
    'val.size.tooLarge': `El archivo excede el límite de ${issue.value || '50MB'}`,
    'val.headers.mismatch': 'Encabezados no coinciden con el formato esperado',
    'val.req.empty': `Campo requerido '${issue.field}' está vacío (línea ${issue.line})`,
    'val.num.invalid': `Campo '${issue.field}' debe ser numérico (línea ${issue.line})`,
    'val.date.invalid': `Fecha inválida en '${issue.field}' (línea ${issue.line})`,
    'val.time.invalid': `Hora inválida en '${issue.field}' (línea ${issue.line})`,
    'advice.export.pipe': "Exporta como TXT delimitado por '|' desde Excel"
  };
  
  return messages[key] || key;
}
```

---

## 5. Interacciones de Usuario

| Acción | Evento | Handler | Efecto |
|---|---|---|---|
| Click en "Confirmar y Subir" | onClick | onConfirm() | Procede con upload al backend |
| Click en "Cancelar" | onClick | onCancel() | Resetea estado y permite seleccionar otro archivo |
| Click en "Seleccionar otro archivo" | onClick | onCancel() | Resetea estado |

---

## 6. Accesibilidad

### 6.1. ARIA Attributes

```tsx
// Contenedor principal
<div role="region" aria-label="Resultado de validación">

// Botones con labels explícitos
<button aria-label="Confirmar y subir archivo">

// Errores como alerts
<div role="alert" aria-live="polite">
  {errors.map(...)}
</div>
```

### 6.2. Navegación por Teclado

- Botones son focuseables con Tab
- Enter/Space activan los botones
- Orden de tabulación lógico: Confirmar → Cancelar

### 6.3. Contraste de Colores

| Elemento | Fondo | Texto | Ratio |
|---|---|---|---|
| Error | bg-red-50 | text-red-800 | 7.2:1 ✅ |
| Warning | bg-yellow-50 | text-yellow-800 | 5.1:1 ✅ |
| Success | bg-green-50 | text-green-700 | 6.8:1 ✅ |

---

## 7. Testing

### 7.1. Unit Tests

```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import { FileValidationResult } from './FileValidationResult';

describe('FileValidationResult', () => {
  const validResult: ValidationResult = {
    isValid: true,
    errors: [],
    warnings: [],
    summary: {
      extension: 'valid',
      size: { current: 15 * 1024 * 1024, max: 50 * 1024 * 1024, valid: true },
      encoding: 'UTF-8',
      headers: { found: ['col1'], expected: ['col1'], valid: true },
      dataSample: { linesValidated: 10, errors: 0, warnings: 0 }
    }
  };
  
  const invalidResult: ValidationResult = {
    isValid: false,
    errors: [{
      code: 'EXTENSION_NOT_ALLOWED',
      severity: 'ERROR',
      message: 'val.ext.notAllowed',
      suggestion: 'advice.export.pipe'
    }],
    warnings: [],
    summary: {
      extension: 'invalid',
      size: { current: 0, max: 50 * 1024 * 1024, valid: true },
      encoding: 'UTF-8',
      headers: { found: [], expected: [], valid: false },
      dataSample: { linesValidated: 0, errors: 1, warnings: 0 }
    }
  };
  
  it('muestra mensaje de éxito cuando archivo es válido', () => {
    render(<FileValidationResult result={validResult} />);
    
    expect(screen.getByText(/archivo válido y listo para subir/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/confirmar y subir/i)).toBeInTheDocument();
  });
  
  it('muestra mensaje de error cuando archivo es inválido', () => {
    render(<FileValidationResult result={invalidResult} />);
    
    expect(screen.getByText(/archivo con errores/i)).toBeInTheDocument();
    expect(screen.getByText(/solo se permiten archivos .txt/i)).toBeInTheDocument();
  });
  
  it('muestra resumen de validación', () => {
    render(<FileValidationResult result={validResult} />);
    
    expect(screen.getByText(/extensión/i)).toBeInTheDocument();
    expect(screen.getByText(/tamaño/i)).toBeInTheDocument();
    expect(screen.getByText(/codificación/i)).toBeInTheDocument();
    expect(screen.getByText(/encabezados/i)).toBeInTheDocument();
  });
  
  it('llama onConfirm al hacer click en Confirmar', () => {
    const mockConfirm = vi.fn();
    
    render(
      <FileValidationResult 
        result={validResult} 
        onConfirm={mockConfirm}
      />
    );
    
    const confirmButton = screen.getByLabelText(/confirmar y subir/i);
    fireEvent.click(confirmButton);
    
    expect(mockConfirm).toHaveBeenCalled();
  });
  
  it('llama onCancel al hacer click en Cancelar', () => {
    const mockCancel = vi.fn();
    
    render(
      <FileValidationResult 
        result={validResult} 
        onCancel={mockCancel}
      />
    );
    
    const cancelButton = screen.getByText(/cancelar/i);
    fireEvent.click(cancelButton);
    
    expect(mockCancel).toHaveBeenCalled();
  });
  
  it('muestra lista de errores con detalles', () => {
    const resultWithError: ValidationResult = {
      ...invalidResult,
      errors: [{
        code: 'INVALID_NUMBER',
        severity: 'ERROR',
        field: 'Cantidad',
        line: 5,
        value: 'N/A',
        message: 'val.num.invalid',
        suggestion: 'Reemplaza valores no numéricos'
      }]
    };
    
    render(<FileValidationResult result={resultWithError} />);
    
    expect(screen.getByText(/campo 'cantidad' debe ser numérico/i)).toBeInTheDocument();
    expect(screen.getByText(/línea: 5/i)).toBeInTheDocument();
    expect(screen.getByText(/n\/a/i)).toBeInTheDocument();
  });
  
  it('oculta botones cuando showButtons=false', () => {
    render(
      <FileValidationResult 
        result={validResult} 
        showButtons={false}
      />
    );
    
    expect(screen.queryByLabelText(/confirmar/i)).not.toBeInTheDocument();
  });
  
  it('tiene contraste accesible', async () => {
    const { container } = render(<FileValidationResult result={validResult} />);
    
    const results = await axe(container);
    expect(results).toHaveNoViolations();
  });
});
```

---

## 8. i18n

### 8.1. Claves de Mensajes

```typescript
// locales/es.json
{
  "val.ext.notAllowed": "Solo se permiten archivos .txt",
  "val.file.empty": "El archivo está vacío",
  "val.size.tooLarge": "El archivo excede el límite de {max} MB",
  "val.headers.mismatch": "Encabezados no coinciden con el formato esperado",
  "val.req.empty": "Campo requerido '{field}' está vacío (línea {line})",
  "val.num.invalid": "Campo '{field}' debe ser numérico (línea {line})",
  "val.date.invalid": "Fecha inválida en '{field}' (línea {line})",
  "val.time.invalid": "Hora inválida en '{field}' (línea {line})",
  "advice.export.pipe": "Exporta como TXT delimitado por '|'",
  
  "validation.result.valid": "Archivo válido y listo para subir",
  "validation.result.invalid": "Archivo con errores - No se puede subir",
  "validation.action.confirm": "Confirmar y Subir",
  "validation.action.cancel": "Cancelar",
  "validation.action.selectAnother": "Seleccionar otro archivo"
}
```

---

## 9. Dependencias

### 9.1. Internas
- `@/types/file-validation.types` (ValidationResult, ValidationIssue)
- Tailwind CSS (estilos)

### 9.2. Externas
- React

---

## 10. Checklist de Implementación

- [ ] Crear `src/components/FileValidationResult.tsx`
- [ ] Implementar renderizado de resumen
- [ ] Implementar lista de errores/warnings
- [ ] Añadir componentes auxiliares (SummaryItem, IssueItem)
- [ ] Implementar función translateMessage (i18n básico)
- [ ] Añadir botones de acción con callbacks
- [ ] Estilizar con Tailwind
- [ ] Escribir tests unitarios
- [ ] Validar accesibilidad (axe, contraste)
- [ ] Integrar en DataIngestionPage

---

## 11. Referencias
- TD: `.gemini/sprints/technical-designs/TD-001-validacion-carga-archivos-etl-v2.md`
- FTV-001: FileValidator (tipos)

