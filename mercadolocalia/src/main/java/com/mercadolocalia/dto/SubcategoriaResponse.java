package com.mercadolocalia.dto;

public class SubcategoriaResponse {

    private Integer idSubcategoria;
    private String nombreSubcategoria;
    private String descripcionSubcategoria;
    private Integer idCategoria;
    private String nombreCategoria;

    public Integer getIdSubcategoria() {
        return idSubcategoria;
    }

    public void setIdSubcategoria(Integer idSubcategoria) {
        this.idSubcategoria = idSubcategoria;
    }

    public String getNombreSubcategoria() {
        return nombreSubcategoria;
    }

    public void setNombreSubcategoria(String nombreSubcategoria) {
        this.nombreSubcategoria = nombreSubcategoria;
    }

    public String getDescripcionSubcategoria() {
        return descripcionSubcategoria;
    }

    public void setDescripcionSubcategoria(String descripcionSubcategoria) {
        this.descripcionSubcategoria = descripcionSubcategoria;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }
}
