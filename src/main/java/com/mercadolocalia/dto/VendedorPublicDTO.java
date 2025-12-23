package com.mercadolocalia.dto;

import java.util.List;

public class VendedorPublicDTO {

    private Integer idVendedor;
    private String nombreEmpresa;

    private String nombreVendedor;
    private String apellidoVendedor;

    private String direccion;
    private String telefono;
    private Double calificacionPromedio;
    private List<ProductoPublicDTO> productos;

    public VendedorPublicDTO(
            Integer idVendedor,
            String nombreEmpresa,
            String nombreVendedor,
            String apellidoVendedor,
            String direccion,
            String telefono,
            Double calificacionPromedio,
            List<ProductoPublicDTO> productos
    ) {
        this.idVendedor = idVendedor;
        this.nombreEmpresa = nombreEmpresa;
        this.nombreVendedor = nombreVendedor;
        this.apellidoVendedor = apellidoVendedor;
        this.direccion = direccion;
        this.telefono = telefono;
        this.calificacionPromedio = calificacionPromedio;
        this.productos = productos;
    }

	public Integer getIdVendedor() {
		return idVendedor;
	}

	public void setIdVendedor(Integer idVendedor) {
		this.idVendedor = idVendedor;
	}

	public String getNombreEmpresa() {
		return nombreEmpresa;
	}

	public void setNombreEmpresa(String nombreEmpresa) {
		this.nombreEmpresa = nombreEmpresa;
	}

	public String getNombreVendedor() {
		return nombreVendedor;
	}

	public void setNombreVendedor(String nombreVendedor) {
		this.nombreVendedor = nombreVendedor;
	}

	public String getApellidoVendedor() {
		return apellidoVendedor;
	}

	public void setApellidoVendedor(String apellidoVendedor) {
		this.apellidoVendedor = apellidoVendedor;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public Double getCalificacionPromedio() {
		return calificacionPromedio;
	}

	public void setCalificacionPromedio(Double calificacionPromedio) {
		this.calificacionPromedio = calificacionPromedio;
	}

	public List<ProductoPublicDTO> getProductos() {
		return productos;
	}

	public void setProductos(List<ProductoPublicDTO> productos) {
		this.productos = productos;
	}

    
}
