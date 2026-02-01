package com.mercadolocalia.services;

import org.springframework.web.multipart.MultipartFile;

public interface CloudStorageService {
    String uploadImage(MultipartFile file, String folder);
    String uploadDocument(MultipartFile file, String folder);
    boolean deleteFile(String url);
    String extractPublicIdFromUrl(String url);
}