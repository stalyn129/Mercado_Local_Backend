package com.mercadolocalia.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadolocalia.entities.Valoracion;
import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.entities.Consumidor;

public interface ValoracionRepository extends JpaRepository<Valoracion, Integer> {

    List<Valoracion> findByProducto(Producto producto);

    List<Valoracion> findByConsumidor(Consumidor consumidor);

    boolean existsByProductoAndConsumidor(Producto producto, Consumidor consumidor);
}
