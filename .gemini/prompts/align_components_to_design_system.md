# Prompt: Alinear Componentes con el Sistema de Diseño (v2)

## 1. CONFIGURACIÓN (PARA EL HUMANO)
*   **Propósito:** Este prompt instruye a la IA para auditar y alinear sistemáticamente una lista de componentes con la guía de estilo `design_tokens.md`.
*   **Acción Requerida:** Lista los archivos a refactorizar en la variable `ARCHIVOS_A_REFACTORIZAR`.
*   **Modo de Uso:** Edita la variable, copia todo el contenido y pégalo en Gemini CLI. La IA te guiará a través del proceso optimizado.

---

## 2. TEMPLATE DE EJECUCIÓN (EDITAR ESTA SECCIÓN)

ARCHIVOS_A_REFACTORIZAR: """
- @src/pages/admin/AdminDashboardPage.tsx
- @src/pages/AuthPages/ResetPassword.tsx
- @src/pages/AuthPages/UpdatePassword.tsx
- @src/pages/Account.tsx
"""

---

## 3. MANDATO OPERATIVO (PARA LA IA)

Tu rol es el de un **Senior Frontend Architect** ejecutando una auditoría de consistencia de UI. Tu misión es refactorizar los componentes en `{{ARCHIVOS_A_REFACTORIZAR}}` para que se conformen al 100% con nuestra guía de estilo canónica.

**Fuentes de Verdad (Sources of Truth):**
1.  **La Guía de Estilo (El Objetivo):** `@.gemini/strategy/4_Design_Tokens.md`
2.  **Los Componentes (El Estado Actual):** La lista de archivos en `{{ARCHIVOS_A_REFACTORIZAR}}`.

**PROTOCOLO DE REFACTORIZACIÓN ITERATIVO Y OPTIMIZADO (MANDATORIO):**
Procesa **un archivo a la vez**. Para cada archivo, ejecuta el siguiente ciclo de dos fases.

---

**Inicio del Proceso:**
*   Anuncia el número total de archivos a procesar y cuál es el primer archivo con el que vas a trabajar.

**CICLO POR ARCHIVO:**

### Fase 1: Auditoría y Propuesta de Refactorización
*   **Objetivo:** Analizar las desviaciones del componente actual y proponer un plan de acción concreto, justificado y listo para aprobar.
*   **Acciones:**
    1.  Lee el contenido del archivo React actual.
    2.  Compáralo meticulosamente con las reglas en `design_tokens.md`.
    3.  Genera y presenta un **"Reporte de Auditoría"** que contenga dos partes:
        *   **A. Propuesta de Cambios (Formato `diff`):** Muestra las líneas a eliminar (`-`) y las a añadir (`+`). Concéntrate en JSX y `className`.
        *   **B. Justificación de Cambios:** Para cada cambio significativo en el `diff`, proporciona una justificación clara que lo vincule a una regla específica de la guía de estilo.

    *   **Ejemplo de Justificación:**
        *   "`- <h2 className="text-2xl">...`"
        *   "`+ <h2 className="text-xl font-semibold text-gray-800 dark:text-white/90">...`"
        *   "**Justificación:** Se alinea con el token `Tipografía > Título de Página (h2)` de `design_tokens.md`."

*   **Punto de Control:** Presenta el reporte completo (diff + justificación) y **espera la aprobación explícita** ("Aprobado, aplica los cambios") antes de proceder.

### Fase 2: Ejecución del Refactor
*   **Objetivo:** Aplicar los cambios aprobados al archivo de forma segura.
*   **Acciones:**
    1.  Modifica el archivo para aplicar el `diff` exacto que fue aprobado en la Fase 1.
    2.  Asegúrate de gestionar las importaciones necesarias para cualquier nuevo componente de la librería que se haya introducido.

*   **Punto de Control:** Muestra el código completo y final del archivo refactorizado. Luego, anuncia cuál es el siguiente archivo en la lista y espera la confirmación ("OK, procede con el siguiente") para iniciar el ciclo de nuevo con él.

---
**Finalización:**

### Fase Final: Reporte de Cierre
*   Al finalizar el último archivo, presenta un resumen final que liste todos los archivos que fueron refactorizados exitosamente. Confirma que el proceso ha terminado.