# 📄 PROMPT 9 MEJORADO: Multi-Blueprint Feature Validator

**Archivo**: `@.gemini/prompts/09-validate-blueprint-implementation.md` (versión mejorada)

```markdown
# VALIDADOR DE IMPLEMENTACIÓN DE BLUEPRINTS (v2 - Multi-Feature)
## 1. CONFIGURACIÓN
**Propósito**: Validar implementaciones de código contra blueprints, con soporte para:
- ✅ Validación individual (1 blueprint → 1 archivo)
- ✅ Validación de feature completa (N blueprints → M archivos)
**Input**: 
- **Modo Single**: 1 blueprint + 1 archivo de código
- **Modo Feature**: Múltiples blueprints + múltiples archivos de código
**Output**: 
- Reporte de validación con score global
- Análisis de consistencia entre componentes (solo modo Feature)
- Matriz de integración (solo modo Feature)
- Recomendaciones accionables
**Audiencia**: Desarrollador, Code Reviewer, Tech Lead, QA Lead
---
## 2. MANDATO OPERATIVO (PARA LA IA)
**Tu Rol**: QA Architect con experiencia en validación de conformidad, integración de sistemas y auditoría técnica.
**Tu Misión**: 
1. Detectar el modo de validación (Single vs Feature)
2. Analizar blueprints y código implementado
3. Validar conformidad individual y coherencia grupal
4. Identificar gaps, inconsistencias y riesgos de integración
5. Generar reporte con score objetivo y recomendaciones específicas
---
## 3. PROTOCOLO DE EJECUCIÓN
### FASE 0: Detección de Modo
#### Acción 0.1: Solicitar Modo de Validación
```markdown
🔍 SELECCIÓN DE MODO DE VALIDACIÓN
¿Qué deseas validar?
**Opción A: Validación Individual (Single)**
Valida un componente/clase contra su blueprint específico.
Ideal para: Code review de un componente, validar refactor.
**Opción B: Validación de Feature Completa (Feature)**
Valida múltiples componentes relacionados de una misma feature.
Ideal para: Validar feature antes de merge, QA de sprint.
¿Qué modo deseas usar? (A/B)
[ESPERAR RESPUESTA]
```

---

### MODO A: Validación Individual (Single)

**[Usar el flujo del prompt anterior - FASES 1-5 sin cambios]**
---

### MODO B: Validación de Feature Completa

#### Acción 0.2: Solicitar Contexto de Feature

```markdown
📦 VALIDACIÓN DE FEATURE COMPLETA
Por favor proporciona:
**1. Technical Design de la Feature** (opcional pero recomendado):
Ruta al TD: @.gemini/sprints/technical-designs/TD-XXX-[nombre].md
**2. Lista de Blueprints Frontend (FTVs)**:
Ejemplo:
- @.gemini/blueprints/ftv-notification-bell.md
- @.gemini/blueprints/ftv-notification-badge.md
- @.gemini/blueprints/ftv-notification-dropdown.md
- @.gemini/blueprints/ftv-notification-list.md
- @.gemini/blueprints/ftv-notification-item.md
**3. Lista de Archivos Implementados (Frontend)**:
Ejemplo:
- src/components/notifications/NotificationBell.tsx
- src/components/notifications/NotificationBadge.tsx
- src/components/notifications/NotificationDropdown.tsx
- src/components/notifications/NotificationList.tsx
- src/components/notifications/NotificationItem.tsx
**4. Lista de Blueprints Backend (BSS)** (si aplica):
Ejemplo:
- @.gemini/blueprints/backend/bss-notification-service.md
- @.gemini/blueprints/backend/bss-notification-controller.md
**5. Lista de Archivos Implementados (Backend)** (si aplica):
Ejemplo:
- src/main/java/.../service/NotificationServiceImpl.java
- src/main/java/.../controller/NotificationController.java
[ESPERAR RESPUESTA]
```

---

#### Acción 0.3: Parsear Entrada Multi-Blueprint

```typescript
interface FeatureValidationInput {
 featureName: string; // Inferir del nombre común (ej: "Notifications")
 technicalDesign?: string; // Contenido del TD (opcional)

frontend: {
 blueprints: Blueprint[]; // FTVs parseadas
 codigo: CodigoFuente[]; // Archivos .tsx/.ts
 };

backend?: {
 blueprints: Blueprint[]; // BSS parseadas
 codigo: CodigoFuente[]; // Archivos .java
 };
}
function parsearInputFeature(input: string): FeatureValidationInput {
 const lines = input.split('\n');

const result = {
 featureName: inferirNombreFeature(input),
 frontend: { blueprints: [], codigo: [] },
 backend: { blueprints: [], codigo: [] }
 };

let currentSection = null;

lines.forEach(line => {
 if (line.includes('Technical Design:')) {
 const tdPath = extraerPath(line);
 result.technicalDesign = leerArchivo(tdPath);
 }

if (line.includes('Blueprints Frontend:')) {
 currentSection = 'ftv';
 } else if (line.includes('Archivos Implementados (Frontend):')) {
 currentSection = 'codigo-frontend';
 } else if (line.includes('Blueprints Backend:')) {
 currentSection = 'bss';
 } else if (line.includes('Archivos Implementados (Backend):')) {
 currentSection = 'codigo-backend';
 }

if (line.trim().startsWith('-') || line.trim().startsWith('•')) {
 const path = extraerPath(line);

switch(currentSection) {
 case 'ftv':
 result.frontend.blueprints.push(parsearBlueprint(leerArchivo(path)));
 break;
 case 'codigo-frontend':
 result.frontend.codigo.push({ path, contenido: leerArchivo(path) });
 break;
 case 'bss':
 result.backend.blueprints.push(parsearBlueprint(leerArchivo(path)));
 break;
 case 'codigo-backend':
 result.backend.codigo.push({ path, contenido: leerArchivo(path) });
 break;
 }
 }
 });

return result;
}
function inferirNombreFeature(input: string): string {
 // Buscar nombre común en los blueprints
 // Ej: ftv-notification-bell, ftv-notification-badge → "Notification"
 const blueprints = extraerNombresBlueprints(input);
 const palabrasComunes = encontrarPalabrasComunes(blueprints);
 return capitalize(palabrasComunes[0]);
}
```

---

### FASE 1: Validación Individual de Componentes

#### Acción 1.1: Validar Cada Par (Blueprint → Código)

```typescript
interface ComponentValidation {
 blueprintId: string;
 blueprintNombre: string;
 archivoImplementacion: string;
 scoreIndividual: number;
 resultados: Map<string, ValidacionResult>;
 divergencias: Divergencia[];
 estado: 'EXCELENTE' | 'BUENO' | 'ACEPTABLE' | 'INSUFICIENTE';
}
function validarComponentesIndividuales(
 input: FeatureValidationInput
): ComponentValidation[] {

const validaciones = [];

// Frontend
 input.frontend.blueprints.forEach(ftv => {
 const archivoCorrespondiente = buscarArchivoCorrespondiente(
 ftv,
 input.frontend.codigo
 );

if (!archivoCorrespondiente) {
 validaciones.push({
 blueprintId: ftv.id,
 blueprintNombre: ftv.nombreComponente,
 archivoImplementacion: null,
 scoreIndividual: 0,
 estado: 'INSUFICIENTE',
 divergencias: [{ 
tipo: 'ARCHIVO_FALTANTE',
 severidad: 'CRITICA',
 mensaje: `No se encontró implementación para ${ftv.nombreComponente}`
 }]
 });
 return;
 }

// Validar usando criterios existentes
 const validacion = validarComponenteIndividual(
 ftv,
 archivoCorrespondiente,
 CRITERIOS_VALIDACION_FRONTEND
 );

validaciones.push(validacion);
 });

// Backend (si aplica)
 if (input.backend) {
 input.backend.blueprints.forEach(bss => {
 const archivoCorrespondiente = buscarArchivoCorrespondiente(
 bss,
 input.backend.codigo
 );

if (!archivoCorrespondiente) {
 validaciones.push({
 blueprintId: bss.id,
 blueprintNombre: bss.nombreClase,
 archivoImplementacion: null,
 scoreIndividual: 0,
 estado: 'INSUFICIENTE',
 divergencias: [{ 
tipo: 'ARCHIVO_FALTANTE',
 severidad: 'CRITICA'
 }]
 });
 return;
 }

const validacion = validarComponenteIndividual(
 bss,
 archivoCorrespondiente,
 CRITERIOS_VALIDACION_BACKEND
 );

validaciones.push(validacion);
 });
 }

return validaciones;
}
function buscarArchivoCorrespondiente(
 blueprint: Blueprint,
 archivos: CodigoFuente[]
): CodigoFuente | null {

const nombreEsperado = blueprint.nombreComponente || blueprint.nombreClase;

// Buscar por nombre exacto
 let match = archivos.find(archivo => 
archivo.path.includes(nombreEsperado)
 );

if (match) return match;

// Buscar por nombre en kebab-case
 const nombreKebab = toKebabCase(nombreEsperado);
 match = archivos.find(archivo => 
archivo.path.includes(nombreKebab)
 );

return match;
}
```

---

### FASE 2: Validación de Integración Entre Componentes

**Esto es lo NUEVO y MUY importante para features completas**

#### Acción 2.1: Construir Grafo de Dependencias

```typescript
interface ComponentNode {
 nombre: string;
 tipo: 'Page' | 'Feature' | 'UI';
 archivo: string;
 dependeDe: string[]; // Componentes que importa/usa
 usadoPor: string[]; // Componentes que lo usan
}
function construirGrafoDependencias(
 validaciones: ComponentValidation[],
 codigo: CodigoFuente[]
): ComponentNode[] {

const grafo = validaciones.map(v => {
 const nodo: ComponentNode = {
 nombre: v.blueprintNombre,
 tipo: inferirTipoComponente(v.blueprintNombre),
 archivo: v.archivoImplementacion,
 dependeDe: [],
 usadoPor: []
 };

// Analizar imports del archivo
 const codigoArchivo = codigo.find(c => c.path === v.archivoImplementacion);
 if (codigoArchivo) {
 nodo.dependeDe = extraerImportsDeComponentes(
 codigoArchivo.contenido,
 validaciones.map(val => val.blueprintNombre)
 );
 }

return nodo;
 });

// Llenar usadoPor (reverse dependencies)
 grafo.forEach(nodo => {
 nodo.dependeDe.forEach(dep => {
 const nodoDep = grafo.find(n => n.nombre === dep);
 if (nodoDep) {
 nodoDep.usadoPor.push(nodo.nombre);
 }
 });
 });

return grafo;
}
function extraerImportsDeComponentes(
 codigo: string,
 componentesEsperados: string[]
): string[] {
 const imports = [];

// Buscar imports de React
 const importRegex = /import\s+{?\s*(\w+)\s*}?\s+from\s+['"]([^'"]+)['"]/g;
 let match;

while ((match = importRegex.exec(codigo)) !== null) {
 const componenteImportado = match[1];

if (componentesEsperados.includes(componenteImportado)) {
 imports.push(componenteImportado);
 }
 }

return imports;
}
```

---

#### Acción 2.2: Validar Coherencia de Props Entre Componentes

**Validación crítica**: Si NotificationBell usa NotificationBadge, las props deben coincidir.

```typescript
interface IntegrationIssue {
 tipo: 'PROP_MISMATCH' | 'CALLBACK_SIGNATURE' | 'TYPE_INCONSISTENCY';
 severidad: 'CRITICA' | 'ALTA' | 'MEDIA';
 componentePadre: string;
 componenteHijo: string;
 detalles: string;
 ejemplo?: string;
}
function validarCoherenciaProps(
 grafo: ComponentNode[],
 validaciones: ComponentValidation[],
 codigo: CodigoFuente[]
): IntegrationIssue[] {

const issues = [];

grafo.forEach(nodo => {
 if (nodo.dependeDe.length === 0) return; // No tiene hijos

const codigoPadre = codigo.find(c => c.path === nodo.archivo);
 if (!codigoPadre) return;

nodo.dependeDe.forEach(nombreHijo => {
 const nodoHijo = grafo.find(n => n.nombre === nombreHijo);
 if (!nodoHijo) return;

// Buscar cómo se usa el componente hijo en el padre
 const usagePattern = new RegExp(
 `<${nombreHijo}[^>]*>`, 
'g'
 );

const usages = codigoPadre.contenido.match(usagePattern);
 if (!usages) return;

usages.forEach(usage => {
 // Extraer props pasadas
 const propsPasadas = extraerPropsDeUsage(usage);

// Obtener props esperadas del blueprint del hijo
 const blueprintHijo = validaciones.find(v => v.blueprintNombre === nombreHijo);
 if (!blueprintHijo) return;

const propsEsperadas = blueprintHijo.propsRequeridas;

// Validar que todas las props requeridas se pasen
 propsEsperadas.forEach(propEsperada => {
 const propPasada = propsPasadas.find(p => p.nombre === propEsperada.nombre);

if (!propPasada) {
 issues.push({
 tipo: 'PROP_MISMATCH',
 severidad: 'CRITICA',
 componentePadre: nodo.nombre,
 componenteHijo: nombreHijo,
 detalles: `Prop requerida "${propEsperada.nombre}" no se está pasando`,
 ejemplo: `<${nombreHijo} ${propEsperada.nombre}={...} />`
 });
 } else if (propPasada.tipo !== propEsperada.tipo) {
 issues.push({
 tipo: 'TYPE_INCONSISTENCY',
 severidad: 'ALTA',
 componentePadre: nodo.nombre,
 componenteHijo: nombreHijo,
 detalles: `Prop "${propEsperada.nombre}" tiene tipo incorrecto. Esperado: ${propEsperada.tipo}, Encontrado: ${propPasada.tipo}`
 });
 }
 });
 });
 });
 });

return issues;
}
function extraerPropsDeUsage(usage: string): Prop[] {
 // Extraer atributos del JSX
 // Ej: <NotificationBadge count={5} variant="primary" />
 // → [{ nombre: 'count', tipo: inferir }, { nombre: 'variant', tipo: inferir }]

const propRegex = /(\w+)=(?:{([^}]+)}|"([^"]+)")/g;
 const props = [];
 let match;

