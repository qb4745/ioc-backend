package com.cambiaso.ioc.dto.analytics;

import java.math.BigDecimal;

/**
 * DTO para top máquinas por unidades producidas.
 * Ordenado descendente por totalUnidades.
 */
public record TopMachineDto(
    String maquinaNombre,
    String maquinaCodigo,
    BigDecimal totalUnidades,
    Integer numRegistros
) {}

