package com.formation.pharmacy_manager.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Erreurs de validation des DTOs (@Valid) -> 400 Bad Request.
     * Exemple : un champ obligatoire manquant.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Mauvais identifiants au login -> 401 Unauthorized.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", "Identifiants invalides");
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Problème technique pendant l'authentification (base injoignable, etc.) -> 500.
     * Le "detail" est utile en développement, à RETIRER en production (fuite d'info).
     */
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<Map<String, String>> handleInternalAuthenticationException(
            InternalAuthenticationServiceException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", "Erreur interne pendant l'authentification");
        body.put("detail", String.valueOf(ex.getMessage()));
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Optional.get() sur un élément absent -> 404 Not Found.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchElement(NoSuchElementException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", "Ressource introuvable");
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Capturer toutes les RuntimeException métier.
     * C'est le handler qui va débloquer nos messages clairs :
     *   - "Suppression impossible : médicament utilisé..."
     *   - "Stock insuffisant..."
     *   - "Quantité négative..."
     * On retourne un 400 Bad Request (erreur client, pas serveur).
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", ex.getMessage() != null ? ex.getMessage() : "Erreur de traitement");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /// ===========================
    /// Gestion des erreurs internes (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", "Une erreur interne est survenue. Contactez l'administrateur.");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    

    /**
     * Accès refusé (l'utilisateur essaie d'accéder aux données d'un autre) -> 403.
     */
    @ExceptionHandler(com.formation.pharmacy_manager.exceptions.ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(
            com.formation.pharmacy_manager.exceptions.ForbiddenException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }
    
}