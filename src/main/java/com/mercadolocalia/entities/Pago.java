package com.mercadolocalia.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPago;

    private Integer idPedido;
    private Integer idConsumidor;

    private Double monto;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodo; // TARJETA, EFECTIVO, TRANSFERENCIA

    @Enumerated(EnumType.STRING)
    private EstadoPago estado; // PAGADO, PENDIENTE, PENDIENTE_VERIFICACION

    private LocalDateTime fecha = LocalDateTime.now();

	public Integer getIdPago() {
		return idPago;
	}

	public void setIdPago(Integer idPago) {
		this.idPago = idPago;
	}

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

	public MetodoPago getMetodo() {
		return metodo;
	}

	public void setMetodo(MetodoPago metodo) {
		this.metodo = metodo;
	}

	public EstadoPago getEstado() {
		return estado;
	}

	public void setEstado(EstadoPago estado) {
		this.estado = estado;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}
    
    
    
}
