package com.mercadolocalia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${mercadolocalia.allowed-origins:}")
    private String allowedOrigins;
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();

        // Si hay origins configurados en properties, usarlos
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            String[] origins = allowedOrigins.split(",");
            cors.setAllowedOrigins(Arrays.asList(origins));
            System.out.println("✅ CORS configurado para: " + Arrays.toString(origins));
        } else {
            // Fallback a los origins por defecto
            cors.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:5175",
                "http://localhost:3000",
                "http://192.168.1.13:8081",  // Tu Expo
                "http://192.168.1.13:3000",  // Posible React web
                "exp://192.168.1.13:8081"    // Expo protocol
            ));
        }

        cors.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        cors.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));

        cors.setExposedHeaders(List.of(
            "Content-Disposition",
            "Authorization"
        ));

        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L); // 1 hora de cache para preflight

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);

        return source;
    }
}