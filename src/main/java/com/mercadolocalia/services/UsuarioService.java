package com.mercadolocalia.services;

import java.util.List;

import com.mercadolocalia.dto.UsuarioResponse;
import com.mercadolocalia.dto.UsuarioPerfilDTO;
import com.mercadolocalia.dto.UsuarioRequest;
import com.mercadolocalia.entities.Usuario;

public interface UsuarioService {

	UsuarioPerfilDTO obtenerPerfilDTO(String correo);

    List<UsuarioResponse> listarUsuarios();

    String cambiarEstado(Integer id);

    UsuarioResponse actualizarUsuario(Integer id, UsuarioRequest request);

    // ⭐ NUEVO → ELIMINAR USUARIO
    void eliminarUsuario(Integer id);
}
