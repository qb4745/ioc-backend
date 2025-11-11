# 📘 **INFORME TÉCNICO: Estrategia de Testing Implementada**

> **Para:** Agentes de IA y Desarrolladores Futuros  
> **Fecha:** 2025-10-25  
> **Proyecto:** IOC Backend  
> **Resultado:** ✅ 100% Tests Pasando

---

## 📊 **RESUMEN EJECUTIVO**

### **Problema Original**
- ❌ Tests fallaban por dependencias no satisfechas (`NotificationService`, `SimpMessagingTemplate`, `MeterRegistry`)
- ❌ Configuración duplicada causaba conflictos de beans
- ❌ Falta de consistencia entre tests (cada uno configurado diferente)
- ❌ Tests lentos por cargar contextos completos innecesariamente

### **Solución Implementada**
- ✅ **Patrón de Testing Centralizado** con `AbstractIntegrationTest` + `GlobalTestConfiguration`
- ✅ **Mocks globales** para servicios externos (WebSocket, métricas, notificaciones)
- ✅ **Configuración por perfil** (`test` para H2, `pgtest` para PostgreSQL)
- ✅ **Slice tests** donde es apropiado (`@WebMvcTest`, `@DataJpaTest`)

### **Resultado**
- ✅ **Todos los tests pasan** (100% success rate)
- ✅ **Configuración mantenible** (un solo lugar para cambios globales)
- ✅ **Tests rápidos** (H2 en memoria para la mayoría)
- ✅ **Arquitectura escalable** (fácil agregar nuevos tests)

---

## 🏗️ **ARQUITECTURA DE TESTING IMPLEMENTADA**

### **Estructura de Archivos Clave**

```
src/test/
├── java/com/cambiaso/ioc/
│   ├── AbstractIntegrationTest.java          ← Clase base para tests de integración
│   ├── config/
│   │   └── GlobalTestConfiguration.java      ← Mocks globales
│   ├── controller/
│   │   ├── DashboardControllerTest.java      ← Usa AbstractIntegrationTest
│   │   └── SecurityConfigTest.java           ← Usa AbstractIntegrationTest
│   ├── integration/
│   │   └── RoleManagementIntegrationTest.java ← Usa AbstractIntegrationTest
│   └── service/
│       └── AdvisoryLockSerializationTest.java ← Usa Testcontainers + perfil pgtest
│
└── resources/
    ├── application-test.properties           ← Config para tests con H2
    └── application-pgtest.properties         ← Config para tests con PostgreSQL
```

---

## 🎯 **COMPONENTES PRINCIPALES**

### **1. `AbstractIntegrationTest` - Clase Base**

```java
@SpringBootTest
@ActiveProfiles("test")
@Import(GlobalTestConfiguration.class)
@Transactional
public abstract class AbstractIntegrationTest {
    // Todos los tests de integración heredan de aquí
}
```

**Propósito:**
- ✅ Centraliza configuración común
- ✅ Carga perfil `test` (H2 en memoria)
- ✅ Importa mocks globales automáticamente
- ✅ Transaccionalidad con rollback automático

**Cuándo usar:**
- ✅ Tests de integración Service + Repository
- ✅ Tests de Controller que necesitan el contexto completo
- ✅ Tests que prueban lógica de negocio con BD

**Cuándo NO usar:**
- ❌ Tests unitarios puros
- ❌ Tests que necesitan PostgreSQL (usar Testcontainers)
- ❌ Tests de controller aislados (usar `@WebMvcTest`)

---

### **2. `GlobalTestConfiguration` - Mocks Centralizados**

```java
@TestConfiguration
public class GlobalTestConfiguration {
    
    @Bean @Primary
    public SimpMessagingTemplate simpMessagingTemplate() {
        return mock(SimpMessagingTemplate.class);
    }
    
    @Bean @Primary
    public NotificationService notificationService() {
        return mock(NotificationService.class);
    }
    
    @Bean @Primary
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}
```

**Propósito:**
- ✅ Provee mocks de servicios externos (WebSocket, métricas)
- ✅ Se carga automáticamente vía `AbstractIntegrationTest`
- ✅ Evita repetición de `@MockBean` en cada test

**Cuándo agregar aquí:**
- ✅ Servicios que dependen de infraestructura externa no disponible en tests
- ✅ Beans que causan errores de "unsatisfied dependency" en múltiples tests

**Cuándo NO agregar:**
- ❌ Mocks específicos de UN solo test (usar `@MockBean` local)
- ❌ Servicios de negocio que SÍ quieres probar (no mockear)

---

### **3. Configuración por Perfil**

#### **`application-test.properties` (H2 - Mayoría de tests)**

```properties
# H2 en memoria (rápido)
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL
spring.jpa.hibernate.ddl-auto=create-drop
spring.sql.init.mode=never

# Metabase de prueba
metabase.secret-key=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef
metabase.dashboards[0].id=1
metabase.dashboards[0].allowed-roles=ROLE_USER,ROLE_ADMIN

# Circuit breaker deshabilitado para tests predecibles
resilience4j.circuitbreaker.configs.default.failureRateThreshold=100
```