while ((match = propRegex.exec(usage)) !== null) {
 props.push({
 nombre: match[1],
 valor: match[2] || match[3],
 tipo: inferirTipoDePropValue(match[2] || match[3])
 });
 }

return props;
}
```

---

#### Acción 2.3: Validar Coherencia de Tipos Compartidos

```typescript
function validarTiposCompartidos(
 codigo: CodigoFuente[]
): IntegrationIssue[] {

const issues = [];

// Buscar definiciones de tipos/interfaces compartidas
 const tiposDefinidos = new Map<string, TypeDefinition[]>();

codigo.forEach(archivo => {
 const tipos = extraerTiposTypeScript(archivo.contenido);

tipos.forEach(tipo => {
 if (!tiposDefinidos.has(tipo.nombre)) {
 tiposDefinidos.set(tipo.nombre, []);
 }

tiposDefinidos.get(tipo.nombre).push({
 definicion: tipo,
 archivo: archivo.path
 });
 });
 });

// Detectar tipos duplicados con definiciones diferentes
 tiposDefinidos.forEach((definiciones, nombreTipo) => {
 if (definiciones.length > 1) {
 // Comparar si las definiciones son iguales
 const primeraDefinicion = definiciones[0].definicion;

for (let i = 1; i < definiciones.length; i++) {
 const otraDefinicion = definiciones[i].definicion;

if (!sonTiposEquivalentes(primeraDefinicion, otraDefinicion)) {
 issues.push({
 tipo: 'TYPE_INCONSISTENCY',
 severidad: 'CRITICA',
 componentePadre: definiciones[0].archivo,
 componenteHijo: definiciones[i].archivo,
 detalles: `Tipo "${nombreTipo}" está definido de forma diferente en múltiples archivos`,
 ejemplo: `
 // ${definiciones[0].archivo}
 ${primeraDefinicion.codigo}

// ${definiciones[i].archivo}
 ${otraDefinicion.codigo}

💡 Solución: Mover a src/types/shared.ts
 `
 });
 }
 }
 }
 });

return issues;
}
```

---

#### Acción 2.4: Validar Cobertura End-to-End de la Feature

```typescript
function validarCoberturaEndToEnd(
 technicalDesign: string,
 validaciones: ComponentValidation[],
 grafo: ComponentNode[]
): CoverageReport {

const report = {
 userStories: [],
 flowsCovered: [],
 flowsMissing: [],
 coveragePercentage: 0
 };

if (!technicalDesign) {
 return {
 ...report,
 warning: 'No se proporcionó Technical Design, validación E2E limitada'
 };
 }

// Extraer User Stories del TD
 const stories = extraerUserStories(technicalDesign);

stories.forEach(story => {
 // Analizar si el flujo completo está implementado
 const componentesNecesarios = inferirComponentesDeStory(story);
 const componentesImplementados = validaciones
 .filter(v => v.scoreIndividual >= 70) // Solo considerar bien implementados
 .map(v => v.blueprintNombre);

const todosCubiertos = componentesNecesarios.every(comp => 
componentesImplementados.includes(comp)
 );

if (todosCubiertos) {
 report.flowsCovered.push({
 story: story.titulo,
 componentes: componentesNecesarios,
 estado: 'COMPLETO'
 });
 } else {
 const faltantes = componentesNecesarios.filter(comp => 
!componentesImplementados.includes(comp)
 );

report.flowsMissing.push({
 story: story.titulo,
 componentesFaltantes: faltantes,
 estado: 'INCOMPLETO'
 });
 }
 });

report.coveragePercentage = Math.round(
 (report.flowsCovered.length / stories.length) * 100
 );

return report;
}
function inferirComponentesDeStory(story: UserStory): string[] {
 // Heurística: buscar nombres de componentes mencionados en el criterio de aceptación
 const criterios = story.criteriosAceptacion.join(' ');
 const palabrasClave = [
 'notification', 'bell', 'badge', 'dropdown', 'list', 'item',
 'modal', 'button', 'form', 'table', 'card', 'panel'
 ];

const componentes = [];

palabrasClave.forEach(keyword => {
 if (criterios.toLowerCase().includes(keyword)) {
 // Capitalizar primera letra
 componentes.push(capitalize(keyword));
 }
 });

return componentes;
}
```

---

### FASE 3: Cálculo de Score Global de Feature

```typescript
function calcularScoreFeature(
 validacionesIndividuales: ComponentValidation[],
 issuesIntegracion: IntegrationIssue[],
 coverageE2E: CoverageReport
): FeatureScore {

// 1. Score promedio de componentes individuales (60% del total)
 const scoreIndividualPromedio = validacionesIndividuales.reduce(
 (sum, v) => sum + v.scoreIndividual, 0
 ) / validacionesIndividuales.length;

// 2. Penalización por issues de integración (20% del total)
 let scoreIntegracion = 100;

issuesIntegracion.forEach(issue => {
 if (issue.severidad === 'CRITICA') {
 scoreIntegracion -= 15;
 } else if (issue.severidad === 'ALTA') {
 scoreIntegracion -= 10;
 } else {
 scoreIntegracion -= 5;
 }
 });

scoreIntegracion = Math.max(0, scoreIntegracion);

// 3. Coverage end-to-end (20% del total)
 const scoreE2E = coverageE2E.coveragePercentage;

// Score final ponderado
 const scoreFinal = Math.round(
 (scoreIndividualPromedio * 0.6) +
 (scoreIntegracion * 0.2) +
 (scoreE2E * 0.2)
 );

return {
 scoreFinal: scoreFinal,
 desglose: {
 individual: {
 score: Math.round(scoreIndividualPromedio),
 peso: 60,
 aporte: Math.round(scoreIndividualPromedio * 0.6)
 },
 integracion: {
 score: scoreIntegracion,
 peso: 20,
 aporte: Math.round(scoreIntegracion * 0.2),
 issuesDetectados: issuesIntegracion.length
 },
 endToEnd: {
 score: scoreE2E,
 peso: 20,
 aporte: Math.round(scoreE2E * 0.2),
 flujosCompletos: coverageE2E.flowsCovered.length,
 flujosFaltantes: coverageE2E.flowsMissing.length
 }
 },
 calificacion: obtenerCalificacion(scoreFinal),
 estadoFeature: clasificarEstadoFeature(scoreFinal, issuesIntegracion)
 };
}
function clasificarEstadoFeature(
 score: number,
 issues: IntegrationIssue[]
): string {

const tieneCriticos = issues.some(i => i.severidad === 'CRITICA');

if (tieneCriticos) {
 return '🚨 BLOQUEADO (Issues críticos de integración)';
 }

if (score >= 90) return '✅ LISTO PARA PRODUCCIÓN';
 if (score >= 80) return '✔️ LISTO PARA QA';
 if (score >= 70) return '⚠️ REQUIERE REFINAMIENTO';
 if (score >= 60) return '⚠️ REQUIERE TRABAJO SIGNIFICATIVO';
 return '❌ NO LISTO (Requiere re-implementación)';
}
```

---

### FASE 4: Generación de Reporte Consolidado

```markdown
# 📊 Reporte de Validación de Feature: [Nombre de Feature]
**Technical Design**: [TD-XXX-nombre.md] 
**Fecha de Validación**: [ISO 8601] 
**Blueprints Analizados**: [X] FTVs + [Y] BSS 
**Archivos Implementados**: [Z] archivos 
**Validado por**: Blueprint Multi-Feature Validator v2
---
## 🎯 Score Global de Feature
```

┌────────────────────────────────────────────────┐
│ │
│ SCORE FINAL DE FEATURE: [XX]% │
│ │
│ Estado: [ESTADO] │
│ Calificación: [A/B/C/D/F] │
│ │
└────────────────────────────────────────────────┘

```
**Desglose de Score**:
| Dimensión | Score | Peso | Aporte | Estado |
|-----------|-------|------|--------|--------|
| **Componentes Individuales** | [XX]% | 60% | [XX] pts | [Estado] |
| **Integración Entre Componentes** | [XX]% | 20% | [XX] pts | [Estado] |
| **Cobertura End-to-End** | [XX]% | 20% | [XX] pts | [Estado] |
| **TOTAL** | **[XX]%** | **100%** | **[XX] pts** | **[Estado]** |
---
## 📦 Validación de Componentes Individuales
### Resumen por Componente
| Componente | Blueprint | Implementación | Score | Estado |
|------------|-----------|----------------|-------|--------|
| NotificationBell | ftv-notification-bell.md | NotificationBell.tsx | 85% | ✔️ Bueno |
| NotificationBadge | ftv-notification-badge.md | NotificationBadge.tsx | 92% | ✅ Excelente |
| NotificationDropdown | ftv-notification-dropdown.md | NotificationDropdown.tsx | 78% | ✔️ Bueno |
| NotificationList | ftv-notification-list.md | NotificationList.tsx | 71% | ⚠️ Aceptable |
| NotificationItem | ftv-notification-item.md | NotificationItem.tsx | 88% | ✅ Excelente |
| NotificationService | bss-notification-service.md | NotificationServiceImpl.java | 94% | ✅ Excelente |
**Score Promedio**: 84.7% (Bueno)
### Componentes con Score Bajo (< 75%)
#### ⚠️ NotificationList (71%)
**Principales Issues**:
- ❌ Sin tests implementados (0 de 5 esperados)
- ⚠️ Falta prop opcional `maxItems`
- ⚠️ No implementa virtualización (especificado en FTV para listas > 100 items)
**Recomendación**: Priorizar implementación de tests y virtualización.
---
## 🔗 Validación de Integración Entre Componentes
### Grafo de Dependencias
```

NotificationCenterPage (Page)
 ↓
NotificationBell (Feature)
 ├─ NotificationBadge (UI) ✅
 └─ NotificationDropdown (Feature)
 └─ NotificationList (Feature)
 └─ NotificationItem (UI) ✅

```
**Componentes Huérfanos**: Ninguno ✅ 
**Dependencias Circulares**: Ninguna ✅
---
### Issues de Integración Detectados
#### 🚨 CRÍTICO #1: Prop Mismatch en NotificationBell → NotificationBadge
**Componente Padre**: `NotificationBell.tsx` 
**Componente Hijo**: `NotificationBadge.tsx`
**Problema**:
```tsx
// Blueprint NotificationBadge especifica:
interface NotificationBadgeProps {
 count: number; // ← Requerido
 maxDisplay?: number; // Default: 99
 variant?: 'default' | 'primary';
}
// Pero en NotificationBell.tsx se está usando:
<NotificationBadge variant="primary" />
// ^^^^^^^^^^^^^^^^
// ❌ Falta prop requerida "count"
```

