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
	        .cors(cors -> {})   // 🔥 Activa tu CorsConfig sin romper nada
	        .csrf(csrf -> csrf.disable())
	        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .authorizeHttpRequests(auth -> auth

	                // ========= Swagger =========
	                .requestMatchers(
	                        "/swagger-ui.html",
	                        "/swagger-ui/**",
	                        "/v3/api-docs/**",
	                        "/v3/api-docs",
	                        "/api-docs/**",
	                        "/api-docs/swagger-config",
	                        "/swagger-resources/**",
	                        "/swagger-config",
	                        "/webjars/**"
	                ).permitAll()

	                // ========= Público =========
	                .requestMatchers("/auth/**", "/uploads/**").permitAll()
	                .requestMatchers("/categorias/**", "/subcategorias/**").permitAll()
	                .requestMatchers(HttpMethod.GET, "/productos/**").permitAll()

	                // ========= Solo VENDEDOR =========
	                .requestMatchers(HttpMethod.GET, "/productos/**").permitAll()

	             .requestMatchers(HttpMethod.POST, "/productos/crear").hasAuthority("VENDEDOR")
	             .requestMatchers(HttpMethod.PUT, "/productos/editar/**").hasAuthority("VENDEDOR")
	             .requestMatchers(HttpMethod.DELETE, "/productos/eliminar/**").hasAuthority("VENDEDOR")
	             .requestMatchers(HttpMethod.PUT, "/productos/estado/**").hasAuthority("VENDEDOR")
	             .requestMatchers(HttpMethod.GET, "/productos/vendedor/**").hasAuthority("VENDEDOR")


	                // ========= Admin =========
	                .requestMatchers("/admin/**").hasAuthority("ADMIN")

	             // ========= Consumidor =========
	                .requestMatchers("/consumidor/**").hasAuthority("CONSUMIDOR")
	                .requestMatchers(HttpMethod.POST, "/valoraciones/crear").hasAuthority("CONSUMIDOR")
	                .requestMatchers(HttpMethod.GET, "/valoraciones/**").permitAll()
	                .requestMatchers(HttpMethod.POST, "/favoritos/agregar").hasAuthority("CONSUMIDOR")
	             // Listar favoritos (solo consumidor autenticado)
	                .requestMatchers(HttpMethod.GET, "/favoritos/listar/**").hasAuthority("CONSUMIDOR")

	                
	             // ========= Pedidos =========

	             // FIRST — Finalizar compra
	             .requestMatchers(HttpMethod.PUT, "/pedidos/finalizar/**").hasAuthority("CONSUMIDOR")

	             // Comprar ahora
	             .requestMatchers(HttpMethod.POST, "/pedidos/comprar-ahora").hasAuthority("CONSUMIDOR")

	             // Listar pedidos y detalles
	             .requestMatchers(HttpMethod.GET, "/pedidos/**").hasAuthority("CONSUMIDOR")


	                .anyRequest().authenticated()

	        )

	        // JWT
	        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}



	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
