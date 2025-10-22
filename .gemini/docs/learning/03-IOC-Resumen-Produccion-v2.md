# 🎓 IOC Backend - Resumen, Patrones y Preparación para Producción (VERSIÓN COMPLETA)

> **Proyecto**: Inteligencia Operacional Cambiaso (IOC)  
> **Framework**: Spring Boot 3.5.5 + Java 21  
> **Fecha de Análisis**: 2025-10-22  
> **Nivel**: Junior Developer  
> **Versión**: 2.0 - Completa y Mejorada

---

## 📘 Sobre Este Documento

**Propósito**: Este es el **documento final** de la serie de análisis del proyecto IOC. Aquí consolidamos todo lo aprendido y te proporcionamos un **plan de acción concreto** para seguir creciendo como desarrollador Spring Boot.

**Qué encontrarás aquí**:
- ✅ Checklist completa de autoevaluación
- ✅ Patrones de diseño con ejemplos reales
- ✅ Mejores prácticas observadas en el proyecto
- ✅ Análisis profundo de seguridad, resiliencia y observabilidad
- ✅ Ejercicios prácticos hands-on
- ✅ Plan de estudio de 8 semanas con recursos
- ✅ Próximos pasos claros

**Tiempo de lectura**: 60-90 minutos  
**Tiempo de práctica sugerido**: 40-60 horas (siguiendo el plan completo)

---

## 📋 Índice

