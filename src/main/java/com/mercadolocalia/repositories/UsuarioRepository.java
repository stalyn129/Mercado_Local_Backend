package com.mercadolocalia.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.mercadolocalia.entities.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);

    // 🔥 Necesario para AdminDashboardController
    List<Usuario> findTop5ByOrderByFechaRegistroDesc();
}
