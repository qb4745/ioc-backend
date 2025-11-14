package com.cambiaso.ioc.service;

import org.springframework.security.core.Authentication;

/**
 * Servicio para verificar acceso a dashboards basado en roles.
 *
 * R4: Verificación centralizada de acceso a dashboards
 * - Consulta configuración de metabase.dashboards[*].allowed-roles
 * - Bypass para ROLE_ADMIN
 * - Cache de decisiones (60s)
 * - Auditoría de accesos denegados
 */
public interface DashboardAccessService {

    /**
     * Verifica si el usuario autenticado tiene acceso al dashboard especificado.
     *
     * @param authentication Usuario autenticado (con roles)
     * @param dashboardId ID del dashboard a verificar
     * @return true si el usuario tiene acceso, false en caso contrario
     */
    boolean canAccess(Authentication authentication, int dashboardId);

    /**
     * Verifica acceso y lanza excepción si denegado.
     *
     * @param authentication Usuario autenticado
     * @param dashboardId ID del dashboard
     * @throws com.cambiaso.ioc.exception.DashboardAccessDeniedException si acceso denegado
     */
    void checkAccessOrThrow(Authentication authentication, int dashboardId);
}

