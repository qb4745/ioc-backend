# **Diseño de Arquitectura: Sistema de Ingesta y Sincronización Dimensional**

**Proyecto:** Inteligencia Operacional Cambiaso (IOC)
**Fecha:** 20 de Septiembre de 2025
**Versión:** 7.2 (Final & Consolidated)

#### **1. Resumen ejecutivo**
La arquitectura implementa un pipeline de ingesta asincrónico, idempotente y transaccional, modelado en **Esquema de Estrella**. Se basa en un particionado anual de la tabla de hechos, una clave de negocio única para integridad a nivel de fila y una capa de gobernanza de jobs para una trazabilidad completa. Se ofrecen dos estrategias de sincronización (Delete+Insert y UPSERT) para asegurar la flexibilidad a largo plazo.

#### **2. Objetivos y RNF**
*   **Rendimiento:** Procesamiento en segundo plano, inserciones en lote y consultas aceleradas por particionamiento e índices.
*   **Fiabilidad e Integridad:** Transacciones atómicas, validaciones, checks SQL, e idempotencia multinivel (archivo, ventana, fila).
*   **Usabilidad y Observabilidad:** Respuesta `202 Accepted` inmediata con seguimiento por `jobId` vía WebSocket y API REST.

#### **3. Modelo de Datos (Fuente de Verdad: `schema.sql`)**
El diseño completo y detallado de la base de datos, incluyendo tablas, tipos de datos, particionamiento, índices y constraints, está definido en el archivo **`schema.sql`**, que es la única fuente de verdad para la estructura de datos.

*   **Esquema:** Modelo Dimensional de Kimball (Esquema de Estrella).
*   **Tabla de Hechos:** `fact_production`, particionada anualmente sobre `fecha_contabilizacion` y con una clave de negocio única para garantizar la integridad.
*   **Tablas de Dimensiones:** `dim_maquina`, `dim_maquinista`, que contienen los atributos descriptivos.
*   **Tablas de Gobernanza:** `etl_jobs` (con unicidad por `file_hash`) y `quarantined_records` para auditoría y manejo de errores.

#### **4. Vista Lógica y Flujo**
- **API REST** recibe el archivo, valida duplicados y concurrencia, registra el `etl_job` con estado `INICIADO`, y devuelve `202 Accepted` con el `jobId`.
- Un **Orquestador Asíncrono** (`@Async`) ejecuta el parseo, la resolución de dimensiones ("find-or-create"), y delega la sincronización a un servicio transaccional.
- Las notificaciones **STOMP por `jobId`** reportan el progreso en tiempo real. Un endpoint **`GET /api/etl/jobs/{jobId}/status`** sirve como mecanismo de consulta de estado.

#### **5. Componentes**
- **`EtlController`:** `POST /api/etl/start-process`, `GET /api/etl/jobs/{jobId}/status`.
- **`EtlProcessingService` (`@Async("etlExecutor")`):** Orquestación, guardas de concurrencia y emisión de eventos.
- **`DataSyncService` (`@Transactional`):** `syncWithDeleteInsert(...)` (default) y `syncWithUpsert(...)` (opcional).
- **`EtlJobService`:** Gestión del ciclo de vida de `etl_jobs` y lógica de "job guards".
- **Servicios de Dimensión:** Lógica "find-or-create" (ej. `DimMaquinaService`).
- **`NotificationService`:** Publicación de eventos STOMP.

#### **6. Concurrencia y Guardas**
- **Job Guard por Ventana:** Se implementa una guarda lógica en el `EtlJobService` que previene el inicio de jobs con rangos de fecha superpuestos a otros que ya estén en un estado activo, respondiendo con `409 Conflict`.

#### **7. Estrategias de Sincronización**
- **Default (MVP):** "Delete+Insert" transaccional por ventana. Simple, robusto e idempotente a nivel de rango.
- **Alternativa Escalable:** "UPSERT" (`INSERT ... ON CONFLICT DO UPDATE`) por clave de negocio. Se puede activar por configuración (`app.etl.sync-mode=UPSERT`) para optimizar cargas incrementales o de gran volumen.

#### **8. API y Contrato**
- **`POST /api/etl/start-process` (multipart):** Devuelve `202 Accepted` con `{ "jobId": "..." }`.
- **`GET /api/etl/jobs/{jobId}/status`:** Devuelve un DTO con el estado, contadores de progreso, timestamps y mensajes.
- **WebSocket `/user/topic/etl-jobs/{jobId}`:** Emite eventos de estado (`INICIADO`, `SINCRONIZANDO`, `EXITO`, `FALLO`).

#### **9. Observabilidad y Seguridad**
- **Métricas:** Capturar latencias por etapa, filas procesadas/segundo y tamaño de la cola del `ThreadPoolTaskExecutor`.
- **Logs Estructurados (MDC):** Incluir `jobId`, `fileHash` y `userId` para correlación de trazas.
- **Seguridad:** Autorización por rol, límites de tamaño de archivo y validación de MIME type.

#### **10. Configuración Asíncrona (Esqueleto)**
Se define un `ThreadPoolTaskExecutor` explícito en una clase `@Configuration` para controlar los recursos del proceso ETL y desacoplarlo del pool de hilos de la API web.
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "etlExecutor")
    public ThreadPoolTaskExecutor etlExecutor() { /* ... configuración ... */ }
}
```

#### **11. Sincronización (Esqueleto)**
El `DataSyncService` encapsula las dos estrategias de persistencia.
```java
@Service
public class DataSyncService {
    @Transactional
    public void syncWithDeleteInsert(...) { /* ... */ }
    
    @Transactional
    public void syncWithUpsert(...) { /* ... */ }
}
```

#### **12. Estrategia de Pruebas**
- **Unitarias:** Lógica de negocio pura (validación, parseo, transiciones de estado).
- **Integración:** Repositorios (`@DataJpaTest`), servicios transaccionales, y endpoints (`@SpringBootTest` con `MockMvc`).
- **E2E:** Flujo completo validando la respuesta `202`, las notificaciones WebSocket, la consulta de estado por API, la idempotencia y el control de concurrencia (`409 Conflict`).

---
