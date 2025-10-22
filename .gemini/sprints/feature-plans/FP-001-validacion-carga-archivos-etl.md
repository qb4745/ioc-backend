- Scroll si excede espacio disponible

**Mockup actualizado**:
```
┌─ Preview del Archivo ──────────────────────────────────────┐
│ Mostrando primeras 14 líneas de produccion_2024-01-15.txt │
│                                                            │
│  1 | Fecha de contabilización|Máquina|Número de Log|...   │
│  2 | 15/01/2024|MAQ001|1234567|...                         │
│  3 | 15/01/2024|MAQ002|1234568|...                         │
│  4 | [TOTALES SAP - Línea ignorada en validación] 🔵      │
│  5 | 15/01/2024|MAQ003|1234569|...                         │
│  ... (líneas 6-14)                                         │
│ 14 | 15/01/2024|MAQ012|1234578|...                         │
│                                                            │
│ ℹ️ La línea 4 contiene totales de SAP y no se valida     │
└────────────────────────────────────────────────────────────┘
```

**Impacto en Estimación**: 
- Fase 2 aumenta +2 horas (componente FilePreview ahora es requerido, no opcional)
- Estimación total se mantiene en 5-7 días (dentro del buffer)

---

#### ✅ D3: Override de Validación - RESUELTO (2025-10-19)

**Decisión Tomada**: 
- **NO permitir override de validación**
- **Justificación**: Priorizar seguridad y consistencia de datos sobre flexibilidad
- **Excepción**: La línea 4 (totales SAP) se ignora automáticamente, no requiere override manual

**Implementación**:
- Eliminar el toggle "Modo Permisivo" del diseño inicial
- Validación es siempre estricta (con excepción de línea 4)
- Si hay errores críticos, el botón "Confirmar y Subir" permanece deshabilitado
- Mensajes de error deben ser muy claros y accionables para que el usuario corrija el archivo

**Cambios en Historias de Usuario**:
- ~~Historia 4: Modo de Validación (Strict vs Permissive)~~ → **ELIMINADA**
- Solo quedan 3 historias de usuario principales
- Estimación se reduce ligeramente (-1 hora en Fase 2 por no implementar toggle)

**Impacto en UX**:
- Más restrictivo, pero más predecible
- Usuarios deben corregir errores antes de subir (mejor para calidad de datos)
- Reduce riesgo de archivos "casi válidos" que luego fallan en backend
# Feature Plan: Validación de Carga de Archivos ETL (Capa de Protección Frontend)

## Metadata
- **ID**: FP-001
- **Sprint**: Sprint 2
- **Prioridad**: Alta
- **Tipo**: Mejora / Nueva Feature
- **Estimación**: 5-7 días (13 Story Points)
- **Asignado a**: Pendiente
- **Estado**: 
  - [x] Planificación
  - [ ] En Diseño
  - [ ] Listo para Desarrollo
  - [ ] En Desarrollo
  - [ ] En Testing
  - [ ] Completado
- **Fecha Creación**: 2025-01-18
- **Última Actualización**: 2025-01-18

---

## 1. Contexto de Negocio

### 1.1. Problema a Resolver

**Contexto Actual**:
El sistema actual permite a los administradores cargar archivos TXT para el proceso ETL sin ninguna validación en el frontend. La única validación que existe es en el backend (ParserService.java), que valida:
1. Campos requeridos (NOT NULL)
2. Formato de datos (parseo defensivo con safeParseLong, safeParseInt, safeParseBigDecimal)
3. Duplicados lógicos (mediante canonicalKey)

Sin embargo, el archivo se sube completamente al backend antes de detectar errores estructurales básicos, lo que genera:
- Consumo innecesario de ancho de banda
- Carga innecesaria en el servidor
- Mala experiencia de usuario (espera larga solo para recibir un error)
- Logs de errores que podrían prevenirse

**Problema Específico**:
Los usuarios pueden cargar archivos que claramente no son válidos (extensión incorrecta, vacíos, sin estructura de columnas esperadas, codificación incorrecta) y solo descubren el error después de que el archivo fue subido y procesado parcialmente por el backend, desperdiciando tiempo y recursos.

