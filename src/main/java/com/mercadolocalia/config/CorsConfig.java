package com.mercadolocalia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Se define desde variables de entorno en cloud:
     * mercadolocalia.allowed-origins=https://tufrontend.cloudfront.net
     */
    @Value("${mercadolocalia.allowed-origins:}")
    private String allowedOrigins;

    // ============================
    // SPRING MVC (nivel controller)
    // ============================
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(getAllowedOriginPatterns())
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // ============================
    // SPRING SECURITY / FILTRO
    // ============================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Orígenes permitidos (cloud o local)
        config.setAllowedOrigins(getAllowedOriginsList());

        // Métodos permitidos
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Headers permitidos
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "Cache-Control",
                "X-Requested-With"
        ));

        // Headers expuestos
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition"
        ));

        // JWT / cookies
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        System.out.println("✅ CORS ORIGINS: " + getAllowedOriginsList());

        return source;
    }

    // ============================
    // ORÍGENES PERMITIDOS
    // ============================
    private List<String> getAllowedOriginsList() {
        // CLOUD (AWS / ECS)
        if (allowedOrigins != null && !allowedOrigins.trim().isEmpty()) {
            return Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .toList();
        }

        // LOCAL (desarrollo)
        return Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        );
    }

    // ============================
    // PATRONES (solo para local)
    // ============================
    private String[] getAllowedOriginPatterns() {
        return new String[]{
                "http://localhost:*",
                "http://127.0.0.1:*"
        };
    }
}
