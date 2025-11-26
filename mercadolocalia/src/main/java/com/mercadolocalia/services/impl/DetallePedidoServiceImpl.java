package com.mercadolocalia.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.DetallePedidoAddRequest;
import com.mercadolocalia.dto.DetallePedidoUpdateRequest;
import com.mercadolocalia.entities.DetallePedido;
import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.repositories.DetallePedidoRepository;
import com.mercadolocalia.repositories.PedidoRepository;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.services.DetallePedidoService;

@Service
public class DetallePedidoServiceImpl implements DetallePedidoService {

    @Autowired
    private DetallePedidoRepository detalleRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // ============================================================
    // AGREGAR UN DETALLE A PEDIDO EXISTENTE
    // ============================================================
    @Override
    public DetallePedido agregarDetalle(DetallePedidoAddRequest request) {

        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        Producto producto = productoRepository.findById(request.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getStockProducto() < request.getCantidad()) {
            throw new RuntimeException("Stock insuficiente del producto: " + producto.getNombreProducto());
        }

        // Restar stock
        producto.setStockProducto(producto.getStockProducto() - request.getCantidad());
        productoRepository.save(producto);

        double precioUnitario = producto.getPrecioProducto();
        double subtotalDetalle = precioUnitario * request.getCantidad();

        DetallePedido detalle = new DetallePedido();
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setCantidad(request.getCantidad());
        detalle.setPrecioUnitario(precioUnitario);
        detalle.setSubtotal(subtotalDetalle);

        detalleRepository.save(detalle);

        // Recalcular totales
        recalcularTotales(pedido);

        return detalle;
    }

    // ============================================================
    // EDITAR CANTIDAD
    // ============================================================
    @Override
    public DetallePedido editarDetalle(Integer idDetalle, DetallePedidoUpdateRequest request) {

        DetallePedido detalle = detalleRepository.findById(idDetalle)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));

        Pedido pedido = detalle.getPedido();
        Producto producto = detalle.getProducto();

        int cantidadActual = detalle.getCantidad();
        int nuevaCantidad = request.getCantidad();

        if (nuevaCantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }

        int diferencia = nuevaCantidad - cantidadActual;

        // Si la diferencia es positiva → usuario quiere MÁS unidades
        if (diferencia > 0) {
            if (producto.getStockProducto() < diferencia) {
                throw new RuntimeException("Stock insuficiente para aumentar la cantidad");
            }
            producto.setStockProducto(producto.getStockProducto() - diferencia);
        } 
        // Si la diferencia es negativa → devolver stock
        else {
            producto.setStockProducto(producto.getStockProducto() + Math.abs(diferencia));
        }

        productoRepository.save(producto);

        // Actualizar el detalle
        detalle.setCantidad(nuevaCantidad);
        detalle.setSubtotal(producto.getPrecioProducto() * nuevaCantidad);
        detalleRepository.save(detalle);

        // Recalcular totales
        recalcularTotales(pedido);

        return detalle;
    }

    // ============================================================
    // ELIMINAR DETALLE
    // ============================================================
    @Override
    public void eliminarDetalle(Integer idDetalle) {

        DetallePedido detalle = detalleRepository.findById(idDetalle)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));

        Pedido pedido = detalle.getPedido();
        Producto producto = detalle.getProducto();

        // Devolver stock
        producto.setStockProducto(producto.getStockProducto() + detalle.getCantidad());
        productoRepository.save(producto);

        detalleRepository.delete(detalle);

        recalcularTotales(pedido);
    }

    // ============================================================
    // MÉTODO PARA RECALCULAR SUBTOTAL, IVA Y TOTAL
    // ============================================================
    private void recalcularTotales(Pedido pedido) {

        List<DetallePedido> detalles = detalleRepository.findByPedido(pedido);

        double subtotal = detalles.stream()
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();

        double iva = subtotal * 0.12;
        double total = subtotal + iva;

        pedido.setSubtotal(subtotal);
        pedido.setIva(iva);
        pedido.setTotal(total);

        pedidoRepository.save(pedido);
    }
}
