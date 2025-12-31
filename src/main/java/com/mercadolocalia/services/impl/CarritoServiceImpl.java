package com.mercadolocalia.services.impl;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.CarritoItemResponse;
import com.mercadolocalia.dto.CarritoResponse;
import com.mercadolocalia.dto.ProductoSimpleResponse;
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

    // ==================================================
    // 🔒 MÉTODO INTERNO (NO SE EXPONE AL CONTROLLER)
    // ==================================================
    private Carrito obtenerCarrito(Integer idConsumidor) {

        return carritoRepository
            .findByConsumidorIdConsumidor(idConsumidor)
            .orElseGet(() -> {
                Carrito carrito = new Carrito();
                carrito.setConsumidor(
                    consumidorRepository.findById(idConsumidor).orElseThrow()
                );
                return carritoRepository.save(carrito);
            });
    }

    // ==================================================
    // 🟢 CARRITO EN FORMATO SEGURO PARA FRONTEND
    // ==================================================
    @Override
    public CarritoResponse obtenerCarritoResponse(Integer idConsumidor) {

        Carrito carrito = obtenerCarrito(idConsumidor);

        CarritoResponse response = new CarritoResponse();
        response.setIdCarrito(carrito.getIdCarrito());

        response.setItems(
            carrito.getItems()
                .stream()
                .map(item -> {

                    var p = item.getProducto();

                    ProductoSimpleResponse producto =
                        new ProductoSimpleResponse(
                            p.getIdProducto(),
                            p.getNombreProducto(),
                            p.getPrecioProducto(), // 👈 este es tu campo REAL
                            p.getImagenProducto()
                        );

                    CarritoItemResponse dto = new CarritoItemResponse();
                    dto.setIdItem(item.getIdItem());
                    dto.setCantidad(item.getCantidad());
                    dto.setProducto(producto);

                    return dto;
                })
                .collect(Collectors.toList())
        );

        return response;
    }

    // ==================================================
    // 🛒 AGREGAR ITEM
    // ==================================================
    @Override
    public String agregarItem(Integer idConsumidor, Integer idProducto, Integer cantidad) {

        Carrito carrito = obtenerCarrito(idConsumidor);

        CarritoItem item = new CarritoItem();
        item.setCarrito(carrito);
        item.setProducto(
            productoRepository.findById(idProducto).orElseThrow()
        );
        item.setCantidad(cantidad);

        carritoItemRepository.save(item);

        return "Producto agregado al carrito.";
    }

    // ==================================================
    // ❌ ELIMINAR ITEM
    // ==================================================
    @Override
    public String eliminarItem(Integer idItem) {

        if (!carritoItemRepository.existsById(idItem)) {
            throw new RuntimeException("Item no encontrado");
        }

        carritoItemRepository.deleteById(idItem);
        return "Item eliminado del carrito.";
    }


    // ==================================================
    // 🧹 VACIAR CARRITO
    // ==================================================
    @Override
    public String vaciarCarrito(Integer idConsumidor) {

        Carrito carrito = obtenerCarrito(idConsumidor);

        carritoItemRepository.deleteAllByCarrito_IdCarrito(
            carrito.getIdCarrito()
        );

        return "Carrito vaciado.";
    }


}