#### **`application-pgtest.properties` (PostgreSQL - Tests específicos)**

```properties
# PostgreSQL vía Testcontainers (configurado dinámicamente)
spring.jpa.hibernate.ddl-auto=update

# Metabase de prueba (mismo que test)
metabase.secret-key=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef

# ETL config
etl.lock.enabled=true
```

---

## 📋 **GUÍA: CÓMO CREAR NUEVOS TESTS**

### **Decisión Tree (Qué Tipo de Test Crear)**

```
┌─ ¿Qué quieres probar?
│
├─ 📄 CONTROLLER (capa web)
│  │
│  ├─ ¿Necesitas servicios reales?
│  │  │
│  │  ├─ NO → @WebMvcTest + @MockBean
│  │  │       (Más rápido, aislado)
│  │  │
│  │  └─ SÍ → extends AbstractIntegrationTest + @AutoConfigureMockMvc
│  │          (Contexto completo)
│
├─ 🔧 SERVICE (lógica de negocio)
│  │
│  ├─ ¿Necesitas BD real?
│  │  │
│  │  ├─ NO → Test unitario con mocks
│  │  │       (Sin Spring, solo Mockito)
│  │  │
│  │  └─ SÍ → extends AbstractIntegrationTest
│  │          (H2 + transacciones)
│
├─ 💾 REPOSITORY (persistencia)
│  │
│  ├─ ¿Queries simples de CRUD?
│  │  │
│  │  ├─ SÍ → @DataJpaTest
│  │  │       (Slice test de JPA)
│  │  │
│  │  └─ NO (lógica compleja) → extends AbstractIntegrationTest
│
└─ 🔬 TESTS ESPECIALES (locks, concurrencia, features PostgreSQL)
   │
   └─ @Testcontainers + @ActiveProfiles("pgtest")
      (PostgreSQL real en Docker)
```

---

## ✅ **PATRONES CORRECTOS**

### **Patrón 1: Test de Controller Simple**

```java
@AutoConfigureMockMvc
class MyControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "USER")
    void shouldDoSomething() throws Exception {
        mockMvc.perform(get("/api/endpoint"))
            .andExpect(status().isOk());
    }
}
```

**Características:**
- ✅ Extiende `AbstractIntegrationTest`
- ✅ Usa `@AutoConfigureMockMvc` para inyectar `MockMvc`
- ✅ NO necesita `@SpringBootTest` ni `@ActiveProfiles` (heredados)
- ✅ Mocks globales disponibles automáticamente

---

### **Patrón 2: Test de Service con Repository**

```java
class MyServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MyService myService;
    
    @Autowired
    private MyRepository myRepository;

    @Test
    void shouldProcessBusinessLogic() {
        // Test con BD real (H2) y transacciones
        MyEntity result = myService.doSomething();
        assertThat(result).isNotNull();
    }
}
```

**Características:**
- ✅ Extiende `AbstractIntegrationTest`
- ✅ Transaccional (rollback automático después del test)
- ✅ H2 en memoria (rápido)

---

### **Patrón 3: Test Unitario de Controller (Aislado)**

```java
@WebMvcTest(MyController.class)
@ActiveProfiles("test")
class MyControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MyService myService;  // Mock local

    @BeforeEach
    void setUp() {
        when(myService.getData()).thenReturn(someData);
    }

    @Test
    @WithMockUser
    void shouldReturnData() throws Exception {
        mockMvc.perform(get("/api/data"))
            .andExpect(status().isOk());
    }
}
```

**Características:**
- ✅ NO extiende `AbstractIntegrationTest`
- ✅ Usa `@WebMvcTest` (slice test)
- ✅ Mockea servicios con `@MockBean`
- ✅ Más rápido (no carga toda la aplicación)

---

### **Patrón 4: Test con PostgreSQL (Testcontainers)**

```java
@Testcontainers
@SpringBootTest
@ActiveProfiles("pgtest")
class MyPostgreSQLTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("testdb");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldTestPostgreSQLFeature() {
        // Test con PostgreSQL real
    }
}
```

**Características:**
- ✅ NO extiende `AbstractIntegrationTest` (usa PostgreSQL, no H2)
- ✅ Usa perfil `pgtest`
- ✅ Configura datasource dinámicamente
- ✅ Para tests que REALMENTE necesitan PostgreSQL

---

### **🐘 PostgreSQL Extensions en Testcontainers**

#### **Problema Común**

```
ERROR: type "citext" does not exist
Position: 218

org.postgresql.util.PSQLException: ERROR: type "citext" does not exist
```

#### **Causa**

- Las entidades JPA usan `columnDefinition = "citext"` para columnas case-insensitive (ej: `email`)
- PostgreSQL en Testcontainers **NO** tiene extensiones habilitadas por defecto
- Hibernate intenta crear tablas con tipo `citext` pero la extensión no existe

