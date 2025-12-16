package com.mercadolocalia.services.impl;

import com.mercadolocalia.entities.Notificacion;
import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.repositories.NotificacionRepository;
import com.mercadolocalia.services.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Override
    public void crearNotificacion(Usuario usuario, String mensaje, String tipo) {
        Notificacion n = new Notificacion();
        n.setUsuario(usuario);
        n.setMensaje(mensaje);
        n.setTipo(tipo);
        n.setLeido(false);
        notificacionRepository.save(n);
    }

    @Override
    public List<Notificacion> obtenerNoLeidas(Usuario usuario) {
        return notificacionRepository
                .findByUsuarioAndLeidoFalseOrderByFechaDesc(usuario);
    }

    @Override
    public Long contarNoLeidas(Usuario usuario) {
        return notificacionRepository
                .countByUsuarioAndLeidoFalse(usuario);
    }

    @Override
    public void marcarTodasComoLeidas(Usuario usuario) {
        List<Notificacion> lista = obtenerNoLeidas(usuario);
        lista.forEach(n -> n.setLeido(true));
        notificacionRepository.saveAll(lista);
    }
}
