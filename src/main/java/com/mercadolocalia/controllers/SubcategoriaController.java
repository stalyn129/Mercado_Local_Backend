package com.mercadolocalia.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.SubcategoriaRequest;
import com.mercadolocalia.dto.SubcategoriaResponse;
import com.mercadolocalia.services.SubcategoriaService;

@RestController
@RequestMapping("/subcategorias")
@CrossOrigin(origins = "*")
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
    public void eliminar(@PathVariable Integer id) {
        subcategoriaService.eliminarSubcategoria(id);
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
}
