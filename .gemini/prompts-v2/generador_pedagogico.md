# 🎯 Prompt Completo - Generador de Documentación Pedagógica Spring Boot

```markdown
Actúa como un **Spring Boot Senior Developer** experto en mentoría técnica y arquitectura de software.

**OBJETIVO**: Ayudarme a aprender Spring Boot profundamente, analizando este proyecto real desde los fundamentos hasta los detalles. Genera documentación pedagógica en 3 archivos Markdown separados.

---

## 📁 FORMATO DE SALIDA (3 ARCHIVOS MARKDOWN)

Genera el análisis en **3 archivos Markdown separados** con esta estructura:

### 📄 Archivo 1: `01-IOC-Vision-General.md`
**Contenido**: Fases 1 y 2 completas
- Estructura del proyecto y arquitectura
- Patrón arquitectónico identificado
- Flujo de datos principal
- Responsabilidades detalladas por cada capa (dto/, entity/, repository/, service/, controller/, mapper/, exception/, config/, security/)

**Tamaño**: 3000-4000 palabras

---

### 📄 Archivo 2: `02-IOC-Analisis-Detallado.md`
**Contenido**: Fase 3 completa
- Análisis archivo por archivo siguiendo el orden pedagógico
- IocbackendApplication.java
- Configuraciones (SecurityConfig, CorsConfig, etc.)
- Entities, DTOs, Repositories, Services, Controllers
- Mappers y Exception Handlers

**Tamaño**: 5000-7000 palabras (el más extenso)

---

### 📄 Archivo 3: `03-IOC-Resumen-Produccion.md`
**Contenido**: Fases 4 y 5 completas
- Resumen de aprendizaje con checklist de conceptos
- Patrones de diseño identificados
- Glosario completo de anotaciones
- Análisis de seguridad, resiliencia y observabilidad
- Plan de estudio sugerido

**Tamaño**: 2000-3000 palabras

---

**INSTRUCCIONES DE GENERACIÓN**:

1. Genera **UN ARCHIVO A LA VEZ**
2. Al finalizar cada archivo, muestra:
   ```
   ✅ Archivo [nombre.md] completado.
   📝 Copia el contenido de arriba y guárdalo como [nombre.md]
   
   ¿Continúo con el siguiente archivo?
   Responde "CONTINUAR" o "PAUSA"
   ```
3. Cada archivo debe:
   - Ser completamente **auto-contenido**
   - Incluir **índice navegable**
   - Tener **links de navegación** al final (anterior/siguiente)
   - Usar **emojis** para mejorar legibilidad
   - Incluir **diagramas ASCII** cuando sea útil

---

## 📊 INFORMACIÓN DEL PROYECTO

**Proyecto**: Inteligencia Operacional Cambiaso (IOC) - Backend  
**Framework**: Spring Boot 3.5.5 + Java 21  
**Ruta raíz**: `src/main/java/com/cambiaso/iocbackend`  
**Archivo principal**: `IocbackendApplication.java`  
**Descripción**: Plataforma de BI para automatizar ingesta, procesamiento y visualización de datos de producción  
**Nivel del estudiante**: Junior Developer

---

## 🎯 CONTENIDO DEL ARCHIVO 1: `01-IOC-Vision-General.md`

Genera un documento Markdown con esta estructura exacta:

```markdown
# 📘 IOC Backend - Visión General y Conceptos Fundamentales

> **Proyecto**: Inteligencia Operacional Cambiaso (IOC)  
> **Framework**: Spring Boot 3.5.5 + Java 21  
> **Fecha de Análisis**: [Fecha actual]  
> **Nivel**: Junior Developer  
> **Prerequisito**: Conocimientos básicos de Java y HTTP

---

## 📋 Índice

