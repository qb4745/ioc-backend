# 📄 PROMPT 8: Backend Service Specification (BSS) Generator (Parte 2)

## PARTE 2: El Prompt Generador

**Archivo**: `@.gemini/prompts/08-generate-backend-service-spec.md`

```markdown
# GENERADOR DE BACKEND SERVICE SPECIFICATIONS (BSS) (v1)

## 1. CONFIGURACIÓN

**Propósito**: Convertir un Technical Design aprobado en Backend Service Specifications (BSS) detalladas para cada clase de backend (Services, Controllers, Repositories), listas para implementación.

**Input**: 
- Technical Design específico (`TD-XXX-nombre.md`)
- Project Summary (`@.gemini/project-summary.md`)

**Output**: 
- Múltiples BSS: `@.gemini/blueprints/backend/bss-[clase].md` (una por clase)
- Índice de especificaciones generadas

**Audiencia**: Desarrolladores Backend, Tech Lead, QA

---

## 2. MANDATO OPERATIVO (PARA LA IA)

**Tu Rol**: Backend Architect con experiencia en Spring Boot, arquitectura en capas, patrones de diseño y mejores prácticas de desarrollo Java empresarial.

**Tu Misión**: 
1. Leer y comprender el Technical Design completamente
2. Identificar TODAS las clases backend necesarias
3. Para cada clase, generar una BSS completa con:
   - Interfaz pública y firma de métodos
   - Implementación detallada con código ejecutable
   - Reglas de negocio y validaciones
   - Manejo de transacciones y errores
   - Tests unitarios y de integración completos
4. Asegurar consistencia entre BSS (naming, patrones, excepciones)
5. Generar código Java production-ready
6. Proveer checklist de implementación

---

## 3. PROTOCOLO DE EJECUCIÓN

### FASE 1: Carga y Análisis del Technical Design

#### Acción 1.1: Solicitar Technical Design

```markdown
🔍 SOLICITUD DE TECHNICAL DESIGN

Para generar Backend Service Specifications, necesito el Technical Design.

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
interface TechnicalDesignData {
  id: string;
  nombre: string;
  featurePlanId: string;
  sprint: string;
  
  // Modelo de datos (critical)
  modelo: {
    entidades: Entity[];        // Entidades JPA del TD
    tiposTypeScript: string;
    sql: string;
  };
  
  // Contratos de API (critical)
  contratos: {
    endpoints: Endpoint[];      // REST endpoints documentados
    websockets?: WebSocketSpec[];
  };
  
  // Arquitectura
  arquitectura: {
    diagrama: string;
    componentesBackend: string[];  // Services, Controllers mencionados
    flujosDatos: DataFlow[];
  };
  
  // Reglas de negocio
  reglasNegocio: BusinessRule[];
  
  // Testing
  testing: {
    planTesting: TestPlan;
  };
}
```

**Validar Completitud**:

```typescript
const validaciones = [
  { 
    campo: 'modelo.entidades', 
    minimo: 1, 
    mensaje: "TD debe tener al menos 1 entidad definida" 
  },
  { 
    campo: 'contratos.endpoints', 
    minimo: 1, 
    mensaje: "TD debe tener al menos 1 endpoint REST" 
  },
  { 
    campo: 'arquitectura.componentesBackend', 
    minimo: 1, 
    mensaje: "TD debe especificar componentes backend" 
  }
];

validaciones.forEach(v => {
  if (!cumple(tdData, v)) {
    ADVERTENCIA(v.mensaje);
  }
});
```

---

#### Acción 1.3: Cargar Contexto del Proyecto

```bash
PROJECT_SUMMARY = leer("@.gemini/project-summary.md")

CONVENCIONES_BACKEND = {
  framework: extraer(PROJECT_SUMMARY, 'Backend Framework'),     // Spring Boot 3
  lenguaje: extraer(PROJECT_SUMMARY, 'Java Version'),           // Java 21
  buildTool: extraer(PROJECT_SUMMARY, 'Build Tool'),            // Maven
  packageBase: extraer(PROJECT_SUMMARY, 'Package Base'),        // com.cambiaso.ioc
  testingFramework: extraer(PROJECT_SUMMARY, 'Testing Backend'), // JUnit 5
  estructuraPaquetes: extraer(PROJECT_SUMMARY, 'Estructura Backend')
}

