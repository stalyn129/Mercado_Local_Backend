package com.mercadolocalia.dto;

public class AuthResponse {
    private String token;
    private String mensaje;
    private Integer idUsuario;
    private String rol;
    private Integer idConsumidor;
    private Integer idVendedor;
    private String nombre;
    private String apellido;
    private String correo;
    private Boolean googleAuth;
    private Boolean emailVerificado;
    
    // Getters y Setters para todos los campos
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    
    public Integer getIdConsumidor() { return idConsumidor; }
    public void setIdConsumidor(Integer idConsumidor) { this.idConsumidor = idConsumidor; }
    
    public Integer getIdVendedor() { return idVendedor; }
    public void setIdVendedor(Integer idVendedor) { this.idVendedor = idVendedor; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    
    public Boolean getGoogleAuth() { return googleAuth; }
    public void setGoogleAuth(Boolean googleAuth) { this.googleAuth = googleAuth; }
    
    public Boolean getEmailVerificado() { return emailVerificado; }
    public void setEmailVerificado(Boolean emailVerificado) { this.emailVerificado = emailVerificado; }
}