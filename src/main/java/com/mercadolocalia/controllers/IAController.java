package com.mercadolocalia.controllers;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import com.mercadolocalia.services.IAClientService;

@RestController
@RequestMapping("/api/ia")
@CrossOrigin(origins = "*")
public class IAController {

    @Autowired
    private IAClientService iaService;

    @GetMapping("/precio/{idProducto}")
    public Object precio(@PathVariable Long idProducto) {
        return iaService.getPrecioRecomendado(idProducto);
    }

    @GetMapping("/demanda/{idProducto}")
    public Object demanda(@PathVariable Long idProducto) {
        return iaService.getDemanda(idProducto);
    }

    @GetMapping("/recomendar/{idUsuario}")
    public Object recomendar(@PathVariable Long idUsuario) {
        return iaService.getRecomendaciones(idUsuario);
    }

    @PostMapping("/chat")
    public Object chat(@RequestBody ChatMessage req) {
        return iaService.enviarMensajeChat(req.id_usuario, req.rol, req.mensaje);
    }

    public static class ChatMessage {
        public Long id_usuario;
        public String rol;
        public String mensaje;
    }
    
    @PostMapping("/precio/recomendar")
    public Object recomendarPrecio(@RequestBody Map<String, String> req) {
        return iaService.recomendarPrecio(req.get("nombre"), req.get("precio"));
    }

}
