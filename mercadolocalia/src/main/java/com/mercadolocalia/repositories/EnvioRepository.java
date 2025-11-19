package com.mercadolocalia.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadolocalia.entities.Envio;
import com.mercadolocalia.entities.Pedido;

public interface EnvioRepository extends JpaRepository<Envio, Integer> {

    Envio findByPedido(Pedido pedido);
}
