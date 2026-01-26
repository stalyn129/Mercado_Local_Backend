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
    private final Path comprobantesLocation;

    public FileStorageServiceImpl(
            @Value("${mercadolocalia.upload-dir:uploads}") String uploadDir
    ) {
        // Carpeta base: /uploads/productos
        this.rootLocation = Paths.get(uploadDir, "productos").toAbsolutePath().normalize();
        
        // Carpeta para comprobantes: /uploads/comprobantes
        this.comprobantesLocation = Paths.get(uploadDir, "comprobantes").toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.rootLocation);
            Files.createDirectories(this.comprobantesLocation);
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

    @Override
    public String storeFile(MultipartFile file) {
        // Implementación genérica para guardar cualquier archivo
        return guardarArchivo(file, this.rootLocation, "/uploads/productos/");
    }

    @Override
    public String storeComprobante(MultipartFile file) {
        // Específico para guardar comprobantes de pago
        return guardarArchivo(file, this.comprobantesLocation, "/uploads/comprobantes/");
    }

    @Override
    public byte[] loadFile(String fileName) throws IOException {
        try {
            Path filePath = this.rootLocation.resolve(fileName).normalize();
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new IOException("No se pudo cargar el archivo: " + fileName, e);
        }
    }

    @Override
    public byte[] loadComprobante(String fileName) throws IOException {
        try {
            Path filePath = this.comprobantesLocation.resolve(fileName).normalize();
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new IOException("No se pudo cargar el comprobante: " + fileName, e);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.rootLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo eliminar el archivo: " + fileName, e);
        }
    }

    @Override
    public void deleteComprobante(String fileName) {
        try {
            Path filePath = this.comprobantesLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo eliminar el comprobante: " + fileName, e);
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/productos/")
                .path(fileName)
                .toUriString();
    }

    @Override
    public String getComprobanteUrl(String fileName) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/comprobantes/")
                .path(fileName)
                .toUriString();
    }

    // Método privado para reutilizar lógica de guardado
    private String guardarArchivo(MultipartFile file, Path location, String urlPath) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (contentType != null && !isValidContentType(contentType)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido: " + contentType);
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

            Path destino = location.resolve(fileName);

            // Validar tamaño (máximo 10MB)
            if (file.getSize() > 10 * 1024 * 1024) { // 10MB
                throw new IllegalArgumentException("El archivo es demasiado grande. Máximo 10MB");
            }

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            // Construir URL pública
            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(urlPath)
                    .path(fileName)
                    .toUriString();

            return url;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }
    }

    // Validar tipos MIME permitidos
    private boolean isValidContentType(String contentType) {
        // Para imágenes y PDFs (comprobantes)
        return contentType.startsWith("image/") || 
               contentType.equals("application/pdf") ||
               contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/gif");
    }

    @Override
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        return contentType != null && 
               (contentType.equals("image/jpeg") || 
                contentType.equals("image/png") || 
                contentType.equals("image/jpg") ||
                contentType.equals("image/gif"));
    }

    @Override
    public boolean isValidComprobante(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        return contentType != null && 
               (contentType.startsWith("image/") || 
                contentType.equals("application/pdf"));
    }

    @Override
    public long getFileSize(MultipartFile file) {
        return file != null ? file.getSize() : 0;
    }
}