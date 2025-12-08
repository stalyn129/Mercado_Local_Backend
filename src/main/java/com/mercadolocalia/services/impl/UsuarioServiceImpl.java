package com.mercadolocalia.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.UsuarioRequest;
import com.mercadolocalia.dto.UsuarioResponse;
import com.mercadolocalia.entities.Rol;
import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.repositories.RolRepository;
import com.mercadolocalia.repositories.UsuarioRepository;
import com.mercadolocalia.services.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private RolRepository rolRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    // ====================================================
    // OBTENER PERFIL (para el usuario logueado)
    // ====================================================
    @Override
    public Usuario obtenerPerfil(String correo) {
        return usuarioRepo.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // ====================================================
    // LISTAR USUARIOS (ADMIN)
    // ====================================================
    @Override
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepo.findAll().stream().map(u -> new UsuarioResponse(
                u.getIdUsuario(),
                u.getNombre(),
                u.getApellido(),
                u.getCorreo(),
                "********",                      // Nunca enviar hash
                u.getFechaNacimiento(),
                u.getRol().getNombreRol(),
                u.getEsAdministrador(),
                u.getFechaRegistro(),
                u.getEstado()
        )).toList();
    }

    // ====================================================
    // CAMBIAR ESTADO (Activo / Suspendido)
    // ====================================================
    @Override
    public String cambiarEstado(Integer id) {
        Usuario user = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getEstado() == null) {
            user.setEstado("Activo");
        } else {
            user.setEstado(user.getEstado().equalsIgnoreCase("Activo") ? "Suspendido" : "Activo");
        }

        usuarioRepo.save(user);
        return user.getEstado();
    }

    // ====================================================
    // ACTUALIZAR USUARIO COMPLETO (ADMIN)
    // ====================================================
    @Override
    public UsuarioResponse actualizarUsuario(Integer id, UsuarioRequest req) {

        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        u.setNombre(req.nombre());
        u.setApellido(req.apellido());
        u.setCorreo(req.correo());
        u.setFechaNacimiento(req.fechaNacimiento());
        u.setEstado(req.estado());

        // ==============================
        // Si se envía nueva contraseña
        // ==============================
        if (req.contrasena() != null && !req.contrasena().isBlank()) {
            u.setContrasena(passwordEncoder.encode(req.contrasena()));
        }

        // ==============================
        // Cambio de Rol
        // ==============================
        Rol nuevoRol = rolRepo.findByNombreRol(req.rol())
                .orElseThrow(() -> new RuntimeException("Rol no válido"));

        u.setRol(nuevoRol);

        usuarioRepo.save(u);

        return new UsuarioResponse(
                u.getIdUsuario(),
                u.getNombre(),
                u.getApellido(),
                u.getCorreo(),
                "********",                      // No retornar contraseña
                u.getFechaNacimiento(),
                u.getRol().getNombreRol(),
                u.getEsAdministrador(),
                u.getFechaRegistro(),
                u.getEstado()
        );
    }
    
    @Override
    public void eliminarUsuario(Integer id) {
        Usuario user = usuarioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuarioRepo.delete(user);
    }

}
