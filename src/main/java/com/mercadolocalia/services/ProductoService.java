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

    // ====================== BORRADO LÓGICO ======================
    ProductoResponse desactivarProducto(Integer id, String motivo);
    ProductoResponse reactivarProducto(Integer id);
    
    // ====================== LECTURA NORMAL ======================
    ProductoResponse obtenerPorId(Integer id);
    List<ProductoResponse> listarPorVendedor(Integer idVendedor);
    List<ProductoResponse> listarPorSubcategoria(Integer idSubcategoria);
    List<ProductoResponse> listarTodos();
    List<ProductoResponse> listarActivos(); // NUEVO: solo activos
    List<ProductoResponse> listarInactivos(); // NUEVO: solo inactivos
    ProductoResponse cambiarEstado(Integer id, String nuevoEstado);

    // ====================== DETALLE COMPLETO ======================
    ProductoDetalleResponse obtenerDetalleProducto(Integer idProducto);
    
    // ====================== TOP 20 PARA HOME ======================
    List<ProductoResponse> listarTop20Mejores();
    
}