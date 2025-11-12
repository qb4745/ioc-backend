# Informe: Fuga de Conexiones durante ETL (HikariCP leak)

Fecha: 2025-11-11
Autor: Diagnóstico automático

## Resumen ejecutivo

Se detectó una fuga de conexiones (connection leak) reportada por HikariCP durante la ejecución del proceso ETL asíncrono. El síntoma principal es el warning:

```
Apparent connection leak detected
```

y el stacktrace muestra que la conexión fue tomada durante una transacción iniciada por `DataSyncService.executeOnce()` (invocado desde `EtlProcessingService.processFile()`), y no fue devuelta al pool.

Este informe documenta evidencia, diagnóstico, causa(s) probables, impacto, y la solución propuesta con pasos de validación y mitigación.

---

## Checklist (acciones incluidas en el informe)

- [x] Revisión del stacktrace y contexto de ejecución
- [x] Identificación de puntos del código implicados (`DataSyncService`, `EtlProcessingService`, `EntityManager`)
- [x] Diagnóstico de causas probables
- [x] Propuesta de corrección con código ejemplo (sin aplicar cambios)
- [x] Plan de pruebas y validación
- [x] Recomendaciones operacionales y monitoreo

---

## Evidencia (extracto del log)

```
2025-11-11T22:07:09.152-03:00  WARN 83864 --- [ioc-backend] [rod:housekeeper] com.zaxxer.hikari.pool.ProxyLeakTask : Connection leak detection triggered for org.postgresql.jdbc.PgConnection@2685dc97 on thread ETL-1

... stacktrace ...
 at com.cambiaso.ioc.service.DataSyncService.executeOnce(DataSyncService.java:134)
 at com.cambiaso.ioc.service.DataSyncService.syncWithDeleteInsert(DataSyncService.java:74)
 at com.cambiaso.ioc.service.EtlProcessingService.processFile(EtlProcessingService.java:103)
```

Contexto adicional: `DataSyncService.executeOnce()` ejecuta un bloque transaccional a través de `TransactionTemplate.execute(...)` y dentro de ese bloque se llama a `entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(?);")` para adquirir un advisory lock en Postgres.

---

## Diagnóstico (causa raíz y causas probables)

1. Causa primaria probable
   - La transacción que adquiere el `pg_advisory_xact_lock()` no finaliza correctamente (commit/rollback) en algunos caminos de error (excepciones, retries), dejando la conexión tomada y sin devolver al pool.

2. Causas secundarias contribuyentes
   - Uso de `TransactionTemplate.execute()` en combinación con ejecución asíncrona: si la excepción se propaga fuera del contexto transaccional o el hilo se interrumpe, la transacción puede quedar en estado inconsistente.
   - Loop de reintentos (`syncWithDeleteInsert`) que puede interrumpir el ciclo de transacciones sin asegurarse de rollback o limpieza completa.
   - `EntityManager.createNativeQuery(...)` usado para el advisory lock dentro de la misma transacción: la lock API se mantiene ligada a la transacción y, si ésta no se cierra, la conexión permanecerá ocupada.

3. Por qué se detecta como "leak"
   - HikariCP detecta que una conexión fue prestada y no fue devuelta al pool dentro del `leakDetectionThreshold` (o después de superar tiempos razonables), y emite el warning con stacktrace de obtención de la conexión.

---

## Impacto

- Severidad: alta. Si no se corrige, el pool de conexiones se agotará y la aplicación dejará de atender peticiones que necesiten DB.
- Afecta al procesamiento ETL y a cualquier otro flujo que requiera conexiones DB.
- Requiere intervención (posible reinicio) para liberar conexiones en caso de bloqueo generalizado.

---

## Solución propuesta (resumen)

Objetivo: asegurar que cualquier conexión obtenida para ejecutar la transacción sea siempre devuelta, incluso ante excepciones, y aislar/adaptar el uso del advisory lock para que no impida la liberación de conexiones.

Pasos propuestos (ordenados por prioridad):

1. Habilitar detección y logs de leak (diagnóstico) — inmediato
   - `spring.datasource.hikari.leak-detection-threshold=30000` (60s o 30s según ambiente)
   - Activar logs DEBUG para Hikari y transacciones:
     - `logging.level.com.zaxxer.hikari=DEBUG`
     - `logging.level.org.springframework.transaction=DEBUG`

