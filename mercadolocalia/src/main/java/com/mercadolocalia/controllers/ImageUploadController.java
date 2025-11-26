package com.mercadolocalia.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;

@RestController
@RequestMapping("/uploads")
public class ImageUploadController {

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/producto")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Archivo vacío");
            }

            // Crear carpeta si no existe
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            // Nombre único
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // Ruta destino
            Path path = Paths.get(UPLOAD_DIR + fileName);

            // Guardar archivo
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // URL pública (localhost)
            String fileUrl = "http://localhost:8080/uploads/" + fileName;

            return ResponseEntity.ok(fileUrl);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
