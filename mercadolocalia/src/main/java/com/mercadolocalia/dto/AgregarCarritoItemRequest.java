package com.mercadolocalia.dto;

public class AgregarCarritoItemRequest {

    private Integer idConsumidor;
    private Integer idProducto;
    private Integer cantidad;

    public Integer getIdConsumidor() {
        return idConsumidor;
    }

    public void setIdConsumidor(Integer idConsumidor) {
        this.idConsumidor = idConsumidor;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
