package com.mercadolocalia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.AgregarCarritoItemRequest;
import com.mercadolocalia.dto.CarritoResponse;
import com.mercadolocalia.services.CarritoService;

@RestController
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    // ==================================================
    // 🟢 OBTENER CARRITO (DTO SEGURO PARA FRONTEND)
    // ==================================================
    @GetMapping("/{idConsumidor}")
    public CarritoResponse obtenerCarrito(@PathVariable Integer idConsumidor) {
        return carritoService.obtenerCarritoResponse(idConsumidor);
    }

    // ==================================================
    // 🛒 AGREGAR ITEM AL CARRITO
    // ==================================================
    @PostMapping("/agregar")
    public CarritoResponse agregarItem(@RequestBody AgregarCarritoItemRequest request) {

        String mensaje = carritoService.agregarItem(
            request.getIdConsumidor(),
            request.getIdProducto(),
            request.getCantidad()
        );

        CarritoResponse response = new CarritoResponse();
        response.setMensaje(mensaje);

        return response;
    }

    // ==================================================
    // ❌ ELIMINAR ITEM DEL CARRITO
    // ==================================================
    @DeleteMapping("/item/{idItem}")
    public CarritoResponse eliminarItem(@PathVariable Integer idItem) {

        String mensaje = carritoService.eliminarItem(idItem);

        CarritoResponse response = new CarritoResponse();
        response.setMensaje(mensaje);

        return response;
    }

    // ==================================================
    // 🧹 VACIAR CARRITO
    // ==================================================
    @DeleteMapping("/vaciar/{idConsumidor}")
    public CarritoResponse vaciarCarrito(@PathVariable Integer idConsumidor) {

        String mensaje = carritoService.vaciarCarrito(idConsumidor);

        CarritoResponse response = new CarritoResponse();
        response.setMensaje(mensaje);

        return response;
    }
}
