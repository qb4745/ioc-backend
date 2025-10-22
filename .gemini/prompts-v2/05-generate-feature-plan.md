# GENERADOR DE FEATURE PLAN (v1)
## 1. CONFIGURACIÓN
**Propósito**: Convertir una idea o requerimiento de negocio en un Feature Plan estructurado y completo, listo para ser convertido en Technical Design.
**Input**: 
- Idea/requerimiento del usuario (texto libre)
- Context: `@.gemini/project-summary.md`
**Output**: 
- `@.gemini/sprints/feature-plans/FP-XXX-[nombre].md`
- Checklist de próximos pasos
**Audiencia del Documento**: Product Owner, Tech Lead, Stakeholders, Equipo de Desarrollo
---
## 2. MANDATO OPERATIVO (PARA LA IA)
**Tu Rol**: Product Manager Senior con experiencia técnica, capaz de traducir ideas de negocio en especificaciones accionables.
**Tu Misión**: 
1. Entender profundamente la idea/problema del usuario
2. Hacer preguntas clarificadoras si falta información crítica
3. Leer el contexto del proyecto (project-summary.md)
4. Generar un Feature Plan completo siguiendo la plantilla
5. Asegurar que es accionable (no ambiguo, con criterios medibles)
---
## 3. PROTOCOLO DE EJECUCIÓN
### FASE 1: Recopilación de Información
#### Acción 1.1: Identificar Tipo de Input
El usuario puede proporcionar el requerimiento en diferentes formatos:
**Formato A: Idea General** (más común)
```

"Quiero agregar notificaciones en tiempo real para cuando termine un trabajo ETL"

```
**Formato B: Historia de Usuario**
```

Como administrador, quiero recibir notificaciones cuando un archivo termine de procesarse,
para no tener que refrescar manualmente la página.

```
**Formato C: Problema Específico**
```

Los usuarios se quejan de que no saben cuándo termina el procesamiento de archivos.
Tenemos que reducir el tiempo de espera percibido.

```
**Formato D: Requerimiento Técnico**
```

Necesitamos implementar WebSocket para push notifications desde el backend.

```
**Tu tarea**: Detectar el formato y extraer:
- `problema_core`: ¿Qué problema se intenta resolver?
- `solucion_propuesta`: ¿Hay alguna solución específica mencionada?
- `usuarios_afectados`: ¿Quiénes se benefician?
- `urgencia`: ¿Es crítico, importante, o nice-to-have?
---
#### Acción 1.2: Cargar Contexto del Proyecto
```bash
# Leer project-summary.md para entender:
- Stack tecnológico actual
- Arquitectura existente
- Features ya implementadas
- Servicios externos disponibles
```

**Extraer del Project Summary**:

```typescript
interface ProjectContext {
 nombre: string;
 proposito: string;
 stackFrontend: {
 framework: string;
 lenguaje: string;
 libreriasClave: string[];
 };
 stackBackend: {
 framework: string;
 lenguaje: string;
 libreriasClave: string[];
 };
 serviciosExternos: string[];
 featuresExistentes: string[];
 endpointsActuales: Endpoint[];
}
```

**Si NO encuentras project-summary.md**:

```markdown
⚠️ ADVERTENCIA: Contexto del Proyecto No Encontrado
No se encontró `@.gemini/project-summary.md`.
Puedo generar un Feature Plan genérico, pero será más preciso si primero generas el Project Summary.
🔧 Opciones:
A) Generar Feature Plan genérico (sin contexto técnico específico)
B) Generar primero el Project Summary
 ```bash
 cd ioc-backend
 gemini-cli < @.gemini/prompts/01-generate-project-summary-backend.md
```

C) Proporcionarme información del proyecto manualmente
¿Qué prefieres? (A/B/C)
[ESPERAR RESPUESTA]

```
---
#### Acción 1.3: Hacer Preguntas Clarificadoras (Modo Socrático)
**Objetivo**: Llenar vacíos de información CRÍTICA antes de generar el documento.
**Preguntas por Categoría**:
##### 1. Sobre el PROBLEMA:
```markdown
🤔 PREGUNTAS SOBRE EL PROBLEMA
He entendido que quieres: [RESUMEN DE LA IDEA]
Para crear un Feature Plan completo, necesito clarificar:
1. **Contexto del Problema**:
 - ¿Cuál es la situación actual que hace necesaria esta feature?
 - ¿Qué pasa si NO implementamos esto? (impacto de no hacer nada)
2. **Usuarios Afectados**:
 - ¿Quiénes específicamente necesitan esta feature? (roles/personas)
 - ¿Con qué frecuencia enfrentan este problema? (diario, semanal, ocasional)
3. **Severidad**:
 - En escala de 1-10, ¿qué tan crítico es este problema?
 - ¿Es un bloqueante, un pain point, o una mejora nice-to-have?
[SI EL USUARIO NO RESPONDE CON DETALLE, HACER INFERENCIAS RAZONABLES Y MARCAR COMO [ASUMIR]]
```