**Impacto del Problema**:
- **Usuarios afectados**: Administradores que cargan archivos ETL (estimado 5-10 usuarios activos)
- **Frecuencia**: Diaria - cada vez que se carga un archivo
- **Severidad**: Media-Alta (no es crítico pero afecta la productividad)
- **Coste de NO resolverlo**: 
  - Desperdicio de ~30% de uploads (archivos rechazados por errores básicos)
  - Frustración del usuario por retroalimentación tardía
  - Costos de procesamiento en backend para archivos inválidos
  - Logs innecesarios que dificultan debugging

**Ejemplo de Caso de Uso**:
```
Usuario: Administrador de Producción
Situación: Intenta cargar un archivo exportado desde Excel con extensión .xlsx
Problema: El sistema acepta el archivo, lo sube (50MB), espera 2 minutos, y luego falla
Impacto: Pérdida de tiempo, frustración, y tiene que convertir manualmente a TXT
```

---

### 1.2. Solución Propuesta

**Qué vamos a construir** (descripción de alto nivel):

Implementaremos una **capa de validación en el frontend** que valide archivos TXT antes de enviarlos al backend. Esta validación será progresiva y educativa:

1. **Validación Básica (Pre-upload)**: Extensión del archivo (.txt), tamaño, y que no esté vacío
2. **Validación de Estructura (Client-side parsing)**: Leer las primeras líneas del archivo en el navegador para:
   - Verificar que tiene una línea de encabezados
   - Verificar que los encabezados coinciden con las columnas esperadas del modelo FactProduction
   - Verificar que hay al menos 1 línea de datos
   - Detectar problemas de codificación (caracteres extraños)
3. **Validación de Formato de Datos (Sampling)**: Leer una muestra de las primeras 10-20 líneas y validar:
   - Que campos numéricos contienen números (similar a safeParseLong del backend)
   - Que campos de fecha tienen formato válido
   - Que no hay campos requeridos vacíos
4. **Feedback Visual en Tiempo Real**: Mostrar mensajes claros y accionables si se detecta algún error

**Valor para el Usuario**:
- **Retroalimentación inmediata**: Sabe en 1-2 segundos si el archivo es válido, sin esperar el upload
- **Guía educativa**: Mensajes claros indican exactamente qué está mal y cómo arreglarlo
- **Prevención de errores**: No puede accidentalmente subir un archivo incorrecto
- **Ahorro de tiempo**: No espera 2-3 minutos solo para descubrir un error obvio

**Valor para el Negocio**:
- **Reducción de carga en backend**: ~30% menos de archivos inválidos procesados
- **Reducción de costos de infraestructura**: Menos ancho de banda y procesamiento desperdiciado
- **Mejora de métricas de éxito**: Más archivos procesados exitosamente en el primer intento
- **Mejor experiencia de usuario**: Mayor satisfacción y productividad

---

### 1.3. Alcance del MVP

**✅ Dentro del Alcance** (Sprint 2):
- [x] **Validación de extensión y tamaño**: Solo archivos .txt, máximo 50MB
- [x] **Validación de encabezados**: Verificar que el archivo tiene los encabezados esperados (comparación con lista de columnas requeridas)
- [x] **Validación de estructura básica**: Al menos 1 línea de datos después del header
- [x] **Validación de codificación**: Detectar archivos que no son UTF-8 o ISO-8859-1
- [x] **Validación de campos requeridos (sampling)**: Verificar en las primeras 10 líneas que los campos NOT NULL no estén vacíos (EXCEPTO línea 4 - totales SAP)
- [x] **Validación de tipos de datos (sampling)**: Verificar que campos numéricos son parseables (EXCEPTO línea 4)
- [x] **UI de feedback**: Mensajes de error claros con iconos y sugerencias de solución
- [x] **Preview de archivo obligatorio**: Mostrar las primeras **14 líneas** del archivo siempre (DECISIÓN D2 ✅)
- [x] **Modo Strict únicamente**: Validación estricta sin opción de override (DECISIÓN D1 ✅ y D3 ✅)
- [x] **Excepción línea 4 SAP**: Ignorar validación en línea 4 que contiene totales de SAP (DECISIÓN D1 ✅)

