package com.mercadolocalia.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercadolocalia.dto.FacturaRequest;
import com.mercadolocalia.dto.FacturaEstadoRequest;
import com.mercadolocalia.dto.FacturaResponse;
import com.mercadolocalia.entities.*;
import com.mercadolocalia.repositories.*;
import com.mercadolocalia.services.FacturaService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FacturaServiceImpl implements FacturaService {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ConsumidorRepository consumidorRepository;

    @Autowired
    private DetalleFacturaRepository detalleFacturaRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    // GENERAR NÚMERO DE FACTURA
    private String generarNumeroFactura() {
        Optional<Factura> ultimaFactura = facturaRepository.findFirstByOrderByIdFacturaDesc();
        int ultimoNumero = 0;
        
        if (ultimaFactura.isPresent()) {
            String numero = ultimaFactura.get().getNumeroFactura();
            try {
                ultimoNumero = Integer.parseInt(numero.replace("FAC-", ""));
            } catch (Exception e) {
                ultimoNumero = ultimaFactura.get().getIdFactura();
            }
        }
        
        return "FAC-" + String.format("%06d", ultimoNumero + 1);
    }

    @Override
    @Transactional
    public FacturaResponse crearFactura(FacturaRequest request) {
        // 1. Obtener pedido
        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // 2. Verificar que el pago esté VERIFICADO (EstadoPago.PAGADO)
        if (pedido.getEstadoPago() != EstadoPago.PAGADO) {
            throw new RuntimeException("El pedido debe tener pago VERIFICADO para generar factura. Estado actual: " + 
                    (pedido.getEstadoPago() != null ? pedido.getEstadoPago().name() : "NO DEFINIDO"));
        }

        // 3. Verificar si ya existe factura
        if (facturaRepository.findByPedido(pedido) != null) {
            throw new RuntimeException("Ya existe factura para este pedido");
        }

        // 4. Obtener consumidor (directamente del pedido)
        Consumidor consumidor = pedido.getConsumidor();
        if (consumidor == null) {
            throw new RuntimeException("No se encontró consumidor en el pedido");
        }

        // 5. Crear factura
        Factura factura = new Factura();
        factura.setPedido(pedido);
        factura.setConsumidor(consumidor);
        
        // Número único
        String numeroFactura;
        do {
            numeroFactura = generarNumeroFactura();
        } while (facturaRepository.existsByNumeroFactura(numeroFactura));
        
        factura.setNumeroFactura(numeroFactura);
        factura.setFechaEmision(LocalDateTime.now());
        factura.setSubtotal(pedido.getSubtotal());
        factura.setIva(pedido.getIva());
        factura.setTotal(pedido.getTotal());
        factura.setEstado("Emitida");
        factura.setMetodoPago(pedido.getMetodoPago());

        // Guardar factura
        factura = facturaRepository.save(factura);

        // 6. Crear detalles de factura
        List<DetallePedido> detallesPedido = detallePedidoRepository.findByPedido(pedido);
        
        if (detallesPedido.isEmpty()) {
            throw new RuntimeException("El pedido no tiene productos");
        }
        
        for (DetallePedido detalle : detallesPedido) {
            Producto producto = detalle.getProducto();
            if (producto == null) {
                throw new RuntimeException("Producto no encontrado en detalle de pedido");
            }
            
            Vendedor vendedor = producto.getVendedor();
            if (vendedor == null) {
                throw new RuntimeException("Vendedor no encontrado para el producto: " + producto.getNombreProducto());
            }
            
            DetalleFactura detalleFactura = new DetalleFactura();
            detalleFactura.setFactura(factura);
            detalleFactura.setVendedor(vendedor);
            detalleFactura.setProducto(producto);
            detalleFactura.setCantidad(detalle.getCantidad());
            // Usar getPrecio() de DetallePedido si existe, o getPrecioProducto() de Producto
            Double precio = (detalle.getPrecioUnitario() != null) ? detalle.getPrecioUnitario() : producto.getPrecioProducto();
            detalleFactura.setPrecioUnitario(precio);
            detalleFactura.setSubtotalProducto(detalle.getCantidad() * precio);
            detalleFactura.setCreatedAt(LocalDateTime.now());
            
            detalleFacturaRepository.save(detalleFactura);
        }

        // 7. Actualizar estado del pedido (opcional)
        pedido.setEstadoPedido(EstadoPedido.COMPLETADO);
        pedidoRepository.save(pedido);

        // 8. Retornar respuesta
        return convertirAFacturaResponse(factura);
    }

    @Override
    public FacturaResponse obtenerPorId(Integer idFactura) {
        Factura factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        return convertirAFacturaResponse(factura);
    }

    @Override
    public FacturaResponse obtenerPorPedido(Integer idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        
        Factura factura = facturaRepository.findByPedido(pedido);
        if (factura == null) {
            throw new RuntimeException("No hay factura para este pedido");
        }
        
        return convertirAFacturaResponse(factura);
    }

    @Override
    @Transactional
    public FacturaResponse actualizarEstado(Integer idFactura, FacturaEstadoRequest request) {
        Factura factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        // Validar estados permitidos
        List<String> estadosPermitidos = Arrays.asList("Emitida", "Anulada", "Pagada");
        if (!estadosPermitidos.contains(request.getEstado())) {
            throw new RuntimeException("Estado no válido. Estados permitidos: " + estadosPermitidos);
        }

        factura.setEstado(request.getEstado());
        factura = facturaRepository.save(factura);
        
        return convertirAFacturaResponse(factura);
    }

    // NUEVO MÉTODO: Obtener facturas por consumidor
    @Override
    public List<FacturaResponse> obtenerPorConsumidor(Integer idConsumidor) {
        try {
            Consumidor consumidor = consumidorRepository.findById(idConsumidor)
                    .orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));
            
            List<Factura> facturas = facturaRepository.findByConsumidor(consumidor);
            
            return facturas.stream()
                    .map(this::convertirAFacturaResponse)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener facturas por consumidor: " + e.getMessage());
        }
    }
    
    // NUEVO MÉTODO: Obtener todas las facturas
    @Override
    public List<FacturaResponse> obtenerTodas() {
        try {
            List<Factura> facturas = facturaRepository.findAll();
            
            return facturas.stream()
                    .map(this::convertirAFacturaResponse)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener todas las facturas: " + e.getMessage());
        }
    }

    // CONVERTIR ENTIDAD A DTO
    private FacturaResponse convertirAFacturaResponse(Factura factura) {
        FacturaResponse response = new FacturaResponse();
        
        // Datos básicos
        response.setIdFactura(factura.getIdFactura());
        response.setNumeroFactura(factura.getNumeroFactura());
        response.setFechaEmision(factura.getFechaEmision());
        response.setSubtotal(factura.getSubtotal());
        response.setIva(factura.getIva());
        response.setTotal(factura.getTotal());
        response.setEstado(factura.getEstado());
        response.setMetodoPago(factura.getMetodoPago());
        response.setRutaPdf(factura.getRutaPdf());
        
        // Pedido
        response.setIdPedido(factura.getPedido().getIdPedido());
        
        // Cliente (desde consumidor)
        Consumidor consumidor = factura.getConsumidor();
        if (consumidor != null) {
            Usuario usuario = consumidor.getUsuario();
            
            response.setNombreCliente(usuario.getNombre());
            response.setApellidoCliente(usuario.getApellido());
            response.setCedulaCliente(consumidor.getCedulaConsumidor());
            response.setCorreoCliente(usuario.getCorreo());
            response.setTelefonoCliente(consumidor.getTelefonoConsumidor());
            response.setDireccionCliente(consumidor.getDireccionConsumidor());
        }
        
        // Detalles por vendedor
        List<DetalleFactura> detalles = detalleFacturaRepository.findByFactura(factura);
        if (!detalles.isEmpty()) {
            // Agrupar por vendedor
            Map<Vendedor, List<DetalleFactura>> agrupados = detalles.stream()
                    .collect(Collectors.groupingBy(DetalleFactura::getVendedor));
            
            List<FacturaResponse.VendedorDetalle> vendedores = new ArrayList<>();
            
            for (Map.Entry<Vendedor, List<DetalleFactura>> entry : agrupados.entrySet()) {
                Vendedor vendedor = entry.getKey();
                List<DetalleFactura> detalleList = entry.getValue();
                
                FacturaResponse.VendedorDetalle vendedorDetalle = new FacturaResponse.VendedorDetalle();
                vendedorDetalle.setRazonSocial(vendedor.getNombreEmpresa());
                vendedorDetalle.setRuc(vendedor.getRucEmpresa());
                
                List<FacturaResponse.ProductoDetalle> productos = detalleList.stream()
                        .map(d -> {
                            FacturaResponse.ProductoDetalle p = new FacturaResponse.ProductoDetalle();
                            p.setNombre(d.getProducto().getNombreProducto());
                            p.setCantidad(d.getCantidad());
                            p.setPrecioUnitario(d.getPrecioUnitario());
                            p.setSubtotal(d.getSubtotalProducto());
                            return p;
                        })
                        .collect(Collectors.toList());
                
                vendedorDetalle.setProductos(productos);
                vendedores.add(vendedorDetalle);
            }
            
            response.setDetallesPorVendedor(vendedores);
        }
        
        return response;
    }
    
    // NUEVO MÉTODO: Generar PDF simple (para pruebas)
    public String generarPDFSimple(Integer idFactura) {
        try {
            Factura factura = facturaRepository.findById(idFactura)
                    .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
            
            // Crear contenido simple del PDF (texto)
            StringBuilder pdfContent = new StringBuilder();
            pdfContent.append("========================================\n");
            pdfContent.append("            FACTURA ELECTRÓNICA\n");
            pdfContent.append("========================================\n\n");
            
            pdfContent.append("Número Factura: ").append(factura.getNumeroFactura()).append("\n");
            pdfContent.append("Fecha Emisión: ").append(factura.getFechaEmision()).append("\n");
            pdfContent.append("Estado: ").append(factura.getEstado()).append("\n");
            pdfContent.append("Método Pago: ").append(factura.getMetodoPago()).append("\n\n");
            
            // Datos del cliente
            if (factura.getConsumidor() != null && factura.getConsumidor().getUsuario() != null) {
                pdfContent.append("DATOS DEL CLIENTE:\n");
                pdfContent.append("Nombre: ").append(factura.getConsumidor().getUsuario().getNombre())
                          .append(" ").append(factura.getConsumidor().getUsuario().getApellido()).append("\n");
                pdfContent.append("Cédula: ").append(factura.getConsumidor().getCedulaConsumidor()).append("\n");
                pdfContent.append("Email: ").append(factura.getConsumidor().getUsuario().getCorreo()).append("\n");
                pdfContent.append("Teléfono: ").append(factura.getConsumidor().getTelefonoConsumidor()).append("\n\n");
            }
            
            // Totales
            pdfContent.append("TOTALES:\n");
            pdfContent.append("Subtotal: $").append(String.format("%.2f", factura.getSubtotal())).append("\n");
            pdfContent.append("IVA (12%): $").append(String.format("%.2f", factura.getIva())).append("\n");
            pdfContent.append("TOTAL: $").append(String.format("%.2f", factura.getTotal())).append("\n\n");
            
            pdfContent.append("========================================\n");
            pdfContent.append("        MY HARVEST MERCADO LOCAL\n");
            pdfContent.append("========================================\n");
            pdfContent.append("Gracias por su compra\n");
            pdfContent.append("Fecha: ").append(LocalDateTime.now()).append("\n");
            
            return pdfContent.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage());
        }
    }
}