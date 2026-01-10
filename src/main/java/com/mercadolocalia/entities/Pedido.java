package com.mercadolocalia.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.mercadolocalia.entities.EstadoPedido;

@Entity
@Table(name = "pedidos")
public class Pedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;
    
    @ManyToOne
    @JoinColumn(name = "id_consumidor", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})  // ✅
    private Consumidor consumidor;
    
    @ManyToOne(fetch = FetchType.LAZY)  // ✅ Agregar LAZY
    @JoinColumn(name = "id_vendedor", nullable = true)  // ✅ Cambiar a nullable = true
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})  // ✅
    private Vendedor vendedor;
    
    @OneToMany(
        mappedBy = "pedido", 
        cascade = CascadeType.ALL, 
        orphanRemoval = true, 
        fetch = FetchType.LAZY
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})  // ✅
    private List<DetallePedido> detalles = new ArrayList<>();
    
    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pedido")
    private EstadoPedido estadoPedido;
    
    @Enumerated(EnumType.STRING)
    private EstadoSeguimientoPedido estadoSeguimiento;
    
    @Column(name = "subtotal")
    private Double subtotal;
    
    @Column(name = "iva")
    private Double iva;
    
    @Column(name = "total")
    private Double total;
    
    @Column(name = "metodo_pago")
    private String metodoPago;
    
    @Column(name = "comprobante_url", nullable = true)
    private String comprobanteUrl;
    
    @Column(name = "datos_tarjeta", nullable = true)
    private String datosTarjeta;

    // Getters y Setters
    
    public Integer getIdPedido() {
        return idPedido;
    }
    
    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }
    
    public Consumidor getConsumidor() {
        return consumidor;
    }
    
    public void setConsumidor(Consumidor consumidor) {
        this.consumidor = consumidor;
    }
    
    public Vendedor getVendedor() {
        return vendedor;
    }
    
    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
    }
    
    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }
    
    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }
    
    public EstadoPedido getEstadoPedido() {
        return estadoPedido;
    }
    
    public void setEstadoPedido(EstadoPedido estadoPedido) {
        this.estadoPedido = estadoPedido;
    }
    
    public EstadoSeguimientoPedido getEstadoSeguimiento() {
        return estadoSeguimiento;
    }
    
    public void setEstadoSeguimiento(EstadoSeguimientoPedido estadoSeguimiento) {
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
    
    public String getMetodoPago() {
        return metodoPago;
    }
    
    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
    
    public List<DetallePedido> getDetalles() {
        return detalles;
    }
    
    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }
    
    public String getComprobanteUrl() {
        return comprobanteUrl;
    }
    
    public void setComprobanteUrl(String comprobanteUrl) {
        this.comprobanteUrl = comprobanteUrl;
    }
    
    public String getDatosTarjeta() {
        return datosTarjeta;
    }
    
    public void setDatosTarjeta(String datosTarjeta) {
        this.datosTarjeta = datosTarjeta;
    }
}