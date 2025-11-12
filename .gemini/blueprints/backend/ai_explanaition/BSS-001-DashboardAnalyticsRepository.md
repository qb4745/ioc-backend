# Backend Service Specification: DashboardAnalyticsRepository

## Metadata
- **BSS ID**: BSS-001
- **Technical Design**: TD-001A (Dashboard AI Explanation)
- **Feature Plan**: FP-001A
- **Clase**: `DashboardAnalyticsRepository`
- **Tipo**: Repository (JDBC Template)
- **Package**: `com.cambiaso.ioc.repository`
- **Sprint**: Sprint 2
- **Fecha Creación**: 2025-11-12
- **Estado**: DRAFT

---

## 1. Propósito y Responsabilidades

### 1.1. Propósito
Repositorio especializado para consultas analíticas agregadas sobre `fact_production` y dimensiones relacionadas. Provee datos pre-procesados para alimentar explicaciones de IA generadas por Gemini.

### 1.2. Responsabilidades
- Ejecutar 5 queries SQL agregadas definidas en `FP-001A-aggregations.sql`
- Mapear resultados SQL a DTOs tipados (Records Java)
- Validar rangos de fechas (máximo 12 meses)
- Manejar parámetros nombrados con seguridad
- Proporcionar datos optimizados (sin N+1, sin lazy loading issues)

### 1.3. Ubicación en Arquitectura
```
DashboardExplanationService
         ↓
   [DashboardAnalyticsRepository]
         ↓
    NamedParameterJdbcTemplate
         ↓
      PostgreSQL (fact_production + dimensiones)
```

---

## 2. Interfaz Pública

### 2.1. Signature Completa

```java
package com.cambiaso.ioc.repository;

import com.cambiaso.ioc.dto.analytics.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Repositorio para consultas analíticas agregadas sobre producción.
 * Usa JDBC Template para queries optimizadas sin overhead JPA.
 */
@Repository
public class DashboardAnalyticsRepository {

    /**
     * Obtiene totales agregados para el rango de fechas especificado.
     * 
     * @param fechaInicio Fecha inicial (inclusive)
     * @param fechaFin Fecha final (inclusive)
     * @return DTO con totales: unidades, fallos, defectos, eficiencia
     * @throws IllegalArgumentException si el rango excede 12 meses
     */
    public TotalsDto fetchTotals(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtiene top 10 operarios por unidades producidas.
     * 
     * @param fechaInicio Fecha inicial (inclusive)
     * @param fechaFin Fecha final (inclusive)
     * @return Lista ordenada DESC por unidades
     */
    public List<TopOperarioDto> fetchTopOperarios(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtiene distribución de unidades por turno.
     * 
     * @param fechaInicio Fecha inicial (inclusive)
     * @param fechaFin Fecha final (inclusive)
     * @return Lista con totales por turno (Día/Noche/Mixto)
     */
    public List<TurnoDistributionDto> fetchDistribucionTurno(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtiene top 10 máquinas por unidades producidas.
     * 
     * @param fechaInicio Fecha inicial (inclusive)
     * @param fechaFin Fecha final (inclusive)
     * @return Lista ordenada DESC por unidades
     */
    public List<TopMachineDto> fetchTopMaquinas(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtiene tendencia diaria de producción.
     * 
     * @param fechaInicio Fecha inicial (inclusive)
     * @param fechaFin Fecha final (inclusive)
     * @return Lista con puntos por día (fecha, unidades)
     */
    public List<DailyTrendPoint> fetchTendenciaDiaria(LocalDate fechaInicio, LocalDate fechaFin);
}
```

---

## 3. Dependencias Inyectadas

### 3.1. Constructor Injection

```java
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DashboardAnalyticsRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    
    // Métodos...
}
```

### 3.2. Grafo de Dependencias
```
DashboardAnalyticsRepository
    ↓ (requiere)
NamedParameterJdbcTemplate (Spring Bean)
    ↓
DataSource (PostgreSQL configurado)
```

---

## 4. Reglas de Negocio

