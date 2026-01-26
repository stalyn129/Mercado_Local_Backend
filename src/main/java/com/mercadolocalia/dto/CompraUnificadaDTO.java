package com.mercadolocalia.dto;

import lombok.Data;
import com.mercadolocalia.entities.*;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.HashMap;

@Data
public class CompraUnificadaDTO {
    private String idCompraUnificada;
    private Double subtotalGeneral;
    private Double ivaGeneral;
    private Double totalGeneral;
    private String metodoPago;
    private String fechaCompra;
    private Integer cantidadPedidos;
    private Integer cantidadVendedores;
    private List<String> vendedores;
    private List<Pedido> pedidos;
    
    // 🔥 NUEVOS CAMPOS PARA INFORMACIÓN DE PAGOS
    private String estadoPagoGeneral;
    private Boolean tieneComprobantePendiente;
    private Boolean pagosCompletamenteVerificados;
    private Map<String, Object> infoPago; // Información adicional de pagos
    private Map<Integer, EstadoPago> estadosPagoPorVendedor; // Estado de pago por cada vendedor
    private List<PedidoResumenDTO> pedidosResumen; // Resumen simplificado de pedidos
    private Boolean puedeReintentarPago; // Si algún pago fue rechazado
    
    // Constructor para crear fácilmente el DTO
    public CompraUnificadaDTO(String idCompraUnificada, List<Pedido> pedidos) {
        this.idCompraUnificada = idCompraUnificada;
        this.pedidos = pedidos;
        this.cantidadPedidos = pedidos.size();
        
        // Calcular totales
        this.subtotalGeneral = pedidos.stream()
            .mapToDouble(p -> p.getSubtotal() != null ? p.getSubtotal() : 0.0)
            .sum();
        
        this.ivaGeneral = pedidos.stream()
            .mapToDouble(p -> p.getIva() != null ? p.getIva() : 0.0)
            .sum();
        
        this.totalGeneral = pedidos.stream()
            .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0)
            .sum();
        
        // Obtener vendedores únicos
        this.vendedores = pedidos.stream()
            .map(p -> p.getVendedor() != null ? 
                p.getVendedor().getNombreEmpresa() : "Sin vendedor")
            .distinct()
            .toList();
        
        this.cantidadVendedores = this.vendedores.size();
        
