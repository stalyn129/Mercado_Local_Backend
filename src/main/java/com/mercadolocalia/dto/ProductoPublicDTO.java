package com.mercadolocalia.dto;

public class ProductoPublicDTO {

    private Integer idProducto;
    private String nombreProducto;
    private Double precioProducto;
    private String imagenProducto;
    private String nombreSubcategoria;
    private Double promedioValoracion;
    private Long totalValoraciones;
    private Integer idVendedor;

    public ProductoPublicDTO(
            Integer idProducto,
            String nombreProducto,
            Double precioProducto,
            String imagenProducto,
            String nombreSubcategoria,
            Double promedioValoracion,
            Long totalValoraciones,
            Integer idVendedor
    ) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.precioProducto = precioProducto;
        this.imagenProducto = imagenProducto;
        this.nombreSubcategoria = nombreSubcategoria;
        this.promedioValoracion = promedioValoracion;
        this.totalValoraciones = totalValoraciones;
        this.idVendedor = idVendedor;
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

	public String getNombreSubcategoria() {
		return nombreSubcategoria;
	}

	public void setNombreSubcategoria(String nombreSubcategoria) {
		this.nombreSubcategoria = nombreSubcategoria;
	}

	public Double getPromedioValoracion() {
		return promedioValoracion;
	}

	public void setPromedioValoracion(Double promedioValoracion) {
		this.promedioValoracion = promedioValoracion;
	}

	public Long getTotalValoraciones() {
		return totalValoraciones;
	}

	public void setTotalValoraciones(Long totalValoraciones) {
		this.totalValoraciones = totalValoraciones;
	}

	public Integer getIdVendedor() {
		return idVendedor;
	}

	public void setIdVendedor(Integer idVendedor) {
		this.idVendedor = idVendedor;
	}

    
}
