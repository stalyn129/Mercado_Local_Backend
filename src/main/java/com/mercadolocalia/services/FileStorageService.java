package com.mercadolocalia.services;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {
    
    // Métodos específicos
    String guardarImagenProducto(MultipartFile file);
    String storeComprobante(MultipartFile file);
    
    // Métodos generales
    String storeFile(MultipartFile file);
    byte[] loadFile(String fileName) throws IOException;
    byte[] loadComprobante(String fileName) throws IOException;
    void deleteFile(String fileName);
    void deleteComprobante(String fileName);
    String getFileUrl(String fileName);
    String getComprobanteUrl(String fileName);
    
    // Validaciones
    boolean isValidImage(MultipartFile file);
    boolean isValidComprobante(MultipartFile file);
    long getFileSize(MultipartFile file);
    
    // Métodos adicionales para manejo de rutas (NUEVOS)
    String getFilenameFromPath(String path);
    String getRelativePathFromUrl(String url);
}