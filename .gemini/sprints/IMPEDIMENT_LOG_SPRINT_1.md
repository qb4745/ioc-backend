# Registro de Impedimentos del Sprint 1 (v1.2)

Este documento es un artefacto vivo utilizado para rastrear, gestionar y aprender de todos los obstáculos que afectan el progreso del Sprint 1.

---

## Proceso de Gestión de Impedimentos
1.  **Identificación:** Cualquier miembro del equipo añade un nuevo impedimento a la lista.
2.  **Análisis:** En la Daily Scrum, se define el plan de acción, el responsable, la causa raíz y la acción preventiva.
3.  **Seguimiento:** El estado se actualiza diariamente hasta su resolución.

---

## Registro de Impedimentos

| ID | Impedimento | Responsable | Plan de Acción | Causa Raíz | Acción Preventiva | Estado |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **IMP-001** | **Flujo de usuario incompleto (Error 404):** La página `/update-password` no existía. | **Jaime** | Implementar la vista y ruta faltantes según el blueprint. | **Planificación Incompleta:** El blueprint inicial no contempló la página de destino del enlace de reseteo. | **Mejora de Blueprints:** Actualizar todos los futuros blueprints para incluir explícitamente todas las vistas y rutas implicadas en un flujo. | ✅ **Resuelto** |
| **IMP-002** | **Deuda técnica por tipado laxo:** El uso de `any` y la falta de tipos específicos generaron advertencias de calidad. | **Jaime** | Refactorizar los componentes afectados para usar tipos estrictos e interfaces. | **Desarrollo Acelerado:** Se priorizó la funcionalidad sobre la calidad de tipos en la implementación inicial. | **Definición de Hecho (DoD):** Añadir un criterio al DoD que exija "cero advertencias de `any` explícito" en el código nuevo. | ✅ **Resuelto** |
| **IMP-03** | **UX Inconsistente en notificaciones:** El uso de `alert()` nativo rompía con el sistema de diseño. | **Jaime** | Instalar `react-hot-toast` y refactorizar los formularios para usar notificaciones emergentes. | **Falta de un Sistema de Notificaciones:** El proyecto no contaba con una solución estandarizada para notificaciones. | **Decisión de Arquitectura:** Adoptar `react-hot-toast` como la librería oficial para todas las notificaciones emergentes del proyecto. | ✅ **Resuelto** |
| **IMP-004** | **Cierre de sesión inseguro:** El logout desde el menú de usuario no invalidaba la sesión correctamente. | **Jaime** | Implementar la lógica `handleSignOut` en `UserDropdown.tsx` usando `window.location.href` para forzar la recarga. | **Lógica Duplicada e Incompleta:** La funcionalidad de logout estaba implementada de forma incorrecta en un componente y correcta en otro. | **Centralizar Lógica Crítica:** Refactorizar la lógica de autenticación a hooks (`useAuth`) para evitar implementaciones inconsistentes. | ✅ **Resuelto** |
| **IMP-005** | **UI Rota en Tabla de Historial:** La tabla de ingesta de datos se mostraba desalineada. | **Boris** | Refactorizar la tabla para usar patrones de la plantilla (padding en celdas y contenedor con overflow) en lugar de anchos fijos. | **Desviación del Sistema de Diseño:** Se aplicó una solución CSS (`table-fixed`) que no era consistente con el resto de la aplicación. | **Revisión de Patrones Existentes:** Antes de crear un componente complejo, verificar si la plantilla ya ofrece un patrón similar para reutilizar. | ✅ **Resuelto** |
| **IMP-006** | **Feedback de UI incompleto:** Faltaban indicadores visuales para la validación de archivos y el estado de carga. | **Boris** | 1. Crear un `Spinner` reutilizable. 2. Modificar el `Dropzone` para mostrar el `Spinner` y mensajes de error contextuales. | **Omisión en la Implementación:** Los estados se manejaron lógicamente (deshabilitando botones) pero no visualmente. | **Mejora del DoD:** Añadir al DoD: "Todos los procesos asíncronos deben tener un indicador de carga visual. Todos los formularios deben mostrar errores de validación contextuales." | ✅ **Resuelto** |
| | | | | | | |
