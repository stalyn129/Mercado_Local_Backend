package com.mercadolocalia.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mercadolocalia.dto.PagoRequest;
import com.mercadolocalia.dto.PagoTarjetaRequest;
import com.mercadolocalia.dto.VerificacionPagoRequest;
import com.mercadolocalia.entities.*;
import com.mercadolocalia.repositories.PagoRepository;
import com.mercadolocalia.repositories.PedidoRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private FileStorageService fileStorageService;

    // =========================================================
    // PAGO GENERAL (EFECTIVO / TRANSFERENCIA / TARJETA)
    // =========================================================
    public Pago procesarPago(PagoRequest request) {
        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        
        // Validar que el pedido no tenga ya un pago procesado
        if (pedido.getPago() != null) {
            throw new RuntimeException("Este pedido ya tiene un pago registrado");
        }
        
        // Configurar método de pago en el pedido
        pedido.setMetodoPago(request.getMetodoPago().name());
        
        EstadoPago estadoPago;
        EstadoPedido estadoPedido;

        switch (request.getMetodoPago()) {
            case EFECTIVO:
                estadoPago = EstadoPago.PENDIENTE;
                estadoPedido = EstadoPedido.PROCESANDO;
                pedido.setEstadoPedidoVendedor(EstadoPedidoVendedor.NUEVO);
                pedido.setEstadoSeguimiento(EstadoSeguimientoPedido.ESPERANDO_PAGO);
                break;
                
            case TRANSFERENCIA:
                estadoPago = EstadoPago.EN_VERIFICACION;
                estadoPedido = EstadoPedido.PENDIENTE;
                pedido.setEstadoSeguimiento(EstadoSeguimientoPedido.ESPERANDO_PAGO);
                break;
                
            case TARJETA:
                // Para tarjeta, usar el método específico
                throw new IllegalArgumentException("Para pagos con tarjeta, use procesarPagoTarjeta()");
                
            default:
                throw new IllegalArgumentException("Método de pago no válido");
        }
        
        pedido.setEstadoPago(estadoPago);
        pedido.setEstadoPedido(estadoPedido);
        pedidoRepository.save(pedido);
        
        // Crear registro de pago
        Pago pago = crearPagoBase(pedido, request.getMonto(), request.getMetodoPago(), estadoPago);
        
        return pago;
    }

    // =========================================================
    // SUBIR COMPROBANTE DE TRANSFERENCIA
    // =========================================================
    public Pedido subirComprobanteTransferencia(Integer pedidoId, MultipartFile archivo) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        
        // Validar que el pedido use transferencia
        if (!"TRANSFERENCIA".equalsIgnoreCase(pedido.getMetodoPago())) {
            throw new RuntimeException("Este pedido no usa transferencia como método de pago");
        }
        
        // Validar estado - permitir subir si está PENDIENTE o RECHAZADO
        if (pedido.getEstadoPago() != EstadoPago.PENDIENTE && 
            pedido.getEstadoPago() != EstadoPago.RECHAZADO) {
            throw new RuntimeException("No se puede subir comprobante en este estado");
        }
        
        // Validar tipo de archivo
        String contentType = archivo.getContentType();
        if (contentType == null || 
            (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new RuntimeException("Solo se permiten imágenes (JPG, PNG) y PDF");
        }
        
        // Validar tamaño (máximo 5MB)
        if (archivo.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("El archivo es demasiado grande (máximo 5MB)");
        }
        
        try {
            // Guardar archivo
            String fileName = fileStorageService.storeFile(archivo);
            String fileUrl = "/uploads/" + fileName;
            
            // Actualizar pedido
            pedido.setComprobanteUrl(fileUrl);
            pedido.setFechaSubidaComprobante(LocalDateTime.now());
            pedido.setEstadoPago(EstadoPago.EN_VERIFICACION);
            pedido.setEstadoPedido(EstadoPedido.PENDIENTE);
            
            // Si había sido rechazado antes, limpiar motivo
            if (pedido.getEstadoPago() == EstadoPago.RECHAZADO) {
                pedido.setMotivoRechazo(null);
            }
            
            // Actualizar el estado del pago asociado si existe
            if (pedido.getPago() != null) {
                pedido.getPago().setEstado(EstadoPago.EN_VERIFICACION);
                pagoRepository.save(pedido.getPago());
            }
            
            return pedidoRepository.save(pedido);
            
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el comprobante: " + e.getMessage());
        }
    }

 // =========================================================
 // VERIFICAR PAGO (PARA VENDEDOR) - CORREGIDO
 // =========================================================
 public Pedido verificarPago(Integer pedidoId, Integer vendedorId, VerificacionPagoRequest request) {
     Pedido pedido = pedidoRepository.findById(pedidoId)
         .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
     
     // 🔥 CORRECCIÓN: Validación más flexible
     boolean tienePermiso = false;
     
     // 1. Verificar si el vendedor está asignado directamente al pedido
     if (pedido.getVendedor() != null && pedido.getVendedor().getIdVendedor().equals(vendedorId)) {
         tienePermiso = true;
         System.out.println("✅ Permiso concedido: Vendedor asignado al pedido");
     }
     // 2. Verificar si el vendedor tiene productos en este pedido
     else if (pedido.getDetalles() != null && !pedido.getDetalles().isEmpty()) {
         for (DetallePedido detalle : pedido.getDetalles()) {
             if (detalle.getProducto() != null && 
                 detalle.getProducto().getVendedor() != null &&
                 detalle.getProducto().getVendedor().getIdVendedor().equals(vendedorId)) {
                 
                 tienePermiso = true;
                 
                 // 🔥 Asignar el vendedor al pedido si no estaba asignado
                 if (pedido.getVendedor() == null) {
                     pedido.setVendedor(detalle.getProducto().getVendedor());
                     System.out.println("✅ Vendedor asignado al pedido desde producto");
                 }
                 break;
             }
         }
     }
     
     if (!tienePermiso) {
         System.out.println("❌ Validación de permisos falló:");
         System.out.println("❌ Vendedor autenticado ID: " + vendedorId);
         System.out.println("❌ Vendedor del pedido: " + 
             (pedido.getVendedor() != null ? pedido.getVendedor().getIdVendedor() : "null"));
         System.out.println("❌ Detalles del pedido: " + 
             (pedido.getDetalles() != null ? pedido.getDetalles().size() : 0));
         
         throw new RuntimeException("No tienes permisos para verificar este pago");
     }
     
     // Validar que esté en estado EN_VERIFICACION
     if (pedido.getEstadoPago() != EstadoPago.EN_VERIFICACION) {
         throw new RuntimeException("El pago no está pendiente de verificación. Estado actual: " + 
             pedido.getEstadoPago());
     }
     
     // Validar que tenga comprobante
     if (pedido.getComprobanteUrl() == null || pedido.getComprobanteUrl().isEmpty()) {
         throw new RuntimeException("El pedido no tiene comprobante para verificar");
     }
     
     // Procesar la verificación
     if (request.isAprobado()) {
         // PAGO APROBADO
         pedido.setEstadoPago(EstadoPago.PAGADO);
         pedido.setEstadoPedido(EstadoPedido.PROCESANDO);
         pedido.setEstadoPedidoVendedor(EstadoPedidoVendedor.EN_PROCESO);
         pedido.setEstadoSeguimiento(EstadoSeguimientoPedido.RECOLECTANDO);
         pedido.setMotivoRechazo(null); // Limpiar motivo si existía
         System.out.println("✅ Pago APROBADO para pedido: " + pedidoId);
     } else {
         // PAGO RECHAZADO
         pedido.setEstadoPago(EstadoPago.RECHAZADO);
         pedido.setMotivoRechazo(request.getMotivo());
         System.out.println("✅ Pago RECHAZADO para pedido: " + pedidoId + 
             " - Motivo: " + request.getMotivo());
     }
     
     pedido.setFechaVerificacionPago(LocalDateTime.now());
     
     // 🔥 CORRECCIÓN: verificadoPor es Integer, no String
     pedido.setVerificadoPor(vendedorId);  // Esto ahora funciona porque ambos son Integer
     
     // Actualizar también el registro de pago si existe
     if (pedido.getPago() != null) {
         pedido.getPago().setEstado(pedido.getEstadoPago());
         pagoRepository.save(pedido.getPago());
     }
     
     // Guardar cambios en el pedido
     Pedido pedidoActualizado = pedidoRepository.save(pedido);
     
     System.out.println("✅ Verificación completada exitosamente");
     System.out.println("✅ Nuevo estado: " + pedidoActualizado.getEstadoPago());
     System.out.println("✅ Verificado por (ID): " + pedidoActualizado.getVerificadoPor());
     
     return pedidoActualizado;
 }

    // =========================================================
    // PAGO CON TARJETA (SIMULADO)
    // =========================================================
    public Pago procesarPagoTarjetaSimulado(PagoTarjetaRequest request) {
        Pedido pedido = pedidoRepository.findById(request.getIdPedido())
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        
        // Validar que el pedido no tenga ya un pago
        if (pedido.getPago() != null) {
            throw new RuntimeException("Este pedido ya tiene un pago registrado");
        }
        
        validarDatosTarjeta(request);
        
        String tarjeta = request.getNumeroTarjeta().replace(" ", "");
        
        switch (tarjeta) {
            // ✅ TARJETA APROBADA
            case "4111111111111111":
                // Actualizar pedido
                pedido.setMetodoPago("TARJETA");
                pedido.setEstadoPago(EstadoPago.PAGADO);
                pedido.setEstadoPedido(EstadoPedido.PROCESANDO);
                pedido.setEstadoPedidoVendedor(EstadoPedidoVendedor.EN_PROCESO);
                pedido.setEstadoSeguimiento(EstadoSeguimientoPedido.RECOLECTANDO);
                pedido.setDatosTarjeta("XXXX-XXXX-XXXX-" + tarjeta.substring(12));
                pedidoRepository.save(pedido);
                
                // Crear registro de pago
                Pago pagoAprobado = crearPagoBase(pedido, request.getMonto(), 
                    MetodoPago.TARJETA, EstadoPago.PAGADO);
                pagoAprobado.setDatosTransaccion("Transacción simulada aprobada - Tarjeta: " + 
                    tarjeta.substring(0, 4) + "XXXXXXXX" + tarjeta.substring(12));
                
                return pagoAprobado;
                
            // ❌ ERRORES SIMULADOS
            case "4000000000000002":
                throw new RuntimeException("Fondos insuficientes");
                
            case "4000000000009995":
                throw new RuntimeException("Tarjeta bloqueada");
                
            default:
                throw new RuntimeException("Tarjeta inválida o no reconocida");
        }
    }
    
    // =========================================================
    // OBTENER PEDIDOS PENDIENTES DE VERIFICACIÓN
    // =========================================================
    public List<Pedido> obtenerPendientesVerificacion(Integer vendedorId) {
        // 🔥 CORRECCIÓN: Usar el método corregido del repository
        return pedidoRepository.findByVendedor_IdVendedorAndEstadoPago(
            vendedorId, EstadoPago.EN_VERIFICACION
        );
    }
    
    // =========================================================
    // OBTENER PAGO POR ID DE PEDIDO
    // =========================================================
    public Pago obtenerPagoPorPedidoId(Integer pedidoId) {
        return pagoRepository.findByPedidoIdPedido(pedidoId)
            .orElseThrow(() -> new RuntimeException("No se encontró pago para este pedido"));
    }
    
    // =========================================================
    // RE-SUBIR COMPROBANTE (cuando fue rechazado)
    // =========================================================
    public Pedido resubirComprobante(Integer pedidoId, MultipartFile archivo) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        
        // Validar que el pago fue rechazado
        if (pedido.getEstadoPago() != EstadoPago.RECHAZADO) {
            throw new RuntimeException("Solo se puede re-subir comprobante si el pago fue rechazado");
        }
        
        return subirComprobanteTransferencia(pedidoId, archivo);
    }
    
    // =========================================================
    // MÉTODOS PRIVADOS DE APOYO
    // =========================================================
    
    private Pago crearPagoBase(Pedido pedido, Double monto, MetodoPago metodo, EstadoPago estado) {
        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setIdConsumidor(pedido.getConsumidor().getIdConsumidor());
        pago.setMonto(monto != null ? monto : pedido.getTotal());
        pago.setMetodo(metodo);
        pago.setEstado(estado);
        pago.setFechaPago(LocalDateTime.now());
        
        // Establecer relación bidireccional
        pedido.setPago(pago);
        
        return pagoRepository.save(pago);
    }
    
    
    private void validarDatosTarjeta(PagoTarjetaRequest request) {
        if (request.getNumeroTarjeta() == null || 
            request.getNumeroTarjeta().replace(" ", "").length() < 13) {
            throw new RuntimeException("Número de tarjeta inválido");
        }
        
        if (request.getCvv() == null || request.getCvv().length() < 3) {
            throw new RuntimeException("CVV inválido");
        }
        
        if (request.getFechaExpiracion() == null || request.getFechaExpiracion().isEmpty()) {
            throw new RuntimeException("Fecha de expiración inválida");
        }
        
        if (request.getTitular() == null || request.getTitular().isEmpty()) {
            throw new RuntimeException("Titular inválido");
        }
    }
    
    // =========================================================
    // MÉTODOS ADICIONALES ÚTILES
    // =========================================================
    
    public List<Pedido> obtenerPedidosPorEstadoPago(Integer vendedorId, EstadoPago estadoPago) {
        return pedidoRepository.findByVendedor_IdVendedorAndEstadoPago(vendedorId, estadoPago);
    }
    
   /* public List<Pedido> obtenerHistorialPagosConsumidor(Integer consumidorId) {
        return pedidoRepository.findByConsumidor_IdConsumidorAndEstadoPagoIsNotNull(consumidorId);
    }
    */
    
    public Double obtenerTotalPagadoPorVendedor(Integer vendedorId) {
        return pedidoRepository.findByVendedor_IdVendedorAndEstadoPago(vendedorId, EstadoPago.PAGADO)
            .stream()
            .mapToDouble(Pedido::getTotal)
            .sum();
    }
}