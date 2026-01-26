package com.mercadolocalia.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPago;

    @OneToOne
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido; // 🔥 CAMBIAR: Relación en lugar de solo id

    @Column(name = "id_consumidor", nullable = false)
    private Integer idConsumidor;

    @Column(nullable = false)
    private Double monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPago metodo; // TARJETA, EFECTIVO, TRANSFERENCIA

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPago estado; // PENDIENTE, EN_VERIFICACION, PAGADO, RECHAZADO, CANCELADO

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "referencia_pago", length = 100)
    private String referenciaPago; // Para transferencias, número de referencia

    @Column(name = "datos_transaccion", length = 500)
    private String datosTransaccion; // Información adicional de la transacción

    // Constructor
    public Pago() {
        this.fechaPago = LocalDateTime.now();
    }

    // 🔥 MÉTODO PARA ACTUALIZAR FECHA DE ACTUALIZACIÓN
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Integer getIdPago() {
        return idPago;
    }

    public void setIdPago(Integer idPago) {
        this.idPago = idPago;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
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
        this.fechaActualizacion = LocalDateTime.now(); // Actualizar al cambiar estado
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getReferenciaPago() {
        return referenciaPago;
    }

    public void setReferenciaPago(String referenciaPago) {
        this.referenciaPago = referenciaPago;
    }

    public String getDatosTransaccion() {
        return datosTransaccion;
    }

    public void setDatosTransaccion(String datosTransaccion) {
        this.datosTransaccion = datosTransaccion;
    }
}