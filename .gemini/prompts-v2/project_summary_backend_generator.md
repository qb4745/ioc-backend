

## 📄 PROMPT 1: Backend - Project Summary Generator

```markdown
# GENERADOR DE RESUMEN TÉCNICO - MÓDULO BACKEND

## 1. CONFIGURACIÓN

**Contexto de Ejecución**: Este prompt se ejecuta en el repositorio `ioc-backend`.

**Objetivo**: Generar el documento base `project-summary.md` con:
- ✅ Toda la información arquitectónica
- ✅ Stack completo de backend
- ✅ Base de datos y servicios externos
- ✅ Seguridad y autenticación
- ⏳ Placeholders para que el frontend complete sus secciones

**Salida**: `@.gemini/project-summary.md` (versión backend completa con TODOs para frontend)

---

## 2. MANDATO OPERATIVO (PARA LA IA)

**Tu Rol**: Tech Lead de Backend con visión arquitectónica completa.

**Tu Responsabilidad**:
1. Analizar el repositorio backend
2. Generar documento completo de arquitectura
3. Dejar secciones específicas de frontend marcadas con `<!-- FRONTEND: completar -->`
4. Crear archivo de instrucciones para el equipo frontend

---

## 3. PROTOCOLO DE ANÁLISIS

### FASE 1: Detección de Contexto

Ejecuta este script mental:

```bash
# Verificar que estamos en el repositorio correcto
¿Existe pom.xml o build.gradle? → Backend Java confirmado
¿Existe src/main/java/? → Estructura Spring Boot confirmada
¿Existe package.json? → ERROR: Estás en el repo frontend

# Si estás en el repo correcto:
MODO = "BACKEND"
GENERAR = "Documento completo con placeholders frontend"
```

### FASE 2: Análisis Backend Completo

Realiza escaneo profundo de:

#### 2.1. Configuración Spring Boot

```java
// Archivos a analizar:
- pom.xml → Dependencias y versiones
- src/main/resources/application.properties → Configuración
- src/main/resources/application.yml → Configuración alternativa
- src/main/java/**/config/ → Clases de configuración

// Extraer:
- Versión de Spring Boot
- Java version (en pom.xml <java.version>)
- Dependencias principales (Spring Security, JPA, WebSocket, etc.)
- Perfiles configurados (dev, staging, prod)
```

#### 2.2. Estructura de Paquetes y Arquitectura

```java
// Analizar estructura en src/main/java:
com.[empresa].[proyecto]/
├── config/          → Configuraciones (contar archivos)
├── controller/      → REST Controllers (contar endpoints)
├── service/         → Servicios de negocio
├── repository/      → Repositories JPA
├── model/entity/    → Entidades de BD
├── dto/             → DTOs
├── security/        → Configuración de seguridad
├── exception/       → Manejo de errores
└── [otros paquetes]

// Extraer:
- Arquitectura: ¿Layered? ¿Hexagonal? ¿DDD?
- Patrones identificados (DTO, Repository, Service)
```

#### 2.3. Endpoints (Escaneo de Controllers)

```java
// Buscar en archivos con @RestController o @Controller:
@RestController
@RequestMapping("/api/v1/[recurso]")
public class [Nombre]Controller {
    
    @GetMapping, @PostMapping, @PutMapping, @DeleteMapping
    // Extraer: Método HTTP + Ruta + Roles requeridos
}

// Generar tabla:
| Método | Ruta | Controller | Roles | Estado |
|--------|------|------------|-------|--------|
| GET | /api/v1/dashboards/{id} | DashboardController | ADMIN, ANALYST | ✅ Implementado |
```

#### 2.4. Modelo de Datos

```java
// Analizar entidades JPA en package **.model o **.entity:
@Entity
@Table(name = "users")
public class User {
    @Id, @GeneratedValue
    @Column, @ManyToOne, @OneToMany, etc.
}

// Extraer:
- Nombre de tablas principales
- Relaciones entre entidades
- Enums importantes
```

#### 2.5. Seguridad

```java
// Buscar en package **.security o **.config:
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Analizar configuración de:
    - JWT validation
    - CORS
    - CSRF
    - Role-based access
    - Rate limiting
}
```

#### 2.6. Servicios Externos

```properties
# Analizar application.properties / .env.example:
supabase.url=
supabase.jwt.issuer=
metabase.url=
metabase.secret=
aws.s3.bucket=
# etc.

