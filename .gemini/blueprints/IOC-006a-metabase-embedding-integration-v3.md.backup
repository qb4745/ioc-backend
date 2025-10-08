# Blueprint: Visualización de Dashboards con Metabase (v2 - Refined)

Esta guía detalla el proceso paso a paso para implementar la funcionalidad de incrustación de dashboards de Metabase.

## 1. Objetivo y Alcance
*   **Objetivo:** Permitir que los usuarios vean dashboards de Metabase de forma segura y filtrada dentro de la aplicación IOC, cumpliendo con la historia de usuario IOC-006a.
*   **Stack Tecnológico Involucrado:** Spring Boot, React, Docker, Metabase.
*   **Contexto Adicional:** La implementación se basará en el método de Static (Signed) Embedding de Metabase para garantizar la seguridad con la versión Open Source.

## 2. Prerrequisitos
*   **Dependencias:** Investigar si el proyecto ya usa una librería JWT. Si no, añadir `io.jsonwebtoken`.
*   **Configuración:** Variable de entorno `METABASE_SECRET_KEY`.
*   **Stack Principal:** Backend: Spring Boot, Frontend: React.

## 3. Implementación Paso a Paso

### 3.1 Backend (Spring Boot)

*   **Paso 1: Configuración Robusta (`application.properties` y Clase de Configuración)**
    *   **Archivo:** `application.properties`
    *   **Objetivo:** Definir una configuración escalable para los dashboards y sus permisos.
    *   **Código:**
        ```properties
        metabase.site.url=http://localhost:3000
        metabase.secret.key=${METABASE_SECRET_KEY}

        # Configuración de Dashboards Permitidos (formato de lista)
        metabase.dashboards[0].id=5
        metabase.dashboards[0].name=Dashboard Gerencial
        metabase.dashboards[0].allowedRoles=ROLE_ADMIN,ROLE_MANAGER
        metabase.dashboards[1].id=6
        metabase.dashboards[1].name=Dashboard Operacional
        metabase.dashboards[1].allowedRoles=ROLE_USER,ROLE_ADMIN
        ```
    *   **Archivo:** `src/main/java/com/cambiaso/ioc/config/MetabaseProperties.java`
    *   **Objetivo:** Crear una clase para cargar la configuración de forma segura y tipada.
    *   **Código:**
        ```java
        import lombok.Data;
        import org.springframework.boot.context.properties.ConfigurationProperties;
        import org.springframework.context.annotation.Configuration;
        import java.util.List;
        import java.util.Set;

        @Data
        @Configuration
        @ConfigurationProperties(prefix = "metabase")
        public class MetabaseProperties {
            private String siteUrl;
            private String secretKey;
            private List<DashboardConfig> dashboards;

            @Data
            public static class DashboardConfig {
                private int id;
                private String name;
                private Set<String> allowedRoles;
            }
        }
        ```
    *   **Validación:** La aplicación arranca y las propiedades se inyectan correctamente en el bean `MetabaseProperties`.

*   **Paso 2: Crear Excepciones Personalizadas**
    *   **Archivo:** `src/main/java/com/cambiaso/ioc/exception/DashboardAccessDeniedException.java`
    *   **Objetivo:** Crear excepciones que se integren con el `GlobalExceptionHandler`. **No usar `@ResponseStatus`**.
    *   **Código:**
        ```java
        public class DashboardAccessDeniedException extends RuntimeException {
            public DashboardAccessDeniedException(String message) { super(message); }
        }
        // Crear también DashboardNotFoundException de la misma manera.
        ```
    *   **Validación:** El `GlobalExceptionHandler` se actualizará para manejar estas nuevas excepciones y devolver respuestas 403 y 404 estandarizadas.

