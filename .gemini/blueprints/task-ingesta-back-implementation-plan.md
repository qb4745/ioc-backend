# Blueprint de Implementación: Ingesta Asincrónica (v3 - Refined)

## 1. Objetivo
Este documento es la guía de implementación detallada para construir el ETL dimensional asincrónico. Cada tarea está diseñada para ser ejecutada en secuencia, construyendo la funcionalidad de manera incremental y verificable.

---

### **Fase 1: Cimientos - Capa de Persistencia y Entidades**
*Objetivo: Establecer el mapeo Objeto-Relacional (ORM) y asegurar la conectividad con la base de datos.*

*   **[ ] `TASK-INGESTA-01`:** Mapear Entidades de Dimensión y Gobernanza
    *   **Propietario:** Jaime Vicencio (Backend)
    *   **Acción:** Crear las siguientes clases de entidad JPA en el paquete `com.cambiaso.ioc.persistence.entity`:
        *   `DimMaquina`: Mapea la tabla `dim_maquina`.
        *   `DimMaquinista`: Mapea la tabla `dim_maquinista`.
        *   `EtlJob`: Mapea la tabla `etl_jobs`. Usar `UUID` para `jobId`.
        *   `QuarantinedRecord`: Mapea la tabla `quarantined_records`.
    *   **Verificación:**
        *   **Éxito:** La aplicación arranca y las tablas son reconocidas por Hibernate.

*   **[ ] `TASK-INGESTA-02`:** Mapear Entidad de Hechos con Clave Compuesta
    *   **Propietario:** Jaime Vicencio (Backend)
    *   **Dependencias:** `TASK-INGESTA-01`.
    *   **Acción:**
        1.  Crear una clase `@Embeddable` llamada `FactProductionId` que contenga `id` y `fechaContabilizacion`.
        2.  Crear la entidad `FactProduction` que mapea la tabla `fact_production` y usa `@EmbeddedId` para la clave compuesta.
    *   **Verificación:**
        *   **Éxito:** La aplicación arranca y la relación de clave compuesta es manejada correctamente por Hibernate.

*   **[ ] `TASK-INGESTA-03`:** Crear Repositorios JPA
    *   **Propietario:** Jaime Vicencio (Backend)
    *   **Dependencias:** `TASK-INGESTA-02`.
    *   **Acción:** Crear las interfaces de Spring Data JPA correspondientes para todas las entidades en el paquete `com.cambiaso.ioc.persistence.repository`.
    *   **Verificación:**
        *   **Éxito:** Escribir un test de integración (`@DataJpaTest`) que guarde y recupere una entidad de cada tipo para validar el mapeo y las constraints.

---

### **Fase 2: Infraestructura Core - Asincronía y Notificaciones**
*Objetivo: Configurar los hilos de ejecución para el ETL y el canal de comunicación en tiempo real.*

*   **[ ] `TASK-INGESTA-04`:** Configurar Ejecutor Asincrónico
    *   **Propietario:** Jaime Vicencio (Backend)
    *   **Acción:** Crear la clase `AsyncConfig` con `@EnableAsync`. Definir un `Bean` de `ThreadPoolTaskExecutor` con el nombre `etlExecutor`. Configurar el core size, max size y queue capacity desde `application.properties`.
    *   **Verificación:**
        *   **Éxito:** La aplicación arranca y el bean `etlExecutor` está presente en el contexto de Spring.

*   **[ ] `TASK-INGESTA-05`:** Configurar WebSockets (STOMP)
    *   **Propietario:** Jaime Vicencio (Backend)
    *   **Acción:**
        1.  Añadir la dependencia `spring-boot-starter-websocket` al `pom.xml`.
        2.  Crear `WebSocketConfig` para registrar el endpoint STOMP en `/ws` y configurar el message broker.
    *   **Verificación:**
        *   **Éxito:** La aplicación arranca y un cliente de WebSocket puede conectarse a `http://localhost:8080/ws`.

*   **[ ] `TASK-INGESTA-06`:** Implementar Seguridad para WebSockets
    *   **Propietario:** Jaime Vicencio (Backend)
    *   **Dependencias:** `TASK-INGESTA-05`.
    *   **Acción:** Crear un `ChannelInterceptor` que intercepte los mensajes `CONNECT` para extraer el token JWT del header, validarlo y asociar el `Principal` del usuario a la sesión del WebSocket.
    *   **Verificación:**
        *   **Éxito:** Un cliente WebSocket que se conecta con un JWT válido establece la conexión.
        *   **Falla:** Un cliente sin JWT o con un JWT inválido es rechazado.

