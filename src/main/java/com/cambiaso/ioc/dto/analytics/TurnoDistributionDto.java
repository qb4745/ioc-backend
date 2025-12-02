package com.cambiaso.ioc.dto.analytics;

import java.math.BigDecimal;

/**
 * DTO para distribución de producción por turno.
 * Agrupa totales por turno (Día/Noche/Mixto).
 */
public record TurnoDistributionDto(
    String turno,
    BigDecimal totalUnidades,
    Integer numRegistros
) {}

