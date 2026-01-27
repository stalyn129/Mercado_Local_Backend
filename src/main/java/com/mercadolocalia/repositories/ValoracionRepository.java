package com.mercadolocalia.repositories;

import java.util.List;
import java.util.Map;

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

	// Reportes graficos vendedor

	 // 1. PRODUCTOS MEJOR VALORADOS POR VENDEDOR - CORREGIDO
    @Query("""
        SELECT new map(
            p.nombreProducto as producto,
            AVG(v.calificacion) as promedioCalificacion,
            COUNT(v) as totalResenas
        )
        FROM Valoracion v
        JOIN v.producto p
        WHERE p.vendedor.idVendedor = :idVendedor
        GROUP BY p.idProducto, p.nombreProducto
        HAVING COUNT(v) >= 1
        ORDER BY promedioCalificacion DESC
    """)
    List<Map<String, Object>> obtenerProductosMejorValorados(@Param("idVendedor") Integer idVendedor);

    // 2. DISTRIBUCIÓN DE CALIFICACIONES
    @Query("""
        SELECT 
            v.calificacion as estrellas,
            COUNT(v) as cantidad
        FROM Valoracion v
        JOIN v.producto p
        WHERE p.vendedor.idVendedor = :idVendedor
        GROUP BY v.calificacion
        ORDER BY v.calificacion DESC
    """)
    List<Object[]> obtenerDistribucionCalificaciones(@Param("idVendedor") Integer idVendedor);

    // 3. RESEÑAS RECIENTES - CORREGIDO
    @Query("""
        SELECT v 
        FROM Valoracion v
        JOIN v.producto p
        WHERE p.vendedor.idVendedor = :idVendedor
        ORDER BY v.fechaValoracion DESC
        LIMIT 10
    """)
    List<Valoracion> findTop10ByVendedorOrderByFechaValoracionDesc(@Param("idVendedor") Integer idVendedor);

}
