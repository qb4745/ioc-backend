# GENERADOR DE TECHNICAL DESIGN (v1)

## 1. CONFIGURACIÓN

**Propósito**: Convertir un Feature Plan aprobado en un Technical Design detallado con arquitectura, modelo de datos, contratos de API y plan de implementación completo.

**Input**: 
- Feature Plan específico (`FP-XXX-nombre.md`)
- Project Summary (`@.gemini/project-summary.md`)

**Output**: 
- `@.gemini/sprints/technical-designs/TD-XXX-[nombre].md`
- Checklist de implementación

**Audiencia**: Tech Lead, Desarrolladores (Backend y Frontend), DevOps

---

## 2. MANDATO OPERATIVO (PARA LA IA)

**Tu Rol**: Senior Software Architect con experiencia en arquitecturas fullstack (React + Spring Boot), bases de datos relacionales, y mejores prácticas de desarrollo.

**Tu Misión**: 
1. Leer y comprender el Feature Plan completamente
2. Analizar el contexto técnico actual (Project Summary)
3. Diseñar la arquitectura técnica óptima para la feature
4. Generar contratos de API completos y precisos
5. Diseñar modelo de datos normalizado y eficiente
6. Proveer código de ejemplo para guiar la implementación
7. Identificar consideraciones de performance, seguridad y escalabilidad
8. Crear un plan de testing comprehensivo

---

## 3. PROTOCOLO DE EJECUCIÓN

### FASE 1: Carga de Contexto

#### Acción 1.1: Validar que se Proporcione el Feature Plan

```markdown
🔍 SOLICITUD DE FEATURE PLAN

Para generar un Technical Design, necesito el Feature Plan a implementar.

Por favor proporciona:

**Opción A**: ID del Feature Plan
Ejemplo: FP-005

**Opción B**: Ruta completa del archivo
Ejemplo: @.gemini/sprints/feature-plans/FP-005-real-time-notifications.md

**Opción C**: Pega el contenido del Feature Plan aquí

[ESPERAR RESPUESTA]
```

**Si el usuario proporciona solo un ID**:

```bash
# Buscar el archivo automáticamente
RUTA = "@.gemini/sprints/feature-plans/"
PATRON = "FP-{ID}-*.md"

# Buscar archivo que coincida
ARCHIVO_ENCONTRADO = buscar(RUTA + PATRON)

if not ARCHIVO_ENCONTRADO:
    ERROR("No se encontró Feature Plan con ID: {ID}")
    LISTAR_DISPONIBLES()
    DETENER()
```

---

#### Acción 1.2: Leer y Parsear Feature Plan

```typescript
// Extraer información crítica del FP

interface FeaturePlanData {
  id: string;                    // FP-005
  nombre: string;                // Real-Time Notifications
  sprint: string;                // Sprint 2
  prioridad: string;             // Alta
  estimacion: string;            // 5-7 días
  
  // Contexto de negocio
  problema: string;              // Sección 1.1
  solucion: string;              // Sección 1.2
  alcanceMVP: string[];          // Sección 1.3
  
  // Requisitos funcionales
  historias: UserStory[];        // Sección 3.1
  criteriosAceptacion: string[]; // De las historias
  requisitosNoFuncionales: NFR[]; // Sección 3.2
  
  // Análisis técnico previo (del FP)
  componentesAfectados: {
    frontend: string[];
    backend: string[];
    baseDatos: string[];
  };
  dependenciasTecnicas: string[];
  
  // Diseño UI (del FP)
  wireframes: string;            // URL o descripción
  flujoUsuario: string;
  
  // Contratos preliminares (del FP)
  endpointsPropuestos: Endpoint[];
  modeloDatosPreliminar: any;
  
  // Riesgos
  riesgos: Risk[];
}
```

**Validar Completitud del FP**:

```typescript
const validaciones = [
  { campo: 'historias', minimo: 1, mensaje: "FP debe tener al menos 1 historia de usuario" },
  { campo: 'alcanceMVP', minimo: 1, mensaje: "FP debe definir alcance del MVP" },
  { campo: 'componentesAfectados', requerido: true, mensaje: "FP debe especificar componentes afectados" }
];

validaciones.forEach(v => {
  if (!cumple(featurePlanData, v)) {
    ADVERTENCIA(v.mensaje);
    // Continuar pero marcar como [ASUMIR]
  }
});
```

---

#### Acción 1.3: Cargar Project Summary

```bash
# Leer contexto del proyecto
ARCHIVO_PS = "@.gemini/project-summary.md"

if not existe(ARCHIVO_PS):
    ADVERTENCIA(`
        ⚠️ Project Summary no encontrado
        
        El diseño técnico será más genérico sin contexto del proyecto.
        
        Recomendación: Ejecutar primero:
        @.gemini/prompts/01-generate-project-summary-backend.md
        
        ¿Continuar sin contexto? (S/N)
    `)
    ESPERAR_RESPUESTA()
```

**Extraer del Project Summary**:

```typescript
interface ProjectContext {
  // Stack
  frontend: {
    framework: string;           // React 19
    lenguaje: string;            // TypeScript
    buildTool: string;           // Vite
    libreriasClave: Library[];   // zustand, axios, etc.
  };
  backend: {
    framework: string;           // Spring Boot 3
    lenguaje: string;            // Java 21
    buildTool: string;           // Maven
    libreriasClave: Library[];   // Spring Security, JPA, etc.
  };
  baseDatos: {
    dbms: string;                // PostgreSQL
    version: string;             // 15.x
    orm: string;                 // Hibernate
  };
  
  // Arquitectura existente
  serviciosExternos: Service[];  // Supabase, Metabase, etc.
  endpointsExistentes: Endpoint[];
  tablasExistentes: Table[];
  
  // Convenciones
  estiloAPI: string;             // REST, GraphQL, etc.
  versionadoAPI: string;         // /api/v1, etc.
  autenticacion: string;         // JWT (Supabase)
  
  // Estructura de directorios
  estructuraFrontend: string;
  estructuraBackend: string;
}
```

