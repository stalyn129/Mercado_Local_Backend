package com.mercadolocalia.dto;

import java.time.LocalDateTime;

public class ProductoResponse {

    private Integer idProducto;
    private String nombreProducto;
    private String descripcionProducto;
    private Double precioProducto;
    private Integer stockProducto;
    private String imagenProducto;
    private LocalDateTime fechaPublicacion;
    private String estado;

    private Integer idVendedor;
    private String nombreEmpresa;

    private Integer idSubcategoria;
    private String nombreSubcategoria;

    private Integer idCategoria;
    private String nombreCategoria;

    // ⭐ Importante para mostrar precio/unidad
    private String unidad;
    
    // ⭐ NUEVO: Promedio de estrellas y total de reseñas
    private Double promedioValoracion;
    private Integer totalValoraciones;

    // ====================== CAMPOS PARA BORRADO LÓGICO ======================
    private Boolean activo = true;
    private LocalDateTime fechaDesactivacion;
    private String motivoDesactivacion;
    private LocalDateTime ultimaActualizacion;
    
    // ====================== URL COMPLETA DE IMAGEN ======================
    private String imagenUrlCompleta;

    // ====================== CONSTRUCTORES ======================
    public ProductoResponse() {
        this.activo = true;
        this.estado = "Disponible";
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    // Constructor completo para consultas JPQL
    public ProductoResponse(Integer idProducto, String nombreProducto, 
                           String descripcionProducto, Double precioProducto, 
                           Integer stockProducto, String unidad, String imagenProducto,
                           LocalDateTime fechaPublicacion, String estado,
                           Integer idVendedor, String nombreEmpresa,
                           Integer idSubcategoria, String nombreSubcategoria,
                           Integer idCategoria, String nombreCategoria,
                           Double promedioValoracion, Integer totalValoraciones,
                           Boolean activo, LocalDateTime fechaDesactivacion,
                           String motivoDesactivacion, LocalDateTime ultimaActualizacion) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.descripcionProducto = descripcionProducto;
        this.precioProducto = precioProducto;
        this.stockProducto = stockProducto;
        this.unidad = unidad;
        this.imagenProducto = imagenProducto;
        this.fechaPublicacion = fechaPublicacion;
        this.estado = estado;
        this.idVendedor = idVendedor;
        this.nombreEmpresa = nombreEmpresa;
        this.idSubcategoria = idSubcategoria;
        this.nombreSubcategoria = nombreSubcategoria;
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.promedioValoracion = promedioValoracion;
        this.totalValoraciones = totalValoraciones;
        this.activo = activo != null ? activo : true;
        this.fechaDesactivacion = fechaDesactivacion;
        this.motivoDesactivacion = motivoDesactivacion;
        this.ultimaActualizacion = ultimaActualizacion != null ? ultimaActualizacion : LocalDateTime.now();
        
        // Generar URL completa de imagen
        if (imagenProducto != null && !imagenProducto.isEmpty()) {
            if (imagenProducto.startsWith("http")) {
                this.imagenUrlCompleta = imagenProducto;
            } else {
                this.imagenUrlCompleta = "http://localhost:8080/uploads/" + imagenProducto;
            }
        }
    }
    
    // Constructor simplificado
    public ProductoResponse(Integer idProducto, String nombreProducto, 
                           Double precioProducto, Integer stockProducto, 
                           String unidad, String imagenProducto, String estado,
                           String nombreEmpresa, String nombreSubcategoria, 
                           String nombreCategoria) {
        this();
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.precioProducto = precioProducto;
        this.stockProducto = stockProducto;
        this.unidad = unidad;
        this.imagenProducto = imagenProducto;
        this.estado = estado;
        this.nombreEmpresa = nombreEmpresa;
        this.nombreSubcategoria = nombreSubcategoria;
        this.nombreCategoria = nombreCategoria;
        
        // Generar URL completa de imagen
        if (imagenProducto != null && !imagenProducto.isEmpty()) {
            if (imagenProducto.startsWith("http")) {
                this.imagenUrlCompleta = imagenProducto;
            } else {
                this.imagenUrlCompleta = "http://localhost:8080/uploads/" + imagenProducto;
            }
        }
    }