##### 2. Sobre la SOLUCIÓN:

```markdown
🎯 PREGUNTAS SOBRE LA SOLUCIÓN
[SI EL USUARIO YA PROPUSO UNA SOLUCIÓN]:
Has mencionado [SOLUCIÓN]. 
- ¿Es esta la única solución que consideras, o estás abierto a alternativas?
- ¿Hay restricciones técnicas que obligan a esta solución?
[SI EL USUARIO SOLO DESCRIBIÓ EL PROBLEMA]:
Entiendo el problema. Tengo algunas ideas de solución:
Opción A: [Solución 1 basada en contexto]
Opción B: [Solución 2 alternativa]
¿Alguna de estas se alinea con lo que tienes en mente, o prefieres algo diferente?
[ESPERAR RESPUESTA O ASUMIR LA MÁS SIMPLE]
```

##### 3. Sobre el ALCANCE:

```markdown
📏 PREGUNTAS SOBRE ALCANCE
Para el MVP (primera versión), ¿qué es absolutamente esencial?
Te ayudo a priorizar. De estas funcionalidades, marca cuáles son MUST-HAVE vs NICE-TO-HAVE:
- [ ] [Funcionalidad core 1 detectada]
- [ ] [Funcionalidad core 2 detectada]
- [ ] [Funcionalidad adicional 1]
- [ ] [Funcionalidad adicional 2]
¿Hay algo más que debería estar en el alcance inicial?
[SI NO HAY RESPUESTA, ASUMIR MVP MÍNIMO]
```

##### 4. Sobre URGENCIA y PRIORIDAD:

```markdown
⏰ PREGUNTAS SOBRE TIMING
- ¿Para cuándo necesitas esto? (sprint actual, próximo sprint, backlog)
- ¿Hay alguna fecha límite de negocio? (lanzamiento, demo, evento)
- ¿Qué tan flexible es el deadline?
Estimación preliminar (sin diseño técnico aún): [X-Y] días de desarrollo.
¿Esto se alinea con tus expectativas de tiempo?
[SI NO HAY RESPUESTA, MARCAR COMO "Sprint Próximo - Prioridad Media"]
```

---

**REGLA IMPORTANTE**: 

- Máximo 3 rondas de preguntas
- Si el usuario no responde o da respuestas vagas, **hacer suposiciones razonables** basadas en:
  - Contexto del proyecto
  - Mejores prácticas
  - MVP mínimo viable
- Marcar claramente en el documento las secciones que son `[INFERIDO]` o `[ASUMIR - VALIDAR CON EQUIPO]`

---

### FASE 2: Análisis y Estructuración

#### Acción 2.1: Generar ID del Feature Plan

```typescript
// Leer directorio de feature plans existentes
const featurePlans = listarArchivos('@.gemini/sprints/feature-plans/');
// Extraer números (FP-001, FP-002, etc.)
const numerosExistentes = featurePlans.map(fp => {
 const match = fp.match(/FP-(\d+)/);
 return match ? parseInt(match[1]) : 0;
});
// Siguiente número
const siguienteNumero = Math.max(...numerosExistentes, 0) + 1;
const nuevoID = `FP-${siguienteNumero.toString().padStart(3, '0')}`;
// Generar slug del nombre
const nombreFeature = extraerNombreDeIdea(ideaUsuario);
const slug = nombreFeature.toLowerCase().replace(/\s+/g, '-');
// Nombre final del archivo
const nombreArchivo = `${nuevoID}-${slug}.md`;
// Ejemplo: FP-005-real-time-notifications.md
```

---

#### Acción 2.2: Analizar Impacto Técnico

Basado en la idea y el project summary, determinar:

```typescript
interface ImpactoTecnico {
 frontend: {
 componentesNuevos: string[]; // Inferir de la solución
 componentesAModificar: string[]; // Buscar en project summary
 nuevasRutas: string[];
 nuevasDependencias: string[]; // Sugerir basado en solución
 };
 backend: {
 endpointsNuevos: Endpoint[]; // Inferir contratos preliminares
 endpointsAModificar: Endpoint[];
 nuevosServicios: string[];
 nuevasDependencias: string[];
 };
 baseDatos: {
 tablas nuevas: Tabla[]; // Inferir modelo preliminar
 tablasAModificar: string[];
 migracionesRequeridas: boolean;
 };
 integraciones: {
 serviciosExternos: string[]; // Si requiere APIs de terceros
 };
}
```

**Ejemplo de Inferencia**:
Si el usuario dijo: "Notificaciones en tiempo real cuando termina un ETL"
Inferir:

```typescript
{
 frontend: {
 componentesNuevos: [
 "NotificationBell (badge en navbar)",
 "NotificationCenter (modal con lista)",
 "NotificationItem (item individual)"
 ],
 componentesAModificar: [
 "Navbar (agregar NotificationBell)",
 "ETLPage (mostrar notificación al subir archivo)"
 ],
 nuevasRutas: [], // No requiere rutas nuevas
 nuevasDependencias: [
 "socket.io-client (WebSocket para real-time)"
 ]
 },
 backend: {
 endpointsNuevos: [
 { method: "WS", path: "/ws/notifications", desc: "WebSocket para push notifications" },
 { method: "GET", path: "/api/v1/notifications", desc: "Obtener historial" },
 { method: "PATCH", path: "/api/v1/notifications/{id}/read", desc: "Marcar como leída" }
 ],
 nuevasDependencias: [
 "spring-boot-starter-websocket"
 ]
 },
 baseDatos: {
 tablasNuevas: [
 {
 nombre: "notifications",
 campos: ["id", "user_id", "type", "message", "is_read", "created_at"]
 }
 ]
 }
}
```

---

#### Acción 2.3: Generar Historias de Usuario

Convertir la idea en historias de usuario estructuradas en formato Gherkin.
**Algoritmo**:

1. Identificar actores (del problema descrito)
2. Identificar acciones clave (verbos en la solución)
3. Identificar resultados esperados
4. Generar criterios de aceptación en Gherkin
   **Ejemplo**:
   Input: "Notificaciones cuando termina ETL"
   Output:
   
   ```gherkin
   Historia 1: Recibir notificación de éxito
   Como administrador,
   Quiero recibir una notificación cuando un archivo ETL termine de procesarse exitosamente,
   Para saber que puedo revisar los resultados sin tener que refrescar manualmente.
   Criterios de Aceptación:
   Escenario: ETL exitoso con usuario conectado
   Dado que he subido un archivo ETL
   Y estoy conectado a la aplicación
   Cuando el procesamiento termina exitosamente
   Entonces veo una notificación toast con mensaje "Archivo procesado: [nombre]"
   Y veo un badge con contador en el ícono de notificaciones
   Y el badge incrementa en 1
   Escenario: ETL exitoso con usuario desconectado
   Dado que he subido un archivo ETL
   Y he cerrado la aplicación
   Cuando vuelvo a entrar después de que el procesamiento terminó
   Entonces veo el badge con las notificaciones no leídas
   Y puedo abrir el centro de notificaciones para ver el historial
   ```

---

Historia 2: Recibir notificación de error
Como administrador,
Quiero recibir una notificación cuando un archivo ETL falle,
Para poder tomar acción correctiva rápidamente.
Criterios de Aceptación:
Escenario: ETL falla por datos inválidos
 Dado que he subido un archivo ETL con datos incorrectos
 Cuando el procesamiento falla
 Entonces veo una notificación de error con el motivo
 Y puedo hacer clic en la notificación para ver detalles del error
 Y tengo opción de re-intentar o corregir el archivo

