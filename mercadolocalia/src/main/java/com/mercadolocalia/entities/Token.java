package com.mercadolocalia.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tokens")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Integer idToken;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 255)
    private String token;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    // ===== GETTERS & SETTERS =====

    public Integer getIdToken() {
        return idToken;
    }

    public void setIdToken(Integer idToken) {
        this.idToken = idToken;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }
}
