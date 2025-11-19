package com.mercadolocalia.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadolocalia.entities.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    boolean existsByNombreCategoria(String nombreCategoria);
}
