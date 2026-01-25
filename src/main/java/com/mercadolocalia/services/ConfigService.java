// src/main/java/com/mercadolocalia/services/ConfigService.java
package com.mercadolocalia.services;

import com.mercadolocalia.dto.ConfigDTO;
import com.mercadolocalia.entities.Usuario;

public interface ConfigService {
    
    /**
     * Obtiene la configuración actual del sistema
     * @return ConfigDTO con toda la configuración
     */
    ConfigDTO obtenerConfiguracion();
    
    /**
     * Actualiza la configuración del sistema
     * @param configDTO Datos de configuración a actualizar
     * @param usuario Usuario que realiza la actualización
     * @return ConfigDTO actualizada
     */
    ConfigDTO actualizarConfiguracion(ConfigDTO configDTO, Usuario usuario);
    
    /**
     * Restaura la configuración a valores por defecto
     * @param usuario Usuario que realiza la restauración
     * @return ConfigDTO restaurada
     */
    ConfigDTO restaurarConfiguracionDefault(Usuario usuario);
}