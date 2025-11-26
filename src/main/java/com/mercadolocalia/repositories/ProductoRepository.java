package com.mercadolocalia.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.entities.Subcategoria;
import com.mercadolocalia.entities.Vendedor;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByVendedor(Vendedor vendedor); 

    List<Producto> findBySubcategoria(Subcategoria subcategoria);

    List<Producto> findByEstado(String estado);

    // ✔ contar productos del vendedor
    Integer countByVendedor(Vendedor vendedor);
    
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.vendedor.idVendedor = :id AND p.estado = 'Disponible'")
    Integer contarDisponiblesPorVendedor(Integer id);

}
