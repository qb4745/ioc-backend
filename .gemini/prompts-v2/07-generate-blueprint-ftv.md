# 📄 PROMPT 7: Blueprint/FTV Generator (Parte 2)

## PARTE 2: El Prompt Generador

**Archivo**: `@.gemini/prompts/07-generate-blueprint-ftv.md`

```markdown
# GENERADOR DE BLUEPRINTS/FTVs (v1)

## 1. CONFIGURACIÓN

**Propósito**: Convertir un Technical Design aprobado en Fichas Técnicas de Vista (FTVs) detalladas para cada componente frontend, listas para implementación.

**Input**: 
- Technical Design específico (`TD-XXX-nombre.md`)
- Project Summary (`@.gemini/project-summary.md`)

**Output**: 
- Múltiples FTVs: `@.gemini/blueprints/ftv-[componente].md` (una por componente)
- Índice de blueprints generados

**Audiencia**: Desarrolladores Frontend, QA, UX Designer

---

## 2. MANDATO OPERATIVO (PARA LA IA)

**Tu Rol**: Frontend Architect con experiencia en React, arquitectura de componentes, accesibilidad y mejores prácticas de desarrollo UI.

**Tu Misión**: 
1. Leer y comprender el Technical Design completamente
2. Identificar TODOS los componentes frontend necesarios
3. Para cada componente, generar una FTV completa con:
   - Especificación visual detallada
   - Props, estado y lógica
   - Contratos de API consumidos
   - Manejo de errores y estados
   - Tests y accesibilidad
4. Asegurar consistencia entre FTVs (naming, patrones, estilos)
5. Generar código de ejemplo ejecutable
6. Proveer checklist de implementación

---

## 3. PROTOCOLO DE EJECUCIÓN

### FASE 1: Carga y Análisis del Technical Design

#### Acción 1.1: Solicitar Technical Design

```markdown
🔍 SOLICITUD DE TECHNICAL DESIGN

Para generar Blueprints/FTVs, necesito el Technical Design a implementar.

Por favor proporciona:

**Opción A**: ID del Technical Design
Ejemplo: TD-005

**Opción B**: Ruta completa del archivo
Ejemplo: @.gemini/sprints/technical-designs/TD-005-real-time-notifications.md

**Opción C**: Pega el contenido del Technical Design aquí

[ESPERAR RESPUESTA]
```

**Si el usuario proporciona solo un ID**:

```bash
# Buscar el archivo automáticamente
RUTA = "@.gemini/sprints/technical-designs/"
PATRON = "TD-{ID}-*.md"

ARCHIVO_ENCONTRADO = buscar(RUTA + PATRON)

if not ARCHIVO_ENCONTRADO:
    ERROR("No se encontró Technical Design con ID: {ID}")
    LISTAR_DISPONIBLES()
    DETENER()
```

---

#### Acción 1.2: Parsear Technical Design

```typescript
// Extraer información crítica del TD

interface TechnicalDesignData {
  id: string;                    // TD-005
  nombre: string;                // Real-Time Notifications
  featurePlanId: string;         // FP-005
  sprint: string;                // Sprint 2
  
  // Arquitectura
  arquitectura: {
    diagrama: string;
    componentesFrontend: string[];  // De diagrama y sección 2.3
    componentesBackend: string[];
    flujosDatos: DataFlow[];
  };
  
  // Modelo de datos
  modelo: {
    entidades: Entity[];
    tiposTypeScript: string;     // Código TS compartido
  };
  
  // Contratos de API
  contratos: {
    endpoints: Endpoint[];       // REST endpoints
    websockets?: WebSocketSpec[]; // WebSocket si aplica
  };
  
  // Testing
  testing: {
    planTesting: TestPlan;
  };
  
  // Otros
  decisiones: Decision[];
  optimizaciones: Optimization[];
  seguridad: SecurityConsideration[];
}
```

**Validar Completitud del TD**:

```typescript
const validaciones = [
  { 
    campo: 'arquitectura.componentesFrontend', 
    minimo: 1, 
    mensaje: "TD debe especificar al menos 1 componente frontend" 
  },
  { 
    campo: 'contratos.endpoints', 
    minimo: 1, 
    mensaje: "TD debe tener al menos 1 endpoint de API" 
  },
  { 
    campo: 'modelo.tiposTypeScript', 
    requerido: true, 
    mensaje: "TD debe tener tipos TypeScript definidos" 
  }
];

validaciones.forEach(v => {
  if (!cumple(tdData, v)) {
    ADVERTENCIA(v.mensaje);
    // Continuar pero marcar como [INFERIR]
  }
});
```

---

#### Acción 1.3: Cargar Contexto Adicional

```bash
# Cargar Project Summary
PROJECT_SUMMARY = leer("@.gemini/project-summary.md")

# Extraer convenciones del proyecto
CONVENCIONES = {
  framework: extraer(PROJECT_SUMMARY, 'Frontend Framework'),
  stateManagement: extraer(PROJECT_SUMMARY, 'State Management'),
  uiLibrary: extraer(PROJECT_SUMMARY, 'UI Framework'),
  testingFramework: extraer(PROJECT_SUMMARY, 'Testing Framework'),
  estructuraDirectorios: extraer(PROJECT_SUMMARY, 'Estructura Frontend')
}

