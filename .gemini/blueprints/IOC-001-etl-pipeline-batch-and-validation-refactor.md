# Blueprint: Optimización y Robustecimiento del Pipeline de Ingesta de Datos ETL (v3 - Production Grade)

Esta guía detalla el proceso paso a paso para refactorizar el sistema de ingesta de datos a un modelo de procesamiento por lotes, añadiendo una capa de validación robusta y métricas de monitoreo.

## 1. Objetivo y Alcance
*   **Objetivo:** Transformar el pipeline de ETL para procesar archivos de gran tamaño de forma eficiente, detectar y segregar datos inválidos, y emitir métricas operativas para monitoreo.
*   **Stack Tecnológico Involucrado:** Spring Boot, Spring Data JPA, Jakarta Bean Validation, Micrometer.
*   **Contexto Adicional:** Refactorizar el ParserService actual para que procese archivos grandes de forma eficiente (sin agotar la memoria) y añadir una capa de validación explícita para detectar datos corruptos antes de llegar a la base de datos.

## 2. Prerrequisitos
*   **Dependencias:** `spring-boot-starter-validation`, `spring-boot-starter-actuator` (para Micrometer).
*   **Configuración:** Se añadirán nuevas propiedades en `application.properties` para controlar el ETL.
*   **Stack Principal:** Spring Boot + JPA + Jakarta Bean Validation

## 3. Implementación Paso a Paso

### 3.1. Configuración y Dependencias
*   **Archivo:** `pom.xml`
*   **Objetivo:** Asegurar que los módulos de validación y métricas estén disponibles.
*   **Código:**
    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    ```
*   **Archivo:** `src/main/resources/application.properties`
*   **Objetivo:** Externalizar la configuración del ETL para permitir ajustes sin recompilar.
*   **Código:**
    ```properties
    # ETL Configuration
    etl.batch.size=1000
    etl.validation.enabled=true
    ```

### 3.2. Creación de Nuevos Componentes
*   **Paso 1: Crear el Servicio de Métricas**
    *   **Archivo:** `src/main/java/com/cambiaso/ioc/service/EtlMetricsService.java`
    *   **Objetivo:** Centralizar la recolección de métricas operativas del ETL.
    *   **Código:**
        ```java
        package com.cambiaso.ioc.service;
        import io.micrometer.core.instrument.MeterRegistry;
        import lombok.RequiredArgsConstructor;
        import org.springframework.stereotype.Service;

        @Service
        @RequiredArgsConstructor
        public class EtlMetricsService {
            private final MeterRegistry meterRegistry;

            public void recordValidRecords(int count) {
                meterRegistry.counter("etl.records.processed", "status", "valid").increment(count);
            }

            public void recordQuarantinedRecords(int count) {
                meterRegistry.counter("etl.records.processed", "status", "quarantined").increment(count);
            }
        }
        ```

*   **Paso 2: Crear DTO de Resultado y Servicio de Validación**
    *   (El código para `ParseResult.java` y `ValidationService.java` permanece igual que en la v2)

### 3.3. Modificación de Componentes Existentes
*   **Paso 1: Anotar Entidad `FactProduction` con Reglas de Validación**
    *   (La implementación es la misma que en la v2)

*   **Paso 2: Refactorizar `ParserService` a `OptimizedParserService`**
    *   (La implementación es la misma que en la v2, con la adición de la lógica de validación y retorno de `ParseResult`)

*   **Paso 3: Adaptar `EtlProcessingService` para el Flujo Completo**
    *   **Archivo:** `src/main/java/com/cambiaso/ioc/service/EtlProcessingService.java`
    *   **Objetivo:** Orquestar el flujo completo, incluyendo la llamada al servicio de métricas.
    *   **Código (Fragmento Clave):**
        ```java
        // ... (inyecciones de dependencias para otros servicios, incluyendo EtlMetricsService) ...
        
        @Value("${etl.batch.size:1000}")
        private int batchSize;

        // ...
        ParseResult parseResult = parserService.parse(file.getInputStream());
        
        // Guardar y medir registros en cuarentena
        if (!parseResult.quarantinedRecords().isEmpty()) {
            quarantinedRecordRepository.saveAll(parseResult.quarantinedRecords());
            etlMetricsService.recordQuarantinedRecords(parseResult.quarantinedRecords().size());
        }

        // Procesar y medir registros válidos en lotes
        List<FactProduction> validRecords = parseResult.validRecords();
        Streams.partition(validRecords.stream(), batchSize).forEach(batch -> {
            dataSyncService.syncBatch(batch);
            etlMetricsService.recordValidRecords(batch.size());
            notifyProgress(userId, jobId, batch.size());
        });
        // ...
        ```

## 4. Testing y Validación
*   (Los tests definidos en la v2 siguen siendo válidos y se deben complementar con tests para `EtlMetricsService` usando `@Mock MeterRegistry`)

## 5. Flujo de Verificación
1.  Ejecutar `mvn test`.
2.  Iniciar la aplicación y subir un archivo CSV mixto.
3.  Verificar que los datos se persisten correctamente en `fact_production` y `quarantined_records`.
4.  **Nuevo:** Acceder al endpoint de Actuator (`/actuator/metrics/etl.records.processed`) y verificar que los contadores reflejan el número correcto de registros válidos y en cuarentena.

## 6. Consideraciones de Performance
*   **Monitoreo:** Las nuevas métricas (`etl.records.processed`) son fundamentales para crear dashboards de monitoreo y alertas. Permiten observar en tiempo real la tasa de éxito y error de los archivos procesados.

## 7. Consideraciones de Seguridad y Buenas Prácticas
*   **Configuración Externalizada:** Mover el `batch.size` a `application.properties` permite ajustar el rendimiento del ETL sin necesidad de redesplegar el código.

## 8. Troubleshooting Común
*   **Error:** El endpoint `/actuator/metrics` no está disponible o no muestra las métricas del ETL.
    *   **Causa:** Falta la dependencia `spring-boot-starter-actuator` en el `pom.xml` o la configuración de Actuator no expone el endpoint de métricas.
    *   **Solución:** Asegurarse de que la dependencia exista. Añadir `management.endpoints.web.exposure.include=metrics` a `application.properties` si es necesario.
