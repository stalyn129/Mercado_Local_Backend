package com.mercadolocalia.services;

import com.mercadolocalia.entities.Carrito;

public interface CarritoService {

    Carrito obtenerCarrito(Integer idConsumidor);

    String agregarItem(Integer idConsumidor, Integer idProducto, Integer cantidad);

    String eliminarItem(Integer idItem);

    String vaciarCarrito(Integer idConsumidor);

}