if (CONVENCIONES_BACKEND.packageBase === null) {
  CONVENCIONES_BACKEND.packageBase = "com.example.app";  // Default
}
```

---

### FASE 2: Identificación de Clases Backend

#### Acción 2.1: Identificar Clases Necesarias

```typescript
function identificarClasesBackend(td: TechnicalDesignData): BackendClass[] {
  const clases = [];
  
  // ESTRATEGIA 1: Por cada entidad, generar Service + Repository
  td.modelo.entidades.forEach(entidad => {
    // Service
    clases.push({
      nombre: `${entidad.nombre}Service`,
      tipo: 'Service',
      entidadRelacionada: entidad.nombre,
      packageSufijo: 'service',
      responsabilidad: `Lógica de negocio para ${entidad.nombre}`
    });
    
    // Repository
    clases.push({
      nombre: `${entidad.nombre}Repository`,
      tipo: 'Repository',
      entidadRelacionada: entidad.nombre,
      packageSufijo: 'repository',
      responsabilidad: `Acceso a datos para ${entidad.nombre}`
    });
  });
  
  // ESTRATEGIA 2: Por cada grupo de endpoints, generar Controller
  const endpointsPorRecurso = agruparPor(td.contratos.endpoints, 'recurso');
  
  Object.entries(endpointsPorRecurso).forEach(([recurso, endpoints]) => {
    const nombreEntidad = singularizar(capitalize(recurso));
    
    clases.push({
      nombre: `${nombreEntidad}Controller`,
      tipo: 'Controller',
      entidadRelacionada: nombreEntidad,
      packageSufijo: 'controller',
      responsabilidad: `REST API para ${recurso}`,
      endpoints: endpoints
    });
  });
  
  // ESTRATEGIA 3: WebSocket Handlers (si hay WebSocket en TD)
  if (td.contratos.websockets) {
    td.contratos.websockets.forEach(ws => {
      clases.push({
        nombre: `${ws.nombre}Handler`,
        tipo: 'WebSocketHandler',
        packageSufijo: 'websocket',
        responsabilidad: `Manejo de WebSocket para ${ws.nombre}`
      });
    });
  }
  
  // ESTRATEGIA 4: Clases mencionadas en arquitectura
  const mencionadasEnDiagrama = extraerDeArquitectura(td.arquitectura);
  clases.push(...mencionadasEnDiagrama);
  
  return deduplicar(clases);
}

interface BackendClass {
  nombre: string;                 // NotificationService
  tipo: ClassType;                // Service, Controller, Repository, etc.
  entidadRelacionada?: string;    // Notification
  packageSufijo: string;          // service, controller, repository
  responsabilidad: string;        // Descripción
  endpoints?: Endpoint[];         // Si es Controller
}

