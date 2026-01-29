package com.mercadolocalia.services.impl;

import com.mercadolocalia.services.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path rootLocation;
    private final Path comprobantesLocation;
    
    @Value("${mercadolocalia.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Value("${mercadolocalia.allowed-image-types:image/jpeg,image/jpg,image/png,image/gif,image/webp}")
    private String allowedImageTypes;
    
    @Value("${mercadolocalia.allowed-document-types:application/pdf,image/jpeg,image/jpg,image/png}")
    private String allowedDocumentTypes;
    
    @Value("${mercadolocalia.max-image-size:10485760}")
    private long maxImageSize;
    
    @Value("${mercadolocalia.max-document-size:10485760}")
    private long maxDocumentSize;

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
            
            System.out.println("=========================================");
            System.out.println("📁 SISTEMA DE ARCHIVOS INICIALIZADO");
            System.out.println("=========================================");
            System.out.println("✅ Directorio de productos: " + this.rootLocation);
            System.out.println("✅ Directorio de comprobantes: " + this.comprobantesLocation);
            System.out.println("✅ URL base configurada: " + baseUrl);
            System.out.println("✅ Tamaño máximo imagen: " + (maxImageSize / 1024 / 1024) + "MB");
            System.out.println("✅ Tipos de imagen permitidos: " + allowedImageTypes);
            System.out.println("=========================================");
            
        } catch (IOException e) {
            System.err.println("❌ ERROR: No se pudo crear la carpeta de uploads");
            System.err.println("   Ruta: " + this.rootLocation);
            System.err.println("   Error: " + e.getMessage());
            throw new RuntimeException("No se pudo crear la carpeta de uploads", e);
        }
    }

    @Override
    public String guardarImagenProducto(MultipartFile file) {
        System.out.println("🖼️ Iniciando guardado de imagen de producto...");
        
        if (file == null || file.isEmpty()) {
            System.out.println("❌ Archivo de imagen vacío");
            throw new IllegalArgumentException("El archivo de imagen está vacío");
        }

        try {
            // Validar tipo de imagen
            if (!isValidImage(file)) {
                String contentType = file.getContentType();
                System.out.println("❌ Tipo de imagen no permitido: " + contentType);
                System.out.println("   Tipos permitidos: " + allowedImageTypes);
                throw new IllegalArgumentException("Tipo de imagen no permitido: " + contentType + 
                    ". Tipos permitidos: " + allowedImageTypes);
            }
            
            // Validar tamaño
            long fileSize = getFileSize(file);
            System.out.println("   Tamaño del archivo: " + fileSize + " bytes");
            
            if (fileSize > maxImageSize) {
                System.out.println("❌ Imagen excede tamaño máximo: " + fileSize + " > " + maxImageSize);
                throw new IllegalArgumentException("La imagen es demasiado grande. Máximo: " + 
                    (maxImageSize / 1024 / 1024) + "MB");
            }

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            System.out.println("   Nombre original: " + originalFilename);
            
            String extension = obtenerExtension(originalFilename, file.getContentType());
            System.out.println("   Extensión determinada: " + extension);

            // Nombre único
            String fileName = UUID.randomUUID().toString() + extension;
            System.out.println("   Nombre único generado: " + fileName);

            Path destino = this.rootLocation.resolve(fileName);
            System.out.println("   Ruta destino: " + destino);

            // Guardar archivo
            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            
            // Ruta relativa para guardar en BD
            String relativePath = "/uploads/productos/" + fileName;
            System.out.println("✅ Imagen guardada exitosamente");
            System.out.println("   Ruta relativa: " + relativePath);
            System.out.println("   URL accesible: " + baseUrl + relativePath);
            
            return relativePath;

        } catch (IOException e) {
            System.err.println("❌ Error IO al guardar imagen: " + e.getMessage());
            throw new RuntimeException("Error al guardar la imagen del producto: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Error de validación: " + e.getMessage());
            throw e; // Re-lanzar para que el controller lo maneje
        }
    }

    @Override
    public String storeComprobante(MultipartFile file) {
        System.out.println("📄 Iniciando guardado de comprobante...");
        return guardarArchivo(file, this.comprobantesLocation, "/uploads/comprobantes/", 
                             allowedDocumentTypes, maxDocumentSize, "comprobante");
    }

    @Override
    public String storeFile(MultipartFile file) {
        System.out.println("📎 Iniciando guardado de archivo genérico...");
        return guardarArchivo(file, this.rootLocation, "/uploads/productos/", 
                             allowedImageTypes, maxImageSize, "archivo");
    }

    @Override
    public byte[] loadFile(String fileName) throws IOException {
        try {
            Path filePath = this.rootLocation.resolve(fileName).normalize();
            System.out.println("📥 Cargando archivo: " + fileName);
            
            if (!Files.exists(filePath)) {
                System.out.println("❌ Archivo no encontrado: " + filePath);
                throw new IOException("Archivo no encontrado: " + fileName);
            }
            
            byte[] data = Files.readAllBytes(filePath);
            System.out.println("✅ Archivo cargado: " + fileName + " (" + data.length + " bytes)");
            return data;
            
        } catch (IOException e) {
            System.err.println("❌ Error cargando archivo: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public byte[] loadComprobante(String fileName) throws IOException {
        try {
            Path filePath = this.comprobantesLocation.resolve(fileName).normalize();
            System.out.println("📥 Cargando comprobante: " + fileName);
            
            if (!Files.exists(filePath)) {
                System.out.println("❌ Comprobante no encontrado: " + filePath);
                throw new IOException("Comprobante no encontrado: " + fileName);
            }
            
            return Files.readAllBytes(filePath);
            
        } catch (IOException e) {
            System.err.println("❌ Error cargando comprobante: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.rootLocation.resolve(fileName).normalize();
            boolean deleted = Files.deleteIfExists(filePath);
            
            if (deleted) {
                System.out.println("🗑️ Archivo eliminado: " + fileName);
            } else {
                System.out.println("⚠️ Archivo no encontrado: " + fileName);
            }
            
        } catch (IOException e) {
            System.err.println("❌ Error eliminando archivo: " + e.getMessage());
            throw new RuntimeException("No se pudo eliminar el archivo: " + fileName, e);
        }
    }

    @Override
    public void deleteComprobante(String fileName) {
        try {
            Path filePath = this.comprobantesLocation.resolve(fileName).normalize();
            boolean deleted = Files.deleteIfExists(filePath);
            
            if (deleted) {
                System.out.println("🗑️ Comprobante eliminado: " + fileName);
            }
            
        } catch (IOException e) {
            System.err.println("❌ Error eliminando comprobante: " + e.getMessage());
            throw new RuntimeException("No se pudo eliminar el comprobante: " + fileName, e);
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        System.out.println("🔗 Construyendo URL para archivo: " + fileName);
        
        if (fileName == null || fileName.trim().isEmpty()) {
            System.out.println("⚠️ Nombre de archivo vacío");
            return null;
        }
        
        // Si ya es una URL completa
        if (fileName.startsWith("http")) {
            System.out.println("✅ Ya es URL completa: " + fileName);
            return fileName;
        }
        
        // Si es ruta relativa
        if (fileName.startsWith("/uploads/")) {
            String url = baseUrl + fileName;
            System.out.println("🔗 URL construida desde ruta: " + url);
            return url;
        }
        
        // Solo nombre de archivo
        String url = baseUrl + "/uploads/productos/" + fileName;
        System.out.println("🔗 URL construida desde nombre: " + url);
        return url;
    }

    @Override
    public String getComprobanteUrl(String fileName) {
        System.out.println("🔗 Construyendo URL para comprobante: " + fileName);
        
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        
        if (fileName.startsWith("http")) {
            return fileName;
        }
        
        if (fileName.startsWith("/uploads/")) {
            return baseUrl + fileName;
        }
        
        return baseUrl + "/uploads/comprobantes/" + fileName;
    }

    @Override
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        boolean isValid = contentType != null && Arrays.asList(allowedImageTypes.split(","))
            .contains(contentType.trim().toLowerCase());
        
        System.out.println("🔍 Validando imagen - Tipo: " + contentType + " -> " + (isValid ? "✅" : "❌"));
        return isValid;
    }

    @Override
    public boolean isValidComprobante(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        boolean isValid = contentType != null && Arrays.asList(allowedDocumentTypes.split(","))
            .contains(contentType.trim().toLowerCase());
        
        System.out.println("🔍 Validando comprobante - Tipo: " + contentType + " -> " + (isValid ? "✅" : "❌"));
        return isValid;
    }

    @Override
    public long getFileSize(MultipartFile file) {
        return file != null ? file.getSize() : 0;
    }

    @Override
    public String getFilenameFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        
        String normalizedPath = path.replace('\\', '/');
        int lastSlash = normalizedPath.lastIndexOf('/');
        
        if (lastSlash >= 0) {
            return normalizedPath.substring(lastSlash + 1);
        }
        
        return path;
    }

    @Override
    public String getRelativePathFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // Si ya es ruta relativa
        if (url.startsWith("/uploads/")) {
            return url;
        }
        
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            
            if (path != null && path.startsWith("/uploads/")) {
                return path;
            }
        } catch (URISyntaxException e) {
            // Si no es una URL válida, intentar extraer manualmente
        }
        
        // Extraer manualmente
        if (url.contains("/uploads/")) {
            int index = url.indexOf("/uploads/");
            return url.substring(index);
        }
        
        return url;
    }

    // =============================================
    // MÉTODOS PRIVADOS AUXILIARES
    // =============================================
    
    private String guardarArchivo(MultipartFile file, Path location, String urlPath, 
                                 String allowedTypes, long maxSize, String tipo) {
        System.out.println("💾 Guardando " + tipo + "...");
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Validar tipo
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(allowedTypes.split(","))
                .contains(contentType.trim().toLowerCase())) {
            throw new IllegalArgumentException("Tipo de archivo no permitido para " + tipo + ": " + contentType);
        }

        try {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = obtenerExtension(originalFilename, contentType);
            
            // Validar tamaño
            if (file.getSize() > maxSize) {
                throw new IllegalArgumentException("El " + tipo + " es demasiado grande. Máximo: " + 
                    (maxSize / 1024 / 1024) + "MB");
            }

            // Nombre único
            String fileName = UUID.randomUUID().toString() + extension;
            Path destino = location.resolve(fileName);

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            
            String relativePath = urlPath + fileName;
            System.out.println("✅ " + tipo.substring(0, 1).toUpperCase() + tipo.substring(1) + " guardado: " + relativePath);
            
            return relativePath;

        } catch (IOException e) {
            System.err.println("❌ Error guardando " + tipo + ": " + e.getMessage());
            throw new RuntimeException("Error al guardar el " + tipo, e);
        }
    }
    
    private String obtenerExtension(String filename, String contentType) {
        String extension = "";
        
        if (filename != null) {
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = filename.substring(dotIndex).toLowerCase();
            }
        }
        
        // Si no tiene extensión, determinar por contentType
        if (extension.isEmpty() && contentType != null) {
            if (contentType.equals("image/jpeg") || contentType.equals("image/jpg")) {
                extension = ".jpg";
            } else if (contentType.equals("image/png")) {
                extension = ".png";
            } else if (contentType.equals("image/gif")) {
                extension = ".gif";
            } else if (contentType.equals("image/webp")) {
                extension = ".webp";
            } else if (contentType.equals("application/pdf")) {
                extension = ".pdf";
            } else {
                extension = ".bin";
            }
        }
        
        return extension;
    }
    
    // Método para verificar si el servicio está funcionando
    public String getServiceStatus() {
        try {
            boolean productosDirExists = Files.exists(this.rootLocation);
            boolean comprobantesDirExists = Files.exists(this.comprobantesLocation);
            boolean productosDirWritable = Files.isWritable(this.rootLocation);
            
            return String.format(
                "FileStorageService Status:\n" +
                "  ✅ Directorio productos: %s (%s)\n" +
                "  ✅ Directorio comprobantes: %s\n" +
                "  ✅ URL base: %s\n" +
                "  ✅ Permisos escritura: %s\n" +
                "  ✅ Máximo imagen: %dMB\n" +
                "  ✅ Tipos permitidos: %s",
                this.rootLocation,
                productosDirExists ? "EXISTE" : "NO EXISTE",
                comprobantesDirExists ? "EXISTE" : "NO EXISTE",
                baseUrl,
                productosDirWritable ? "OK" : "SIN PERMISOS",
                maxImageSize / 1024 / 1024,
                allowedImageTypes
            );
        } catch (Exception e) {
            return "Error obteniendo estado: " + e.getMessage();
        }
    }
}