// src/main/java/com/mercadolocalia/entities/AuditoriaConfig.java
package com.mercadolocalia.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AuditoriaConfig {
    private Boolean respaldoAutomatico = true;
    private String frecuenciaRespaldo = "daily";
    private Integer mantenerLogsDias = 30;
}