# Cargar Feature Plan para contexto de negocio
if (tdData.featurePlanId) {
  FEATURE_PLAN = leer(`@.gemini/sprints/feature-plans/${tdData.featurePlanId}*.md`)
  HISTORIAS_USUARIO = extraer(FEATURE_PLAN, 'Historias de Usuario')
  CRITERIOS_ACEPTACION = extraer(FEATURE_PLAN, 'Criterios de Aceptación')
}
```

---

### FASE 2: Identificación de Componentes

#### Acción 2.1: Extraer Componentes del TD

```typescript
function identificarComponentes(td: TechnicalDesignData): Component[] {
  const componentes = [];
  
  // FUENTE 1: Diagrama de arquitectura
  const delDiagrama = parsearDiagrama(td.arquitectura.diagrama);
  componentes.push(...delDiagrama);
  
  // FUENTE 2: Sección 2.3 (Componentes y Responsabilidades)
  const deTabla = parsearTablaComponentes(td.arquitectura.componentesFrontend);
  componentes.push(...deTabla);
  
  // FUENTE 3: Nombres mencionados en flujos de datos
  const deFlujos = extraerDeFlujosLiteral(td.arquitectura.flujosDatos);
  componentes.push(...deFlujos);
  
  // Deduplicar y clasificar
  return deduplicarYClasificar(componentes);
}
```

**Ejemplo de parsing de diagrama**:

```typescript
// Si el diagrama tiene:
// <NotificationBell>
//   ├─ <NotificationBadge>
//   └─ <NotificationDropdown>

const componentes = [
  { nombre: 'NotificationBell', tipo: 'Feature Component', padre: null },
  { nombre: 'NotificationBadge', tipo: 'UI Component', padre: 'NotificationBell' },
  { nombre: 'NotificationDropdown', tipo: 'Feature Component', padre: 'NotificationBell' }
];
```

---

#### Acción 2.2: Clasificar Componentes

```typescript
interface Component {
  nombre: string;               // NotificationBell
  nombreArchivo: string;        // notification-bell (kebab-case)
  tipo: ComponentType;          // Page | Layout | Feature | UI
  padre: string | null;         // NotificationCenter (si es hijo)
  hijos: string[];              // [NotificationBadge, NotificationDropdown]
  responsabilidad: string;      // Descripción de qué hace
  ubicacion: string;            // src/components/notifications/
}

enum ComponentType {
  PAGE = 'Page',                // Componente de ruta completa
  LAYOUT = 'Layout',            // Wrapper de páginas
  FEATURE = 'Feature Component', // Funcionalidad específica
  UI = 'UI Component'           // Reutilizable, presentacional
}

function clasificarComponente(comp: Component, td: TechnicalDesignData): ComponentType {
  // Es Page si está en rutas
  if (tieneRutaAsociada(comp, td)) {
    return ComponentType.PAGE;
  }
  
  // Es Layout si envuelve múltiples páginas
  if (comp.nombre.includes('Layout') || comp.hijos.length > 3) {
    return ComponentType.LAYOUT;
  }
  
  // Es UI Component si es puramente presentacional
  if (esPresentacional(comp)) {
    return ComponentType.UI;
  }
  
  // Default: Feature Component
  return ComponentType.FEATURE;
}

