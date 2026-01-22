package com.mercadolocalia.entities;

public enum EstadoPedidoVendedor {
    NUEVO("Nuevo", "Pedido recibido, pendiente de procesar"),
    EN_PROCESO("En Proceso", "Pedido en preparación"),
    DESPACHADO("Despachado", "Pedido enviado al cliente"),
    ENTREGADO("Entregado", "Pedido entregado al cliente"),
    CANCELADO("Cancelado", "Pedido cancelado");
    
    private final String nombre;
    private final String descripcion;
    
    EstadoPedidoVendedor(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}