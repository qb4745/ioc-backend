# Guía de Implementación: Metabase Static Embedding para el Proyecto IOC

## 1. Objetivo
Este documento sirve como la guía técnica para integrar dashboards de Metabase en la aplicación frontend de React, utilizando el método de **Static (Signed) Embedding**. Este enfoque es el único viable para la versión Open Source de Metabase que garantiza la seguridad y el filtrado de datos por usuario.

## 2. Arquitectura y Flujo de Datos
El flujo de la petición será el siguiente:

1.  **Frontend (React):** Un componente de React que necesita mostrar un dashboard realiza una petición a nuestro backend de Spring Boot. La petición puede incluir parámetros de filtrado (ej. rango de fechas).
2.  **Backend (Spring Boot):**
    *   Recibe la petición del frontend.
    *   Construye un payload de JWT (JSON Web Token) que contiene el ID del recurso (el dashboard) y los parámetros de filtrado.
    *   Firma criptográficamente este JWT usando el **"Embedding Secret Key"** proporcionado por Metabase.
    *   Construye la URL final del `iframe`, que incluye el token firmado.
    *   Devuelve esta URL completa y firmada al frontend.
3.  **Frontend (React):**
    *   Recibe la URL firmada del backend.
    *   Renderiza un componente `<iframe>` con esa URL.
4.  **Metabase:**
    *   Recibe la petición del `iframe`.
    *   Verifica la firma del JWT usando su "Embedding Secret Key".
    *   Si la firma es válida, muestra el dashboard con los filtros aplicados. Si no, muestra un error.

**Principio de Seguridad Clave:** El "Embedding Secret Key" NUNCA debe salir del backend. La firma del token siempre debe ocurrir en el lado del servidor.

---

## 3. Fase 1: Configuración en Metabase (Prerrequisitos)

Estos pasos deben ser realizados por un administrador en la interfaz de Metabase.

1.  **Habilitar "Embedding":**
    *   Ir a `Settings` -> `Admin Settings` -> `Embedding`.
    *   Hacer clic en **"Enable"**.

2.  **Obtener la "Embedding Secret Key":**
    *   En la misma página, se generará una clave secreta. Cópiala. Esta clave se usará en nuestro backend.

3.  **Habilitar un Dashboard para Incrustación:**
    *   Navega al dashboard que quieres incrustar.
    *   Haz clic en el **ícono de compartir** (flecha).
    *   Selecciona **"Embedding and public sharing"**.
    *   En la ventana que aparece, haz clic en **"Enable embedding"**.
    *   **Importante:** Anota el **ID del dashboard** que aparece en la URL (ej. `/dashboard/5-my-dashboard`, el ID es `5`).

---

## 4. Fase 2: Implementación del Backend (Spring Boot)

### 4.1. Dependencia de JWT
Añadir una librería para la creación de JWTs al `pom.xml`. `jjwt` es un estándar de la industria.

```xml
<!-- pom.xml -->
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

### 4.2. Configuración
Añadir la URL de Metabase y la clave secreta a `application.properties`.

```properties
# application.properties
metabase.site.url=http://localhost:3000
metabase.secret.key=TU_EMBEDDING_SECRET_KEY_DE_METABASE
```

### 4.3. Lógica de Firma (Servicio)
Crear un servicio que se encargue de generar la URL firmada.

```java
// MetabaseEmbeddingService.java
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

    public String getSignedDashboardUrl(int dashboardId, Map<String, Object> params) {
        SecretKey key = Keys.hmacShaKeyFor(metabaseSecretKey.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .claim("resource", Map.of("dashboard", dashboardId))
                .claim("params", params)
                .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10))) // Expira en 10 minutos
                .signWith(key)
                .compact();

        return String.format("%s/embed/dashboard/%s#bordered=true&titled=true", metabaseSiteUrl, token);
    }
}
```

### 4.4. Endpoint del Controlador
Crear un endpoint que el frontend pueda llamar para obtener la URL.

```java
// DashboardController.java
package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.service.MetabaseEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final MetabaseEmbeddingService embeddingService;

    @GetMapping("/{dashboardId}")
    public ResponseEntity<Map<String, String>> getDashboardUrl(@PathVariable int dashboardId) {
        // Aquí puedes añadir lógica para determinar qué filtros aplicar
        // basados en el usuario autenticado, etc.
        Map<String, Object> params = Map.of(); // Sin parámetros por ahora

        String signedUrl = embeddingService.getSignedDashboardUrl(dashboardId, params);

        return ResponseEntity.ok(Map.of("signedUrl", signedUrl));
    }
}
```

---

## 5. Fase 3: Implementación del Frontend (React)

### 5.1. Componente de Dashboard
Crear un componente de React que obtenga la URL del backend y la muestre en un `iframe`.

```jsx
// DashboardComponent.jsx
import React, { useState, useEffect } from 'react';

const DashboardComponent = ({ dashboardId }) => {
    const [iframeUrl, setIframeUrl] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchDashboardUrl = async () => {
            try {
                // Asume que tienes un token de autenticación para tu propio backend
                const authToken = '...'; // Obtener de tu contexto de autenticación

                const response = await fetch(`/api/dashboards/${dashboardId}`, {
                    headers: {
                        'Authorization': `Bearer ${authToken}`
                    }
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch dashboard URL');
                }

                const data = await response.json();
                setIframeUrl(data.signedUrl);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardUrl();
    }, [dashboardId]);

    if (loading) {
        return <div>Loading Dashboard...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        <iframe
            src={iframeUrl}
            frameBorder="0"
            width="100%"
            height="800"
            allowTransparency
        ></iframe>
    );
};

export default DashboardComponent;
```
