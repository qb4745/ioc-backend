# FTV-001: FileValidator (Servicio)

## Metadata
- **ID**: FTV-001
- **Technical Design**: TD-001-validacion-carga-archivos-etl-v2.md
- **Tipo**: Servicio (Service/Utility)
- **Ubicación**: `src/services/FileValidator.ts`
- **Sprint**: Sprint 2
- **Fecha Creación**: 2025-10-19
- **Estado**: Draft

---

## 1. Propósito

### 1.1. Descripción
Servicio utilitario que valida archivos TXT del proceso ETL en el lado del cliente antes de subirlos al backend. Implementa validaciones progresivas (extensión, tamaño, encoding, headers, muestreo de datos) y retorna un resultado estructurado con errores, warnings y resumen.

### 1.2. Caso de Uso
```
Como administrador que carga archivos ETL,
Cuando selecciono un archivo en el dropzone,
Entonces el sistema valida el archivo usando FileValidator,
Para mostrarme inmediatamente si hay errores sin esperar el upload al servidor.
```

### 1.3. Ubicación en la App
- Invocado por el hook `useFileValidation`
- Usado en `DataUploadDropzone` antes de permitir la carga
- No tiene UI propia (servicio puro)

---

## 2. API Pública del Servicio

### 2.1. Función Principal: `validateFile`

```typescript
/**
 * Valida un archivo TXT para el proceso ETL
 * @param file - Archivo a validar
 * @param config - Configuración de validación
 * @returns Resultado de validación con errores, warnings y resumen
 */
export async function validateFile(
  file: File,
  config: FileValidationConfig
): Promise<ValidationResult>
```

### 2.2. Configuración

```typescript
export interface FileValidationConfig {
  maxBytes: number;              // 50 * 1024 * 1024 (50MB)
  headerExpected: string[];      // Encabezados esperados
  previewLines: number;          // 14
  sampleLines: number;           // 10
  readBytes: number;             // 100 * 1024
  delimiter: '|' | ';' | ',';    // '|'
  skipValidationLines: number[]; // [4] - línea de totales SAP
  strictMode: true;
  locale: 'es' | 'en';
  featureFlagEnabled: boolean;
}
```

### 2.3. Tipos de Retorno

```typescript
export interface ValidationResult {
  isValid: boolean;
  errors: ValidationIssue[];
  warnings: ValidationIssue[];
  summary: ValidationSummary;
}

export interface ValidationIssue {
  code: ValidationCode;
  severity: ValidationSeverity;
  field?: string;
  line?: number;
  value?: string;
  message: string;      // i18n key
  suggestion?: string;  // i18n key
}

export interface ValidationSummary {
  extension: 'valid' | 'invalid';
  size: { current: number; max: number; valid: boolean };
  encoding: Encoding;
  headers: { found: string[]; expected: string[]; valid: boolean };
  dataSample: { linesValidated: number; errors: number; warnings: number };
}

export enum ValidationCode {
  EXTENSION_NOT_ALLOWED = 'EXTENSION_NOT_ALLOWED',
  FILE_TOO_LARGE = 'FILE_TOO_LARGE',
  EMPTY_FILE = 'EMPTY_FILE',
  ENCODING_UNDETECTED = 'ENCODING_UNDETECTED',
  HEADERS_MISMATCH = 'HEADERS_MISMATCH',
  REQUIRED_FIELD_EMPTY = 'REQUIRED_FIELD_EMPTY',
  INVALID_NUMBER = 'INVALID_NUMBER',
  INVALID_DATE = 'INVALID_DATE',
  INVALID_TIME = 'INVALID_TIME',
  INTERNAL_ERROR = 'INTERNAL_ERROR'
}
```

---

## 3. Algoritmos e Implementación

### 3.1. Flujo Principal (validateFile)

```typescript
export async function validateFile(
  file: File,
  cfg: FileValidationConfig
): Promise<ValidationResult> {
  // Fase 1: Prechecks (extensión y tamaño)
  const errors = validateExtensionAndSize(file, cfg);
  if (errors.length > 0) {
    return summarize({ errors, warnings: [] }, { extension: 'invalid' });
  }

  // Fase 2: Lectura parcial y detección de encoding
  const chunk = new Uint8Array(await file.slice(0, cfg.readBytes).arrayBuffer());
  const encoding = await detectEncoding(chunk);
  const text = new TextDecoder(encoding === 'Windows-1252' ? 'windows-1252' : 'utf-8').decode(chunk);

  // Fase 3: Parsing y validación de headers
  const lines = splitLines(text);
  const headerLine = lines[0] ?? '';
  const { ok, found } = parseHeaders(headerLine, cfg.delimiter, cfg.headerExpected);
  
  if (!ok) {
    errors.push({
      code: ValidationCode.HEADERS_MISMATCH,
      severity: ValidationSeverity.ERROR,
      message: 'val.headers.mismatch'
    });
    return summarize({ errors, warnings: [] }, { encoding, foundHeaders: found });
  }

  // Fase 4: Validación de muestra de datos
  const samplingIssues = validateSampleData(lines, cfg);
  errors.push(...samplingIssues.filter(i => i.severity === ValidationSeverity.ERROR));
  const warnings = samplingIssues.filter(i => i.severity === ValidationSeverity.WARNING);

  // Fase 5: Compilar resultado
  return summarize(
    { errors, warnings },
    { encoding, foundHeaders: found, linesValidated: cfg.sampleLines }
  );
}
```

