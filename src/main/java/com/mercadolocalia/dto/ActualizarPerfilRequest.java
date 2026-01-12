package com.mercadolocalia.dto;

import java.time.LocalDate;

public class ActualizarPerfilRequest {
    
    // Datos editables del Usuario (comunes a todos los roles)
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    
    // Datos editables del Consumidor (solo si rol = CONSUMIDOR)
    private String direccionConsumidor;
    private String telefonoConsumidor;
    
    // Datos editables del Vendedor (solo si rol = VENDEDOR)
    private String direccionEmpresa;
    private String telefonoEmpresa;

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { 
        this.fechaNacimiento = fechaNacimiento; 
    }
    
    public String getDireccionConsumidor() { return direccionConsumidor; }
    public void setDireccionConsumidor(String direccionConsumidor) { 
        this.direccionConsumidor = direccionConsumidor; 
    }
    
    public String getTelefonoConsumidor() { return telefonoConsumidor; }
    public void setTelefonoConsumidor(String telefonoConsumidor) { 
        this.telefonoConsumidor = telefonoConsumidor; 
    }
    
    public String getDireccionEmpresa() { return direccionEmpresa; }
    public void setDireccionEmpresa(String direccionEmpresa) { 
        this.direccionEmpresa = direccionEmpresa; 
    }
    
    public String getTelefonoEmpresa() { return telefonoEmpresa; }
    public void setTelefonoEmpresa(String telefonoEmpresa) { 
        this.telefonoEmpresa = telefonoEmpresa; 
    }
}