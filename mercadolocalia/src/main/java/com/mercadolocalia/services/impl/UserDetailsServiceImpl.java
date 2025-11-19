package com.mercadolocalia.services.impl;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.repositories.UsuarioRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Mantener referencia del usuario actual cargado
    private Usuario usuarioEntidad;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        this.usuarioEntidad = usuario; // Guardarlo para validación con JWT

        // Asignar rol de Spring Security
        SimpleGrantedAuthority authority =
        		new SimpleGrantedAuthority(usuario.getRol().getNombreRol());

        return new User(
                usuario.getCorreo(),
                usuario.getContrasena(),
                Arrays.asList(authority)
        );
    }

    public Usuario getUsuarioEntidad() {
        return usuarioEntidad;
    }
}
