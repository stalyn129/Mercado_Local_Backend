package com.mercadolocalia.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadolocalia.entities.Subcategoria;
import com.mercadolocalia.entities.Categoria;

public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Integer> {

    boolean existsByNombreSubcategoria(String nombreSubcategoria);

    List<Subcategoria> findByCategoria(Categoria categoria);
}
