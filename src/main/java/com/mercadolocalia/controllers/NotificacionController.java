package com.mercadolocalia.controllers;

import com.mercadolocalia.entities.Notificacion;
import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.repositories.UsuarioRepository;
import com.mercadolocalia.services.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificaciones")
@PreAuthorize("hasRole('CONSUMIDOR')")
public class NotificacionController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping("/usuario/{idUsuario}")
    public List<Notificacion> obtenerNoLeidas(@PathVariable Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow();
        return notificacionService.obtenerNoLeidas(usuario);
    }

    @GetMapping("/contar/{idUsuario}")
    public Long contar(@PathVariable Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow();
        return notificacionService.contarNoLeidas(usuario);
    }

    @PutMapping("/marcar-leidas/{idUsuario}")
    public void marcarLeidas(@PathVariable Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow();
        notificacionService.marcarTodasComoLeidas(usuario);
    }
}
