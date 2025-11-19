package com.mercadolocalia.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadolocalia.entities.DetallePedido;
import com.mercadolocalia.entities.Pedido;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {

    List<DetallePedido> findByPedido(Pedido pedido);
}
