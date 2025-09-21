# Prompt: Generar Brief de Sincronización para el Backend (v3)

## 1. CONFIGURACIÓN (PARA EL HUMANO)
*   **Propósito:** Este prompt instruye a la IA para que analice un directorio completo de Fichas Técnicas, sintetice los requisitos de la API, y genere un brief de sincronización validado para el equipo de backend.
*   **Acción Requerida:** Ninguna. Este prompt está diseñado para auditar automáticamente el directorio de blueprints.
*   **Modo de Uso:** Copia y pega el contenido completo en Gemini CLI.

---

## 2. MANDATO OPERATIVO (PARA LA IA)

Tu rol es el de un **Lead Software Architect** especializado en la definición de contratos de API. Tu misión es generar un **"Backend Sync Brief"** validado, que traduzca los requisitos de la UI en especificaciones de API claras, consistentes y accionables.

### Contexto de Operación (Tu Mapa Mental)
Para esta tarea, debes tener en cuenta la siguiente arquitectura de información:
*   **Repositorio de Blueprints (Tu Fuente Primaria):** El directorio `@.gemini/blueprints` contiene todas las Fichas Técnicas de Vista (FTVs) que debes analizar. Todas las referencias a FTVs deben apuntar aquí.
*   **Contexto del Sprint (Tu Foco Táctico):** El directorio `@.gemini/sprints` contiene el backlog (`Sprint-1-Backlog.md`) y la bitácora del sprint actual. Puedes usar estos archivos para entender el **propósito de negocio** de un endpoint si la FTV no es suficientemente clara.

**PROTOCOLO DE SÍNTESIS Y GENERACIÓN (MANDATORIO):**

### Fase 1: Análisis y Síntesis Inteligente
*   **Acción:**
    1.  Realiza un escaneo completo del directorio `@.gemini/blueprints` para identificar todos los archivos de Ficha Técnica de Vista (`.md`).
    2.  Extrae todos los requisitos de API de cada FTV.
    3.  **Agrupa los requisitos por endpoint único (Método + Ruta)**.
    4.  Para cada endpoint, **fusiona los contratos de datos**. Si encuentras contratos conflictivos para el mismo endpoint, **detén el proceso y reporta la inconsistencia** para que sea resuelta por un humano.

### Fase 2: Generación del Brief Validado
*   **Acción:** Una vez que todos los contratos han sido fusionados y validados, genera el documento de especificaciones.

**Tarea de Salida:**
Crea un nuevo archivo llamado `backend_sync_brief.md` en la ruta `@.gemini/sprints/`. El contenido debe seguir estrictamente la siguiente plantilla:

---
### **PLANTILLA DE SALIDA OBLIGATORIA**

# Backend Sync Brief (v[YYYY-MM-DD])

## 1. Metadatos del Documento
*   **Generado el:** [Insertar fecha y hora actual]
*   **Fuentes Analizadas:**
    *   [Listar los nombres de archivo del directorio @.gemini/blueprints que fueron procesados]

## 2. Resumen Ejecutivo
Este documento especifica los requisitos de la API consolidados desde la perspectiva del Frontend. Sirve como un contrato unificado para la implementación de los endpoints necesarios.

## 3. Política de Seguridad Global
*   **Mecanismo:** El Frontend enviará un **JWT** en la cabecera `Authorization: Bearer <token>` en todas las peticiones a los endpoints listados a continuación. El Backend debe proteger todos estos endpoints.

---

## 4. API Endpoints Requeridos

### Endpoint: [Método y Ruta, ej. GET /api/admin/dashboard/summary]
*   **Propósito:** <!-- Describe de forma consolidada para qué necesita el frontend estos datos. Puedes consultar @.gemini/sprints para el contexto de negocio. -->
*   **FTVs de Origen:** <!-- Cita TODOS los archivos FTV del directorio @.gemini/blueprints que requieren este endpoint. -->
*   **Contrato de Petición (Request Body/Params):**
    ```typescript
    // N/A
    ```
*   **Contrato de Respuesta (Response Body - Consolidado):**
    ```typescript
    // Muestra la interfaz de respuesta final y fusionada.
    interface DashboardSummary {
      // ...
    }
    ```

### Endpoint: [Siguiente método y ruta...]
*   <!-- Repetir la estructura para cada endpoint único. -->

---

## 5. Resumen y Próximos Pasos
*   El equipo de Frontend utilizará estos contratos para desarrollar contra datos `mock`.
*   Se solicita al equipo de Backend que implemente estos endpoints y notifique cuando estén listos para la integración.