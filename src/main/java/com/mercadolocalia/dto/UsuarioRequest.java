package com.mercadolocalia.dto;

import java.time.LocalDate;

public record UsuarioRequest(
        String nombre,
        String apellido,
        String correo,
        String contrasena,   // opcional
        LocalDate fechaNacimiento,
        String rol,
        String estado
) {}
