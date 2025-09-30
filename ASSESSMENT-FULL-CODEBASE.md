# ASSESSMENT COMPLETO - Código Fuente

Fecha: 2025-09-30
Responsable: (auto generado)

Objetivo
--------
Revisar recursivamente todo el código fuente del proyecto y producir un informe por funcionalidad (archivo/clase/módulo) con:
- Propósito funcional
- Nivel de madurez (Alta/Media/Baja)
- Calidad de código (estilo, patrones, antipatróns detectados)
- Cobertura de tests (qué pruebas existen / fallan / están deshabilitadas)
- Riesgos, defectos y observaciones técnicas
- Recomendaciones concretas y priorizadas para corrección

Resumen ejecutivo (rápido)
--------------------------
La aplicación es una Spring Boot bien estructurada con un núcleo ETL robusto: parseo de archivos, sincronización con la BD, gestión de jobs y notificaciones WebSocket. Se incorporaron mecanismos avanzados: retry frente a violaciones UNIQUE, advisory locks para serializar ventanas, watchdog para jobs stuck y métricas Micrometer. La calidad general es buena y el diseño modular es sólido.

Pros
- Arquitectura modular basada en Spring Boot: capas bien separadas (controller, service, persistence, config) que facilitan mantenimiento y extensión.
- Núcleo ETL maduro: parsing defensivo, deduplicado, persistencia por batch, manejo de dimensiones y orquestación de jobs.
- Mecanismos resilientes: retry configurables, circuit-breaker (Resilience4j), advisory locks y watchdog para casos de jobs atascados.
- Observabilidad integrada: Micrometer (timers, counters, summaries) y `HealthIndicator` para chequeos de integridad de la BD.
- Soporte para notificaciones en tiempo real (WebSocket / STOMP) y control de rate-limiting por usuario.
- Buenas prácticas transaccionales: uso de `TransactionTemplate` y `REQUIRES_NEW` para aislar persistencia de dimensiones.
- Tests presentes (unitarios e integración) para escenarios críticos, y uso de Testcontainers para pruebas con Postgres.

Contras
- Inestabilidad en tests de concurrencia/integración: varios tests intermitentes o deshabilitados, lo que impacta la confianza en CI.
- Detección de violaciones UNIQUE mediante reflexión y parsing de mensajes es frágil y dependiente del driver/stack (puede fallar en algunos entornos).
- Uso de `@Data` de Lombok en entidades con relaciones LAZY puede introducir efectos colaterales en `equals/hashCode` y en la serialización/compare de entidades.
- Dependencia de `pg_advisory_xact_lock` y timing fino: la serialización esperada puede fallar por diferencias en conexiones/transacciones en contenedores o CI.
- Consultas de salud/diagnóstico ejecutan queries pesadas (COUNT/GROUP BY) que pueden impactar a bases con tablas grandes si se ejecutan con demasiada frecuencia.
- Algunas métricas y gauges faltan o son "best-effort"; la observabilidad puede mejorarse con mayor cobertura de métricas críticas (por ejemplo: file size, jobs activos en detalle, índices de retraso).
- Código legado y archivos huérfanos (`FactProductionId.java`) y reflexiones que complican el mantenimiento.

<!-- Inserted: Table of Contents, TL;DR, Test Matrix, Playbook, Acceptance Criteria -->

## Índice (TOC)
- Resumen ejecutivo
- Pros / Contras
- TL;DR — Acciones inmediatas (Prioridad alta)
- Matriz de Estado de Tests
- Playbook de Reproducción (comandos)
- Criterios de Aceptación (DoD) para recomendaciones
- Diagnóstico y recomendaciones detalladas
- Detalle por archivo
- Conclusión y siguientes pasos

