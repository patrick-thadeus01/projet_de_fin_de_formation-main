package com.formation.pharmacy_manager.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Compte administrateur (créé au démarrage par DataInitializer).
 * Même principe que Receptionist : une sous-classe de User sans champ supplémentaire.
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Administrator extends User {

}
