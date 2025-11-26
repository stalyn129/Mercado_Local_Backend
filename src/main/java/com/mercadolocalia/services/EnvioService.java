package com.mercadolocalia.services;

import com.mercadolocalia.dto.EnvioRequest;
import com.mercadolocalia.dto.EnvioUpdateEstadoRequest;
import com.mercadolocalia.entities.Envio;

public interface EnvioService {

    Envio crearEnvio(EnvioRequest request);

    Envio actualizarEstado(Integer idEnvio, EnvioUpdateEstadoRequest request);

    Envio obtenerPorPedido(Integer idPedido);

    Envio obtenerPorId(Integer idEnvio);
}
