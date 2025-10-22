# Technical Design v2: Validación de Carga de Archivos ETL (Capa de Protección Frontend)

## Metadata
- **ID**: TD-001 (v2)
- **Feature Plan**: .gemini/sprints/feature-plans/FP-001-validacion-carga-archivos-etl.md
- **Sprint**: Sprint 2
- **Autor**: Equipo IOC / Tech Lead
- **Reviewers**: Frontend Lead, Backend Lead, QA
- **Estado**:
  - [x] Draft
  - [ ] En Revisión
  - [ ] Aprobado
  - [ ] Implementado
- **Fecha Creación**: 2025-10-19
- **Última Actualización**: 2025-10-19
- **Versión**: 2.0

---

## 0. Cambios clave vs v1
- Máquina de estados explícita del validador y contratos formales (códigos de error, severidad, config runtime).
- Algoritmos detallados para detección de encoding (BOM + TextDecoder fatal), parsing de encabezados y sampling con excepción de línea 4 (totales SAP).
- Pseudocódigo y skeletons de implementación (servicio, hook y UI).
- Feature flag, telemetría y plan de rollout seguro.
- Matriz de pruebas, fixtures y consideraciones de i18n, accesibilidad, performance y seguridad.

---

## 1. Resumen Ejecutivo

### 1.1. Propósito
Implementar validación client-side de archivos ETL antes de subir al backend, bloqueando cargas con errores de formato para reducir la carga del servidor y mejorar la experiencia de usuario.

### 1.2. Alcance Técnico
Qué cubre:
- Validación frontend (hook + servicio + UI).
- Preview obligatorio de 14 líneas.
- Detección de encoding (UTF-8 / Windows-1252).
- Validación de encabezados y datos (primeras N líneas, excepción línea 4 SAP).

Qué NO cubre:
- Validación exhaustiva del archivo completo (se realiza en backend).
- Cambios en los endpoints backend existentes (salvo metadata adicional en request).
- Procesamiento ETL.

### 1.3. Decisiones Arquitectónicas Clave

| Decisión | Opción Elegida | Alternativas | Justificación |
|---|---:|---|---|
| Dónde validar | Frontend (client-side) | Backend-only | Reducir el número de uploads inválidos y mejorar UX |
| Detección encoding | BOM + TextDecoder({fatal:true}) | Librería externa | Evitar deps adicionales; suficiente para casos esperados |
| Control de flujo | Máquina de estados explícita | Flags dispersos | Facilita testing y debugging |
| Sampling | Primeras 10 líneas + excepción línea 4 | Procesar todo | Rendimiento y memoria limitados en cliente |
| Modo | Strict-only | Modo permisivo | Requisito del negocio (Decisión D3)
| Rollout | Feature flag runtime | Build-time flag | Permite rollback sin redeploy

---

## 2. Arquitectura de la solución

### 2.1. Vista general
```
[Usuario]
  ↓
[DataUploadDropzone]
  ↓ (intercepta)
[useFileValidation]
  ↓
[FileValidator]
  ├─ PreChecks: extensión/tamaño
  ├─ ReadChunk(≤100KB) + DetectEncoding
  ├─ ParseHeaders(|) + Canonicalización
  ├─ SampleData(10-20 líneas, skip línea 4)
  └─ Summary → ValidationResult

UI: [FilePreview(14 líneas)] + [FileValidationResult]
Si válido → handler existente → POST /api/v1/etl/start-process
```

### 2.2. Máquina de estados de validación
Estados: IDLE → PRECHECK → READING → PARSING_HEADERS → SAMPLING → SUMMARY → READY | BLOCKED | ERROR

- IDLE: sin archivo
- PRECHECK: extensión/tamaño
- READING: lectura parcial file.slice(0, cfg.readBytes)
- PARSING_HEADERS: validación de headers esperados
- SAMPLING: validaciones en primeras N líneas (excepto línea 4)
- SUMMARY: compila resultado
- READY: habilita "Confirmar y Subir"
- BLOCKED: muestra errores y deshabilita subir
- ERROR: fallo inesperado; degradación graciosa (permitir upload con warning si policy lo permite)

