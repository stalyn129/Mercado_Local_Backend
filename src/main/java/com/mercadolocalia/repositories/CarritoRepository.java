package com.mercadolocalia.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadolocalia.entities.Carrito;

public interface CarritoRepository extends JpaRepository<Carrito, Integer> {

    Optional<Carrito> findByConsumidorIdConsumidor(Integer idConsumidor);

}