```
Generar **mínimo 2-3 historias** que cubran:
- Happy path
- Casos de error
- Edge cases importantes
---
#### Acción 2.4: Identificar Riesgos
Basado en la complejidad técnica, identificar riesgos automáticamente:
```typescript
const riesgos = [];
// RIESGO 1: Nuevas dependencias
if (impactoTecnico.frontend.nuevasDependencias.length > 0 || 
impactoTecnico.backend.nuevasDependencias.length > 0) {
 riesgos.push({
 id: "R1",
 descripcion: "Nuevas dependencias pueden introducir vulnerabilidades o conflictos",
 probabilidad: "Media",
 impacto: "Medio",
 severidad: "🟡",
 mitigacion: "Auditar dependencias con npm audit / OWASP dependency check"
 });
}
// RIESGO 2: Cambios en base de datos
if (impactoTecnico.baseDatos.tablasNuevas.length > 0) {
 riesgos.push({
 id: "R2",
 descripcion: "Migraciones de base de datos pueden fallar en producción",
 probabilidad: "Baja",
 impacto: "Alto",
 severidad: "🟡",
 mitigacion: "Probar migraciones en staging, tener rollback plan, backup de BD"
 });
}
// RIESGO 3: Integraciones externas
if (impactoTecnico.integraciones.serviciosExternos.length > 0) {
 riesgos.push({
 id: "R3",
 descripcion: "Servicio externo puede estar down o cambiar API",
 probabilidad: "Media",
 impacto: "Alto",
 severidad: "🔴",
 mitigacion: "Implementar circuit breaker, timeouts, y degradación graceful"
 });
}
// RIESGO 4: Estimación insuficiente (siempre incluir)
riesgos.push({
 id: "R4",
 descripcion: "Complejidad subestimada, especialmente en testing e integración",
 probabilidad: "Alta",
 impacto: "Medio",
 severidad: "🟡",
 mitigacion: "Agregar 30% de buffer a la estimación, hacer spikes técnicos si hay incertidumbre"
});
```

---

#### Acción 2.5: Generar Estimación Preliminar

```typescript
// Sistema de puntos basado en complejidad
let puntosTotal = 0;
// Frontend
puntosTotal += impactoTecnico.frontend.componentesNuevos.length * 2; // 2 pts por componente nuevo
puntosTotal += impactoTecnico.frontend.componentesAModificar.length * 1;
puntosTotal += impactoTecnico.frontend.nuevasRutas.length * 1;
// Backend
puntosTotal += impactoTecnico.backend.endpointsNuevos.length * 3; // 3 pts por endpoint
puntosTotal += impactoTecnico.backend.endpointsAModificar.length * 2;
puntosTotal += impactoTecnico.backend.nuevosServicios.length * 4;
// Base de datos
puntosTotal += impactoTecnico.baseDatos.tablasNuevas.length * 2;
puntosTotal += impactoTecnico.baseDatos.tablasAModificar.length * 1;
// Integraciones
puntosTotal += impactoTecnico.integraciones.serviciosExternos.length * 5; // Alto riesgo
// Convertir a días (1 punto = ~0.5 días)
const diasEstimados = Math.ceil(puntosTotal * 0.5);
// Agregar buffer del 30%
const diasConBuffer = Math.ceil(diasEstimados * 1.3);
// Generar rango
const estimacion = {
 optimista: diasEstimados,
 realista: diasConBuffer,
 pesimista: Math.ceil(diasConBuffer * 1.5),
 storyPoints: puntosTotal
};
// Ejemplo de output:
// Estimación: 5-7 días (Story Points: 13)
```

---

### FASE 3: Generación del Documento

#### Acción 3.1: Llenar Plantilla

Usando toda la información recopilada y analizada, llenar la plantilla sección por sección:

```typescript
// Pseudo-código del proceso
const plantilla = leerArchivo('@.gemini/templates/feature-plan-template.md');
const datos = {
 // METADATA
 id: nuevoID,
 nombre: nombreFeature,
 sprint: "Sprint Actual + 1", // O el especificado por usuario
 prioridad: prioridadInferida,
 tipo: "Nueva Feature", // O el tipo inferido
 estimacion: `${estimacion.realista} días (${estimacion.storyPoints} SP)`,
 asignadoA: "Pendiente",
 estado: "Planificación",
 fechaCreacion: hoy(),

// SECCIÓN 1: Contexto de Negocio
 contextoActual: generarContextoActual(ideaUsuario, projectSummary),
 problemaEspecifico: extraerProblema(ideaUsuario),
 impactoProblema: inferirImpacto(ideaUsuario),
 solucionPropuesta: generarSolucion(ideaUsuario, projectSummary),
 valorUsuario: inferirValorUsuario(solucion),
 valorNegocio: inferirValorNegocio(solucion),
 alcanceMVP: generarAlcance(impactoTecnico),

// SECCIÓN 2: Análisis Técnico
 componentesAfectados: impactoTecnico,
 dependenciasTecnicas: extraerDependencias(impactoTecnico),
 impactoArquitectura: evaluarImpactoArquitectura(impactoTecnico),

// SECCIÓN 3: Requisitos Funcionales
 historiasUsuario: generarHistorias(ideaUsuario),
 casosUso: generarCasosUso(historias),
 requisitosNoFuncionales: generarNFRs(solucion),

// SECCIÓN 4: Diseño de Interfaz
 descripcionVisual: generarDescripcionUI(solucion),
 flujoUsuario: generarFlujoUsuario(historias),
 estados: generarEstadosUI(),

// SECCIÓN 5: Contratos de API
 endpointsNuevos: generarContratosPreliminares(impactoTecnico.backend.endpointsNuevos),
 endpointsModificados: generarCambios(impactoTecnico.backend.endpointsAModificar),

// SECCIÓN 6: Modelo de Datos
 nuevasEntidades: generarModeloDatos(impactoTecnico.baseDatos.tablasNuevas),
 modificaciones: generarAlters(impactoTecnico.baseDatos.tablasAModificar),

// SECCIÓN 7: Plan de Implementación
 fases: generarFases(estimacion, impactoTecnico),
 estimacionTotal: estimacion,

// SECCIÓN 8: Riesgos
 riesgos: riesgos,
 mitigaciones: generarMitigaciones(riesgos),

// SECCIÓN 9: Criterios de Éxito
 metricasAceptacion: generarMetricas(solucion),
 definicionHecho: generarDoD(),

// SECCIÓN 10: Decisiones Pendientes
 decisionesPendientes: identificarDecisionesPendientes(impactoTecnico),

// SECCIÓN 11-14: Metadata
 proximosPasos: generarChecklistProximosPasos(),
 referencias: generarReferencias(projectSummary),
 aprobaciones: generarTablaAprobaciones(),
 changelog: generarEntradaInicial()
};
// Reemplazar placeholders en la plantilla
let documentoFinal = plantilla;
for (const [clave, valor] of Object.entries(datos)) {
 documentoFinal = reemplazar(documentoFinal, `[${clave}]`, valor);
}
// Escribir archivo
const rutaSalida = `@.gemini/sprints/feature-plans/${nombreArchivo}`;
escribirArchivo(rutaSalida, documentoFinal);
```

---

#### Acción 3.2: Validar Completitud del Documento

Antes de guardar, validar que no haya placeholders sin llenar:

```typescript
const validaciones = [
 {
 nombre: "Sin placeholders vacíos",
 check: () => !documentoFinal.includes('[PENDIENTE]') && !documentoFinal.includes('[COMPLETAR]'),
 severidad: "ERROR"
 },
 {
 nombre: "Al menos 2 historias de usuario",
 check: () => contarHistorias(documentoFinal) >= 2,
 severidad: "WARNING"
 },
 {
 nombre: "Al menos 1 riesgo identificado",
 check: () => contarRiesgos(documentoFinal) >= 1,
 severidad: "WARNING"
 },
 {
 nombre: "Estimación presente",
 check: () => documentoFinal.includes('Estimación Total'),
 severidad: "ERROR"
 },
 {
 nombre: "Criterios de aceptación en Gherkin",
 check: () => documentoFinal.includes('Dado que') && documentoFinal.includes('Cuando'),
 severidad: "WARNING"
 }
];
const fallos = validaciones.filter(v => !v.check());
if (fallos.some(f => f.severidad === "ERROR")) {
 MOSTRAR_ERROR(`
 ❌ Validación Fallida

El documento generado tiene problemas críticos:
 ${fallos.filter(f => f.severidad === "ERROR").map(f => `- ${f.nombre}`).join('\n')}

No se guardará hasta resolver estos problemas.
 `);
 DETENER();
}
if (fallos.some(f => f.severidad === "WARNING")) {
 ADVERTENCIA(`
 ⚠️ Advertencias Detectadas

${fallos.filter(f => f.severidad === "WARNING").map(f => `- ${f.nombre}`).join('\n')}

El documento se guardará, pero considera completar estas secciones manualmente.
 `);
}
```

---

### FASE 4: Output y Próximos Pasos

#### Acción 4.1: Guardar Documento

```bash
# Crear directorios si no existen
mkdir -p @.gemini/sprints/feature-plans
# Escribir archivo
echo "$documentoFinal" > @.gemini/sprints/feature-plans/${nombreArchivo}
```

---

#### Acción 4.2: Generar Resumen Ejecutivo

```markdown
✅ FEATURE PLAN GENERADO EXITOSAMENTE
**Archivo**: `@.gemini/sprints/feature-plans/FP-005-real-time-notifications.md`
---
## 📋 Resumen del Feature Plan
**Feature**: Real-Time Notifications 
**ID**: FP-005 
**Prioridad**: Alta 
**Estimación**: 5-7 días (13 Story Points)
**Problema**:
Los administradores no saben cuándo termina el procesamiento de archivos ETL, lo que genera ineficiencia y mala UX.
**Solución**:
Sistema de notificaciones en tiempo real usando WebSocket que informa al usuario cuando un trabajo ETL termina (éxito o error).
**Impacto Técnico**:
- Frontend: 3 componentes nuevos, 1 dependencia (socket.io-client)
- Backend: 3 endpoints nuevos (1 WebSocket, 2 REST), 1 dependencia
- Base de Datos: 1 tabla nueva (notifications)
**Riesgos Identificados**: 4 (1 crítico, 3 medios)
---
## 🎯 Próximos Pasos
### 1. Revisar y Aprobar (Humanos)
```bash
# Abrir el documento
code @.gemini/sprints/feature-plans/FP-005-real-time-notifications.md
# Revisar:
- [ ] Sección 1: ¿El problema está bien definido?
- [ ] Sección 3: ¿Las historias de usuario cubren todo?
- [ ] Sección 7: ¿La estimación es realista?
- [ ] Sección 8: ¿Los riesgos están bien identificados?
- [ ] Sección 10: ¿Hay decisiones pendientes que resolver?
```

