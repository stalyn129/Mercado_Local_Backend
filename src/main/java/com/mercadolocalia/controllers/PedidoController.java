package com.mercadolocalia.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    // CAMBIAR ESTADO DEL PEDIDO
    // ============================================================
    @PutMapping("/estado/{idPedido}")
    public Pedido cambiarEstado(@PathVariable Integer idPedido,
                                @RequestParam String estado) {
        return pedidoService.cambiarEstado(idPedido, estado);
    }
}