enum ClassType {
  SERVICE = 'Service',
  CONTROLLER = 'Controller',
  REPOSITORY = 'Repository',
  WEBSOCKET_HANDLER = 'WebSocketHandler',
  MAPPER = 'Mapper',
  VALIDATOR = 'Validator',
  EVENT_LISTENER = 'EventListener'
}
```

---

#### Acción 2.2: Determinar Orden de Generación

```typescript
function ordenarClases(clases: BackendClass[]): BackendClass[] {
  // Orden bottom-up (dependencias primero):
  // 1. Repositories (no dependen de nadie)
  // 2. Mappers y Validators (utilidades)
  // 3. Services (dependen de Repositories)
  // 4. Controllers (dependen de Services)
  // 5. WebSocket Handlers (dependen de Services)
  // 6. Event Listeners (dependen de Services)
  
  const orden = {
    'Repository': 1,
    'Mapper': 2,
    'Validator': 2,
    'Service': 3,
    'Controller': 4,
    'WebSocketHandler': 5,
    'EventListener': 6
  };
  
  return clases.sort((a, b) => {
    return (orden[a.tipo] || 99) - (orden[b.tipo] || 99);
  });
}
```

---

### FASE 3: Generación de BSS

#### Acción 3.1: Para Cada Clase, Generar BSS

```typescript
for (const clase of clasesOrdenadas) {
  console.log(`📝 Generando BSS para: ${clase.nombre}`);
  
  const bss = await generarBSS(clase, td, projectContext);
  
  const nombreArchivo = `bss-${kebabCase(clase.nombre)}.md`;
  const ruta = `@.gemini/blueprints/backend/${nombreArchivo}`;
  
  escribirArchivo(ruta, bss);
  
  console.log(`✅ Generado: ${ruta}`);
}
```

---

#### Acción 3.2: Generador de BSS (Función Principal)

```typescript
async function generarBSS(
  clase: BackendClass,
  td: TechnicalDesignData,
  ctx: ProjectContext
): Promise<string> {
  
  // Cargar plantilla
  const plantilla = leerArchivo('@.gemini/templates/bss-template.md');
  
  // Generar ID incremental
  const bssId = generarIdBSS();
  
  // Generar cada sección según el tipo de clase
  const datos = {
    // METADATA
    id: bssId,
    tdId: td.id,
    nombreClase: clase.nombre,
    tipo: clase.tipo,
    packageCompleto: generarPackage(clase, ctx),
    sprint: td.sprint,
    fechaCreacion: hoy(),
    
    // SECCIÓN 1: Propósito
    proposito: generarProposito(clase, td),
    responsabilidades: generarResponsabilidades(clase, td),
    ubicacionArquitectura: generarUbicacionArquitectura(clase, td),
    
    // SECCIÓN 2: Interfaz Pública
    interfazPublica: generarInterfazPublica(clase, td, ctx),
    signatureCompleta: generarSignatureCompleta(clase, td, ctx),
    
    // SECCIÓN 3: Dependencias
    inyeccionDependencias: generarInyeccionDependencias(clase, td, ctx),
    grafoDependencias: generarGrafoDependencias(clase, td),
    
    // SECCIÓN 4: Reglas de Negocio
    reglasNegocio: extraerReglasNegocio(clase, td),
    invariantes: generarInvariantes(clase, td),
    validacionesNegocio: generarValidacionesNegocio(clase, td),
    
    // SECCIÓN 5: Transacciones
    estrategiaTransacciones: generarEstrategiaTransacciones(clase, td),
    manejoRollback: generarManejoRollback(clase, td),
    
    // SECCIÓN 6: Implementación Detallada
    implementacionMetodos: generarImplementacionMetodos(clase, td, ctx),
    
    // SECCIÓN 7: Manejo de Errores
    excepcionesLanzadas: generarExcepcionesLanzadas(clase, td),
    manejoGlobal: generarManejoGlobalExcepciones(clase, td),
    loggingErrores: generarLoggingErrores(clase, td),
    
    // SECCIÓN 8: Performance
    consultasOptimizadas: generarConsultasOptimizadas(clase, td),
    caching: generarCaching(clase, td),
    batchOperations: generarBatchOperations(clase, td),
    
    // SECCIÓN 9: Testing
    testsUnitarios: generarTestsUnitarios(clase, td, ctx),
    testsIntegracion: generarTestsIntegracion(clase, td, ctx),
    coberturaObjetivo: '85%',
    
    // SECCIÓN 10: Observabilidad
    logging: generarLogging(clase, td),
    metricas: generarMetricas(clase, td),
    tracing: generarTracing(clase, td),
    
    // SECCIÓN 11: Seguridad
    validacionInput: generarValidacionInput(clase, td),
    autorizacion: generarAutorizacion(clase, td),
    prevencionMassAssignment: generarPrevencionMassAssignment(clase, td),
    
    // SECCIÓN 12: Configuración
    properties: generarProperties(clase, td),
    
    // SECCIÓN 13: Eventos
    publicarEventos: generarPublicarEventos(clase, td),
    consumirEventos: generarConsumirEventos(clase, td),
    
    // SECCIÓN 14: Checklist
    checklistImplementacion: generarChecklistImplementacion(clase, td),
    
    // SECCIÓN 15: Referencias
    referencias: generarReferencias(clase, td)
  };
  
  // Reemplazar placeholders
  let bss = plantilla;
  for (const [clave, valor] of Object.entries(datos)) {
    bss = reemplazarPlaceholder(bss, clave, valor);
  }
  
  // Validar completitud
  validarBSS(bss, clase);
  
  return bss;
}
```

---

#### Acción 3.3: Generadores de Secciones Específicas

**Interfaz Pública (Service)**:

```typescript
function generarInterfazPublica(
  clase: BackendClass, 
  td: TechnicalDesignData,
  ctx: ProjectContext
): string {
  
  if (clase.tipo !== 'Service') {
    return generarInterfazParaOtroTipo(clase, td, ctx);
  }
  
  const entidad = clase.entidadRelacionada;
  const endpoints = extraerEndpointsDeEntidad(entidad, td);
  
  const metodos = [];
  
  // Generar métodos CRUD basados en endpoints
  endpoints.forEach(endpoint => {
    if (endpoint.method === 'POST' && !endpoint.path.includes('{')) {
      // CREATE
      metodos.push({
        nombre: 'create',
        retorno: `${entidad}Response`,
        parametros: [
          { nombre: 'userId', tipo: 'UUID' },
          { nombre: 'request', tipo: `Create${entidad}Request` }
        ],
        proposito: `Crea un nuevo ${entidad}`,
        excepciones: [
          'ResourceNotFoundException si userId no existe',
          'BusinessException si viola reglas de negocio'
        ]
      });
    }
    
    if (endpoint.method === 'GET' && endpoint.path.includes('{id}')) {
      // READ ONE
      metodos.push({
        nombre: 'findById',
        retorno: `${entidad}Response`,
        parametros: [
          { nombre: 'id', tipo: 'UUID' },
          { nombre: 'userId', tipo: 'UUID' }
        ],
        proposito: `Busca un ${entidad} por ID`,
        excepciones: [
          'ResourceNotFoundException si no existe',
          'ForbiddenException si el usuario no tiene acceso'
        ]
      });
    }
    
    if (endpoint.method === 'GET' && !endpoint.path.includes('{id}')) {
      // READ LIST
      metodos.push({
        nombre: 'findAll',
        retorno: `Page<${entidad}Response>`,
        parametros: [
          { nombre: 'userId', tipo: 'UUID' },
          { nombre: 'params', tipo: `${entidad}QueryParams` }
        ],
        proposito: `Lista ${pluralizar(entidad)} con paginación y filtros`,
        excepciones: []
      });
    }
    
    if (endpoint.method === 'PATCH' || endpoint.method === 'PUT') {
      // UPDATE
      metodos.push({
        nombre: 'update',
        retorno: `${entidad}Response`,
        parametros: [
          { nombre: 'id', tipo: 'UUID' },
          { nombre: 'userId', tipo: 'UUID' },
          { nombre: 'request', tipo: `Update${entidad}Request` }
        ],
        proposito: `Actualiza un ${entidad} existente`,
        excepciones: [
          'ResourceNotFoundException si no existe',
          'ForbiddenException si el usuario no es el owner'
        ]
      });
    }
    
    if (endpoint.method === 'DELETE') {
      // DELETE
      metodos.push({
        nombre: 'delete',
        retorno: 'void',
        parametros: [
          { nombre: 'id', tipo: 'UUID' },
          { nombre: 'userId', tipo: 'UUID' }
        ],
        proposito: `Elimina un ${entidad}`,
        excepciones: [
          'ResourceNotFoundException si no existe',
          'ForbiddenException si el usuario no tiene permisos',
          'BusinessException si tiene dependencias'
        ]
      });
    }
  });
  
  // Métodos de negocio adicionales (acciones custom)
  const metodosNegocio = extraerMetodosNegocioDeEndpoints(endpoints, entidad);
  metodos.push(...metodosNegocio);
  
  // Generar código Java
  return generarCodigoInterfazService(clase.nombre, metodos, ctx);
}

