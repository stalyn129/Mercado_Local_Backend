package com.mercadolocalia.services;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.mercadolocalia.dto.PedidoCarritoRequest;
import com.mercadolocalia.dto.PedidoRequest;
import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.DetallePedido;

public interface PedidoService {

    Pedido crearPedido(PedidoRequest request);

    Pedido obtenerPedidoPorId(Integer id);

    List<Pedido> listarPedidosPorConsumidor(Integer idConsumidor);

    List<Pedido> listarPedidosPorVendedor(Integer idVendedor);

    List<DetallePedido> listarDetalles(Integer idPedido);

    Pedido cambiarEstado(Integer idPedido, String nuevoEstado);
    
    Pedido cambiarEstadoSeguimiento(Integer idPedido, String nuevoEstadoSeguimiento);

    Pedido comprarAhora(PedidoRequest request);

    Pedido finalizarPedido(Integer idPedido, String metodoPago);

    Pedido finalizarPedido(
        Integer idPedido,
        String metodoPago,
        MultipartFile comprobante,
        String numTarjeta,
        String fechaTarjeta,
        String cvv,
        String titular
    );

    Pedido crearPedidoDesdeCarrito(PedidoCarritoRequest request);

    Map<String, Object> obtenerEstadisticasVendedor(Integer idVendedor);

    List<Map<String, Object>> obtenerVentasMensuales(Integer idVendedor);
    
    List<Pedido> checkoutMultiVendedor(Integer idConsumidor);
    
    Pedido cancelarPedido(Integer idPedido);

    List<Pedido> listarPedidosHistorial(Integer idConsumidor);

    
    
}