# Extraer configuraciones de servicios externos
```

---

## 4. PLANTILLA DE SALIDA (BACKEND)

Genera el archivo `@.gemini/project-summary.md` con esta estructura:

```markdown
# Resumen Técnico del Proyecto: [NOMBRE DEL PROYECTO]

> **Generado por**: Backend Module (ioc-backend)  
> **Fecha**: [ISO 8601]  
> **Versión**: 1.0-BACKEND  
> **Estado**: ⏳ Pendiente completar secciones Frontend

---

## 1. Contexto del Proyecto

### 1.1. Propósito

<!-- NOTA PARA QUIEN COMPLETE: Esta sección puede ser completada por Backend o Frontend -->

**¿Qué es este proyecto?**  
[Descripción inferida del código, README, o solicitar al usuario]

**Problema que Resuelve**:  
[Explicación basada en la lógica de negocio del código]

**Valor para el Usuario**:  
[Inferir de los endpoints y funcionalidades implementadas]

### 1.2. Objetivos Clave

<!-- BACKEND: Completar basado en funcionalidades implementadas -->
1. **[Objetivo 1]**: [Inferido de los módulos principales]
2. **[Objetivo 2]**: [Inferido de los endpoints]
3. **[Objetivo 3]**: [Inferido de integraciones]

### 1.3. Audiencia/Usuarios

<!-- BACKEND: Inferir de los roles en el código -->
- **Usuarios Primarios**: [Basado en roles: ADMIN, USER, ANALYST, etc.]
- **Administradores**: [Si existe rol ADMIN o SUPER_ADMIN]

### 1.4. Estado Actual

**Backend**:
- ✅ Autenticación y autorización implementada
- ✅ [X] endpoints REST implementados
- ✅ Integración con [servicios externos detectados]
- 🚧 [Features en desarrollo según código comentado o TODOs]

**Frontend**:
<!-- FRONTEND: Completar esta sección -->
```
⏳ PENDIENTE: Equipo frontend debe completar:
- Estado de componentes implementados
- Features de UI completadas
- Integraciones con backend
```

---

## 2. Arquitectura del Sistema

### 2.1. Arquitectura de Alto Nivel

```
┌─────────────────────────────────────────────────────────────┐
│                       FRONTEND                              │
│  <!-- FRONTEND: Completar framework y tecnologías -->      │
│  [Framework] + [Lenguaje] + [Build Tool]                   │
│  Deployed on: [Plataforma]                                  │
│                                                              │
│  ⏳ PENDIENTE: Completar por equipo frontend               │
└─────────────────┬───────────────────────────────────────────┘
                  │ HTTPS/REST + JWT
                  │
┌─────────────────▼───────────────────────────────────────────┐
│                       BACKEND (ioc-backend)                 │
│  Spring Boot [VERSION] + Java [VERSION]                     │
│  Build: Maven [VERSION]                                     │
│  Deployed on: [DETECTADO O ESPECIFICAR]                    │
│                                                              │
│  Endpoints: /api/v1/**                                      │
│  Security: JWT Validation (Supabase) + Spring Security     │
└─────────────────┬───────────────────────────────────────────┘
                  │
      ┌───────────┼───────────┬──────────────┬───────────────┐
      ▼           ▼           ▼              ▼               ▼
┌──────────┐ ┌─────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│PostgreSQL│ │ Supabase│ │ Metabase │ │   AWS    │ │  [Otro]  │
│   (DB)   │ │  (Auth) │ │(Analytics)│ │   [S3]   │ │ Servicio │
└──────────┘ └─────────┘ └──────────┘ └──────────┘ └──────────┘
```

### 2.2. Decisiones Arquitectónicas Clave

| Decisión | Tecnología Elegida | Razón |
|----------|-------------------|-------|
| Backend Framework | Spring Boot [VERSION] | [Detectado: Robustez empresarial, ecosistema maduro] |
| Lenguaje | Java [VERSION] | [Detectado en pom.xml] |
| Base de Datos | PostgreSQL | [Detectado en dependencias JPA + datasource] |
| ORM | Spring Data JPA / Hibernate | [Detectado en dependencias] |
| Autenticación | Supabase GoTrue (JWT) | [Detectado en configuración de seguridad] |
| Seguridad | Spring Security [VERSION] | [Detectado en dependencias] |
| **Frontend** | <!-- FRONTEND: Completar --> | ⏳ Pendiente |
| **UI Framework** | <!-- FRONTEND: Completar --> | ⏳ Pendiente |

