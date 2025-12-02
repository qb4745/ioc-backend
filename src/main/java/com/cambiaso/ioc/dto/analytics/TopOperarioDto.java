package com.cambiaso.ioc.dto.analytics;

import java.math.BigDecimal;

/**
 * DTO para top operarios (maquinistas) por unidades producidas.
 * Ordenado descendente por totalUnidades.
 */
public record TopOperarioDto(
    String nombreCompleto,
    Long codigoMaquinista,
    BigDecimal totalUnidades,
    Integer numRegistros
) {}
