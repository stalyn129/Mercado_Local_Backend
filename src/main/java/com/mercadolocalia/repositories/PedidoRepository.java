package com.mercadolocalia.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.Consumidor;
import com.mercadolocalia.entities.Vendedor;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByConsumidor(Consumidor consumidor);

    List<Pedido> findByVendedor(Vendedor vendedor);
}
