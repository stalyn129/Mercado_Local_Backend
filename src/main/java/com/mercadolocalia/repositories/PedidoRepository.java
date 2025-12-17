package com.mercadolocalia.repositories;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.Consumidor;
import com.mercadolocalia.entities.Vendedor;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    // ============================================================
    // 🔥 CONSULTAS PARA VENDEDOR SERVICE
    // ============================================================

    List<Pedido> findByConsumidor(Consumidor consumidor);

    List<Pedido> findByVendedor(Vendedor vendedor);

    List<Pedido> findTop10ByVendedorOrderByFechaPedidoDesc(Vendedor vendedor);

    Integer countByVendedor(Vendedor vendedor);

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


}
