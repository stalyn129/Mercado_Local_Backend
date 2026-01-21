package com.mercadolocalia.dto;


import lombok.Data;
import java.util.List;

import com.mercadolocalia.entities.Pedido;

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
            this.metodoPago = pedidos.get(0).getMetodoPago();
            this.fechaCompra = pedidos.get(0).getFechaPedido().toString();
        }
    }
}