## TL;DR — Acciones Inmediatas (Prioridad Alta)
1. Instrumentar logs diagnósticos en `DataSyncService.tryAcquireAdvisoryLock` y `isUniqueConstraintViolation` (propiedad para activarlo en CI) — Owner: Backend Lead — Est.: 30–60m — Criterio: logs disponibles que muestren SQLState o stack cuando ocurre `23505`.
2. Crear Matriz de Estado de Tests y reproducir los 3 tests fallidos en un entorno local con Testcontainers; capturar logs — Owner: QA — Est.: 60–120m — Criterio: comandos reproducibles y logs adjuntos en un artefacto.
3. Mover tests inestables a perfil `integration` y configurar gating distinto en CI (no romper pipeline rápido) — Owner: CI/CD — Est.: 30–60m — Criterio: pipeline rápido verde, integration pipeline ejecuta tests pesados.
4. Revisar `EtlHealthIndicator` queries costosas y añadir cache TTL 5m o probe asíncrono — Owner: SRE — Est.: 60–120m — Criterio: health probe responde en <200ms en producción replica mínima.
5. Cambiar equals/hashCode de `FactProduction` para excluir asociaciones (propuesta de PR) — Owner: Backend — Est.: 30m — Criterio: compilar y tests unitarios pasan.

## Matriz de Estado de Tests (Test Status Matrix)
| Test (clase#método) | Estado | Disabled | Entorno | Comando reproducible | Owner | Nota breve |
|---|---:|:---:|---|---|---|---|
| com.cambiaso.ioc.service.AdvisoryLockSerializationTest#advisoryLockBlocksSecondAndLastBatchWins | FAIL (intermitente) | No | Local/CI (Testcontainers Postgres) | `mvn -Dtest=com.cambiaso.ioc.service.AdvisoryLockSerializationTest#advisoryLockBlocksSecondAndLastBatchWins test` | QA/Backend | No se observó delay; salida mostró 4 registros en vez de 2 |
| com.cambiaso.ioc.service.DataSyncConcurrencyTest#uniqueConstraintRetryOnConcurrentInsert | FAIL (intermitente) | No | Local/CI | `mvn -Dtest=com.cambiaso.ioc.service.DataSyncConcurrencyTest#uniqueConstraintRetryOnConcurrentInsert test` | QA/Backend | Un hilo propagó excepción, retry no absorbió colisión |
| com.cambiaso.ioc.service.EtlJobWatchdogTest#windowAndBatchSummaries | FAIL | No | Local (H2) | `mvn -Dtest=com.cambiaso.ioc.service.EtlJobWatchdogTest#windowAndBatchSummaries test` | QA | Summary metric null; investigar registro de métricas en contexto transaccional |

> Nota: En el repositorio existen también tests anotados con `@Disabled`, por ejemplo:
> - `DataSyncConcurrencyTest.advisoryLockSerializesOverlappingRanges` (deshabilitado por inestabilidad en CI)
> - `DataSyncConcurrencyIT.advisoryLockSerializesOverlappingRanges` (deshabilitado)
> - `EtlJobWatchdogTest.watchdogTerminatesStuckJobs` (deshabilitado para H2 vs Postgres mismatch)
> - `ParserServiceTest` contiene tests deshabilitados para validación manual con archivos grandes.

## Playbook de Reproducción (comandos concretos)
1. Ejecutar test individual (Advisory Lock) con logging aumentado y guardar salida:

```bash
mvn -Dtest=com.cambiaso.ioc.service.AdvisoryLockSerializationTest#advisoryLockBlocksSecondAndLastBatchWins \
  -Dlogging.level.com.cambiaso.ioc.service=TRACE \
  -Dlogging.level.org.hibernate.SQL=DEBUG \
  -DfailIfNoTests=false test > mvn_advisory_lock_test.log 2>&1
# luego revisar mvn_advisory_lock_test.log
```

2. Ejecutar retry-unique test con sleep inducido (si aplica la propiedad):

```bash
mvn -Dtest=com.cambiaso.ioc.service.DataSyncConcurrencyTest#uniqueConstraintRetryOnConcurrentInsert \
  -Detl.sync.test.sleep-ms=300 \
  -Dlogging.level.com.cambiaso.ioc.service=TRACE test > mvn_retry_unique_test.log 2>&1
```

3. Ejecutar watchdog test (nota: en H2 puede comportarse distinto):

```bash
mvn -Dtest=com.cambiaso.ioc.service.EtlJobWatchdogTest#windowAndBatchSummaries \
  -Dspring.profiles.active=test -Dlogging.level.com.cambiaso.ioc.service=DEBUG test > mvn_watchdog_test.log 2>&1
```

4. Capturar trazas de excepción completas (usar redirect a archivo y subir como artifact en CI). Recomiendo: setear `-DargLine='-Dlogging.level.com.cambiaso.ioc.service=TRACE'` en el job de CI durante la investigación.

## Criterios de Aceptación (Definition of Done) para recomendaciones principales
- Logging diagnóstico (DataSyncService): PR con cambios, propiedad `etl.diagnostics.enabled` soportada, logs muestran SQLState o causa cuando ocurre excepción; tests de integración que reproducen la excepción deben generar trazas en el log artifact.
- Test Matrix & Playbook: tabla completada con estado verificado, comandos reproducibles que generan logs; owner asignado y ticket en backlog creado.
- Mover tests a perfil `integration`: pipeline rápido pasa y pipeline `integration` ejecuta tests deshabilitados; documentación en README de CI.
- HealthIndicator caching: PR que añade cache con TTL 5m o job asíncrono; métrica de latencia del health <200ms en réplica mínima.
- FactProduction equals/hashCode: PR con cambio, compila, y test suite local pasa.

Contenido del informe
---------------------
A continuación se documenta por componente / archivo.

1) Persistencia - Entidades
---------------------------
- `FactProduction` (src/main/java/.../persistence/entity/FactProduction.java)
  - Propósito: entidad core que representa registro de producción.
  - Madurez: Alta.
  - Calidad:
    - Usa `@Data` de Lombok: genera equals/hashCode para todos los campos. Como la entidad tiene relaciones `@ManyToOne(fetch=LAZY)`, esto puede inducir a `LazyInitializationException` o comparaciones inesperadas si las colecciones/entidades relacionadas se incluyen en equals/hashCode. Recomendación: usar `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` o excluir asociaciones.
    - Buen `toString()` defensivo evitando forzar inicialización de proxies.
  - Tests: fuertemente usados por los tests de sync y parse.
  - Riesgos: equals/hashCode por defecto.

