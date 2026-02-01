package com.mercadolocalia.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mercadolocalia.services.CloudStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryServiceImpl implements CloudStorageService {

    private final Cloudinary cloudinary;
    
    @Value("${cloudinary.productos-folder:productos}")
    private String productosFolder;
    
    @Value("${cloudinary.comprobantes-folder:comprobantes}")
    private String comprobantesFolder;

    public CloudinaryServiceImpl(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        
        cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        ));
        
        System.out.println("✅ Cloudinary configurado:");
        System.out.println("   Cloud Name: " + cloudName);
        System.out.println("   API Key: " + apiKey.substring(0, 5) + "...");
        System.out.println("   Productos Folder: " + productosFolder);
        System.out.println("   Comprobantes Folder: " + comprobantesFolder);
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        try {
            // Validar tipo de archivo
            if (!isValidImage(file)) {
                throw new IllegalArgumentException("Tipo de imagen no válido");
            }

            // Usar la carpeta correcta
            String actualFolder = "productos".equals(folder) ? productosFolder : 
                                 "comprobantes".equals(folder) ? comprobantesFolder : folder;
            
            // Generar nombre único
            String publicId = actualFolder + "/" + UUID.randomUUID().toString();

            System.out.println("📤 Subiendo imagen a Cloudinary:");
            System.out.println("   Carpeta: " + actualFolder);
            System.out.println("   Tipo: " + file.getContentType());
            System.out.println("   Tamaño: " + file.getSize() + " bytes");

            // Subir a Cloudinary
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", actualFolder,
                    "resource_type", "auto",
                    "quality", "auto:good"
                )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            System.out.println("✅ Imagen subida exitosamente:");
            System.out.println("   URL: " + secureUrl);
            
            return secureUrl;

        } catch (IOException e) {
            throw new RuntimeException("Error al subir imagen a Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadDocument(MultipartFile file, String folder) {
        try {
            // Usar la carpeta correcta
            String actualFolder = "productos".equals(folder) ? productosFolder : 
                                 "comprobantes".equals(folder) ? comprobantesFolder : folder;
            
            String publicId = actualFolder + "/" + UUID.randomUUID().toString();

            System.out.println("📤 Subiendo documento a Cloudinary:");
            System.out.println("   Carpeta: " + actualFolder);
            System.out.println("   Tipo: " + file.getContentType());

            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", actualFolder,
                    "resource_type", "auto"
                )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            System.out.println("✅ Documento subido exitosamente:");
            System.out.println("   URL: " + secureUrl);
            
            return secureUrl;

        } catch (IOException e) {
            throw new RuntimeException("Error al subir documento a Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String url) {
        try {
            String publicId = extractPublicIdFromUrl(url);
            System.out.println("🗑️ Eliminando de Cloudinary: " + publicId);
            
            Map<String, Object> result = cloudinary.uploader().destroy(
                publicId, 
                ObjectUtils.asMap("resource_type", "auto")
            );
            
            boolean deleted = "ok".equals(result.get("result"));
            System.out.println(deleted ? "✅ Eliminado" : "❌ No se pudo eliminar");
            return deleted;
            
        } catch (Exception e) {
            System.err.println("❌ Error eliminando archivo: " + e.getMessage());
            throw new RuntimeException("Error al eliminar archivo de Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL no válida");
        }
        
        try {
            String[] parts = url.split("/upload/");
            if (parts.length > 1) {
                String path = parts[1];
                // Remover versión (v1234567/) si existe
                if (path.startsWith("v") && path.contains("/")) {
                    path = path.substring(path.indexOf('/') + 1);
                }
                // Remover extensión
                int dotIndex = path.lastIndexOf('.');
                if (dotIndex > 0) {
                    path = path.substring(0, dotIndex);
                }
                return path;
            }
            throw new IllegalArgumentException("URL de Cloudinary no válida: " + url);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error extrayendo public_id de: " + url, e);
        }
    }

    private boolean isValidImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && 
               (contentType.startsWith("image/jpeg") ||
                contentType.startsWith("image/png") ||
                contentType.startsWith("image/gif") ||
                contentType.startsWith("image/webp"));
    }
}