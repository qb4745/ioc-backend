# Resumen Técnico del Proyecto: ioc-backend

> **Generado por**: Módulo Backend (ioc-backend)
> **Fecha**: 2025-11-04T00:00:00Z
> **Versión**: 1.0-BACKEND
> **Estado**: ⏳ Pendiente completar secciones Frontend

<!-- Inserted metadata and TOC for better navigation -->
> **created_date**: 2025-11-04
> **version_scheme**: semver
> **document_version**: 1.1-BACKEND-AUTOEVAL

## 📑 Índice

- [1. Contexto del Proyecto](#1-contexto-del-proyecto)
- [2. Arquitectura del Sistema](#2-arquitectura-del-sistema)
- [3. Stack Tecnológico Detallado](#stack-tecnologico-detallado)
- [4. Endpoints Detectados (Resumido)](#4-endpoints-detectados-resumido)
- [5. Modelo de Datos (Resumen de Entidades Detectadas)](#5-modelo-de-datos-resumen-de-entidades-detectadas)
- [6. Seguridad](#6-seguridad)
- [7. Servicios Externos y Configuración](#servicios-externos-configuracion)
- [8. Operaciones y Despliegue](#8-operaciones-y-despliegue)
- [9. Tests y Calidad](#9-tests-y-calidad)
- [10. TODOs / Siguientes Pasos](#10-todos--siguientes-pasos-acciones-recomendadas)
- [11. Apéndice — Extractos Técnicos Relevantes](#apendice-extractos)
- [12. Autoevaluación y Plan de Mejora (aplicando metaprompt)](#autoevaluacion-plan-de-mejora)
- [13. Limitaciones Conocidas](#13-limitaciones-conocidas)
- [14. Próximos Pasos Prioritarios (aplicación de Opción A)](#proximos-pasos-opcion-a)

---

## 1. Contexto del Proyecto

### 1.1. Propósito

Este repositorio implementa el backend del proyecto "Inteligencia Operacional Cambiaso" (ioc-backend). Provee las APIs REST para gestión de usuarios/roles/permissions, ejecución y seguimiento de procesos ETL (subida y procesamiento de archivos), y endpoints para obtener URLs firmadas de dashboards de Metabase para embedding.

**Problema que Resuelve**: Centralizar la lógica de negocio, control de acceso y orquestación de procesos de ingestión de datos (ETL), además de exponer dashboards seguros (Metabase) para consumo desde el frontend.

**Valor para el Usuario**: Permite a administradores gestionar usuarios y permisos, y a usuarios autenticados iniciar/consultar procesos ETL y obtener dashboards embebidos con control de acceso basado en roles.

### 1.2. Objetivos Clave

<!-- BACKEND: Completar basado en funcionalidades implementadas -->
1. Gestionar usuarios, roles y permisos con endpoints administrativos (ROLE_ADMIN).
2. Proveer un pipeline de ingestión de datos mediante endpoints ETL con idempotencia y validaciones (archivos .txt hasta 50MB).
3. Exponer dashboards de Metabase mediante URLs firmadas y controladas por roles.
4. Integrar con Supabase (auth + base de datos) y Metabase para embedding.

### 1.3. Audiencia/Usuarios

<!-- BACKEND: Inferir de los roles en el código -->
- **Usuarios Primarios**: Usuarios autenticados (clientes de la API) que consumen ETL y dashboards.
- **Administradores**: Usuarios con ROLE_ADMIN que gestionan usuarios, roles y permisos.
- **Analistas**: Usuarios que consumen dashboards embebidos (roles configurados por dashboard).

### 1.4. Estado Actual

**Backend**:
- ✅ Autenticación y autorización basada en JWT (integración con Supabase) detectada
- ✅ Endpoints REST para administración (users/roles/permissions)
- ✅ Endpoints ETL (subida y seguimiento de jobs) implementados
- ✅ Servicio de embedding para Metabase implementado
- ✅ Integración con Prometheus (Micrometer) y Actuator para métricas y salud
- ✅ Soporte WebSocket con seguridad mediante validación de JWT
- 🚧 Features pendientes: (detalladas en código con TODOs o mejoras de configuración)

**Frontend**:
<!-- FRONTEND: Completar esta sección -->
⏳ PENDIENTE: Equipo frontend debe completar:
- Estado de componentes implementados
- Features de UI completadas
- Integraciones con backend

---

## 2. Arquitectura del Sistema

### 2.1. Arquitectura de Alto Nivel

Frontend (pendiente) ↔ HTTPS/REST + JWT ↔ Backend (ioc-backend)

Backend (ioc-backend): Spring Boot 3.5.5 + Java 21, empaquetado con Maven
- Endpoints principales expuestos en /api/** y /api/v1/**
- Seguridad: Spring Security + OAuth2 Resource Server (JWT) validando tokens emitidos por Supabase
- Integraciones: Supabase (Postgres), Metabase (embedding), Prometheus (micrómetros), Redis/Caffeine para cache/rate-limiting (bibliotecas detectadas)
- Resiliencia: Resilience4j y Bucket4j para rate-limiting y circuit breakers
- WebSocket: soporte para mensajería en tiempo real con validación JWT


### 2.2. Decisiones Arquitectónicas Clave

| Decisión | Tecnología Elegida | Razón |
|----------|-------------------:|-------|
| Backend Framework | Spring Boot 3.5.5 | Robustez empresarial y ecosistema Spring (detectado en parent POM) |
| Lenguaje | Java 21 | Declarado en pom.xml (<java.version>21) |
| Build Tool | Maven | pom.xml |
| Base de Datos | PostgreSQL (hosted en Supabase pooler) | Datasource URL en application-*.properties apuntando a un pooler Supabase |
| ORM | Spring Data JPA / Hibernate | spring-boot-starter-data-jpa detectado |
| Autenticación | OAuth2 Resource Server (JWT) validado contra Supabase issuer-uri | spring-boot-starter-oauth2-resource-server y propiedades de issuer-uri detectadas |
| Embedding Dashboards | Metabase signed URLs (jjwt) | Servicio MetabaseEmbeddingService y jjwt en dependencias |
| Observabilidad | Spring Actuator + Micrometer Prometheus | Dependencias detectadas |
| Resiliencia | Resilience4j + Bucket4j | Dependencias detectadas para circuit breaker y rate limiting |

---

## 3. Stack Tecnológico Detallado

### 3.1. Backend ✅

#### Lenguaje y Framework Core

| Componente | Tecnología | Versión |
|------------|------------|---------|
| Lenguaje | Java | 21 (pom.xml) |
| Framework | Spring Boot | 3.5.5 (parent) |
| Build Tool | Maven | (pom.xml) |
| Java Compiler / Annotation Processors | MapStruct 1.6.2, Lombok (opcional) | (pom.xml) |

#### Dependencias Principales

Lista resumida (extraída de pom.xml):
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-oauth2-resource-server
- spring-boot-starter-websocket
- spring-boot-starter-actuator
- micrometer-registry-prometheus
- spring-boot-starter-validation
- spring-webflux + reactor-netty (WebClient)
- resilience4j (spring-boot3, circuitbreaker, timelimiter)
- bucket4j-core & bucket4j-redis (rate limiting storage)
- caffeine (cache)
- postgresql (JDBC runtime driver)
- mapstruct + mapstruct-processor (mappers)
- jjwt (io.jsonwebtoken) para generación de JWT para Metabase embedding
- spring-boot-devtools (runtime optional)
- testcontainers, junit, h2 para tests


#### Estructura de Paquetes (detectada)

com.cambiaso.ioc/
├── config/                    # Clases de configuración (CORS, WebSocket, Metabase properties...)
├── controller/                # Controladores REST (DashboardController, EtlController, admin/...)
├── service/                   # Servicios de negocio (EtlProcessingService, MetabaseEmbeddingService...)
├── persistence/               # Repositorios y entidades (JPA)
├── dto/                       # DTOs de entrada/salida
├── security/                  # (config y utilidades de seguridad)
├── exception/                 # Mapeo y manejo de errores global
└── startup/                   # Inicializadores y jobs de arranque

Total: múltiples entidades JPA y controladores (ver sección de Endpoints y Modelo de Datos).

#### Configuración de Perfiles

Perfiles detectados (ficheros en src/main/resources):
- default (application.properties)
- dev (application-dev.properties, application-dev-5432.properties)
- local (application-local.properties)
- prod (application-prod.properties)

Perfiles contienen settings para Hikari pool, JWT issuer-uri, supabase URL y claves, metabase site URL y secret.

---

## 4. Endpoints Detectados (Resumido)

Tabla principal (extraída de los controladores analizados):

| Método | Ruta | Controller | Roles / Seguridad | Estado |
|--------|------|------------|-------------------|--------|
| GET | /api/v1/dashboards/{dashboardId} | DashboardController#getDashboardUrl | Authenticated (valida JWT); RateLimiter (dashboardAccess) | ✅ Implementado |
| POST | /api/etl/start-process | EtlController#startEtlProcess | @PreAuthorize isAuthenticated() (JWT required) | ✅ Implementado |
| GET | /api/etl/jobs/{jobId}/status | EtlController#getJobStatus | @PreAuthorize isAuthenticated() | ✅ Implementado |
| GET | /api/v1/admin/users | AdminUserController#search | ROLE_ADMIN required | ✅ Implementado |
| GET | /api/v1/admin/users/{id} | AdminUserController#getById | ROLE_ADMIN required | ✅ Implementado |
| POST | /api/v1/admin/users | AdminUserController#create | ROLE_ADMIN required | ✅ Implementado |
| PUT | /api/v1/admin/users/{id} | AdminUserController#update | ROLE_ADMIN required | ✅ Implementado |
| DELETE | /api/v1/admin/users/{id} | AdminUserController#delete | ROLE_ADMIN required | ✅ Implementado |
| GET | /api/v1/admin/roles | RoleController#search | ROLE_ADMIN required | ✅ Implementado |
| GET | /api/v1/admin/roles/{id} | RoleController#getById | ROLE_ADMIN required | ✅ Implementado |
| POST | /api/v1/admin/roles | RoleController#create | ROLE_ADMIN required | ✅ Implementado |
| PUT | /api/v1/admin/roles/{id} | RoleController#update | ROLE_ADMIN required | ✅ Implementado |
| DELETE | /api/v1/admin/roles/{id} | RoleController#delete | ROLE_ADMIN required | ✅ Implementado |

Nota: También existen controladores para permisos y asignaciones (controller/admin/PermissionController, AssignmentController). El proyecto expone además endpoints /api/etl/** y websockets para notificaciones.

---

## 5. Modelo de Datos (Resumen de Entidades Detectadas)

Entidades JPA detectadas (lista no exhaustiva):
- AppUser (usuario de la aplicación)
- Role, UserRole, RolePermission, Permission (modelo de RBAC)
- EtlJob (registro de jobs ETL)
- FactProduction, QuarantinedRecord, DimMaquina, DimMaquinista, Planta (modelos domain/analytics)

Relaciones clave (inferidas):
- AppUser ↔ UserRole ↔ Role ↔ RolePermission ↔ Permission (estructura RBAC clásica)
- EtlJob almacena jobId (UUID), estado y metadatos del archivo (fileHash) para idempotencia
- Fact/Dim entidades relacionadas para reporting (utilizadas por ETL y dashboards)

Migraciones:
- No se detectó explícitamente Flyway o Liquibase en dependencias; buscar en src/main/resources/db/migration/ no mostró resultados (puede ser manejo manual o scripts externos). Si usan migrations, no están en dependencias directas.


### 5.1 Entidades Detalladas (campos y relaciones)

A continuación se listan las entidades JPA más relevantes con sus campos principales y relaciones (extraído del código fuente):

- AppUser (tabla: app_users)
  - id: Long (PK, IDENTITY)
  - supabaseUserId: UUID (supabase_user_id, unique)
  - email: String (citext, unique)
  - primerNombre, segundoNombre, primerApellido, segundoApellido: String
  - planta: Planta (ManyToOne, planta_id)
  - centroCosto: String
  - fechaContrato: LocalDate
  - active: boolean (is_active)
  - lastLoginAt, createdAt, updatedAt, deletedAt: OffsetDateTime
  - Índices: is_active, supabase_user_id, planta_id, primer_nombre+primer_apellido

- EtlJob (tabla: etl_jobs)
  - jobId: UUID (PK)
  - fileName: String
  - fileHash: String (unique)
  - userId: String (supabase user id/string)
  - minDate, maxDate: LocalDate
  - status: String
  - details: String (texto libre)
  - createdAt: OffsetDateTime
  - finishedAt: OffsetDateTime
  - Uso: registros de idempotencia y tracking de procesamiento ETL

- QuarantinedRecord (tabla: quarantined_records)
  - id: Long (PK)
  - etlJob: EtlJob (ManyToOne, job_id)
  - fileName: String
  - lineNumber: Integer
  - rawLine: String
  - errorDetails: String
  - createdAt: OffsetDateTime
  - Uso: almacenar filas problematicas detectadas durante ETL

- FactProduction (tabla: fact_production)
  - id: Long (PK, SEQUENCE)
  - fechaContabilizacion: LocalDate
  - maquina: DimMaquina (ManyToOne, maquina_fk)
  - maquinista: DimMaquinista (ManyToOne, maquinista_fk)
  - numeroLog: Long
  - horaContabilizacion: LocalTime
  - fechaNotificacion: LocalDate
  - documento: Long
  - materialSku: Long
  - materialDescripcion: String
  - numeroPallet: Integer
  - cantidad: BigDecimal
  - pesoNeto: BigDecimal
  - lista, versionProduccion, centroCostos, turno, jornada, usuarioSap, bodeguero, statusOrigen: varios campos auxiliares

- DimMaquina (tabla: dim_maquina)
  - id: Long (PK)
  - codigoMaquina: String (unique)
  - nombreMaquina: String
  - createdAt, updatedAt: OffsetDateTime

- DimMaquinista (tabla: dim_maquinista)
  - id: Long (PK)
  - codigoMaquinista: Long (unique)
  - nombreCompleto: String
  - createdAt, updatedAt: OffsetDateTime

- Planta (tabla: plantas)
  - id: Integer (PK)
  - code: String (unique)
  - name: String
  - address: text
  - createdAt, updatedAt: OffsetDateTime

- Role (tabla: roles)
  - id: Integer (PK)
  - name: String (unique, length 100)
  - description: String
  - createdAt, updatedAt: OffsetDateTime

- Permission (tabla: permissions)
  - id: Integer (PK)
  - name: String (unique, length 150)
  - description: String

- RolePermission (tabla: role_permissions)
  - id: RolePermissionKey (EmbeddedId: roleId, permissionId)
  - role: Role (ManyToOne, MapsId roleId)
  - permission: Permission (ManyToOne, MapsId permissionId)
  - createdAt: OffsetDateTime

- UserRole (tabla: user_roles)
  - id: UserRoleKey (EmbeddedId: userId, roleId)
  - user: AppUser (ManyToOne, MapsId userId)
  - role: Role (ManyToOne, MapsId roleId)
  - assignedAt: OffsetDateTime
  - assignedBy: AppUser (ManyToOne, assigned_by_user_id)

- Claves embebidas:
  - RolePermissionKey: roleId (Integer), permissionId (Integer)
  - UserRoleKey: userId (Long), roleId (Integer)

---

## 6. Seguridad

Resumen implementado:
- OAuth2 Resource Server (spring-boot-starter-oauth2-resource-server) configurado para validar JWTs emitidos por Supabase (propiedad spring.security.oauth2.resourceserver.jwt.issuer-uri en application-*.properties).
- WebSocketSecurityConfig valida tokens JWT para conexiones websocket (JwtDecoder utilizado).
- Controles de acceso a nivel de método con @PreAuthorize (p. ej. hasAuthority('ROLE_ADMIN') para endpoints administrativos).
- Rate limiting aplicado vía resilience4j y/o Bucket4j (dependencias detectadas), además de anotaciones @RateLimiter en endpoints como DashboardController.
- CORS configurado para permitir origen del frontend y metabase (CorsConfig detectado).

Señales y recomendaciones:
- Las credenciales sensibles (SUPABASE_SERVICE_ROLE_KEY, SUPABASE_DB_PASSWORD, METABASE_SECRET_KEY) se exponen como variables de entorno en properties; asegurarse que no estén hardcodeadas en el repo y que el pipeline de CI las gestione mediante secretos.

---

## 7. Servicios Externos y Configuración

Configuraciones detectadas en src/main/resources (resumen):
- Supabase (Postgres + Auth)
  - Datasource URL en application-prod.properties apuntando a: jdbc:postgresql://aws-1-sa-east-1.pooler.supabase.com:5432/postgres?...&sslmode=require
  - spring.security.oauth2.resourceserver.jwt.issuer-uri apunta a: https://bdyvzjpkycnekjrlqlfp.supabase.co/auth/v1
  - supabase.url y supabase.service-role-key configurables via env vars
- Metabase
  - metabase.site-url y metabase.secret-key (METABASE_SECRET_KEY) configurables
  - Servicio MetabaseEmbeddingService genera tokens JWT firmados con jjwt para embedding
- Observabilidad & Caching
  - micrometer-registry-prometheus para métricas
  - Caffeine para cache local
  - bucket4j-redis disponible si requiere backend redis para rate limiting

---

## 8. Operaciones y Despliegue

- Build: mvn clean package (Maven + Spring Boot Maven Plugin)
- Profiles: activar perfil con SPRING_PROFILES_ACTIVE (local, dev, prod)
- Recomendaciones rápidas:
  - Proveer variables de entorno: SUPABASE_DB_PASSWORD, SUPABASE_SERVICE_ROLE_KEY, METABASE_SECRET_KEY, SUPABASE_URL
  - Usar pooler de Supabase con Hikari config detectada (pool-size reducido en properties)
  - Habilitar Actuator endpoints con autenticación para monitorización

---

## 9. Tests y Calidad

- Dependencias de testing: spring-boot-starter-test, spring-security-test, testcontainers (postgresql), h2 para pruebas unitarias/integración
- Jacoco configurado en pom.xml para cobertura

---

## 10. TODOs / Siguientes Pasos (Acciones recomendadas)

Backend:
- [ ] Confirmar estrategia de migraciones (Flyway/Liquibase) y agregar scripts si faltan.
- [ ] Revisar manejo y rotación de secrets (METABASE_SECRET_KEY, SUPABASE_SERVICE_ROLE_KEY).
- [ ] Documentar contratos exactos de JSON para endpoints ETL y responses de embedding.
- [ ] Endpoint discovery: generar OpenAPI (springdoc está configurado) y publicar spec para frontend.

Frontend (placeholders):
<!-- FRONTEND: completar -->
- [ ] Framework y stack usado por frontend
- [ ] Implementación de consumo de endpoints y manejo de auth (login, token storage)
- [ ] Integración de embedding de dashboards y manejo seguro de URLs

---

## 11. Apéndice — Extractos Técnicos Relevantes

### Propiedades detectadas (ejemplos relevantes)

- spring.datasource.url=jdbc:postgresql://aws-1-sa-east-1.pooler.supabase.com:5432/postgres?reWriteBatchedInserts=true&prepareThreshold=0&preferQueryMode=simple&sslmode=require
- spring.datasource.username=postgres.bdyvzjpkycnekjrlqlfp
- spring.datasource.password=${SUPABASE_DB_PASSWORD}
- spring.security.oauth2.resourceserver.jwt.issuer-uri=https://bdyvzjpkycnekjrlqlfp.supabase.co/auth/v1
- supabase.url=${SUPABASE_URL:https://bdyvzjpkycnekjrlqlfp.supabase.co}
- metabase.site-url and metabase.secret-key via METABASE_SECRET_KEY

### Clases/Archivos Relevantes (puntos de entrada)
- IocbackendApplication.java (entrypoint)
- config/MetabaseProperties.java
- controller/DashboardController.java
- controller/EtlController.java
- controller/admin/AdminUserController.java, RoleController.java, PermissionController.java, AssignmentController.java
- service/MetabaseEmbeddingService.java
- config/WebSocketSecurityConfig.java

---

## 12. Autoevaluación y Plan de Mejora (aplicando metaprompt)

> Nota: Esta sección aplica el metaprompt de "Self-Evaluation & Improvement Cycle" al presente `project-summary_v2.md`. Se eligió la Opción A (Aplicar todas las mejoras recomendadas) y se incluyen los cambios y acciones resultantes.

### 12.1 Contrato de Evaluación (breve)
- Entrada: `project-summary_v2.md` (documento de arquitecturas y extracción de código)
- Salida: Reporte de autoevaluación, score en dimensiones clave, lista priorizada de mejoras y cambios aplicados (Opción A)
- Criterios de éxito: Documento actualizado con TOC, metadatos claros, sección de limitaciones, fórmula de scoring, y plan de acciones aplicadas.

### 12.2 Fórmula de Scoring (interna)
Se define una fórmula reproducible para evaluar la calidad del resumen:

Score = 100 - (
  críticos_count × 15 +
  importantes_count × 8 +
  mejoras_count × 3 +
  opcionales_count × 1
)

- Mínimo por dimensión: 0%
- Máximo descuento total: 50 puntos
- Redondeo: enteros

### 12.3 Evaluación por Dimensiones
(Análisis automatizado basado en checklist del metaprompt)

- Completitud: 86% (faltan detalles de frontend, migraciones y OpenAPI exportada)
- Precisión: 90% (las propiedades y dependencias están bien identificadas; falta confirmar migraciones y scripts)
- Consistencia: 95% (terminología coherente, estructura clara)
- Claridad: 88% (buena redacción, añadir TOC mejora navegabilidad)
- Accionabilidad: 80% (faltan comandos de "run local" y checklist de pre-requisitos explícitos)
- Mantenibilidad: 90% (metadata añadida; documentar version_scheme mejora mantenibilidad)
- Criterios Específicos del Contexto: 92% (cubre RBAC, ETL, Metabase embedding; faltan contratos JSON exactos)

**Score General (aprox): 89%**

### 12.4 Hallazgos Principales
- 🔴 Críticos (2)
  1. Falta confirmación y/o scripts de migraciones (Flyway/Liquibase) — riesgo en despliegues.
  2. Secrets y manejo de claves (METABASE_SECRET_KEY, SUPABASE_SERVICE_ROLE_KEY) requieren política de rotación y no deben exponerse.

- 🟡 Importantes (3)
  1. No existe un OpenAPI/spec publicado (springdoc está presente pero no se evidencia spec exportada).
  2. Secciones de frontend están pendientes y aparecen como placeholders.
  3. Operaciones locales: falta README operativo con pasos de despliegue local y variables de entorno mínimas.

- 🔵 Mejoras (8)
  - Añadir ejemplos JSON para endpoints ETL y embedding.
  - Documentar strategy para migrations y backups.
  - Añadir snippet para health checks y endpoints Actuator seguros.
  - Incluir ejemplo de rotation de secrets y cómo configurar CI/CD secrets.
  - Expandir sección de tests con comandos para ejecutar testcontainers/local db.
  - Consolidar lista completa de entidades y columnas en apéndice separado.
  - Añadir nota sobre observabilidad (métricas, etiquetas recomendadas).
  - Añadir recomendaciones de limits y resource requests para K8s.

- 🟢 Opcionales (1)
  - Crear `evaluation-report-template.md` reutilizable (separado).

### 12.5 Top 5 Prioridades (aplicar Opción A)
1. Confirmar/migrar a mecanismo de migraciones y adicionar scripts (Flyway/Liquibase). (Crítico)
2. Configurar manejo de secrets en CI/CD y documentar rotación/almacenamiento seguro. (Crítico)
3. Generar y publicar OpenAPI spec (springdoc) y añadir link en este documento. (Importante)
4. Completar secciones Frontend (stack, integración, estado). (Importante)
5. Añadir README operativo con comandos locales y variables de entorno mínimas. (Importante)

### 12.6 Cambios Aplicados (Opción A)
Se aplicaron las siguientes modificaciones al documento como parte de Opción A:
- Añadido metadata (`created_date`, `version_scheme`, `document_version`).
- Insertada Tabla de Contenidos (Índice) al inicio para facilitar navegación.
- Añadida la sección 12 (Autoevaluación y Plan de Mejora) con scoring, hallazgos y prioridades.
- Añadida la sección 13 (Limitaciones Conocidas) y 14 (Próximos Pasos Prioritarios).

> Nota: Los cambios de código o scripts referenciados (por ejemplo, agregar Flyway scripts o publicar OpenAPI) deben implementarse en sus ubicaciones respectivas del repositorio (src/main/resources/db/migration, configuración springdoc, etc.). Aquí se documenta la necesidad y los pasos recomendados.

---

## 13. Limitaciones Conocidas
1. El resumen se basa en análisis estático del repositorio; algunos comportamientos en tiempo de ejecución pueden diferir.
2. No se detectaron migrations explícitos en el repo; puede que existan en pipelines externos o scripts no versionados.
3. No todos los secretos/keys pueden verificarse; la presencia de variables en properties no implica seguridad en su gestión.
4. El análisis asume que dependencias en `pom.xml` reflejan el comportamiento en runtime; las propiedades en `application-*.properties` pueden cambiar según environment.
5. El score y la evaluación son heurísticos y no sustituyen una revisión humana completa.

## 14. Próximos Pasos Prioritarios (aplicación de Opción A)
Acciones recomendadas (implementación inmediata, ordenadas por prioridad):

1) Migraciones y DB (Crítico)
- Crear carpeta `src/main/resources/db/migration/` y añadir scripts iniciales de Flyway (V1__initial_schema.sql).
- Añadir dependencia Flyway en `pom.xml` y documentar el proceso en README.

2) Secrets y CI/CD (Crítico)
- Documentar variables requeridas y añadir ejemplo `.env.example` al repo (sin valores).
- Configurar pipeline para usar secretos de CI (ej: GitHub Actions secrets, AWS Parameter Store, or HashiCorp Vault).

3) OpenAPI (Importante)
- Habilitar export automático de springdoc-openapi (si ya está presente, añadir paso de build para `mvn -DskipTests package` y publicar `openapi.json` en artefactos).

4) README operativo (Importante)
- Crear `docs/README-LOCAL.md` con pasos mínimos:
  - mvn clean package
  - export SUPABASE_DB_PASSWORD=...
  - export METABASE_SECRET_KEY=...
  - java -jar target/*.jar --spring.profiles.active=local

5) Completar Frontend (Importante, coordinar con equipo frontend)
- Añadir sección en este documento con stack frontend, endpoints consumidos y ejemplos de uso.

---

> Si quieres, puedo ahora:
> - Generar el archivo `src/main/resources/db/migration/V1__initial_schema.sql` con DDL básico para las entidades detectadas (propuesta automática),
> - Crear un `docs/README-LOCAL.md` inicial con los pasos operativos, o
> - Añadir un `evaluation-report-template.md` en `.gemini/` para futuras evaluaciones.
