package com.mercadolocalia.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre_usuario", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido_usuario", nullable = false, length = 100)
    private String apellido;

    @Column(name = "correo_electronico", nullable = false, unique = true, length = 100)
    private String correo;

    @Column(name = "contrasena_usuario", nullable = false, length = 255)
    private String contrasena;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "es_administrador")
    private Boolean esAdministrador;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "estado")
    private String estado;

    // ======================= GETTERS & SETTERS =======================

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Boolean getEsAdministrador() {
        return esAdministrador;
    }

    public void setEsAdministrador(Boolean esAdministrador) {
        this.esAdministrador = esAdministrador;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
