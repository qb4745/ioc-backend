# Ficha Técnica de Implementación (FTI): Integración de Metabase (v2 - Refined)

## 1. Resumen Ejecutivo y Objetivo

*   **Feature:** Visualización de Datos y KPIs a través de Metabase.
*   **Objetivo:** Integrar la plataforma de Business Intelligence (BI) Metabase (versión Open Source) con el ecosistema IOC para proporcionar capacidades de visualización de datos, creación de dashboards y KPIs.
*   **Enfoque Arquitectónico:** Se desplegará Metabase como un servicio auto-hosteado (vía Docker). El backend de Spring Boot actuará como un intermediario seguro para generar URLs de incrustación firmadas (Static Embedding), que el frontend de React consumirá para renderizar los dashboards en `iframes`.

## 2. Actores y Responsabilidades

*   **Administrador del Sistema:** Despliega y mantiene Metabase, gestiona la conexión a la BD y configura los dashboards.
*   **Desarrollador Backend:** Implementa el endpoint seguro que genera las URLs firmadas.
*   **Desarrollador Frontend:** Crea el componente de React que gestiona los estados (carga, error) y renderiza el `iframe`.
*   **Analista / Administrador de Datos:** Crea y edita gráficos y dashboards dentro de Metabase.

## 3. Diseño de Alto Nivel y Contrato de API

### 3.1. Diagrama de Flujo
```
+---------------------+      +-------------------------+      +------------------------+
| Frontend (React)    |      | Backend (Spring Boot)   |      | Metabase (Docker)      |
+---------------------+      +-------------------------+      +------------------------+
           |                           |                                |
           | 1. GET /api/dashboards/5  |                                |
           |    (con Auth Token User)  |                                |
           |-------------------------->|                                |
           |                           | 2. Autoriza: ¿Puede el usuario |
           |                           |    ver el Dashboard 5?         |
           |                           |                                |
           |                           | 3. Firma JWT de Embedding      |
           |                           |    con Secret Key de Metabase  |
           |                           |                                |
           | 4. Devuelve 200 OK con URL|                                |
           |<--------------------------|                                |
           |                           |                                |
           | 5. Renderiza <iframe>     |                                |
           |    con la URL firmada     |                                |
           |----------------------------------------------------------->|
           |                           |                                | 6. Valida firma y muestra
           |                           |                                |
```

### 3.2. Contrato de API Explícito

**Endpoint:** `GET /api/dashboards/{dashboardId}`
*   **Autenticación:** Requerida (Bearer Token JWT de usuario).
*   **Parámetros de Ruta:**
    *   `dashboardId` (integer): El ID del dashboard de Metabase a incrustar.
*   **Respuestas de Éxito:**
    *   **`200 OK`**:
        ```json
        {
          "signedUrl": "http://localhost:3000/embed/dashboard/eyJh..."
        }
        ```
*   **Respuestas de Error:**
    *   **`401 Unauthorized`**: Si el token de usuario no es válido.
    *   **`403 Forbidden`**: Si el usuario está autenticado pero no tiene permiso para ver el `dashboardId` solicitado.
    *   **`404 Not Found`**: Si el `dashboardId` no existe o no está configurado para embedding en el backend.

## 4. Plan de Implementación Detallado

### Fase 1: Infraestructura y Configuración (DevOps)

1.  **Crear Usuario de BD de Solo Lectura:**
    *   **Acción:** En Supabase, crear un rol `metabase_reader` con permisos de `SELECT` sobre las tablas y vistas necesarias.
2.  **Desplegar Metabase con Docker:**
    *   **Acción:** Usar un `docker-compose.metabase.yml` para levantar el servicio.
3.  **Configuración Inicial de Metabase:**
    *   **Acción:** Conectar Metabase a la BD principal usando las credenciales del usuario `metabase_reader`.
4.  **Habilitar Embedding y Obtener Secretos:**
    *   **Acción:** En `Admin Settings` -> `Embedding`, habilitar la funcionalidad, copiar el **Embedding Secret Key** y habilitar un dashboard de prueba, anotando su ID.

### Fase 2: Backend (Spring Boot)

1.  **Investigación de Dependencias:**
    *   **Acción:** Verificar si el `pom.xml` ya contiene una librería para manejar JWTs (provista por Spring Security OAuth2). Si es así, reutilizarla. Si no, añadir `io.jsonwebtoken`.
