# 📄 PROMPT 8: Backend Service Specification (BSS) Generator

---

## PARTE 1: Plantilla de Backend Service Specification (BSS)

**Archivo**: `@.gemini/templates/bss-template.md`

```markdown
# Backend Service Specification: [NombreClase]

## Metadata
- **ID**: BSS-[XXX]
- **Technical Design**: [TD-XXX-nombre.md]
- **Clase**: `[NombreClase]`
- **Tipo**: [Service | Controller | Repository | WebSocket Handler | Scheduler | Event Listener]
- **Package**: `com.[empresa].[proyecto].[modulo]`
- **Sprint**: Sprint [X]
- **Estado**: 
  - [ ] Draft
  - [ ] En Revisión
  - [ ] Aprobado
  - [ ] Implementado
- **Autor**: [Nombre o IA]
- **Fecha Creación**: [YYYY-MM-DD]
- **Última Actualización**: [YYYY-MM-DD]

---

## 1. Propósito y Responsabilidad

### 1.1. Descripción

[Descripción de qué hace esta clase y por qué existe en la arquitectura]

**Responsabilidad Principal**:
- [Responsabilidad 1]
- [Responsabilidad 2]
- [Responsabilidad 3]

**NO es responsable de** (anti-patterns a evitar):
- ❌ [Cosa que NO debe hacer]
- ❌ [Otra cosa fuera de scope]

---

### 1.2. Ubicación en la Arquitectura

```
[Cliente/Frontend]
      ↓
[Controller Layer] ← (Si esta clase es Controller)
      ↓
[Service Layer]    ← (Si esta clase es Service)
      ↓
[Repository Layer] ← (Si esta clase es Repository)
      ↓
[Database/External Service]
```

**Capa**: [Presentation | Business Logic | Data Access | Infrastructure]

---

## 2. Interfaz Pública

### 2.1. Métodos Públicos

```java
package com.[empresa].[proyecto].[modulo];

import [imports necesarios];

/**
 * [Descripción de la clase]
 * 
 * @author [Nombre]
 * @since [Versión]
 */
@Service  // o @RestController, @Repository, etc.
public class [NombreClase] {
    
    /**
     * [Descripción del método]
     * 
     * @param param1 [descripción]
     * @param param2 [descripción]
     * @return [descripción del retorno]
     * @throws [ExceptionType] cuando [condición]
     */
    public [ReturnType] metodo1([Param1Type] param1, [Param2Type] param2) {
        // Implementación en sección 6
    }
    
    /**
     * [Descripción del método]
     * 
     * @param param [descripción]
     * @return [descripción del retorno]
     */
    public [ReturnType] metodo2([ParamType] param) {
        // Implementación en sección 6
    }
    
    // ... más métodos
}
```

---

### 2.2. Signature Completa (si es Service)

```java
public interface [NombreClase]Service {
    
    // CREATE
    [Entity]Response create(UUID userId, Create[Entity]Request request);
    
    // READ
    [Entity]Response findById(UUID id, UUID userId);
    Page<[Entity]Response> findAll(UUID userId, [Entity]QueryParams params);
    
    // UPDATE
    [Entity]Response update(UUID id, UUID userId, Update[Entity]Request request);
    
    // DELETE
    void delete(UUID id, UUID userId);
    
    // BUSINESS METHODS
    void [businessMethod](UUID id, UUID userId);
}

@Service
@Transactional
public class [NombreClase]ServiceImpl implements [NombreClase]Service {
    // Implementación
}
```

---

### 2.3. Endpoints (si es Controller)

```java
@RestController
@RequestMapping("/api/v1/[recurso]")
@RateLimiter(name = "[recurso]")
public class [NombreClase]Controller {
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<[Entity]Response> create(
        @Valid @RequestBody Create[Entity]Request request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        // Implementación
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<[Entity]Response> findById(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        // Implementación
    }
    
    // ... más endpoints
}
```

---

## 3. Dependencias

### 3.1. Inyección de Dependencias

```java
@Service
@RequiredArgsConstructor  // Lombok: genera constructor con final fields
public class [NombreClase]Service {
    
    // Repositorios
    private final [Entity]Repository repository;
    private final UserRepository userRepository;
    
    // Otros servicios
    private final [Otro]Service otroService;
    
    // Utilidades
    private final [Mapper] mapper;
    
    // Configuración
    @Value("${app.config.maxItems}")
    private int maxItems;
    
