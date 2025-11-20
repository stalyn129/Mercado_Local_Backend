package com.mercadolocalia.services;

import java.util.List;

import com.mercadolocalia.entities.Favorito;

public interface FavoritoService {

    String agregarFavorito(Integer idConsumidor, Integer idProducto);

    List<Favorito> listarFavoritos(Integer idConsumidor);

    String eliminarFavorito(Integer idFavorito);

}