---

## 3. Stack Tecnológico Detallado

### 3.1. Frontend

<!-- FRONTEND: Completar toda esta sección -->

```
⏳ PENDIENTE: Equipo frontend debe completar:

#### Lenguaje y Framework Core
- Lenguaje: [TypeScript/JavaScript]
- Framework: [React/Vue/Angular]
- Build Tool: [Vite/Webpack]

#### Librerías Principales
[Tabla con dependencias principales del frontend]

#### Estructura de Directorios
[Árbol de directorios del frontend]

📝 Instrucciones: Ejecutar el prompt "Frontend - Project Summary Completer"
en el repositorio ioc-frontend
```

---

### 3.2. Backend ✅

#### Lenguaje y Framework Core

| Componente | Tecnología | Versión |
|------------|------------|---------|
| **Lenguaje** | Java | [DETECTADO de pom.xml] |
| **Framework** | Spring Boot | [DETECTADO de parent version] |
| **Build Tool** | Maven | [DETECTADO] |
| **Java Version** | [DETECTADO de <java.version>] | - |

#### Dependencias Principales (Extraídas de pom.xml)

| Categoría | Artifact | Versión | Propósito |
|-----------|----------|---------|-----------|
| **Web** | spring-boot-starter-web | [VERSION] | REST API |
| **Seguridad** | spring-boot-starter-security | [VERSION] | Autenticación/Autorización |
| **OAuth2** | spring-boot-starter-oauth2-resource-server | [VERSION] | JWT Validation |
| **Base de Datos** | spring-boot-starter-data-jpa | [VERSION] | ORM |
| **PostgreSQL** | postgresql | [VERSION] | Driver JDBC |
| **Validación** | spring-boot-starter-validation | [VERSION] | Validación de DTOs |
| **JWT** | jjwt-api, jjwt-impl | [VERSION] | Generación JWT (Metabase) |
| **Resilience** | resilience4j-spring-boot3 | [VERSION] | Rate Limiting, Circuit Breaker |
| **Lombok** | lombok | [VERSION] | Reducción de boilerplate |
| **Testing** | spring-boot-starter-test | [VERSION] | Tests unitarios/integración |
| **[Otros]** | [DETECTAR Y LISTAR] | - | - |

#### Estructura de Paquetes

<!-- GENERADO AUTOMÁTICAMENTE DEL ESCANEO -->

```
com.[empresa].[proyecto]/
├── config/                    # [X] archivos de configuración
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   ├── [otros...]
├── controller/                # [X] controladores REST
│   ├── [Listar principales]
├── service/                   # [X] servicios de negocio
├── repository/                # [X] repositories JPA
├── model/
│   ├── entity/               # [X] entidades
│   ├── dto/                  # [X] DTOs
│   └── enums/                # [X] enumeraciones
├── security/
│   ├── JwtAuthenticationFilter.java
│   └── [otros...]
├── exception/
│   └── GlobalExceptionHandler.java
└── util/                     # Utilidades

Total: [X] clases, [Y] interfaces, [Z] enums
```

#### Configuración de Perfiles

<!-- EXTRAÍDO DE application.properties / application.yml -->

```yaml
# Perfiles detectados:
- default (application.properties)
- dev (application-dev.properties) [SI EXISTE]
- staging (application-staging.properties) [SI EXISTE]
- prod (application-prod.properties) [SI EXISTE]
```

---

### 3.3. Base de Datos

#### Sistema de Gestión

- **DBMS**: PostgreSQL [VERSION si se detecta]
- **Hosting**: [DETECTADO de datasource URL o PENDIENTE]
- **ORM**: Hibernate (via Spring Data JPA)
- **Dialect**: PostgreSQL Dialect

#### Esquema Principal

**Entidades Detectadas**:

<!-- GENERADO DEL ESCANEO DE @Entity -->

```sql
-- Tablas principales (inferidas de entidades JPA):

1. [nombre_tabla_1]  (Entidad: [NombreClase])
   - Campos principales: [listar @Column importantes]
   
2. [nombre_tabla_2]  (Entidad: [NombreClase])
   - Relaciones: @ManyToOne → [tabla_relacionada]
   
[Continuar con todas las entidades detectadas...]

-- Relaciones clave:
- [Tabla A] ←[1:N]→ [Tabla B]
- [Tabla C] ←[N:M]→ [Tabla D] (tabla intermedia: [tabla_join])
```