function esPresentacional(comp: Component): boolean {
  // UI Components típicamente:
  // - Nombre genérico (Button, Card, Badge)
  // - Sin lógica de negocio (solo props → render)
  // - Reutilizable en múltiples contextos
  const nombresUI = ['Button', 'Badge', 'Icon', 'Card', 'Avatar', 'Spinner'];
  return nombresUI.some(n => comp.nombre.includes(n));
}
```

---

#### Acción 2.3: Determinar Orden de Generación

```typescript
function ordenarComponentes(componentes: Component[]): Component[] {
  // Generar en orden bottom-up (hijos antes que padres)
  // para que las referencias estén disponibles
  
  const ordenados = [];
  const procesados = new Set<string>();
  
  const procesar = (comp: Component) => {
    if (procesados.has(comp.nombre)) return;
    
    // Primero procesar hijos
    comp.hijos.forEach(nombreHijo => {
      const hijo = componentes.find(c => c.nombre === nombreHijo);
      if (hijo) procesar(hijo);
    });
    
    // Luego este componente
    ordenados.push(comp);
    procesados.add(comp.nombre);
  };
  
  // Procesar todos los componentes raíz (sin padre)
  componentes
    .filter(c => !c.padre)
    .forEach(procesar);
  
  return ordenados;
}
```

---

### FASE 3: Generación de FTVs

#### Acción 3.1: Para Cada Componente, Generar FTV

```typescript
for (const componente of componentesOrdenados) {
  console.log(`📝 Generando FTV para: ${componente.nombre}`);
  
  const ftv = await generarFTV(componente, td, projectSummary);
  
  const nombreArchivo = `ftv-${componente.nombreArchivo}.md`;
  const ruta = `@.gemini/blueprints/${nombreArchivo}`;
  
  escribirArchivo(ruta, ftv);
  
  console.log(`✅ Generado: ${ruta}`);
}
```

---

#### Acción 3.2: Generador de FTV (Función Principal)

```typescript
async function generarFTV(
  comp: Component, 
  td: TechnicalDesignData,
  ctx: ProjectContext
): Promise<string> {
  
  // Cargar plantilla
  const plantilla = leerArchivo('@.gemini/templates/ftv-template.md');
  
  // Generar ID (incremental)
  const ftvId = generarIdFTV();
  
  // Generar cada sección
  const datos = {
    // METADATA
    id: ftvId,
    tdId: td.id,
    nombreComponente: comp.nombre,
    tipo: comp.tipo,
    ruta: inferirRuta(comp, td),
    sprint: td.sprint,
    fechaCreacion: hoy(),
    
    // SECCIÓN 1: Propósito
    proposito: generarProposito(comp, td),
    casoUso: generarCasoUso(comp, td),
    ubicacionApp: generarUbicacionApp(comp, td),
    
    // SECCIÓN 2: Especificación Visual
    wireframe: generarWireframe(comp, td),
    estadosVisuales: generarEstadosVisuales(comp, td),
    responsive: generarResponsive(comp, td),
    
    // SECCIÓN 3: Jerarquía de Componentes
    arbolComponentes: generarArbolComponentes(comp, td),
    componentesHijos: listarComponentesHijos(comp),
    componentesReutilizados: identificarComponentesReutilizados(comp, ctx),
    
    // SECCIÓN 4: Props y API
    propsInterface: generarPropsInterface(comp, td),
    valoresPorDefecto: generarDefaultProps(comp, td),
    ejemplosUso: generarEjemplosUso(comp, td),
    
    // SECCIÓN 5: Estado Interno
    stateManagement: generarStateManagement(comp, td, ctx),
    variablesEstado: generarVariablesEstado(comp, td),
    maquinaEstados: generarMaquinaEstados(comp, td),
    
    // SECCIÓN 6: Lógica de Negocio
    reglasNegocio: extraerReglasNegocio(comp, td),
    validaciones: generarValidaciones(comp, td),
    transformaciones: generarTransformaciones(comp, td),
    
    // SECCIÓN 7: Interacciones de Usuario
    eventosUsuario: generarEventosUsuario(comp, td),
    flujos: generarFlujosInteraccion(comp, td),
    atajosTeclado: generarAtajosTeclado(comp, td),
    
    // SECCIÓN 8: Integración con Backend
    endpointsConsumidos: extraerEndpoints(comp, td),
    estrategiaCarga: generarEstrategiaCarga(comp, td),
    optimisticUpdates: generarOptimisticUpdates(comp, td),
    
    // SECCIÓN 9: Manejo de Errores
    tiposErrores: generarTiposErrores(comp, td),
    componentesError: generarComponentesError(comp, td),
    estrategiaRetry: generarEstrategiaRetry(comp, td),
    
    // SECCIÓN 10: Performance
    optimizaciones: generarOptimizaciones(comp, td),
    metricas: generarMetricasPerformance(comp, td),
    
    // SECCIÓN 11: Accesibilidad
    checklistA11y: generarChecklistA11y(comp, td),
    ariaAttributes: generarAriaAttributes(comp, td),
    navegacionTeclado: generarNavegacionTeclado(comp, td),
    
    // SECCIÓN 12: Testing
    testPlan: generarTestPlan(comp, td),
    casosPrueba: generarCasosPrueba(comp, td),
    coberturaObjetivo: '80%',
    
    // SECCIÓN 13: Dependencias
    libreriasExternas: identificarLibrerias(comp, td, ctx),
    hooksPersonalizados: identificarHooks(comp, td, ctx),
    servicios: identificarServicios(comp, td, ctx),
    
    // SECCIÓN 14: Feature Flags
    variablesEntorno: generarVariablesEntorno(comp, td),
    featureFlags: generarFeatureFlags(comp, td),
    
    // SECCIÓN 15: Notas de Implementación
    consideraciones: generarConsideraciones(comp, td),
    deudaTecnica: generarDeudaTecnica(comp, td),
    todos: generarTodos(comp, td),
    
    // SECCIÓN 16: Checklist
    checklistImplementacion: generarChecklistImplementacion(comp, td),
    
    // SECCIÓN 17: Referencias
    referencias: generarReferencias(comp, td)
  };
  
  // Reemplazar placeholders en plantilla
  let ftv = plantilla;
  for (const [clave, valor] of Object.entries(datos)) {
    ftv = reemplazarPlaceholder(ftv, clave, valor);
  }
  
  // Validar completitud
  validarFTV(ftv, comp);
  
  return ftv;
}
```

---

#### Acción 3.3: Generadores de Secciones Específicas

**Propósito del Componente**:

```typescript
function generarProposito(comp: Component, td: TechnicalDesignData): string {
  // Buscar descripción en el TD (sección 2.3 - Componentes)
  const descripcionTD = buscarDescripcionEnTD(comp.nombre, td);
  
  if (descripcionTD) {
    return descripcionTD;
  }
  
  // Inferir del nombre y tipo
  if (comp.tipo === 'Page') {
    return `Página principal de ${humanize(comp.nombre)}. Orquesta la funcionalidad completa de [feature].`;
  }
  
  if (comp.nombre.includes('Button')) {
    return `Botón reutilizable con variantes de estilo y estados (disabled, loading).`;
  }
  
  if (comp.nombre.includes('Modal') || comp.nombre.includes('Dialog')) {
    return `Modal/diálogo que presenta ${humanize(comp.nombre)} en una capa superpuesta.`;
  }
  
  // Default
  return `Componente ${comp.tipo} responsable de ${comp.responsabilidad || humanize(comp.nombre)}.`;
}
```

---

**Caso de Uso**:

```typescript
function generarCasoUso(comp: Component, td: TechnicalDesignData): string {
  // Buscar en historias de usuario del FP si están disponibles
  if (HISTORIAS_USUARIO) {
    const historiaRelacionada = HISTORIAS_USUARIO.find(h => 
      h.titulo.toLowerCase().includes(comp.nombre.toLowerCase())
    );
    
    if (historiaRelacionada) {
      return `
Como ${historiaRelacionada.como},
Cuando ${historiaRelacionada.cuando},
Entonces ${historiaRelacionada.entonces.replace('sistema', `veo el componente ${comp.nombre}`)},
Para ${historiaRelacionada.para}
      `.trim();
    }
  }
  
  // Inferir genérico
  return `
Como usuario,
Cuando [acción que desencadena este componente],
Entonces interactúo con ${comp.nombre},
Para [objetivo que cumple]
  `.trim();
}
```

---

**Wireframe ASCII**:

```typescript
function generarWireframe(comp: Component, td: TechnicalDesignData): string {
  // Si es un componente simple (UI Component)
  if (comp.tipo === 'UI Component') {
    if (comp.nombre.includes('Button')) {
      return `
┌─────────────────────┐
│  [Icon] Text        │  ← Button
└─────────────────────┘
      `;
    }
    
    if (comp.nombre.includes('Badge')) {
      return `
┌─────┐
│  3  │  ← Badge (contador)
└─────┘
      `;
    }
  }
  
  // Si es Feature Component o Page
  if (comp.tipo === 'Feature Component' || comp.tipo === 'Page') {
    let wireframe = `
┌─────────────────────────────────────────────────────────────┐
│ ${comp.nombre.padEnd(59)} │
├─────────────────────────────────────────────────────────────┤
│                                                             │
`;
    
    // Agregar hijos
    comp.hijos.forEach(nombreHijo => {
      wireframe += `
│  ┌──────────────────────────────────────────┐             │
│  │ ${nombreHijo.padEnd(40)} │             │
│  └──────────────────────────────────────────┘             │
│                                                             │
`;
    });
    
    wireframe += `
└─────────────────────────────────────────────────────────────┘
    `;
    
    return wireframe;
  }
  
  // Default genérico
  return `
┌─────────────────────────────────────────────────────────────┐
│ [Descripción visual del componente]                        │
│                                                             │
│ [Contenido principal]                                       │
│                                                             │
│ [Elementos interactivos: botones, inputs, etc.]           │
└─────────────────────────────────────────────────────────────┘

**Nota**: Referencia de diseño en Figma: [URL si existe]
  `;
}
```

---

**Props Interface**:

```typescript
function generarPropsInterface(comp: Component, td: TechnicalDesignData): string {
  // Inferir props basándose en:
  // 1. Endpoints que consume (si hace fetch)
  // 2. Eventos que debe manejar (callbacks)
  // 3. Datos que debe mostrar
  // 4. Configuración
  
  const props = [];
  
  // Props básicas (siempre presentes)
  if (comp.tipo !== 'Page') {
    props.push(`  className?: string;  // Clases CSS adicionales`);
  }
  
  // Si muestra datos
  const endpointsConsumidos = extraerEndpoints(comp, td);
  if (endpointsConsumidos.length > 0) {
    const endpoint = endpointsConsumidos[0];
    const tipoDato = inferirTipoDato(endpoint);
    
    if (endpoint.method === 'GET' && endpoint.returnsList) {
      props.push(`  items: ${tipoDato}[];  // Lista de items a mostrar`);
    } else {
      props.push(`  data: ${tipoDato};  // Datos a mostrar`);
    }
  }
  
  // Callbacks (eventos)
  const eventos = inferirEventos(comp, td);
  eventos.forEach(evento => {
    props.push(`  ${evento.nombre}: ${evento.signature};  // ${evento.descripcion}`);
  });
  
  // Props de configuración
  if (tieneConfiguracion(comp)) {
    props.push(`  config?: {`);
    props.push(`    [opciones de configuración]`);
    props.push(`  };`);
  }
  
  // Props opcionales de comportamiento
  if (puedeEstarDeshabilitado(comp)) {
    props.push(`  disabled?: boolean;  // Deshabilitar interacción`);
  }
  
  if (tieneVariantes(comp)) {
    props.push(`  variant?: 'default' | 'compact' | 'full';  // Variante visual`);
  }
  
  // Generar interface
  return `
\`\`\`typescript
interface ${comp.nombre}Props {
${props.map(p => '  ' + p).join('\n')}
}
\`\`\`
  `.trim();
}
```

---

**Variables de Estado**:

```typescript
function generarVariablesEstado(comp: Component, td: TechnicalDesignData): string {
  const estados = [];
  
  // Estado de carga (si consume API)
  const endpointsConsumidos = extraerEndpoints(comp, td);
  if (endpointsConsumidos.length > 0) {
    estados.push({
      nombre: 'isLoading',
      tipo: 'boolean',
      inicial: 'false',
      proposito: 'Indica si está cargando datos'
    });
    
    estados.push({
      nombre: 'error',
      tipo: 'Error | null',
      inicial: 'null',
      proposito: 'Almacena error si la carga falla'
    });
  }
  
  // Estado de datos
  if (endpointsConsumidos.some(e => e.method === 'GET')) {
    const tipoDato = inferirTipoDato(endpointsConsumidos[0]);
    estados.push({
      nombre: 'data',
      tipo: `${tipoDato}[]`,
      inicial: '[]',
      proposito: 'Datos obtenidos del backend'
    });
  }
  
  // Estado de UI (modales, tabs, etc.)
  if (comp.nombre.includes('Modal')) {
    estados.push({
      nombre: 'isOpen',
      tipo: 'boolean',
      inicial: 'false',
      proposito: 'Controla si el modal está visible'
    });
  }
  
  if (tieneFormulario(comp)) {
    estados.push({
      nombre: 'formData',
      tipo: 'FormData',
      inicial: 'defaultFormData',
      proposito: 'Datos del formulario'
    });
    
    estados.push({
      nombre: 'errors',
      tipo: 'ValidationErrors',
      inicial: '{}',
      proposito: 'Errores de validación'
    });
  }
  
  if (tieneTabs(comp)) {
    estados.push({
      nombre: 'activeTab',
      tipo: "'tab1' | 'tab2' | 'tab3'",
      inicial: "'tab1'",
      proposito: 'Tab actualmente seleccionada'
    });
  }
  
  // Generar código
  let codigo = `\`\`\`typescript\n`;
  
  estados.forEach(estado => {
    codigo += `// ${estado.proposito}\n`;
    codigo += `const [${estado.nombre}, set${capitalize(estado.nombre)}] = useState<${estado.tipo}>(${estado.inicial});\n\n`;
  });
  
  codigo += `\`\`\``;
  
  return codigo;
}
```

---

**Eventos de Usuario**:

```typescript
function generarEventosUsuario(comp: Component, td: TechnicalDesignData): string {
  const eventos = [];
  
  // Inferir eventos comunes basados en tipo de componente
  if (tieneFormulario(comp)) {
    eventos.push({
      accion: 'Submit de formulario',
      evento: 'onSubmit',
      handler: 'handleSubmit()',
      efecto: 'Valida y envía datos al backend'
    });
    
    eventos.push({
      accion: 'Cambio en input',
      evento: 'onChange',
      handler: 'handleInputChange()',
      efecto: 'Actualiza estado del formulario'
    });
  }
  
  if (comp.nombre.includes('Button') || tieneBotones(comp)) {
    eventos.push({
      accion: 'Click en botón principal',
      evento: 'onClick',
      handler: 'handleAction()',
      efecto: 'Ejecuta acción principal del componente'
    });
  }
  
  if (comp.nombre.includes('Modal') || comp.nombre.includes('Dialog')) {
    eventos.push({
      accion: 'Click fuera del modal',
      evento: 'onClick (overlay)',
      handler: 'handleClose()',
      efecto: 'Cierra el modal'
    });
    
    eventos.push({
      accion: 'Tecla "Escape"',
      evento: 'onKeyDown',
      handler: 'handleEscape()',
      efecto: 'Cierra el modal'
    });
  }
  
  if (comp.nombre.includes('Upload') || comp.nombre.includes('Dropzone')) {
    eventos.push({
      accion: 'Drag and drop de archivo',
      evento: 'onDrop',
      handler: 'handleFileDrop()',
      efecto: 'Procesa el archivo subido'
    });
  }
  
  // Buscar eventos en el TD (flujos de datos)
  const eventosDeTD = extraerEventosDeFlujos(comp, td);
  eventos.push(...eventosDeTD);
  
  // Generar tabla
  let tabla = `| Acción del Usuario | Evento | Handler | Efecto |\n`;
  tabla += `|-------------------|--------|---------|--------|\n`;
  
  eventos.forEach(e => {
    tabla += `| ${e.accion} | ${e.evento} | \`${e.handler}\` | ${e.efecto} |\n`;
  });
  
  return tabla;
}
```

---

**Endpoints Consumidos**:

```typescript
function extraerEndpoints(comp: Component, td: TechnicalDesignData): Endpoint[] {
  // Buscar en el TD qué endpoints consume este componente
  
  // ESTRATEGIA 1: Buscar menciones en flujos de datos
  const endpointsDeFlujos = td.arquitectura.flujosDatos
    .filter(flujo => flujo.componente === comp.nombre)
    .flatMap(flujo => flujo.endpointsUsados);
  
  // ESTRATEGIA 2: Inferir por nombre del componente
  // Ej: "NotificationList" probablemente consume "GET /notifications"
  const endpointsInferidos = td.contratos.endpoints.filter(endpoint => {
    const recurso = extraerRecurso(endpoint.path); // "/api/v1/notifications" → "notifications"
    return comp.nombre.toLowerCase().includes(recurso.toLowerCase());
  });
  
  // ESTRATEGIA 3: Buscar en código de ejemplo del TD
  const endpointsDeCodigo = extraerDeCodigoEjemplo(comp, td);
  
  // Combinar y deduplicar
  const todosEndpoints = [...endpointsDeFlujos, ...endpointsInferidos, ...endpointsDeCodigo];
  return deduplicar(todosEndpoints);
}

