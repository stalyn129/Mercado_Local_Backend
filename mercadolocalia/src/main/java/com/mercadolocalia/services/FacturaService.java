package com.mercadolocalia.services;

import com.mercadolocalia.dto.FacturaRequest;
import com.mercadolocalia.dto.FacturaEstadoRequest;
import com.mercadolocalia.entities.Factura;

public interface FacturaService {

    Factura crearFactura(FacturaRequest request);

    Factura obtenerPorId(Integer idFactura);

    Factura obtenerPorPedido(Integer idPedido);

    Factura actualizarEstado(Integer idFactura, FacturaEstadoRequest request);
}
