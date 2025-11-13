package com.cambiaso.ioc.dto.ai;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Response DTO con explicación generada por IA para un dashboard.
 *
 * Estructura JSON esperada por el frontend:
 * - resumenEjecutivo: párrafo de 2-3 líneas
 * - keyPoints: lista de 3-5 puntos clave
 * - insightsAccionables: lista de insights con recomendaciones
 * - alertas: lista de alertas o problemas detectados
 * - metadata: información sobre generación y cache
 */
public record DashboardExplanationResponse(

    // Contenido generado por IA
    String resumenEjecutivo,
    List<String> keyPoints,
    List<String> insightsAccionables,
    List<String> alertas,

    // Metadata del dashboard analizado
    Integer dashboardId,
    String dashboardTitulo,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaInicio,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fechaFin,

    Map<String, String> filtrosAplicados,

    // Metadata de generación
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant generadoEn,

    Boolean fromCache,
    Integer tokensUsados,
    Integer cacheTTLSeconds
) {
    /**
     * Crea una copia de la respuesta marcando que viene de cache.
     */
    public DashboardExplanationResponse withFromCache(boolean cached) {
        return new DashboardExplanationResponse(
            resumenEjecutivo,
            keyPoints,
            insightsAccionables,
            alertas,
            dashboardId,
            dashboardTitulo,
            fechaInicio,
            fechaFin,
            filtrosAplicados,
            generadoEn,
            cached,
            tokensUsados,
            cacheTTLSeconds
        );
    }
}