*   **Paso 3: Implementar el Servicio de Embedding**
    *   **Archivo:** `src/main/java/com/cambiaso/ioc/service/MetabaseEmbeddingService.java`
    *   **Objetivo:** Centralizar la lógica de validación, autorización y firma.
    *   **Código:**
        ```java
        @Service
        public class MetabaseEmbeddingService {
            private final MetabaseProperties properties;
            private final SecretKey key;

            public MetabaseEmbeddingService(MetabaseProperties properties) {
                this.properties = properties;
                // Validación al arranque
                if (properties.getSecretKey() == null || properties.getSecretKey().length() < 16) {
                    throw new IllegalArgumentException("Metabase secret key is not configured or is too short.");
                }
                this.key = Keys.hmacShaKeyFor(properties.getSecretKey().getBytes(StandardCharsets.UTF_8));
            }

            public String getSignedDashboardUrl(int dashboardId, Authentication authentication) {
                MetabaseProperties.DashboardConfig config = findDashboardConfig(dashboardId);
                checkAuthorization(config, authentication);

                Map<String, Object> params = buildParams(config, authentication);

                String token = Jwts.builder()
                        .claim("resource", Map.of("dashboard", dashboardId))
                        .claim("params", params)
                        .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)))
                        .signWith(key)
                        .compact();

                return String.format("%s/embed/dashboard/%s#bordered=true&titled=true", properties.getSiteUrl(), token);
            }

            private MetabaseProperties.DashboardConfig findDashboardConfig(int dashboardId) {
                return properties.getDashboards().stream()
                        .filter(d -> d.getId() == dashboardId)
                        .findFirst()
                        .orElseThrow(() -> new DashboardNotFoundException("Dashboard with ID " + dashboardId + " not found."));
            }

            private void checkAuthorization(MetabaseProperties.DashboardConfig config, Authentication authentication) {
                boolean isAuthorized = authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> config.getAllowedRoles().contains(grantedAuthority.getAuthority()));
                if (!isAuthorized) {
                    throw new DashboardAccessDeniedException("User does not have the required roles to view this dashboard.");
                }
            }
            
            // Manejo seguro de parámetros
            private Map<String, Object> buildParams(MetabaseProperties.DashboardConfig config, Authentication authentication) {
                // Por ahora, no se pasan parámetros del usuario.
                // Si en el futuro se necesita filtrar por user_id, se añadiría aquí de forma segura
                // desde el objeto 'authentication', no desde la petición del cliente.
                return Map.of();
            }
        }
        ```
    *   **Validación:** Tests unitarios verifican la lógica de `findDashboardConfig` y `checkAuthorization`.

*   **Paso 4: Actualizar el Controlador**
    *   **Archivo:** `src/main/java/com/cambiaso/ioc/controller/DashboardController.java`
    *   **Objetivo:** Simplificar el controlador para que solo delegue al servicio.
    *   **Código:**
        ```java
        @RestController
        @RequestMapping("/api/dashboards")
        @RequiredArgsConstructor
        public class DashboardController {
            private final MetabaseEmbeddingService embeddingService;

            @GetMapping("/{dashboardId}")
            public ResponseEntity<Map<String, String>> getDashboardUrl(
                @PathVariable int dashboardId,
                Authentication authentication
            ) {
                String signedUrl = embeddingService.getSignedDashboardUrl(dashboardId, authentication);
                return ResponseEntity.ok(Map.of("signedUrl", signedUrl));
            }
        }
        ```
    *   **Validación:** Tests de integración verifican los casos de éxito (200), no encontrado (404) y prohibido (403).

### 3.2 Frontend (React)
*   **Paso 1: Crear Componente de Embedding con Manejo de Estados**
    *   **Archivo:** `src/components/DashboardEmbed.tsx`
    *   **Objetivo:** Crear un componente robusto que maneje todos los estados de la carga.
    *   **Código:**
        ```tsx
        import React, { useState, useEffect } from 'react';
        import Spinner from './ui/Spinner'; // Reutilizar componente existente
        import Alert from './ui/Alert';   // Reutilizar componente existente

        // ... (interfaz y props) ...

        const DashboardEmbed: React.FC<DashboardEmbedProps> = ({ dashboardId }) => {
            // ... (hooks de estado) ...

            useEffect(() => { /* ... (lógica de fetch) ... */ }, [dashboardId]);

            if (loading) {
                return <Spinner />;
            }

            if (error) {
                return <Alert variant="error" title="Error al Cargar" message={error} />;
            }

            return (
                <iframe
                    src={iframeUrl}
                    // ... (atributos del iframe) ...
                ></iframe>
            );
        };
        ```
    *   **Validación:** El componente se prueba en Storybook o en una página de demostración para verificar visualmente los estados de carga, error y éxito.

## 4. Tareas Técnicas Derivadas (Granulares)

*   **BE-TASK-01:** Investigar y añadir dependencias JWT si es necesario.
*   **BE-TASK-02:** Crear la clase `MetabaseProperties` con `@ConfigurationProperties`.
*   **BE-TASK-03:** Actualizar `application.properties` con la nueva estructura de configuración para dashboards.
*   **BE-TASK-04:** Crear las excepciones `DashboardNotFoundException` y `DashboardAccessDeniedException`.
*   **BE-TASK-05:** Actualizar `GlobalExceptionHandler` para manejar las nuevas excepciones.
*   **BE-TASK-06:** Implementar el método `findDashboardConfig` en `MetabaseEmbeddingService`.
*   **BE-TASK-07:** Implementar el método `checkAuthorization` en `MetabaseEmbeddingService`.
*   **BE-TASK-08:** Implementar el método principal `getSignedDashboardUrl` y la validación al arranque.
*   **BE-TASK-09:** Implementar el `DashboardController`.
*   **FE-TASK-01:** Crear el componente `DashboardEmbed.tsx` con los estados de `loading`, `error` y `success`.
*   **FE-TASK-02:** Integrar los componentes `Spinner` y `Alert` existentes en `DashboardEmbed.tsx`.
*   **QA-TASK-01:** Escribir tests unitarios para la lógica de autorización y configuración en `MetabaseEmbeddingService`.
*   **QA-TASK-02:** Escribir tests de integración para `DashboardController` cubriendo los casos 200, 403 y 404.
