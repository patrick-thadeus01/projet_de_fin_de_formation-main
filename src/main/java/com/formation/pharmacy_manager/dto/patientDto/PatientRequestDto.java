package com.formation.pharmacy_manager.dto.patientDto;

import com.formation.pharmacy_manager.dto.userDto.UserRequestDto;

import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class PatientRequestDto extends UserRequestDto {

    @Min(value = 0, message = "L'âge doit être positif ou nul.")
    private int age;

    public PatientRequestDto(String userName, String phoneNumber, String email,
                             String password, int age, String role) {
        super(userName, phoneNumber, email, password, role);
        this.age = age;
    }
}