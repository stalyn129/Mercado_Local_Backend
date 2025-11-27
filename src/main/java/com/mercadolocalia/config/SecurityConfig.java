package com.mercadolocalia.config;

import com.mercadolocalia.security.jwt.JwtAuthenticationFilter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;



import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5175"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
                config.setAllowCredentials(true);
                return config;
            }))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

            	    // ==================== PUBLICO =====================
            	    .requestMatchers("/auth/**", "/uploads/**").permitAll()
            	    .requestMatchers("/categorias/listar", "/subcategorias/listar").permitAll()

            	    // 🔥 PERMITIR TODOS LOS GET DE PRODUCTOS (lista + detalle + imágenes)
            	    .requestMatchers(HttpMethod.GET, "/productos/**").permitAll()

            	    // ==================== VENDEDOR =====================
            	    .requestMatchers(
            	        "/productos/crear",
            	        "/productos/editar/**",
            	        "/productos/eliminar/**",
            	        "/productos/estado/**",
            	        "/productos/vendedor/**"
            	    ).hasAuthority("VENDEDOR")

            	    // ==================== ADMIN =====================
            	    .requestMatchers("/admin/**").hasAuthority("ADMIN")

            	    // ==================== CONSUMIDOR =====================
            	    .requestMatchers("/consumidor/**").hasAuthority("CONSUMIDOR")

            	    .anyRequest().authenticated()
            	)


            // 🔥 EXCLUSIÓN: JWT NO SE EJECUTA PARA RUTAS PUBLICAS
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
