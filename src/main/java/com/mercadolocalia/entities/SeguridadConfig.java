// src/main/java/com/mercadolocalia/entities/SeguridadConfig.java
package com.mercadolocalia.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SeguridadConfig {
    private Integer expiracionToken = 24; // horas
    private Integer tiempoSesion = 60; // minutos
    private Integer maxIntentosLogin = 5;
    private Boolean requiere2FA = false;
    private Integer longitudMinimaPassword = 8;
    private Boolean notificacionesEmail = true;
}