**Migraciones**:

```
<!-- DETECTAR -->
- Herramienta: [Flyway / Liquibase / Manual]
- Ubicación: [src/main/resources/db/migration/ o similar]
- Scripts detectados: [X] migraciones
```

**Enums Importantes**:

<!-- EXTRAER DE package **.enums -->

```java
// Enumeraciones detectadas:
- [NombreEnum]: [VALUES...]
- [NombreEnum]: [VALUES...]
```

---

### 3.4. Servicios de Infraestructura

<!-- DETECTADOS DE application.properties, .env.example, código -->

| Servicio | Proveedor | Propósito | Configuración |
|----------|-----------|-----------|---------------|
| **Autenticación** | Supabase GoTrue | Gestión de usuarios, emisión JWT | `supabase.jwt.issuer` |
| **Base de Datos** | [PostgreSQL en Supabase/AWS RDS/Otro] | Persistencia | `spring.datasource.url` |
| **Analytics** | Metabase | Dashboards de BI | `metabase.url`, `metabase.secret-key` |
| **[Detectar otros]** | [AWS S3/SendGrid/etc.] | [Propósito] | [Variables detectadas] |

---

## 4. API Endpoints

### 4.1. Endpoints de Autenticación

**Proveedor**: Supabase Auth API  
**Base URL**: `https://[PROJECT_ID].supabase.co/auth/v1`

<!-- ESTOS SON ESTÁNDAR DE SUPABASE -->

| Método | Endpoint | Propósito |
|--------|----------|-----------|
| POST | `/token?grant_type=password` | Iniciar sesión |
| POST | `/signup` | Registro de usuario |
| POST | `/recover` | Reseteo de contraseña |
| POST | `/user` | Actualizar perfil |
| POST | `/logout` | Cerrar sesión |

---

### 4.2. Endpoints de Negocio (Backend Propio)

**Base URL**: [DETECTAR de server.servlet.context-path o asumir `/api/v1`]  
**Autenticación**: Bearer Token (JWT de Supabase)

<!-- GENERADO DEL ESCANEO DE @RestController -->

#### Resumen por Módulo

<!-- AGRUPAR ENDPOINTS POR CONTROLLER O POR PREFIJO DE RUTA -->

**[Módulo 1: Nombre inferido]** (`/api/v1/[recurso]`)

| Método | Endpoint | Propósito | Controller | Roles | Estado |
|--------|----------|-----------|------------|-------|--------|
| GET | `/api/v1/dashboards/{id}` | [Inferir de nombre método] | DashboardController | [Detectar de @PreAuthorize] | ✅ |
| POST | `/api/v1/etl/upload` | [Inferir] | ETLController | ADMIN | ✅ |
| GET | `/api/v1/etl/history` | [Inferir] | ETLController | ADMIN | ✅ |
| [Continuar con todos los endpoints detectados...] |

**Total Endpoints Implementados**: [X]

**Endpoints con TODOs/Comentarios** (posiblemente en desarrollo):
- [ ] [Endpoint comentado o con TODO en código]

---

### 4.3. Contratos de API Detallados

<!-- NOTA: Solo incluir ejemplos principales aquí -->
<!-- Los contratos completos deben estar en backend_sync_brief.md -->

**Ejemplo: GET /api/v1/dashboards/{dashboardId}**

```java
// Detectado en: [NombreController.java]

// Request:
// - Path Param: dashboardId (Integer/Long/UUID)
// - Query Params: [DETECTAR de @RequestParam]

// Response (inferida de método):
// - Tipo de retorno: [ResponseEntity<DashboardDTO>]
// - DTO: [Mostrar estructura del DTO si se encuentra]

// Roles: [DETECTAR de @PreAuthorize("hasRole('...')")]
```

**Nota**: Para contratos completos de todos los endpoints, consultar:
- `@.gemini/sprints/backend_sync_brief.md` (generado por prompt específico)

---

## 5. Seguridad

### 5.1. Autenticación ✅

**Mecanismo**: JWT (JSON Web Tokens)  
**Proveedor**: Supabase GoTrue  
**Validación**: Spring Security OAuth2 Resource Server

**Flujo Detectado**:

```java
// Configuración detectada en SecurityConfig:

1. Frontend envía: Authorization: Bearer <supabase_jwt>

2. Spring Security intercepta la petición

3. JwtDecoder valida el token:
   - Issuer: [DETECTADO de supabase.jwt.issuer]
   - JWKS URI: [DETECTADO de supabase.jwks-uri]
   - Algoritmo: RS256 (estándar Supabase)

4. Si válido, extrae claims:
   - sub (userId)
   - email
   - [otros claims detectados en código]

5. Carga roles desde:
   [DETECTAR: ¿JWT claims? ¿Database lookup? ¿UserDetailsService?]
```

### 5.2. Autorización ✅

**Modelo**: Role-Based Access Control (RBAC)

**Roles Detectados**:

<!-- EXTRAER DE @PreAuthorize, hasRole(), o enums -->

```java
// Roles encontrados en el código:
- [ROLE_ADMIN]
- [ROLE_ANALYST]
- [ROLE_USER]
- [otros roles detectados...]

// Fuente: [Enum, constantes, o anotaciones]
```

**Almacenamiento de Roles**:

<!-- DETECTAR ESTRATEGIA -->

```
[OPCIÓN DETECTADA]:
- [ ] En JWT claims (app_metadata.role de Supabase)
- [ ] En tabla user_roles (PostgreSQL)
- [ ] En UserDetailsService personalizado
- [ ] [PENDIENTE: Especificar si no se puede detectar]
```

### 5.3. Configuración de Seguridad

<!-- EXTRAÍDO DE SecurityConfig.java -->

```java
// Configuración detectada:

✅ CORS: [Habilitado/Deshabilitado]
   - Orígenes permitidos: [DETECTAR de @CrossOrigin o CorsConfiguration]
   
✅ CSRF: [Habilitado/Deshabilitado]
   - Justificación: [Si está deshabilitado para REST API, mencionar]
   
✅ Endpoints Públicos:
   [LISTAR rutas permitidas sin autenticación]
   Ejemplo: /api/v1/public/**, /actuator/health
   
✅ Endpoints Protegidos:
   [LISTAR patrones protegidos]
   Ejemplo: /api/v1/** requires authentication
```

### 5.4. Rate Limiting

<!-- DETECTAR RESILIENCE4J O SIMILAR -->

```java
// Configuración detectada:

[SI SE ENCUENTRA RESILIENCE4J]:
✅ Rate Limiter configurado
   - Límites por endpoint: [DETECTAR de configuration]
   - Ventana de tiempo: [DETECTAR]
   
[SI NO SE ENCUENTRA]:
⚠️  Rate limiting no detectado en código
   Recomendación: Implementar para endpoints críticos
```

### 5.5. Otras Medidas de Seguridad

<!-- CHECKLIST DE SEGURIDAD -->

- [✅/❌] **Validación de Input**: [Detectar @Valid, @Validated]
- [✅/❌] **SQL Injection**: Protegido por JPA/Hibernate
- [✅/❌] **XSS**: [Detectar sanitización en DTOs]
- [✅/❌] **HTTPS**: [DETECTAR de server.ssl o asumir en producción]
- [✅/❌] **Logging de Seguridad**: [Detectar AuditLog o SecurityEventListener]

---

## 6. Configuración de Entorno

### 6.1. Variables de Entorno (Frontend)

<!-- FRONTEND: Completar esta sección -->

```
⏳ PENDIENTE: Equipo frontend debe listar variables de entorno
del archivo .env.example o similar en ioc-frontend
```

---

### 6.2. Variables de Entorno (Backend) ✅

**Archivo**: `application.properties` / `.env`

<!-- EXTRAER DE application.properties Y .env.example -->

```properties
# === SERVIDOR ===
SERVER_PORT=[DETECTADO o 8080 por defecto]
SERVER_SERVLET_CONTEXT_PATH=[DETECTADO o /]

# === BASE DE DATOS ===
SPRING_DATASOURCE_URL=[EJEMPLO de .env.example o placeholder]
SPRING_DATASOURCE_USERNAME=[PLACEHOLDER - NO EXPONER REAL]
SPRING_DATASOURCE_PASSWORD=[PLACEHOLDER - NO EXPONER REAL]
SPRING_JPA_HIBERNATE_DDL_AUTO=[DETECTADO: validate/update/create-drop]

# === SUPABASE (JWT VALIDATION) ===
SUPABASE_JWT_ISSUER=[DETECTADO]
SUPABASE_JWKS_URI=[DETECTADO]
SUPABASE_JWT_AUDIENCE=[DETECTADO si existe]

# === METABASE ===
METABASE_URL=[DETECTADO]
METABASE_SECRET_KEY=[PLACEHOLDER]
METABASE_SITE_URL=[DETECTADO]

# === AWS (si aplica) ===
[DETECTAR VARIABLES AWS_*]

# === OTRAS INTEGRACIONES ===
[DETECTAR OTRAS VARIABLES DE SERVICIOS EXTERNOS]

# === LOGGING ===
LOGGING_LEVEL_ROOT=[DETECTADO]
LOGGING_LEVEL_COM_[EMPRESA]_[PROYECTO]=[DETECTADO]
```

