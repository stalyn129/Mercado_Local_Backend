package com.mercadolocalia.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.PagoRequest;
import com.mercadolocalia.dto.PagoTarjetaRequest;
import com.mercadolocalia.entities.EstadoPago;
import com.mercadolocalia.entities.EstadoPedido;
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
		EstadoPedido estadoPedido;

		switch (request.getMetodoPago()) {

		case EFECTIVO:
			estadoPago = EstadoPago.PENDIENTE;
			estadoPedido = EstadoPedido.PROCESANDO;
			break;

		case TRANSFERENCIA:
			estadoPago = EstadoPago.EN_VERIFICACION;
			estadoPedido = EstadoPedido.PENDIENTE;
			break;

		default:
			throw new IllegalArgumentException("Este método requiere otro endpoint");
		}

		Pago pago = crearPagoBase(request.getIdPedido(), request.getIdConsumidor(), request.getMonto(),
				request.getMetodoPago(), estadoPago);

		actualizarEstadoPedido(request.getIdPedido(), estadoPedido);

		return pago;
	}

//=========================================================
//PAGO CON TARJETA (SIMULADO)
//=========================================================
	public Pago procesarPagoTarjetaSimulado(PagoTarjetaRequest request) {

		validarDatosTarjeta(request);

		String tarjeta = request.getNumeroTarjeta().replace(" ", "");

		switch (tarjeta) {

		// ✅ TARJETA APROBADA
		case "4111111111111111":

			Pago pagoAprobado = crearPagoBase(request.getIdPedido(), request.getIdConsumidor(), request.getMonto(),
					MetodoPago.TARJETA, EstadoPago.PAGADO);

			// 🔥 PEDIDO COMPLETADO
			actualizarEstadoPedido(request.getIdPedido(), EstadoPedido.COMPLETADO);

			return pagoAprobado;

		// ❌ ERRORES SIMULADOS
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

	private Pago crearPagoBase(Integer idPedido, Integer idConsumidor, Double monto, MetodoPago metodo,
			EstadoPago estado) {
		Pago pago = new Pago();
		pago.setIdPedido(idPedido);
		pago.setIdConsumidor(idConsumidor);
		pago.setMonto(monto);
		pago.setMetodo(metodo);
		pago.setEstado(estado);

		return pagoRepository.save(pago);
	}

	private void actualizarEstadoPedido(Integer idPedido, EstadoPedido nuevoEstado) {
		pedidoRepository.findById(idPedido).ifPresent(pedido -> {
			pedido.setEstadoPedido(nuevoEstado);
			pedidoRepository.save(pedido);
		});
	}

	private void validarDatosTarjeta(PagoTarjetaRequest request) {

		if (request.getNumeroTarjeta() == null || request.getNumeroTarjeta().replace(" ", "").length() < 13) {
			throw new RuntimeException("Número de tarjeta inválido");
		}

		if (request.getCvv() == null || request.getCvv().length() < 3) {
			throw new RuntimeException("CVV inválido");
		}

		if (request.getFechaExpiracion() == null || request.getFechaExpiracion().isEmpty()) {
			throw new RuntimeException("Fecha de expiración inválida");
		}

		if (request.getTitular() == null || request.getTitular().isEmpty()) {
			throw new RuntimeException("Titular inválido");
		}
	}
}