### 2. Completar Decisiones Pendientes

Hay **2 decisiones técnicas** que requieren tu input:
**D1**: ¿Usar WebSocket nativo o Socket.IO?

- Opción A: WebSocket nativo (menos deps)
- Opción B: Socket.IO (más features, reconexión auto)
- **Recomendación IA**: Socket.IO por resiliencia
  **D2**: ¿Dónde persistir notificaciones?
- Opción A: PostgreSQL (consistencia)
- Opción B: Redis (performance)
- **Recomendación IA**: PostgreSQL (menos complejidad)
  
  ### 3. Aprobar con Stakeholders
  
  Compartir este Feature Plan con:

- [ ] Product Owner
- [ ] Tech Lead
- [ ] Frontend Lead
- [ ] Backend Lead
  
  ### 4. Crear Technical Design (Siguiente Paso)
  
  Una vez aprobado este FP, ejecutar:
  
  ```bash
  gemini-cli < @.gemini/prompts/06-generate-technical-design.md \
  --feature-plan=FP-005
  ```
  
  Esto generará:

- `@.gemini/sprints/technical-designs/TD-005-real-time-notifications.md`
  
  ### 5. Agregar al Sprint Backlog (Opcional - Manual)
  
  Editar `@.gemini/sprints/Sprint-X-Backlog.md` y agregar:
  ```markdown
  
  ## Feature: Real-Time Notifications (FP-005)
  
  **Estimación**: 13 SP
  **Prioridad**: Alta
  
  ### User Stories:

- [ ] US-1: Recibir notificación de éxito
- [ ] US-2: Recibir notificación de error
- [ ] US-3: Ver historial de notificaciones
  **Docs**: 

- Feature Plan: `feature-plans/FP-005-real-time-notifications.md`
- Technical Design: Pendiente
  
  ```
  
  ```

---

## 📊 Estadísticas del Documento Generado

- **Secciones completadas**: 14/14 (100%)
- **Historias de usuario**: 3
- **Criterios de aceptación**: 6 escenarios
- **Endpoints nuevos**: 3
- **Tablas nuevas**: 1
- **Riesgos identificados**: 4
- **Decisiones pendientes**: 2

---

## ⚠️ Advertencias

[SI HAY INFERENCIAS]:
Las siguientes secciones fueron **inferidas** porque no proporcionaste información específica:

- Sección 1.3: Alcance del MVP → Asumí funcionalidad mínima
- Sección 4.1: Diseño de UI → Generé descripción textual (sin mockups)
  Revisa estas secciones y ajusta según sea necesario.
  [SI HAY DECISIONES PENDIENTES]:
  Hay **2 decisiones técnicas** sin resolver. El desarrollo no puede empezar hasta que se resuelvan.

---

**Feature Plan generado por**: IA Feature Plan Generator v1 
**Tiempo de generación**: 12 segundos 
**Fecha**: 2024-01-15 10:45:00

```
---
## 4. REGLAS DE CALIDAD
### Regla 1: Preferir Inferencias Razonables sobre Placeholders Vacíos
❌ MAL:
```markdown
**Problema Específico**: [COMPLETAR - Describir el problema]
```

