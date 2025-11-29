package com.mercadolocalia.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.Consumidor;
import com.mercadolocalia.entities.Vendedor;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    // ========================= 🔥 PARA VENDEDOR SERVICE =========================
    
    List<Pedido> findByConsumidor(Consumidor consumidor);
    List<Pedido> findByVendedor(Vendedor vendedor);
    List<Pedido> findTop10ByVendedorOrderByFechaPedidoDesc(Vendedor vendedor);
    Integer countByVendedor(Vendedor vendedor);

    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.vendedor.idVendedor = :vendedorId")
    Double sumarIngresosPorVendedor(Integer vendedorId);

    // ========================= 🔥 PARA ADMIN DASHBOARD =========================

    @Query("SELECT SUM(p.total) FROM Pedido p")
    Double sumTotalVentas();

    @Query("SELECT SUM(p.total) FROM Pedido p WHERE MONTH(p.fechaPedido) = MONTH(CURRENT_DATE)")
    Double sumVentasMesActual();

    List<Pedido> findTop5ByOrderByIdPedidoDesc();
}
