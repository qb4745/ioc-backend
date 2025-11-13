package com.cambiaso.ioc.dto.ai;

import java.util.List;

/**
 * Estructura interna esperada del JSON retornado por Gemini.
 *
 * Este DTO se usa solo internamente para parsear la respuesta
 * del LLM antes de construir el DashboardExplanationResponse final.
 */
public record GeminiJsonResponse(
    String resumenEjecutivo,
    List<String> keyPoints,
    List<String> insightsAccionables,
    List<String> alertas
) {}
