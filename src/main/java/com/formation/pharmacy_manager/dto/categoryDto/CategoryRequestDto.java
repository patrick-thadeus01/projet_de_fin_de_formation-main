package com.formation.pharmacy_manager.dto.categoryDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class CategoryRequestDto {

    @NotBlank(message = "Le type de catégorie est obligatoire.")
    @Size(max = 50, message = "Le type de catégorie ne doit pas dépasser 50 caractères.")
    private String categoryType;

    @NotBlank(message = "Le nom de la catégorie est obligatoire.")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères.")
    private String categoryName;
}