### 3.2. Helpers Principales

#### validateExtensionAndSize
```typescript
function validateExtensionAndSize(file: File, cfg: FileValidationConfig): ValidationIssue[] {
  const issues: ValidationIssue[] = [];
  const ext = (file.name.split('.').pop() || '').toLowerCase();
  
  if (ext !== 'txt') {
    issues.push({
      code: ValidationCode.EXTENSION_NOT_ALLOWED,
      severity: ValidationSeverity.ERROR,
      message: 'val.ext.notAllowed',
      suggestion: 'advice.export.pipe'
    });
  }
  
  if (file.size === 0) {
    issues.push({
      code: ValidationCode.EMPTY_FILE,
      severity: ValidationSeverity.ERROR,
      message: 'val.file.empty'
    });
  }
  
  if (file.size > cfg.maxBytes) {
    issues.push({
      code: ValidationCode.FILE_TOO_LARGE,
      severity: ValidationSeverity.ERROR,
      message: 'val.size.tooLarge',
      value: `${(file.size / 1024 / 1024).toFixed(2)}MB`
    });
  }
  
  return issues;
}
```

#### detectEncoding
```typescript
async function detectEncoding(view: Uint8Array): Promise<Encoding> {
  // Check BOM UTF-8
  const hasUtf8Bom = view[0] === 0xEF && view[1] === 0xBB && view[2] === 0xBF;
  if (hasUtf8Bom) return 'UTF-8';
  
  // Try strict UTF-8 decoding
  try {
    new TextDecoder('utf-8', { fatal: true }).decode(view);
    return 'UTF-8';
  } catch {
    // Fallback to Windows-1252 (used by backend)
    try {
      new TextDecoder('windows-1252').decode(view);
      return 'Windows-1252';
    } catch {
      return 'unknown';
    }
  }
}
```

#### parseHeaders
```typescript
function parseHeaders(
  firstLine: string,
  delimiter: string,
  expected: string[]
): { ok: boolean; found: string[]; missing: string[] } {
  const found = firstLine.split(delimiter).map(normalize);
  const exp = expected.map(normalize);
  const ok = exp.length === found.length && exp.every((e, i) => e === found[i]);
  const missing = ok ? [] : exp.filter(e => !found.includes(e));
  
  return { ok, found, missing };
}

function normalize(s: string): string {
  return s
    .toLowerCase()
    .trim()
    .replace(/\s+/g, ' ')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, ''); // Remove accents
}
```

#### validateSampleData
```typescript
function validateSampleData(lines: string[], cfg: FileValidationConfig): ValidationIssue[] {
  const issues: ValidationIssue[] = [];
  let sampled = 0;
  
  for (let i = 1; i < lines.length && sampled < cfg.sampleLines; i++) {
    // Skip línea 4 (totales SAP)
    if (i === 3) continue; // 0-indexed, línea 4 es índice 3
    
    const cols = lines[i].split(cfg.delimiter);
    const lineNum = i + 1;
    
    // Validar campos requeridos
    cfg.headerExpected.forEach((header, idx) => {
      const value = cols[idx]?.trim();
      if (!value || value === '') {
        issues.push({
          code: ValidationCode.REQUIRED_FIELD_EMPTY,
          severity: ValidationSeverity.ERROR,
          field: header,
          line: lineNum,
          message: 'val.req.empty'
        });
      }
    });
    
    // Validar campos numéricos (Cantidad, Peso neto)
    const cantidadIdx = cfg.headerExpected.findIndex(h => 
      normalize(h).includes('cantidad')
    );
    if (cantidadIdx >= 0 && cols[cantidadIdx]) {
      if (!isValidNumber(cols[cantidadIdx])) {
        issues.push({
          code: ValidationCode.INVALID_NUMBER,
          severity: ValidationSeverity.ERROR,
          field: 'Cantidad',
          line: lineNum,
          value: cols[cantidadIdx],
          message: 'val.num.invalid'
        });
      }
    }
    
    // Validar fechas
    const fechaIdx = cfg.headerExpected.findIndex(h =>
      normalize(h).includes('fecha de contabilizacion')
    );
    if (fechaIdx >= 0 && cols[fechaIdx]) {
      if (!isValidDate(cols[fechaIdx])) {
        issues.push({
          code: ValidationCode.INVALID_DATE,
          severity: ValidationSeverity.ERROR,
          field: 'Fecha de contabilizacion',
          line: lineNum,
          value: cols[fechaIdx],
          message: 'val.date.invalid'
        });
      }
    }
    
    sampled++;
  }
  
  return issues;
}

function isValidNumber(value: string): boolean {
  const normalized = value.replace(',', '.');
  return /^[-+]?\d+(\.\d+)?$/.test(normalized);
}

function isValidDate(value: string): boolean {
  // DD/MM/YYYY or DD-MM-YYYY
  const regex = /^(\d{2})[/-](\d{2})[/-](\d{4})$/;
  const match = value.match(regex);
  if (!match) return false;
  
  const [, day, month, year] = match;
  const d = parseInt(day, 10);
  const m = parseInt(month, 10);
  const y = parseInt(year, 10);
  
  if (d < 1 || d > 31) return false;
  if (m < 1 || m > 12) return false;
  if (y < 1900 || y > 2100) return false;
  
  return true;
}
```

