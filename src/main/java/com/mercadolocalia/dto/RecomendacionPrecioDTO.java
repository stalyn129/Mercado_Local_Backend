package com.mercadolocalia.dto;

import java.util.List;

/**
 * DTO para enviar la respuesta de análisis de precios de la IA al Frontend
 */
public class RecomendacionPrecioDTO {
    
    private boolean similar_found;
    private double precio_promedio;
    private double precio_ingresado;
    private double recomendado;
    private String estado;
    private List<ProductoSimilDTO> productos_similares;

    // Constructor vacío (necesario para Jackson)
    public RecomendacionPrecioDTO() {}

    // Constructor completo para facilitar la creación en el Controller
    public RecomendacionPrecioDTO(boolean similar_found, double precio_promedio, 
                                  double precio_ingresado, double recomendado, 
                                  String estado, List<ProductoSimilDTO> productos_similares) {
        this.similar_found = similar_found;
        this.precio_promedio = precio_promedio;
        this.precio_ingresado = precio_ingresado;
        this.recomendado = recomendado;
        this.estado = estado;
        this.productos_similares = productos_similares;
    }

    // --- Clase Interna para los productos de comparación ---
    public static class ProductoSimilDTO {
        private String nombre;
        private double precio;

        public ProductoSimilDTO() {}

        public ProductoSimilDTO(String nombre, double precio) {
            this.nombre = nombre;
            this.precio = precio;
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public double getPrecio() { return precio; }
        public void setPrecio(double precio) { this.precio = precio; }
    }

    // --- Getters y Setters de la clase principal ---
    public boolean isSimilar_found() { return similar_found; }
    public void setSimilar_found(boolean similar_found) { this.similar_found = similar_found; }

    public double getPrecio_promedio() { return precio_promedio; }
    public void setPrecio_promedio(double precio_promedio) { this.precio_promedio = precio_promedio; }

    public double getPrecio_ingresado() { return precio_ingresado; }
    public void setPrecio_ingresado(double precio_ingresado) { this.precio_ingresado = precio_ingresado; }

    public double getRecomendado() { return recomendado; }
    public void setRecomendado(double recomendado) { this.recomendado = recomendado; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<ProductoSimilDTO> getProductos_similares() { return productos_similares; }
    public void setProductos_similares(List<ProductoSimilDTO> productos_similares) { 
        this.productos_similares = productos_similares; 
    }
}