package com.mercadolocalia.repositories;

import com.mercadolocalia.entities.Notificacion;
import com.mercadolocalia.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    List<Notificacion> findByUsuarioAndLeidoFalseOrderByFechaDesc(Usuario usuario);

    Long countByUsuarioAndLeidoFalse(Usuario usuario);
}
