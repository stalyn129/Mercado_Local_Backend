package com.mercadolocalia.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.SubcategoriaRequest;
import com.mercadolocalia.dto.SubcategoriaResponse;
import com.mercadolocalia.services.SubcategoriaService;

@RestController
@RequestMapping("/subcategorias")
public class SubcategoriaController {

    @Autowired
    private SubcategoriaService subcategoriaService;

    @PostMapping("/crear")
    public SubcategoriaResponse crear(@RequestBody SubcategoriaRequest request) {
        return subcategoriaService.crearSubcategoria(request);
    }

    @PutMapping("/actualizar/{id}")
    public SubcategoriaResponse actualizar(@PathVariable Integer id, 
                                           @RequestBody SubcategoriaRequest request) {
        return subcategoriaService.actualizarSubcategoria(id, request);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            // Primero verificar si tiene productos asociados
            if (subcategoriaService.tieneProductosAsociados(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar la subcategoría porque tiene productos asociados");
            }
            subcategoriaService.eliminarSubcategoria(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al eliminar la subcategoría: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public SubcategoriaResponse obtenerPorId(@PathVariable Integer id) {
        return subcategoriaService.obtenerSubcategoriaPorId(id);
    }

    @GetMapping("/listar")
    public List<SubcategoriaResponse> listar() {
        return subcategoriaService.listarSubcategorias();
    }

    @GetMapping("/categoria/{idCategoria}")
    public List<SubcategoriaResponse> listarPorCategoria(@PathVariable Integer idCategoria) {
        return subcategoriaService.listarPorCategoria(idCategoria);
    }
    
    // ============================================================
    // 🔍 VERIFICAR SI TIENE PRODUCTOS ASOCIADOS
    // ============================================================
    @GetMapping("/tiene-productos/{id}")
    public ResponseEntity<Boolean> tieneProductos(@PathVariable Integer id) {
        try {
            boolean tieneProductos = subcategoriaService.tieneProductosAsociados(id);
            return ResponseEntity.ok(tieneProductos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(true);
        }
    }
}