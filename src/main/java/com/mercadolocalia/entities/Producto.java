package com.mercadolocalia.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    // ============================
    // RELACIÓN: VENDEDOR
    // ============================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendedor", nullable = false)
    private Vendedor vendedor;

    // ============================
    // RELACIÓN: SUBCATEGORÍA
    // ============================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_subcategoria", nullable = false)
    private Subcategoria subcategoria;

    // ============================
    // DATOS PRINCIPALES
    // ============================
    @Column(name = "nombre_producto", nullable = false, length = 150)
    private String nombreProducto;

    @Column(name = "descripcion_producto", nullable = false, columnDefinition = "TEXT")
    private String descripcionProducto;

    @Column(name = "precio_producto", nullable = false)
    private Double precioProducto;

    @Column(name = "stock_producto", nullable = false)
    private Integer stockProducto;

    // ========= NUEVO ⚠ — Unidad de medida (Kg/Lb/Litro/Unidad/etc) ========== 
    @Column(name = "unidad", length = 20, nullable = false)
    private String unidad; // <-- MUY IMPORTANTE PARA TU FRONTEND

    // ============================
    // IMAGEN (URL)
    // ============================
    @Column(name = "imagen_producto", length = 500)
    private String imagenProducto;

    // ============================
    // FECHA Y ESTADO
    // ============================
    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "estado", length = 20)
    private String estado = "Disponible"; // Valor por defecto

    // ============================
    // BORRADO LÓGICO - CAMPOS NUEVOS
    // ============================
    @Column(name = "activo", nullable = false)
    private Boolean activo = true; // true = visible, false = "eliminado" lógicamente

    @Column(name = "fecha_desactivacion")
    private LocalDateTime fechaDesactivacion;

    @Column(name = "motivo_desactivacion", length = 200)
    private String motivoDesactivacion;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion = LocalDateTime.now();

    // ============================
    // VALORACIONES
    // ============================
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Valoracion> valoraciones;

    // ============================
    // CONSTRUCTORES
    // ============================
    
    public Producto() {
        // Valores por defecto
        this.activo = true;
        this.estado = "Disponible";
        this.fechaPublicacion = LocalDateTime.now();
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    public Producto(String nombreProducto, String descripcionProducto, 
                   Double precioProducto, Integer stockProducto, String unidad) {
        this();
        this.nombreProducto = nombreProducto;
        this.descripcionProducto = descripcionProducto;
        this.precioProducto = precioProducto;
        this.stockProducto = stockProducto;
        this.unidad = unidad;
    }

    // ============================
    // MÉTODOS PARA BORRADO LÓGICO
    // ============================
    
    /**
     * Desactiva el producto (borrado lógico)
     * @param motivo Razón de la desactivación
     */
    public void desactivar(String motivo) {
        this.activo = false;
        this.estado = "Inactivo";
        this.fechaDesactivacion = LocalDateTime.now();
        this.motivoDesactivacion = motivo;
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    /**
     * Desactiva el producto con motivo por defecto
     */
    public void desactivar() {
        desactivar("Desactivado por administrador");
    }
    
    /**
     * Reactiva el producto
     */
    public void reactivar() {
        this.activo = true;
        this.estado = "Disponible";
        this.fechaDesactivacion = null;
        this.motivoDesactivacion = null;
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    /**
     * Verifica si el producto está activo
     * @return true si está activo, false si está inactivo
     */
    public boolean estaActivo() {
        return Boolean.TRUE.equals(activo) && !"Inactivo".equals(estado);
    }
    
    /**
     * Verifica si el producto está disponible para venta
     * @return true si está activo y disponible
     */
    public boolean estaDisponible() {
        return estaActivo() && "Disponible".equals(estado) && stockProducto > 0;
    }
    
    /**
     * Actualiza el stock y marca como actualizado
     * @param nuevoStock Nuevo valor de stock
     */
    public void actualizarStock(Integer nuevoStock) {
        this.stockProducto = nuevoStock;
        this.ultimaActualizacion = LocalDateTime.now();
        
        // Actualizar estado según stock
        if (nuevoStock <= 0) {
            this.estado = "Agotado";
        } else if (nuevoStock <= 10 && "Disponible".equals(this.estado)) {
            this.estado = "Stock Bajo";
        }
    }
    
    /**
     * Actualiza el precio y marca como actualizado
     * @param nuevoPrecio Nuevo precio
     */
    public void actualizarPrecio(Double nuevoPrecio) {
        this.precioProducto = nuevoPrecio;
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    /**
     * Actualiza el nombre y marca como actualizado
     * @param nuevoNombre Nuevo nombre
     */
    public void actualizarNombre(String nuevoNombre) {
        this.nombreProducto = nuevoNombre;
        this.ultimaActualizacion = LocalDateTime.now();
    }

    // ============================
    // GETTERS & SETTERS COMPLETOS
    // ============================

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }

    public Vendedor getVendedor() { return vendedor; }
    public void setVendedor(Vendedor vendedor) { 
        this.vendedor = vendedor; 
        this.ultimaActualizacion = LocalDateTime.now();
    }

    public Subcategoria getSubcategoria() { return subcategoria; }
    public void setSubcategoria(Subcategoria subcategoria) { 
        this.subcategoria = subcategoria; 
        this.ultimaActualizacion = LocalDateTime.now();
    }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { 
        this.nombreProducto = nombreProducto; 
        this.ultimaActualizacion = LocalDateTime.now();
    }

    public String getDescripcionProducto() { return descripcionProducto; }
    public void setDescripcionProducto(String descripcionProducto) { 
        this.descripcionProducto = descripcionProducto; 
        this.ultimaActualizacion = LocalDateTime.now();
    }

    public Double getPrecioProducto() { return precioProducto; }
    public void setPrecioProducto(Double precioProducto) { 
        this.precioProducto = precioProducto; 
        this.ultimaActualizacion = LocalDateTime.now();
    }

    public Integer getStockProducto() { return stockProducto; }
    public void setStockProducto(Integer stockProducto) { 
        this.stockProducto = stockProducto; 
        this.ultimaActualizacion = LocalDateTime.now();
        
        // Actualizar estado según stock
        if (stockProducto <= 0) {
            this.estado = "Agotado";
        } else if (stockProducto <= 10 && "Disponible".equals(this.estado)) {
            this.estado = "Stock Bajo";
        }
    }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { 
        this.unidad = unidad; 
        this.ultimaActualizacion = LocalDateTime.now();
    }

    public String getImagenProducto() { return imagenProducto; }
    public void setImagenProducto(String imagenProducto) { 
        this.imagenProducto = imagenProducto; 
        this.ultimaActualizacion = LocalDateTime.now();
    }

    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) { 
        this.fechaPublicacion = fechaPublicacion; 
        this.ultimaActualizacion = LocalDateTime.now();
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { 
        this.estado = estado; 
        this.ultimaActualizacion = LocalDateTime.now();
    }

    // ============================
    // GETTERS & SETTERS BORRADO LÓGICO
    // ============================
    
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { 
        this.activo = activo; 
        this.ultimaActualizacion = LocalDateTime.now();
        
        if (Boolean.FALSE.equals(activo)) {
            this.estado = "Inactivo";
            if (this.fechaDesactivacion == null) {
                this.fechaDesactivacion = LocalDateTime.now();
            }
        }
    }
    
    public LocalDateTime getFechaDesactivacion() { return fechaDesactivacion; }
    public void setFechaDesactivacion(LocalDateTime fechaDesactivacion) { 
        this.fechaDesactivacion = fechaDesactivacion; 
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    public String getMotivoDesactivacion() { return motivoDesactivacion; }
    public void setMotivoDesactivacion(String motivoDesactivacion) { 
        this.motivoDesactivacion = motivoDesactivacion; 
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    public LocalDateTime getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) { 
        this.ultimaActualizacion = ultimaActualizacion; 
    }

    // ============================
    // RELACIONES
    // ============================
    
    public List<Valoracion> getValoraciones() { return valoraciones; }
    public void setValoraciones(List<Valoracion> valoraciones) { 
        this.valoraciones = valoraciones; 
        this.ultimaActualizacion = LocalDateTime.now();
    }

    // ============================
    // MÉTODOS UTILITARIOS
    // ============================
    
    @Override
    public String toString() {
        return "Producto{" +
                "idProducto=" + idProducto +
                ", nombreProducto='" + nombreProducto + '\'' +
                ", precioProducto=" + precioProducto +
                ", stockProducto=" + stockProducto +
                ", unidad='" + unidad + '\'' +
                ", estado='" + estado + '\'' +
                ", activo=" + activo +
                '}';
    }
    
    /**
     * Obtiene la URL completa de la imagen
     * @param baseUrl URL base del servidor (ej: "http://localhost:8080")
     * @return URL completa o null si no hay imagen
     */
    public String obtenerImagenUrlCompleta(String baseUrl) {
        if (imagenProducto == null || imagenProducto.trim().isEmpty()) {
            return null;
        }
        
        if (imagenProducto.startsWith("http")) {
            return imagenProducto;
        }
        
        return baseUrl + "/uploads/" + imagenProducto;
    }
    
    /**
     * Verifica si el producto necesita reabastecimiento
     * @return true si el stock es bajo
     */
    public boolean necesitaReabastecimiento() {
        return estaActivo() && stockProducto <= 10 && stockProducto > 0;
    }
    
    /**
     * Verifica si el producto está agotado
     * @return true si el stock es 0
     */
    public boolean estaAgotado() {
        return stockProducto <= 0 && estaActivo();
    }
}