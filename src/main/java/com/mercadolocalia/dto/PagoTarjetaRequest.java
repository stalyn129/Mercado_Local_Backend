package com.mercadolocalia.dto;

public class PagoTarjetaRequest {

    private Integer idPedido;
    private Integer idConsumidor;
    private Double monto;

    private String numeroTarjeta;
    private String cvv;
    private String fechaExpiracion;
    private String titular;
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
	public String getNumeroTarjeta() {
		return numeroTarjeta;
	}
	public void setNumeroTarjeta(String numeroTarjeta) {
		this.numeroTarjeta = numeroTarjeta;
	}
	public String getCvv() {
		return cvv;
	}
	public void setCvv(String cvv) {
		this.cvv = cvv;
	}
	public String getFechaExpiracion() {
		return fechaExpiracion;
	}
	public void setFechaExpiracion(String fechaExpiracion) {
		this.fechaExpiracion = fechaExpiracion;
	}
	public String getTitular() {
		return titular;
	}
	public void setTitular(String titular) {
		this.titular = titular;
	}

    
}