function generarCodigoInterfazService(
  nombreClase: string, 
  metodos: any[],
  ctx: ProjectContext
): string {
  
  let codigo = `\`\`\`java\n`;
  codigo += `package ${ctx.packageBase}.service;\n\n`;
  codigo += `import ${ctx.packageBase}.dto.*;\n`;
  codigo += `import org.springframework.data.domain.Page;\n`;
  codigo += `import java.util.UUID;\n\n`;
  
  // Interface
  codigo += `/**\n`;
  codigo += ` * Servicio de lógica de negocio para ${nombreClase.replace('Service', '')}\n`;
  codigo += ` */\n`;
  codigo += `public interface ${nombreClase} {\n\n`;
  
  metodos.forEach((metodo, idx) => {
    // JavaDoc
    codigo += `    /**\n`;
    codigo += `     * ${metodo.proposito}\n`;
    codigo += `     *\n`;
    metodo.parametros.forEach(param => {
      codigo += `     * @param ${param.nombre} ${param.descripcion || ''}\n`;
    });
    codigo += `     * @return ${metodo.retorno}\n`;
    metodo.excepciones.forEach(exc => {
      codigo += `     * @throws ${exc}\n`;
    });
    codigo += `     */\n`;
    
    // Signature
    const params = metodo.parametros
      .map(p => `${p.tipo} ${p.nombre}`)
      .join(', ');
    
    codigo += `    ${metodo.retorno} ${metodo.nombre}(${params});\n`;
    
    if (idx < metodos.length - 1) codigo += `\n`;
  });
  
  codigo += `}\n`;
  codigo += `\`\`\`\n`;
  
  return codigo;
}
```

---

**Implementación de Métodos**:

```typescript
function generarImplementacionMetodos(
  clase: BackendClass,
  td: TechnicalDesignData,
  ctx: ProjectContext
): string {
  
  if (clase.tipo !== 'Service') {
    return generarImplementacionOtroTipo(clase, td, ctx);
  }
  
  const entidad = clase.entidadRelacionada;
  const endpoints = extraerEndpointsDeEntidad(entidad, td);
  
  let implementaciones = '';
  
  // Generar implementación para cada método CRUD
  
  // CREATE
  if (endpoints.some(e => e.method === 'POST')) {
    implementaciones += generarMetodoCreate(entidad, td, ctx);
    implementaciones += '\n\n---\n\n';
  }
  
  // READ ONE
  if (endpoints.some(e => e.method === 'GET' && e.path.includes('{id}'))) {
    implementaciones += generarMetodoFindById(entidad, td, ctx);
    implementaciones += '\n\n---\n\n';
  }
  
  // READ LIST
  if (endpoints.some(e => e.method === 'GET' && !e.path.includes('{id}'))) {
    implementaciones += generarMetodoFindAll(entidad, td, ctx);
    implementaciones += '\n\n---\n\n';
  }
  
  // UPDATE
  if (endpoints.some(e => e.method === 'PATCH' || e.method === 'PUT')) {
    implementaciones += generarMetodoUpdate(entidad, td, ctx);
    implementaciones += '\n\n---\n\n';
  }
  
  // DELETE
  if (endpoints.some(e => e.method === 'DELETE')) {
    implementaciones += generarMetodoDelete(entidad, td, ctx);
  }
  
  return implementaciones;
}

