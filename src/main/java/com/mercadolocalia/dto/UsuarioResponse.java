package com.mercadolocalia.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UsuarioResponse(
        Integer id,
        String nombre,
        String apellido,
        String correo,
        String contrasena,
        LocalDate fechaNacimiento,
        String rol,
        Boolean esAdministrador,
        LocalDateTime fechaRegistro,
        String estado
) {}
