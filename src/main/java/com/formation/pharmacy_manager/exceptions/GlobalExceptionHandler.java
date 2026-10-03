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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Mauvais identifiants (BadCredentialsException, compte désactivé, etc.) -> 401 au lieu d'un 403 trompeur
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", "Identifiants invalides");
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    // Problème technique pendant l'authentification (base injoignable, table manquante...) -> 500 explicite.
    // Plus spécifique que AuthenticationException, donc prioritaire pour ce cas.
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<Map<String, String>> handleInternalAuthenticationException(InternalAuthenticationServiceException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", "Erreur interne pendant l'authentification");
        body.put("detail", String.valueOf(ex.getMessage()));
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Optional.get() sur un élément absent (findById(...).get()) -> 404 au lieu d'un 500
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchElement(NoSuchElementException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", "Ressource introuvable");
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}
