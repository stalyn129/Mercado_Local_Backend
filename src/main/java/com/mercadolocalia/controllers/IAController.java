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

    // ============================
    // 🔹 PREDICCIÓN DE PRECIO
    // ============================
    @GetMapping("/precio/{idProducto}")
    public Object precio(@PathVariable Long idProducto) {
        return iaService.getPrecioRecomendado(idProducto);
    }

    // ============================
    // 🔹 PREDICCIÓN DE DEMANDA
    // ============================
    @GetMapping("/demanda/{idProducto}")
    public Object demanda(@PathVariable Long idProducto) {
        return iaService.getDemanda(idProducto);
    }

    // ============================
    // 🔹 RECOMENDACIÓN PERSONALIZADA
    // ============================
    @GetMapping("/recomendar/{idUsuario}")
    public Object recomendar(@PathVariable Long idUsuario) {
        return iaService.getRecomendaciones(idUsuario);
    }

    // ============================
    // 🤖 IA CHATBOT
    // ============================
    @PostMapping("/chat")
    public Object chat(@RequestBody ChatMessage req) {
        return iaService.enviarMensajeChat(
            req.id_usuario,
            req.rol,
            req.mensaje
        );
    }

    public static class ChatMessage {
        public Long id_usuario;
        public String rol;
        public String mensaje;
    }

    // ============================
    // 💰 RECOMENDACIÓN DE PRECIO
    // ============================
    @PostMapping("/precio/recomendar")
    public Object recomendarPrecio(@RequestBody Map<String, Object> req) {
        String nombre = (String) req.get("nombre");
        Double precio = Double.valueOf(req.get("precio").toString());
        return iaService.recomendarPrecio(nombre, precio);
    }
}
