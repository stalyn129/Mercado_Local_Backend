package com.mercadolocalia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.ConsumidorRequest;
import com.mercadolocalia.entities.Consumidor;
import com.mercadolocalia.services.ConsumidorService;

@RestController
@RequestMapping("/consumidor")
@CrossOrigin(origins = "*")
public class ConsumidorController {

    @Autowired
    private ConsumidorService consumidorService;

    @PostMapping("/registrar")
    public Consumidor registrar(@RequestBody ConsumidorRequest request) {
        return consumidorService.registrarConsumidor(request);
    }

    @GetMapping("/usuario/{idUsuario}")
    public Consumidor obtenerPorUsuario(@PathVariable Integer idUsuario) {
        return consumidorService.obtenerConsumidorPorUsuario(idUsuario);
    }

    @GetMapping("/{id}")
    public Consumidor obtenerPorId(@PathVariable Integer id) {
        return consumidorService.obtenerConsumidorPorId(id);
    }
}