✅ BIEN:

```markdown
**Problema Específico**: 
Los administradores que cargan archivos ETL deben hacer polling manual (refrescar la página cada 30 segundos) para saber si el procesamiento terminó, lo que genera una experiencia de usuario ineficiente y aumenta la percepción de lentitud del sistema.
[INFERIDO del requerimiento "notificaciones en tiempo real para ETL" - VALIDAR CON EQUIPO]
```

---

### Regla 2: Generar Criterios de Aceptación Testables

❌ MAL:

```gherkin
Entonces el usuario está feliz
```

✅ BIEN:

```gherkin
Entonces veo una notificación toast en la esquina superior derecha
Y el mensaje dice "Archivo procesado: datos.csv"
Y el toast desaparece después de 5 segundos
Y el badge de notificaciones incrementa de 0 a 1
```

---

### Regla 3: Estimaciones Realistas (No Optimistas)

Siempre agregar buffer del 30% mínimo:

```
Cálculo base: 5 días
Buffer 30%: 6.5 días
Redondear: 7 días
Presentar como rango: "5-7 días"
```

---

### Regla 4: Riesgos Reales (No Genéricos)

❌ MAL:

```
R1: El proyecto puede retrasarse
```

✅ BIEN:

```
R1: La integración con WebSocket puede tener problemas de reconexión si el usuario está en redes inestables (mobile), causando pérdida de notificaciones
Probabilidad: Alta en usuarios mobile
Impacto: Alto (UX degradada)
Mitigación: Implementar queue de notificaciones en backend, sincronizar al reconectar
```

