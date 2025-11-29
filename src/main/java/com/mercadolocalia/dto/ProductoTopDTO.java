package com.mercadolocalia.dto;

public class ProductoTopDTO {

    private Integer idProducto;
    private String nombreProducto;
    private Double precioProducto;
    private String imagenProducto;
    private String nombreVendedor;

    // ======== CONSTRUCTOR COMPLETO (🔥 ESTE ES EL QUE PEDÍA EL CONTROLADOR) ========
    public ProductoTopDTO(Integer idProducto, String nombreProducto, Double precioProducto,
                          String imagenProducto, String nombreVendedor) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.precioProducto = precioProducto;
        this.imagenProducto = imagenProducto;
        this.nombreVendedor = nombreVendedor;
    }

    // ======= GETTERS & SETTERS =======

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public Double getPrecioProducto() { return precioProducto; }
    public void setPrecioProducto(Double precioProducto) { this.precioProducto = precioProducto; }

    public String getImagenProducto() { return imagenProducto; }
    public void setImagenProducto(String imagenProducto) { this.imagenProducto = imagenProducto; }

    public String getNombreVendedor() { return nombreVendedor; }
    public void setNombreVendedor(String nombreVendedor) { this.nombreVendedor = nombreVendedor; }
}