**❌ Fuera del Alcance** (diferir para futuro):
- [ ] ~~**Modo permissive/override de validación**~~ → RECHAZADO por decisión D3 (prioridad en seguridad)
- [ ] **Validación de duplicados lógicos en frontend** → Sprint 3 (requiere cargar todo el archivo en memoria, puede ser costoso)
- [ ] **Validación de reglas de negocio complejas** → Backend es mejor lugar (ej: validar que SKU existe en catálogo)
- [ ] **Corrección automática de errores** → Backlog (feature avanzada, ej: auto-convertir formato de fecha)
- [ ] **Validación de archivos CSV** → Backlog (requiere parser diferente)
- [ ] **Validación asincrónica completa** → Requiere investigación (usar Web Workers para archivos grandes)

**Justificación del Alcance**:
El MVP se enfoca en validaciones que:
1. Son rápidas de ejecutar en el cliente (< 1 segundo)
2. Previenen los errores más comunes (80/20 rule)
3. No duplican lógica compleja del backend
4. Mejoran UX sin añadir complejidad excesiva

---

## 2. Análisis Técnico (Alto Nivel)

### 2.1. Componentes Afectados

**Frontend**:
- [x] **Componentes Nuevos**:
  - `FileValidator.ts` - Servicio de validación con métodos estáticos
  - `FileValidationResult.tsx` - Componente para mostrar resultado de validación (errores/warnings)
  - `FilePreview.tsx` - Componente para mostrar preview del archivo (opcional, nice-to-have)
  
- [x] **Componentes a Modificar**:
  - `DataUploadDropzone.tsx` - Añadir llamada a validación antes de emitir `onFileSelect`
  - `DataIngestionPage.tsx` - Manejar resultado de validación, mostrar modal si hay errores

  - Ninguna (modificación en página existente)

- [x] **Nuevos Hooks** (opcional):
  - `useFileValidation.ts` - Hook personalizado para encapsular lógica de validación

**Backend**:
- [ ] **No requiere cambios** - La validación del backend (ParserService) se mantiene como segunda capa de defensa

**Base de Datos**:
- [ ] **Sin cambios** - No se requieren nuevas tablas ni migraciones

**Integraciones Externas**:
- [ ] **Ninguna** - Validación completamente client-side

---

### 2.2. Dependencias Técnicas

**Nuevas Librerías/Dependencias**:
- Frontend:
  - `papaparse` (v5.4.1) - Para parsing de archivos delimitados (TSV/CSV-like) de forma robusta
  - Alternativa: `csv-parser` o implementación manual (evaluar en diseño técnico)
  - **Decisión Pendiente**: Ver sección 10.1

**Features/Sistemas Existentes Requeridos**:
- File API del navegador (ya disponible en navegadores modernos)
- React hooks (`useState`, `useCallback`) - ya en uso
- Toast notifications (`react-hot-toast`) - ya integrado

**Bloqueos Técnicos**:
- Ninguno identificado

---

### 2.3. Impacto en la Arquitectura

**Cambios Arquitectónicos**:
- [x] No hay cambios (feature aislada)
- [ ] Cambios menores (agregar endpoint/componente)
- [ ] Cambios mayores (nueva integración/servicio)
- [ ] Cambios críticos (refactor de módulo existente)

**Descripción**:
Esta es una feature completamente aislada que añade una capa de validación client-side sin modificar contratos de API ni lógica de backend. Es un patrón de **defensa en profundidad** (defense in depth) donde:
- Frontend valida para UX y eficiencia
- Backend valida para seguridad y consistencia de datos

No hay riesgo de conflicto con otras features.

---

## 3. Requisitos Funcionales

### 3.1. Historias de Usuario

#### Historia 1: Validación de Extensión y Tamaño

**Como** administrador que carga archivos ETL,  
**Quiero** que el sistema valide la extensión y tamaño del archivo antes de subirlo,  
**Para** evitar errores obvios y no desperdiciar tiempo esperando un upload que fallará.

**Criterios de Aceptación**:
```gherkin
Escenario: Usuario intenta cargar archivo con extensión incorrecta
  Dado que estoy en la página de Ingesta de Datos
  Cuando arrastro un archivo "datos.xlsx" al dropzone
  Entonces veo un mensaje de error "Solo se permiten archivos .txt"
  Y el archivo NO se sube al servidor
  Y veo un ícono de error en el dropzone
  Y el botón de "Cancelar" no aparece (porque no hubo upload)

Escenario: Usuario intenta cargar archivo mayor a 50MB
  Dado que estoy en la página de Ingesta de Datos
  Cuando selecciono un archivo "datos.txt" de 75MB
  Entonces veo un mensaje de error "El archivo excede el límite de 50MB"
  Y veo el tamaño del archivo: "Tamaño: 75MB (límite: 50MB)"
  Y el archivo NO se sube

Escenario: Usuario carga archivo válido (extensión y tamaño)
  Dado que estoy en la página de Ingesta de Datos
  Cuando selecciono un archivo "datos.txt" de 10MB
  Entonces la validación pasa la primera fase
  Y continúa con la validación de estructura
```