function generarMetodoCreate(
  entidad: string,
  td: TechnicalDesignData,
  ctx: ProjectContext
): string {
  
  const reglasNegocio = extraerReglasNegocioDeEntidad(entidad, td);
  const camposEntidad = extraerCamposEntidad(entidad, td);
  
  return `
### 6.1. Método: create()

\`\`\`java
/**
 * Crea un nuevo ${entidad}
 * 
 * @param userId ID del usuario que crea el recurso
 * @param request Datos del nuevo ${entidad}
 * @return ${entidad} creado con ID asignado
 * @throws ResourceNotFoundException si userId no existe
 * @throws BusinessException si viola reglas de negocio
 */
@Transactional
public ${entidad}Response create(UUID userId, Create${entidad}Request request) {
    log.info("Creating ${entidad} for user: {}", userId);
    
    // 1. Validar que el usuario existe
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    
    // 2. Construir entidad desde el request
    ${entidad} entity = new ${entidad}();
    entity.setUser(user);
${camposEntidad.map(c => `    entity.set${capitalize(c.nombre)}(request.get${capitalize(c.nombre)}());`).join('\n')}
    entity.setStatus(${entidad}Status.PENDING);  // Estado inicial
    
    // 3. Validar reglas de negocio
    validateBusinessRules(entity, user);
    
    // 4. Persistir
    ${entidad} saved = repository.save(entity);
    log.debug("${entidad} created with ID: {}", saved.getId());
    
    // 5. Publicar evento de dominio
    eventPublisher.publishEvent(new ${entidad}CreatedEvent(saved.getId()));
    
    // 6. Convertir a DTO y retornar
    return mapper.toResponse(saved);
}

/**
 * Valida reglas de negocio antes de crear
 */
private void validateBusinessRules(${entidad} entity, User user) {
${reglasNegocio.map(rn => `    // ${rn.id}: ${rn.descripcion}
    if (${rn.condicion}) {
        throw new BusinessException("${rn.mensajeError}");
    }`).join('\n\n')}
}
\`\`\`
  `.trim();
}

function generarMetodoFindById(
  entidad: string,
  td: TechnicalDesignData,
  ctx: ProjectContext
): string {
  
  return `
### 6.2. Método: findById()

\`\`\`java
/**
 * Busca un ${entidad} por ID
 * 
 * @param id ID del ${entidad}
 * @param userId ID del usuario que hace la consulta
 * @return ${entidad} encontrado
 * @throws ResourceNotFoundException si no existe
 * @throws ForbiddenException si el usuario no tiene acceso
 */
@Transactional(readOnly = true)
public ${entidad}Response findById(UUID id, UUID userId) {
    log.debug("Finding ${entidad} {} for user {}", id, userId);
    
    // 1. Buscar entidad
    ${entidad} entity = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("${entidad} no encontrado"));
    
    // 2. Validar ownership (solo puede ver los suyos, excepto admins)
    if (!entity.getUserId().equals(userId) && !isAdmin(userId)) {
        log.warn("User {} tried to access ${entidad} {} (not owner)", userId, id);
        throw new ForbiddenException("No tienes acceso a este recurso");
    }
    
    // 3. Convertir a DTO y retornar
    return mapper.toResponse(entity);
}

private boolean isAdmin(UUID userId) {
    return userRepository.findById(userId)
        .map(User::isAdmin)
        .orElse(false);
}
\`\`\`
  `.trim();
}
```

---

**Tests Unitarios**:

```typescript
function generarTestsUnitarios(
  clase: BackendClass,
  td: TechnicalDesignData,
  ctx: ProjectContext
): string {
  
  if (clase.tipo !== 'Service') {
    return generarTestsOtroTipo(clase, td, ctx);
  }
  
  const entidad = clase.entidadRelacionada;
  const metodos = extraerMetodosDeClase(clase, td);
  
  let tests = `\`\`\`java\n`;
  tests += `@ExtendWith(MockitoExtension.class)\n`;
  tests += `class ${clase.nombre}Test {\n\n`;
  
  // Mocks
  tests += `    @Mock\n`;
  tests += `    private ${entidad}Repository repository;\n\n`;
  tests += `    @Mock\n`;
  tests += `    private UserRepository userRepository;\n\n`;
  tests += `    @Mock\n`;
  tests += `    private ApplicationEventPublisher eventPublisher;\n\n`;
  tests += `    @InjectMocks\n`;
  tests += `    private ${clase.nombre}Impl service;\n\n`;
  
  // Setup
  tests += `    private User mockUser;\n`;
  tests += `    private ${entidad} mock${entidad};\n\n`;
  tests += `    @BeforeEach\n`;
  tests += `    void setUp() {\n`;
  tests += `        mockUser = new User();\n`;
  tests += `        mockUser.setId(UUID.randomUUID());\n`;
  tests += `        mockUser.setEmail("test@example.com");\n\n`;
  tests += `        mock${entidad} = new ${entidad}();\n`;
  tests += `        mock${entidad}.setId(UUID.randomUUID());\n`;
  tests += `        mock${entidad}.setUser(mockUser);\n`;
  tests += `    }\n\n`;
  
  // Test cases
  tests += generarTestCreate(entidad);
  tests += generarTestCreateUserNotFound(entidad);
  tests += generarTestFindByIdSuccess(entidad);
  tests += generarTestFindByIdNotOwner(entidad);
  tests += generarTestDelete(entidad);
  
  tests += `}\n`;
  tests += `\`\`\`\n`;
  
  return tests;
}