        // Método de pago (tomar del primer pedido)
        if (!pedidos.isEmpty()) {
            Pedido primerPedido = pedidos.get(0);
            this.metodoPago = primerPedido.getMetodoPago();
            this.fechaCompra = primerPedido.getFechaPedido() != null ? 
                primerPedido.getFechaPedido().toString() : LocalDateTime.now().toString();
            
            // 🔥 INICIALIZAR NUEVOS CAMPOS
            inicializarCamposPago(pedidos);
            inicializarEstadosPago(pedidos);
            inicializarPedidosResumen(pedidos);
        }
    }
    
    // 🔥 MÉTODO PARA INICIALIZAR INFORMACIÓN DE PAGOS
    private void inicializarCamposPago(List<Pedido> pedidos) {
        this.infoPago = new HashMap<>();
        
        // Verificar si todos los pedidos están pagados
        boolean todosPagados = pedidos.stream()
            .allMatch(p -> p.getEstadoPago() == EstadoPago.PAGADO);
        
        // Verificar si hay algún pedido en verificación
        boolean algunoEnVerificacion = pedidos.stream()
            .anyMatch(p -> p.getEstadoPago() == EstadoPago.EN_VERIFICACION);
        
        // Verificar si hay algún pedido rechazado
        boolean algunoRechazado = pedidos.stream()
            .anyMatch(p -> p.getEstadoPago() == EstadoPago.RECHAZADO);
        
        // Verificar si hay comprobantes pendientes
        boolean tieneComprobante = pedidos.stream()
            .anyMatch(p -> p.getComprobanteUrl() != null && !p.getComprobanteUrl().isEmpty());
        
        // Determinar estado general del pago
        if (algunoRechazado) {
            this.estadoPagoGeneral = "RECHAZADO";
            this.puedeReintentarPago = true;
        } else if (algunoEnVerificacion) {
            this.estadoPagoGeneral = "EN_VERIFICACION";
            this.puedeReintentarPago = false;
        } else if (todosPagados) {
            this.estadoPagoGeneral = "PAGADO";
            this.puedeReintentarPago = false;
        } else {
            this.estadoPagoGeneral = "PENDIENTE";
            this.puedeReintentarPago = false;
        }
        
        this.tieneComprobantePendiente = tieneComprobante;
        this.pagosCompletamenteVerificados = todosPagados;
        
        // Información adicional
        infoPago.put("todosPagados", todosPagados);
        infoPago.put("algunoEnVerificacion", algunoEnVerificacion);
        infoPago.put("algunoRechazado", algunoRechazado);
        infoPago.put("totalPedidos", pedidos.size());
        
        // Contar por estado de pago
        long pagados = pedidos.stream()
            .filter(p -> p.getEstadoPago() == EstadoPago.PAGADO)
            .count();
        long enVerificacion = pedidos.stream()
            .filter(p -> p.getEstadoPago() == EstadoPago.EN_VERIFICACION)
            .count();
        long rechazados = pedidos.stream()
            .filter(p -> p.getEstadoPago() == EstadoPago.RECHAZADO)
            .count();
        long pendientes = pedidos.stream()
            .filter(p -> p.getEstadoPago() == EstadoPago.PENDIENTE)
            .count();
        
        infoPago.put("pagados", pagados);
        infoPago.put("enVerificacion", enVerificacion);
        infoPago.put("rechazados", rechazados);
        infoPago.put("pendientes", pendientes);
    }
    
    // 🔥 MÉTODO PARA INICIALIZAR ESTADOS DE PAGO POR VENDEDOR
    private void inicializarEstadosPago(List<Pedido> pedidos) {
        this.estadosPagoPorVendedor = new HashMap<>();
        
        for (Pedido pedido : pedidos) {
            if (pedido.getVendedor() != null && pedido.getEstadoPago() != null) {
                estadosPagoPorVendedor.put(
                    pedido.getVendedor().getIdVendedor(),
                    pedido.getEstadoPago()
                );
            }
        }
    }
    
    // 🔥 MÉTODO PARA CREAR RESUMEN SIMPLIFICADO DE PEDIDOS
    private void inicializarPedidosResumen(List<Pedido> pedidos) {
        this.pedidosResumen = pedidos.stream()
            .map(p -> {
                PedidoResumenDTO resumen = new PedidoResumenDTO();
                resumen.setIdPedido(p.getIdPedido());
                resumen.setIdCompraUnificada(p.getIdCompraUnificada());
                
                if (p.getVendedor() != null) {
                    resumen.setVendedorId(p.getVendedor().getIdVendedor());
                    resumen.setVendedorNombre(p.getVendedor().getNombreEmpresa());
                }
                
                resumen.setTotal(p.getTotal());
                resumen.setEstadoPedido(p.getEstadoPedido() != null ? 
                    p.getEstadoPedido().name() : "CREADO");
                resumen.setEstadoPago(p.getEstadoPago() != null ? 
                    p.getEstadoPago().name() : "PENDIENTE");
                resumen.setMetodoPago(p.getMetodoPago());
                
                if (p.getComprobanteUrl() != null) {
                    resumen.setTieneComprobante(true);
                    resumen.setComprobanteUrl(p.getComprobanteUrl());
                }
                
                if (p.getMotivoRechazo() != null) {
                    resumen.setMotivoRechazo(p.getMotivoRechazo());
                }
                
                if (p.getFechaVerificacionPago() != null) {
                    resumen.setFechaVerificacion(p.getFechaVerificacionPago().toString());
                }
                
                // Productos en el pedido
                if (p.getDetalles() != null && !p.getDetalles().isEmpty()) {
                    List<String> productos = p.getDetalles().stream()
                        .map(d -> {
                            String nombre = d.getProducto() != null ? 
                                d.getProducto().getNombreProducto() : "Producto desconocido";
                            return nombre + " (x" + d.getCantidad() + ")";
                        })
                        .toList();
                    resumen.setProductos(productos);
                }
                
                return resumen;
            })
            .toList();
    }
    
    // 🔥 INNER CLASS PARA RESUMEN DE PEDIDO
    @Data
    public static class PedidoResumenDTO {
        private Integer idPedido;
        private String idCompraUnificada;
        private Integer vendedorId;
        private String vendedorNombre;
        private Double total;
        private String estadoPedido;
        private String estadoPago;
        private String metodoPago;
        private Boolean tieneComprobante = false;
        private String comprobanteUrl;
        private String motivoRechazo;
        private String fechaVerificacion;
        private List<String> productos;
    }
    
    // 🔥 MÉTODOS UTILES PARA EL FRONTEND
    
    public boolean isPendientePago() {
        return "PENDIENTE".equals(estadoPagoGeneral) || 
               "EN_VERIFICACION".equals(estadoPagoGeneral);
    }
    
    public boolean isPagado() {
        return "PAGADO".equals(estadoPagoGeneral);
    }
    
    public boolean isRechazado() {
        return "RECHAZADO".equals(estadoPagoGeneral);
    }
    
    public boolean necesitaAccion() {
        return isRechazado() || 
               (tieneComprobantePendiente && isPendientePago());
    }
    
    // 🔥 MÉTODO PARA OBTENER PEDIDOS POR ESTADO DE PAGO
    public List<Pedido> getPedidosPorEstadoPago(EstadoPago estado) {
        return pedidos.stream()
            .filter(p -> p.getEstadoPago() == estado)
            .toList();
    }
    
    // 🔥 MÉTODO PARA OBTENER PEDIDOS CON COMPROBANTE
    public List<Pedido> getPedidosConComprobante() {
        return pedidos.stream()
            .filter(p -> p.getComprobanteUrl() != null && !p.getComprobanteUrl().isEmpty())
            .toList();
    }
    
    // 🔥 MÉTODO PARA OBTENER PEDIDOS QUE NECESITAN VERIFICACIÓN
    public List<Pedido> getPedidosNecesitanVerificacion() {
        return pedidos.stream()
            .filter(p -> p.getEstadoPago() == EstadoPago.EN_VERIFICACION ||
                        (p.getComprobanteUrl() != null && 
                         !p.getComprobanteUrl().isEmpty() && 
                         p.getEstadoPago() == EstadoPago.PENDIENTE))
            .toList();
    }
}