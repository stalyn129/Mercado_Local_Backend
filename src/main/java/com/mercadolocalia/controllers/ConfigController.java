// src/main/java/com/mercadolocalia/controllers/ConfigController.java
package com.mercadolocalia.controllers;

import com.mercadolocalia.dto.ConfigDTO;
import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.repositories.UsuarioRepository;
import com.mercadolocalia.services.ConfigService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@Slf4j
public class ConfigController {
    
    private final ConfigService configService;
    private final UsuarioRepository usuarioRepository;
    
    @GetMapping
    public ResponseEntity<?> obtenerConfiguracion() {
        try {
            log.info("Solicitando configuración del sistema...");
            
            // Obtener autenticación del contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                log.error("Usuario no autenticado");
                return ResponseEntity.status(401).body(Map.of(
                    "error", "No autenticado",
                    "message", "Debe iniciar sesión para acceder a esta configuración"
                ));
            }
            
            // Obtener el usuario del contexto de seguridad
            Object principal = authentication.getPrincipal();
            Usuario usuario = null;
            
            log.info("Principal class: {}", principal.getClass().getName());
            
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                String username = userDetails.getUsername(); // Esto es el correo
                
                // Buscar usuario por correo en la base de datos
                usuario = usuarioRepository.findByCorreo(username).orElse(null);
                log.info("Usuario encontrado por correo {}: {}", username, usuario != null ? usuario.getNombre() : "null");
                
            } else if (principal instanceof String) {
                String username = (String) principal;
                usuario = usuarioRepository.findByCorreo(username).orElse(null);
                log.info("Usuario encontrado por String correo {}: {}", username, usuario != null ? usuario.getNombre() : "null");
            }
            
            if (usuario == null) {
                log.error("No se pudo obtener el usuario del contexto de seguridad");
                // Aún así, devolvemos la configuración (sin usuario)
                ConfigDTO config = configService.obtenerConfiguracion();
                return ResponseEntity.ok(config);
            }
            
            log.info("Usuario obtenido: ID={}, Nombre={}, EsAdmin={}", 
                usuario.getIdUsuario(), usuario.getNombre(), usuario.getEsAdministrador());
            
