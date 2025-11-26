package com.mercadolocalia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.DetallePedidoAddRequest;
import com.mercadolocalia.dto.DetallePedidoUpdateRequest;
import com.mercadolocalia.entities.DetallePedido;
import com.mercadolocalia.services.DetallePedidoService;

@RestController
@RequestMapping("/detalles")
@CrossOrigin(origins = "*")
public class DetallePedidoController {

    @Autowired
    private DetallePedidoService detalleService;

    // AGREGAR
    @PostMapping("/agregar")
    public DetallePedido agregar(@RequestBody DetallePedidoAddRequest request) {
        return detalleService.agregarDetalle(request);
    }

    // EDITAR CANTIDAD
    @PutMapping("/editar/{idDetalle}")
    public DetallePedido editar(@PathVariable Integer idDetalle,
                                @RequestBody DetallePedidoUpdateRequest request) {
        return detalleService.editarDetalle(idDetalle, request);
    }

    // ELIMINAR
    @DeleteMapping("/eliminar/{idDetalle}")
    public void eliminar(@PathVariable Integer idDetalle) {
        detalleService.eliminarDetalle(idDetalle);
    }
}
