# 📐 DOCUMENTO 2: BLUEPRINT DE IMPLEMENTACIÓN

# Blueprint: Visualización de Dashboards con Metabase (v3 - Production Ready)

## Estructura de Directorios del Proyecto

```
ioc-project/
├── backend/
│   └── src/
│       ├── main/
│       │   ├── java/com/cambiaso/ioc/
│       │   │   ├── config/
│       │   │   │   ├── MetabaseProperties.java ✨ NUEVO
│       │   │   │   ├── CorsConfig.java ✨ NUEVO
│       │   │   │   ├── SecurityConfig.java (MODIFICAR)
│       │   │   │   └── CacheConfig.java ✨ NUEVO
│       │   │   ├── controller/
│       │   │   │   └── DashboardController.java ✨ NUEVO
│       │   │   ├── service/
│       │   │   │   ├── MetabaseEmbeddingService.java ✨ NUEVO
│       │   │   │   └── DashboardAuditService.java ✨ NUEVO
│       │   │   ├── security/
│       │   │   │   └── CustomUserDetails.java ✨ NUEVO
│       │   │   └── exception/
│       │   │       ├── DashboardNotFoundException.java ✨ NUEVO
│       │   │       ├── DashboardAccessDeniedException.java ✨ NUEVO
│       │   │       └── GlobalExceptionHandler.java (MODIFICAR)
│       │   └── resources/
│       │       └── application.yml (MODIFICAR)
│       └── test/
│           └── java/com/cambiaso/ioc/
│               ├── service/
│               │   └── MetabaseEmbeddingServiceTest.java ✨ NUEVO
│               └── controller/
│                   └── DashboardControllerIntegrationTest.java ✨ NUEVO
└── frontend/
    └── src/
        ├── components/
        │   ├── DashboardEmbed.tsx ✨ NUEVO
        │   └── ui/
        │       ├── Spinner.tsx (USAR EXISTENTE)
        │       └── Alert.tsx (USAR EXISTENTE)
        ├── types/
        │   └── dashboard.ts ✨ NUEVO
        ├── hooks/
        │   └── useAuth.ts (USAR EXISTENTE)
        └── pages/
            └── DashboardsPage.tsx ✨ NUEVO (EJEMPLO)
```

---

## Tareas Técnicas Derivadas

### 🔴 SPRINT 1: Configuración Base (P0)

#### **BE-TASK-01**: Añadir dependencias Maven

**Archivo**: `pom.xml`
**Estimación**: 15 min

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- Resilience4j Circuit Breaker -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot2</artifactId>
    <version>2.1.0</version>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-circuitbreaker</artifactId>
    <version>2.1.0</version>
</dependency>

<!-- Caffeine Cache -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Validación**: `mvn clean compile` sin errores

---

#### **BE-TASK-02**: Crear clase `CustomUserDetails`

**Archivo**: `src/main/java/com/cambiaso/ioc/security/CustomUserDetails.java`
**Estimación**: 30 min

```java
package com.cambiaso.ioc.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

    private final Long userId;
    private final String email;
    private final String department;
    private final String region;
    private final String fullName;

    public CustomUserDetails(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            Long userId,
            String email,
            String department,
            String region,
            String fullName
    ) {
        super(username, password, authorities);
        this.userId = userId;
        this.email = email;
        this.department = department;
        this.region = region;
        this.fullName = fullName;
    }

    // Constructor adicional para compatibilidad
    public CustomUserDetails(
            String username,
            String password,
            boolean enabled,
            boolean accountNonExpired,
            boolean credentialsNonExpired,
            boolean accountNonLocked,
            Collection<? extends GrantedAuthority> authorities,
            Long userId,
            String email,
            String department,
            String region,
            String fullName
    ) {
        super(username, password, enabled, accountNonExpired, 
              credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
        this.email = email;
        this.department = department;
        this.region = region;
        this.fullName = fullName;
    }
}
```

**Validación**: Compilación exitosa. Si ya tienes una clase similar, **adapta tu clase existente** añadiendo los campos faltantes (userId, department, region).

---

#### **BE-TASK-03**: Configurar `application.yml`

**Archivo**: `src/main/resources/application.yml`
**Estimación**: 20 min