**Impacto**: El componente Badge no puede mostrar el contador de notificaciones.
**Solución**:

```tsx
// NotificationBell.tsx - línea 45
<NotificationBadge 
count={unreadCount} // ← Agregar
 variant="primary" 
/>
```

---

#### ⚠️ ALTO #2: Type Inconsistency - Notification Interface

**Archivos Afectados**:

- `src/types/notification.ts` (define `Notification`)
- `src/components/notifications/NotificationItem.tsx` (redefine `Notification`)
  **Problema**:
  
  ```typescript
  // src/types/notification.ts
  interface Notification {
  id: string;
  type: 'ETL_COMPLETED' | 'ETL_FAILED';
  message: string;
  createdAt: string; // ← ISO 8601
  }
  // src/components/notifications/NotificationItem.tsx
  interface Notification { // ← Nombre duplicado
  id: string;
  type: string; // ← Tipo más permisivo
  message: string;
  timestamp: number; // ← Nombre diferente + tipo diferente
  }
  ```
  
  **Impacto**: Confusión de tipos, posible bug en runtime si se mezclan.
  **Solución**:
  
  ```typescript
  // NotificationItem.tsx - eliminar redefinición
  import { Notification } from '@/types/notification';
  // O renombrar si es intencionalmente diferente:
  interface NotificationItemProps {
  notification: Notification; // Usar el tipo compartido
  onRead: (id: string) => void;
  }
  ```

