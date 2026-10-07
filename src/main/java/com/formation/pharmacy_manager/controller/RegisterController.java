package com.formation.pharmacy_manager.controller;

import com.formation.pharmacy_manager.dto.patientDto.PatientRequestDto;
import com.formation.pharmacy_manager.dto.patientDto.PatientResponseDto;
import com.formation.pharmacy_manager.services.servicePatient.PatientService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur PUBLIC d'inscription.
 * N'importe qui peut créer un compte PATIENT sans être admin.
 */
@RestController
@RequestMapping("/register")
@AllArgsConstructor
public class RegisterController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientResponseDto> register(@Valid @RequestBody PatientRequestDto dto) {
        // Force le rôle à PATIENT (personne ne peut s'inscrire en ADMIN)
        PatientRequestDto safeDto = new PatientRequestDto(
                dto.getUserName(),
                dto.getPhoneNumber(),
                dto.getEmail(),
                dto.getPassword(),
                dto.getAge(),
                "PATIENT"
        );
        PatientResponseDto created = patientService.createPatient(safeDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}