function generarDocumentacionEndpoint(endpoint: Endpoint, comp: Component): string {
  return `
#### Endpoint: ${endpoint.proposito || endpoint.method + ' ' + endpoint.path}

\`\`\`typescript
${endpoint.method} ${endpoint.path}
\`\`\`

**Propósito**: ${endpoint.proposito}

**Request**:
\`\`\`typescript
${generarRequestExample(endpoint)}
\`\`\`

**Response (${endpoint.successCode || '200 OK'})**:
\`\`\`typescript
${generarResponseExample(endpoint)}
\`\`\`

**Manejo de Errores**:
${generarManejoErrores(endpoint)}

**Código**:
\`\`\`typescript
${generarCodigoLlamadaAPI(endpoint, comp)}
\`\`\`
  `.trim();
}
```

---

**Test Plan**:

```typescript
function generarCasosPrueba(comp: Component, td: TechnicalDesignData): string {
  const casos = [];
  
  // Test 1: Renderizado básico (SIEMPRE)
  casos.push({
    nombre: 'Renderizado Básico',
    codigo: `
it('renderiza correctamente con props mínimas', () => {
  render(<${comp.nombre} ${generarPropsMinimas(comp)} />);
  
  expect(screen.getByText('${inferirTextoEsperado(comp)}')).toBeInTheDocument();
});
    `
  });
  
  // Test 2: Interacción (si tiene eventos)
  const eventos = inferirEventos(comp, td);
  if (eventos.length > 0) {
    const eventosPrincipales = eventos.slice(0, 2); // Primeros 2 eventos
    
    eventosPrincipales.forEach(evento => {
      casos.push({
        nombre: `Interacción: ${evento.descripcion}`,
        codigo: `
it('${evento.descripcion.toLowerCase()}', async () => {
  const mock${capitalize(evento.nombre)} = vi.fn();
  const user = userEvent.setup();
  
  render(<${comp.nombre} ${evento.nombre}={mock${capitalize(evento.nombre)}} />);
  
  ${generarCodigoInteraccion(evento, comp)}
  
  expect(mock${capitalize(evento.nombre)}).toHaveBeenCalled();
});
        `
      });
    });
  }
  
  // Test 3: Validación (si tiene formulario)
  if (tieneFormulario(comp)) {
    casos.push({
      nombre: 'Validación de Formulario',
      codigo: `
it('muestra errores de validación', async () => {
  const user = userEvent.setup();
  
  render(<${comp.nombre} />);
  
  const submitButton = screen.getByRole('button', { name: /enviar/i });
  await user.click(submitButton);
  
  expect(screen.getByRole('alert')).toHaveTextContent('Campo requerido');
});
      `
    });
  }
  
  // Test 4: Estados de carga (si consume API)
  const endpoints = extraerEndpoints(comp, td);
  if (endpoints.length > 0) {
    casos.push({
      nombre: 'Estados de Carga',
      codigo: `
it('muestra spinner mientras carga datos', async () => {
  render(<${comp.nombre} />);
  
  expect(screen.getByRole('status')).toBeInTheDocument();
  
  await waitFor(() => {
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
  });
});
      `
    });
  }
  
  // Test 5: Accesibilidad (SIEMPRE)
  casos.push({
    nombre: 'Accesibilidad',
    codigo: `
it('no tiene violaciones de accesibilidad', async () => {
  const { container } = render(<${comp.nombre} />);
  const results = await axe(container);
  expect(results).toHaveNoViolations();
});
    `
  });
  
  // Generar documento
  let doc = '';
  casos.forEach((caso, idx) => {
    doc += `\n#### Test ${idx + 1}: ${caso.nombre}\n\n`;
    doc += `\`\`\`typescript${caso.codigo}\`\`\`\n`;
  });
  
  return doc;
}
```

---

### FASE 4: Validación y Post-procesamiento

#### Acción 4.1: Validar Cada FTV Generada

```typescript
function validarFTV(ftv: string, comp: Component): void {
  const validaciones = [
    {
      nombre: 'Tiene sección de Props',
      check: () => ftv.includes('## 4. Props y API del Componente'),
      severidad: 'ERROR'
    },
    {
      nombre: 'Tiene al menos 1 ejemplo de uso',
      check: () => ftv.includes('### 4.3. Ejemplos de Uso'),
      severidad: 'ERROR'
    },
    {
      nombre: 'Tiene código TypeScript válido',
      check: () => validarSintaxisTypeScript(ftv),
      severidad: 'ERROR'
    },
    {
      nombre: 'Tiene plan de testing',
      check: () => ftv.includes('## 12. Testing'),
      severidad: 'WARNING'
    },
    {
      nombre: 'Tiene checklist de a11y',
      check: () => ftv.includes('### 11.1. Checklist de Accesibilidad'),
      severidad: 'WARNING'
    },
    {
      nombre: 'No tiene placeholders sin llenar',
      check: () => !ftv.includes('[PENDIENTE]') && !ftv.includes('[TODO]'),
      severidad: 'WARNING'
    }
  ];
  
  const fallos = validaciones.filter(v => !v.check());
  
  if (fallos.some(f => f.severidad === 'ERROR')) {
    throw new Error(`
      FTV inválida para ${comp.nombre}:
      ${fallos.filter(f => f.severidad === 'ERROR').map(f => '- ' + f.nombre).join('\n')}
    `);
  }
  
  if (fallos.some(f => f.severidad === 'WARNING')) {
    console.warn(`
      ⚠️ Advertencias en FTV de ${comp.nombre}:
      ${fallos.filter(f => f.severidad === 'WARNING').map(f => '- ' + f.nombre).join('\n')}
    `);
  }
}
```

---

#### Acción 4.2: Generar Índice de Blueprints

```typescript
function generarIndice(componentes: Component[], ftvs: string[]): string {
  let indice = `# Índice de Blueprints - ${td.nombre}\n\n`;
  indice += `**Technical Design**: \`${td.id}-${kebabCase(td.nombre)}.md\`\n`;
  indice += `**Sprint**: ${td.sprint}\n`;
  indice += `**Fecha de Generación**: ${hoy()}\n\n`;
  indice += `---\n\n`;
  indice += `## Componentes Generados (${componentes.length})\n\n`;
  
  // Agrupar por tipo
  const porTipo = agruparPor(componentes, 'tipo');
  
  Object.entries(porTipo).forEach(([tipo, comps]) => {
    indice += `### ${tipo}s\n\n`;
    indice += `| Componente | Archivo FTV | Descripción |\n`;
    indice += `|------------|-------------|-------------|\n`;
    
    comps.forEach(comp => {
      const archivo = `ftv-${comp.nombreArchivo}.md`;
      indice += `| \`${comp.nombre}\` | [${archivo}](./${archivo}) | ${comp.responsabilidad} |\n`;
    });
    
    indice += `\n`;
  });
  
  indice += `---\n\n`;
  indice += `## Árbol de Componentes\n\n`;
  indice += `\`\`\`\n${generarArbolCompleto(componentes)}\`\`\`\n\n`;
  
  indice += `---\n\n`;
  indice += `## Próximos Pasos\n\n`;
  indice += `1. ✅ Revisar cada FTV generada\n`;
  indice += `2. ✅ Ajustar props/estado si es necesario\n`;
  indice += `3. ✅ Generar Backend Sync Brief\n`;
  indice += `   \`\`\`bash\n`;
  indice += `   gemini-cli < @.gemini/prompts/04-generate-backend-sync-brief.md\n`;
  indice += `   \`\`\`\n`;
  indice += `4. ✅ Comenzar implementación\n\n`;
  
  indice += `---\n\n`;
  indice += `**Generado por**: Blueprint Generator v1\n`;
  indice += `**Fecha**: ${hoy()}\n`;
  
  return indice;
}
```

---

### FASE 5: Output Final

```markdown
✅ BLUEPRINTS/FTVs GENERADOS EXITOSAMENTE

