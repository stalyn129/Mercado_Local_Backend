package com.mercadolocalia.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.DetallePedidoAddRequest;
import com.mercadolocalia.dto.PedidoRequest;
import com.mercadolocalia.entities.Consumidor;
import com.mercadolocalia.entities.DetallePedido;
import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.repositories.ConsumidorRepository;
import com.mercadolocalia.repositories.DetallePedidoRepository;
import com.mercadolocalia.repositories.PedidoRepository;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.repositories.VendedorRepository;
import com.mercadolocalia.services.PedidoService;

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
   


    // ============================================================
    // CREAR PEDIDO COMPLETO (CARRITO)
    // ============================================================
    @Override
    public Pedido crearPedido(PedidoRequest request) {

        Consumidor consumidor = consumidorRepository.findById(request.getIdConsumidor())
                .orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));

        Vendedor vendedor = vendedorRepository.findById(request.getIdVendedor())
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        Pedido pedido = new Pedido();
        pedido.setConsumidor(consumidor);
        pedido.setVendedor(vendedor);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstadoPedido("Pendiente");
        pedido.setMetodoPago(request.getMetodoPago());

        pedido.setSubtotal(0.0);
        pedido.setIva(0.0);
        pedido.setTotal(0.0);

        pedidoRepository.save(pedido);

        double subtotal = 0;

        List<DetallePedido> detalles = new ArrayList<>();

        for (DetallePedidoAddRequest detRequest : request.getDetalles()) {

            Producto producto = productoRepository.findById(detRequest.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (producto.getStockProducto() < detRequest.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombreProducto());
            }

            producto.setStockProducto(producto.getStockProducto() - detRequest.getCantidad());
            productoRepository.save(producto);

            double precioUnitario = producto.getPrecioProducto();
            double subtotalDetalle = precioUnitario * detRequest.getCantidad();

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(detRequest.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotalDetalle);

            detallePedidoRepository.save(detalle);

            detalles.add(detalle);
            subtotal += subtotalDetalle;
        }

        double iva = subtotal * 0.12;
        double total = subtotal + iva;

        pedido.setSubtotal(subtotal);
        pedido.setIva(iva);
        pedido.setTotal(total);

        pedidoRepository.save(pedido);

        return pedido;
    }

    // ============================================================
    // OBTENER POR ID
    // ============================================================
    @Override
    public Pedido obtenerPedidoPorId(Integer id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }

    // ============================================================
    // LISTAR POR CONSUMIDOR
    // ============================================================
    @Override
    public List<Pedido> listarPedidosPorConsumidor(Integer idConsumidor) {

        Consumidor consumidor = consumidorRepository.findById(idConsumidor)
                .orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));

        return pedidoRepository.findByConsumidor(consumidor);
    }

    // ============================================================
    // LISTAR POR VENDEDOR
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
    // CAMBIAR ESTADO
    // ============================================================
    @Override
    public Pedido cambiarEstado(Integer idPedido, String nuevoEstado) {

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setEstadoPedido(nuevoEstado);
        pedidoRepository.save(pedido);

        return pedido;
    }
    
    
    // ============================================================
    // COMPRAR AHORA (1 SOLO PRODUCTO)
    // ============================================================
    @Override
    public Pedido comprarAhora(PedidoRequest request) {

        Consumidor consumidor = consumidorRepository.findById(request.getIdConsumidor())
                .orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));

        Vendedor vendedor = vendedorRepository.findById(request.getIdVendedor())
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        Pedido pedido = new Pedido();
        pedido.setConsumidor(consumidor);
        pedido.setVendedor(vendedor);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstadoPedido("Pendiente");
        pedido.setMetodoPago(request.getMetodoPago());

        pedido.setSubtotal(0.0);
        pedido.setIva(0.0);
        pedido.setTotal(0.0);

        pedidoRepository.save(pedido);

        // Solo 1 producto en la lista de detalles
        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new RuntimeException("No se ha enviado ningún producto en la compra.");
        }

        DetallePedidoAddRequest detRequest = request.getDetalles().get(0);

        Producto producto = productoRepository.findById(detRequest.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getStockProducto() < detRequest.getCantidad()) {
            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombreProducto());
        }

        producto.setStockProducto(producto.getStockProducto() - detRequest.getCantidad());
        productoRepository.save(producto);

        double subtotalDetalle = producto.getPrecioProducto() * detRequest.getCantidad();
        double iva = subtotalDetalle * 0.12;
        double total = subtotalDetalle + iva;

        DetallePedido detalle = new DetallePedido();
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setCantidad(detRequest.getCantidad());
        detalle.setPrecioUnitario(producto.getPrecioProducto());
        detalle.setSubtotal(subtotalDetalle);

        detallePedidoRepository.save(detalle);

        pedido.setSubtotal(subtotalDetalle);
        pedido.setIva(iva);
        pedido.setTotal(total);

        pedidoRepository.save(pedido);

        return pedido;
    }
    
    // ============================================================
    // FINALIZAR COMPRA
    // ============================================================
    
    @Override
    public Pedido finalizarPedido(Integer idPedido, String metodoPago) {

        Pedido pedido = pedidoRepository.findById(idPedido)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setMetodoPago(metodoPago);
        pedido.setEstadoPedido("COMPLETADO");

        return pedidoRepository.save(pedido);
    }

    
}
