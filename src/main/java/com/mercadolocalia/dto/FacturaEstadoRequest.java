package com.mercadolocalia.dto;

public class FacturaEstadoRequest {

    private String estado; // Emitida / Anulada

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
