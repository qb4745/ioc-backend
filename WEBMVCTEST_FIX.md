# Fix: Error "entityManagerFactory bean not found" en @WebMvcTest

## 🔴 Problema Original

Al ejecutar `DashboardControllerTest`, el test fallaba con el error:

```
A component required a bean named 'entityManagerFactory' that could not be found.
```

Este error no debería ocurrir en un `@WebMvcTest`, ya que este tipo de test solo debería cargar la capa web, NO la capa de persistencia (JPA).

## 🔍 Causa Raíz Identificada

El stacktrace mostraba:

```
Error creating bean with name 'jpaSharedEM_entityManagerFactory'
classes = [com.cambiaso.ioc.IocbackendApplication]
```

Esto indicaba que **toda la aplicación** se estaba cargando, no solo el slice web.

### ¿Por qué pasaba esto?

**Existen DOS archivos de configuración de test que se cargan automáticamente:**

1. **`/src/test/java/com/cambiaso/ioc/config/TestSecurityConfig.java`**
   - Tiene `@TestConfiguration` + `@EnableWebSecurity`
   - Define un bean que depende de `OncePerRequestFilter jwtAuthoritiesAugmentor`
   - Esa dependencia arrastra `SecurityConfig` principal de la aplicación

2. **`/src/test/java/com/cambiaso/ioc/config/TestApplication.java`** ⭐ **ESTE ERA EL CULPABLE PRINCIPAL**
   - Tiene `@Configuration` + `@EnableJpaRepositories`
   - Spring lo descubre **automáticamente** en todos los tests
   - Activa la configuración completa de JPA
   - JPA intenta crear el `entityManagerFactory`
   - **Boom!** Error

## ✅ Solución Implementada

### Cambio en `DashboardControllerTest.java`:

```java
@WebMvcTest(
    controllers = DashboardController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {TestSecurityConfig.class, TestApplication.class}  // ← EXCLUIR AMBOS
    ),
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
    }
)
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetabaseEmbeddingService metabaseEmbeddingService;

    /**
     * Mock UserDetailsService para evitar que Spring Security intente cargar
     * la capa de persistencia (JPA/Hibernate) durante el test de la capa web.
     */
    @MockBean
    private UserDetailsService userDetailsService;

    // ... resto del código
}
```

### ¿Qué hace esta solución?

1. **`excludeFilters`**: Excluye `TestSecurityConfig` y `TestApplication` para este test específico
2. **`excludeAutoConfiguration`**: Desactiva explícitamente las auto-configuraciones de JPA
3. El test queda completamente aislado a solo la capa web
4. `@MockBean UserDetailsService` proporciona el bean mínimo que Spring Security necesita

## 📊 Beneficios de esta solución

| Aspecto | Antes | Después |
|---------|-------|---------|
| **Contexto cargado** | Aplicación completa + JPA | Solo capa web |
| **Velocidad del test** | Lento (carga JPA + DB) | Rápido |
| **Aislamiento** | ❌ Malo | ✅ Perfecto |
| **Mantenibilidad** | ❌ Frágil | ✅ Robusto |

## 🎯 Lecciones Aprendidas

1. **`@Configuration` en tests se descubre automáticamente**: Cualquier clase con `@Configuration` o `@TestConfiguration` en el classpath de tests será descubierta por Spring, a menos que se excluya explícitamente.

2. **`TestApplication` es para tests de integración, NO para slice tests**: Este archivo está diseñado para `@SpringBootTest`, pero interfiere con `@WebMvcTest`, `@DataJpaTest`, etc.

3. **`@WebMvcTest` NO es inmune a configuraciones globales**: Aunque `@WebMvcTest` está diseñado para ser un slice test, configuraciones con `@Configuration` en el classpath pueden colarse.

4. **Doble protección es necesaria**: 
   - `excludeFilters` → Excluye las clases de configuración problemáticas
   - `excludeAutoConfiguration` → Previene que Spring Boot active JPA automáticamente

## 🔧 Estrategia de Exclusión (3 Capas de Defensa)

```java
@WebMvcTest(
    controllers = DashboardController.class,
    // Capa 1: Excluir configuraciones de test problemáticas
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {TestSecurityConfig.class, TestApplication.class}
    ),
    // Capa 2: Excluir auto-configuraciones de JPA
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
    }
)
@ActiveProfiles("test")
class DashboardControllerTest {
    // Capa 3: Mock de beans mínimos necesarios
    @MockBean
    private UserDetailsService userDetailsService;
}
```

---

**Fecha:** 2025-10-24  
**Autor:** GitHub Copilot  
**Tipo:** Bug Fix - Test Configuration
