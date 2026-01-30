package com.mercadolocalia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${mercadolocalia.allowed-origins:}")
    private String allowedOrigins;

    // Configuración para Spring MVC
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(getAllowedOriginPatternsArray())
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // Configuración para filtro CORS (funciona a nivel más bajo)
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildCorsConfiguration());
        return new CorsFilter(source);
    }

    // Configuración CORS para seguridad
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildCorsConfiguration());
        return source;
    }

    private CorsConfiguration buildCorsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Configurar orígenes permitidos desde properties
        List<String> originsList = getAllowedOriginsList();
        config.setAllowedOrigins(originsList);
        
        // También permitir patrones de origen (útil para Expo y direcciones IP)
        config.setAllowedOriginPatterns(getAllowedOriginPatterns());
        
        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Headers permitidos
        config.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "Cache-Control"
        ));
        
        // Headers expuestos
        config.setExposedHeaders(Arrays.asList(
            "Content-Disposition",
            "Authorization",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        // Permitir credenciales (necesario para cookies y auth)
        config.setAllowCredentials(true);
        
        // Tiempo de cache para preflight requests
        config.setMaxAge(3600L);
        
        System.out.println("✅ CORS configurado para orígenes: " + originsList);
        System.out.println("✅ Patrones de origen permitidos: " + getAllowedOriginPatterns());
        
        return config;
    }

    private List<String> getAllowedOriginsList() {
        if (allowedOrigins != null && !allowedOrigins.trim().isEmpty()) {
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            return origins;
        } else {
            // Orígenes por defecto para desarrollo
            return Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:5175",
                "http://192.168.1.13:8081",
                "http://192.168.1.13:3000"
            );
        }
    }

    private List<String> getAllowedOriginPatterns() {
        // Patrones más flexibles para Expo y entornos de desarrollo
        return Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://192.168.1.*:*",
            "exp://*",
            "http://*.local:*"
        );
    }

    private String[] getAllowedOriginPatternsArray() {
        // Convertir la lista a array para CorsRegistry
        return new String[] {
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://192.168.1.*:*",
            "exp://*",
            "http://*.local:*"
        };
    }
}