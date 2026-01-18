package com.mercadolocalia.controllers;

import com.mercadolocalia.dto.ChatbotRequest;
import com.mercadolocalia.entities.ChatHistorial;
import com.mercadolocalia.services.impl.ChatbotServiceImpl; // Importamos la implementación para acceder a los nuevos métodos
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chatbot")
@CrossOrigin(origins = "*", allowedHeaders = "*") 
public class ChatbotController {

    @Autowired
    private ChatbotServiceImpl chatbotService;

    /**
     * Endpoint principal para enviar mensajes
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody ChatbotRequest request) {
        try {
            String respuesta = chatbotService.responder(
                    request.getMensaje(),
                    request.getRol(),
                    request.getIdConsumidor(),
                    request.getIdVendedor()
            );

            return ResponseEntity.ok(Map.of("respuesta", respuesta));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("respuesta", "Lo siento, ocurrió un error interno."));
        }
    }


    @GetMapping("/historial/{idUsuario}")
    public ResponseEntity<List<ChatHistorial>> obtenerHistorial(
            @PathVariable Integer idUsuario,
            @RequestParam String rol // Recibimos el rol desde la URL
    ) {
        try {
            // Ahora pasamos idUsuario Y rol al servicio
            List<ChatHistorial> historial = chatbotService.obtenerHistorial(idUsuario, rol);
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/historial/{idUsuario}")
    public ResponseEntity<Map<String, String>> limpiarHistorial(@PathVariable Integer idUsuario) {
        try {
            chatbotService.limpiarHistorial(idUsuario);
            return ResponseEntity.ok(Map.of("mensaje", "Historial eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "No se pudo eliminar el historial"));
        }
    }
}