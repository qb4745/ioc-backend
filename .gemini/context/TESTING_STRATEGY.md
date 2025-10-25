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

## ❌ **ANTI-PATRONES (QUÉ NO HACER)**

### **❌ Anti-Patrón 1: @MockBean Duplicados**

```java
// ❌ MAL
class MyTest extends AbstractIntegrationTest {
    
    @MockBean  // ← Innecesario, ya está en GlobalTestConfiguration
    private NotificationService notificationService;
    
    @MockBean  // ← Innecesario
    private SimpMessagingTemplate simpMessagingTemplate;
}

// ✅ BIEN
class MyTest extends AbstractIntegrationTest {
    // Los mocks se heredan automáticamente
}
```

---

### **❌ Anti-Patrón 2: Usar @SpringBootTest Sin Especificar Classes**

```java
// ❌ MAL - Carga TODO, incluso si no lo necesitas
@SpringBootTest
@ActiveProfiles("test")
class MyTest {
}

// ✅ BIEN - Extiende la clase base
class MyTest extends AbstractIntegrationTest {
}
```

---

### **❌ Anti-Patrón 3: Crear TestApplication con @SpringBootApplication**

```java
// ❌ MAL - Causa doble escaneo de beans
@SpringBootApplication
public class TestApplication {
}

// ✅ BIEN - Usar @Configuration + @EnableAutoConfiguration
@Configuration
@EnableAutoConfiguration
public class GlobalTestConfiguration {
}
```

---

### **❌ Anti-Patrón 4: Usar PostgreSQL para TODO**

```java
// ❌ MAL - Testcontainers es lento
@Testcontainers
@ActiveProfiles("pgtest")
class SimpleRepositoryTest {
    // Test simple de CRUD
}

// ✅ BIEN - H2 es suficiente para tests simples
class SimpleRepositoryTest extends AbstractIntegrationTest {
    // Más rápido con H2
}
```

---

### **❌ Anti-Patrón 5: Duplicar Configuración en Cada Test**

```java
// ❌ MAL
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(GlobalTestConfiguration.class)
class MyTest1 { }

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(GlobalTestConfiguration.class)
class MyTest2 { }

// ✅ BIEN
class MyTest1 extends AbstractIntegrationTest { }
class MyTest2 extends AbstractIntegrationTest { }
```

---

## 📝 **CHECKLIST PARA NUEVOS TESTS**

### **Antes de Crear un Test**

- [ ] ¿Qué estoy probando? (Controller, Service, Repository, E2E)
- [ ] ¿Necesito BD real o puedo usar mocks?
- [ ] ¿Necesito PostgreSQL o H2 es suficiente?
- [ ] ¿Qué tipo de test es el más apropiado?

### **Durante la Implementación**

- [ ] ¿Extiendo `AbstractIntegrationTest`? (para mayoría de tests)
- [ ] ¿O uso `@WebMvcTest`/`@DataJpaTest`? (slice tests)
- [ ] ¿O necesito Testcontainers? (solo para features PostgreSQL)
- [ ] ¿Necesito mocks adicionales o los globales son suficientes?
- [ ] ¿Estoy duplicando configuración que ya existe?

### **Después de Escribir el Test**

- [ ] ¿Compila sin errores?
- [ ] ¿Pasa el test individualmente? (`./mvnw -Dtest=MyTest test`)
- [ ] ¿Pasa con toda la suite? (`./mvnw test`)
- [ ] ¿Es rápido? (< 5 segundos para tests de integración)
- [ ] ¿Está bien documentado con `@DisplayName`?

---

## 🎯 **COMANDOS ÚTILES**

```bash
# Ejecutar un test específico
./mvnw -Dtest=NombreDelTest test

# Ejecutar tests de un paquete
./mvnw -Dtest=com.cambiaso.ioc.controller.* test

# Ejecutar todos los tests
./mvnw test

# Ejecutar tests sin JaCoCo (más rápido)
./mvnw test -Djacoco.skip=true

# Ver cobertura
./mvnw test jacoco:report
xdg-open target/site/jacoco/index.html

# Ejecutar tests en modo debug
./mvnw -Dtest=MyTest test -X

# Limpiar antes de ejecutar
./mvnw clean test
```

