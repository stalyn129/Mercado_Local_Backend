package com.mercadolocalia.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.mercadolocalia.entities.Valoracion;
import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.entities.Consumidor;

public interface ValoracionRepository extends JpaRepository<Valoracion, Integer> {

    List<Valoracion> findByProducto(Producto producto);

    List<Valoracion> findByConsumidor(Consumidor consumidor);

    boolean existsByProductoAndConsumidor(Producto producto, Consumidor consumidor);
    
    @Query("""
    	    SELECT v FROM Valoracion v
    	    JOIN v.producto p
    	    JOIN p.vendedor ven
    	    WHERE ven.idVendedor = :idVendedor
    	    ORDER BY v.fechaValoracion DESC
    	""")
    	List<Valoracion> findByVendedor(@Param("idVendedor") Integer idVendedor);

}
