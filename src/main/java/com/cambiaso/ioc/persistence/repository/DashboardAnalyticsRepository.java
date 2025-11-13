package com.cambiaso.ioc.persistence.repository;

import com.cambiaso.ioc.dto.analytics.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Repositorio para consultas analíticas agregadas sobre producción.
 * Usa JDBC Template para queries optimizadas sin overhead JPA.
 *
 * BSS-001: DashboardAnalyticsRepository
 * Feature: FP-001A - Dashboard AI Explanation
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class DashboardAnalyticsRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Obtiene totales agregados para el rango de fechas especificado.
     *
     * @param fechaInicio Fecha inicial (inclusive)
     * @param fechaFin Fecha final (inclusive)
     * @return DTO con totales: registros, unidades, peso neto
     * @throws IllegalArgumentException si el rango excede 12 meses
     */
    public TotalsDto fetchTotals(LocalDate fechaInicio, LocalDate fechaFin) {
        validateDateRange(fechaInicio, fechaFin);

        log.debug("Fetching totals for range: {} to {}", fechaInicio, fechaFin);

        String sql = """
            SELECT 
                COUNT(DISTINCT fp.id) AS total_registros,
                COALESCE(SUM(fp.cantidad), 0) AS total_unidades,
                COALESCE(SUM(fp.peso_neto), 0) AS peso_neto_total
            FROM fact_production fp
            WHERE fp.fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
            """;

        Map<String, Object> params = Map.of(
            "fechaInicio", fechaInicio,
            "fechaFin", fechaFin
        );

        try {
            return jdbcTemplate.queryForObject(sql, params, (rs, rowNum) ->
                new TotalsDto(
                    rs.getLong("total_registros"),
                    rs.getBigDecimal("total_unidades"),
                    rs.getBigDecimal("peso_neto_total")
                )
            );
        } catch (EmptyResultDataAccessException e) {
            log.warn("No data found for range {} to {}", fechaInicio, fechaFin);
            return new TotalsDto(0L, BigDecimal.ZERO, BigDecimal.ZERO);
        } catch (DataAccessException e) {
            log.error("Database error fetching totals: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Obtiene top 10 operarios (maquinistas) por unidades producidas.
     *
     * @param fechaInicio Fecha inicial (inclusive)
     * @param fechaFin Fecha final (inclusive)
     * @return Lista ordenada DESC por unidades (máximo 10)
     */
    public List<TopOperarioDto> fetchTopOperarios(LocalDate fechaInicio, LocalDate fechaFin) {
        validateDateRange(fechaInicio, fechaFin);

        log.debug("Fetching top operarios for range: {} to {}", fechaInicio, fechaFin);

        String sql = """
            SELECT 
                dm.nombre_completo,
                dm.codigo_maquinista,
                COALESCE(SUM(fp.cantidad), 0) AS total_unidades,
                COUNT(DISTINCT fp.id) AS num_registros
            FROM fact_production fp
            JOIN dim_maquinista dm ON fp.maquinista_fk = dm.id
            WHERE fp.fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
            GROUP BY dm.id, dm.nombre_completo, dm.codigo_maquinista
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
                rs.getLong("codigo_maquinista"),
                rs.getBigDecimal("total_unidades"),
                rs.getInt("num_registros")
            )
        );
    }

    /**
     * Obtiene distribución de unidades por turno.
     *
     * @param fechaInicio Fecha inicial (inclusive)
     * @param fechaFin Fecha final (inclusive)
     * @return Lista con totales por turno (Día/Noche/Mixto)
     */
    public List<TurnoDistributionDto> fetchDistribucionTurno(LocalDate fechaInicio, LocalDate fechaFin) {
        validateDateRange(fechaInicio, fechaFin);

        log.debug("Fetching turno distribution for range: {} to {}", fechaInicio, fechaFin);

        String sql = """
            SELECT 
                fp.turno,
                COALESCE(SUM(fp.cantidad), 0) AS total_unidades,
                COUNT(DISTINCT fp.id) AS num_registros
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
                rs.getBigDecimal("total_unidades"),
                rs.getInt("num_registros")
            )
        );
    }

    /**
     * Obtiene top 10 máquinas por unidades producidas.
     *
     * @param fechaInicio Fecha inicial (inclusive)
     * @param fechaFin Fecha final (inclusive)
     * @return Lista ordenada DESC por unidades (máximo 10)
     */
    public List<TopMachineDto> fetchTopMaquinas(LocalDate fechaInicio, LocalDate fechaFin) {
        validateDateRange(fechaInicio, fechaFin);

        log.debug("Fetching top machines for range: {} to {}", fechaInicio, fechaFin);

        String sql = """
            SELECT 
                dm.nombre_maquina AS maquina_nombre,
                dm.codigo_maquina AS maquina_codigo,
                COALESCE(SUM(fp.cantidad), 0) AS total_unidades,
                COUNT(DISTINCT fp.id) AS num_registros
            FROM fact_production fp
            JOIN dim_maquina dm ON fp.maquina_fk = dm.id
            WHERE fp.fecha_contabilizacion BETWEEN :fechaInicio AND :fechaFin
            GROUP BY dm.id, dm.nombre_maquina, dm.codigo_maquina
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
                rs.getBigDecimal("total_unidades"),
                rs.getInt("num_registros")
            )
        );
    }

    /**
     * Obtiene tendencia diaria de producción.
     *
     * @param fechaInicio Fecha inicial (inclusive)
     * @param fechaFin Fecha final (inclusive)
     * @return Lista con puntos por día (fecha, unidades)
     */
    public List<DailyTrendPoint> fetchTendenciaDiaria(LocalDate fechaInicio, LocalDate fechaFin) {
        validateDateRange(fechaInicio, fechaFin);

        log.debug("Fetching daily trend for range: {} to {}", fechaInicio, fechaFin);

        String sql = """
            SELECT 
                fp.fecha_contabilizacion AS fecha,
                COALESCE(SUM(fp.cantidad), 0) AS total_unidades,
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
                rs.getBigDecimal("total_unidades"),
                rs.getInt("num_registros")
            )
        );
    }

    /**
     * Valida que el rango de fechas sea válido y no exceda 12 meses.
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @throws IllegalArgumentException si las validaciones fallan
     */
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
}