#### splitLines
```typescript
function splitLines(text: string): string[] {
  return text.split(/\r?\n/).filter(line => line.trim() !== '');
}
```

#### summarize
```typescript
function summarize(
  issues: { errors: ValidationIssue[]; warnings: ValidationIssue[] },
  metadata: Partial<ValidationSummary>
): ValidationResult {
  return {
    isValid: issues.errors.length === 0,
    errors: issues.errors,
    warnings: issues.warnings,
    summary: {
      extension: metadata.extension || 'valid',
      size: metadata.size || { current: 0, max: 0, valid: true },
      encoding: metadata.encoding || 'UTF-8',
      headers: metadata.headers || { found: [], expected: [], valid: true },
      dataSample: {
        linesValidated: metadata.linesValidated || 0,
        errors: issues.errors.length,
        warnings: issues.warnings.length
      }
    }
  };
}
```

---

## 4. Manejo de Errores

### 4.1. Errores Capturados
- **FileReader falla**: Capturar y retornar `INTERNAL_ERROR`
- **TextDecoder falla**: Fallback a Windows-1252
- **Parsing falla**: Continuar validación y reportar lo que se pudo validar

### 4.2. Degradación Graciosa
```typescript
try {
  return await validateFile(file, config);
} catch (error) {
  console.error('FileValidator error:', error);
  return {
    isValid: false,
    errors: [{
      code: ValidationCode.INTERNAL_ERROR,
      severity: ValidationSeverity.ERROR,
      message: 'Error inesperado durante validación'
    }],
    warnings: [],
    summary: { /* valores por defecto */ }
  };
}
```

---

## 5. Performance

### 5.1. Optimizaciones
- **Lectura parcial**: Solo lee primeros 100KB del archivo
- **Early exit**: Detiene validación en errores críticos (extensión, headers)
- **Sampling limitado**: Solo valida primeras N líneas (configurable)

### 5.2. Métricas Objetivo
- Archivos pequeños (<1MB): < 100ms
- Archivos grandes (50MB): < 250ms (lee solo 100KB)

---

## 6. Testing

### 6.1. Unit Tests

