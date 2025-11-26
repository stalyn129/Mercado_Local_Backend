package com.mercadolocalia.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.CategoriaRequest;
import com.mercadolocalia.dto.CategoriaResponse;
import com.mercadolocalia.entities.Categoria;
import com.mercadolocalia.repositories.CategoriaRepository;
import com.mercadolocalia.services.CategoriaService;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public CategoriaResponse crearCategoria(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNombreCategoria(request.getNombreCategoria());
        categoria.setDescripcionCategoria(request.getDescripcionCategoria());
        categoriaRepository.save(categoria);

        return convertir(categoria);
    }

    @Override
    public CategoriaResponse actualizarCategoria(Integer id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        categoria.setNombreCategoria(request.getNombreCategoria());
        categoria.setDescripcionCategoria(request.getDescripcionCategoria());

        categoriaRepository.save(categoria);

        return convertir(categoria);
    }

    @Override
    public void eliminarCategoria(Integer id) {
        categoriaRepository.deleteById(id);
    }

    @Override
    public CategoriaResponse obtenerCategoriaPorId(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        return convertir(categoria);
    }

    @Override
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::convertir)
                .toList();
    }

    private CategoriaResponse convertir(Categoria categoria) {
        CategoriaResponse dto = new CategoriaResponse();
        dto.setIdCategoria(categoria.getIdCategoria());
        dto.setNombreCategoria(categoria.getNombreCategoria());
        dto.setDescripcionCategoria(categoria.getDescripcionCategoria());
        return dto;
    }
}
