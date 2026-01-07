package com.mercadolocalia.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mercadolocalia.dto.CheckoutRequest;
import com.mercadolocalia.dto.PedidoCarritoRequest;
import com.mercadolocalia.dto.PedidoDTO;
import com.mercadolocalia.dto.PedidoRequest;
import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.Consumidor;
import com.mercadolocalia.entities.DetallePedido;
import com.mercadolocalia.entities.EstadoPedido;
import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.repositories.ConsumidorRepository;
import com.mercadolocalia.repositories.PedidoRepository;
import com.mercadolocalia.repositories.UsuarioRepository;
import com.mercadolocalia.repositories.VendedorRepository;
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
		// Usuario autenticado
		Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

		// 🔑 Obtener vendedor asociado a ese usuario
		Vendedor vendedor = vendedorRepository.findByUsuario(usuario).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no está asociado a un vendedor"));

		Pedido pedido = pedidoRepository.findById(idPedido)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

		// 🔒 VALIDACIÓN CLAVE
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
	// FINALIZAR COMPRA SIMPLE
	// ============================================================
	
	@PutMapping("/finalizar/{idPedido}")
	public Pedido finalizarPedidoSimple(@PathVariable Integer idPedido, @RequestParam String metodoPago) {

		return pedidoService.finalizarPedido(idPedido, metodoPago);
	}

	// ============================================================
	// FINALIZAR COMPRA COMPLETA
	// ============================================================
	
	@PostMapping(value = "/finalizar/{idPedido}", consumes = "multipart/form-data")
	public Pedido finalizarPedidoCompleto(@PathVariable Integer idPedido, @RequestParam String metodoPago,
			@RequestParam(required = false) MultipartFile comprobante,
			@RequestParam(required = false) String numTarjeta, @RequestParam(required = false) String fechaTarjeta,
			@RequestParam(required = false) String cvv, @RequestParam(required = false) String titular) {

		return pedidoService.finalizarPedido(idPedido, metodoPago, comprobante, numTarjeta, fechaTarjeta, cvv, titular);
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

		// 🔐 VALIDACIÓN CLAVE
		if (!pedido.getConsumidor().getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para ver este pedido");
		}

		return ResponseEntity.ok(pedido);
	}

	@PostMapping("/checkout")
	@PreAuthorize("hasRole('CONSUMIDOR')")
	public ResponseEntity<List<PedidoDTO>> checkout(
	        @RequestBody CheckoutRequest request,
	        Authentication authentication
	) {
	    // Obtener usuario autenticado por email
	    Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
	            .orElseThrow(() -> new ResponseStatusException(
	                HttpStatus.UNAUTHORIZED, "Usuario no autenticado"
	            ));

	    // Buscar el consumidor asociado a ese usuario

	    Consumidor consumidor = consumidorRepository.findByUsuario(usuario);

	    if (consumidor == null) {
	        throw new ResponseStatusException(
	            HttpStatus.FORBIDDEN, 
	            "El usuario no está registrado como consumidor"
	        );
	    }

	    // Validar que el idConsumidor del request sea el mismo del usuario autenticado
	    if (!consumidor.getIdConsumidor().equals(request.getIdConsumidor())) {
	        throw new ResponseStatusException(
	            HttpStatus.FORBIDDEN, 
	            "No puedes crear pedidos para otro usuario"
	        );
	    }

	    // Procesar checkout
	    List<Pedido> pedidos = pedidoService.checkoutMultiVendedor(request.getIdConsumidor());

	    List<PedidoDTO> response = pedidos.stream()
	            .map(this::mapToDTO)
	            .toList();

	    return ResponseEntity.ok(response);
	}

	
	private PedidoDTO mapToDTO(Pedido pedido) {

	    PedidoDTO dto = new PedidoDTO();

	    dto.setIdPedido(pedido.getIdPedido());
	    dto.setNumeroPedido("PED-" + pedido.getIdPedido());

	    // ✅ ENUM → String
	    dto.setEstado(pedido.getEstadoPedido().name());

	    dto.setSubtotal(pedido.getSubtotal());
	    dto.setIva(pedido.getIva());
	    dto.setTotal(pedido.getTotal());
	    dto.setFechaPedido(pedido.getFechaPedido());

	    // ================= CLIENTE =================
	    if (pedido.getConsumidor() != null &&
	        pedido.getConsumidor().getUsuario() != null) {

	        dto.setClienteNombre(
	            pedido.getConsumidor().getUsuario().getNombre() + " " +
	            pedido.getConsumidor().getUsuario().getApellido()
	        );
	    } else {
	        dto.setClienteNombre("Cliente");
	    }

	    // ================= VENDEDOR =================
	    if (pedido.getVendedor() != null &&
	        pedido.getVendedor().getUsuario() != null) {

	        dto.setVendedorNombre(
	            pedido.getVendedor().getUsuario().getNombre()
	        );
	    } else {
	        dto.setVendedorNombre("Vendedor");
	    }

	    return dto;
	}


	@PutMapping("/{id}/cancelar")
	public ResponseEntity<?> cancelarPedido(@PathVariable Integer id) {

	    Pedido pedido = pedidoRepository.findById(id)
	        .orElseThrow(() -> new RuntimeException("Pedido no existe"));

	    if (pedido.getEstadoPedido() == EstadoPedido.PENDIENTE) {
	        pedido.setEstadoPedido(EstadoPedido.CANCELADO);
	        pedidoRepository.save(pedido);
	    }

	    return ResponseEntity.ok().build();
	}

	

}