**Notas Técnicas**:
- Usar `File.name` para extensión y `File.size` para tamaño
- Validación debe ejecutarse antes de leer el contenido del archivo

---

#### Historia 2: Validación de Estructura y Encabezados

**Como** administrador que carga archivos ETL,  
**Quiero** que el sistema valide que el archivo tiene la estructura esperada (encabezados correctos),  
**Para** saber inmediatamente si el formato del archivo es compatible con el sistema.

**Criterios de Aceptación**:
```gherkin
Escenario: Archivo sin línea de encabezados
  Dado que he seleccionado un archivo "datos.txt" válido en tamaño
  Cuando el sistema lee el archivo y encuentra solo datos sin header
  Entonces veo un mensaje de error "Archivo sin encabezados detectado"
  Y veo una sugerencia: "La primera línea debe contener: Fecha de contabilización, Máquina, Número de Log, ..."
  Y el archivo NO se sube

Escenario: Archivo con encabezados incorrectos
  Dado que he seleccionado un archivo "datos.txt"
  Y la primera línea es "Columna1|Columna2|Columna3"
  Cuando el sistema valida los encabezados
  Entonces veo un mensaje de error "Encabezados no coinciden con el formato esperado"
  Y veo una comparación:
    """
    ❌ Encontrado: Columna1, Columna2, Columna3
    ✅ Esperado: Fecha de contabilización, Máquina, Número de Log, ...
    """
  Y el archivo NO se sube

Escenario: Archivo con encabezados correctos
  Dado que he seleccionado un archivo con la primera línea:
    """
    Fecha de contabilización|Máquina|Número de Log|Hora de contabilización|Fecha notificación|Material SKU|Cantidad|Peso neto|Turno
    """
  Cuando el sistema valida los encabezados
  Entonces veo un ícono de éxito ✅ junto a "Encabezados válidos"
  Y continúa con la validación de datos

Escenario: Archivo con encabezados con variaciones menores (case-insensitive)
  Dado que he seleccionado un archivo con "FECHA DE CONTABILIZACIÓN" (mayúsculas)
  Cuando el sistema valida los encabezados
  Entonces acepta el archivo (validación case-insensitive)
  Y muestra un warning: "Encabezados válidos pero con diferencias en mayúsculas/minúsculas"
```

**Notas Técnicas**:
- Leer solo las primeras 100 KB del archivo (suficiente para header + algunas líneas)
- Usar FileReader API con `readAsText(file.slice(0, 102400))`
- Comparar headers contra lista esperada (definida como constante)
- Delimitador esperado: `|` (pipe)

---

#### Historia 3: Validación de Datos (Sampling)

**Como** administrador que carga archivos ETL,  
**Quiero** que el sistema valide una muestra de los datos antes de subir,  
**Para** detectar problemas de formato en campos numéricos o fechas sin procesar todo el archivo.

**Criterios de Aceptación**:
```gherkin
Escenario: Campos numéricos con valores no numéricos
  Dado que he seleccionado un archivo válido en estructura
  Y en la línea 3 el campo "Cantidad" contiene "abc"
  Cuando el sistema valida la muestra de datos (primeras 10 líneas)
  Entonces veo un error "Campo 'Cantidad' debe ser numérico (línea 3)"
  Y veo el valor problemático: "Valor encontrado: 'abc'"
  Y el archivo NO se sube

Escenario: Campos requeridos vacíos
  Dado que he seleccionado un archivo válido en estructura
  Y en la línea 5 el campo "Máquina" está vacío
  Cuando el sistema valida la muestra
  Entonces veo un error "Campo requerido 'Máquina' está vacío (línea 5)"
  Y veo una lista de campos requeridos que no pueden estar vacíos
  Y el archivo NO se sube

Escenario: Fechas con formato incorrecto
  Dado que he seleccionado un archivo válido
  Y en la línea 2 el campo "Fecha de contabilización" contiene "32/13/2024"
  Cuando el sistema valida la muestra
  Entonces veo un error "Formato de fecha inválido en 'Fecha de contabilización' (línea 2)"
  Y veo el formato esperado: "DD/MM/YYYY o DD-MM-YYYY"
  Y el archivo NO se sube

Escenario: Todas las validaciones pasan
  Dado que he seleccionado un archivo válido en todos los aspectos
  Cuando el sistema completa todas las validaciones
  Entonces veo un resumen de validación:
    """
    ✅ Extensión: .txt
    ✅ Tamaño: 10.5MB / 50MB
    ✅ Encabezados: 9/9 correctos
    ✅ Datos (10 líneas muestreadas): Sin errores
    ✅ Codificación: UTF-8
    """
  Y veo un botón "Confirmar y Subir"
  Y el archivo se sube al hacer clic
```

