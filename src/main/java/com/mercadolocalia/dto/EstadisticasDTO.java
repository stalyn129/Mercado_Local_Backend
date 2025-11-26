package com.mercadolocalia.dto;

public class EstadisticasDTO {

    private Double ingresosTotales;
    private Integer totalPedidos;
    private Integer productosDisponibles;

    public Double getIngresosTotales() {
        return ingresosTotales;
    }

    public void setIngresosTotales(Double ingresosTotales) {
        this.ingresosTotales = ingresosTotales;
    }

    public Integer getTotalPedidos() {
        return totalPedidos;
    }

    public void setTotalPedidos(Integer totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public Integer getProductosDisponibles() {
        return productosDisponibles;
    }

    public void setProductosDisponibles(Integer productosDisponibles) {
        this.productosDisponibles = productosDisponibles;
    }
}