---

### ✅ Integraciones Validadas Correctamente

- NotificationBell importa y usa NotificationDropdown con todas las props requeridas ✅
- NotificationList pasa correctamente `notification` prop a NotificationItem ✅
- Callbacks (`onRead`, `onDismiss`) tienen signatures consistentes ✅

---

## 🎬 Validación de Cobertura End-to-End

### Flujos de Usuario (desde Technical Design)

#### ✅ Flujo 1: Recibir Notificación de ETL Exitoso (COMPLETO)

**User Story**: "Como administrador, quiero recibir notificación cuando un archivo ETL termina exitosamente"
**Componentes Necesarios**:

- NotificationService (Backend) → **94% ✅**
- NotificationBell (Frontend) → **85% ✅**
- NotificationBadge (Frontend) → **92% ✅**
- NotificationItem (Frontend) → **88% ✅**
  **Estado**: ✅ COMPLETO (todos los componentes implementados con score > 70%)

---

#### ⚠️ Flujo 2: Ver Historial de Notificaciones (INCOMPLETO)

**User Story**: "Como usuario, quiero ver el historial completo de mis notificaciones"
**Componentes Necesarios**:

- NotificationCenterPage (Frontend) → **❌ NO IMPLEMENTADO**
- NotificationList (Frontend) → **71% ✅**
- Backend endpoint `GET /api/v1/notifications` → **94% ✅**
  **Estado**: ⚠️ INCOMPLETO (falta `NotificationCenterPage`)
  **Impacto**: El usuario puede recibir notificaciones pero no puede ver el historial completo.

