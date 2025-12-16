package com.mercadolocalia.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UsuarioPerfilDTO(

    // ===== USUARIO =====
    Integer idUsuario,
    String nombre,
    String apellido,
    String correo,
    LocalDate fechaNacimiento,
    String rol,
    Boolean esAdministrador,
    LocalDateTime fechaRegistro,
    String estado,

    // ===== CONSUMIDOR =====
    Integer idConsumidor,
    String direccionConsumidor,
    String telefonoConsumidor,
    String cedulaConsumidor,

    // ===== VENDEDOR =====
    Integer idVendedor,
    String nombreEmpresa,
    String direccionEmpresa,
    String telefonoEmpresa,
    String rucEmpresa,
    Double calificacionPromedio

) {}
