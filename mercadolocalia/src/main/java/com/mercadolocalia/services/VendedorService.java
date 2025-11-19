package com.mercadolocalia.services;

import com.mercadolocalia.dto.VendedorRequest;
import com.mercadolocalia.entities.Vendedor;

public interface VendedorService {

    Vendedor registrarVendedor(VendedorRequest request);

    Vendedor obtenerVendedorPorUsuario(Integer idUsuario);

    Vendedor obtenerVendedorPorId(Integer idVendedor);
}
