package com.mercadolocalia.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.ProductoDetalleResponse;
import com.mercadolocalia.dto.ProductoRequest;
import com.mercadolocalia.dto.ProductoResponse;
import com.mercadolocalia.services.ProductoService;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // ==================== CREAR PRODUCTO ====================
    @PostMapping("/crear")
    public ProductoResponse crearProducto(@RequestBody ProductoRequest request) {  
        return productoService.crearProducto(request);
    }


    // ==================== EDITAR PRODUCTO ====================
    @PutMapping("/editar/{id}")
    public ProductoResponse editarProducto(@PathVariable Integer id,
                                           ProductoRequest request) {
        return productoService.actualizarProducto(id, request);
    }

    // ==================== ELIMINAR ====================
    @DeleteMapping("/eliminar/{id}")
    public void eliminarProducto(@PathVariable Integer id) {
        productoService.eliminarProducto(id);
    }

    // ==================== OBTENER POR ID ====================
    @GetMapping("/{id}")
    public ProductoResponse obtenerProducto(@PathVariable Integer id) {
        return productoService.obtenerPorId(id);
    }

    // ==================== LISTAR TODOS ====================
    @GetMapping("/listar")
    public List<ProductoResponse> listarProductos() {
        return productoService.listarTodos();
    }

    // ==================== LISTAR POR VENDEDOR ====================
    @GetMapping("/vendedor/{idVendedor}")
    public List<ProductoResponse> listarPorVendedor(@PathVariable Integer idVendedor) {
        return productoService.listarPorVendedor(idVendedor);
    }

    // ==================== LISTAR POR SUBCATEGORÍA ====================
    @GetMapping("/subcategoria/{idSubcategoria}")
    public List<ProductoResponse> listarPorSubcategoria(@PathVariable Integer idSubcategoria) {
        return productoService.listarPorSubcategoria(idSubcategoria);
    }

    // ==================== CAMBIAR ESTADO ====================
    @PutMapping("/estado/{id}")
    public ProductoResponse cambiarEstado(@PathVariable Integer id,
                                          @RequestParam String estado) {
        return productoService.cambiarEstado(id, estado);
    }

    // ==================== DETALLE COMPLETO (CON VALORACIONES) ====================
    @GetMapping("/detalle/{id}")
    public ProductoDetalleResponse obtenerDetalle(@PathVariable Integer id) {
        return productoService.obtenerDetalleProducto(id);
    }
}
