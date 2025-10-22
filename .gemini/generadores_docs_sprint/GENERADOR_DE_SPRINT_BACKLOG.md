# PROMPT: Generador de Sprint Backlog

Actúa como Scrum Master. Genera un Sprint Backlog completo y detallado 
para el sprint basándote en la información proporcionada.

## INFORMACIÓN DEL SPRINT

**Proyecto:** [Nombre del proyecto]
**Sprint:** [Número y nombre del sprint]
**Período:** [Fecha inicio - Fecha fin]
**Duración:** [X] semanas / [Y] días hábiles
**Equipo:** [Nombres y roles]
**Festivos/No laborables:** [Si aplica]

---

## SPRINT GOAL

[Describe el objetivo del sprint en 1-2 frases que capture el valor de negocio 
que se entregará. Debe ser inspirador y medible]

Ejemplo: "Entregar un ciclo de valor E2E: un usuario podrá autenticarse, 
cargar datos CSV y visualizar dashboards inmediatamente"

---

## HISTORIAS COMPROMETIDAS

[Lista de historias que el equipo se compromete a completar]

**Formato:**
| ID | Título | Feature | Prioridad | SP | Asignado | Estado |

**Fuente:** Product Backlog seleccionado en Sprint Planning

---

## TAREAS TÉCNICAS

[Descomposición de cada historia en tareas técnicas específicas]

**Para cada historia, proporciona:**
- Tareas de Frontend (FE-TASK-XX)
- Tareas de Backend (BE-TASK-XX)
- Tareas de Testing (TEST-TASK-XX) si aplica
- Tareas de DevOps (OPS-TASK-XX) si aplica

**Formato:**
| Nº | ID | Capa | Historia | Responsable | Descripción | Estado |

---

## INSTRUCCIONES DE GENERACIÓN

### 1. ESTRUCTURA OBLIGATORIA

Genera el documento con estas secciones:

**a) Metadata del Sprint**
- Período del Sprint (fechas, duración, días hábiles)
- Objetivo del Sprint (Sprint Goal)

**b) Historias Comprometidas**
- Tabla con todas las historias del sprint
- Incluir: ID, Título completo, Tipo, Feature, Prioridad, SP, Asignado, Estado

**c) Criterios de Aceptación**
- Para CADA historia, listar sus criterios de aceptación
- Formato: ✅ [Criterio específico y medible]
- Mínimo 3 criterios por historia

**d) Checklist de Tareas Técnicas**
- Tabla detallada de todas las tareas
- Numeradas secuencialmente
- Organizadas por capa (Frontend, Backend, Testing, etc.)
- Cada tarea con descripción específica

**e) Progreso del Sprint**
- Historias completadas: X/Y (%)
- Story Points completados: X/Y (%)
- Lista de historias completadas

**f) Riesgos y Dependencias**
- Dependencias técnicas (servicios externos, APIs, etc.)
- Dependencias entre historias
- Riesgos identificados con mitigación

**g) Lecciones Aprendidas** (al finalizar el sprint)
- Qué funcionó bien (✅)
- Qué mejorar (📝)

---

### 2. CRITERIOS DE CALIDAD

**Historias:**
- ✅ Seguir formato: "Como [rol], quiero [acción] para [beneficio]"
- ✅ Tener prioridad clara (Crítica/Alta/Media/Baja)
- ✅ Story Points estimados
- ✅ Asignación tentativa (puede cambiar en daily)

**Criterios de Aceptación:**
- ✅ Específicos y medibles
- ✅ Formato: "Dado [contexto], cuando [acción], entonces [resultado]"
- ✅ Cubrir casos de éxito, error y límite
- ✅ Verificables (se puede demostrar en Sprint Review)

**Tareas Técnicas:**
- ✅ Granularidad: 2-8 horas de esfuerzo cada una
- ✅ Descripción clara de QUÉ hacer (no solo "implementar X")
- ✅ Organizadas lógicamente (dependencias respetadas)
- ✅ Incluir tareas de testing, no solo desarrollo