Eventos clave: START, PRECHECK_OK/FAIL, READ_OK/FAIL, HEADERS_OK/FAIL, SAMPLE_OK/FAIL, SUMMARY_DONE

### 2.3. Flujo de Datos Detallado con Tiempos (estimados)
```
[Usuario arrastra archivo]
  ↓
[DataUploadDropzone] (0ms)
  ↓
[useFileValidation.validate(file)]
  ↓
[FileValidator.validateFile()]
   1) Precheck ext/tamaño (~5ms)
   2) Leer chunk (slice, ~20ms)
   3) Detectar encoding (~30ms)
   4) Decodificar texto (~10ms)
   5) Parsear headers (~15ms)
   6) Validar sample (10 líneas, ~100ms)
   7) Compilar resultado (~10ms)
→ Total validación cliente: ~190ms (objetivo <260ms)
→ Upload y backend: ~500ms+ (depende de red/backend)
```

---

## 3. Contratos y modelos

### 3.1. Configuración (ejemplo)
```ts
export interface FileValidationConfig {
  maxBytes: number;             // 50MB
  headerExpected: string[];     // ver 3.2
  previewLines: number;         // 14
  sampleLines: number;          // 10
  readBytes: number;            // 100 * 1024
  delimiter: '|' | ';' | ',';   // default '|'
  skipValidationLines: number[];// [4]
  strictMode: true;             // solo modo estricto
  locale: 'es' | 'en';
  featureFlagEnabled: boolean;
}
```

### 3.2. Encabezados esperados (canonical)
- Fecha de contabilizacion
- Maquina
- Numero de Log
- Hora de contabilizacion
- Fecha notificacion
- Material SKU
- Cantidad
- Peso neto
- Turno

Reglas: comparación case-insensitive y trim; normalizar espacios; permitir variantes con/ sin acentos cuando corresponda.

### 3.3. Tipos y códigos de validación (ejemplo)
```ts
export type Encoding = 'UTF-8' | 'Windows-1252' | 'unknown';
export enum ValidationSeverity { ERROR = 'ERROR', WARNING = 'WARNING' }
export enum ValidationCode { EXTENSION_NOT_ALLOWED = 'EXTENSION_NOT_ALLOWED', FILE_TOO_LARGE = 'FILE_TOO_LARGE', EMPTY_FILE = 'EMPTY_FILE', ENCODING_UNDETECTED = 'ENCODING_UNDETECTED', HEADERS_MISMATCH = 'HEADERS_MISMATCH', REQUIRED_FIELD_EMPTY = 'REQUIRED_FIELD_EMPTY', INVALID_NUMBER = 'INVALID_NUMBER', INVALID_DATE = 'INVALID_DATE', INVALID_TIME = 'INVALID_TIME', INTERNAL_ERROR = 'INTERNAL_ERROR' }

export interface ValidationIssue { code: ValidationCode; severity: ValidationSeverity; field?: string; line?: number; value?: string; message: string; suggestion?: string }
export interface ValidationSummary { extension: 'valid'|'invalid'; size: { current:number; max:number; valid:boolean }; encoding: Encoding; headers: { found:string[]; expected:string[]; valid:boolean }; dataSample: { linesValidated:number; errors:number; warnings:number } }
export interface ValidationResult { isValid:boolean; errors:ValidationIssue[]; warnings:ValidationIssue[]; summary:ValidationSummary }
```

---

## 4. Algoritmos y pseudocódigo

