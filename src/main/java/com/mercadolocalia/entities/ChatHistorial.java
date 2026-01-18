package com.mercadolocalia.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_historial")
public class ChatHistorial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campos de tipo simple
    private Integer idUsuario; 
    private String rol;        
    private String autor;      
    
    @Column(columnDefinition = "TEXT")
    private String texto;
    private LocalDateTime fecha;

    public ChatHistorial() {}

    // Este es el constructor que tu Service está buscando
    public ChatHistorial(Integer idUsuario, String rol, String autor, String texto) {
        this.idUsuario = idUsuario;
        this.rol = rol;
        this.autor = autor;
        this.texto = texto;
        this.fecha = LocalDateTime.now();
    }

    // GETTERS Y SETTERS (Asegúrate de que coincidan con los nombres arriba)
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	
}