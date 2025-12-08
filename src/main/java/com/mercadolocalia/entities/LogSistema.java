package com.mercadolocalia.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "logs_sistema")
public class LogSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String accion;

    private String usuario;

    private LocalDateTime fecha = LocalDateTime.now();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAccion() {
		return accion;
	}

	public void setAccion(String accion) {
		this.accion = accion;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

    
}
