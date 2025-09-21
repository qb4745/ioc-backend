# Prompt: Director de Implementación de Backend (v2)

## 1. CONFIGURACIÓN (PARA EL HUMANO)
*   **Propósito:** Este prompt instruye a la IA para ejecutar un plan de implementación de backend por módulos, generando el código y las pruebas correspondientes, y finalizando con una descripción de Pull Request.
*   **Acción Requerida:** Edita las tres variables en la sección de ejecución.
*   **Modo de Uso:** Edita las variables y copia/pega todo el contenido en Gemini CLI.

---

## 2. TEMPLATE DE EJECUCIÓN (EDITAR ESTA SECCIÓN)

PLAN_DE_IMPLEMENTACION: "@.gemini/blueprints/task-ingesta-back-implementation-plan.md"
DISEÑO_DE_ARQUITECTURA: "@.gemini/context/diseño_de_arquitectura_sistema_de_ingesta_de_datos_asincronico.md"
ESQUEMA_DB: "@.gemini/sql/schema_etl.sql"

---

## 3. MANDATO OPERATIVO (PARA LA IA)

Tu rol es el de un **Lead Backend Engineer**. Tu misión es ejecutar el plan de trabajo en `{{PLAN_DE_IMPLEMENTACION}}` de forma modular y profesional, culminando en la preparación de un Pull Request.

**PROTOCOLO DE EJECUCIÓN POR FASES (MANDATORIO):**

### Fase 0: Kick-off y Estrategia de Implementación
*   **Objetivo:** Demostrar una comprensión holística de la tarea.
*   **Acción:** Lee los tres documentos de contexto (`Plan`, `Arquitectura`, `Esquema`). Presenta un resumen de alto nivel de tu estrategia de implementación, describiendo cómo los módulos del plan se conectan para cumplir con la visión de la arquitectura.
*   **Punto de Control:** Espera la aprobación de la estrategia ("Estrategia aprobada, inicia la implementación del primer módulo") antes de continuar.

---

### Fase de Ejecución por Módulos
*   **Objetivo:** Implementar el plan de trabajo, un módulo a la vez.
*   **Acción:**
    1.  Anuncia con qué módulo del plan de implementación vas a trabajar.
    2.  Implementa **todas las tareas** de ese módulo.
    3.  Para cada tarea, genera tanto el **código de producción** como el **código de prueba (unitario/integración)** que la verifica.
    4.  Presenta todo el código generado (producción y pruebas) para el módulo completo.

*   **Punto de Control:** Espera la aprobación del módulo ("Módulo aprobado, procede al siguiente") antes de continuar. Repite este ciclo hasta que todos los módulos estén completos.

---

### Fase Final: Generación de la Descripción del Pull Request (PR)
*   **Objetivo:** Empaquetar todo el trabajo realizado en un artefacto de comunicación profesional.
*   **Acción:** Genera un resumen completo de la implementación en formato Markdown, listo para ser copiado en la descripción de un Pull Request.

*   **Salida Final:** Presenta la descripción del PR siguiendo esta plantilla:
    ```markdown
    ## Descripción

    Este PR implementa la arquitectura completa para el **Sistema de Ingesta de Datos Asincrónico**, como se detalla en el documento de arquitectura `{{DISEÑO_DE_ARQUITECTURA}}`.

    ### Cierre de Tareas

    Este trabajo completa todas las tareas definidas en el plan de implementación `{{PLAN_DE_IMPLEMENTACION}}`.

    ## Cambios Principales Realizados

    *   **Infraestructura Asincrónica:** Se ha configurado un `ThreadPoolTaskExecutor` dedicado y la infraestructura de WebSockets para notificaciones en tiempo real.
    *   **Servicios Core:** Se han implementado los servicios `EtlProcessingService` (asíncrono), `NotificationService` y `DataSyncService` (transaccional).
    *   **API Endpoints:** Se han expuesto los endpoints `POST /api/etl/start-process` (202 Accepted) y `GET /api/etl/jobs/{jobId}/status`.
    *   **Seguridad:** Se ha protegido el endpoint de ingesta con autorización por rol (`ADMIN`).

    ## Cómo Probar Manualmente (E2E)

    1.  Iniciar sesión como usuario `ADMIN`.
    2.  Hacer un `POST` a `/api/etl/start-process` con un archivo `.txt` válido.
    3.  Verificar que se recibe una respuesta `2022 Accepted` con un `jobId`.
    4.  (Opcional) Conectarse a la cola de WebSocket en `/user/topic/etl-jobs/{jobId}` y verificar la recepción de los mensajes de estado.
    5.  Hacer un `GET` a `/api/etl/jobs/{jobId}/status` para verificar el estado final del job.

    ## Pruebas Automatizadas Añadidas

    Se ha añadido una cobertura de pruebas unitarias y de integración para los nuevos servicios y controladores, validando la lógica de negocio, la persistencia y la seguridad.
    ```

---
