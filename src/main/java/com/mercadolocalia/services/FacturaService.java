package com.mercadolocalia.services;

import com.mercadolocalia.dto.FacturaRequest;
import com.mercadolocalia.dto.FacturaEstadoRequest;
import com.mercadolocalia.dto.FacturaResponse;
import java.util.List;

public interface FacturaService {

    FacturaResponse crearFactura(FacturaRequest request);

    FacturaResponse obtenerPorId(Integer idFactura);

    FacturaResponse obtenerPorPedido(Integer idPedido);

    FacturaResponse actualizarEstado(Integer idFactura, FacturaEstadoRequest request);
    
    // NUEVOS MÉTODOS
    List<FacturaResponse> obtenerPorConsumidor(Integer idConsumidor);
    
    List<FacturaResponse> obtenerTodas();
    
    // MÉTODO PARA GENERAR PDF SIMPLE
    String generarPDFSimple(Integer idFactura);
}