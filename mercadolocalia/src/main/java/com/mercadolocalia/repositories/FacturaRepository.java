package com.mercadolocalia.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadolocalia.entities.Factura;
import com.mercadolocalia.entities.Pedido;

public interface FacturaRepository extends JpaRepository<Factura, Integer> {

    Factura findByPedido(Pedido pedido);

    boolean existsByNumeroFactura(String numeroFactura);
}
