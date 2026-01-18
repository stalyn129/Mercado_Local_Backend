package com.mercadolocalia.security.jwt;

import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.repositories.UsuarioRepository;
import com.mercadolocalia.services.impl.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired private JwtService jwtService;
    @Autowired private UserDetailsServiceImpl userDetailsService;
    @Autowired private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // ================== RUTAS PÚBLICAS ==================
        if (path.startsWith("/auth") ||
            path.startsWith("/uploads") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/api-docs") ||
            (path.startsWith("/productos") && request.getMethod().equals("GET")) ||
            (path.startsWith("/api/categorias") && request.getMethod().equals("GET")) ||
            (path.startsWith("/api/subcategorias") && request.getMethod().equals("GET"))
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        // ================== LECTURA DEL TOKEN ==================
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String correo = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            try {
                correo = jwtService.obtenerCorreoDesdeToken(token);
            } catch (Exception e) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // ================== VALIDACIÓN TOKEN ==================
        if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(correo);
            Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);

            if (usuario != null && jwtService.validarToken(token, usuario)) {

                // extrae "ADMIN" | "VENDEDOR" | "CONSUMIDOR"
                String rol = jwtService.extraerValor(token, "rol", String.class);

                // Spring requiere formato ROLE_XXXX
                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase());

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                List.of(authority)
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
