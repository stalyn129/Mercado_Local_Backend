package com.mercadolocalia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.EnvioRequest;
import com.mercadolocalia.dto.EnvioUpdateEstadoRequest;
import com.mercadolocalia.entities.Envio;
import com.mercadolocalia.services.EnvioService;

@RestController
@RequestMapping("/envios")
@CrossOrigin(origins = "*")
public class EnvioController {

    @Autowired
    private EnvioService envioService;

    @PostMapping("/crear")
    public Envio crearEnvio(@RequestBody EnvioRequest request) {
        return envioService.crearEnvio(request);
    }

    @PutMapping("/estado/{idEnvio}")
    public Envio actualizarEstado(@PathVariable Integer idEnvio,
                                  @RequestBody EnvioUpdateEstadoRequest request) {
        return envioService.actualizarEstado(idEnvio, request);
    }

    @GetMapping("/pedido/{idPedido}")
    public Envio obtenerPorPedido(@PathVariable Integer idPedido) {
        return envioService.obtenerPorPedido(idPedido);
    }

    @GetMapping("/{idEnvio}")
    public Envio obtenerPorId(@PathVariable Integer idEnvio) {
        return envioService.obtenerPorId(idEnvio);
    }
}
