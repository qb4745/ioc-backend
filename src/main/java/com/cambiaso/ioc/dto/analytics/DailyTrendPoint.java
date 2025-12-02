package com.cambiaso.ioc.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para punto de tendencia diaria de producción.
 * Usado para gráficos de series de tiempo.
 */
public record DailyTrendPoint(
    LocalDate fecha,
    BigDecimal totalUnidades,
    Integer numRegistros
) {}