    // Event publishers (si aplica)
    private final ApplicationEventPublisher eventPublisher;
}
```

---

### 3.2. Grafo de Dependencias

```
[NombreClase]Service
    ├─ [Entity]Repository (data access)
    ├─ UserRepository (validación de ownership)
    ├─ [Otro]Service (lógica relacionada)
    ├─ [Mapper] (Entity ↔ DTO)
    └─ ApplicationEventPublisher (eventos de dominio)
```

**Inyecciones Circulares**: ❌ Ninguna (validado)

---

## 4. Reglas de Negocio

### 4.1. Reglas Implementadas

| ID | Regla | Validación | Excepción si Falla |
|----|-------|------------|-------------------|
| RN-1 | Usuario solo puede acceder a sus propios recursos | `entity.getUserId().equals(userId)` | `ForbiddenException` |
| RN-2 | No se puede eliminar si tiene dependencias | `repository.hasDependencies(id)` | `BusinessException` |
| RN-3 | [Descripción de regla] | [Cómo se valida] | [Excepción] |

---

### 4.2. Invariantes del Dominio

**Invariantes que esta clase DEBE mantener**:
- [ ] [Invariante 1]: [Descripción]
- [ ] [Invariante 2]: [Descripción]

**Ejemplo**:
```java
// Invariante: Un [Entity] siempre tiene un owner válido
private void validateInvariant([Entity] entity) {
    if (entity.getUser() == null) {
        throw new IllegalStateException("Entity must have an owner");
    }
}
```

---

### 4.3. Validaciones de Negocio

```java
/**
 * Valida reglas de negocio antes de crear/actualizar
 */
private void validateBusinessRules([Entity] entity, User user) {
    // RN-1: Validar ownership
    if (!entity.getUserId().equals(user.getId()) && !user.isAdmin()) {
        throw new ForbiddenException("No tienes permisos sobre este recurso");
    }
    
    // RN-2: Validar límites
    if (entity.getCampo2() > maxItems) {
        throw new BusinessException("El campo2 no puede exceder " + maxItems);
    }
    
    // RN-3: Validar estado
    if (entity.getStatus() == Status.COMPLETED && !entity.isValid()) {
        throw new BusinessException("No se puede marcar como completado si tiene errores");
    }
    
    // RN-4: Validaciones custom del dominio
    // ...
}
```

---

## 5. Transacciones

### 5.1. Estrategia de Transacciones

```java
@Service
@Transactional  // Default: propagation=REQUIRED, readOnly=false
public class [NombreClase]Service {
    
    // READ: Transacción read-only (optimización)
    @Transactional(readOnly = true)
    public [Entity]Response findById(UUID id, UUID userId) {
        // Solo lectura, no modifica datos
    }
    
    // WRITE: Transacción de escritura (default)
    @Transactional
    public [Entity]Response create(UUID userId, Create[Entity]Request request) {
        // Modificación de datos
        // Si lanza excepción, rollback automático
    }
    
    // REQUIRED_NEW: Nueva transacción (independiente de la externa)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void audit(UUID userId, String action) {
        // Se persiste aunque la transacción externa falle
    }
}
```

---

### 5.2. Manejo de Rollback

**Rollback automático en**:
- RuntimeException (cualquiera)
- Error (cualquiera)

**NO hace rollback en**:
- Checked exceptions (a menos que se configure)

**Configuración Explícita**:
```java
@Transactional(rollbackFor = Exception.class)  // Rollback en cualquier Exception
public void metodo() throws Exception {
    // ...
}

@Transactional(noRollbackFor = CustomException.class)  // NO rollback en CustomException
public void metodo() {
    // ...
}
```

---

## 6. Implementación Detallada

### 6.1. Método: create()

```java
/**
 * Crea un nuevo [Entity]
 * 
 * @param userId ID del usuario que crea el recurso
 * @param request Datos del nuevo [Entity]
 * @return [Entity] creado con ID asignado
 * @throws ResourceNotFoundException si userId no existe
 * @throws BusinessException si viola reglas de negocio
 */
