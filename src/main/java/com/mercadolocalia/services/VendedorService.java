package com.mercadolocalia.services;

import java.util.List;

import com.mercadolocalia.dto.EstadisticasDTO;
import com.mercadolocalia.dto.PedidoDTO;
import com.mercadolocalia.dto.VendedorRequest;
import com.mercadolocalia.entities.Vendedor;

public interface VendedorService {

    Vendedor registrarVendedor(VendedorRequest request);

    Vendedor obtenerVendedorPorUsuario(Integer idUsuario);

    Vendedor obtenerVendedorPorId(Integer idVendedor);

    EstadisticasDTO obtenerEstadisticas(Integer vendedorId);

    List<PedidoDTO> obtenerPedidosRecientes(Integer vendedorId);
}
