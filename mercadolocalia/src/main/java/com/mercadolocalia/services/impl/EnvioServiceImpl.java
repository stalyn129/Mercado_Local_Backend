package com.mercadolocalia.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.EnvioRequest;
import com.mercadolocalia.dto.EnvioUpdateEstadoRequest;
import com.mercadolocalia.entities.Envio;
import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.repositories.EnvioRepository;
import com.mercadolocalia.repositories.PedidoRepository;
import com.mercadolocalia.services.EnvioService;

import java.time.LocalDateTime;

@Service
public class EnvioServiceImpl implements EnvioService {

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Override
    public Envio crearEnvio(EnvioRequest request) {

        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        Envio envio = new Envio();
        envio.setPedido(pedido);
        envio.setTipoEnvio(request.getTipoEnvio());
        envio.setDireccionEnvio(request.getDireccionEnvio());
        envio.setCiudad(request.getCiudad());
        envio.setProvincia(request.getProvincia());
        envio.setCostoEnvio(request.getCostoEnvio());
        envio.setFechaEnvio(LocalDateTime.now());
        envio.setEstadoEnvio("Pendiente");

        return envioRepository.save(envio);
    }

    @Override
    public Envio actualizarEstado(Integer idEnvio, EnvioUpdateEstadoRequest request) {

        Envio envio = envioRepository.findById(idEnvio)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado"));

        envio.setEstadoEnvio(request.getEstadoEnvio());

        if (request.getEstadoEnvio().equalsIgnoreCase("En tránsito")) {
            envio.setFechaEnvio(LocalDateTime.now());
        }

        if (request.getEstadoEnvio().equalsIgnoreCase("Entregado")) {
            envio.setFechaEntrega(LocalDateTime.now());
        }

        return envioRepository.save(envio);
    }

    @Override
    public Envio obtenerPorPedido(Integer idPedido) {

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        return envioRepository.findByPedido(pedido);
    }

    @Override
    public Envio obtenerPorId(Integer idEnvio) {
        return envioRepository.findById(idEnvio)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado"));
    }
}
