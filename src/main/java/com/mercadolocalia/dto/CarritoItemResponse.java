package com.mercadolocalia.dto;

public class CarritoItemResponse {

	private Integer idItem;
    private Integer cantidad;
    private ProductoSimpleResponse producto;
    
	public Integer getIdItem() {
		return idItem;
	}
	public void setIdItem(Integer idItem) {
		this.idItem = idItem;
	}
	public Integer getCantidad() {
		return cantidad;
	}
	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}
	public ProductoSimpleResponse getProducto() {
		return producto;
	}
	public void setProducto(ProductoSimpleResponse producto) {
		this.producto = producto;
	}
    
    
}
