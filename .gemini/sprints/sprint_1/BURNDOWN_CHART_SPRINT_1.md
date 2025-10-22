# Burndown Chart del Sprint 1 (v1.1)

Este documento visualiza el progreso del equipo y registra las decisiones clave tomadas durante el Sprint 1.

## Parámetros del Sprint
*   **Duración Total:** 13 días hábiles
*   **Story Points (SP) Comprometidos:** 21 SP
*   **Progreso Ideal por Día (SP quemados):** 21 SP / 13 días ≈ **1.62 SP/día**

---

## Tabla de Seguimiento (Burndown)

| Día | Fecha (Estimada) | Decisiones / Acciones Clave del Día | SP Quemados (Día) | SP Quemados (Acumulado) | SP Restantes (Real) | SP Restantes (Ideal) | Desviación |
| :-: | :--- | :--- | :---: | :---: | :---: | :---: | :---: |
| 0   | 15/09 | Inicio del Sprint | 0 | 0 | 21 | 21.00 | 0.00 |
| 1   | 16/09 | Implementación inicial de autenticación. | 0 | 0 | 21 | 19.38 | **+1.62** |
| 2   | 17/09 | Finalización de HU `IOC-021` y `IOC-022`. | 8 | 8 | 13 | 17.76 | **-4.76** |
| 3   | 18/09 | (Feriado) | 0 | 8 | 13 | 17.76 | **-4.76** |
| 4   | 19/09 | (Feriado) | 0 | 8 | 13 | 17.76 | **-4.76** |
| 5   | 22/09 | **Decisión:** Iniciar refactorización y corrección de deuda técnica identificada por Qodana. | 0 | 8 | 13 | 16.14 | **-3.14** |
| 6   | 23/09 | **Decisión:** Implementar notificaciones emergentes (`react-hot-toast`) para mejorar la UX. | 0 | 8 | 13 | 14.52 | **-1.52** |
| 7   | 24/09 | Implementación de la vista `AdminDashboard` (`01-FTV-panel-principal`). | 0 | 8 | 13 | 12.90 | **+0.10** |
| 8   | 25/09 | | | | | 11.28 | |
| 9   | 26/09 | | | | | 9.66 | |
| 10  | 29/09 | | | | | 8.04 | |
| 11  | 30/09 | | | | | 6.42 | |
| 12  | 01/10 | | | | | 4.80 | |
| 13  | 02/10 | | | | | **0.00** | |

---

### Análisis del Progreso (al Día 7)

*   **Estado Actual:** Estamos ligeramente por **delante** de la línea ideal por **0.10 Story Points**.
*   **Observaciones:** A pesar de dedicar tiempo a tareas de no-desarrollo (refactorización, documentación), el rápido avance inicial nos mantiene en una buena posición. La desviación positiva es mínima, lo que indica que estamos casi exactamente donde deberíamos estar.
*   **Plan de Acción:** El foco inmediato debe ser completar la HU `IOC-023` (8 SP). Terminar esta historia en los próximos días nos pondrá significativamente por delante de la curva, dándonos un colchón para la HU final `IOC-001`.

### Riesgos y Mitigaciones
*   **Riesgo Identificado:** La HU `IOC-023` (8 SP) es grande y podría tener complejidades ocultas.
*   **Plan de Mitigación:** Desglosar la implementación de `IOC-023` en tareas más pequeñas (ej. "Restaurar Rutas de Calendario", "Restaurar Rutas de Formularios") para quemar Story Points de forma incremental y tener una visión más clara del progreso real.
