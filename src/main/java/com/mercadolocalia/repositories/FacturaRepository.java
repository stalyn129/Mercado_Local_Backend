package com.mercadolocalia.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mercadolocalia.entities.Factura;
import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.Consumidor;
import java.util.List;
import java.util.Optional;

public interface FacturaRepository extends JpaRepository<Factura, Integer> {

    Factura findByPedido(Pedido pedido);
    
    List<Factura> findByConsumidor(Consumidor consumidor);

    boolean existsByNumeroFactura(String numeroFactura);
    
    Optional<Factura> findFirstByOrderByIdFacturaDesc();
}