*   **[ ] `TASK-INGESTA-07`:** Crear Servicio de Notificaciones
    *   **Propietario:** Jaime Vicencio (Backend)
    *   **Dependencias:** `TASK-INGESTA-05`.
    *   **Acción:**
        1.  Crear el DTO `NotificationPayload(String status, String message)`.
        2.  Crear `NotificationService` que inyecte `SimpMessagingTemplate`.
        3.  Implementar un método `notifyUser(String userId, String jobId, NotificationPayload payload)` que envíe el mensaje al destino privado `/user/topic/etl-jobs/{jobId}`.
    *   **Verificación:**
        *   **Éxito:** Un test unitario con `@Mock SimpMessagingTemplate` verifica que `convertAndSendToUser` es llamado con los parámetros correctos.

---

### **Fase 3: Lógica de Negocio - Gobernanza y Sincronización**
*Objetivo: Implementar la lógica de negocio que asegura la robustez, idempotencia y atomicidad del proceso.*

*   **[ ] `TASK-INGESTA-08`:** Implementar Servicio de Gobernanza de Jobs
    *   **Propietario:** Jaime Vicencio (Backend)
    *   **Dependencias:** `TASK-INGESTA-03`.
    *   **Acción:** Crear `EtlJobService`. Implementar métodos para:
        *   `createJob(...)`: Crea una nueva entrada en `etl_jobs` con estado `INICIADO`.
        *   `updateStatus(...)`: Cambia el estado de un job.
        *   `isWindowLocked(...)`: Lógica de guarda que verifica si un rango de fechas se solapa con otro job activo.
        *   `findByFileHash(...)`: Verifica la idempotencia a nivel de archivo.
    *   **Verificación:**
        *   **Éxito:** Tests de integración validan que la guarda de ventana (`isWindowLocked`) retorna `true` para rangos superpuestos y `false` para rangos libres.
        *   **Falla:** Un test que intenta crear un job con un `file_hash` duplicado lanza una `DataIntegrityViolationException`.

*   **[ ] `TASK-INGESTA-09`:** Implementar Servicio de Sincronización de Datos
    *   **Propietario:** Jaime Vicencio (Backend)
    *   **Dependencias:** `TASK-INGESTA-03`.
    *   **Acción:** Crear `DataSyncService`. Implementar los siguientes métodos `@Transactional`:
        *   `syncWithDeleteInsert(LocalDate minDate, LocalDate maxDate, List<FactProduction> records)`: Borra los datos en el rango y luego inserta el nuevo lote.
        *   (Opcional, si se activa) `syncWithUpsert(...)` usando `JdbcTemplate` y `ON CONFLICT`.
    *   **Verificación:**
        *   **Éxito:** Un test de integración verifica que una ejecución exitosa persiste los datos.
        *   **Falla:** Un test de integración que simula una excepción a mitad de la inserción verifica que se realiza un `rollback` completo y no quedan datos parciales.

---

### **Fase 4: Orquestación y Exposición API**
*Objetivo: Ensamblar todos los componentes y exponer la funcionalidad de forma segura a través de una API REST.*

*   **[ ] `TASK-INGESTA-10`:** Implementar el Orquestador Asincrónico
    *   **Propietario:** Jaime Vicencio (Backend)
    *   **Dependencias:** `TASK-INGESTA-07`, `TASK-INGESTA-08`, `TASK-INGESTA-09`.
    *   **Acción:** Crear `EtlProcessingService` con el método `@Async("etlExecutor")`. Este método orquestará la lógica principal:
        1.  Validar archivo (hash, tipo, etc.).
        2.  Llamar a `EtlJobService` para crear el job y verificar las guardas de concurrencia.
        3.  Notificar estado `PROCESANDO`.
        4.  Parsear el archivo.
        5.  Llamar a `DataSyncService` para persistir los datos.
        6.  Notificar `EXITO` o `FALLO` y actualizar el estado final del job.
    *   **Verificación:**
        *   **Éxito:** Un test de integración de punta a punta (con `@EnableAsync`) que provee un archivo válido resulta en un estado final de `EXITO` en la base de datos.

*   **[ ] `TASK-INGESTA-11`:** Crear el Controlador API
    *   **Propietario:** Jaime Vicencio (Backend)
    *   **Dependencias:** `TASK-INGESTA-10`.
    *   **Acción:** Crear `EtlController` con los siguientes endpoints protegidos:
        *   `POST /api/etl/start-process`: Recibe el `MultipartFile`, obtiene el `userId` del `Principal`, invoca al orquestador y retorna `202 Accepted` con el `jobId`.
        *   `GET /api/etl/jobs/{jobId}/status`: Consulta `EtlJobService` y devuelve el estado actual del job.
    *   **Verificación:**
        *   **Éxito:** Un test con `MockMvc` y un token JWT válido a `POST /api/etl/start-process` retorna un `202`.
        *   **Falla:** Una petición sin token retorna `401`. Una petición a un job con un rango de fechas bloqueado retorna `409 Conflict`.
