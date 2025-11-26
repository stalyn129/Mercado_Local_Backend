package com.mercadolocalia.dto;

public class PrecioSugeridoRequest {

    private Integer idProducto;
    private Double precioActual;
    private Double costo;
    private Integer stockActual;
    private Integer ventasUltimos30Dias;

    public Integer getIdProducto() {
        return idProducto;
    }
    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Double getPrecioActual() {
        return precioActual;
    }
    public void setPrecioActual(Double precioActual) {
        this.precioActual = precioActual;
    }

    public Double getCosto() {
        return costo;
    }
    public void setCosto(Double costo) {
        this.costo = costo;
    }

    public Integer getStockActual() {
        return stockActual;
    }
    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
    }

    public Integer getVentasUltimos30Dias() {
        return ventasUltimos30Dias;
    }
    public void setVentasUltimos30Dias(Integer ventasUltimos30Dias) {
        this.ventasUltimos30Dias = ventasUltimos30Dias;
    }
}
