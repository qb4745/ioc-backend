# FTV-005: Integración en DataUploadDropzone

## Metadata
- **ID**: FTV-005
- **Technical Design**: TD-001-validacion-carga-archivos-etl-v2.md
- **Tipo**: Integration Guide (Modificación de componente existente)
- **Ubicación**: `src/components/admin/DataUploadDropzone.tsx` (o similar)
- **Sprint**: Sprint 2
- **Fecha Creación**: 2025-10-19
- **Estado**: Draft
- **Dependencias**: FTV-001, FTV-002, FTV-003, FTV-004

---

## 1. Propósito

### 1.1. Descripción
Guía para integrar la validación de archivos en el componente existente `DataUploadDropzone`, interceptando la selección de archivos y orquestando el flujo de validación antes de permitir el upload al backend.

### 1.2. Objetivo de la Integración
- Interceptar evento `onFileSelect` antes de proceder con upload
- Mostrar `FilePreview` y `FileValidationResult` durante validación
- Habilitar botón "Confirmar y Subir" solo si validación pasa
- Permitir cancelación y reselección de archivo

---

## 2. Componente Actual (Estado Previo)

### 2.1. Estructura Actual Inferida

```tsx
// Estructura actual aproximada (antes de la integración)
function DataUploadDropzone({ onFileSelect, onUploadComplete }) {
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  
  const handleDrop = (acceptedFiles: File[]) => {
    const selectedFile = acceptedFiles[0];
    setFile(selectedFile);
    
    // PROBLEMA: Sube directamente sin validar
    onFileSelect(selectedFile);
  };
  
  return (
    <div {...getRootProps()}>
      <input {...getInputProps()} />
      {/* UI del dropzone */}
    </div>
  );
}
```

---

## 3. Componente Modificado (Estado Final)

### 3.1. Código Completo con Integración

```tsx
import React, { useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { useFileValidation } from '@/hooks/useFileValidation';
import { FilePreview } from '@/components/FilePreview';
import { FileValidationResult } from '@/components/FileValidationResult';
import { DEFAULT_FILE_VALIDATION_CONFIG } from '@/services/FileValidator';

interface DataUploadDropzoneProps {
  onFileSelect: (file: File) => void;
  onUploadComplete?: (result: any) => void;
  onUploadError?: (error: Error) => void;
}

export function DataUploadDropzone({
  onFileSelect,
  onUploadComplete,
  onUploadError
}: DataUploadDropzoneProps) {
  // Estado del archivo
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  
  // Hook de validación
  const {
    state: validationState,
    result: validationResult,
    isValidating,
    validate,
    reset: resetValidation
  } = useFileValidation({
    // Usar config por defecto o custom si existe variable de entorno
    ...DEFAULT_FILE_VALIDATION_CONFIG,
    featureFlagEnabled: import.meta.env.VITE_VALIDATE_UPLOADS !== 'false'
  });
  
  // Dropzone configuration
  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop: handleFileDrop,
    accept: {
      'text/plain': ['.txt']
    },
    maxFiles: 1,
    disabled: isValidating || uploading
  });
  
  /**
   * Handler: Archivo seleccionado/dropeado
   */
  async function handleFileDrop(acceptedFiles: File[]) {
    const file = acceptedFiles[0];
    if (!file) return;
    
    // Guardar archivo
    setSelectedFile(file);
    
    // Ejecutar validación
    await validate(file);
  }
  
  /**
   * Handler: Confirmar y subir archivo (después de validación)
   */
  async function handleConfirmUpload() {
    if (!selectedFile || !validationResult?.isValid) {
      return;
    }
    
    try {
      setUploading(true);
      
      // Llamar al handler original del componente padre
      await onFileSelect(selectedFile);
      
      // Éxito
      onUploadComplete?.({ fileName: selectedFile.name });
      
      // Resetear estado
      handleReset();
      
    } catch (error) {
      console.error('Upload error:', error);
      onUploadError?.(error instanceof Error ? error : new Error('Upload failed'));
      setUploading(false);
    }
  }
  
  /**
   * Handler: Cancelar o seleccionar otro archivo
   */
  function handleReset() {
    setSelectedFile(null);
    setUploading(false);
    resetValidation();
  }
  
  // Estados del componente
  const showDropzone = !selectedFile && !isValidating;
  const showValidation = selectedFile && (isValidating || validationResult);
  const canUpload = validationResult?.isValid && !uploading;
  
  return (
    <div className="space-y-4">
      {/* Dropzone (mostrar solo si no hay archivo seleccionado) */}
      {showDropzone && (
        <div
          {...getRootProps()}
          className={`
            border-2 border-dashed rounded-lg p-8 text-center cursor-pointer
            transition-colors
            ${isDragActive 
              ? 'border-blue-500 bg-blue-50' 
              : 'border-gray-300 hover:border-gray-400 bg-gray-50'
            }
          `}
        >
          <input {...getInputProps()} />
          
          <div className="flex flex-col items-center">
            <svg
              className="w-12 h-12 text-gray-400 mb-3"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
              />
            </svg>
            
            <p className="text-lg font-medium text-gray-700 mb-1">
              {isDragActive
                ? 'Suelta el archivo aquí'
                : 'Arrastra tu archivo .txt aquí'
              }
            </p>
            <p className="text-sm text-gray-500">
              o haz clic para seleccionar
            </p>
            <p className="text-xs text-gray-400 mt-2">
              Solo archivos .txt • Máximo 50MB
            </p>
          </div>
        </div>
      )}
      
      {/* Spinner de validación */}
      {isValidating && (
        <div className="flex items-center justify-center py-8">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
          <span className="ml-3 text-gray-600">
            Validando archivo... (Estado: {validationState})
          </span>
        </div>
      )}
      
      {/* Preview del archivo (siempre mostrar si hay archivo) */}
      {selectedFile && !isValidating && (
        <FilePreview 
          file={selectedFile}
          highlightLines={[4]}
        />
      )}
      
      {/* Resultado de validación */}
      {validationResult && !isValidating && (
        <FileValidationResult
          result={validationResult}
          onConfirm={handleConfirmUpload}
          onCancel={handleReset}
        />
      )}
      
      {/* Indicador de upload en progreso */}
      {uploading && (
        <div className="flex items-center justify-center py-4 bg-blue-50 rounded-lg">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600" />
          <span className="ml-3 text-blue-700">
            Subiendo archivo al servidor...
          </span>
        </div>
      )}
    </div>
  );
}
```