**Technical Design**: TD-005-real-time-notifications.md

---

## 📋 Componentes Generados (6)

### Pages (1)
- `NotificationCenterPage` → `ftv-notification-center-page.md`

### Feature Components (3)
- `NotificationBell` → `ftv-notification-bell.md`
- `NotificationDropdown` → `ftv-notification-dropdown.md`
- `NotificationList` → `ftv-notification-list.md`

### UI Components (2)
- `NotificationBadge` → `ftv-notification-badge.md`
- `NotificationItem` → `ftv-notification-item.md`

---

## 📁 Archivos Generados

```
@.gemini/blueprints/
├── ftv-notification-center-page.md       (Page)
├── ftv-notification-bell.md              (Feature Component)
├── ftv-notification-dropdown.md          (Feature Component)
├── ftv-notification-list.md              (Feature Component)
├── ftv-notification-badge.md             (UI Component)
├── ftv-notification-item.md              (UI Component)
└── INDEX.md                               (Índice)
```

---

## 🌳 Árbol de Componentes

```
NotificationCenterPage
└─ NotificationBell
   ├─ NotificationBadge
   └─ NotificationDropdown
      └─ NotificationList
         └─ NotificationItem (×N)
```

---

## 📊 Estadísticas

**Componentes**:
- Total: 6
- Pages: 1
- Feature Components: 3
- UI Components: 2