**Notas Técnicas**:
- Validar solo las primeras 10-20 líneas (configurable)
- Reutilizar lógica similar a `safeParseLong`, `safeParseInt`, `safeParseBigDecimal` del backend
- Campos requeridos según backend: fechaContabilizacion, maquina, numeroLog, horaContabilizacion, fechaNotificacion, materialSku, cantidad, pesoNeto, turno

---

#### Historia 4: Modo de Validación (Strict vs Permissive)

**Como** administrador experimentado,  
**Quiero** poder elegir si los warnings bloquean la subida o solo me advierten,  
**Para** tener flexibilidad en casos excepcionales donde sé que el archivo es válido aunque tenga warnings.

**Criterios de Aceptación**:
```gherkin
Escenario: Modo Strict (por defecto)
  Dado que estoy en modo de validación "Strict"
  Cuando el sistema detecta un warning (ej: mayúsculas en headers)
  Entonces el warning se convierte en error bloqueante
  Y el botón "Confirmar y Subir" está deshabilitado

Escenario: Modo Permissive
  Dado que he activado el toggle "Modo Permisivo"
  Cuando el sistema detecta un warning
  Entonces veo el warning con ícono ⚠️
  Y el botón "Confirmar y Subir" está habilitado
  Y veo un mensaje: "Hay 2 advertencias. ¿Deseas continuar?"
  Y puedo subir el archivo bajo mi responsabilidad

Escenario: Errores críticos siempre bloquean
  Dado que estoy en modo "Permissivo"
  Cuando el sistema detecta un error crítico (ej: campo numérico con texto)
  Entonces el error BLOQUEA la subida independientemente del modo
  Y veo un mensaje: "Este error es crítico y no puede ignorarse"
```

**Notas Técnicas**:
- Clasificar validaciones en: `error` (crítico) vs `warning` (no crítico)
- Toggle de modo en la UI (checkbox o switch)
- Por defecto: modo Strict

---

### 3.2. Casos de Uso Detallados

#### Caso de Uso 1: Flujo Completo de Validación Exitosa

**Actor Principal**: Administrador de Producción  
**Precondiciones**: 
- Usuario autenticado
- Tiene acceso a página de Ingesta de Datos
- Tiene un archivo .txt válido preparado

**Trigger**: Usuario arrastra archivo al dropzone o hace clic en "Seleccionar archivo"

**Flujo Normal**:
1. Usuario selecciona archivo "produccion_2024-01-15.txt" (15MB)
2. Sistema captura el evento `onFileSelect`
3. Sistema ejecuta `FileValidator.validate(file)`
4. Sistema valida extensión: ✅ `.txt`
5. Sistema valida tamaño: ✅ `15MB < 50MB`
6. Sistema lee las primeras 100KB del archivo
7. Sistema valida codificación: ✅ `UTF-8 detectado`
8. Sistema parsea la primera línea y valida encabezados: ✅ `9/9 columnas coinciden`
9. Sistema parsea las siguientes 10 líneas y valida:
   - Campos requeridos no vacíos: ✅
   - Campos numéricos son números: ✅
   - Campos de fecha tienen formato válido: ✅
10. Sistema muestra componente `FileValidationResult` con resumen de validación
11. Sistema habilita botón "Confirmar y Subir"
12. Usuario hace clic en "Confirmar y Subir"
13. Sistema invoca `handleFileSelect(file)` original
14. Archivo se sube al backend (flujo existente continúa)

**Postcondiciones**: 
- Archivo validado y subido
- Proceso ETL iniciado en backend
- Historial actualizado con nuevo registro

---