#### **Solución: Habilitar Extensiones en Tests**

**Paso 1: Crear script de inicialización**

`src/test/resources/init-postgresql.sql`:

```sql
-- ===================================================================
-- POSTGRESQL TESTCONTAINERS INITIALIZATION SCRIPT
-- ===================================================================
-- Este script se ejecuta automáticamente al arrancar el contenedor

-- Enable citext extension (case-insensitive text type)
-- Usado en: AppUser.email y otras columnas que requieren búsquedas sin distinción de mayúsculas
CREATE EXTENSION IF NOT EXISTS citext;

-- Enable UUID generation functions
-- Usado en: Entidades con columnas UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Verify extensions are installed (opcional, para debugging)
SELECT extname, extversion FROM pg_extension 
WHERE extname IN ('citext', 'uuid-ossp')
ORDER BY extname;
```

**Paso 2: Configurar Testcontainer para usar el script**

```java
@Container
static PostgreSQLContainer<?> postgres = 
    new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withInitScript("init-postgresql.sql");  // ← Ejecuta script al arrancar
```

#### **Patrón Recomendado: Clase Base para Tests PostgreSQL**

Para evitar duplicación si tienes múltiples tests con Testcontainers:

**`src/test/java/com/cambiaso/ioc/AbstractPostgreSQLTest.java`:**

```java
package com.cambiaso.ioc;

import com.cambiaso.ioc.config.GlobalTestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Clase base para tests de integración que requieren PostgreSQL real.
 * Configura Testcontainers con extensiones necesarias (citext, uuid-ossp).
 * 
 * Uso:
 * <pre>
 * class MyPostgreSQLTest extends AbstractPostgreSQLTest {
 *     @Test
 *     void myTest() {
 *         // PostgreSQL con citext disponible
 *     }
 * }
 * </pre>
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("pgtest")
@Import(GlobalTestConfiguration.class)
public abstract class AbstractPostgreSQLTest {

    @Container
    protected static final PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init-postgresql.sql");  // Habilita extensiones

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

**Uso en tests:**

```java
// ❌ ANTES - Duplicación de configuración
@Testcontainers
@SpringBootTest
@ActiveProfiles("pgtest")
class AdvisoryLockSerializationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    // ... configuración repetitiva
}

// ✅ DESPUÉS - Reutiliza configuración
class AdvisoryLockSerializationTest extends AbstractPostgreSQLTest {
    @Test
    void advisoryLockBlocksSecondAndLastBatchWins() {
        // Test aquí - PostgreSQL con citext ya disponible
    }
}
```

#### **Configuración de application-pgtest.properties**

```properties
# src/test/resources/application-pgtest.properties

# Database (configurado dinámicamente por Testcontainers)
spring.jpa.hibernate.ddl-auto=update

# NO incluir hibernate.dialect (PostgreSQL lo detecta automáticamente)
# ❌ spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Metabase (valores de prueba - REQUERIDO)
metabase.site-url=https://test-metabase.com
metabase.secret-key=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef
metabase.token-expiration-minutes=10

# Metabase Dashboards (mínimo requerido)
metabase.dashboards[0].id=1
metabase.dashboards[0].name=Test Dashboard
metabase.dashboards[0].allowed-roles=ROLE_USER,ROLE_ADMIN
metabase.dashboards[0].filters[0].name=user_id
metabase.dashboards[0].filters[0].type=USER_ATTRIBUTE
metabase.dashboards[0].filters[0].source=userId

# JWT Issuer (REQUERIDO para SecurityConfig)
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.supabase.co/auth/v1

# ETL Config
etl.lock.enabled=true
etl.duplicate.check.enabled=false
etl.duplicate.fail-on-detect=false

# Circuit Breaker (permisivo para tests)
resilience4j.circuitbreaker.configs.default.failureRateThreshold=100
resilience4j.circuitbreaker.configs.default.waitDurationInOpenState=1000ms
```

#### **Cuándo Usar PostgreSQL (Testcontainers) vs H2**

| Escenario | Base de Datos | Razón |
|-----------|---------------|-------|
| **Tests simples de CRUD** | H2 (`AbstractIntegrationTest`) | ✅ Más rápido |
| **Tests de lógica de negocio** | H2 (`AbstractIntegrationTest`) | ✅ Suficiente para la mayoría |
| **Tests de features PostgreSQL** | PostgreSQL (`AbstractPostgreSQLTest`) | ✅ Advisory locks, citext, etc. |
| **Tests de concurrencia** | PostgreSQL (`AbstractPostgreSQLTest`) | ✅ Comportamiento real |
| **Tests de SQL nativo con funciones PG** | PostgreSQL (`AbstractPostgreSQLTest`) | ✅ Requiere compatibilidad exacta |

#### **Extensiones PostgreSQL Disponibles para Tests**

Puedes agregar más extensiones al script `init-postgresql.sql` según necesite
