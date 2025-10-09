package com.cambiaso.ioc.exception;

/**
 * Excepción lanzada cuando se solicita un dashboard que no está configurado
 * o no existe.
 * Será manejada por el GlobalExceptionHandler para devolver un 404 Not Found.
 */
public class DashboardNotFoundException extends RuntimeException {
    public DashboardNotFoundException(String message) {
        super(message);
    }
}
