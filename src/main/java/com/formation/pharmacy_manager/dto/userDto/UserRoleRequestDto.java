package com.formation.pharmacy_manager.dto.userDto;

import com.formation.pharmacy_manager.enumEntities.Type;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserRoleRequestDto {

    @NotNull(message = "Le type de rôle est obligatoire.")
    Type type;

    @NotBlank(message = "L'email est obligatoire.")
    @Email(message = "L'email doit être valide.")
    String email;
}