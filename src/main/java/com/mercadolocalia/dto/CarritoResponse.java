package com.mercadolocalia.dto;

import java.util.List;

public class CarritoResponse {

	private Integer idCarrito;
    private List<CarritoItemResponse> items;
    private String mensaje;
    
    
	public Integer getIdCarrito() {
		return idCarrito;
	}
	public void setIdCarrito(Integer idCarrito) {
		this.idCarrito = idCarrito;
	}
	public List<CarritoItemResponse> getItems() {
		return items;
	}
	public void setItems(List<CarritoItemResponse> items) {
		this.items = items;
	}
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
    
    
}
