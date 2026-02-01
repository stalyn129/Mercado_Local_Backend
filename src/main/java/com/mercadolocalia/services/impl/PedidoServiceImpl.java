package com.mercadolocalia.services.impl;

import java.time.LocalDateTime;
import java.util.*;
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
    @Autowired
    private PagoService pagoService;
    
    // ✅ AGREGAR ESTA DEPENDENCIA PARA CLOUDINARY
    @Autowired
    private FileStorageService fileStorageService;

    // ============================================================
    // 🔥 CHECKOUT MULTI-VENDEDOR (VERSIÓN 1 - SIN ID)
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
        
        return checkoutMultiVendedorConIdCompra(idConsumidor, idCompraUnificada);
    }

    // ============================================================
    // 🔥 CHECKOUT MULTI-VENDEDOR (VERSIÓN 2 - CON ID)
    // ============================================================
    @Override
    @Transactional
    public CheckoutResponseDTO checkoutMultiVendedor(Integer idConsumidor, String idCompraUnificada) {
        System.out.println("🔍 CHECKOUT CON ID PROPORCIONADO: " + idCompraUnificada);
        
        // Validar que el ID no sea nulo o vacío
        if (idCompraUnificada == null || idCompraUnificada.trim().isEmpty()) {
            throw new RuntimeException("El ID de compra unificada no puede estar vacío");
        }
        
        return checkoutMultiVendedorConIdCompra(idConsumidor, idCompraUnificada);
    }

    // ============================================================
    // 🔥 CHECKOUT MULTI-VENDEDOR CON ID (MÉTODO PRINCIPAL)
    // ============================================================
    @Override
    @Transactional
    public CheckoutResponseDTO checkoutMultiVendedorConIdCompra(Integer idConsumidor, String idCompraUnificada) {
        
        System.out.println("🔍 ========================================");
        System.out.println("🔍 INICIANDO CHECKOUT MULTI-VENDEDOR CON ID");
        System.out.println("🔍 ========================================");
        System.out.println("🔍 ID Consumidor: " + idConsumidor);
        System.out.println("🔍 ID Compra Unificada: " + idCompraUnificada);

        // 1️⃣ Obtener el carrito
        Carrito carrito = carritoRepository.findByConsumidorIdConsumidor(idConsumidor)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        System.out.println("✅ Carrito encontrado. Items: " + carrito.getItems().size());

        if (carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // 2️⃣ 🔥 VALIDAR STOCK ANTES DE CREAR PEDIDOS
        for (CarritoItem item : carrito.getItems()) {
            Producto producto = item.getProducto();
            if (producto.getStockProducto() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombreProducto() + 
                                         " (Stock: " + producto.getStockProducto() + 
                                         ", Solicitado: " + item.getCantidad() + ")");
            }
        }

        // 3️⃣ AGRUPAR ITEMS POR VENDEDOR
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

        // 4️⃣ CREAR UN PEDIDO POR CADA VENDEDOR
        for (Map.Entry<Vendedor, List<CarritoItem>> entry : itemsPorVendedor.entrySet()) {
            Vendedor vendedor = entry.getKey();
            List<CarritoItem> items = entry.getValue();

            System.out.println("🛍️ Procesando pedido para vendedor: " + vendedor.getNombreEmpresa() + 
                               " con " + items.size() + " productos");

            // 5️⃣ Calcular totales
            double subtotal = 0.0;
            for (CarritoItem item : items) {
                double precioProducto = item.getProducto().getPrecioProducto();
                int cantidad = item.getCantidad();
                subtotal += precioProducto * cantidad;
            }

            double iva = subtotal * 0.12;
            double total = subtotal + iva;

            System.out.println("💰 Subtotal: $" + subtotal + " | IVA: $" + iva + " | Total: $" + total);

            // 6️⃣ Crear pedido
            Pedido pedido = new Pedido();
            pedido.setConsumidor(carrito.getConsumidor());
            pedido.setVendedor(vendedor);
            pedido.setMetodoPago("PENDIENTE");
            pedido.setEstadoPedido(EstadoPedido.CREADO);
            pedido.setEstadoPago(EstadoPago.PENDIENTE);
            pedido.setEstadoPedidoVendedor(EstadoPedidoVendedor.NUEVO);
            pedido.setEstadoSeguimiento(EstadoSeguimientoPedido.PEDIDO_REALIZADO);
            pedido.setFechaPedido(LocalDateTime.now());
            pedido.setSubtotal(subtotal);
            pedido.setIva(iva);
            pedido.setTotal(total);
            
            // 🔥 ASIGNAR EL MISMO ID DE COMPRA UNIFICADA A TODOS LOS PEDIDOS
            pedido.setIdCompraUnificada(idCompraUnificada);

            // 7️⃣ Guardar el pedido
            Pedido pedidoGuardado = pedidoRepository.save(pedido);
            System.out.println("✅ Pedido creado ID: " + pedidoGuardado.getIdPedido());

            // 8️⃣ Crear detalles del pedido
            for (CarritoItem item : items) {
                DetallePedido detalle = new DetallePedido();
                detalle.setPedido(pedidoGuardado);
                detalle.setProducto(item.getProducto());
                detalle.setCantidad(item.getCantidad());
                detalle.setPrecioUnitario(item.getProducto().getPrecioProducto());
                detalle.setSubtotal(item.getProducto().getPrecioProducto() * item.getCantidad());
                detallePedidoRepository.save(detalle);
            }

            // 9️⃣ Registrar en tabla pedido_vendedor
            PedidoVendedor pv = new PedidoVendedor();
            pv.setPedido(pedidoGuardado);
            pv.setVendedor(vendedor);
            pv.setEstado(EstadoPedidoVendedor.NUEVO);
            pv.setFechaActualizacion(LocalDateTime.now());
            pedidoVendedorRepo.save(pv);

            pedidosCreados.add(pedidoGuardado);
        }

        // 🔟 Limpiar el carrito
        carritoItemRepository.deleteAll(carrito.getItems());
        System.out.println("🗑️ Carrito limpiado exitosamente");

        // 1️⃣1️⃣ Notificar al consumidor
        notificacionService.crearNotificacion(
            carrito.getConsumidor().getUsuario(),
            "🛒 Se crearon " + pedidosCreados.size() + " pedido(s) exitosamente. ID Compra: " + idCompraUnificada,
            "PEDIDO",
            pedidosCreados.get(0).getIdPedido()
        );

        System.out.println("✅ ========================================");
        System.out.println("✅ CHECKOUT COMPLETADO CON ÉXITO");
        System.out.println("✅ ID Compra: " + idCompraUnificada);
        System.out.println("✅ Pedidos creados: " + pedidosCreados.size());
        System.out.println("✅ ========================================");

        // 1️⃣2️⃣ Retornar respuesta
        return new CheckoutResponseDTO(idCompraUnificada, pedidosCreados);
    }

    // ============================================================
    // 🔥 CHECKOUT LEGACY (para compatibilidad)
    // ============================================================
    @Override
    @Transactional
    public List<Pedido> checkoutMultiVendedorLegacy(Integer idConsumidor) {
        CheckoutResponseDTO respuesta = checkoutMultiVendedor(idConsumidor);
        return respuesta.getPedidos();
    }

    // ============================================================
    // 🔥 OBTENER COMPRA UNIFICADA
    // ============================================================
    @Override
    public CompraUnificadaDTO obtenerCompraUnificada(String idCompraUnificada, Integer idConsumidor) {
        Consumidor consumidor = consumidorRepository.findById(idConsumidor)
            .orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));
        
        List<Pedido> pedidos = pedidoRepository
            .findByIdCompraUnificadaAndConsumidor_IdConsumidor(idCompraUnificada, idConsumidor);
        
        if (pedidos.isEmpty()) {
            throw new RuntimeException("Compra no encontrada o no pertenece al consumidor");
        }
        
        CompraUnificadaDTO dto = new CompraUnificadaDTO(idCompraUnificada, pedidos);
        
        if (!pedidos.isEmpty()) {
            Pedido primerPedido = pedidos.get(0);
            dto.setMetodoPago(primerPedido.getMetodoPago());
            dto.setFechaCompra(primerPedido.getFechaPedido() != null ? 
                primerPedido.getFechaPedido().toString() : "");
            
            Map<String, Object> infoPago = new HashMap<>();
            infoPago.put("estadoPago", primerPedido.getEstadoPago());
            infoPago.put("metodoPago", primerPedido.getMetodoPago());
            if (primerPedido.getComprobanteUrl() != null) {
                infoPago.put("tieneComprobante", true);
            }
            dto.setInfoPago(infoPago);
        }
        
        return dto;
    }

    // ============================================================
    // 🔥 OBTENER COMPRAS UNIFICADAS DEL CONSUMIDOR
    // ============================================================
    @Override
    public List<CompraUnificadaDTO> obtenerComprasUnificadasPorConsumidor(Integer idConsumidor) {
        List<Pedido> todosPedidos = pedidoRepository
            .findByConsumidor_IdConsumidorOrderByFechaPedidoDesc(idConsumidor);
        
        if (todosPedidos.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<String, List<Pedido>> pedidosPorCompra = todosPedidos.stream()
            .filter(p -> p.getIdCompraUnificada() != null && !p.getIdCompraUnificada().isEmpty())
            .collect(Collectors.groupingBy(Pedido::getIdCompraUnificada));
        
        if (pedidosPorCompra.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<CompraUnificadaDTO> compras = new ArrayList<>();
        
        for (Map.Entry<String, List<Pedido>> entry : pedidosPorCompra.entrySet()) {
            List<Pedido> pedidosOrdenados = entry.getValue().stream()
                .sorted((p1, p2) -> p2.getFechaPedido().compareTo(p1.getFechaPedido()))
                .collect(Collectors.toList());
            
            CompraUnificadaDTO compraDTO = new CompraUnificadaDTO(entry.getKey(), pedidosOrdenados);
            
            if (!pedidosOrdenados.isEmpty()) {
                Pedido primerPedido = pedidosOrdenados.get(0);
                compraDTO.setMetodoPago(primerPedido.getMetodoPago());
                compraDTO.setFechaCompra(primerPedido.getFechaPedido() != null ? 
                    primerPedido.getFechaPedido().toString() : "");
                
                Map<String, Object> infoPago = new HashMap<>();
                infoPago.put("estadoPago", primerPedido.getEstadoPago());
                infoPago.put("metodoPago", primerPedido.getMetodoPago());
                if (primerPedido.getComprobanteUrl() != null) {
                    infoPago.put("tieneComprobante", true);
                }
                compraDTO.setInfoPago(infoPago);
            }
            
            compras.add(compraDTO);
        }
        
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
    // MÉTODOS AUXILIARES
    // ============================================================

    private void descontarStock(Pedido pedido) {
        System.out.println("📦 Descontando stock para pedido #" + pedido.getIdPedido());
        
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            
            if (producto.getStockProducto() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para " + producto.getNombreProducto());
            }
            
            int nuevoStock = producto.getStockProducto() - detalle.getCantidad();
            System.out.println("  🔻 " + producto.getNombreProducto() + 
                             ": Stock " + producto.getStockProducto() + 
                             " → " + nuevoStock);
            
            producto.setStockProducto(nuevoStock);
            productoRepository.save(producto);
        }
    }
    
    private void devolverStock(Pedido pedido) {
        System.out.println("📦 Devolviendo stock para pedido cancelado #" + pedido.getIdPedido());
        
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            
            int nuevoStock = producto.getStockProducto() + detalle.getCantidad();
            System.out.println("  🔼 " + producto.getNombreProducto() + 
                             ": Stock " + producto.getStockProducto() + 
                             " → " + nuevoStock);
            
            producto.setStockProducto(nuevoStock);
            productoRepository.save(producto);
        }
    }
    
    private void validarPedidoEditable(Pedido pedido) {
        if (pedido.getEstadoPedido() == EstadoPedido.CANCELADO) {
            throw new RuntimeException("Este pedido está cancelado y no puede modificarse");
        }

        if (pedido.getEstadoPedido() == EstadoPedido.COMPLETADO) {
            throw new RuntimeException("Este pedido ya fue completado y no puede modificarse");
        }
    }
    
    // ============================================================
    // CREAR PEDIDO
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
		pedido.setEstadoPedido(EstadoPedido.CREADO);
		pedido.setEstadoPago(EstadoPago.PENDIENTE);
		pedido.setEstadoPedidoVendedor(EstadoPedidoVendedor.NUEVO);

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
	// OBTENER PEDIDO POR ID
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

		// 🔥 VALIDAR PAGO ANTES DE PROCESAR
		if (estadoNuevo != EstadoPedido.PENDIENTE && estadoNuevo != EstadoPedido.CANCELADO) {
			if (pedido.getEstadoPago() != EstadoPago.PAGADO && 
				!(pedido.getMetodoPago().equalsIgnoreCase("EFECTIVO") && estadoNuevo == EstadoPedido.PROCESANDO)) {
				throw new RuntimeException("No se puede procesar el pedido. El pago no está verificado. Estado: " + pedido.getEstadoPago());
			}
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
	        pedido.setEstadoPago(EstadoPago.PENDIENTE);
	        pedido.setEstadoPedidoVendedor(EstadoPedidoVendedor.NUEVO);
	        pedido.setEstadoSeguimiento(EstadoSeguimientoPedido.ESPERANDO_PAGO);

	    }
	    // ==========================
	    // TRANSFERENCIA
	    // ==========================
	    else if (metodoPago.equalsIgnoreCase("TRANSFERENCIA")) {

	        pedido.setEstadoPedido(EstadoPedido.PENDIENTE);
	        pedido.setEstadoPago(EstadoPago.EN_VERIFICACION);
	        pedido.setEstadoSeguimiento(EstadoSeguimientoPedido.ESPERANDO_PAGO);

	    }
	    // ==========================
	    // TARJETA
	    // ==========================
	    else if (metodoPago.equalsIgnoreCase("TARJETA")) {
	        throw new RuntimeException("Para pagos con tarjeta use el endpoint con parámetros completos");
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
	            "📦 Pedido #" + pedido.getIdPedido() + " listo para procesar. Método: " + metodoPago,
	            "PEDIDO",
	            pedido.getIdPedido()
	    );

	    return pedidoRepository.save(pedido);
	}

	// ============================================================
	// FINALIZAR PEDIDO (COMPLETO) - CON CLOUDINARY ✅
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

	    System.out.println("🔍 FINALIZANDO PEDIDO COMPLETO #" + idPedido + " - Método: " + metodoPago);

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
	        pedido.setEstadoPago(EstadoPago.PENDIENTE);
	        pedido.setEstadoPedidoVendedor(EstadoPedidoVendedor.NUEVO);
	        pedido.setEstadoSeguimiento(EstadoSeguimientoPedido.ESPERANDO_PAGO);

	        notificacionService.crearNotificacion(
	                pedido.getVendedor().getUsuario(),
	                "💵 Pedido #" + pedido.getIdPedido() + " en efectivo, pendiente de entrega",
	                "PEDIDO",
	                pedido.getIdPedido()
	        );
	    }

	    // ===============================
	    // TRANSFERENCIA - CON CLOUDINARY ✅
	    // ===============================
	    else if (metodoPago.equalsIgnoreCase("TRANSFERENCIA")) {

	     if (comprobante == null || comprobante.isEmpty()) {
	         throw new RuntimeException("Debe subir el comprobante de transferencia");
	     }

	     try {
	         System.out.println("📤 Subiendo comprobante a Cloudinary...");
	         
	         // ✅ VALIDAR EL COMPROBANTE
	         if (!fileStorageService.isValidComprobante(comprobante)) {
	             throw new RuntimeException("Formato de comprobante no válido. Use PDF, JPG, PNG o JPEG");
	         }
	         
	         // ✅ VALIDAR TAMAÑO
	         long fileSize = fileStorageService.getFileSize(comprobante);
	         if (fileSize > 10 * 1024 * 1024) { // 10MB
	             throw new RuntimeException("El comprobante es demasiado grande. Máximo 10MB");
	         }
	         
	         System.out.println("✅ Comprobante validado:");
	         System.out.println("   Tipo: " + comprobante.getContentType());
	         System.out.println("   Tamaño: " + (fileSize / 1024) + " KB");
	         System.out.println("   Nombre: " + comprobante.getOriginalFilename());
	         
	         // ✅ SUBIR A CLOUDINARY
	         String comprobanteUrl = fileStorageService.storeComprobante(comprobante);
	         
	         System.out.println("✅ Comprobante subido exitosamente a Cloudinary:");
	         System.out.println("   URL: " + comprobanteUrl);
	         
	         // 🔥 GUARDAR URL DE CLOUDINARY
	         pedido.setComprobanteUrl(comprobanteUrl);
	         pedido.setFechaSubidaComprobante(LocalDateTime.now());
	         pedido.setEstadoPedido(EstadoPedido.PENDIENTE);
	         pedido.setEstadoPago(EstadoPago.EN_VERIFICACION);
	         pedido.setEstadoSeguimiento(EstadoSeguimientoPedido.ESPERANDO_PAGO);

	     } catch (RuntimeException e) {
	         System.err.println("❌ Error al validar comprobante: " + e.getMessage());
	         throw e;
	     } catch (Exception e) {
	         System.err.println("❌ Error inesperado al subir comprobante: " + e.getMessage());
	         e.printStackTrace();
	         throw new RuntimeException("Error al subir comprobante: " + e.getMessage());
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

	        pedido.setEstadoPedido(EstadoPedido.PROCESANDO);
	        pedido.setEstadoPago(EstadoPago.PAGADO);
	        pedido.setEstadoPedidoVendedor(EstadoPedidoVendedor.EN_PROCESO);
	        pedido.setEstadoSeguimiento(EstadoSeguimientoPedido.RECOLECTANDO);

	        // 🔥 DESCONTAR STOCK SOLO AQUÍ
	        descontarStock(pedido);
	        
	        // 🔥 CREAR REGISTRO DE PAGO PARA TARJETA
	        PagoTarjetaRequest pagoRequest = new PagoTarjetaRequest();
	        pagoRequest.setIdPedido(idPedido);
	        pagoRequest.setIdConsumidor(pedido.getConsumidor().getIdConsumidor());
	        pagoRequest.setMonto(pedido.getTotal());
	        pagoRequest.setNumeroTarjeta(numTarjeta);
	        pagoRequest.setFechaExpiracion(fechaTarjeta);
	        pagoRequest.setCvv(cvv);
	        pagoRequest.setTitular(titular);
	        
	        try {
	            pagoService.procesarPagoTarjetaSimulado(pagoRequest);
	        } catch (Exception e) {
	            throw new RuntimeException("Error al procesar pago con tarjeta: " + e.getMessage());
	        }
	    }

	    else {
	        throw new RuntimeException("Método de pago no válido");
	    }

	    // ===============================
	    // NOTIFICACIÓN AL CONSUMIDOR
	    // ===============================
	    String mensajeNotificacion = "";
	    if ("TRANSFERENCIA".equalsIgnoreCase(metodoPago)) {
	        mensajeNotificacion = "📤 Comprobante subido exitosamente a la nube. El vendedor verificará tu pago.";
	    } else {
	        mensajeNotificacion = "💳 Tu pedido #" + pedido.getIdPedido() + " fue procesado con método: " + metodoPago;
	    }
	    
	    notificacionService.crearNotificacion(
	            pedido.getConsumidor().getUsuario(),
	            mensajeNotificacion,
	            "PEDIDO",
	            pedido.getIdPedido()
	    );
	    
	    // NOTIFICACIÓN AL VENDEDOR
	    if ("TRANSFERENCIA".equalsIgnoreCase(metodoPago)) {
	        notificacionService.crearNotificacion(
	                pedido.getVendedor().getUsuario(),
	                "📋 Nuevo comprobante subido para pedido #" + pedido.getIdPedido(),
	                "PAGO",
	                pedido.getIdPedido()
	        );
	    }

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

		pedido.setEstadoPedido(EstadoPedido.CREADO);
		pedido.setEstadoPago(EstadoPago.PENDIENTE);
		pedido.setEstadoPedidoVendedor(EstadoPedidoVendedor.NUEVO);

		pedido.setMetodoPago(null);

		pedido = pedidoRepository.save(pedido);

		double subtotal = 0;

		for (PedidoCarritoRequest.DetalleProducto item : request.getDetalles()) {

			Producto producto = productoRepository.findById(item.getIdProducto())
					.orElseThrow(() -> new RuntimeException("Producto no encontrado"));

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
		
		// 🔥 AGREGAR ESTADÍSTICAS DE PAGOS
		stats.put("pendientesVerificacion", pedidoRepository.countByVendedor_IdVendedorAndEstadoPago(
			idVendedor, EstadoPago.EN_VERIFICACION));
		stats.put("pagados", pedidoRepository.countByVendedor_IdVendedorAndEstadoPago(
			idVendedor, EstadoPago.PAGADO));
		stats.put("rechazados", pedidoRepository.countByVendedor_IdVendedorAndEstadoPago(
			idVendedor, EstadoPago.RECHAZADO));

		return stats;
	}

	// ============================================================
	// VENTAS MENSUALES
	// ============================================================
	@Override
	public List<Map<String, Object>> obtenerVentasMensuales(Integer idVendedor) {

		List<Object[]> data = pedidoRepository.obtenerVentasMensualesPagadasPorVendedor(idVendedor);
		List<Map<String, Object>> res = new ArrayList<>();

		String[] meses = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre",
				"Octubre", "Noviembre", "Diciembre" };

		for (Object[] row : data) {
			Map<String, Object> item = new HashMap<>();
			item.put("mes", meses[((Number) row[0]).intValue() - 1]);
			item.put("total", row[1]);
			item.put("estadoPago", row[2]);
			res.add(item);
		}

		return res;
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
	        pedido.getEstadoPago() != EstadoPago.PENDIENTE) {
	        throw new RuntimeException(
	            "Los pedidos en efectivo no pueden cancelarse una vez confirmados. " +
	            "Contacta al vendedor si tienes algún problema."
	        );
	    }
	    
	    // 🚨 VALIDACIÓN: No permitir cancelar si el pago ya está en verificación o pagado
	    if (pedido.getEstadoPago() == EstadoPago.EN_VERIFICACION || 
	        pedido.getEstadoPago() == EstadoPago.PAGADO) {
	        throw new RuntimeException(
	            "No se puede cancelar el pedido porque el pago ya está en proceso de verificación o fue aprobado. " +
	            "Contacta al vendedor para más información."
	        );
	    }

	    if (pedido.getEstadoPedido() != EstadoPedido.CREADO &&
	        pedido.getEstadoPedido() != EstadoPedido.PENDIENTE &&
	        pedido.getEstadoPedido() != EstadoPedido.PROCESANDO) {
	        throw new RuntimeException("Solo se pueden cancelar pedidos en estado CREADO, PENDIENTE o PROCESANDO");
	    }

	    // 1️⃣ Cancelar pedido
	    pedido.setEstadoPedido(EstadoPedido.CANCELADO);
	    pedido.setEstadoPago(EstadoPago.CANCELADO);
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
	// LISTAR PEDIDOS HISTORIAL (CON ID)
	// ============================================================
	@Override
	public List<Pedido> listarPedidosHistorial(Integer idConsumidor) {

	    Consumidor consumidor = consumidorRepository.findById(idConsumidor)
	            .orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));

	    List<Pedido> pedidos = pedidoRepository
	            .findByConsumidor_IdConsumidorOrderByFechaPedidoDesc(idConsumidor);

	    // Filtrar pedidos válidos para historial
	    pedidos.removeIf(p ->
	            p.getDetalles() == null || p.getDetalles().isEmpty() ||
	            p.getEstadoPedido() == EstadoPedido.CANCELADO ||
	            (p.getTotal() != null && p.getTotal() <= 0));

	    return pedidos;
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
	    
	    // Opcional: Ordenar por fecha de creación
	    pedidosFiltrados.sort(Comparator.comparing(Pedido::getFechaPedido).reversed());
	    
	    return pedidosFiltrados;
	}

	// ============================================================
	// CAMBIAR ESTADO DE SEGUIMIENTO
	// ============================================================
	@Override
	public Pedido cambiarEstadoSeguimiento(Integer idPedido, String nuevoEstadoSeguimiento) {

	    Pedido pedido = pedidoRepository.findById(idPedido)
	        .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

	    // 🔥 VALIDAR QUE EL PAGO ESTÉ VERIFICADO ANTES DE CAMBIAR SEGUIMIENTO
	    if (pedido.getEstadoPago() != EstadoPago.PAGADO && 
	        !("EFECTIVO".equalsIgnoreCase(pedido.getMetodoPago()) && 
	          pedido.getEstadoPago() == EstadoPago.PENDIENTE)) {
	        throw new RuntimeException("No se puede cambiar el seguimiento. El pago no está verificado.");
	    }

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

	    // 🔥 VALIDAR QUE EL PAGO ESTÉ VERIFICADO ANTES DE PROCESAR
	    Pedido pedido = pv.getPedido();
	    if (pedido.getEstadoPago() != EstadoPago.PAGADO && 
	        !("EFECTIVO".equalsIgnoreCase(pedido.getMetodoPago()) && 
	          pedido.getEstadoPago() == EstadoPago.PENDIENTE)) {
	        throw new RuntimeException("No se puede procesar el pedido. El pago no está verificado. Estado: " + pedido.getEstadoPago());
	    }

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
	        
	        // 🔥 DESCONTAR STOCK SI NO SE HA HECHO (para pagos en efectivo)
	        if ("EFECTIVO".equalsIgnoreCase(pedido.getMetodoPago())) {
	            descontarStock(pedido);
	        }
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
	// CAMBIAR ESTADO PEDIDO VENDEDOR
	// ============================================================
	@Override
	public Pedido cambiarEstadoPedidoVendedor(Integer idPedido, String nuevoEstado) {
	    Pedido pedido = pedidoRepository.findById(idPedido)
	            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
	    
	    // Convertir String a Enum
	    EstadoPedidoVendedor estado = EstadoPedidoVendedor.valueOf(nuevoEstado);
	    
	    // 🔥 VALIDAR QUE EL PAGO ESTÉ VERIFICADO ANTES DE CAMBIAR ESTADO
	    if (estado != EstadoPedidoVendedor.NUEVO) {
	        if (pedido.getEstadoPago() != EstadoPago.PAGADO && 
	            !("EFECTIVO".equalsIgnoreCase(pedido.getMetodoPago()) && 
	              pedido.getEstadoPago() == EstadoPago.PENDIENTE)) {
	            throw new RuntimeException("No se puede procesar el pedido. El pago no está verificado. Estado: " + pedido.getEstadoPago());
	        }
	    }
	    
	    // Validar transición
	    // (Aquí puedes agregar lógica de validación de transiciones)
	    
	    pedido.setEstadoPedidoVendedor(estado);
	    
	    // Si se marca como ENTREGADO, actualizar estado general
	    if (estado == EstadoPedidoVendedor.ENTREGADO) {
	        pedido.setEstadoPedido(EstadoPedido.COMPLETADO);
	        pedido.setEstadoSeguimiento(EstadoSeguimientoPedido.ENTREGADO);
	        
	        // 🔥 DESCONTAR STOCK SI NO SE HA HECHO (para pagos en efectivo)
	        if ("EFECTIVO".equalsIgnoreCase(pedido.getMetodoPago())) {
	            descontarStock(pedido);
	        }
	    }
	    
	    // Si se marca como CANCELADO, actualizar estado general
	    if (estado == EstadoPedidoVendedor.CANCELADO) {
	        pedido.setEstadoPedido(EstadoPedido.CANCELADO);
	        pedido.setEstadoPago(EstadoPago.CANCELADO);
	        
	        // 🔥 DEVOLVER STOCK SI SE HABÍA DESCONTADO
	        devolverStock(pedido);
	    }
	    
	    return pedidoRepository.save(pedido);
	}
}