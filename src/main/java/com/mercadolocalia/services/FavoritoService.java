package com.mercadolocalia.services;

import java.util.List;
import com.mercadolocalia.dto.FavoritoListDTO;

public interface FavoritoService {

    String agregarFavorito(Integer idConsumidor, Integer idProducto);

    List<FavoritoListDTO> listarFavoritosDTO(Integer idConsumidor);

    String eliminarFavorito(Integer idFavorito);
}
