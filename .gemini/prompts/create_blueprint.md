# Prompt: Crear Nuevo Blueprint de Implementación

### Guía Rápida para el Humano
1.  **Edita la sección `## Template de Ejecución`** más abajo con los detalles de la tarea que acabas de completar.
2.  **Copia el contenido completo de este archivo** (`Ctrl+A`, `Ctrl+C`).
3.  **Pega el contenido en Gemini CLI** y presiona `Enter`.

---

## Template de Ejecución (EDITAR ESTA SECCIÓN)

ID_DE_LA_HISTORIA: "IOC-021"
DESCRIPCION_DE_LA_FUNCIONALIDAD: "Flujo de Autenticación con Supabase"
TAREA_ESPECIFICA_EN_KEBAB_CASE: "supabase-auth-flow"

---

## Instrucciones para la IA (NO EDITAR ESTA SECCIÓN)

Tu rol es el de un Senior Solutions Architect y Asistente de Documentación Técnica.

Utilizando las variables definidas en la sección `## Template de Ejecución`, realiza las siguientes acciones:

1.  **Analiza el Contexto:** Revisa nuestra conversación reciente, el plan, el código generado y las pruebas realizadas para la implementación de la historia `{{ID_DE_LA_HISTORIA}}`. Esta es tu fuente de verdad.

2.  **Define el Nombre del Archivo:** Construye el nombre del archivo para el nuevo blueprint usando el formato `[ID_Historia]-[tarea-especifica].md`, basado en las variables `{{ID_DE_LA_HISTORIA}}` y `{{TAREA_ESPECIFICA_EN_KEBAB_CASE}}`.

3.  **Genera el Contenido del Blueprint:** Utilizando el contexto analizado, genera el contenido completo del blueprint siguiendo estrictamente la siguiente plantilla de Markdown.

    ```markdown
    # Blueprint: {{DESCRIPCION_DE_LA_FUNCIONALIDAD}}

    Esta guía detalla el proceso paso a paso para implementar la funcionalidad.

    ## 1. Objetivo y Alcance
    *   **Objetivo:** <!-- Describir qué se logra con este blueprint. -->
    *   **Stack Tecnológico Involucrado:** <!-- Listar tecnologías clave. -->

    ## 2. Prerrequisitos
    *   **Dependencias:** <!-- Listar dependencias de npm o pom.xml. -->
    *   **Configuración:** <!-- Mencionar variables de entorno o configuración. -->

    ## 3. Implementación Paso a Paso

    ### 3.1 Backend (Spring Boot)
    *   **Paso 1: <!-- Nombre del Paso -->**
        *   **Archivo:** <!-- Ruta al archivo. -->
        *   **Código:**
            ```java
            // Insertar aquí el código Java relevante.
            ```

    ### 3.2 Frontend (React)
    *   **Paso 1: <!-- Nombre del Paso -->**
        *   **Archivo:** <!-- Ruta al archivo. -->
        *   **Código:**
            ```tsx
            // Insertar aquí el código TSX relevante.
            ```

    ## 4. Flujo de Verificación
    *   <!-- Describir los pasos para la verificación manual. -->

    ## 5. Consideraciones de Seguridad y Buenas Prácticas
    *   <!-- Añadir notas importantes. -->
    ```

4.  **Crea el Archivo del Blueprint:**
    *   Crea el nuevo archivo en la ruta `@.gemini/blueprints/[nombre-del-archivo]` y escribe el contenido generado en él.
    *   Confirma la creación exitosa del archivo.