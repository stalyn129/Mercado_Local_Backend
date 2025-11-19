package com.mercadolocalia.dto;

public class DemandaRequest {

    private Integer idProducto;
    private Integer horizonteDias;
    private Integer ventasUltimos30Dias;
    private Integer stockActual;

    public Integer getIdProducto() {
        return idProducto;
    }
    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getHorizonteDias() {
        return horizonteDias;
    }
    public void setHorizonteDias(Integer horizonteDias) {
        this.horizonteDias = horizonteDias;
    }

    public Integer getVentasUltimos30Dias() {
        return ventasUltimos30Dias;
    }
    public void setVentasUltimos30Dias(Integer ventasUltimos30Dias) {
        this.ventasUltimos30Dias = ventasUltimos30Dias;
    }

    public Integer getStockActual() {
        return stockActual;
    }
    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
    }
}
