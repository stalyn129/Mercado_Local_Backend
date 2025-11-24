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
import com.mercadolocalia.services.SubcategoriaService;

@Service
public class SubcategoriaServiceImpl implements SubcategoriaService {

    @Autowired
    private SubcategoriaRepository subcategoriaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public SubcategoriaResponse crearSubcategoria(SubcategoriaRequest request) {

        if (subcategoriaRepository.existsByNombreSubcategoria(request.getNombreSubcategoria())) {
            throw new RuntimeException("La subcategoría ya existe");
        }

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Subcategoria sub = new Subcategoria();
        sub.setCategoria(categoria);
        sub.setNombreSubcategoria(request.getNombreSubcategoria());
        sub.setDescripcionSubcategoria(request.getDescripcionSubcategoria());

        subcategoriaRepository.save(sub);

        return convertir(sub);
    }

    @Override
    public SubcategoriaResponse actualizarSubcategoria(Integer id, SubcategoriaRequest request) {

        Subcategoria sub = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategoría no encontrada"));

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        sub.setCategoria(categoria);
        sub.setNombreSubcategoria(request.getNombreSubcategoria());
        sub.setDescripcionSubcategoria(request.getDescripcionSubcategoria());

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
                .collect(Collectors.toList());
    }

    @Override
    public List<SubcategoriaResponse> listarPorCategoria(Integer idCategoria) {

        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        return subcategoriaRepository.findByCategoria(categoria)
                .stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    private SubcategoriaResponse convertir(Subcategoria sub) {
        SubcategoriaResponse res = new SubcategoriaResponse();

        res.setIdSubcategoria(sub.getIdSubcategoria());
        res.setNombreSubcategoria(sub.getNombreSubcategoria());
        res.setDescripcionSubcategoria(sub.getDescripcionSubcategoria());
        res.setIdCategoria(sub.getCategoria().getIdCategoria());
        res.setNombreCategoria(sub.getCategoria().getNombreCategoria());

        return res;
    }
}
