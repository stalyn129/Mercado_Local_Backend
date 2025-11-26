package com.mercadolocalia.services.impl;

import com.mercadolocalia.services.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path rootLocation;

    public FileStorageServiceImpl(
            @Value("${mercadolocalia.upload-dir:uploads}") String uploadDir
    ) {
        // Carpeta base: /uploads/productos
        this.rootLocation = Paths.get(uploadDir, "productos").toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear la carpeta de uploads: " + this.rootLocation, e);
        }
    }

    @Override
    public String guardarImagenProducto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen está vacío");
        }

        try {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = "";

            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }

            // Nombre único
            String fileName = UUID.randomUUID().toString() + extension;

            Path destino = this.rootLocation.resolve(fileName);

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            // Construir URL pública: http://localhost:8080/uploads/productos/<fileName>
            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/productos/")
                    .path(fileName)
                    .toUriString();

            return url;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen del producto", e);
        }
    }
}
