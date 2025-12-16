package com.mercadolocalia.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.UsuarioResponse;
import com.mercadolocalia.dto.UsuarioPerfilDTO;
import com.mercadolocalia.dto.UsuarioRequest;
import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.services.UsuarioService;

@RestController
@RequestMapping("/")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // ============================
    // PERFIL
    // ============================
    @GetMapping("/usuarios/perfil")
    public UsuarioPerfilDTO obtenerPerfil(Authentication auth) {
        return usuarioService.obtenerPerfilDTO(auth.getName());
    }

    // ============================
    // ADMIN → LISTAR USUARIOS
    // ============================
    @GetMapping("/api/admin/usuarios")
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    // ============================
    // ADMIN → CAMBIAR ESTADO
    // ============================
    @PutMapping("/api/admin/usuarios/{id}/estado")
    public String cambiarEstado(@PathVariable Integer id) {
        return usuarioService.cambiarEstado(id);
    }

    @PutMapping("/api/admin/usuarios/{id}")
    public UsuarioResponse actualizarUsuario(
            @PathVariable Integer id,
            @RequestBody UsuarioRequest request
    ) {
        return usuarioService.actualizarUsuario(id, request);
    }

    @DeleteMapping("/api/admin/usuarios/{id}")
    public String eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
        return "Usuario eliminado correctamente";
    }
}