@Transactional
public [Entity]Response create(UUID userId, Create[Entity]Request request) {
    log.info("Creating [Entity] for user: {}", userId);
    
    // 1. Validar que el usuario existe
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    
    // 2. Construir entidad desde el request
    [Entity] entity = new [Entity]();
    entity.setUser(user);
    entity.setCampo1(request.getCampo1());
    entity.setCampo2(request.getCampo2());
    entity.setStatus(Status.PENDING);  // Estado inicial
    
    // 3. Validar reglas de negocio
    validateBusinessRules(entity, user);
    
    // 4. Persistir
    [Entity] saved = repository.save(entity);
    log.debug("[Entity] created with ID: {}", saved.getId());
    
    // 5. Publicar evento de dominio (si aplica)
    eventPublisher.publishEvent(new [Entity]CreatedEvent(saved.getId()));
    
    // 6. Convertir a DTO y retornar
    return mapper.toResponse(saved);
}
```

---

### 6.2. Método: findById()

```java
/**
 * Busca un [Entity] por ID
 * 
 * @param id ID del [Entity]
 * @param userId ID del usuario que hace la consulta
 * @return [Entity] encontrado
 * @throws ResourceNotFoundException si no existe
 * @throws ForbiddenException si el usuario no tiene acceso
 */
@Transactional(readOnly = true)
public [Entity]Response findById(UUID id, UUID userId) {
    log.debug("Finding [Entity] {} for user {}", id, userId);
    
    // 1. Buscar entidad
    [Entity] entity = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("[Entity] no encontrado"));
    
    // 2. Validar ownership (solo puede ver los suyos, excepto admins)
    if (!entity.getUserId().equals(userId) && !isAdmin(userId)) {
        log.warn("User {} tried to access [Entity] {} (not owner)", userId, id);
        throw new ForbiddenException("No tienes acceso a este recurso");
    }
    
    // 3. Convertir a DTO y retornar
    return mapper.toResponse(entity);
}

private boolean isAdmin(UUID userId) {
    // Verificar si el usuario tiene rol ADMIN
    return userRepository.findById(userId)
        .map(User::isAdmin)
        .orElse(false);
}
```

---

### 6.3. Método: findAll() con Paginación

```java
/**
 * Lista [Entity]s del usuario con paginación y filtros
 * 
 * @param userId ID del usuario
 * @param params Parámetros de consulta (page, size, filters)
 * @return Página de [Entity]s
 */
@Transactional(readOnly = true)
public Page<[Entity]Response> findAll(UUID userId, [Entity]QueryParams params) {
    log.debug("Listing [Entity]s for user {} with params: {}", userId, params);
    
    // 1. Construir paginación
    Sort sort = params.getOrder().equals("asc") 
        ? Sort.by(params.getSortBy()).ascending()
        : Sort.by(params.getSortBy()).descending();
    
    Pageable pageable = PageRequest.of(params.getPage(), params.getSize(), sort);
    
    // 2. Construir specifications dinámicas
    Specification<[Entity]> spec = Specification.where(
        [Entity]Specifications.belongsToUser(userId)
    );
    
    if (params.getStatus() != null) {
        spec = spec.and([Entity]Specifications.hasStatus(params.getStatus()));
    }
    
    if (params.getSearch() != null && !params.getSearch().isBlank()) {
        spec = spec.and([Entity]Specifications.searchInCampo1(params.getSearch()));
    }
    
    // 3. Ejecutar query con JOIN FETCH para evitar N+1
    Page<[Entity]> page = repository.findAll(spec, pageable);
    
    // 4. Convertir a DTOs
    return page.map(mapper::toResponse);
}
```

---

### 6.4. Método: update()

```java
/**
 * Actualiza un [Entity] existente
 * 
 * @param id ID del [Entity] a actualizar
 * @param userId ID del usuario que actualiza
 * @param request Datos a actualizar
 * @return [Entity] actualizado
 * @throws ResourceNotFoundException si no existe
 * @throws ForbiddenException si el usuario no es el owner
 */
