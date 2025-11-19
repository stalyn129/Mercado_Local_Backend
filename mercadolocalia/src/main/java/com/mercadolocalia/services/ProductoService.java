package com.mercadolocalia.services;

import java.util.List;

import com.mercadolocalia.dto.ProductoRequest;
import com.mercadolocalia.dto.ProductoResponse;

public interface ProductoService {

    ProductoResponse crearProducto(ProductoRequest request);

    ProductoResponse actualizarProducto(Integer id, ProductoRequest request);

    void eliminarProducto(Integer id);

    ProductoResponse obtenerPorId(Integer id);

    List<ProductoResponse> listarPorVendedor(Integer idVendedor);

    List<ProductoResponse> listarPorSubcategoria(Integer idSubcategoria);

    List<ProductoResponse> listarTodos();
}
