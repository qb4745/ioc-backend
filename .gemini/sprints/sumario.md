# Sumario técnico – ioc-backend

Este documento resume el propósito del proyecto, componentes clave, prácticas de calidad aplicadas y recomendaciones de mejora.

## Resumen ejecutivo
Backend Spring Boot para orquestar procesos ETL: recibe archivos, valida, calcula hash/idempotencia, ejecuta carga transaccional (delete+insert por ventana), publica métricas y notificaciones en tiempo real, y protege endpoints con JWT y rate limiting estándar.

- Público objetivo: devs backend/frontend, SRE/ops, QA.
- Objetivos: confiabilidad del ETL, trazabilidad (job/status), observabilidad y seguridad por defecto.

## Stack y dependencias
- Lenguaje/Runtime: Java 21
- Framework: Spring Boot 3.5.5
- Persistencia: Spring Data JPA / Hibernate, PostgreSQL
- Seguridad: Spring Security (Resource Server, JWT)
- Observabilidad: Spring Boot Actuator, Micrometer, Prometheus
- Resiliencia: Resilience4j (Circuit Breaker, TimeLimiter)
- Tiempo real: WebSocket (STOMP)
- Rate limiting: Bucket4j (con Caffeine Cache)
- Utilidades: Lombok

## Arquitectura y componentes
- Controller
  - EtlController: expone endpoints para iniciar el proceso ETL y consultar estado.
- Services
  - EtlProcessingService: orquesta el pipeline ETL (validación, parseo, ventana de fechas, sincronización, métricas, notificaciones).
  - EtlJobService: gestión de entidad de jobs ETL (crear, actualizar estado, ventanas, locking de rango).
  - DataSyncService: operación transaccional de borrado + inserción para sincronizar datos (delete+insert en rango) con manejo de excepciones.
  - NotificationService: envío de notificaciones a usuarios y sistema (integración WebSocket).