---

#### ❌ Flujo 3: Marcar Todas como Leídas (NO INICIADO)

**Componentes Necesarios**:

- NotificationDropdown con botón "Marcar todas" → **❌ NO IMPLEMENTADO**
- Backend endpoint `POST /api/v1/notifications/mark-all-read` → **❌ NO IMPLEMENTADO**
  **Estado**: ❌ NO INICIADO

---

### Resumen de Cobertura

```
Flujos Completos: 1 de 3 (33%)
Flujos Parciales: 1 de 3 (33%)
Flujos No Iniciados: 1 de 3 (33%)
SCORE E2E: 33% ⚠️
```

---

## 📋 Plan de Acción Priorizado

### 🚨 Prioridad CRÍTICA (Bloquea Merge)

1. **Corregir Prop Mismatch en NotificationBadge**
   - Archivo: `src/components/notifications/NotificationBell.tsx` línea 45
   - Acción: Agregar prop `count={unreadCount}`
   - Tiempo estimado: 5 minutos
   - Responsable: [Asignar]
2. **Resolver Duplicación de Tipo `Notification`**
   - Archivos: `NotificationItem.tsx`
   - Acción: Usar tipo compartido de `@/types/notification`
   - Tiempo estimado: 10 minutos
   - Responsable: [Asignar]

