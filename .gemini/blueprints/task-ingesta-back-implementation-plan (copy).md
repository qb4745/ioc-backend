# **Blueprint de Implementación: TASK-INGESTA-BACK (v6 - Production Grade)**

#### **1. Resumen y Objetivo**
Este documento detalla las tareas y el plan de pruebas para construir el **Sistema de Ingesta y Sincronización de Datos**. El objetivo es producir un sistema **gobernado, idempotente por archivo, transaccional, asincrónico y observable**, listo para un entorno de producción.

#### **2. Plan de Implementación y Pruebas por Fases**

##### **Fase 1: Configuración de la Infraestructura Core y Gobernanza**

*   **[ ] `TASK-INGESTA-BACK-001`:** Definir el Modelo de Datos de Gobernanza.
    *   **Acción:** Crear las tablas en Supabase: `etl_jobs` y `quarantined_records` según el DDL propuesto. Crear las entidades JPA correspondientes (`EtlJob`, `QuarantinedRecord`) y sus repositorios.
    *   **Verificación:** Las entidades se mapean correctamente y los repositorios pueden realizar operaciones CRUD básicas.

*   **[ ] `TASK-INGESTA-BACK-002`:** Configurar el Pool de Hilos Asíncrono Explícito.
    *   **Acción:** Crear la clase `AsyncConfig` con un `Bean` que defina el `ThreadPoolTaskExecutor` nombrado `etlExecutor`.
    *   **Verificación:** La aplicación arranca y las métricas de Actuator (si están habilitadas) muestran el nuevo pool de hilos.

*   **[ ] `TASK-INGESTA-BACK-003`:** Habilitar WebSockets.
    *   **Acción:** Añadir dependencia `websocket`, y clase `WebSocketConfig`.
    *   **Verificación:** (Sin cambios) Ver caso `E2E-WSC-01`.

##### **Fase 2: Implementación de la Lógica de Gobernanza y Sincronización**

*   **[ ] `TASK-INGESTA-BACK-004`:** Implementar el Servicio de Gobernanza (`EtlJobService`).
    *   **Acción:** Crear un nuevo `EtlJobService`. Implementar métodos para:
        1.  `createJob(file, userId)`: Calcula el hash del archivo, crea una entrada en `etl_jobs` con estado `INICIADO` y devuelve el `jobId`.
        2.  `updateJobStatus(jobId, newStatus, details)`: Actualiza el estado de un job.
        3.  `findJobById(jobId)`: Recupera los detalles de un job.
        4.  `isDuplicate(fileHash)`: Verifica si ya existe un job completado o en progreso con el mismo hash.
    *   **Verificación:** Pruebas unitarias para cada método, verificando la correcta interacción con `EtlJobRepository`.

*   **[ ] `TASK-INGESTA-BACK-005`:** Extender el Repositorio con Borrado por Rango.
    *   **Acción:** Añadir `deleteAllByFechaContabilizacionBetween(...)` a `ProductionDataRepository`.
    *   **Verificación:** Ver caso `IT-JPA-01`.

*   **[ ] `TASK-INGESTA-BACK-006`:** Implementar el `DataSyncService` Transaccional.
    *   **Acción:** Crear `DataSyncService` con el método `@Transactional syncData(...)` que ejecuta "borrar e insertar".
    *   **Verificación:** Ver casos `IT-DS-01` y `IT-DS-02`.

##### **Fase 3: Orquestación del Flujo Asincrónico Completo**

*   **[ ] `TASK-INGESTA-BACK-007`:** Implementar `EtlProcessingService` (Orquestador).
    *   **Acción:** Refactorizar el servicio para que:
        1.  Use el `ThreadPoolTaskExecutor` explícito: `@Async("etlExecutor")`.
        2.  Inyecte y utilice `EtlJobService`.
        3.  **Su lógica de `processFile` ahora recibe un `jobId`**.
        4.  Antes de la transformación, actualiza el estado del job a `TRANSFORMANDO`.
        5.  Durante la transformación, guarda los registros inválidos en la tabla de cuarentena.
        6.  Antes de la carga, actualiza el estado a `SINCRONIZANDO` y luego invoca a `dataSyncService`.
        7.  Al finalizar, actualiza el estado a `EXITO` o `FALLO`.
    *   **Verificación:** Ver casos `IT-ORC-01` y `IT-ORC-02`.

