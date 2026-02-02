package com.mercadolocalia.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;
    
    @ManyToOne
    @JoinColumn(name = "id_consumidor", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Consumidor consumidor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendedor", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Vendedor vendedor;
    
    @Column(name = "id_compra_unificada")
    private String idCompraUnificada;

    @OneToMany(
        mappedBy = "pedido", 
        cascade = CascadeType.ALL, 
        orphanRemoval = true, 
        fetch = FetchType.LAZY
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<DetallePedido> detalles = new ArrayList<>();
    
    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pedido")
    private EstadoPedido estadoPedido;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pedido_vendedor")
    private EstadoPedidoVendedor estadoPedidoVendedor;
    
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
    
    // 🔥 CAMPOS DE PAGO MODIFICADOS
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago")
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;
    
    @Column(name = "comprobante_url", nullable = true)
    private String comprobanteUrl;
    
    @Column(name = "fecha_subida_comprobante", nullable = true)
    private LocalDateTime fechaSubidaComprobante;
    
    @Column(name = "fecha_verificacion_pago", nullable = true)
    private LocalDateTime fechaVerificacionPago;
    
    @Column(name = "verificado_por", nullable = true)
    private Integer verificadoPor; // ID del vendedor/admin que verificó
    
    @Column(name = "motivo_rechazo", length = 500, nullable = true)
    private String motivoRechazo;
    
    @Column(name = "datos_tarjeta", nullable = true)
    private String datosTarjeta;
    
    // 🔥 ELIMINAR: Ya no necesitamos el booleano 'pagado'
    // @Column(name = "pagado")
    // private Boolean pagado = false;

    // 🔥 RELACIÓN CON LA ENTIDAD PAGO (SI LA MANTIENES)
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"pedido", "hibernateLazyInitializer", "handler"})
    private Pago pago;
    
    // Constructor
    public Pedido() {
        this.fechaPedido = LocalDateTime.now();
        this.estadoPago = EstadoPago.PENDIENTE;
        this.estadoPedido = EstadoPedido.CREADO;
    }
    
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
    
    public String getIdCompraUnificada() {
        return idCompraUnificada;
    }
    
    public void setIdCompraUnificada(String idCompraUnificada) {
        this.idCompraUnificada = idCompraUnificada;
    }
    
    public List<DetallePedido> getDetalles() {
        return detalles;
    }
    
    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
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
    
    public EstadoPedidoVendedor getEstadoPedidoVendedor() {
        return estadoPedidoVendedor;
    }
    
    public void setEstadoPedidoVendedor(EstadoPedidoVendedor estadoPedidoVendedor) {
        this.estadoPedidoVendedor = estadoPedidoVendedor;
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
    
    // 🔥 NUEVOS GETTERS Y SETTERS PARA PAGO
    public EstadoPago getEstadoPago() {
        return estadoPago;
    }
    
    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }
    
    public String getComprobanteUrl() {
        return comprobanteUrl;
    }
    
    public void setComprobanteUrl(String comprobanteUrl) {
        this.comprobanteUrl = comprobanteUrl;
    }
    
    public LocalDateTime getFechaSubidaComprobante() {
        return fechaSubidaComprobante;
    }
    
    public void setFechaSubidaComprobante(LocalDateTime fechaSubidaComprobante) {
        this.fechaSubidaComprobante = fechaSubidaComprobante;
    }
    
    public LocalDateTime getFechaVerificacionPago() {
        return fechaVerificacionPago;
    }
    
    public void setFechaVerificacionPago(LocalDateTime fechaVerificacionPago) {
        this.fechaVerificacionPago = fechaVerificacionPago;
    }
    
    public Integer getVerificadoPor() {
        return verificadoPor;
    }
    
    public void setVerificadoPor(Integer verificadoPor) {
        this.verificadoPor = verificadoPor;
    }
    
    public String getMotivoRechazo() {
        return motivoRechazo;
    }
    
    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }
    
    public String getDatosTarjeta() {
        return datosTarjeta;
    }
    
    public void setDatosTarjeta(String datosTarjeta) {
        this.datosTarjeta = datosTarjeta;
    }
    
    public Pago getPago() {
        return pago;
    }
    
    public void setPago(Pago pago) {
        this.pago = pago;
    }
    
    // 🔥 MÉTODO PARA SUBIR COMPROBANTE
    public void subirComprobante(String urlComprobante) {
        this.comprobanteUrl = urlComprobante;
        this.fechaSubidaComprobante = LocalDateTime.now();
        this.estadoPago = EstadoPago.EN_VERIFICACION;
        this.estadoPedido = EstadoPedido.PENDIENTE;
    }
    
 // En tu entidad Pedido.java, actualiza el método verificarPago
    public void verificarPago(boolean aprobado, Integer idVerificador, String motivo) {
        if (aprobado) {
            this.estadoPago = EstadoPago.PAGADO;
            this.estadoPedido = EstadoPedido.PROCESANDO;
            this.estadoPedidoVendedor = EstadoPedidoVendedor.EN_PROCESO;
            this.estadoSeguimiento = EstadoSeguimientoPedido.RECOLECTANDO;
        } else {
            this.estadoPago = EstadoPago.RECHAZADO;
            this.motivoRechazo = motivo;
        }
        
        this.fechaVerificacionPago = LocalDateTime.now();
        this.verificadoPor = idVerificador;  // Esto ahora es Integer
    }
}