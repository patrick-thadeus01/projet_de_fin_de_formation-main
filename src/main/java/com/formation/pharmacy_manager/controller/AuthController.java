package com.formation.pharmacy_manager.controller;

import com.formation.pharmacy_manager.dto.userDto.UserConnectDto;
import com.formation.pharmacy_manager.dto.patientDto.PatientRequestDto;
import com.formation.pharmacy_manager.dto.userDto.RegisterRequestDto;
import com.formation.pharmacy_manager.dto.userDto.UserConnectResponse;
import com.formation.pharmacy_manager.enumEntities.Type;
import com.formation.pharmacy_manager.repository.UserRepository;
import com.formation.pharmacy_manager.services.servicePatient.PatientService;
import jakarta.validation.Valid;
import com.formation.pharmacy_manager.security.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
public class AuthController {

    private final PatientService patientService; // Création du compte patient
    private final UserRepository userRepository; // Contrôle des doublons
    private final AuthenticationManager authenticationManager; // Gère l’authentification
    private final JwtUtil jwtUtil; // Utilitaire JWT



    @PostMapping("/login") // Endpoint POST /login
    public ResponseEntity<?> login(@RequestBody UserConnectDto request) {
        // Crée un token UsernamePassword pour vérifier les identifiants
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Si authentification réussie
        if (authentication.isAuthenticated()) {
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replace("ROLE_", "")) // Nettoie le prefix "ROLE_" si nécessaire
                    .toList();

            // Génère un JWT pour l’utilisateur
            String token = jwtUtil.generateToken(request.getUsername(),roles);

            // Retourne le token + username dans une réponse JSON
            return ResponseEntity.ok(new UserConnectResponse(token, authentication.getName()));
        } else {
            // Sinon renvoie 401 Unauthorized
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    // Inscription publique : le compte créé est TOUJOURS un patient (rôle non choisi par le client)
    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto request) {
        if (userRepository.findDistinctByUserName(request.userName()) != null
                || userRepository.findDistinctByEmail(request.email()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Nom d'utilisateur ou email déjà utilisé"));
        }
        PatientRequestDto dto = new PatientRequestDto(
                request.userName(),
                request.phoneNumber(),
                request.email(),
                request.password(),
                request.age(),
                Type.PATIENT.name()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.createPatient(dto));
    }
}