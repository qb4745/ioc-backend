# Índice de Blueprints/FTVs - Validación de Carga de Archivos ETL

**Technical Design**: `TD-001-validacion-carga-archivos-etl-v2.md`  
**Sprint**: Sprint 2  
**Fecha de Generación**: 2025-10-19  
**Estado**: Draft - Listo para Revisión

---

## 📋 Resumen Ejecutivo

Este conjunto de FTVs (Fichas Técnicas de Vista) documenta la implementación completa de la validación client-side de archivos ETL antes de subirlos al backend. La solución incluye 1 servicio, 1 hook personalizado, 2 componentes UI y 1 guía de integración.

**Objetivo**: Reducir en ~80% las cargas de archivos inválidos al backend, mejorando la experiencia de usuario con feedback inmediato.

---

## 📁 Componentes Generados (5 FTVs)

### Servicios (1)

| ID | Componente | Archivo FTV | Tipo | Descripción |
|---|---|---|---|---|
| FTV-001 | **FileValidator** | [ftv-file-validator.md](./ftv-file-validator.md) | Servicio | Valida archivos TXT con algoritmos de detección de encoding, parsing de headers y sampling de datos |

### Hooks (1)

| ID | Componente | Archivo FTV | Tipo | Descripción |
|---|---|---|---|---|
| FTV-002 | **useFileValidation** | [ftv-use-file-validation.md](./ftv-use-file-validation.md) | Custom Hook | Orquesta el proceso de validación con máquina de estados y telemetría |

### Componentes UI (2)

| ID | Componente | Archivo FTV | Tipo | Descripción |
|---|---|---|---|---|
| FTV-003 | **FilePreview** | [ftv-file-preview.md](./ftv-file-preview.md) | UI Component | Muestra preview de 14 líneas del archivo con highlighting de línea SAP |
| FTV-004 | **FileValidationResult** | [ftv-file-validation-result.md](./ftv-file-validation-result.md) | Feature Component | Presenta resultado de validación con resumen, errores y botones de acción |

### Guías de Integración (1)

| ID | Componente | Archivo FTV | Tipo | Descripción |
|---|---|---|---|---|
| FTV-005 | **DataUploadDropzone** | [ftv-data-upload-dropzone-integration.md](./ftv-data-upload-dropzone-integration.md) | Integration Guide | Guía para integrar validación en componente existente de carga |

---

## 🌳 Árbol de Dependencias

```
DataUploadDropzone (integración)
  ├─ useFileValidation (hook)
  │   └─ FileValidator (servicio)
  │       └─ file-validation.types.ts
  ├─ FilePreview (UI)
  └─ FileValidationResult (UI)
      └─ file-validation.types.ts
```

**Orden de Implementación Sugerido** (bottom-up):
1. `file-validation.types.ts` (tipos compartidos)
2. `FileValidator.ts` (servicio base)
3. `useFileValidation.ts` (hook que usa el servicio)
4. `FilePreview.tsx` (componente standalone)
5. `FileValidationResult.tsx` (componente standalone)
6. Integración en `DataUploadDropzone.tsx`

---

## 📊 Estadísticas del Proyecto

**Código a Implementar**:
- Líneas de código TypeScript: ~1,200 líneas
- Líneas de tests: ~600 líneas
- Interfaces TypeScript: 8
- Componentes React: 2
- Hooks personalizados: 1
- Servicios: 1

**Tests Planificados**:
- Unit tests: 35 casos
- Integration tests: 8 casos
- Accessibility tests: 5 casos
- **Cobertura objetivo**: 80%+

**Endpoints Integrados**:
- `POST /api/v1/etl/start-process` (existente, sin modificar)

**Fixtures de Prueba**:
- 8 archivos de ejemplo en `test/fixtures/etl-upload/`

---

## 🎯 Validaciones Implementadas

| Validación | Tipo | Fase | Bloquea Upload |
|---|---|---|---|
| Extensión (.txt) | Crítica | PRECHECK | ✅ Sí |
| Tamaño (≤50MB) | Crítica | PRECHECK | ✅ Sí |
| Archivo vacío | Crítica | PRECHECK | ✅ Sí |
| Encoding (UTF-8/Windows-1252) | Informativa | READING | ❌ No |
| Headers (9 columnas esperadas) | Crítica | PARSING_HEADERS | ✅ Sí |
| Campos requeridos (NOT NULL) | Crítica | SAMPLING | ✅ Sí |
| Campos numéricos (Cantidad, Peso) | Crítica | SAMPLING | ✅ Sí |
| Fechas (DD/MM/YYYY) | Crítica | SAMPLING | ✅ Sí |
| **Excepción: Línea 4 (SAP)** | N/A | SAMPLING | ⏭️ Ignorada |

