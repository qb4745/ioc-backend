package com.cambiaso.ioc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardAuditService {
    
    // TODO: Inyectar un futuro AuditLogRepository cuando esté disponible para persistir los logs.
    // private final AuditLogRepository auditLogRepository;
    
    /**
     * Registra un evento de acceso a un dashboard.
     * @param username El nombre del usuario que intenta acceder.
     * @param dashboardId El ID del dashboard solicitado.
     * @param dashboardName El nombre del dashboard (si se conoce).
     * @param granted Si el acceso fue concedido o denegado.
     */
    public void logDashboardAccess(
            String username, 
            int dashboardId, 
            String dashboardName, 
            boolean granted
    ) {
        LocalDateTime timestamp = LocalDateTime.now();
        
        if (granted) {
            log.info("AUDIT: Dashboard access GRANTED - User: [{}], Dashboard ID: [{}], Dashboard Name: [{}], Timestamp: [{}]", 
                username, dashboardId, dashboardName, timestamp);
        } else {
            log.warn("AUDIT: Dashboard access DENIED - User: [{}], Dashboard ID: [{}], Timestamp: [{}]", 
                username, dashboardId, timestamp);
        }
        
        // TODO: Descomentar y adaptar cuando el repositorio de auditoría esté disponible.
        // AuditLog auditLog = new AuditLog(username, dashboardId, dashboardName, granted, timestamp);
        // auditLogRepository.save(auditLog);
    }
}
