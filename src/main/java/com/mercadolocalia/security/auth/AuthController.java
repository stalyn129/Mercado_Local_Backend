package com.mercadolocalia.security.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mercadolocalia.dto.AuthResponse;
import com.mercadolocalia.dto.LoginRequest;
import com.mercadolocalia.dto.RegisterRequest;
import com.mercadolocalia.services.LogService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private LogService logService;   // 👈 AÑADIDO

    // ========== REGISTRO ==========
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.registrar(request);

        // Guardar log
        logService.guardar("Registro de nuevo usuario", request.getCorreo());

        return ResponseEntity.ok(response);
    }

    // ========== LOGIN ==========
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        try {
            AuthResponse response = authService.login(request);

            // 🔥 LOG DE LOGIN EXITOSO
            logService.guardar("Inicio de sesión exitoso", request.getCorreo());

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException | UsernameNotFoundException e) {

            // ❗ OPCIONAL: guardar intento fallido (lo dejo comentado)
            // logService.guardar("Intento fallido de inicio de sesión", request.getCorreo());

            AuthResponse error = new AuthResponse();
            error.setMensaje("Correo o contraseña incorrectos");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

        } catch (RuntimeException e) {

            AuthResponse error = new AuthResponse();
            error.setMensaje(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
