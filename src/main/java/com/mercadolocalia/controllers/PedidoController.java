package com.mercadolocalia.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mercadolocalia.dto.*;
import com.mercadolocalia.entities.*;
import com.mercadolocalia.mappers.PedidoMapper;
import com.mercadolocalia.repositories.*;
import com.mercadolocalia.services.PedidoService;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

	@Autowired
	private PedidoService pedidoService;

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private VendedorRepository vendedorRepository;

	@Autowired
	private ConsumidorRepository consumidorRepository;

	// ============================================================
	// CREAR PEDIDO COMPLETO
	// ============================================================
	@PostMapping("/crear")
	public Pedido crearPedido(@RequestBody PedidoRequest request) {
		return pedidoService.crearPedido(request);
	}

	// ============================================================
	// 🔒 DETALLE DE PEDIDO (VENDEDOR)
	// ============================================================
	@GetMapping("/vendedor/detalle/{idPedido}")
	@PreAuthorize("hasRole('VENDEDOR')")
	public ResponseEntity<?> obtenerPedidoVendedor(@PathVariable Integer idPedido, Authentication authentication) {
		Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

		Vendedor vendedor = vendedorRepository.findByUsuario(usuario).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no está asociado a un vendedor"));

		Pedido pedido = pedidoRepository.findById(idPedido)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

		if (!pedido.getVendedor().getIdVendedor().equals(vendedor.getIdVendedor())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para ver este pedido");
		}

		return ResponseEntity.ok(pedido);
	}

	// ============================================================
	// LISTAR PEDIDOS POR CONSUMIDOR
	// ============================================================
	@GetMapping("/consumidor/{idConsumidor}")
	public List<Pedido> listarPorConsumidor(@PathVariable Integer idConsumidor) {
		return pedidoService.listarPedidosPorConsumidor(idConsumidor);
	}

	// ============================================================
	// LISTAR PEDIDOS POR VENDEDOR
	// ============================================================
	@GetMapping("/vendedor/{idVendedor}")
	public List<Pedido> listarPorVendedor(@PathVariable Integer idVendedor) {
		return pedidoService.listarPedidosPorVendedor(idVendedor);
	}

	// ============================================================
	// LISTAR DETALLES DE UN PEDIDO
	// ============================================================
	@GetMapping("/{idPedido}/detalles")
	public List<DetallePedido> listarDetalles(@PathVariable Integer idPedido) {
		return pedidoService.listarDetalles(idPedido);
	}

	// ============================================================
	// CAMBIAR ESTADO DEL PEDIDO (VENDEDOR)
	// ============================================================
	@PutMapping("/estado/{idPedido}")
	@PreAuthorize("hasRole('VENDEDOR')")
	public Pedido cambiarEstado(@PathVariable Integer idPedido, @RequestParam String estado) {
		return pedidoService.cambiarEstado(idPedido, estado);
	}

	// ============================================================
	// COMPRAR AHORA
	// ============================================================
	@PostMapping("/comprar-ahora")
	public Pedido comprarAhora(@RequestBody PedidoRequest request) {
		return pedidoService.comprarAhora(request);
	}

	// ============================================================
	// FINALIZAR COMPRA CON EFECTIVO (JSON)
	// ============================================================
	@PutMapping(value = "/finalizar/{idPedido}", consumes = "application/json")
	@PreAuthorize("hasRole('CONSUMIDOR')")
	public Pedido finalizarPedidoEfectivo(
	    @PathVariable Integer idPedido, 
	    @RequestBody Map<String, Object> body
	) {
	    String metodoPago = (String) body.get("metodoPago");
	    return pedidoService.finalizarPedido(idPedido, metodoPago);
	}

	// ============================================================
	// FINALIZAR COMPRA CON TRANSFERENCIA/TARJETA (MULTIPART)
	// ============================================================
	@PutMapping(value = "/finalizar/{idPedido}", consumes = "multipart/form-data")
	@PreAuthorize("hasRole('CONSUMIDOR')")
	public Pedido finalizarPedidoConArchivo(
	    @PathVariable Integer idPedido, 
	    @RequestParam String metodoPago,
	    @RequestParam(required = false) MultipartFile comprobante,
	    @RequestParam(required = false) String numTarjeta, 
	    @RequestParam(required = false) String fechaTarjeta,
	    @RequestParam(required = false) String cvv, 
	    @RequestParam(required = false) String titular
	) {
	    return pedidoService.finalizarPedido(
	        idPedido, 
	        metodoPago, 
	        comprobante, 
	        numTarjeta, 
	        fechaTarjeta, 
	        cvv, 
	        titular
	    );
	}
	
	// ============================================================
	// CREAR PEDIDO DESDE CARRITO
	// ============================================================
	@PostMapping("/carrito")
	public Pedido crearDesdeCarrito(@RequestBody PedidoCarritoRequest request) {
		return pedidoService.crearPedidoDesdeCarrito(request);
	}

	// ============================================================
	// 📊 ESTADÍSTICAS VENDEDOR
	// ============================================================
	@GetMapping("/estadisticas/vendedor/{idVendedor}")
	public ResponseEntity<?> obtenerEstadisticasVendedor(@PathVariable Integer idVendedor) {
		return ResponseEntity.ok(pedidoService.obtenerEstadisticasVendedor(idVendedor));
	}

	// ============================================================
	// 📈 VENTAS MENSUALES
	// ============================================================
	@GetMapping("/estadisticas/mensuales/{idVendedor}")
	public ResponseEntity<?> ventasMensuales(@PathVariable Integer idVendedor) {
		return ResponseEntity.ok(pedidoService.obtenerVentasMensuales(idVendedor));
	}

	// ============================================================
	// 🔒 DETALLE DE PEDIDO (CONSUMIDOR)
	// ============================================================
	@GetMapping("/{idPedido}")
	@PreAuthorize("hasRole('CONSUMIDOR')")
	public ResponseEntity<?> obtenerPedidoConsumidor(@PathVariable Integer idPedido, Authentication authentication) {
		Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

		Pedido pedido = pedidoRepository.findById(idPedido)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

		if (!pedido.getConsumidor().getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para ver este pedido");
		}

		return ResponseEntity.ok(pedido);
	}

	// ============================================================
	// 🛒 PROCESAR CHECKOUT UNIFICADO (UN SOLO PEDIDO)
	// ============================================================
	@PostMapping("/checkout")
	@PreAuthorize("hasRole('CONSUMIDOR')")
	public ResponseEntity<Pedido> checkout(
	        @RequestBody CheckoutRequest request,
	        Authentication authentication) {
	    
	    System.out.println("🔍 CHECKOUT REQUEST RECIBIDO");
	    System.out.println("🔍 ID Consumidor: " + request.getIdConsumidor());
	    System.out.println("🔍 Usuario autenticado: " + authentication.getName());
	    
	    try {
	        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
	                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

	        System.out.println("✅ Usuario encontrado: " + usuario.getCorreo());

	        Consumidor consumidor = consumidorRepository.findByUsuario(usuario);

	        if (consumidor == null) {
	            System.out.println("❌ Usuario no es consumidor");
	            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no está registrado como consumidor");
	        }

	        System.out.println("✅ Consumidor encontrado: " + consumidor.getIdConsumidor());

	        if (!consumidor.getIdConsumidor().equals(request.getIdConsumidor())) {
	            System.out.println("❌ ID de consumidor no coincide");
	            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes crear pedidos para otro usuario");
	        }

	        // 🔥 Llamar al nuevo método
	        System.out.println("🔍 Llamando a checkoutUnificado...");
	        Pedido pedidoUnico = pedidoService.checkoutUnificado(request.getIdConsumidor());

	        System.out.println("✅ Pedido creado exitosamente: " + pedidoUnico.getIdPedido());

	        return ResponseEntity.ok(pedidoUnico);
	        
	    } catch (Exception e) {
	        System.out.println("❌ ERROR EN CHECKOUT CONTROLLER: " + e.getMessage());
	        e.printStackTrace();
	        throw e;
	    }
	}
	// ============================================================
	// ❌ CANCELAR PEDIDO
	// ============================================================
	@PutMapping("/{idPedido}/cancelar")
	@PreAuthorize("hasRole('CONSUMIDOR')")
	public ResponseEntity<?> cancelarPedido(@PathVariable Integer idPedido, Authentication authentication) {
		Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));

		Pedido pedido = pedidoRepository.findById(idPedido)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no existe"));

		if (!pedido.getConsumidor().getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		return ResponseEntity.ok(pedidoService.cancelarPedido(idPedido));
	}

	// ============================================================
	// 📦 MIS PEDIDOS (HISTORIAL CONSUMIDOR)
	// ============================================================
	@GetMapping("/mis-pedidos")
	@PreAuthorize("hasRole('CONSUMIDOR')")
	public List<PedidoResponse> misPedidos(Authentication authentication) {
		Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

		Consumidor consumidor = consumidorRepository.findByUsuario(usuario);

		if (consumidor == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Consumidor no encontrado");
		}

		return pedidoService.listarPedidosHistorial(consumidor.getIdConsumidor()).stream().map(PedidoMapper::toResponse)
				.toList();
	}

	// ============================================================
	// 🧾 FACTURA (SOLO DISPONIBLE TRAS EL PAGO)
	// ============================================================
	@GetMapping("/{idPedido}/factura")
	@PreAuthorize("hasRole('CONSUMIDOR')")
	public Pedido obtenerFactura(@PathVariable Integer idPedido, Authentication authentication) {
		Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

		Pedido pedido = pedidoRepository.findById(idPedido)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

		if (!pedido.getConsumidor().getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
		}

		if (pedido.getEstadoPedido() == EstadoPedido.CREADO || pedido.getEstadoPedido() == EstadoPedido.PENDIENTE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La factura aún no está disponible");
		}

		return pedido;
	}

	// ============================================================
	// 📍 ACTUALIZAR ESTADO DE SEGUIMIENTO
	// ============================================================
	@PatchMapping("/{id}/estado-seguimiento")
	public ResponseEntity<Pedido> cambiarEstadoSeguimiento(@PathVariable Integer id, @RequestParam String estado) {
		return ResponseEntity.ok(pedidoService.cambiarEstadoSeguimiento(id, estado));
	}

	// ============================================================
	// 🏪 LISTAR PEDIDOS PARA DASHBOARD VENDEDOR (TABLA PUENTE)
	// ============================================================
	@GetMapping("/vendedor/dashboard/{idVendedor}")
	@PreAuthorize("hasRole('VENDEDOR')")
	public ResponseEntity<List<PedidoVendedor>> listarPedidosDashboard(@PathVariable Integer idVendedor) {
		List<PedidoVendedor> pedidos = pedidoService.listarPedidosParaDashboardVendedor(idVendedor);
		return ResponseEntity.ok(pedidos);
	}

	// ============================================================
	// 🔘 CAMBIAR ESTADO OPERATIVO DEL VENDEDOR (BOTONES DASHBOARD)
	// ============================================================
	@PutMapping("/operativo/{idPedidoVendedor}/estado")
	@PreAuthorize("hasRole('VENDEDOR')")
	public ResponseEntity<?> actualizarEstadoVendedor(@PathVariable Integer idPedidoVendedor,
			@RequestParam String nuevoEstado) {
		try {
			pedidoService.actualizarEstadoOperativo(idPedidoVendedor, nuevoEstado);
			return ResponseEntity.ok().body("{\"message\": \"Estado del vendedor actualizado\"}");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	// ============================================================
	// 📋 DETALLES ESPECÍFICOS DE PRODUCTOS POR VENDEDOR
	// ============================================================
	@GetMapping("/vendedor/{idVendedor}/pedido/{idPedido}/detalles")
	@PreAuthorize("hasRole('VENDEDOR')")
	public ResponseEntity<List<DetallePedido>> obtenerDetallesEspecificos(@PathVariable Integer idVendedor,
			@PathVariable Integer idPedido) {

		List<DetallePedido> detalles = pedidoService.listarDetallesPorVendedor(idPedido, idVendedor);
		return ResponseEntity.ok(detalles);
	}

	
	@PutMapping("/{idPedido}/marcar-entregado")
	@PreAuthorize("hasAnyRole('VENDEDOR', 'REPARTIDOR')")
	public ResponseEntity<?> marcarComoEntregado(
	        @PathVariable Integer idPedido,
	        @RequestBody Map<String, Boolean> request,
	        Authentication authentication) {
	    
	    try {
	        Pedido pedido = pedidoRepository.findById(idPedido)
	                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

	        // Marcar como COMPLETADO
	        pedido.setEstadoPedido(EstadoPedido.PENDIENTE);
	        
	        // Marcar como ENTREGADO (seguimiento)
	        pedido.setEstadoSeguimiento(EstadoSeguimientoPedido.ENTREGADO);
	        
	        // Si el método de pago es EFECTIVO, marcar como pagado
	        Boolean pagadoEnEfectivo = request.getOrDefault("pagado", false);
	        if ("EFECTIVO".equalsIgnoreCase(pedido.getMetodoPago()) && pagadoEnEfectivo) {
	            pedido.setPagado(true);
	        }
	        
	        pedidoRepository.save(pedido);

	        return ResponseEntity.ok(Map.of(
	            "mensaje", "Pedido marcado como entregado",
	            "pedido", pedido
	        ));
	        
	    } catch (Exception e) {
	        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
	    }
	}
}