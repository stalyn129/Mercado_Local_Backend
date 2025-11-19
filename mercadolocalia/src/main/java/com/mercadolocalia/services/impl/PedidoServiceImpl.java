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
        pedido.setEstadoPedido("Pendiente");   // Estado inicial
        pedido.setMetodoPago(request.getMetodoPago());

        // Guarda temporalmente mientras se calculan totales
        pedido.setSubtotal(0.0);
        pedido.setIva(0.0);
        pedido.setTotal(0.0);

        pedidoRepository.save(pedido);

        double subtotal = 0;

        // ============================================================
        // PROCESAR DETALLES DEL PEDIDO
        // ============================================================
        List<DetallePedido> detalles = new ArrayList<>();

        for (DetallePedidoAddRequest detRequest : request.getDetalles()) {

            Producto producto = productoRepository.findById(detRequest.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (producto.getStockProducto() < detRequest.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombreProducto());
            }

            // Restar stock
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

        // ============================================================
        // CALCULAR IVA Y TOTAL
        // ============================================================
        double iva = subtotal * 0.12;
        double total = subtotal + iva;

        pedido.setSubtotal(subtotal);
        pedido.setIva(iva);
        pedido.setTotal(total);

        pedidoRepository.save(pedido);

        return pedido;
    }

    // ============================================================
    // OBTENER PEDIDO POR ID
    // ============================================================
    @Override
    public Pedido obtenerPedidoPorId(Integer id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
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
    // LISTAR DETALLES DE UN PEDIDO
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
    public Pedido cambiarEstado(Integer idPedido, String nuevoEstado) {

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setEstadoPedido(nuevoEstado);
        pedidoRepository.save(pedido);

        return pedido;
    }
}