1. [Información del Proyecto](#información-del-proyecto)
2. [FASE 1: Visión General](#fase-1-visión-general)
   - [Estructura del Proyecto](#estructura-del-proyecto)
   - [Patrón Arquitectónico](#patrón-arquitectónico)
   - [Flujo de Datos Principal](#flujo-de-datos-principal)
   - [Clase Principal](#clase-principal)
3. [FASE 2: Responsabilidades por Capa](#fase-2-responsabilidades-por-capa)
   - [Carpeta dto/](#carpeta-dto)
   - [Carpeta entity/](#carpeta-entity)
   - [Carpeta repository/](#carpeta-repository)
   - [Carpeta service/](#carpeta-service)
   - [Carpeta controller/](#carpeta-controller)
   - [Carpeta mapper/](#carpeta-mapper)
   - [Carpeta exception/](#carpeta-exception)
   - [Carpeta config/](#carpeta-config)
   - [Carpeta security/](#carpeta-security)
4. [Checklist de Comprensión](#checklist-de-comprensión)
5. [Navegación](#navegación)

---

## 📊 Información del Proyecto

[Resumen ejecutivo del proyecto]

---

## FASE 1: Visión General

### 🏗️ Estructura del Proyecto

**Árbol de carpetas** (con descripción de cada una):

```
src/main/java/com/cambiaso/iocbackend/
├── 📦 config/         → [Descripción breve]
├── 📦 controller/     → [Descripción breve]
├── 📦 service/        → [Descripción breve]
├── 📦 repository/     → [Descripción breve]
├── 📦 model/
│   ├── entity/        → [Descripción breve]
│   └── dto/           → [Descripción breve]
├── 📦 mapper/         → [Descripción breve]
├── 📦 exception/      → [Descripción breve]
├── 📦 security/       → [Descripción breve]
└── 📦 util/           → [Descripción breve]
```

### 🎨 Patrón Arquitectónico

- **Patrón identificado**: [Nombre del patrón]
- **Justificación**: [Por qué se identifica así]
- **Beneficios** para este proyecto:
  1. [Beneficio 1]
  2. [Beneficio 2]
  3. [Beneficio 3]
- **Separación de responsabilidades**: [Cómo se logra]

### 🔄 Flujo de Datos Principal

[Diagrama ASCII completo del flujo Request → Response]

**Ejemplo**:
```
Cliente HTTP
    ↓ (1) Request JSON
┌─────────────────────────────┐
│   @RestController           │ ← Valida formato (@Valid)
│   UserController            │   Define ruta y método HTTP
└──────────────┬──────────────┘   Mapea status codes
               ↓ (2) DTO de Request
┌─────────────────────────────┐
│   @Service                  │ ← Lógica de negocio
│   UserService               │   Valida reglas de negocio
└──────────────┬──────────────┘   Maneja transacciones
               ↓ (3) Entity
┌─────────────────────────────┐
│   @Repository               │ ← Acceso a datos
│   UserRepository            │   Genera SQL automáticamente
└──────────────┬──────────────┘
               ↓ (4) SQL
┌─────────────────────────────┐
│   PostgreSQL Database       │
└──────────────┬──────────────┘
               ↓ (5) Entity cargada
┌─────────────────────────────┐
│   @Mapper (MapStruct)       │ ← Transforma Entity → DTO
└──────────────┬──────────────┘
               ↓ (6) DTO de Response
Cliente HTTP (JSON)
```

**Explicación paso a paso**:
1. [Explicación del paso 1]
2. [Explicación del paso 2]
[...]

### 🚀 Clase Principal

**IocbackendApplication.java**

- **¿Qué pasa cuando arranca la aplicación?**
  [Explicación detallada]

- **Anotaciones principales**:
  - `@SpringBootApplication`: [Qué hace - explicación pedagógica]
  - [Otras anotaciones encontradas]

- **Configuraciones automáticas activadas**:
  [Lista de auto-configurations detectadas]

---

## FASE 2: Responsabilidades por Capa

> **IMPORTANTE**: Lee esta sección completa antes de pasar al Archivo 2.  
> Aquí entenderás QUÉ HACE y POR QUÉ EXISTE cada carpeta/capa.

---

### 📂 Carpeta: `dto/` (Data Transfer Objects)

#### ¿Qué son los DTOs?

[Explicación en 3-4 frases con lenguaje simple]

**Analogía del mundo real**:  
[Ejemplo concreto y memorable]

#### ¿Para qué sirven?

**Problema que resuelven**:  
[Explicación del problema sin DTOs]

**Beneficios**:
1. **[Beneficio 1]**: [Explicación + mini-ejemplo]
2. **[Beneficio 2]**: [Explicación + mini-ejemplo]
3. **[Beneficio 3]**: [Explicación + mini-ejemplo]

#### ¿Cuándo usar DTOs?

✅ **SÍ usar cuando**:
- [Caso 1 con ejemplo]
- [Caso 2 con ejemplo]
- [Caso 3 con ejemplo]

❌ **NO usar cuando**:
- [Caso 1 con ejemplo]
- [Caso 2 con ejemplo]

#### Tipos comunes en esta carpeta

```
dto/
├── request/     → Datos que ENTRAN al sistema (POST/PUT)
│                  Ejemplo: UserCreateRequest, UserUpdateRequest
│
└── response/    → Datos que SALEN del sistema (GET)
                   Ejemplo: UserResponse, UserListResponse
```

#### Ejemplo del proyecto IOC

```java
// UserCreateRequest.java (Request DTO)
// El cliente envía esto al hacer POST /api/users
{
  "email": "juan@example.com",
  "primerNombre": "Juan",
  "primerApellido": "Pérez"
}

// UserResponse.java (Response DTO)
// El servidor devuelve esto
{
  "id": 123,
  "email": "juan@example.com",
  "nombreCompleto": "Juan Pérez",  ← Calculado, NO existe en BD
  "roles": ["ADMIN", "USER"]        ← Agregado desde relación
}
```

**¿Notas la diferencia?**  
[Explicación de las diferencias clave]

#### Características técnicas

- **Anotaciones comunes**:
  - `@Data`: [Qué hace - Lombok]
  - `@NotNull`, `@Email`, `@Size`: [Bean Validation]
  - `@JsonProperty`: [Personalizar nombres JSON]

- **Validaciones**: [Cómo funcionan con @Valid]

- **Inmutabilidad**: 
  - Request DTOs: [¿Mutables? ¿Por qué?]
  - Response DTOs: [¿Inmutables? ¿Por qué?]

- **Relación con Entities**: [Cómo se mapean - adelanto de Mappers]

#### ⚠️ Anti-patrones comunes

❌ **Exponer Entities directamente en la API**
```java
// NUNCA hagas esto
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {  // ← Entity expuesta
    return userRepository.findById(id);
}
```
**Por qué es malo**:
1. [Razón 1: Seguridad]
2. [Razón 2: Acoplamiento]
3. [Razón 3: Lazy loading issues]

✅ **Hazlo así**:
```java
@GetMapping("/users/{id}")
public UserResponse getUser(@PathVariable Long id) {  // ← DTO
    User user = userService.getUserById(id);
    return userMapper.toResponse(user);
}
```

---

### 📂 Carpeta: `entity/` (Entidades JPA)

#### ¿Qué son las Entities?

[Explicación pedagógica de ORM y entities]

**Analogía del mundo real**:  
[Ejemplo memorable]

#### ¿Para qué sirven?

**Problema que resuelven**:  
[Explicar el problema antes de ORM - SQL manual]

**Beneficios**:
1. **[Beneficio 1]**: [Trabajar con objetos en vez de SQL]
2. **[Beneficio 2]**: [Validaciones a nivel de modelo]
3. **[Beneficio 3]**: [Relaciones automáticas]

#### Entity vs DTO: Diferencias Críticas

| Aspecto | Entity (@Entity) | DTO (Request/Response) |
|---------|------------------|------------------------|
| **Propósito** | Mapear tabla de BD | Transferir datos en API |
| **Anotaciones** | @Entity, @Table, @Column | @Data, @NotNull, @JsonProperty |
| **Mutabilidad** | Mutable (JPA lo requiere) | Request: mutable, Response: preferible inmutable |
| **Relaciones** | @OneToMany, @ManyToOne | Sin relaciones directas |
| **Campos calculados** | No (solo BD) | Sí (nombreCompleto, etc.) |
| **Exponer en API** | ❌ NUNCA | ✅ SIEMPRE |
| **Lazy Loading** | Sí (puede causar N+1) | No aplica |

#### ¿Por qué NUNCA exponer Entities en la API?

**3 razones críticas**:

1. **Seguridad** 🔒
   ```java
   @Entity
   public class User {
       private String password;  // ← Se expondría en la API
       private boolean isAdmin;  // ← Campo interno sensible
   }
   ```

2. **Lazy Loading Issues** ⚠️
   ```java
   @Entity
   public class User {
       @OneToMany(fetch = FetchType.LAZY)
       private List<Order> orders;  // ← LazyInitializationException en JSON
   }
   ```

3. **Acoplamiento** 🔗
   ```java
   // Si cambias la BD, cambias la API (breaking change)
   ALTER TABLE users RENAME COLUMN email TO email_address;
   // ← Rompe contratos de API existentes
   ```

#### Características técnicas

**Anotaciones de mapeo**:
- `@Entity`: [Qué hace]
- `@Table(name = "...")`: [Cuándo usar]
- `@Column(name = "...", nullable = false)`: [Configuración]
- `@Id` + `@GeneratedValue`: [Estrategias de generación de IDs]

**Relaciones**:
- `@OneToOne`: [Ejemplo del proyecto]
- `@OneToMany` / `@ManyToOne`: [Ejemplo del proyecto]
- `@ManyToMany`: [Ejemplo del proyecto]
- `fetch = LAZY` vs `EAGER`: [Diferencias y cuándo usar cada uno]
- `cascade`: [Tipos y riesgos]

**Ciclo de vida**:
- `@PrePersist`: [Cuándo se ejecuta]
- `@PreUpdate`: [Cuándo se ejecuta]
- `@PreRemove`: [Cuándo se ejecuta]

**Auditoría**:
- `@CreatedDate`: [Spring Data JPA Auditing]
- `@LastModifiedDate`: [Configuración requerida]
- `@CreatedBy`, `@LastModifiedBy`: [Para tracking de usuarios]

#### Ejemplo del proyecto IOC

[Mostrar una Entity real del proyecto con todas las anotaciones explicadas línea por línea]

---

### 📂 Carpeta: `repository/` (Acceso a Datos)

#### ¿Qué son los Repositories?

[Explicación del Repository Pattern]

**Analogía del mundo real**:  
[Ejemplo del bibliotecario o almacenista]

#### ¿Para qué sirven?

**Patrón de diseño**: Repository Pattern (Domain-Driven Design)

**Problema que resuelven**:  
[Abstraer acceso a datos, cambiar BD sin tocar lógica de negocio]

**Spring Data JPA - "Magia" automática**:  
[Qué hace Spring por ti sin escribir SQL]

#### Métodos GRATIS de Spring Data JPA

```java
public interface UserRepository extends JpaRepository<User, Long> {
    // ✅ Estos métodos NO necesitas implementarlos:
    
    // CRUD básico
    Optional<User> findById(Long id);
    List<User> findAll();
    User save(User user);
    void deleteById(Long id);
    boolean existsById(Long id);
    long count();
    
    // Con paginación
    Page<User> findAll(Pageable pageable);
    
    // Con sorting
    List<User> findAll(Sort sort);
}
```

**¿Cómo funciona?**  
[Explicación de proxies dinámicos de Spring]

#### Query Methods (Queries Derivadas)

Spring genera SQL automáticamente **basándose en el nombre del método**:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring genera: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
    
    // Spring genera: SELECT * FROM users WHERE email = ? AND is_active = ?
    List<User> findByEmailAndIsActive(String email, boolean isActive);
    
    // Spring genera: SELECT * FROM users WHERE planta_id = ? AND is_active = TRUE
    List<User> findByPlantaIdAndIsActiveTrue(Integer plantaId);
    
    // Spring genera: SELECT * FROM users WHERE email LIKE %?%
    List<User> findByEmailContaining(String emailPart);
    
    // Spring genera: SELECT * FROM users WHERE created_at > ?
    List<User> findByCreatedAtAfter(LocalDateTime date);
    
    // Spring genera: DELETE FROM users WHERE email = ?
    void deleteByEmail(String email);
    
    // Spring genera: SELECT COUNT(*) FROM users WHERE is_active = ?
    long countByIsActive(boolean isActive);
}
```

**Palabras clave soportadas**:  
`findBy`, `countBy`, `deleteBy`, `existsBy`, `And`, `Or`, `Between`, `LessThan`, `GreaterThan`, `Like`, `Containing`, `StartingWith`, `EndingWith`, `IsNull`, `IsNotNull`, `True`, `False`, `OrderBy`, etc.

#### ¿Cuándo usar cada tipo de query?

| Tipo | Cuándo Usar | Complejidad | Ejemplo |
|------|-------------|-------------|---------|
| **CRUD básico** | Operaciones simples por ID | Baja | `findById()`, `save()` |
| **Query Methods** | Búsquedas por 1-3 campos | Baja-Media | `findByEmail()` |
| **@Query (JPQL)** | Joins, agregaciones, lógica compleja | Media-Alta | Ver abajo |
| **Native Query** | SQL específico de BD, funciones nativas | Alta | Ver abajo |
| **Specifications** | Búsquedas dinámicas (filtros variables) | Alta | Criteria API |

#### @Query Personalizado (JPQL)

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // JPQL (Java Persistence Query Language)
    @Query("SELECT u FROM User u WHERE u.email LIKE %:domain%")
    List<User> findByEmailDomain(@Param("domain") String domain);
    
    // Con JOIN
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    // Con agregación
    @Query("SELECT u.planta.name, COUNT(u) FROM User u GROUP BY u.planta.name")
    List<Object[]> countUsersByPlanta();
}
```

#### Native Query (SQL puro)

```java
@Query(value = "SELECT * FROM app_users WHERE email ILIKE :pattern", nativeQuery = true)
List<User> findByEmailPattern(@Param("pattern") String pattern);
```

**Cuándo usar Native Query**:
- Funciones específicas de PostgreSQL (ILIKE, JSON operators, etc.)
- Queries de performance crítica
- Acceso a vistas o funciones de BD

#### Ejemplo del proyecto IOC

[Mostrar repositorio real con diferentes tipos de queries explicadas]

---

### 📂 Carpeta: `service/` (Lógica de Negocio)

#### ¿Qué son los Services?

[Explicación del Service Layer]

**Analogía del mundo real**:  
[Ejemplo del chef de restaurante]

#### ¿Para qué sirven?

**Responsabilidad ÚNICA**: Lógica de negocio (NO presentación, NO persistencia)

**Orquestación**: Coordina múltiples componentes para cumplir un caso de uso

#### ¿Qué va en Service vs Controller?

| Responsabilidad | Controller | Service | Repository |
|----------------|------------|---------|------------|
| **Recibir HTTP request** | ✅ | ❌ | ❌ |
| **Validar formato JSON** | ✅ (@Valid) | ❌ | ❌ |
| **Validar reglas de negocio** | ❌ | ✅ | ❌ |
| **Mapear DTO ↔ Entity** | ❌ | ✅ (usa Mapper) | ❌ |
| **Ejecutar lógica de negocio** | ❌ | ✅ | ❌ |
| **Acceder a BD** | ❌ | ❌ | ✅ |
| **Coordinar múltiples repos** | ❌ | ✅ | ❌ |
| **Manejar transacciones** | ❌ | ✅ (@Transactional) | ❌ |
| **Devolver HTTP status** | ✅ | ❌ | ❌ |
| **Manejar excepciones HTTP** | ✅ | ❌ (lanza excepciones de negocio) | ❌ |

#### Ejemplo de responsabilidades correctas

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // ✅ 1. Validación de reglas de negocio
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }
        
        // ✅ 2. Transformación DTO → Entity (vía Mapper)
        User user = userMapper.toEntity(request);
        
        // ✅ 3. Aplicar lógica de negocio
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        
        // ✅ 4. Asignar rol por defecto (regla de negocio)
        Role defaultRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new RoleNotFoundException("USER"));
        user.addRole(defaultRole);
        
        // ✅ 5. Persistir (vía Repository)
        User savedUser = userRepository.save(user);
        
        // ✅ 6. Orquestar otros servicios (envío de email)
        emailService.sendWelcomeEmail(savedUser.getEmail());
        
        // ✅ 7. Transformar Entity → DTO (vía Mapper)
        return userMapper.toResponse(savedUser);
    }
}
```

#### ❌ Anti-patrones comunes

**Anti-patrón 1: Service con lógica HTTP**
```java
// ❌ MALO - Service NO debe manejar HTTP
public ResponseEntity<UserResponse> createUser(...) {
    // ResponseEntity es responsabilidad del Controller
}
```

**Anti-patrón 2: Controller con lógica de negocio**
```java
// ❌ MALO - Controller NO debe tener lógica de negocio
@PostMapping("/users")
public UserResponse createUser(@RequestBody UserCreateRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {  // ← Mal lugar
        throw new UserAlreadyExistsException();
    }
    // ...
}
```

**Anti-patrón 3: Service accediendo directamente a Entity sin DTO**
```java
// ❌ MALO - Devolver Entity
public User createUser(User user) {
    return userRepository.save(user);
}

// ✅ BUENO - Trabajar con DTOs
public UserResponse createUser(UserCreateRequest request) {
    User user = userMapper.toEntity(request);
    User saved = userRepository.save(user);
    return userMapper.toResponse(saved);
}
```

#### Anotaciones clave

**@Service**
- **Qué hace**: Marca la clase como un bean de Spring de tipo "servicio"
- **Diferencia con @Component**: Semántica (indica que contiene lógica de negocio)
- **Cómo se inyecta**: Via constructor, field, o setter injection

**@Transactional**
- **Qué hace**: Envuelve el método en una transacción de BD
- **Cuándo usar**: Siempre que modifiques datos (save, update, delete)
- **readOnly = true**: Para queries (optimización)
- **Rollback automático**: Si lanza RuntimeException (o @Transactional(rollbackFor = ...))
- **Propagation**: REQUIRED (default), REQUIRES_NEW, NESTED, etc.

**@Async**
- **Qué hace**: Ejecuta el método en un thread separado
- **Cuándo usar**: Operaciones largas que no bloquean (emails, reports, etc.)
- **Requiere**: @EnableAsync en configuración

#### Ejemplo del proyecto IOC

[Mostrar Service real con transacciones y orquestación explicada]

---

### 📂 Carpeta: `controller/` (Capa de Presentación)

#### ¿Qué son los Controllers?

[Explicación del patrón MVC/Controller]

**Analogía del mundo real**:  
[Ejemplo del mesero]

#### ¿Para qué sirven?

**Responsabilidad**: Ser el punto de entrada HTTP de la aplicación

**NO contienen**: Lógica de negocio, acceso a BD, mapeos complejos

#### Anatomía de un endpoint completo

```java
@RestController  // ← (1) Marca como REST controller
@RequestMapping("/api/v1/users")  // ← (2) Base path
@RequiredArgsConstructor  // ← (3) Inyección vía constructor (Lombok)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")  // ← (4) Seguridad a nivel de clase
@Validated  // ← (5) Habilitar validación de parámetros
public class UserController {
    
    private final UserService userService;  // ← (6) Dependencia inyectada
    
    @PostMapping  // ← (7) HTTP Method: POST /api/v1/users
    @ResponseStatus(HttpStatus.CREATED)  // ← (8) Status code: 201
    @Operation(summary = "Crear usuario")  // ← (9) OpenAPI/Swagger
    public UserResponse createUser(
        @Valid @RequestBody UserCreateRequest request  // ← (10) Validación + cuerpo JSON
    ) {
        return userService.createUser(request);  // ← (11) Delega a Service
    }
    
    @GetMapping("/{id}")  // ← (12) Path variable
    public ResponseEntity<UserResponse> getUser(
        @PathVariable @Positive Long id  // ← (13) Validación de path param
    ) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);  // ← (14) ResponseEntity explícito
    }
    
    @GetMapping  // ← (15) Query params
    public Page<UserResponse> listUsers(
        @RequestParam(required = false) String search,  // ← (16) Opcional
        @PageableDefault(size = 20, sort = "id") Pageable pageable  // ← (17) Paginación
    ) {
        return userService.searchUsers(search, pageable);
    }
}
```

**Explicación de anotaciones numeradas**:
1. `@RestController`: [Explicación]
2. `@RequestMapping`: [Explicación]
[... continuar con todas]

#### Responsabilidades del Controller (checklist)

✅ **SÍ hace**:
1. ✅ Definir rutas (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`)
2. ✅ Validar formato de entrada (`@Valid`, `@Validated`)
3. ✅ Extraer parámetros HTTP:
   - Body: `@RequestBody`
   - Path: `@PathVariable` (ej: `/users/{id}`)
   - Query: `@RequestParam` (ej: `/users?search=...`)
   - Headers: `@RequestHeader`
4. ✅ Definir status codes (`@ResponseStatus`, `ResponseEntity`)
5. ✅ Aplicar seguridad (`@PreAuthorize`, `@Secured`)
6. ✅ Delegar a Service (NO hacer lógica aquí)
7. ✅ Documentar API (`@Operation`, `@ApiResponse` de Swagger)

❌ **NO hace**:
1. ❌ Lógica de negocio (ej: calcular descuentos, validar stock)
2. ❌ Acceso directo a Repository
3. ❌ Mapeos Entity ↔ DTO (el Service usa Mapper)
4. ❌ Manejar transacciones (es responsabilidad del Service)
5. ❌ Logs excesivos (solo entrada/salida si es necesario)

#### Tipos de respuesta

**Opción 1: Tipo directo + @ResponseStatus**
```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public UserResponse createUser(@Valid @RequestBody UserCreateRequest request) {
    return userService.createUser(request);
}
```

**Opción 2: ResponseEntity (más control)**
```java
@GetMapping("/{id}")
public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
    UserResponse user = userService.getUserById(id);
    return ResponseEntity.ok()
        .header("X-Custom-Header", "value")
        .body(user);
}
```

**Cuándo usar cada uno**:
- Tipo directo: Si solo necesitas el body y status estándar
- ResponseEntity: Si necesitas headers custom o status dinámico

#### Validaciones en Controller

```java
@PostMapping
public UserResponse createUser(
    @Valid @RequestBody UserCreateRequest request  // ← Bean Validation
) {
    // Si las validaciones fallan, Spring automáticamente devuelve 400 Bad Request
}

// En el DTO:
public class UserCreateRequest {
    @NotNull(message = "Email es requerido")
    @Email(message = "Formato de email inválido")
    private String email;
    
    @NotBlank(message = "Primer nombre es requerido")
    @Size(min = 2, max = 100)
    private String primerNombre;
}
```

#### Ejemplo del proyecto IOC

[Mostrar Controller real con todos los tipos de endpoints explicados]

---

### 📂 Carpeta: `mapper/` (Transformación de Datos)

#### ¿Qué son los Mappers?

[Explicación de MapStruct]

**Analogía del mundo real**:  
[Ejemplo del traductor]

#### ¿Por qué existen?

**Problema sin Mapper**:
```java
// ❌ Mapeo manual - tedioso, propenso a errores
public UserResponse toResponse(User user) {
    UserResponse response = new UserResponse();
    response.setId(user.getId());
    response.setEmail(user.getEmail());
    response.setNombreCompleto(
        user.getPrimerNombre() + " " + 
        user.getSegundoNombre() + " " + 
        user.getPrimerApellido()
    );
    response.setPlantaId(user.getPlanta() != null ? user.getPlanta().getId() : null);
    // ... 20 campos más
    return response;
}
```

**Solución con MapStruct**:
```java
// ✅ Declarativo, type-safe, performance nativa
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "nombreCompleto", expression = "java(buildFullName(user))")
    @Mapping(target = "plantaId", source = "planta.id")
    UserResponse toResponse(User user);
    
    default String buildFullName(User user) {
        return Stream.of(user.getPrimerNombre(), user.getSegundoNombre(), 
                         user.getPrimerApellido(), user.getSegundoApellido())
            .filter(Objects::nonNull)
            .collect(Collectors.joining(" "));
    }
}
```

#### Ventajas de MapStruct

1. **Performance**: Genera código Java puro (NO usa reflection)
2. **Type-Safe**: Errores en compilación, NO en runtime
3. **Mantenibilidad**: Código declarativo
4. **Spring Integration**: Se registra como `@Component` automáticamente

#### Tipos de mapeo

**Mapeo simple (campos con mismo nombre)**:
```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);  // ← Auto-mapea id, email, etc.
}
```

**Mapeo con expresiones custom**:
```java
@Mapping(target = "nombreCompleto", expression = "java(buildFullName(user))")
```

**Mapeo de nested properties**:
```java
@Mapping(target = "plantaId", source = "planta.id")
@Mapping(target = "plantaNombre", source = "planta.name")
```

**Mapeo bidireccional**:
```java
// Entity → DTO
UserResponse toResponse(User user);

// DTO → Entity
@Mapping(target = "id", ignore = true)
@Mapping(target = "createdAt", ignore = true)
User toEntity(UserCreateRequest request);
```

**Update de Entity existente**:
```java
@Mapping(target = "id", ignore = true)
@Mapping(target = "email", ignore = true)  // No se puede cambiar
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
void updateEntityFromRequest(UserUpdateRequest request, @MappingTarget User user);
```

#### Ejemplo del proyecto IOC

[Mostrar Mapper real con diferentes tipos de transformaciones]

---

### 📂 Carpeta: `exception/` (Manejo de Errores)

#### ¿Qué contiene?

**Dos tipos de archivos**:
1. **Excepciones custom**: `UserNotFoundException`, `EmailAlreadyExistsException`
2. **GlobalExceptionHandler**: Maneja excepciones centralizadamente

#### ¿Para qué sirve?

**Problema sin Exception Handler global**:
```java
// ❌ Código duplicado en cada Controller
@PostMapping
public UserResponse createUser(@RequestBody UserCreateRequest request) {
    try {
        return userService.createUser(request);
    } catch (UserAlreadyExistsException e) {
        return ResponseEntity.status(409).body(new ErrorResponse(...));
    } catch (ValidationException e) {
        return ResponseEntity.status(400).body(new ErrorResponse(...));
    }
}
```

**Solución con @RestControllerAdvice**:
```java
// ✅ Manejo centralizado
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(UserNotFoundException ex) {
        return new ErrorResponse(
            "USER_NOT_FOUND",
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
    }
    
    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAlreadyExists(UserAlreadyExistsException ex) {
        return new ErrorResponse(
            "USER_ALREADY_EXISTS",
            ex.getMessage(),
            HttpStatus.CONFLICT.value()
        );
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage
            ));
        return new ValidationErrorResponse("VALIDATION_ERROR", errors);
    }
}
```

#### Jerarquía de excepciones

```java
// Excepción base
public abstract class BusinessException extends RuntimeException {
    private final String errorCode;
    
    protected BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

// Excepciones específicas
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(Long userId) {
        super("USER_NOT_FOUND", "Usuario con ID " + userId + " no encontrado");
    }
}

public class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(String email) {
        super("USER_ALREADY_EXISTS", "El email " + email + " ya está registrado");
    }
}
```

#### ErrorResponse DTO

```java
@Data
@AllArgsConstructor
public class ErrorResponse {
    private String code;         // USER_NOT_FOUND
    private String message;      // Usuario con ID 123 no encontrado
    private int status;          // 404
    private LocalDateTime timestamp;
    
    public ErrorResponse(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}
```

#### Ejemplo del proyecto IOC

[Mostrar GlobalExceptionHandler real y excepciones custom]

---

### 📂 Carpeta: `config/` (Configuraciones)

#### ¿Qué contiene?

Clases `@Configuration` que personalizan el comportamiento de Spring Boot:
- **SecurityConfig**: JWT, CORS, autorización
- **DatabaseConfig**: DataSource, conexiones, pools
- **WebConfig**: CORS, interceptors, message converters
- **AsyncConfig**: Thread pools para @Async
- **CacheConfig**: Configuración de caché

#### ¿Para qué sirve?

**Personalizar beans** que Spring crea automáticamente, o crear nuevos.

#### Ejemplo: SecurityConfig

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Deshabilitado para REST API
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter()))
            );
        return http.build();
    }
    
    @Bean
    public JwtAuthenticationConverter jwtConverter() {
        // Mapear claims del JWT a authorities de Spring Security
    }
}
```

#### Ejemplo del proyecto IOC

[Mostrar configuraciones reales y explicar cada bean]

---

### 📂 Carpeta: `security/` (Seguridad)

#### ¿Qué contiene?

Componentes personalizados de seguridad:
- **Filtros JWT**: Validación de tokens
- **Authentication Providers**: Autenticación custom
- **Access Decision Voters**: Lógica de autorización compleja
- **Password Encoders**: Bcrypt, etc.

#### Ejemplo: JWT Filter

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                     HttpServletResponse response, 
                                     FilterChain filterChain) {
        String token = extractToken(request);
        if (token != null && jwtValidator.isValid(token)) {
            Authentication auth = jwtConverter.convert(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
```

[Explicar con detalle si existe en el proyecto]

---

## ✅ Checklist de Comprensión

Antes de pasar al Archivo 2, asegúrate de que puedes responder:

- [ ] ¿Cuál es la diferencia entre Entity y DTO?
- [ ] ¿Por qué NUNCA debes exponer Entities en la API?
- [ ] ¿Qué hace cada capa? (Controller, Service, Repository)
- [ ] ¿Cuándo usar Query Methods vs @Query?
- [ ] ¿Qué responsabilidades tiene un Controller?
- [ ] ¿Qué responsabilidades tiene un Service?
- [ ] ¿Para qué sirven los Mappers?
- [ ] ¿Por qué usar @Transactional?
- [ ] ¿Cómo funciona el GlobalExceptionHandler?

Si respondiste "No sé" a alguna, **relee esa sección** antes de continuar.

---

## 🗺️ Navegación

**Archivos de esta serie**:
1. ✅ **01-IOC-Vision-General.md** (estás aquí)
2. ➡️ **02-IOC-Analisis-Detallado.md** (análisis archivo por archivo)
3. ➡️ **03-IOC-Resumen-Produccion.md** (resumen + seguridad)

**Próximo paso**: Una vez que comprendas todos los conceptos de este archivo, continúa con el análisis detallado de cada archivo del proyecto en `02-IOC-Analisis-Detallado.md`.

---

**Fecha de generación**: [Fecha]  
**Versión**: 1.0  
**Autor**: Análisis asistido por IA

---

✅ **Archivo `01-IOC-Vision-General.md` completado.**

📝 **Acción**: Copia todo el contenido de arriba y guárdalo como `01-IOC-Vision-General.md`

¿Quieres que continúe con el **Archivo 2: `02-IOC-Analisis-Detallado.md`**?  
Responde **"CONTINUAR"** o **"PAUSA"**
```

---

## 🎯 CONTENIDO DEL ARCHIVO 2: `02-IOC-Analisis-Detallado.md`

[Instrucciones similares con estructura para análisis archivo por archivo]

---

## 🎯 CONTENIDO DEL ARCHIVO 3: `03-IOC-Resumen-Produccion.md`

[Instrucciones para resumen, glosario, seguridad y plan de estudio]

---

## ⚙️ RESTRICCIONES GENERALES

✅ **SÍ hacer en TODOS los archivos**:
- Usar lenguaje pedagógico (para junior developer)
- Explicar el "por qué" además del "qué"
- Usar analogías del mundo real
- Incluir ejemplos simplificados cuando el código sea complejo
- Usar diagramas ASCII para flujos
- Usar emojis para mejorar legibilidad
- Conectar cada concepto con Spring Boot

❌ **NO hacer**:
- Explicar imports básicos (java.util.*, org.springframework.*, etc.)
- Copiar código completo sin agregar valor explicativo
- Usar jerga técnica sin explicarla primero
- Asumir conocimiento previo de patrones complejos

---

## 📏 LÍMITES DE TAMAÑO

- **Archivo 1**: 3000-4000 palabras
- **Archivo 2**: 5000-7000 palabras (análisis de múltiples archivos)
- **Archivo 3**: 2000-3000 palabras
- **Snippets de código**: Máximo 20 líneas, enfocarse en lo relevante
- **Si un archivo tiene >10 métodos**: Agrupar por funcionalidad

---

## 🚀 INICIO DE EJECUCIÓN

**Comienza ahora generando el Archivo 1: `01-IOC-Vision-General.md`**

Sigue la estructura EXACTA proporcionada arriba, reemplazando los placeholders con análisis real del proyecto ubicado en:

`src/main/java/com/cambiaso/iocbackend`

Al terminar el Archivo 1, espera mi confirmación para continuar con el Archivo 2.

**¡ADELANTE! 🚀**
```

---

## ✅ **Instrucciones de Uso**

1. **Copia el prompt completo** de arriba
2. **Pégalo en tu asistente de IA** (Claude, ChatGPT, etc.)
3. El asistente generará **Archivo 1** completo
4. **Guarda** el contenido como `01-IOC-Vision-General.md`
5. Responde **"CONTINUAR"** para generar Archivo 2
6. Repite para Archivo 3

¡Listo! 🎉