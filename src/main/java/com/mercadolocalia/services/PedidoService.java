package com.mercadolocalia.services;

import com.mercadolocalia.dto.*;
import com.mercadolocalia.entities.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface PedidoService {
    
    // Métodos básicos (existentes)
    Pedido crearPedido(PedidoRequest request);
    Pedido obtenerPedidoPorId(Integer id);
    List<Pedido> listarPedidosPorConsumidor(Integer idConsumidor);
    List<Pedido> listarPedidosPorVendedor(Integer idVendedor);
    List<DetallePedido> listarDetalles(Integer idPedido);
    
    // Cambios de estado
    Pedido cambiarEstado(Integer idPedido, String estado);
    Pedido cambiarEstadoSeguimiento(Integer idPedido, String estado);
    Pedido cambiarEstadoPedidoVendedor(Integer idPedido, String nuevoEstado);
    
    // Pago
    Pedido finalizarPedido(Integer idPedido, String metodoPago);
    Pedido finalizarPedido(Integer idPedido, String metodoPago, MultipartFile comprobante,
                          String numTarjeta, String fechaTarjeta, String cvv, String titular);
    
    // Carrito y checkout
    Pedido comprarAhora(PedidoRequest request);
    Pedido crearPedidoDesdeCarrito(PedidoCarritoRequest request);
    
    // 🔥 MODIFICADO: Checkout devuelve DTO en lugar de List<Pedido>
    CheckoutResponseDTO checkoutMultiVendedor(Integer idConsumidor);
    
    // 🔥 NUEVO: Método para compatibilidad con frontend existente
    List<Pedido> checkoutMultiVendedorLegacy(Integer idConsumidor);
    
    // 🔥 NUEVO: Obtener compra unificada
    CompraUnificadaDTO obtenerCompraUnificada(String idCompraUnificada, Integer idConsumidor);
    
    // 🔥 NUEVO: Listar compras unificadas del consumidor
    List<CompraUnificadaDTO> obtenerComprasUnificadasPorConsumidor(Integer idConsumidor);
    
    // Cancelación
    Pedido cancelarPedido(Integer idPedido);
    
    // Historial
    List<Pedido> listarPedidosHistorial(Integer idConsumidor);
    List<Pedido> listarPedidosHistorial(Consumidor consumidor);
    
    // Estadísticas y reportes
    Map<String, Object> obtenerEstadisticasVendedor(Integer idVendedor);
    List<Map<String, Object>> obtenerVentasMensuales(Integer idVendedor);
    
    // Dashboard vendedor (PedidoVendedor)
    List<PedidoVendedor> listarPedidosParaDashboardVendedor(Integer idVendedor);
    void actualizarEstadoOperativo(Integer idPedidoVendedor, String estado);
    
    // Detalles específicos por vendedor
    List<DetallePedido> listarDetallesPorVendedor(Integer idPedido, Integer idVendedor);
}