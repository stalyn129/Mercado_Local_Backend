package com.mercadolocalia.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "consumidores")
public class Consumidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consumidor")
    private Integer idConsumidor;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "cedula_consumidor", nullable = false, unique = true, length = 10)
    private String cedulaConsumidor;

    @Column(name = "direccion_consumidor", nullable = false)
    private String direccionConsumidor;

    @Column(name = "telefono_consumidor", nullable = false, length = 10)
    private String telefonoConsumidor;

    // ============================
    // GETTERS & SETTERS
    // ============================

    public Integer getIdConsumidor() {
        return idConsumidor;
    }

    public void setIdConsumidor(Integer idConsumidor) {
        this.idConsumidor = idConsumidor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getCedulaConsumidor() {
        return cedulaConsumidor;
    }

    public void setCedulaConsumidor(String cedulaConsumidor) {
        this.cedulaConsumidor = cedulaConsumidor;
    }

    public String getDireccionConsumidor() {
        return direccionConsumidor;
    }

    public void setDireccionConsumidor(String direccionConsumidor) {
        this.direccionConsumidor = direccionConsumidor;
    }

    public String getTelefonoConsumidor() {
        return telefonoConsumidor;
    }

    public void setTelefonoConsumidor(String telefonoConsumidor) {
        this.telefonoConsumidor = telefonoConsumidor;
    }
}