```typescript
// validateExtensionAndSize
describe('validateExtensionAndSize', () => {
  it('rechaza archivos .xlsx', () => {
    const file = new File(['content'], 'data.xlsx', { type: 'application/vnd.ms-excel' });
    const config = createDefaultConfig();
    
    const issues = validateExtensionAndSize(file, config);
    
    expect(issues).toHaveLength(1);
    expect(issues[0].code).toBe(ValidationCode.EXTENSION_NOT_ALLOWED);
  });
  
  it('rechaza archivos >50MB', () => {
    const largeContent = new Array(51 * 1024 * 1024).fill('x').join('');
    const file = new File([largeContent], 'large.txt');
    const config = createDefaultConfig();
    
    const issues = validateExtensionAndSize(file, config);
    
    expect(issues.some(i => i.code === ValidationCode.FILE_TOO_LARGE)).toBe(true);
  });
  
  it('acepta archivos .txt válidos', () => {
    const file = new File(['content'], 'data.txt', { type: 'text/plain' });
    const config = createDefaultConfig();
    
    const issues = validateExtensionAndSize(file, config);
    
    expect(issues).toHaveLength(0);
  });
});

// detectEncoding
describe('detectEncoding', () => {
  it('detecta UTF-8 con BOM', async () => {
    const bom = new Uint8Array([0xEF, 0xBB, 0xBF, 0x48, 0x65, 0x6C, 0x6C, 0x6F]);
    
    const encoding = await detectEncoding(bom);
    
    expect(encoding).toBe('UTF-8');
  });
  
  it('detecta UTF-8 sin BOM', async () => {
    const content = new TextEncoder().encode('Hello World');
    
    const encoding = await detectEncoding(content);
    
    expect(encoding).toBe('UTF-8');
  });
  
  it('fallback a Windows-1252 si UTF-8 falla', async () => {
    const invalidUtf8 = new Uint8Array([0xFF, 0xFE]);
    
    const encoding = await detectEncoding(invalidUtf8);
    
    expect(encoding).toBe('Windows-1252');
  });
});

// parseHeaders
describe('parseHeaders', () => {
  const expected = ['Fecha de contabilizacion', 'Maquina', 'Numero de Log'];
  
  it('valida headers correctos', () => {
    const line = 'Fecha de contabilizacion|Maquina|Numero de Log';
    
    const result = parseHeaders(line, '|', expected);
    
    expect(result.ok).toBe(true);
    expect(result.missing).toHaveLength(0);
  });
  
  it('normaliza case-insensitive', () => {
    const line = 'FECHA DE CONTABILIZACION|MAQUINA|NUMERO DE LOG';
    
    const result = parseHeaders(line, '|', expected);
    
    expect(result.ok).toBe(true);
  });
  
  it('detecta headers faltantes', () => {
    const line = 'Fecha|Maquina';
    
    const result = parseHeaders(line, '|', expected);
    
    expect(result.ok).toBe(false);
    expect(result.missing.length).toBeGreaterThan(0);
  });
});

// validateFile (integration)
describe('validateFile', () => {
  it('valida archivo correcto completo', async () => {
    const content = `Fecha de contabilizacion|Maquina|Numero de Log|Cantidad
15/01/2024|MAQ001|123456|100
16/01/2024|MAQ002|123457|200`;
    
    const file = new File([content], 'valid.txt');
    const config = createDefaultConfig();
    
    const result = await validateFile(file, config);
    
    expect(result.isValid).toBe(true);
    expect(result.errors).toHaveLength(0);
  });
  
  it('detecta línea 4 y la ignora en validación', async () => {
    const content = `Fecha|Maquina|Log|Cantidad
15/01/2024|MAQ001|123|100
16/01/2024|MAQ002|124|200
17/01/2024|MAQ003|125|300
TOTALES SAP|invalid|invalid|invalid
18/01/2024|MAQ004|126|400`;
    
    const file = new File([content], 'with-sap-line.txt');
    const config = createDefaultConfig();
    
    const result = await validateFile(file, config);
    
    // No debe reportar errores de la línea 4
    expect(result.errors.every(e => e.line !== 4)).toBe(true);
  });
});
```

### 6.2. Fixtures
Ubicación: `test/fixtures/etl-upload/`
- `ok_utf8_10lines.txt`
- `headers_mismatch.txt`
- `wrong_extension.xlsx`
- `encoding_windows_1252.txt`
- `totals_line4.txt`
- `numeric_errors.txt`
- `date_time_errors.txt`

---

## 7. Dependencias

### 7.1. Externas
- Ninguna (usa APIs nativas del navegador)

### 7.2. Internas
- `types/file-validation.types.ts` (tipos compartidos)
- `utils/i18n.ts` (para claves de mensajes, opcional)

---

## 8. Configuración por Defecto

```typescript
export const DEFAULT_FILE_VALIDATION_CONFIG: FileValidationConfig = {
  maxBytes: 50 * 1024 * 1024, // 50MB
  headerExpected: [
    'Fecha de contabilizacion',
    'Maquina',
    'Numero de Log',
    'Hora de contabilizacion',
    'Fecha notificacion',
    'Material SKU',
    'Cantidad',
    'Peso neto',
    'Turno'
  ],
  previewLines: 14,
  sampleLines: 10,
  readBytes: 100 * 1024, // 100KB
  delimiter: '|',
  skipValidationLines: [4], // Línea de totales SAP (1-indexed)
  strictMode: true,
  locale: 'es',
  featureFlagEnabled: true
};
```

---

## 9. Checklist de Implementación

- [ ] Crear `src/services/FileValidator.ts`
- [ ] Implementar `validateFile` y helpers
- [ ] Crear `src/types/file-validation.types.ts` con interfaces
- [ ] Escribir tests unitarios (>80% cobertura)
- [ ] Crear fixtures de prueba
- [ ] Validar con archivos reales del negocio
- [ ] Documentar configuración en README

---

## 10. Referencias
- TD: `.gemini/sprints/technical-designs/TD-001-validacion-carga-archivos-etl-v2.md`
- TextDecoder API: https://developer.mozilla.org/en-US/docs/Web/API/TextDecoder
- FileReader API: https://developer.mozilla.org/en-US/docs/Web/API/FileReader
