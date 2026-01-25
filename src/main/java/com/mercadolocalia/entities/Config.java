// src/main/java/com/mercadolocalia/entities/Config.java (Actualizado)
package com.mercadolocalia.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuraciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Config {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String sistema = "mercadolocal";
    
    // Seguridad
    @Embedded
    private SeguridadConfig seguridad = new SeguridadConfig();
    
    // Parámetros
    @Embedded
    private ParametrosConfig parametros = new ParametrosConfig();
    
    // UI
    @Embedded
    private UiConfig ui = new UiConfig();
    
    // Auditoría
    @Embedded
    private AuditoriaConfig auditoria = new AuditoriaConfig();
    
    // Metadata
    @ManyToOne
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;
    
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn = LocalDateTime.now();
}