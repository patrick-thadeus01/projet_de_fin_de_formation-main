package com.formation.pharmacy_manager.controller;

import com.formation.pharmacy_manager.dto.userDto.UserConnectDto;
import com.formation.pharmacy_manager.dto.userDto.UserConnectResponse;
import com.formation.pharmacy_manager.security.JwtUtil;
import com.formation.pharmacy_manager.security.LoginAttemptService;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserConnectDto request) {
        String username = request.getUsername();

        //1. Vérifier si le compte est actuellement bloqué
        if (loginAttemptService.isBlocked(username)) {
            Map<String, String> body = new HashMap<>();
            body.put("message", "Compte temporairement bloqué après trop de tentatives échouées. "
                    + "Réessayez dans " + loginAttemptService.getRemainingLockMinutes(username) + " minute(s).");
            return new ResponseEntity<>(body, HttpStatus.TOO_MANY_REQUESTS);  // 429
        }

        try {
            // 2. Tenter l'authentification
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );

            //3. Connexion réussie : réinitialiser le compteur
            loginAttemptService.loginSucceeded(username);

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replace("ROLE_", ""))
                    .toList();

            String token = jwtUtil.generateToken(authentication.getName(), roles);

            return ResponseEntity.ok(new UserConnectResponse(token, authentication.getName()));

        } catch (BadCredentialsException e) {
            //4. Échec : enregistrer la tentative
            loginAttemptService.loginFailed(username);

            int remaining = loginAttemptService.getRemainingAttempts(username);
            Map<String, String> body = new HashMap<>();
            if (remaining > 0) {
                body.put("message", "Identifiants invalides. "
                        + remaining + " tentative(s) restante(s) avant blocage.");
            } else {
                body.put("message", "Identifiants invalides. Compte bloqué pendant "
                        + loginAttemptService.getRemainingLockMinutes(username) + " minute(s).");
            }
            return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            Map<String, String> body = new HashMap<>();
            body.put("message", "Erreur d'authentification : " + e.getMessage());
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}