---

### ⚠️ Prioridad ALTA (Completar antes de QA)

3. **Implementar Tests de NotificationList**
   - Archivo: Crear `NotificationList.test.tsx`
   - Acción: Implementar 5 casos de test del blueprint
   - Tiempo estimado: 2 horas
   - Responsable: [Asignar]
4. **Implementar NotificationCenterPage**
   - Blueprint: `ftv-notification-center-page.md`
   - Acción: Crear componente completo
   - Tiempo estimado: 4 horas
   - Responsable: [Asignar]

---

### 📌 Prioridad MEDIA (Nice-to-have para este Sprint)

5. **Agregar Virtualización a NotificationList**
   - Librería: `react-window`
   - Tiempo estimado: 3 horas
6. **Implementar Funcionalidad "Marcar Todas como Leídas"**
   - Frontend + Backend
   - Tiempo estimado: 5 horas

---

## 📊 Métricas Consolidadas de Feature

| Métrica                            | Valor                         |
| ---------------------------------- | ----------------------------- |
| **Blueprints Analizados**          | 6 (5 FTVs + 1 BSS)            |
| **Archivos Implementados**         | 6 de 6 esperados (100%)       |
| **Líneas de Código Total**         | ~1,200                        |
| **Tests Implementados**            | 18 de 30 esperados (60%)      |
| **Issues Críticos**                | 2                             |
| **Issues Alta Prioridad**          | 2                             |
| **Coverage E2E**                   | 33% (1 de 3 flujos completos) |
| **Tiempo Estimado para Completar** | ~11 horas                     |

