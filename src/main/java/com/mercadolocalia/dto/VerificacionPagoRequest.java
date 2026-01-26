package com.mercadolocalia.dto;

public class VerificacionPagoRequest {
    private boolean aprobado;
    private String motivo;
    
    // Getters y Setters
    public boolean isAprobado() {
        return aprobado;
    }
    
    public void setAprobado(boolean aprobado) {
        this.aprobado = aprobado;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}