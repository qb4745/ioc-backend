# Resumen Técnico del Proyecto: Inteligencia Operacional Cambiaso (IOC)

> **Generado por**: Módulo de IA (Full-Stack)
> **Fecha**: 2025-10-10T19:30:00Z
> **Versión**: 1.1-INTEGRATED
> **Estado**: ✅ Completado

---

## 1. Contexto del Proyecto

### 1.1. Propósito

**¿Qué es este proyecto?**
Es una plataforma de Business Intelligence (BI) diseñada para automatizar la ingesta, procesamiento y visualización de datos de producción de la empresa Cambiaso.

**Problema que Resuelve**:
Elimina la dependencia de procesos manuales en planillas de cálculo, centralizando los datos para permitir una toma de decisiones más ágil y basada en evidencia.

**Valor para el Usuario**:
Proporciona a los administradores y gerentes dashboards interactivos y en tiempo real sobre la producción, eficiencia y otros KPIs operativos.

### 1.2. Objetivos Clave

1. **Ingesta de Datos Asincrónica**: Procesar archivos de producción de gran tamaño sin bloquear la interfaz de usuario.
2. **Visualización Segura de Dashboards**: Integrar Metabase para mostrar dashboards, con control de acceso por roles.
3. **Gobernanza y Monitoreo**: Proveer herramientas para auditar las cargas de datos y monitorear la salud del sistema.

### 1.3. Audiencia/Usuarios

- **Usuarios Primarios**: Administradores (para la carga de datos) y Gerentes/Analistas (para la visualización de dashboards).
- **Roles Detectados**: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_USER` (inferido de `MetabaseProperties`).

### 1.4. Estado Actual

**Backend**:

- ✅ Autenticación y autorización implementada.
- ✅ 3 endpoints REST implementados.
- ✅ Integración con Supabase (Auth y DB) y Metabase.
- ✅ Pipeline ETL asincrónico con monitoreo de jobs.
- ✅ Watchdog para jobs atascados.
- ✅ Sistema de caché y circuit breaker.

**Frontend**:

- ✅ Flujo de autenticación de usuario completo (Login, Logout, Signup, Reset Password).
- ✅ UI para la página de "Ingesta de Datos" implementada (pendiente de conexión a API).
- ✅ Integración de dashboards de Metabase 100% funcional, incluyendo auto-refresco de token, manejo de errores y resiliencia.
- ✅ Layout principal de la aplicación con rutas protegidas y navegación por menú lateral.

---

## 2. Arquitectura del Sistema

### 2.1. Arquitectura de Alto Nivel

```
┌─────────────────────────────────────────────────────────────┐
│                       FRONTEND                              │
│  React 19 + TypeScript + Vite + Tailwind CSS                │
│  Single Page Application (SPA)                              │
│  Deployed on: Vercel (inferido de CORS)                     │
│                                                              │
└─────────────────┬───────────────────────────────────────────┘
                  │ HTTPS/REST + JWT
                  │
