# FTV-002: useFileValidation (Hook)

## Metadata
- **ID**: FTV-002
- **Technical Design**: TD-001-validacion-carga-archivos-etl-v2.md
- **Tipo**: Custom Hook
- **Ubicación**: `src/hooks/useFileValidation.ts`
- **Sprint**: Sprint 2
- **Fecha Creación**: 2025-10-19
- **Estado**: Draft
- **Dependencias**: FTV-001 (FileValidator)

---

## 1. Propósito

### 1.1. Descripción
Hook personalizado que orquesta el proceso de validación de archivos, gestionando la máquina de estados (IDLE → PRECHECK → READING → PARSING_HEADERS → SAMPLING → READY/BLOCKED/ERROR), el estado de carga y el resultado de validación.

Nota sobre encoding: El hook reporta el encoding detectado por `FileValidator` y lo propaga vía `result.summary.encoding`. La heurística es UTF-8 por defecto y fallback a Windows-1252 (alineado con el backend).

### 1.2. Caso de Uso
```
Como componente que necesita validar archivos,
Cuando invoco useFileValidation(config),
Entonces obtengo funciones para validar archivos y estado reactivo,
Para mostrar feedback al usuario durante el proceso de validación.
```

### 1.3. Responsabilidades
- Gestionar máquina de estados de validación
- Invocar FileValidator y capturar resultado
- Proveer estado de loading/error
- Permitir reset del estado
- Medir telemetría (duración, éxito/fallo, encoding detectado)

---

## 2. API del Hook

### 2.1. Signature

```typescript
function useFileValidation(
  config?: Partial<FileValidationConfig>
): UseFileValidationReturn
```

### 2.2. Return Type

```typescript
interface UseFileValidationReturn {
  // Estado
  state: ValidationState;
  result: ValidationResult | null;
  isValidating: boolean;
  
  // Acciones
  validate: (file: File) => Promise<void>;
  reset: () => void;
  
  // Metadata
  validationDuration: number | null; // ms
}

enum ValidationState {
  IDLE = 'IDLE',
  PRECHECK = 'PRECHECK',
  READING = 'READING',
  PARSING_HEADERS = 'PARSING_HEADERS',
  SAMPLING = 'SAMPLING',
  SUMMARY = 'SUMMARY',
  READY = 'READY',
  BLOCKED = 'BLOCKED',
  ERROR = 'ERROR'
}
```

---

## 3. Implementación

### 3.1. Código Completo

