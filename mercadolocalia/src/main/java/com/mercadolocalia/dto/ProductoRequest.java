package com.mercadolocalia.dto;

public class ProductoRequest {

    private Integer idVendedor;  // opcional
    private Integer idUsuario;   // también opcional

    private Integer idSubcategoria;
    private String nombreProducto;
    private String descripcionProducto;
    private Double precioProducto;
    private Integer stockProducto;
    private String imagenProducto;

    public Integer getIdVendedor() { return idVendedor; }
    public void setIdVendedor(Integer idVendedor) { this.idVendedor = idVendedor; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdSubcategoria() { return idSubcategoria; }
    public void setIdSubcategoria(Integer idSubcategoria) { this.idSubcategoria = idSubcategoria; }

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
}
