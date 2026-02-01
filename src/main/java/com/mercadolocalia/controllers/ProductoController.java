package com.mercadolocalia.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mercadolocalia.dto.ProductoDetalleResponse;
import com.mercadolocalia.dto.ProductoRequest;
import com.mercadolocalia.dto.ProductoResponse;
import com.mercadolocalia.services.FileStorageService;
import com.mercadolocalia.services.ProductoService;

@RestController
@RequestMapping("/productos")
@CrossOrigin(origins = "http://localhost:3000") // Agrega CORS para React
public class ProductoController {

    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private FileStorageService fileStorageService;

    // ==================== SUBIR IMAGEN A CLOUDINARY ====================
    @PostMapping("/subir-imagen")
    public ResponseEntity<?> subirImagenProducto(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("📤 Subiendo imagen de producto a Cloudinary...");
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El archivo está vacío"));
            }
            
            // Validar que sea una imagen
            if (!fileStorageService.isValidImage(file)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Formato de imagen no válido. Use JPG, PNG, GIF o WEBP"
                ));
            }
            
            // Validar tamaño (10MB máximo)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "La imagen es demasiado grande. Máximo 10MB"
                ));
            }
            
            // Subir a Cloudinary
            String imageUrl = fileStorageService.guardarImagenProducto(file);
            
            System.out.println("✅ Imagen subida exitosamente: " + imageUrl);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "imageUrl", imageUrl,
                "filename", fileStorageService.getFilenameFromPath(imageUrl)
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error subiendo imagen: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al subir imagen: " + e.getMessage()
            ));
        }
    }

    // ==================== CREAR PRODUCTO ====================
    @PostMapping("/crear")
    public ResponseEntity<ProductoResponse> crearProducto(@RequestBody ProductoRequest request) {
        try {
            ProductoResponse response = productoService.crearProducto(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ==================== EDITAR PRODUCTO (SOLO NOMBRE Y PRECIO) ====================
    @PutMapping("/editar/{id}")
    public ResponseEntity<?> editarProducto(@PathVariable Integer id, @RequestBody Map<String, Object> updates) {
        try {
            // Crear un ProductoRequest con solo los campos permitidos
            ProductoRequest request = new ProductoRequest();

            // Solo permitir nombre y precio
            if (updates.containsKey("nombreProducto")) {
                request.setNombreProducto((String) updates.get("nombreProducto"));
            }

            if (updates.containsKey("precioProducto")) {
                request.setPrecioProducto(Double.valueOf(updates.get("precioProducto").toString()));
            }

            // No permitir stock, unidad, etc.
            ProductoResponse response = productoService.actualizarProducto(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== ELIMINAR (BORRADO LÓGICO) ====================
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Integer id) {
        try {
            productoService.eliminarProducto(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Producto eliminado (borrado lógico) correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== DESACTIVAR PRODUCTO (BORRADO LÓGICO) ====================
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivarProducto(@PathVariable Integer id,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            String motivo = request != null ? request.get("motivo") : "Desactivado por administrador";
            ProductoResponse producto = productoService.desactivarProducto(id, motivo);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Producto desactivado correctamente");
            response.put("producto", producto);
            response.put("tipo", "borrado_logico");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== REACTIVAR PRODUCTO ====================
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<?> reactivarProducto(@PathVariable Integer id) {
        try {
            ProductoResponse producto = productoService.reactivarProducto(id);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Producto reactivado correctamente");
            response.put("producto", producto);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== OBTENER POR ID ====================
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProducto(@PathVariable Integer id) {
        try {
            ProductoResponse response = productoService.obtenerPorId(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== LISTAR TODOS (PARA ADMIN) ====================
    @GetMapping("/admin/listar")
    public ResponseEntity<?> listarProductosAdmin() {
        try {
            List<ProductoResponse> productos = productoService.listarTodos();

            // Convertir a formato que necesita el frontend
            List<Map<String, Object>> response = convertirParaFrontend(productos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== LISTAR ACTIVOS (PARA EXPLORAR) ====================
    @GetMapping("/listar")
    public ResponseEntity<?> listarProductos() {
        try {
            // Para el frontend público, solo productos activos
            List<ProductoResponse> productos = productoService.listarActivos();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== LISTAR INACTIVOS ====================
    @GetMapping("/inactivos")
    public ResponseEntity<?> listarProductosInactivos() {
        try {
            List<ProductoResponse> productos = productoService.listarInactivos();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== LISTAR POR VENDEDOR ====================
    @GetMapping("/vendedor/{idVendedor}")
    public ResponseEntity<?> listarPorVendedor(@PathVariable Integer idVendedor) {
        try {
            List<ProductoResponse> productos = productoService.listarPorVendedor(idVendedor);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== LISTAR POR SUBCATEGORÍA ====================
    @GetMapping("/subcategoria/{idSubcategoria}")
    public ResponseEntity<?> listarPorSubcategoria(@PathVariable Integer idSubcategoria) {
        try {
            List<ProductoResponse> productos = productoService.listarPorSubcategoria(idSubcategoria);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== CAMBIAR ESTADO ====================
    @PutMapping("/estado/{id}")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestParam String estado) {
        try {
            ProductoResponse response = productoService.cambiarEstado(id, estado);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== DETALLE COMPLETO (CON VALORACIONES) ====================
    @GetMapping("/detalle/{id}")
    public ResponseEntity<?> obtenerDetalle(@PathVariable Integer id) {
        try {
            ProductoDetalleResponse response = productoService.obtenerDetalleProducto(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== TOP 20 MEJORES PARA HOME ====================
    @GetMapping("/top")
    public ResponseEntity<?> listarTop20Mejores() {
        try {
            List<ProductoResponse> productos = productoService.listarTop20Mejores();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Convierte ProductoResponse a formato para el frontend admin
     */
    private List<Map<String, Object>> convertirParaFrontend(List<ProductoResponse> productos) {
        return productos.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idProducto", p.getIdProducto());
            map.put("nombreProducto", p.getNombreProducto());
            map.put("descripcionProducto", p.getDescripcionProducto());
            map.put("precioProducto", p.getPrecioProducto());
            map.put("stockProducto", p.getStockProducto());
            map.put("unidad", p.getUnidad());
            map.put("imagenProducto", p.getImagenProducto());
            map.put("estado", p.getEstado());
            map.put("activo", p.getActivo());
            map.put("fechaDesactivacion", p.getFechaDesactivacion());
            map.put("motivoDesactivacion", p.getMotivoDesactivacion());
            map.put("ultimaActualizacion", p.getUltimaActualizacion());

            // ✅ CORRECTO: Usar los campos que ya están en ProductoResponse
            if (p.getNombreSubcategoria() != null) {
                map.put("nombreSubcategoria", p.getNombreSubcategoria());
                map.put("idSubcategoria", p.getIdSubcategoria());
            }

            if (p.getNombreCategoria() != null) {
                map.put("nombreCategoria", p.getNombreCategoria());
                map.put("idCategoria", p.getIdCategoria());
            }

            if (p.getNombreEmpresa() != null) {
                map.put("nombreEmpresa", p.getNombreEmpresa());
                map.put("idVendedor", p.getIdVendedor());
            }

            // Agregar URL completa de imagen usando el método del DTO
            map.put("imagenUrl", p.getImagenUrlCompleta());

            return map;
        }).collect(Collectors.toList());
    }
}