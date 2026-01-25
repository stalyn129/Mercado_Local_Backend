// src/main/java/com/mercadolocalia/entities/ParametrosConfig.java
package com.mercadolocalia.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ParametrosConfig {
    private String nombreSistema = "MercadoLocal-IA";
    private Double tasaComision = 15.0; // porcentaje
    private Integer limiteProductosVendedor = 50;
    private String moneda = "USD";
    private String zonaHoraria = "America/Mexico_City";
}