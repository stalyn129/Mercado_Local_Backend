package com.mercadolocalia.services;

import com.mercadolocalia.dto.DetallePedidoAddRequest;
import com.mercadolocalia.dto.DetallePedidoUpdateRequest;
import com.mercadolocalia.entities.DetallePedido;

public interface DetallePedidoService {

    DetallePedido agregarDetalle(DetallePedidoAddRequest request);

    DetallePedido editarDetalle(Integer idDetalle, DetallePedidoUpdateRequest request);

    void eliminarDetalle(Integer idDetalle);
}
