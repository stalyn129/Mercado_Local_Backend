package com.mercadolocalia.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProductoDetalleResponse {

    // ========= DATOS PRINCIPALES =========
    private Integer idProducto;
    private String nombreProducto;
    private String descripcionProducto;
    private Double precioProducto;
    private Integer stockProducto;
    private String imagenProducto;
    
    // NUEVO ⚠ Para mostrar en frontend → $precio / unidad
    private String unidad;  // kg | unidad | litro | libra | docena
    
    private LocalDateTime fechaPublicacion;
    private String estado;

    // ========= CAMPOS DE BORRADO LÓGICO (AGREGADOS) =========
    private Boolean activo = true;
    private LocalDateTime fechaDesactivacion;
    private String motivoDesactivacion;
    private LocalDateTime ultimaActualizacion;

    // ========= CATEGORÍA / SUBCATEGORÍA =========
    private Integer idCategoria;
    private String nombreCategoria;
    private Integer idSubcategoria;
    private String nombreSubcategoria;

    // ========= VENDEDOR =========
    private Integer idVendedor;
    private String nombreEmpresa;
    private String nombreVendedor; // nombre + apellido

    // ========= VALORACIONES =========
    private List<ValoracionResponse> valoraciones;
    private Double promedioValoracion;
    private Integer totalValoraciones;
    
    // ========= URL COMPLETA DE IMAGEN =========
    private String imagenUrlCompleta;

    // ========= GETTERS & SETTERS =========

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
        // Actualizar URL completa cuando se cambia la imagen
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

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // ========= GETTERS & SETTERS PARA BORRADO LÓGICO =========
    
    public Boolean getActivo() { 
        return activo != null ? activo : true; // Por defecto true
    }
    
    public void setActivo(Boolean activo) {  // ✅ CORREGIDO: setActivo
        this.activo = activo;
    }
    
    public LocalDateTime getFechaDesactivacion() { return fechaDesactivacion; }
    
    public void setFechaDesactivacion(LocalDateTime fechaDesactivacion) { 
        this.fechaDesactivacion = fechaDesactivacion; 
    }
    
    public String getMotivoDesactivacion() { return motivoDesactivacion; }
    
    public void setMotivoDesactivacion(String motivoDesactivacion) { 
        this.motivoDesactivacion = motivoDesactivacion; 
    }
    
    public LocalDateTime getUltimaActualizacion() { return ultimaActualizacion; }
    
    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) { 
        this.ultimaActualizacion = ultimaActualizacion; 
    }

    // ========= GETTERS & SETTERS PARA CATEGORÍAS Y VENDEDOR =========
    
    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }

    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }

    public Integer getIdSubcategoria() { return idSubcategoria; }
    public void setIdSubcategoria(Integer idSubcategoria) { this.idSubcategoria = idSubcategoria; }

    public String getNombreSubcategoria() { return nombreSubcategoria; }
    public void setNombreSubcategoria(String nombreSubcategoria) { this.nombreSubcategoria = nombreSubcategoria; }

    public Integer getIdVendedor() { return idVendedor; }
    public void setIdVendedor(Integer idVendedor) { this.idVendedor = idVendedor; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public String getNombreVendedor() { return nombreVendedor; }
    public void setNombreVendedor(String nombreVendedor) { this.nombreVendedor = nombreVendedor; }

    public List<ValoracionResponse> getValoraciones() { return valoraciones; }
    public void setValoraciones(List<ValoracionResponse> valoraciones) { this.valoraciones = valoraciones; }

    public Double getPromedioValoracion() { return promedioValoracion; }
    public void setPromedioValoracion(Double promedioValoracion) { this.promedioValoracion = promedioValoracion; }

    public Integer getTotalValoraciones() { return totalValoraciones; }
    public void setTotalValoraciones(Integer totalValoraciones) { this.totalValoraciones = totalValoraciones; }
    
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
    
    // ========= MÉTODOS UTILITARIOS =========
    
    /**
     * Verifica si el producto está disponible (tiene stock)
     */
    public boolean estaDisponible() {
        return "Disponible".equals(estado) && stockProducto != null && stockProducto > 0;
    }
    
    /**
     * Verifica si el producto está agotado
     */
    public boolean estaAgotado() {
        return "Agotado".equals(estado) || (stockProducto != null && stockProducto <= 0);
    }
    
    /**
     * Verifica si el producto está activo
     */
    public boolean estaActivo() {
        return Boolean.TRUE.equals(activo);
    }
    
    /**
     * Verifica si el producto está inactivo
     */
    public boolean estaInactivo() {
        return Boolean.FALSE.equals(activo);
    }
    
    /**
     * Obtiene el precio formateado con unidad
     */
    public String getPrecioFormateado() {
        if (precioProducto == null) return "$0.00";
        String unidadTexto = unidad != null ? unidad.toLowerCase() : "unidad";
        return String.format("$%.2f / %s", precioProducto, unidadTexto);
    }
    
    /**
     * Obtiene el stock formateado con unidad
     */
    public String getStockFormateado() {
        if (stockProducto == null) return "0 unidades";
        String unidadTexto = unidad != null ? unidad.toLowerCase() : "unidades";
        return String.format("%d %s", stockProducto, unidadTexto);
    }
    
    /**
     * Obtiene la fecha formateada
     */
    public String getFechaPublicacionFormateada() {
        if (fechaPublicacion == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return fechaPublicacion.format(formatter);
    }
    
    /**
     * Obtiene la fecha de desactivación formateada
     */
    public String getFechaDesactivacionFormateada() {
        if (fechaDesactivacion == null) return "No desactivado";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return fechaDesactivacion.format(formatter);
    }
    
    /**
     * Obtiene la fecha relativa (ej: "hace 2 días")
     */
    public String getFechaPublicacionRelativa() {
        if (fechaPublicacion == null) return "";
        
        LocalDateTime ahora = LocalDateTime.now();
        java.time.Duration duracion = java.time.Duration.between(fechaPublicacion, ahora);
        
        long dias = duracion.toDays();
        long horas = duracion.toHours();
        long minutos = duracion.toMinutes();
        
        if (dias > 0) {
            return "Hace " + dias + (dias == 1 ? " día" : " días");
        } else if (horas > 0) {
            return "Hace " + horas + (horas == 1 ? " hora" : " horas");
        } else if (minutos > 0) {
            return "Hace " + minutos + (minutos == 1 ? " minuto" : " minutos");
        } else {
            return "Recién publicado";
        }
    }
    
    /**
     * Obtiene las estrellas redondeadas para mostrar
     */
    public Integer getEstrellasRedondeadas() {
        if (promedioValoracion == null) return 0;
        return (int) Math.round(promedioValoracion);
    }
    
    /**
     * Verifica si tiene valoraciones
     */
    public boolean tieneValoraciones() {
        return valoraciones != null && !valoraciones.isEmpty();
    }
    
    @Override
    public String toString() {
        return "ProductoDetalleResponse{" +
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
    
    // ========= BUILDER PATTERN (OPCIONAL) =========
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private ProductoDetalleResponse productoDetalleResponse;
        
        public Builder() {
            productoDetalleResponse = new ProductoDetalleResponse();
        }
        
        public Builder idProducto(Integer idProducto) {
            productoDetalleResponse.setIdProducto(idProducto);
            return this;
        }
        
        public Builder nombreProducto(String nombreProducto) {
            productoDetalleResponse.setNombreProducto(nombreProducto);
            return this;
        }
        
        public Builder descripcionProducto(String descripcionProducto) {
            productoDetalleResponse.setDescripcionProducto(descripcionProducto);
            return this;
        }
        
        public Builder precioProducto(Double precioProducto) {
            productoDetalleResponse.setPrecioProducto(precioProducto);
            return this;
        }
        
        public Builder stockProducto(Integer stockProducto) {
            productoDetalleResponse.setStockProducto(stockProducto);
            return this;
        }
        
        public Builder unidad(String unidad) {
            productoDetalleResponse.setUnidad(unidad);
            return this;
        }
        
        public Builder imagenProducto(String imagenProducto) {
            productoDetalleResponse.setImagenProducto(imagenProducto);
            return this;
        }
        
        public Builder estado(String estado) {
            productoDetalleResponse.setEstado(estado);
            return this;
        }
        
        public Builder activo(Boolean activo) {
            productoDetalleResponse.setActivo(activo);
            return this;
        }
        
        public Builder fechaDesactivacion(LocalDateTime fechaDesactivacion) {
            productoDetalleResponse.setFechaDesactivacion(fechaDesactivacion);
            return this;
        }
        
        public Builder motivoDesactivacion(String motivoDesactivacion) {
            productoDetalleResponse.setMotivoDesactivacion(motivoDesactivacion);
            return this;
        }
        
        public Builder ultimaActualizacion(LocalDateTime ultimaActualizacion) {
            productoDetalleResponse.setUltimaActualizacion(ultimaActualizacion);
            return this;
        }
        
        public Builder nombreCategoria(String nombreCategoria) {
            productoDetalleResponse.setNombreCategoria(nombreCategoria);
            return this;
        }
        
        public Builder nombreSubcategoria(String nombreSubcategoria) {
            productoDetalleResponse.setNombreSubcategoria(nombreSubcategoria);
            return this;
        }
        
        public Builder nombreEmpresa(String nombreEmpresa) {
            productoDetalleResponse.setNombreEmpresa(nombreEmpresa);
            return this;
        }
        
        public Builder promedioValoracion(Double promedioValoracion) {
            productoDetalleResponse.setPromedioValoracion(promedioValoracion);
            return this;
        }
        
        public ProductoDetalleResponse build() {
            return productoDetalleResponse;
        }
    }
}