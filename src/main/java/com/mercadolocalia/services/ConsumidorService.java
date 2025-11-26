package com.mercadolocalia.services;

import com.mercadolocalia.dto.ConsumidorRequest;
import com.mercadolocalia.entities.Consumidor;

public interface ConsumidorService {

    Consumidor registrarConsumidor(ConsumidorRequest request);

    Consumidor obtenerConsumidorPorUsuario(Integer idUsuario);

    Consumidor obtenerConsumidorPorId(Integer idConsumidor);
}
