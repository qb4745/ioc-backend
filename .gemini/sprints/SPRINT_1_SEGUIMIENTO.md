# Seguimiento del Sprint 1: Flujo de Datos y Seguridad

Este documento proporciona una vista en tiempo real del progreso del Sprint 1, mostrando el estado de las historias de usuario y el avance en términos de Story Points (SP).

*   **Fecha de Inicio (Estimada):** Lunes, 15 de Septiembre de 2025
*   **Duración:** 2 semanas (10 días hábiles)
*   **Total de Story Points Comprometidos:** 21 SP

---

## Resumen de Progreso

*   **Story Points Completados:** 8 SP
*   **Story Points Restantes:** 13 SP
*   **Progreso del Sprint:** 38% completado

---

## 1. Tablero de Estado de Historias

Esta tabla muestra el estado actual de cada historia comprometida en el sprint.

| ID      | Título                                       | SP | Estado        | Notas / Próximos Pasos                                                                                             |
| :------ | :------------------------------------------- | :- | :------------ | :----------------------------------------------------------------------------------------------------------------- |
| `IOC-021` | Como Usuario, quiero iniciar sesión          | 5  | ✅ **Terminada**  | Flujo completo de registro, login y reseteo de contraseña implementado.                                            |
| `IOC-022` | Como Usuario, quiero cerrar mi sesión        | 3  | ✅ **Terminada**  | Funcionalidad de logout implementada y verificada como parte de la historia `IOC-021`.                             |
| `IOC-023` | Construir el Layout y Rutas Protegidas       | 8  | 🟡 **En Progreso** | Base de rutas protegidas implementada. **Próximo paso:** Restaurar todas las rutas del dashboard dentro del layout. |
| `IOC-001` | Cargar y validar un archivo CSV              | 5  | ❌ **Por Hacer**  | Pendiente de iniciar.                                                                                              |

---

## 2. Tabla de Burndown

Esta tabla simula el gráfico de burndown, registrando el trabajo completado (SP "quemados") al final de cada día.

| Día del Sprint | Fecha       | Tareas Completadas del Día                               | SP Quemados | SP Restantes |
| :------------- | :---------- | :------------------------------------------------------- | :---------- | :----------- |
| **Día 0**      | 15/09/2025  | Inicio del Sprint                                        | 0           | 21           |
| **Día 1**      | 16/09/2025  | -                                                        | 0           | 21           |
| **Día 2**      | 17/09/2025  | Implementación base de `IOC-021` y `IOC-022`             | 8           | 13           |
| **Día 3**      | 18/09/2025  | _(Actualizar al final del día)_                          |             |              |
| **...**        | ...         | ...                                                      | ...         | ...          |
| **Día 10**     | 26/09/2025  | **Objetivo Final**                                       | **21**      | **0**        |

---

## 3. Foco Inmediato y Próximos Pasos

Basado en el estado actual, el foco inmediato para avanzar en el sprint es:

1.  **Completar la Historia `IOC-023`:** La tarea prioritaria es **restaurar todas las rutas del dashboard** (`/profile`, `/calendar`, etc.) dentro del `ProtectedRoute` en el archivo `App.tsx`. Esto hará que la aplicación sea completamente funcional para los usuarios que han iniciado sesión.