---

## 5. EJEMPLO DE EJECUCIÓN

```bash
$ gemini-cli < @.gemini/prompts/05-generate-feature-plan.md
[IA] 🚀 Feature Plan Generator v1 iniciado
[IA] 📝 Por favor, describe la feature que quieres implementar:
[IA] (Puede ser una idea general, un problema, o una historia de usuario)
[Usuario] > Quiero que los usuarios puedan exportar los dashboards de Metabase a PDF
[IA] 🔍 Entendido. Analizando idea...
[IA] 📚 Cargando contexto del proyecto...
[IA] ✅ Leído: @.gemini/project-summary.md
[IA] ℹ️ Proyecto: IOC Platform (React + Spring Boot + Metabase)
[IA] 🤔 Tengo algunas preguntas para clarificar:
[IA] **1. Contexto del Problema**:
 - ¿Por qué los usuarios necesitan exportar a PDF? ¿Qué hacen con esos PDFs?
 - Opción A: Compartir con stakeholders sin acceso al sistema
 - Opción B: Archivar reportes para compliance/auditoría
 - Opción C: Otro (especifica)
[Usuario] > A y B
[IA] **2. Usuarios Afectados**:
 - ¿Todos los usuarios o solo ciertos roles?
[Usuario] > Solo administradores y analistas
[IA] **3. Alcance del MVP**:
 - Exportar dashboard completo, o permitir seleccionar qué gráficos incluir?
[Usuario] > Solo dashboard completo por ahora
[IA] ✅ Perfecto, tengo suficiente información.
[IA] 🔬 Analizando impacto técnico...
[IA] ✅ Detectado: Metabase tiene API de exportación (usaremos esa)
[IA] ✅ Frontend: Agregar botón "Exportar PDF" en DashboardView
[IA] ✅ Backend: Proxy request a Metabase API + autenticación
[IA] 📊 Generando estimación...
[IA] ✅ Estimación: 3-4 días (8 Story Points)
[IA] 📝 Generando Feature Plan...
[IA] ✅ ID asignado: FP-006
[IA] ✅ Generadas 2 historias de usuario
[IA] ✅ Generados 4 criterios de aceptación
[IA] ✅ Identificados 3 riesgos
[IA] ✅ Generado plan de implementación en 2 fases
[IA] 💾 Guardando documento...
[IA] ✅ Archivo creado: @.gemini/sprints/feature-plans/FP-006-export-dashboard-pdf.md
[Mostrar resumen ejecutivo como en Acción 4.2]
```

---
