package com.mercadolocalia.config;

import com.mercadolocalia.security.jwt.JwtAuthenticationFilter;
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
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
        	.cors(withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
            		
            		.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ============================
                // 🔓 PÚBLICO
                // ============================
                .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/v3/api-docs",
                        "/api-docs/**",
                        "/swagger-resources/**",
                        "/swagger-config",
                        "/webjars/**"
                ).permitAll()

                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()

                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "VENDEDOR")
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "VENDEDOR")
                
                .requestMatchers("/reportes/**").permitAll() 

             // 🔥 NUEVO: PERMITE /api/productos PARA GET (admin dashboard)
                .requestMatchers(HttpMethod.GET, "/api/productos").hasRole("ADMIN")
                
                // 🔥 NUEVO: PERMITE /productos PARA GET (admin dashboard)
                .requestMatchers(HttpMethod.GET, "/productos").hasAnyRole("ADMIN", "VENDEDOR", "CONSUMIDOR")
                
                // Productos públicos (solo lectura)
                .requestMatchers(HttpMethod.GET, "/productos/**").permitAll()
                
                // Categorías públicas (solo lectura)
                .requestMatchers(HttpMethod.GET, "/categorias/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/subcategorias/**").permitAll()
                
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/chatbot").authenticated()

                // ============================
                // 👑 ADMIN - GESTIÓN DE CATEGORÍAS
                // ============================
                .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/subcategorias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/subcategorias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/subcategorias/**").hasRole("ADMIN")

                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin/logs/**").hasRole("ADMIN")
                .requestMatchers("/reportes/**").hasRole("ADMIN")

             // 🔔 NOTIFICACIONES
                .requestMatchers("/notificaciones/**")
                .hasRole("CONSUMIDOR")

                // ============================
                // 🔥 PERMITIR MICRO SERVICIO IA
                // ============================
                .requestMatchers("/api/ia/**").permitAll()
                // Esto habilita:
                // - /api/ia/precio/{id}
                // - /api/ia/demanda/{id}
                // - /api/ia/recomendar/{id}
                // - /api/ia/chat   ✔✔✔

                // ============================
                // 🛒 VENDEDOR
                // ============================
                .requestMatchers("/api/pedidos-vendedor/**").hasAnyRole("VENDEDOR", "ADMIN")
                .requestMatchers("/vendedor/**").hasAnyRole("VENDEDOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/productos/crear").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.PUT, "/productos/editar/**").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.DELETE, "/productos/eliminar/**").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.PUT, "/productos/estado/**").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.GET, "/productos/vendedor/**").hasRole("VENDEDOR")
                .requestMatchers("/pedidos/vendedor/**").hasRole("VENDEDOR")
                .requestMatchers(HttpMethod.GET, "/valoraciones/vendedor/**").hasRole("VENDEDOR")
                .requestMatchers("/pedidos/estadisticas/**").hasRole("VENDEDOR")
                
             // ============================
             // 👤 CONSUMIDOR
             // ============================
             .requestMatchers("/consumidor/**").hasRole("CONSUMIDOR")
             .requestMatchers(HttpMethod.POST, "/valoraciones/crear").hasRole("CONSUMIDOR")
             .requestMatchers(HttpMethod.GET, "/valoraciones/**").permitAll()
             .requestMatchers(HttpMethod.POST, "/favoritos/agregar").hasRole("CONSUMIDOR")
             .requestMatchers(HttpMethod.GET, "/favoritos/listar/**").hasRole("CONSUMIDOR")

             // ✅ Mis pedidos (SIN mandar id)
             .requestMatchers(HttpMethod.GET, "/pedidos/mis-pedidos").hasRole("CONSUMIDOR")

             // ✅ Flujo compra
             .requestMatchers(HttpMethod.POST, "/pedidos/checkout").hasRole("CONSUMIDOR")
             .requestMatchers(HttpMethod.POST, "/pedidos/comprar-ahora").hasRole("CONSUMIDOR")
             .requestMatchers(HttpMethod.PUT, "/pedidos/finalizar/**").hasRole("CONSUMIDOR")
             .requestMatchers(HttpMethod.PUT, "/pedidos/*/cancelar").hasRole("CONSUMIDOR")

             // ✅ Cualquier otro pedido del consumidor
             .requestMatchers("/pedidos/**").hasRole("CONSUMIDOR")

             // carrito
             .requestMatchers(HttpMethod.POST, "/carrito/agregar").hasRole("CONSUMIDOR")
             .requestMatchers("/carrito/**").authenticated()

                // ============================
                // 🔐 CUALQUIER OTRA RUTA REQUIERE TOKEN
                // ============================
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