1. [Introducción](#introducción)
2. [FASE 4: Resumen de Aprendizaje y Patrones](#fase-4-resumen-de-aprendizaje-y-patrones)
   - [Checklist de Autoevaluación](#checklist-de-autoevaluación)
   - [Patrones de Diseño Identificados](#patrones-de-diseño-identificados)
   - [Mejores Prácticas Observadas](#mejores-prácticas-observadas)
   - [Glosario Rápido de Anotaciones](#glosario-rápido-de-anotaciones)
3. [FASE 5: Análisis para Producción](#fase-5-análisis-para-producción)
   - [Análisis de Seguridad Profundo](#análisis-de-seguridad-profundo)
   - [Análisis de Resiliencia](#análisis-de-resiliencia)
   - [Análisis de Observabilidad](#análisis-de-observabilidad)
   - [Mejoras Propuestas](#mejoras-propuestas)
4. [Ejercicios Prácticos](#ejercicios-prácticos)
5. [Plan de Estudio Completo (8 Semanas)](#plan-de-estudio-completo-8-semanas)
6. [Mapa de Navegación por Concepto](#mapa-de-navegación-por-concepto)
7. [¿Qué Hacer Ahora?](#qué-hacer-ahora)
8. [Recursos de Aprendizaje](#recursos-de-aprendizaje)
9. [Navegación](#navegación)

---

## 🚀 Introducción

Has completado un recorrido profundo por el proyecto IOC Backend. Ahora es momento de **consolidar** lo aprendido y **planificar** tu crecimiento.

**Lo que has logrado hasta ahora**:
- ✅ Entiendes la arquitectura en capas del proyecto
- ✅ Conoces las responsabilidades de cada capa
- ✅ Has visto código real de producción
- ✅ Comprendes conceptos avanzados (CSP, JWT, Async)

**Lo que sigue**:
- 🎯 Autoevaluar tu comprensión
- 🏋️ Practicar con ejercicios reales
- 📚 Seguir un plan de estudio estructurado
- 🚀 Aplicar lo aprendido en tus propios proyectos

---

## FASE 4: Resumen de Aprendizaje y Patrones

### ✅ Checklist de Autoevaluación

Usa esta lista para medir tu comprensión. **Sé honesto contigo mismo**. Si no puedes explicar un concepto con tus propias palabras, márcalo como "pendiente" y revisa la sección correspondiente.

#### **Arquitectura en Capas**

- [ ] **Puedo explicar** la responsabilidad de un `@RestController` sin ver código
- [ ] **Puedo explicar** la responsabilidad de un `@Service` sin ver código
- [ ] **Puedo explicar** la responsabilidad de un `@Repository` sin ver código
- [ ] **Sé por qué** un `Controller` NO debe hablar directamente con un `Repository`
- [ ] **Puedo dibujar** el flujo de datos desde una petición HTTP hasta la base de datos

**Dónde repasar si fallas**:
- Archivo 1, sección "Patrón Arquitectónico"
- Archivo 1, sección "Flujo de Datos Principal"

---

#### **Manejo de Datos**

- [ ] **Puedo explicar** la diferencia fundamental entre una `@Entity` y un DTO
- [ ] **Conozco al menos 3 razones** por las que NO se deben exponer `Entities` en una API:
  1. Seguridad (campos sensibles)
  2. Lazy Loading (LazyInitializationException)
  3. Acoplamiento (cambios en BD rompen API)
- [ ] **Entiendo** el propósito de un `Mapper` (MapStruct)
- [ ] **Puedo crear** un DTO de Request y Response para una entidad nueva

**Dónde repasar si fallas**:
- Archivo 1, tabla "Entity vs DTO"
- Archivo 2, sección "¿Por qué NUNCA exponer Entities?"

---

#### **Spring Core**

- [ ] **Entiendo** qué es la Inyección de Dependencias
- [ ] **Puedo explicar** cómo funciona `@RequiredArgsConstructor` de Lombok
- [ ] **Sé** qué hace la anotación `@SpringBootApplication` (3 comportamientos)
- [ ] **Entiendo** el propósito de una clase `@Configuration`
- [ ] **Puedo explicar** cuándo usar `@Bean`

**Dónde repasar si fallas**:
- Archivo 1, sección "Clase Principal"
- Archivo 2, análisis de `IocbackendApplication.java`

---

#### **Base de Datos (JPA/Hibernate)**

- [ ] **Entiendo** qué hace la anotación `@Transactional` en un método de servicio
- [ ] **Sé** la diferencia entre `FetchType.LAZY` y `FetchType.EAGER`
- [ ] **Entiendo** qué es un "Query Method" en `JpaRepository`
- [ ] **Puedo crear** un Query Method para buscar por 2 campos
- [ ] **Sé cuándo** usar `@Query` en vez de Query Method

**Dónde repasar si fallas**:
- Archivo 1, sección "Carpeta repository/"
- Archivo 2, ejemplos de Query Methods

---

#### **API REST y Validación**

- [ ] **Entiendo** cómo funciona un `GlobalExceptionHandler` con `@RestControllerAdvice`
- [ ] **Sé** la diferencia entre `@PathVariable` y `@RequestParam`
- [ ] **Puedo explicar** qué hace `@Valid` en un parámetro de un Controller
- [ ] **Conozco** 3 anotaciones de Bean Validation (`@NotNull`, `@Email`, `@Size`)

**Dónde repasar si fallas**:
- Archivo 1, sección "Carpeta exception/"
- Archivo 2, análisis de Controllers

---

#### **Seguridad Avanzada**

- [ ] **Entiendo** qué es JWT y cómo se valida
- [ ] **Puedo explicar** qué es Content Security Policy (CSP)
- [ ] **Sé** por qué `'unsafe-inline'` es un trade-off de seguridad
- [ ] **Entiendo** la diferencia entre CSRF y CORS
- [ ] **Sé** por qué CSRF está desactivado en este proyecto

**Dónde repasar si fallas**:
- Archivo 2, sección "Content Security Policy - Fundamentos"
- Archivo 2, sección "Trade-offs de Seguridad"

---

#### **Concurrencia y Asincronía**

- [ ] **Entiendo** el propósito de `@Async`
- [ ] **Puedo explicar** qué es un `ThreadPoolTaskExecutor`
- [ ] **Sé** cuándo usar `@Async` vs procesamiento síncrono

**Dónde repasar si fallas**:
- Archivo 2, sección "AsyncConfig.java"

---

### 🎨 Patrones de Diseño Identificados

El proyecto IOC aplica varios patrones de diseño de forma muy clara. Reconocerlos te ayudará a diseñar mejor tus propias aplicaciones.

---

#### **1. Inyección de Dependencias (Dependency Injection)**

**Descripción**:  
Un objeto no crea sus propias dependencias, sino que se le "inyectan" desde fuera (generalmente por el framework). Esto invierte el control: en vez de que tu código llame al framework, el framework llama a tu código.

**Ejemplo en el proyecto**:

```java
@Service
@RequiredArgsConstructor // Lombok genera constructor con todos los campos final
public class UserAdminServiceImpl implements UserAdminService {
    
    // Estas dependencias son INYECTADAS por Spring
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    
    // Spring crea instancias de los repositorios y el mapper,
    // luego los pasa al constructor generado por Lombok
}
```

**Cómo funciona internamente**:

```
1. Spring escanea y encuentra @Service UserAdminServiceImpl
2. Ve que necesita AppUserRepository, RoleRepository, UserMapper
3. Busca beans de esos tipos (ya creados previamente)
4. Crea una instancia de UserAdminServiceImpl llamando:
   new UserAdminServiceImpl(appUserRepo, roleRepo, userMapper)
5. Registra esta instancia como bean para futuras inyecciones
```

**Beneficios**:

| Beneficio | Explicación | Ejemplo |
|-----------|-------------|---------|
| **Desacoplamiento** | Las clases no saben cómo se crean sus dependencias | `UserService` no sabe que `AppUserRepository` es JPA |
| **Testeabilidad** | Puedes inyectar Mocks en tests | `@InjectMocks UserService` + `@Mock AppUserRepository` |
| **Flexibilidad** | Cambiar implementación sin tocar código | Cambiar `JpaRepository` por `MongoRepository` |
| **Mantenibilidad** | Reduce código boilerplate | No `new UserRepository()` por todos lados |

**Cuándo aplicar en tus proyectos**:
- ✅ SIEMPRE en aplicaciones Spring Boot
- ✅ Prefiere Constructor Injection (lo que hace `@RequiredArgsConstructor`)
- ❌ Evita Field Injection (`@Autowired` en campos directamente)

---

#### **2. Patrón de Repositorio (Repository Pattern)**

**Descripción**:  
Actúa como una **colección en memoria** de objetos del dominio, abstrayendo completamente el mecanismo de persistencia.

**Ejemplo en el proyecto**:

```java
// La interfaz define el CONTRATO
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}

// El Service usa el repositorio como si fuera una colección
@Service
public class UserService {
    private final AppUserRepository userRepository;
    
    public UserResponse getByEmail(String email) {
        // No sé si esto viene de PostgreSQL, MongoDB o un archivo
        // Solo sé que el repositorio me trae el usuario
        AppUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));
        return mapper.toResponse(user);
    }
}
```

**Beneficios**:

| Beneficio | Explicación |
|-----------|-------------|
| **Abstracción** | La capa de negocio no sabe de SQL, tablas ni conexiones |
| **Testabilidad** | Puedes crear un `InMemoryUserRepository` para tests |
| **Flexibilidad** | Cambiar de PostgreSQL a MongoDB requiere solo cambiar la implementación del repositorio |

**Cuándo aplicar**:
- ✅ Siempre que accedas a datos (BD, API externa, archivos)
- ✅ Define interfaces, no implementaciones concretas
- ✅ Mantén los repositorios "tontos" (sin lógica de negocio)

---

#### **3. Patrón DTO (Data Transfer Object)**

**Descripción**:  
Objeto simple cuyo **único propósito** es transferir datos entre procesos o capas. No tiene lógica de negocio.

**Ejemplo en el proyecto**:

```java
// DTO de Request (lo que envía el frontend)
public class UserCreateRequest {
    private String email;
    private String primerNombre;
    private String primerApellido;
    private UUID supabaseUserId;
    // Sin lógica, solo getters/setters
}

// DTO de Response (lo que devuelve el backend)
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;  // ← Campo calculado
    private List<String> roles;
    // Sin lógica, solo getters/setters
}
```

**Por qué es crítico**:

```java
// ❌ SIN DTOs (malo)
@PostMapping("/users")
public AppUser create(@RequestBody AppUser user) {
    // Problema 1: Frontend puede setear cualquier campo (id, createdAt, etc.)
    // Problema 2: Expones estructura interna de BD al mundo
    // Problema 3: No puedes agregar campos calculados
    return userRepository.save(user);
}

// ✅ CON DTOs (bien)
@PostMapping("/users")
public UserResponse create(@Valid @RequestBody UserCreateRequest request) {
    // Controlas exactamente qué se recibe
    // Controlas exactamente qué se devuelve
    // Puedes agregar validaciones (@NotNull, @Email)
    AppUser user = userMapper.toEntity(request);
    AppUser saved = userRepository.save(user);
    return userMapper.toResponse(saved);
}
```

**Cuándo aplicar**:
- ✅ SIEMPRE en APIs REST públicas
- ✅ Request DTO diferente a Response DTO
- ✅ Usa validaciones (`@Valid` + Bean Validation)

---

#### **4. Patrón Mapper**

**Descripción**:  
Componente especializado en **transformar** objetos de un tipo a otro.

**Ejemplo en el proyecto**:

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    // Entity → Response DTO
    @Mapping(target = "fullName", expression = "java(buildFullName(user))")
    @Mapping(target = "plantaCode", source = "planta.code")
    UserResponse toResponse(AppUser user);
    
    // Request DTO → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    AppUser toEntity(UserCreateRequest request);
    
    // Helper para campo calculado
    default String buildFullName(AppUser user) {
        return Stream.of(
            user.getPrimerNombre(),
            user.getSegundoNombre(),
            user.getPrimerApellido(),
            user.getSegundoApellido()
        ).filter(Objects::nonNull).collect(Collectors.joining(" "));
    }
}
```

**Por qué usar MapStruct**:

| Aspecto | Manual | MapStruct |
|---------|--------|-----------|
| **Performance** | ⚡⚡⚡ Nativa | ⚡⚡⚡ Nativa (genera Java puro) |
| **Type-Safety** | ❌ Errores en runtime | ✅ Errores en compilación |
| **Mantenibilidad** | ❌ Mucho código repetitivo | ✅ Declarativo |
| **Debugging** | ✅ Código visible | ✅ Código generado visible |

**Cuándo aplicar**:
- ✅ Cuando tengas muchas conversiones Entity ↔ DTO
- ✅ Usa MapStruct en vez de escribir manualmente
- ✅ Define helpers (`default`) para campos calculados

---

#### **5. Patrón Facade (a nivel de Service)**

**Descripción**:  
Proporciona una **interfaz simplificada** a un conjunto complejo de subsistemas.

**Ejemplo en el proyecto**:

```java
// El Controller solo ve esta interfaz simple
public interface UserAdminService {
    UserResponse createUser(UserCreateRequest request);
}

// Internamente, el Service orquesta múltiples subsistemas
@Service
public class UserAdminServiceImpl implements UserAdminService {
    
    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;
    
    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // Paso 1: Validar con AppUserRepository
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException();
        }
        
        // Paso 2: Convertir con UserMapper
        AppUser user = userMapper.toEntity(request);
        
        // Paso 3: Persistir con AppUserRepository
        AppUser saved = userRepository.save(user);
        
        // Paso 4: Asignar roles con RoleRepository + UserRoleRepository
        for (String roleName : request.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName));
            userRoleRepository.save(new UserRole(saved.getId(), role.getId()));
        }
        
        // Paso 5: Convertir respuesta con UserMapper
        return userMapper.toResponse(saved);
    }
}
```

**Beneficio**:  
El Controller solo llama `createUser()` y no necesita saber que internamente se coordinan 3 repositorios y 2 mappers.

**Cuándo aplicar**:
- ✅ Cuando un caso de uso requiere orquestar múltiples componentes
- ✅ Mantén los Controllers delgados (thin controllers)
- ✅ La complejidad va en el Service, no en el Controller

---

### ✨ Mejores Prácticas Observadas en Este Proyecto

Estas prácticas hacen que el código sea **profesional** y **mantenible**. Cópialas en tus proyectos.

---

#### **1. Separación Estricta de Responsabilidades**

**Qué hace el proyecto**:

```java
// ✅ Controller: SOLO HTTP
@PostMapping
public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest req) {
    // No lógica de negocio aquí
    UserResponse created = userService.create(req);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}

// ✅ Service: SOLO lógica de negocio
@Transactional
public UserResponse create(UserCreateRequest req) {
    // Validación de negocio
    if (userRepository.existsByEmail(req.getEmail())) {
        throw new UserAlreadyExistsException();
    }
    // Transformación
    AppUser user = userMapper.toEntity(req);
    // Persistencia
    AppUser saved = userRepository.save(user);
    return userMapper.toResponse(saved);
}

// ✅ Repository: SOLO acceso a datos
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    boolean existsByEmail(String email);
}
```

**Por qué es importante**:

| Beneficio | Explicación |
|-----------|-------------|
| **Testeabilidad** | Puedes testear cada capa aisladamente con mocks |
| **Mantenibilidad** | Cambios en HTTP no afectan lógica de negocio |
| **Reusabilidad** | El mismo Service puede ser usado por REST API, GraphQL, Batch Jobs |

**Cópialo en tus proyectos**:
- ❌ NO pongas lógica de negocio en Controllers
- ❌ NO pongas lógica HTTP (`ResponseEntity`) en Services
- ❌ NO pongas queries SQL en Controllers

---

#### **2. DTOs para TODO en la API**

**Qué hace el proyecto**:
- ✅ NUNCA expone `@Entity` directamente
- ✅ Request DTOs separados de Response DTOs
- ✅ Validaciones en Request DTOs (`@Valid`)
- ✅ Mappers automáticos (MapStruct)

**Estructura típica**:

```
dto/
├── request/
│   ├── UserCreateRequest.java    (POST)
│   └── UserUpdateRequest.java    (PUT)
└── response/
    ├── UserResponse.java          (GET /users/{id})
    └── UserListResponse.java      (GET /users)
```

**Por qué es importante**:
- Permite evolucionar BD sin romper API
- Previene exposición de datos sensibles
- Facilita versionado de API (v1, v2)

---

#### **3. Uso Consistente de `@Transactional`**

**Qué hace el proyecto**:

```java
@Service
@Transactional  // ← A nivel de clase (todas las operaciones son transaccionales)
public class UserAdminServiceImpl {
    
    @Transactional(readOnly = true)  // ← Optimización para reads
    public UserResponse getUserById(Long id) {
        // ...
    }
    
    // Los métodos de escritura heredan @Transactional de la clase
    public UserResponse createUser(UserCreateRequest request) {
        // Si algo falla aquí, TODO hace rollback automático
    }
}
```

**Por qué es importante**:
- Garantiza consistencia de datos (ACID)
- Rollback automático si hay excepciones
- `readOnly=true` optimiza queries SELECT

**Regla de oro**:
- ✅ `@Transactional` en Service, NO en Controller
- ✅ `readOnly=true` en métodos que solo leen
- ✅ Propaga la transacción por defecto

---

#### **4. Validación en Múltiples Capas**

**Qué hace el proyecto**:

```java
// Capa 1: Validación de FORMATO en DTO
public class UserCreateRequest {
    @NotNull(message = "Email es requerido")
    @Email(message = "Formato de email inválido")
    private String email;
    
    @NotBlank
    @Size(min = 2, max = 100)
    private String primerNombre;
}

// Capa 2: Validación de FORMATO en Controller
@PostMapping
public UserResponse create(@Valid @RequestBody UserCreateRequest req) {
    // @Valid ejecuta validaciones del DTO
    // Si falla, lanza MethodArgumentNotValidException → 400 Bad Request
}

// Capa 3: Validación de NEGOCIO en Service
public UserResponse create(UserCreateRequest req) {
    if (userRepository.existsByEmail(req.getEmail())) {
        throw new UserAlreadyExistsException();  // ← Regla de negocio
    }
}
```

**Defensa en profundidad**:
- **Capa 1+2**: Formato (email válido, no vacío)
- **Capa 3**: Reglas de negocio (unicidad, permisos)

---

#### **5. Health Checks Profundos (Avanzado)**

**Qué hace el proyecto**:

```java
@Component
public class EtlHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // No solo verifica que la BD esté viva
        // Verifica INTEGRIDAD LÓGICA de los datos
        
        long duplicates = etlRepository.countDuplicateIndexes();
        if (duplicates > 0) {
            return Health.down()
                .withDetail("reason", "Duplicados detectados en índices ETL")
                .withDetail("count", duplicates)
                .build();
        }
        
        return Health.up().build();
    }
}
```

**Por qué es brillante**:
- Va más allá de "¿está viva la BD?"
- Detecta problemas de datos en producción
- Integra con sistemas de monitoreo (Kubernetes, Prometheus)

**Cópialo en tus proyectos**:
- ✅ Crea health checks para cada dependencia crítica
- ✅ Verifica integridad lógica, no solo disponibilidad
- ✅ Devuelve detalles útiles para debugging

---

#### **6. Manejo Centralizado de Errores**

**Qué hace el proyecto**:

```java
@RestControllerAdvice  // ← Intercepta TODAS las excepciones de controllers
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(UserNotFoundException ex) {
        return new ErrorResponse("USER_NOT_FOUND", ex.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidation(MethodArgumentNotValidException ex) {
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

**Por qué es importante**:
- Frontend siempre recibe JSON con formato consistente
- No hay duplicación de código de manejo de errores
- Fácil agregar logging centralizado

---

### 📚 Glosario Rápido de Anotaciones

**Referencia rápida** para cuando necesites recordar qué hace una anotación.

#### **Core Spring Boot**

| Anotación | Qué hace | Dónde se usa |
|-----------|----------|--------------|
| `@SpringBootApplication` | Inicia aplicación Spring Boot (auto-config + component scan + config) | Clase main |
| `@Configuration` | Marca clase como fuente de beans | Clases de config |
| `@Bean` | Declara un bean gestionado por Spring | Métodos en @Configuration |
| `@Component` | Marca clase como bean genérico | Utilidades |
| `@Service` | Marca clase como servicio de negocio | Capa de servicio |
| `@Repository` | Marca interfaz como repositorio de datos | Capa de persistencia |
| `@RestController` | Combina @Controller + @ResponseBody (devuelve JSON) | Capa de presentación |
| `@Autowired` | Inyecta dependencia (preferir constructor injection) | Campos/constructores |
| `@RequiredArgsConstructor` | (Lombok) Genera constructor con campos final | Classes con DI |

#### **JPA/Hibernate**

| Anotación | Qué hace | Ejemplo |
|-----------|----------|---------|
| `@Entity` | Marca clase como entidad JPA | `@Entity public class User` |
| `@Table(name = "...")` | Especifica nombre de tabla | `@Table(name = "app_users")` |
| `@Id` | Marca clave primaria | `@Id private Long id` |
| `@GeneratedValue` | Auto-incremento de PK | `@GeneratedValue(strategy = IDENTITY)` |
| `@Column(name = "...")` | Mapea campo a columna | `@Column(name = "first_name")` |
| `@ManyToOne` | Relación muchos-a-uno | Un usuario → una planta |
| `@OneToMany` | Relación uno-a-muchos | Una planta → muchos usuarios |
| `@ManyToMany` | Relación muchos-a-muchos | Usuarios ↔ Roles |
| `@JoinColumn` | Especifica nombre de FK | `@JoinColumn(name = "planta_id")` |
| `@Transactional` | Ejecuta método en transacción | Métodos de Service |

#### **REST API**

| Anotación | Qué hace | Ejemplo |
|-----------|----------|---------|
| `@RequestMapping("/path")` | Define ruta base del controller | `@RequestMapping("/api/v1/users")` |
| `@GetMapping` | Mapea a GET HTTP | `@GetMapping("/{id}")` |
| `@PostMapping` | Mapea a POST HTTP | `@PostMapping` |
| `@PutMapping` | Mapea a PUT HTTP | `@PutMapping("/{id}")` |
| `@DeleteMapping` | Mapea a DELETE HTTP | `@DeleteMapping("/{id}")` |
| `@PathVariable` | Extrae variable de URL | `GET /users/{id}` |
| `@RequestParam` | Extrae parámetro de query | `GET /users?search=john` |
| `@RequestBody` | Deserializa JSON a objeto | `@RequestBody UserRequest req` |
| `@ResponseStatus` | Define status HTTP | `@ResponseStatus(HttpStatus.CREATED)` |
| `@Valid` | Activa validación Bean Validation | `@Valid @RequestBody UserRequest` |

#### **Validación (Bean Validation)**

| Anotación | Qué valida | Ejemplo |
|-----------|------------|---------|
| `@NotNull` | Campo no puede ser null | `@NotNull private String email` |
| `@NotBlank` | String no puede estar vacío | `@NotBlank private String name` |
| `@Email` | Formato de email | `@Email private String email` |
| `@Size(min, max)` | Longitud de string/colección | `@Size(min=2, max=100)` |
| `@Min` / `@Max` | Rango numérico | `@Min(18) private int age` |
| `@Pattern(regexp)` | Expresión regular | `@Pattern(regexp="\\d{10}")` |

#### **Seguridad**

| Anotación | Qué hace | Ejemplo |
|-----------|----------|---------|
| `@EnableWebSecurity` | Activa configuración de seguridad | Clase SecurityConfig |
| `@EnableMethodSecurity` | Permite seguridad a nivel de método | Clase SecurityConfig |
| `@PreAuthorize` | Valida autorización ANTES del método | `@PreAuthorize("hasRole('ADMIN')")` |
| `@PostAuthorize` | Valida autorización DESPUÉS del método | `@PostAuthorize("returnObject.owner == authentication.name")` |

#### **Manejo de Errores**

| Anotación | Qué hace | Ejemplo |
|-----------|----------|---------|
| `@RestControllerAdvice` | Handler global de excepciones | Clase GlobalExceptionHandler |
| `@ExceptionHandler` | Maneja tipo específico de excepción | `@ExceptionHandler(UserNotFoundException.class)` |

#### **Asincronía y Scheduling**

| Anotación | Qué hace | Ejemplo |
|-----------|----------|---------|
| `@EnableAsync` | Activa soporte @Async | Clase AsyncConfig |
| `@Async` | Ejecuta método en hilo separado | `@Async("etlExecutor")` |
| `@EnableScheduling` | Activa soporte @Scheduled | Clase main |
| `@Scheduled` | Ejecuta método en intervalo fijo | `@Scheduled(cron = "0 */5 * * * *")` |

---

## FASE 5: Análisis para Producción

Una aplicación no solo debe **funcionar**, debe ser **segura**, **robusta** y **observable** en producción.

---

### 🛡️ Análisis de Seguridad Profundo

#### **1. SQL Injection**

**Estado**: ✅ **PROTEGIDO (Muy Alto)**

**Cómo se protege**:

```java
// ✅ SEGURO: JPA parametriza automáticamente
Optional<AppUser> findByEmail(String email);
// SQL generado: SELECT * FROM app_users WHERE email = ?
// El valor de 'email' se envía como parámetro, NO concatenado

// ✅ SEGURO: @Query con parámetros nombrados
@Query("SELECT u FROM AppUser u WHERE u.email = :email")
AppUser findByEmailCustom(@Param("email") String email);
// Parámetros siempre separados del SQL
```

**Qué evita**:

```java
// ❌ VULNERABLE (nunca hacer esto):
String sql = "SELECT * FROM app_users WHERE email = '" + email + "'";
// Si email = "'; DROP TABLE app_users; --"
// SQL final: SELECT * FROM app_users WHERE email = ''; DROP TABLE app_users; --'
```

**Verificación en el código**:
1. ✅ No hay construcción de strings SQL con `+` o `concat`
2. ✅ Todos los `@Query` usan parámetros nombrados (`:param`)
3. ✅ No hay `createNativeQuery(String sql + variable)`

**Nivel de protección**: 🟢 **MUY ALTO**

---

#### **2. XSS (Cross-Site Scripting)**

**Estado**: ⚠️ **RESPONSABILIDAD COMPARTIDA (Medio)**

**Análisis**:

```java
// Backend NO sanitiza HTML al guardar
@PostMapping("/users")
public UserResponse create(@Valid @RequestBody UserCreateRequest req) {
    // Si req.primerNombre = "<script>alert('XSS')</script>"
    // Se guarda TAL CUAL en la base de datos
}
```

**Mitigación actual**:
- Frontend (React) escapa automáticamente al renderizar: `{user.primerNombre}`
- Content Security Policy bloquea scripts inline

**Recomendación de mejora**:

Agregar sanitización en backend:

```java
@Component
public class HtmlSanitizer {
    public String sanitize(String input) {
        if (input == null) return null;
        return Jsoup.clean(input, Safelist.none());
        // Safelist.none() = remueve TODOS los tags HTML
    }
}

// Uso en Service:
@Service
public class UserServiceImpl {
    private final HtmlSanitizer sanitizer;
    
    public UserResponse create(UserCreateRequest req) {
        AppUser user = new AppUser();
        user.setPrimerNombre(sanitizer.sanitize(req.getPrimerNombre()));
        // ...
    }
}
```

**Nivel actual**: 🟡 **MEDIO**  
**Nivel con sanitización**: 🟢 **ALTO**

---

#### **3. CSRF (Cross-Site Request Forgery)**

**Estado**: ✅ **PROTEGIDO (Correcto para API REST)**

**Decisión del proyecto**:

```java
http.csrf(AbstractHttpConfigurer::disable)
```

**Por qué es correcto desactivarlo**:

| Característica | App Web Tradicional | API REST (IOC) |
|----------------|---------------------|----------------|
| **Autenticación** | Cookies de sesión | JWT en header `Authorization` |
| **Estado** | Stateful (sesión en servidor) | Stateless (cada petición independiente) |
| **CSRF aplica** | ✅ SÍ (cookies se envían automáticamente) | ❌ NO (headers no se envían automáticamente) |

**Análisis de seguridad**:

```
Ataque CSRF típico:
1. Usuario autenticado en banco.com
2. Abre email malicioso con: <img src="banco.com/transferir?a=atacante&monto=1000">
3. Navegador envía cookies de banco.com automáticamente
4. ¡Transferencia exitosa sin que el usuario lo sepa!

¿Por qué IOC no es vulnerable?
1. Usuario autenticado en ioc.cambiaso.com
2. Abre email malicioso con: <img src="ioc.cambiaso.com/api/users/delete">
3. Navegador NO envía el header "Authorization: Bearer <token>" automáticamente
4. Backend rechaza con 401 (no hay token)
5. ✅ Ataque bloqueado
```

**Nivel de protección**: 🟢 **CORRECTO**

---

#### **4. JWT - Validación Criptográfica**

**Estado**: ✅ **ROBUSTO (Muy Alto)**

**Cómo se valida**:

```java
@Bean
public JwtDecoder jwtDecoder() {
    String jwkSetUri = issuerUri + "/.well-known/jwks.json";
    return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
            .jwsAlgorithm(SignatureAlgorithm.ES256)
            .build();
}
```

**Flujo de validación**:

```
1. Frontend envía: Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGc...
                                         ↓
2. Spring Security extrae el token
                                         ↓
3. JwtDecoder descarga llaves públicas de Supabase (JWKS)
   GET https://supabase.io/.well-known/jwks.json
                                         ↓
4. Verifica firma criptográfica con algoritmo ES256
   - ¿El token fue firmado por Supabase? ✅/❌
   - ¿El token no ha sido modificado? ✅/❌
                                         ↓
5. Verifica claims:
   - ¿exp (expiration) > now()? ✅/❌
   - ¿iss (issuer) == supabase? ✅/❌
   - ¿aud (audience) == expected? ✅/❌
                                         ↓
6. Si TODO es válido → Authentication creado
   Si ALGO falla → 401 Unauthorized
```

**Por qué es seguro**:
- ✅ Usa criptografía asimétrica (ES256)
- ✅ Llaves públicas obtenidas de fuente confiable (Supabase)
- ✅ Verificación automática de expiración
- ✅ No almacena secretos en el backend (solo valida con llave pública)

**Nivel de protección**: 🟢 **MUY ALTO**

---

#### **5. Content Security Policy (CSP)**

**Estado**: ⚠️ **COMPROMISO CALCULADO (requiere `'unsafe-inline'`)**

Ya analizado en detalle en Archivo 2. Resumen:

```java
.contentSecurityPolicy(csp -> csp
    .policyDirectives("frame-ancestors 'self' https://...cloudflare.com; " +
                      "style-src 'self' 'unsafe-inline'; " +  // ← Trade-off
                      "default-src 'self'")
)
```

**Trade-off**:
- ❌ `'unsafe-inline'` abre vector XSS
- ✅ Necesario para que Metabase funcione
- ✅ Mitigado con `default-src 'self'` + CORS + validación

**Nivel de protección**: 🟡 **MEDIO (aceptable con mitigaciones)**

---

#### **6. Información Sensible en Logs**

**Estado**: ⚠️ **REQUIERE ATENCIÓN**

**Problema potencial**:

```java
// ❌ PELIGROSO
log.info("Usuario creado: {}", userCreateRequest);
// Si UserCreateRequest contiene password, se logea en texto plano

// ❌ PELIGROSO
log.debug("JWT Token: {}", jwtToken);
// Tokens en logs pueden ser robados
```

**Recomendación**:

```java
// ✅ SEGURO: Solo logear identificadores
log.info("Usuario creado exitosamente. ID: {}, Email: {}", 
         saved.getId(), saved.getEmail());

// ✅ SEGURO: Nunca logear tokens completos
log.debug("JWT validado para usuario: {}", claims.getSubject());

// ✅ SEGURO: Enmascarar datos sensibles
log.info("Procesando pago con tarjeta: ****{}", 
         creditCard.substring(creditCard.length() - 4));
```

**Nivel actual**: 🟡 **MEJORAR (revisar logs)**

---

### 💪 Análisis de Resiliencia

#### **1. Procesamiento Asíncrono**

**Estado**: ✅ **EXCELENTE**

**Qué hace el proyecto**:

```java
@Service
public class EtlService {
    
    @Async("etlExecutor")  // ← Ejecuta en pool de hilos dedicado
    public CompletableFuture<Void> processFileAsync(MultipartFile file) {
        try {
            // Procesamiento largo (30-60 segundos)
            List<Record> records = parseFile(file);
            validateRecords(records);
            saveToDatabase(records);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
```

**Beneficios**:

| Escenario | Sin @Async | Con @Async |
|-----------|------------|------------|
| Usuario sube archivo de 10k filas | Espera 60 seg viendo loading | Recibe 202 en <1 seg |
| 10 usuarios suben archivos simultáneamente | 10 hilos HTTP bloqueados (¡puede saturar Tomcat!) | 10 tareas en cola, HTTP liberado |
| Proceso falla | Usuario ve 500 después de 60 seg | Usuario puede consultar estado del job |

**Configuración del pool de hilos**:

```java
executor.setCorePoolSize(2);     // Mínimo de hilos
executor.setMaxPoolSize(5);      // Máximo de hilos
executor.setQueueCapacity(100);  // Tareas en espera
```

**Política de rechazo**:
```java
executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
// Si se alcanzan 5 hilos + 100 en cola:
// La tarea se ejecuta en el hilo que la invocó (el HTTP thread)
// Efecto: El usuario espera, pero la tarea NO se pierde
```

**Nivel de resiliencia**: 🟢 **MUY ALTO**

---

#### **2. Manejo de Errores Transaccional**

**Estado**: ✅ **ROBUSTO**

**Qué hace `@Transactional`**:

```java
@Transactional
public UserResponse createUser(UserCreateRequest request) {
    // PASO 1: Guardar usuario
    AppUser user = userRepository.save(userMapper.toEntity(request));
    
    // PASO 2: Asignar rol
    Role role = roleRepository.findByName("USER")
        .orElseThrow(() -> new RoleNotFoundException());
    userRoleRepository.save(new UserRole(user.getId(), role.getId()));
    
    // Si PASO 2 falla → ROLLBACK automático del PASO 1
    // No quedan "usuarios huérfanos" sin rol
}
```

**Sin `@Transactional` (desastre)**:

```
PASO 1: ✅ Usuario guardado (ID: 123)
PASO 2: ❌ Rol no encontrado → Exception
Resultado: Usuario 123 existe en BD sin roles → ¡ESTADO INCONSISTENTE!
```

**Con `@Transactional` (correcto)**:

```
PASO 1: Usuario guardado en memoria (no committeado aún)
PASO 2: Exception lanzada
Spring detecta exception → ROLLBACK
Resultado: Usuario NO existe en BD → ✅ Consistencia mantenida
```

**Nivel de resiliencia**: 🟢 **ALTO**

---

#### **3. Circuit Breaker (Resilience4j)**

**Estado**: ⚠️ **CONFIGURADO PERO NO IMPLEMENTADO**

**Qué existe**:

```java
// ResilienceConfig.java existe con configuración
// Pero NO vemos anotaciones @CircuitBreaker en los servicios
```

**Dónde debería estar**:

```java
@Service
public class MetabaseEmbeddingService {
    
    // ⚠️ FALTA: Protección si Metabase se cae
    @CircuitBreaker(name = "metabase", fallbackMethod = "getDefaultDashboardUrl")
    public String getSignedDashboardUrl(Integer dashboardId) {
        // Llama a Metabase API
        // Si falla 5 veces consecutivas, Circuit Breaker se ABRE
        // Siguientes llamadas van directo al fallback (no esperan timeout)
    }
    
    private String getDefaultDashboardUrl(Integer dashboardId, Exception e) {
        // Fallback: devolver URL genérico o cachear último valor conocido
        return "/dashboards/unavailable";
    }
}
```

**Por qué es importante**:

```
Sin Circuit Breaker:
Metabase se cae → Cada petición espera 30 seg (timeout) → Backend se satura

Con Circuit Breaker:
Metabase se cae (5 fallos) → Circuit ABRE → Respuestas instantáneas con fallback
Después de 60 seg → Circuit intenta CERRAR (half-open) → 1 petición de prueba
Si funciona → Circuit CIERRA → Tráfico normal restaurado
```

**Recomendación**: 🔴 **IMPLEMENTAR (alta prioridad)**

---

#### **4. Rate Limiting**

**Estado**: ✅ **IMPLEMENTADO (básico)**

Existe `RateLimitInterceptor` y configuración de Resilience4j para rate limiting.

**Beneficios**:
- Protege contra abuso de API
- Previene ataques DoS
- Asegura fair usage entre usuarios

**Nivel de resiliencia**: 🟢 **ALTO**

---

### 🔭 Análisis de Observabilidad

#### **1. Logging**

**Estado**: ✅ **BUENO**

**Qué se logea**:

```java
@Slf4j
@Service
public class EtlService {
    
    public void processFile(MultipartFile file) {
        log.info("Iniciando proceso ETL. Archivo: {}, Tamaño: {} bytes", 
                 file.getOriginalFilename(), file.getSize());
        
        try {
            // ...
            log.info("Proceso ETL completado. Registros procesados: {}", records.size());
        } catch (Exception e) {
            log.error("Error en proceso ETL. Archivo: {}", file.getOriginalFilename(), e);
            // ↑ Pasar la excepción como último parámetro para incluir stack trace
        }
    }
}
```

**Mejores prácticas observadas**:
- ✅ Logs estructurados con contexto
- ✅ Stack traces incluidos en errores
- ✅ Niveles apropiados (INFO, WARN, ERROR)

**Recomendación de mejora**:

Agregar **Correlation ID** para rastrear peticiones:

```java
@Component
public class CorrelationIdFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);  // ← SLF4J MDC
        
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}

// En logback.xml:
// <pattern>%d [%X{correlationId}] %-5level %logger{36} - %msg%n</pattern>

// Logs resultantes:
// 2025-10-22 10:30:00 [a1b2c3d4] INFO  UserController - Creando usuario...
// 2025-10-22 10:30:01 [a1b2c3d4] INFO  UserService - Usuario validado...
// 2025-10-22 10:30:02 [a1b2c3d4] ERROR EtlService - Error en proceso...
//                     ↑ Mismo ID = misma petición
```

**Nivel actual**: 🟢 **BUENO**  
**Nivel con Correlation ID**: 🟢 **EXCELENTE**

---

#### **2. Métricas (Actuator + Prometheus)**

**Estado**: ✅ **EXCELENTE**

**Qué expone**:

```bash
# Endpoint de métricas
GET http://localhost:8080/actuator/prometheus

# Métricas automáticas expuestas:
- jvm_memory_used_bytes          # Memoria JVM
- jvm_gc_pause_seconds            # Pausas de Garbage Collector
- http_server_requests_seconds    # Latencia de peticiones HTTP
- hikaricp_connections_active     # Conexiones activas a BD
- system_cpu_usage                # Uso de CPU

# Métricas custom del proyecto:
- etl_jobs_total{status="success"}
- etl_jobs_total{status="failed"}
- etl_processing_duration_seconds
```

**Métricas custom (EtlJobMetricsRegistrar)**:

```java
@Component
public class EtlJobMetricsRegistrar {
    
    private final Counter jobsTotal;
    private final Timer processingDuration;
    
    public EtlJobMetricsRegistrar(MeterRegistry registry) {
        this.jobsTotal = Counter.builder("etl_jobs_total")
            .tag("status", "success")
            .register(registry);
        
        this.processingDuration = Timer.builder("etl_processing_duration")
            .register(registry);
    }
    
    public void recordSuccess(Duration duration) {
        jobsTotal.increment();
        processingDuration.record(duration);
    }
}
```

**Nivel de observabilidad**: 🟢 **EXCELENTE**

---

#### **3. Health Checks**

**Estado**: ✅ **EXCELENTE (con health check profundo)**

**Health check básico (Actuator default)**:

```bash
GET /actuator/health

{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

**Health check profundo (EtlHealthIndicator)**:

```java
@Component
public class EtlHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // Verificar integridad de datos, no solo disponibilidad
        long duplicates = etlRepository.countDuplicateIndexes();
        
        if (duplicates > 0) {
            return Health.down()
                .withDetail("reason", "Duplicados detectados en índices ETL")
                .withDetail("count", duplicates)
                .withDetail("action", "Ejecutar cleanup de datos")
                .build();
        }
        
        long orphanedRecords = etlRepository.countOrphanedRecords();
        if (orphanedRecords > 100) {
            return Health.status("DEGRADED")  // Estado intermedio
                .withDetail("reason", "Muchos registros huérfanos")
                .withDetail("count", orphanedRecords)
                .build();
        }
        
        return Health.up()
            .withDetail("lastCheck", LocalDateTime.now())
            .build();
    }
}
```

**Por qué es brillante**:
- Va más allá de "¿está viva la BD?"
- Detecta problemas de integridad de datos
- Kubernetes/AWS puede reiniciar el pod automáticamente si está DOWN

**Nivel de observabilidad**: 🟢 **EXCELENTE (best practice avanzada)**

---

#### **4. Tracing Distribuido**

**Estado**: ❌ **AUSENTE**

**Qué falta**:

```xml
<!-- NO existe en pom.xml -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

**Qué permitiría**:

```
Petición HTTP atraviesa:
┌────────────────────────────────────────────────────┐
│ [Trace ID: abc123]                                 │
│   Span 1: UserController.create() → 50ms          │
│   Span 2: UserService.create() → 200ms            │
│     Span 2.1: AppUserRepository.save() → 30ms     │
│     Span 2.2: RoleRepository.findByName() → 15ms  │
│     Span 2.3: UserRoleRepository.save() → 10ms    │
│   Span 3: UserMapper.toResponse() → 5ms           │
│ TOTAL: 255ms                                       │
└────────────────────────────────────────────────────┘

// Visualizado en Zipkin UI para debug de performance
```

**Recomendación**: 🟡 **NICE TO HAVE (no crítico para monolito)**

---

### 🔧 Mejoras Propuestas (Priorizadas)

| Prioridad | Mejora | Impacto | Esfuerzo | Justificación |
|-----------|--------|---------|----------|---------------|
| 🔴 **ALTA** | Implementar Circuit Breaker en `MetabaseService` | Alto | Bajo (2h) | Previene saturación si Metabase falla |
| 🔴 **ALTA** | Sanitización HTML en inputs | Alto | Medio (4h) | Defensa adicional contra XSS |
| 🟡 **MEDIA** | Agregar Correlation ID a logs | Medio | Bajo (2h) | Facilita debugging en producción |
| 🟡 **MEDIA** | Externalizar URL de Cloudflare a properties | Medio | Bajo (1h) | Elimina hardcoding en `SecurityConfig` |
| 🟡 **MEDIA** | Revisar logs para info sensible | Medio | Bajo (2h) | Evitar leak de tokens/passwords |
| 🟢 **BAJA** | Agregar tracing distribuido | Bajo | Alto (8h) | Solo útil si crece a microservicios |

---

## 🏋️ Ejercicios Prácticos

Ahora que comprendes la teoría, **es hora de practicar**. Estos ejercicios te ayudarán a consolidar lo aprendido.

---

### **Ejercicio 1: Crear Endpoint de Plantas (Básico)**

**Objetivo**: Implementar un endpoint completo siguiendo todos los patrones del proyecto.

**Funcionalidad**: `GET /api/v1/plantas` que devuelva todas las plantas.

**Pasos**:

#### **1. Crear Response DTO**

```java
// src/main/java/com/cambiaso/ioc/dto/response/PlantaResponse.java
package com.cambiaso.ioc.dto.response;

import lombok.Data;

@Data
public class PlantaResponse {
    private Integer id;
    private String code;
    private String name;
    private String address;
}
```

#### **2. Crear Mapper**

```java
// src/main/java/com/cambiaso/ioc/mapper/PlantaMapper.java
package com.cambiaso.ioc.mapper;

import org.mapstruct.Mapper;
import com.cambiaso.ioc.persistence.entity.Planta;
import com.cambiaso.ioc.dto.response.PlantaResponse;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PlantaMapper {
    PlantaResponse toResponse(Planta planta);
    List<PlantaResponse> toResponseList(List<Planta> plantas);
}
```

#### **3. Crear Service Interface**

```java
// src/main/java/com/cambiaso/ioc/service/PlantaService.java
package com.cambiaso.ioc.service;

import com.cambiaso.ioc.dto.response.PlantaResponse;
import java.util.List;

public interface PlantaService {
    List<PlantaResponse> getAllPlantas();
}
```

#### **4. Implementar Service**

```java
// src/main/java/com/cambiaso/ioc/service/impl/PlantaServiceImpl.java
package com.cambiaso.ioc.service.impl;

import com.cambiaso.ioc.service.PlantaService;
import com.cambiaso.ioc.persistence.repository.PlantaRepository;
import com.cambiaso.ioc.mapper.PlantaMapper;
import com.cambiaso.ioc.dto.response.PlantaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlantaServiceImpl implements PlantaService {
    
    private final PlantaRepository plantaRepository;
    private final PlantaMapper plantaMapper;
    
    @Override
    public List<PlantaResponse> getAllPlantas() {
        return plantaMapper.toResponseList(plantaRepository.findAll());
    }
}
```

#### **5. Crear Controller**

```java
// src/main/java/com/cambiaso/ioc/controller/PlantaController.java
package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.service.PlantaService;
import com.cambiaso.ioc.dto.response.PlantaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/plantas")
@RequiredArgsConstructor
public class PlantaController {
    
    private final PlantaService plantaService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Cualquier usuario autenticado puede ver plantas
    public ResponseEntity<List<PlantaResponse>> getAll() {
        return ResponseEntity.ok(plantaService.getAllPlantas());
    }
}
```

#### **6. Probar**

```bash
# Sin JWT → 401
curl http://localhost:8080/api/v1/plantas

# Con JWT válido → 200
curl -H "Authorization: Bearer <tu-token-jwt>" \
     http://localhost:8080/api/v1/plantas

# Respuesta esperada:
[
  {
    "id": 1,
    "code": "PLT-01",
    "name": "Planta Norte",
    "address": "Av. Industrial 123"
  },
  {
    "id": 2,
    "code": "PLT-02",
    "name": "Planta Sur",
    "address": "Ruta 9 km 45"
  }
]
```

**Validación**:
- [ ] Al hacer GET sin JWT devuelve 401
- [ ] Con JWT válido devuelve 200 + JSON array
- [ ] El código compila sin errores
- [ ] Los logs muestran la query SQL ejecutada

**Tiempo estimado**: 30 minutos

---

### **Ejercicio 2: Agregar Filtro de Búsqueda (Intermedio)**

**Objetivo**: Extender el endpoint anterior para buscar plantas por nombre.

**Funcionalidad**: `GET /api/v1/plantas?search=norte`

**Pasos**:

#### **1. Agregar Query Method al Repository**

```java
// src/main/java/com/cambiaso/ioc/persistence/repository/PlantaRepository.java
public interface PlantaRepository extends JpaRepository<Planta, Integer> {
    
    // Query Method: buscar por nombre que contenga el texto (case-insensitive)
    List<Planta> findByNameContainingIgnoreCase(String name);
}
```

#### **2. Actualizar Service**

```java
public interface PlantaService {
    List<PlantaResponse> getAllPlantas();
    List<PlantaResponse> searchPlantas(String search); // ← Nuevo método
}

@Service
public class PlantaServiceImpl implements PlantaService {
    // ...
    
    @Override
    public List<PlantaResponse> searchPlantas(String search) {
        if (search == null || search.isBlank()) {
            return getAllPlantas(); // Sin filtro, devolver todas
        }
        return plantaMapper.toResponseList(
            plantaRepository.findByNameContainingIgnoreCase(search)
        );
    }
}
```

#### **3. Actualizar Controller**

```java
@GetMapping
@PreAuthorize("isAuthenticated()")
public ResponseEntity<List<PlantaResponse>> getAll(
    @RequestParam(required = false) String search
) {
    return ResponseEntity.ok(plantaService.searchPlantas(search));
}
```

#### **4. Probar**

```bash
# Todas las plantas
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/api/v1/plantas

# Solo las que contengan "norte"
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/api/v1/plantas?search=norte
```

**Validación**:
- [ ] Sin `?search` devuelve todas las plantas
- [ ] Con `?search=norte` devuelve solo las que contienen "norte" en el nombre
- [ ] La búsqueda es case-insensitive (NORTE, norte, NoRtE funciona igual)

**Tiempo estimado**: 20 minutos

---

### **Ejercicio 3: Implementar Circuit Breaker (Avanzado)**

**Objetivo**: Proteger una llamada a un servicio externo con Resilience4j Circuit Breaker.

**Escenario**: El `MetabaseEmbeddingService` llama a la API de Metabase. Si Metabase falla, queremos evitar saturar nuestro backend.

**Pasos**:

#### **1. Verificar Dependencia**

```xml
<!-- Debería estar en pom.xml -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
```

#### **2. Configurar Circuit Breaker**

```yaml
# application.yml
resilience4j:
  circuitbreaker:
    instances:
      metabase:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 60s
        permitted-number-of-calls-in-half-open-state: 3
        minimum-number-of-calls: 5
```

**Qué significa**:
- Ventana de 10 llamadas
- Si >50% fallan → Circuit ABRE
- Espera 60 seg en estado ABIERTO
- Permite 3 llamadas de prueba en HALF-OPEN

#### **3. Aplicar en Service**

```java
@Service
public class MetabaseEmbeddingService {
    
    @CircuitBreaker(name = "metabase", fallbackMethod = "getFallbackDashboardUrl")
    public String getSignedDashboardUrl(Integer dashboardId, String userId) {
        // Llamada a API externa (puede fallar)
        String jwt = generateMetabaseJwt(dashboardId, userId);
        String url = metabaseUrl + "/embed/dashboard/" + dashboardId + "?token=" + jwt;
        
        // Simular verificación con timeout
        restTemplate.getForEntity(url, String.class);
        
        return url;
    }
    
    // Método fallback: se ejecuta si el circuit está ABIERTO o si el método principal falla
    private String getFallbackDashboardUrl(Integer dashboardId, String userId, Exception e) {
        log.warn("Circuit Breaker activado para Metabase. Dashboard ID: {}, Error: {}", 
                 dashboardId, e.getMessage());
        return "/dashboards/unavailable?id=" + dashboardId;
    }
}
```

#### **4. Probar**

```bash
# Simular fallo de Metabase (desconectar VPN, cambiar URL a inválida, etc.)

# Las primeras 5 llamadas esperan timeout (ej: 5 seg cada una)
# La 6ta llamada (>50% fallo) → Circuit ABRE
# Las siguientes llamadas devuelven fallback instantáneamente (sin esperar)
```

**Validación**:
- [ ] Sin fallos, el circuit permanece CERRADO
- [ ] Después de 5+ fallos, el circuit se ABRE
- [ ] En estado ABIERTO, las respuestas son instantáneas (fallback)
- [ ] Después de 60 seg, el circuit pasa a HALF-OPEN y prueba nuevamente

**Tiempo estimado**: 45 minutos

---

## 📚 Plan de Estudio Completo (8 Semanas)

Un roadmap detallado para dominar Spring Boot basándote en el proyecto IOC.

---

### **Semana 1-2: Fundamentos Spring Boot**

**Objetivo**: Dominar Inyección de Dependencias y Spring Core

**Teoría** (6 horas):

- [ ] **Spring Framework Reference - Core**
  📎 https://docs.spring.io/spring-framework/reference/core.html
  - Secciones clave: IoC Container, Dependency Injection, Bean Scopes

- [ ] **Video: Spring Boot Tutorial for Beginners** (freeCodeCamp)
  📎 https://www.youtube.com/watch?v=vtPkZShrvXQ
  - Duración: 3 horas

- [ ] **Baeldung: A Guide to Spring Boot**
  📎 https://www.baeldung.com/spring-boot

**Práctica** (10 horas):

- [ ] **Ejercicio 1**: Crea un proyecto Spring Boot desde cero
  - Usa Spring Initializr: https://start.spring.io
  - Dependencias: Web, Lombok
  - Crea 2 servicios que se inyecten entre sí
  - Prueba Constructor Injection vs Field Injection

- [ ] **Ejercicio 2**: Replica estructura de IOC
  - Crea carpetas: controller/, service/, repository/
  - Implementa endpoint simple: `GET /api/hello`
  - Inyecta un Service en el Controller

**Evaluación**:
- [ ] ¿Puedes explicar qué hace `@Autowired`?
- [ ] ¿Cuál es la diferencia entre Constructor vs Field Injection?
- [ ] ¿Qué es un Bean en Spring?

---

### **Semana 3-4: Spring Data JPA**

**Objetivo**: Dominar persistencia con JPA/Hibernate

**Teoría** (6 horas):

- [ ] **Spring Data JPA Reference**
  📎 https://docs.spring.io/spring-data/jpa/reference/
  - Query Methods, @Query, Pagination

- [ ] **Video: Spring Data JPA Tutorial** (Amigoscode)
  📎 https://www.youtube.com/watch?v=8SGI_XS5OPw

- [ ] **Baeldung: JPA Entity Lifecycle**
  📎 https://www.baeldung.com/jpa-entity-lifecycle-events

**Práctica** (10 horas):

- [ ] **Ejercicio 1**: Replica modelo de datos IOC
  - Crea entidades: User, Role, Permission
  - Relaciones: @ManyToOne, @ManyToMany
  - Testea con H2 en memoria

- [ ] **Ejercicio 2**: Query Methods
  - Implementa: `findByEmailIgnoreCase`
  - Implementa: `findByRoleNameAndIsActiveTrue`
  - Compara SQL generado con logs

- [ ] **Ejercicio 3**: @Query custom
  - Crea búsqueda con JOIN
  - Crea agregación (COUNT, GROUP BY)

**Evaluación**:
- [ ] ¿Qué es Lazy Loading y cuándo usarlo?
- [ ] ¿Cómo funciona `@Transactional`?
- [ ] ¿Cuándo usar Query Method vs @Query?

---

### **Semana 5-6: Security + JWT**

**Objetivo**: Implementar autenticación y autorización

**Teoría** (8 horas):

- [ ] **Spring Security Reference**
  📎 https://docs.spring.io/spring-security/reference/
  - OAuth 2.0 Resource Server, JWT

- [ ] **Video: Spring Security JWT** (Amigoscode)
  📎 https://www.youtube.com/watch?v=KYNR5js2cXE

- [ ] **Baeldung: Spring Security - CSRF**
  📎 https://www.baeldung.com/spring-security-csrf

**Práctica** (12 horas):

- [ ] **Ejercicio 1**: Implementa SecurityConfig
  - Configura JWT validation
  - Define endpoints públicos vs privados
  - Testea con Postman

- [ ] **Ejercicio 2**: @PreAuthorize
  - Protege endpoints por rol
  - Testea con usuarios de diferentes roles

- [ ] **Ejercicio 3**: Integra Supabase (opcional)
  - Crea proyecto en Supabase
  - Configura JwtDecoder con JWKS
  - Valida tokens emitidos por Supabase

**Evaluación**:
- [ ] ¿Por qué desactivar CSRF en APIs REST?
- [ ] ¿Qué es un Resource Server OAuth2?
- [ ] ¿Cómo funciona la validación criptográfica de JWT?

---

### **Semana 7: Resiliencia + Observabilidad**

**Objetivo**: Hacer tu app production-ready

**Teoría** (6 horas):

- [ ] **Resilience4j Docs**
  📎 https://resilience4j.readme.io/docs/circuitbreaker

- [ ] **Spring Boot Actuator Reference**
  📎 https://docs.spring.io/spring-boot/reference/actuator/

- [ ] **Baeldung: Spring Boot with Prometheus**
  📎 https://www.baeldung.com/spring-boot-self-monitoring-actuator

**Práctica** (10 horas):

- [ ] **Ejercicio 1**: Circuit Breaker (ver Ejercicio 3 arriba)

- [ ] **Ejercicio 2**: Health Checks custom
  - Crea un `HealthIndicator` que verifique integridad de datos
  - Expone en `/actuator/health`

- [ ] **Ejercicio 3**: Métricas custom
  - Crea un `Counter` para operaciones CRUD
  - Expone en `/actuator/prometheus`
  - Visualiza en Prometheus + Grafana (Docker)

**Evaluación**:
- [ ] ¿Cuándo se ABRE un Circuit Breaker?
- [ ] ¿Qué diferencia hay entre un Health Check básico y uno profundo?
- [ ] ¿Qué métricas son críticas monitorear en producción?

---

### **Semana 8: Testing + Best Practices**

**Objetivo**: Escribir tests profesionales

**Teoría** (4 horas):

- [ ] **Spring Boot Testing**
  📎 https://docs.spring.io/spring-boot/reference/testing/

- [ ] **Baeldung: Testing in Spring Boot**
  📎 https://www.baeldung.com/spring-boot-testing

**Práctica** (12 horas):

- [ ] **Ejercicio 1**: Unit Tests
  - Testea un Service con `@Mock` (Mockito)
  - Verifica que llama al Repository correctamente

- [ ] **Ejercicio 2**: Integration Tests
  - Testea un Controller con `@SpringBootTest` + `MockMvc`
  - Verifica status codes, JSON response, seguridad

- [ ] **Ejercicio 3**: Testcontainers
  - Levanta PostgreSQL con Testcontainers
  - Testea un Repository contra BD real

**Evaluación**:
- [ ] ¿Qué diferencia hay entre Unit Test e Integration Test?
- [ ] ¿Cuándo usar `@Mock` vs `@Spy` vs `@InjectMocks`?
- [ ] ¿Por qué usar Testcontainers en vez de H2?

---

