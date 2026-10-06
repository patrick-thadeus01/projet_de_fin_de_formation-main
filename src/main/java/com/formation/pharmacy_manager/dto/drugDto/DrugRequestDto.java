package com.formation.pharmacy_manager.dto.drugDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DrugRequestDto {

    @NotBlank(message = "Le nom du médicament est obligatoire.")
    @Size(max = 100, message = "Le nom du médicament ne doit pas dépasser 100 caractères.")
    private String drugName;

    @NotBlank(message = "La description est obligatoire.")
    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères.")
    private String drugDescription;

    @NotNull(message = "La date de péremption est obligatoire.")
    private LocalDate peremption;

    @Positive(message = "Le prix doit être strictement positif.")
    private double price;

    @NotBlank(message = "Le type de catégorie est obligatoire.")
    private String type;
}