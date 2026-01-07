package com.mercadolocalia.services;

import com.mercadolocalia.dto.CarritoResponse;

public interface CarritoService {

    CarritoResponse obtenerCarritoResponse(Integer idConsumidor);

    String agregarItem(Integer idConsumidor, Integer idProducto, Integer cantidad);

    String eliminarItem(Integer idItem);

    String vaciarCarrito(Integer idConsumidor);
    
    void actualizarCantidad(Integer idItem, Integer cantidad);
}