```typescript
import { useState, useCallback, useRef } from 'react';
import { validateFile, DEFAULT_FILE_VALIDATION_CONFIG } from '@/services/FileValidator';
import type { 
  FileValidationConfig, 
  ValidationResult 
} from '@/types/file-validation.types';

export enum ValidationState {
  IDLE = 'IDLE',
  PRECHECK = 'PRECHECK',
  READING = 'READING',
  PARSING_HEADERS = 'PARSING_HEADERS',
  SAMPLING = 'SAMPLING',
  SUMMARY = 'SUMMARY',
  READY = 'READY',
  BLOCKED = 'BLOCKED',
  ERROR = 'ERROR'
}

export interface UseFileValidationReturn {
  state: ValidationState;
  result: ValidationResult | null;
  isValidating: boolean;
  validate: (file: File) => Promise<void>;
  reset: () => void;
  validationDuration: number | null;
}

export function useFileValidation(
  configOverride?: Partial<FileValidationConfig>
): UseFileValidationReturn {
  // Merge config con defaults
  const config = {
    ...DEFAULT_FILE_VALIDATION_CONFIG,
    ...configOverride
  };
  
  // Estado
  const [state, setState] = useState<ValidationState>(ValidationState.IDLE);
  const [result, setResult] = useState<ValidationResult | null>(null);
  const [validationDuration, setValidationDuration] = useState<number | null>(null);
  
  // Ref para cancelación (si se implementa en el futuro)
  const abortControllerRef = useRef<AbortController | null>(null);
  
  // Derivados
  const isValidating = [
    ValidationState.PRECHECK,
    ValidationState.READING,
    ValidationState.PARSING_HEADERS,
    ValidationState.SAMPLING,
    ValidationState.SUMMARY
  ].includes(state);
  
  /**
   * Valida un archivo
   */
  const validate = useCallback(async (file: File) => {
    const startTime = performance.now();
    
    try {
      // Check feature flag
      if (!config.featureFlagEnabled) {
        console.warn('File validation is disabled by feature flag');
        setState(ValidationState.READY);
        setResult({
          isValid: true,
          errors: [],
          warnings: [],
          summary: {} as any // Skip validation
        });
        return;
      }
      
      // Telemetría: inicio
      logTelemetry('val.start', {
        fileName: file.name,
        sizeBytes: file.size
      });
      
      // Estados intermedios (UI friendly)
      setState(ValidationState.PRECHECK);
      await sleep(10);
      setState(ValidationState.READING);
      setState(ValidationState.PARSING_HEADERS);
      setState(ValidationState.SAMPLING);
      
      // Ejecutar validación
      const validationResult = await validateFile(file, config);
      
      // Estado: SUMMARY
      setState(ValidationState.SUMMARY);
      setResult(validationResult);
      
      // Estado final
      const finalState = validationResult.isValid 
        ? ValidationState.READY 
        : ValidationState.BLOCKED;
      setState(finalState);
      
      // Telemetría: fin (incluye encoding 'UTF-8' o 'Windows-1252')
      const duration = performance.now() - startTime;
      setValidationDuration(duration);
      
      logTelemetry(
        validationResult.isValid ? 'val.ready' : 'val.blocked',
        {
          fileName: file.name,
          sizeBytes: file.size,
          durationMs: duration,
          encoding: validationResult.summary.encoding,
          errorsCount: validationResult.errors.length,
          warningsCount: validationResult.warnings.length
        }
      );
      
    } catch (error) {
      console.error('useFileValidation error:', error);
      
      setState(ValidationState.ERROR);
      setResult({
        isValid: false,
        errors: [{
          code: 'INTERNAL_ERROR' as any,
          severity: 'ERROR' as any,
          message: 'Error inesperado durante validación'
        }],
        warnings: [],
        summary: {} as any
      });
      
      // Telemetría: error
      const duration = performance.now() - startTime;
      setValidationDuration(duration);
      
      logTelemetry('val.error', {
        fileName: file.name,
        sizeBytes: file.size,
        durationMs: duration,
        error: error instanceof Error ? error.message : 'Unknown error'
      });
    }
  }, [config]);
  
  /**
   * Resetea el estado del hook
   */
  const reset = useCallback(() => {
    setState(ValidationState.IDLE);
    setResult(null);
    setValidationDuration(null);
    
    // Cancelar validación en progreso si existe
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
      abortControllerRef.current = null;
    }
  }, []);
  
  return {
    state,
    result,
    isValidating,
    validate,
    reset,
    validationDuration
  };
}

// Helpers

function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms));
}

function logTelemetry(event: string, data: Record<string, any>): void {
  // En desarrollo: consola
  if (import.meta.env.DEV) {
    console.log(`[Telemetry] ${event}`, data);
  }
  
  // En producción: enviar a servicio de telemetría
  if (import.meta.env.PROD) {
    // TODO: Implementar cuando exista services/telemetry.ts
    // telemetryService.track(event, data);
  }
}

// Nota: El encoding reportado en telemetría es importante para detectar discrepancias con el backend (que usa Windows-1252). Asegúrate de que el backend vuelva a validar o convierta cuando sea necesario.
```

---

## 4. Estados y Transiciones

### 4.1. Máquina de Estados

```
IDLE
  ↓ validate(file)
PRECHECK (validar ext/tamaño)
  ↓ si OK
READING (leer chunk)
  ↓
PARSING_HEADERS (validar headers)
  ↓ si OK
SAMPLING (validar datos)
  ↓
SUMMARY (compilar resultado)
  ↓
READY (isValid=true) | BLOCKED (isValid=false)

ERROR (cualquier excepción)
  ↑
  └─ desde cualquier estado
```

### 4.2. Transiciones

| Estado Actual | Evento | Estado Siguiente | Condición |
|---|---|---|---|
| IDLE | `validate()` | PRECHECK | Siempre |
| PRECHECK | Ext/tamaño OK | READING | Validación pasa |
| PRECHECK | Ext/tamaño FAIL | BLOCKED | Validación falla |
| READING | Lectura OK | PARSING_HEADERS | Sin errores |
| PARSING_HEADERS | Headers OK | SAMPLING | Headers coinciden |
| PARSING_HEADERS | Headers FAIL | BLOCKED | Headers no coinciden |
| SAMPLING | Datos OK | SUMMARY | Sin errores críticos |
| SAMPLING | Datos FAIL | SUMMARY | Con errores (se compila igual) |
| SUMMARY | isValid=true | READY | Sin errores |
| SUMMARY | isValid=false | BLOCKED | Con errores |
| Cualquiera | Excepción | ERROR | try/catch |
| Cualquiera | `reset()` | IDLE | Siempre |