**Líneas de código de ejemplo**: ~2,400 líneas

**Tests planificados**: 30 casos (5 por componente)

**Endpoints integrados**: 3
- GET /api/v1/notifications
- PATCH /api/v1/notifications/{id}/read
- WS /ws/notifications

---

## 🎯 Próximos Pasos

### 1. Revisar FTVs Generadas

```bash
# Abrir cada FTV y validar:
code @.gemini/blueprints/ftv-notification-bell.md
code @.gemini/blueprints/ftv-notification-dropdown.md
# ...

# Checklist de Revisión:
- [ ] Props son correctas y completas
- [ ] Estado interno está bien modelado
- [ ] Eventos de usuario son los esperados
- [ ] Integración con API es correcta
- [ ] Plan de testing es suficiente
```

### 2. Generar Backend Sync Brief

```bash
gemini-cli < @.gemini/prompts/04-generate-backend-sync-brief.md
```

Esto consolidará todos los contratos API de las FTVs en un documento unificado para el backend.

### 3. Comenzar Implementación

**Orden Sugerido** (bottom-up):

```bash
# 1. UI Components primero (sin dependencias)
src/components/notifications/NotificationBadge.tsx
src/components/notifications/NotificationItem.tsx

# 2. Feature Components (usan UI Components)
src/components/notifications/NotificationList.tsx
src/components/notifications/NotificationDropdown.tsx
src/components/notifications/NotificationBell.tsx

# 3. Pages (orquestan todo)
src/pages/NotificationCenterPage.tsx
```

