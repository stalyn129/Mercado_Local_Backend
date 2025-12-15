package com.mercadolocalia.services.impl;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mercadolocalia.dto.DetallePedidoAddRequest;
import com.mercadolocalia.dto.PedidoCarritoRequest;
import com.mercadolocalia.dto.PedidoRequest;
import com.mercadolocalia.entities.Consumidor;
import com.mercadolocalia.entities.DetallePedido;
import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.repositories.ConsumidorRepository;
import com.mercadolocalia.repositories.DetallePedidoRepository;
import com.mercadolocalia.repositories.PedidoRepository;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.repositories.VendedorRepository;
import com.mercadolocalia.services.PedidoService;


@Service
public class PedidoServiceImpl implements PedidoService {

	@Autowired
	private PedidoRepository pedidoRepository;
	@Autowired
	private DetallePedidoRepository detallePedidoRepository;
	@Autowired
	private ConsumidorRepository consumidorRepository;
	@Autowired
	private VendedorRepository vendedorRepository;
	@Autowired
	private ProductoRepository productoRepository;


	// ============================================================
	// CREAR PEDIDO COMPLETO (CARRITO)
	// ============================================================
	@Override
	public Pedido crearPedido(PedidoRequest request) {

		Consumidor consumidor = consumidorRepository.findById(request.getIdConsumidor())
				.orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));

		Vendedor vendedor = vendedorRepository.findById(request.getIdVendedor())
				.orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

		Pedido pedido = new Pedido();
		pedido.setConsumidor(consumidor);
		pedido.setVendedor(vendedor);
		pedido.setFechaPedido(LocalDateTime.now());
		pedido.setEstadoPedido("Pendiente");
		pedido.setMetodoPago(request.getMetodoPago());
		pedido.setSubtotal(0.0);
		pedido.setIva(0.0);
		pedido.setTotal(0.0);

		pedidoRepository.save(pedido);

		double subtotal = 0;

		List<DetallePedido> detalles = new ArrayList<>();

		for (DetallePedidoAddRequest detRequest : request.getDetalles()) {

			Producto producto = productoRepository.findById(detRequest.getIdProducto())
					.orElseThrow(() -> new RuntimeException("Producto no encontrado"));

			if (producto.getStockProducto() < detRequest.getCantidad()) {
				throw new RuntimeException("Stock insuficiente para " + producto.getNombreProducto());
			}

			producto.setStockProducto(producto.getStockProducto() - detRequest.getCantidad());
			productoRepository.save(producto);

			double precioUnitario = producto.getPrecioProducto();
			double subtotalDet = precioUnitario * detRequest.getCantidad();

			DetallePedido detalle = new DetallePedido();
			detalle.setPedido(pedido);
			detalle.setProducto(producto);
			detalle.setCantidad(detRequest.getCantidad());
			detalle.setPrecioUnitario(precioUnitario);
			detalle.setSubtotal(subtotalDet);

			detallePedidoRepository.save(detalle);
			detalles.add(detalle);

			subtotal += subtotalDet;
		}

		double iva = subtotal * 0.12;
		double total = subtotal + iva;

		pedido.setSubtotal(subtotal);
		pedido.setIva(iva);
		pedido.setTotal(total);

		return pedidoRepository.save(pedido);
	}

	// ============================================================
	// OBTENER PEDIDO POR ID
	// ============================================================
	@Override
	public Pedido obtenerPedidoPorId(Integer id) {
		return pedidoRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
	}

	// ============================================================
	// LISTAR POR CONSUMIDOR
	// ============================================================
	@Override
	public List<Pedido> listarPedidosPorConsumidor(Integer idConsumidor) {

		Consumidor consumidor = consumidorRepository.findById(idConsumidor)
				.orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));

		return pedidoRepository.findByConsumidor(consumidor);
	}

	// ============================================================
	// LISTAR POR VENDEDOR
	// ============================================================
	@Override
	public List<Pedido> listarPedidosPorVendedor(Integer idVendedor) {

		Vendedor vendedor = vendedorRepository.findById(idVendedor)
				.orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

		return pedidoRepository.findByVendedor(vendedor);
	}

	// ============================================================
	// LISTAR DETALLES
	// ============================================================
	@Override
	public List<DetallePedido> listarDetalles(Integer idPedido) {

		Pedido pedido = pedidoRepository.findById(idPedido)
				.orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

		return detallePedidoRepository.findByPedido(pedido);
	}

	// ============================================================
	// CAMBIAR ESTADO
	// ============================================================
	@Override
	public Pedido cambiarEstado(Integer idPedido, String nuevoEstado) {

		Pedido pedido = pedidoRepository.findById(idPedido)
				.orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

		pedido.setEstadoPedido(nuevoEstado);
		return pedidoRepository.save(pedido);
	}

	// ============================================================
	// COMPRAR AHORA (1 SOLO PRODUCTO)
	// ============================================================
	@Override
	public Pedido comprarAhora(PedidoRequest request) {

		Consumidor consumidor = consumidorRepository.findById(request.getIdConsumidor())
				.orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));

		Vendedor vendedor = vendedorRepository.findById(request.getIdVendedor())
				.orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

		Pedido pedido = new Pedido();
		pedido.setConsumidor(consumidor);
		pedido.setVendedor(vendedor);
		pedido.setFechaPedido(LocalDateTime.now());
		pedido.setEstadoPedido("Pendiente");
		pedido.setMetodoPago(request.getMetodoPago());
		pedido.setSubtotal(0.0);
		pedido.setIva(0.0);
		pedido.setTotal(0.0);

		pedidoRepository.save(pedido);

		if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
			throw new RuntimeException("No se envió ningún producto para la compra.");
		}

		DetallePedidoAddRequest detRequest = request.getDetalles().get(0);

		Producto producto = productoRepository.findById(detRequest.getIdProducto())
				.orElseThrow(() -> new RuntimeException("Producto no encontrado"));

		if (producto.getStockProducto() < detRequest.getCantidad()) {
			throw new RuntimeException("Stock insuficiente para " + producto.getNombreProducto());
		}

		producto.setStockProducto(producto.getStockProducto() - detRequest.getCantidad());
		productoRepository.save(producto);

		double subtotal = producto.getPrecioProducto() * detRequest.getCantidad();
		double iva = subtotal * 0.12;
		double total = subtotal + iva;

		DetallePedido detalle = new DetallePedido();
		detalle.setPedido(pedido);
		detalle.setProducto(producto);
		detalle.setCantidad(detRequest.getCantidad());
		detalle.setPrecioUnitario(producto.getPrecioProducto());
		detalle.setSubtotal(subtotal);

		detallePedidoRepository.save(detalle);

		pedido.setSubtotal(subtotal);
		pedido.setIva(iva);
		pedido.setTotal(total);

		return pedidoRepository.save(pedido);
	}

	// ============================================================
	// FINALIZAR COMPRA (EFECTIVO – TRANSFERENCIA – TARJETA)
	// ============================================================
	@Override
	public Pedido finalizarPedido(Integer idPedido, String metodoPago, MultipartFile comprobante, String numTarjeta,
			String fechaTarjeta, String cvv, String titular) {

		Pedido pedido = pedidoRepository.findById(idPedido)
				.orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

		pedido.setMetodoPago(metodoPago);

		// ---------------- EFECTIVO ----------------
		if (metodoPago.equalsIgnoreCase("EFECTIVO")) {
			pedido.setEstadoPedido("COMPLETADO");
			return pedidoRepository.save(pedido);
		}

		// ---------------- TRANSFERENCIA ----------------
		if (metodoPago.equalsIgnoreCase("TRANSFERENCIA")) {

			if (comprobante == null || comprobante.isEmpty()) {
				throw new RuntimeException("Debe subir comprobante.");
			}

			try {
				String carpeta = "uploads/comprobantes/";
				File destinoCarpeta = new File(carpeta);
				if (!destinoCarpeta.exists())
					destinoCarpeta.mkdirs();

				String nombreArchivo = System.currentTimeMillis() + "_" + comprobante.getOriginalFilename();
				File destino = new File(carpeta + nombreArchivo);

				comprobante.transferTo(destino);

				pedido.setComprobanteUrl("/" + carpeta + nombreArchivo);
				pedido.setEstadoPedido("PENDIENTE_VERIFICACION");

			} catch (Exception e) {
				throw new RuntimeException("Error al guardar comprobante: " + e.getMessage());
			}

			return pedidoRepository.save(pedido);
		}

		// ---------------- TARJETA ----------------
		if (metodoPago.equalsIgnoreCase("TARJETA")) {

			if (numTarjeta == null || numTarjeta.length() < 16)
				throw new RuntimeException("Número de tarjeta inválido");

			if (cvv == null || cvv.length() < 3)
				throw new RuntimeException("CVV inválido");

			if (fechaTarjeta == null)
				throw new RuntimeException("Fecha de expiración inválida");

			if (titular == null || titular.length() < 3)
				throw new RuntimeException("Nombre del titular inválido");

			String ultimos4 = numTarjeta.substring(numTarjeta.length() - 4);
			pedido.setDatosTarjeta("**** **** **** " + ultimos4);

			pedido.setEstadoPedido("COMPLETADO");
		}

		return pedidoRepository.save(pedido);
	}

	// ============================================================
	// CREAR PEDIDO DESDE CARRITO (NUEVO)
	// ============================================================
	@Override
	public Pedido crearPedidoDesdeCarrito(PedidoCarritoRequest request) {

		Consumidor consumidor = consumidorRepository.findById(request.getIdConsumidor())
				.orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));

		Vendedor vendedor = vendedorRepository.findById(request.getIdVendedor())
				.orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

		Pedido pedido = new Pedido();
		pedido.setConsumidor(consumidor);
		pedido.setVendedor(vendedor);
		pedido.setFechaPedido(LocalDateTime.now());
		pedido.setEstadoPedido("Pendiente");
		pedido.setMetodoPago("SIN_ASIGNAR");

		pedidoRepository.save(pedido);

		double subtotal = 0;

		for (PedidoCarritoRequest.DetalleProducto item : request.getDetalles()) {

			Producto producto = productoRepository.findById(item.getIdProducto())
					.orElseThrow(() -> new RuntimeException("Producto no encontrado"));

			if (producto.getStockProducto() < item.getCantidad()) {
				throw new RuntimeException("Stock insuficiente para " + producto.getNombreProducto());
			}

			producto.setStockProducto(producto.getStockProducto() - item.getCantidad());
			productoRepository.save(producto);

			double precio = producto.getPrecioProducto();
			double subtotalDet = precio * item.getCantidad();

			DetallePedido det = new DetallePedido();
			det.setPedido(pedido);
			det.setProducto(producto);
			det.setCantidad(item.getCantidad());
			det.setPrecioUnitario(precio);
			det.setSubtotal(subtotalDet);

			detallePedidoRepository.save(det);

			subtotal += subtotalDet;
		}

		double iva = subtotal * 0.12;
		double total = subtotal + iva;

		pedido.setSubtotal(subtotal);
		pedido.setIva(iva);
		pedido.setTotal(total);

		return pedidoRepository.save(pedido);
	}

	// ============================================================
	// FINALIZAR COMPRA — MODO SIMPLE (PUT)
	// ============================================================
	@Override
	public Pedido finalizarPedido(Integer idPedido, String metodoPago) {

		Pedido pedido = pedidoRepository.findById(idPedido)
				.orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

		pedido.setMetodoPago(metodoPago);

		// Si es efectivo → se completa inmediatamente
		if (metodoPago.equalsIgnoreCase("EFECTIVO")) {
			pedido.setEstadoPedido("COMPLETADO");
		}

		// Si es transferencia o tarjeta → queda pendiente
		if (metodoPago.equalsIgnoreCase("TRANSFERENCIA") || metodoPago.equalsIgnoreCase("TARJETA")) {

			pedido.setEstadoPedido("PENDIENTE_VERIFICACION");
		}

		return pedidoRepository.save(pedido);
	}

	// ============================================================
	// ESTADISTICAS DE VENTAS
	// ============================================================
	@Override
	public Map<String, Object> obtenerEstadisticasVendedor(Integer idVendedor) {

	    Vendedor vendedor = vendedorRepository.findById(idVendedor)
	            .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

	    Integer pedidosCompletados = pedidoRepository.countByVendedor(vendedor);

	    Double totalGenerado = pedidoRepository.sumarIngresosPorVendedor(idVendedor);

	    if (totalGenerado == null) {
	        totalGenerado = 0.0;
	    }

	    Double promedio =
	            pedidosCompletados > 0 ? totalGenerado / pedidosCompletados : 0.0;

	    Map<String, Object> stats = new HashMap<>();
	    stats.put("pedidos", pedidosCompletados);
	    stats.put("total", totalGenerado);
	    stats.put("promedio", promedio);

	    return stats;
	}


	// ============================================================
	// VENTAS MENSUALES
	// ============================================================
	
	@Override
	public List<Map<String, Object>> obtenerVentasMensuales(Integer idVendedor) {

	    List<Object[]> resultados =
	            pedidoRepository.obtenerVentasMensualesPorVendedor(idVendedor);

	    String[] meses = {
	        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
	        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
	    };

	    List<Map<String, Object>> respuesta = new ArrayList<>();

	    for (Object[] fila : resultados) {
	        Integer mesNumero = ((Number) fila[0]).intValue();
	        Double total = fila[1] != null ? ((Number) fila[1]).doubleValue() : 0.0;

	        Map<String, Object> item = new HashMap<>();
	        item.put("mes", meses[mesNumero - 1]);
	        item.put("total", total);

	        respuesta.add(item);
	    }

	    return respuesta;
	}

}
