# 📘 IOC Backend - Visión General y Conceptos Fundamentales (VERSIÓN CORREGIDA)

> **Proyecto**: Inteligencia Operacional Cambiaso (IOC)  
> **Framework**: Spring Boot 3.5.5 + Java 21  
> **Fecha de Análisis**: 2025-10-22  
> **Nivel**: Junior Developer  
> **Prerequisito**: Conocimientos básicos de Java y HTTP  
> **Versión**: 1.1 - Corregida y Mejorada

---

## 📘 Sobre Este Documento

**Objetivo**: Proporcionar una **visión general** de la arquitectura del proyecto IOC para entender el "big picture" antes de profundizar en detalles técnicos.

**Nivel de detalle**:
- ✅ Conceptos fundamentales de Spring Boot
- ✅ Responsabilidades por capa arquitectónica
- ✅ Flujos principales de datos
- ✅ Estructura del proyecto completa
- ❌ Análisis línea por línea de código (ver `02-IOC-Analisis-Detallado.md`)
- ❌ Detalles de seguridad y CSP (ver `02-IOC-Analisis-Detallado.md`)
- ❌ Trade-offs técnicos (ver `02-IOC-Analisis-Detallado.md`)

**Audiencia**: Junior developers que necesitan entender la arquitectura antes de codificar.

**Tiempo de lectura**: 45-60 minutos

**Próximo paso**: Una vez que domines estos conceptos, profundiza en `02-IOC-Analisis-Detallado.md` para ver código real línea por línea y análisis de seguridad.

---

## 🎯 Conceptos Clave que Dominarás

Al completar esta serie de 3 documentos, entenderás:

### **Arquitectura y Patrones** (Este documento)
- ✅ Layered Architecture (Controller → Service → Repository)
- ✅ Dependency Injection (Inyección de Dependencias)
- ✅ DTOs vs Entities (y por qué NUNCA exponer entities en API)
- ✅ Repository Pattern
- ✅ Separation of Concerns

### **Seguridad** (Documento 2)
- ✅ JWT Authentication con Supabase
- ✅ Content Security Policy (CSP) para embedding
- ✅ CORS (Cross-Origin Resource Sharing)
- ✅ Stateless Sessions
- ✅ Trade-offs entre seguridad y funcionalidad

### **Concurrencia y Asincronía** (Documento 2)
- ✅ Async Processing con Thread Pools
- ✅ @Transactional y manejo de transacciones
- ✅ ETL Jobs en background

### **Integraciones Externas** (Documentos 2-3)
- ✅ Embedding de dashboards de Metabase
- ✅ Autenticación con Supabase
- ✅ Validación de tokens JWT

### **Best Practices** (Documento 3)
- ✅ Resiliencia y Circuit Breakers
- ✅ Observabilidad (Logging, Metrics, Tracing)
- ✅ Testing (Unit, Integration, Security)
- ✅ Manejo centralizado de errores

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

El proyecto **Inteligencia Operacional Cambiaso (IOC)** es una aplicación backend construida con Spring Boot 3.5.5 y Java 21. Su propósito principal es servir como el cerebro de una plataforma de Business Intelligence (BI). 

**Funcionalidades principales**:
1. **Ingesta de datos**: Recibe datos de producción a través de carga de archivos (ETL)
2. **Procesamiento**: Valida, transforma y almacena datos en PostgreSQL (Supabase)
3. **Exposición**: Proporciona API REST segura con autenticación JWT
4. **Visualización**: Integra dashboards interactivos de Metabase mediante embedding seguro
5. **Monitoreo**: Incluye health checks, métricas y logging para observabilidad

**Stack tecnológico**:
- **Backend**: Spring Boot 3.5.5 + Java 21
- **Base de datos**: PostgreSQL (Supabase)
- **Autenticación**: Supabase Auth (JWT)
- **BI/Dashboards**: Metabase (embedded)
- **Seguridad**: Spring Security + Content Security Policy

---

## FASE 1: Visión General

### 🏗️ Estructura del Proyecto

La organización del código sigue una estructura de paquetes estándar y muy bien definida, basada en la funcionalidad y las capas arquitectónicas.

**Árbol de carpetas completo** (todas verificadas como existentes):

```
src/main/java/com/cambiaso/ioc/
├── 📦 config/         → Configuraciones de Spring (Security, CORS, Async, etc.)
├── 📦 controller/     → Endpoints HTTP de la API REST
├── 📦 dto/            → Data Transfer Objects (Request/Response)
├── 📦 exception/      → Manejo centralizado de errores
├── 📦 health/         → Health checks personalizados (Actuator)
├── 📦 interceptor/    → Interceptores HTTP (rate limiting, logging)
├── 📦 mapper/         → Conversión automática Entity ↔ DTO (MapStruct)
├── 📦 metrics/        → Métricas custom de la aplicación
├── 📦 persistence/
│   ├── entity/        → Mapeo de tablas (JPA Entities)
│   └── repository/    → Acceso a datos (Spring Data JPA)
├── 📦 security/       → Configuración de seguridad (JWT, CSP, RLS)
├── 📦 service/        → Lógica de negocio y orquestación
├── 📦 startup/        → Lógica ejecutada al iniciar la aplicación
└── 📦 validation/     → Validadores personalizados (Bean Validation)
```

