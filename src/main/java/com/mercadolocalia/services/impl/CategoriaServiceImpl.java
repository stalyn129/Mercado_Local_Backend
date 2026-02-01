package com.mercadolocalia.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.CategoriaRequest;
import com.mercadolocalia.dto.CategoriaResponse;
import com.mercadolocalia.entities.Categoria;
import com.mercadolocalia.repositories.CategoriaRepository;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.services.CategoriaService;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public CategoriaResponse crearCategoria(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNombreCategoria(request.getNombreCategoria());
        categoria.setDescripcionCategoria(request.getDescripcionCategoria());
        
        Categoria saved = categoriaRepository.save(categoria);
        return mapToResponse(saved);
    }

    @Override
    public CategoriaResponse actualizarCategoria(Integer id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        
        categoria.setNombreCategoria(request.getNombreCategoria());
        categoria.setDescripcionCategoria(request.getDescripcionCategoria());
        
        Categoria updated = categoriaRepository.save(categoria);
        return mapToResponse(updated);
    }

    @Override
    public void eliminarCategoria(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        categoriaRepository.delete(categoria);
    }

    @Override
    public CategoriaResponse obtenerCategoriaPorId(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        return mapToResponse(categoria);
    }

    @Override
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean tieneProductosAsociados(Integer idCategoria) {
        // Verificar si existe algún producto con esta categoría
        // Asumiendo que Producto tiene una relación directa con Categoria
        return productoRepository.existsByCategoriaIdCategoria(idCategoria);
    }

    private CategoriaResponse mapToResponse(Categoria categoria) {
        CategoriaResponse response = new CategoriaResponse();
        response.setIdCategoria(categoria.getIdCategoria());
        response.setNombreCategoria(categoria.getNombreCategoria());
        response.setDescripcionCategoria(categoria.getDescripcionCategoria());
        return response;
    }
}