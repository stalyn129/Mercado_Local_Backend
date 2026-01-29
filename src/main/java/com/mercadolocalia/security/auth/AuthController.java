package com.mercadolocalia.security.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.AuthResponse;
import com.mercadolocalia.dto.LoginRequest;
import com.mercadolocalia.dto.RegisterRequest;
import com.mercadolocalia.services.LogService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private LogService logService;

    // ========== REGISTRO NORMAL ==========
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.registrar(request);
        logService.guardar("Registro de nuevo usuario", request.getCorreo());
        return ResponseEntity.ok(response);
    }

    // ========== REGISTRO CON GOOGLE ==========
    @PostMapping("/register-google")
    public ResponseEntity<AuthResponse> registrarConGoogle(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.registrarConGoogle(request);
        logService.guardar("Registro con Google", request.getCorreo());
        return ResponseEntity.ok(response);
    }

    // ========== VERIFICAR EMAIL (para Google OAuth) ==========
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailExists(@RequestParam String email) {
        try {
            boolean exists = authService.checkEmailExists(email);
            Map<String, Object> response = new HashMap<>();
            response.put("exists", exists);
            response.put("message", exists ? "El email ya está registrado" : "Email disponible");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error verificando email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ========== LOGIN NORMAL ==========
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            logService.guardar("Inicio de sesión exitoso", request.getCorreo());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            AuthResponse error = new AuthResponse();
            error.setMensaje("Correo o contraseña incorrectos");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (RuntimeException e) {
            AuthResponse error = new AuthResponse();
            error.setMensaje(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== LOGIN CON GOOGLE ==========
    @PostMapping("/login-google")
    public ResponseEntity<AuthResponse> loginConGoogle(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                AuthResponse error = new AuthResponse();
                error.setMensaje("Email es requerido");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Añadir logs para debug
            System.out.println("=== GOOGLE LOGIN REQUEST ===");
            System.out.println("Email recibido: " + email);
            
            AuthResponse response = authService.loginConGoogle(email);
            logService.guardar("Login con Google exitoso", email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.out.println("ERROR en login-google: " + e.getMessage());
            e.printStackTrace();
            AuthResponse error = new AuthResponse();
            error.setMensaje(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== VERIFICAR TOKEN GOOGLE ==========
    @PostMapping("/verify-google-token")
    public ResponseEntity<Map<String, Object>> verifyGoogleToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            boolean isValid = token != null && !token.isEmpty();
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("message", isValid ? "Token válido" : "Token inválido");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error verificando token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}