2. Aislar la transacción y manejo del advisory lock — corregir código
   - Reemplazar uso programático de `TransactionTemplate.execute(...)` por un método anotado con `@Transactional` (propagación controlada) o asegurar que la transacción se complete en todos los caminos.
   - Considerar `@Transactional(propagation = Propagation.REQUIRES_NEW)` para `executeOnce()` de forma que cada intento quede en su propia transacción independiente.
   - Si se mantiene el advisory lock, mover la adquisición del lock a un scope controlado y documentado; en caso de fallo, garantizar rollback explícito y no bloquear la conexión indefinidamente.

3. Añadir limpieza/rollback explícito en bloques de retry
   - Antes de reintentar, forzar rollback de la transacción previa y limpiar el estado de entidades (evitar reusar `EntityManager` con estado sucio).

4. Asegurar resiliencia del hilo asíncrono
   - Capturar excepciones en `EtlProcessingService.processFile()` y registrar/forzar cleanup en `finally`.

5. Ajustes operativos en Hikari (configuración)
   - `maximum-pool-size` acorde a la carga y env (por ejemplo 10-20)
   - `connection-timeout`, `max-lifetime`, `idle-timeout` y `leak-detection-threshold` configurados explícitamente.

6. Testing y validación
   - Reproducir con pool pequeño (`max-pool-size=3`) para identificar y exponer leaks rápidamente.
   - Ejecutar el flujo ETL en paralelismo alto y verificar que no crecen conexiones en uso.

---

## Snippets de ejemplo (no aplicar automáticamente)

### HikariCP (application.properties)

```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=30000
```

### Manejo transaccional sugerido (pseudocódigo)

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void executeOnce(LocalDate minDate, LocalDate maxDate, List<FactProduction> records) {
    // Acquire advisory lock via native query inside transaction
    try {
        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(?);")
            .setParameter(1, lockKey)
            .getSingleResult();
        // delete/insert logic
    } catch (RuntimeException ex) {
        // ensure transaction rollback by rethrowing or marking rollback
        throw ex;
    }
}
```

### Mejor captura en el hilo asíncrono

```java
@Async("etlExecutor")
public void processFile(MultipartFile file, String userId, UUID jobId) {
    try {
        dataSyncService.syncWithDeleteInsert(...);
    } catch (Exception e) {
        log.error("ETL failed for job {}: {}", jobId, e.getMessage(), e);
        // opcional: marcar job como FAILED
    }
}
```

---

## Plan de validación y pruebas

1. **Configurar detección de leak** y logs (ver apartado Hikari) y desplegar en staging.
2. **Reproducción controlada**:
   - Reducir `maximum-pool-size` a 3 y ejecutar múltiples jobs ETL en paralelo. Observar si aparecen warnings.
   - Revisar logs DEBUG de transacciones para ver begin/commit/rollback.
3. **Unit / Integration tests**:
   - Test que simula excepciones en medio del flujo y valida que no quedan conexiones abiertas.
   - Test con Testcontainers/Postgres que corre 10 ETL jobs secuenciales y verifica que el número de conexiones activas no crezca indefinidamente.
4. **Smoke test en staging**:
   - Ejecutar 5 jobs ETL consecutivos y confirmar que después de completados el uso de conexiones vuelve a niveles normales.

---

## Rollout y mitigación inmediata

- **Mitigación temporal**: si el servicio está en producción y el pool se está agotando, reiniciar el servicio liberará conexiones. Esto es un *mitigante*, no solución.
- **Deploy seguro**: aplicar primero cambios de configuración de Hikari y logging; luego desplegar los cambios transaccionales en una rama y validar en staging.

---

## Riesgos y consideraciones

- Forzar `REQUIRES_NEW` puede cambiar la semántica de las transacciones y debe ser probado con cuidado (por ejemplo, relación con auditorías o triggers que esperen atomicidad mayor).
- Mover o eliminar advisory locks puede reintroducir race conditions; si se quitan, hay que garantizar idempotencia y concurrencia segura.

---

## Conclusión

La evidencia apunta a que la fuga de conexiones ocurre por transacciones que no se cierran correctamente durante el flujo ETL (adquisición de advisory lock + retry + ejecución asíncrona). La solución propuesta combina: mejorar la configuración de Hikari para detectar y exponer el problema; refactorizar la gestión transaccional para usar `@Transactional` (o `REQUIRES_NEW`) y asegurar rollback/cleanup explícito en casos de excepción; y endurecer la gestión del advisory lock y la robustez del hilo asíncrono.

Si quieres, puedo:
- generar los parches de código propuestos (con tests) en una rama nueva, o
- preparar un PR con los cambios de configuración y la refactorización segura del método `executeOnce()`.

---

**Fin del informe**