---

## 📚 **RESUMEN DE MEJORES PRÁCTICAS**

### **✅ HACER**

1. **Extender `AbstractIntegrationTest`** para la mayoría de tests de integración
2. **Usar H2** cuando sea posible (más rápido)
3. **Usar slice tests** (`@WebMvcTest`, `@DataJpaTest`) cuando sea apropiado
4. **Agregar mocks globales** a `GlobalTestConfiguration` si afectan múltiples tests
5. **Documentar tests** con `@DisplayName`
6. **Mantener tests rápidos** (< 5 segundos para tests de integración)
7. **Usar `@WithMockUser`** para simular autenticación

### **❌ NO HACER**

1. **NO duplicar `@MockBean`** si ya está en `GlobalTestConfiguration`
2. **NO usar `@SpringBootTest` sin extender `AbstractIntegrationTest`** (duplicación)
3. **NO usar Testcontainers** si H2 es suficiente
4. **NO crear múltiples clases de configuración de test** (centralizar en `GlobalTestConfiguration`)
5. **NO olvidar limpiar datos** si no usas `@Transactional`
6. **NO hardcodear valores** (usar properties)
7. **NO probar múltiples cosas** en un solo test (un test = una responsabilidad)

---

## 🎓 **PARA AGENTES DE IA**

### **Cuando un Test Falla con "Bean Not Found"**

```
1. Verificar si el bean necesita estar disponible en tests
   ↓
2. ¿Es un servicio externo (WebSocket, APIs, etc.)?
   │
   ├─ SÍ → Agregar mock a GlobalTestConfiguration
   │
   └─ NO → Verificar que el test cargue el contexto correcto
           (extender AbstractIntegrationTest o usar @SpringBootTest)
```

### **Cuando un Test es Lento**

```
1. ¿Usa Testcontainers innecesariamente?
   → Cambiar a H2 (AbstractIntegrationTest)

2. ¿Carga contexto completo innecesariamente?
   → Usar @WebMvcTest o @DataJpaTest

3. ¿Hace operaciones pesadas en @BeforeEach?
   → Mover a @BeforeAll o usar datos estáticos
```

### **Cuando Hay Conflictos de Beans**

```
1. ¿Hay múltiples @SpringBootApplication en src/test?
   → Eliminar, usar solo GlobalTestConfiguration

2. ¿Hay @EnableJpaRepositories duplicadas?
   → Consolidar en una sola clase de configuración

3. ¿spring.main.allow-bean-definition-overriding=true?
   → Eliminar, arreglar la causa raíz
```

---

## 🚀 **CONCLUSIÓN**

### **Estado Actual**
- ✅ **Arquitectura de testing robusta y escalable**
- ✅ **100% de tests pasando**
- ✅ **Configuración centralizada y mantenible**
- ✅ **Documentación clara para futuros desarrollos**

### **Próximos Pasos Recomendados**
1. Mantener el patrón establecido para nuevos tests
2. Refactorizar tests legacy si existen
3. Considerar agregar tests E2E con `@SpringBootTest` completo para flujos críticos
4. Mantener cobertura de código > 60%

---

**Generado:** 2025-10-25  
**Autor:** Equipo de Desarrollo IOC  
**Versión:** 1.0

---

💡 **¿Preguntas frecuentes?**

- ❓ "¿Por qué mi test no encuentra un bean?" → Revisa "Anti-Patrón 1" y "Para Agentes de IA"
- ❓ "¿Qué tipo de test crear?" → Usa el "Decisión Tree"
- ❓ "¿Cómo hacer que mi test sea más rápido?" → Sección "Cuando un Test es Lento"

---

Este documento debe mantenerse actualizado conforme evolucione la arquitectura de testing. 📘