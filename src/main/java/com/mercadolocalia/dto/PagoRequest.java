package com.mercadolocalia.dto;

import com.mercadolocalia.entities.MetodoPago;

public class PagoRequest {

    private Integer idPedido;
    private Integer idConsumidor;
    private Double monto;
    private MetodoPago metodoPago;
	public Integer getIdPedido() {
		return idPedido;
	}
	public void setIdPedido(Integer idPedido) {
		this.idPedido = idPedido;
	}
	public Integer getIdConsumidor() {
		return idConsumidor;
	}
	public void setIdConsumidor(Integer idConsumidor) {
		this.idConsumidor = idConsumidor;
	}
	public Double getMonto() {
		return monto;
	}
	public void setMonto(Double monto) {
		this.monto = monto;
	}
	public MetodoPago getMetodoPago() {
		return metodoPago;
	}
	public void setMetodoPago(MetodoPago metodoPago) {
		this.metodoPago = metodoPago;
	}

    
}
