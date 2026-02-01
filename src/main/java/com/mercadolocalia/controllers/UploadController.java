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
public class UploadController {

    @Autowired
    private FileStorageService fileStorageService;

    // Para imágenes de productos
    @PostMapping("/producto")
    public ResponseEntity<FileUploadResponse> uploadImagenProducto(
            @RequestParam("file") MultipartFile file) {
        try {
            // Validaciones...
            String imageUrl = fileStorageService.guardarImagenProducto(file);
            
            return ResponseEntity.ok(FileUploadResponse.success(
                imageUrl,
                imageUrl,
                fileStorageService.getFilenameFromPath(imageUrl),
                "Imagen de producto subida exitosamente"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(FileUploadResponse.error("Error: " + e.getMessage()));
        }
    }

    // Para comprobantes (PDF o imágenes)
    @PostMapping("/comprobante")
    public ResponseEntity<FileUploadResponse> uploadComprobante(
            @RequestParam("file") MultipartFile file) {
        try {
            // Validaciones...
            String comprobanteUrl = fileStorageService.storeComprobante(file);
            
            return ResponseEntity.ok(FileUploadResponse.success(
                comprobanteUrl,
                comprobanteUrl,
                fileStorageService.getFilenameFromPath(comprobanteUrl),
                "Comprobante subido exitosamente"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(FileUploadResponse.error("Error: " + e.getMessage()));
        }
    }

    // Para eliminar archivos
    @DeleteMapping("/archivo")
    public ResponseEntity<FileUploadResponse> deleteArchivo(
            @RequestParam("url") String url) {
        try {
            fileStorageService.deleteFile(url);
            return ResponseEntity.ok(FileUploadResponse.success(
                null, null, null, "Archivo eliminado exitosamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(FileUploadResponse.error("Error eliminando: " + e.getMessage()));
        }
    }
}