package com.mercadolocalia.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.EstadisticasDTO;
import com.mercadolocalia.dto.PedidoDTO;
import com.mercadolocalia.dto.VendedorRequest;
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.services.VendedorService;

@RestController
@RequestMapping("/vendedor")
public class VendedorController {

    @Autowired
    private VendedorService vendedorService;

    @PostMapping("/registrar")
    public Vendedor registrar(@RequestBody VendedorRequest request) {
        return vendedorService.registrarVendedor(request);
    }

    @GetMapping("/usuario/{idUsuario}")
    public Vendedor obtenerPorUsuario(@PathVariable Integer idUsuario) {
        return vendedorService.obtenerVendedorPorUsuario(idUsuario);
    }

    @GetMapping("/{id}")
    public Vendedor obtenerPorId(@PathVariable Integer id) {
        return vendedorService.obtenerVendedorPorId(id);
    }

    // ✔ NUEVO — ESTADÍSTICAS
    @GetMapping("/{vendedorId}/estadisticas")
    public EstadisticasDTO obtenerEstadisticas(@PathVariable Integer vendedorId) {
        return vendedorService.obtenerEstadisticas(vendedorId);
    }

    // ✔ NUEVO — PEDIDOS RECIENTES
    @GetMapping("/{vendedorId}/pedidos/recientes")
    public List<PedidoDTO> obtenerPedidosRecientes(@PathVariable Integer vendedorId) {
        return vendedorService.obtenerPedidosRecientes(vendedorId);
    }
}