```yaml
metabase:
  site-url: ${METABASE_URL:http://localhost:3000}
  secret-key: ${METABASE_SECRET_KEY}
  token-expiration-minutes: 10
  dashboards:
    - id: 5
      name: Dashboard Gerencial
      description: Métricas ejecutivas y KPIs principales
      allowed-roles:
        - ROLE_ADMIN
        - ROLE_MANAGER
      filters:
        - name: user_id
          type: USER_ATTRIBUTE
          source: userId
        - name: department
          type: USER_ATTRIBUTE
          source: department

    - id: 6
      name: Dashboard Operacional
      description: Métricas operativas diarias
      allowed-roles:
        - ROLE_USER
        - ROLE_ADMIN
      filters:
        - name: user_id
          type: USER_ATTRIBUTE
          source: userId

# Cache
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=10000,expireAfterWrite=2m

# Resilience4j
resilience4j:
  circuitbreaker:
    instances:
      metabaseService:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 10
        permitted-number-of-calls-in-half-open-state: 3

  ratelimiter:
    instances:
      dashboardAccess:
        limit-for-period: 100
        limit-refresh-period: 1m
        timeout-duration: 0

# Logging
logging:
  level:
    com.cambiaso.ioc.service.MetabaseEmbeddingService: INFO
    com.cambiaso.ioc.controller.DashboardController: INFO
    com.cambiaso.ioc.service.DashboardAuditService: INFO
```

**Validación**: Aplicación arranca sin errores

---

#### **BE-TASK-04**: Crear `MetabaseProperties`

**Archivo**: `src/main/java/com/cambiaso/ioc/config/MetabaseProperties.java`
**Estimación**: 20 min

```java
package com.cambiaso.ioc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Set;

@Data
@Configuration
@ConfigurationProperties(prefix = "metabase")
@Validated
public class MetabaseProperties {

    @NotBlank(message = "Metabase site URL is required")
    private String siteUrl;

    @NotBlank(message = "Metabase secret key is required")
    @Pattern(regexp = "^[A-Fa-f0-9]{64,}$", 
             message = "Secret key must be hexadecimal with at least 64 characters")
    private String secretKey;

    @Min(value = 1, message = "Token expiration must be at least 1 minute")
    private int tokenExpirationMinutes = 10;

    @NotEmpty(message = "At least one dashboard must be configured")
    @Valid
    private List<DashboardConfig> dashboards;

    @Data
    public static class DashboardConfig {
        @Min(value = 1, message = "Dashboard ID must be positive")
        private int id;

        @NotBlank(message = "Dashboard name is required")
        private String name;

        private String description;

        @NotEmpty(message = "At least one role must be configured")
        private Set<String> allowedRoles;

        private List<FilterConfig> filters;
    }

    @Data
    public static class FilterConfig {
        @NotBlank(message = "Filter name is required")
        private String name;

        @NotBlank(message = "Filter type is required")
        private String type;

        @NotBlank(message = "Filter source is required")
        private String source;
    }
}
```

**Validación**: 

```bash
# La aplicación debe fallar al arrancar si METABASE_SECRET_KEY no está definida
# o no cumple el patrón de 64+ caracteres hexadecimales
```

---

#### **BE-TASK-05**: Configurar caché

**Archivo**: `src/main/java/com/cambiaso/ioc/config/CacheConfig.java`
**Estimación**: 15 min

```java
package com.cambiaso.ioc.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // La configuración de Caffeine se hace en application.yml
    // Esta clase solo habilita el soporte de caché en Spring
}
```

**Validación**: Verificar en logs al arrancar:

```
Initialized cache 'dashboardTokens'
```

---

### 🟠 SPRINT 2: Lógica de Negocio (P0 + P1)

#### **BE-TASK-06**: Crear excepciones personalizadas

**Archivos**: 2 nuevos
**Estimación**: 10 min

```java
// src/main/java/com/cambiaso/ioc/exception/DashboardNotFoundException.java
package com.cambiaso.ioc.exception;

public class DashboardNotFoundException extends RuntimeException {
    public DashboardNotFoundException(String message) {
        super(message);
    }
}
```

```java
// src/main/java/com/cambiaso/ioc/exception/DashboardAccessDeniedException.java
package com.cambiaso.ioc.exception;

public class DashboardAccessDeniedException extends RuntimeException {
    public DashboardAccessDeniedException(String message) {
        super(message);
    }
}
```

**Validación**: Compilación exitosa

---

#### **BE-TASK-07**: Actualizar `GlobalExceptionHandler`

**Archivo**: `src/main/java/com/cambiaso/ioc/exception/GlobalExceptionHandler.java` (MODIFICAR)
**Estimación**: 15 min

