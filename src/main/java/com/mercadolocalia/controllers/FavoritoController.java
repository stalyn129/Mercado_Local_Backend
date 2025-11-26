package com.mercadolocalia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.AgregarFavoritoRequest;
import com.mercadolocalia.dto.FavoritoResponse;
import com.mercadolocalia.entities.Favorito;
import com.mercadolocalia.services.FavoritoService;

import java.util.List;

@RestController
@RequestMapping("/favoritos")
public class FavoritoController {

    @Autowired
    private FavoritoService favoritoService;

    // ============================
    // AGREGAR FAVORITO (POST)
    // ============================
    @PostMapping("/agregar")
    public FavoritoResponse agregar(@RequestBody AgregarFavoritoRequest request) {

        FavoritoResponse response = new FavoritoResponse();

        // Servicio retorna solo mensaje
        String mensaje = favoritoService.agregarFavorito(
                request.getIdConsumidor(),
                request.getIdProducto()
        );

        // Buscar el favorito recién agregado
        List<Favorito> lista = favoritoService.listarFavoritos(request.getIdConsumidor());

        Favorito ultimo = lista.stream()
                .filter(f -> f.getProducto().getIdProducto().equals(request.getIdProducto()))
                .findFirst()
                .orElse(null);

        response.setMensaje(mensaje);
        response.setIdFavorito(ultimo != null ? ultimo.getIdFavorito() : null);
        response.setIdProducto(request.getIdProducto());

        return response;
    }

    // ============================
    // LISTAR FAVORITOS
    // ============================
    @GetMapping("/listar/{idConsumidor}")
    public List<Favorito> listar(@PathVariable Integer idConsumidor) {
        return favoritoService.listarFavoritos(idConsumidor);
    }

    // ============================
    // ELIMINAR FAVORITO
    // ============================
    @DeleteMapping("/eliminar/{idFavorito}")
    public FavoritoResponse eliminar(@PathVariable Integer idFavorito) {

        FavoritoResponse response = new FavoritoResponse();
        response.setMensaje(favoritoService.eliminarFavorito(idFavorito));
        response.setIdFavorito(idFavorito);

        return response;
    }
}
