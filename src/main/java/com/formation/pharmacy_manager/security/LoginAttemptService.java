
package com.formation.pharmacy_manager.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service de protection contre les attaques par force brute sur /login.
 *
 * Règles :
 *  - Après 5 tentatives échouées pour un même userName,
 *    le compte est bloqué pendant 15 minutes.
 *  - Une connexion réussie réinitialise le compteur.
 *  - Stockage en mémoire (pour un déploiement mono-serveur).
 *
 * En production multi-instances, remplacer par Redis (avec expiration TTL).
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;

    /** Nombre de tentatives échouées par userName. */
    private final Map<String, Integer> attemptsByUsername = new ConcurrentHashMap<>();

    /** Date d'expiration du blocage par userName. */
    private final Map<String, LocalDateTime> lockExpirationByUsername = new ConcurrentHashMap<>();

    /**
     * Vérifie si un compte est actuellement bloqué.
     */
    public boolean isBlocked(String username) {
        LocalDateTime expiration = lockExpirationByUsername.get(username);
        if (expiration == null) {
            return false;
        }
        if (LocalDateTime.now().isAfter(expiration)) {
            // Le blocage a expiré, on nettoie
            lockExpirationByUsername.remove(username);
            attemptsByUsername.remove(username);
            return false;
        }
        return true;
    }

    /**
     * Enregistre une tentative échouée.
     * Si on atteint le seuil, on bloque le compte pour LOCK_DURATION_MINUTES.
     */
    public void loginFailed(String username) {
        int attempts = attemptsByUsername.getOrDefault(username, 0) + 1;
        attemptsByUsername.put(username, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            lockExpirationByUsername.put(username,
                    LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }
    }

    /**
     * Enregistre une connexion réussie : réinitialise le compteur.
     */
    public void loginSucceeded(String username) {
        attemptsByUsername.remove(username);
        lockExpirationByUsername.remove(username);
    }

    /**
     * Nombre de minutes restantes avant déblocage.
     */
    public long getRemainingLockMinutes(String username) {
        LocalDateTime expiration = lockExpirationByUsername.get(username);
        if (expiration == null) {
            return 0;
        }
        long minutes = Duration.between(LocalDateTime.now(), expiration).toMinutes();
        return Math.max(minutes, 1);
    }

    /**
     * Nombre de tentatives restantes avant blocage.
     */
    public int getRemainingAttempts(String username) {
        return Math.max(0, MAX_ATTEMPTS - attemptsByUsername.getOrDefault(username, 0));
    }
}