@Transactional
public [Entity]Response update(UUID id, UUID userId, Update[Entity]Request request) {
    log.info("Updating [Entity] {} by user {}", id, userId);
    
    // 1. Buscar entidad
    [Entity] entity = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("[Entity] no encontrado"));
    
    // 2. Validar ownership
    if (!entity.getUserId().equals(userId) && !isAdmin(userId)) {
        throw new ForbiddenException("No puedes modificar este recurso");
    }
    
    // 3. Aplicar cambios (solo campos no null del request)
    if (request.getCampo1() != null) {
        entity.setCampo1(request.getCampo1());
    }
    
    if (request.getCampo2() != null) {
        entity.setCampo2(request.getCampo2());
    }
    
    if (request.getStatus() != null) {
        // Validar transición de estado
        validateStatusTransition(entity.getStatus(), request.getStatus());
        entity.setStatus(request.getStatus());
    }
    
    // 4. Validar reglas de negocio
    validateBusinessRules(entity, entity.getUser());
    
    // 5. Persistir (Hibernate detecta cambios automáticamente)
    // No hace falta save() si la entidad está managed
    [Entity] updated = repository.save(entity);
    
    // 6. Publicar evento
    eventPublisher.publishEvent(new [Entity]UpdatedEvent(updated.getId()));
    
    return mapper.toResponse(updated);
}

private void validateStatusTransition(Status current, Status target) {
    // Validar transiciones permitidas
    if (current == Status.COMPLETED && target != Status.COMPLETED) {
        throw new BusinessException("No se puede cambiar el estado de un [Entity] completado");
    }
}
```

---

### 6.5. Método: delete()

```java
/**
 * Elimina un [Entity] (soft delete o hard delete según lógica de negocio)
 * 
 * @param id ID del [Entity] a eliminar
 * @param userId ID del usuario que elimina
 * @throws ResourceNotFoundException si no existe
 * @throws ForbiddenException si el usuario no tiene permisos
 * @throws BusinessException si tiene dependencias
 */
@Transactional
public void delete(UUID id, UUID userId) {
    log.info("Deleting [Entity] {} by user {}", id, userId);
    
    // 1. Buscar entidad
    [Entity] entity = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("[Entity] no encontrado"));
    
    // 2. Solo ADMINs pueden eliminar (ejemplo de regla)
    if (!isAdmin(userId)) {
        throw new ForbiddenException("Solo administradores pueden eliminar");
    }
    
    // 3. Validar que no tenga dependencias
    if (hasDependencies(entity)) {
        throw new BusinessException(
            "No se puede eliminar porque tiene [dependencias] asociadas"
        );
    }
    
    // 4. Soft delete (marcar como eliminado) o Hard delete
    // Opción A: Soft delete
    entity.setDeleted(true);
    entity.setDeletedAt(LocalDateTime.now());
    repository.save(entity);
    
    // Opción B: Hard delete
    // repository.delete(entity);
    
    log.info("[Entity] {} deleted successfully", id);
}

private boolean hasDependencies([Entity] entity) {
    // Verificar si tiene registros relacionados
    return relatedRepository.countByEntityId(entity.getId()) > 0;
}
```

---

## 7. Manejo de Errores

### 7.1. Excepciones Lanzadas

| Excepción | Cuándo | HTTP Status | Mensaje |
|-----------|--------|-------------|---------|
| `ResourceNotFoundException` | Entity no encontrado por ID | 404 | "[Entity] no encontrado" |
| `ForbiddenException` | Usuario sin permisos | 403 | "No tienes acceso a este recurso" |
| `BusinessException` | Violación de regla de negocio | 400 | Mensaje específico de la regla |
| `ValidationException` | Datos inválidos | 400 | Detalles de validación |
| `ConflictException` | Estado inconsistente | 409 | "El recurso ya existe" |

---

### 7.2. Manejo Global de Excepciones

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
            ));
    }
    
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value(),
                LocalDateTime.now()
            ));
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
            ));
    }
}
```

---

### 7.3. Logging de Errores

```java
@Slf4j
public class [NombreClase]Service {
    
    public [Entity]Response create(UUID userId, Create[Entity]Request request) {
        try {
            log.info("Creating [Entity] for user: {}", userId);
            // ... lógica
            return response;
        } catch (BusinessException e) {
            log.warn("Business rule violation in create: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating [Entity] for user {}: {}", 
                userId, e.getMessage(), e);
            throw new InternalServerException("Error inesperado");
        }
    }
}
```

---

## 8. Performance y Optimización

### 8.1. Consultas Optimizadas

**Problema N+1**:
```java
// ❌ MAL: N+1 queries
public List<[Entity]Response> findAll(UUID userId) {
    List<[Entity]> entities = repository.findByUserId(userId);  // 1 query
    
    return entities.stream()
        .map(e -> {
            User user = e.getUser();  // N queries adicionales!
            return toResponse(e);
        })
        .collect(Collectors.toList());
}

// ✅ BIEN: JOIN FETCH
@Query("SELECT e FROM [Entity] e JOIN FETCH e.user WHERE e.user.id = :userId")
List<[Entity]> findByUserIdWithUser(@Param("userId") UUID userId);  // 1 query total
```