### 4.1. Validaciones

| Regla | Implementación | Excepción |
|-------|----------------|-----------|
| Rango máximo 12 meses | `ChronoUnit.MONTHS.between(fi, ff) <= 12` | `IllegalArgumentException` |
| Fecha inicio <= Fecha fin | `fechaInicio.isAfter(fechaFin)` → error | `IllegalArgumentException` |
| Fechas no nulas | Bean Validation en DTO | - |

### 4.2. Invariantes
- Las queries siempre usan parámetros nombrados (`:fechaInicio`, `:fechaFin`) para prevenir SQL injection
- Los resultados son inmutables (Records)
- No se realizan operaciones de escritura (read-only)

---

## 5. Implementación Detallada

### 5.1. Método: fetchTotals

```java
public TotalsDto fetchTotals(LocalDate fechaInicio, LocalDate fechaFin) {
    validateDateRange(fechaInicio, fechaFin);
    
    log.debug("Fetching totals for range: {} to {}", fechaInicio, fechaFin);
    
    String sql = """
        SELECT 
            COUNT(DISTINCT fp.id) AS total_registros,
            SUM(fp.unidades_ok) AS total_unidades,
            SUM(fp.unidades_defectuosas) AS total_defectos,
            SUM(fp.unidades_fallo) AS total_fallos,
            ROUND(AVG(fp.eficiencia_porcentaje), 2) AS eficiencia_promedio
        FROM fact_production fp
        WHERE fp.fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
        """;
    
    Map<String, Object> params = Map.of(
        "fechaInicio", fechaInicio,
        "fechaFin", fechaFin
    );
    
    return jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> 
        new TotalsDto(
            rs.getLong("total_registros"),
            rs.getLong("total_unidades"),
            rs.getLong("total_defectos"),
            rs.getLong("total_fallos"),
            rs.getBigDecimal("eficiencia_promedio")
        )
    );
}
```

### 5.2. Método: fetchTopOperarios

```java
public List<TopOperarioDto> fetchTopOperarios(LocalDate fechaInicio, LocalDate fechaFin) {
    validateDateRange(fechaInicio, fechaFin);
    
    log.debug("Fetching top operarios for range: {} to {}", fechaInicio, fechaFin);
    
    String sql = """
        SELECT 
            do.nombre_completo,
            do.usuario_sap,
            SUM(fp.unidades_ok) AS total_unidades,
            COUNT(DISTINCT fp.id) AS num_registros,
            ROUND(AVG(fp.eficiencia_porcentaje), 2) AS eficiencia_promedio
        FROM fact_production fp
        JOIN dim_operario do ON fp.operario_id = do.id
        WHERE fp.fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
        GROUP BY do.id, do.nombre_completo, do.usuario_sap
        ORDER BY total_unidades DESC
        LIMIT 10
        """;
    
    Map<String, Object> params = Map.of(
        "fechaInicio", fechaInicio,
        "fechaFin", fechaFin
    );
    
    return jdbcTemplate.query(sql, params, (rs, rowNum) -> 
        new TopOperarioDto(
            rs.getString("nombre_completo"),
            rs.getString("usuario_sap"),
            rs.getLong("total_unidades"),
            rs.getInt("num_registros"),
            rs.getBigDecimal("eficiencia_promedio")
        )
    );
}
```

### 5.3. Método: fetchDistribucionTurno

```java
public List<TurnoDistributionDto> fetchDistribucionTurno(LocalDate fechaInicio, LocalDate fechaFin) {
    validateDateRange(fechaInicio, fechaFin);
    
    log.debug("Fetching turno distribution for range: {} to {}", fechaInicio, fechaFin);
    
    String sql = """
        SELECT 
            fp.turno,
            SUM(fp.unidades_ok) AS total_unidades,
            COUNT(DISTINCT fp.id) AS num_registros,
            ROUND(AVG(fp.eficiencia_porcentaje), 2) AS eficiencia_promedio
        FROM fact_production fp
        WHERE fp.fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
        GROUP BY fp.turno
        ORDER BY total_unidades DESC
        """;
    
    Map<String, Object> params = Map.of(
        "fechaInicio", fechaInicio,
        "fechaFin", fechaFin
    );
    
    return jdbcTemplate.query(sql, params, (rs, rowNum) -> 
        new TurnoDistributionDto(
            rs.getString("turno"),
            rs.getLong("total_unidades"),
            rs.getInt("num_registros"),
            rs.getBigDecimal("eficiencia_promedio")
        )
    );
}
```