function generarTestCreate(entidad: string): string {
  return `
    @Test
    void create_ValidRequest_ReturnsEntity() {
        // Arrange
        UUID userId = mockUser.getId();
        Create${entidad}Request request = new Create${entidad}Request();
        request.setCampo1("test");
        request.setCampo2(42);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(repository.save(any(${entidad}.class))).thenReturn(mock${entidad});
        
        // Act
        ${entidad}Response response = service.create(userId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals("test", response.getCampo1());
        
        verify(userRepository).findById(userId);
        verify(repository).save(any(${entidad}.class));
        verify(eventPublisher).publishEvent(any(${entidad}CreatedEvent.class));
    }
`;
}

function generarTestCreateUserNotFound(entidad: string): string {
  return `
    @Test
    void create_UserNotFound_ThrowsException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Create${entidad}Request request = new Create${entidad}Request();
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            service.create(userId, request);
        });
        
        verify(repository, never()).save(any());
    }
`;
}
```

---

### FASE 4: Validación y Post-procesamiento

#### Acción 4.1: Validar Cada BSS Generada

```typescript
function validarBSS(bss: string, clase: BackendClass): void {
  const validaciones = [
    {
      nombre: 'Tiene interfaz pública',
      check: () => bss.includes('## 2. Interfaz Pública'),
      severidad: 'ERROR'
    },
    {
      nombre: 'Tiene código Java sintácticamente válido',
      check: () => validarSintaxisJava(bss),
      severidad: 'ERROR'
    },
    {
      nombre: 'Tiene implementación de al menos 1 método',
      check: () => bss.includes('### 6.1. Método:'),
      severidad: 'ERROR'
    },
    {
      nombre: 'Tiene tests unitarios',
      check: () => bss.includes('### 9.1. Tests Unitarios'),
      severidad: 'WARNING'
    },
    {
      nombre: 'Tiene manejo de transacciones (si es Service)',
      check: () => clase.tipo !== 'Service' || bss.includes('@Transactional'),
      severidad: 'ERROR'
    },
    {
      nombre: 'Tiene logging',
      check: () => bss.includes('log.info') || bss.includes('log.debug'),
      severidad: 'WARNING'
    },
    {
      nombre: 'No tiene placeholders sin llenar',
      check: () => !bss.includes('[PENDIENTE]') && !bss.includes('[TODO]'),
      severidad: 'WARNING'
    }
  ];
  
  const fallos = validaciones.filter(v => !v.check());
  
  if (fallos.some(f => f.severidad === 'ERROR')) {
    throw new Error(`
      BSS inválida para ${clase.nombre}:
      ${fallos.filter(f => f.severidad === 'ERROR').map(f => '- ' + f.nombre).join('\n')}
    `);
  }
  
  if (fallos.some(f => f.severidad === 'WARNING')) {
    console.warn(`
      ⚠️ Advertencias en BSS de ${clase.nombre}:
      ${fallos.filter(f => f.severidad === 'WARNING').map(f => '- ' + f.nombre).join('\n')}
    `);
  }
}
```

---

#### Acción 4.2: Generar Índice de BSS

```typescript
function generarIndiceBSS(clases: BackendClass[], bsss: string[]): string {
  let indice = `# Índice de Backend Service Specifications - ${td.nombre}\n\n`;
  indice += `**Technical Design**: \`${td.id}-${kebabCase(td.nombre)}.md\`\n`;
  indice += `**Sprint**: ${td.sprint}\n`;
  indice += `**Fecha de Generación**: ${hoy()}\n\n`;
  indice += `---\n\n`;
  indice += `## Clases Generadas (${clases.length})\n\n`;
  
  // Agrupar por tipo
  const porTipo = agruparPor(clases, 'tipo');
  
  Object.entries(porTipo).forEach(([tipo, clasesDelTipo]) => {
    indice += `### ${tipo}s (${clasesDelTipo.length})\n\n`;
    indice += `| Clase | Archivo BSS | Responsabilidad |\n`;
    indice += `|-------|-------------|------------------|\n`;
    
    clasesDelTipo.forEach(clase => {
      const archivo = `bss-${kebabCase(clase.nombre)}.md`;
      indice += `| \`${clase.nombre}\` | [${archivo}](./${archivo}) | ${clase.responsabilidad} |\n`;
    });
    
    indice += `\n`;
  });
  
  indice += `---\n\n`;
  indice += `## Estructura de Paquetes\n\n`;
  indice += `\`\`\`\n${generarEstructuraPaquetes(clases, ctx)}\`\`\`\n\n`;
  
  indice += `---\n\n`;
  indice += `## Diagrama de Dependencias\n\n`;
  indice += `\`\`\`\n${generarDiagramaDependencias(clases)}\`\`\`\n\n`;
  
  indice += `---\n\n`;
  indice += `## Próximos Pasos\n\n`;
  indice += `1. ✅ Revisar cada BSS generada\n`;
  indice += `2. ✅ Validar reglas de negocio\n`;
  indice += `3. ✅ Implementar clases en orden (Repositories → Services → Controllers)\n`;
  indice += `4. ✅ Ejecutar tests\n`;
  indice += `5. ✅ Code review\n\n`;
  
  indice += `---\n\n`;
  indice += `**Generado por**: Backend Service Specification Generator v1\n`;
  indice += `**Fecha**: ${hoy()}\n`;
  
  return indice;
}

