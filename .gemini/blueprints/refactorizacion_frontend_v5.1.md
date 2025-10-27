# Blueprint: Refactorización de Frontend para Sincronización con API de Ingesta (v5.1 - Final Corregido)

Este documento detalla el plan de acción para refactorizar la vista de Ingesta de Datos. **Versión 5.1 reintegra la estrategia de pruebas completa y añade las mejoras de QA avanzado.**

---
## 1. Plan de Implementación

### Archivo: `package.json`
*   **Acción:** Añadir `axios`, `vitest`, `msw`, `@testing-library/react`.

### Archivo: `src/types/api.ts` (Nuevo)
*   **Acción:** Crear el archivo con las interfaces `JobStatus`, `StartProcessResponse`, `EtlJobStatusDto`, `StandardError`, y `EtlJob`.

### Archivo: `src/utils/apiError.ts` (Nuevo)
*   **Acción:** Crear el helper `parseApiError` para un manejo de errores consistente.

### Archivo: `src/services/apiService.ts` (Nuevo)
*   **Acción:** Implementar el servicio de API con `axios`, incluyendo el sistema de "Token Provider", interceptores, soporte para `AbortSignal` y helper para `Retry-After`.

### Archivo: `src/main.tsx`
*   **Acción:** Inyectar el proveedor de tokens de Supabase al inicio de la aplicación usando `setTokenProvider`.

### Archivo: `src/pages/admin/DataIngestionPage.tsx`
*   **Acción:** Implementar la lógica de `AbortController` para permitir la cancelación de subidas y el `useEffect` robusto para el sondeo.

---
## 2. Estrategia de Pruebas y Calidad (Completa)

### A. Pruebas Unitarias y de Integración (Vitest + MSW)
*   **`src/services/tests/apiService.spec.ts`:**
    *   **Cobertura:**
        *   Verificar que el interceptor de petición añade correctamente el `Authorization header`.
        *   Simular una respuesta `401` y verificar que el interceptor de respuesta llama a la función de `signOut`.
        *   Probar la función `startEtlProcess`, simulando una respuesta `202` y verificando que los datos devueltos son correctos.
        *   Probar `parseApiError` con diferentes payloads de error (`409`, `413`, etc.) y verificar que la salida es la esperada.
        *   **[Nuevo]** Probar que el `AbortSignal` se pasa correctamente a `axios` en `startEtlProcess`.
*   **`src/pages/admin/tests/DataIngestionPage.spec.tsx`:**
    *   **Cobertura:**
        *   Renderizar el componente y simular una subida de archivo exitosa usando MSW.
        *   Verificar que el estado de carga (`isUploading`) se activa y desactiva correctamente.
        *   Verificar que, tras una respuesta exitosa, el nuevo job aparece en la tabla.
        *   Simular una respuesta de sondeo que cambia el estado de 'INICIADO' a 'EXITO' y verificar que la UI se actualiza.
        *   **[Nuevo]** Probar el clic en un botón "Cancelar" y verificar que la petición de subida se cancela.

### B. Pruebas End-to-End (E2E) y Manuales
*   **Happy Path:**
    1.  Login.
    2.  Navegar a la página de ingesta.
    3.  Subir un archivo CSV válido.
    4.  **Verificar:** Se muestra el progreso, luego un toast de éxito, y la tabla se actualiza con el estado "INICIADO". Tras unos segundos, el estado cambia a "EXITO".
*   **Casos de Error:**
    1.  **Token Vencido:** Simular un token vencido y realizar una subida. **Verificar:** La aplicación redirige al login.
    2.  **Archivo Duplicado (409):** Subir el mismo archivo dos veces. **Verificar:** Se muestra un toast de error "Este archivo ya ha sido procesado."
    3.  **Archivo Grande (413):** Intentar subir un archivo que exceda el límite del frontend. **Verificar:** El `Dropzone` muestra un mensaje de error y la petición no se envía.

### C. Checklist de QA Manual
*   **[ ]** **Flujo Visual:** El estado de la UI (idle, uploading, success, error) es claro y sigue el sistema de diseño.
*   **[ ]** **Controles Deshabilitados:** Todos los botones y controles interactivos en la página se deshabilitan correctamente durante la subida.
*   **[ ]** **Limpieza del Polling:** Navegar fuera de la página de ingesta mientras un job está en progreso y verificar (en la consola de red) que las peticiones de sondeo se detienen.
*   **[ ]** **Cancelación de Subida:** Iniciar la subida de un archivo grande y hacer clic en "Cancelar". Verificar que la petición de red se detiene y se muestra un mensaje de cancelación.
*   **[ ]** **Accesibilidad:** Se puede navegar por todo el flujo de subida usando solo el teclado. Los mensajes de error son leídos por lectores de pantalla.
*   **[ ]** **Formato de Fechas:** Las fechas en la tabla de historial se muestran en un formato localizado y legible.

### D. Pruebas de Aceptación de Usuario (UAT)
*   **[ ]** Un usuario no técnico puede completar el flujo de subida de un archivo válido sin necesidad de instrucciones.
*   **[ ]** Los mensajes de error (archivo inválido, duplicado, etc.) son claros y comprensibles para un usuario no técnico.
