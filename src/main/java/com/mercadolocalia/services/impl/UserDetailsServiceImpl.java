package com.mercadolocalia.services.impl;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.repositories.UsuarioRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Guarda la entidad cargada en caso de usarla desde filtros
    private Usuario usuarioEntidad;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new UsernameNotFoundException("❌ Usuario no encontrado con correo: " + correo)
                );

        // Guardamos para echar mano desde el filtro si se necesita
        this.usuarioEntidad = usuario;

        // 🔥 Convertimos el rol a un formato válido para Spring Security
        //  ADMIN → ROLE_ADMIN
        String rolSpring = "ROLE_" + usuario.getRol().getNombreRol().toUpperCase();

        return new User(
                usuario.getCorreo(),                      // usuario
                usuario.getContrasena(),                  // password Hash BCrypt
                Collections.singleton(new SimpleGrantedAuthority(rolSpring)) // autoridad válida
        );
    }

    // 🔥 Extra accesible si luego quieres obtener info del usuario autenticado
    public Usuario getUsuarioEntidad() {
        return usuarioEntidad;
    }
}
