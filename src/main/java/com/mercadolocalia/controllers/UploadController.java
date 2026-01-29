package com.mercadolocalia.controllers;

import com.mercadolocalia.dto.FileUploadResponse;
import com.mercadolocalia.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = {
		"http://localhost:5173",      // Vite/React dev
	    "http://127.0.0.1:5173",      // Vite/React dev
	    "http://localhost:3000",      // React/CRA dev
	    "http://localhost:8081",      // Expo web
	    "http://192.168.1.13:8081",   // Expo en tu IP (¡IMPORTANTE!)
	    "http://192.168.1.13:3000",   // React web en tu IP
	    "exp://192.168.1.13:8081",    // Expo protocol
	    "http://192.168.1.13",        // Tu IP sin puerto
	    "http://192.168.1.13:5173"    // Vite en tu IP
})
public class UploadController {

    @Autowired
    private FileStorageService fileStorageService;
    
    @Value("${app.base-url:}")
    private String baseUrl;

    @PostMapping("/producto")
    public ResponseEntity<FileUploadResponse> uploadImagenProducto(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(FileUploadResponse.error("El archivo está vacío"));
            }
            
            // Validar que sea una imagen
            if (!fileStorageService.isValidImage(file)) {
                return ResponseEntity.badRequest()
                    .body(FileUploadResponse.error("Formato de imagen no válido. Use JPG, PNG, JPEG, GIF o WEBP"));
            }
            
            // Validar tamaño
            long fileSize = fileStorageService.getFileSize(file);
            if (fileSize > 10 * 1024 * 1024) { // 10MB máximo
                return ResponseEntity.badRequest()
                    .body(FileUploadResponse.error("La imagen no puede superar los 10MB"));
            }
            
            // Guardar imagen y obtener ruta relativa
            String relativePath = fileStorageService.guardarImagenProducto(file);
            
            // Extraer nombre de archivo de la ruta
            String filename = extractFilename(relativePath);
            
            // Construir URL completa
            String fullUrl = buildFullUrl(relativePath);
            
            // Crear respuesta
            FileUploadResponse response = FileUploadResponse.success(
                relativePath,
                fullUrl,
                filename
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(FileUploadResponse.error("Error al subir la imagen: " + e.getMessage()));
        }
    }
    
    private String extractFilename(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
    
    private String buildFullUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }
        
        // Si ya es una URL completa
        if (relativePath.startsWith("http")) {
            return relativePath;
        }
        
        // Si tenemos baseUrl configurada en properties, usarla
        if (baseUrl != null && !baseUrl.trim().isEmpty()) {
            // Asegurar que relativePath empiece con /
            String path = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
            return baseUrl + path;
        }
        
        // Si no, construir desde el contexto actual (para desarrollo)
        return ServletUriComponentsBuilder.fromCurrentContextPath()
            .path(relativePath.startsWith("/") ? relativePath : "/" + relativePath)
            .toUriString();
    }
}