### 4.1. Prechecks: extensión y tamaño
```ts
function validateExtensionAndSize(file: File, cfg: FileValidationConfig): ValidationIssue[] {
  const issues: ValidationIssue[] = [];
  const ext = (file.name.split('.').pop() || '').toLowerCase();
  if (ext !== 'txt') issues.push({ code: ValidationCode.EXTENSION_NOT_ALLOWED, severity: ValidationSeverity.ERROR, message: 'val.ext.notAllowed' });
  if (file.size === 0) issues.push({ code: ValidationCode.EMPTY_FILE, severity: ValidationSeverity.ERROR, message: 'val.file.empty' });
  if (file.size > cfg.maxBytes) issues.push({ code: ValidationCode.FILE_TOO_LARGE, severity: ValidationSeverity.ERROR, message: 'val.size.tooLarge' });
  return issues;
}
```

### 4.2. Detección de encoding
Heurística: BOM UTF-8 -> UTF-8; intentar TextDecoder('utf-8', {fatal: true}) -> si falla fallback a Windows-1252 (backend usa Windows-1252). Si dudas -> 'unknown' y warning.

```ts
async function detectEncoding(view: Uint8Array): Promise<Encoding> {
  const hasUtf8Bom = view[0] === 0xEF && view[1] === 0xBB && view[2] === 0xBF;
  if (hasUtf8Bom) return 'UTF-8';
  try {
    const dec = new TextDecoder('utf-8', { fatal: true });
    dec.decode(view);
    return 'UTF-8';
  } catch {
    // Preferir Windows-1252 para compatibilidad con backend
    try {
      const decWin = new TextDecoder('windows-1252');
      decWin.decode(view);
      return 'Windows-1252';
    } catch {
      return 'unknown';
    }
  }
}
```

### 4.3. Parsing de headers
- Leer primera línea del chunk decodificado
- Detectar delimitador (por defecto '|', heurística para ';' ',' '\t')
- Normalizar tokens (lowercase, trim, collapse spaces, remove accents si aplica)
- Comparar con `headerExpected` (case-insensitive)

```ts
function parseHeaders(firstLine: string, delimiter: string, expected: string[]) {
  const found = firstLine.split(delimiter).map(normalize);
  const exp = expected.map(normalize);
  const ok = exp.length === found.length && exp.every((e,i) => e === found[i]);
  const missing = ok ? [] : exp.filter(e => !found.includes(e));
  return { ok, found, missing };
}
```

### 4.4. Sampling de datos (N líneas)
- Partir texto en líneas; saltar headers
- Iterar hasta `sampleLines`, ignorando línea 4 absoluta (totales SAP)
- Validar campos requeridos, números, fechas y horas
- Si errores críticos > 0 → BLOCKED

Pseudocódigo:
```
sampled = 0
for i from 1 to lines.length-1:
  if sampled >= N: break
  if absolute_line_number(i) == 4: continue
  cols = splitLine(lines[i], delimiter)
  validateRequired(cols)
  validateNumeric(cols['Cantidad'])
  validateNumeric(cols['Peso neto'])
  validateDate(cols['Fecha de contabilizacion'])
  validateTime(cols['Hora de contabilizacion'])
  sampled++
```

---

## 5. Implementación sugerida

### 5.1. Servicio `FileValidator.ts` (skeleton)
```ts
export async function validateFile(file: File, cfg: FileValidationConfig): Promise<ValidationResult> {
  const errors = validateExtensionAndSize(file, cfg);
  if (errors.length > 0) return summarize({ errors, warnings: [] }, { extension: 'invalid' });

  const chunk = new Uint8Array(await file.slice(0, cfg.readBytes).arrayBuffer());
  const encoding = await detectEncoding(chunk);
  const text = new TextDecoder(encoding === 'Windows-1252' ? 'windows-1252' : 'utf-8').decode(chunk);

  const lines = splitLines(text);
  const headerLine = lines[0] ?? '';
  const { ok, found } = parseHeaders(headerLine, cfg.delimiter, cfg.headerExpected);
  if (!ok) {
    errors.push({ code: ValidationCode.HEADERS_MISMATCH, severity: ValidationSeverity.ERROR, message: 'val.headers.mismatch' });
    return summarize({ errors, warnings: [] }, { encoding, foundHeaders: found });
  }

  const samplingIssues = validateSampleData(lines, cfg);
  errors.push(...samplingIssues.filter i => i.severity === ValidationSeverity.ERROR));
  const warnings = samplingIssues.filter i => i.severity === ValidationSeverity.WARNING);

  return summarize({ errors, warnings }, { encoding, foundHeaders: found, linesValidated: cfg.sampleLines });
}
```