- Interceptors
  - RateLimitInterceptor: control de tasa por usuario/IP en rutas /api/etl/**, devuelve headers estándar de rate limit.
- Configuración
  - SecurityConfig: CORS, autenticación JWT como Resource Server, reglas de autorización.
  - WebConfig: registro de interceptores.
  - WebSocketConfig y WebSocketSecurityConfig: broker STOMP y autenticación vía JWT en CONNECT.
  - MetricsConfig / ResilienceConfig / AsyncConfig / RateLimitingConfig: wiring para métricas, resiliencia, ejecución asíncrona y límites.
- Persistencia
  - Entidades y repositorios (ej.: FactProductionRepository) con operaciones concretas (deleteByFechaContabilizacionBetween, saveAll, flush).

## Flujo del proceso ETL
1. POST /api/etl/start-process
   - Validación de archivo (tamaño, extensión, content-type).
   - Cálculo de hash y verificación de idempotencia (no re-procesar el mismo archivo).
   - Creación de registro EtlJob y arranque de proceso asíncrono.
2. EtlProcessingService.processFile
   - Validación extra + métricas de tiempo (Timer Sample).
   - Parseo del archivo y extracción de rango de fechas y registros (placeholder actual; implementar real).
   - Actualización del rango del job y guardia de ventana (evitar solapes concurrentes).
   - Sincronización con DataSyncService.syncWithDeleteInsert(minDate, maxDate, records) en una transacción.
   - Notificaciones al usuario (con Circuit Breaker y fallback), métricas de éxito/fracaso.
3. GET /api/etl/jobs/{jobId}/status
   - Devuelve estado del job (DTO) o 404 si no existe.

## Contrato API (breve)
- Autenticación: Bearer JWT (Resource Server)
- Endpoints
  - POST /api/etl/start-process
    - Form-data: file (CSV). Respuesta 202 con { jobId, fileName, status }.
    - Errores: 400 (validación), 409 (conflicto idempotencia), 429 (rate limit), 401/403.
  - GET /api/etl/jobs/{jobId}/status
    - Respuesta 200 (EtlJobStatusDto) | 404.
- 429 ejemplo (JSON)
  - { "error": "Rate limit exceeded", "message": "Try again in N seconds", "retryAfter": N }
- Headers de rate limit (RFC 9331): RateLimit-Limit, RateLimit-Remaining, RateLimit-Reset, Retry-After

## Modelo de datos (resumen)
- EtlJob
  - jobId (UUID), fileName, fileHash (único), userId, status, createdAt, updatedAt, minDate, maxDate.
- FactProduction
  - Campos de producción; clave de negocio por fecha y atributos; fechaContabilizacion indexada.
- Relaciones: EtlJob no es dueño de FactProduction; el ETL borra/inserta por rango de fechas.
- Reglas: fileHash único para idempotencia; ventana [minDate, maxDate] no debe solaparse con jobs activos.

## Configuración y entornos
- Variables de entorno
  - SUPABASE_DB_PASSWORD, SUPABASE_JWT_ISSUER_URI
- Propiedades clave (application.properties)
  - spring.datasource.url/username/password
  - spring.jpa.hibernate.ddl-auto=update, show-sql=true (desarrollo)
  - management.endpoints.web.exposure.include=health,metrics,prometheus,info
  - CORS definido en SecurityConfig (orígenes locales por defecto)
- Perfiles sugeridos
  - local/dev: show-sql, logging detallado, orígenes CORS abiertos controlados
  - prod: DDL off, logs controlados, CORS restringido, métricas expuestas sólo internamente

## Observabilidad y resiliencia
- Micrometer + Prometheus: métricas de duración, contadores de éxito/fracaso, tamaños de archivo, errores de hash, fallback de notificaciones.
- Actuator: health, metrics, prometheus (expuestos en configuración).
- Resilience4j: Circuit Breaker en notificaciones con método de fallback.

## Seguridad y CORS
- Resource Server JWT con decodificación vía JWK (ES256).
- CORS: expone cabeceras estándar de rate limit (Retry-After, RateLimit-Limit, RateLimit-Remaining, RateLimit-Reset).
- WebSocket CONNECT autenticado (JWT en header Authorization), user seteado en accessor.
- Recomendaciones
  - DB: principio de mínimo privilegio (sólo CRUD necesario), rotación de credenciales, no exponer superusuario.
  - JWT: validar expiración, aud, iss; mapear claims a roles/permisos; caducidad coherente.
  - Secretos: inyectar vía variables/secret manager; rotación periódica.
  - CORS prod: orígenes explícitos, no wildcard; revisar preflight.

## Runbook operativo (resumen)
- Arranque
  - mvn spring-boot:run o contenedor. Comprobar /actuator/health.
- Verificación
  - /actuator/metrics, /actuator/prometheus, métricas etl.processing.duration.
- Fallos comunes y mitigación
  - 429: respetar Retry-After; revisar RateLimit-*.
  - 409: archivo ya procesado (fileHash); confirmar si debe re-procesarse y limpiar job.
  - Violaciones DB: revisar logs de flush(); corregir datos fuente y reintentar.
  - JWT inválido/expirado: renovar token; revisar reloj del servidor.
- Rollback/Idempotencia
  - Operación transaccional; en fallo, no se persiste inserción parcial. Reintentar con mismo archivo (hash).

## Rate limiting (HTTP)
- Bucket4j por usuario/IP.
- Respuestas 429 con headers estándar (RFC 9331): RateLimit-Limit, RateLimit-Remaining, RateLimit-Reset, Retry-After.
- CORS expone estas cabeceras para consumo en front-end.

## Estrategia de testing
- Unit: servicios y validaciones (DataSyncService, EtlJobService, NotificationService, etc.).
- Integración: endpoints /api/etl/** con seguridad, rate limit y CORS (pendiente ampliar).
- WebSocket: autenticación CONNECT y entrega de notificaciones (pendiente ampliar).
- Cobertura: priorizar rutas de error (validación archivo, conflictos, límites, fallos DB).
- Política flaky: aislar dependencias externas; retries limitados en tests inestables.

## SLIs/SLOs sugeridos
- Latencia ETL end-to-end (p50/p95): objetivo p95 < X min (definir por dominio/volumen).
- Tasa de error por job: objetivo < 1% semanal.
- Éxito de notificaciones: > 99% entregadas.
- Backlog de jobs pendientes: por debajo de umbral operativo.

## Calidad (estado actual resumido)
- Compilación: OK en archivos modificados.
- Lint estático: se corrigieron advertencias (nullability, constantes redundantes, cabeceras HTTP).
- Tests: existen pruebas unitarias de servicios y capa de persistencia (según reports en target/surefire-reports). Recomendado ampliar a integración HTTP/WebSocket.

## Recomendaciones de mejora
1. Implementar parser real (CSV/Excel) con streaming y validaciones de dominio; reportes de errores por fila.
2. Añadir tests de integración para RateLimitInterceptor y CORS (verificar headers expuestos y 429 JSON).
3. Unificar manejo de errores HTTP con @ControllerAdvice + problem+json.
4. Externalizar y parametrizar límites de rate limiting y tamaños de archivo.
5. Añadir métricas de negocio (registros insertados, filas inválidas, latencia de parseo por etapas).
6. Asegurar encoding UTF-8 en todo el pipeline (ficheros, BDD, logs) y validar locales.
7. Incorporar políticas de reintento (retry) idempotentes para notificaciones y/o sincronización si aplica.
8. Añadir RateLimit-Policy (opcional) para documentar ventana/ratio a clientes.
9. Definir SLOs acordados y alarmas (latencia, errores, backlog) en Prometheus/Alertmanager.

## Cómo ejecutar (local)
```bash
mvn -v
mvn clean verify
# Para arrancar la app
mvn spring-boot:run
```

Variables de entorno relevantes (ejemplos):
- SUPABASE_DB_PASSWORD, SUPABASE_JWT_ISSUER_URI

---
Este sumario se mantendrá vivo con los cambios del repositorio; conviene revisarlo al introducir nuevas integraciones o cambios de arquitectura.
