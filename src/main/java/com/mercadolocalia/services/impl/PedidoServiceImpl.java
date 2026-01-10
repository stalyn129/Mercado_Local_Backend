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
import com.mercadolocalia.entities.EstadoPedidoVendedor;
import com.mercadolocalia.entities.EstadoSeguimientoPedido;
import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.PedidoVendedor;
import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.repositories.CarritoItemRepository;
import com.mercadolocalia.repositories.CarritoRepository;
import com.mercadolocalia.repositories.ConsumidorRepository;
import com.mercadolocalia.repositories.DetallePedidoRepository;
import com.mercadolocalia.repositories.PedidoRepository;
import com.mercadolocalia.repositories.PedidoVendedorRepository;
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
	@Autowired
	private PedidoVendedorRepository pedidoVendedorRepo;

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
	         // ✅ RUTA ABSOLUTA
	         String directorioBase = System.getProperty("user.dir");
	         String carpeta = directorioBase + "/uploads/comprobantes/";
	         
	         File directorio = new File(carpeta);
	         if (!directorio.exists()) {
	             boolean creado = directorio.mkdirs();
	             System.out.println("📁 Directorio creado: " + creado + " en: " + carpeta);
	         }

	         String nombre = System.currentTimeMillis() + "_" + comprobante.getOriginalFilename();
	         File archivo = new File(carpeta + nombre);
	         
	         System.out.println("💾 Guardando archivo en: " + archivo.getAbsolutePath());
	         comprobante.transferTo(archivo);

	         pedido.setComprobanteUrl("/uploads/comprobantes/" + nombre);
	         pedido.setEstadoPedido(EstadoPedido.PENDIENTE_VERIFICACION);

	     } catch (Exception e) {
	         // ✅ MOSTRAR ERROR REAL
	         e.printStackTrace();
	         System.err.println("❌ Error al guardar comprobante: " + e.getMessage());
	         throw new RuntimeException("Error al guardar comprobante: " + e.getMessage());
	     }
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

	    Map<Vendedor, List<CarritoItem>> itemsPorVendedor = new HashMap<>();
	    for (CarritoItem item : carrito.getItems()) {
	        Vendedor vendedor = item.getProducto().getVendedor();
	        itemsPorVendedor.computeIfAbsent(vendedor, v -> new ArrayList<>()).add(item);
	    }

	    List<Pedido> pedidosCreados = new ArrayList<>();

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
	        for (CarritoItem item : items) {
	            subtotal += (item.getProducto().getPrecioProducto() * item.getCantidad());
	        }

	        pedido.setSubtotal(subtotal);
	        pedido.setIva(subtotal * 0.12);
	        pedido.setTotal(subtotal + pedido.getIva());

	        // Guardamos el pedido como siempre
	        Pedido pedidoGuardado = pedidoRepository.save(pedido);

	        // ============================================================
	        // 🔥 NUEVO: REGISTRAR EN LA TABLA DEL VENDEDOR
	        // ============================================================
	        PedidoVendedor pv = new PedidoVendedor();
	        pv.setPedido(pedidoGuardado);
	        pv.setVendedor(vendedor);
	        pv.setEstado(EstadoPedidoVendedor.NUEVO); 
	        pv.setFechaActualizacion(LocalDateTime.now());
	        pedidoVendedorRepo.save(pv); 
	        // ============================================================

	        for (CarritoItem item : items) {
	            DetallePedido detalle = new DetallePedido();
	            detalle.setPedido(pedidoGuardado);
	            detalle.setProducto(item.getProducto());
	            detalle.setCantidad(item.getCantidad());
	            detalle.setPrecioUnitario(item.getProducto().getPrecioProducto());
	            detalle.setSubtotal(item.getProducto().getPrecioProducto() * item.getCantidad());
	            detallePedidoRepository.save(detalle);
	        }

	        pedidosCreados.add(pedidoGuardado);
	    }

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

	    // 🚨 NUEVA VALIDACIÓN: No permitir cancelar si es efectivo y ya fue confirmado
	    if ("EFECTIVO".equalsIgnoreCase(pedido.getMetodoPago()) && 
	        pedido.getEstadoPedido() != EstadoPedido.PENDIENTE) {
	        throw new RuntimeException(
	            "Los pedidos en efectivo no pueden cancelarse una vez confirmados. " +
	            "Contacta al vendedor si tienes algún problema."
	        );
	    }

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
	
	//
	
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

	
	@Override
	public Pedido cambiarEstadoSeguimiento(Integer idPedido, String nuevoEstadoSeguimiento) {

	    Pedido pedido = pedidoRepository.findById(idPedido)
	        .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

	    EstadoSeguimientoPedido estado = EstadoSeguimientoPedido.valueOf(nuevoEstadoSeguimiento);

	    pedido.setEstadoSeguimiento(estado);

	    return pedidoRepository.save(pedido);
	}
	
	// ============================================================
    // ACTUALIZAR ESTADO OPERATIVO (PARA EL DASHBOARD DEL VENDEDOR)
    // ============================================================
	@Override
	public List<PedidoVendedor> listarPedidosParaDashboardVendedor(Integer idVendedor) {
	    return pedidoVendedorRepo.findByVendedor_IdVendedor(idVendedor);
	}

	@Override
	@Transactional
	public void actualizarEstadoOperativo(Integer idPedidoVendedor, String nuevoEstado) {
	    // 1. Buscar el registro del vendedor
	    PedidoVendedor pv = pedidoVendedorRepo.findById(idPedidoVendedor)
	        .orElseThrow(() -> new RuntimeException("No se encontró el registro para el vendedor"));

	    // 2. Actualizar estado del vendedor
	    try {
	        EstadoPedidoVendedor estadoEnum = EstadoPedidoVendedor.valueOf(nuevoEstado.toUpperCase());
	        pv.setEstado(estadoEnum);
	    } catch (IllegalArgumentException e) {
	        throw new RuntimeException("Estado '" + nuevoEstado + "' no es válido.");
	    }
	    
	    pv.setFechaActualizacion(LocalDateTime.now());
	    pedidoVendedorRepo.save(pv);

	    // 3. LOGICA DE SINCRONIZACIÓN AUTOMÁTICA
	    // Si este vendedor completa su parte, verificamos si el pedido global debe completarse
	    if (pv.getEstado() == EstadoPedidoVendedor.ENTREGADO) {
	        verificarYFinalizarPedidoGlobal(pv.getPedido());
	    }

	    // 4. Notificar al Consumidor
	    notificacionService.crearNotificacion(
	        pv.getPedido().getConsumidor().getUsuario(),
	        "📦 Tu paquete de " + pv.getVendedor().getNombreEmpresa() + " cambió a: " + nuevoEstado,
	        "PEDIDO",
	        pv.getPedido().getIdPedido()
	    );
	}

	// Método privado para cerrar el pedido general automáticamente
	private void verificarYFinalizarPedidoGlobal(Pedido pedido) {
	    // 1. Buscamos todos los registros de "pedido_vendedor" que pertenecen a este mismo pedido
	    List<PedidoVendedor> participaciones = pedidoVendedorRepo.findAll().stream()
	            .filter(pv -> pv.getPedido().getIdPedido().equals(pedido.getIdPedido()))
	            .toList();

	    // 2. Verificamos si todos ya están en estado ENTREGADO
	    boolean todosEntregaron = participaciones.stream()
	            .allMatch(pv -> pv.getEstado() == EstadoPedidoVendedor.ENTREGADO);

	    // 3. Si todos terminaron, el pedido general pasa a COMPLETADO
	    if (todosEntregaron) {
	        pedido.setEstadoPedido(EstadoPedido.COMPLETADO);
	        pedidoRepository.save(pedido);
	    }
	}
	
	//
	
	@Override
	public List<DetallePedido> listarDetallesPorVendedor(Integer idPedido, Integer idVendedor) {
	    // Buscamos todos los detalles del pedido original
	    List<DetallePedido> todosLosDetalles = detallePedidoRepository.findByPedido(
	        pedidoRepository.findById(idPedido).orElseThrow(() -> new RuntimeException("Pedido no encontrado"))
	    );

	    // Filtramos para devolver solo los que pertenecen al vendedor que consulta
	    return todosLosDetalles.stream()
	            .filter(d -> d.getProducto().getVendedor().getIdVendedor().equals(idVendedor))
	            .toList();
	}
	
	@Override
	@Transactional
	public Pedido checkoutUnificado(Integer idConsumidor) {
	    
	    System.out.println("🔍 INICIANDO CHECKOUT UNIFICADO para consumidor: " + idConsumidor);

	    // 1️⃣ Obtener el carrito
	    Carrito carrito = carritoRepository.findByConsumidorIdConsumidor(idConsumidor)
	            .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

	    System.out.println("✅ Carrito encontrado. Items: " + carrito.getItems().size());

	    if (carrito.getItems().isEmpty()) {
	        throw new RuntimeException("El carrito está vacío");
	    }

	    // 🔥 OBTENER EL PRIMER VENDEDOR DEL CARRITO
	    CarritoItem primerItem = carrito.getItems().get(0);
	    System.out.println("🔍 Primer item: " + primerItem.getProducto().getNombreProducto());
	    
	    Vendedor primerVendedor = primerItem.getProducto().getVendedor();
	    
	    if (primerVendedor == null) {
	        System.out.println("❌ ERROR: El producto no tiene vendedor asignado");
	        throw new RuntimeException("Los productos no tienen vendedor asignado");
	    }
	    
	    System.out.println("✅ Vendedor encontrado: " + primerVendedor.getIdVendedor());

	    // 2️⃣ CREAR UN SOLO PEDIDO
	    Pedido pedido = new Pedido();
	    pedido.setConsumidor(carrito.getConsumidor());
	    pedido.setVendedor(primerVendedor);  // ✅ ASIGNAR VENDEDOR
	    pedido.setMetodoPago("PENDIENTE");
	    pedido.setEstadoPedido(EstadoPedido.PENDIENTE);
	    pedido.setFechaPedido(LocalDateTime.now());

	    System.out.println("✅ Pedido creado (sin guardar aún)");

	    // 3️⃣ Calcular el total de TODOS los items
	    double subtotal = 0.0;
	    for (CarritoItem item : carrito.getItems()) {
	        subtotal += (item.getProducto().getPrecioProducto() * item.getCantidad());
	    }

	    pedido.setSubtotal(subtotal);
	    pedido.setIva(subtotal * 0.12);
	    pedido.setTotal(subtotal + pedido.getIva());

	    System.out.println("✅ Totales calculados - Subtotal: " + subtotal + ", Total: " + pedido.getTotal());

	    // 4️⃣ Guardar el pedido único
	    System.out.println("🔍 Intentando guardar pedido...");
	    
	    try {
	        Pedido pedidoGuardado = pedidoRepository.save(pedido);
	        System.out.println("✅ Pedido guardado con ID: " + pedidoGuardado.getIdPedido());
	        
	        // 5️⃣ Crear detalles y registros en pedido_vendedor
	        Map<Vendedor, List<CarritoItem>> itemsPorVendedor = new HashMap<>();
	        for (CarritoItem item : carrito.getItems()) {
	            Vendedor vendedor = item.getProducto().getVendedor();
	            itemsPorVendedor.computeIfAbsent(vendedor, v -> new ArrayList<>()).add(item);
	        }

	        // 6️⃣ Crear detalles del pedido
	        for (CarritoItem item : carrito.getItems()) {
	            DetallePedido detalle = new DetallePedido();
	            detalle.setPedido(pedidoGuardado);
	            detalle.setProducto(item.getProducto());
	            detalle.setCantidad(item.getCantidad());
	            detalle.setPrecioUnitario(item.getProducto().getPrecioProducto());
	            detalle.setSubtotal(item.getProducto().getPrecioProducto() * item.getCantidad());
	            detallePedidoRepository.save(detalle);
	        }
	        
	        System.out.println("✅ Detalles del pedido creados");

	        // 7️⃣ CREAR REGISTROS EN PEDIDO_VENDEDOR PARA CADA VENDEDOR
	        for (Vendedor vendedor : itemsPorVendedor.keySet()) {
	            PedidoVendedor pv = new PedidoVendedor();
	            pv.setPedido(pedidoGuardado);
	            pv.setVendedor(vendedor);
	            pv.setEstado(EstadoPedidoVendedor.NUEVO);
	            pv.setFechaActualizacion(LocalDateTime.now());
	            pedidoVendedorRepo.save(pv);
	        }
	        
	        System.out.println("✅ Registros de pedido_vendedor creados");

	        // 8️⃣ Limpiar el carrito
	        carritoItemRepository.deleteAll(carrito.getItems());
	        System.out.println("✅ Carrito limpiado");

	        return pedidoGuardado;
	        
	    } catch (Exception e) {
	        System.out.println("❌ ERROR AL GUARDAR PEDIDO: " + e.getMessage());
	        e.printStackTrace();
	        throw e;
	    }
	}

}