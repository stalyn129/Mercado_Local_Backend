package com.mercadolocalia.controllers;

import com.mercadolocalia.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173"
})
public class UploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/producto")
    public ResponseEntity<Map<String, String>> uploadImagenProducto(
            @RequestParam("file") MultipartFile file
    ) {
        String url = fileStorageService.guardarImagenProducto(file);

        Map<String, String> response = new HashMap<>();
        response.put("url", url);

        return ResponseEntity.ok(response);
    }
}