    // ====================== GETTERS Y SETTERS ======================
    
    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getDescripcionProducto() { return descripcionProducto; }
    public void setDescripcionProducto(String descripcionProducto) { this.descripcionProducto = descripcionProducto; }

    public Double getPrecioProducto() { return precioProducto; }
    public void setPrecioProducto(Double precioProducto) { this.precioProducto = precioProducto; }

    public Integer getStockProducto() { return stockProducto; }
    public void setStockProducto(Integer stockProducto) { this.stockProducto = stockProducto; }

    public String getImagenProducto() { return imagenProducto; }
    public void setImagenProducto(String imagenProducto) { 
        this.imagenProducto = imagenProducto;
        // Actualizar URL completa al cambiar imagen
        if (imagenProducto != null && !imagenProducto.isEmpty()) {
            if (imagenProducto.startsWith("http")) {
                this.imagenUrlCompleta = imagenProducto;
            } else {
                this.imagenUrlCompleta = "http://localhost:8080/uploads/" + imagenProducto;
            }
        } else {
            this.imagenUrlCompleta = null;
        }
    }

    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { 
        this.estado = estado; 
        this.ultimaActualizacion = LocalDateTime.now();
    }

    public Integer getIdVendedor() { return idVendedor; }
    public void setIdVendedor(Integer idVendedor) { this.idVendedor = idVendedor; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public Integer getIdSubcategoria() { return idSubcategoria; }
    public void setIdSubcategoria(Integer idSubcategoria) { this.idSubcategoria = idSubcategoria; }

    public String getNombreSubcategoria() { return nombreSubcategoria; }
    public void setNombreSubcategoria(String nombreSubcategoria) { this.nombreSubcategoria = nombreSubcategoria; }

    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }

    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { 
        this.unidad = unidad; 
        this.ultimaActualizacion = LocalDateTime.now();
    }

    public Double getPromedioValoracion() { return promedioValoracion; }
    public void setPromedioValoracion(Double promedioValoracion) { this.promedioValoracion = promedioValoracion; }

    public Integer getTotalValoraciones() { return totalValoraciones; }
    public void setTotalValoraciones(Integer totalValoraciones) { this.totalValoraciones = totalValoraciones; }
    
    // ====================== GETTERS Y SETTERS BORRADO LÓGICO ======================
    
    public Boolean getActivo() { 
        return activo != null ? activo : true; // Por defecto true
    }
    
