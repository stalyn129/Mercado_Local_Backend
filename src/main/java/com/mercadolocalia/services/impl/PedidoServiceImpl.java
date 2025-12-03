package com.mercadolocalia.services.impl;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mercadolocalia.dto.DetallePedidoAddRequest;
import com.mercadolocalia.dto.PedidoCarritoRequest;
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
    public Pedido finalizarPedido(
            Integer idPedido,
            String metodoPago,
            MultipartFile comprobante,
            String numTarjeta,
            String fechaTarjeta,
            String cvv,
            String titular
    ) {

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setMetodoPago(metodoPago);

        // ============================
        //   EFECTIVO
        // ============================
        if (metodoPago.equalsIgnoreCase("EFECTIVO")) {
            pedido.setEstadoPedido("COMPLETADO");
            return pedidoRepository.save(pedido);
        }

        // ============================
        //   TRANSFERENCIA
        // ============================
        if (metodoPago.equalsIgnoreCase("TRANSFERENCIA")) {

            if (comprobante == null || comprobante.isEmpty()) {
                throw new RuntimeException("Debe subir comprobante.");
            }

            try {
                String carpeta = "uploads/comprobantes/";
                File carpetaDestino = new File(carpeta);

                if (!carpetaDestino.exists()) carpetaDestino.mkdirs();

                String nombreArchivo = System.currentTimeMillis() + "_" + comprobante.getOriginalFilename();
                File destino = new File(carpeta + nombreArchivo);

                comprobante.transferTo(destino);

                pedido.setComprobanteUrl("/" + carpeta + nombreArchivo);
                pedido.setEstadoPedido("PENDIENTE_VERIFICACION");

            } catch (Exception e) {
                throw new RuntimeException("Error al guardar comprobante: " + e.getMessage());
            }

            return pedidoRepository.save(pedido);
        }

        // ============================
        //   TARJETA
        // ============================
        if (metodoPago.equalsIgnoreCase("TARJETA")) {

            if (numTarjeta == null || numTarjeta.length() < 16)
                throw new RuntimeException("Número de tarjeta inválido");

            if (cvv == null || cvv.length() < 3)
                throw new RuntimeException("CVV inválido");

            if (fechaTarjeta == null)
                throw new RuntimeException("Fecha inválida");

            if (titular == null || titular.length() < 3)
                throw new RuntimeException("Nombre inválido");

            // Guardar seguro
            String ultimos4 = numTarjeta.substring(numTarjeta.length() - 4);
            pedido.setDatosTarjeta("**** **** **** " + ultimos4);

            pedido.setEstadoPedido("COMPLETADO");
        }

        return pedidoRepository.save(pedido);
    }
    
    @Override
    public Pedido crearPedidoDesdeCarrito(PedidoCarritoRequest request) {

        Consumidor consumidor = consumidorRepository.findById(request.getIdConsumidor())
                .orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));

        Vendedor vendedor = vendedorRepository.findById(request.getIdVendedor())
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        Pedido pedido = new Pedido();
        pedido.setConsumidor(consumidor);
        pedido.setVendedor(vendedor);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstadoPedido("Pendiente");
        pedido.setMetodoPago("SIN_ASIGNAR");

        pedidoRepository.save(pedido);

        double subtotal = 0;

        for (PedidoCarritoRequest.DetalleProducto item : request.getDetalles()) {

            Producto producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (producto.getStockProducto() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para " + producto.getNombreProducto());
            }

            producto.setStockProducto(producto.getStockProducto() - item.getCantidad());
            productoRepository.save(producto);

            double precio = producto.getPrecioProducto();
            double subDet = precio * item.getCantidad();

            DetallePedido det = new DetallePedido();
            det.setPedido(pedido);
            det.setProducto(producto);
            det.setCantidad(item.getCantidad());
            det.setPrecioUnitario(precio);
            det.setSubtotal(subDet);

            detallePedidoRepository.save(det);

            subtotal += subDet;
        }

        double iva = subtotal * 0.12;
        double total = subtotal + iva;

        pedido.setSubtotal(subtotal);
        pedido.setIva(iva);
        pedido.setTotal(total);

        return pedidoRepository.save(pedido);
    }


}
