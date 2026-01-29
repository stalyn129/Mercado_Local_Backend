package com.mercadolocalia.dto;

public class RegisterRequest {

    // ======================================
    // DATOS BÁSICOS DEL USUARIO
    // ======================================
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasena;
    private String fechaNacimiento; // Recibe "yyyy-MM-dd"

    // Rol del usuario (2 = vendedor, 3 = consumidor)
    private Integer idRol;

    // ======================================
    // CAMPOS NUEVOS PARA GOOGLE OAUTH ✅
    // ======================================
    private Boolean googleAuth = false;      // Indica si es registro con Google
    private Boolean emailVerified = false;   // Indica si el email está verificado

    // ======================================
    // DATOS DEL CONSUMIDOR
    // ======================================
    private String cedula;
    private String direccion;
    private String telefono;

    // ======================================
    // DATOS DEL VENDEDOR
    // ======================================
    private String nombreEmpresa;
    private String ruc;
    private String direccionEmpresa;
    private String telefonoEmpresa;
    private String descripcion;

    // ======================================
    // CONSTRUCTORES
    // ======================================
    
    public RegisterRequest() {
        // Valores por defecto para Google OAuth
        this.googleAuth = false;
        this.emailVerified = false;
    }
    
    // ======================================
    // GETTERS Y SETTERS
    // ======================================

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

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    // ====== Campos Google OAuth ====== ✅

    public Boolean getGoogleAuth() {
        return googleAuth;
    }

    public void setGoogleAuth(Boolean googleAuth) {
        this.googleAuth = googleAuth;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    // ====== Datos Consumidor ======

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
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

    // ====== Datos Vendedor ======

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    // ======================================
    // MÉTODO TOSTRING PARA DEBUG
    // ======================================
    @Override
    public String toString() {
        return "RegisterRequest{" +
                "nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", correo='" + correo + '\'' +
                ", contrasena='" + (contrasena != null ? "[PROTEGIDA]" : "null") + '\'' +
                ", fechaNacimiento='" + fechaNacimiento + '\'' +
                ", idRol=" + idRol +
                ", googleAuth=" + googleAuth +
                ", emailVerified=" + emailVerified +
                ", cedula='" + cedula + '\'' +
                ", direccion='" + direccion + '\'' +
                ", telefono='" + telefono + '\'' +
                ", nombreEmpresa='" + nombreEmpresa + '\'' +
                ", ruc='" + ruc + '\'' +
                ", direccionEmpresa='" + direccionEmpresa + '\'' +
                ", telefonoEmpresa='" + telefonoEmpresa + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}