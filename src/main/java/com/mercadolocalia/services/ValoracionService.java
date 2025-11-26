package com.mercadolocalia.services;

import java.util.List;

import com.mercadolocalia.dto.ValoracionRequest;
import com.mercadolocalia.dto.ValoracionResponse;

public interface ValoracionService {

    ValoracionResponse crearValoracion(ValoracionRequest request);

    List<ValoracionResponse> listarPorProducto(Integer idProducto);

    List<ValoracionResponse> listarPorConsumidor(Integer idConsumidor);
}
