package com.mercadolocalia.security.auth;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.AuthResponse;
import com.mercadolocalia.dto.LoginRequest;
import com.mercadolocalia.dto.RegisterRequest;
import com.mercadolocalia.entities.Rol;
import com.mercadolocalia.entities.Token;
import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.repositories.RolRepository;
import com.mercadolocalia.repositories.TokenRepository;
import com.mercadolocalia.repositories.UsuarioRepository;
import com.mercadolocalia.security.jwt.JwtService;

@Service
public class AuthenticationService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    // ============================
    // REGISTRO
    // ============================
    public AuthResponse registrar(RegisterRequest request) {

        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            AuthResponse response = new AuthResponse();
            response.setMensaje("El correo ya está registrado");
            return response;
        }

        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setCorreo(request.getCorreo());
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        usuario.setFechaNacimiento(LocalDate.parse(request.getFechaNacimiento()));
        usuario.setRol(rol);
        usuario.setEsAdministrador(false);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setEstado("Activo");

        usuarioRepository.save(usuario);

        String tokenJwt = jwtService.generarToken(usuario);

        Token token = new Token();
        token.setUsuario(usuario);
        token.setToken(tokenJwt);
        token.setFechaExpiracion(LocalDateTime.now().plusDays(1));

        tokenRepository.save(token);

        AuthResponse response = new AuthResponse();
        response.setToken(tokenJwt);
        response.setMensaje("Usuario registrado exitosamente");
        response.setIdUsuario(usuario.getIdUsuario());
        response.setRol(usuario.getRol().getNombreRol());

        return response;
    }

    // ============================
    // LOGIN
    // ============================
    public AuthResponse login(LoginRequest request) {

        // Validar credenciales
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getCorreo(),
                            request.getContrasena()
                    )
            );
        } catch (Exception ex) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String tokenJwt = jwtService.generarToken(usuario);

        Token token = new Token();
        token.setUsuario(usuario);
        token.setToken(tokenJwt);
        token.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        tokenRepository.save(token);

        AuthResponse response = new AuthResponse();
        response.setToken(tokenJwt);
        response.setMensaje("Login exitoso");
        response.setIdUsuario(usuario.getIdUsuario());
        response.setRol(usuario.getRol().getNombreRol());

        return response;
    }
}
