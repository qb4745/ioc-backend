# **Diseño de Arquitectura: Sistema de Ingesta y Sincronización de Datos Gobernado**

**Proyecto:** Inteligencia Operacional Cambiaso (IOC)
**Fecha:** 14 de Septiembre de 2025
**Autor:** [Tu Nombre/Equipo]
**Versión:** 5.0 (Production Grade)

#### **1. Resumen Ejecutivo**

Este documento detalla la arquitectura para el sistema de Ingesta, Transformación y Carga (ETL). El sistema está diseñado para ser **gobernado, asincrónico, idempotente y transaccional**, proporcionando una plataforma robusta y auditable para la sincronización de datos de producción.

Para cumplir con los RNF de **Rendimiento, Usabilidad, Fiabilidad e Integridad de Datos**, la arquitectura desacopla la carga de la transformación, gestiona cada proceso como un "Job" rastreable, y utiliza un patrón de sincronización que previene la corrupción de datos.

#### **2. Requisitos y Desafíos Arquitectónicos**

*   **Gobernanza y Auditoría:** Rastrear cada ejecución del ETL, su estado y resultado.
*   **Procesamiento Asincrónico:** Transformar archivos pesados en segundo plano sin bloquear la API.
*   **Idempotencia Robusta:** Prevenir la re-ingesta accidental del mismo archivo y manejar consistentemente los datos superpuestos.
*   **Observabilidad:** Permitir que los clientes consulten el estado de un proceso de ingesta.

#### **3. Arquitectura Seleccionada: Servicio Asincrónico Gobernado**

Se ha seleccionado un modelo de **Servicio Asincrónico Integrado** con una capa de gobernanza explícita.

**Componentes Clave:**

1.  **Habilitadores de Spring Framework:** `@EnableAsync` (con un `ThreadPoolTaskExecutor` configurado) y `spring-boot-starter-websocket`.

2.  **Servicio de Gobernanza (`EtlJobService`):** (NUEVO)
    *   Componente central para la gestión del ciclo de vida de los jobs de ingesta.
    *   **Responsabilidades:** Crear, actualizar y consultar el estado de los jobs en la tabla `etl_jobs`. Calcular el `file_hash` para detectar y rechazar archivos duplicados.

3.  **Punto de Entrada Unificado (`EtlController`):**
    *   `POST /api/etl/start-process`: Valida el archivo (incluyendo la comprobación de duplicados con `EtlJobService`), crea un nuevo `EtlJob`, delega la ejecución al `EtlProcessingService` y devuelve `HTTP 202 Accepted` con el `jobId`.
    *   `GET /api/etl/jobs/{jobId}/status`: Permite a los clientes consultar el estado de un job específico.

4.  **Servicio de Orquestación Asíncrono (`EtlProcessingService`):**
    *   Método `@Async("etlExecutor") processFile(jobId, ...)` que recibe el `jobId` para actualizar su estado.
    *   **Responsabilidades:** Orquestar la transformación, la cuarentena de registros inválidos y delegar la carga al `DataSyncService`.

5.  **Servicio de Sincronización Transaccional (`DataSyncService`):**
    *   Contiene el método `@Transactional syncData(...)` que ejecuta la lógica de "borrar por rango e insertar en lote".

6.  **Servicio de Notificación (`NotificationService`):** Envía mensajes de estado a los clientes a través de WebSockets.

#### **4. Flujo de Datos y Secuencia de Eventos**

##### **4.1. Diagrama de Secuencia del Flujo Gobernado**

```mermaid
sequenceDiagram
    participant Cliente as Cliente (React/Script)
    participant Controller as EtlController
    participant JobService as EtlJobService
    participant Worker as EtlProcessingService (@Async)
    participant Syncer as DataSyncService (@Transactional)

    Cliente->>+Controller: 1. POST /start-process (con .txt)
    Controller->>+JobService: 2. isDuplicate(fileHash)?
    alt Archivo duplicado
        JobService-->>Controller: true
        Controller-->>-Cliente: 409 Conflict
    else Archivo nuevo
        JobService-->>Controller: false
        Controller->>+JobService: 3. createJob(file, userId)
        JobService-->>Controller: job_id
        Controller->>+Worker: 4. processFile(jobId, file)
        Controller-->>-Cliente: 5. 202 Accepted + job_id
    end

    activate Worker
    Note right of Worker: 6. Transformación, Cuarentena,<br/>y determina rango de fechas
    Worker->>+Syncer: 7. syncDataForDateRange(...)
    activate Syncer
    Note right of Syncer: 8. (Tx) DELETE + INSERT [BATCH]
    deactivate Syncer
    Worker->>+JobService: 9. updateJobStatus(jobId, "EXITO")
    deactivate Worker
```

#### **5. Diseño para la Resiliencia, Idempotencia y Seguridad**

##### **5.1. Estrategia de Gobernanza e Idempotencia por Archivo**

La robustez del sistema se basa en una doble capa de idempotencia:

1.  **Idempotencia de Proceso:** Antes de iniciar cualquier trabajo, el `EtlController` calcula un hash SHA-256 del archivo de entrada. El `EtlJobService` consulta la tabla `etl_jobs` para verificar si ya existe un job `COMPLETADO` o `EN_PROCESO` con el mismo `file_hash`. Si es así, la petición es rechazada con un `HTTP 409 Conflict`, previniendo el re-procesamiento accidental.
2.  **Idempotencia de Datos:** Si el proceso continúa, el `DataSyncService` garantiza la consistencia de los datos a través de la estrategia transaccional de "borrar por rango e insertar", como se describió anteriormente.

La tabla `etl_jobs` actúa como un **registro de auditoría inmutable**, proporcionando trazabilidad completa sobre qué se cargó, quién lo hizo y cuál fue el resultado.

##### **5.2. Transaccionalidad y Manejo de Fallos**

*   **Manejo de Registros Inválidos (Cuarentena):** Las filas malformadas se desvían a la tabla `quarantined_records` sin detener el procesamiento de las filas válidas.
*   **Fallo Durante la Sincronización:** El `rollback` transaccional del `DataSyncService` previene la corrupción de datos. El `EtlProcessingService` captura la excepción y actualiza el estado del job en `etl_jobs` a `FALLO` con los detalles del error.

##### **5.3. Consideraciones de Seguridad**

*   **Autenticación y Autorización:** Todos los endpoints están protegidos y requieren roles adecuados.
*   **Validación de Carga:** Se aplican validaciones de tamaño y tipo de archivo.
*   **Seguridad del WebSocket:** Se utilizan destinos de usuario para notificaciones privadas.

#### **6. Justificación y Evolución Futura**

La arquitectura de servicio asincrónico gobernado proporciona una base de nivel de producción que es robusta, auditable y observable. El diseño es modular y permite futuras optimizaciones, como la implementación de estrategias de `UPSERT` en el `DataSyncService` o la migración de la lógica de orquestación a una plataforma serverless (GCP Cloud Run) sin alterar el contrato de la API.

---