            ConfigDTO config = configService.obtenerConfiguracion();
            log.info("Configuración obtenida exitosamente");
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            log.error("Error en obtenerConfiguracion: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al obtener configuración",
                "message", e.getMessage(),
                "details", e.getCause() != null ? e.getCause().getMessage() : "No hay detalles adicionales"
            ));
        }
    }
    
    @PutMapping
    public ResponseEntity<?> actualizarConfiguracion(@RequestBody ConfigDTO configDTO) {
        try {
            log.info("Actualizando configuración...");
            
            // Obtener usuario del contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "No autenticado",
                    "message", "Debe iniciar sesión para actualizar la configuración"
                ));
            }
            
            Object principal = authentication.getPrincipal();
            Usuario usuario = obtenerUsuarioDesdePrincipal(principal);
            
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Usuario no encontrado",
                    "message", "No se pudo identificar al usuario"
                ));
            }
            
            // Verificar que sea administrador
            if (usuario.getEsAdministrador() == null || !usuario.getEsAdministrador()) {
                log.warn("Usuario no administrador intentando actualizar configuración: ID={}", usuario.getIdUsuario());
                return ResponseEntity.status(403).body(Map.of(
                    "error", "No autorizado",
                    "message", "Solo los administradores pueden modificar la configuración"
                ));
            }
            
            ConfigDTO configActualizada = configService.actualizarConfiguracion(configDTO, usuario);
            log.info("Configuración actualizada exitosamente por usuario: ID={}", usuario.getIdUsuario());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Configuración actualizada exitosamente",
                "config", configActualizada
            ));
        } catch (Exception e) {
            log.error("Error en actualizarConfiguracion: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al actualizar configuración",
                "message", e.getMessage(),
                "details", e.getCause() != null ? e.getCause().getMessage() : "No hay detalles adicionales"
            ));
        }
    }
    
    @PostMapping("/restaurar")
    public ResponseEntity<?> restaurarConfiguracion() {
        try {
            log.info("Restaurando configuración...");
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "No autenticado",
                    "message", "Debe iniciar sesión para restaurar la configuración"
                ));
            }
            
            Object principal = authentication.getPrincipal();
            Usuario usuario = obtenerUsuarioDesdePrincipal(principal);
            
            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Usuario no encontrado",
                    "message", "No se pudo identificar al usuario"
                ));
            }
            
            // Verificar que sea administrador
            if (usuario.getEsAdministrador() == null || !usuario.getEsAdministrador()) {
                log.warn("Usuario no administrador intentando restaurar configuración: ID={}", usuario.getIdUsuario());
                return ResponseEntity.status(403).body(Map.of(
                    "error", "No autorizado",
                    "message", "Solo los administradores pueden restaurar la configuración"
                ));
            }
            
            ConfigDTO configRestaurada = configService.restaurarConfiguracionDefault(usuario);
            log.info("Configuración restaurada exitosamente por usuario: ID={}", usuario.getIdUsuario());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Configuración restaurada a valores por defecto",
                "config", configRestaurada
            ));
        } catch (Exception e) {
            log.error("Error en restaurarConfiguracion: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al restaurar configuración",
                "message", e.getMessage(),
                "details", e.getCause() != null ? e.getCause().getMessage() : "No hay detalles adicionales"
            ));
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        try {
            log.info("=== TEST ENDPOINT LLAMADO ===");
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("authentication", authentication != null ? "presente" : "null");
            
            if (authentication != null) {
                response.put("authenticated", authentication.isAuthenticated());
                response.put("principalClass", authentication.getPrincipal().getClass().getName());
                response.put("principal", authentication.getPrincipal().toString());
                response.put("authorities", authentication.getAuthorities().toString());
                
                Object principal = authentication.getPrincipal();
                Usuario usuario = obtenerUsuarioDesdePrincipal(principal);
                
                if (usuario != null) {
                    response.put("usuario", "encontrado");
                    response.put("userId", usuario.getIdUsuario());
                    response.put("nombre", usuario.getNombre());
                    response.put("correo", usuario.getCorreo());
                    response.put("esAdministrador", usuario.getEsAdministrador());
                    response.put("rol", usuario.getRol() != null ? usuario.getRol().getNombreRol() : "null");
                } else {
                    response.put("usuario", "no encontrado");
                    if (principal instanceof UserDetails) {
                        response.put("username", ((UserDetails) principal).getUsername());
                    } else if (principal instanceof String) {
                        response.put("username", principal);
                    }
                }
            }
            
            response.put("message", "Test endpoint funcionando");
            response.put("timestamp", new java.util.Date());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error en test endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error en test endpoint",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/simple")
    public ResponseEntity<?> configSimple() {
        try {
            log.info("=== SIMPLE ENDPOINT LLAMADO ===");
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("endpoint", "/api/admin/config/simple");
            response.put("backend", "Spring Boot Config Controller");
            response.put("timestamp", new java.util.Date());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error en simple endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/public-test")
    public ResponseEntity<?> publicTest() {
        try {
            log.info("=== PUBLIC TEST ENDPOINT ===");
            return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Endpoint público funcionando",
                "backend", "Spring Boot",
                "timestamp", new java.util.Date()
            ));
        } catch (Exception e) {
            log.error("Error en publicTest: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/exportar")
    public void exportarConfiguracion(HttpServletResponse response) throws IOException {
        try {
            log.info("Exportando configuración...");
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                response.setStatus(401);
                response.getWriter().write("{\"error\":\"No autenticado\"}");
                return;
            }
            
            Object principal = authentication.getPrincipal();
            Usuario usuario = obtenerUsuarioDesdePrincipal(principal);
            
            if (usuario == null) {
                response.setStatus(401);
                response.getWriter().write("{\"error\":\"Usuario no encontrado\"}");
                return;
            }
            
            if (usuario.getEsAdministrador() == null || !usuario.getEsAdministrador()) {
                response.setStatus(403);
                response.getWriter().write("{\"error\":\"No autorizado\"}");
                return;
            }
            
            ConfigDTO config = configService.obtenerConfiguracion();
            
            response.setContentType("application/json");
            response.setHeader("Content-Disposition", "attachment; filename=configuracion_sistema.json");
            response.setCharacterEncoding("UTF-8");
            
            String json = convertirAJson(config);
            response.getWriter().write(json);
            log.info("Configuración exportada exitosamente");
            
        } catch (Exception e) {
            log.error("Error en exportarConfiguracion: {}", e.getMessage(), e);
            response.setStatus(500);
            response.getWriter().write("{\"error\":\"Error al exportar configuración\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
    
    // Método auxiliar para obtener usuario desde el principal
    private Usuario obtenerUsuarioDesdePrincipal(Object principal) {
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            String username = userDetails.getUsername(); // Esto es el correo
            return usuarioRepository.findByCorreo(username).orElse(null);
        } else if (principal instanceof String) {
            String username = (String) principal;
            return usuarioRepository.findByCorreo(username).orElse(null);
        }
        return null;
    }
    
    private String convertirAJson(ConfigDTO config) {
        return "{" +
                "\"tokenExpiration\": " + config.getTokenExpiration() + "," +
                "\"sessionTimeout\": " + config.getSessionTimeout() + "," +
                "\"maxLoginAttempts\": " + config.getMaxLoginAttempts() + "," +
                "\"require2FA\": " + config.getRequire2FA() + "," +
                "\"passwordMinLength\": " + config.getPasswordMinLength() + "," +
                "\"emailNotifications\": " + config.getEmailNotifications() + "," +
                "\"systemName\": \"" + (config.getSystemName() != null ? config.getSystemName() : "") + "\"," +
                "\"commissionRate\": " + config.getCommissionRate() + "," +
                "\"productLimitPerVendor\": " + config.getProductLimitPerVendor() + "," +
                "\"currency\": \"" + (config.getCurrency() != null ? config.getCurrency() : "") + "\"," +
                "\"timezone\": \"" + (config.getTimezone() != null ? config.getTimezone() : "") + "\"," +
                "\"theme\": \"" + (config.getTheme() != null ? config.getTheme() : "") + "\"," +
                "\"primaryColor\": \"" + (config.getPrimaryColor() != null ? config.getPrimaryColor() : "") + "\"," +
                "\"secondaryColor\": \"" + (config.getSecondaryColor() != null ? config.getSecondaryColor() : "") + "\"," +
                "\"enableAnimations\": " + config.getEnableAnimations() + "," +
                "\"autoBackup\": " + config.getAutoBackup() + "," +
                "\"backupFrequency\": \"" + (config.getBackupFrequency() != null ? config.getBackupFrequency() : "") + "\"," +
                "\"keepLogsDays\": " + config.getKeepLogsDays() +
                "}";
    }
}