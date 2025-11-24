package com.mercadolocalia.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.mercadolocalia.dto.PrecioSugeridoRequest;
import com.mercadolocalia.dto.PrecioSugeridoResponse;
import com.mercadolocalia.dto.DemandaRequest;
import com.mercadolocalia.dto.DemandaResponse;
import com.mercadolocalia.services.IaService;

@Service
public class IaServiceImpl implements IaService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.ia.base-url}")
    private String iaBaseUrl;

    @Override
    public PrecioSugeridoResponse obtenerPrecioSugerido(PrecioSugeridoRequest request) {

        String url = iaBaseUrl + "/precio-sugerido"; // endpoint en FastAPI

        try {
            PrecioSugeridoResponse response = restTemplate.postForObject(
                    url,
                    request,
                    PrecioSugeridoResponse.class
            );
            return response;
        } catch (RestClientException e) {
            // Aquí podrías loguear el error o lanzar una excepción personalizada
            throw new RuntimeException("Error al comunicarse con el servicio de IA (precio sugerido)", e);
        }
    }

    @Override
    public DemandaResponse predecirDemanda(DemandaRequest request) {

        String url = iaBaseUrl + "/prediccion-demanda"; // endpoint en FastAPI

        try {
            DemandaResponse response = restTemplate.postForObject(
                    url,
                    request,
                    DemandaResponse.class
            );
            return response;
        } catch (RestClientException e) {
            throw new RuntimeException("Error al comunicarse con el servicio de IA (predicción demanda)", e);
        }
    }
}
