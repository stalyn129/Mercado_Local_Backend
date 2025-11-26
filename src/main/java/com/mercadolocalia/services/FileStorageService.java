package com.mercadolocalia.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Guarda una imagen de producto en disco y devuelve la URL pública.
     */
    String guardarImagenProducto(MultipartFile file);
}
