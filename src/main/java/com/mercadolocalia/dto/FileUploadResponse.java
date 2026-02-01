package com.mercadolocalia.dto;

public class FileUploadResponse {
    private boolean success;
    private String path;
    private String url;
    private String filename;
    private String message;
    
    // Constructores...
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
    
    // Getters y Setters (DEBES AGREGARLOS SI NO LOS TIENES)
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    // Métodos estáticos - CORRIGE ESTOS:
    
    // 1. Método de 3 parámetros (ya existe)
    public static FileUploadResponse success(String path, String url, String filename) {
        return new FileUploadResponse(path, url, filename);
    }
    
    // 2. Método de 4 parámetros (FALTA ESTE - AGREGA)
    public static FileUploadResponse success(String path, String url, String filename, String message) {
        return new FileUploadResponse(path, url, filename, message);
    }
    
    // 3. Método para solo mensaje
    public static FileUploadResponse successWithMessage(String message) {
        FileUploadResponse response = new FileUploadResponse();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }
    
    // 4. Método de error
    public static FileUploadResponse error(String message) {
        return new FileUploadResponse(message);
    }
}