function generarEstructuraPaquetes(clases: BackendClass[], ctx: ProjectContext): string {
  return `
${ctx.packageBase}/
├── controller/
${clases.filter(c => c.tipo === 'Controller').map(c => `│   └── ${c.nombre}.java`).join('\n')}
├── service/
${clases.filter(c => c.tipo === 'Service').map(c => `│   ├── ${c.nombre}.java (interface)\n│   └── ${c.nombre}Impl.java`).join('\n')}
├── repository/
${clases.filter(c => c.tipo === 'Repository').map(c => `│   └── ${c.nombre}.java`).join('\n')}
├── websocket/
${clases.filter(c => c.tipo === 'WebSocketHandler').map(c => `│   └── ${c.nombre}.java`).join('\n')}
└── event/
${clases.filter(c => c.tipo === 'EventListener').map(c => `    └── ${c.nombre}.java`).join('\n')}
  `.trim();
}
```

---

### FASE 5: Output Final

```markdown
✅ BACKEND SERVICE SPECIFICATIONS GENERADAS EXITOSAMENTE

**Technical Design**: TD-005-real-time-notifications.md

---

## 📋 Clases Generadas (7)

### Services (2)
- `NotificationService` → `bss-notification-service.md`
- `NotificationWebSocketService` → `bss-notification-websocket-service.md`

### Controllers (1)
- `NotificationController` → `bss-notification-controller.md`

### Repositories (2)
- `NotificationRepository` → `bss-notification-repository.md`
- `UserNotificationPreferencesRepository` → `bss-user-notification-preferences-repository.md`

### WebSocket Handlers (1)
- `NotificationWebSocketHandler` → `bss-notification-websocket-handler.md`

### Event Listeners (1)
- `ETLCompletedListener` → `bss-etl-completed-listener.md`

---

## 📁 Archivos Generados

```
@.gemini/blueprints/backend/
├── bss-notification-service.md
├── bss-notification-controller.md
├── bss-notification-repository.md
├── bss-notification-websocket-handler.md
├── bss-notification-websocket-service.md
├── bss-user-notification-preferences-repository.md
├── bss-etl-completed-listener.md
└── INDEX.md
```

---

## 🏗️ Estructura de Paquetes

```
com.cambiaso.ioc/
├── controller/
│   └── NotificationController.java
├── service/
│   ├── NotificationService.java (interface)
│   ├── NotificationServiceImpl.java
│   ├── NotificationWebSocketService.java
│   └── NotificationWebSocketServiceImpl.java
├── repository/
│   ├── NotificationRepository.java
│   └── UserNotificationPreferencesRepository.java
└── websocket/
    └── NotificationWebSocketHandler.java
```

---

## 🔗 Diagrama de Dependencias

```
NotificationController
    ↓
NotificationService
    ├─ NotificationRepository
    ├─ UserRepository
    ├─ NotificationWebSocketService
    └─ ApplicationEventPublisher

NotificationWebSocketHandler
    ↓
NotificationWebSocketService
    └─ NotificationRepository

ETLCompletedListener
    ↓
NotificationService
```

---

## 📊 Estadísticas

**Clases**: 7
- Services: 2
- Controllers: 1
- Repositories: 2
- WebSocket Handlers: 1
- Event Listeners: 1

**Líneas de código de ejemplo**: ~3,200 líneas Java

**Tests generados**: 45 casos
- Unitarios: 35
- Integración: 10

**Endpoints documentados**: 3 REST + 1 WebSocket

**Reglas de negocio**: 12 validaciones implementadas

---

## 🎯 Próximos Pasos

### 1. Revisar BSS Generadas

```bash
# Abrir cada BSS y validar:
code @.gemini/blueprints/backend/bss-notification-service.md
code @.gemini/blueprints/backend/bss-notification-controller.md
# ...

# Checklist de Revisión:
- [ ] Métodos públicos son los correctos
- [ ] Reglas de negocio están completas
- [ ] Transacciones están bien configuradas
- [ ] Tests cubren casos importantes
- [ ] Manejo de errores es robusto
```

