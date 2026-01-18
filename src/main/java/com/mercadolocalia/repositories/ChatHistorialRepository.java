package com.mercadolocalia.repositories;

import com.mercadolocalia.entities.ChatHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ChatHistorialRepository extends JpaRepository<ChatHistorial, Long> {

    // Recupera mensajes de un usuario específico según su rol
    List<ChatHistorial> findByIdUsuarioAndRolOrderByFechaAsc(Integer idUsuario, String rol);

    @Modifying
    @Transactional
    void deleteByIdUsuarioAndRol(Integer idUsuario, String rol);
}