---

## 5. Ejemplo de Uso

### 5.1. Uso Básico

```typescript
function DataUploadComponent() {
  const { 
    state, 
    result, 
    isValidating, 
    validate, 
    reset 
  } = useFileValidation();
  
  const handleFileSelect = async (file: File) => {
    await validate(file);
  };
  
  return (
    <div>
      <input 
        type="file" 
        onChange={(e) => e.target.files?.[0] && handleFileSelect(e.target.files[0])}
        disabled={isValidating}
      />
      
      {isValidating && (
        <div>Validando... (Estado: {state})</div>
      )}
      
      {result && (
        <ValidationResult result={result} />
      )}
      
      {state === 'READY' && (
        <button onClick={() => uploadFile()}>
          Confirmar y Subir
        </button>
      )}
      
      {state === 'BLOCKED' && (
        <button onClick={reset}>
          Seleccionar otro archivo
        </button>
      )}
    </div>
  );
}
```

### 5.2. Uso con Configuración Custom

```typescript
function CustomValidation() {
  const validation = useFileValidation({
    sampleLines: 20,        // Validar 20 líneas en lugar de 10
    maxBytes: 100 * 1024 * 1024, // Permitir 100MB
    delimiter: ';'          // Usar punto y coma
  });
  
  // ... resto del componente
}
```

### 5.3. Uso con Telemetría

```typescript
function MonitoredUpload() {
  const { validate, validationDuration, result } = useFileValidation();
  
  const handleValidate = async (file: File) => {
    await validate(file);
    
    // Registrar métrica
    if (validationDuration) {
      console.log(`Validación completada en ${validationDuration}ms`);
      
      if (validationDuration > 1000) {
        console.warn('Validación lenta detectada');
      }
    }
  };
  
  return (
    <div>
      {/* UI */}
      {validationDuration && (
        <small>Validado en {validationDuration.toFixed(0)}ms</small>
      )}
    </div>
  );
}
```

---

## 6. Manejo de Errores

### 6.1. Errores Capturados

```typescript
// Error en FileValidator (extensión inválida, headers incorrectos, etc.)
// → Estado: BLOCKED
// → result.isValid = false
// → result.errors contiene detalles

// Excepción no controlada (crash del FileValidator)
// → Estado: ERROR
// → result con error genérico INTERNAL_ERROR
```

### 6.2. Degradación Graciosa

Si el feature flag está deshabilitado:
```typescript
if (!config.featureFlagEnabled) {
  // Skip validación, permitir upload
  setState(ValidationState.READY);
  setResult({ isValid: true, /* ... */ });
}
```

---

## 7. Performance

### 7.1. Optimizaciones

- **useCallback**: `validate` y `reset` están memoizados para evitar re-renders
- **Early exit**: Si feature flag deshabilitado, skip validación inmediatamente
- **No bloquea UI**: Estados intermedios permiten mostrar spinners

### 7.2. Métricas

```typescript
// Medir duración total
const startTime = performance.now();
// ... validación ...
const duration = performance.now() - startTime;
setValidationDuration(duration);
```

---

## 8. Testing

### 8.1. Tests del Hook

