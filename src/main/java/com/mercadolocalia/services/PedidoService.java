package com.mercadolocalia.services;

import com.mercadolocalia.dto.*;
import com.mercadolocalia.entities.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface PedidoService {
    
    // ============================================================
    // 🔥 MÉTODOS CHECKOUT (ACTUALIZADOS)
    // ============================================================
    
    // 🔥 VERSIÓN 1: Sin ID (genera automático)
    CheckoutResponseDTO checkoutMultiVendedor(Integer idConsumidor);
    
    // 🔥 VERSIÓN 2: Con ID proporcionado
    CheckoutResponseDTO checkoutMultiVendedor(Integer idConsumidor, String idCompraUnificada);
    
    // 🔥 VERSIÓN 3: Método principal con ID
    CheckoutResponseDTO checkoutMultiVendedorConIdCompra(Integer idConsumidor, String idCompraUnificada);
    
    // 🔥 VERSIÓN 4: Para compatibilidad (devuelve List<Pedido>)
    List<Pedido> checkoutMultiVendedorLegacy(Integer idConsumidor);
    
    // ============================================================
    // 🔥 COMPRAS UNIFICADAS
    // ============================================================
    CompraUnificadaDTO obtenerCompraUnificada(String idCompraUnificada, Integer idConsumidor);
    List<CompraUnificadaDTO> obtenerComprasUnificadasPorConsumidor(Integer idConsumidor);
    
    // ============================================================
    // MÉTODOS BÁSICOS (EXISTENTES)
    // ============================================================
    Pedido crearPedido(PedidoRequest request);
    Pedido obtenerPedidoPorId(Integer id);
    List<Pedido> listarPedidosPorConsumidor(Integer idConsumidor);
    List<Pedido> listarPedidosPorVendedor(Integer idVendedor);
    List<DetallePedido> listarDetalles(Integer idPedido);
    
    // ============================================================
    // CAMBIOS DE ESTADO
    // ============================================================
    Pedido cambiarEstado(Integer idPedido, String estado);
    Pedido cambiarEstadoSeguimiento(Integer idPedido, String estado);
    Pedido cambiarEstadoPedidoVendedor(Integer idPedido, String nuevoEstado);
    
    // ============================================================
    // PAGO
    // ============================================================
    Pedido finalizarPedido(Integer idPedido, String metodoPago);
    Pedido finalizarPedido(Integer idPedido, String metodoPago, MultipartFile comprobante,
                          String numTarjeta, String fechaTarjeta, String cvv, String titular);
    
    // ============================================================
    // CARRITO Y COMPRA
    // ============================================================
    Pedido comprarAhora(PedidoRequest request);
    Pedido crearPedidoDesdeCarrito(PedidoCarritoRequest request);
    
    // ============================================================
    // CANCELACIÓN
    // ============================================================
    Pedido cancelarPedido(Integer idPedido);
    
    // ============================================================
    // HISTORIAL
    // ============================================================
    List<Pedido> listarPedidosHistorial(Integer idConsumidor);
    List<Pedido> listarPedidosHistorial(Consumidor consumidor);
    
    // ============================================================
    // ESTADÍSTICAS Y REPORTES
    // ============================================================
    Map<String, Object> obtenerEstadisticasVendedor(Integer idVendedor);
    List<Map<String, Object>> obtenerVentasMensuales(Integer idVendedor);
    
    // ============================================================
    // DASHBOARD VENDEDOR
    // ============================================================
    List<PedidoVendedor> listarPedidosParaDashboardVendedor(Integer idVendedor);
    void actualizarEstadoOperativo(Integer idPedidoVendedor, String estado);
    
    // ============================================================
    // DETALLES ESPECÍFICOS
    // ============================================================
    List<DetallePedido> listarDetallesPorVendedor(Integer idPedido, Integer idVendedor);
}