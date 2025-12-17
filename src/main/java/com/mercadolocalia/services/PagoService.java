package com.mercadolocalia.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.PagoRequest;
import com.mercadolocalia.dto.PagoTarjetaRequest;
import com.mercadolocalia.entities.EstadoPago;
import com.mercadolocalia.entities.MetodoPago;
import com.mercadolocalia.entities.Pago;
import com.mercadolocalia.repositories.PagoRepository;
import com.mercadolocalia.repositories.PedidoRepository;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    // =========================================================
    // PAGO GENERAL (EFECTIVO / TRANSFERENCIA)
    // =========================================================
    public Pago procesarPago(PagoRequest request) {

        EstadoPago estadoPago;

        switch (request.getMetodoPago()) {
            case EFECTIVO:
                estadoPago = EstadoPago.PENDIENTE;
                break;

            case TRANSFERENCIA:
                estadoPago = EstadoPago.PENDIENTE_VERIFICACION;
                break;

            default:
                throw new IllegalArgumentException(
                    "Este método requiere otro endpoint"
                );
        }

        Pago pago = crearPagoBase(
            request.getIdPedido(),
            request.getIdConsumidor(),
            request.getMonto(),
            request.getMetodoPago(),
            estadoPago
        );

        actualizarEstadoPedido(request.getIdPedido(), estadoPago);

        return pago;
    }

    // =========================================================
    // PAGO CON TARJETA (SIMULADO)
    // =========================================================
    public Pago procesarPagoTarjetaSimulado(PagoTarjetaRequest request) {

        validarDatosTarjeta(request);

        String tarjeta = request.getNumeroTarjeta().replace(" ", "");

        // 🔥 TARJETAS QUEMADAS (SIMULACIÓN)
        switch (tarjeta) {

            case "4111111111111111":
                // ✅ Tarjeta aprobada
                Pago pagoAprobado = crearPagoBase(
                    request.getIdPedido(),
                    request.getIdConsumidor(),
                    request.getMonto(),
                    MetodoPago.TARJETA,
                    EstadoPago.PAGADO
                );

                actualizarEstadoPedido(
                    request.getIdPedido(),
                    EstadoPago.PAGADO
                );

                return pagoAprobado;

            case "4000000000000002":
                throw new RuntimeException("Fondos insuficientes");

            case "4000000000009995":
                throw new RuntimeException("Tarjeta bloqueada");

            default:
                throw new RuntimeException("Tarjeta inválida o no reconocida");
        }
    }

    // =========================================================
    // MÉTODOS PRIVADOS DE APOYO
    // =========================================================

    private Pago crearPagoBase(
        Integer idPedido,
        Integer idConsumidor,
        Double monto,
        MetodoPago metodo,
        EstadoPago estado
    ) {
        Pago pago = new Pago();
        pago.setIdPedido(idPedido);
        pago.setIdConsumidor(idConsumidor);
        pago.setMonto(monto);
        pago.setMetodo(metodo);
        pago.setEstado(estado);

        return pagoRepository.save(pago);
    }

    private void actualizarEstadoPedido(
        Integer idPedido,
        EstadoPago estadoPago
    ) {
        pedidoRepository.findById(idPedido).ifPresent(pedido -> {

            switch (estadoPago) {
                case PAGADO:
                    pedido.setEstadoPedido("PAGADO");
                    break;

                case PENDIENTE:
                    pedido.setEstadoPedido("PENDIENTE_PAGO");
                    break;

                case PENDIENTE_VERIFICACION:
                    pedido.setEstadoPedido("PENDIENTE_VERIFICACION");
                    break;

                default:
                    pedido.setEstadoPedido("PENDIENTE_PAGO");
            }

            pedidoRepository.save(pedido);
        });
    }

    private void validarDatosTarjeta(PagoTarjetaRequest request) {

        if (request.getNumeroTarjeta() == null ||
            request.getNumeroTarjeta().replace(" ", "").length() < 13) {
            throw new RuntimeException("Número de tarjeta inválido");
        }

        if (request.getCvv() == null || request.getCvv().length() < 3) {
            throw new RuntimeException("CVV inválido");
        }

        if (request.getFechaExpiracion() == null ||
            request.getFechaExpiracion().isEmpty()) {
            throw new RuntimeException("Fecha de expiración inválida");
        }

        if (request.getTitular() == null ||
            request.getTitular().isEmpty()) {
            throw new RuntimeException("Titular inválido");
        }
    }
}