```typescript
import { renderHook, act } from '@testing-library/react';
import { useFileValidation } from './useFileValidation';

describe('useFileValidation', () => {
  it('inicia en estado IDLE', () => {
    const { result } = renderHook(() => useFileValidation());
    
    expect(result.current.state).toBe('IDLE');
    expect(result.current.result).toBeNull();
    expect(result.current.isValidating).toBe(false);
  });
  
  it('valida archivo correcto y transiciona a READY', async () => {
    const { result } = renderHook(() => useFileValidation());
    
    const validFile = new File(
      ['Fecha|Maquina\n15/01/2024|MAQ001'],
      'valid.txt',
      { type: 'text/plain' }
    );
    
    await act(async () => {
      await result.current.validate(validFile);
    });
    
    expect(result.current.state).toBe('READY');
    expect(result.current.result?.isValid).toBe(true);
    expect(result.current.validationDuration).toBeGreaterThan(0);
  });
  
  it('bloquea archivo con extensión incorrecta', async () => {
    const { result } = renderHook(() => useFileValidation());
    
    const invalidFile = new File(['data'], 'data.xlsx');
    
    await act(async () => {
      await result.current.validate(invalidFile);
    });
    
    expect(result.current.state).toBe('BLOCKED');
    expect(result.current.result?.isValid).toBe(false);
    expect(result.current.result?.errors.length).toBeGreaterThan(0);
  });
  
  it('resetea estado correctamente', async () => {
    const { result } = renderHook(() => useFileValidation());
    
    const file = new File(['data'], 'data.xlsx');
    
    await act(async () => {
      await result.current.validate(file);
    });
    
    expect(result.current.state).toBe('BLOCKED');
    
    act(() => {
      result.current.reset();
    });
    
    expect(result.current.state).toBe('IDLE');
    expect(result.current.result).toBeNull();
    expect(result.current.validationDuration).toBeNull();
  });
  
  it('maneja errores de validación', async () => {
    const { result } = renderHook(() => useFileValidation());
    
    // Simular archivo que cause error interno
    const problematicFile = new File(
      [new ArrayBuffer(1024)],
      'binary.txt'
    );
    
    await act(async () => {
      await result.current.validate(problematicFile);
    });
    
    // Debe manejar gracefully
    expect(result.current.state).toMatch(/BLOCKED|ERROR/);
  });
  
  it('respeta feature flag deshabilitado', async () => {
    const { result } = renderHook(() => 
      useFileValidation({ featureFlagEnabled: false })
    );
    
    const file = new File(['invalid'], 'data.xlsx');
    
    await act(async () => {
      await result.current.validate(file);
    });
    
    // Debe permitir sin validar
    expect(result.current.state).toBe('READY');
    expect(result.current.result?.isValid).toBe(true);
  });
});
```

---

## 9. Telemetría

### 9.1. Eventos Rastreados

| Evento | Cuándo | Payload |
|---|---|---|
| `val.start` | Inicio de validación | `{ fileName, sizeBytes }` |
| `val.ready` | Validación exitosa | `{ fileName, sizeBytes, durationMs, encoding, errorsCount: 0 }` |
| `val.blocked` | Validación con errores | `{ fileName, sizeBytes, durationMs, encoding, errorsCount, warningsCount }` |
| `val.error` | Excepción durante validación | `{ fileName, sizeBytes, durationMs, error }` |

Nota: `encoding` será `'UTF-8'` o `'Windows-1252'`. Este dato es clave para detectar problemas de exportación y alinear con el backend.

### 9.2. Implementación Futura

```typescript
// services/telemetry.ts (placeholder)
export const telemetryService = {
  track(event: string, data: Record<string, any>) {
    // Enviar a backend o servicio analytics
    fetch('/api/v1/telemetry', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ event, data, timestamp: new Date().toISOString() })
    });
  }
};
```

---

## 10. Dependencias

### 10.1. Internas
- `@/services/FileValidator` (FTV-001)
- `@/types/file-validation.types`
- React hooks: `useState`, `useCallback`, `useRef`

### 10.2. Externas
- Ninguna

---

## 11. Variables de Entorno

```typescript
// Leer feature flag
const isEnabled = import.meta.env.VITE_VALIDATE_UPLOADS === 'true';

// En config
const config = {
  ...DEFAULT_FILE_VALIDATION_CONFIG,
  featureFlagEnabled: isEnabled,
  ...configOverride
};
```

Archivo `.env`:
```bash
VITE_VALIDATE_UPLOADS=true
```

---

## 12. Checklist de Implementación

- [ ] Crear `src/hooks/useFileValidation.ts`
- [ ] Implementar máquina de estados
- [ ] Implementar función `validate`
- [ ] Implementar función `reset`
- [ ] Añadir telemetría (logs en dev, servicio en prod)
- [ ] Escribir tests con `renderHook`
- [ ] Validar transiciones de estados
- [ ] Probar feature flag on/off
- [ ] Documentar en Storybook (opcional)

---

## 13. Referencias
- TD: `.gemini/sprints/technical-designs/TD-001-validacion-carga-archivos-etl-v2.md`
- FTV-001: FileValidator
- React hooks: https://react.dev/reference/react/hooks
