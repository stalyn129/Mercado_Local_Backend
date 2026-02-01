package com.mercadolocalia.services;

import java.util.List;
import com.mercadolocalia.dto.CategoriaRequest;
import com.mercadolocalia.dto.CategoriaResponse;

public interface CategoriaService {
    CategoriaResponse crearCategoria(CategoriaRequest request);
    CategoriaResponse actualizarCategoria(Integer id, CategoriaRequest request);
    void eliminarCategoria(Integer id);
    CategoriaResponse obtenerCategoriaPorId(Integer id);
    List<CategoriaResponse> listarCategorias();
    boolean tieneProductosAsociados(Integer idCategoria); // Método nuevo
}