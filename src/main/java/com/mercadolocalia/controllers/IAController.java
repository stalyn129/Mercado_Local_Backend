package com.mercadolocalia.controllers;

import com.mercadolocalia.dto.RecomendacionPrecioDTO;
import com.mercadolocalia.services.IAClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ia")
@CrossOrigin(origins = "*")
public class IAController {

    @Autowired
    private IAClientService iaService;

    @PostMapping("/precio/recomendar")
    public ResponseEntity<RecomendacionPrecioDTO> recomendarPrecio(@RequestBody Map<String, Object> req) {
        try {
            String nombre = (String) req.get("nombre");
            
            // Validación de seguridad para el precio
            Object precioObj = req.get("precio");
            Double precio = 0.0;
            if (precioObj != null && !precioObj.toString().isEmpty()) {
                precio = Double.valueOf(precioObj.toString());
            }

            // Llamamos al servicio que hará la lógica de comparación
            RecomendacionPrecioDTO recomendacion = iaService.recomendarPrecio(nombre, precio);
            return ResponseEntity.ok(recomendacion);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ============================
    // 🤖 IA CHATBOT
    // ============================
    @PostMapping("/chat")
    public ResponseEntity<Object> chat(@RequestBody ChatMessage req) {
        try {
            Object respuesta = iaService.enviarMensajeChat(
                req.id_usuario,
                req.rol,
                req.mensaje
            );
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al procesar el chat");
        }
    }

    // ============================
    // 🔹 PREDICCIÓN DE PRECIO (BASADO EN ID)
    // ============================
    @GetMapping("/precio/{idProducto}")
    public ResponseEntity<Object> precio(@PathVariable Long idProducto) {
        return ResponseEntity.ok(iaService.getPrecioRecomendado(idProducto));
    }

    // ============================
    // 🔹 PREDICCIÓN DE DEMANDA
    // ============================
    @GetMapping("/demanda/{idProducto}")
    public ResponseEntity<Object> demanda(@PathVariable Long idProducto) {
        return ResponseEntity.ok(iaService.getDemanda(idProducto));
    }

    // ============================
    // 🔹 RECOMENDACIÓN PERSONALIZADA
    // ============================
    @GetMapping("/recomendar/{idUsuario}")
    public ResponseEntity<Object> recomendar(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(iaService.getRecomendaciones(idUsuario));
    }

    // --- Clase interna para el Request del Chat ---
    public static class ChatMessage {
        public Long id_usuario;
        public String rol;
        public String mensaje;
    }
}