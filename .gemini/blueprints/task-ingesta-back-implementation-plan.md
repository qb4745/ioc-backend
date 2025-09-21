# **Blueprint de Implementación: TASK-INGESTA-BACK (v9 - Production Grade)**

## 1) Objetivo
Construir y validar un ETL dimensional gobernado, concurrente-seguro, idempotente a nivel de archivo/ventana/fila, asincrónico, transaccional y observable, con palanca operativa para UPSERT y guardas de concurrencia multi-instancia.

## 2) Dependencias y configuración
- Spring Web, Validation, WebSocket/STOMP, Data JPA, PostgreSQL Driver, Lombok, Micrometer/OTel, (opcional) MapStruct.
- Propiedades base:
  ```properties
  spring.servlet.multipart.max-file-size=25MB
  spring.servlet.multipart.max-request-size=25MB
  app.etl.sync-mode=DELETE_INSERT  # opciones: DELETE_INSERT | UPSERT
  app.etl.batch-size=500
  app.etl.executor.core=2
  app.etl.executor.max=4
  app.etl.executor.queue=200
  ```

## 3) Fases y tareas

### Fase 1: Esquema y entidades
- **TASK-001:** Aplicar `schema.sql` v5.0 en Supabase (fact particionada, dimensiones, gobernanza, clave de negocio y índices).
- **TASK-002:** Mapear entidades JPA (usar `@IdClass`/`@EmbeddedId` según estrategia para `fact_production`), repos y DDL de prueba.
- **Criterios:** tablas/particiones/índices presentes; CRUD básico y constraints activas.

### Fase 2: Infraestructura asíncrona y WS
- **TASK-010:** `AsyncConfig` con `ThreadPoolTaskExecutor` dedicado y política de rechazo `CallerRuns`.
- **TASK-011:** `WebSocketConfig` (STOMP), destinos `/topic/etl/{jobId}` y `NotificationService`.
- **Criterios:** E2E-WSC-01 recibe `INICIADO → SINCRONIZANDO → EXITO/FALLO` bajo 10 jobs concurrentes.

### Fase 3: Gobernanza e idempotencia
- **TASK-020:** `EtlJobService` con unicidad por `file_hash` y política de reproceso (`FALLO` o `force=true`).
- **TASK-021:** Guard por ventana (`isWindowLocked(min,max)`) y rechazo 409 si hay solape con job activo.
- **TASK-022 (multi-instancia):** Advisory lock PG en sincronización (`pg_try_advisory_lock(...)`).
- **Criterios:** IT-JOBS-01 estados; IT-GUARD-01 solape retorna 409; IT-ADV-01 lock funciona entre nodos.

### Fase 4: Ingesta y validación
- **TASK-030:** `FileValidator` (MIME/extensión/tamaño) y cálculo de `file_hash` (SHA-256).
- **TASK-031:** `Parser` en streaming + `QuarantineService` con índices operativos.
- **Criterios:** UNIT-PARSE-*; IT-QUAR-01 inserta y consulta por `job_id`.

### Fase 5: Sincronización
- **TASK-040:** `DataSyncService.syncWithDeleteInsert(...)` transaccional con `saveAll` en batch.
- **TASK-041:** `DataSyncService.syncWithUpsert(...)` con `JdbcTemplate` y `ON CONFLICT` (clave de negocio).
- **Criterios:** IT-SYNC-01 rollback total ante fallo; IT-SYNC-02 re-ejecución idempotente; PERF-UPSERT-01 compara lock-time y throughput.

### Fase 6: API y seguridad
- **TASK-050:** POST `/api/etl/start-process` (202 + `{jobId}`), validaciones y rol de acceso.
- **TASK-051:** GET `/api/etl/jobs/{jobId}/status` con contadores y `details`.
- **Criterios:** E2E-API-01 circuito completo; SEC-API-01 política de acceso.

### Fase 7: Observabilidad
- **TASK-060:** Métricas (temporizadores por etapa, filas/s, batch-size, hilos/cola).
- **TASK-061:** Logging estructurado con MDC (`jobId`, `fileHash`, `userId`, `minDate`, `maxDate`).
- **Criterios:** OBS-01 métricas visibles; OBS-02 correlación en logs y equivalencia con eventos STOMP.

### Fase 8: Mantenimiento de particiones
- **TASK-070:** Job de creación/rotación de particiones (anual o mensual según volumetría real).
- **TASK-071:** Verificación de ANALYZE/VACUUM y checks de sanidad (cantidad/peso >= 0).
- **Criterios:** PART-01 nuevas particiones a tiempo; PART-02 sin degradación en consultas típicas.

## 4) Skeletons clave
```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/etl")
public class EtlController {
    private final EtlProcessingService service;

    @PostMapping("/start-process")
    public ResponseEntity<Map<String,Object>> start(@RequestParam MultipartFile file, Principal p,
                                                   @RequestParam(required=false, defaultValue="false") boolean force) {
        UUID jobId = service.startAsync(file, p.getName(), force);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }
}
```

```java
@Service
@RequiredArgsConstructor
public class EtlProcessingService {
    private final JobRegistryService jobs;
    private final NotificationService notify;
    private final Parser parser;
    private final DataSyncService sync;

    @Async("etlExecutor")
    public UUID startAsync(MultipartFile file, String user, boolean force) {
        UUID jobId = jobs.start(file.getOriginalFilename(), hash(file), user, force);
        try {
            notify.started(jobId);
            var batch = parser.parse(file.getInputStream());
            var range = batch.getDateRange();
            jobs.guardWindowOrThrow(range.min(), range.max()); // 409 si solape
            if (isUpsertMode()) {
                sync.syncWithUpsert(batch.getRows());
            } else {
                sync.syncWithDeleteInsert(range.min(), range.max(), batch.getRows());
            }
            jobs.success(jobId, batch.getProcessed(), batch.getQuarantined());
            notify.success(jobId);
        } catch (Exception ex) {
            jobs.fail(jobId, ex.getMessage());
            notify.fail(jobId, ex.getMessage());
        }
        return jobId;
    }
}
```

## 5) Pruebas
- **Unitarias:** validación/parseo/hash, estados y eventos.
- **Integración:** delete por rango, unicidad de negocio, advisory lock.
- **E2E:** 202, WS, GET status, re-ejecución idempotente, 409 por solape, y comparación DELETE_INSERT vs UPSERT en throughput/locks.