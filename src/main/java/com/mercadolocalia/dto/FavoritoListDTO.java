package com.mercadolocalia.dto;

public class FavoritoListDTO {

    private Integer idFavorito;
    private Integer idProducto;
    private String nombreProducto;
    private Double precioProducto;
    private String imagenProducto;

    public FavoritoListDTO(Integer idFavorito, Integer idProducto, String nombreProducto,
                           Double precioProducto, String imagenProducto) {
        this.idFavorito = idFavorito;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.precioProducto = precioProducto;
        this.imagenProducto = imagenProducto;
    }

    public Integer getIdFavorito() {
        return idFavorito;
    }

    public void setIdFavorito(Integer idFavorito) {
        this.idFavorito = idFavorito;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public Double getPrecioProducto() {
        return precioProducto;
    }

    public void setPrecioProducto(Double precioProducto) {
        this.precioProducto = precioProducto;
    }

    public String getImagenProducto() {
        return imagenProducto;
    }

    public void setImagenProducto(String imagenProducto) {
        this.imagenProducto = imagenProducto;
    }
}