---

### 8.2. Caching

```java
@Service
public class [NombreClase]Service {
    
    // Cachear resultados de lectura
    @Cacheable(value = "[entity]", key = "#id")
    @Transactional(readOnly = true)
    public [Entity]Response findById(UUID id, UUID userId) {
        // ...
    }
    
    // Invalidar cache al modificar
    @CacheEvict(value = "[entity]", key = "#id")
    @Transactional
    public [Entity]Response update(UUID id, UUID userId, Update[Entity]Request request) {
        // ...
    }
    
    // Invalidar todo el cache al eliminar
    @CacheEvict(value = "[entity]", allEntries = true)
    @Transactional
    public void delete(UUID id, UUID userId) {
        // ...
    }
}
```

**Configuración**:
```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=10m
```

---

### 8.3. Batch Operations

```java
/**
 * Marca múltiples [Entity]s como leídos en batch (1 query vs N)
 */
@Transactional
public void markAllAsRead(UUID userId, List<UUID> ids) {
    // ❌ MAL: N queries
    // ids.forEach(id -> markAsRead(id, userId));
    
    // ✅ BIEN: 1 query
    int updated = repository.updateStatusBatch(ids, userId, Status.READ);
    log.info("Marked {} [Entity]s as read for user {}", updated, userId);
}

// En el Repository:
@Modifying
@Query("UPDATE [Entity] e SET e.status = :status WHERE e.id IN :ids AND e.userId = :userId")
int updateStatusBatch(
    @Param("ids") List<UUID> ids, 
    @Param("userId") UUID userId,
    @Param("status") Status status
);
```

---

### 8.4. Paginación Eficiente

```java
// Evitar COUNT(*) costoso cuando no es necesario
@Query(
    value = "SELECT e FROM [Entity] e WHERE e.userId = :userId",
    countQuery = "SELECT COUNT(e) FROM [Entity] e WHERE e.userId = :userId"
)
Page<[Entity]> findByUserIdOptimized(@Param("userId") UUID userId, Pageable pageable);

// O usar Slice si no necesitas el total
Slice<[Entity]> findByUserId(UUID userId, Pageable pageable);  // Sin COUNT
```

---

## 9. Testing

### 9.1. Tests Unitarios (Mockito)

```java
@ExtendWith(MockitoExtension.class)
class [NombreClase]ServiceTest {
    
    @Mock
    private [Entity]Repository repository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @InjectMocks
    private [NombreClase]Service service;
    
    private User mockUser;
    private [Entity] mockEntity;
    
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");
        
        mockEntity = new [Entity]();
        mockEntity.setId(UUID.randomUUID());
        mockEntity.setUser(mockUser);
        mockEntity.setCampo1("test");
    }
    
    @Test
    void create_ValidRequest_ReturnsEntity() {
        // Arrange
        UUID userId = mockUser.getId();
        Create[Entity]Request request = new Create[Entity]Request();
        request.setCampo1("test");
        request.setCampo2(42);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(repository.save(any([Entity].class))).thenReturn(mockEntity);
        
        // Act
        [Entity]Response response = service.create(userId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals("test", response.getCampo1());
        
        verify(userRepository).findById(userId);
        verify(repository).save(any([Entity].class));
        verify(eventPublisher).publishEvent(any([Entity]CreatedEvent.class));
    }
    
    @Test
    void create_UserNotFound_ThrowsException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Create[Entity]Request request = new Create[Entity]Request();
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            service.create(userId, request);
        });
        
        verify(repository, never()).save(any());
    }
    
    @Test
    void findById_UserNotOwner_ThrowsForbidden() {
        // Arrange
        UUID ownerId = mockUser.getId();
        UUID otherId = UUID.randomUUID();
        
        mockEntity.setUserId(ownerId);
        
        when(repository.findById(mockEntity.getId())).thenReturn(Optional.of(mockEntity));
        when(userRepository.findById(otherId)).thenReturn(Optional.of(new User()));
        
        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            service.findById(mockEntity.getId(), otherId);
        });
    }
    
    @Test
    void delete_HasDependencies_ThrowsBusinessException() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        User admin = new User();
        admin.setId(adminId);
        admin.setRole(Role.ADMIN);
        
        when(repository.findById(mockEntity.getId())).thenReturn(Optional.of(mockEntity));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(relatedRepository.countByEntityId(mockEntity.getId())).thenReturn(5L);
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            service.delete(mockEntity.getId(), adminId);
        });
        
        verify(repository, never()).delete(any());
    }
}
```

