# Plan de Tareas para la Refactorización de Ingesta de Datos (Sprint 1)

Este documento desglosa el blueprint `refactorizacion_frontend_v3.md` en tareas accionables para ser gestionadas en un tablero Kanban (GitHub Projects).

---

### **Épica: Refactorización de la Ingesta de Datos (IOC-001 Integración)**

---

#### **Fase 1: Configuración y Estructura (SETUP)**

-   [ ] **RF-S01:** **Configurar Dependencias:** Instalar `axios` para peticiones HTTP y `vitest`, `msw`, `@testing-library/react` para el entorno de pruebas.
-   [ ] **RF-S02:** **Definir Contratos de API:** Crear el archivo `src/types/api.ts` y añadir las interfaces TypeScript para todos los DTOs de éxito (`StartProcessResponse`, `EtlJobStatusDto`) y de error (`StandardError`).
-   [ ] **RF-S03:** **Crear Helper de Errores:** Crear el archivo `src/utils/apiError.ts` con la función `parseApiError` para estandarizar el manejo de errores de la API en toda la aplicación.

---

#### **Fase 2: Servicio de API (API)**

-   [ ] **RF-A01:** **Inicializar Cliente HTTP:** Crear el archivo `src/services/apiService.ts` y configurar la instancia de `axios` con la `baseURL` y los interceptores.
-   [ ] **RF-A02:** **Implementar Interceptor de Petición:** Añadir la lógica al interceptor de `axios` para obtener el token JWT del `AuthProvider` e inyectarlo en la cabecera `Authorization` de cada petición.
-   [ ] **RF-A03:** **Implementar Interceptor de Respuesta:** Añadir la lógica al interceptor de `axios` para detectar respuestas `401` o `403` y ejecutar una función de `signOut` global.
-   [ ] **RF-A04:** **Implementar Subida de Archivo:** Crear la función `startEtlProcess` en `apiService.ts` que envíe el archivo como `multipart/form-data` y gestione el progreso de la subida.
-   [ ] **RF-A05:** **Implementar Consulta de Estado:** Crear la función `getJobStatus` en `apiService.ts` que acepte un `jobId` y consulte el estado del trabajo.

---

#### **Fase 3: Refactorización de Componentes de UI (UI)**

-   [ ] **RF-UI01:** **Refactorizar Página de Ingesta:** Modificar `DataIngestionPage.tsx` para eliminar la lógica simulada y usar el nuevo `apiService`. Gestionar el estado de los trabajos (`jobs`) y el progreso de la subida.
-   [ ] **RF-UI02:** **Implementar Sondeo (Polling):** Añadir la lógica de `useEffect` y `useRef` en `DataIngestionPage.tsx` para crear un único intervalo que consulte periódicamente el estado de los trabajos "INICIADO".
-   [ ] **RF-UI03:** **Mejorar Dropzone:** Actualizar `DataUploadDropzone.tsx` para añadir validación de tamaño de archivo en el frontend y mostrar la barra de progreso que se le pasa por props.
-   [ ] **RF-UI04:** **Actualizar Tabla de Historial:** Refactorizar `UploadHistoryTable.tsx` para que acepte la lista de `jobs` (en lugar de `uploads`), muestre el `status` real del backend y un `Spinner` si el trabajo está "INICIADO".

---

#### **Fase 4: Pruebas Automatizadas (TEST)**

-   [ ] **RF-T01:** **Configurar Entorno de Pruebas:** Configurar `vitest` y `msw` en el proyecto para poder ejecutar pruebas unitarias y de integración.
-   [ ] **RF-T02:** **Probar Servicio de API:** Crear `apiService.spec.ts` y escribir pruebas unitarias para los interceptores y las funciones `startEtlProcess` y `getJobStatus`, usando `msw` para mockear la API.
-   [ ] **RF-T03:** **Probar Integración de UI:** Crear `DataIngestionPage.spec.tsx` y escribir pruebas de integración que simulen el flujo completo: subir un archivo, verificar el estado de carga y la actualización de la tabla tras el sondeo.

---

#### **Fase 5: Pruebas Manuales y Cierre (QA)**

-   [ ] **RF-Q01:** **Ejecutar Checklist de QA:** Realizar todas las pruebas manuales definidas en el blueprint v3.0, cubriendo el happy path, los casos de error y la accesibilidad.
-   [ ] **RF-Q02:** **Realizar UAT:** Conducir una prueba de aceptación con un usuario no técnico para validar la usabilidad y claridad de la interfaz y los mensajes.