2.  **Actualizar Configuración (`application.properties`):**
    *   **Acción:** Añadir las siguientes propiedades, asegurando que los secretos se gestionen a través de variables de entorno.
    ```properties
    metabase.site.url=http://localhost:3000
    metabase.secret.key=${METABASE_SECRET_KEY}
    # Lista de dashboards permitidos y sus roles requeridos
    metabase.embedding.dashboards.5.roles=ROLE_ADMIN,ROLE_MANAGER
    metabase.embedding.dashboards.6.roles=ROLE_USER
    ```
3.  **Crear Excepciones Personalizadas:**
    *   **Acción:** Crear `DashboardNotFoundException` y `DashboardAccessDeniedException` para integrarse con el `GlobalExceptionHandler`.
4.  **Crear `MetabaseEmbeddingService`:**
    *   **Acción:** Implementar el servicio que contiene la lógica de firma de JWT. Debe incluir:
        *   Un método para verificar si un dashboardId es válido y está en la configuración.
        *   Un método para verificar si el rol del usuario actual está en la lista de roles permitidos para ese dashboard.
        *   El método principal `getSignedDashboardUrl` que realiza estas validaciones antes de firmar.
5.  **Actualizar `DashboardController`:**
    *   **Acción:** Implementar el endpoint `GET /api/dashboards/{dashboardId}`.
        *   Debe inyectar `MetabaseEmbeddingService`.
        *   Debe llamar a los métodos de validación. Si fallan, lanzar las excepciones personalizadas correspondientes.
        *   Si tienen éxito, devolver la URL firmada.

### Fase 3: Frontend (React)

1.  **Crear Componente `DashboardEmbed`:**
    *   **Acción:** Crear un componente que acepte `dashboardId` como prop.
2.  **Implementar Manejo de Estados:**
    *   **Acción:** Utilizar un hook de estado para gestionar `loading`, `error` y `iframeUrl`.
        *   **Estado `loading`:** Mostrar un componente de esqueleto o un spinner mientras se espera la respuesta del backend.
        *   **Estado `error`:** Si la llamada a la API falla (ej. 403, 404), mostrar un componente de alerta con un mensaje amigable (ej. "No tienes permiso para ver este reporte" o "El reporte no pudo ser cargado").
        *   **Estado `success`:** Renderizar el `<iframe>` con la URL recibida.
3.  **Lógica de Fetching:**
    *   **Acción:** Usar `useEffect` para llamar al endpoint del backend, asegurando que se envíe el token de autenticación del usuario.

## 5. Consideraciones de Seguridad (OWASP Top 10)

*   **A01: Broken Access Control:**
    *   **Mitigación:** **Implementada.** La validación de roles en el `MetabaseEmbeddingService` antes de generar la URL asegura que un usuario no pueda acceder a dashboards no autorizados. El uso de un usuario de BD de solo lectura previene el acceso no autorizado a nivel de datos.
*   **A02: Cryptographic Failures:**
    *   **Mitigación:** **Implementada.** El `METABASE_SECRET_KEY` se gestionará como una variable de entorno y nunca se expondrá al frontend ni se commiteará en el repositorio.
*   **A07: Server-Side Request Forgery (SSRF):**
    *   **Mitigación:** La URL de Metabase (`metabase.site.url`) se define en la configuración del backend y no es controlable por el usuario, previniendo que la aplicación sea forzada a generar peticiones a servidores internos no deseados.

## 6. Estrategia de Pruebas

*   **Backend:**
    *   **Test Unitario:** Para `MetabaseEmbeddingService`, verificar que la lógica de autorización (roles) funciona correctamente y que los JWTs se firman como se espera.
    *   **Test de Integración:** Para `DashboardController`, simular peticiones con `MockMvc`:
        *   Petición con un rol válido -> `200 OK`.
        *   Petición con un rol no válido -> `403 Forbidden`.
        *   Petición para un `dashboardId` inexistente -> `404 Not Found`.
*   **Frontend:**
    *   **Test de Componente:** Verificar que `DashboardEmbed` renderiza correctamente los estados de `loading` y `error`, y que el `iframe` se muestra con la URL correcta en caso de éxito.
*   **Test E2E (Manual):**
    *   Verificar el flujo completo con dos usuarios de roles diferentes, asegurando que cada uno solo pueda ver los dashboards que le corresponden.

## 7. Riesgos y Mitigaciones

*   **Riesgo:** El `METABASE_SECRET_KEY` se filtra.
    *   **Mitigación:** Rotar la clave inmediatamente en Metabase y actualizar la variable de entorno. Implementar auditoría sobre el acceso a los secretos.
*   **Riesgo:** Metabase está caído.
    *   **Mitigación:** El `iframe` no cargará. El frontend debería tener un `timeout` o manejar el evento `onerror` del `iframe` para mostrar un mensaje de error amigable.