---

## 🚀 Próximos Pasos

### 1. Revisar FTVs Generados

Cada FTV contiene:
- ✅ Propósito y caso de uso
- ✅ Props/API completa
- ✅ Código de implementación (skeletons)
- ✅ Algoritmos detallados
- ✅ Plan de testing con casos de prueba
- ✅ Consideraciones de accesibilidad y performance

**Acción**: Revisar cada FTV y validar que cumple con requisitos del negocio.

---

### 2. Crear Estructura de Archivos

```bash
# Crear directorios
mkdir -p src/services
mkdir -p src/hooks
mkdir -p src/components
mkdir -p src/types
mkdir -p test/fixtures/etl-upload

# Crear archivos base
touch src/types/file-validation.types.ts
touch src/services/FileValidator.ts
touch src/hooks/useFileValidation.ts
touch src/components/FilePreview.tsx
touch src/components/FileValidationResult.tsx
```

---

### 3. Implementar en Orden

#### Fase 1: Base (Día 1-2)
```bash
# 1. Tipos
src/types/file-validation.types.ts

# 2. Servicio
src/services/FileValidator.ts
└─ Tests: src/services/FileValidator.test.ts

# 3. Hook
src/hooks/useFileValidation.ts
└─ Tests: src/hooks/useFileValidation.test.ts
```

#### Fase 2: UI (Día 3-4)
```bash
# 4. Componentes UI
src/components/FilePreview.tsx
└─ Tests: src/components/FilePreview.test.tsx

src/components/FileValidationResult.tsx
└─ Tests: src/components/FileValidationResult.test.tsx
```

#### Fase 3: Integración (Día 5)
```bash
# 5. Modificar componente existente
src/components/admin/DataUploadDropzone.tsx
└─ Tests: integration tests

# 6. Fixtures de prueba
test/fixtures/etl-upload/ok_utf8_10lines.txt
test/fixtures/etl-upload/headers_mismatch.txt
# ... (8 archivos total)
```

#### Fase 4: QA y Ajustes (Día 6-7)
- Pruebas manuales con archivos reales
- Ajustes de UX
- Validación de accesibilidad
- Performance profiling

---

### 4. Configurar Feature Flag

```bash
# .env
VITE_VALIDATE_UPLOADS=true
```

```bash
# .env.production (para rollout gradual)
VITE_VALIDATE_UPLOADS=false  # Inicialmente desactivado

# Luego activar en staging
VITE_VALIDATE_UPLOADS=true

# Y finalmente en producción
VITE_VALIDATE_UPLOADS=true
```

---

### 5. Generar Backend Sync Brief (Opcional)

Si el backend necesita ajustar algo:

```bash
# Usar generador de Backend Sync Brief
gemini-cli < .gemini/prompts-v2/04-generate-backend-sync-brief.md
```

Esto consolidará los contratos API mencionados en los FTVs.

---

## ⚠️ Decisiones Pendientes de Validación

Las siguientes inferencias se hicieron del TD y **deben validarse** con el equipo:

### Validaciones
- [ ] Confirmar lista exacta de encabezados esperados (actualmente 9 columnas)
- [ ] Validar que línea 4 es siempre la de totales SAP
- [ ] Confirmar formatos de fecha/hora aceptados
- [ ] Definir campos NOT NULL exactos

### UX/UI
- [ ] Confirmar si preview de 14 líneas es suficiente o necesita ser configurable
- [ ] Validar textos de mensajes de error con equipo de producto
- [ ] Revisar flujo de cancelación con UX

### Performance
- [ ] Validar límite de 100KB para lectura parcial
- [ ] Confirmar si 10 líneas de muestreo son suficientes
- [ ] Definir métricas de performance aceptables en producción

### Seguridad
- [ ] Revisar estrategia de sanitización con equipo de seguridad
- [ ] Confirmar que validación client-side no reemplaza backend

