package com.mercadolocalia.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.ProductoRequest;
import com.mercadolocalia.dto.ProductoResponse;
import com.mercadolocalia.services.ProductoService;

@RestController
@RequestMapping("/productos")
@CrossOrigin(
        origins = {"http://localhost:5175","http://localhost:5173"},
        allowCredentials = "true"
)
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // CREAR PRODUCTO (YA ESTABA)
    @PostMapping("/crear")
    public ProductoResponse crearProducto(@ModelAttribute ProductoRequest request) {
        return productoService.crearProducto(request);
    }

    // OBTENER PRODUCTO POR ID
    @GetMapping("/{id}")
    public ProductoResponse obtenerProducto(@PathVariable Integer id) {
        return productoService.obtenerPorId(id);
    }

    // LISTAR POR VENDEDOR
    @GetMapping("/vendedor/{idVendedor}")
    public List<ProductoResponse> listarPorVendedor(@PathVariable Integer idVendedor) {
        return productoService.listarPorVendedor(idVendedor);
    }

    // 🔥 NUEVO → EDITAR PRODUCTO CON IMAGEN OPCIONAL
    @PutMapping("/editar/{id}")
    public ProductoResponse actualizarProducto(
            @PathVariable Integer id,
            @ModelAttribute ProductoRequest request) {
        return productoService.actualizarProducto(id, request);
    }

    // CAMBIAR ESTADO
    @PutMapping("/{id}/estado")
    public ProductoResponse cambiarEstado(
            @PathVariable Integer id,
            @RequestParam String estado) {
        return productoService.cambiarEstado(id, estado);
    }

    @DeleteMapping("/{id}")
    public void eliminarProducto(@PathVariable Integer id) {
        productoService.eliminarProducto(id);
    }
}

