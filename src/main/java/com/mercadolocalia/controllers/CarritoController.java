package com.mercadolocalia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.AgregarCarritoItemRequest;
import com.mercadolocalia.dto.CarritoResponse;
import com.mercadolocalia.entities.Carrito;
import com.mercadolocalia.services.CarritoService;

@RestController
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    // ============================
    // OBTENER CARRITO
    // ============================
    @GetMapping("/{idConsumidor}")
    public Carrito obtener(@PathVariable Integer idConsumidor) {
        return carritoService.obtenerCarrito(idConsumidor);
    }

    // ============================
    // AGREGAR ITEM AL CARRITO
    // ============================
    @PostMapping("/agregar")
    public CarritoResponse agregarItem(@RequestBody AgregarCarritoItemRequest request) {

        String mensaje = carritoService.agregarItem(
                request.getIdConsumidor(),
                request.getIdProducto(),
                request.getCantidad()
        );

        Carrito carrito = carritoService.obtenerCarrito(request.getIdConsumidor());

        CarritoResponse response = new CarritoResponse();
        response.setMensaje(mensaje);
        response.setIdCarrito(carrito.getIdCarrito());

        return response;
    }

    // ============================
    // ELIMINAR ITEM DEL CARRITO
    // ============================
    @DeleteMapping("/item/{idItem}")
    public CarritoResponse eliminarItem(@PathVariable Integer idItem) {

        CarritoResponse response = new CarritoResponse();
        response.setMensaje(carritoService.eliminarItem(idItem));

        return response;
    }

    // ============================
    // VACIAR CARRITO
    // ============================
    @DeleteMapping("/vaciar/{idConsumidor}")
    public CarritoResponse vaciar(@PathVariable Integer idConsumidor) {

        String mensaje = carritoService.vaciarCarrito(idConsumidor);
        Carrito carrito = carritoService.obtenerCarrito(idConsumidor);

        CarritoResponse response = new CarritoResponse();
        response.setMensaje(mensaje);
        response.setIdCarrito(carrito.getIdCarrito());

        return response;
    }
}
