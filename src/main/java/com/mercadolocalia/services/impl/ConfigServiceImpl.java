// src/main/java/com/mercadolocalia/services/impl/ConfigServiceImpl.java
package com.mercadolocalia.services.impl;

import com.mercadolocalia.dto.ConfigDTO;
import com.mercadolocalia.entities.Config;
import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.repositories.ConfigRepository;
import com.mercadolocalia.services.ConfigService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigServiceImpl implements ConfigService {
    
    private final ConfigRepository configRepository;
    
    @Override
    public ConfigDTO obtenerConfiguracion() {
        try {
            log.info("Obteniendo configuración del sistema...");
            Config config = configRepository.findBySistema("mercadolocal")
                    .orElseGet(() -> {
                        log.info("No se encontró configuración, creando por defecto");
                        return crearConfiguracionDefault();
                    });
            
            log.info("Configuración obtenida exitosamente");
            return convertirAConfigDTO(config);
            
        } catch (Exception e) {
            log.error("Error al obtener configuración: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener configuración: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ConfigDTO actualizarConfiguracion(ConfigDTO configDTO, Usuario usuario) {
        try {
            log.info("Actualizando configuración para usuario ID: {}", usuario.getIdUsuario());
            
            Config config = configRepository.findBySistema("mercadolocal")
                    .orElseGet(() -> {
                        log.info("No se encontró configuración existente, creando nueva");
                        return crearConfiguracionDefault();
                    });
            
            // Validar datos de entrada
            validarConfigDTO(configDTO);
            
            // Actualizar seguridad
            if (configDTO.getTokenExpiration() != null) {
                config.getSeguridad().setExpiracionToken(configDTO.getTokenExpiration());
            }
            if (configDTO.getSessionTimeout() != null) {
                config.getSeguridad().setTiempoSesion(configDTO.getSessionTimeout());
            }
            if (configDTO.getMaxLoginAttempts() != null) {
                config.getSeguridad().setMaxIntentosLogin(configDTO.getMaxLoginAttempts());
            }
            if (configDTO.getRequire2FA() != null) {
                config.getSeguridad().setRequiere2FA(configDTO.getRequire2FA());
            }
            if (configDTO.getPasswordMinLength() != null) {
                config.getSeguridad().setLongitudMinimaPassword(configDTO.getPasswordMinLength());
            }
            if (configDTO.getEmailNotifications() != null) {
                config.getSeguridad().setNotificacionesEmail(configDTO.getEmailNotifications());
            }
            
            // Actualizar parámetros
            if (configDTO.getSystemName() != null) {
                config.getParametros().setNombreSistema(configDTO.getSystemName());
            }
            if (configDTO.getCommissionRate() != null) {
                config.getParametros().setTasaComision(configDTO.getCommissionRate());
            }
            if (configDTO.getProductLimitPerVendor() != null) {
                config.getParametros().setLimiteProductosVendedor(configDTO.getProductLimitPerVendor());
            }
            if (configDTO.getCurrency() != null) {
                config.getParametros().setMoneda(configDTO.getCurrency());
            }
            if (configDTO.getTimezone() != null) {
                config.getParametros().setZonaHoraria(configDTO.getTimezone());
            }
            
            // Actualizar UI
            if (configDTO.getTheme() != null) {
                config.getUi().setTema(configDTO.getTheme());
            }
            if (configDTO.getPrimaryColor() != null) {
                config.getUi().setColorPrimario(configDTO.getPrimaryColor());
            }
            if (configDTO.getSecondaryColor() != null) {
                config.getUi().setColorSecundario(configDTO.getSecondaryColor());
            }
            if (configDTO.getEnableAnimations() != null) {
                config.getUi().setAnimacionesHabilitadas(configDTO.getEnableAnimations());
            }
            
            // Actualizar auditoría
            if (configDTO.getAutoBackup() != null) {
                config.getAuditoria().setRespaldoAutomatico(configDTO.getAutoBackup());
            }
            if (configDTO.getBackupFrequency() != null) {
                config.getAuditoria().setFrecuenciaRespaldo(configDTO.getBackupFrequency());
            }
            if (configDTO.getKeepLogsDays() != null) {
                config.getAuditoria().setMantenerLogsDias(configDTO.getKeepLogsDays());
            }
            
            // Actualizar metadata
            config.setActualizadoPor(usuario);
            config.setActualizadoEn(LocalDateTime.now());
            
            log.info("Guardando configuración...");
            Config configGuardada = configRepository.save(config);
            log.info("Configuración guardada exitosamente con ID: {}", configGuardada.getId());
            
            return convertirAConfigDTO(configGuardada);
            
        } catch (Exception e) {
            log.error("Error al actualizar configuración: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar configuración: " + e.getMessage());
        }
    }
    
    private void validarConfigDTO(ConfigDTO configDTO) {
        // Validar seguridad
        if (configDTO.getTokenExpiration() != null && (configDTO.getTokenExpiration() < 1 || configDTO.getTokenExpiration() > 720)) {
            throw new IllegalArgumentException("La expiración del token debe estar entre 1 y 720 horas");
        }
        if (configDTO.getSessionTimeout() != null && (configDTO.getSessionTimeout() < 5 || configDTO.getSessionTimeout() > 1440)) {
            throw new IllegalArgumentException("El tiempo de sesión debe estar entre 5 y 1440 minutos");
        }
        if (configDTO.getPasswordMinLength() != null && (configDTO.getPasswordMinLength() < 4 || configDTO.getPasswordMinLength() > 32)) {
            throw new IllegalArgumentException("La longitud mínima de contraseña debe estar entre 4 y 32 caracteres");
        }
        // Validar comisión
        if (configDTO.getCommissionRate() != null && (configDTO.getCommissionRate() < 0 || configDTO.getCommissionRate() > 100)) {
            throw new IllegalArgumentException("La comisión debe estar entre 0 y 100%");
        }
    }
    
    @Override
    @Transactional
    public ConfigDTO restaurarConfiguracionDefault(Usuario usuario) {
        try {
            log.info("Restaurando configuración a valores por defecto para usuario ID: {}", usuario.getIdUsuario());
            
            // Eliminar configuración existente si existe
            configRepository.findBySistema("mercadolocal").ifPresent(config -> {
                configRepository.delete(config);
                log.info("Configuración existente eliminada");
            });
            
            // Crear nueva configuración por defecto
            Config config = crearConfiguracionDefault();
            config.setActualizadoPor(usuario);
            config.setActualizadoEn(LocalDateTime.now());
            
            Config configGuardada = configRepository.save(config);
            log.info("Configuración restaurada exitosamente con ID: {}", configGuardada.getId());
            
            return convertirAConfigDTO(configGuardada);
            
        } catch (Exception e) {
            log.error("Error al restaurar configuración: {}", e.getMessage(), e);
            throw new RuntimeException("Error al restaurar configuración: " + e.getMessage());
        }
    }
    
    // ============ MÉTODOS DE UTILIDAD ============
    
    private Config crearConfiguracionDefault() {
        Config config = new Config();
        config.setSistema("mercadolocal");
        return config;
    }
    
    private ConfigDTO convertirAConfigDTO(Config config) {
        ConfigDTO dto = new ConfigDTO();
        
        // Seguridad
        dto.setTokenExpiration(config.getSeguridad().getExpiracionToken());
        dto.setSessionTimeout(config.getSeguridad().getTiempoSesion());
        dto.setMaxLoginAttempts(config.getSeguridad().getMaxIntentosLogin());
        dto.setRequire2FA(config.getSeguridad().getRequiere2FA());
        dto.setPasswordMinLength(config.getSeguridad().getLongitudMinimaPassword());
        dto.setEmailNotifications(config.getSeguridad().getNotificacionesEmail());
        
        // Parámetros
        dto.setSystemName(config.getParametros().getNombreSistema());
        dto.setCommissionRate(config.getParametros().getTasaComision());
        dto.setProductLimitPerVendor(config.getParametros().getLimiteProductosVendedor());
        dto.setCurrency(config.getParametros().getMoneda());
        dto.setTimezone(config.getParametros().getZonaHoraria());
        
        // UI
        dto.setTheme(config.getUi().getTema());
        dto.setPrimaryColor(config.getUi().getColorPrimario());
        dto.setSecondaryColor(config.getUi().getColorSecundario());
        dto.setEnableAnimations(config.getUi().getAnimacionesHabilitadas());
        
        // Auditoría
        dto.setAutoBackup(config.getAuditoria().getRespaldoAutomatico());
        dto.setBackupFrequency(config.getAuditoria().getFrecuenciaRespaldo());
        dto.setKeepLogsDays(config.getAuditoria().getMantenerLogsDias());
        
        return dto;
    }
}