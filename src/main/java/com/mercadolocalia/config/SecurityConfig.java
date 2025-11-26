package com.mercadolocalia.config;

import com.mercadolocalia.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            // Permitir CORS con tu configuración de WebMvcConfigurer
            .cors(Customizer.withDefaults())

            // ❌ Desactivar CSRF porque usas API REST
            .csrf(csrf -> csrf.disable())

            // JWT = Sin sesiones
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                /* ========================================
                   🟩 SWAGGER (esto permite que cargue)
                   ======================================== */
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api-docs/**"
                ).permitAll()

                /* ========================================
                   🟩 ARCHIVOS (imágenes estáticas)
                   ======================================== */
                .requestMatchers("/uploads/**").permitAll()

                /* ========================================
                   🟩 RUTAS PÚBLICAS
                   ======================================== */
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/categorias/**").permitAll()
                .requestMatchers("/subcategorias/**").permitAll()
                .requestMatchers("/productos/todos").permitAll()
                .requestMatchers("/productos/subcategoria/**").permitAll()

                /* ========================================
                   🟦 PRODUCTOS — SOLO VENDEDORES
                   ======================================== */
                .requestMatchers(
                        "/productos/crear",
                        "/productos/actualizar/**",
                        "/productos/eliminar/**",
                        "/productos/estado/**",
                        "/productos/vendedor/**"
                ).hasAuthority("VENDEDOR")

                /* ========================================
                   🟧 ADMINISTRADOR
                   ======================================== */
                .requestMatchers("/admin/**").hasAuthority("ADMIN")

                /* ========================================
                   🟪 VENDEDOR
                   ======================================== */
                .requestMatchers("/vendedor/**").hasAuthority("VENDEDOR")

                /* ========================================
                   🟨 CONSUMIDOR
                   ======================================== */
                .requestMatchers("/consumidor/**").hasAuthority("CONSUMIDOR")

                /* ========================================
                   🟫 RUTAS PRIVADAS
                   ======================================== */
                .requestMatchers("/usuarios/**").authenticated()

                /* ========================================
                   🔐 Cualquier otra ruta requiere token
                   ======================================== */
                .anyRequest().authenticated()
            )

            // Filtro JWT antes del filtro de Spring Security
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
