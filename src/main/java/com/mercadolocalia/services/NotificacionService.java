package com.mercadolocalia.services;

import com.mercadolocalia.entities.Notificacion;
import com.mercadolocalia.entities.Usuario;

import java.util.List;

public interface NotificacionService {

    void crearNotificacion(Usuario usuario, String mensaje, String tipo);

    List<Notificacion> obtenerNoLeidas(Usuario usuario);

    Long contarNoLeidas(Usuario usuario);

    void marcarTodasComoLeidas(Usuario usuario);
}