---

### 9.2. Tests de Integración (Testcontainers)

```java
@SpringBootTest
@Testcontainers
class [NombreClase]ServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb");
    
    @Autowired
    private [NombreClase]Service service;
    
    @Autowired
    private [Entity]Repository repository;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        repository.deleteAll();
        
        testUser = new User();
        testUser.setEmail("integration@test.com");
        testUser = userRepository.save(testUser);
    }
    
    @Test
    void create_ValidRequest_PersistsToDatabase() {
        // Arrange
        Create[Entity]Request request = new Create[Entity]Request();
        request.setCampo1("integration test");
        request.setCampo2(100);
        
        // Act
        [Entity]Response response = service.create(testUser.getId(), request);
        
        // Assert
        assertNotNull(response.getId());
        
        // Verificar en DB
        Optional<[Entity]> saved = repository.findById(UUID.fromString(response.getId()));
        assertTrue(saved.isPresent());
        assertEquals("integration test", saved.get().getCampo1());
    }
    
    @Test
    void update_ExistingEntity_UpdatesInDatabase() {
        // Arrange
        [Entity] entity = new [Entity]();
        entity.setUser(testUser);
        entity.setCampo1("original");
        entity.setCampo2(50);
        entity = repository.save(entity);
        
        Update[Entity]Request request = new Update[Entity]Request();
        request.setCampo1("updated");
        
        // Act
        [Entity]Response response = service.update(entity.getId(), testUser.getId(), request);
        
        // Assert
        assertEquals("updated", response.getCampo1());
        
        // Verificar en DB
        [Entity] updated = repository.findById(entity.getId()).get();
        assertEquals("updated", updated.getCampo1());
    }
    
    @Test
    @Transactional
    void delete_ExistingEntity_RemovesFromDatabase() {
        // Arrange
        [Entity] entity = new [Entity]();
        entity.setUser(testUser);
        entity = repository.save(entity);
        
        testUser.setRole(Role.ADMIN);  // Solo admin puede eliminar
        userRepository.save(testUser);
        
        // Act
        service.delete(entity.getId(), testUser.getId());
        
        // Assert
        Optional<[Entity]> deleted = repository.findById(entity.getId());
        // Si es soft delete:
        assertTrue(deleted.get().isDeleted());
        // Si es hard delete:
        // assertFalse(deleted.isPresent());
    }
}
```

---

### 9.3. Cobertura Objetivo

**Objetivo**: ≥ 85% de cobertura

- **Statements**: 90%
- **Branches**: 85%
- **Methods**: 95%
- **Lines**: 90%

**Configuración Jacoco**:
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <rules>
            <rule>
                <element>CLASS</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.85</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

---

## 10. Observabilidad

### 10.1. Logging

```java
@Slf4j
@Service
public class [NombreClase]Service {
    
    public [Entity]Response create(UUID userId, Create[Entity]Request request) {
        // INFO: Operaciones importantes
        log.info("Creating [Entity] for user: {}", userId);
        
        // DEBUG: Detalles para debugging
        log.debug("Request data: campo1={}, campo2={}", 
            request.getCampo1(), request.getCampo2());
        
        try {
            // ... lógica
            
            log.info("[Entity] created successfully with ID: {}", saved.getId());
            return response;
            
        } catch (BusinessException e) {
            // WARN: Errores esperados (reglas de negocio)
            log.warn("Business validation failed for user {}: {}", userId, e.getMessage());
            throw e;
            
        } catch (Exception e) {
            // ERROR: Errores inesperados
            log.error("Unexpected error creating [Entity] for user {}: {}", 
                userId, e.getMessage(), e);
            throw new InternalServerException("Error interno");
        }
    }
}
```

---

### 10.2. Métricas (Micrometer)

