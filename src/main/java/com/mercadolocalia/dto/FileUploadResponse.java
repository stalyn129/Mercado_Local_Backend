package com.mercadolocalia.dto;

public class FileUploadResponse {
    private boolean success;
    private String path;      // Ruta relativa: /uploads/productos/filename.jpg
    private String url;       // URL completa: http://localhost:8080/uploads/productos/filename.jpg
    private String filename;  // Nombre del archivo: filename.jpg
    private String message;
    
    // Constructores
    public FileUploadResponse() {
        this.success = true;
    }
    
    public FileUploadResponse(String path, String url, String filename) {
        this.success = true;
        this.path = path;
        this.url = url;
        this.filename = filename;
        this.message = "Archivo subido exitosamente";
    }
    
    public FileUploadResponse(String path, String url, String filename, String message) {
        this.success = true;
        this.path = path;
        this.url = url;
        this.filename = filename;
        this.message = message;
    }
    
    // Constructor de error
    public FileUploadResponse(String errorMessage) {
        this.success = false;
        this.message = errorMessage;
    }
    
    // Getters y Setters
    public boolean isSuccess() { 
        return success; 
    }
    
    public void setSuccess(boolean success) { 
        this.success = success; 
    }
    
    public String getPath() { 
        return path; 
    }
    
    public void setPath(String path) { 
        this.path = path; 
    }
    
    public String getUrl() { 
        return url; 
    }
    
    public void setUrl(String url) { 
        this.url = url; 
    }
    
    public String getFilename() { 
        return filename; 
    }
    
    public void setFilename(String filename) { 
        this.filename = filename; 
    }
    
    public String getMessage() { 
        return message; 
    }
    
    public void setMessage(String message) { 
        this.message = message; 
    }
    
    // Método estático para crear respuesta de éxito
    public static FileUploadResponse success(String path, String url, String filename) {
        return new FileUploadResponse(path, url, filename);
    }
    
    // Método estático para crear respuesta de error
    public static FileUploadResponse error(String message) {
        return new FileUploadResponse(message);
    }
}