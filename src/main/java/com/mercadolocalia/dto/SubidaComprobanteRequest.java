package com.mercadolocalia.dto;

public class SubidaComprobanteRequest {
    private Integer pedidoId;
    private String archivoBase64;
    
    // Getters y Setters
    public Integer getPedidoId() {
        return pedidoId;
    }
    
    public void setPedidoId(Integer pedidoId) {
        this.pedidoId = pedidoId;
    }
    
    public String getArchivoBase64() {
        return archivoBase64;
    }
    
    public void setArchivoBase64(String archivoBase64) {
        this.archivoBase64 = archivoBase64;
    }
}