- `DimMaquina`, `DimMaquinista`
  - Propósito: dimensiones; contienen unique codes.
  - Madurez: Alta.
  - Calidad: correctas, usan `@CreationTimestamp/@UpdateTimestamp`.

- `EtlJob`
  - Propósito: tracking de jobs ETL.
  - Madurez: Alta.
  - Calidad: bien modelada; `file_hash` unique para idempotencia.

- `QuarantinedRecord`
  - Propósito: almacenar líneas con errores.
  - Madurez: Media.

Observación general de entidades
- El archivo `FactProductionId.java` está vacío (comentario de legacy). Limpiar/eliminar.

2) Persistencia - Repositorios
------------------------------
- `FactProductionRepository`:
  - deleteByFechaContabilizacionBetween: query JPQL `DELETE FROM FactProduction fp WHERE fp.fechaContabilizacion BETWEEN :minDate AND :maxDate` usada por el flujo delete+insert. Correcto y marcado `@Modifying`.
  - Riesgo: dependencia de transacción y flush/clear correctamente gestionados por `TransactionTemplate`.

- `EtlJobRepository` + `EtlJobRepositoryImpl`:
  - `markStuckAsFailed` implementado en `Impl` por limitaciones JPQL con OffsetDateTime. Implementación segura: query select + per-row update.
  - Madurez: Alta.
  - Observación: la implementación hace updates en memoria y confía en flush/commit por transacción; es razonable.

- `DimMaquinaRepository`, `DimMaquinistaRepository`, `QuarantinedRecordRepository` activos y simples.

3) Servicios (módulo ETL)
-------------------------
- `DataSyncService`
  - Propósito: sincronizar lote de `FactProduction` para un rango de fechas. Implementa:
    - Advisory locks (pg_advisory_xact_lock)
    - Delete por rango y luego insert (saveAll + flush)
    - Retry en caso de violación UNIQUE (23505) con backoff y reset de IDs
    - Métricas (Micrometer): rows deleted/inserted, attempts, collisions, summaries
  - Madurez: Alta (completa y robusta)
  - Calidad:
    - Manejo exhaustivo de excepciones, heurísticas para detectar `23505` a través de la causa y texto del stack. Esto es defensivo pero complejo.
    - `computeLockKey` usa (a << 32) ^ b: produce una llave determinística; revisar si el shift provoca colisiones en fechas largas (recomendado documentar key scheme y considerar usar hashing estable o pair (int,int) con `pg_advisory_xact_lock(key1, key2)`).
    - `executeOnce` envuelve lógica en `TransactionTemplate.execute(...)` — esto asegura aislamiento de la transacción en cada intento.
  - Tests:
    - Varios tests de integración y unitarios cubren delete/insert, rollback en fallos, retry concurrente y efecto de lock.
    - Algunos tests fallan o son inestables en CI (ver más abajo).
  - Riesgos y observaciones:
    - Intermitencia en tests de advisory lock: en algunos entornos la serialización no se observa (tiempos cortos), posiblemente por timing entre adquisición de lock y DELETE o por cómo Testcontainers / contenedor PostgreSQL administra conexiones y transacciones.
    - `isUniqueConstraintViolation` es amplia y puede fallar silenciosamente en algunos drivers.