**⚠️ Seguridad**: 
- Este archivo NO debe contener valores reales
- Solo ejemplos o placeholders
- Valores reales están en variables de entorno del servidor

---

## 7. Deployment

### 7.1. Frontend

<!-- FRONTEND: Completar esta sección -->

```
⏳ PENDIENTE: Equipo frontend debe especificar:
- Plataforma de deployment (Vercel, Netlify, AWS S3, etc.)
- URLs de producción y staging
- Proceso de CI/CD
- Comando de build
```

---

### 7.2. Backend ✅

**Plataforma**: [DETECTAR de Dockerfile, scripts, o PENDIENTE]  
**URL Producción**: [ESPECIFICAR]  
**URL Staging**: [ESPECIFICAR]

**Build Command**:

```bash
# Maven
mvn clean package -DskipTests

# Genera: target/[artifact-id]-[version].jar
```

**Deploy Process**:

<!-- DETECTAR DE .github/workflows, Jenkinsfile, etc. -->

```
[SI SE DETECTA CI/CD]:
✅ CI/CD configurado
   - Pipeline: [GitHub Actions / Jenkins / GitLab CI]
   - Trigger: [Push a main/staging]
   - Steps: [Listar pasos del pipeline]

[SI NO SE DETECTA]:
⏳ Proceso de deployment manual o no documentado en código
   Recomendación: Documentar proceso actual
```

**Perfil de Producción**:

```properties
# application-prod.properties (si existe)
[MOSTRAR CONFIGURACIONES ESPECÍFICAS DE PROD]
```

---

### 7.3. Base de Datos

**Hosting**: [DETECTAR de datasource URL]  
**Backup Strategy**: [PENDIENTE - Especificar]  
**Migraciones**: 

```
[SI HAY FLYWAY/LIQUIBASE]:
✅ Migraciones automáticas con [Herramienta]
   - Ejecutadas en: Application startup
   - Scripts en: [Ubicación]
   
[SI NO]:
⚠️  Migraciones manuales o no detectadas
```

---

## 8. Testing

### 8.1. Frontend

<!-- FRONTEND: Completar esta sección -->

```
⏳ PENDIENTE: Equipo frontend debe especificar:
- Framework de testing (Vitest, Jest, etc.)
- Tipos de tests implementados
- Coverage objetivo
- Comandos para ejecutar tests
```

---

### 8.2. Backend ✅

**Framework**: JUnit 5 + Mockito + Spring Boot Test

**Tipos de Tests Detectados**:

<!-- ESCANEAR src/test/java/ -->

```
[ANALIZAR ESTRUCTURA DE TEST]:

✅ Unit Tests: [X] archivos
   - Ubicación: src/test/java/**/service/
   - Naming: *Test.java
   
✅ Integration Tests: [Y] archivos
   - Ubicación: src/test/java/**/integration/
   - Naming: *IntegrationTest.java
   - Usa: @SpringBootTest
   
[SI SE ENCUENTRAN]:
✅ Security Tests: [Z] archivos
   - Testing de autorización
   
✅ Repository Tests: [W] archivos
   - @DataJpaTest detectado
```

**Coverage**:

<!-- DETECTAR DE pom.xml jacoco plugin -->

```
[SI JACOCO CONFIGURADO]:
✅ Jacoco configurado
   - Coverage objetivo: [DETECTAR de <limit>]
   
[SI NO]:
⏳ Coverage tracking no configurado
```

**Comandos**:

```bash
# Ejecutar tests unitarios
mvn test

# Ejecutar tests de integración
mvn verify

# Generar reporte de coverage (si Jacoco configurado)
mvn test jacoco:report
# Reporte en: target/site/jacoco/index.html
```

