package com.mercadolocalia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.services.UsuarioService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/perfil")
    public Usuario obtenerMiPerfil(Authentication auth) {

        String correo = auth.getName(); // viene del token

        return usuarioService.obtenerPerfil(correo);
    }
}
