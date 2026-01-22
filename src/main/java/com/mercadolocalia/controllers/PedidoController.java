package com.mercadolocalia.controllers;

import java.util.HashMap;
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
import com.mercadolocalia.services.impl.PedidoServiceImpl;

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
        try {
            System.out.println("🔍 ========================================");
            System.out.println("🔍 SOLICITANDO DETALLE PEDIDO VENDEDOR");
            System.out.println("🔍 ========================================");
            System.out.println("🔍 ID Pedido: " + idPedido);
            
            Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

            Vendedor vendedor = vendedorRepository.findByUsuario(usuario).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no está asociado a un vendedor"));

            Pedido pedido = pedidoRepository.findById(idPedido)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

            if (!pedido.getVendedor().getIdVendedor().equals(vendedor.getIdVendedor())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para ver este pedido");
            }

            System.out.println("✅ Pedido encontrado para vendedor: " + vendedor.getNombreEmpresa());
            System.out.println("✅ Estado pedido vendedor: " + (pedido.getEstadoPedidoVendedor() != null ? 
                pedido.getEstadoPedidoVendedor() : "NUEVO"));
            
            return ResponseEntity.ok(pedido);
            
        } catch (ResponseStatusException e) {
            System.out.println("❌ Error controlado: " + e.getReason());
            throw e;
        } catch (Exception e) {
            System.out.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error al obtener detalle del pedido");
        }
    }

    // ============================================================
    // 🔧 ACTUALIZAR ESTADO DEL PEDIDO PARA VENDEDOR (ESPECÍFICO PARA FRONTEND)
    // ============================================================
    @PutMapping("/vendedor/{idPedido}/estado")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<?> actualizarEstadoPedidoVendedor(
            @PathVariable Integer idPedido,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        try {
            System.out.println("🔍 ========================================");
            System.out.println("🔍 ACTUALIZANDO ESTADO PEDIDO VENDEDOR");
            System.out.println("🔍 ========================================");
            System.out.println("🔍 ID Pedido: " + idPedido);
            System.out.println("🔍 Request: " + request);
            
            // 1️⃣ Validar usuario autenticado
            Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

            // 2️⃣ Validar que sea vendedor
            Vendedor vendedor = vendedorRepository.findByUsuario(usuario)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no es vendedor"));

            System.out.println("✅ Vendedor encontrado: " + vendedor.getNombreEmpresa());

            // 3️⃣ Obtener el pedido
            Pedido pedido = pedidoRepository.findById(idPedido)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

            // 4️⃣ Verificar que el pedido pertenece a este vendedor
            if (!pedido.getVendedor().getIdVendedor().equals(vendedor.getIdVendedor())) {
                System.out.println("❌ El pedido no pertenece a este vendedor");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para modificar este pedido");
            }

            // 5️⃣ Obtener el nuevo estado del cuerpo de la solicitud
            String nuevoEstado = request.get("estadoPedidoVendedor");
            if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El campo 'estadoPedidoVendedor' es requerido");
            }

            System.out.println("🔍 Estado actual vendedor: " + pedido.getEstadoPedidoVendedor());
            System.out.println("🔍 Nuevo estado solicitado: " + nuevoEstado);

            // 6️⃣ Validar y actualizar el estado
            Pedido pedidoActualizado = pedidoService.cambiarEstadoPedidoVendedor(idPedido, nuevoEstado);

            System.out.println("✅ Estado actualizado exitosamente");
            System.out.println("✅ Nuevo estado: " + pedidoActualizado.getEstadoPedidoVendedor());
            
            // 7️⃣ Devolver el pedido actualizado
            return ResponseEntity.ok(pedidoActualizado);

        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error: Estado inválido - " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido: " + e.getMessage());
        } catch (ResponseStatusException e) {
            System.out.println("❌ Error controlado: " + e.getReason());
            throw e;
        } catch (Exception e) {
            System.out.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error al actualizar el estado del pedido: " + e.getMessage());
        }
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
    // CAMBIAR ESTADO DEL PEDIDO (GENERAL)
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
    // 🛒 CHECKOUT LEGACY (para mantener compatibilidad con frontend existente)
    // ============================================================
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CONSUMIDOR')")
    public ResponseEntity<List<Pedido>> checkout(
            @RequestBody CheckoutRequest request,
            Authentication authentication) {

        try {
            System.out.println("🔍 ========================================");
            System.out.println("🔍 CHECKOUT REQUEST RECIBIDO (LEGACY)");
            System.out.println("🔍 ========================================");
            System.out.println("🔍 ID Consumidor: " + request.getIdConsumidor());
            System.out.println("🔍 Usuario autenticado: " + authentication.getName());

            // 1️⃣ Validar usuario autenticado
            Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

            System.out.println("✅ Usuario encontrado: " + usuario.getCorreo());

            // 2️⃣ Validar que sea consumidor
            Consumidor consumidor = consumidorRepository.findByUsuario(usuario);

            if (consumidor == null) {
                System.out.println("❌ Usuario no es consumidor");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no está registrado como consumidor");
            }

            System.out.println("✅ Consumidor encontrado: " + consumidor.getIdConsumidor());

            // 3️⃣ Validar que coincida el ID
            if (!consumidor.getIdConsumidor().equals(request.getIdConsumidor())) {
                System.out.println("❌ ID de consumidor no coincide");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes crear pedidos para otro usuario");
            }

            // 4️⃣ 🔥 USAR MÉTODO LEGACY QUE DEVUELVE List<Pedido>
            System.out.println("🔍 Llamando a checkoutMultiVendedorLegacy...");
            
            // NECESITAS CASTEAR A PedidoServiceImpl O CAMBIAR TU INTERFACE
            List<Pedido> pedidosCreados;
            
            if (pedidoService instanceof PedidoServiceImpl) {
                // Si puedes hacer casting a la implementación
                PedidoServiceImpl pedidoServiceImpl = (PedidoServiceImpl) pedidoService;
                pedidosCreados = pedidoServiceImpl.checkoutMultiVendedorLegacy(request.getIdConsumidor());
            } else {
                // Alternativa: llamar al método nuevo y extraer los pedidos
                CheckoutResponseDTO respuesta = pedidoService.checkoutMultiVendedor(request.getIdConsumidor());
                pedidosCreados = respuesta.getPedidos();
            }

            System.out.println("✅ ========================================");
            System.out.println("✅ CHECKOUT EXITOSO");
            System.out.println("✅ Se crearon " + pedidosCreados.size() + " pedido(s)");
            System.out.println("✅ ========================================");
            
            // 5️⃣ Imprimir IDs de los pedidos creados y ID de compra unificada
            if (!pedidosCreados.isEmpty()) {
                System.out.println("🆔 ID Compra Unificada: " + pedidosCreados.get(0).getIdCompraUnificada());
            }
            
            for (int i = 0; i < pedidosCreados.size(); i++) {
                Pedido p = pedidosCreados.get(i);
                System.out.println("  📦 Pedido " + (i + 1) + ": #" + p.getIdPedido() + 
                                 " - Vendedor: " + p.getVendedor().getNombreEmpresa() +
                                 " - Total: $" + p.getTotal());
            }

            return ResponseEntity.ok(pedidosCreados);

        } catch (ResponseStatusException e) {
            System.out.println("❌ ERROR CONTROLADO: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("❌ ========================================");
            System.out.println("❌ ERROR EN CHECKOUT CONTROLLER");
            System.out.println("❌ ========================================");
            System.out.println("❌ Mensaje: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error al procesar checkout: " + e.getMessage());
        }
    }

    // ============================================================
    // 🛒 CHECKOUT COMPLETO (devuelve CheckoutResponseDTO)
    // ============================================================
    @PostMapping("/checkout-completo")
    @PreAuthorize("hasRole('CONSUMIDOR')")
    public ResponseEntity<?> checkoutCompleto(
            @RequestBody CheckoutRequest request,
            Authentication authentication) {

        try {
            System.out.println("🔍 ========================================");
            System.out.println("🔍 CHECKOUT COMPLETO SOLICITADO");
            System.out.println("🔍 ========================================");
            System.out.println("🔍 ID Consumidor: " + request.getIdConsumidor());
            System.out.println("🔍 Usuario autenticado: " + authentication.getName());

            // 1️⃣ Validar usuario autenticado
            Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

            System.out.println("✅ Usuario encontrado: " + usuario.getCorreo());

            // 2️⃣ Validar que sea consumidor
            Consumidor consumidor = consumidorRepository.findByUsuario(usuario);

            if (consumidor == null) {
                System.out.println("❌ Usuario no es consumidor");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no está registrado como consumidor");
            }

            System.out.println("✅ Consumidor encontrado: " + consumidor.getIdConsumidor());

            // 3️⃣ Validar que coincida el ID
            if (!consumidor.getIdConsumidor().equals(request.getIdConsumidor())) {
                System.out.println("❌ ID de consumidor no coincide");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes crear pedidos para otro usuario");
            }

            // 4️⃣ 🔥 LLAMAR AL MÉTODO NUEVO (devuelve CheckoutResponseDTO)
            System.out.println("🔍 Llamando a checkoutMultiVendedor (completo)...");
            CheckoutResponseDTO respuesta = pedidoService.checkoutMultiVendedor(request.getIdConsumidor());

            System.out.println("✅ ========================================");
            System.out.println("✅ CHECKOUT COMPLETO EXITOSO");
            System.out.println("✅ ========================================");
            System.out.println("✅ ID Compra Unificada: " + respuesta.getIdCompraUnificada());
            System.out.println("✅ Pedidos creados: " + respuesta.getCantidadPedidos());
            System.out.println("✅ Vendedores: " + respuesta.getCantidadVendedores());
            System.out.println("✅ Subtotal general: $" + respuesta.getSubtotalGeneral());
            System.out.println("✅ IVA general: $" + respuesta.getIvaGeneral());
            System.out.println("✅ Total general: $" + respuesta.getTotalGeneral());

            return ResponseEntity.ok(respuesta);

        } catch (ResponseStatusException e) {
            System.out.println("❌ ERROR CONTROLADO: " + e.getReason());
            throw e;
        } catch (Exception e) {
            System.out.println("❌ ========================================");
            System.out.println("❌ ERROR EN CHECKOUT COMPLETO");
            System.out.println("❌ ========================================");
            System.out.println("❌ Mensaje: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                                            "Error al procesar checkout: " + e.getMessage());
        }
    }

    // ============================================================
    // 🛍️ OBTENER COMPRA UNIFICADA POR ID (MEJORADO)
    // ============================================================
    @GetMapping("/compra-unificada/{idCompraUnificada}")
    @PreAuthorize("hasRole('CONSUMIDOR')")
    public ResponseEntity<?> obtenerCompraUnificada(
            @PathVariable String idCompraUnificada,
            Authentication authentication) {
        
        try {
            System.out.println("🔍 ========================================");
            System.out.println("🔍 SOLICITUD COMPRA UNIFICADA");
            System.out.println("🔍 ========================================");
            System.out.println("🔍 ID Compra Unificada: " + idCompraUnificada);
            
            // 1️⃣ Validar usuario autenticado
            Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

            System.out.println("✅ Usuario encontrado: " + usuario.getCorreo());

            // 2️⃣ Validar que sea consumidor
            Consumidor consumidor = consumidorRepository.findByUsuario(usuario);
            if (consumidor == null) {
                System.out.println("❌ Usuario no es consumidor");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no está registrado como consumidor");
            }

            System.out.println("✅ Consumidor encontrado: " + consumidor.getIdConsumidor());

            // 3️⃣ 🔥 OBTENER LA COMPRA UNIFICADA usando el servicio
            System.out.println("🔍 Buscando compra unificada: " + idCompraUnificada);
            CompraUnificadaDTO compra = pedidoService.obtenerCompraUnificada(idCompraUnificada, consumidor.getIdConsumidor());
            
            System.out.println("✅ Pedidos encontrados: " + compra.getCantidadPedidos());
            
            // 4️⃣ Validar que se encontró la compra
            if (compra.getPedidos() == null || compra.getPedidos().isEmpty()) {
                System.out.println("❌ No se encontraron pedidos para esta compra unificada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "error", "Compra no encontrada",
                        "idCompraUnificada", idCompraUnificada,
                        "mensaje", "No tienes acceso a esta compra o no existe"
                    ));
            }
            
            System.out.println("✅ ========================================");
            System.out.println("✅ COMPRA UNIFICADA ENCONTRADA");
            System.out.println("✅ ========================================");
            System.out.println("✅ ID Compra: " + compra.getIdCompraUnificada());
            System.out.println("✅ Total pedidos: " + compra.getCantidadPedidos());
            System.out.println("✅ Vendedores: " + compra.getCantidadVendedores());
            System.out.println("✅ Total general: $" + compra.getTotalGeneral());
            System.out.println("✅ ========================================");
            
            return ResponseEntity.ok(compra);
            
        } catch (ResponseStatusException e) {
            System.out.println("❌ ERROR CONTROLADO: " + e.getReason());
            throw e;
        } catch (Exception e) {
            System.out.println("❌ ========================================");
            System.out.println("❌ ERROR EN COMPRA UNIFICADA");
            System.out.println("❌ ========================================");
            System.out.println("❌ Mensaje: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener la compra unificada");
            errorResponse.put("detalle", e.getMessage());
            errorResponse.put("idCompraUnificada", idCompraUnificada);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================
    // 📋 LISTAR COMPRAS UNIFICADAS DEL CONSUMIDOR
    // ============================================================
    @GetMapping("/mis-compras-unificadas")
    @PreAuthorize("hasRole('CONSUMIDOR')")
    public ResponseEntity<?> obtenerMisComprasUnificadas(Authentication authentication) {
        
        try {
            System.out.println("🔍 ========================================");
            System.out.println("🔍 SOLICITANDO COMPRAS UNIFICADAS DEL USUARIO");
            System.out.println("🔍 ========================================");
            
            // 1️⃣ Validar usuario autenticado
            Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

            System.out.println("✅ Usuario encontrado: " + usuario.getCorreo());

            // 2️⃣ Validar que sea consumidor
            Consumidor consumidor = consumidorRepository.findByUsuario(usuario);
            if (consumidor == null) {
                System.out.println("❌ Usuario no es consumidor");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no está registrado como consumidor");
            }

            System.out.println("✅ Consumidor encontrado: " + consumidor.getIdConsumidor());

            // 3️⃣ 🔥 OBTENER LAS COMPRAS UNIFICADAS
            System.out.println("🔍 Buscando compras unificadas del consumidor...");
            List<CompraUnificadaDTO> compras = pedidoService.obtenerComprasUnificadasPorConsumidor(consumidor.getIdConsumidor());
            
            System.out.println("✅ Compras unificadas encontradas: " + compras.size());
            
            if (compras.isEmpty()) {
                System.out.println("ℹ️ El consumidor no tiene compras unificadas");
                return ResponseEntity.ok(List.of());
            }
            
            // 4️⃣ Mostrar resumen
            for (int i = 0; i < compras.size(); i++) {
                CompraUnificadaDTO compra = compras.get(i);
                System.out.println("  🛍️ Compra " + (i + 1) + ": " + compra.getIdCompraUnificada() + 
                                 " - " + compra.getCantidadPedidos() + " pedidos - $" + compra.getTotalGeneral());
            }
            
            return ResponseEntity.ok(compras);
            
        } catch (ResponseStatusException e) {
            System.out.println("❌ ERROR CONTROLADO: " + e.getReason());
            throw e;
        } catch (Exception e) {
            System.out.println("❌ ========================================");
            System.out.println("❌ ERROR OBTENIENDO COMPRAS UNIFICADAS");
            System.out.println("❌ ========================================");
            System.out.println("❌ Mensaje: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener las compras unificadas");
            errorResponse.put("detalle", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
    // 📦 MIS PEDIDOS (HISTORIAL CONSUMIDOR) - INCLUYE COMPRAS UNIFICADAS
    // ============================================================
    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasRole('CONSUMIDOR')")
    public ResponseEntity<?> misPedidos(Authentication authentication) {
        try {
            System.out.println("🔍 ========================================");
            System.out.println("🔍 SOLICITANDO HISTORIAL DE PEDIDOS");
            System.out.println("🔍 ========================================");
            
            Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

            Consumidor consumidor = consumidorRepository.findByUsuario(usuario);

            if (consumidor == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Consumidor no encontrado");
            }

            System.out.println("✅ Consumidor encontrado: " + consumidor.getIdConsumidor());

            // 🔥 Obtener tanto pedidos individuales como compras unificadas
            List<Pedido> pedidosIndividuales = pedidoService.listarPedidosHistorial(consumidor.getIdConsumidor());
            List<CompraUnificadaDTO> comprasUnificadas = pedidoService.obtenerComprasUnificadasPorConsumidor(consumidor.getIdConsumidor());
            
            System.out.println("✅ Pedidos individuales: " + pedidosIndividuales.size());
            System.out.println("✅ Compras unificadas: " + comprasUnificadas.size());

            // Crear respuesta combinada
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("pedidosIndividuales", pedidosIndividuales.stream().map(PedidoMapper::toResponse).toList());
            respuesta.put("comprasUnificadas", comprasUnificadas);
            respuesta.put("totalPedidos", pedidosIndividuales.size());
            respuesta.put("totalComprasUnificadas", comprasUnificadas.size());
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            System.out.println("❌ ERROR OBTENIENDO HISTORIAL: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error al obtener el historial de pedidos: " + e.getMessage());
        }
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

    // ============================================================
    // ✅ MARCAR PEDIDO COMO ENTREGADO
    // ============================================================
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
            pedido.setEstadoPedido(EstadoPedido.COMPLETADO);
            
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
                "pedido", pedido,
                "idCompraUnificada", pedido.getIdCompraUnificada()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================================
    // 🔥 NUEVO: OBTENER TODOS LOS PEDIDOS DE UNA COMPRA UNIFICADA (PARA VENDEDORES)
    // ============================================================
    @GetMapping("/vendedor/compra/{idCompraUnificada}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<?> obtenerPedidosDeCompraParaVendedor(
            @PathVariable String idCompraUnificada,
            Authentication authentication) {
        
        try {
            System.out.println("🔍 ========================================");
            System.out.println("🔍 VENDEDOR SOLICITANDO PEDIDOS DE COMPRA");
            System.out.println("🔍 ========================================");
            System.out.println("🔍 ID Compra Unificada: " + idCompraUnificada);
            
            // 1️⃣ Validar usuario autenticado
            Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

            // 2️⃣ Validar que sea vendedor
            Vendedor vendedor = vendedorRepository.findByUsuario(usuario)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no es vendedor"));

            System.out.println("✅ Vendedor encontrado: " + vendedor.getNombreEmpresa());

            // 3️⃣ Obtener pedidos de esta compra unificada para este vendedor específico
            List<Pedido> pedidos = pedidoRepository.findByIdCompraUnificada(idCompraUnificada);
            
            // Filtrar solo los pedidos que pertenecen a este vendedor
            List<Pedido> pedidosDelVendedor = pedidos.stream()
                    .filter(p -> p.getVendedor() != null && 
                               p.getVendedor().getIdVendedor().equals(vendedor.getIdVendedor()))
                    .toList();
            
            if (pedidosDelVendedor.isEmpty()) {
                System.out.println("❌ No se encontraron pedidos para este vendedor en esta compra");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No tienes pedidos en esta compra unificada"));
            }
            
            System.out.println("✅ Pedidos encontrados para el vendedor: " + pedidosDelVendedor.size());
            
            // 4️⃣ Preparar respuesta
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("idCompraUnificada", idCompraUnificada);
            respuesta.put("pedidos", pedidosDelVendedor);
            respuesta.put("totalVendedor", pedidosDelVendedor.stream()
                    .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0)
                    .sum());
            respuesta.put("cantidadPedidos", pedidosDelVendedor.size());
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener pedidos de la compra: " + e.getMessage()));
        }
    }
}