---

## ✅ Respuestas al Checklist de Validación (Backend)

- Encabezados esperados: Confirmado; el parser backend admite ~20 columnas, pero 9 son críticas. La lista puede ajustarse sin romper compatibilidad.
- Línea 4 SAP: Backend no asume número de línea fijo; ignora filas no-datos por contenido. En frontend mantenemos la excepción de línea 4 (a validar con negocio) por performance, con plan de endurecer por contenido si es necesario.
- Formatos de fecha/hora: Confirmado. Fecha dd.MM.yyyy y hora HH:mm:ss en backend. El frontend valida DD/MM/YYYY estrictamente por ahora; coordinar convergencia o normalización.
- Campos NOT NULL: Confirmado. Las 9 columnas críticas son obligatorias; fuente de verdad en backend `isRecordValid`.
- Performance: Límite 100KB es frontend-only; backend procesa completo y reporta métricas Micrometer.
- Seguridad: Confirmado. Frontend es capa UX; backend revalida todo y es la fuente de verdad.

---

## 📚 Referencias Técnicas

### Documentos del Proyecto
- **Technical Design**: `.gemini/sprints/technical-designs/TD-001-validacion-carga-archivos-etl-v2.md`
- **Feature Plan**: `.gemini/sprints/feature-plans/FP-001-validacion-carga-archivos-etl.md`
- **Project Summary**: `.gemini/project_summary.md`

### APIs Web Utilizadas
- [FileReader API](https://developer.mozilla.org/en-US/docs/Web/API/FileReader)
- [TextDecoder API](https://developer.mozilla.org/en-US/docs/Web/API/TextDecoder)
- [File API](https://developer.mozilla.org/en-US/docs/Web/API/File)

### Librerías
- React 19
- TypeScript 5.x
- Tailwind CSS
- Vitest (testing)
- @testing-library/react

---

## 🔍 Validación de FTVs

### Checklist de Calidad

Cada FTV generado cumple con:
- [x] Tiene sección de Propósito clara
- [x] Props/API completamente documentada
- [x] Código de ejemplo ejecutable
- [x] Plan de testing con casos específicos
- [x] Consideraciones de accesibilidad (ARIA, keyboard nav)
- [x] Manejo de errores documentado
- [x] Performance y optimizaciones descritas
- [x] Dependencias listadas
- [x] Checklist de implementación

### Coherencia Entre FTVs

- [x] Tipos TypeScript consistentes entre componentes
- [x] Naming conventions unificados
- [x] Estilos Tailwind consistentes
- [x] Patrones de testing similares
- [x] Manejo de errores coherente

---

## 📞 Contacto y Soporte

**Preguntas sobre implementación**: Revisar el FTV específico  
**Dudas de negocio**: Consultar Feature Plan (FP-001)  
**Decisiones arquitectónicas**: Revisar Technical Design (TD-001)

---

## 📝 Notas de Implementación

### Feature Flags
- `VITE_VALIDATE_UPLOADS`: Activar/desactivar validación client-side
- Default: `true` en desarrollo, configurable en producción

### Telemetría
Eventos instrumentados:
- `val.start`: Inicio de validación
- `val.ready`: Validación exitosa
- `val.blocked`: Validación con errores
- `val.error`: Excepción durante validación

### i18n
Claves de mensajes en `locales/es.json`:
- `val.*`: Mensajes de validación
- `advice.*`: Sugerencias para el usuario

---

## 🎉 Conclusión

Este conjunto de FTVs proporciona una guía completa y detallada para implementar la validación de archivos ETL en el frontend. Cada componente está diseñado para ser:

- ✅ **Testeable**: Cobertura >80%
- ✅ **Accesible**: WCAG 2.1 AA compliant
- ✅ **Performante**: Validación <1s
- ✅ **Seguro**: Sin XSS, sanitización adecuada
- ✅ **Mantenible**: Código modular y bien documentado

**Tiempo estimado de implementación**: 5-7 días  
**Story Points**: 13  
**Riesgo**: Bajo (feature aislada, rollback fácil con feature flag)

---

**Generado por**: Blueprint/FTV Generator v1  
**Basado en**: Technical Design TD-001-v2  
**Fecha**: 2025-10-19  
**Versión**: 1.0
