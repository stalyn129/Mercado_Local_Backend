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
                    .requestMatchers("/api/admin/config/**").hasRole("ADMIN")

                    // ============================
                    // 🔥 PERMITIR MICRO SERVICIO IA
                    // ============================
                    .requestMatchers("/api/ia/**").permitAll()

                    // ============================
                    // 🛒 VENDEDOR - DEBE IR ANTES DE LA REGLA GENERAL DE PEDIDOS
                    // ============================
                    // Endpoints específicos de vendedor para pedidos
                    .requestMatchers("/pedidos/vendedor/**").hasRole("VENDEDOR")
                    .requestMatchers("/pedidos/*/verificar-pago").hasRole("VENDEDOR")  // ← AGREGAR ESTO
                    .requestMatchers("/pedidos/*/resubir-comprobante").hasRole("CONSUMIDOR") // El cliente re-sube
                    .requestMatchers("/pedidos/vendedor/pendientes-verificacion").hasRole("VENDEDOR")
                    
                    // Resto de endpoints de vendedor
                    .requestMatchers("/api/pedidos-vendedor/**").hasAnyRole("VENDEDOR", "ADMIN")
                    .requestMatchers("/vendedor/**").hasAnyRole("VENDEDOR", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/productos/crear").hasRole("VENDEDOR")
                    .requestMatchers(HttpMethod.PUT, "/productos/editar/**").hasRole("VENDEDOR")
                    .requestMatchers(HttpMethod.DELETE, "/productos/eliminar/**").hasRole("VENDEDOR")
                    .requestMatchers(HttpMethod.PUT, "/productos/estado/**").hasRole("VENDEDOR")
                    .requestMatchers(HttpMethod.GET, "/productos/vendedor/**").hasRole("VENDEDOR")
                    .requestMatchers("/pedidos/estadisticas/**").hasRole("VENDEDOR")
                    .requestMatchers(HttpMethod.GET, "/valoraciones/vendedor/**").hasRole("VENDEDOR")

                    // ============================
                    // 👤 CONSUMIDOR
                    // ============================
                    .requestMatchers("/consumidor/**").hasRole("CONSUMIDOR")
                    
                    // Endpoints específicos de consumidor para pedidos
                    .requestMatchers(HttpMethod.POST, "/pedidos/checkout").hasRole("CONSUMIDOR")
                    .requestMatchers(HttpMethod.POST, "/pedidos/comprar-ahora").hasRole("CONSUMIDOR")
                    .requestMatchers(HttpMethod.PUT, "/pedidos/finalizar/**").hasRole("CONSUMIDOR")
                    .requestMatchers(HttpMethod.PUT, "/pedidos/*/cancelar").hasRole("CONSUMIDOR")
                    .requestMatchers(HttpMethod.GET, "/pedidos/mis-pedidos").hasRole("CONSUMIDOR")
                    
                    // Regla general de pedidos para consumidores - DEBE IR DESPUÉS
                    .requestMatchers("/pedidos/**").hasRole("CONSUMIDOR")

                    .requestMatchers(HttpMethod.POST, "/valoraciones/crear").hasRole("CONSUMIDOR")
                    .requestMatchers(HttpMethod.GET, "/valoraciones/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/favoritos/agregar").hasRole("CONSUMIDOR")
                    .requestMatchers(HttpMethod.GET, "/favoritos/listar/**").hasRole("CONSUMIDOR")

                    // Carrito
                    .requestMatchers(HttpMethod.POST, "/carrito/agregar").hasRole("CONSUMIDOR")
                    .requestMatchers("/carrito/**").authenticated()

                    // ============================
                    // 📄 FACTURAS
                    // ============================
                    .requestMatchers("/api/facturas/**").authenticated()
                    .requestMatchers("/facturas/**").authenticated()

                    // ============================
                    // 👑 ADMIN
                    // ============================
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/admin/logs/**").hasRole("ADMIN")
                    .requestMatchers("/reportes/**").hasAnyRole("ADMIN", "VENDEDOR")

                    .requestMatchers(HttpMethod.GET, "/api/productos").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/productos").hasAnyRole("ADMIN", "VENDEDOR", "CONSUMIDOR")
                    
                    // Productos públicos (solo lectura)
                    .requestMatchers(HttpMethod.GET, "/productos/**").permitAll()
                    
                    // Categorías públicas (solo lectura)
                    .requestMatchers(HttpMethod.GET, "/categorias/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/subcategorias/**").permitAll()
                    
                    // Categorías admin
                    .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/subcategorias/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/subcategorias/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/subcategorias/**").hasRole("ADMIN")

                    // Notificaciones
                    .requestMatchers("/notificaciones/**").hasRole("CONSUMIDOR")

                    // ============================
                    // 🔐 CUALQUIER OTRA RUTA
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