- `ParserService`
  - Propósito: parsear archivos .txt, deduplicar lógicamente, crear dimensiones faltantes en memoria y persistirlas.
  - Madurez: Alta.
  - Calidad:
    - Buen manejo de encoding Windows-1252 y defensivo ante líneas malformadas.
    - Caching de dimensiones en memoria y persistencia posterior (batch save) — patrón correcto.
    - Métricas para parsed, malformed, duplicates y ratio guardado apropiadamente.
  - Riesgos:
    - Dependencia de `maquina.getCodigoMaquina()` en equals/keys; si las entidades no se persisten antes de ser referenciadas puede haber problemas de identidad (pero el código guarda dimensiones luego de parse).

- `EtlProcessingService` / `EtlJobService`
  - Orquestan el proceso: crear job, calcular hash, lanzar proceso asíncrono (Executor/@Async), notificar y trackear métricas.
  - Madurez: Alta.
  - Calidad: Uso de Resilience4j para circuit breaker, timers para duración. `EtlJobService` añade timers por `status`.

- `EtlJobWatchdog`
  - Propósito: detectar y marcar jobs stuck.
  - Madurez: Media-Alta.
  - Calidad: Implementado con `@Scheduled` y uso de repositorio custom para marcar stuck.
  - Tests: Un test sintético existe pero está `@Disabled` (H2 vs Postgres behavior). Riesgo: falta validación e2e en Postgres.

- `DimensionSyncService`
  - Propósito: persistir nuevas dimensiones en transacciones REQUIRES_NEW para aislar de fallos del ETL.
  - Madurez: Alta.

- `NotificationService`
  - Propósito: enviar notificaciones vía WebSocket.
  - Madurez: Alta.
  - Calidad: Manejo de errores explícito y seguimiento de usuarios activos en `ConcurrentHashSet`.

4) Controladores / API
---------------------
- `EtlController`
  - Endpoints: `POST /api/etl/start-process` y `GET /api/etl/jobs/{jobId}/status`.
  - Madurez: Alta.
  - Calidad:
    - Validación de archivos robusta.
    - Idempotencia check por `fileHash` antes de crear job.
    - Lanza `JobConflictException` para rechazar duplicados.

5) Observabilidad / Métricas / Health
------------------------------------
- `EtlJobMetricsRegistrar`: registra gauges `etl.jobs.active`, `etl.jobs.stuck`, `etl.unique.index.present`. Buen manejo de motores no-Postgres.
- `EtlHealthIndicator`: consulta DB directamente para comprobar índice unique, filas duplicadas y secuencia. Robusto pero puede impactar a DB si se ejecuta frecuentemente.
- `MetricsConfig`: common tags + deny actuator metrics in URIs.

6) Config y WebSocket
---------------------
- `RateLimitingConfig` y `RateLimitInterceptor`: good use of `bucket4j`, per-user buckets. Ensure configuration aligns with production expectations.
- `AsyncConfig`: Executor `etlExecutor` configurado con pool y CallerRunsPolicy.
- `WebSocketConfig` y `WebSocketSecurityConfig`: implementan seguridad e interceptores para JWT en connect. Good.

7) Tests
--------
- Buena cobertura general: unidades e integración (Testcontainers/Postgres) en áreas críticas.
- Tests que fallaron o están inestables en tus runs:
  - `AdvisoryLockSerializationTest.advisoryLockBlocksSecondAndLastBatchWins` → falló (no se observó delay; duración de transacciones muy corta, además resultado final contenía 4 registros en una ejecución) — indica que la serialización no se produjo en la BD.
  - `DataSyncConcurrencyTest.uniqueConstraintRetryOnConcurrentInsert` → falló en una ejecución: 1 hilo propagó un fallo (se esperaba 0) — indica que retry logic no absorbió la violación unique en al menos un camino de excepción.
  - `EtlJobWatchdogTest.windowAndBatchSummaries` → falló aserción null en summary, posible no registro de metric o ejecución en un contexto transaccional que no registró métricas.
