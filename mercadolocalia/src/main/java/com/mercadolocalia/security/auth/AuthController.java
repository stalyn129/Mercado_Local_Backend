package com.mercadolocalia.security.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mercadolocalia.dto.AuthResponse;
import com.mercadolocalia.dto.LoginRequest;
import com.mercadolocalia.dto.RegisterRequest;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationService authService;

    // ============================
    // REGISTRO
    // ============================
    @PostMapping("/register")
    public AuthResponse registrar(@RequestBody RegisterRequest request) {
        return authService.registrar(request);
    }

    // ============================
    // LOGIN
    // ============================
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