### 2. Implementar en Orden

**Orden Sugerido** (bottom-up):

```bash
# 1. Repositories primero (sin dependencias)
src/main/java/com/cambiaso/ioc/repository/NotificationRepository.java
src/main/java/com/cambiaso/ioc/repository/UserNotificationPreferencesRepository.java

# 2. Services (dependen de Repositories)
src/main/java/com/cambiaso/ioc/service/NotificationService.java
src/main/java/com/cambiaso/ioc/service/NotificationServiceImpl.java

# 3. Controllers y WebSocket (dependen de Services)
src/main/java/com/cambiaso/ioc/controller/NotificationController.java
src/main/java/com/cambiaso/ioc/websocket/NotificationWebSocketHandler.java

# 4. Event Listeners (dependen de Services)
src/main/java/com/cambiaso/ioc/event/ETLCompletedListener.java
```

**Para cada clase**:
1. Crear archivo en package correcto
2. Copiar signature de la BSS
3. Implementar métodos según BSS
4. Escribir tests según casos en BSS
5. Ejecutar tests
6. Code review

---

### 3. Ejecutar Tests

```bash
# Tests unitarios
mvn test

# Tests de integración
mvn verify

# Coverage report
mvn test jacoco:report
open target/site/jacoco/index.html
```

---

### 4. Generar Frontend Blueprints

```bash
# Ahora que el backend está especificado, generar FTVs:
gemini-cli < @.gemini/prompts/07-generate-blueprint-ftv.md \
  --technical-design=TD-005
```

---

### 5. Consolidar Contratos API

```bash
# Generar Backend Sync Brief (consolida todos los contratos):
gemini-cli < @.gemini/prompts/04-generate-backend-sync-brief.md
```

---

## ⚠️ Notas Importantes

### Inferencias Realizadas

**Reglas de Negocio**:
- Usuario solo puede marcar como leídas sus propias notificaciones → Inferido de mejores prácticas
- Máximo 1000 notificaciones por usuario → Inferido como límite razonable

**Transacciones**:
- Métodos de lectura con `readOnly=true` → Inferido por optimización
- Crear notificación en nueva transacción → Inferido para garantizar persistencia

**Eventos**:
- Publicar `NotificationCreatedEvent` después de crear → Inferido del patrón Event-Driven

### Decisiones Técnicas

**Performance**:
- Cache de 1 minuto en lista de notificaciones → Basado en que cambian frecuentemente
- JOIN FETCH en consultas → Prevenir N+1 queries

**Seguridad**:
- Validación de ownership en todos los métodos → Basado en mejores prácticas
- Sanitización de inputs → Prevenir XSS/SQL injection

---

## 📚 Referencias

- **Technical Design**: `@.gemini/sprints/technical-designs/TD-005-real-time-notifications.md`
- **Feature Plan**: `@.gemini/sprints/feature-plans/FP-005-real-time-notifications.md`
- **Frontend Blueprints**: `@.gemini/blueprints/ftv-*.md` (pendiente)
- **Backend Sync Brief**: Pendiente (generar después de FTVs)

---

**Generado por**: Backend Service Specification Generator v1  
**Tiempo total**: 3 minutos  
**Fecha**: 2024-01-15 13:00:00
```

---

## 4. REGLAS DE CALIDAD

### Regla 1: Código Java Debe Compilar

```java
// ✅ Generar código sintácticamente correcto
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    // Código completo y ejecutable
}

// ❌ Evitar código incompleto o con errores
public class NotificationService {
    public void create(  // Falta cerrar paréntesis, falta tipo retorno
}
```

---

### Regla 2: Tests Deben Ser Ejecutables

```java
// ✅ BIEN: Test completo y ejecutable
@Test
void create_ValidRequest_ReturnsNotification() {
    // Arrange con mocks configurados
    when(repository.save(any())).thenReturn(mockEntity);
    
    // Act
    var result = service.create(userId, request);
    
    // Assert
    assertNotNull(result);
    verify(repository).save(any());
}

// ❌ MAL: Test incompleto
@Test
void testCreate() {
    // TODO: implementar
}
```

---

### Regla 3: Excepciones Consistentes

Todas las BSS deben usar las mismas excepciones:
- `ResourceNotFoundException` para 404
- `ForbiddenException` para 403
- `BusinessException` para validaciones de negocio
- `ValidationException` para validación de datos

---

### Regla 4: Logging Consistente

```java
// ✅ BIEN: Logging estructurado
log.info("Creating notification for user: {}", userId);
log.debug("Notification data: type={}, message={}", type, message);
log.error("Error creating notification: {}", e.getMessage(), e);

// ❌ MAL: Logging inconsistente
System.out.println("Creating notification");
log.info("Error: " + e);  // Concatenación en lugar de placeholders
```

---

