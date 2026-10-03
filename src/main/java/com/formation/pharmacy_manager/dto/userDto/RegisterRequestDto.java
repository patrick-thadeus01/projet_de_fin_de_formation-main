package com.formation.pharmacy_manager.dto.userDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corps de la requête d'inscription publique.
 * Pas de champ "role" : le rôle PATIENT est imposé côté serveur.
 */
public record RegisterRequestDto(
        @NotBlank(message = "Le nom d'utilisateur ne doit pas être vide.")
        @Size(max = 20, message = "Le nom d'utilisateur doit contenir au maximum 20 caractères.")
        String userName,

        @NotBlank(message = "Le numéro de téléphone est requis.")
        @Size(min = 9, max = 12, message = "Le numéro doit contenir entre 9 et 12 chiffres.")
        String phoneNumber,

        @NotBlank(message = "L'email est requis.")
        @Email(message = "L'email doit être valide.")
        String email,

        @NotBlank(message = "Le mot de passe est requis.")
        @Size(min = 12, message = "Le mot de passe doit contenir au moins 12 caractères.")
        String password,

        @Min(value = 0, message = "L'âge doit être positif.")
        int age
) {
}
