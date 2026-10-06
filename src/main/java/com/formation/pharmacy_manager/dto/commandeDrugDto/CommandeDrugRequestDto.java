package com.formation.pharmacy_manager.dto.commandeDrugDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommandeDrugRequestDto {

    @NotBlank(message = "Le pseudo de la commande est obligatoire.")
    private String pseudo;

    @NotBlank(message = "Le nom du médicament est obligatoire.")
    private String drugName;

    @Positive(message = "La quantité doit être strictement positive.")
    private int quantity;
}