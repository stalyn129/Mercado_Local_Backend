package com.mercadolocalia.services;

import java.util.List;

import com.mercadolocalia.dto.ProductoDetalleResponse;
import com.mercadolocalia.dto.ProductoRequest;
import com.mercadolocalia.dto.ProductoResponse;

public interface ProductoService {

    // ====================== CRUD ======================
    ProductoResponse crearProducto(ProductoRequest request);
    ProductoResponse actualizarProducto(Integer id, ProductoRequest request);
    void eliminarProducto(Integer id);

    // ====================== LECTURA NORMAL ======================
    ProductoResponse obtenerPorId(Integer id);  // LO QUE USA EXPLORAR Y ESTÁ FUNCIONANDO
    List<ProductoResponse> listarPorVendedor(Integer idVendedor);
    List<ProductoResponse> listarPorSubcategoria(Integer idSubcategoria);
    List<ProductoResponse> listarTodos();
    ProductoResponse cambiarEstado(Integer id, String nuevoEstado);

    // ====================== DETALLE COMPLETO ======================
    // 🔥 CON VALORACIONES Y PROMEDIO
    ProductoDetalleResponse obtenerDetalleProducto(Integer idProducto);
}