---

## 🎓 Recomendaciones Arquitectónicas

### 1. Centralizar Tipos Compartidos

Crear archivo `src/types/notifications/index.ts` con todos los tipos relacionados:

```typescript
export type { Notification, NotificationType };
export type { NotificationFilter, NotificationSort };
```

### 2. Patron de Props Consistente

Estandarizar nombres de callbacks:

```typescript
// ✅ USAR:
on[Action]: (params) => void
// Ejemplos: onRead, onDismiss, onMarkAllRead
// ❌ EVITAR:
handle[Action], [action]Callback
```

### 3. Estrategia de Testing

- Componentes UI (Badge, Item): Tests unitarios de rendering + accesibilidad
- Componentes Feature (Bell, List): Tests de integración con MSW
- Pages: Tests E2E con Playwright

---

## 💾 Archivos de Evidencia

Este reporte se basa en el análisis de los siguientes archivos:
**Blueprints**:

- `@.gemini/blueprints/ftv-notification-bell.md`
- `@.gemini/blueprints/ftv-notification-badge.md`
- `@.gemini/blueprints/ftv-notification-dropdown.md`
- `@.gemini/blueprints/ftv-notification-list.md`
- `@.gemini/blueprints/ftv-notification-item.md`
- `@.gemini/blueprints/backend/bss-notification-service.md`
  **Implementaciones**:
