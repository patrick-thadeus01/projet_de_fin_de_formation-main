package com.formation.pharmacy_manager.configurations;

import com.formation.pharmacy_manager.entities.Administrator;
import com.formation.pharmacy_manager.entities.Role;
import com.formation.pharmacy_manager.enumEntities.Type;
import com.formation.pharmacy_manager.repository.RoleRepository;
import com.formation.pharmacy_manager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Initialisation au démarrage (idempotente : peut être relancée sans créer de doublons).
 *  1. crée les rôles manquants (un par valeur de l'enum Type) ;
 *  2. crée un administrateur initial si app.admin.password est renseigné.
 *
 * Toute erreur est journalisée mais n'empêche pas l'application de démarrer.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String adminUsername;
    private final String adminEmail;
    private final String adminPhone;
    private final String adminPassword;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.username:admin}") String adminUsername,
                           @Value("${app.admin.email:admin@pharmacy.local}") String adminEmail,
                           @Value("${app.admin.phone:690000000}") String adminPhone,
                           @Value("${app.admin.password:}") String adminPassword) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail;
        this.adminPhone = adminPhone;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        try {
            seedRoles();
        } catch (Exception e) {
            log.error("Initialisation des rôles impossible : {}", e.getMessage(), e);
            return; // sans rôles, inutile de créer l'admin
        }
        try {
            seedAdmin();
        } catch (Exception e) {
            log.error("Création de l'administrateur initial impossible : {}", e.getMessage(), e);
        }
    }

    private void seedRoles() {
        Set<Type> existing = roleRepository.findAll().stream()
                .map(Role::getType)
                .collect(Collectors.toSet());
        for (Type type : Type.values()) {
            if (!existing.contains(type)) {
                Role role = new Role();
                role.setType(type);
                roleRepository.save(role);
                log.info("Rôle créé : {}", type);
            }
        }
    }

    private void seedAdmin() {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("Aucun administrateur initial créé : définir app.admin.password "
                    + "(variable d'environnement APP_ADMIN_PASSWORD) pour en créer un.");
            return;
        }
        if (userRepository.findDistinctByUserName(adminUsername) != null) {
            log.info("Administrateur '{}' déjà présent, rien à faire.", adminUsername);
            return;
        }
        Role adminRole = roleRepository.findAll().stream()
                .filter(r -> r.getType() == Type.ADMIN)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Rôle ADMIN introuvable"));

        Administrator admin = new Administrator();
        admin.setUserName(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPhoneNumber(adminPhone);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setCreation_date(LocalDate.now());
        admin.setUpdate_date(new Date());
        admin.getRoles().add(adminRole);
        userRepository.save(admin);
        log.info("Administrateur initial '{}' créé.", adminUsername);
    }
}
