package com.mercadolocalia.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mercadolocalia.security.jwt.JwtAuthenticationFilter;
import com.mercadolocalia.services.impl.UserDetailsServiceImpl;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    // ============================
    // 1) PASSWORD ENCODER (BCrypt)
    // ============================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ============================
    // 2) SECURITY FILTER CHAIN
    // ============================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // RUTAS PÚBLICAS
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/api-docs/**").permitAll()

                    // RUTAS PRIVADAS POR ROL
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/vendedor/**").hasRole("VENDEDOR")
                    .requestMatchers("/consumidor/**").hasRole("CONSUMIDOR")

                    // 🔵 NUEVO: cualquier usuario logueado puede acceder
                    .requestMatchers("/usuarios/**").authenticated()

                    // cualquier otra ruta requiere token
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    // ============================
    // 3) AUTHENTICATION MANAGER
    // ============================
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
