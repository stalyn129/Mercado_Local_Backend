package com.mercadolocalia.dto;

public class AuthResponse {

	private String token;
	private String mensaje;
	private Integer idUsuario;
	private String rol;
	private Integer idConsumidor;
	private Integer idVendedor;

	// ===== GETTERS & SETTERS =====

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public Integer getIdConsumidor() {
		return idConsumidor;
	}

	public void setIdConsumidor(Integer idConsumidor) {
		this.idConsumidor = idConsumidor;
	}

	public Integer getIdVendedor() {
		return idVendedor;
	}

	public void setIdVendedor(Integer idVendedor) {
		this.idVendedor = idVendedor;
	}
}
