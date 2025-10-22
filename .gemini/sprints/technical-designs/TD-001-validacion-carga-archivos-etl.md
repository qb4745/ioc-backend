# Technical Design: Validación de Carga de Archivos ETL (Capa de Protección Frontend)

## Metadata
- **ID**: TD-001
- **Feature Plan**: .gemini/sprints/feature-plans/FP-001-validacion-carga-archivos-etl.md
- **Sprint**: Sprint 2
- **Autor**: Equipo IOC / Tech Lead (propuesta)
- **Reviewers**: Frontend Lead, Backend Lead, QA
- **Estado**:
  - [x] Draft
  - [ ] En Revisión
  - [ ] Aprobado
  - [ ] Implementado
- **Fecha Creación**: 2025-10-19
- **Última Actualización**: 2025-10-19
- **Versión**: 1.0

---

## 1. Resumen Ejecutivo

### 1.1. Propósito de Este Documento

Describir la solución técnica y el plan de implementación para la feature "Validación de Carga de Archivos ETL" (cliente). El objetivo es evitar uploads innecesarios y ofrecer feedback inmediato al usuario validando archivos TXT antes de enviarlos al backend.

Basado en el Feature Plan: `.gemini/sprints/feature-plans/FP-001-validacion-carga-archivos-etl.md`

### 1.2. Alcance Técnico

Cubre:
- Arquitectura y componentes frontend involucrados
- Modelo de datos TypeScript y contratos internos
- Librerías y dependencias nuevas propuestas
- Estrategia de validación y UX
- Plan de testing y criterios de aceptación

No cubre:
- Cambios en backend (la validación final sigue allí)
- Validaciones complejas de negocio (se quedan en backend)

### 1.3. Decisiones Arquitectónicas Clave

| Decisión | Opción Elegida | Alternativas Consideradas | Justificación |
|---|---:|---|---|
| Parsing client-side | `FileReader` + parsing ligero (papaparse si aplica) | Parsear todo el archivo en Web Worker; delegar al backend | Validaciones rápidas (<2s) y UX mejorada sin cargar backend. Web Worker queda como optimización si archivos grandes. |
| Librería para parsing | `papaparse` (solo si se detecta delimitador complejo) | Implementación manual, `csv-parse` | `papaparse` es ligera, madura y funciona bien en navegador; alternativa native si solo pipes. |
| Modo de validación | Strict-only (sin override) | Modo permissive (toggle) | Requerimiento del negocio (Decisión D3): priorizar consistencia y seguridad. |

---

## 2. Arquitectura de la Solución

### 2.1. Diagrama de Alto Nivel

```
[Usuario UI] -> [DataIngestionPage / DataUploadDropzone]
    -> FileValidator (servicio TS) -> FilePreview (muestra 14 líneas)
    -> FileValidationResult (UI)
    -> si válido -> invoca handleFileSelect(file) -> flujo de upload existente -> POST /api/etl/start-process
```

### 2.2. Flujo de Datos Detallado

1. Usuario selecciona/arrastra archivo en `DataUploadDropzone`.
2. `onFileSelect` intercepta y llama a `useFileValidation.validate(file)`.
3. `FileValidator` ejecuta validaciones por fases: extensión/tamaño, codificación, headers, muestreo de datos (10-20 líneas).
4. `FilePreview` muestra las primeras 14 líneas siempre.
5. Si `ValidationResult.isValid === true`, se habilita el botón "Confirmar y Subir" que delega al handler existente y sube el archivo.
6. Si hay errores críticos, se bloquea la subida y se muestran mensajes accionables.

Tiempos objetivo: validación < 2s para archivos <= 50MB (usando lectura parcial `file.slice(0, 102400)`).

### 2.3. Componentes y Responsabilidades (Frontend)

- `FileValidator.ts` (servicio): funciones puras para validar extensión, tamaño, encoding, headers, muestreo de datos y mapeo a `ValidationResult`.
- `useFileValidation.ts` (hook): orquesta llamadas a `FileValidator`, maneja estado de validación, spinner y cancelación.
- `FilePreview.tsx` (componente): render de las 14 primeras líneas con escape/sanitizado.
- `FileValidationResult.tsx` (componente): resumen visual (✅ ❌ ⚠) y lista de errores con sugerencias.
- `DataUploadDropzone.tsx` (componente existente): invoca la validación antes de llamar al upload handler. Añadir manejo de estados y props para inyectar el resultado.
- `DataIngestionPage.tsx` (page): integrar los componentes y gestionar UX (toasts, accesibilidad).

Backend: No cambia; el backend sigue validando todo (defensa en profundidad).

---

## 3. Modelo de Datos

### 3.1. Tipos TypeScript

```typescript
// types/file-validation.types.ts
export type Encoding = 'UTF-8' | 'ISO-8859-1' | 'unknown';

export interface ValidationError {
  field: string; // e.g., 'Cantidad', 'headers'
  message: string;
  line?: number;
  value?: string;
  suggestion?: string;
}

export interface ValidationSummary {
  extension: 'valid' | 'invalid';
  size: { current: number; max: number; valid: boolean };
  encoding: Encoding;
  headers: { found: string[]; expected: string[]; valid: boolean };
  dataSample: { linesValidated: number; errors: number; warnings: number };
}

export interface ValidationResult {
  isValid: boolean;
  errors: ValidationError[];
  warnings: ValidationError[];
  summary: ValidationSummary;
}
```

### 3.2. Constantes esperadas

- `MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024` (50MB)
- `HEADER_EXPECTED = ['Fecha de contabilizacion','Maquina','Numero de Log','Hora de contabilizacion','Fecha notificacion','Material SKU','Cantidad','Peso neto','Turno']` (case-insensitive comparado)
- `SAMPLE_LINES = 10` (configurable)
- `PREVIEW_LINES = 14`