```java
@Service
public class [NombreClase]Service {
    
    private final MeterRegistry meterRegistry;
    private final Counter createCounter;
    private final Timer createTimer;
    
    public [NombreClase]Service(MeterRegistry meterRegistry, ...) {
        this.meterRegistry = meterRegistry;
        this.createCounter = Counter.builder("entity.created")
            .tag("service", "[NombreClase]")
            .register(meterRegistry);
        this.createTimer = Timer.builder("entity.create.duration")
            .register(meterRegistry);
    }
    
    public [Entity]Response create(UUID userId, Create[Entity]Request request) {
        return createTimer.record(() -> {
            try {
                [Entity]Response response = doCreate(userId, request);
                createCounter.increment();
                return response;
            } catch (Exception e) {
                meterRegistry.counter("entity.create.errors").increment();
                throw e;
            }
        });
    }
}
```

---

### 10.3. Distributed Tracing (Sleuth/Zipkin)

```java
// Spring Cloud Sleuth agrega automáticamente trace IDs a los logs
// Solo necesitas configurar:

// application.yml
spring:
  sleuth:
    sampler:
      probability: 1.0  # 100% en desarrollo, 10% en producción
  zipkin:
    base-url: http://zipkin:9411

// Los logs automáticamente incluirán:
// [appName,traceId,spanId,exportable]
```

---

## 11. Seguridad

### 11.1. Validación de Input

```java
@Service
public class [NombreClase]Service {
    
    public [Entity]Response create(UUID userId, Create[Entity]Request request) {
        // 1. Validación de Bean Validation (@Valid en Controller ya validó)
        // Pero podemos agregar validaciones adicionales programáticas:
        
        validateInput(request);
        
        // ... resto de lógica
    }
    
    private void validateInput(Create[Entity]Request request) {
        // Sanitización de strings (prevenir XSS, SQL injection)
        if (request.getCampo1() != null) {
            String sanitized = sanitize(request.getCampo1());
            request.setCampo1(sanitized);
        }
        
        // Validaciones custom
        if (containsProhibitedWords(request.getCampo1())) {
            throw new ValidationException("Campo1 contiene palabras prohibidas");
        }
    }
    
    private String sanitize(String input) {
        // Remover caracteres peligrosos
        return input.replaceAll("[<>\"']", "");
    }
}
```

---

### 11.2. Autorización

```java
@Service
public class [NombreClase]Service {
    
    /**
     * Verifica que el usuario tenga permiso para acceder al recurso
     */
    private void checkAuthorization([Entity] entity, UUID userId, Permission required) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ForbiddenException("Usuario no encontrado"));
        
        // Verificar ownership
        boolean isOwner = entity.getUserId().equals(userId);
        
        // Verificar rol
        boolean hasRole = user.hasRole(required.getRole());
        
        // Verificar permiso específico
        boolean hasPermission = user.hasPermission(required);
        
        if (!isOwner && !hasRole && !hasPermission) {
            log.warn("Authorization failed: user {} tried to {} on entity {}", 
                userId, required, entity.getId());
            throw new ForbiddenException("No tienes permisos para esta operación");
        }
    }
}
```

---

### 11.3. Prevención de Mass Assignment

```java
// ❌ MAL: Nunca exponer entidades directamente
@PostMapping
public [Entity] create(@RequestBody [Entity] entity) {
    return repository.save(entity);  // Usuario puede setear cualquier campo!
}

// ✅ BIEN: Usar DTOs
@PostMapping
public [Entity]Response create(@Valid @RequestBody Create[Entity]Request request) {
    // Solo campos permitidos en el DTO
    // Campos sensibles (id, createdAt, etc.) son seteados por el service
    return service.create(getUserId(), request);
}
```

---

## 12. Configuración

### 12.1. Properties

```java
@Configuration
@ConfigurationProperties(prefix = "app.[entity]")
@Validated
public class [Entity]Properties {
    
    @NotNull
    @Min(1)
    @Max(1000)
    private Integer maxItems = 100;
    
    @NotNull
    @Min(1)
    @Max(365)
    private Integer retentionDays = 30;
    
    private boolean enableNotifications = true;
    
    // Getters y Setters
}

// Uso en Service:
@Service
@RequiredArgsConstructor
public class [NombreClase]Service {
    
    private final [Entity]Properties properties;
    
    public void doSomething() {
        int max = properties.getMaxItems();
        // ...
    }
}
```

```yaml
# application.yml
app:
  [entity]:
    max-items: 100
    retention-days: 30
    enable-notifications: true
```

---

## 13. Eventos de Dominio

### 13.1. Publicar Eventos

