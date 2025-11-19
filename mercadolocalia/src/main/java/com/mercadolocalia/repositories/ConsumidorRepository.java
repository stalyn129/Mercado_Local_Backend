package com.mercadolocalia.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadolocalia.entities.Consumidor;
import com.mercadolocalia.entities.Usuario;

public interface ConsumidorRepository extends JpaRepository<Consumidor, Integer> {

    // Buscar consumidor por usuario
    Consumidor findByUsuario(Usuario usuario);

    boolean existsByUsuario(Usuario usuario);
}
