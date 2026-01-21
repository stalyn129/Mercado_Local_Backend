package com.mercadolocalia.dto;

import com.mercadolocalia.entities.Pedido;
import lombok.Data;
import java.util.List;

@Data
public class CheckoutResponseDTO {
    private String idCompraUnificada;
    private List<Pedido> pedidos;
    private Double subtotalGeneral;
    private Double ivaGeneral;
    private Double totalGeneral;
    private Integer cantidadPedidos;
    private Integer cantidadVendedores;
    private String mensaje;
    
    public CheckoutResponseDTO(String idCompraUnificada, List<Pedido> pedidos) {
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
        
        // Obtener cantidad de vendedores únicos
        this.cantidadVendedores = (int) pedidos.stream()
            .map(p -> p.getVendedor() != null ? p.getVendedor().getIdVendedor() : null)
            .distinct()
            .count();
        
        this.mensaje = "Checkout completado exitosamente";
    }
}