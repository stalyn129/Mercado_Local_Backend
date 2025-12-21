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
    

    // ============================================================
    // 🔵 REGISTRAR VENDEDOR
    // ============================================================
    @PostMapping("/registrar")
    public Vendedor registrar(@RequestBody VendedorRequest request) {
        return vendedorService.registrarVendedor(request);
    }

    // ============================================================
    // 🟣 OBTENER VENDEDOR POR ID_USUARIO
    // ============================================================
    @GetMapping("/usuario/{idUsuario}")
    public Vendedor obtenerPorUsuario(@PathVariable Integer idUsuario) {
        return vendedorService.obtenerVendedorPorUsuario(idUsuario);
    }

    // ============================================================
    // 🟤 OBTENER VENDEDOR POR ID
    // ============================================================
    @GetMapping("/{id}")
    public Vendedor obtenerPorId(@PathVariable Integer id) {
        return vendedorService.obtenerVendedorPorId(id);
    }

    // ============================================================
    // 🟠 ESTADÍSTICAS DEL VENDEDOR
    // ============================================================
    @GetMapping("/{vendedorId}/estadisticas")
    public EstadisticasDTO obtenerEstadisticas(@PathVariable Integer vendedorId) {
        return vendedorService.obtenerEstadisticas(vendedorId);
    }

    // ============================================================
    // 🟡 PEDIDOS RECIENTES DEL VENDEDOR
    // ============================================================
    @GetMapping("/{vendedorId}/pedidos/recientes")
    public List<PedidoDTO> obtenerPedidosRecientes(@PathVariable Integer vendedorId) {
        return vendedorService.obtenerPedidosRecientes(vendedorId);
    }

    // ============================================================
    // 🟢 LISTAR TODOS LOS VENDEDORES (solo existía en tu local)
    // ============================================================
    @GetMapping("/listar")
    public List<Vendedor> listarTodos() {
        return vendedorService.listarTodos();
    }

}