    public void setActivo(Boolean activo) { 
        this.activo = activo; 
        this.ultimaActualizacion = LocalDateTime.now();
        if (Boolean.FALSE.equals(activo) && !"Inactivo".equals(this.estado)) {
            this.estado = "Inactivo";
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
    
    // ====================== URL COMPLETA DE IMAGEN ======================
    
    public String getImagenUrlCompleta() {
        if (imagenUrlCompleta != null) {
            return imagenUrlCompleta;
        }
        
        // Generar si no existe
        if (imagenProducto != null && !imagenProducto.isEmpty()) {
            if (imagenProducto.startsWith("http")) {
                return imagenProducto;
            } else {
                return "http://localhost:8080/uploads/" + imagenProducto;
            }
        }
        
        return null;
    }
    
    public void setImagenUrlCompleta(String imagenUrlCompleta) {
        this.imagenUrlCompleta = imagenUrlCompleta;
    }
    
    // ====================== MÉTODOS UTILITARIOS ======================
    
    /**
     * Verifica si el producto está activo
     */
    public boolean estaActivo() {
        return Boolean.TRUE.equals(activo) && !"Inactivo".equals(estado);
    }
    
    /**
     * Verifica si el producto está disponible para venta
     */
    public boolean estaDisponible() {
        return estaActivo() && "Disponible".equals(estado) && stockProducto != null && stockProducto > 0;
    }
    
    /**
     * Verifica si el producto necesita reabastecimiento
     */
    public boolean necesitaReabastecimiento() {
        return estaActivo() && stockProducto != null && stockProducto <= 10 && stockProducto > 0;
    }
    
    /**
     * Verifica si el producto está agotado
     */
    public boolean estaAgotado() {
        return stockProducto != null && stockProducto <= 0 && estaActivo();
    }
    
    /**
     * Desactiva el producto (borrado lógico)
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
        this.estado = (stockProducto != null && stockProducto > 0) ? "Disponible" : "Agotado";
        this.fechaDesactivacion = null;
        this.motivoDesactivacion = null;
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    /**
     * Actualiza el stock y ajusta el estado automáticamente
     */
    public void actualizarStock(Integer nuevoStock) {
        this.stockProducto = nuevoStock;
        this.ultimaActualizacion = LocalDateTime.now();
        
        if (estaActivo()) {
            if (nuevoStock <= 0) {
                this.estado = "Agotado";
            } else if (nuevoStock <= 10 && "Disponible".equals(this.estado)) {
                this.estado = "Stock Bajo";
            } else if (nuevoStock > 10 && "Stock Bajo".equals(this.estado)) {
                this.estado = "Disponible";
            } else if (nuevoStock > 0 && "Agotado".equals(this.estado)) {
                this.estado = "Disponible";
            }
        }
    }
    
    /**
     * Obtiene el precio formateado con unidad
     */
    public String getPrecioFormateado() {
        if (precioProducto == null) return "$0.00";
        return String.format("$%.2f / %s", precioProducto, unidad != null ? unidad.toLowerCase() : "unidad");
    }
    
    /**
     * Obtiene el stock formateado con unidad
     */
    public String getStockFormateado() {
        if (stockProducto == null) return "0";
        return String.format("%d %s", stockProducto, unidad != null ? unidad.toLowerCase() : "unidades");
    }
    
    @Override
    public String toString() {
        return "ProductoResponse{" +
                "idProducto=" + idProducto +
                ", nombreProducto='" + nombreProducto + '\'' +
                ", precioProducto=" + precioProducto +
                ", stockProducto=" + stockProducto +
                ", unidad='" + unidad + '\'' +
                ", estado='" + estado + '\'' +
                ", activo=" + activo +
                ", vendedor='" + nombreEmpresa + '\'' +
                ", categoria='" + nombreCategoria + '\'' +
                '}';
    }
    
    // ====================== BUILDER PATTERN (OPCIONAL) ======================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private ProductoResponse productoResponse;
        
        public Builder() {
            productoResponse = new ProductoResponse();
        }
        
        public Builder idProducto(Integer idProducto) {
            productoResponse.setIdProducto(idProducto);
            return this;
        }
        
        public Builder nombreProducto(String nombreProducto) {
            productoResponse.setNombreProducto(nombreProducto);
            return this;
        }
        
        public Builder precioProducto(Double precioProducto) {
            productoResponse.setPrecioProducto(precioProducto);
            return this;
        }
        
        public Builder stockProducto(Integer stockProducto) {
            productoResponse.setStockProducto(stockProducto);
            return this;
        }
        
        public Builder unidad(String unidad) {
            productoResponse.setUnidad(unidad);
            return this;
        }
        
        public Builder imagenProducto(String imagenProducto) {
            productoResponse.setImagenProducto(imagenProducto);
            return this;
        }
        
        public Builder estado(String estado) {
            productoResponse.setEstado(estado);
            return this;
        }
        
        public Builder activo(Boolean activo) {
            productoResponse.setActivo(activo);
            return this;
        }
        
        public Builder nombreEmpresa(String nombreEmpresa) {
            productoResponse.setNombreEmpresa(nombreEmpresa);
            return this;
        }
        
        public Builder nombreSubcategoria(String nombreSubcategoria) {
            productoResponse.setNombreSubcategoria(nombreSubcategoria);
            return this;
        }
        
        public Builder nombreCategoria(String nombreCategoria) {
            productoResponse.setNombreCategoria(nombreCategoria);
            return this;
        }
        
        public ProductoResponse build() {
            return productoResponse;
        }
    }
}