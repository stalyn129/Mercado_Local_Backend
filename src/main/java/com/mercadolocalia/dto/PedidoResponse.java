package com.mercadolocalia.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PedidoResponse {

    private Integer idPedido;
    private String numeroPedido;

    // 👇 ESTADOS (CLAVE)
    private String estadoPedido;            // técnico
    private String estadoSeguimiento;       // visual para cliente

    private Double subtotal;
    private Double iva;
    private Double total;
    private LocalDateTime fechaPedido;

    // Opcional (para vista detalle)
    private List<DetallePedidoAddRequest> detalles;

    // getters y setters
    

	public Integer getIdPedido() {
		return idPedido;
	}

	public void setIdPedido(Integer idPedido) {
		this.idPedido = idPedido;
	}

	public String getNumeroPedido() {
		return numeroPedido;
	}

	public void setNumeroPedido(String numeroPedido) {
		this.numeroPedido = numeroPedido;
	}

	public String getEstadoPedido() {
		return estadoPedido;
	}

	public void setEstadoPedido(String estadoPedido) {
		this.estadoPedido = estadoPedido;
	}

	public String getEstadoSeguimiento() {
		return estadoSeguimiento;
	}

	public void setEstadoSeguimiento(String estadoSeguimiento) {
		this.estadoSeguimiento = estadoSeguimiento;
	}

	public Double getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(Double subtotal) {
		this.subtotal = subtotal;
	}

	public Double getIva() {
		return iva;
	}

	public void setIva(Double iva) {
		this.iva = iva;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}

	public LocalDateTime getFechaPedido() {
		return fechaPedido;
	}

	public void setFechaPedido(LocalDateTime fechaPedido) {
		this.fechaPedido = fechaPedido;
	}

	public List<DetallePedidoAddRequest> getDetalles() {
		return detalles;
	}

	public void setDetalles(List<DetallePedidoAddRequest> detalles) {
		this.detalles = detalles;
	}

    
}
