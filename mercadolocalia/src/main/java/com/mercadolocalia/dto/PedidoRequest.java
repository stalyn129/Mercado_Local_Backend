package com.mercadolocalia.dto;

import java.util.List;

public class PedidoRequest {

    private Integer idConsumidor;
    private Integer idVendedor;
    private String metodoPago;

    private List<DetallePedidoAddRequest> detalles;

    public Integer getIdConsumidor() {
        return idConsumidor;
    }

    public void setIdConsumidor(Integer idConsumidor) {
        this.idConsumidor = idConsumidor;
    }

    public Integer getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(Integer idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public List<DetallePedidoAddRequest> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedidoAddRequest> detalles) {
        this.detalles = detalles;
    }
}