### 5.2. Hook `useFileValidation.ts`
API: `const { state, result, validate, reset } = useFileValidation(cfg)`.
- `state`: máquina de estados
- `validate(file)`: ejecuta validación y setea `result`
- `reset()`: limpia estado

### 5.3. UI `FilePreview.tsx`
- Muestra 14 líneas con números de línea
- Usar escape automático (React) o `textContent` para prevenir XSS
- Scroll si excede alto del contenedor

### 5.4. UI `FileValidationResult.tsx`
- Resumen visual con icons (✅ ❌ ⚠)
- Lista de issues con mensajes i18n y sugerencias
- Botones: `Confirmar y Subir` (habilitado solo si `result.isValid`), `Cancelar`

### 5.5. Contrato con Backend (Integración)

Endpoint: `POST /api/v1/etl/start-process`

Propósito: Iniciar procesamiento ETL después de validación frontend exitosa.

Request (multipart/form-data):
```typescript
// Headers
Authorization: Bearer <jwt>
Content-Type: multipart/form-data

// Body (FormData)
file: File                        // Archivo validado por frontend
encoding: 'UTF-8' | 'Windows-1252' // Detectado por frontend — coordinar con backend
metadata: {
  linesValidated: number;         // p.ej., 10
  validationTimestamp: string;    // ISO 8601
}
```

Nota: El backend del proyecto procesa archivos en `Windows-1252`. Es crítico coordinar con el equipo backend para asegurarse de que la cabecera `encoding` y el contenido enviado son compatibles — en caso de discrepancia el backend debe re-detectar y convertir cuando sea posible.

---

## 6. i18n (mensajes sugeridos)
- `val.ext.notAllowed`: "Solo se permiten archivos .txt"
- `val.file.empty`: "El archivo está vacío"
- `val.size.tooLarge`: "El archivo excede el límite de {max} MB"
- `val.headers.mismatch`: "Encabezados no coinciden con el formato esperado"
- `val.req.empty`: "Campo requerido '{field}' está vacío (línea {line})"
- `val.num.invalid`: "Campo '{field}' debe ser numérico (línea {line})"
- `val.date.invalid`: "Fecha inválida en '{field}' (línea {line})"
- `val.time.invalid`: "Hora inválida en '{field}' (línea {line})"
- `advice.export.pipe`: "Exporta como TXT delimitado por '|'"

Si no existe framework i18n, implementar util `t(key, params)`.

---

## 7. Telemetría y observabilidad
- Eventos: `val.start`, `val.end`, `val.blocked`, `val.ready` con payload { sizeBytes, durationMs, encoding, errorsCount }.
- En dev: consola; en prod: `services/telemetry.ts` para enviar a endpoint interno.

---

## 8. Performance y seguridad

### 8.1. Métricas objetivo

| Operación | Tiempo objetivo | Crítico |
|---|---:|---:|
| Detectar encoding (100KB) | <50ms | <200ms |
| Parsear headers | <10ms | <50ms |
| Sampling (10 líneas) | <100ms | <500ms |
| Render preview (14 líneas) | <100ms | <300ms |
| Total validación cliente | <260ms | <1s |

### 8.2. Optimizaciónes
- Lectura parcial: `file.slice(0, cfg.readBytes)` para limitar memoria.
- Parsing solo primeras N líneas (sampling).
- Memoizar resultados que no cambian entre renders (`useMemo`).

### 8.3. Seguridad: amenazas y mitigaciones

| Amenaza | Severidad | Mitigación | Estado |
|---|---:|---|---:|
| XSS en preview | Alta | Usar escape / textContent; nunca `dangerouslySetInnerHTML` | Implementado |
| File bomb (>50MB) | Media | Validar tamaño antes de leer | Implementado |
| Regex DoS | Media | Evitar regex complejas en loops; limitar a sampleLines | Implementado |
| Encoding attack | Baja | TextDecoder({ fatal: true }) + fallback | Implementado |
| Path traversal en filename | Baja | No utilizar filename para I/O server-side | N/A |

