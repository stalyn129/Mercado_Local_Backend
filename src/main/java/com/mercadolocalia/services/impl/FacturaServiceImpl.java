package com.mercadolocalia.services.impl;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.FacturaRequest;
import com.mercadolocalia.dto.FacturaEstadoRequest;
import com.mercadolocalia.entities.Factura;
import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.repositories.FacturaRepository;
import com.mercadolocalia.repositories.PedidoRepository;
import com.mercadolocalia.services.FacturaService;

@Service
public class FacturaServiceImpl implements FacturaService {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    // ===========================================
    // GENERAR NÚMERO DE FACTURA
    // ===========================================
    private String generarNumeroFactura() {
        Random random = new Random();
        return "FAC-" + (100000 + random.nextInt(900000));
    }

    @Override
    public Factura crearFactura(FacturaRequest request) {

        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        Factura factura = new Factura();
        factura.setPedido(pedido);

        // Generar número único
        String numero;
        do {
            numero = generarNumeroFactura();
        } while (facturaRepository.existsByNumeroFactura(numero));

        factura.setNumeroFactura(numero);
        factura.setRucEmisor(request.getRucEmisor());
        factura.setRazonSocial(request.getRazonSocial());

        factura.setFechaEmision(LocalDateTime.now());
        factura.setSubtotal(pedido.getSubtotal());
        factura.setIva(pedido.getIva());
        factura.setTotal(pedido.getTotal());
        factura.setEstado("Emitida");

        return facturaRepository.save(factura);
    }

    @Override
    public Factura obtenerPorId(Integer idFactura) {
        return facturaRepository.findById(idFactura)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
    }

    @Override
    public Factura obtenerPorPedido(Integer idPedido) {

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        return facturaRepository.findByPedido(pedido);
    }

    @Override
    public Factura actualizarEstado(Integer idFactura, FacturaEstadoRequest request) {

        Factura factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        factura.setEstado(request.getEstado());

        return facturaRepository.save(factura);
    }
}
