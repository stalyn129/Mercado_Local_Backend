package com.mercadolocalia.dto;

public class ConsumidorRequest {

    private Integer idUsuario;
    private String cedulaConsumidor;
    private String direccionConsumidor;
    private String telefonoConsumidor;

    public Integer getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    public String getCedulaConsumidor() {
        return cedulaConsumidor;
    }
    public void setCedulaConsumidor(String cedulaConsumidor) {
        this.cedulaConsumidor = cedulaConsumidor;
    }
    public String getDireccionConsumidor() {
        return direccionConsumidor;
    }
    public void setDireccionConsumidor(String direccionConsumidor) {
        this.direccionConsumidor = direccionConsumidor;
    }
    public String getTelefonoConsumidor() {
        return telefonoConsumidor;
    }
    public void setTelefonoConsumidor(String telefonoConsumidor) {
        this.telefonoConsumidor = telefonoConsumidor;
    }
}