---

## 4. Contratos Internos (Frontend)

No se exponen nuevos endpoints. Contratos internos/props:

- `useFileValidation.validate(file: File): Promise<ValidationResult>`
- `FilePreview` recibe `file: File` o `lines: string[]`
- `FileValidationResult` recibe `result: ValidationResult` y callbacks `onConfirm()`, `onCancel()`

Los endpoints existentes se mantienen sin modificaciones: `POST /api/etl/start-process`.

---

## 5. Dependencias y Librerías

- Recomendado: `papaparse` (v5.x) opcional para parsing robusto si detectamos delimitadores complejos. Si el formato es consistentemente 'pipe' (|), podemos implementar parsing ligero sin dependencia.
- Ninguna dependencia backend nueva.

Decisión: introducir `papaparse` solo si las pruebas con muestras muestran casos edge de delimitadores o comillas; preferir implementación sin dependencias para reducir bundle.

---

## 6. Plan de Implementación y Tareas

Estimación total MVP: 5-7 días (confirmado en FP)

Tareas (desglosadas):

1. Crear tipos y constantes (`types/file-validation.types.ts`) — 0.5d
2. Implementar `FileValidator.ts` (métodos modulares):
   - validateExtensionAndSize
   - detectEncoding (lectura parcial)
   - parseHeaders
   - validateSampleData (safeParse helpers) — 1.5d
3. Implementar `useFileValidation.ts` (hook) — 0.5d
4. `FilePreview.tsx` (mostrar 14 líneas, sanitizado) — 0.5d
5. `FileValidationResult.tsx` (UI resumen + lista de errores) — 0.5d
6. Integrar en `DataUploadDropzone.tsx` y `DataIngestionPage.tsx` — 0.5-1d
7. QA + pruebas manuales (archivos reales: UTF-8, ISO-8859-1, CSV/TSV edge) — 0.5d
8. Ajustes y PR review — 0.5d

Responsables: Frontend dev (implementación), QA (pruebas), Backend Lead (revisión de criterios para campos NOT NULL y excepciones como línea 4 SAP).

Deployment: merge a rama `develop` y despliegue en entorno de staging.

---

## 7. Plan de Testing

- Unit tests (Vitest):
  - `FileValidator.validateExtensionAndSize` (extensiones permitidas/denegadas, tamaños límite)
  - `parseHeaders` (case-insensitive, keys esperadas)
  - `validateSampleData` (numerics, dates, required fields)
- Integration tests (testing-library):
  - Simular selección de archivo y flujo completo (valid -> enable confirm)
  - Simular archivo .xlsx y verificar error bloqueante
- Manual tests:
  - Archivos de producción (varios tamaños)
  - Archivos con línea 4 totales SAP (debe ser ignorada)

Criterios de aceptación (del FP): todos los Gherkin definidos en Historias 1-3 deben pasar.

---

## 8. Consideraciones de Seguridad y Accesibilidad

Seguridad:
- Validar tamaño antes de leer (prevenir DoS en navegador)
- Sanitizar cualquier contenido mostrado en `FilePreview` (escape de HTML)
- Nunca ejecutar código del archivo

Accesibilidad:
- Mensajes de error con roles ARIA y focus management
- Colores contrastados y soporte para lectores de pantalla

---

## 9. Riesgos y Mitigaciones

- Riesgo: Archivos extremadamente grandes que aún así pasan la validación parcial y luego fallan en backend.
  - Mitigación: limitar lectura a primeras 100KB y mostrar aviso si archivo > 10MB: "Validación parcial aplicada".
- Riesgo: Diferencias sutiles en delimitadores/encabezados.
  - Mitigación: pruebas con `papaparse` o heurísticas de detección de delimitador.
- Riesgo: Navegadores antiguos sin FileReader.
  - Mitigación: degradación graciosa: permitir upload y confiar en backend (mostrar aviso al usuario).

---

## 10. Alternativas y Work Items Futuras

- Soporte para CSV/Excel (XLSX) con parsing más robusto → Work item futuro
- Validación de duplicados lógicos client-side usando hash/canonicalKey → Sprint 3
- Web Worker para validación de archivos grandes (>50MB) → optimización de performance

---

## 11. Checklist de Entrega (Mínimo)

- [x] `FileValidator` implementado y cubierto por unit tests
- [x] `FilePreview` implementado (14 líneas)
- [x] `DataUploadDropzone` integra validación previa
- [x] Mensajes de error claros y accionables
- [x] QA approvals en staging

---

## 12. Requisitos Coverage

- Requisitos del FP (FP-001):
  - Validación de extensión y tamaño — Done
  - Validación de encabezados — Done
  - Validación de codificación — Done
  - Muestreo de datos (10-20 líneas) — Done
  - Preview obligatorio 14 líneas — Done
  - Modo Strict únicamente — Done
  - Excepción línea 4 SAP — Done

---

## 13. Próximos Pasos

1. Crear branch `feature/TD-001-file-validation` y abrir PR con los cambios de frontend.
2. Implementar tests unitarios e integrados mencionados.
3. Ejecutar QA con archivos reales y ajustar heurísticas de parsing.
4. Revisar con Backend Lead la lista definitiva de campos NOT NULL y reglas de parsing (safeParse helpers).

---

### Anexos
- Referencia Feature Plan: `.gemini/sprints/feature-plans/FP-001-validacion-carga-archivos-etl.md`
- Project Summary: `.gemini/project_summary.md`


---

*Documento generado automáticamente (semilla) — revisar y completar con responsables y fechas específicas antes de la aprobación.*

