package com.mercadolocalia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mercadolocalia.dto.PagoRequest;
import com.mercadolocalia.dto.PagoTarjetaRequest;
import com.mercadolocalia.entities.Pago;
import com.mercadolocalia.services.PagoService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/pagos")
@CrossOrigin(origins = "*")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @PostMapping("/procesar")
    public ResponseEntity<Pago> procesar(@RequestBody PagoRequest request) {
        return ResponseEntity.ok(pagoService.procesarPago(request));
    }
    
    @PostMapping("/tarjeta")
    public ResponseEntity<?> pagarConTarjeta(@RequestBody PagoTarjetaRequest req) {
        try {
            Pago pago = pagoService.procesarPagoTarjetaSimulado(req);
            return ResponseEntity.ok(pago);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
