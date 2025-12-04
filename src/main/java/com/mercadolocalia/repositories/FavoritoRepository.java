package com.mercadolocalia.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadolocalia.entities.Favorito;

public interface FavoritoRepository extends JpaRepository<Favorito, Integer> {

    boolean existsByConsumidorIdConsumidorAndProductoIdProducto(Integer idConsumidor, Integer idProducto);

    List<Favorito> findByConsumidorIdConsumidor(Integer idConsumidor);
}