**Descripción por carpeta**:

| Carpeta | Propósito | Ejemplos de archivos |
|---------|-----------|----------------------|
| **config/** | Configuración de beans y comportamiento de Spring | `SecurityConfig`, `CorsConfig`, `AsyncConfig` |
| **controller/** | Puntos de entrada HTTP de la API | `DashboardController`, `EtlController`, `AdminUserController` |
| **dto/** | Objetos para transferir datos entre capas | `UserCreateRequest`, `UserResponse`, `DashboardEmbedResponse` |
| **exception/** | Excepciones de negocio y handler global | `GlobalExceptionHandler`, `ResourceNotFoundException` |
| **health/** | Indicadores de salud personalizados | `DatabaseHealthIndicator`, `MetabaseHealthIndicator` |
| **interceptor/** | Lógica transversal en peticiones HTTP | `RateLimitInterceptor`, `LoggingInterceptor` |
| **mapper/** | Conversión automática de objetos | `UserMapper`, `PlantaMapper` (MapStruct) |
| **metrics/** | Registro de métricas custom | `EtlMetrics`, `ApiMetrics` |
| **persistence/entity/** | Mapeo de tablas de base de datos | `AppUser`, `Role`, `Permission`, `Planta` |
| **persistence/repository/** | Acceso a datos y queries | `AppUserRepository`, `RoleRepository` |
| **security/** | Seguridad y autenticación | `SecurityConfig`, `JwtAuthenticationFilter` |
| **service/** | Lógica de negocio principal | `UserAdminService`, `EtlService`, `MetabaseEmbeddingService` |
| **startup/** | Tareas de inicialización | `DatabaseSeeder`, `CacheWarmer` |
| **validation/** | Validadores custom | `@UniqueEmail`, `@ValidSupabaseUUID` |

**Nota sobre la organización**: Esta estructura sigue el principio de "package by layer" (empaquetar por capa), que facilita encontrar código según su responsabilidad arquitectónica.

---

### 🎨 Patrón Arquitectónico

**Patrón identificado**: **Arquitectura en Capas (Layered Architecture)**, también conocida como Arquitectura de N-Capas.

#### **Capas del proyecto**:

```
┌─────────────────────────────────────────────────────────┐
│  CAPA DE PRESENTACIÓN (controller/, dto/)              │
│  Responsabilidad: Recibir HTTP, validar formato,       │
│                   mapear status codes                   │
└────────────────┬────────────────────────────────────────┘
                 ↓ Delega
┌─────────────────────────────────────────────────────────┐
│  CAPA DE NEGOCIO (service/)                            │
│  Responsabilidad: Lógica de negocio, validaciones,     │
│                   orquestación, transacciones           │
└────────────────┬────────────────────────────────────────┘
                 ↓ Usa
┌─────────────────────────────────────────────────────────┐
│  CAPA DE PERSISTENCIA (persistence/)                   │
│  Responsabilidad: Acceso a BD, queries, mapeo ORM      │
└────────────────┬────────────────────────────────────────┘
                 ↓ Interactúa con
┌─────────────────────────────────────────────────────────┐
│  BASE DE DATOS (PostgreSQL/Supabase)                   │
└─────────────────────────────────────────────────────────┘

        CAPAS TRANSVERSALES (aplican a todas):
    ┌───────────────────────────────────────────┐
    │ security/  → Autenticación y autorización │
    │ exception/ → Manejo de errores            │
    │ mapper/    → Transformación de datos      │
    │ config/    → Configuración de beans       │
    └───────────────────────────────────────────┘
```

#### **Justificación del patrón**:

El código está **claramente separado** en capas con responsabilidades bien definidas:

1. **Capa de Presentación (`controller/` + `dto/`)**: 
   - Expone la API REST
   - Maneja protocolo HTTP
   - Valida formato de entrada
   - NO contiene lógica de negocio

2. **Capa de Negocio (`service/`)**: 
   - Orquesta operaciones
   - Aplica reglas de negocio
   - Coordina múltiples repositorios
   - Maneja transacciones
   - NO sabe de HTTP ni de base de datos directamente

3. **Capa de Persistencia (`persistence/`)**: 
   - Abstrae acceso a datos
   - Mapea objetos a tablas (ORM)
   - Ejecuta queries
   - NO conoce reglas de negocio

#### **Beneficios para este proyecto**:

| Beneficio | Explicación | Ejemplo en IOC |
|-----------|-------------|----------------|
| **Mantenibilidad** | Fácil localizar código por responsabilidad | Bug en ETL → buscar en `EtlService.java` |
| **Testeabilidad** | Capas se pueden testear aisladamente | Service tests sin BD real (mocks) |
| **Separación de Responsabilidades** | Bajo acoplamiento entre capas | Cambiar PostgreSQL por MySQL solo afecta `repository/` |
| **Escalabilidad** | Capas independientes escalan diferente | Más instancias de API sin escalar BD |
| **Onboarding** | Nuevos developers entienden estructura rápido | Junior encuentra `UserController` fácilmente |

#### **Separación de responsabilidades**:

Se logra mediante **Inyección de Dependencias (Dependency Injection)**:

```java
// Controller depende de Service (inyectado por Spring)
@RestController
public class UserController {
    private final UserService userService;  // ← Inyectado
    
    // Controller NO crea el service, Spring lo inyecta
}

// Service depende de Repository (inyectado por Spring)
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;  // ← Inyectado
    
    // Service NO crea el repository, Spring lo inyecta
}
```

**Flujo de dependencias** (siempre unidireccional):
```
Controller → Service → Repository → Database
     ↑          ↑          ↑
  NO conoce  NO conoce  NO conoce
  Service    Repository  Database
  internos   internos    schema
```

---

### 🔄 Flujo de Datos Principal

Veamos el viaje completo de una petición HTTP real del proyecto.

#### **Ejemplo: Obtener URL firmada de un dashboard de Metabase**

**Endpoint**: `GET /api/v1/dashboards/{dashboardId}/embed`

**Diagrama de Flujo Completo**:

```
┌──────────────────────────────────────────────────────────────┐
│ 1. Cliente (Frontend React)                                  │
│    fetch('/api/v1/dashboards/3/embed', {                    │
│      headers: { Authorization: 'Bearer eyJ0eXAi...' }       │
│    })                                                        │
└────────────────────┬─────────────────────────────────────────┘
                     ↓ HTTP GET + JWT Token
┌──────────────────────────────────────────────────────────────┐
│ 2. SecurityConfig & JWT Filter                               │
│    - Extrae token del header Authorization                   │
│    - Valida firma con llaves públicas de Supabase            │
│    - Verifica expiración (exp claim)                         │
│    - Extrae claims (sub, email, roles)                       │
│    ✅ Token válido → continúa                                │
│    ❌ Token inválido → 401 Unauthorized                      │
└────────────────────┬─────────────────────────────────────────┘
                     ↓ Petición autenticada
┌──────────────────────────────────────────────────────────────┐
│ 3. @RestController - DashboardController                     │
│    @GetMapping("/{dashboardId}/embed")                       │
│    public DashboardEmbedResponse getEmbedUrl(                │
│        @PathVariable Integer dashboardId,                    │
│        Authentication auth                                   │
│    ) {                                                       │
│        // Extrae ID del dashboard (3)                        │
│        // Extrae usuario del token JWT (auth)                │
│        return dashboardService.generateEmbedUrl(...);        │
│    }                                                         │
└────────────────────┬─────────────────────────────────────────┘
                     ↓ Delega a Service
┌──────────────────────────────────────────────────────────────┐
│ 4. @Service - MetabaseEmbeddingService                       │
│    public DashboardEmbedResponse generateEmbedUrl(           │
│        Integer dashboardId, User user                        │
│    ) {                                                       │
│        // LÓGICA DE NEGOCIO:                                 │
│        // 1. Validar que el usuario tenga permiso            │
│        // 2. Construir payload JWT para Metabase             │
│        // 3. Firmar JWT con secreto de Metabase              │
│        // 4. Construir URL completa con token                │
│        String signedUrl = metabaseUrl + "/embed/" +          │
│                          dashboardId + "?token=" + jwt;      │
│        return new DashboardEmbedResponse(signedUrl);         │
│    }                                                         │
└────────────────────┬─────────────────────────────────────────┘
                     ↓ Devuelve URL firmada
┌──────────────────────────────────────────────────────────────┐
│ 5. DashboardController (nuevamente)                          │
│    - Recibe DashboardEmbedResponse del service               │
│    - Spring lo serializa automáticamente a JSON              │
│    - Devuelve HTTP 200 OK                                    │
└────────────────────┬─────────────────────────────────────────┘
                     ↓ JSON Response
┌──────────────────────────────────────────────────────────────┐
│ 6. Cliente recibe JSON:                                      │
│    {                                                         │
│      "url": "https://metabase.../embed/3?token=eyJ...",     │
│      "expiresAt": "2025-10-22T15:30:00Z"                    │
│    }                                                         │
│                                                              │
│    Frontend carga URL en <iframe src={url} />               │
└──────────────────────────────────────────────────────────────┘
```

#### **Explicación paso a paso**:

| Paso | Qué ocurre | Responsable |
|------|------------|-------------|
| **1** | Frontend hace petición con JWT | React App |
| **2** | Validación de seguridad (autenticación) | `SecurityConfig` + Spring Security |
| **3** | Recepción de petición HTTP, extracción de parámetros | `DashboardController` |
| **4** | Lógica de negocio: generar URL firmada de Metabase | `MetabaseEmbeddingService` |
| **5** | Conversión a JSON y envío de respuesta | `DashboardController` + Spring MVC |
| **6** | Renderizado del dashboard en iframe | React App |

#### **Nota importante sobre este flujo**:

Este ejemplo **NO accede a la base de datos** (no usa `Repository`) porque:
- Solo necesita generar un JWT firmado (operación en memoria)
- Los permisos ya están validados en el JWT del usuario
- Es una operación stateless (sin estado)

En otros flujos (ej: crear usuario), SÍ se usaría la capa de persistencia:
```
Controller → Service → Repository → Database
```

---

### 🚀 Clase Principal

**`IocbackendApplication.java`**

Esta es la puerta de entrada de la aplicación. Todo comienza aquí.

#### **¿Qué pasa cuando arranca la aplicación?**

Cuando ejecutas esta clase (con `java -jar iocbackend.jar` o desde el IDE), ocurre lo siguiente:

**Secuencia de inicio**:

```
1. JVM inicia
    ↓
2. Método main() se ejecuta
    ↓
3. SpringApplication.run() inicia el Application Context
    ↓
4. Spring escanea paquetes buscando anotaciones:
    - @Component, @Service, @Repository, @Controller
    - @Configuration, @Bean
    ↓
5. Spring crea instancias de todas las clases anotadas (beans)
    ↓
6. Spring resuelve dependencias e inyecta (Dependency Injection)
    ↓
7. Spring ejecuta @PostConstruct y CommandLineRunners
    ↓
8. Spring inicia servidor web embebido (Tomcat en puerto 8080)
    ↓
9. Aplicación lista para recibir peticiones HTTP
    ↓
10. Logs: "Started IocbackendApplication in X seconds"
```

#### **Anotaciones principales**:

```java
@SpringBootApplication                                    // (1)
@EntityScan("com.cambiaso.ioc.persistence.entity")      // (2)
@EnableJpaRepositories("com.cambiaso.ioc.persistence.repository") // (3)
@EnableScheduling                                        // (4)
public class IocbackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(IocbackendApplication.class, args);
    }
}
```

**Explicación de anotaciones**:

| Anotación | Qué hace | Por qué existe |
|-----------|----------|----------------|
| **(1) `@SpringBootApplication`** | Activa auto-configuración + component scan + configuración | Es la anotación "maestra" que enciende Spring Boot |
| **(2) `@EntityScan`** | Le dice a JPA dónde buscar clases `@Entity` | La estructura de paquetes no es estándar (dentro de `persistence/`) |
| **(3) `@EnableJpaRepositories`** | Le dice a Spring Data dónde buscar interfaces `@Repository` | Igual que arriba, ubicación no estándar |
| **(4) `@EnableScheduling`** | Activa soporte para `@Scheduled` (tareas programadas) | El proyecto tiene jobs programados (ej: `EtlJobWatchdog` cada 5 min) |

**Qué pasaría sin estas anotaciones**:

```java
// Sin @EntityScan
❌ Error: "Not a managed type: class AppUser"
   (Spring no encuentra las entidades)

// Sin @EnableJpaRepositories
❌ Error: "Field repository required a bean of type 'AppUserRepository'"
   (Spring no crea los repositorios)

// Sin @EnableScheduling
⚠️  Métodos @Scheduled se ignoran silenciosamente
   (Los jobs NO se ejecutan)
```

---

## FASE 2: Responsabilidades por Capa

> **IMPORTANTE**: Lee esta sección completa antes de pasar al Archivo 2.  
> Aquí entenderás QUÉ HACE y POR QUÉ EXISTE cada carpeta/capa.

Esta sección es el **fundamento conceptual** de Spring Boot. Dominar estos conceptos es más importante que memorizar sintaxis.

---

### 📂 Carpeta: `dto/` (Data Transfer Objects)

#### ¿Qué son los DTOs?

Los DTOs (Data Transfer Objects) son **objetos simples** que solo contienen datos, sin lógica de negocio. Su única misión es **transportar datos** entre las capas de la aplicación, especialmente entre el backend y el frontend.

**Analogía del mundo real**:  
Un DTO es como un **formulario estandarizado**. Cuando vas al banco a abrir una cuenta, el empleado te da un formulario pre-impreso con campos específicos. Ese formulario es un DTO:
- Define exactamente qué información necesita el banco (`UserCreateRequest`)
- Define exactamente qué información te devuelve el banco (`UserResponse`)
- No contiene lógica (el formulario no toma decisiones)

#### ¿Para qué sirven?

**Problema que resuelven**:  
Sin DTOs, estarías **acoplando** la estructura interna de tu base de datos con la estructura de tu API pública.

**Analogía**: Sería como si el banco te mostrara directamente su base de datos interna cuando pides tu saldo. Verías campos internos como `internal_account_id`, `risk_score`, `credit_limit`, que no deberías ver.

#### **Beneficios**:

| Beneficio | Explicación | Ejemplo en IOC |
|-----------|-------------|----------------|
| **🛡️ Seguridad** | Ocultar campos sensibles | `AppUser` tiene `passwordHash` en BD, `UserResponse` NO lo expone |
| **🤸 Flexibilidad** | Adaptar datos a necesidades de UI | Combinar `primerNombre` + `primerApellido` → `fullName` |
| **🔗 Desacoplamiento** | Cambiar BD sin romper API | Renombrar columna en BD → solo cambiar Mapper, DTO igual |
| **📐 Validación** | Validar formato de entrada | `@Email`, `@NotNull`, `@Size` en DTOs de request |
| **📊 Documentación** | API auto-documentada | OpenAPI/Swagger genera docs desde DTOs |

#### **¿Cuándo usar DTOs?**

✅ **SÍ usar cuando**:
- Expones datos en una API REST
- Recibes datos del frontend
- Quieres controlar exactamente qué se envía/recibe
- Necesitas calcular campos derivados

❌ **NO usar cuando**:
- Comunicación interna entre services del mismo backend
- Transferencia dentro de una transacción
- Objetos inmutables simples (Value Objects)

#### **Tipos comunes en `dto/`**:

```
dto/
├── request/     → Datos que ENTRAN al sistema (POST/PUT)
│   ├── UserCreateRequest.java
│   ├── UserUpdateRequest.java
│   └── EtlUploadRequest.java
│
└── response/    → Datos que SALEN del sistema (GET)
    ├── UserResponse.java
    ├── UserListResponse.java
    └── DashboardEmbedResponse.java
```

#### **Ejemplo real del proyecto IOC**:

```java
// UserCreateRequest.java (Request DTO)
// El frontend envía esto al hacer POST /api/v1/admin/users
{
  "email": "juan.perez@cambiaso.com",
  "primerNombre": "Juan",
  "segundoNombre": "Carlos",
  "primerApellido": "Pérez",
  "segundoApellido": "González",
  "supabaseUserId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "plantaId": 2,
  "roles": ["GERENTE"]
}

// UserResponse.java (Response DTO)
// El backend devuelve esto
{
  "id": 123,
  "email": "juan.perez@cambiaso.com",
  "fullName": "Juan Carlos Pérez González",  // ← Calculado por Mapper
  "plantaCode": "PLT-02",                     // ← De la relación con Planta
  "plantaName": "Planta Norte",               // ← De la relación con Planta
  "roles": ["GERENTE"],                       // ← De la relación UserRole
  "isActive": true,
  "createdAt": "2025-10-22T10:30:00Z"
}
```

**¿Notas las diferencias?**

| Aspecto | Request DTO | Response DTO |
|---------|-------------|--------------|
| **ID** | ❌ No tiene (será generado) | ✅ Tiene (ya existe en BD) |
| **Nombre completo** | ❌ Campos separados | ✅ Campo calculado `fullName` |
| **Planta** | Solo `plantaId` (referencia) | `plantaCode` + `plantaName` (denormalizado) |
| **Timestamps** | ❌ No los envía el usuario | ✅ `createdAt` generado por BD |
| **Campos internos** | Solo lo necesario | Solo lo seguro de exponer |

#### **Características técnicas**:

```java
// Ejemplo de Request DTO con validaciones
public class UserCreateRequest {
    
    @NotNull(message = "Email es requerido")
    @Email(message = "Formato de email inválido")
    private String email;
    
    @NotBlank(message = "Primer nombre es requerido")
    @Size(min = 2, max = 100, message = "Debe tener entre 2 y 100 caracteres")
    private String primerNombre;
    
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", 
             message = "UUID inválido")
    private String supabaseUserId;
    
    // getters y setters...
}
```

**Anotaciones de validación comunes**:
- `@NotNull` / `@NotBlank` / `@NotEmpty`: Campo requerido
- `@Email`: Valida formato de email
- `@Size(min, max)`: Valida longitud de string
- `@Min` / `@Max`: Valida rango de números
- `@Pattern(regexp)`: Valida contra expresión regular
- `@Valid`: Valida objetos anidados

#### ⚠️ **Anti-patrón: Exponer Entities en la API**

```java
// ❌ NUNCA HAGAS ESTO
@GetMapping("/users/{id}")
public AppUser getUser(@PathVariable Long id) {  // ← Entity expuesta
    return userRepository.findById(id).orElseThrow();
}
```

**Por qué es malo**:

1. **Seguridad 🔒**: 
   ```java
   // La Entity tiene campos que NO deberías exponer
   public class AppUser {
       private String passwordHash;  // ← Se expondría en JSON
       private boolean isDeleted;    // ← Campo interno
       private String internalNotes; // ← Datos sensibles
   }
   ```

2. **Lazy Loading Issues ⚠️**:
   ```java
   @Entity
   public class AppUser {
       @OneToMany(fetch = FetchType.LAZY)
       private List<UserRole> roles;  // ← No se carga automáticamente
   }
   
   // Al convertir a JSON fuera de la transacción:
   // LazyInitializationException: failed to lazily initialize
   ```

3. **Acoplamiento Fuerte 🔗**:
   ```sql
   -- Si renombras columna en BD:
   ALTER TABLE app_users RENAME COLUMN email TO email_address;
   ```
   ```java
   // Rompe el contrato de API (cambio no retrocompatible)
   // Antes: { "email": "..." }
   // Ahora:  { "emailAddress": "..." }  ← Frontend se rompe
   ```

**Solución correcta con DTO**:
```java
// ✅ BIEN: Usar DTO
@GetMapping("/users/{id}")
public UserResponse getUser(@PathVariable Long id) {  // ← DTO
    AppUser user = userService.getUserById(id);
    return userMapper.toResponse(user);  // ← Mapper convierte Entity → DTO
}
```

Con esta arquitectura:
- Puedes renombrar columnas en BD sin romper la API
- Controlas exactamente qué se expone
- No hay problemas de lazy loading
- La API está desacoplada de la implementación interna

---

### 📂 Carpeta: `entity/` (Entidades JPA)

#### ¿Qué son las Entities?

Las **Entities** (Entidades) son clases Java que representan **directamente** una tabla de la base de datos. Cada instancia de una Entity corresponde a una **fila** en esa tabla. Son el pilar del ORM (Object-Relational Mapping) que proporciona JPA/Hibernate.

**Analogía del mundo real**:  
Una Entity es como el **plano arquitectónico** de un edificio. Define la estructura exacta:
- Cuántas columnas (campos) tiene
- Qué tipo de dato es cada columna (String, Integer, Date)
- Cómo se relaciona con otras tablas (llaves foráneas)
- Qué restricciones tiene (NOT NULL, UNIQUE)

#### **Entity vs DTO: Diferencias Críticas**

| Aspecto | Entity (`@Entity`) | DTO (Request/Response) |
|---------|-------------------|------------------------|
| **Propósito** | Mapear tabla de BD | Transferir datos en API |
| **Anotaciones** | `@Entity`, `@Table`, `@Column`, `@Id` | `@Data`, `@NotNull`, `@JsonProperty` |
| **Mutabilidad** | Mutable (JPA requiere setters) | Request: mutable, Response: preferible inmutable |
| **Relaciones** | Sí (`@OneToMany`, `@ManyToOne`) | No (campos planos) |
| **Campos calculados** | No (solo BD) | Sí (`fullName` = `primerNombre` + `primerApellido`) |
| **Exponer en API** | ❌ **NUNCA** | ✅ **SIEMPRE** |
| **Lazy Loading** | Sí (optimización) | No aplica |
| **Ciclo de vida** | Gestionado por JPA | Creado/destruido libremente |

#### **¿Por qué NUNCA exponer Entities en la API?**

Ya vimos los problemas en la sección de DTOs. Aquí un resumen ejecutivo:

| Problema | Consecuencia | Solución |
|----------|--------------|----------|
| **Datos sensibles** | Expones `passwordHash`, campos internos | DTO filtra campos |
| **Lazy Loading** | `LazyInitializationException` en JSON | DTO carga solo lo necesario |
| **Acoplamiento** | Cambios en BD rompen API | Mapper traduce cambios |
| **Performance** | Cargas relaciones innecesarias | DTO denormaliza lo justo |

#### **Ejemplo real del proyecto IOC**:

```java
@Entity
@Table(name = "app_users")
@Data
public class AppUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "supabase_user_id", unique = true, nullable = false)
    private UUID supabaseUserId;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(name = "primer_nombre", nullable = false)
    private String primerNombre;
    
    @Column(name = "segundo_nombre")
    private String segundoNombre;
    
    @Column(name = "primer_apellido", nullable = false)
    private String primerApellido;
    
    @Column(name = "segundo_apellido")
    private String segundoApellido;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planta_id")
    private Planta planta;
    
    @Column(name = "centro_costo")
    private String centroCosto;
    
    @Column(name = "fecha_contrato")
    private LocalDate fechaContrato;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
```

**Anotaciones importantes**:

| Anotación | Qué hace | Ejemplo |
|-----------|----------|---------|
| **`@Entity`** | Marca clase como entidad JPA | `@Entity public class AppUser` |
| **`@Table`** | Mapea a tabla específica | `@Table(name = "app_users")` |
| **`@Id`** | Marca clave primaria | `@Id private Long id` |
| **`@GeneratedValue`** | Auto-incremento en BD | `@GeneratedValue(strategy = IDENTITY)` |
| **`@Column`** | Mapea campo a columna | `@Column(name = "primer_nombre")` |
| **`@ManyToOne`** | Relación muchos-a-uno | Un usuario tiene una planta |
| **`@JoinColumn`** | Nombre de FK en tabla | `@JoinColumn(name = "planta_id")` |
| **`fetch = LAZY`** | Carga bajo demanda (performance) | No carga `planta` hasta accederla |

#### **Relaciones entre Entities**:

```java
// Relación: Un usuario pertenece a UNA planta
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "planta_id")
private Planta planta;

// Relación: Un usuario tiene MUCHOS roles (tabla intermedia)
@ManyToMany
@JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles;
```

**Tipos de relaciones**:

| Tipo | Descripción | Ejemplo IOC |
|------|-------------|-------------|
| **`@OneToOne`** | 1:1 | Usuario ↔ Perfil extendido |
| **`@OneToMany`** | 1:N | Planta ↔ Usuarios |
| **`@ManyToOne`** | N:1 | Usuario ↔ Planta |
| **`@ManyToMany`** | N:M | Usuario ↔ Roles |

---

### 📂 Carpeta: `repository/` (Acceso a Datos)

#### ¿Qué son los Repositories?

Los **Repositories** son interfaces que definen los métodos para **interactuar con la base de datos** para una entidad específica. Abstraen completamente la necesidad de escribir SQL.

**Analogía del mundo real**:  
Un Repository es como el **bibliotecario** de una biblioteca:
- Le pides "dame el libro con ISBN X" (`findById(X)`)
- Le pides "dame todos los libros del autor Y" (`findByAuthor(Y)`)
- Él se encarga de ir a las estanterías (BD) y traértelos
- Tú NO necesitas saber dónde están físicamente guardados

#### **Spring Data JPA - "Magia" automática**

Lo más poderoso: **NO necesitas implementar estas interfaces**. Al extender `JpaRepository`, Spring crea automáticamente la implementación en tiempo de ejecución.

```java
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    // ✅ GRATIS (Spring los genera automáticamente):
    // - findById(Long id)
    // - findAll()
    // - save(AppUser user)
    // - deleteById(Long id)
    // - existsById(Long id)
    // - count()
    // ... y muchos más
}
```

#### **Query Methods (Queries Derivadas)**

Spring genera SQL automáticamente **basándose en el nombre del método**:

```java
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    
    // Spring genera: SELECT * FROM app_users WHERE email = ? LIMIT 1
    Optional<AppUser> findByEmail(String email);
    
    // Spring genera: SELECT * FROM app_users WHERE email = ? (case-insensitive)
    Optional<AppUser> findByEmailIgnoreCase(String email);
    
    // Spring genera: SELECT COUNT(*) > 0 FROM app_users WHERE supabase_user_id = ?
    boolean existsBySupabaseUserId(UUID supabaseUserId);
    
    // Spring genera: SELECT * FROM app_users WHERE planta_id = ? AND is_active = TRUE
    List<AppUser> findByPlantaIdAndIsActiveTrue(Integer plantaId);
    
    // Spring genera: SELECT * FROM app_users WHERE email LIKE %?%
    List<AppUser> findByEmailContaining(String emailPart);
    
    // Spring genera: SELECT * FROM app_users WHERE created_at > ? ORDER BY created_at DESC
    List<AppUser> findByCreatedAtAfterOrderByCreatedAtDesc(OffsetDateTime date);
}
```

**Palabras clave soportadas**:
- `findBy`, `getBy`, `queryBy`, `readBy`, `streamBy`
- `countBy`, `deleteBy`, `removeBy`, `existsBy`
- `And`, `Or`
- `LessThan`, `GreaterThan`, `Between`
- `Like`, `Containing`, `StartingWith`, `EndingWith`
- `IsNull`, `IsNotNull`
- `True`, `False`
- `IgnoreCase`
- `OrderBy` + `Asc` / `Desc`

#### **¿Cuándo usar cada tipo de query?**

| Tipo | Complejidad | Cuándo Usar | Ejemplo |
|------|-------------|-------------|---------|
| **CRUD básico** | Baja | Operaciones simples por ID | `findById()`, `save()` |
| **Query Methods** | Baja-Media | Búsquedas por 1-3 campos | `findByEmail()` |
| **@Query (JPQL)** | Media-Alta | Joins, agregaciones, filtros complejos | Ver abajo |
| **Native Query** | Alta | SQL específico de PostgreSQL | Funciones nativas, JSON operators |
| **Specifications** | Alta | Filtros dinámicos (usuario elige) | Criteria API |

#### **@Query Personalizado (JPQL)**

Para queries complejos que no se pueden expresar con el nombre del método:

```java
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    
    // JPQL (Java Persistence Query Language) - independiente de BD
    @Query("SELECT u FROM AppUser u WHERE u.email LIKE %:domain%")
    List<AppUser> findByEmailDomain(@Param("domain") String domain);
    
    // Con JOIN
    @Query("SELECT u FROM AppUser u JOIN u.roles r WHERE r.name = :roleName")
    List<AppUser> findByRoleName(@Param("roleName") String roleName);
    
    // Con agregación
    @Query("SELECT u.planta.name, COUNT(u) FROM AppUser u GROUP BY u.planta.name")
    List<Object[]> countUsersByPlanta();
    
    // UPDATE personalizado
    @Modifying
    @Query("UPDATE AppUser u SET u.isActive = false WHERE u.deletedAt IS NOT NULL")
    int deactivateDeletedUsers();
}
```

#### **Native Query (SQL puro)**

Cuando necesitas features específicas de PostgreSQL:

```java
@Query(value = "SELECT * FROM app_users WHERE email ILIKE :pattern", nativeQuery = true)
List<AppUser> findByEmailPatternCaseInsensitive(@Param("pattern") String pattern);

// Usando funciones de PostgreSQL
@Query(value = "SELECT * FROM app_users WHERE created_at > NOW() - INTERVAL '7 days'", nativeQuery = true)
List<AppUser> findRecentUsers();
```

---

### 📂 Carpeta: `service/` (Lógica de Negocio)

#### ¿Qué son los Services?

Los **Services** son el **cerebro** de la aplicación. Aquí reside la lógica de negocio principal. Un Service orquesta operaciones, coordina múltiples repositorios y aplica reglas de negocio.

**Analogía del mundo real**:  
Un Service es como el **chef** de un restaurante:
- El mesero (Controller) toma la orden
- El chef (Service) ejecuta la receta (lógica de negocio)
- Pide ingredientes al almacén (Repository)
- Combina todo según las reglas culinarias
- Entrega el plato terminado

#### **¿Qué va en Service vs Controller?**

| Responsabilidad | Controller | Service | Repository |
|----------------|------------|---------|------------|
| **Recibir HTTP** | ✅ | ❌ | ❌ |
| **Validar formato JSON** | ✅ (@Valid) | ❌ | ❌ |
| **Validar reglas de negocio** | ❌ | ✅ | ❌ |
| **Mapear DTO ↔ Entity** | ❌ | ✅ (usa Mapper) | ❌ |
| **Ejecutar lógica de negocio** | ❌ | ✅ | ❌ |
| **Acceder a BD** | ❌ | ❌ | ✅ |
| **Coordinar múltiples repos** | ❌ | ✅ | ❌ |
| **Manejar transacciones** | ❌ | ✅ (@Transactional) | ❌ |
| **Devolver HTTP status** | ✅ | ❌ | ❌ |

#### **Ejemplo de Service bien diseñado**:

```java
@Service
@RequiredArgsConstructor
@Transactional
public class UserAdminServiceImpl implements UserAdminService {
    
    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    
    @Override
    public UserResponse createUser(UserCreateRequest request) {
        // ✅ 1. Validación de reglas de negocio
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }
        
        // ✅ 2. Transformación DTO → Entity (vía Mapper)
        AppUser user = userMapper.toEntity(request);
        
        // ✅ 3. Aplicar lógica de negocio
        user.setIsActive(true);
        user.setCreatedAt(OffsetDateTime.now());
        
        // ✅ 4. Asignar rol por defecto si no se especifica
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            Role defaultRole = roleRepository.findByName("ANALISTA")
                .orElseThrow(() -> new RoleNotFoundException("ANALISTA"));
            user.addRole(defaultRole);
        }
        
        // ✅ 5. Persistir (vía Repository)
        AppUser savedUser = userRepository.save(user);
        
        // ✅ 6. Transformar Entity → DTO (vía Mapper)
        return userMapper.toResponse(savedUser);
    }
}
```

**Por qué `@Transactional` está aquí**:
- Si `save()` funciona pero `addRole()` falla → rollback automático
- Mantiene consistencia de datos
- Evita estados intermedios inválidos

---

### 📂 Carpeta: `controller/` (Capa de Presentación)

#### ¿Qué son los Controllers?

Los **Controllers** son la **puerta de entrada** HTTP de tu API. Sus métodos están mapeados a endpoints (URLs) y se encargan de recibir peticiones, validar formato y delegar a Services.

**Analogía del mundo real**:  
Un Controller es como el **mesero** de un restaurante:
- Toma la orden del cliente (petición HTTP)
- Verifica que la orden esté completa (validación)
- Pasa la orden al chef (Service)
- Entrega el plato al cliente (respuesta HTTP)

**NO cocina** (no tiene lógica de negocio).

---

### 📂 Carpeta: `mapper/` (Transformación de Datos)

#### ¿Qué son los Mappers?

Son componentes especializados en **convertir** objetos de un tipo a otro. En IOC, convierten `Entity` ↔ `DTO`.

**Herramienta**: **MapStruct** (genera código automáticamente en compilación).

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "fullName", expression = "java(buildFullName(user))")
    @Mapping(target = "plantaCode", source = "planta.code")
    UserResponse toResponse(AppUser user);
    
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

**Ventajas**:
- ✅ Performance nativa (no reflection)
- ✅ Type-safe (errores en compilación)
- ✅ Código generado visible

---

### 📂 Carpeta: `exception/` (Manejo de Errores)

Contiene:
1. **Excepciones custom**: `UserNotFoundException`, `EmailAlreadyExistsException`
2. **GlobalExceptionHandler**: Centraliza manejo de errores

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(UserNotFoundException ex) {
        return new ErrorResponse("USER_NOT_FOUND", ex.getMessage());
    }
}
```

---

### 📂 Carpetas: `config/` y `security/`

- **`config/`**: Configuraciones de Spring (`AsyncConfig`, `CorsConfig`, etc.)
- **`security/`**: Configuración de seguridad (JWT, CSP, RLS)

*Análisis profundo en Archivo 2.*

---

## ✅ Checklist de Comprensión

Antes de pasar al Archivo 2, asegúrate de poder responder:

**Conceptos Fundamentales**:
- [ ] ¿Cuál es la diferencia entre Entity y DTO?
- [ ] ¿Por qué NUNCA debes exponer Entities en la API? (3 razones)
- [ ] ¿Qué hace cada capa? (Controller, Service, Repository)
- [ ] ¿Qué es un Query Method y cómo funciona?

**Responsabilidades**:
- [ ] ¿Qué responsabilidades tiene un Controller? ¿Y un Service?
- [ ] ¿Para qué sirven los Mappers y por qué usar MapStruct?
- [ ] ¿Por qué usar `@Transactional` en Service y no en Controller?

**Flujo de Datos**:
- [ ] ¿Puedes explicar el flujo completo de una petición HTTP?
- [ ] ¿Cómo funciona el `GlobalExceptionHandler`?

Si respondiste "No sé" a alguna, **relee esa sección** antes de continuar.

---

## 🗺️ Navegación

**Archivos de esta serie**:

1. ✅ **01-IOC-Vision-General.md** (estás aquí)
2. ➡️ **02-IOC-Analisis-Detallado.md** - Análisis línea por línea con código real
3. ➡️ **03-IOC-Resumen-Produccion.md** - Resumen de aprendizaje y aspectos de producción

---

**Fecha de generación**: 2025-10-22  
**Versión**: 1.1 - Corregida y Mejorada  
**Cambios principales**:
- ✅ Confirmadas todas las carpetas como existentes
- ✅ Agregada sección "Sobre Este Documento"
- ✅ Agregada sección "Conceptos Clave que Dominarás"
- ✅ Nomenclatura corregida (español → inglés en nombres de clases)
- ✅ Ejemplos más detallados con código real
- ✅ Tablas comparativas mejoradas
- ✅ Analogías expandidas

**Autor**: Análisis pedagógico asistido por IA

---

✅ **Archivo `01-IOC-Vision-General.md` completado (versión corregida 1.1).**

