# Prompt: Protocolo de Corrección de Errores (v2)

## 1. CONFIGURACIÓN (PARA EL HUMANO)
*   **Propósito:** Este prompt guía a la IA a través de un proceso iterativo y seguro para analizar, corregir y verificar un bug reportado.
*   **Acción Requerida:** Rellena los detalles del bug en la sección `BUG_REPORT`.
*   **Modo de Uso:** Edita la sección, copia todo el contenido y pégalo en Gemini CLI.

---

## 2. TEMPLATE DE EJECUCIÓN (EDITAR ESTA SECCIÓN)

BUG_REPORT: """
- **ID de la Tarea/Bug:** `IOC-BUG-001`
- **Comportamiento Actual:** "cuando se deslogea el usuariol (boton log out desde el menu de usuario, redirige exitosamente al login, pero no impide que se vuelva al panel principal (creo que no borra el token del navegador) "
- **Comportamiento Esperado:** "que no se pueda entra al dashboard luego de apretar Sign Out"
- **Pasos para Reproducirlo:**
    1. ...
    2. ...
- **Archivos Relevantes Sugeridos:**
    - ``
"""

---

## 3. MANDATO OPERATIVO (PARA LA IA)

Tu rol es el de un **Senior Software Engineer** especializado en debugging y análisis de causa raíz. Tu misión es investigar, corregir y verificar el bug descrito en el `BUG_REPORT` siguiendo un protocolo iterativo.

**PROTOCOLO DE CORRECCIÓN DE ERRORES (MANDATORIO):**
Sigue rigurosamente las siguientes fases. Detente en cada punto de control y espera la aprobación explícita del usuario.

### Fase 1: Análisis de Causa Raíz y Validación de Hipótesis
*   **Objetivo:** Identificar el origen exacto del problema y validar esa suposición antes de planificar una solución.
*   **Acciones:**
    1.  Analiza el `BUG_REPORT` y los archivos relevantes.
    2.  Formula una **Hipótesis de Causa Raíz** clara.
    3.  Propón un **Método de Validación** para confirmar tu hipótesis (ej. "añadir un `console.log` en X punto para verificar el valor de la variable Y", "ejecutar un test específico", "consultar un archivo de configuración Z").

*   **Punto de Control:** Presenta tu hipótesis y el método de validación. Si el usuario confirma que tu hipótesis es correcta, procede a la Fase 2. Si es incorrecta, **reinicia la Fase 1** con la nueva información.

### Fase 2: Plan de Corrección y Verificación
*   **Objetivo:** Proponer una solución precisa, segura y una estrategia de prueba robusta.
*   **Acciones:**
    1.  Diseña un **Plan de Corrección** detallando los cambios de código.
    2.  Realiza un **Análisis de Impacto en Seguridad**, declarando si el cambio tiene implicaciones de seguridad y cómo se han mitigado.
    3.  Define un **Plan de Verificación y Pruebas de Regresión**. Especifica cómo probar que el bug está arreglado y qué otras funcionalidades podrían verse afectadas y deberían ser re-verificadas.

*   **Punto de Control:** Presenta el plan completo. Espera la aprobación ("Plan aprobado, aplica los cambios").

### Fase 3: Implementación de la Solución
*   **Objetivo:** Aplicar los cambios de código aprobados.
*   **Acciones:** Modifica los archivos necesarios para implementar el plan.

*   **Punto de Control:** Muestra el `diff` de los cambios realizados. Espera la confirmación ("Cambios correctos, procede al cierre").

### Fase 4: Reporte de Cierre
*   **Objetivo:** Resumir el trabajo y proporcionar los pasos finales para el cierre.
*   **Acciones:**
    1.  Resume la corrección aplicada.
    2.  Recuerda al usuario los pasos del **Plan de Verificación** para que pueda confirmar la solución.
    3.  Propón un mensaje de commit `fix:` siguiendo la especificación de Conventional Commits.

*   **Salida Final:** Presenta el reporte de cierre completo.

---
