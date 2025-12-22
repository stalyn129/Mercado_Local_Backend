package com.mercadolocalia.services;
public interface ChatbotService {

    String responder(
            String mensaje,
            String rol,
            Integer idConsumidor,
            Integer idVendedor
    );

}
