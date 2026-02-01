package com.mercadolocalia.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.CategoriaRequest;
import com.mercadolocalia.dto.CategoriaResponse;
import com.mercadolocalia.services.CategoriaService;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    // ============================================================
    // 🟢 CREAR
    // ============================================================
    @PostMapping("/crear")
    public CategoriaResponse crear(@RequestBody CategoriaRequest request) {
        return categoriaService.crearCategoria(request);
    }

    // ============================================================
    // 🟡 ACTUALIZAR
    // ============================================================
    @PutMapping("/actualizar/{id}")
    public CategoriaResponse actualizar(
            @PathVariable Integer id,
            @RequestBody CategoriaRequest request) {

        return categoriaService.actualizarCategoria(id, request);
    }

    // ============================================================
    // 🔴 ELIMINAR
    // ============================================================
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            // Primero verificar si tiene productos asociados
            if (categoriaService.tieneProductosAsociados(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar la categoría porque tiene productos asociados");
            }
            categoriaService.eliminarCategoria(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al eliminar la categoría: " + e.getMessage());
        }
    }

    // ============================================================
    // 🔵 OBTENER POR ID
    // ============================================================
    @GetMapping("/{id}")
    public CategoriaResponse obtenerPorId(@PathVariable Integer id) {
        return categoriaService.obtenerCategoriaPorId(id);
    }

    // ============================================================
    // 🟣 LISTAR (ruta explícita)
    // ============================================================
    @GetMapping("/listar")
    public List<CategoriaResponse> listar() {
        return categoriaService.listarCategorias();
    }

    // ============================================================
    // 🟤 LISTAR ROOT (/categorias)
    // ============================================================
    @GetMapping("")
    public List<CategoriaResponse> listarRoot() {
        return categoriaService.listarCategorias();
    }
    
    // ============================================================
    // 🔍 VERIFICAR SI TIENE PRODUCTOS ASOCIADOS
    // ============================================================
    @GetMapping("/tiene-productos/{id}")
    public ResponseEntity<Boolean> tieneProductos(@PathVariable Integer id) {
        try {
            boolean tieneProductos = categoriaService.tieneProductosAsociados(id);
            return ResponseEntity.ok(tieneProductos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(true);
        }
    }
}