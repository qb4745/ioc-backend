package com.cambiaso.ioc.dto.ai;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.Map;

/**
 * Request DTO para solicitar explicación de dashboard con IA.
 *
 * Validaciones aplicadas:
 * - dashboardId debe ser positivo
 * - fechaInicio y fechaFin no pueden ser nulos
 * - Validación de rango máximo (12 meses) se realiza en Repository
 */
public record DashboardExplanationRequest(

    @NotNull(message = "dashboardId es requerido")
    @Positive(message = "dashboardId debe ser positivo")
    Integer dashboardId,

    @NotNull(message = "fechaInicio es requerida")
    LocalDate fechaInicio,

    @NotNull(message = "fechaFin es requerida")
    LocalDate fechaFin,

    Map<String, String> filtros
) {
    /**
     * Constructor con validación adicional.
     */
    public DashboardExplanationRequest {
        // Normalizar filtros nulos a Map vacío
        if (filtros == null) {
            filtros = Map.of();
        }
    }
}

