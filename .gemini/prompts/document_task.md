# Prompt: Documentar Tarea Completada

Tu rol ahora es el de un Asistente de Documentación Técnica.

Nuestra tarea es documentar la implementación de la historia de usuario **{{ID_DE_LA_HISTORIA}}** que acabamos de completar, añadiendo una nueva entrada al archivo de bitácora `@.gemini/sprints/BITACORA_SPRINT_1.md`.

**Instrucciones:**

1.  **Analiza el Contexto:** Revisa nuestra conversación reciente, el plan, el código generado y las pruebas realizadas para la historia **{{ID_DE_LA_HISTORIA}}**.

2.  **Genera el Contenido:** Utilizando la información analizada, completa la siguiente plantilla de Markdown. No incluyas los comentarios de la plantilla en la salida final.

    ```markdown
    ## {{ID_DE_LA_HISTORIA}}: {{TITULO_DE_LA_HISTORIA}}

    *   **Objetivo de Negocio (El "Para Qué"):** <!-- Rellenar con el valor de negocio de la historia -->
    *   **Criterios de Aceptación Clave:**
        *   <!-- Listar aquí los criterios de aceptación principales -->

    ### Desglose Técnico de la Implementación

    *   **Plan Aprobado:** <!-- Resumir el plan de alto nivel que se acordó -->

    *   **Archivos Creados/Modificados:**
        *   <!-- Listar archivos clave modificados -->

    *   **Resumen de la Implementación:**
        <!-- Explicar de forma concisa pero técnica qué se hizo -->

    ### Verificación y Pruebas Realizadas

    *   **Pruebas Automatizadas:**
        *   **(Backend) Pruebas Unitarias:** <!-- Describir las pruebas unitarias creadas -->
    *   **Pruebas Manuales (End-to-End):**
        *   **Caso de Prueba 1 (Happy Path):** <!-- Describir el flujo de prueba exitoso -->
        *   **Caso de Prueba 2 (Caso de Error):** <!-- Describir un flujo de prueba de error -->

    *   **Commit de Referencia (Squashed):**
        *   `[hash-del-commit-post-merge] - tipo(ámbito): descripción corta ({{ID_DE_LA_HISTORIA}}) (#PR)`
    ---
    ```

3.  **Actualiza el Archivo de Bitácora:**
    *   Lee el contenido del archivo `@.gemini/sprints/BITACORA_SPRINT_1.md`.
    *   Busca el marcador: `<!-- NUEVAS ENTRADAS DE HISTORIAS SE AÑADEN AQUÍ -->`.
    *   Inserta el bloque de Markdown generado **justo encima** de ese marcador.
    *   Escribe el contenido modificado de vuelta al archivo.```


