package com.mercadolocalia.services;

import java.util.List;

import com.mercadolocalia.dto.PedidoRequest;
import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.DetallePedido;

public interface PedidoService {

    Pedido crearPedido(PedidoRequest request);

    Pedido obtenerPedidoPorId(Integer id);

    List<Pedido> listarPedidosPorConsumidor(Integer idConsumidor);

    List<Pedido> listarPedidosPorVendedor(Integer idVendedor);

    List<DetallePedido> listarDetalles(Integer idPedido);

    Pedido cambiarEstado(Integer idPedido, String nuevoEstado);
    
    Pedido comprarAhora(PedidoRequest request);
    
    Pedido finalizarPedido(Integer idPedido, String metodoPago);

}
