package com.cambiaso.ioc.exception;

/**
 * Excepción lanzada cuando un usuario autenticado intenta acceder a un dashboard
 * para el cual no tiene los roles requeridos.
 * Será manejada por el GlobalExceptionHandler para devolver un 403 Forbidden.
 */
public class DashboardAccessDeniedException extends RuntimeException {
    public DashboardAccessDeniedException(String message) {
        super(message);
    }
}