**Dependencias:**
- ✅ Identificar servicios externos críticos
- ✅ Documentar dependencias entre historias
- ✅ Especificar mitigación para cada riesgo

---

### 3. FORMATO DE ESTADOS

**Para historias:**
- 📋 Backlog
- 🔄 En Progreso
- 👁️ En Review
- ✅ Terminada
- ❌ Bloqueada
- ⏸️ En Espera

**Para tareas:**
- ⬜ Pendiente
- 🔄 En Progreso
- ✅ Terminada

---

### 4. DESCOMPOSICIÓN DE TAREAS

**Por cada historia, incluir tareas de:**

**Frontend (si aplica):**
- Componentes de UI
- Integración con APIs
- Manejo de estados
- Validaciones
- Testing E2E

**Backend (si aplica):**
- Endpoints/Controllers
- Servicios de negocio
- Repositorios/Entidades
- Validaciones
- Testing unitario/integración

**Infraestructura (si aplica):**
- Configuración de servicios
- Variables de entorno
- CI/CD
- Deployment

**Testing:**
- Tests unitarios
- Tests de integración
- Tests E2E
- Tests de performance (si aplica)

---

### 5. ESTIMACIÓN DE ESFUERZO

**Asegurar que:**
- Suma de SP comprometidos = Velocity del equipo
- Tareas distribuidas equilibradamente entre miembros
- Buffer del 10-15% para impedimentos
- No más de 8 horas de tareas por persona/día

---

## DATOS DE ENTRADA

**Proporciona:**

### Del Product Backlog:
[Lista de historias seleccionadas para el sprint]

Ejemplo:
IOC-021: Como usuario, quiero iniciar sesión (5 SP)
IOC-001: Como admin, quiero cargar CSV (13 SP)
IOC-006: Como gerente, quiero ver dashboard (13 SP)

text


### Del Equipo:
- Velocity histórica: [X] SP/sprint
- Miembros y capacidad: [Nombres con % de dedicación]
- Días no laborables: [Festivos, vacaciones]

### Técnico (si se conoce):
- Stack tecnológico: [Frontend: X, Backend: Y, DB: Z]
- Dependencias externas: [APIs, servicios cloud, etc.]
- Restricciones conocidas: [Performance, seguridad, etc.]

---

## FORMATO DE SALIDA

Genera el documento completo en Markdown siguiendo EXACTAMENTE 
la estructura del ejemplo proporcionado.

**Incluir:**
- Tablas bien formateadas
- Checkboxes (✅ ❌ 🔄) para estados
- Secciones claramente delimitadas
- Referencias a otros documentos (.gemini/...)
- Numeración secuencial de tareas

---

## EJEMPLO DE CALIDAD DE CRITERIOS

❌ **Malo:** "El sistema funciona correctamente"
✅ **Bien:** "Dado un usuario con credenciales válidas, cuando ingresa email y 
contraseña, entonces el sistema lo autentica y redirige al dashboard en <2 segundos"

❌ **Malo:** "Validar el archivo"
✅ **Bien:** "Dado un archivo CSV con columnas faltantes, cuando el usuario 
intenta cargarlo, entonces el sistema muestra mensaje de error específico 
indicando qué columnas faltan"

---

## EJEMPLO DE CALIDAD DE TAREAS

❌ **Malo:** "Implementar login"
✅ **Bien:** "Crear componente SignInPage.tsx con formulario de email/password, 
validación client-side, integración con Supabase Auth y redirección a dashboard 
tras login exitoso"

❌ **Malo:** "Hacer el backend"
✅ **Bien:** "Implementar EtlController.java con endpoint POST /api/v1/etl/start-process 
que recibe MultipartFile, valida formato CSV, crea registro EtlJob y retorna 
jobId con status 202 Accepted"

---

[PEGA AQUÍ LA INFORMACIÓN DEL SPRINT]

GENERA EL SPRINT BACKLOG COMPLETO AHORA.