- Observación: varios tests de integración están `@Disabled` con notas sobre inestabilidad en contenedores/CI.

Diagnóstico y causas probables de los fallos (concrete)
------------------------------------------------------
1) Advisory lock no bloquea:
   - Posible causas:
     a) `etl.lock.enabled` estaba desactivado en la ejecución concreta (múltiples entornos tienen defaults variables). Verificar propiedades dinámicas en Testcontainers.
     b) `pg_advisory_xact_lock` no se ejecutó dentro de la misma transacción que el DELETE (por ejemplo: `transactionTemplate` puede estar configurado con `PROPAGATION_REQUIRED`, pero si el EntityManager usa una conexión distinta o si autocommit está activado, el lock no persiste como se espera). Sin embargo `transactionTemplate` debe asegurarlo.
     c) El `lockTestSleepMs` no se aplicó (propiedad inyectada no disponible, o valor fue 0). Asegurarse de que `@Value` leyó la propiedad del `DynamicPropertySource`.
     d) Concurrency scheduling/timing: ambos hilos pueden adquirir lock casi simultáneamente si la doble transferencia a la BD usa diferentes sesiones y el lock key colisiona o no (por implementación del driver). Rara pero posible.
   - Reproducción: ejecutar el test aislado con logging DEBUG para `DataSyncService` y agregar trazas en `tryAcquireAdvisoryLock` para confirmar si la llamada nativa falló o fue silenciosa.

2) Retry no absorbió excepción:
   - Posible causas:
     a) La excepción lanzada no fue reconocida por `isUniqueConstraintViolation` (por ejemplo, la excepción es un `DataIntegrityViolationException` con causa diferente o un `PersistenceException` envolviendo la `ConstraintViolationException` con SQLState no accesible por reflexión).
     b) Efecto de saveAll + flush produce BatchUpdateException con `SQLState` en `nextException` — el código intenta detectarlo pero puede fallar si driver/jdbc wrapper difiere.
     c) El reintento se produjo pero el segundo intento también falló y el test detectó una excepción propagada.
   - Reproducción: ejecutar test aislado con loggers TRACE/DEBUG y capturar la traza completa de la excepción; instrumentar `isUniqueConstraintViolation` con logs de las condiciones evaluadas.

Recomendaciones concretas y priorizadas
---------------------------------------
Prioridad Alta (resolver test e integridad)
1. Agregar logging diagnóstico en `DataSyncService.tryAcquireAdvisoryLock(...)` y en `isUniqueConstraintViolation(...)` para capturar exactamente qué excepción aparece en ejecución (clase, SQLState, mensaje, stack-trace parcial). Esto ayudará a entender por qué retry no detecta la violación o por qué lock no se adquiere.
   - Inserta logs TRACE antes y después de `createNativeQuery(...)` y atrapa excepciones específicas.

2. Reproducir los tests en modo aislado con Testcontainers y con propiedades de system log habilitadas:
   - mvn -Dtest=com.cambiaso.ioc.service.AdvisoryLockSerializationTest#advisoryLockBlocksSecondAndLastBatchWins test
   - Ejecutar con `-Dlogging.level.com.cambiaso.ioc.service=DEBUG` y `-Dorg.hibernate.SQL=DEBUG` para ver SQL statements.

3. Asegurar que `lockTestSleepMs` y `syncTestSleepMs` están siendo inyectados en entornos de test (DynamicPropertySource sí los define en tests; validar lectura en `DataSyncService` con un log al inicio del `executeOnce`).

4. Fortalecer `isUniqueConstraintViolation`:
   - Añadir logging en cada chequeo.
   - Añadir detección explícita para `org.springframework.dao.DataIntegrityViolationException` y `org.hibernate.exception.ConstraintViolationException` sin depender exclusivamente de reflexión.
   - Cuando se detecte un `BatchUpdateException`, iterar `ex.getNextException()` y loggear SQLState.