#### Caso de Uso 2: Flujo con Errores de Validación

**Actor Principal**: Administrador de Producción  
**Precondiciones**: Usuario autenticado, tiene un archivo .xlsx (incorrecto)  
**Trigger**: Usuario arrastra archivo "datos.xlsx"

**Flujo Normal**:
1. Usuario selecciona archivo "datos.xlsx"
2. Sistema ejecuta `FileValidator.validate(file)`
3. Sistema valida extensión: ❌ `.xlsx` no permitido
4. Sistema detiene validación inmediatamente
5. Sistema muestra toast de error: "Solo se permiten archivos .txt"
6. Sistema muestra en `FileValidationResult`:
   ```
   ❌ Extensión: .xlsx (esperado: .txt)
   
   💡 Sugerencia: Exporta el archivo como TXT delimitado por pipes (|)
   ```
7. Sistema NO invoca `handleFileSelect(file)`
8. Botón "Cancelar" NO aparece (no hay upload en progreso)
9. Usuario descarta el archivo y selecciona uno correcto

**Flujos Alternativos**:
- **3a. Error en tamaño**:
  - Sistema muestra: "Archivo demasiado grande: 75MB (límite: 50MB)"
  - Sugerencia: "Divide el archivo en partes más pequeñas"
  
- **6a. Error en encabezados**:
  - Sistema muestra comparación visual de headers encontrados vs esperados
  - Sugerencia: "Verifica que la primera línea contenga los nombres de columna correctos"

- **9a. Error en datos (campo numérico con texto)**:
  - Sistema muestra: "Campo 'Cantidad' debe ser numérico (línea 5, valor: 'N/A')"
  - Sugerencia: "Reemplaza valores no numéricos con 0 o elimina la fila"

**Postcondiciones**: 
- Archivo NO subido
- Usuario informado del problema específico
- Usuario puede corregir y reintentar

---

### 3.3. Requisitos No Funcionales

**Performance**:
- Tiempo de validación: **< 2 segundos** para archivos de hasta 50MB
- Lectura de archivo: Solo primeras 100KB (suficiente para header + muestra)
- No bloquear la UI: Mostrar spinner durante validación
- Si archivo > 10MB, considerar validación progresiva con feedback

**Seguridad**:
- Autenticación requerida: **Sí** (ya implementado en DataIngestionPage)
- Roles permitidos: **Administradores** (ya validado por rutas protegidas)
- Validación de datos: 
  - Sanitizar contenido leído (prevenir XSS si se muestra en UI)
  - No ejecutar código del archivo
  - Validar tamaño antes de leer (prevenir DoS por archivos gigantes)
- **Nota**: Esta validación NO reemplaza la validación del backend (defensa en profundidad)

**Usabilidad**:
- Accesibilidad: Mensajes de error con etiquetas ARIA para screen readers
- Responsive: Componente de validación debe adaptarse a móvil
- Idiomas: Español (actual del sistema)
- Feedback visual claro: Íconos (✅ ❌ ⚠️) + colores (verde, rojo, amarillo)
- Mensajes accionables: No solo decir "error", sino "qué hacer para arreglarlo"

**Confiabilidad**:
- Manejo de errores: Si la validación falla (ej: FileReader error), permitir upload con warning
- Degradación graceful: Si navegador no soporta FileReader API, saltar validación y permitir upload
- Rollback: Si hay problemas, se puede desactivar la validación con feature flag

---

## 4. Diseño de Interfaz (UX/UI)

### 4.1. Wireframes / Mockups

**Referencias de Diseño**:
- No hay mockups de Figma/Sketch aún
- Seguir patrones existentes de la plantilla (ComponentCard, toast notifications)

**Descripción Visual**:

```
┌─────────────────────────────────────────────────────────────┐
│ Centro de Ingesta de Datos de Producción                   │
└─────────────────────────────────────────────────────────────┘

┌─ Carga de Archivo TXT ─────────────────────────────────────┐
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  📁 Arrastra tu archivo .txt aquí                     │ │
│  │     o haz clic para seleccionar                       │ │
│  │                                                        │ │
│  │  [archivo seleccionado: produccion.txt - 15MB]        │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  ┌─ Resultado de Validación ──────────────────────────────┐│
│  │ ✅ Extensión: .txt                                     ││
│  │ ✅ Tamaño: 15MB / 50MB                                 ││
│  │ ✅ Codificación: UTF-8                                 ││
│  │ ✅ Encabezados: 9/9 columnas coinciden                 ││
│  │ ✅ Datos (10 líneas muestreadas): Sin errores          ││
│  │                                                        ││
│  │ [Confirmar y Subir]  [Cancelar]                       ││
│  └────────────────────────────────────────────────────────┘│
│                                                             │
└─────────────────────────────────────────────────────────────┘

┌─ Historial de Cargas ──────────────────────────────────────┐
│ [Tabla con uploads anteriores...]                          │
└─────────────────────────────────────────────────────────────┘
```

