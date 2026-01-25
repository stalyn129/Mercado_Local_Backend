// src/main/java/com/mercadolocalia/entities/UiConfig.java
package com.mercadolocalia.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class UiConfig {
    private String tema = "light";
    private String colorPrimario = "#FF6B35";
    private String colorSecundario = "#8B5CF6";
    private Boolean animacionesHabilitadas = true;
}