---

## 9. Monitoreo y Logging

### 9.1. Logging ✅

**Framework**: SLF4J + Logback (estándar Spring Boot)

**Configuración Detectada**:

<!-- ANALIZAR logback-spring.xml o application.properties -->

```xml
<!-- Configuración de logging: -->

Niveles:
- Root: [DETECTADO o INFO por defecto]
- com.[empresa].[proyecto]: [DETECTADO]
- org.springframework: [DETECTADO]

Appenders detectados:
- [ ] Console (stdout)
- [ ] File ([ubicación si se detecta])
- [ ] [Otros: Logstash, Sentry, CloudWatch]

Formato:
- [Detectar patrón de log]
- ¿JSON estructurado?: [SI/NO]
```

**Logs Críticos Implementados**:

<!-- BUSCAR EN CÓDIGO Logger.error, Logger.warn -->

```java
// Eventos de seguridad logueados:
- [ ] Failed login attempts
- [ ] Unauthorized access attempts
- [ ] [Otros eventos detectados en SecurityConfig o filters]

// Eventos de negocio logueados:
- [ ] [Detectar en Services]
```

---

### 9.2. Monitoreo

**Spring Boot Actuator**:

<!-- DETECTAR DEPENDENCIA spring-boot-starter-actuator -->

```
[SI ACTUATOR CONFIGURADO]:
✅ Actuator habilitado
   - Endpoints expuestos: [DETECTAR de management.endpoints.web.exposure.include]
   - URL base: [DETECTAR de management.endpoints.web.base-path o /actuator]
   - Seguridad: [DETECTAR si están protegidos en SecurityConfig]
   
   Endpoints disponibles:
   - /actuator/health → Health check
   - /actuator/metrics → Métricas de la app
   - /actuator/info → Info de la aplicación
   - [Otros detectados]

[SI NO]:
⏳ Actuator no configurado
   Recomendación: Habilitar para monitoring en producción
```

**APM/Error Tracking**:

<!-- BUSCAR DEPENDENCIAS: sentry, newrelic, datadog, etc. -->

```
[SI SE DETECTA]:
✅ [Herramienta] configurada
   
[SI NO]:
⏳ No se detectó APM o error tracking
   Recomendación: Considerar Sentry, New Relic, o similar
```

---

## 10. Documentación Relacionada

### 10.1. Documentación de Backend ✅

```
Repositorio: ioc-backend/
├── README.md
├── @.gemini/
│   ├── project-summary.md (este archivo)
│   ├── sprints/
│   │   ├── Sprint-X-Backlog.md
│   │   ├── technical-designs/
│   │   └── backend_sync_brief.md
│   └── [otros docs de backend]
```

### 10.2. Documentación de Frontend

<!-- FRONTEND: Completar ubicación de docs -->

```
⏳ PENDIENTE: Equipo frontend debe especificar:
- Ubicación de documentación (README, wiki, etc.)
- Storybook (si existe)
- Design system (Figma, etc.)
```

### 10.3. API Documentation

**Swagger/OpenAPI**:

<!-- DETECTAR DEPENDENCIA springdoc-openapi -->

```
[SI SWAGGER CONFIGURADO]:
✅ Swagger UI habilitado
   - URL: [DETECTAR de springdoc.swagger-ui.path o /swagger-ui.html]
   - OpenAPI JSON: [/v3/api-docs]

[SI NO]:
⏳ Documentación OpenAPI no detectada
   Recomendación: Agregar springdoc-openapi-starter-webmvc-ui
```

---

## 11. Contactos y Recursos

### 11.1. Equipo

<!-- SOLICITAR AL USUARIO O DEJAR PENDIENTE -->

| Rol | Nombre | Contacto |
|-----|--------|----------|
| Product Owner | [PENDIENTE] | [PENDIENTE] |
| Tech Lead | [PENDIENTE] | [PENDIENTE] |
| Backend Lead | [PENDIENTE] | [PENDIENTE] |
| Frontend Lead | [PENDIENTE] | [PENDIENTE] |
| DevOps | [PENDIENTE] | [PENDIENTE] |

### 11.2. Repositorios

- **Backend**: [URL del repo ioc-backend]
- **Frontend**: [URL del repo ioc-frontend - PENDIENTE]

### 11.3. Accesos

**Backend**:
- Repositorio: [URL]
- Supabase Dashboard: [URL]
- Base de Datos Admin: [Especificar herramienta]
- Metabase Admin: [URL]
- AWS Console: [URL si aplica]

