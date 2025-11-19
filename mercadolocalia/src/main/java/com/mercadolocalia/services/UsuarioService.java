package com.mercadolocalia.services;

import com.mercadolocalia.entities.Usuario;

public interface UsuarioService {
    Usuario obtenerPerfil(String correo);
}
