# Resumen Técnico de Arquitectura: Integración de Metabase

## 1. Objetivo y Decisión Arquitectónica Clave

El objetivo de esta implementación es integrar dashboards de Metabase de forma segura y observable en la aplicación IOC.

La decisión arquitectónica fundamental fue utilizar **Static (Signed) Embedding**. Este enfoque se eligió porque es el único método proporcionado por **Metabase Open Source** que garantiza la seguridad de los datos, a diferencia de los "Public Links" (inseguros) y el "Full-App Embedding" (que requiere una licencia de pago).

El flujo consiste en un **backend (Spring Boot) que actúa como un intermediario seguro**, generando URLs firmadas con un secreto compartido, las cuales son consumidas por el **frontend (React)** para renderizar los dashboards en `iframes`.

## 2. Postura de Seguridad (Security by Design)

Se implementó una estrategia de defensa en profundidad para proteger el acceso a los datos.

*   **Principio de Mínimo Privilegio a Nivel de Base de Datos:**
    *   **Problema Solucionado:** Prevenir que una posible vulnerabilidad en Metabase pueda comprometer la base de datos de producción.
    *   **Solución Implementada:** Se creó un rol PostgreSQL dedicado (`metabase_reader`) con permisos exclusivos de `SELECT` sobre las tablas necesarias. Metabase se conecta a los datos de la aplicación utilizando únicamente este rol.

*   **Control de Acceso a Nivel de API (Autorización):**
    *   **Problema Solucionado:** Evitar que un usuario autenticado pueda acceder a dashboards para los que no tiene permiso.
    *   **Solución Implementada:** Se implementó una capa de autorización en el `MetabaseEmbeddingService`. La lógica lee una configuración centralizada en `application.properties` que mapea cada `dashboardId` a una lista de roles permitidos (`allowedRoles`). Antes de firmar una URL, el servicio verifica que los roles del usuario autenticado coincidan con los roles permitidos.

*   **Protección contra Falsificación de Peticiones (Firma de JWT):**
    *   **Problema Solucionado:** Prevenir que un usuario manipule los parámetros de un dashboard (ej. filtros) directamente en la URL.
    *   **Solución Implementada:** El backend genera un token JWT de corta duración (10 minutos) que contiene los parámetros del dashboard. Este token es firmado criptográficamente con el `METABASE_SECRET_KEY`. Metabase valida esta firma antes de renderizar el dashboard, rechazando cualquier petición manipulada.

*   **Protección contra Clickjacking:**
    *   **Problema Solucionado:** Evitar que un sitio web malicioso pueda incrustar la aplicación IOC en un `iframe` para engañar a los usuarios.
    *   **Solución Implementada:** Se configuró Spring Security para emitir los encabezados HTTP `Content-Security-Policy: frame-ancestors 'self'` y `X-Frame-Options: SAMEORIGIN`, instruyendo al navegador para que solo permita la incrustación desde el mismo dominio.

*   **Gestión de Secretos:**
    *   **Problema Solucionado:** Prevenir la exposición de claves secretas en el control de versiones.
    *   **Solución Implementada:** El `METABASE_SECRET_KEY` se gestiona a través de variables de entorno y se carga de forma segura en la aplicación mediante `@ConfigurationProperties`. Se añadió una validación al arranque que aborta el inicio si la clave no está configurada o no es segura.

## 3. Resiliencia y Tolerancia a Fallos

*   **Aislamiento de Fallos (Circuit Breaker):**
    *   **Problema Solucionado:** Prevenir que una caída o lentitud en el servicio de Metabase cause una degradación en cascada del backend de la aplicación.
    *   **Solución Implementada:** Se anotó el método `getSignedDashboardUrl` con `@CircuitBreaker` de **Resilience4j**. Si las llamadas para generar la URL fallan repetidamente, el circuit breaker se "abrirá", y las peticiones subsiguientes fallarán rápidamente sin intentar contactar al servicio, devolviendo un error controlado al usuario.

## 4. Rendimiento y Optimización

*   **Reducción de Latencia (Caching):**
    *   **Problema Solucionado:** Evitar la sobrecarga de generar y firmar un nuevo JWT en cada petición para el mismo dashboard por el mismo usuario.
    *   **Solución Implementada:** Se anotó el método `getSignedDashboardUrl` con `@Cacheable` de Spring. Las URLs firmadas se guardan en un caché en memoria (Caffeine) con una vida corta (2 minutos), reduciendo la latencia en recargas de página o peticiones frecuentes.

## 5. Observabilidad (Métricas y Auditoría)

*   **Auditoría de Acceso:**
    *   **Problema Solucionado:** Tener un registro de quién intenta acceder a qué dashboard y si el acceso fue concedido.
    *   **Solución Implementada:** Se creó un `DashboardAuditService` que registra en los logs cada intento de acceso, incluyendo el `username`, `dashboardId`, y si fue `GRANTED` o `DENIED`.

*   **Métricas de Rendimiento y Uso:**
    *   **Problema Solucionado:** Cuantificar el uso y el rendimiento de la funcionalidad.
    *   **Solución Implementada:** Se integró **Micrometer** para registrar:
        *   Un `Timer` (`metabase.dashboard.request.duration`) que mide la latencia de generación de la URL.
        *   Un `Counter` (`metabase.dashboard.access`) con tags para `status` (success, denied, circuit_open), permitiendo crear alertas y dashboards sobre la tasa de éxito y errores.

## 6. Contrato de API

*   **Endpoint:** `GET /api/v1/dashboards/{dashboardId}`
*   **Autenticación:** Requerida (Bearer Token JWT de usuario).
*   **Respuestas:**
    *   `200 OK`: Devuelve `{ "signedUrl": "..." }`.
    *   `401 Unauthorized`: Token de usuario inválido.
    *   `403 Forbidden`: Usuario válido, pero sin permiso para el dashboard solicitado.
    *   `404 Not Found`: El `dashboardId` no está configurado en el backend.
    *   `503 Service Unavailable`: El Circuit Breaker está abierto (Metabase probablemente caído).
