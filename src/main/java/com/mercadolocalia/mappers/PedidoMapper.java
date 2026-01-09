package com.mercadolocalia.mappers;

import com.mercadolocalia.dto.PedidoResponse;
import com.mercadolocalia.entities.Pedido;

public class PedidoMapper {

    // 🔁 ENTIDAD -> RESPONSE
    public static PedidoResponse toResponse(Pedido pedido) {

        if (pedido == null) {
            return null;
        }

        PedidoResponse response = new PedidoResponse();

        // ================= IDENTIDAD =================
        response.setIdPedido(pedido.getIdPedido());
        response.setNumeroPedido("PED-" + pedido.getIdPedido());

        // ================= ESTADOS =================
        if (pedido.getEstadoPedido() != null) {
            response.setEstadoPedido(pedido.getEstadoPedido().name());
        }

        if (pedido.getEstadoSeguimiento() != null) {
            response.setEstadoSeguimiento(pedido.getEstadoSeguimiento().name());
        }

        // ================= MONTOS =================
        response.setSubtotal(pedido.getSubtotal());
        response.setIva(pedido.getIva());
        response.setTotal(pedido.getTotal());

        // ================= FECHA =================
        response.setFechaPedido(pedido.getFechaPedido());

        return response;
    }
}
