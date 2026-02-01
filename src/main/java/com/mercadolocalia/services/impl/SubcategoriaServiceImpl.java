package com.mercadolocalia.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.SubcategoriaRequest;
import com.mercadolocalia.dto.SubcategoriaResponse;
import com.mercadolocalia.entities.Categoria;
import com.mercadolocalia.entities.Subcategoria;
import com.mercadolocalia.repositories.CategoriaRepository;
import com.mercadolocalia.repositories.SubcategoriaRepository;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.services.SubcategoriaService;

@Service
public class SubcategoriaServiceImpl implements SubcategoriaService {

    @Autowired
    private SubcategoriaRepository subcategoriaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public SubcategoriaResponse crearSubcategoria(SubcategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Subcategoria sub = new Subcategoria();
        sub.setNombreSubcategoria(request.getNombreSubcategoria());
        sub.setDescripcionSubcategoria(request.getDescripcionSubcategoria());
        sub.setCategoria(categoria);

        subcategoriaRepository.save(sub);

        return convertir(sub);
    }

    @Override
    public SubcategoriaResponse actualizarSubcategoria(Integer id, SubcategoriaRequest request) {
        Subcategoria sub = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategoría no encontrada"));

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        sub.setNombreSubcategoria(request.getNombreSubcategoria());
        sub.setDescripcionSubcategoria(request.getDescripcionSubcategoria());
        sub.setCategoria(categoria);

        subcategoriaRepository.save(sub);

        return convertir(sub);
    }

    @Override
    public void eliminarSubcategoria(Integer id) {
        subcategoriaRepository.deleteById(id);
    }

    @Override
    public SubcategoriaResponse obtenerSubcategoriaPorId(Integer id) {
        Subcategoria sub = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategoría no encontrada"));
        return convertir(sub);
    }

    @Override
    public List<SubcategoriaResponse> listarSubcategorias() {
        return subcategoriaRepository.findAll()
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    public List<SubcategoriaResponse> listarPorCategoria(Integer idCategoria) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        return subcategoriaRepository.findByCategoria(categoria)
                .stream()
                .map(this::convertir)
                .toList();
    }
    
    @Override
    public boolean tieneProductosAsociados(Integer idSubcategoria) {
        // Verificar si existe algún producto con esta subcategoría
        // Asumiendo que Producto tiene una relación directa con Subcategoria
        return productoRepository.existsBySubcategoriaIdSubcategoria(idSubcategoria);
    }

    private SubcategoriaResponse convertir(Subcategoria sub) {
        SubcategoriaResponse dto = new SubcategoriaResponse();

        dto.setIdSubcategoria(sub.getIdSubcategoria());
        dto.setNombreSubcategoria(sub.getNombreSubcategoria());
        dto.setDescripcionSubcategoria(sub.getDescripcionSubcategoria());

        dto.setIdCategoria(sub.getCategoria().getIdCategoria());
        dto.setNombreCategoria(sub.getCategoria().getNombreCategoria());

        return dto;
    }
}