**Para cada componente**:
1. Crear archivo según estructura del proyecto
2. Copiar interface de Props de la FTV
3. Implementar estado según FTV
4. Implementar handlers según FTV
5. Escribir tests según casos en FTV
6. Validar accesibilidad

---

## ⚠️ Notas Importantes

### Inferencias Realizadas

Las siguientes decisiones fueron **inferidas** del TD y deben validarse:

**Props**:
- `NotificationBell.onNotificationClick` → Inferido porque necesita manejar clicks
- `NotificationList.maxItems` → Inferido como configurable (default: 50)

**Estado**:
- `NotificationDropdown.isOpen` → Inferido porque es un dropdown
- `NotificationList.unreadCount` → Inferido porque se muestra en el badge

**Integraciones**:
- WebSocket reconecta automáticamente → Inferido de mejores prácticas, validar con backend

### Decisiones de Diseño

**Accesibilidad**:
- Todos los componentes tienen `role` apropiado
- Focus trap en NotificationDropdown
- ARIA live regions para actualizaciones

**Performance**:
- NotificationList usa virtualización si > 100 items
- Memoización aplicada a NotificationItem
- Debounce en mark-as-read (500ms)

---

## 📚 Referencias

- **Technical Design**: `@.gemini/sprints/technical-designs/TD-005-real-time-notifications.md`
- **Feature Plan**: `@.gemini/sprints/feature-plans/FP-005-real-time-notifications.md`
- **Project Summary**: `@.gemini/project-summary.md`

