# Inteligencia Operacional Cambiaso (IOC) - Backend API

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-green?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue?logo=docker)
![Status](https://img.shields.io/badge/Status-Production-success)

Servidor central de la plataforma IOC. Este proyecto implementa una arquitectura de microservicios monolíticos (Modular Monolith) encargada de la orquestación de datos, seguridad, integración con IA y lógica de negocio.

## 🧠 Arquitectura y Propósito

El backend actúa como el núcleo de la solución, operando como una **API RESTful** segura y desacoplada.

### Responsabilidades Clave:
1.  **OAuth2 Resource Server:** Validación Stateless de JWTs emitidos por Supabase.
2.  **ETL Pipeline:** Procesamiento masivo de archivos de producción con validación, deduplicación y manejo de errores (Tabla de Cuarentena).
3.  **BI Integration:** Generación de URLs firmadas (HMAC-SHA256) para embedding seguro de Metabase.
4.  **Generative AI:** Integración con **Google Gemini** vía **Spring AI** para análisis de datos en Streaming (SSE).

---

## 🛠 Stack Tecnológico

Basado en Java 21 LTS y Spring Boot 3.5.5.

*   **Core:** Spring Boot Web, Spring Data JPA.
*   **Base de Datos:** PostgreSQL (vía Supabase) + HikariCP.
*   **Seguridad:** Spring Security (OAuth2 Resource Server).
*   **IA:** Spring AI (Google Gemini Flash-Lite).
*   **Resiliencia:** Resilience4j (Circuit Breakers & Rate Limiters).
*   **Performance:** Caffeine Cache (Local caching), JDBC Batching.
*   **Testing:** JUnit 5, Mockito, Testcontainers.
*   **Documentación:** OpenAPI 3 (Swagger).

---

## 🚀 Instalación y Ejecución Local

### Prerrequisitos
*   JDK 21
*   Maven 3.8+ (o usar el wrapper incluido `./mvnw`)
*   Instancia de PostgreSQL (Local o Docker)

### Pasos
1.  **Clonar el repositorio:**
    ```bash
    git clone https://github.com/tu-org/ioc-backend.git
    cd ioc-backend
    ```

2.  **Configuración:**
    Copia el archivo de propiedades de ejemplo o configura tus variables de entorno.
    *Archivo:* `src/main/resources/application.properties`

    Variables críticas requeridas:
    ```properties
    # DB Connection
    SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ioc_db
    SPRING_DATASOURCE_USERNAME=postgres
    SPRING_DATASOURCE_PASSWORD=secret

    # Supabase Auth (Validación JWT)
    SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=https://tu-proyecto.supabase.co/auth/v1
    
    # Metabase Embedding
    METABASE_SITE_URL=https://tu-metabase-url.com
    METABASE_SECRET_KEY=clave_hex_64_chars

    # Google Gemini AI
    GEMINI_API_KEY=tu_api_key_google
    ```

3.  **Compilar y Ejecutar:**
    ```bash
    ./mvnw spring-boot:run
    ```

4.  **Verificación:**
    *   API Health: `http://localhost:8080/actuator/health`
    *   Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 💎 Características Destacadas

### 1. ETL de Alto Rendimiento
Implementación de **JDBC Batching** para resolver el problema de "N+1 queries" durante la carga masiva de datos.
*   **Resultado:** Reducción de tiempo de carga de 4 minutos a <30 segundos para archivos de 17k filas.
*   **Manejo de Errores:** Los registros corruptos no fallan la transacción completa; se derivan a la tabla `quarantined_records`.

### 2. Seguridad "Defense in Depth"
*   **Capa 1 (Red):** Cloudflare Tunnel para exposición segura (HTTPS).
*   **Capa 2 (App):** RBAC (Role-Based Access Control) granular a nivel de endpoint.
*   **Capa 3 (Datos):** Validación de entidades y constraints a nivel de BD.

### 3. Análisis Cognitivo (Streaming)
Endpoint `/api/v1/ai/explain` que utiliza **Server-Sent Events (SSE)**.
*   Envía el contexto JSON del dashboard a Gemini.
*   Retorna la explicación token por token al frontend, reduciendo la latencia percibida por el usuario.

---

## 🧪 Testing

El proyecto cuenta con una suite de pruebas unitarias y de integración.

```bash
# Ejecutar todos los tests
./mvnw test

# Ejecutar tests de integración específicos
./mvnw verify
```

---

## 📦 Despliegue

Optimizado para **Render.com** (Docker/Maven).

El `Dockerfile` incluido realiza un build multi-stage para generar una imagen ligera basada en `eclipse-temurin:21-jre-alpine`.

```bash
# Build manual de Docker
docker build -t ioc-backend .
docker run -p 8080:8080 -e GEMINI_API_KEY=xyz ioc-backend
```

---

## 👥 Autores

*   **Jaime Vicencio** - *Backend Architect & DevOps*
*   **Boris Arriagada** - *Data Engineer & Product Owner*

---
*Proyecto Capstone - Duoc UC 2025*
