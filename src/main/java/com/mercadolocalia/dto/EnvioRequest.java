package com.mercadolocalia.dto;

public class EnvioRequest {

    private Integer idPedido;
    private String tipoEnvio;
    private String direccionEnvio;
    private String ciudad;
    private String provincia;
    private Double costoEnvio;

    public Integer getIdPedido() {
        return idPedido;
    }
    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public String getTipoEnvio() {
        return tipoEnvio;
    }
    public void setTipoEnvio(String tipoEnvio) {
        this.tipoEnvio = tipoEnvio;
    }

    public String getDireccionEnvio() {
        return direccionEnvio;
    }
    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public String getCiudad() {
        return ciudad;
    }
    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getProvincia() {
        return provincia;
    }
    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public Double getCostoEnvio() {
        return costoEnvio;
    }
    public void setCostoEnvio(Double costoEnvio) {
        this.costoEnvio = costoEnvio;
    }
}