**Frontend**:
<!-- FRONTEND: Completar accesos de frontend -->
```
⏳ PENDIENTE
```

---

## 12. Próximos Pasos

### Para Completar Este Documento

**Equipo Frontend debe**:
1. ✅ Ejecutar el prompt "Frontend - Project Summary Completer" en `ioc-frontend`
2. ✅ Completar todas las secciones marcadas con `<!-- FRONTEND: completar -->`
3. ✅ Validar que la información de integración backend-frontend es consistente

**Instrucciones**:
```bash
# Desde el repositorio ioc-frontend:
cd ../ioc-frontend
gemini-cli < @.gemini/prompts/complete-project-summary-frontend.md

# Esto leerá este archivo y completará las secciones faltantes
```

### Secciones Pendientes de Validación Humana

- [ ] **Sección 1.1**: Validar propósito y objetivos del proyecto
- [ ] **Sección 11.1**: Completar información del equipo
- [ ] **Sección 7.2**: Confirmar proceso de deployment de backend
- [ ] **Todas las secciones con ⏳**: Revisar placeholders

---

## 13. Changelog del Documento

| Versión | Fecha | Autor | Cambios |
|---------|-------|-------|---------|
| 1.0-BACKEND | [YYYY-MM-DD] | Backend Module (IA) | Generación inicial desde ioc-backend |
| 1.0-FULL | [PENDIENTE] | Frontend Module (IA) | Completar secciones frontend |
| 1.1 | [PENDIENTE] | [Humano] | Validación y ajustes finales |

---

## METADATA PARA SINCRONIZACIÓN

```yaml
# NO EDITAR - Usado para sincronización automática
generated_by: "Backend Module"
source_repo: "ioc-backend"
version: "1.0-BACKEND"
status: "INCOMPLETE"
pending_completion: "ioc-frontend"
last_updated: "[ISO 8601]"
checksum: "[HASH del código analizado - opcional]"
```

---

**⚠️ IMPORTANTE**: Este documento está incompleto hasta que el equipo Frontend ejecute el prompt complementario y complete las secciones marcadas.

**Documento generado automáticamente por**: Backend - Project Summary Generator v1  
**Repositorio analizado**: ioc-backend  
**Fecha de análisis**: [ISO 8601]
```

---

## 5. REGLAS DE EJECUCIÓN FINAL

### Si detectas que NO estás en el repositorio correcto:

```markdown
❌ ERROR: Repositorio Incorrecto

Este prompt está diseñado para ejecutarse en el repositorio BACKEND (ioc-backend).

Repositorio detectado: [ioc-frontend / otro]

🔧 Acción requerida:
1. Navega al repositorio correcto: `cd ../ioc-backend`
2. Vuelve a ejecutar este prompt

O, si necesitas completar las secciones de frontend, usa:
"Frontend - Project Summary Completer" (Prompt #2)
```

### Si el usuario solicita información que no puedes detectar:

```markdown
⚠️ INFORMACIÓN REQUERIDA DEL USUARIO

No pude determinar los siguientes datos del código:

1. [Campo faltante 1]: [¿Qué necesitas saber?]
2. [Campo faltante 2]: [¿Qué necesitas saber?]

Opciones:
A) Proporciónanos esta información ahora
B) Marca como [PENDIENTE] y continúa
C) Usa valores por defecto razonables

¿Qué prefieres? (A/B/C)
```

---

## 6. VALIDACIÓN PRE-GENERACIÓN

Antes de generar el documento, ejecuta este checklist:

```markdown
## Checklist de Validación Backend

### Escaneo Completado
- [ ] pom.xml analizado correctamente
- [ ] Estructura de paquetes mapeada
- [ ] Al menos [5] endpoints detectados
- [ ] Configuración de seguridad encontrada
- [ ] Variables de entorno identificadas

### Información Suficiente
- [ ] Versión de Spring Boot detectada
- [ ] Versión de Java detectada
- [ ] Base de datos identificada
- [ ] Servicios externos listados

### Calidad del Documento
- [ ] Todas las secciones backend están completas
- [ ] Placeholders frontend están claramente marcados
- [ ] No hay información contradictoria
- [ ] Checksums/metadata para sincronización incluidos

Si algún item crítico no se cumple, DETENER y solicitar información al usuario.
```

---

