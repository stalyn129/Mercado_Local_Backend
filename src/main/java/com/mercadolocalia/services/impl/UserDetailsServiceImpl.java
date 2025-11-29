package com.mercadolocalia.services.impl;

import java.util.Collections;

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

    // Guarda el usuario cargado para validarlo con el JWT
    private Usuario usuarioEntidad;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new UsernameNotFoundException("❌ Usuario no encontrado con correo: " + correo)
                );

        // Guardamos para obtenerlo en validaciones del Token
        this.usuarioEntidad = usuario;

        // IMPORTANTE: Usamos ROLE_ para que Security lo reconozca
        String rolSpring = "ROLE_" + usuario.getRol().getNombreRol().toUpperCase();

        return new User(
                usuario.getCorreo(),              // username usado para login
                usuario.getContrasena(),          // password
                Collections.singleton(new SimpleGrantedAuthority(rolSpring)) // autoridad
        );
    }

    // 🟢 Permite acceder al usuario cargado desde filtros JWT si lo necesitas
    public Usuario getUsuarioEntidad() {
        return usuarioEntidad;
    }
}
