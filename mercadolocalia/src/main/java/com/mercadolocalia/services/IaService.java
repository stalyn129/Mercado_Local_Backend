package com.mercadolocalia.services;

import com.mercadolocalia.dto.PrecioSugeridoRequest;
import com.mercadolocalia.dto.PrecioSugeridoResponse;
import com.mercadolocalia.dto.DemandaRequest;
import com.mercadolocalia.dto.DemandaResponse;

public interface IaService {

    PrecioSugeridoResponse obtenerPrecioSugerido(PrecioSugeridoRequest request);

    DemandaResponse predecirDemanda(DemandaRequest request);
}