---

## 4. Flujo de Estados

### 4.1. Diagrama de Flujo

```
[INICIAL]
  ↓
[Mostrar Dropzone]
  ↓ Usuario selecciona/dropea archivo
[Ocultar Dropzone]
[Mostrar Spinner "Validando..."]
  ↓ validate(file)
[Validación completa]
  ↓
[Mostrar FilePreview]
[Mostrar FileValidationResult]
  ↓
  ├─ Si isValid=true → [Botón "Confirmar" habilitado]
  │    ↓ Usuario confirma
  │    [Spinner "Subiendo..."]
  │    ↓ onFileSelect(file)
  │    [Upload completo]
  │    ↓
  │    [Resetear todo] → INICIAL
  │
  └─ Si isValid=false → [Botón "Seleccionar otro" visible]
       ↓ Usuario cancela
       [Resetear todo] → INICIAL
```

### 4.2. Matriz de Estados

| Estado | Dropzone | Spinner | Preview | ValidationResult | Uploading |
|---|---|---|---|---|---|
| Inicial | ✅ Visible | ❌ | ❌ | ❌ | ❌ |
| Validando | ❌ | ✅ Visible | ❌ | ❌ | ❌ |
| Validado (OK) | ❌ | ❌ | ✅ Visible | ✅ READY | ❌ |
| Validado (Error) | ❌ | ❌ | ✅ Visible | ✅ BLOCKED | ❌ |
| Uploading | ❌ | ❌ | ✅ Visible | ❌ | ✅ Spinner |
| Completado | ✅ Visible | ❌ | ❌ | ❌ | ❌ |

---

## 5. Props y Callbacks

### 5.1. Props Requeridas (sin cambios)

```typescript
interface DataUploadDropzoneProps {
  onFileSelect: (file: File) => void;      // ✅ Mantener
  onUploadComplete?: (result: any) => void; // ✅ Mantener
  onUploadError?: (error: Error) => void;   // ✅ Mantener
}
```

### 5.2. Nuevos Props Opcionales (si se requiere customización)

```typescript
interface DataUploadDropzoneProps {
  // ... props existentes
  
  // Nuevos (opcionales)
  validationConfig?: Partial<FileValidationConfig>;
  showPreview?: boolean;  // Default: true
  showValidationSummary?: boolean; // Default: true
}
```

---

## 6. Manejo de Errores

### 6.1. Errores de Validación

```typescript
// Errores de validación se muestran en FileValidationResult
// No se llama a onFileSelect si validación falla
if (!validationResult?.isValid) {
  // Bloquear upload
  return;
}
```

### 6.2. Errores de Upload

