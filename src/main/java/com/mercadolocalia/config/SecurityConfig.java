package com.mercadolocalia.config;

import com.mercadolocalia.security.jwt.JwtAuthenticationFilter;
import com.mercadolocalia.services.impl.UserDetailsServiceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
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

    @Autowired
    private UserDetailsServiceImpl userDetailsService;


    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ================= CORS CORRECTO PARA FRONT =================
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:5173"));  // Front
                config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .userDetailsService(userDetailsService)


            .authorizeHttpRequests(auth -> auth

                // ========= Permitir OPTIONS (CORS Preflight) =========
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ========= Swagger =========
                .requestMatchers("/swagger-ui/**","/v3/api-docs/**","/api-docs/**","/swagger-config").permitAll()

                // ========= Login / Registro =========
                .requestMatchers("/auth/**","/uploads/**").permitAll()

                // ========= ADMIN =========
                .requestMatchers("/api/admin/**").hasRole("ADMIN")         // 🔥 AHORA SÍ CORRECTO

                // ========= VENDEDOR =========
                .requestMatchers(HttpMethod.POST, "/productos/crear").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.PUT, "/productos/editar/**").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.DELETE, "/productos/eliminar/**").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.PUT, "/productos/estado/**").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.GET, "/productos/vendedor/**").hasRole("VENDEDOR")

                // ========= Pública =========
                .requestMatchers(HttpMethod.GET, "/productos/**").permitAll()
                .requestMatchers("/categorias/**","/subcategorias/**").permitAll()

                // ========= Consumidor =========
                .requestMatchers("/consumidor/**").hasRole("CONSUMIDOR")

                // ========= Todo lo demás requiere token =========
                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
