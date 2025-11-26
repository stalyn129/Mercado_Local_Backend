package com.mercadolocalia.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "vendedores")
public class Vendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vendedor")
    private Integer idVendedor;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "nombre_empresa", nullable = false, length = 100)
    private String nombreEmpresa;

    @Column(name = "ruc_empresa", nullable = false, unique = true, length = 13)
    private String rucEmpresa;

    @Column(name = "direccion_empresa", nullable = false)
    private String direccionEmpresa;

    @Column(name = "telefono_empresa", nullable = false, length = 10)
    private String telefonoEmpresa;

    @Column(name = "calificacion_promedio", nullable = true)
    private Double calificacionPromedio;

    // ============================
    // GETTERS & SETTERS
    // ============================

    public Integer getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(Integer idVendedor) {
        this.idVendedor = idVendedor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getRucEmpresa() {
        return rucEmpresa;
    }

    public void setRucEmpresa(String rucEmpresa) {
        this.rucEmpresa = rucEmpresa;
    }

    public String getDireccionEmpresa() {
        return direccionEmpresa;
    }

    public void setDireccionEmpresa(String direccionEmpresa) {
        this.direccionEmpresa = direccionEmpresa;
    }

    public String getTelefonoEmpresa() {
        return telefonoEmpresa;
    }

    public void setTelefonoEmpresa(String telefonoEmpresa) {
        this.telefonoEmpresa = telefonoEmpresa;
    }

    public Double getCalificacionPromedio() {
        return calificacionPromedio;
    }

    public void setCalificacionPromedio(Double calificacionPromedio) {
        this.calificacionPromedio = calificacionPromedio;
    }
}
