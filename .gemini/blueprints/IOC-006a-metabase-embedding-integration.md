# Blueprint: Visualización de Dashboards con Metabase

Esta guía detalla el proceso paso a paso para implementar la funcionalidad de incrustación de dashboards de Metabase.

## 1. Objetivo y Alcance
*   **Objetivo:** Permitir que los usuarios vean dashboards de Metabase de forma segura y filtrada dentro de la aplicación IOC, cumpliendo con la historia de usuario IOC-006a.
*   **Stack Tecnológico Involucrado:** Spring Boot, React, Docker, Metabase.
*   **Contexto Adicional:** La implementación se basará en el método de Static (Signed) Embedding de Metabase para garantizar la seguridad con la versión Open Source. El backend generará URLs firmadas y el frontend las renderizará en un iframe.

## 2. Prerrequisitos
*   **Dependencias:**
    *   Backend: `io.jsonwebtoken` (jjwt-api, jjwt-impl, jjwt-jackson).
*   **Configuración:**
    *   Variables de entorno: `METABASE_SECRET_KEY`.
    *   `application.properties`: `metabase.site.url`, `metabase.secret.key`, `metabase.embedding.dashboards.*.roles`.
*   **Stack Principal:** Backend: Spring Boot, Frontend: React

## 3. Implementación Paso a Paso

### 3.1 Backend (Spring Boot)
*   **Paso 1: Añadir Dependencias JWT**
    *   **Archivo:** `pom.xml`
    *   **Objetivo:** Incluir las librerías necesarias para firmar los tokens de embedding.
    *   **Código:**
        ```xml
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
        ```
    *   **Validación:** El proyecto compila y las dependencias se descargan correctamente.

*   **Paso 2: Crear Excepciones Personalizadas**
    *   **Archivo:** `src/main/java/com/cambiaso/ioc/exception/DashboardAccessDeniedException.java`
    *   **Objetivo:** Crear una excepción específica para manejar intentos de acceso no autorizados a dashboards.
    *   **Código:**
        ```java
        import org.springframework.http.HttpStatus;
        import org.springframework.web.bind.annotation.ResponseStatus;

        @ResponseStatus(HttpStatus.FORBIDDEN)
        public class DashboardAccessDeniedException extends RuntimeException {
            public DashboardAccessDeniedException(String message) {
                super(message);
            }
        }
        ```
    *   **Validación:** La clase es reconocida por el compilador.

*   **Paso 3: Implementar el Servicio de Embedding**
    *   **Archivo:** `src/main/java/com/cambiaso/ioc/service/MetabaseEmbeddingService.java`
    *   **Objetivo:** Encapsular la lógica de validación de permisos y firma de URLs.
    *   **Código:**
        ```java
        package com.cambiaso.ioc.service;

        import io.jsonwebtoken.Jwts;
        import io.jsonwebtoken.security.Keys;
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.stereotype.Service;
        import javax.crypto.SecretKey;
        import java.nio.charset.StandardCharsets;
        import java.util.Map;
        import java.util.concurrent.TimeUnit;
        import java.util.Date;

        @Service
        public class MetabaseEmbeddingService {

            @Value("${metabase.site.url}")
            private String metabaseSiteUrl;

            @Value("${metabase.secret.key}")
            private String metabaseSecretKey;

            // Lógica de autorización (simplificada, expandir según sea necesario)
            public void checkAuthorization(int dashboardId, org.springframework.security.core.Authentication authentication) {
                // TODO: Implementar lógica para verificar si los roles del usuario
                // (authentication.getAuthorities()) tienen permiso para ver el dashboardId.
                // Por ahora, se permite a todos los autenticados.
                if (!authentication.isAuthenticated()) {
                    throw new com.cambiaso.ioc.exception.DashboardAccessDeniedException("User is not authenticated.");
                }
            }

            public String getSignedDashboardUrl(int dashboardId, Map<String, Object> params) {
                SecretKey key = Keys.hmacShaKeyFor(metabaseSecretKey.getBytes(StandardCharsets.UTF_8));

                String token = Jwts.builder()
                        .claim("resource", Map.of("dashboard", dashboardId))
                        .claim("params", params)
                        .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)))
                        .signWith(key)
                        .compact();

                return String.format("%s/embed/dashboard/%s#bordered=true&titled=true", metabaseSiteUrl, token);
            }
        }
        ```
    *   **Validación:** Un test unitario verifica que el método genera una URL firmada correctamente.

*   **Paso 4: Crear el Controlador**
    *   **Archivo:** `src/main/java/com/cambiaso/ioc/controller/DashboardController.java`
    *   **Objetivo:** Exponer un endpoint seguro para que el frontend obtenga la URL del dashboard.
    *   **Código:**
        ```java
        package com.cambiaso.ioc.controller;

        import com.cambiaso.ioc.service.MetabaseEmbeddingService;
        import lombok.RequiredArgsConstructor;
        import org.springframework.http.ResponseEntity;
        import org.springframework.security.core.Authentication;
        import org.springframework.web.bind.annotation.*;
        import java.util.Map;

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
                embeddingService.checkAuthorization(dashboardId, authentication);
                
                Map<String, Object> params = Map.of(); // Sin parámetros por ahora
                String signedUrl = embeddingService.getSignedDashboardUrl(dashboardId, params);

                return ResponseEntity.ok(Map.of("signedUrl", signedUrl));
            }
        }
        ```
    *   **Validación:** Un test de integración con `MockMvc` verifica que el endpoint devuelve un 200 OK para usuarios autenticados y un 403 Forbidden para usuarios no autorizados.

