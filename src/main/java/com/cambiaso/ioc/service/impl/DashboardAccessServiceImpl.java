package com.cambiaso.ioc.service.impl;

import com.cambiaso.ioc.config.MetabaseProperties;
import com.cambiaso.ioc.exception.DashboardAccessDeniedException;
import com.cambiaso.ioc.service.DashboardAccessService;
import com.cambiaso.ioc.service.DashboardAuditService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de verificación de acceso a dashboards.
 *
 * R4: Verificación centralizada de acceso
 * - Usa metabase.dashboards[*].allowed-roles como fuente de verdad
 * - Bypass automático para ROLE_ADMIN
 * - Cache de decisiones por 60s (configurado en @Cacheable)
 * - Auditoría mediante DashboardAuditService
 * - Métricas de accesos permitidos/denegados
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardAccessServiceImpl implements DashboardAccessService {

    private final MetabaseProperties metabaseProperties;
    private final DashboardAuditService auditService;
    private final MeterRegistry meterRegistry;

    @Override
    @Cacheable(value = "dashboardAccess", key = "#authentication.name + ':' + #dashboardId")
    public boolean canAccess(Authentication authentication, int dashboardId) {
        String username = authentication.getName();

        // Bypass para administradores
        Set<String> userRoles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

        if (userRoles.contains("ROLE_ADMIN")) {
            log.debug("Admin bypass - User: {} accessing dashboard: {}", username, dashboardId);
            auditService.logDashboardAccess(username, dashboardId, "Dashboard " + dashboardId, true);
            meterRegistry.counter("dashboard.access", "result", "granted", "reason", "admin").increment();
            return true;
        }

        // Buscar configuración del dashboard
        MetabaseProperties.DashboardConfig dashboardConfig = metabaseProperties.getDashboards().stream()
            .filter(d -> d.getId() == dashboardId)
            .findFirst()
            .orElse(null);

        if (dashboardConfig == null) {
            // Dashboard no configurado = acceso denegado (política segura)
            log.warn("Dashboard {} not found in configuration - denying access to user: {}",
                dashboardId, username);
            auditService.logDashboardAccess(username, dashboardId, null, false);
            meterRegistry.counter("dashboard.access", "result", "denied", "reason", "not_found").increment();
            return false;
        }

        // Verificar si el usuario tiene alguno de los roles requeridos
        Set<String> allowedRoles = dashboardConfig.getAllowedRoles();
        boolean hasAccess = userRoles.stream()
            .anyMatch(allowedRoles::contains);

        if (hasAccess) {
            log.debug("Access granted - User: {} has required role for dashboard: {} ({})",
                username, dashboardId, dashboardConfig.getName());
            auditService.logDashboardAccess(username, dashboardId, dashboardConfig.getName(), true);
            meterRegistry.counter("dashboard.access", "result", "granted", "reason", "role_match").increment();
        } else {
            log.warn("Access denied - User: {} lacks required roles for dashboard: {} ({}). User roles: {}, Required: {}",
                username, dashboardId, dashboardConfig.getName(), userRoles, allowedRoles);
            auditService.logDashboardAccess(username, dashboardId, dashboardConfig.getName(), false);
            meterRegistry.counter("dashboard.access", "result", "denied", "reason", "role_mismatch").increment();
        }

        return hasAccess;
    }

    @Override
    public void checkAccessOrThrow(Authentication authentication, int dashboardId) {
        if (!canAccess(authentication, dashboardId)) {
            throw new DashboardAccessDeniedException(
                String.format("Access denied to dashboard %d for user %s",
                    dashboardId, authentication.getName())
            );
        }
    }
}

