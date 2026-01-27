package com.mercadolocalia.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.Consumidor;
import com.mercadolocalia.entities.EstadoPedido;
import com.mercadolocalia.entities.EstadoPago;
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.entities.EstadoPedidoVendedor;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

	// ============================================================
	// 🔥 NUEVOS MÉTODOS PARA VERIFICACIÓN DE PAGOS
	// ============================================================

	// 🔥 Buscar pedidos por vendedor y estado de pago
	List<Pedido> findByVendedor_IdVendedorAndEstadoPago(Integer idVendedor, EstadoPago estadoPago);

	// 🔥 Buscar pedidos en verificación por vendedor
	@Query("SELECT p FROM Pedido p WHERE p.vendedor.idVendedor = :vendedorId AND p.estadoPago = 'EN_VERIFICACION'")
	List<Pedido> findPendientesVerificacionByVendedor(@Param("vendedorId") Integer vendedorId);

	// 🔥 Buscar pedidos por consumidor y estado de pago
	List<Pedido> findByConsumidor_IdConsumidorAndEstadoPago(Integer idConsumidor, EstadoPago estadoPago);

	// 🔥 Buscar pedidos con comprobante pendiente de verificación
	@Query("SELECT p FROM Pedido p WHERE p.comprobanteUrl IS NOT NULL AND p.estadoPago = 'EN_VERIFICACION'")
	List<Pedido> findConComprobantePendiente();

	// 🔥 Buscar pedidos rechazados que pueden re-subir comprobante
	@Query("SELECT p FROM Pedido p WHERE p.estadoPago = 'RECHAZADO' AND p.comprobanteUrl IS NULL")
	List<Pedido> findRechazadosSinComprobante();

	// 🔥 Contar pedidos pendientes de verificación por vendedor
	Integer countByVendedor_IdVendedorAndEstadoPago(Integer idVendedor, EstadoPago estadoPago);

	// 🔥 Buscar pedidos verificados recientemente (últimos 7 días)
	@Query("SELECT p FROM Pedido p WHERE p.fechaVerificacionPago >= :fechaInicio AND p.estadoPago = 'PAGADO'")
	List<Pedido> findVerificadosRecientemente(@Param("fechaInicio") LocalDateTime fechaInicio);

	// 🔥 Buscar pedidos con filtros avanzados para dashboard del vendedor
	@Query("""
			    SELECT p FROM Pedido p
			    WHERE p.vendedor.idVendedor = :vendedorId
			    AND (:estadoPago IS NULL OR p.estadoPago = :estadoPago)
			    AND (:estadoPedido IS NULL OR p.estadoPedido = :estadoPedido)
			    AND (:fechaDesde IS NULL OR p.fechaPedido >= :fechaDesde)
			    AND (:fechaHasta IS NULL OR p.fechaPedido <= :fechaHasta)
			    ORDER BY p.fechaPedido DESC
			""")
	List<Pedido> findPedidosConFiltrosVendedor(@Param("vendedorId") Integer vendedorId,
			@Param("estadoPago") EstadoPago estadoPago, @Param("estadoPedido") EstadoPedido estadoPedido,
			@Param("fechaDesde") LocalDateTime fechaDesde, @Param("fechaHasta") LocalDateTime fechaHasta);

	// ============================================================
	// 🔥 MÉTODOS PARA COMPRA UNIFICADA
	// ============================================================

	List<Pedido> findByIdCompraUnificada(String idCompraUnificada);

	List<Pedido> findByIdCompraUnificadaAndConsumidor_IdConsumidor(String idCompraUnificada, Integer idConsumidor);

	List<Pedido> findByIdCompraUnificadaOrderByFechaPedidoDesc(String idCompraUnificada);

	@Query("SELECT p FROM Pedido p WHERE p.idCompraUnificada = :idCompraUnificada")
	List<Pedido> buscarPorIdCompraUnificada(@Param("idCompraUnificada") String idCompraUnificada);

	@Query("""
			    SELECT p FROM Pedido p
			    WHERE p.idCompraUnificada = :idCompraUnificada
			    AND p.consumidor.idConsumidor = :idConsumidor
			    ORDER BY p.fechaPedido DESC
			""")
	List<Pedido> buscarPorIdCompraYConsumidor(@Param("idCompraUnificada") String idCompraUnificada,
			@Param("idConsumidor") Integer idConsumidor);

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
	// VENTAS POR CATEGORÍA
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

	// 🔥 VENTAS MENSUALES CON ESTADO DE PAGO
	@Query("""
			    SELECT
			        MONTH(p.fechaPedido) as mes,
			        SUM(p.total) as total,
			        p.estadoPago as estadoPago
			    FROM Pedido p
			    WHERE p.vendedor.idVendedor = :idVendedor
			      AND p.estadoPago = 'PAGADO'
			    GROUP BY MONTH(p.fechaPedido), p.estadoPago
			    ORDER BY MONTH(p.fechaPedido)
			""")
	List<Object[]> obtenerVentasMensualesPagadasPorVendedor(Integer idVendedor);

	// ============================================================
	// CONSULTAS POR CONSUMIDOR
	// ============================================================

	List<Pedido> findByConsumidor_IdConsumidor(Integer idConsumidor);

	List<Pedido> findByConsumidorAndTotalGreaterThanAndEstadoPedidoNot(Consumidor consumidor, Double total,
			EstadoPedido estado);

	List<Pedido> findByConsumidor_IdConsumidorOrderByFechaPedidoDesc(Integer idConsumidor);

	// 🔥 PEDIDOS POR CONSUMIDOR CON ESTADO DE PAGO
	List<Pedido> findByConsumidor_IdConsumidorAndEstadoPagoOrderByFechaPedidoDesc(Integer idConsumidor,
			EstadoPago estadoPago);

	// ============================================================
	// CONSULTAS POR VENDEDOR Y ESTADO
	// ============================================================

	List<Pedido> findByVendedor_IdVendedorAndEstadoPedido(Integer idVendedor, EstadoPedido estado);

	// 🔥 CON ESTADO DE VENDEDOR
	List<Pedido> findByVendedor_IdVendedorAndEstadoPedidoVendedor(Integer idVendedor,
			EstadoPedidoVendedor estadoPedidoVendedor);

	// 🔥 CON MÚLTIPLES ESTADOS DE PAGO
	@Query("SELECT p FROM Pedido p WHERE p.vendedor.idVendedor = :vendedorId AND p.estadoPago IN :estados")
	List<Pedido> findByVendedorAndEstadoPagoIn(@Param("vendedorId") Integer vendedorId,
			@Param("estados") List<EstadoPago> estados);

	// ============================================================
	// CONTEO POR ESTADOS
	// ============================================================

	Integer countByVendedor_IdVendedorAndEstadoPedido(Integer idVendedor, EstadoPedido estado);

	// 🔥 CONTEO POR ESTADO DE PAGO
	// Integer countByVendedor_IdVendedorAndEstadoPago(Integer idVendedor,
	// EstadoPago estadoPago);

	// 🔥 CONTEO POR MÚLTIPLES ESTADOS DE PAGO
	@Query("SELECT COUNT(p) FROM Pedido p WHERE p.vendedor.idVendedor = :vendedorId AND p.estadoPago IN :estados")
	Integer countByVendedorAndEstadoPagoIn(@Param("vendedorId") Integer vendedorId,
			@Param("estados") List<EstadoPago> estados);

	// ============================================================
	// CONSULTAS ADICIONALES ÚTILES
	// ============================================================

	List<Pedido> findByConsumidor_IdConsumidorAndEstadoPedido(Integer idConsumidor, EstadoPedido estado);

	@Query("SELECT p FROM Pedido p WHERE p.consumidor.idConsumidor = :idConsumidor ORDER BY p.fechaPedido DESC LIMIT :limit")
	List<Pedido> findRecentByConsumidor(@Param("idConsumidor") Integer idConsumidor, @Param("limit") int limit);

	@Query("SELECT SUM(p.total) FROM Pedido p WHERE p.consumidor.idConsumidor = :idConsumidor")
	Double sumTotalByConsumidor(@Param("idConsumidor") Integer idConsumidor);

	// 🔥 SUMA POR ESTADO DE PAGO
	@Query("SELECT SUM(p.total) FROM Pedido p WHERE p.consumidor.idConsumidor = :idConsumidor AND p.estadoPago = :estadoPago")
	Double sumTotalByConsumidorAndEstadoPago(@Param("idConsumidor") Integer idConsumidor,
			@Param("estadoPago") EstadoPago estadoPago);

	// ============================================================
	// CONSULTAS CON FILTROS COMBINADOS
	// ============================================================

	@Query("""
			    SELECT p FROM Pedido p
			    WHERE (:idConsumidor IS NULL OR p.consumidor.idConsumidor = :idConsumidor)
			    AND (:idVendedor IS NULL OR p.vendedor.idVendedor = :idVendedor)
			    AND (:estadoPedido IS NULL OR p.estadoPedido = :estadoPedido)
			    AND (:estadoPago IS NULL OR p.estadoPago = :estadoPago)
			    AND (:idCompraUnificada IS NULL OR p.idCompraUnificada = :idCompraUnificada)
			    AND (:fechaDesde IS NULL OR p.fechaPedido >= :fechaDesde)
			    AND (:fechaHasta IS NULL OR p.fechaPedido <= :fechaHasta)
			    ORDER BY p.fechaPedido DESC
			""")
	List<Pedido> findWithFilters(@Param("idConsumidor") Integer idConsumidor, @Param("idVendedor") Integer idVendedor,
			@Param("estadoPedido") EstadoPedido estadoPedido, @Param("estadoPago") EstadoPago estadoPago,
			@Param("idCompraUnificada") String idCompraUnificada, @Param("fechaDesde") LocalDateTime fechaDesde,
			@Param("fechaHasta") LocalDateTime fechaHasta);

	// 🔥 CONSULTA ESPECÍFICA PARA DASHBOARD DE VENDEDOR
	@Query("""
			    SELECT
			        COUNT(p) as totalPedidos,
			        SUM(CASE WHEN p.estadoPago = 'PAGADO' THEN 1 ELSE 0 END) as pagados,
			        SUM(CASE WHEN p.estadoPago = 'EN_VERIFICACION' THEN 1 ELSE 0 END) as pendientesVerificacion,
			        SUM(CASE WHEN p.estadoPago = 'RECHAZADO' THEN 1 ELSE 0 END) as rechazados,
			        SUM(p.total) as ingresosTotales
			    FROM Pedido p
			    WHERE p.vendedor.idVendedor = :vendedorId
			    AND p.fechaPedido >= :fechaInicio
			""")
	Map<String, Object> obtenerEstadisticasVendedor(@Param("vendedorId") Integer vendedorId,
			@Param("fechaInicio") LocalDateTime fechaInicio);

	// ============================================================
	// CONSULTAS TEMPORALES
	// ============================================================

	Long countByFechaPedido(LocalDate fecha);

	Long countByFechaPedidoBetween(LocalDate inicio, LocalDate fin);

	// 🔥 POR FECHAS CON ESTADO DE PAGO
	@Query("SELECT COUNT(p) FROM Pedido p WHERE DATE(p.fechaPedido) = :fecha AND p.estadoPago = :estadoPago")
	Long countByFechaPedidoAndEstadoPago(@Param("fecha") LocalDate fecha, @Param("estadoPago") EstadoPago estadoPago);

	// 🔥 ENTRE FECHAS CON ESTADO DE PAGO
	@Query("SELECT COUNT(p) FROM Pedido p WHERE DATE(p.fechaPedido) BETWEEN :inicio AND :fin AND p.estadoPago = :estadoPago")
	Long countByFechaPedidoBetweenAndEstadoPago(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin,
			@Param("estadoPago") EstadoPago estadoPago);

	// 🔥 PEDIDOS CON COMPROBANTE POR FECHA
	@Query("SELECT p FROM Pedido p WHERE p.comprobanteUrl IS NOT NULL AND DATE(p.fechaSubidaComprobante) = :fecha")
	List<Pedido> findConComprobantePorFecha(@Param("fecha") LocalDate fecha);

	// Graficos vendedor

	// ============================================================
    // 🔥 CONSULTAS CORREGIDAS PARA TU ESTRUCTURA
    // ============================================================

    // 1. ESTADÍSTICAS BÁSICAS DEL VENDEDOR
    @Query("""
        SELECT new map(
            COUNT(ped) as totalPedidos,
            SUM(CASE WHEN ped.estadoPago = 'PAGADO' THEN ped.total ELSE 0 END) as ingresosTotales,
            AVG(CASE WHEN ped.estadoPago = 'PAGADO' THEN ped.total ELSE NULL END) as promedioVenta,
            COUNT(DISTINCT ped.consumidor.idConsumidor) as clientesUnicos
        )
        FROM Pedido ped
        WHERE ped.vendedor.idVendedor = :idVendedor
          AND ped.estadoPedido = 'COMPLETADO'
    """)
    Map<String, Object> obtenerEstadisticasVendedor(@Param("idVendedor") Integer idVendedor);

    // 2. PRODUCTOS TOP POR VENDEDOR
    @Query("""
        SELECT new map(
            p.nombreProducto as producto,
            SUM(d.cantidad) as cantidadVendida,
            SUM(d.subtotal) as totalVentas
        )
        FROM Pedido ped
        JOIN ped.detalles d
        JOIN d.producto p
        WHERE ped.vendedor.idVendedor = :idVendedor
          AND ped.estadoPedido = 'COMPLETADO'
          AND ped.estadoPago = 'PAGADO'
        GROUP BY p.idProducto, p.nombreProducto
        ORDER BY totalVentas DESC
    """)
    List<Map<String, Object>> obtenerProductosTopPorVendedor(@Param("idVendedor") Integer idVendedor);

    // 3. CLIENTES RECURRENTES - CORREGIDO
    @Query("""
        SELECT new map(
            CONCAT(c.usuario.nombre, ' ', c.usuario.apellido) as cliente,
            COUNT(ped) as totalPedidos,
            SUM(ped.total) as totalGastado,
            MAX(ped.fechaPedido) as ultimaCompra
        )
        FROM Pedido ped
        JOIN ped.consumidor c
        WHERE ped.vendedor.idVendedor = :idVendedor
          AND ped.estadoPedido = 'COMPLETADO'
          AND ped.estadoPago = 'PAGADO'
        GROUP BY c.idConsumidor, c.usuario.nombre, c.usuario.apellido
        HAVING COUNT(ped) > 1
        ORDER BY totalGastado DESC
    """)
    List<Map<String, Object>> obtenerClientesRecurrentesPorVendedor(@Param("idVendedor") Integer idVendedor);

    // 4. ESTADO DE PEDIDOS
    @Query("""
        SELECT 
            ped.estadoPedido as estado,
            COUNT(ped) as cantidad
        FROM Pedido ped
        WHERE ped.vendedor.idVendedor = :idVendedor
        GROUP BY ped.estadoPedido
    """)
    List<Object[]> obtenerPedidosPorEstado(@Param("idVendedor") Integer idVendedor);

    // 5. TENDENCIA DE VENTAS (ÚLTIMOS 30 DÍAS)
    @Query("""
        SELECT 
            DATE(ped.fechaPedido) as fecha,
            COUNT(ped) as pedidos,
            SUM(ped.total) as ventas
        FROM Pedido ped
        WHERE ped.vendedor.idVendedor = :idVendedor
          AND ped.estadoPedido = 'COMPLETADO'
          AND ped.estadoPago = 'PAGADO'
          AND ped.fechaPedido >= :fechaInicio
        GROUP BY DATE(ped.fechaPedido)
        ORDER BY fecha
    """)
    List<Object[]> obtenerTendenciaVentas(
        @Param("idVendedor") Integer idVendedor,
        @Param("fechaInicio") LocalDateTime fechaInicio
    );

    // 6. VENTAS MENSUALES
    @Query("""
        SELECT 
            MONTH(ped.fechaPedido) as mes,
            SUM(ped.total) as total
        FROM Pedido ped
        WHERE ped.vendedor.idVendedor = :idVendedor
          AND ped.estadoPedido = 'COMPLETADO'
          AND ped.estadoPago = 'PAGADO'
        GROUP BY MONTH(ped.fechaPedido)
        ORDER BY MONTH(ped.fechaPedido)
    """)
    List<Object[]> obtenerVentasMensualesPorVendedor1(@Param("idVendedor") Integer idVendedor);

}