### 3.2 Frontend (React)
*   **Paso 1: Crear Componente de Embedding**
    *   **Archivo:** `src/components/DashboardEmbed.tsx`
    *   **Objetivo:** Crear un componente reutilizable para mostrar dashboards de Metabase.
    *   **Código:**
        ```tsx
        import React, { useState, useEffect } from 'react';

        interface DashboardEmbedProps {
          dashboardId: number;
        }

        const DashboardEmbed: React.FC<DashboardEmbedProps> = ({ dashboardId }) => {
            const [iframeUrl, setIframeUrl] = useState<string>('');
            const [loading, setLoading] = useState<boolean>(true);
            const [error, setError] = useState<string | null>(null);

            useEffect(() => {
                const fetchDashboardUrl = async () => {
                    try {
                        // Asume que el token de autenticación se maneja a través de un interceptor de Axios/fetch
                        const response = await fetch(`/api/dashboards/${dashboardId}`);

                        if (!response.ok) {
                            if (response.status === 403) {
                                throw new Error('No tienes permiso para ver este dashboard.');
                            }
                            throw new Error('No se pudo cargar el dashboard.');
                        }

                        const data = await response.json();
                        setIframeUrl(data.signedUrl);
                    } catch (err: any) {
                        setError(err.message);
                    } finally {
                        setLoading(false);
                    }
                };

                fetchDashboardUrl();
            }, [dashboardId]);

            if (loading) {
                return <div>Cargando Dashboard...</div>; // Reemplazar con un componente Spinner
            }

            if (error) {
                return <div>Error: {error}</div>; // Reemplazar con un componente Alert
            }

            return (
                <iframe
                    src={iframeUrl}
                    frameBorder="0"
                    width="100%"
                    height="800px"
                    allowTransparency
                    title={`Metabase Dashboard ${dashboardId}`}
                ></iframe>
            );
        };

        export default DashboardEmbed;
        ```
    *   **Validación:** El componente se renderiza en una página de prueba y muestra correctamente los estados de carga, error y el iframe final.

## 4. Testing y Validación
### 4.1 Tests Unitarios
*   **Archivo:** `src/test/java/com/cambiaso/ioc/service/MetabaseEmbeddingServiceTest.java`
*   **Casos de Prueba:**
    *   Verificar que `getSignedDashboardUrl` genera un token JWT válido y bien formado.
    *   Verificar que `checkAuthorization` lanza `DashboardAccessDeniedException` para usuarios no autenticados.

### 4.2 Tests de Integración
*   **Archivo:** `src/test/java/com/cambiaso/ioc/controller/DashboardControllerTest.java`
*   **Casos de Prueba:**
    *   Simular una petición GET a `/api/dashboards/5` con un token válido y esperar un 200 OK.
    *   Simular una petición GET a `/api/dashboards/5` sin token y esperar un 401 Unauthorized.

## 5. Flujo de Verificación
1.  Ejecutar el backend con las nuevas variables de entorno (`METABASE_SECRET_KEY`).
2.  Ejecutar el frontend.
3.  Iniciar sesión en la aplicación.
4.  Navegar a la página que contiene el componente `<DashboardEmbed dashboardId={5} />`.
5.  Verificar que se muestra el estado de "Cargando...", seguido del dashboard de Metabase incrustado.

## 6. Consideraciones de Performance
*   **Backend:** La firma de JWT es una operación criptográfica muy rápida. El impacto en el rendimiento del endpoint será insignificante.
*   **Frontend:** El tiempo de carga principal dependerá de la complejidad del dashboard en Metabase y de la velocidad de la conexión a la base de datos. El `iframe` se carga de forma asíncrona y no bloqueará el renderizado del resto de la aplicación.

## 7. Consideraciones de Seguridad y Buenas Practicas
*   El **Embedding Secret Key** NUNCA debe ser expuesto en el código del frontend ni en el control de versiones. Debe gestionarse como un secreto del lado del servidor.
*   La lógica de autorización en `checkAuthorization` debe ser implementada de forma robusta, basándose en los roles del usuario y una configuración centralizada de permisos por dashboard.

## 8. Troubleshooting Común
*   **Error:** El `iframe` muestra "Embedding is not enabled for this dashboard".
    *   **Causa:** Olvidaste habilitar el embedding para ese dashboard específico en la configuración de Metabase.
*   **Error:** El `iframe` muestra "Signature does not match".
    *   **Causa:** El `METABASE_SECRET_KEY` en tu backend no coincide con el que está configurado en Metabase.
