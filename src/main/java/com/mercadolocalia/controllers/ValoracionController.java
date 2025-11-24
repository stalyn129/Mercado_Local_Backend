package com.mercadolocalia.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.ValoracionRequest;
import com.mercadolocalia.dto.ValoracionResponse;
import com.mercadolocalia.services.ValoracionService;

@RestController
@RequestMapping("/valoraciones")
@CrossOrigin(origins = "*")
public class ValoracionController {

    @Autowired
    private ValoracionService valoracionService;

    @PostMapping("/crear")
    public ValoracionResponse crear(@RequestBody ValoracionRequest request) {
        return valoracionService.crearValoracion(request);
    }

    @GetMapping("/producto/{idProducto}")
    public List<ValoracionResponse> listarPorProducto(@PathVariable Integer idProducto) {
        return valoracionService.listarPorProducto(idProducto);
    }

    @GetMapping("/consumidor/{idConsumidor}")
    public List<ValoracionResponse> listarPorConsumidor(@PathVariable Integer idConsumidor) {
        return valoracionService.listarPorConsumidor(idConsumidor);
    }
}
