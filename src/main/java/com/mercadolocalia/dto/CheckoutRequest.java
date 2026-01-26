package com.mercadolocalia.dto;

import lombok.Data;

@Data
public class CheckoutRequest {
    private Integer idConsumidor;
    private String idCompraUnificada; // 🔥 AGREGAR ESTE CAMPO

    public CheckoutRequest() {}

    public CheckoutRequest(Integer idConsumidor, String idCompraUnificada) {
        this.idConsumidor = idConsumidor;
        this.idCompraUnificada = idCompraUnificada;
    }
}