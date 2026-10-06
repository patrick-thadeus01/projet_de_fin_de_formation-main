package com.formation.pharmacy_manager.security;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.formation.pharmacy_manager.exceptions.ForbiddenException;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Utilitaire pour lire l'utilisateur connecté à partir du SecurityContext.
 * Le SecurityContext est rempli par JwtAuthenticationFilter à chaque requête.
 */
@Component
public class SecurityUtils {

    /**
     * Retourne le userName de l'utilisateur connecté.
     * Retourne null si personne n'est connecté (ne devrait pas arriver
     * car toutes les routes protégées exigent un token valide).
     */
    public String getCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }

    /**
     * Vérifie si l'utilisateur connecté possède le rôle ADMIN.
     */
    public boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie que l'utilisateur connecté est soit admin, soit le propriétaire
     * de la ressource. Sinon, lève une RuntimeException (capturée par le
     * GlobalExceptionHandler et transformée en 403).
     */
    public void checkOwnershipOrAdmin(String ownerUserName) {
        if (isCurrentUserAdmin()) {
            return;
        }
        String current = getCurrentUserName();
        if (current == null || !current.equals(ownerUserName)) {
            throw new ForbiddenException("Accès refusé : vous ne pouvez accéder qu'à vos propres données.");
        }
    }

}