```typescript
try {
  await onFileSelect(selectedFile);
  onUploadComplete?.({ fileName: selectedFile.name });
} catch (error) {
  // Capturar y notificar
  onUploadError?.(error instanceof Error ? error : new Error('Upload failed'));
  
  // Mantener archivo seleccionado para reintentar
  setUploading(false);
}
```

---

## 7. Feature Flag

### 7.1. Activación/Desactivación

```typescript
// .env
VITE_VALIDATE_UPLOADS=true  # Activar validación
# VITE_VALIDATE_UPLOADS=false  # Desactivar (permitir upload directo)
```

### 7.2. Comportamiento con Flag Deshabilitado

```typescript
const {
  validate,
  // ...
} = useFileValidation({
  ...DEFAULT_FILE_VALIDATION_CONFIG,
  featureFlagEnabled: import.meta.env.VITE_VALIDATE_UPLOADS !== 'false'
});

// Si flag=false, validate() pasa automáticamente y permite upload
```

---

## 8. Testing

### 8.1. Integration Tests

```typescript
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { DataUploadDropzone } from './DataUploadDropzone';

describe('DataUploadDropzone - Integración con Validación', () => {
  it('muestra dropzone inicialmente', () => {
    render(<DataUploadDropzone onFileSelect={vi.fn()} />);
    
    expect(screen.getByText(/arrastra tu archivo/i)).toBeInTheDocument();
  });
  
  it('valida archivo al seleccionarlo', async () => {
    const user = userEvent.setup();
    const mockOnFileSelect = vi.fn();
    
    render(<DataUploadDropzone onFileSelect={mockOnFileSelect} />);
    
    const validFile = new File(
      ['Fecha|Maquina\n15/01/2024|MAQ001'],
      'valid.txt',
      { type: 'text/plain' }
    );
    
    const input = screen.getByLabelText(/arrastra/i, { selector: 'input' });
    await user.upload(input, validFile);
    
    // Debe mostrar spinner de validación
    expect(screen.getByText(/validando/i)).toBeInTheDocument();
    
    // Esperar a que termine validación
    await waitFor(() => {
      expect(screen.queryByText(/validando/i)).not.toBeInTheDocument();
    });
    
    // Debe mostrar preview y resultado
    expect(screen.getByText(/preview del archivo/i)).toBeInTheDocument();
    expect(screen.getByText(/archivo válido/i)).toBeInTheDocument();
  });
  
  it('habilita botón Confirmar solo si validación pasa', async () => {
    const user = userEvent.setup();
    const mockOnFileSelect = vi.fn();
    
    render(<DataUploadDropzone onFileSelect={mockOnFileSelect} />);
    
    const validFile = new File(['Fecha|Maquina\n15/01/2024|MAQ001'], 'v.txt');
    const input = screen.getByLabelText(/arrastra/i, { selector: 'input' });
    
    await user.upload(input, validFile);
    
    await waitFor(() => {
      expect(screen.getByLabelText(/confirmar y subir/i)).toBeInTheDocument();
    });
    
    const confirmButton = screen.getByLabelText(/confirmar y subir/i);
    expect(confirmButton).not.toBeDisabled();
  });
  
  it('bloquea upload si validación falla', async () => {
    const user = userEvent.setup();
    const mockOnFileSelect = vi.fn();
    
    render(<DataUploadDropzone onFileSelect={mockOnFileSelect} />);
    
    const invalidFile = new File(['data'], 'data.xlsx');
    const input = screen.getByLabelText(/arrastra/i, { selector: 'input' });
    
    await user.upload(input, invalidFile);
    
    await waitFor(() => {
      expect(screen.getByText(/archivo con errores/i)).toBeInTheDocument();
    });
    
    // No debe haber botón "Confirmar"
    expect(screen.queryByLabelText(/confirmar y subir/i)).not.toBeInTheDocument();
    
    // onFileSelect NO debe haber sido llamado
    expect(mockOnFileSelect).not.toHaveBeenCalled();
  });
  
  it('permite cancelar y seleccionar otro archivo', async () => {
    const user = userEvent.setup();
    
    render(<DataUploadDropzone onFileSelect={vi.fn()} />);
    
    const file = new File(['data'], 'data.xlsx');
    const input = screen.getByLabelText(/arrastra/i, { selector: 'input' });
    
    await user.upload(input, file);
    
    await waitFor(() => {
      expect(screen.getByText(/seleccionar otro archivo/i)).toBeInTheDocument();
    });
    
    // Click en "Seleccionar otro archivo"
    const cancelButton = screen.getByText(/seleccionar otro archivo/i);
    await user.click(cancelButton);
    
    // Debe volver a mostrar dropzone
    expect(screen.getByText(/arrastra tu archivo/i)).toBeInTheDocument();
  });
  
  it('sube archivo después de confirmar', async () => {
    const user = userEvent.setup();
    const mockOnFileSelect = vi.fn().mockResolvedValue(undefined);
    
    render(<DataUploadDropzone onFileSelect={mockOnFileSelect} />);
    
    const validFile = new File(['Fecha|Maquina\n15/01/2024|MAQ001'], 'v.txt');
    const input = screen.getByLabelText(/arrastra/i, { selector: 'input' });
    
    await user.upload(input, validFile);
    
    await waitFor(() => {
      expect(screen.getByLabelText(/confirmar y subir/i)).toBeInTheDocument();
    });
    
    const confirmButton = screen.getByLabelText(/confirmar y subir/i);
    await user.click(confirmButton);
    
    // Debe llamar a onFileSelect con el archivo
    await waitFor(() => {
      expect(mockOnFileSelect).toHaveBeenCalledWith(validFile);
    });
  });
});
```

