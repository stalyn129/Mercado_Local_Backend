package com.mercadolocalia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.FacturaRequest;
import com.mercadolocalia.dto.FacturaEstadoRequest;
import com.mercadolocalia.dto.FacturaResponse;
import com.mercadolocalia.services.FacturaService;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@CrossOrigin(origins = "*")
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

    @PostMapping
    public ResponseEntity<?> crearFactura(@RequestBody FacturaRequest request) {
        try {
            FacturaResponse factura = facturaService.crearFactura(request);
            return ResponseEntity.ok(factura);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            FacturaResponse factura = facturaService.obtenerPorId(id);
            return ResponseEntity.ok(factura);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<?> obtenerPorPedido(@PathVariable Integer idPedido) {
        try {
            FacturaResponse factura = facturaService.obtenerPorPedido(idPedido);
            return ResponseEntity.ok(factura);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id,
                                           @RequestBody FacturaEstadoRequest request) {
        try {
            FacturaResponse factura = facturaService.actualizarEstado(id, request);
            return ResponseEntity.ok(factura);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    @GetMapping("/consumidor/{idConsumidor}")
    public ResponseEntity<?> obtenerPorConsumidor(@PathVariable Integer idConsumidor) {
        try {
            List<FacturaResponse> facturas = facturaService.obtenerPorConsumidor(idConsumidor);
            return ResponseEntity.ok(facturas);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> obtenerTodas() {
        try {
            List<FacturaResponse> facturas = facturaService.obtenerTodas();
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener facturas: " + e.getMessage());
        }
    }

    // ENDPOINT SIMPLE - solo devuelve los datos de la factura
    @GetMapping("/{id}/datos-pdf")
    public ResponseEntity<?> obtenerDatosParaPDF(@PathVariable Integer id) {
        try {
            FacturaResponse factura = facturaService.obtenerPorId(id);
            return ResponseEntity.ok(factura);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener datos para PDF: " + e.getMessage());
        }
    }
}