5. Considerar usar `pg_advisory_xact_lock(bigint, bigint)` con dos ints derivados (minDateEpoch & maxDateEpoch) y evitar shift XOR que podría colisionar. Documentar la función `computeLockKey`.

Prioridad Media (calidad y limpieza)
1. Cambiar equals/hashCode en `FactProduction` para excluir relaciones y evitar efectos secundarios:
   - `@Data` -> `@Getter @Setter @ToString` + `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` o usar `@EqualsAndHashCode(exclude = {"maquina","maquinista"})`.

2. Eliminar `FactProductionId.java` si no se usa.

3. Revisar `EtlJobMetricsRegistrar` y `EtlHealthIndicator` para asegurar que consultas pesadas no se ejecuten con demasiada frecuencia en producción (usar cache con TTL para `unique index presence`).

4. Añadir tests unitarios que "simulen" excepciones concretas a `isUniqueConstraintViolation` para validar detección en distintos drivers.

Prioridad Baja (ops / mejoras)
1. Implementar gauges y métricas pendientes (`etl.jobs.active`, `etl.jobs.stuck` ya presentes; `etl.file.size.bytes`, `etl.dim.new.*` ya parcialmente implementadas en parser). Completar `EtlHealthIndicator` con `uniqueIndex` como gauge enlazado.

2. Documentar esquema de locks (cómo se calculan las claves) y agregar comentario de compatibilidad DB.

Sugerencia de pasos para depuración rápida (ordenado)
----------------------------------------------------
1. Ejecutar el test que falla aislado y con logs:

   mvn -Dtest=com.cambiaso.ioc.service.AdvisoryLockSerializationTest#advisoryLockBlocksSecondAndLastBatchWins -DargLine='-Dlogging.level.com.cambiaso.ioc.service=TRACE -Dlogging.level.org.hibernate.SQL=DEBUG' test

2. Añadir temporalmente trazas en el código (DataSyncService):
   - `log.debug("lockTestSleepMs={}, etlLockEnabled={}", lockTestSleepMs, etlLockEnabled)` al inicio de `executeOnce`.
   - En `tryAcquireAdvisoryLock` loguear entrada, success, y captura de excepción con `ex.getClass().getName()`.
   - En `isUniqueConstraintViolation` loguear clase de excepción y SQLState (si se extrae).

3. Repetir `DataSyncConcurrencyTest.uniqueConstraintRetryOnConcurrentInsert` con `-Dlogging.level` a TRACE para ver secuencia de intentos/colisiones.

Observaciones de estilo y arquitectura
-------------------------------------
- Código modular y cohesivo; buen uso de Spring idiomático y Micrometer.
- Buenas prácticas: uso de `TransactionTemplate` para control fino de transacciones en reintentos; `REQUIRES_NEW` para dimension sync.
- Areas de mejora: reducir uso de reflexión para detección de SQLState (usar APIs de SQLException/BatchUpdateException cuando sea posible), mejorar mensajes y añadir pruebas que simulen distintos drivers/encapsulaciones de excepción.

Archivos recomendados a modificar inmediatamente (patch sugerido)
----------------------------------------------------------------
1. `DataSyncService`:
   - Añadir trazas en `executeOnce`, `tryAcquireAdvisoryLock` y `isUniqueConstraintViolation`.
   - Considerar opción de usar `pg_try_advisory_xact_lock` con comprobación clara y fallback con retry short-circuit.

2. `FactProduction`:
   - Reemplazar `@Data` por una combinación controlada de anotaciones para evitar equals/hashCode con asociaciones.

3. Tests:
   - Deshabilitar temporalmente tests inestables que afectan pipeline o moverlos a perfil `integration` con `mvn -Pit` separado.

Entrega
-------
He creado este documento `ASSESSMENT-FULL-CODEBASE.md` en la raíz del repositorio con el contenido de este informe. Continúo disponible para:
- Aplicar los cambios mínimos y seguros (instrumentación de logs, cambio en equals/hashCode) y ejecutar tests aislados para validar.
- Probar a reproducir fallos localmente y preparar PR con fixes concretos si quieres que lo aplique.

¿Quieres que implemente los cambios de diagnóstico y ejecute los tests problemáticos ahora (añadiendo trazas y re-ejecutando los tests que fallan)? Si sí, ejecutaré los editores necesarios, correré `mvn -Dtest=... test` localmente y reportaré los resultados y siguientes pasos.