### 5.4. Método: fetchTopMaquinas

```java
public List<TopMachineDto> fetchTopMaquinas(LocalDate fechaInicio, LocalDate fechaFin) {
    validateDateRange(fechaInicio, fechaFin);
    
    log.debug("Fetching top machines for range: {} to {}", fechaInicio, fechaFin);
    
    String sql = """
        SELECT 
            dm.nombre AS maquina_nombre,
            dm.codigo_sap AS maquina_codigo,
            SUM(fp.unidades_ok) AS total_unidades,
            COUNT(DISTINCT fp.id) AS num_registros,
            ROUND(AVG(fp.eficiencia_porcentaje), 2) AS eficiencia_promedio
        FROM fact_production fp
        JOIN dim_maquina dm ON fp.maquina_id = dm.id
        WHERE fp.fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
        GROUP BY dm.id, dm.nombre, dm.codigo_sap
        ORDER BY total_unidades DESC
        LIMIT 10
        """;
    
    Map<String, Object> params = Map.of(
        "fechaInicio", fechaInicio,
        "fechaFin", fechaFin
    );
    
    return jdbcTemplate.query(sql, params, (rs, rowNum) -> 
        new TopMachineDto(
            rs.getString("maquina_nombre"),
            rs.getString("maquina_codigo"),
            rs.getLong("total_unidades"),
            rs.getInt("num_registros"),
            rs.getBigDecimal("eficiencia_promedio")
        )
    );
}
```

### 5.5. Método: fetchTendenciaDiaria

```java
public List<DailyTrendPoint> fetchTendenciaDiaria(LocalDate fechaInicio, LocalDate fechaFin) {
    validateDateRange(fechaInicio, fechaFin);
    
    log.debug("Fetching daily trend for range: {} to {}", fechaInicio, fechaFin);
    
    String sql = """
        SELECT 
            fp.fecha_contabilizacion AS fecha,
            SUM(fp.unidades_ok) AS total_unidades,
            COUNT(DISTINCT fp.id) AS num_registros
        FROM fact_production fp
        WHERE fp.fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
        GROUP BY fp.fecha_contabilizacion
        ORDER BY fp.fecha_contabilizacion ASC
        """;
    
    Map<String, Object> params = Map.of(
        "fechaInicio", fechaInicio,
        "fechaFin", fechaFin
    );
    
    return jdbcTemplate.query(sql, params, (rs, rowNum) -> 
        new DailyTrendPoint(
            rs.getDate("fecha").toLocalDate(),
            rs.getLong("total_unidades"),
            rs.getInt("num_registros")
        )
    );
}
```

### 5.6. Método Helper: validateDateRange

```java
private void validateDateRange(LocalDate fechaInicio, LocalDate fechaFin) {
    if (fechaInicio == null || fechaFin == null) {
        throw new IllegalArgumentException("Fechas no pueden ser nulas");
    }
    
    if (fechaInicio.isAfter(fechaFin)) {
        throw new IllegalArgumentException(
            "fechaInicio debe ser <= fechaFin"
        );
    }
    
    long monthsBetween = ChronoUnit.MONTHS.between(fechaInicio, fechaFin);
    if (monthsBetween > 12) {
        throw new IllegalArgumentException(
            String.format("Rango de fechas excede el máximo permitido de 12 meses (actual: %d meses)", 
                monthsBetween)
        );
    }
}
```

---

## 6. DTOs (Records)

### 6.1. TotalsDto

