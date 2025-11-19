package com.mercadolocalia.dto;

public class DemandaResponse {

    private Integer idProducto;
    private Integer horizonteDias;
    private Double demandaEsperada;
    private String recomendacion;

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

    public Double getDemandaEsperada() {
        return demandaEsperada;
    }
    public void setDemandaEsperada(Double demandaEsperada) {
        this.demandaEsperada = demandaEsperada;
    }

    public String getRecomendacion() {
        return recomendacion;
    }
    public void setRecomendacion(String recomendacion) {
        this.recomendacion = recomendacion;
    }
}
