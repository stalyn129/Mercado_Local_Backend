package com.mercadolocalia.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.entities.Usuario;

public interface VendedorRepository extends JpaRepository<Vendedor, Integer> {

    // ✔ IMPORTANTE PARA REGISTRO DE VENDEDOR (no se debe borrar)
    Vendedor findByUsuario(Usuario usuario);
    boolean existsByUsuario(Usuario usuario);

    // ✔ NECESARIO PARA ADMIN DASHBOARD
    List<Vendedor> findTop5ByOrderByIdVendedorDesc();
}