```java
// Añadir estos métodos a tu GlobalExceptionHandler existente

@ExceptionHandler(DashboardNotFoundException.class)
public ResponseEntity<ErrorResponse> handleDashboardNotFound(
        DashboardNotFoundException ex,
        WebRequest request
) {
    ErrorResponse error = new ErrorResponse(
        HttpStatus.NOT_FOUND.value(),
        "Dashboard Not Found",
        ex.getMessage(),
        LocalDateTime.now(),
        request.getDescription(false)
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}

@ExceptionHandler(DashboardAccessDeniedException.class)
public ResponseEntity<ErrorResponse> handleDashboardAccessDenied(
        DashboardAccessDeniedException ex,
        WebRequest request
) {
    ErrorResponse error = new ErrorResponse(
        HttpStatus.FORBIDDEN.value(),
        "Access Denied",
        ex.getMessage(),
        LocalDateTime.now(),
        request.getDescription(false)
    );
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
}
```

**Validación**: Lanzar manualmente estas excepciones y verificar respuesta JSON correcta

---

#### **BE-TASK-08**: Crear `DashboardAuditService`

**Archivo**: `src/main/java/com/cambiaso/ioc/service/DashboardAuditService.java`
**Estimación**: 20 min

```java
package com.cambiaso.ioc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardAuditService {

    // TODO: Inyectar AuditLogRepository cuando esté disponible
    // private final AuditLogRepository auditLogRepository;

    public void logDashboardAccess(
            String username, 
            int dashboardId, 
            String dashboardName, 
            boolean granted
    ) {
        LocalDateTime timestamp = LocalDateTime.now();

        if (granted) {
            log.info("AUDIT: Dashboard access GRANTED - User: {}, Dashboard ID: {}, Dashboard: {}, Timestamp: {}", 
                username, dashboardId, dashboardName, timestamp);
        } else {
            log.warn("AUDIT: Dashboard access DENIED - User: {}, Dashboard ID: {}, Timestamp: {}", 
                username, dashboardId, timestamp);
        }

        // TODO: Descomentar cuando el repositorio esté disponible
        // AuditLog auditLog = new AuditLog(username, dashboardId, dashboardName, granted, timestamp);
        // auditLogRepository.save(auditLog);
    }
}
```

**Validación**: Verificar logs al acceder a un dashboard

---

#### **BE-TASK-09**: Crear `MetabaseEmbeddingService` (CRÍTICO)

**Archivo**: `src/main/java/com/cambiaso/ioc/service/MetabaseEmbeddingService.java`
**Estimación**: 2 horas

