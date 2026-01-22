package com.mercadolocalia.services.impl;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mercadolocalia.dto.*;
import com.mercadolocalia.entities.*;
import com.mercadolocalia.repositories.*;
import com.mercadolocalia.services.*;

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
		case PENDIENTE:
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

	        pedido.setEstadoPedido(EstadoPedido.PENDIENTE);

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
	         pedido.setEstadoPedido(EstadoPedido.PENDIENTE);

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

	// ============================================================
	// 🔥 CHECKOUT MULTI-VENDEDOR (CORREGIDO CON ID COMPRA UNIFICADA)
	// ============================================================
	@Override
	@Transactional
	public CheckoutResponseDTO checkoutMultiVendedor(Integer idConsumidor) {
	    
	    System.out.println("🔍 INICIANDO CHECKOUT MULTI-VENDEDOR para consumidor: " + idConsumidor);

	    // 1️⃣ Obtener el carrito
	    Carrito carrito = carritoRepository.findByConsumidorIdConsumidor(idConsumidor)
	            .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

	    System.out.println("✅ Carrito encontrado. Items: " + carrito.getItems().size());

	    if (carrito.getItems().isEmpty()) {
	        throw new RuntimeException("El carrito está vacío");
	    }

	    // 2️⃣ 🔥 GENERAR ID ÚNICO PARA COMPRA UNIFICADA
	    String idCompraUnificada = "COMPRA-" + 
	        System.currentTimeMillis() + "-" + 
	        UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	    
	    System.out.println("🆔 ID Compra Unificada generado: " + idCompraUnificada);

	    // 3️⃣ 🔥 AGRUPAR ITEMS POR VENDEDOR
	    Map<Vendedor, List<CarritoItem>> itemsPorVendedor = new HashMap<>();
	    for (CarritoItem item : carrito.getItems()) {
	        Vendedor vendedor = item.getProducto().getVendedor();
	        
	        if (vendedor == null) {
	            throw new RuntimeException("El producto '" + item.getProducto().getNombreProducto() + 
	                                     "' no tiene vendedor asignado");
	        }
	        
	        itemsPorVendedor.computeIfAbsent(vendedor, v -> new ArrayList<>()).add(item);
	    }

	    System.out.println("🏪 Vendedores involucrados: " + itemsPorVendedor.size());

	    List<Pedido> pedidosCreados = new ArrayList<>();

	    // 4️⃣ 🔥 CREAR UN PEDIDO POR CADA VENDEDOR CON MISMO idCompraUnificada
	    for (Map.Entry<Vendedor, List<CarritoItem>> entry : itemsPorVendedor.entrySet()) {
	        Vendedor vendedor = entry.getKey();
	        List<CarritoItem> items = entry.getValue();

	        System.out.println("🛍️ Procesando pedido para vendedor: " + vendedor.getNombreEmpresa() + 
	                           " (ID: " + vendedor.getIdVendedor() + ") con " + items.size() + " productos");

	        // 5️⃣ Calcular totales SOLO de los productos de este vendedor
	        double subtotal = 0.0;
	        for (CarritoItem item : items) {
	            double precioProducto = item.getProducto().getPrecioProducto();
	            int cantidad = item.getCantidad();
	            subtotal += precioProducto * cantidad;
	        }

	        double iva = subtotal * 0.12;
	        double total = subtotal + iva;

	        System.out.println("💰 Vendedor: " + vendedor.getNombreEmpresa() + 
	                           " | Subtotal: " + subtotal + 
	                           " | IVA: " + iva + 
	                           " | Total: " + total);

	        // 6️⃣ Crear pedido para este vendedor específico
	        Pedido pedido = new Pedido();
	        pedido.setConsumidor(carrito.getConsumidor());
	        pedido.setVendedor(vendedor);
	        pedido.setMetodoPago("PENDIENTE");
	        pedido.setEstadoPedido(EstadoPedido.CREADO);
	        pedido.setEstadoSeguimiento(EstadoSeguimientoPedido.PEDIDO_REALIZADO);
	        pedido.setFechaPedido(LocalDateTime.now());
	        pedido.setSubtotal(subtotal);
	        pedido.setIva(iva);
	        pedido.setTotal(total);
	        pedido.setPagado(false);
	        
	        // 🔥🔥🔥 AQUÍ ESTÁ LA CLAVE: MISMO ID PARA TODOS LOS PEDIDOS
	        pedido.setIdCompraUnificada(idCompraUnificada);

	        // 7️⃣ Guardar el pedido
	        Pedido pedidoGuardado = pedidoRepository.save(pedido);
	        System.out.println("✅ Pedido creado ID: " + pedidoGuardado.getIdPedido() + 
	                           " para vendedor: " + vendedor.getNombreEmpresa() +
	                           " | ID Compra: " + idCompraUnificada);

	        // 8️⃣ 🔥 Registrar en tabla pedido_vendedor
	        PedidoVendedor pv = new PedidoVendedor();
	        pv.setPedido(pedidoGuardado);
	        pv.setVendedor(vendedor);
	        pv.setEstado(EstadoPedidoVendedor.NUEVO);
	        pv.setFechaActualizacion(LocalDateTime.now());
	        pedidoVendedorRepo.save(pv);
	        System.out.println("✅ Registro pedido_vendedor creado");

	        // 9️⃣ Crear detalles del pedido SOLO con productos de este vendedor
	        for (CarritoItem item : items) {
	            DetallePedido detalle = new DetallePedido();
	            detalle.setPedido(pedidoGuardado);
	            detalle.setProducto(item.getProducto());
	            detalle.setCantidad(item.getCantidad());
	            detalle.setPrecioUnitario(item.getProducto().getPrecioProducto());
	            detalle.setSubtotal(item.getProducto().getPrecioProducto() * item.getCantidad());
	            detallePedidoRepository.save(detalle);
	            
	            System.out.println("  📝 Detalle agregado: " + item.getProducto().getNombreProducto() + 
	                               " x" + item.getCantidad());
	        }

	        // 🔟 Actualizar stock de productos
	        for (CarritoItem item : items) {
	            Producto producto = item.getProducto();
	            if (producto.getStockProducto() != null) {
	                int nuevoStock = producto.getStockProducto() - item.getCantidad();
	                if (nuevoStock < 0) {
	                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombreProducto());
	                }
	                producto.setStockProducto(nuevoStock);
	                productoRepository.save(producto);
	            }
	        }

	        // 1️⃣1️⃣ Crear notificación para el vendedor
	        notificacionService.crearNotificacion(
	            vendedor.getUsuario(),
	            "📦 Nuevo pedido #" + pedidoGuardado.getIdPedido() + " - Total: $" + total,
	            "PEDIDO",
	            pedidoGuardado.getIdPedido()
	        );

	        pedidosCreados.add(pedidoGuardado);
	    }

	    // 1️⃣2️⃣ Limpiar TODO el carrito al final
	    System.out.println("🗑️ Limpiando carrito completo...");
	    carritoItemRepository.deleteAll(carrito.getItems());
	    
	    // 1️⃣3️⃣ Notificar al consumidor
	    String mensaje = pedidosCreados.size() == 1 
	        ? "🛒 Tu pedido #" + pedidosCreados.get(0).getIdPedido() + " fue creado exitosamente"
	        : "🛒 Se crearon " + pedidosCreados.size() + " pedidos de " + itemsPorVendedor.size() + " vendedores";
	    
	    notificacionService.crearNotificacion(
	        carrito.getConsumidor().getUsuario(),
	        mensaje,
	        "PEDIDO",
	        pedidosCreados.get(0).getIdPedido()
	    );

	    System.out.println("🎉 Checkout completado: " + pedidosCreados.size() + " pedido(s) creados");

	    // 1️⃣4️⃣ 🔥 Retornar DTO con toda la información
	    return new CheckoutResponseDTO(idCompraUnificada, pedidosCreados);
	}

	// ============================================================
	// 🔥 CHECKOUT LEGACY (para compatibilidad con frontend existente)
	// ============================================================
	@Override
	@Transactional
	public List<Pedido> checkoutMultiVendedorLegacy(Integer idConsumidor) {
	    // Simplemente llama al método nuevo y extrae la lista de pedidos
	    CheckoutResponseDTO respuesta = checkoutMultiVendedor(idConsumidor);
	    return respuesta.getPedidos();
	}

	// ============================================================
	// 🔥 NUEVO: OBTENER COMPRA UNIFICADA
	// ============================================================
	@Override
	public CompraUnificadaDTO obtenerCompraUnificada(String idCompraUnificada, Integer idConsumidor) {
	    // Validar que el consumidor exista
	    Consumidor consumidor = consumidorRepository.findById(idConsumidor)
	        .orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));
	    
	    // Obtener pedidos usando el método seguro del repository
	    List<Pedido> pedidos = pedidoRepository
	        .findByIdCompraUnificadaAndConsumidor_IdConsumidor(idCompraUnificada, idConsumidor);
	    
	    if (pedidos.isEmpty()) {
	        throw new RuntimeException("Compra no encontrada o no pertenece al consumidor");
	    }
	    
	    // Crear y retornar DTO
	    CompraUnificadaDTO dto = new CompraUnificadaDTO(idCompraUnificada, pedidos);
	    
	    // Agregar información adicional
	    if (!pedidos.isEmpty()) {
	        Pedido primerPedido = pedidos.get(0);
	        dto.setMetodoPago(primerPedido.getMetodoPago());
	        dto.setFechaCompra(primerPedido.getFechaPedido() != null ? 
	            primerPedido.getFechaPedido().toString() : "");
	    }
	    
	    return dto;
	}

	// ============================================================
	// 🔥 NUEVO: LISTAR COMPRAS UNIFICADAS DEL CONSUMIDOR
	// ============================================================
	@Override
	public List<CompraUnificadaDTO> obtenerComprasUnificadasPorConsumidor(Integer idConsumidor) {
	    // Obtener todos los pedidos del consumidor
	    List<Pedido> todosPedidos = pedidoRepository
	        .findByConsumidor_IdConsumidorOrderByFechaPedidoDesc(idConsumidor);
	    
	    if (todosPedidos.isEmpty()) {
	        return new ArrayList<>();
	    }
	    
	    // Filtrar solo los que tienen idCompraUnificada
	    Map<String, List<Pedido>> pedidosPorCompra = todosPedidos.stream()
	        .filter(p -> p.getIdCompraUnificada() != null && !p.getIdCompraUnificada().isEmpty())
	        .collect(Collectors.groupingBy(Pedido::getIdCompraUnificada));
	    
	    if (pedidosPorCompra.isEmpty()) {
	        return new ArrayList<>();
	    }
	    
	    // Crear DTOs para cada compra unificada
	    List<CompraUnificadaDTO> compras = new ArrayList<>();
	    
	    for (Map.Entry<String, List<Pedido>> entry : pedidosPorCompra.entrySet()) {
	        // Ordenar pedidos por fecha descendente
	        List<Pedido> pedidosOrdenados = entry.getValue().stream()
	            .sorted((p1, p2) -> p2.getFechaPedido().compareTo(p1.getFechaPedido()))
	            .collect(Collectors.toList());
	        
	        CompraUnificadaDTO compraDTO = new CompraUnificadaDTO(entry.getKey(), pedidosOrdenados);
	        
	        // Agregar información adicional
	        if (!pedidosOrdenados.isEmpty()) {
	            Pedido primerPedido = pedidosOrdenados.get(0);
	            compraDTO.setMetodoPago(primerPedido.getMetodoPago());
	            compraDTO.setFechaCompra(primerPedido.getFechaPedido() != null ? 
	                primerPedido.getFechaPedido().toString() : "");
	        }
	        
	        compras.add(compraDTO);
	    }
	    
	    // Ordenar compras por fecha (más reciente primero)
	    compras.sort((c1, c2) -> {
	        if (c1.getPedidos() == null || c1.getPedidos().isEmpty()) return 1;
	        if (c2.getPedidos() == null || c2.getPedidos().isEmpty()) return -1;
	        
	        LocalDateTime fecha1 = c1.getPedidos().get(0).getFechaPedido();
	        LocalDateTime fecha2 = c2.getPedidos().get(0).getFechaPedido();
	        
	        if (fecha1 == null || fecha2 == null) return 0;
	        return fecha2.compareTo(fecha1);
	    });
	    
	    return compras;
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
	
	// ============================================================
	// LISTAR PEDIDOS HISTORIAL
	// ============================================================
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

	// ============================================================
	// CAMBIAR ESTADO DE SEGUIMIENTO
	// ============================================================
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
	
	// ============================================================
	// LISTAR DETALLES POR VENDEDOR
	// ============================================================
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
	
	// ============================================================
	// LISTAR PEDIDOS HISTORIAL (CON CONSUMIDOR)
	// ============================================================
	@Override
	public List<Pedido> listarPedidosHistorial(Consumidor consumidor) {
	    // Validar que el consumidor exista
	    if (consumidor == null) {
	        throw new IllegalArgumentException("El consumidor no puede ser nulo");
	    }
	    
	    // Verificar que el consumidor tenga un ID válido
	    if (consumidor.getIdConsumidor() == null) {
	        throw new RuntimeException("El consumidor no tiene un ID válido");
	    }
	    
	    // Obtener todos los pedidos del consumidor
	    List<Pedido> todosPedidos = pedidoRepository.findByConsumidor_IdConsumidorOrderByFechaPedidoDesc(
	        consumidor.getIdConsumidor()
	    );
	    
	    // Si no hay pedidos, devolver lista vacía
	    if (todosPedidos == null || todosPedidos.isEmpty()) {
	        return new ArrayList<>();
	    }
	    
	    // Filtrar pedidos según tu lógica de negocio
	    List<Pedido> pedidosFiltrados = todosPedidos.stream()
	        .filter(pedido -> {
	            // Excluir pedidos cancelados del historial
	            if (pedido.getEstadoPedido() == EstadoPedido.CANCELADO) {
	                return false;
	            }
	            
	            // Verificar que el pedido tenga detalles
	            if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
	                return false;
	            }
	            
	            // Verificar que el pedido tenga un total mayor a 0
	            if (pedido.getTotal() == null || pedido.getTotal() <= 0) {
	                return false;
	            }
	            
	            return true;
	        })
	        .collect(Collectors.toList());
	    
	    // Opcional: Ordenar por fecha de creación (ya debería estar ordenado por el repositorio)
	    pedidosFiltrados.sort(Comparator.comparing(Pedido::getFechaPedido).reversed());
	    
	    return pedidosFiltrados;
	}

	@Override
	public Pedido cambiarEstadoPedidoVendedor(Integer idPedido, String nuevoEstado) {
	    Pedido pedido = pedidoRepository.findById(idPedido)
	            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
	    
	    // Convertir String a Enum
	    EstadoPedidoVendedor estado = EstadoPedidoVendedor.valueOf(nuevoEstado);
	    
	    // Validar transición
	    // (Aquí puedes agregar lógica de validación de transiciones)
	    
	    pedido.setEstadoPedidoVendedor(estado);
	    
	    // Si se marca como ENTREGADO, actualizar estado general
	    if (estado == EstadoPedidoVendedor.ENTREGADO) {
	        pedido.setEstadoPedido(EstadoPedido.COMPLETADO);
	        pedido.setEstadoSeguimiento(EstadoSeguimientoPedido.ENTREGADO);
	    }
	    
	    // Si se marca como CANCELADO, actualizar estado general
	    if (estado == EstadoPedidoVendedor.CANCELADO) {
	        pedido.setEstadoPedido(EstadoPedido.CANCELADO);
	    }
	    
	    return pedidoRepository.save(pedido);
	}
}