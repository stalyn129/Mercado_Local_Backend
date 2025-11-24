package com.mercadolocalia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.PrecioSugeridoRequest;
import com.mercadolocalia.dto.PrecioSugeridoResponse;
import com.mercadolocalia.dto.DemandaRequest;
import com.mercadolocalia.dto.DemandaResponse;
import com.mercadolocalia.services.IaService;

@RestController
@RequestMapping("/ia")
@CrossOrigin(origins = "*")
public class IaController {

    @Autowired
    private IaService iaService;

    // =========================================
    // 1) Precio sugerido para un producto
    // =========================================
    @PostMapping("/precio-sugerido")
    public PrecioSugeridoResponse obtenerPrecioSugerido(@RequestBody PrecioSugeridoRequest request) {
        return iaService.obtenerPrecioSugerido(request);
    }

    // =========================================
    // 2) Predicción de demanda
    // =========================================
    @PostMapping("/prediccion-demanda")
    public DemandaResponse predecirDemanda(@RequestBody DemandaRequest request) {
        return iaService.predecirDemanda(request);
    }
}
