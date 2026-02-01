package com.mercadolocalia.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_factura")
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_factura")
    private Integer idDetalleFactura;

    @ManyToOne
    @JoinColumn(name = "id_factura", nullable = false)
    private Factura factura;

    @ManyToOne
    @JoinColumn(name = "id_vendedor", nullable = false)
    private Vendedor vendedor;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;

    @Column(name = "subtotal_producto", nullable = false)
    private Double subtotalProducto;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // GETTERS & SETTERS
    public Integer getIdDetalleFactura() {
        return idDetalleFactura;
    }

    public void setIdDetalleFactura(Integer idDetalleFactura) {
        this.idDetalleFactura = idDetalleFactura;
    }

    public Factura getFactura() {
        return factura;
    }

    public void setFactura(Factura factura) {
        this.factura = factura;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Double getSubtotalProducto() {
        return subtotalProducto;
    }

    public void setSubtotalProducto(Double subtotalProducto) {
        this.subtotalProducto = subtotalProducto;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}