---

### FASE 2: Diseño de Arquitectura

#### Acción 2.1: Generar Diagrama de Arquitectura

Basado en el Feature Plan y el contexto del proyecto, generar diagrama ASCII:

```typescript
function generarDiagramaArquitectura(fp: FeaturePlanData, ctx: ProjectContext) {
  // Identificar componentes nuevos vs existentes
  const componentesNuevos = identificarNuevos(fp.componentesAfectados);
  const componentesExistentes = identificarExistentes(fp.componentesAfectados, ctx);
  
  // Identificar flujos de datos
  const flujos = identificarFlujos(fp.historias);
  
  // Generar diagrama en capas:
  // 1. Frontend (componentes UI)
  // 2. API Layer (endpoints REST/WebSocket)
  // 3. Business Logic (services)
  // 4. Data Access (repositories)
  // 5. Persistence (DB, servicios externos)
  
  return generarASCIIDiagram(componentesNuevos, componentesExistentes, flujos);
}
```

**Ejemplo de lógica**:

```typescript
// Si el FP menciona "notificaciones en tiempo real"
if (fp.nombre.toLowerCase().includes('real-time') || 
    fp.solucion.toLowerCase().includes('websocket')) {
  
  agregarAlDiagrama({
    frontend: ['WebSocketClient', 'NotificationBell', 'NotificationCenter'],
    backend: ['WebSocketHandler', 'NotificationService'],
    integraciones: ['WebSocket Connection (WSS)']
  });
}

// Si el FP menciona "subir archivos" o "ETL"
if (fp.solucion.toLowerCase().includes('upload') || 
    fp.solucion.toLowerCase().includes('archivo')) {
  
  agregarAlDiagrama({
    frontend: ['FileUploadComponent'],
    backend: ['FileUploadController', 'ETLProcessor (async)'],
    integraciones: ['S3 Bucket / Local Storage']
  });
}
```

---

#### Acción 2.2: Diseñar Flujo de Datos Detallado

Para cada historia de usuario principal, generar flujo paso a paso:

```typescript
function generarFlujoDatos(historia: UserStory, ctx: ProjectContext) {
  const pasos = [];
  
  // PASO 1: Acción del usuario
  pasos.push({
    actor: "Usuario",
    accion: extraerAccion(historia.when),  // Del "Cuando" de Gherkin
    componente: null
  });
  
  // PASO 2: Componente frontend que captura la acción
  const componenteFrontend = inferirComponente(historia);
  pasos.push({
    actor: "Frontend",
    accion: `Renderizar ${componenteFrontend}`,
    componente: componenteFrontend
  });
  
  // PASO 3-4: Validación local
  pasos.push({
    actor: "Frontend",
    accion: "Validar input con Zod",
    componente: `${componenteFrontend}Form`
  });
  
  // PASO 5: Llamada a API
  const endpoint = inferirEndpoint(historia);
  pasos.push({
    actor: "API Client",
    accion: `${endpoint.method} ${endpoint.path} con JWT`,
    componente: "ServicioAPI"
  });
  
  // PASO 6-10: Backend processing
  pasos.push(
    { actor: "Security Filter", accion: "Validar JWT" },
    { actor: "Controller", accion: "Recibir request" },
    { actor: "Service", accion: "Ejecutar lógica de negocio" },
    { actor: "Repository", accion: "Persistir en DB" },
    { actor: "DB", accion: "Commit transacción" }
  );
  
  // PASO 11-13: Response
  pasos.push(
    { actor: "Controller", accion: "Construir DTO response" },
    { actor: "Frontend", accion: "Actualizar estado (Zustand/Redux)" },
    { actor: "UI", accion: "Re-render con nuevos datos" }
  );
  
  // Estimar tiempos
  const tiempos = estimarTiempos(pasos, ctx);
  
  return formatearFlujo(pasos, tiempos);
}
```

---

### FASE 3: Diseño de Modelo de Datos

#### Acción 3.1: Identificar Entidades Necesarias

```typescript
function identificarEntidades(fp: FeaturePlanData) {
  const entidades = [];
  
  // Analizar el dominio del problema
  const sustantivos = extraerSustantivos(fp.problema, fp.solucion);
  
  // Filtrar sustantivos que son entidades de negocio
  const candidatos = sustantivos.filter(s => {
    return esEntidadNegocio(s) && !esEntidadExistente(s);
  });
  
  // Para cada candidato, definir estructura
  candidatos.forEach(nombre => {
    const entidad = {
      nombre: singularizar(capitalize(nombre)),
      tabla: pluralizar(snakeCase(nombre)),
      campos: inferirCampos(nombre, fp),
      relaciones: inferirRelaciones(nombre, fp),
      indices: inferirIndices(nombre, fp)
    };
    
    entidades.push(entidad);
  });
  
  return entidades;
}
```

**Ejemplo**:

```typescript
// Feature: "Sistema de notificaciones en tiempo real"

// Extrae: "notificación", "usuario", "trabajo ETL"
// Filtra: "usuario" ya existe, "trabajo ETL" ya existe
// Resultado: "notificación" es nueva entidad

const notificacion = {
  nombre: "Notification",
  tabla: "notifications",
  campos: [
    { nombre: "id", tipo: "UUID", pk: true },
    { nombre: "user_id", tipo: "UUID", fk: "users.id" },
    { nombre: "type", tipo: "VARCHAR(50)", enum: true },
    { nombre: "title", tipo: "VARCHAR(255)", requerido: true },
    { nombre: "message", tipo: "TEXT", opcional: true },
    { nombre: "metadata", tipo: "JSONB", opcional: true },
    { nombre: "is_read", tipo: "BOOLEAN", default: false },
    { nombre: "created_at", tipo: "TIMESTAMP", auto: true },
    { nombre: "read_at", tipo: "TIMESTAMP", nullable: true }
  ],
  relaciones: [
    { tipo: "ManyToOne", entidad: "User", campo: "user_id" }
  ],
  indices: [
    { campos: ["user_id", "created_at"], orden: "DESC" },
    { campos: ["user_id", "is_read"], where: "is_read = false" }
  ]
};
```

