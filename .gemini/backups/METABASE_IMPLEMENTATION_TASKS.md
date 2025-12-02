# Tareas Técnicas de Implementación: Integración de Metabase (IOC-006a)

Este documento resume el conjunto de tareas técnicas que se completaron para implementar la historia de usuario `IOC-006a`, "Como Gerente, quiero visualizar un dashboard con sus gráficos y KPIs". Estas tareas son adicionales a las que estaban originalmente planificadas en el Kanban y representan el trabajo necesario para construir una integración robusta, segura y observable con la plataforma de Business Intelligence Metabase.

---

## Resumen de la Implementación

La solución se basó en el método de **Static (Signed) Embedding** de Metabase, donde el backend de Spring Boot actúa como un intermediario seguro que genera tokens JWT de corta duración. Estos tokens permiten al frontend de React renderizar dashboards dentro de un `iframe` de forma segura, con filtros aplicados a nivel de usuario.

---

## Tareas Técnicas Completadas

### Backend (BE)

| ID (Sugerido) | Historia | Asignado | Descripción de la Tarea | Estado |
| :--- | :--- | :--- | :--- | :--- |
| `BE-TASK-17` | `IOC-006a` | Jaime | **Configurar `MetabaseProperties.java`:** Crear la clase de configuración para cargar los ajustes de Metabase (URL, secret key, dashboards) desde `application.properties`. | ✅ Done |
| `BE-TASK-18` | `IOC-006a` | Jaime | **Implementar `MetabaseEmbeddingService`:** Crear el servicio principal para la lógica de negocio, incluyendo la validación de autorización por roles y la generación de tokens JWT firmados para Metabase. | ✅ Done |
| `BE-TASK-19` | `IOC-006a` | Jaime | **Crear `DashboardController`:** Exponer el endpoint seguro `GET /api/v1/dashboards/{dashboardId}` para que el frontend pueda solicitar las URLs firmadas. | ✅ Done |
| `BE-TASK-20` | `IOC-006a` | Jaime | **Implementar `DashboardAuditService`:** Crear un servicio para registrar todos los intentos de acceso a dashboards (exitosos y fallidos) en los logs. | ✅ Done |
| `BE-TASK-21` | `IOC-006a` | Jaime | **Ajustar `SecurityConfig` para CSP:** Modificar la Política de Seguridad de Contenido (`Content-Security-Policy`) para permitir que los `iframes` de Metabase se rendericen correctamente. | ✅ Done |
| `BE-TASK-22` | `IOC-006a` | Jaime | **Añadir `JwtAuthenticationConverter`:** Configurar Spring Security para convertir el JWT de Supabase en el objeto `CustomUserDetails`, permitiendo el filtrado por atributos de usuario. | ✅ Done |
| `BE-TASK-23` | `IOC-006a` | Jaime | **Implementar Resiliencia (Circuit Breaker):** Añadir y configurar `Resilience4j` en `MetabaseEmbeddingService` para manejar caídas del servidor de Metabase. | ✅ Done |
| `BE-TASK-24` | `IOC-006a` | Jaime | **Implementar Caché de Tokens:** Añadir y configurar `Caffeine` para cachear los tokens de Metabase y mejorar el rendimiento. | ✅ Done |

### Infraestructura y Despliegue (INFRA)

| ID (Sugerido) | Historia | Asignado | Descripción de la Tarea | Estado |
| :--- | :--- | :--- | :--- | :--- |
| `INFRA-01` | `IOC-006a` | Jaime | **Configurar `docker-compose.yml` para Metabase:** Crear y configurar el archivo para levantar Metabase y su base de datos PostgreSQL en un entorno local. | ✅ Done |
| `INFRA-02` | `IOC-006a` | Jaime | **Crear `Dockerfile` para el Backend:** Escribir el `Dockerfile` optimizado para empaquetar la aplicación Spring Boot para su despliegue. | ✅ Done |
| `INFRA-03` | `IOC-006a` | Jaime | **Crear `render.yaml` para Despliegue:** Configurar el archivo de "Infrastructure as Code" para desplegar el backend en Render, incluyendo la gestión de secretos. | ✅ Done |
| `INFRA-04` | `IOC-006a` | Jaime | **Crear script `pg_cron` para limpieza de conexiones:** Desarrollar y programar una función SQL para terminar automáticamente las conexiones `idle` de Metabase en Supabase. | ✅ Done |

---