---

## 9. Rollback Plan

### 9.1. Desactivar Validación Temporalmente

Si hay problemas en producción:

```bash
# En .env o variables de entorno de Vercel
VITE_VALIDATE_UPLOADS=false
```

Luego redeploy. El componente volverá a comportamiento anterior (upload directo sin validación).

### 9.2. Rollback de Código

Si se necesita revertir completamente:

```bash
git revert <commit-hash-integracion>
```

---

## 10. Checklist de Integración

- [ ] Leer componente actual `DataUploadDropzone.tsx`
- [ ] Importar hook `useFileValidation`
- [ ] Importar componentes `FilePreview` y `FileValidationResult`
- [ ] Modificar `handleDrop` para validar en lugar de subir directamente
- [ ] Añadir `handleConfirmUpload` que llama a `onFileSelect` original
- [ ] Añadir `handleReset` para limpiar estado
- [ ] Añadir renderizado condicional (dropzone/validating/results)
- [ ] Configurar feature flag con `VITE_VALIDATE_UPLOADS`
- [ ] Escribir integration tests
- [ ] Probar manualmente flujo completo
- [ ] Validar que componente padre sigue funcionando igual

---

## 11. Migración Incremental

### 11.1. Paso 1: Agregar Validación Sin Bloqueo

```typescript
// Versión intermedia: validar pero permitir upload siempre
async function handleFileDrop(acceptedFiles: File[]) {
  const file = acceptedFiles[0];
  setSelectedFile(file);
  
  // Validar pero no bloquear
  await validate(file);
  
  // Mostrar resultado pero permitir continuar
  // (botón siempre habilitado)
}
```

### 11.2. Paso 2: Activar Bloqueo con Feature Flag

```typescript
// Versión final: bloquear si flag activado
const shouldBlock = import.meta.env.VITE_VALIDATE_UPLOADS === 'true';

if (shouldBlock && !validationResult?.isValid) {
  // Bloquear upload
  return;
}
```

---

## 12. Referencias
- TD: `.gemini/sprints/technical-designs/TD-001-validacion-carga-archivos-etl-v2.md`
- FTV-001: FileValidator
- FTV-002: useFileValidation
- FTV-003: FilePreview
- FTV-004: FileValidationResult

---

## 13. Especificaciones de Request

### 13.1. Estructura de Request

**Request** (multipart/form-data):
```typescript
// Headers
Authorization: Bearer <jwt>
Content-Type: multipart/form-data

// Body
{
  file: File,              // Archivo validado por frontend
  encoding: 'UTF-8' | 'Windows-1252',  // Detectado por frontend (coordinar con backend)
  metadata: {
    linesValidated: number,
    validationTimestamp: string  // ISO 8601
  }
}
```

**Note**: El backend procesa archivos en `Windows-1252`. El frontend debe reportar el encoding detectado (p.ej. `Windows-1252`) y el backend debe revalidar y aceptar/convertir cuando sea necesario.

### 13.2. Ejemplo de Envío (incluyendo encoding)

```typescript
async function uploadToBackend(file: File, result: ValidationResult) {
  const form = new FormData();
  form.append('file', file);
  form.append('encoding', result.summary.encoding); // 'UTF-8' o 'Windows-1252'
  form.append(
    'metadata',
    JSON.stringify({
      linesValidated: result.summary.dataSample?.linesValidated ?? 0,
      validationTimestamp: new Date().toISOString()
    })
  );

  const res = await fetch('/api/v1/etl/start-process', {
    method: 'POST',
    headers: {
      // Authorization agregado por capa superior si aplica
    },
    body: form
  });

  if (!res.ok) {
    throw new Error('Upload failed');
  }
}
```
