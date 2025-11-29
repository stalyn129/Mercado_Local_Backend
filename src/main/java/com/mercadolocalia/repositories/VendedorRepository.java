package com.mercadolocalia.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.entities.Usuario;

public interface VendedorRepository extends JpaRepository<Vendedor, Integer> {

    // Buscar vendedor por usuario
    Vendedor findByUsuario(Usuario usuario);

    boolean existsByUsuario(Usuario usuario);
}
