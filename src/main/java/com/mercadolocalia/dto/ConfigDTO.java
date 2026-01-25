// src/main/java/com/mercadolocalia/dto/ConfigDTO.java
package com.mercadolocalia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigDTO {
    // Seguridad
    private Integer tokenExpiration;
    private Integer sessionTimeout;
    private Integer maxLoginAttempts;
    private Boolean require2FA;
    private Integer passwordMinLength;
    private Boolean emailNotifications;
    
    // Parámetros
    private String systemName;
    private Double commissionRate;
    private Integer productLimitPerVendor;
    private String currency;
    private String timezone;
    
    // UI
    private String theme;
    private String primaryColor;
    private String secondaryColor;
    private Boolean enableAnimations;
    
    // Auditoría
    private Boolean autoBackup;
    private String backupFrequency;
    private Integer keepLogsDays;
}