```java
package com.cambiaso.ioc.dto.analytics;

import java.math.BigDecimal;

public record TotalsDto(
    Long totalRegistros,
    Long totalUnidades,
    Long totalDefectos,
    Long totalFallos,
    BigDecimal eficienciaPromedio
) {}
```

### 6.2. TopOperarioDto

```java
package com.cambiaso.ioc.dto.analytics;

import java.math.BigDecimal;

public record TopOperarioDto(
    String nombreCompleto,
    String usuarioSap,
    Long totalUnidades,
    Integer numRegistros,
    BigDecimal eficienciaPromedio
) {}
```

### 6.3. TurnoDistributionDto

```java
package com.cambiaso.ioc.dto.analytics;

import java.math.BigDecimal;

public record TurnoDistributionDto(
    String turno,
    Long totalUnidades,
    Integer numRegistros,
    BigDecimal eficienciaPromedio
) {}
```

### 6.4. TopMachineDto

```java
package com.cambiaso.ioc.dto.analytics;

import java.math.BigDecimal;

public record TopMachineDto(
    String maquinaNombre,
    String maquinaCodigo,
    Long totalUnidades,
    Integer numRegistros,
    BigDecimal eficienciaPromedio
) {}
```

### 6.5. DailyTrendPoint

```java
package com.cambiaso.ioc.dto.analytics;

import java.time.LocalDate;

public record DailyTrendPoint(
    LocalDate fecha,
    Long totalUnidades,
    Integer numRegistros
) {}
```

---

## 7. Manejo de Errores

### 7.1. Excepciones Lanzadas

| Excepción | Causa | Manejo |
|-----------|-------|--------|
| `IllegalArgumentException` | Validación rango fechas | Capturar en Service → 400 BAD_REQUEST |
| `EmptyResultDataAccessException` | Query sin resultados (fetchTotals) | Retornar DTO con valores 0 |
| `DataAccessException` | Error SQL/conectividad | Propagar → 500 INTERNAL_ERROR |

### 7.2. Logging de Errores

```java
try {
    return jdbcTemplate.queryForObject(sql, params, rowMapper);
} catch (EmptyResultDataAccessException e) {
    log.warn("No data found for range {} to {}", fechaInicio, fechaFin);
    return new TotalsDto(0L, 0L, 0L, 0L, BigDecimal.ZERO);
} catch (DataAccessException e) {
    log.error("Database error fetching totals: {}", e.getMessage(), e);
    throw e;
}
```

---

## 8. Performance

### 8.1. Optimizaciones
- Uso de índices existentes en `fact_production(fecha_contabilizacion)`
- LIMIT 10 en top queries para reducir transferencia
- Agregación en BD (no en memoria)
- Sin lazy loading / N+1 issues (JDBC directo)

### 8.2. Latencia Esperada
| Query | Rango Mensual | Rango 12 Meses |
|-------|---------------|----------------|
| fetchTotals | 20-50ms | 100-200ms |
| fetchTopOperarios | 30-80ms | 150-300ms |
| fetchTendenciaDiaria | 25-60ms | 120-250ms |

### 8.3. Consideraciones de Escalamiento
- Añadir índice compuesto `(fecha_contabilizacion, turno)` si filtros por turno se añaden
- Pre-agregación nocturna para rangos históricos (optimización futura)

---

## 9. Testing

### 9.1. Tests Unitarios

