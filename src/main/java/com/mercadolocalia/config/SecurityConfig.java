package com.mercadolocalia.config;

import com.mercadolocalia.security.jwt.JwtAuthenticationFilter;
<<<<<<< Updated upstream
=======
import com.mercadolocalia.services.impl.UserDetailsServiceImpl;

import java.util.List;

>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
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
=======
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:5173"));
                config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS")); // ⬅ IMPORTANTÍSIMO
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true); // ⬅ NECESARIO PARA TOKEN
                return config;
            }))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .userDetailsService(userDetailsService)
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
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
=======
                // ⚠ PERMITIR PRE-FLIGHT (OPTIONS) O NADA FUNCIONA
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Swagger
                .requestMatchers(
                    "/swagger-ui/**","/v3/api-docs/**","/api-docs/**","/swagger-config"
                ).permitAll()

                // Login/Register
                .requestMatchers("/auth/**","/uploads/**").permitAll()

                // 🔥 ADMIN con token
                .requestMatchers("/api/admin/**").hasRole("ADMIN")


                // Vendedor
                .requestMatchers("/productos/crear").hasRole("VENDEDOR")

                // Consumidor
                .requestMatchers("/consumidor/**").hasRole("CONSUMIDOR")

                // Cualquier otro → requiere autenticación
                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
>>>>>>> Stashed changes

	                // ========= Consumidor =========
	                .requestMatchers("/consumidor/**").hasAuthority("CONSUMIDOR")

<<<<<<< Updated upstream
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
=======

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
>>>>>>> Stashed changes
}
