package com.mercadolocalia.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import com.mercadolocalia.dto.ProductoPublicDTO;
import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.entities.Subcategoria;
import com.mercadolocalia.entities.Vendedor;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // ============================================================
    // 🔥 CONSULTAS GENERALES
    // ============================================================

    List<Producto> findByVendedor(Vendedor vendedor);

    List<Producto> findBySubcategoria(Subcategoria subcategoria);

    List<Producto> findByEstado(String estado);

    Integer countByVendedor(Vendedor vendedor);
    
    List<Producto> findBySubcategoria_NombreSubcategoriaContainingIgnoreCase(String nombre);
        
    List<Producto> findByNombreProductoContainingIgnoreCaseOrSubcategoria_NombreSubcategoriaContainingIgnoreCase(
    String nombre, String subcategoria
    );
    
    List<Producto> findByNombreProductoContainingIgnoreCaseOrSubcategoria_NombreSubcategoriaContainingIgnoreCaseOrSubcategoria_Categoria_NombreCategoriaContainingIgnoreCase(
            String nombre, String subcategoria, String categoria
        );
  
    

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.vendedor.idVendedor = :id AND p.estado = 'Disponible'")
    Integer contarDisponiblesPorVendedor(Integer id);


    // ============================================================
    // 🔥 TOP 20 MEJORES CALIFICADOS
    // ============================================================

    @Query("""
        SELECT p 
        FROM Producto p 
        LEFT JOIN p.valoraciones v
        GROUP BY p.idProducto
        ORDER BY AVG(v.calificacion) DESC, COUNT(v) DESC
    """)
    List<Producto> findTop20Mejores(Pageable pageable);


    // ============================================================
    // 🔥 REPORTE ADMIN — STOCK DE PRODUCTOS
    // ============================================================

    @Query("""
        SELECT new map(
            p.nombreProducto as producto,
            p.stockProducto as stock
        )
        FROM Producto p
    """)
    List<Map<String, Object>> obtenerStockProductos();
    
    
    @Query("""
    	    SELECT new com.mercadolocalia.dto.ProductoPublicDTO(
    	        p.idProducto,
    	        p.nombreProducto,
    	        p.precioProducto,
    	        p.imagenProducto,
    	        s.nombreSubcategoria,
    	        COALESCE(AVG(v.calificacion), 0),
    	        COUNT(v),
    	        p.vendedor.idVendedor
    	    )
    	    FROM Producto p
    	    LEFT JOIN p.subcategoria s
    	    LEFT JOIN p.valoraciones v
    	    WHERE p.vendedor.idVendedor = :idVendedor
    	      AND p.estado = 'Disponible'
    	    GROUP BY p.idProducto, s.nombreSubcategoria, p.vendedor.idVendedor
    	""")
    	List<ProductoPublicDTO> obtenerProductosPublicosPorVendedor(
    	        @Param("idVendedor") Integer idVendedor
    	);

    // ============================================================
    // 🔥 CORRECCIÓN: Usa fechaPublicacion (LocalDateTime) no fechaCreacion
    // ============================================================
    
    // Método 1: Usando LocalDateTime (recomendado si tienes fechaPublicacion)
    Long countByFechaPublicacionBetween(LocalDateTime inicio, LocalDateTime fin);
    
    // Método 2: Si necesitas LocalDate, crea un método custom
    @Query("SELECT COUNT(p) FROM Producto p WHERE DATE(p.fechaPublicacion) BETWEEN :inicio AND :fin")
    Long countByFechaPublicacionDateBetween(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}