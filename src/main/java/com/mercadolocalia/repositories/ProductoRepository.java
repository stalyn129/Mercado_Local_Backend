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
    // 🔥 MÉTODOS NUEVOS PARA VERIFICAR PRODUCTOS ASOCIADOS
    // ============================================================
    
    // 1. Verificar si hay productos con una categoría específica
    @Query("SELECT COUNT(p) > 0 FROM Producto p " +
           "JOIN p.subcategoria sc " +
           "JOIN sc.categoria c " +
           "WHERE c.idCategoria = :idCategoria")
    boolean existsByCategoriaIdCategoria(@Param("idCategoria") Integer idCategoria);
    
    // 2. Verificar si hay productos con una subcategoría específica
    @Query("SELECT COUNT(p) > 0 FROM Producto p " +
           "WHERE p.subcategoria.idSubcategoria = :idSubcategoria")
    boolean existsBySubcategoriaIdSubcategoria(@Param("idSubcategoria") Integer idSubcategoria);
    
    // 3. Método alternativo si necesitas contar (no solo boolean)
    @Query("SELECT COUNT(p) FROM Producto p " +
           "JOIN p.subcategoria sc " +
           "JOIN sc.categoria c " +
           "WHERE c.idCategoria = :idCategoria")
    Long countByCategoriaIdCategoria(@Param("idCategoria") Integer idCategoria);
    
    @Query("SELECT COUNT(p) FROM Producto p " +
           "WHERE p.subcategoria.idSubcategoria = :idSubcategoria")
    Long countBySubcategoriaIdSubcategoria(@Param("idSubcategoria") Integer idSubcategoria);
    
    // 4. Obtener productos por categoría (para debug o información)
    @Query("SELECT p FROM Producto p " +
           "JOIN p.subcategoria sc " +
           "JOIN sc.categoria c " +
           "WHERE c.idCategoria = :idCategoria")
    List<Producto> findByCategoriaIdCategoria(@Param("idCategoria") Integer idCategoria);
    
    // 5. Obtener productos por subcategoría (para debug o información)
    List<Producto> findBySubcategoriaIdSubcategoria(Integer idSubcategoria);
    
    // 6. Método simplificado - si Producto tuviera relación directa con Categoria
    // @Query("SELECT COUNT(p) > 0 FROM Producto p WHERE p.categoria.idCategoria = :idCategoria")
    // boolean existsByCategoriaIdCategoria(@Param("idCategoria") Integer idCategoria);

    // ============================================================
    // 🔥 CONSULTAS GENERALES EXISTENTES (las que ya tienes)
    // ============================================================

    List<Producto> findByVendedor(Vendedor vendedor);

    List<Producto> findBySubcategoria(Subcategoria subcategoria);

    List<Producto> findByEstado(String estado);

    Integer countByVendedor(Vendedor vendedor);

    List<Producto> findBySubcategoria_NombreSubcategoriaContainingIgnoreCase(String nombre);

    List<Producto> findByNombreProductoContainingIgnoreCaseOrSubcategoria_NombreSubcategoriaContainingIgnoreCase(
            String nombre, String subcategoria);

    List<Producto> findByNombreProductoContainingIgnoreCaseOrSubcategoria_NombreSubcategoriaContainingIgnoreCaseOrSubcategoria_Categoria_NombreCategoriaContainingIgnoreCase(
            String nombre, String subcategoria, String categoria);

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
    List<ProductoPublicDTO> obtenerProductosPublicosPorVendedor(@Param("idVendedor") Integer idVendedor);


    // Método 1: Usando LocalDateTime (recomendado si tienes fechaPublicacion)
    Long countByFechaPublicacionBetween(LocalDateTime inicio, LocalDateTime fin);

    // Método 2: Si necesitas LocalDate, crea un método custom
    @Query("SELECT COUNT(p) FROM Producto p WHERE DATE(p.fechaPublicacion) BETWEEN :inicio AND :fin")
    Long countByFechaPublicacionDateBetween(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    // Reportes graficos vendedor
    // 1. STOCK BAJO
    @Query("""
        SELECT new map(
            p.nombreProducto as producto,
            p.stockProducto as stock,
            sc.nombreSubcategoria as subcategoria,
            c.nombreCategoria as categoria
        )
        FROM Producto p
        JOIN p.subcategoria sc
        JOIN sc.categoria c
        WHERE p.vendedor.idVendedor = :idVendedor
          AND p.stockProducto <= 5
          AND p.estado = 'Disponible'
    """)
    List<Map<String, Object>> obtenerProductosStockBajo(@Param("idVendedor") Integer idVendedor);

    // 2. PRODUCTOS POR CATEGORÍA
    @Query("""
        SELECT new map(
            c.nombreCategoria as categoria,
            COUNT(p) as cantidadProductos
        )
        FROM Producto p
        JOIN p.subcategoria sc
        JOIN sc.categoria c
        WHERE p.vendedor.idVendedor = :idVendedor
          AND p.estado = 'Disponible'
        GROUP BY c.idCategoria, c.nombreCategoria
    """)
    List<Map<String, Object>> obtenerProductosPorCategoria(@Param("idVendedor") Integer idVendedor);

    // 3. PRODUCTOS SIN VENTAS
    @Query("""
        SELECT new map(
            p.nombreProducto as producto,
            p.precioProducto as precio,
            p.stockProducto as stock,
            sc.nombreSubcategoria as subcategoria
        )
        FROM Producto p
        JOIN p.subcategoria sc
        WHERE p.vendedor.idVendedor = :idVendedor
          AND p.estado = 'Disponible'
          AND NOT EXISTS (
              SELECT 1 FROM Pedido ped
              JOIN ped.detalles d
              WHERE d.producto.idProducto = p.idProducto
          )
    """)
    List<Map<String, Object>> obtenerProductosSinVentas(@Param("idVendedor") Integer idVendedor);
}