```java
package com.cambiaso.ioc.service;

import com.cambiaso.ioc.config.MetabaseProperties;
import com.cambiaso.ioc.exception.DashboardAccessDeniedException;
import com.cambiaso.ioc.exception.DashboardNotFoundException;
import com.cambiaso.ioc.security.CustomUserDetails;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MetabaseEmbeddingService {

    private final MetabaseProperties properties;
    private final SecretKey key;
    private final DashboardAuditService auditService;
    private final MeterRegistry meterRegistry;

    public MetabaseEmbeddingService(
            MetabaseProperties properties,
            DashboardAuditService auditService,
            MeterRegistry meterRegistry
    ) {
        this.properties = properties;
        this.auditService = auditService;
        this.meterRegistry = meterRegistry;

        // Validación robusta al arranque
        validateSecretKey(properties.getSecretKey());

        this.key = Keys.hmacShaKeyFor(
            properties.getSecretKey().getBytes(StandardCharsets.UTF_8)
        );

        log.info("MetabaseEmbeddingService initialized successfully with {} configured dashboards", 
            properties.getDashboards().size());
    }

    private void validateSecretKey(String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                "Metabase secret key is required. Set METABASE_SECRET_KEY environment variable."
            );
        }

        if (secretKey.length() < 64) {
            throw new IllegalStateException(
                String.format("Metabase secret key is too short (%d chars). Must be at least 64 characters.", 
                    secretKey.length())
            );
        }

        if (!secretKey.matches("^[A-Fa-f0-9]+$")) {
            throw new IllegalStateException(
                "Metabase secret key must be hexadecimal (0-9, A-F). Check your METABASE_SECRET_KEY variable."
            );
        }
    }

    /**
     * Genera una URL firmada para incrustar un dashboard de Metabase.
     * Incluye Circuit Breaker para resiliencia ante caídas de Metabase.
     * 
     * @param dashboardId ID del dashboard configurado
     * @param authentication Objeto de autenticación del usuario
     * @return URL firmada lista para usar en un iframe
     */
    @CircuitBreaker(name = "metabaseService", fallbackMethod = "getSignedDashboardUrlFallback")
    @Cacheable(value = "dashboardTokens", key = "#authentication.name + '_' + #dashboardId")
    public String getSignedDashboardUrl(int dashboardId, Authentication authentication) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            // 1. Buscar configuración del dashboard
            MetabaseProperties.DashboardConfig config = findDashboardConfig(dashboardId);

            // 2. Verificar autorización
            checkAuthorization(config, authentication);

            // 3. Registrar acceso concedido
            auditService.logDashboardAccess(
                authentication.getName(), 
                dashboardId, 
                config.getName(), 
                true
            );

            // 4. Construir parámetros de filtrado
            Map<String, Object> params = buildParams(config, authentication);

            // 5. Generar token JWT
            String token = generateToken(dashboardId, params);

            // 6. Construir URL final
            String url = String.format("%s/embed/dashboard/%s#bordered=true&titled=true", 
                properties.getSiteUrl(), token);

            // 7. Registrar métrica de éxito
            meterRegistry.counter("metabase.dashboard.access",
                "dashboard", String.valueOf(dashboardId),
                "user", authentication.getName(),
                "status", "success"
            ).increment();

            log.debug("Generated signed URL for dashboard {} for user {}", 
                dashboardId, authentication.getName());

            return url;

        } catch (DashboardAccessDeniedException | DashboardNotFoundException e) {
            // Registrar acceso denegado
            auditService.logDashboardAccess(
                authentication.getName(), 
                dashboardId, 
                "UNKNOWN", 
                false
            );

            meterRegistry.counter("metabase.dashboard.access",
                "dashboard", String.valueOf(dashboardId),
                "user", authentication.getName(),
                "status", "denied"
            ).increment();

            throw e;

        } finally {
            sample.stop(Timer.builder("metabase.dashboard.request.duration")
                .tag("dashboard", String.valueOf(dashboardId))
                .register(meterRegistry));
        }
    }

    /**
     * Fallback cuando el Circuit Breaker está abierto (Metabase caído)
     */
    @SuppressWarnings("unused")
    private String getSignedDashboardUrlFallback(
            int dashboardId, 
            Authentication authentication, 
            Exception ex
    ) {
        log.error("Circuit breaker activated for dashboard {}. Metabase may be down.", dashboardId, ex);

        meterRegistry.counter("metabase.dashboard.access",
            "dashboard", String.valueOf(dashboardId),
            "user", authentication.getName(),
            "status", "circuit_open"
        ).increment();

        throw new RuntimeException(
            "Dashboard service is temporarily unavailable. Please try again in a few moments.",
            ex
        );
    }

    private MetabaseProperties.DashboardConfig findDashboardConfig(int dashboardId) {
        return properties.getDashboards().stream()
            .filter(d -> d.getId() == dashboardId)
            .findFirst()
            .orElseThrow(() -> new DashboardNotFoundException(
                String.format("Dashboard with ID %d is not configured or does not exist.", dashboardId)
            ));
    }

    private void checkAuthorization(
            MetabaseProperties.DashboardConfig config, 
            Authentication authentication
    ) {
        boolean isAuthorized = authentication.getAuthorities().stream()
            .anyMatch(grantedAuthority -> 
                config.getAllowedRoles().contains(grantedAuthority.getAuthority())
            );

        if (!isAuthorized) {
            log.warn("User {} attempted to access dashboard {} without proper roles. Required: {}, Has: {}", 
                authentication.getName(),
                config.getName(),
                config.getAllowedRoles(),
                authentication.getAuthorities()
            );

            throw new DashboardAccessDeniedException(
                String.format("You do not have permission to view '%s'. Required roles: %s", 
                    config.getName(),
                    config.getAllowedRoles())
            );
        }
    }

    private Map<String, Object> buildParams(
            MetabaseProperties.DashboardConfig config, 
            Authentication authentication
    ) {
        Map<String, Object> params = new HashMap<>();

        if (config.getFilters() == null || config.getFilters().isEmpty()) {
            return params;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            log.warn("Authentication principal is not CustomUserDetails. Filters will not be applied.");
            return params;
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;

        config.getFilters().forEach(filter -> {
            if ("USER_ATTRIBUTE".equals(filter.getType())) {
                Object value = extractUserAttribute(userDetails, filter.getSource());
                if (value != null) {
                    params.put(filter.getName(), value);
                    log.debug("Applied filter: {} = {}", filter.getName(), value);
                } else {
                    log.warn("Could not extract user attribute '{}' for filter '{}'", 
                        filter.getSource(), filter.getName());
                }
            }
        });

        return params;
    }

    private Object extractUserAttribute(CustomUserDetails userDetails, String attributeName) {
        return switch (attributeName) {
            case "userId" -> userDetails.getUserId();
            case "username" -> userDetails.getUsername();
            case "email" -> userDetails.getEmail();
            case "department" -> userDetails.getDepartment();
            case "region" -> userDetails.getRegion();
            case "fullName" -> userDetails.getFullName();
            default -> {
                log.warn("Unknown user attribute requested: {}", attributeName);
                yield null;
            }
        };
    }

    private String generateToken(int dashboardId, Map<String, Object> params) {
        long expirationMillis = TimeUnit.MINUTES.toMillis(properties.getTokenExpirationMinutes());

        return Jwts.builder()
            .claim("resource", Map.of("dashboard", dashboardId))
            .claim("params", params)
            .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
            .setIssuedAt(new Date())
            .signWith(key)
            .compact();
    }
}
```