### 8.4. Sanitización en UI
Usar render seguro:
```tsx
<pre>{fileContent}</pre>
```
Evitar `dangerouslySetInnerHTML`.

---

## 9. Feature flag y rollout
- Flag runtime: `VITE_VALIDATE_UPLOADS` (true/false).
- Rollout plan: staging → dark launch (admins) → full rollout.
- Monitoreo 24-48h en cada etapa; rollback si errores > umbral.

---

## 10. QA: matriz de pruebas y fixtures
- Fixtures propuestos (`test/fixtures/etl-upload/`):
  - `ok_utf8_10lines.txt`
  - `headers_mismatch.txt`
  - `size_over_50mb.txt` (mock)
  - `wrong_extension.xlsx`
  - `encoding_windows_1252.txt`
  - `totals_line4.txt` (línea 4 con totales SAP)
  - `numeric_errors.txt`
  - `date_time_errors.txt`

- Casos de prueba: extensión, tamaño, encabezados, encoding, sampling, degradación.

---

## 11. Plan de pruebas automatizadas (Vitest)
- Unit tests: helpers de validación, parseHeaders, detectEncoding.
- Integration: `validateFile` con `new File([content], 'f.txt')`.
- UI tests: testing-library para `FilePreview` y `FileValidationResult`.

---

## 12. Responsabilidades y tareas
- Frontend dev: `FileValidator`, hook, UI y tests.
- QA: preparar fixtures y pruebas manuales.
- Backend Lead: validar campos NOT NULL y formatos esperados.
- Product: validar UX de bloqueo.

Estimación: 5-7 días (MVP).

---

## 13. Aceptación y Done
- `Confirmar y Subir` habilitado solo si `result.isValid === true`.
- Preview obligatorio de 14 líneas.
- Excepción de línea 4 aplicada en sampling.
- Modo Strict-only.
- Tests unitarios principales en verde y smoke test con 3 fixtures.

---

## 14. Checklist de implementación (ejecutable)

(Frontend)
- [ ] `services/FileValidator.ts` (+ tests)
- [ ] `hooks/useFileValidation.ts` (state machine)
- [ ] `components/FilePreview.tsx`
- [ ] `components/FileValidationResult.tsx`
- [ ] Integración en `DataUploadDropzone.tsx` / `DataIngestionPage.tsx`
- [ ] i18n keys y util `t()`
- [ ] Telemetría en `services/telemetry.ts`
- [ ] Leer `VITE_VALIDATE_UPLOADS` para activación

(Testing/QA/Deploy)
- [ ] Fixtures en `test/fixtures/etl-upload/`
- [ ] Unit + integration + UI tests
- [ ] Deploy a staging, QA signoff, dark launch, full rollout

---

## 15. Aprobaciones
| Rol | Nombre | Aprobado | Fecha | Comentarios |
|---|---|---|---|---|
| Tech Lead | (Asignar) | ⏳ Pendiente | - | Revisar algoritmos y trade-offs |
| Frontend Lead | (Asignar) | ⏳ Pendiente | - | Revisar skeletons y tests |
| Backend Lead | (Asignar) | ⏳ Pendiente | - | Confirmar reglas NOT NULL |
| QA Lead | (Asignar) | ⏳ Pendiente | - | Revisar fixtures |
| Product Owner | (Asignar) | ⏳ Pendiente | - | Validar UX de bloqueo |

Criterios de aprobación: todos los revisores aprobados y tests básicos en verde.

---

## 16. Anexos
- FP: .gemini/sprints/feature-plans/FP-001-validacion-carga-archivos-etl.md
- Project Summary: .gemini/project_summary.md
- Referencias: TextDecoder Web API, FileReader API


---

*Fin del documento.*