---

#### Acción 3.2: Generar SQL Schema

```typescript
function generarSQL(entidad: Entity) {
  let sql = `CREATE TABLE ${entidad.tabla} (\n`;
  
  // Campos
  entidad.campos.forEach((campo, idx) => {
    sql += `    ${campo.nombre} ${campo.tipo}`;
    
    if (campo.pk) sql += " PRIMARY KEY";
    if (campo.default !== undefined) sql += ` DEFAULT ${campo.default}`;
    if (campo.requerido) sql += " NOT NULL";
    
    if (idx < entidad.campos.length - 1) sql += ",";
    sql += "\n";
  });
  
  // Foreign Keys
  entidad.relaciones.forEach(rel => {
    if (rel.tipo === "ManyToOne") {
      sql += `    CONSTRAINT fk_${entidad.tabla}_${rel.campo} `;
      sql += `FOREIGN KEY (${rel.campo}) REFERENCES ${rel.tabla}(id)`;
      sql += ` ON DELETE ${rel.onDelete || 'CASCADE'},\n`;
    }
  });
  
  // Constraints
  if (entidad.constraints) {
    entidad.constraints.forEach(c => {
      sql += `    CONSTRAINT ${c.nombre} CHECK (${c.condicion}),\n`;
    });
  }
  
  sql = sql.trimEnd().replace(/,$/, ''); // Remover última coma
  sql += "\n);\n\n";
  
  // Índices
  entidad.indices.forEach(idx => {
    sql += `CREATE INDEX idx_${entidad.tabla}_${idx.campos.join('_')} `;
    sql += `ON ${entidad.tabla}(${idx.campos.join(', ')})`;
    if (idx.where) sql += ` WHERE ${idx.where}`;
    sql += ";\n";
  });
  
  return sql;
}
```

---

#### Acción 3.3: Generar Tipos TypeScript

```typescript
function generarTypeScript(entidad: Entity) {
  let ts = `/**\n * ${entidad.descripcion || entidad.nombre}\n */\n`;
  
  // Enum si tiene campo tipo
  const campoTipo = entidad.campos.find(c => c.enum);
  if (campoTipo) {
    ts += `export enum ${entidad.nombre}${capitalize(campoTipo.nombre)} {\n`;
    campoTipo.valores.forEach(v => {
      ts += `  ${v} = '${v}',\n`;
    });
    ts += "}\n\n";
  }
  
  // Interface principal
  ts += `export interface ${entidad.nombre} {\n`;
  entidad.campos.forEach(campo => {
    const opcional = !campo.requerido ? '?' : '';
    const tipo = mapearTipoTS(campo.tipo, campo.enum);
    ts += `  ${campo.nombre}${opcional}: ${tipo};\n`;
  });
  ts += "}\n\n";
  
  // DTOs
  ts += generarDTOs(entidad);
  
  return ts;
}

function mapearTipoTS(tipoSQL: string, esEnum: boolean) {
  if (esEnum) return `${entidad.nombre}${capitalize(campo.nombre)}`;
  if (tipoSQL.startsWith('VARCHAR') || tipoSQL === 'TEXT') return 'string';
  if (tipoSQL === 'UUID') return 'string';
  if (tipoSQL === 'INTEGER') return 'number';
  if (tipoSQL === 'BOOLEAN') return 'boolean';
  if (tipoSQL === 'TIMESTAMP') return 'string'; // ISO 8601
  if (tipoSQL === 'JSONB') return 'Record<string, any>';
  return 'any';
}
```

---

#### Acción 3.4: Generar Entidades JPA

```typescript
function generarJPA(entidad: Entity, ctx: ProjectContext) {
  const packageName = ctx.backend.packageBase || "com.cambiaso.ioc";
  
  let java = `package ${packageName}.model.entity;\n\n`;
  
  // Imports
  java += `import jakarta.persistence.*;\n`;
  java += `import jakarta.validation.constraints.*;\n`;
  java += `import org.hibernate.annotations.CreationTimestamp;\n`;
  java += `import org.hibernate.annotations.UpdateTimestamp;\n`;
  java += `import java.time.LocalDateTime;\n`;
  java += `import java.util.UUID;\n\n`;
  
  // Anotaciones de clase
  java += `@Entity\n`;
  java += `@Table(name = "${entidad.tabla}"`;
  
  if (entidad.indices.length > 0) {
    java += `, indexes = {\n`;
    entidad.indices.forEach((idx, i) => {
      java += `    @Index(name = "idx_${entidad.tabla}_${idx.campos.join('_')}", `;
      java += `columnList = "${idx.campos.join(', ')}")`;
      if (i < entidad.indices.length - 1) java += ",";
      java += "\n";
    });
    java += "}";
  }
  
  java += ")\n";
  java += `public class ${entidad.nombre} {\n\n`;
  
  // Campos
  entidad.campos.forEach(campo => {
    java += generarCampoJPA(campo);
  });
  
  // Constructores
  java += generarConstructores(entidad);
  
  // Getters y Setters
  java += generarGettersSetters(entidad);
  
  // Métodos de negocio
  java += generarMetodosNegocio(entidad);
  
  // equals, hashCode
  java += generarEqualsHashCode(entidad);
  
  java += "}\n";
  
  return java;
}
```

---

### FASE 4: Diseño de Contratos de API

#### Acción 4.1: Identificar Endpoints Necesarios

```typescript
function identificarEndpoints(fp: FeaturePlanData, entidades: Entity[]) {
  const endpoints = [];
  
  // Para cada entidad, generar CRUD básico
  entidades.forEach(entidad => {
    // Basado en las historias de usuario, determinar qué operaciones se necesitan
    const operaciones = inferirOperaciones(entidad, fp.historias);
    
    if (operaciones.includes('CREATE')) {
      endpoints.push({
        method: 'POST',
        path: `/api/v1/${pluralizar(kebabCase(entidad.nombre))}`,
        proposito: `Crear un nuevo ${entidad.nombre}`,
        roles: inferirRoles(entidad, 'CREATE', fp),
        requestBody: generarRequestDTO(entidad, 'CREATE'),
        response: generarResponseDTO(entidad)
      });
    }
    
    if (operaciones.includes('READ_LIST')) {
      endpoints.push({
        method: 'GET',
        path: `/api/v1/${pluralizar(kebabCase(entidad.nombre))}`,
        proposito: `Listar ${pluralizar(entidad.nombre)} con paginación`,
        roles: inferirRoles(entidad, 'READ', fp),
        queryParams: generarQueryParams(entidad),
        response: generarListResponseDTO(entidad)
      });
    }
    
    if (operaciones.includes('READ_ONE')) {
      endpoints.push({
        method: 'GET',
        path: `/api/v1/${pluralizar(kebabCase(entidad.nombre))}/{id}`,
        proposito: `Obtener ${entidad.nombre} por ID`,
        roles: inferirRoles(entidad, 'READ', fp),
        pathParams: { id: 'UUID' },
        response: generarResponseDTO(entidad)
      });
    }
    
    if (operaciones.includes('UPDATE')) {
      endpoints.push({
        method: 'PATCH',
        path: `/api/v1/${pluralizar(kebabCase(entidad.nombre))}/{id}`,
        proposito: `Actualizar ${entidad.nombre} parcialmente`,
        roles: inferirRoles(entidad, 'UPDATE', fp),
        pathParams: { id: 'UUID' },
        requestBody: generarRequestDTO(entidad, 'UPDATE'),
        response: generarResponseDTO(entidad)
      });
    }
    
    if (operaciones.includes('DELETE')) {
      endpoints.push({
        method: 'DELETE',
        path: `/api/v1/${pluralizar(kebabCase(entidad.nombre))}/{id}`,
        proposito: `Eliminar ${entidad.nombre}`,
        roles: ['ADMIN'], // Delete suele ser solo admin
        pathParams: { id: 'UUID' },
        response: '204 No Content'
      });
    }
  });
  
  // Endpoints especiales basados en historias de usuario
  const endpointsEspeciales = identificarEndpointsEspeciales(fp.historias);
  endpoints.push(...endpointsEspeciales);
  
  return endpoints;
}
```

**Ejemplo de inferencia**:

```typescript
// Historia: "Como usuario, quiero recibir notificación cuando un ETL termina"

// Inferir que se necesita:
// 1. Endpoint para listar notificaciones (GET /api/v1/notifications)
// 2. Endpoint para marcar como leída (PATCH /api/v1/notifications/{id}/read)
// 3. WebSocket para push real-time (WS /ws/notifications)

function identificarEndpointsEspeciales(historias: UserStory[]) {
  const especiales = [];
  
  historias.forEach(historia => {
    // Detectar "recibir en tiempo real" → WebSocket
    if (historia.then.includes('recibo') && 
        (historia.when.includes('inmediatamente') || fp.nombre.includes('real-time'))) {
      especiales.push({
        method: 'WS',
        path: '/ws/notifications',
        proposito: 'WebSocket para notificaciones en tiempo real',
        eventos: {
          'notification:new': 'Nueva notificación creada',
          'notification:updated': 'Notificación marcada como leída'
        }
      });
    }
    
    // Detectar "marcar como" → Endpoint de acción
    if (historia.when.includes('marcar como')) {
      const accion = extraerAccion(historia.when); // "read"
      especiales.push({
        method: 'PATCH',
        path: `/api/v1/notifications/{id}/${accion}`,
        proposito: `Marcar notificación como ${accion}`
      });
    }
  });
  
  return especiales;
}
```

---

#### Acción 4.2: Generar Contratos Completos

Para cada endpoint, generar documentación completa:

```typescript
function generarContratoCompleto(endpoint: Endpoint, ctx: ProjectContext) {
  let contrato = `#### Endpoint: ${endpoint.proposito}\n\n`;
  contrato += `\`\`\`typescript\n${endpoint.method} ${endpoint.path}\n\`\`\`\n\n`;
  
  contrato += `**Autenticación**: ${endpoint.authRequired ? '✅ Requerida (JWT)' : '❌ Pública'}\n`;
  if (endpoint.roles) {
    contrato += `**Roles Permitidos**: ${endpoint.roles.map(r => `\`${r}\``).join(', ')}\n`;
  }
  contrato += `**Rate Limit**: ${endpoint.rateLimit || '100 requests/minuto'}\n\n`;
  
  contrato += `---\n\n`;
  
  // Request
  if (endpoint.requestBody || endpoint.queryParams || endpoint.pathParams) {
    contrato += `**Request**:\n\n`;
    contrato += `\`\`\`typescript\n`;
    
    if (endpoint.pathParams) {
      contrato += `// Path Params\n`;
      Object.entries(endpoint.pathParams).forEach(([key, type]) => {
        contrato += `${key}: ${type}\n`;
      });
      contrato += `\n`;
    }
    
    if (endpoint.queryParams) {
      contrato += `// Query Params\n`;
      contrato += JSON.stringify(endpoint.queryParams, null, 2);
      contrato += `\n\n`;
    }
    
    if (endpoint.requestBody) {
      contrato += `// Body\n`;
      contrato += endpoint.requestBody;
      contrato += `\n`;
    }
    
    contrato += `\`\`\`\n\n`;
  }
  
  // Response Success
  contrato += `**Response (${endpoint.successCode || '200 OK'})**:\n\n`;
  contrato += `\`\`\`typescript\n`;
  contrato += endpoint.response;
  contrato += `\n\`\`\`\n\n`;
  
  // Error Responses
  contrato += `**Error Responses**:\n\n`;
  const errores = endpoint.errores || generarErroresEstandar(endpoint);
  errores.forEach(error => {
    contrato += `- **${error.code}**: ${error.descripcion}\n`;
  });
  contrato += `\n`;
  
  // Implementación Backend
  contrato += `**Implementación Backend**:\n\n`;
  contrato += `\`\`\`java\n`;
  contrato += generarImplementacionController(endpoint, ctx);
  contrato += `\`\`\`\n\n`;
  
  contrato += `\`\`\`java\n`;
  contrato += generarImplementacionService(endpoint, ctx);
  contrato += `\`\`\`\n\n`;
  
  return contrato;
}
```

---

#### Acción 4.3: Generar Implementaciones de Ejemplo

```typescript
function generarImplementacionController(endpoint: Endpoint, ctx: ProjectContext) {
  const className = `${endpoint.entidad}Controller`;
  const serviceName = `${endpoint.entidad}Service`;
  
  let java = `@RestController\n`;
  java += `@RequestMapping("/api/v1/${endpoint.baseUrl}")\n`;
  java += `@RateLimiter(name = "${endpoint.baseUrl}")\n`;
  java += `public class ${className} {\n\n`;
  java += `    private final ${serviceName} service;\n\n`;
  java += `    @Autowired\n`;
  java += `    public ${className}(${serviceName} service) {\n`;
  java += `        this.service = service;\n`;
  java += `    }\n\n`;
  
  // Generar método según el endpoint
  if (endpoint.method === 'POST') {
    java += `    @PostMapping\n`;
    if (endpoint.roles) {
      java += `    @PreAuthorize("hasAnyRole(${endpoint.roles.map(r => `'${r}'`).join(', ')})")\n`;
    }
    java += `    public ResponseEntity<${endpoint.responseType}> create(\n`;
    java += `        @Valid @RequestBody ${endpoint.requestType} request,\n`;
    java += `        @AuthenticationPrincipal Jwt jwt\n`;
    java += `    ) {\n`;
    java += `        UUID userId = UUID.fromString(jwt.getSubject());\n`;
    java += `        ${endpoint.responseType} response = service.create(userId, request);\n`;
    java += `        \n`;
    java += `        return ResponseEntity\n`;
    java += `            .status(HttpStatus.CREATED)\n`;
    java += `            .body(response);\n`;
    java += `    }\n`;
  }
  
  // Similar para GET, PATCH, DELETE...
  
  java += `}\n`;
  
  return java;
}
```

---

### FASE 5: Estrategia de Testing

#### Acción 5.1: Generar Plan de Testing

```typescript
function generarPlanTesting(fp: FeaturePlanData, endpoints: Endpoint[]) {
  const plan = {
    unitTests: {
      backend: [],
      frontend: []
    },
    integrationTests: {
      backend: [],
      frontend: []
    },
    e2eTests: []
  };
  
  // BACKEND: Unit tests para cada service
  endpoints.forEach(ep => {
    plan.unitTests.backend.push({
      clase: `${ep.entidad}ServiceTest`,
      casos: [
        `${ep.method.toLowerCase()}_ValidRequest_ReturnsResponse`,
        `${ep.method.toLowerCase()}_InvalidData_ThrowsException`,
        `${ep.method.toLowerCase()}_UserNotFound_ThrowsException`,
        // Basado en criterios de aceptación
        ...generarCasosDesdeCriterios(fp.criteriosAceptacion, ep)
      ]
    });
  });
  
  // BACKEND: Integration tests para cada endpoint
  endpoints.forEach(ep => {
    plan.integrationTests.backend.push({
      clase: `${ep.entidad}ControllerIntegrationTest`,
      casos: [
        `${ep.method.toLowerCase()}_ValidRequest_Returns${ep.successCode}`,
        `${ep.method.toLowerCase()}_InvalidRequest_Returns400`,
        `${ep.method.toLowerCase()}_NoAuth_Returns401`,
        `${ep.method.toLowerCase()}_NoPermission_Returns403`
      ]
    });
  });
  
  // FRONTEND: Unit tests para componentes
  const componentes = identificarComponentes(fp);
  componentes.forEach(comp => {
    plan.unitTests.frontend.push({
      archivo: `${comp.nombre}.test.tsx`,
      casos: [
        `renderiza correctamente con props`,
        `maneja interacciones de usuario`,
        `muestra errores de validación`,
        `llama a callbacks apropiadamente`
      ]
    });
  });
  
  // E2E: Tests de flujo completo
  fp.historias.forEach(historia => {
    plan.e2eTests.push({
      nombre: sanitizarNombre(historia.titulo),
      pasos: convertirGherkinAPasos(historia.criteriosAceptacion)
    });
  });
  
  return plan;
}
```

---

#### Acción 5.2: Generar Código de Tests

```typescript
function generarCodigoTests(plan: TestPlan) {
  const codigo = {
    backend: {},
    frontend: {},
    e2e: {}
  };
  
  // Backend Unit Test
  plan.unitTests.backend.forEach(test => {
    codigo.backend[test.clase] = generarTestUnitarioBackend(test);
  });
  
  // Backend Integration Test
  plan.integrationTests.backend.forEach(test => {
    codigo.backend[test.clase] = generarTestIntegracionBackend(test);
  });
  
  // Frontend Unit Test
  plan.unitTests.frontend.forEach(test => {
    codigo.frontend[test.archivo] = generarTestUnitarioFrontend(test);
  });
  
  // E2E Test
  plan.e2eTests.forEach(test => {
    codigo.e2e[`${test.nombre}.spec.ts`] = generarTestE2E(test);
  });
  
  return codigo;
}
```

---

### FASE 6: Consideraciones de Implementación

#### Acción 6.1: Identificar Optimizaciones

```typescript
function identificarOptimizaciones(endpoints: Endpoint[], entidades: Entity[]) {
  const optimizaciones = [];
  
  // Detectar N+1 queries potenciales
  endpoints.forEach(ep => {
    if (ep.method === 'GET' && ep.returnsList) {
      const relaciones = encontrarRelaciones(ep.entidad, entidades);
      if (relaciones.length > 0) {
        optimizaciones.push({
          tipo: 'N+1_QUERY_RISK',
          severidad: 'MEDIA',
          ubicacion: `${ep.entidad}Repository.${ep.method}`,
          solucion: `Usar JOIN FETCH para: ${relaciones.join(', ')}`,
          ejemplo: generarEjemploJoinFetch(ep.entidad, relaciones)
        });
      }
    }
  });
  
  // Detectar necesidad de caching
  endpoints.forEach(ep => {
    if (ep.method === 'GET' && esDataEstatica(ep)) {
      optimizaciones.push({
        tipo: 'CACHING_RECOMMENDED',
        severidad: 'BAJA',
        ubicacion: `${ep.entidad}Service.${ep.method}`,
        solucion: `Agregar @Cacheable con TTL apropiado`,
        ejemplo: `@Cacheable(value = "${ep.entidad}", key = "#id")`
      });
    }
  });
  
  // Detectar necesidad de índices adicionales
  entidades.forEach(ent => {
    const camposBuscables = ent.campos.filter(c => c.searchable);
    if (camposBuscables.length > 0) {
      camposBuscables.forEach(campo => {
        if (!tieneIndice(ent, campo.nombre)) {
          optimizaciones.push({
            tipo: 'MISSING_INDEX',
            severidad: 'ALTA',
            ubicacion: `${ent.tabla}.${campo.nombre}`,
            solucion: `Crear índice para búsquedas eficientes`,
            ejemplo: `CREATE INDEX idx_${ent.tabla}_${campo.nombre} ON ${ent.tabla}(${campo.nombre});`
          });
        }
      });
    }
  });
  
  return optimizaciones;
}
```

---

#### Acción 6.2: Identificar Consideraciones de Seguridad

```typescript
function identificarConsideracionesSeguridad(endpoints: Endpoint[]) {
  const consideraciones = [];
  
  endpoints.forEach(ep => {
    // Rate limiting
    if (esEndpointCritico(ep)) {
      consideraciones.push({
        tipo: 'RATE_LIMITING',
        endpoint: ep.path,
        recomendacion: `Implementar rate limit más estricto (${calcularRateLimit(ep)})`,
        implementacion: generarConfigRateLimit(ep)
      });
    }
    
    // Validación de ownership
    if (ep.accedeARecursoDeUsuario) {
      consideraciones.push({
        tipo: 'OWNERSHIP_VALIDATION',
        endpoint: ep.path,
        recomendacion: 'Verificar que el usuario solo acceda a sus propios recursos',
        implementacion: generarCodigoOwnership(ep)
      });
    }
    
    // Input sanitization
    if (ep.requestBody && tieneCamposTexto(ep.requestBody)) {
      consideraciones.push({
        tipo: 'INPUT_SANITIZATION',
        endpoint: ep.path,
        recomendacion: 'Sanitizar inputs de texto para prevenir XSS',
        implementacion: '@SafeHtml en DTOs o sanitizar en service'
      });
    }
  });
  
  return consideraciones;
}
```

---

### FASE 7: Generación del Documento

#### Acción 7.1: Ensamblar Documento Final

```typescript
function generarDocumentoTechnicalDesign(
  fp: FeaturePlanData,
  ctx: ProjectContext,
  arquitectura: Architecture,
  modelo: DataModel,
  contratos: APIContracts,
  testing: TestPlan,
  optimizaciones: Optimization[],
  seguridad: SecurityConsideration[]
) {
  // Cargar plantilla
  const plantilla = leerArchivo('@.gemini/templates/technical-design-template.md');
  
  // Generar ID (mismo que el FP)
  const tdId = fp.id.replace('FP-', 'TD-');
  const nombreArchivo = `${tdId}-${kebabCase(fp.nombre)}.md`;
  
  // Reemplazar placeholders
  let documento = plantilla;
  
  documento = reemplazar(documento, '[Nombre de la Feature]', fp.nombre);
  documento = reemplazar(documento, '[XXX]', tdId.replace('TD-', ''));
  documento = reemplazar(documento, '[FP-XXX-nombre.md]', `${fp.id}-${kebabCase(fp.nombre)}.md`);
  documento = reemplazar(documento, '[Sprint [X]]', fp.sprint);
  documento = reemplazar(documento, '[YYYY-MM-DD]', hoy());
  
  // Sección 1: Resumen Ejecutivo
  documento = reemplazarSeccion(documento, '1.2. Alcance Técnico', 
    generarAlcanceTecnico(arquitectura, modelo));
  
  documento = reemplazarSeccion(documento, '1.3. Decisiones Arquitectónicas Clave',
    generarTablaDecisiones(arquitectura.decisiones));
  
  // Sección 2: Arquitectura
  documento = reemplazarSeccion(documento, '2.1. Diagrama de Arquitectura',
    arquitectura.diagrama);
  
  documento = reemplazarSeccion(documento, '2.2. Flujo de Datos Detallado',
    generarFlujosDatos(arquitectura.flujos));
  
  documento = reemplazarSeccion(documento, '2.3. Componentes y Responsabilidades',
    generarTablaComponentes(arquitectura.componentes));
  
  // Sección 3: Modelo de Datos
  documento = reemplazarSeccion(documento, '3.1. Diagrama Entidad-Relación',
    modelo.diagramaER);
  
  documento = reemplazarSeccion(documento, '3.2. Especificación de Tablas',
    generarEspecificacionTablas(modelo.entidades));
  
  documento = reemplazarSeccion(documento, '3.3. Tipos TypeScript',
    generarTiposTypeScript(modelo.entidades));
  
  documento = reemplazarSeccion(documento, '3.4. Entidades JPA',
    generarEntidadesJPA(modelo.entidades, ctx));
  
  // Sección 4: Contratos de API
  documento = reemplazarSeccion(documento, '4.1. Endpoints REST',
    generarDocumentacionEndpoints(contratos.endpoints));
  
  if (contratos.websockets) {
    documento = reemplazarSeccion(documento, '4.2. Endpoints WebSocket',
      generarDocumentacionWebSocket(contratos.websockets));
  }
  
  // Sección 5: Seguridad
  documento = reemplazarSeccion(documento, '5.1-5.5',
    generarSeccionSeguridad(seguridad, ctx));
  
  // Sección 6: Testing
  documento = reemplazarSeccion(documento, '6.1-6.6',
    generarSeccionTesting(testing));
  
  // Sección 7: Performance
  documento = reemplazarSeccion(documento, '7.1-7.4',
    generarSeccionPerformance(optimizaciones));
  
  // Sección 11: Checklist
  documento = reemplazarSeccion(documento, '11. Checklist de Implementación',
    generarChecklist(arquitectura, modelo, contratos));
  
  // Guardar
  const ruta = `@.gemini/sprints/technical-designs/${nombreArchivo}`;
  escribirArchivo(ruta, documento);
  
  return { ruta, nombreArchivo, documento };
}
```

---

#### Acción 7.2: Validar Documento Generado

```typescript
const validaciones = [
  {
    nombre: "Al menos 1 entidad definida",
    check: () => modelo.entidades.length >= 1,
    severidad: "ERROR"
  },
  {
    nombre: "Al menos 1 endpoint definido",
    check: () => contratos.endpoints.length >= 1,
    severidad: "ERROR"
  },
  {
    nombre: "Diagrama de arquitectura presente",
    check: () => arquitectura.diagrama.length > 100,
    severidad: "WARNING"
  },
  {
    nombre: "SQL válido",
    check: () => validarSQL(modelo.sql),
    severidad: "ERROR"
  },
  {
    nombre: "TypeScript válido",
    check: () => validarTypeScript(modelo.typescript),
    severidad: "ERROR"
  },
  {
    nombre: "Plan de testing presente",
    check: () => testing.unitTests.length > 0,
    severidad: "WARNING"
  },
  {
    nombre: "Consideraciones de seguridad presentes",
    check: () => seguridad.length > 0,
    severidad: "WARNING"
  }
];

const fallos = validaciones.filter(v => !v.check());

if (fallos.some(f => f.severidad === "ERROR")) {
  throw new Error(`Validación fallida: ${fallos.map(f => f.nombre).join(', ')}`);
}

if (fallos.some(f => f.severidad === "WARNING")) {
  console.warn(`⚠️ Advertencias: ${fallos.map(f => f.nombre).join(', ')}`);
}
```

---

### FASE 8: Output y Próximos Pasos

```markdown
✅ TECHNICAL DESIGN GENERADO EXITOSAMENTE

**Archivo**: `@.gemini/sprints/technical-designs/TD-005-real-time-notifications.md`

---

## 📋 Resumen del Technical Design

**Feature**: Real-Time Notifications  
**ID**: TD-005  
**Basado en**: FP-005-real-time-notifications.md  
**Sprint**: Sprint 2

---

## 🏗️ Arquitectura Diseñada

**Componentes Nuevos**:
- Frontend: 3 componentes (NotificationBell, NotificationCenter, NotificationItem)
- Backend: 2 controllers, 2 services, 1 WebSocket handler
- Base de Datos: 1 tabla nueva (notifications)

**Integraciones**:
- WebSocket (bidireccional)
- Supabase JWT (autenticación)

---

## 📊 Modelo de Datos

**Entidades Nuevas**: 1
- `Notification` (tabla: `notifications`)
  - 9 campos
  - 2 relaciones (User)
  - 2 índices

**Modificaciones a Entidades Existentes**: 0

**Migraciones SQL**: Generadas (`V003__add_notifications_table.sql`)

---

## 🔌 API Diseñada

**Endpoints REST**: 3
- POST /api/v1/notifications (crear - interno)
- GET /api/v1/notifications (listar)
- PATCH /api/v1/notifications/{id}/read (marcar como leída)

**WebSocket**: 1
- WS /ws/notifications
  - Eventos: notification:new, notification:updated

**Contratos Completos**: ✅ Generados con TypeScript + Java

---

## 🧪 Plan de Testing

**Tests Generados**:
- Backend Unit: 12 test cases
- Backend Integration: 8 test cases
- Frontend Unit: 9 test cases
- Frontend Integration: 5 test cases
- E2E: 3 flujos completos

**Cobertura Estimada**: 75%

---

## ⚡ Optimizaciones Identificadas

1. **N+1 Query Risk** (MEDIA): Usar JOIN FETCH en NotificationRepository
2. **Caching** (BAJA): Cachear lista de notificaciones por 1 minuto
3. **Índices** (ALTA): Índice compuesto en (user_id, is_read, created_at)

---

## 🔒 Consideraciones de Seguridad

1. **Ownership Validation**: Usuario solo ve sus notificaciones
2. **Rate Limiting**: 60 req/min en GET, 10 req/min en PATCH
3. **WebSocket Auth**: Validar JWT en conexión y mantener sesión

---

## 🎯 Próximos Pasos

### 1. Revisar y Aprobar (Técnicos)

```bash
code @.gemini/sprints/technical-designs/TD-005-real-time-notifications.md
```

Revisar especialmente:
- [ ] Sección 3: ¿El modelo de datos es correcto?
- [ ] Sección 4: ¿Los contratos de API son completos?
- [ ] Sección 6: ¿El plan de testing es suficiente?
- [ ] Sección 7: ¿Las optimizaciones son apropiadas?

### 2. Crear Blueprints/FTVs (Siguiente Paso)

Una vez aprobado este TD, ejecutar:

```bash
gemini-cli < @.gemini/prompts/07-generate-blueprint-ftv.md \
  --technical-design=TD-005
```

Esto generará las Fichas Técnicas de Vista para cada componente frontend:
- `ftv-notification-bell.md`
- `ftv-notification-center.md`
- `ftv-notification-item.md`

### 3. Generar Backend Sync Brief

Después de crear las FTVs:

```bash
gemini-cli < @.gemini/prompts/04-generate-backend-sync-brief.md
```

Esto consolidará todos los contratos API en un documento unificado.

### 4. Implementación

Con TD + FTVs + Backend Sync Brief, el equipo puede empezar a implementar en paralelo:

**Backend**:
```bash
# Crear migración
cp docs/TD-005/V003__add_notifications_table.sql \
   src/main/resources/db/migration/

# Crear entidades, services, controllers según TD
```

**Frontend**:
```bash
# Crear componentes según FTVs
# Usar contratos del Backend Sync Brief para mocks
```

---

## 📊 Estadísticas del Documento

- **Secciones completadas**: 15/15 (100%)
- **Líneas de código de ejemplo**: ~800
- **Diagramas generados**: 2 (arquitectura, ER)
- **Contratos de API**: 4 (3 REST + 1 WebSocket)
- **Tests planificados**: 37 casos
- **Optimizaciones sugeridas**: 3
- **Consideraciones de seguridad**: 3

---

## ⚠️ Notas Importantes

### Inferencias Realizadas

Las siguientes decisiones fueron **inferidas** automáticamente y deben validarse:

- **Modelo de Datos**: El campo `metadata` se definió como JSONB para flexibilidad
- **Roles**: Se asumió que tanto ADMIN como USER pueden recibir notificaciones
- **Rate Limiting**: Límites basados en mejores prácticas, ajustar según carga esperada
- **TTL de WebSocket**: Token JWT válido por 10 minutos (estándar de Metabase)

### Decisiones Pendientes del Feature Plan

Las siguientes decisiones del FP-005 fueron resueltas:

- ✅ **D1**: WebSocket → Se eligió Socket.IO por resiliencia
- ✅ **D2**: Persistencia → Se eligió PostgreSQL por simplicidad

---

**Technical Design generado por**: IA Technical Design Generator v1  
**Tiempo de generación**: 45 segundos  
**Fecha**: 2024-01-15 11:30:00
```

---

## 4. REGLAS DE CALIDAD

### Regla 1: Código de Ejemplo Debe Compilar

```java
// ❌ MAL: Código con errores de sintaxis
public void create(Request request) {
    service.create(request  // Falta cerrar paréntesis
}

// ✅ BIEN: Código sintácticamente correcto
public ResponseEntity<Response> create(@Valid @RequestBody CreateRequest request) {
    Response response = service.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

---

### Regla 2: SQL Debe Ser Ejecutable

```sql
-- Validar que:
-- ✅ Nombres de tablas existen
-- ✅ Tipos de datos son válidos
-- ✅ Foreign keys referencian tablas/columnas existentes
-- ✅ Constraints tienen sintaxis correcta
```

---

### Regla 3: Contratos TypeScript Consistentes

```typescript
// Asegurar que Request y Response son consistentes:

// Request
interface CreateNotificationRequest {
  type: string;
  title: string;
  message?: string;
}

// Response debe incluir campos del request + campos generados por backend
interface NotificationResponse {
  // Del request
  type: string;
  title: string;
  message?: string;
  // Generados por backend
  id: string;
  userId: string;
  isRead: boolean;
  createdAt: string;
}
```

---

### Regla 4: Mapeo Frontend ↔ Backend Consistente

```typescript
// TypeScript (Frontend)
interface Notification {
  id: string;
  userId: string;
  isRead: boolean;
}

// Java (Backend) - DEBE COINCIDIR
public class NotificationDTO {
    private String id;       // ✅ Mapea a id
    private String userId;   // ✅ Mapea a userId
    private Boolean isRead;  // ✅ Mapea a isRead
}

// SQL - DEBE COINCIDIR
CREATE TABLE notifications (
    id UUID,           -- ✅ Mapea a id (String en DTO)
    user_id UUID,      -- ✅ Mapea a userId (camelCase en DTO)
    is_read BOOLEAN    -- ✅ Mapea a isRead (camelCase en DTO)
);
```

---

## 5. MANEJO DE EDGE CASES

### Edge Case 1: Feature Plan Muy Simple

```markdown
Si el FP es muy básico (ej: "Cambiar color de un botón"):

⚠️ FEATURE MUY SIMPLE

Este Feature Plan no requiere cambios arquitectónicos significativos.

**Sugerencias**:
1. No generar Technical Design completo (overkill)
2. Crear solo una sección de "Cambios Menores" en el FP
3. O generar TD simplificado sin modelo de datos ni nuevos endpoints

¿Deseas continuar con TD completo o crear versión simplificada? (C/S)
```

---

### Edge Case 2: Feature Plan Incompleto

```markdown
Si falta información crítica:

⚠️ FEATURE PLAN INCOMPLETO

El Feature Plan no tiene suficiente información para generar TD completo.

**Falta**:
- Historias de usuario (sección 3.1)
- Descripción de solución (sección 1.2)

**Opciones**:
A) Completar el FP primero (recomendado)
B) Continuar con inferencias (marcaré asunciones)
C) Modo interactivo (te haré preguntas)

¿Qué prefieres? (A/B/C)
```

---

### Edge Case 3: Conflictos con Arquitectura Existente

```markdown
Si el diseño propuesto conflictúa con lo existente:

⚠️ CONFLICTO ARQUITECTÓNICO DETECTADO

El diseño propuesto requiere:
- Agregar WebSocket (nuevo)

Pero el Project Summary indica:
- No hay WebSocket configurado actualmente
- No está en las dependencias

**Implicaciones**:
- Requiere agregar dependencia `spring-boot-starter-websocket`
- Requiere configurar CORS para WebSocket
- Requiere actualizar infraestructura (Nginx, Load Balancer)

**Recomendaciones**:
1. Incluir setup de WebSocket en sección de Deployment
2. Marcar como "Requiere Aprobación de Arquitectura"
3. Agregar a decisiones pendientes

¿Continuar con este diseño? (S/N)
```

---