---

Detalle por archivo (resumen por fichero)
----------------------------------------
A continuación se listan los archivos fuente principales con un breve diagnóstico por archivo (propósito, madurez, calidad, pruebas relacionadas y observaciones).

Nota: las rutas son relativas a `src/main/java/com/cambiaso/ioc` o `src/test/java/com/cambiaso/ioc`.

1) Aplicación y arranque
- `IocbackendApplication.java`
  - Propósito: Clase principal Spring Boot (bootstrap).
  - Madurez: Alta.
  - Calidad: Simple y correcta; declara `@EnableScheduling`, `@EnableJpaRepositories` y `@EntityScan`.
  - Tests: cubierto por `IocbackendApplicationTests`.
  - Observación: ninguna.

2) Configuración
- `config/MetricsConfig.java`
  - Propósito: personalización global de Micrometer (common tags, meter filter para actuator).
  - Madurez: Alta.
  - Calidad: Minimalista y adecuada.
  - Observación: añadir filtros adicionales según necesidades de cardinalidad.

- `config/ResilienceConfig.java`
  - Propósito: placeholder para configuración de Resilience4j.
  - Madurez: Baja (vacío técnicamente; la configuración se delega a properties y anotaciones).
  - Observación: mover configuración central aquí si se desea visibilidad/definición programática.

- `config/WebConfig.java`
  - Propósito: registra interceptores (rate limiting) en MVC.
  - Madurez: Alta.
  - Calidad: correcta, orden explícito para interceptor.

- `config/AsyncConfig.java`
  - Propósito: configura `etlExecutor` para procesamiento asíncrono del ETL.
  - Madurez: Alta.
  - Calidad: parámetros sensatos (core/max/queue/rejection). Recomendado exponer configurables vía properties.

- `config/RateLimitingConfig.java`
  - Propósito: fábrica de buckets (Bucket4j) por usuario.
  - Madurez: Alta.
  - Calidad: implementación correcta; revisar TTL de buckets y almacenamiento compartido en Redis para multi-instance.

- `config/WebSocketConfig.java` y `config/WebSocketSecurityConfig.java`
  - Propósito: configurar broker STOMP y seguridad en websocket (decodificar JWT en CONNECT).
  - Madurez: Alta.
  - Calidad: buena integración de JwtDecoder en el channel interceptor; cuidado con performance y validación.

3) Interceptor
- `interceptor/RateLimitInterceptor.java`
  - Propósito: aplicar límites de tasa por usuario/IP para endpoints ETL.
  - Madurez: Alta.
  - Calidad: convence; incluye cabeceras RateLimit RFC y mensaje JSON para 429; revisar las cabeceras por consistencia con infra.

4) Controlador
- `controller/EtlController.java`
  - Propósito: endpoints públicos para iniciar ETL y consultar estado de job.
  - Madurez: Alta.
  - Calidad: validación de archivo completa, idempotencia por hash, seguridad con `@PreAuthorize`.
  - Tests: `EtlControllerTest`, `EtlControllerIntegrationTest`.
  - Observación: comprueba tamaño máximo en código (50MB) — mantener sincronizado con infra y client-side.

5) Health & Metrics
- `health/EtlHealthIndicator.java`
  - Propósito: check /actuator/health específico para ETL, comprueba índice unique, duplicados y secuencia.
  - Madurez: Alta.
  - Calidad: consultas directas a la DB; robusto pero potencialmente costoso en tablas grandes. Recomendado cache/TTL para checks de index.

- `metrics/EtlJobMetricsRegistrar.java`
  - Propósito: registra gauges y comprobaciones para jobs activo/stuck y presencia de índice unique.
  - Madurez: Alta.
  - Calidad: cuidado con detección de Postgres; implementado defensivamente para H2/otros motores.

6) Servicios (detalle corto)
- `service/DataSyncService.java`
  - Propósito: core sync delete+insert, advisory locks, retry unique.
  - Madurez: Alta.
  - Calidad: código complejo pero coherente; principales puntos críticos: adquisición del advisory lock, heurística de detección de 23505 y manejo de transacciones. Ver tests de concurrencia.
  - Tests relacionados: `DataSyncServiceTest`, `DataSyncConcurrencyTest`, `DataSyncConcurrencyIT`, `AdvisoryLockSerializationTest`.

