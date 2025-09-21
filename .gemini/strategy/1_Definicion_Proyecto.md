# GEMINI.MD: Guía de Colaboración de IA para el Proyecto IOC (v2)

Este documento es nuestra memoria central y guía estratégica para la colaboración con la IA en el proyecto "Inteligencia Operacional Cambiaso (IOC)". Seguir estas directrices asegurará que la IA comprenda el contexto, los objetivos y nuestras formas de trabajo para que actúe como un desarrollador senior más del equipo.

## 1. Resumen y Propósito del Proyecto (El "Por Qué")

*   **Objetivo Principal:** Construir una plataforma de software de Business Intelligence (BI) para automatizar la ingesta y centralización de datos de producción, proporcionando a los stakeholders de Cambiaso una herramienta visual e interactiva que optimice la agilidad y fiabilidad de su toma de decisiones operativas.
*   **Problema a Resolver:** Eliminar la dependencia crítica de procesos manuales basados en planillas de cálculo, que generan fragmentación, inconsistencia de datos y una alta latencia en la información, dificultando una gestión ágil y basada en evidencia.
*   **Dominio de Negocio:** Inteligencia de Negocios (Business Intelligence) y Análisis de Datos aplicados a la optimización de operaciones en el sector de producción industrial de consumo masivo.

## 2. Tecnologías Principales y Stack (El "Con Qué")

*   **Backend:**
    *   **Lenguaje:** Java 21
    *   **Framework:** Spring Boot 3.5.5
    *   **Dependencias Clave:** Spring Boot Starters (Data JPA, Security, Validation, Web), Lombok.
    *   **Gestor de Paquetes:** Maven
*   **Frontend:**
    *   **Lenguaje:** TypeScript
    *   **Framework:** React 19
    *   **Dependencias Clave:** Tailwind CSS, ApexCharts, React Router, FullCalendar, Flatpickr.
    *   **Gestor de Paquetes:** npm
*   **Base de Datos:** PostgreSQL, gestionado a través de la plataforma Supabase.
*   **Autenticación:** Supabase Auth, con gestión de tokens JWT entre frontend y backend.
*   **Visualización de Datos (BI):** Metabase (Open Source), ejecutado en Docker y embebido en el frontend.

## 3. Patrones de Arquitectura y Modelo de Datos (El "Cómo")

*   **Arquitectura General:** Arquitectura desacoplada de aplicación web moderna, compuesta por una **API RESTful monolítica** (Backend en Spring Boot) y una **Single Page Application (SPA)** (Frontend en React).
*   **Fuente de Verdad de los Datos:** El **Diagrama Entidad-Relación (DER)** provisto es el modelo canónico. Todas las entidades JPA, DTOs y consultas deben ser estrictamente consistentes con este modelo.
*   **Estructura de Directorios y Ficheros Clave:**
    *   **Backend (Spring Boot):** Estructura estándar de Maven.
        *   **Código Fuente:** `src/main/java/com/cambiaso/ioc` (con subpaquetes `controller`, `service`, `repository`, `entity`, `security`, etc.).
        *   **Punto de Entrada:** `IocbackendApplication.java`.
        *   **Configuración Principal:** `src/main/resources/application.properties`.
        *   **Dependencias:** `pom.xml`.
    *   **Frontend (React):** Estructura modular basada en componentes.
        *   **Código Fuente:** `src` (con subcarpetas `components`, `pages`, `layout`, `context`, `hooks`).
        *   **Punto de Entrada:** `src/main.tsx`.
        *   **Configuraciones Principales:** `vite.config.ts`, `tailwind.config.js`, `tsconfig.json`.

## 4. Flujo de Trabajo y Metodología (El "Cuándo")

*   **Metodología:** **Scrum**. El trabajo está organizado en Sprints con objetivos claros y entregas de valor incrementales.
*   **Gestión de Tareas:** El `IOC-product-backlog.xlsx` es el backlog maestro. Las tareas para la iteración actual se detallan en el `Sprint-1-Backlog.md`.
*   **Actores Clave y Roles:**
    *   **Product Owner (P.O.):** Boris Arriagada.
    *   **Scrum Master y Desarrollador Backend Principal:** Jaime Vicencio.
    *   **Desarrollador Frontend Principal:** Boris Arriagada.

## 5. Instrucciones Específicas para la Colaboración de IA

### 5.1. Foco Actual del Proyecto: Sprint 1

*   **Prioridad Absoluta:** Nuestra atención está centrada **únicamente** en las cuatro historias de usuario comprometidas en el **Sprint 1**: `IOC-001`, `IOC-021`, `IOC-022`, y `IOC-023`. Ignora cualquier otra historia del backlog por ahora.
*   **Documento de Referencia Obligatorio:** Todas las tareas, criterios de aceptación y el desglose técnico detallado se encuentran en el archivo `@Sprint-1-Backlog.md`. Debes leerlo antes de proponer cualquier implementación.
*   **Objetivo del Sprint 1:** Entregar el esqueleto funcional del sistema: autenticación, layout/rutas protegidas y el flujo completo de carga de archivos CSV.

### 5.2. Protocolo de Trabajo Sugerido

*   **1. Comprender (Perceive):** Cuando se te pida implementar una historia de usuario, lee su descripción y criterios de aceptación en `@Sprint-1-Backlog.md`.
*   **2. Planificar (Reason):** Antes de escribir código, presenta un plan de acción conciso.
*   **3. Implementar (Act):** Una vez aprobado el plan, genera el código adhiriéndote a las convenciones y tecnologías definidas.
*   **4. Verificar (Refine):** Sugiere cómo probar la implementación. Para el backend, esto incluye la creación de tests unitarios con JUnit 5, ejecutables con `mvn test`.

### 5.3. Mandatos Clave

*   **Mensajes de Commit:** **Crítico:** Los mensajes de commit deben estar **en español** y ser cortos y descriptivos, siguiendo el estilo observado en el repositorio (ej. "supabase configurado").
*   **Seguridad Primero:** La implementación de la autenticación (`IOC-021`, `IOC-022`) debe seguir las mejores prácticas de Spring Security 6+ y la gestión segura de JWT.
*   **Uso de Lombok:** En el backend, utiliza las anotaciones de Lombok (`@Data`, `@Getter`, `@Setter`, `@Builder`, etc.) para reducir el código boilerplate en entidades y DTOs.
*   **Consistencia del Código:** El código debe ser legible y seguir las convenciones estándar de cada ecosistema (Java y TypeScript/React).