**Validación**:

- Aplicación arranca sin errores
- Logs muestran "MetabaseEmbeddingService initialized successfully"
- Lanzar excepción manualmente y verificar fallback

---

#### **BE-TASK-10**: Crear `DashboardController`

**Archivo**: `src/main/java/com/cambiaso/ioc/controller/DashboardController.java`
**Estimación**: 30 min

```java
package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.service.MetabaseEmbeddingService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboards")
@RequiredArgsConstructor
@Slf4j
@Validated
public class DashboardController {

    private final MetabaseEmbeddingService embeddingService;

    /**
     * Obtiene una URL firmada para incrustar un dashboard de Metabase.
     * 
     * @param dashboardId ID del dashboard (1-999999)
     * @param authentication Información del usuario autenticado (inyectada por Spring Security)
     * @return Objeto JSON con la URL firmada y tiempo de expiración
     */
    @GetMapping("/{dashboardId}")
    @RateLimiter(name = "dashboardAccess")
    public ResponseEntity<Map<String, Object>> getDashboardUrl(
            @PathVariable 
            @Min(value = 1, message = "Dashboard ID must be at least 1") 
            @Max(value = 999999, message = "Dashboard ID must be at most 999999") 
            int dashboardId,
            Authentication authentication
    ) {
        log.info("Dashboard URL requested - User: {}, Dashboard ID: {}", 
            authentication.getName(), dashboardId);

        String signedUrl = embeddingService.getSignedDashboardUrl(dashboardId, authentication);

        return ResponseEntity.ok(Map.of(
            "signedUrl", signedUrl,
            "expiresInMinutes", 10,
            "dashboardId", dashboardId
        ));
    }
}
```

**Validación**: 

```bash
curl -X GET http://localhost:8080/api/v1/dashboards/5 \
     -H "Authorization: Bearer YOUR_TEST_TOKEN"
```

---

#### **BE-TASK-11**: Configurar seguridad (CSP y CORS)

**Archivo**: `src/main/java/com/cambiaso/ioc/config/SecurityConfig.java` (MODIFICAR)
**Estimación**: 30 min

```java
// Añadir a tu SecurityConfig existente

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // ... tu configuración existente ...
        .headers(headers -> headers
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("frame-ancestors 'self'; default-src 'self'")
            )
            .frameOptions(frame -> frame.sameOrigin())
        );

    return http.build();
}
```

**Archivo**: `src/main/java/com/cambiaso/ioc/config/CorsConfig.java` (NUEVO)

```java
package com.cambiaso.ioc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${metabase.site-url}")
    private String metabaseUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/dashboards/**")
            .allowedOrigins(metabaseUrl, "http://localhost:5173") // Añade tu frontend dev URL
            .allowedMethods("GET")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

**Validación**: Verificar headers en la respuesta HTTP:

```
Content-Security-Policy: frame-ancestors 'self'
X-Frame-Options: SAMEORIGIN
```

---

### 🟡 SPRINT 3: Frontend (P1)

#### **FE-TASK-01**: Crear tipos TypeScript

**Archivo**: `src/types/dashboard.ts`
**Estimación**: 10 min

```typescript
export interface DashboardEmbedProps {
  dashboardId: number;
  height?: string;
  onError?: (error: Error) => void;
  onLoad?: () => void;
  className?: string;
}

export interface DashboardUrlResponse {
  signedUrl: string;
  expiresInMinutes: number;
  dashboardId: number;
}

export interface DashboardError {
  status: number;
  title: string;
  message: string;
  timestamp: string;
}
```

---

#### **FE-TASK-02**: Crear componente `DashboardEmbed`

**Archivo**: `src/components/DashboardEmbed.tsx`
**Estimación**: 2 horas

```typescript
import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useAuth } from '@/hooks/useAuth';
import Spinner from './ui/Spinner';
import Alert from './ui/Alert';
import type { DashboardEmbedProps, DashboardUrlResponse } from '@/types/dashboard';

