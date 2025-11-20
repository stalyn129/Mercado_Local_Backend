package com.mercadolocalia.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.entities.Carrito;
import com.mercadolocalia.entities.CarritoItem;
import com.mercadolocalia.repositories.CarritoItemRepository;
import com.mercadolocalia.repositories.CarritoRepository;
import com.mercadolocalia.repositories.ConsumidorRepository;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.services.CarritoService;

@Service
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private CarritoItemRepository carritoItemRepository;

    @Autowired
    private ConsumidorRepository consumidorRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public Carrito obtenerCarrito(Integer idConsumidor) {

        return carritoRepository.findByConsumidorIdConsumidor(idConsumidor)
                .orElseGet(() -> {
                    Carrito carrito = new Carrito();
                    carrito.setConsumidor(consumidorRepository.findById(idConsumidor).orElseThrow());
                    return carritoRepository.save(carrito);
                });
    }

    @Override
    public String agregarItem(Integer idConsumidor, Integer idProducto, Integer cantidad) {

        Carrito carrito = obtenerCarrito(idConsumidor);

        CarritoItem item = new CarritoItem();
        item.setCarrito(carrito);
        item.setProducto(productoRepository.findById(idProducto).orElseThrow());
        item.setCantidad(cantidad);

        carritoItemRepository.save(item);

        return "Producto agregado al carrito.";
    }

    @Override
    public String eliminarItem(Integer idItem) {

        carritoItemRepository.deleteById(idItem);
        return "Item eliminado del carrito.";
    }

    @Override
    public String vaciarCarrito(Integer idConsumidor) {

        Carrito carrito = obtenerCarrito(idConsumidor);
        carritoItemRepository.deleteAll(carrito.getItems());

        return "Carrito vaciado.";
    }
}
