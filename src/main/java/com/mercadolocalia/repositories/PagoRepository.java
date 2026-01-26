package com.mercadolocalia.repositories;

import com.mercadolocalia.entities.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {
    
    // Encontrar pago por ID de pedido
    Optional<Pago> findByPedidoIdPedido(Integer idPedido);
    
    // Encontrar pagos por consumidor
    List<Pago> findByIdConsumidor(Integer idConsumidor);
    
    // Encontrar pagos por estado
    List<Pago> findByEstado(com.mercadolocalia.entities.EstadoPago estado);
    
    // Encontrar pagos por método de pago
    List<Pago> findByMetodo(com.mercadolocalia.entities.MetodoPago metodo);
    
    // Encontrar pagos por consumidor y estado
    List<Pago> findByIdConsumidorAndEstado(Integer idConsumidor, com.mercadolocalia.entities.EstadoPago estado);
    
    // Encontrar pagos por fecha
    @Query("SELECT p FROM Pago p WHERE DATE(p.fechaPago) = :fecha")
    List<Pago> findByFechaPago(@Param("fecha") java.time.LocalDate fecha);
    
    // Encontrar pagos entre fechas
    @Query("SELECT p FROM Pago p WHERE p.fechaPago BETWEEN :inicio AND :fin")
    List<Pago> findByFechaPagoBetween(@Param("inicio") java.time.LocalDateTime inicio, 
                                       @Param("fin") java.time.LocalDateTime fin);
    
    // Contar pagos por estado
    Long countByEstado(com.mercadolocalia.entities.EstadoPago estado);
    
    // Sumar montos por estado
    @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.estado = :estado")
    Double sumMontoByEstado(@Param("estado") com.mercadolocalia.entities.EstadoPago estado);
    
    // Obtener estadísticas de pagos
    @Query("""
        SELECT 
            COUNT(p) as totalPagos,
            SUM(CASE WHEN p.estado = 'PAGADO' THEN 1 ELSE 0 END) as pagados,
            SUM(CASE WHEN p.estado = 'EN_VERIFICACION' THEN 1 ELSE 0 END) as enVerificacion,
            SUM(CASE WHEN p.estado = 'RECHAZADO' THEN 1 ELSE 0 END) as rechazados,
            SUM(p.monto) as montoTotal,
            SUM(CASE WHEN p.estado = 'PAGADO' THEN p.monto ELSE 0 END) as montoPagado
        FROM Pago p
    """)
    Object[] obtenerEstadisticasPagos();
}