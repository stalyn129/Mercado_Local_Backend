package com.mercadolocalia.dto;

import java.time.LocalDateTime;
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
    public void setImagenProducto(String imagenProducto) { this.imagenProducto = imagenProducto; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

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
}
