package com.mercadolocalia.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mercadolocalia.dto.PedidoCarritoRequest;
import com.mercadolocalia.dto.PedidoRequest;
import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.DetallePedido;
import com.mercadolocalia.services.PedidoService;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // ============================================================
    // CREAR PEDIDO COMPLETO
    // ============================================================
    @PostMapping("/crear")
    public Pedido crearPedido(@RequestBody PedidoRequest request) {
        return pedidoService.crearPedido(request);
    }

    // ============================================================
    // OBTENER PEDIDO POR ID
    // ============================================================
    @GetMapping("/{id}")
    public Pedido obtenerPedido(@PathVariable Integer id) {
        return pedidoService.obtenerPedidoPorId(id);
    }

    // ============================================================
    // LISTAR PEDIDOS POR CONSUMIDOR
    // ============================================================
    @GetMapping("/consumidor/{idConsumidor}")
    public List<Pedido> listarPorConsumidor(@PathVariable Integer idConsumidor) {
        return pedidoService.listarPedidosPorConsumidor(idConsumidor);
    }

    // ============================================================
    // LISTAR PEDIDOS POR VENDEDOR
    // ============================================================
    @GetMapping("/vendedor/{idVendedor}")
    public List<Pedido> listarPorVendedor(@PathVariable Integer idVendedor) {
        return pedidoService.listarPedidosPorVendedor(idVendedor);
    }

    // ============================================================
    // LISTAR DETALLES DE UN PEDIDO
    // ============================================================
    @GetMapping("/{idPedido}/detalles")
    public List<DetallePedido> listarDetalles(@PathVariable Integer idPedido) {
        return pedidoService.listarDetalles(idPedido);
    }

    // ============================================================
    // CAMBIAR ESTADO DEL PEDIDO (VENDEDOR)
    // ============================================================
    @PutMapping("/estado/{idPedido}")
    public Pedido cambiarEstado(
            @PathVariable Integer idPedido,
            @RequestParam String estado) {
        return pedidoService.cambiarEstado(idPedido, estado);
    }

    // ============================================================
    // COMPRAR AHORA (1 SOLO PRODUCTO)
    // ============================================================
    @PostMapping("/comprar-ahora")
    public Pedido comprarAhora(@RequestBody PedidoRequest request) {
        return pedidoService.comprarAhora(request);
    }

    // ============================================================
    // FINALIZAR COMPRA — MODO SIMPLE (PUT)
    // ============================================================
    @PutMapping("/finalizar/{idPedido}")
    public Pedido finalizarPedidoSimple(
            @PathVariable Integer idPedido,
            @RequestParam String metodoPago) {

        return pedidoService.finalizarPedido(idPedido, metodoPago);
    }

    // ============================================================
    // FINALIZAR COMPRA — MODO COMPLETO (POST multipart/form-data)
    // ============================================================
    @PostMapping(value = "/finalizar/{idPedido}", consumes = "multipart/form-data")
    public Pedido finalizarPedidoCompleto(
            @PathVariable Integer idPedido,
            @RequestParam String metodoPago,
            @RequestParam(required = false) MultipartFile comprobante,
            @RequestParam(required = false) String numTarjeta,
            @RequestParam(required = false) String fechaTarjeta,
            @RequestParam(required = false) String cvv,
            @RequestParam(required = false) String titular) {

        return pedidoService.finalizarPedido(
                idPedido, metodoPago, comprobante,
                numTarjeta, fechaTarjeta, cvv, titular
        );
    }

    // ============================================================
    // CREAR PEDIDO DESDE CARRITO
    // ============================================================
    @PostMapping("/carrito")
    public Pedido crearDesdeCarrito(@RequestBody PedidoCarritoRequest request) {
        return pedidoService.crearPedidoDesdeCarrito(request);
    }
    
	// ============================================================
	// 📊 ESTADÍSTICAS DE VENTAS - VENDEDOR
	// ============================================================
	@GetMapping("/estadisticas/vendedor/{idVendedor}")
	public ResponseEntity<?> obtenerEstadisticasVendedor(@PathVariable Integer idVendedor) {

		return ResponseEntity.ok(pedidoService.obtenerEstadisticasVendedor(idVendedor));
 }


}