┌─────────────────▼───────────────────────────────────────────┐
│                       BACKEND (ioc-backend)                 │
│  Spring Boot 3.5.5 + Java 21                                │
│  Build: Maven                                               │
│  Deployed on: Render                                │
│                                                              │
│  Endpoints: /api/v1/**                                      │
│  Security: JWT Validation (Supabase) + Spring Security     │
└─────────────────┬───────────────────────────────────────────┘
                  │
      ┌───────────┼───────────┬──────────────┐
      ▼           ▼           ▼              ▼
┌──────────┐ ┌─────────┐ ┌──────────┐ ┌──────────┐
│PostgreSQL│ │ Supabase│ │ Metabase │ │   AWS    │
│ (Supabase)   │ │  (Auth) │ │(Analytics)│ │ (EC2)    │
└──────────┘ └─────────┘ └──────────┘ └──────────┘
```

### 2.2. Decisiones Arquitectónicas Clave

| Decisión          | Tecnología Elegida           | Razón                                             |
| ----------------- | ---------------------------- | ------------------------------------------------- |
| Backend Framework | Spring Boot 3.5.5            | Robustez empresarial, ecosistema maduro.          |
| Lenguaje (Backend)| Java 21                      | Rendimiento y tipado fuerte.                      |
| Base de Datos     | PostgreSQL                   | Estándar de la industria para datos relacionales. |
| ORM               | Spring Data JPA / Hibernate  | Abstracción y productividad.                      |
| Autenticación     | Supabase GoTrue (JWT)        | Solución gestionada para autenticación.           |
| Seguridad (Backend)| Spring Security 6            | Estándar para seguridad en aplicaciones Spring.   |
| **Frontend Framework** | React 19 + Vite         | Ecosistema moderno, alto rendimiento en desarrollo. |
| **Lenguaje (Frontend)**| TypeScript               | Seguridad de tipos y escalabilidad.               |
| **UI Framework**  | Tailwind CSS                 | Utilidades CSS para un desarrollo rápido y consistente. |

---

## 3. Stack Tecnológico Detallado

### 3.1. Frontend ✅

#### Lenguaje y Framework Core

| Componente     | Tecnología   | Versión |
| -------------- | ------------ | ------- |
| **Framework**  | React        | 19      |
| **Lenguaje**   | TypeScript   | 5.x     |
| **Build Tool** | Vite         | 5.x     |

#### Dependencias Principales (Extraídas de package.json)

| Categoría         | Paquete                      | Propósito                               |
| ----------------- | ---------------------------- | --------------------------------------- |
| **Cliente API**   | supabase-js                  | Autenticación con Supabase              |
| **HTTP**          | axios, fetch                 | Peticiones a la API del backend         |
| **Routing**       | react-router-dom             | Navegación y rutas protegidas           |
| **UI**            | tailwindcss                  | Estilos y sistema de diseño             |
| **Notificaciones**| react-hot-toast              | Feedback al usuario                     |
| **Testing**       | vitest, @testing-library/react | Framework de pruebas (planificado)      |

#### Estructura de Carpetas

```
src/
├── components/      # Componentes reutilizables (UI, lógica)
├── pages/           # Componentes de página (vistas)
├── hooks/           # Hooks personalizados (ej. useAuth)
├── services/        # Lógica de comunicación externa (logging, API)
├── utils/           # Funciones de utilidad (validación, fetch)
├── types/           # Interfaces y tipos de TypeScript
├── layout/          # Componentes de estructura (Sidebar, Header)
└── lib/             # Configuración de clientes (Supabase)
```

---

### 3.2. Backend ✅

#### Lenguaje y Framework Core

| Componente     | Tecnología  | Versión |
| -------------- | ----------- | ------- |
| **Lenguaje**   | Java        | 21      |
| **Framework**  | Spring Boot | 3.5.5   |
| **Build Tool** | Maven       | 3.9.11  |

#### Dependencias Principales (Extraídas de pom.xml)

| Categoría         | Artifact                                   | Propósito                      |
| ----------------- | ------------------------------------------ | ------------------------------ |
| **Web**           | spring-boot-starter-web                    | REST API                       |
| **Seguridad**     | spring-boot-starter-security               | Autenticación/Autorización     |
| **OAuth2**        | spring-boot-starter-oauth2-resource-server | Validación de JWT              |
| **Base de Datos** | spring-boot-starter-data-jpa               | ORM                            |
| **PostgreSQL**    | postgresql                                 | Driver JDBC                    |
| **WebSocket**     | spring-boot-starter-websocket              | Notificaciones en tiempo real  |
| **Monitoreo**     | spring-boot-starter-actuator               | Health checks y métricas       |
| **Resilience**    | resilience4j-spring-boot3                  | Rate Limiting, Circuit Breaker |
| **JWT**           | jjwt-api, jjwt-impl                        | Generación de JWT (Metabase)   |
| **Lombok**        | lombok                                     | Reducción de boilerplate       |
| **Testing**       | spring-boot-starter-test                   | Tests unitarios/integración    |

---

### 3.3. Base de Datos

- **DBMS**: PostgreSQL
- **Hosting**: Supabase (inferido de `spring.datasource.url`)
- **ORM**: Hibernate

---

### 3.4. Servicios de Infraestructura

| Servicio          | Proveedor         | Propósito                        | Configuración                                          |
| ----------------- | ----------------- | -------------------------------- | ------------------------------------------------------ |
| **Autenticación** | Supabase GoTrue   | Gestión de usuarios, emisión JWT | `spring.security.oauth2.resourceserver.jwt.issuer-uri` |
| **Base de Datos** | Supabase (Pooler) | Persistencia                     | `spring.datasource.url`                                |
| **Analytics**     | Metabase          | Dashboards de BI                 | `metabase.site-url`, `metabase.secret-key`             |

---

## 4. API Endpoints

**Base URL (Backend)**: `/api`

#### Resumen por Módulo

**Dashboards** (`/api/v1/dashboards`)
| Método | Endpoint | Propósito | Controller | Roles | Estado |
|--------|----------|-----------|------------|-------|--------|
| GET | `/{dashboardId}` | Obtener URL firmada de Metabase | DashboardController | `isAuthenticated()` | ✅ |

**ETL** (`/api/etl`)
| Método | Endpoint | Propósito | Controller | Roles | Estado |
|--------|----------|-----------|------------|-------|--------|
| POST | `/start-process` | Iniciar proceso de ingesta | EtlController | `isAuthenticated()` | ✅ |
| GET | `/jobs/{jobId}/status` | Consultar estado de un job | EtlController | `isAuthenticated()` | ✅ |

**Total Endpoints Implementados**: 3

---

## 5. Seguridad

### 5.1. Autenticación ✅

- **Mecanismo**: JWT (Bearer Token)
- **Proveedor**: Supabase GoTrue
- **Flujo**: El frontend obtiene el token de Supabase y lo envía en la cabecera `Authorization` al backend en cada petición.

### 5.2. Autorización ✅

- **Backend**: Basado en anotaciones (`@PreAuthorize`). Actualmente solo valida que el usuario esté autenticado.
- **Frontend**: Lógica de UI para mostrar/ocultar elementos basada en roles (actualmente simulados).

### 5.3. Configuración de Seguridad

- ✅ **CORS (Backend)**: Habilitado para los orígenes del frontend (`localhost:5173`, Vercel).
- ✅ **CSRF (Backend)**: Deshabilitado (apropiado para una API REST stateless con JWT).
- ✅ **CSP (Backend)**: Se implementa una Política de Seguridad de Contenido que restringe los orígenes de scripts, estilos e iframes.
- ✅ **Sandbox (Frontend)**: El `iframe` de Metabase utiliza un `sandbox` para aislar el contenido, aunque `allow-same-origin` es necesario para su funcionamiento.

---

## 6. Configuración de Entorno

### 6.1. Variables de Entorno (Frontend) ✅

- `VITE_SUPABASE_URL`
- `VITE_SUPABASE_ANON_KEY`
- `VITE_API_BASE_URL`
- `VITE_DASHBOARD_GERENCIAL_ID`
- `VITE_DASHBOARD_OPERACIONAL_ID`

### 6.2. Variables de Entorno (Backend) ✅

- `SUPABASE_DB_PASSWORD`
- `SUPABASE_JWT_ISSUER_URI`
- `METABASE_URL`
- `METABASE_SECRET_KEY`

---

## 7. Deployment

### 7.1. Frontend ✅

- **Build Command**: `npm run build`
- **Hosting**: Vercel (inferido de la configuración de CORS del backend).

### 7.2. Backend ✅

- **Build Command**: `mvn clean package -DskipTests`
- **Hosting**: Render (inferido).

---

## 8. Testing

### 8.1. Frontend ❌

- **Framework**: Vitest, Testing Library (planificado).
- **Estado**: No implementado. Cobertura actual: 0%.

### 8.2. Backend ✅

- **Framework**: JUnit 5 + Mockito + Spring Boot Test + Testcontainers.
- **Tipos de Tests Detectados**: Unitarios e Integración.
- **Coverage**: JaCoCo configurado.

---

## 9. Monitoreo y Logging

### 9.1. Logging ✅

- **Backend**: SLF4J + Logback.
- **Frontend**: `loggingService` personalizado, preparado para integración con Sentry.

### 9.2. Monitoreo ✅

- **Backend**: `Spring Boot Actuator` habilitado (`health`, `metrics`, `prometheus`, `info`).

---

## 10. Próximos Pasos

1. **Implementar Tests en el Frontend**: Tarea crítica para garantizar la calidad y estabilidad de la UI.
2. **Conectar la UI de Ingesta de Datos**: Implementar las llamadas a la API ETL del backend.
3. **Implementar Roles Reales**: Refactorizar el frontend para consumir los roles desde el JWT una vez que el backend los provea.