*   **[ ] `TASK-INGESTA-BACK-008`:** Refactorizar el `EtlController`.
    *   **Acción:** Modificar el endpoint `POST /api/etl/start-process` para que:
        1.  Primero, llame a `etlJobService.isDuplicate()` para rechazar archivos ya procesados (devolviendo `409 Conflict`).
        2.  Luego, llame a `etlJobService.createJob()` para registrar el nuevo job.
        3.  Invoque `etlProcessingService.processFile()` pasándole el `jobId` recién creado.
        4.  Devuelva `202 Accepted` con el `jobId` en el cuerpo de la respuesta.
    *   **Acción (Nuevo Endpoint):** Crear un nuevo endpoint `GET /api/etl/jobs/{jobId}/status` que llame a `etlJobService.findJobById()` y devuelva el estado del job.
    *   **Verificación:** Ver casos `IT-API-01`, `IT-API-02`, `IT-API-03`.

##### **Fase 4: Implementación de la Capa de Notificación**

*   **[ ] `TASK-INGESTA-BACK-009`:** Implementar `NotificationService`.
    *   **Acción:** (Sin cambios) Crear el servicio para enviar mensajes. **Modificación:** El tópico ahora puede incluir el `jobId` para mayor granularidad (ej. `/user/topic/etl-jobs/{jobId}`).
    *   **Verificación:** Ver casos `UT-NS-01`, `UT-NS-02`.

---

#### **3. Estrategia de Pruebas por Capas (Refinada)**
Esta sección define **CÓMO** se validará la calidad.

##### **3.1. Pruebas Unitarias**
*   **`UT-NS-01/02` (NotificationService):** (Sin cambios).
*   **`UT-JS-01` (EtlJobService):** Pruebas para cada método, verificando la lógica de creación, actualización y búsqueda de jobs.

##### **3.2. Pruebas de Integración**
*   **`IT-JPA-01` (Repositorio):** (Sin cambios).
*   **`IT-DS-01/02` (DataSyncService):** (Sin cambios).
*   **`IT-ORC-01/02` (EtlProcessingService):** (Sin cambios, pero las aserciones ahora también verifican las llamadas a `EtlJobService`).
*   **`IT-API-01` (Controller - Éxito):** Verifica que `POST /start-process` devuelve `202 Accepted` y un `jobId`.
*   **`IT-API-02` (Controller - No Autorizado):** (Sin cambios).
*   **`IT-API-03` (Controller - Archivo Duplicado):** Verifica que enviar el mismo archivo dos veces resulta en un `409 Conflict` en la segunda petición.
*   **`IT-API-04` (Controller - Consulta de Estado):** Verifica que `GET /jobs/{jobId}` devuelve el estado correcto del job.

##### **3.3. Pruebas End-to-End (E2E)**
*   **`E2E-WSC-01` (Conexión WebSocket):** (Sin cambios).
*   **`E2E-ETL-01` (Flujo Feliz):**
    1.  **Acción:** Enviar un archivo válido. Capturar el `jobId` de la respuesta `202`.
    2.  **Resultado:** Observar las notificaciones. Usar el `jobId` para consultar el endpoint `GET /jobs/{jobId}/status` y ver cómo cambia el estado hasta llegar a `EXITO`. Verificar los datos en la BBDD.
*   **`E2E-ETL-02` (Flujo de Rechazo por Duplicado):**
    1.  **Acción:** Enviar el mismo archivo una segunda vez.
    2.  **Resultado:** La respuesta HTTP es `409 Conflict`. No se inicia un nuevo job ni se reciben notificaciones.

---
