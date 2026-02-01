package com.mercadolocalia.services;

import java.util.List;
import com.mercadolocalia.dto.SubcategoriaRequest;
import com.mercadolocalia.dto.SubcategoriaResponse;

public interface SubcategoriaService {
    SubcategoriaResponse crearSubcategoria(SubcategoriaRequest request);
    SubcategoriaResponse actualizarSubcategoria(Integer id, SubcategoriaRequest request);
    void eliminarSubcategoria(Integer id);
    SubcategoriaResponse obtenerSubcategoriaPorId(Integer id);
    List<SubcategoriaResponse> listarSubcategorias();
    List<SubcategoriaResponse> listarPorCategoria(Integer idCategoria);
    boolean tieneProductosAsociados(Integer idSubcategoria); // Método nuevo
}