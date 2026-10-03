package com.formation.pharmacy_manager.configurations;

import com.formation.pharmacy_manager.security.JwtAuthenticationFilter;
import com.formation.pharmacy_manager.services.userService.CustomUserService;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques
                        // /error doit être public : sinon toute erreur interne (400/404/500) est masquée par un 403
                        .requestMatchers("/login", "/register", "/api/auth/**", "/error").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html"
                        ).permitAll()
                        // ===== Routes protégées par rôle =====
                        // IMPORTANT : Spring applique la PREMIÈRE règle qui correspond, donc les règles
                        // les plus précises doivent rester AU-DESSUS des règles générales (/**).

                        // Gestion des rôles : administrateurs uniquement
                        .requestMatchers("/api/role/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/roleServiceUser/add", "/api/roleServiceUser/delete").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/roleServiceUser/{email}").hasRole("ADMIN")

                        // Patients : un patient peut consulter une fiche ; créer, lister et supprimer = ADMIN
                        // (l'inscription publique passe par POST /api/auth/register)
                        .requestMatchers(HttpMethod.GET, "/api/patient/{id}").hasAnyRole("PATIENT", "ADMIN")
                        .requestMatchers("/api/patient/**").hasRole("ADMIN")

                        // Distributeurs : créer et supprimer = ADMIN ; le reste = DISTRIBUTOR ou ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/distributor/create").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/distributor/**").hasRole("ADMIN")
                        .requestMatchers("/api/distributor/**").hasAnyRole("DISTRIBUTOR", "ADMIN")

                        // Catalogue : lecture pour tout utilisateur connecté, modification réservée
                        .requestMatchers(HttpMethod.GET, "/api/drug/**", "/api/category/**", "/api/drugline/**").authenticated()
                        .requestMatchers("/api/drug/**", "/api/category/**").hasAnyRole("PHARMACIST", "ADMIN")
                        .requestMatchers("/api/drugline/**").hasAnyRole("DISTRIBUTOR", "ADMIN")

                        // Commandes, lignes de commande, paiements, factures
                        .requestMatchers(HttpMethod.DELETE, "/api/payment/**").hasRole("ADMIN")
                        .requestMatchers("/api/command/**", "/api/commandDrug/**", "/api/payment/**", "/api/bill/**")
                                .hasAnyRole("PATIENT", "ADMIN")
                        // Toutes les autres requêtes nécessitent une authentification
                        .anyRequest().authenticated()
                )
                // Réponses JSON explicites : 401 = non authentifié, 403 = rôle insuffisant
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Authentification requise, ou token invalide/expiré"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN,
                                        "Accès refusé : rôle insuffisant pour cette ressource"))
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // LA LIGNE MANQUANTE : on branche le provider d'authentification
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private static void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"message\":\"" + message.replace("\"", "'") + "\"}");
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}