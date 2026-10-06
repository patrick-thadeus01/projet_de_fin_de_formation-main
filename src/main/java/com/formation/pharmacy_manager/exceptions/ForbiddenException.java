package com.formation.pharmacy_manager.exceptions;

/**
 * Exception métier pour signaler un accès refusé (403 Forbidden).
 * Différente d'une RuntimeException classique : le handler la mappe en 403.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
} 