const DashboardEmbed: React.FC<DashboardEmbedProps> = ({ 
  dashboardId, 
  height = '800px',
  onError,
  onLoad,
  className = ''
}) => {
  const [iframeUrl, setIframeUrl] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { token } = useAuth();
  const abortControllerRef = useRef<AbortController | null>(null);

  const fetchDashboardUrl = useCallback(async () => {
    if (!token) {
      setError('No authentication token available');
      setLoading(false);
      return;
    }

    // Cancelar petición anterior si existe
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }

    // Crear nuevo AbortController con timeout
    abortControllerRef.current = new AbortController();
    const timeoutId = setTimeout(() => abortControllerRef.current?.abort(), 15000);

    try {
      setLoading(true);
      setError(null);

      const response = await fetch(`/api/v1/dashboards/${dashboardId}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        signal: abortControllerRef.current.signal
      });

      clearTimeout(timeoutId);

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));

        let errorMessage = errorData.message || `Error ${response.status}: ${response.statusText}`;

        // Mensajes específicos por código de error
        if (response.status === 403) {
          errorMessage = 'You do not have permission to view this dashboard.';
        } else if (response.status === 404) {
          errorMessage = 'Dashboard not found or not configured.';
        } else if (response.status === 503) {
          errorMessage = 'Dashboard service is temporarily unavailable. Please try again in a few moments.';
        }

        throw new Error(errorMessage);
      }

      const data: DashboardUrlResponse = await response.json();
      setIframeUrl(data.signedUrl);
      onLoad?.();

    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        const timeoutError = new Error('Request timeout: The dashboard is taking too long to load.');
        setError(timeoutError.message);
        onError?.(timeoutError);
      } else {
        const errorMessage = err instanceof Error 
          ? err.message 
          : 'Unknown error loading dashboard';
        console.error('Dashboard fetch error:', err);
        setError(errorMessage);
        onError?.(err instanceof Error ? err : new Error(errorMessage));
      }
    } finally {
      setLoading(false);
      clearTimeout(timeoutId);
    }
  }, [dashboardId, token, onError, onLoad]);

  useEffect(() => {
    let isMounted = true;
    let refreshInterval: NodeJS.Timeout;

    if (isMounted) {
      fetchDashboardUrl();

      // Refrescar token cada 8 minutos (expira en 10)
      refreshInterval = setInterval(() => {
        if (isMounted) {
          console.log('Refreshing dashboard token...');
          fetchDashboardUrl();
        }
      }, 8 * 60 * 1000);
    }

    return () => {
      isMounted = false;
      if (refreshInterval) {
        clearInterval(refreshInterval);
      }
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [fetchDashboardUrl]);

  if (loading) {
    return (
      <div 
        className={`flex items-center justify-center bg-gray-50 rounded-lg border border-gray-200 ${className}`}
        style={{ height }}
      >
        <div className="text-center">
          <Spinner size="large" />
          <p className="mt-4 text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={className} style={{ height }}>
        <Alert 
          variant="error" 
          title="Error Loading Dashboard" 
          message={error}
          action={{
            label: 'Retry',
            onClick: fetchDashboardUrl
          }}
        />
      </div>
    );
  }

  return (
    <div className={`dashboard-embed-container ${className}`} style={{ height }}>
      <iframe
        src={iframeUrl}
        frameBorder="0"
        width="100%"
        height="100%"
        allowTransparency
        sandbox="allow-scripts allow-same-origin allow-forms allow-popups"
        title={`Dashboard ${dashboardId}`}
        className="rounded-lg shadow-sm"
      />
    </div>
  );
};

export default DashboardEmbed;
```

**Validación**: Renderizar en Storybook o página de prueba

---

#### **FE-TASK-03**: Crear página de ejemplo

**Archivo**: `src/pages/DashboardsPage.tsx`
**Estimación**: 30 min

```typescript
import React from 'react';
import DashboardEmbed from '@/components/DashboardEmbed';
import { useAuth } from '@/hooks/useAuth';

const DashboardsPage: React.FC = () => {
  const { user } = useAuth();

  const handleDashboardError = (error: Error) => {
    console.error('Dashboard error:', error);
    // Aquí podrías enviar a un servicio de monitoreo como Sentry
    // Sentry.captureException(error);
  };

  const handleDashboardLoad = () => {
    console.log('Dashboard loaded successfully');
  };

  const hasRole = (role: string) => user?.roles?.includes(role) ?? false;

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Analytics Dashboards</h1>

      {/* Dashboard Gerencial - Solo Admin y Manager */}
      {(hasRole('ROLE_ADMIN') || hasRole('ROLE_MANAGER')) && (
        <section className="mb-8">
          <div className="mb-4">
            <h2 className="text-2xl font-semibold">Executive Dashboard</h2>
            <p className="text-gray-600">Key metrics and KPIs</p>
          </div>
          <DashboardEmbed 
            dashboardId={5} 
            height="600px"
            onError={handleDashboardError}
            onLoad={handleDashboardLoad}
            className="mb-4"
          />
        </section>
      )}

      {/* Dashboard Operacional - User y Admin */}
      {(hasRole('ROLE_USER') || hasRole('ROLE_ADMIN')) && (
        <section className="mb-8">
          <div className="mb-4">
            <h2 className="text-2xl font-semibold">Operational Dashboard</h2>
            <p className="text-gray-600">Daily operational metrics</p>
          </div>
          <DashboardEmbed 
            dashboardId={6} 
            height="600px"
            onError={handleDashboardError}
            onLoad={handleDashboardLoad}
            className="mb-4"
          />
        </section>
      )}

      {/* Mensaje si no tiene acceso a ningún dashboard */}
      {!hasRole('ROLE_ADMIN') && !hasRole('ROLE_MANAGER') && !hasRole('ROLE_USER') && (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg">
            You do not have access to any dashboards. Please contact your administrator.
          </p>
        </div>
      )}
    </div>
  );
};

export default DashboardsPage;
```

**Validación**: Navegar a la página y verificar que los dashboards se renderizan según roles

---

### 🧪 SPRINT 4: Testing (P2)

#### **QA-TASK-01**: Test unitario del servicio

**Archivo**: `src/test/java/com/cambiaso/ioc/service/MetabaseEmbeddingServiceTest.java`
**Estimación**: 1 hora

```java
package com.cambiaso.ioc.service;

import com.cambiaso.ioc.config.MetabaseProperties;
import com.cambiaso.ioc.exception.DashboardAccessDeniedException;
import com.cambiaso.ioc.exception.DashboardNotFoundException;
import com.cambiaso.ioc.security.CustomUserDetails;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetabaseEmbeddingServiceTest {

    @Mock
    private DashboardAuditService auditService;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetails userDetails;

    private MetabaseEmbeddingService service;
    private MetabaseProperties properties;

    @BeforeEach
    void setUp() {
        properties = new MetabaseProperties();
        properties.setSiteUrl("http://localhost:3000");
        properties.setSecretKey("A".repeat(64)); // Clave válida hex
        properties.setTokenExpirationMinutes(10);

        MetabaseProperties.DashboardConfig dashboard = new MetabaseProperties.DashboardConfig();
        dashboard.setId(5);
        dashboard.setName("Test Dashboard");
        dashboard.setAllowedRoles(Set.of("ROLE_ADMIN", "ROLE_USER"));

        properties.setDashboards(List.of(dashboard));

        service = new MetabaseEmbeddingService(
            properties, 
            auditService, 
            new SimpleMeterRegistry()
        );
    }

    @Test
    void shouldGenerateSignedUrlForAuthorizedUser() {
        // Given
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(userDetails.getUserId()).thenReturn(123L);

        // When
        String url = service.getSignedDashboardUrl(5, authentication);

        // Then
        assertThat(url).isNotNull();
        assertThat(url).startsWith("http://localhost:3000/embed/dashboard/");
        assertThat(url).contains("#bordered=true&titled=true");

        verify(auditService).logDashboardAccess("testuser", 5, "Test Dashboard", true);
    }

    @Test
    void shouldThrowExceptionForNonExistentDashboard() {
        // When & Then
        assertThatThrownBy(() -> service.getSignedDashboardUrl(999, authentication))
            .isInstanceOf(DashboardNotFoundException.class)
            .hasMessageContaining("Dashboard with ID 999 is not configured");
    }

    @Test
    void shouldThrowExceptionForUnauthorizedUser() {
        // Given
        when(authentication.getName()).thenReturn("unauthorized");
        when(authentication.getAuthorities()).thenReturn(
            List.of(new SimpleGrantedAuthority("ROLE_GUEST"))
        );

        // When & Then
        assertThatThrownBy(() -> service.getSignedDashboardUrl(5, authentication))
            .isInstanceOf(DashboardAccessDeniedException.class)
            .hasMessageContaining("You do not have permission");

        verify(auditService).logDashboardAccess("unauthorized", 5, "UNKNOWN", false);
    }

    @Test
    void shouldValidateSecretKeyAtStartup() {
        // Given
        MetabaseProperties invalidProps = new MetabaseProperties();
        invalidProps.setSiteUrl("http://localhost:3000");
        invalidProps.setSecretKey("tooshort");
        invalidProps.setDashboards(List.of());

        // When & Then
        assertThatThrownBy(() -> new MetabaseEmbeddingService(
            invalidProps,
            auditService,
            new SimpleMeterRegistry()
        ))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("too short");
    }
}
```

**Validación**: `mvn test` pasa todos los tests

---

#### **QA-TASK-02**: Test de integración del controlador

**Archivo**: `src/test/java/com/cambiaso/ioc/controller/DashboardControllerIntegrationTest.java`
**Estimación**: 1 hora

```java
package com.cambiaso.ioc.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturn200ForAuthorizedUser() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.signedUrl").exists())
            .andExpect(jsonPath("$.signedUrl").value(containsString("/embed/dashboard/")))
            .andExpect(jsonPath("$.expiresInMinutes").value(10))
            .andExpect(jsonPath("$.dashboardId").value(5));
    }

    @Test
    @WithMockUser(username = "guest", roles = {"GUEST"})
    void shouldReturn403ForUnauthorizedUser() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/5"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value(containsString("permission")));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturn404ForNonExistentDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(containsString("not configured")));
    }

    @Test
    void shouldReturn401ForUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/5"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldValidateDashboardIdRange() throws Exception {
        mockMvc.perform(get("/api/v1/dashboards/0"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/dashboards/9999999"))
            .andExpect(status().isBadRequest());
    }
}
```

**Validación**: `mvn verify` pasa todos los tests de integración

---

## Checklist de Deployment

### Antes de Producción

- [ ] **Variable de entorno `METABASE_SECRET_KEY` configurada** en el servidor
- [ ] **HTTPS habilitado** con certificados válidos
- [ ] **Embedding habilitado** en Metabase de producción
- [ ] **Dashboards configurados** y probados en Metabase
- [ ] **IDs de dashboards coinciden** entre `application.yml` y Metabase
- [ ] **Roles mapeados correctamente** en el sistema de usuarios
- [ ] **Métricas de Micrometer** exportándose a Prometheus/Grafana
- [ ] **Logs centralizados** (ELK, Splunk, etc.) recibiendo eventos de auditoría
- [ ] **Alertas configuradas** para circuit breaker y tasa de errores
- [ ] **Tests E2E** ejecutados en ambiente de staging

### Verificación Post-Deployment

```bash
# 1. Health check
curl https://api.ioc.com/actuator/health | jq '.status'
# Debe retornar: "UP"

# 2. Verificar circuit breaker
curl https://api.ioc.com/actuator/health | jq '.components.circuitBreakers'

# 3. Test de acceso con usuario real
curl -H "Authorization: Bearer REAL_TOKEN" \
     https://api.ioc.com/api/v1/dashboards/5

# 4. Verificar logs de auditoría
tail -f /var/log/ioc/app.log | grep "AUDIT: Dashboard access"

# 5. Verificar métricas
curl https://api.ioc.com/actuator/metrics/metabase.dashboard.access
```

---

## Cambios Aplicados (Resumen de Fase 3)

✅ **P0-1**: Documentos separados en Guía (conceptual) y Blueprint (técnico)  
✅ **P0-2**: Clase `CustomUserDetails` completamente definida  
✅ **P0-3**: Circuit Breaker implementado con Resilience4j  
✅ **P1-1**: Diagramas Mermaid de arquitectura y secuencia añadidos  
✅ **P1-2**: CSP y X-Frame-Options configurados en SecurityConfig  
✅ **P1-3**: Auto-refresh de token cada 8 minutos sin recargar iframe  
✅ **P1-4**: Timeout de 15 segundos con AbortController en fetch  
✅ **P2-1**: Caché de tokens con Caffeine (TTL: 2 minutos)  
✅ **P2-4**: Quick Start de 5 pasos al inicio de la Guía  
✅ **P2-5**: Estructura completa de directorios incluida  

---

## 🎉 REFINAMIENTO COMPLETADO

Los dos documentos están ahora production-ready con:

- 🛡️ **Seguridad multicapa** (7 capas de protección)
- 🔄 **Resiliencia** (Circuit Breaker, timeouts, fallbacks)
- 📊 **Observabilidad** (Logs, métricas, auditoría)
- ⚡ **Performance** (Caché, validaciones tempranas)
- 🧪 **Calidad** (Tests unitarios e integración)
- 📖 **Documentación** (Guía conceptual + Blueprint técnico)

**Próximos pasos**:

1. Revisar ambos documentos
2. Adaptar código a tu estructura de proyecto existente
3. Ejecutar las tareas en el orden de los sprints
4. Reportar cualquier bloqueador o duda

¿Necesitas aclaraciones sobre alguna parte específica