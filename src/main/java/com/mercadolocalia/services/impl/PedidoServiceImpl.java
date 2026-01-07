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
import com.mercadolocalia.entities.Carrito;
import com.mercadolocalia.entities.CarritoItem;
import com.mercadolocalia.entities.Consumidor;
import com.mercadolocalia.entities.DetallePedido;
import com.mercadolocalia.entities.EstadoPedido;
import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.repositories.CarritoItemRepository;
import com.mercadolocalia.repositories.CarritoRepository;
import com.mercadolocalia.repositories.ConsumidorRepository;
import com.mercadolocalia.repositories.DetallePedidoRepository;
import com.mercadolocalia.repositories.PedidoRepository;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.repositories.VendedorRepository;
import com.mercadolocalia.services.NotificacionService;
import com.mercadolocalia.services.PedidoService;

import jakarta.transaction.Transactional;

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
	@Autowired
	private NotificacionService notificacionService;
	@Autowired
	private CarritoRepository carritoRepository;
	@Autowired
	private CarritoItemRepository carritoItemRepository;

	// ============================================================
	// CREAR PEDIDO COMPLETO
	// ============================================================
	@Override
	@Transactional
	public Pedido crearPedido(PedidoRequest request) {

		Consumidor consumidor = consumidorRepository.findById(request.getIdConsumidor())
				.orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));

		Vendedor vendedor = vendedorRepository.findById(request.getIdVendedor())
				.orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

		Pedido pedido = new Pedido();
		pedido.setConsumidor(consumidor);
		pedido.setVendedor(vendedor);
		pedido.setFechaPedido(LocalDateTime.now());

		// ✅ ESTADO CORRECTO
		pedido.setEstadoPedido(EstadoPedido.PENDIENTE);

		pedido.setMetodoPago(request.getMetodoPago());
		pedido.setSubtotal(0.0);
		pedido.setIva(0.0);
		pedido.setTotal(0.0);

		pedidoRepository.save(pedido);

		double subtotal = 0;

		for (DetallePedidoAddRequest det : request.getDetalles()) {

			Producto producto = productoRepository.findById(det.getIdProducto())
					.orElseThrow(() -> new RuntimeException("Producto no encontrado"));

			if (producto.getStockProducto() < det.getCantidad()) {
				throw new RuntimeException("Stock insuficiente para " + producto.getNombreProducto());
			}

			// 🔻 Descontar stock
			producto.setStockProducto(producto.getStockProducto() - det.getCantidad());
			productoRepository.save(producto);

			double sub = producto.getPrecioProducto() * det.getCantidad();

			DetallePedido detalle = new DetallePedido();
			detalle.setPedido(pedido);
			detalle.setProducto(producto);
			detalle.setCantidad(det.getCantidad());
			detalle.setPrecioUnitario(producto.getPrecioProducto());
			detalle.setSubtotal(sub);

			detallePedidoRepository.save(detalle);

			subtotal += sub;
		}

		// 🧮 Totales
		pedido.setSubtotal(subtotal);
		pedido.setIva(subtotal * 0.12);
		pedido.setTotal(subtotal + pedido.getIva());

		// 🔔 Notificación
		notificacionService.crearNotificacion(consumidor.getUsuario(),
				"🛒 Pedido #" + pedido.getIdPedido() + " creado correctamente", "PEDIDO", pedido.getIdPedido());

		return pedidoRepository.save(pedido);
	}

	// ============================================================
	// OBTENER PEDIDO
	// ============================================================
	@Override
	public Pedido obtenerPedidoPorId(Integer id) {
		return pedidoRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
	}

	// ============================================================
	// LISTAR PEDIDOS POR CONSUMIDOR
	// ============================================================
	@Override
	public List<Pedido> listarPedidosPorConsumidor(Integer idConsumidor) {

		Consumidor consumidor = consumidorRepository.findById(idConsumidor)
				.orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));

		return pedidoRepository.findByConsumidor(consumidor);
	}

	// ============================================================
	// LISTAR PEDIDOS POR VENDEDOR
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
	// CAMBIAR ESTADO DEL PEDIDO
	// ============================================================
	@Override
	@Transactional
	public Pedido cambiarEstado(Integer idPedido, String nuevoEstado) {

		Pedido pedido = pedidoRepository.findById(idPedido)
				.orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

		EstadoPedido estadoActual = pedido.getEstadoPedido();
		validarPedidoEditable(pedido);

		EstadoPedido estadoNuevo;
		try {
			estadoNuevo = EstadoPedido.valueOf(nuevoEstado.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Estado de pedido inválido");
		}

		// 🔒 Reglas de negocio
		if (estadoActual == EstadoPedido.CANCELADO) {
			throw new RuntimeException("No se puede modificar un pedido cancelado");
		}

		if (estadoActual == EstadoPedido.COMPLETADO) {
			throw new RuntimeException("El pedido ya fue completado");
		}

		// 🔄 Transiciones válidas
		switch (estadoNuevo) {

		case PROCESANDO:
		case PENDIENTE_VERIFICACION:
		case COMPLETADO:
		case CANCELADO:
			pedido.setEstadoPedido(estadoNuevo);
			break;

		default:
			throw new RuntimeException("Transición de estado no permitida");
		}

		pedidoRepository.save(pedido);

		// 🔔 Notificación
		notificacionService.crearNotificacion(pedido.getConsumidor().getUsuario(),
				"📦 Tu pedido #" + pedido.getIdPedido() + " ahora está en estado: " + estadoNuevo.name(), "PEDIDO",
				pedido.getIdPedido());

		return pedido;
	}

	// ============================================================
	// COMPRAR AHORA
	// ============================================================
	@Override
	public Pedido comprarAhora(PedidoRequest request) {
		return crearPedido(request);
	}

	// ============================================================
	// FINALIZAR PEDIDO (SIMPLE)
	// ============================================================
	@Override
	@Transactional
	public Pedido finalizarPedido(Integer idPedido, String metodoPago) {

	    Pedido pedido = pedidoRepository.findById(idPedido)
	            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

	    // 🔒 VALIDACIÓN CENTRALIZADA
	    validarPedidoEditable(pedido);

	    pedido.setMetodoPago(metodoPago.toUpperCase());

	    // ==========================
	    // EFECTIVO
	    // ==========================
	    if (metodoPago.equalsIgnoreCase("EFECTIVO")) {

	        pedido.setEstadoPedido(EstadoPedido.PROCESANDO);

	    }
	    // ==========================
	    // TRANSFERENCIA
	    // ==========================
	    else if (metodoPago.equalsIgnoreCase("TRANSFERENCIA")) {

	        pedido.setEstadoPedido(EstadoPedido.PENDIENTE_VERIFICACION);

	    }
	    // ==========================
	    // MÉTODO NO VÁLIDO
	    // ==========================
	    else {
	        throw new RuntimeException("Método de pago no válido");
	    }

	    // ==========================
	    // NOTIFICACIONES
	    // ==========================
	    notificacionService.crearNotificacion(
	            pedido.getConsumidor().getUsuario(),
	            "💳 Tu pedido #" + pedido.getIdPedido() + " fue finalizado con método: " + metodoPago,
	            "PEDIDO",
	            pedido.getIdPedido()
	    );

	    notificacionService.crearNotificacion(
	            pedido.getVendedor().getUsuario(),
	            "📦 Pedido #" + pedido.getIdPedido() + " listo para procesar",
	            "PEDIDO",
	            pedido.getIdPedido()
	    );

	    return pedidoRepository.save(pedido);
	}


	// ============================================================
	// FINALIZAR PEDIDO (COMPLETO)
	// ============================================================
	@Override
	@Transactional
	public Pedido finalizarPedido(
	        Integer idPedido,
	        String metodoPago,
	        MultipartFile comprobante,
	        String numTarjeta,
	        String fechaTarjeta,
	        String cvv,
	        String titular
	) {

	    Pedido pedido = pedidoRepository.findById(idPedido)
	            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

	    // 🔒 VALIDACIÓN CENTRALIZADA
	    validarPedidoEditable(pedido);

	    pedido.setMetodoPago(metodoPago.toUpperCase());

	    // ===============================
	    // EFECTIVO
	    // ===============================
	    if (metodoPago.equalsIgnoreCase("EFECTIVO")) {

	        pedido.setEstadoPedido(EstadoPedido.PROCESANDO);

	        notificacionService.crearNotificacion(
	                pedido.getVendedor().getUsuario(),
	                "💵 Pedido #" + pedido.getIdPedido() + " en efectivo, pendiente de entrega",
	                "PEDIDO",
	                pedido.getIdPedido()
	        );
	    }

	    // ===============================
	    // TRANSFERENCIA
	    // ===============================
	    else if (metodoPago.equalsIgnoreCase("TRANSFERENCIA")) {

	        if (comprobante == null || comprobante.isEmpty()) {
	            throw new RuntimeException("Debe subir el comprobante de transferencia");
	        }

	        try {
	            String carpeta = "uploads/comprobantes/";
	            new File(carpeta).mkdirs();

	            String nombre = System.currentTimeMillis() + "_" + comprobante.getOriginalFilename();
	            comprobante.transferTo(new File(carpeta + nombre));

	            pedido.setComprobanteUrl("/" + carpeta + nombre);
	            pedido.setEstadoPedido(EstadoPedido.PENDIENTE_VERIFICACION);

	        } catch (Exception e) {
	            throw new RuntimeException("Error al guardar comprobante");
	        }

	        notificacionService.crearNotificacion(
	                pedido.getVendedor().getUsuario(),
	                "💳 Pedido #" + pedido.getIdPedido() + " pendiente de verificación de transferencia",
	                "PEDIDO",
	                pedido.getIdPedido()
	        );
	    }

	    // ===============================
	    // TARJETA
	    // ===============================
	    else if (metodoPago.equalsIgnoreCase("TARJETA")) {

	        if (numTarjeta == null || numTarjeta.length() < 12) {
	            throw new RuntimeException("Número de tarjeta inválido");
	        }

	        String ultimos4 = numTarjeta.substring(numTarjeta.length() - 4);
	        pedido.setDatosTarjeta("**** **** **** " + ultimos4);

	        pedido.setEstadoPedido(EstadoPedido.COMPLETADO);

	        // 🔥 DESCONTAR STOCK SOLO AQUÍ
	        descontarStock(pedido);
	    }

	    else {
	        throw new RuntimeException("Método de pago no válido");
	    }

	    // ===============================
	    // NOTIFICACIÓN AL CONSUMIDOR
	    // ===============================
	    notificacionService.crearNotificacion(
	            pedido.getConsumidor().getUsuario(),
	            "💳 Tu pedido #" + pedido.getIdPedido() + " fue procesado con método: " + metodoPago,
	            "PEDIDO",
	            pedido.getIdPedido()
	    );

	    return pedidoRepository.save(pedido);
	}


	// ============================================================
	// PEDIDO DESDE CARRITO
	// ============================================================
	@Override
	@Transactional
	public Pedido crearPedidoDesdeCarrito(PedidoCarritoRequest request) {

		Consumidor consumidor = consumidorRepository.findById(request.getIdConsumidor())
				.orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));

		Vendedor vendedor = vendedorRepository.findById(request.getIdVendedor())
				.orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

		Pedido pedido = new Pedido();
		pedido.setConsumidor(consumidor);
		pedido.setVendedor(vendedor);
		pedido.setFechaPedido(LocalDateTime.now());

		pedido.setEstadoPedido(EstadoPedido.PENDIENTE);

		pedido.setMetodoPago(null);

		pedido = pedidoRepository.save(pedido);

		double subtotal = 0;

		for (PedidoCarritoRequest.DetalleProducto item : request.getDetalles()) {

			Producto producto = productoRepository.findById(item.getIdProducto())
					.orElseThrow(() -> new RuntimeException("Producto no encontrado"));

			// ⚠️ VALIDAR STOCK
			if (producto.getStockProducto() < item.getCantidad()) {
				throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombreProducto());
			}

			double sub = producto.getPrecioProducto() * item.getCantidad();

			DetallePedido detalle = new DetallePedido();
			detalle.setPedido(pedido);
			detalle.setProducto(producto);
			detalle.setCantidad(item.getCantidad());
			detalle.setPrecioUnitario(producto.getPrecioProducto());
			detalle.setSubtotal(sub);

			detallePedidoRepository.save(detalle);

			subtotal += sub;
		}

		pedido.setSubtotal(subtotal);
		pedido.setIva(subtotal * 0.12);
		pedido.setTotal(subtotal + pedido.getIva());

		return pedidoRepository.save(pedido);
	}

	// ============================================================
	// ESTADÍSTICAS
	// ============================================================
	@Override
	public Map<String, Object> obtenerEstadisticasVendedor(Integer idVendedor) {

		Vendedor vendedor = vendedorRepository.findById(idVendedor)
				.orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

		Map<String, Object> stats = new HashMap<>();
		stats.put("pedidos", pedidoRepository.countByVendedor(vendedor));
		stats.put("total", pedidoRepository.sumarIngresosPorVendedor(idVendedor));

		return stats;
	}

	// ============================================================
	// VENTAS MENSUALES
	// ============================================================
	@Override
	public List<Map<String, Object>> obtenerVentasMensuales(Integer idVendedor) {

		List<Object[]> data = pedidoRepository.obtenerVentasMensualesPorVendedor(idVendedor);
		List<Map<String, Object>> res = new ArrayList<>();

		String[] meses = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre",
				"Octubre", "Noviembre", "Diciembre" };

		for (Object[] row : data) {
			Map<String, Object> item = new HashMap<>();
			item.put("mes", meses[((Number) row[0]).intValue() - 1]);
			item.put("total", row[1]);
			res.add(item);
		}

		return res;
	}

	//Multivendedor
	
	@Override
	@Transactional
	public List<Pedido> checkoutMultiVendedor(Integer idConsumidor) {

	    Carrito carrito = carritoRepository.findByConsumidorIdConsumidor(idConsumidor)
	            .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

	    if (carrito.getItems().isEmpty()) {
	        throw new RuntimeException("El carrito está vacío");
	    }

	    // 1️⃣ Agrupar items por vendedor
	    Map<Vendedor, List<CarritoItem>> itemsPorVendedor = new HashMap<>();

	    for (CarritoItem item : carrito.getItems()) {
	        Vendedor vendedor = item.getProducto().getVendedor();
	        itemsPorVendedor.computeIfAbsent(vendedor, v -> new ArrayList<>()).add(item);
	    }

	    List<Pedido> pedidosCreados = new ArrayList<>();

	    // 2️⃣ Crear un pedido por vendedor
	    for (Map.Entry<Vendedor, List<CarritoItem>> entry : itemsPorVendedor.entrySet()) {

	        Vendedor vendedor = entry.getKey();
	        List<CarritoItem> items = entry.getValue();

	        Pedido pedido = new Pedido();
	        pedido.setConsumidor(carrito.getConsumidor());
	        pedido.setVendedor(vendedor);
	        pedido.setMetodoPago("PENDIENTE");
	        pedido.setEstadoPedido(EstadoPedido.PENDIENTE);
	        pedido.setFechaPedido(LocalDateTime.now());

	        double subtotal = 0.0;

	        // 3️⃣ Calcular subtotal primero
	        for (CarritoItem item : items) {
	            double precio = item.getProducto().getPrecioProducto();
	            int cantidad = item.getCantidad();
	            double subItem = precio * cantidad;
	            subtotal += subItem;
	        }

	        double iva = subtotal * 0.12;
	        double total = subtotal + iva;

	        pedido.setSubtotal(subtotal);
	        pedido.setIva(iva);
	        pedido.setTotal(total);

	        // 🔥 GUARDAR EL PEDIDO PRIMERO
	        Pedido pedidoGuardado = pedidoRepository.save(pedido);

	        // 4️⃣ AHORA crear los detalles con el pedido guardado
	        for (CarritoItem item : items) {
	            double precio = item.getProducto().getPrecioProducto();
	            int cantidad = item.getCantidad();
	            double subItem = precio * cantidad;

	            DetallePedido detalle = new DetallePedido();
	            detalle.setPedido(pedidoGuardado); // ✅ Ya tiene ID
	            detalle.setProducto(item.getProducto());
	            detalle.setCantidad(cantidad);
	            detalle.setPrecioUnitario(precio);
	            detalle.setSubtotal(subItem);

	            detallePedidoRepository.save(detalle);
	        }

	        pedidosCreados.add(pedidoGuardado);
	    }

	    // 5️⃣ Vaciar carrito REAL
	    carritoItemRepository.deleteAll(carrito.getItems());

	    return pedidosCreados;
	}

	// ============================================================
	// DESCONTAR STOCK (una sola vez al pagar)
	// ============================================================
	private void descontarStock(Pedido pedido) {

		for (DetallePedido detalle : pedido.getDetalles()) {

			Producto producto = detalle.getProducto();

			if (producto.getStockProducto() < detalle.getCantidad()) {
				throw new RuntimeException("Stock insuficiente para " + producto.getNombreProducto());
			}

			producto.setStockProducto(producto.getStockProducto() - detalle.getCantidad());

			productoRepository.save(producto);
		}
	}
	
	// ============================================================
	// VALIDAR QUE EL PEDIDO NO ESTÉ CERRADO
	// ============================================================
	private void validarPedidoEditable(Pedido pedido) {

	    if (pedido.getEstadoPedido() == EstadoPedido.CANCELADO) {
	        throw new RuntimeException("Este pedido está cancelado y no puede modificarse");
	    }

	    if (pedido.getEstadoPedido() == EstadoPedido.COMPLETADO) {
	        throw new RuntimeException("Este pedido ya fue completado y no puede modificarse");
	    }
	}

	// ============================================================
	// CANCELAR PEDIDO
	// ============================================================
	@Override
	@Transactional
	public Pedido cancelarPedido(Integer idPedido) {

	    Pedido pedido = pedidoRepository.findById(idPedido)
	        .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

	    if (pedido.getEstadoPedido() != EstadoPedido.PENDIENTE &&
	        pedido.getEstadoPedido() != EstadoPedido.PROCESANDO) {
	        throw new RuntimeException("Solo se pueden cancelar pedidos pendientes");
	    }

	    // 1️⃣ Cancelar pedido
	    pedido.setEstadoPedido(EstadoPedido.CANCELADO);
	    pedidoRepository.save(pedido);

	    // 2️⃣ Recuperar carrito del consumidor
	    Carrito carrito = carritoRepository
	        .findByConsumidorIdConsumidor(
	            pedido.getConsumidor().getIdConsumidor()
	        )
	        .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

	    // 3️⃣ Volver productos al carrito
	    for (DetallePedido detalle : pedido.getDetalles()) {
	        CarritoItem item = new CarritoItem();
	        item.setCarrito(carrito);
	        item.setProducto(detalle.getProducto());
	        item.setCantidad(detalle.getCantidad());

	        carritoItemRepository.save(item);
	    }

	    return pedido;
	}
	
	@Override
	public List<Pedido> listarPedidosHistorial(Integer idConsumidor) {

	    Consumidor consumidor = consumidorRepository.findById(idConsumidor)
	            .orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));

	    List<Pedido> pedidos = pedidoRepository
	            .findByConsumidorAndTotalGreaterThanAndEstadoPedidoNot(
	                    consumidor, 0.0, EstadoPedido.CANCELADO
	            );

	    pedidos.removeIf(p ->
	            p.getDetalles() == null || p.getDetalles().isEmpty());

	    return pedidos;
	}


}
