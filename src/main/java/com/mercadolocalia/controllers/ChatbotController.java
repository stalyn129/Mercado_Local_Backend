package com.mercadolocalia.controllers;

import com.mercadolocalia.dto.ChatbotRequest;
import com.mercadolocalia.services.ChatbotService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody ChatbotRequest request
    ) {
        String respuesta = chatbotService.responder(
                request.getMensaje(),
                request.getRol(),
                request.getIdConsumidor(),
                request.getIdVendedor()
        );

        return ResponseEntity.ok(Map.of("respuesta", respuesta));
    }
}
