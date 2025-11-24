package com.mercadolocalia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.FacturaRequest;
import com.mercadolocalia.dto.FacturaEstadoRequest;
import com.mercadolocalia.entities.Factura;
import com.mercadolocalia.services.FacturaService;

@RestController
@RequestMapping("/facturas")
@CrossOrigin(origins = "*")
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

    @PostMapping("/crear")
    public Factura crear(@RequestBody FacturaRequest request) {
        return facturaService.crearFactura(request);
    }

    @GetMapping("/{id}")
    public Factura obtenerPorId(@PathVariable Integer id) {
        return facturaService.obtenerPorId(id);
    }

    @GetMapping("/pedido/{idPedido}")
    public Factura obtenerPorPedido(@PathVariable Integer idPedido) {
        return facturaService.obtenerPorPedido(idPedido);
    }

    @PutMapping("/estado/{idFactura}")
    public Factura cambiarEstado(@PathVariable Integer idFactura,
                                 @RequestBody FacturaEstadoRequest request) {
        return facturaService.actualizarEstado(idFactura, request);
    }
}
