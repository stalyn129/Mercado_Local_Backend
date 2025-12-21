package com.mercadolocalia.services.impl;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.mercadolocalia.services.NotificacionService;
import com.mercadolocalia.services.PedidoService;

@Service
public class PedidoServiceImpl implements PedidoService {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private DetallePedidoRepository detallePedidoRepository;
    @Autowired private ConsumidorRepository consumidorRepository;
    @Autowired private VendedorRepository vendedorRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private NotificacionService notificacionService;

    // ============================================================
    // CREAR PEDIDO COMPLETO
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
        pedido.setEstadoPedido("PEDIDO_CREADO");
        pedido.setMetodoPago(request.getMetodoPago());
        pedido.setSubtotal(0.0);
        pedido.setIva(0.0);
        pedido.setTotal(0.0);

        pedidoRepository.save(pedido);

        double subtotal = 0;

        for (DetallePedidoAddRequest det : request.getDetalles()) {

            Producto producto = productoRepository.findById(det.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (producto.getStockProducto() < det.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para " + producto.getNombreProducto());
            }

            producto.setStockProducto(producto.getStockProducto() - det.getCantidad());
            productoRepository.save(producto);

            double sub = producto.getPrecioProducto() * det.getCantidad();

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(det.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioProducto());
            detalle.setSubtotal(sub);

            detallePedidoRepository.save(detalle);
            subtotal += sub;
        }

        pedido.setSubtotal(subtotal);
        pedido.setIva(subtotal * 0.12);
        pedido.setTotal(pedido.getSubtotal() + pedido.getIva());

        return pedidoRepository.save(pedido);
    }

    // ============================================================
    // OBTENER PEDIDO
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

        notificacionService.crearNotificacion(
                pedido.getConsumidor().getUsuario(),
                "📦 Tu pedido #" + pedido.getIdPedido() + " ahora está en estado: " + nuevoEstado,
                "PEDIDO",
                pedido.getIdPedido()
        );

        return pedido;
    }

    // ============================================================
    // COMPRAR AHORA
    // ============================================================
    @Override
    public Pedido comprarAhora(PedidoRequest request) {
        return crearPedido(request);
    }

    // ============================================================
    // FINALIZAR PEDIDO (SIMPLE)
    // ============================================================
    @Override
    public Pedido finalizarPedido(Integer idPedido, String metodoPago) {

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setMetodoPago(metodoPago);

        if (metodoPago.equalsIgnoreCase("EFECTIVO")) {
            pedido.setEstadoPedido("COMPLETADO");
        } else {
            pedido.setEstadoPedido("PENDIENTE_VERIFICACION");
        }

        notificacionService.crearNotificacion(
                pedido.getConsumidor().getUsuario(),
                "💳 Tu pedido #" + pedido.getIdPedido() + " fue finalizado con método: " + metodoPago,
                "PEDIDO",
                pedido.getIdPedido()
        );

        notificacionService.crearNotificacion(
                pedido.getVendedor().getUsuario(),
                "📦 Nuevo pedido #" + pedido.getIdPedido() + " listo para procesar",
                "PEDIDO",
                pedido.getIdPedido()
        );

        return pedidoRepository.save(pedido);
    }

    // ============================================================
    // FINALIZAR PEDIDO (COMPLETO)
    // ============================================================
    @Override
    public Pedido finalizarPedido(
            Integer idPedido,
            String metodoPago,
            MultipartFile comprobante,
            String numTarjeta,
            String fechaTarjeta,
            String cvv,
            String titular) {

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setMetodoPago(metodoPago);

        if (metodoPago.equalsIgnoreCase("EFECTIVO")) {
            pedido.setEstadoPedido("COMPLETADO");
        }

        if (metodoPago.equalsIgnoreCase("TRANSFERENCIA")) {

            try {
                String carpeta = "uploads/comprobantes/";
                new File(carpeta).mkdirs();

                String nombre = System.currentTimeMillis() + "_" + comprobante.getOriginalFilename();
                comprobante.transferTo(new File(carpeta + nombre));

                pedido.setComprobanteUrl("/" + carpeta + nombre);
                pedido.setEstadoPedido("PENDIENTE_VERIFICACION");

            } catch (Exception e) {
                throw new RuntimeException("Error al guardar comprobante");
            }

            notificacionService.crearNotificacion(
                    pedido.getVendedor().getUsuario(),
                    "💳 Pedido #" + pedido.getIdPedido() + " pendiente de verificación de transferencia",
                    "PEDIDO",
                    pedido.getIdPedido()
            );
        }

        if (metodoPago.equalsIgnoreCase("TARJETA")) {
            String ultimos4 = numTarjeta.substring(numTarjeta.length() - 4);
            pedido.setDatosTarjeta("**** **** **** " + ultimos4);
            pedido.setEstadoPedido("COMPLETADO");
        }

        notificacionService.crearNotificacion(
                pedido.getConsumidor().getUsuario(),
                "💳 Tu pedido #" + pedido.getIdPedido() + " fue finalizado con método: " + metodoPago,
                "PEDIDO",
                pedido.getIdPedido()
        );

        return pedidoRepository.save(pedido);
    }

    // ============================================================
    // PEDIDO DESDE CARRITO
    // ============================================================
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
        pedido.setEstadoPedido("PEDIDO_CREADO");
        pedido.setMetodoPago("SIN_ASIGNAR");

        pedidoRepository.save(pedido);

        double subtotal = 0;

        for (PedidoCarritoRequest.DetalleProducto item : request.getDetalles()) {

            Producto producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            producto.setStockProducto(producto.getStockProducto() - item.getCantidad());
            productoRepository.save(producto);

            double sub = producto.getPrecioProducto() * item.getCantidad();

            DetallePedido det = new DetallePedido();
            det.setPedido(pedido);
            det.setProducto(producto);
            det.setCantidad(item.getCantidad());
            det.setPrecioUnitario(producto.getPrecioProducto());
            det.setSubtotal(sub);

            detallePedidoRepository.save(det);
            subtotal += sub;
        }

        pedido.setSubtotal(subtotal);
        pedido.setIva(subtotal * 0.12);
        pedido.setTotal(pedido.getSubtotal() + pedido.getIva());

        return pedidoRepository.save(pedido);
    }

    // ============================================================
    // ESTADÍSTICAS
    // ============================================================
    @Override
    public Map<String, Object> obtenerEstadisticasVendedor(Integer idVendedor) {

        Vendedor vendedor = vendedorRepository.findById(idVendedor)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("pedidos", pedidoRepository.countByVendedor(vendedor));
        stats.put("total", pedidoRepository.sumarIngresosPorVendedor(idVendedor));

        return stats;
    }

    // ============================================================
    // VENTAS MENSUALES
    // ============================================================
    @Override
    public List<Map<String, Object>> obtenerVentasMensuales(Integer idVendedor) {

        List<Object[]> data = pedidoRepository.obtenerVentasMensualesPorVendedor(idVendedor);
        List<Map<String, Object>> res = new ArrayList<>();

        String[] meses = {
                "Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
        };

        for (Object[] row : data) {
            Map<String, Object> item = new HashMap<>();
            item.put("mes", meses[((Number) row[0]).intValue() - 1]);
            item.put("total", row[1]);
            res.add(item);
        }

        return res;
    }
}
