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
    origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173"
    },
    allowCredentials = "true"
)
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @PostMapping("/crear")
    public ProductoResponse crearProducto(@RequestBody ProductoRequest request) {
        return productoService.crearProducto(request);
    }

    @GetMapping("/{id}")
    public ProductoResponse obtenerProducto(@PathVariable Integer id) {
        return productoService.obtenerPorId(id);
    }

    @GetMapping("/vendedor/{idVendedor}")
    public List<ProductoResponse> listarPorVendedor(@PathVariable Integer idVendedor) {
        return productoService.listarPorVendedor(idVendedor);
    }

    @GetMapping("/subcategoria/{idSubcategoria}")
    public List<ProductoResponse> listarPorSubcategoria(@PathVariable Integer idSubcategoria) {
        return productoService.listarPorSubcategoria(idSubcategoria);
    }

    @GetMapping("/todos")
    public List<ProductoResponse> listarTodos() {
        return productoService.listarTodos();
    }

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