```java
@SpringBootTest
@Transactional
class DashboardAnalyticsRepositoryTest {

    @Autowired
    private DashboardAnalyticsRepository repository;
    
    @Test
    void fetchTotals_withValidRange_returnsTotals() {
        // Given
        LocalDate inicio = LocalDate.of(2025, 6, 1);
        LocalDate fin = LocalDate.of(2025, 6, 30);
        
        // When
        TotalsDto result = repository.fetchTotals(inicio, fin);
        
        // Then
        assertNotNull(result);
        assertTrue(result.totalUnidades() >= 0);
    }
    
    @Test
    void fetchTotals_exceedsMaxRange_throwsException() {
        // Given
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fin = LocalDate.of(2025, 6, 30);
        
        // When/Then
        assertThrows(IllegalArgumentException.class, 
            () -> repository.fetchTotals(inicio, fin));
    }
    
    @Test
    void fetchTopOperarios_returnsMax10() {
        // Given
        LocalDate inicio = LocalDate.of(2025, 6, 1);
        LocalDate fin = LocalDate.of(2025, 6, 30);
        
        // When
        List<TopOperarioDto> result = repository.fetchTopOperarios(inicio, fin);
        
        // Then
        assertTrue(result.size() <= 10);
        // Verify DESC order
        for (int i = 1; i < result.size(); i++) {
            assertTrue(result.get(i-1).totalUnidades() >= result.get(i).totalUnidades());
        }
    }
    
    @Test
    void fetchDistribucionTurno_groupsByTurno() {
        // Given
        LocalDate inicio = LocalDate.of(2025, 6, 1);
        LocalDate fin = LocalDate.of(2025, 6, 30);
        
        // When
        List<TurnoDistributionDto> result = repository.fetchDistribucionTurno(inicio, fin);
        
        // Then
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(t -> "Día".equals(t.turno()) || "Noche".equals(t.turno())));
    }
}
```

### 9.2. Cobertura Objetivo
- **Líneas**: 90%
- **Branches**: 85%
- **Métodos**: 100%

---

## 10. Observabilidad

### 10.1. Logging

```java
log.debug("Fetching totals for range: {} to {}", fechaInicio, fechaFin);
log.info("Query executed successfully - {} results", results.size());
log.warn("No data found for range {} to {}", fechaInicio, fechaFin);
log.error("Database error: {}", e.getMessage(), e);
```

### 10.2. Métricas (opcional)
- Counter: `analytics_queries_total{query_type=totals|top_operarios|...}`
- Timer: `analytics_query_duration{query_type=...}`

---

## 11. Seguridad

### 11.1. Prevención SQL Injection
- ✅ Uso exclusivo de parámetros nombrados (`:fechaInicio`, `:fechaFin`)
- ✅ No concatenación de strings en SQL
- ✅ Validación de inputs en método helper

### 11.2. Principio de Menor Privilegio
- Usuario BD debe tener permisos READ-ONLY en tablas fact/dim

---

## 12. Configuración

### 12.1. Properties

```properties
# Validación rango fechas (meses)
analytics.max-query-range-months=12

# Pool de conexiones (ya configurado en datasource)
spring.datasource.hikari.maximum-pool-size=10
```

---

## 13. Checklist de Implementación

- [ ] Crear clase `DashboardAnalyticsRepository` en package `com.cambiaso.ioc.repository`
- [ ] Crear DTOs (records) en package `com.cambiaso.ioc.dto.analytics`
- [ ] Implementar 5 métodos fetch con queries SQL del FP
- [ ] Implementar método `validateDateRange`
- [ ] Añadir logs debug/error apropiados
- [ ] Crear tests unitarios para cada método
- [ ] Verificar cobertura >= 85%
- [ ] Validar performance con datasets reales (profiling si necesario)
- [ ] Documentar JavaDoc en métodos públicos
- [ ] Code review por Tech Lead

---

## 14. Referencias

- **Technical Design**: TD-001A-dashboard-ai-explanation-A.md
- **SQL Queries**: FP-001A-aggregations.sql
- **Schema**: analisis_schema_public.sql

---

## 15. Notas de Implementación

### 15.1. Alternativas Consideradas
- **JPA con @Query**: Descartado por overhead innecesario
- **jOOQ**: Descartado por complejidad adicional
- **MyBatis**: Descartado; NamedParameterJdbcTemplate suficiente

### 15.2. Deuda Técnica
- Ninguna identificada para MVP

### 15.3. Extensibilidad Futura
- Añadir filtros dinámicos (turno, centro, máquina) → MapSqlParameterSource
- Soporte para agregaciones personalizadas
- Caché de resultados (nivel repositorio)

---

**Fin BSS-001**