- `src/components/notifications/NotificationBell.tsx` (148 líneas)
- `src/components/notifications/NotificationBadge.tsx` (67 líneas)
- `src/components/notifications/NotificationDropdown.tsx` (201 líneas)
- `src/components/notifications/NotificationList.tsx` (189 líneas)
- `src/components/notifications/NotificationItem.tsx` (112 líneas)
- `src/main/java/.../service/NotificationServiceImpl.java` (342 líneas)

---

**Reporte Generado por**: Blueprint Multi-Feature Validator v2 
**Tiempo de Análisis**: 12.3 segundos 
**Checksum**: `a3f5e8d2c1b9...`

```
---
## 🎯 Resumen: Qué Ganas con el Modo Feature
| Validación | Modo Single | Modo Feature |
|------------|-------------|--------------|
| **Props de componente** | ✅ | ✅ |
| **Estado interno** | ✅ | ✅ |
| **Tests individuales** | ✅ | ✅ |
| **Integración entre componentes** | ❌ | ✅ **NUEVO** |
| **Tipos compartidos duplicados** | ❌ | ✅ **NUEVO** |
| **Grafo de dependencias** | ❌ | ✅ **NUEVO** |
| **Cobertura E2E de user stories** | ❌ | ✅ **NUEVO** |
| **Score consolidado de feature** | ❌ | ✅ **NUEVO** |
| **Plan de acción priorizado** | Básico | ✅ **Avanzado** |
---
## 📝 Ejemplo de Uso del Modo Feature
```bash
$ gemini-cli < @.gemini/prompts/09-validate-blueprint-implementation.md
[IA] ¿Qué modo deseas usar?
> B (Feature)
[IA] Proporciona los datos de la feature:
Technical Design: @.gemini/sprints/technical-designs/TD-002-real-time-notifications.md
Blueprints Frontend:
- @.gemini/blueprints/ftv-notification-bell.md
- @.gemini/blueprints/ftv-notification-badge.md
- @.gemini/blueprints/ftv-notification-dropdown.md
- @.gemini/blueprints/ftv-notification-list.md
- @.gemini/blueprints/ftv-notification-item.md
Archivos Implementados (Frontend):
- src/components/notifications/NotificationBell.tsx
- src/components/notifications/NotificationBadge.tsx
- src/components/notifications/NotificationDropdown.tsx
- src/components/notifications/NotificationList.tsx
- src/components/notifications/NotificationItem.tsx
Blueprints Backend:
- @.gemini/blueprints/backend/bss-notification-service.md
Archivos Implementados (Backend):
- src/main/java/com/cambiaso/ioc/service/NotificationServiceImpl.java
[IA] ... analizando 6 blueprints y 6 archivos ...
[IA] ... construyendo grafo de dependencias ...
[IA] ... validando integración ...
[IA] ... calculando cobertura E2E ...
✅ Reporte generado:
@.gemini/reports/feature-validation-notifications-2024-01-15.md
SCORE FINAL DE FEATURE: 68%
Estado: ⚠️ REQUIERE REFINAMIENTO
Issues Críticos: 2
- Prop mismatch en NotificationBell → NotificationBadge
- Tipo Notification duplicado
Cobertura E2E: 33% (1 de 3 flujos completos)
🔧 Tiempo estimado para resolver issues críticos: 15 minutos
```

---


