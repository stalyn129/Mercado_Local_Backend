package com.mercadolocalia.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.entities.Favorito;
import com.mercadolocalia.repositories.ConsumidorRepository;
import com.mercadolocalia.repositories.FavoritoRepository;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.services.FavoritoService;

@Service
public class FavoritoServiceImpl implements FavoritoService {

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private ConsumidorRepository consumidorRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public String agregarFavorito(Integer idConsumidor, Integer idProducto) {

        if (favoritoRepository.existsByConsumidorIdConsumidorAndProductoIdProducto(idConsumidor, idProducto)) {
            return "El producto ya está en favoritos.";
        }

        Favorito favorito = new Favorito();
        favorito.setConsumidor(consumidorRepository.findById(idConsumidor).orElseThrow());
        favorito.setProducto(productoRepository.findById(idProducto).orElseThrow());

        favoritoRepository.save(favorito);

        return "Producto agregado a favoritos.";
    }

    @Override
    public List<Favorito> listarFavoritos(Integer idConsumidor) {
        return favoritoRepository.findByConsumidorIdConsumidor(idConsumidor);
    }

    @Override
    public String eliminarFavorito(Integer idFavorito) {
        favoritoRepository.deleteById(idFavorito);
        return "Favorito eliminado correctamente.";
    }
}
