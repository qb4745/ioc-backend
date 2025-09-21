# Prompt: Generar Plan de Implementación de Backend (v2)

## 1. CONFIGURACIÓN (PARA EL HUMANO)
*   **Propósito:** Este prompt instruye a la IA para analizar una arquitectura y generar un plan de implementación detallado, con tareas numeradas, dependencias y propietarios sugeridos.
*   **Acción Requerida:** Proporciona la ruta al documento de arquitectura y un prefijo para los IDs de las tareas.
*   **Modo de Uso:** Edita las variables, copia todo el contenido y pégalo en Gemini CLI.

---

## 2. TEMPLATE DE EJECUCIÓN (EDITAR ESTA SECCIÓN)

RUTA_DOCUMENTO_ARQUITECTURA: "@.gemini/blueprints/diseño_de_arquitectura_sistema_de_ingesta_de_datos_asincronico.md"
PREFIJO_TAREAS: "TASK-INGESTA-BACK"

---

## 3. MANDATO OPERATIVO (PARA LA IA)

Tu rol es el de un **Senior Backend Tech Lead**. Tu misión es tomar el documento de arquitectura en `{{RUTA_DOCUMENTO_ARQUITECTURA}}` y descomponerlo en un **Blueprint de Implementación** detallado, secuencial y contextualizado para nuestro equipo.

### Contexto de Operación
*   **Guía Estratégica:** Antes de empezar, lee los archivos: 
*   `@.gemini/strategy/1_Definicion_Proyecto.md`
*   `@.gemini/strategy/2_Product_Backlog.md`
*   `@.gemini/strategy/3_Stack_Tecnologico.md`
*   `@.gemini/strategy/4_Design_Tokens.md`*    para entender los roles y responsabilidades del equipo, Definicion del Proyecto, El Product Backlog, etc.

**PROTOCOLO DE DESCOMPOSICIÓN Y GENERACIÓN (MANDATORIO):**

### Fase 1: Análisis de Dependencias y Asignación
*   **Acción:** Lee el documento de arquitectura. Identifica los componentes clave y sus interdependencias para establecer un orden de construcción lógico. Basándote en los roles del `GEMINI.md`, asigna un propietario sugerido a cada tarea.

### Fase 2: Generación del Blueprint de Implementación
*   **Acción:** Genera un nuevo documento Markdown (`[prefijo-en-kebab-case]-implementation-plan.md`) en la ruta `@.gemini/blueprints/` con el contenido generado, siguiendo estrictamente la siguiente plantilla:

---
### **PLANTILLA DE SALIDA OBLIGATORIA**

# Blueprint de Implementación: {{PREFIJO_TAREAS}}

## 1. Resumen y Objetivo
Este documento detalla las tareas técnicas para implementar la arquitectura de **[Nombre de la Arquitectura]**.

## 2. Plan de Implementación por Fases

### Fase 1: Configuración de la Infraestructura Core

*   **[ ] `{{PREFIJO_TAREAS}}-001`:** Habilitar Soporte Asincrónico y WebSockets
    *   **Propietario Sugerido:** Jaime Vicencio (Backend)
    *   **Dependencias:** Ninguna.
    *   **Acción:** Añadir `@EnableAsync`, la dependencia de WebSocket y las clases de configuración inicial (`AsyncConfig`, `WebSocketConfig`).
    *   **Consideraciones de Seguridad:** La configuración de WebSockets debe prepararse para la autenticación por usuario.
    *   **Verificación:**
        *   **Caso de Éxito:** La aplicación arranca y expone el endpoint `/ws`.
        *   **Caso de Falla:** El build falla si la dependencia no se añade correctamente.

### Fase 2: Desarrollo de Servicios de Soporte

*   **[ ] `{{PREFIJO_TAREAS}}-002`:** Implementar `NotificationService`
    *   **Propietario Sugerido:** Jaime Vicencio (Backend)
    *   **Dependencias:** `{{PREFIJO_TAREAS}}-001`.
    *   **Acción:** Crear el servicio que inyecta `SimpMessagingTemplate` para enviar notificaciones de estado.
    *   **Consideraciones de Seguridad:** El método debe usar `convertAndSendToUser` para asegurar el aislamiento de mensajes.
    *   **Verificación:**
        *   **Caso de Éxito:** Un test unitario con Mockito verifica que el método `convertAndSendToUser` es invocado correctamente.
        *   **Caso de Falla:** Un test verifica que el método maneja un `userId` nulo sin lanzar una `NullPointerException`.

### Fase 3: Implementación del Flujo Principal

*   **[ ] `{{PREFIJO_TAREAS}}-003`:** Implementar el Flujo Asincrónico en `EtlProcessingService`
    *   **Propietario Sugerido:** Jaime Vicencio (Backend)
    *   **Dependencias:** `{{PREFIJO_TAREAS}}-002`.
    *   **Acción:** Crear el servicio con el método `@Async` que orquesta la transformación, carga y las llamadas de notificación.
    *   **Consideraciones de Seguridad:** Validar el contenido del archivo para prevenir ataques de inyección o XXE si se usa XML.
    *   **Verificación:**
        *   **Caso de Éxito:** Una prueba de integración sube un archivo válido y se reciben todas las notificaciones de estado en orden.
        *   **Caso de Falla:** Una prueba sube un archivo malformado y se recibe la notificación de `FALLO`.

*(Continúa este patrón para el resto de las tareas)*

---
