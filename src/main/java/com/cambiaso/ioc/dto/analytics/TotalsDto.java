package com.cambiaso.ioc.dto.analytics;

import java.math.BigDecimal;

/**
 * DTO con totales agregados de producción.
 * Usado por DashboardAnalyticsRepository para consultas analíticas.
 */
public record TotalsDto(
    Long totalRegistros,
    BigDecimal totalUnidades,
    BigDecimal pesoNetoTotal
) {}
