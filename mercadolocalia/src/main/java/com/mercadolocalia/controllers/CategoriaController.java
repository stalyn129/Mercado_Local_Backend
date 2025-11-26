package com.mercadolocalia.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.CategoriaRequest;
import com.mercadolocalia.dto.CategoriaResponse;
import com.mercadolocalia.services.CategoriaService;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @PostMapping("/crear")
    public CategoriaResponse crear(@RequestBody CategoriaRequest request) {
        return categoriaService.crearCategoria(request);
    }

    @PutMapping("/actualizar/{id}")
    public CategoriaResponse actualizar(@PathVariable Integer id,
                                        @RequestBody CategoriaRequest request) {
        return categoriaService.actualizarCategoria(id, request);
    }

    @DeleteMapping("/eliminar/{id}")
    public void eliminar(@PathVariable Integer id) {
        categoriaService.eliminarCategoria(id);
    }

    @GetMapping("/{id}")
    public CategoriaResponse obtenerPorId(@PathVariable Integer id) {
        return categoriaService.obtenerCategoriaPorId(id);
    }

    @GetMapping("/listar")
    public List<CategoriaResponse> listar() {
        return categoriaService.listarCategorias();
    }
}
