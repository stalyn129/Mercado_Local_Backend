package com.mercadolocalia.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.entities.Subcategoria;
import com.mercadolocalia.entities.Vendedor;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByVendedor(Vendedor vendedor);

    List<Producto> findBySubcategoria(Subcategoria subcategoria);

    List<Producto> findByEstado(String estado);
}