**Estado de Error**:
```
┌─ Resultado de Validación ──────────────────────────────────┐
│ ❌ Extensión: .xlsx (esperado: .txt)                       │
│                                                            │
│ 💡 Sugerencia:                                             │
│    Exporta el archivo como TXT delimitado por pipes (|)   │
│    desde Excel: Guardar como → Texto (delimitado por      │
│    tabulaciones) → Reemplazar tabs por pipes              │
│                                                            │
│ [Seleccionar otro archivo]                                 │
└────────────────────────────────────────────────────────────┘
```

---

### 4.2. Flujo de Usuario

```
[Página de Ingesta]
        ↓
[Usuario arrastra archivo]
        ↓
[Validación automática inicia]
        ↓
    (Spinner 1-2seg)
        ↓
    ┌───────┴───────┐
    ↓               ↓
[✅ Válido]    [❌ Error]
    ↓               ↓
[Mostrar        [Mostrar error
 resumen]        detallado]
    ↓               ↓
[Botón           [Bloquear upload]
 "Confirmar"]        ↓
    ↓           [Usuario corrige
[Upload al       archivo]
 backend]            ↓
    ↓           [Reintenta]
[Polling            ↓
 status]        [Vuelve a
    ↓            validar]
[Éxito/Fallo]
```

---

### 4.3. Estados de la Interfaz

**Estados a Considerar**:
- [x] **Estado inicial**: Dropzone vacío, esperando archivo
- [x] **Estado validando**: Spinner con mensaje "Validando archivo..."
- [x] **Estado válido**: Resumen con ✅ y botón "Confirmar y Subir" habilitado
- [x] **Estado con errores**: Resumen con ❌, botón deshabilitado, sugerencias visibles
- [x] **Estado con warnings (modo permisivo)**: Resumen con ⚠️, botón habilitado pero con advertencia
- [x] **Estado subiendo**: Barra de progreso + botón "Cancelar" (estado existente)
- [x] **Estado éxito/fallo**: Toast notification (estado existente)

---

## 5. Contratos de API (Preliminares)

### 5.1. Endpoints Nuevos

**No se requieren endpoints nuevos**. La validación es 100% client-side.

---

### 5.2. Endpoints Existentes (Sin Modificar)

Los endpoints existentes se mantienen sin cambios:

```typescript
// Endpoint de inicio de proceso ETL (sin modificar)
POST /api/etl/start-process
Request: FormData { file: File }
Response: { jobId: string, fileName: string, ... }

// Endpoint de consulta de estado (sin modificar)
GET /api/etl/jobs/{jobId}/status
Response: { status: 'EXITO' | 'FALLO' | 'EN_PROGRESO', details: string }
```

**Nota**: El backend sigue siendo responsable de la validación definitiva. La validación del frontend es una optimización de UX, no un reemplazo.

---

## 6. Modelo de Datos (Preliminar)

### 6.1. Nuevas Entidades

**No se requieren nuevas entidades en base de datos**.

### 6.2. Tipos TypeScript Nuevos

```typescript
// Tipo para resultado de validación
interface ValidationResult {
  isValid: boolean;
  errors: ValidationError[];
  warnings: ValidationWarning[];
  summary: ValidationSummary;
}

interface ValidationError {
  field: string;
  message: string;
  line?: number;
  value?: string;
  suggestion?: string;
}

interface ValidationWarning {
  field: string;
  message: string;
  line?: number;
}

interface ValidationSummary {
  extension: 'valid' | 'invalid';
  size: { current: number; max: number; valid: boolean };
  encoding: 'UTF-8' | 'ISO-8859-1' | 'unknown';
  headers: { found: string[]; expected: string[]; valid: boolean };
  dataSample: { linesValidated: number; errors: number; warnings: number };
}

``