```java
@Service
@RequiredArgsConstructor
public class [NombreClase]Service {
    
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public [Entity]Response create(UUID userId, Create[Entity]Request request) {
        // ... crear entidad
        
        // Publicar evento DESPUÉS del commit
        eventPublisher.publishEvent(new [Entity]CreatedEvent(
            saved.getId(),
            saved.getUserId(),
            saved.getType()
        ));
        
        return response;
    }
}

// Evento
public class [Entity]CreatedEvent {
    private final UUID entityId;
    private final UUID userId;
    private final String type;
    private final Instant occurredAt;
    
    public [Entity]CreatedEvent(UUID entityId, UUID userId, String type) {
        this.entityId = entityId;
        this.userId = userId;
        this.type = type;
        this.occurredAt = Instant.now();
    }
    
    // Getters
}
```

---

### 13.2. Consumir Eventos

```java
@Component
@Slf4j
public class [Entity]EventListener {
    
    @Async  // Procesamiento asíncrono
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEntityCreated([Entity]CreatedEvent event) {
        log.info("Processing [Entity]CreatedEvent: {}", event.getEntityId());
        
        try {
            // Lógica secundaria (enviar notificación, actualizar stats, etc.)
            notificationService.notifyUser(event.getUserId(), "Entity creado");
            
        } catch (Exception e) {
            log.error("Error processing event: {}", e.getMessage(), e);
            // No lanzar excepción para no afectar la transacción principal
        }
    }
}
```

---

## 14. Checklist de Implementación

### Estructura Base
- [ ] Crear clase en package correcto
- [ ] Anotar con `@Service`, `@RestController`, o `@Repository`
- [ ] Definir dependencias con `@RequiredArgsConstructor` (Lombok)
- [ ] Agregar `@Slf4j` para logging

### Métodos Públicos
- [ ] Implementar todos los métodos de la interfaz pública
- [ ] Agregar JavaDoc a cada método
- [ ] Validar parámetros de entrada
- [ ] Implementar lógica de negocio
- [ ] Convertir entre Entity ↔ DTO

### Transacciones
- [ ] Anotar métodos con `@Transactional`
- [ ] Usar `readOnly=true` en consultas
- [ ] Considerar `propagation` apropiada

### Validaciones
- [ ] Implementar validaciones de negocio
- [ ] Validar ownership donde aplique
- [ ] Lanzar excepciones apropiadas

### Manejo de Errores
- [ ] Try-catch en métodos críticos
- [ ] Logging de errores
- [ ] Excepciones específicas del dominio

### Performance
- [ ] Evitar N+1 queries (usar JOIN FETCH)
- [ ] Agregar caching donde sea apropiado
- [ ] Implementar batch operations si aplica
- [ ] Usar paginación en listas

### Testing
- [ ] Tests unitarios con Mockito (≥85% coverage)
- [ ] Tests de integración con Testcontainers
- [ ] Tests de validaciones de negocio
- [ ] Tests de manejo de errores

### Observabilidad
- [ ] Logging en operaciones importantes
- [ ] Métricas con Micrometer
- [ ] Documentación en JavaDoc

### Seguridad
- [ ] Validación de input
- [ ] Autorización en métodos sensibles
- [ ] Sanitización de datos
- [ ] No exponer entidades directamente

### Eventos
- [ ] Publicar eventos de dominio donde aplique
- [ ] Listeners para efectos secundarios

---

## 15. Referencias

- **Technical Design**: `@.gemini/sprints/technical-designs/TD-[XXX]-[nombre].md`
- **Feature Plan**: `@.gemini/sprints/feature-plans/FP-[XXX]-[nombre].md`
- **Entidad JPA**: `[Entity].java`
- **DTOs**: `Create[Entity]Request.java`, `Update[Entity]Request.java`, `[Entity]Response.java`
- **Repository**: `[Entity]Repository.java`
- **Otras BSS relacionadas**: 
  - `bss-[otra-clase].md`

---

## 16. Changelog

| Versión | Fecha | Autor | Cambios |
|---------|-------|-------|---------|
| 1.0 | [YYYY-MM-DD] | [Nombre/IA] | Creación inicial |
| 1.1 | [YYYY-MM-DD] | [Nombre] | [Descripción de cambios] |

---

**BSS creada por**: [Nombre o "IA BSS Generator"]  
**Fecha**: [YYYY-MM-DD]  
**Última actualización**: [YYYY-MM-DD]
```

---

