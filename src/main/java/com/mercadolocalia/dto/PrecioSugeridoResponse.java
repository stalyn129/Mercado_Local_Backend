package com.mercadolocalia.dto;

public class PrecioSugeridoResponse {

    private Integer idProducto;
    private Double precioSugerido;
    private String explicacion;

    public Integer getIdProducto() {
        return idProducto;
    }
    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Double getPrecioSugerido() {
        return precioSugerido;
    }
    public void setPrecioSugerido(Double precioSugerido) {
        this.precioSugerido = precioSugerido;
    }

    public String getExplicacion() {
        return explicacion;
    }
    public void setExplicacion(String explicacion) {
        this.explicacion = explicacion;
    }
}