- `service/ParserService.java`
  - Propósito: parseo de archivos, deduplicado y persistencia de dimensiones faltantes.
  - Madurez: Alta.
  - Calidad: muy defensivo; buen manejo de encoding y saneamiento. Buen registro de métricas y summary.
  - Tests: `ParserServiceTest`, `ParserServiceDedupTest`, `ParserServiceLeadingZerosDedupTest`.

- `service/EtlProcessingService.java`
  - Propósito: orquestación general (crear job, parsear, sincronizar, notificar).
  - Madurez: Alta.
  - Calidad: integración con Resilience4j, métricas y notificaciones. Es punto de coordinación crítico.
  - Tests: `EtlProcessingServiceTest`, `EtlProcessingIntegrationTest`.

- `service/EtlJobService.java`
  - Propósito: CRUD y estado de jobs ETL.
  - Madurez: Alta.
  - Calidad: maneja timestamps, status y métricas.
  - Tests: `EtlJobServiceTest`.

- `service/EtlJobWatchdog.java`
  - Propósito: detectar y marcar jobs stuck.
  - Madurez: Media-Alta.
  - Calidad: bien implementado; test sintético presente pero deshabilitado en algunos contextos. Recomendado validar E2E en Postgres.
  - Tests: `EtlJobWatchdogTest` (algunos deshabilitados por H2/Postgres mismatch).

- `service/DimensionSyncService.java`
  - Propósito: persistir nuevas dimensiones en transacción separada (REQUIRES_NEW).
  - Madurez: Alta.
  - Calidad: correcto; evita contaminación de la transacción principal.

- `service/NotificationService.java`
  - Propósito: envío de notificaciones WebSocket y tracking de usuarios activos.
  - Madurez: Alta.
  - Calidad: manejo de errores explícito y consistente.

7) Persistencia - Repositorios
- `persistence/repository/FactProductionRepository.java`
  - Propósito: repository JPA para `FactProduction`, incluye `deleteByFechaContabilizacionBetween`.
  - Madurez: Alta.
  - Calidad: correcto; revisar uso de `@Transactional` en el repo.

- `persistence/repository/EtlJobRepository.java` y `EtlJobRepositoryImpl.java`
  - Propósito: repo para `EtlJob` con custom `markStuckAsFailed` (impl per-row update para evitar JPQL issues con OffsetDateTime).
  - Madurez: Alta.
  - Calidad: razonable; `Impl` usa EntityManager para evitar incompatibilidades JPQL.

- `persistence/repository/DimMaquinaRepository.java`, `DimMaquinistaRepository.java`, `QuarantinedRecordRepository.java`
  - Propósito: repos básicos para dimensiones y registros en cuarentena.
  - Madurez: Alta/Media.
  - Calidad: simples y correctos.

8) DTOs y utilidades
- `dto/NotificationPayload.java`
  - Propósito: payload para notificaciones WebSocket.
  - Madurez: Alta.
  - Calidad: simple DTO con Lombok.

9) Tests (resumen por carpeta)
- `src/test/java/com/cambiaso/ioc/service`
  - Contiene tests de unidad e integración para DataSync, Parser, EtlProcessing y concurrencia.
  - Notas: varios tests de concurrencia/locking dependen de Testcontainers/Postgres; algunos están deshabilitados por inestabilidad en CI.

- `src/test/java/com/cambiaso/ioc/controller` y `config`
  - Tests para controladores y WebSocket integration; buen coverage.


Conclusión y entrega técnica
---------------------------
- He añadido este bloque "Detalle por archivo" al final de `ASSESSMENT-FULL-CODEBASE.md` para dar visibilidad inmediata por fichero.
- Si quieres, puedo:
  - Generar una versión más amplia por archivo (2-3 párrafos cada uno) y exportar a PDF/Confluence.
  - Preparar parches (logs diagnósticos o cambios puntuales) y ejecutar los tests problemáticos en tu entorno.
  - Filtrar el reporte por audiencia: Devs / SRE / Product y priorizar acciones.

Indica la siguiente acción que deseas (ej.: "expandir por archivo", "crear PR con logs", "ejecutar tests aislados") y la ejecutaré.
