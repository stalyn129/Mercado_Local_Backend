package com.mercadolocalia.services.impl;

import com.mercadolocalia.services.CloudStorageService;
import com.mercadolocalia.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

@Service
@Primary
public class CloudFileStorageServiceImpl implements FileStorageService {

    @Autowired
    private CloudStorageService cloudStorageService;
    
    @Value("${mercadolocalia.allowed-image-types:image/jpeg,image/jpg,image/png,image/gif,image/webp}")
    private String allowedImageTypes;
    
    @Value("${mercadolocalia.allowed-document-types:application/pdf,image/jpeg,image/jpg,image/png}")
    private String allowedDocumentTypes;
    
    @Value("${mercadolocalia.max-image-size:10485760}") // 10MB
    private long maxImageSize;
    
    @Value("${mercadolocalia.max-document-size:10485760}") // 10MB
    private long maxDocumentSize;
    
    @Value("${cloudinary.productos-folder:productos}")
    private String productosFolder;
    
    @Value("${cloudinary.comprobantes-folder:comprobantes}")
    private String comprobantesFolder;

    @Override
    public String guardarImagenProducto(MultipartFile file) {
        System.out.println("🖼️ Subiendo imagen de producto a Cloudinary...");
        
        // Validaciones
        validarArchivo(file, allowedImageTypes.split(","), maxImageSize, "imagen");
        
        try {
            // Subir a Cloudinary
            String imageUrl = cloudStorageService.uploadImage(file, "productos");
            
            System.out.println("✅ Imagen de producto subida exitosamente");
            System.out.println("   URL: " + imageUrl);
            
            return imageUrl;
            
        } catch (Exception e) {
            System.err.println("❌ Error subiendo imagen de producto: " + e.getMessage());
            throw new RuntimeException("Error al subir imagen del producto: " + e.getMessage(), e);
        }
    }

    @Override
    public String storeComprobante(MultipartFile file) {
        System.out.println("📄 Subiendo comprobante a Cloudinary...");
        
        // Validaciones
        validarArchivo(file, allowedDocumentTypes.split(","), maxDocumentSize, "comprobante");
        
        try {
            String contentType = file.getContentType();
            String documentUrl;
            
            if (contentType != null && contentType.equals("application/pdf")) {
                System.out.println("📎 Detectado PDF, subiendo como documento...");
                documentUrl = cloudStorageService.uploadDocument(file, "comprobantes");
            } else {
                System.out.println("🖼️ Detectado imagen de comprobante...");
                documentUrl = cloudStorageService.uploadDocument(file, "comprobantes");
            }
            
            System.out.println("✅ Comprobante subido exitosamente");
            System.out.println("   URL: " + documentUrl);
            
            return documentUrl;
            
        } catch (Exception e) {
            System.err.println("❌ Error subiendo comprobante: " + e.getMessage());
            throw new RuntimeException("Error al subir comprobante: " + e.getMessage(), e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        String contentType = file.getContentType();
        return (contentType != null && contentType.startsWith("image/")) 
            ? guardarImagenProducto(file) 
            : storeComprobante(file);
    }

    @Override
    public byte[] loadFile(String fileName) throws IOException {
        throw new UnsupportedOperationException(
            "Para almacenamiento en la nube, acceda directamente a la URL del archivo: " + fileName
        );
    }

    @Override
    public byte[] loadComprobante(String fileName) throws IOException {
        throw new UnsupportedOperationException(
            "Para almacenamiento en la nube, acceda directamente a la URL del archivo: " + fileName
        );
    }

    @Override
    public void deleteFile(String url) {
        if (url == null || url.isEmpty()) {
            System.out.println("⚠️ URL vacía, nada que eliminar");
            return;
        }
        
        try {
            boolean deleted = cloudStorageService.deleteFile(url);
            if (deleted) {
                System.out.println("✅ Archivo eliminado de Cloudinary: " + getFilenameFromPath(url));
            } else {
                System.out.println("⚠️ No se pudo eliminar el archivo: " + url);
            }
        } catch (Exception e) {
            System.err.println("❌ Error eliminando archivo: " + e.getMessage());
            throw new RuntimeException("Error al eliminar archivo: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteComprobante(String url) {
        deleteFile(url);
    }

    @Override
    public String getFileUrl(String fileName) {
        // fileName ya debería ser una URL completa
        return fileName;
    }

    @Override
    public String getComprobanteUrl(String fileName) {
        return fileName;
    }

    @Override
    public boolean isValidImage(MultipartFile file) {
        return isValidFile(file, allowedImageTypes.split(","));
    }

    @Override
    public boolean isValidComprobante(MultipartFile file) {
        return isValidFile(file, allowedDocumentTypes.split(","));
    }

    @Override
    public long getFileSize(MultipartFile file) {
        return file != null ? file.getSize() : 0;
    }

    @Override
    public String getFilenameFromPath(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        
        // Extraer nombre del archivo de la URL
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash >= 0) {
            String filename = url.substring(lastSlash + 1);
            
            // Remover parámetros de consulta
            int questionMark = filename.indexOf('?');
            if (questionMark > 0) {
                filename = filename.substring(0, questionMark);
            }
            
            // Remover fragmentos
            int hashMark = filename.indexOf('#');
            if (hashMark > 0) {
                filename = filename.substring(0, hashMark);
            }
            
            return filename;
        }
        
        return url;
    }

    @Override
    public String getRelativePathFromUrl(String url) {
        // Con almacenamiento en la nube, siempre es URL completa
        return url;
    }
    
    // ============ MÉTODOS PRIVADOS AUXILIARES ============
    
    private void validarArchivo(MultipartFile file, String[] allowedTypes, long maxSize, String tipo) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de " + tipo + " está vacío");
        }
        
        // Validar tipo
        String contentType = file.getContentType();
        if (!isValidFile(file, allowedTypes)) {
            throw new IllegalArgumentException(
                "Tipo de " + tipo + " no permitido: " + contentType + 
                ". Permitidos: " + String.join(", ", allowedTypes)
            );
        }
        
        // Validar tamaño
        long fileSize = file.getSize();
        if (fileSize > maxSize) {
            double maxMB = maxSize / (1024.0 * 1024.0);
            double fileMB = fileSize / (1024.0 * 1024.0);
            throw new IllegalArgumentException(
                String.format("El %s es demasiado grande (%.2f MB). Máximo permitido: %.2f MB", 
                    tipo, fileMB, maxMB)
            );
        }
        
        System.out.println(String.format("✅ %s validado - %s (%.2f KB)", 
            tipo.substring(0, 1).toUpperCase() + tipo.substring(1),
            contentType,
            fileSize / 1024.0));
    }
    
    private boolean isValidFile(MultipartFile file, String[] allowedTypes) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        for (String allowedType : allowedTypes) {
            if (contentType.equalsIgnoreCase(allowedType.trim())) {
                return true;
            }
        }
        
        return false;
    }
    
    // Método para diagnóstico
    public String getServiceInfo() {
        return String.format(
            "CloudFileStorageService Info:\n" +
            "  ✅ Tipo: Cloudinary\n" +
            "  ✅ Imágenes permitidas: %s\n" +
            "  ✅ Documentos permitidos: %s\n" +
            "  ✅ Máximo imagen: %d MB\n" +
            "  ✅ Máximo documento: %d MB\n" +
            "  ✅ Carpeta productos: %s\n" +
            "  ✅ Carpeta comprobantes: %s",
            allowedImageTypes,
            allowedDocumentTypes,
            maxImageSize / 1024 / 1024,
            maxDocumentSize / 1024 / 1024,
            productosFolder,
            comprobantesFolder
        );
    }
}