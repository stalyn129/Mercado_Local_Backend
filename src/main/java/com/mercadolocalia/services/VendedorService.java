package com.mercadolocalia.services;

import java.util.List;

import com.mercadolocalia.dto.EstadisticasDTO;
import com.mercadolocalia.dto.PedidoDTO;
import com.mercadolocalia.dto.VendedorRequest;
import com.mercadolocalia.entities.PedidoVendedor;
import com.mercadolocalia.entities.Vendedor;

public interface VendedorService {

    // ============================================================
    // CRUD Y REGISTRO
    // ============================================================
    Vendedor registrarVendedor(VendedorRequest request);

    Vendedor obtenerVendedorPorUsuario(Integer idUsuario);

    Vendedor obtenerVendedorPorId(Integer idVendedor);

    // ============================================================
    // DASHBOARD DEL VENDEDOR
    // ============================================================
    EstadisticasDTO obtenerEstadisticas(Integer vendedorId);

    List<PedidoDTO> obtenerPedidosRecientes(Integer vendedorId);

    // ============================================================
    // ADMIN / CONSULTAS GENERALES
    // ============================================================
    List<Vendedor> listarTodos();
    
    // En VendedorService.java
    List<PedidoVendedor> listarMisPedidos(Integer idVendedor);
}
