package com.mercadolocalia.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.Consumidor;
import com.mercadolocalia.entities.EstadoPedido;
import com.mercadolocalia.entities.Vendedor;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    // ============================================================
    // 🔥 NUEVOS MÉTODOS PARA COMPRA UNIFICADA
    // ============================================================
    
    // Buscar pedidos por idCompraUnificada
    List<Pedido> findByIdCompraUnificada(String idCompraUnificada);
    
    // Buscar pedidos por idCompraUnificada y consumidor (más seguro)
    List<Pedido> findByIdCompraUnificadaAndConsumidor_IdConsumidor(String idCompraUnificada, Integer idConsumidor);
    
    // Buscar pedidos por idCompraUnificada ordenados por fecha
    List<Pedido> findByIdCompraUnificadaOrderByFechaPedidoDesc(String idCompraUnificada);
    
    // Consulta personalizada para buscar por idCompraUnificada (alternativa)
    @Query("SELECT p FROM Pedido p WHERE p.idCompraUnificada = :idCompraUnificada")
    List<Pedido> buscarPorIdCompraUnificada(@Param("idCompraUnificada") String idCompraUnificada);
    
    // Consulta personalizada para buscar por idCompraUnificada y consumidor
    @Query("""
        SELECT p FROM Pedido p 
        WHERE p.idCompraUnificada = :idCompraUnificada 
        AND p.consumidor.idConsumidor = :idConsumidor
        ORDER BY p.fechaPedido DESC
    """)
    List<Pedido> buscarPorIdCompraYConsumidor(
        @Param("idCompraUnificada") String idCompraUnificada,
        @Param("idConsumidor") Integer idConsumidor
    );

    // ============================================================
    // 🔥 CONSULTAS PARA VENDEDOR SERVICE
    // ============================================================

    List<Pedido> findByConsumidor(Consumidor consumidor);

    List<Pedido> findByVendedor(Vendedor vendedor);

    List<Pedido> findTop10ByVendedorOrderByFechaPedidoDesc(Vendedor vendedor);

    Integer countByVendedor(Vendedor vendedor);
    
    int countByConsumidor_IdConsumidor(Integer idConsumidor);


    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.vendedor.idVendedor = :vendedorId")
    Double sumarIngresosPorVendedor(Integer vendedorId);


    // ============================================================
    // 🔥 CONSULTAS PARA ADMIN / DASHBOARD
    // ============================================================

    @Query("SELECT SUM(p.total) FROM Pedido p")
    Double sumTotalVentas();

    @Query("SELECT SUM(p.total) FROM Pedido p WHERE MONTH(p.fechaPedido) = MONTH(CURRENT_DATE)")
    Double sumVentasMesActual();

    List<Pedido> findTop5ByOrderByIdPedidoDesc();


    // ============================================================
    // VENTAS POR CATEGORÍA (REPORTES PROFESIONALES)
    // ============================================================

    @Query("""
        SELECT new map(
            c.nombreCategoria as categoria,
            SUM(dp.subtotal) as totalVentas
        )
        FROM Pedido p
        JOIN p.detalles dp
        JOIN dp.producto pr
        JOIN pr.subcategoria sc
        JOIN sc.categoria c
        GROUP BY c.nombreCategoria
    """)
    List<Map<String, Object>> obtenerVentasPorCategoria();
    
    // ============================================================
    // VENTAS MENSUALES
    // ============================================================
    
    
    @Query("""
    	    SELECT 
    	        MONTH(p.fechaPedido) as mes,
    	        SUM(p.total) as total
    	    FROM Pedido p
    	    WHERE p.vendedor.idVendedor = :idVendedor
    	      AND p.estadoPedido = 'COMPLETADO'
    	    GROUP BY MONTH(p.fechaPedido)
    	    ORDER BY MONTH(p.fechaPedido)
    	""")
    	List<Object[]> obtenerVentasMensualesPorVendedor(Integer idVendedor);

    List<Pedido> findByConsumidor_IdConsumidor(Integer idConsumidor);

    List<Pedido> findByConsumidorAndTotalGreaterThanAndEstadoPedidoNot(
        Consumidor consumidor,
        Double total,
        EstadoPedido estado
    );

    List<Pedido> findByConsumidor_IdConsumidorOrderByFechaPedidoDesc(Integer idConsumidor);
    
    // ============================================================
    // 🔥 CONSULTAS ADICIONALES ÚTILES
    // ============================================================
    
    // Buscar pedidos por consumidor y estado
    List<Pedido> findByConsumidor_IdConsumidorAndEstadoPedido(Integer idConsumidor, EstadoPedido estado);
    
    // Buscar pedidos por vendedor y estado
    List<Pedido> findByVendedor_IdVendedorAndEstadoPedido(Integer idVendedor, EstadoPedido estado);
    
    // Contar pedidos por vendedor y estado
    Integer countByVendedor_IdVendedorAndEstadoPedido(Integer idVendedor, EstadoPedido estado);
    
    // Obtener pedidos recientes por consumidor
    @Query("SELECT p FROM Pedido p WHERE p.consumidor.idConsumidor = :idConsumidor ORDER BY p.fechaPedido DESC LIMIT :limit")
    List<Pedido> findRecentByConsumidor(@Param("idConsumidor") Integer idConsumidor, @Param("limit") int limit);
    
    // Obtener total de ventas por consumidor
    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.consumidor.idConsumidor = :idConsumidor")
    Double sumTotalByConsumidor(@Param("idConsumidor") Integer idConsumidor);
    
    // Obtener pedidos con filtros combinados
    @Query("""
        SELECT p FROM Pedido p 
        WHERE (:idConsumidor IS NULL OR p.consumidor.idConsumidor = :idConsumidor)
        AND (:idVendedor IS NULL OR p.vendedor.idVendedor = :idVendedor)
        AND (:estado IS NULL OR p.estadoPedido = :estado)
        AND (:idCompraUnificada IS NULL OR p.idCompraUnificada = :idCompraUnificada)
        ORDER BY p.fechaPedido DESC
    """)
    List<Pedido> findWithFilters(
        @Param("idConsumidor") Integer idConsumidor,
        @Param("idVendedor") Integer idVendedor,
        @Param("estado") EstadoPedido estado,
        @Param("idCompraUnificada") String idCompraUnificada
    );
    
 // NUEVOS MÉTODOS:
    Long countByFechaPedido(LocalDate fecha);
    
    Long countByFechaPedidoBetween(LocalDate inicio, LocalDate fin);
}