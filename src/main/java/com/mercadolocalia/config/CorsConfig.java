package com.mercadolocalia.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cors = new CorsConfiguration();

        // ===== ORÍGENES PERMITIDOS =====
        cors.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:5175",
                "https://*.onrender.com",
                "https://mercado-local-ia.onrender.com"
        ));

        // ===== MÉTODOS PERMITIDOS =====
        cors.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // ===== HEADERS PERMITIDOS =====
        cors.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept"
        ));

        // ===== COOKIES / JWT =====
        cors.setAllowCredentials(true);

        // ===== REGISTRO DEL PATH =====
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);

        return new CorsFilter(source);
    }
}
