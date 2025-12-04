package com.mercadolocalia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.AgregarFavoritoRequest;
import com.mercadolocalia.dto.FavoritoListDTO;
import com.mercadolocalia.dto.FavoritoResponse;
import com.mercadolocalia.services.FavoritoService;

import java.util.List;

@RestController
@RequestMapping("/favoritos")
public class FavoritoController {

    @Autowired
    private FavoritoService favoritoService;

    @PostMapping("/agregar")
    public FavoritoResponse agregar(@RequestBody AgregarFavoritoRequest request) {

        String mensaje = favoritoService.agregarFavorito(
                request.getIdConsumidor(),
                request.getIdProducto()
        );

        FavoritoResponse response = new FavoritoResponse();
        response.setMensaje(mensaje);
        response.setIdProducto(request.getIdProducto());

        return response;
    }

    @GetMapping("/listar/{idConsumidor}")
    public List<FavoritoListDTO> listar(@PathVariable Integer idConsumidor) {
        return favoritoService.listarFavoritosDTO(idConsumidor);
    }

    @DeleteMapping("/eliminar/{idFavorito}")
    public FavoritoResponse eliminar(@PathVariable Integer idFavorito) {

        FavoritoResponse response = new FavoritoResponse();
        response.setMensaje(favoritoService.eliminarFavorito(idFavorito));
        response.setIdFavorito(idFavorito);

        return response;
    }
}
