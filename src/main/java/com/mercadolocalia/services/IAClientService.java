package com.mercadolocalia.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IAClientService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String IA_URL = "http://localhost:8000/api/ia";

    public Object getPrecioRecomendado(Long productId) {
        String url = IA_URL + "/precio/" + productId;
        return restTemplate.getForObject(url, Object.class);
    }

    public Object getDemanda(Long productId) {
        String url = IA_URL + "/demanda/" + productId;
        return restTemplate.getForObject(url, Object.class);
    }

    public Object getRecomendaciones(Long userId) {
        String url = IA_URL + "/recomendar/" + userId;
        return restTemplate.getForObject(url, Object.class);
    }

    public Object enviarMensajeChat(Long userId, String rol, String mensaje) {
        String url = IA_URL + "/chat";

        Map<String, Object> body = new HashMap<>();
        body.put("id_usuario", userId);
        body.put("rol", rol);
        body.put("mensaje", mensaje);

        return restTemplate.postForObject(url, body, Object.class);
    }


    public Object recomendarPrecio(String nombre, Double precio) {
        String endpoint = IA_URL + "/precio/recomendar";

        Map<String, Object> body = new HashMap<>();
        body.put("nombre", nombre);
        body.put("precio", precio);

        return restTemplate.postForObject(endpoint, body, Object.class);
    }
}