---

**Generado por**: Blueprint/FTV Generator v1  
**Tiempo total**: 2 minutos  
**Fecha**: 2024-01-15 12:00:00
```

---

## 4. REGLAS DE CALIDAD

### Regla 1: Código TypeScript Debe Ser Sintácticamente Válido

```typescript
// Validar que no haya:
// ❌ Interfaces incompletas
// ❌ Tipos inválidos
// ❌ Imports faltantes

// ✅ Generar código completo y ejecutable
interface NotificationBellProps {
  unreadCount: number;
  onOpen: () => void;
  maxDisplayCount?: number;
}
```

---

### Regla 2: Props Deben Ser Consistentes Entre Componentes Relacionados

```typescript
// Si NotificationItem tiene:
interface NotificationItemProps {
  notification: Notification;  // ← Usar tipo Notification
  onRead: (id: string) => void;
}

// Entonces NotificationList debe usar el mismo tipo:
interface NotificationListProps {
  notifications: Notification[];  // ← Mismo tipo
  onRead: (id: string) => void;   // ← Mismo callback
}
```

---

### Regla 3: Estados Visuales Completos

Siempre generar AL MENOS:
- Estado Inicial
- Estado Cargando
- Estado Con Datos
- Estado Vacío
- Estado de Error

---

### Regla 4: Tests Deben Cubrir Casos Reales

```typescript
// ❌ MAL: Test demasiado genérico
it('works', () => {
  expect(true).toBe(true);
});

// ✅ BIEN: Test específico y útil
it('marca notificación como leída cuando se hace click', async () => {
  const mockOnRead = vi.fn();
  render(<NotificationItem notification={mockNotif} onRead={mockOnRead} />);
  
  await user.click(screen.getByRole('button', { name: /marcar como leída/i }));
  
  expect(mockOnRead).toHaveBeenCalledWith(mockNotif.id);
});
```

---

## 5. MANEJO DE EDGE CASES

### Edge Case 1: Componente Sin Props Claras

Si no se puede inferir props del TD:

```markdown
⚠️ PROPS AMBIGUAS

No pude inferir props completas para ${comp.nombre} del Technical Design.

**Generé props mínimas**:
- className?: string
- children?: ReactNode

**Acción Requerida**:
Revisar FTV generada y completar props según requisitos de negocio.

¿Continuar? (S/N)
```

---

### Edge Case 2: Componente Mencionado Pero Sin Detalles

```markdown
⚠️ COMPONENTE CON INFORMACIÓN LIMITADA

El componente ${comp.nombre} se menciona en el TD pero sin detalles de implementación.

**Generé FTV básica** con:
- Props inferidas del nombre
- Estado mínimo
- Tests básicos

**Marcaré secciones con [REVISAR]** para que completes manualmente.

¿Continuar? (S/N)
```

---

