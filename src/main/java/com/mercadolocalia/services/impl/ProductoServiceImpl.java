package com.mercadolocalia.services.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercadolocalia.dto.ProductoRequest;
import com.mercadolocalia.dto.ProductoResponse;
import com.mercadolocalia.dto.ProductoDetalleResponse;
import com.mercadolocalia.dto.ValoracionResponse;
import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.entities.Subcategoria;
import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.repositories.SubcategoriaRepository;
import com.mercadolocalia.repositories.UsuarioRepository;
import com.mercadolocalia.repositories.VendedorRepository;
import com.mercadolocalia.services.ProductoService;
import com.mercadolocalia.services.FileStorageService;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired 
    private ProductoRepository productoRepository;
    
    @Autowired 
    private VendedorRepository vendedorRepository;
    
    @Autowired 
    private UsuarioRepository usuarioRepository;
    
    @Autowired 
    private SubcategoriaRepository subcategoriaRepository;
    
    @Autowired 
    private FileStorageService fileStorageService;

    // ===================== CREAR PRODUCTO =====================
    @Override
    @Transactional
    public ProductoResponse crearProducto(ProductoRequest request) {
        System.out.println("🛍️ Creando nuevo producto...");
        
        Producto producto = new Producto();
        asignarDatos(producto, request);
        producto.setFechaPublicacion(LocalDateTime.now());
        producto.setEstado("Disponible");
        producto.setActivo(true);
        producto.setUltimaActualizacion(LocalDateTime.now());
        
        // ✅ VALIDAR Y GUARDAR IMAGEN (URL DE CLOUDINARY)
        validarYGuardarImagen(request, producto);
        
        productoRepository.save(producto);
        
        System.out.println("✅ Producto creado exitosamente ID: " + producto.getIdProducto());
        System.out.println("✅ Imagen URL: " + producto.getImagenProducto());
        
        return convertir(producto);
    }

    // ===================== ACTUALIZAR PRODUCTO =====================
    @Override
    @Transactional
    public ProductoResponse actualizarProducto(Integer id, ProductoRequest request) {
        System.out.println("✏️ Actualizando producto ID: " + id);
        
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id " + id));
        
        // Verificar que esté activo
        if (!producto.estaActivo()) {
            throw new RuntimeException("No se puede actualizar un producto inactivo");
        }
        
        // ✅ ACTUALIZAR CAMPOS PERMITIDOS
        if (request.getNombreProducto() != null && !request.getNombreProducto().trim().isEmpty()) {
            producto.setNombreProducto(request.getNombreProducto());
        }
        
        if (request.getPrecioProducto() != null && request.getPrecioProducto() > 0) {
            producto.setPrecioProducto(request.getPrecioProducto());
        }
        
        // ✅ ACTUALIZAR IMAGEN SI SE PROPORCIONA NUEVA URL
        if (request.getImagenProducto() != null && !request.getImagenProducto().isEmpty()) {
            validarYGuardarImagen(request, producto);
        }
        
        producto.setUltimaActualizacion(LocalDateTime.now());
        productoRepository.save(producto);
        
        System.out.println("✅ Producto actualizado exitosamente");
        
        return convertir(producto);
    }

    // ===================== VALIDAR Y GUARDAR IMAGEN =====================
    private void validarYGuardarImagen(ProductoRequest request, Producto producto) {
        String imagenUrl = request.getImagenProducto();
        
        if (imagenUrl == null || imagenUrl.trim().isEmpty()) {
            System.out.println("⚠️ Producto sin imagen");
            producto.setImagenProducto(null);
            return;
        }
        
        System.out.println("📷 Procesando imagen del producto...");
        System.out.println("   URL recibida: " + (imagenUrl.length() > 100 ? imagenUrl.substring(0, 100) + "..." : imagenUrl));
        
        // ✅ VALIDAR QUE SEA URL DE CLOUDINARY
        if (!esUrlValida(imagenUrl)) {
            System.err.println("❌ URL de imagen no válida");
            throw new RuntimeException("URL de imagen no válida. Debe ser una URL de Cloudinary.");
        }
        
        // ✅ VERIFICAR QUE SEA UNA URL DE IMAGEN
        if (!esUrlDeImagen(imagenUrl)) {
            System.err.println("❌ No es una URL de imagen");
            throw new RuntimeException("La URL proporcionada no parece ser una imagen válida.");
        }
        
        // ✅ GUARDAR URL DIRECTAMENTE
        producto.setImagenProducto(imagenUrl);
        
        System.out.println("✅ Imagen validada y guardada");
    }
    
    private boolean esUrlValida(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        // Aceptar URLs de Cloudinary o cualquier URL HTTP/HTTPS
        return url.startsWith("http://") || 
               url.startsWith("https://") ||
               url.startsWith("https://res.cloudinary.com/");
    }
    
    private boolean esUrlDeImagen(String url) {
        String urlLower = url.toLowerCase();
        return urlLower.contains(".jpg") || 
               urlLower.contains(".jpeg") || 
               urlLower.contains(".png") || 
               urlLower.contains(".gif") || 
               urlLower.contains(".webp") ||
               urlLower.contains("image/upload");
    }

    // ===================== ASIGNAR DATOS DEL PRODUCTO =====================
    private void asignarDatos(Producto producto, ProductoRequest request) {
        // ✅ VALIDAR Y OBTENER SUBCATEGORÍA
        Subcategoria subcategoria = subcategoriaRepository.findById(request.getIdSubcategoria())
                .orElseThrow(() -> new RuntimeException("Subcategoría no existe"));
        
        producto.setSubcategoria(subcategoria);
        producto.setNombreProducto(request.getNombreProducto());
        producto.setDescripcionProducto(request.getDescripcionProducto());
        producto.setPrecioProducto(request.getPrecioProducto());
        producto.setStockProducto(request.getStockProducto());
        producto.setUnidad(request.getUnidad());
        producto.setUltimaActualizacion(LocalDateTime.now());

        // ✅ OBTENER VENDEDOR
        if (request.getIdVendedor() != null) {
            Vendedor vendedor = vendedorRepository.findById(request.getIdVendedor())
                    .orElseThrow(() -> new RuntimeException("Vendedor no existe"));
            producto.setVendedor(vendedor);
        } else if (request.getIdUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no existe"));
            Vendedor vendedor = vendedorRepository.findByUsuario(usuario)
                    .orElseThrow(() -> new RuntimeException("El usuario no es vendedor"));
            producto.setVendedor(vendedor);
        } else {
            throw new RuntimeException("Debe proporcionar idVendedor o idUsuario");
        }
    }

    // ===================== BORRADO LÓGICO - ELIMINAR =====================
    @Override
    @Transactional
    public void eliminarProducto(Integer id) {
        System.out.println("🗑️ Eliminando (desactivando) producto ID: " + id);
        
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        // BORRADO LÓGICO - Desactivar en lugar de eliminar
        producto.desactivar("Eliminado por administrador");
        productoRepository.save(producto);
        
        System.out.println("✅ Producto desactivado exitosamente");
    }
    
    // ===================== DESACTIVAR PRODUCTO =====================
    @Override
    @Transactional
    public ProductoResponse desactivarProducto(Integer id, String motivo) {
        System.out.println("🔒 Desactivando producto ID: " + id + " - Motivo: " + motivo);
        
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        producto.desactivar(motivo);
        productoRepository.save(producto);
        
        System.out.println("✅ Producto desactivado");
        
        return convertir(producto);
    }
    
    // ===================== REACTIVAR PRODUCTO =====================
    @Override
    @Transactional
    public ProductoResponse reactivarProducto(Integer id) {
        System.out.println("🔓 Reactivando producto ID: " + id);
        
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        producto.reactivar();
        productoRepository.save(producto);
        
        System.out.println("✅ Producto reactivado");
        
        return convertir(producto);
    }

    // ===================== OBTENER POR ID =====================
    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Integer id) {
        System.out.println("🔍 Obteniendo producto ID: " + id);
        
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Producto no encontrado con id " + id));
        
        // Para frontend público, solo devolver si está activo
        if (!producto.estaActivo()) {
            throw new RuntimeException("Producto no disponible");
        }
        
        return convertir(producto);
    }

    // ===================== LISTAR POR VENDEDOR =====================
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorVendedor(Integer id) {
        System.out.println("📋 Listando productos del vendedor ID: " + id);
        
        Vendedor vendedor = vendedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
        
        return productoRepository.findByVendedor(vendedor).stream()
                .filter(Producto::estaActivo) // Solo activos
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    // ===================== LISTAR POR SUBCATEGORÍA =====================
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorSubcategoria(Integer id) {
        System.out.println("📋 Listando productos de subcategoría ID: " + id);
        
        Subcategoria subcategoria = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategoría no encontrada"));
        
        return productoRepository.findBySubcategoria(subcategoria).stream()
                .filter(Producto::estaActivo) // Solo activos
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    // ===================== LISTAR TODOS (PARA ADMIN) =====================
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodos() {
        System.out.println("📋 Listando todos los productos (admin)");
        
        return productoRepository.findAll().stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }
    
    // ===================== LISTAR ACTIVOS (PARA EXPLORAR) =====================
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarActivos() {
        System.out.println("📋 Listando productos activos");
        
        return productoRepository.findAll().stream()
                .filter(Producto::estaActivo)
                .map(this::convertir)
                .collect(Collectors.toList());
    }
    
    // ===================== LISTAR INACTIVOS =====================
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarInactivos() {
        System.out.println("📋 Listando productos inactivos");
        
        return productoRepository.findAll().stream()
                .filter(p -> !p.estaActivo())
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    // ===================== CAMBIAR ESTADO =====================
    @Override
    @Transactional
    public ProductoResponse cambiarEstado(Integer id, String estado) {
        System.out.println("🔄 Cambiando estado del producto ID: " + id + " a: " + estado);
        
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        // Solo permitir cambiar estado si está activo
        if (!producto.estaActivo()) {
            throw new RuntimeException("No se puede cambiar estado de producto inactivo");
        }
        
        producto.setEstado(estado);
        producto.setUltimaActualizacion(LocalDateTime.now());
        productoRepository.save(producto);
        
        System.out.println("✅ Estado actualizado");
        
        return convertir(producto);
    }

    // ===================== DETALLE COMPLETO =====================
    @Override
    @Transactional(readOnly = true)
    public ProductoDetalleResponse obtenerDetalleProducto(Integer idProducto) {
        System.out.println("🔍 Obteniendo detalle completo del producto ID: " + idProducto);
        
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductoDetalleResponse response = new ProductoDetalleResponse();

        // BÁSICO
        response.setIdProducto(producto.getIdProducto());
        response.setNombreProducto(producto.getNombreProducto());
        response.setDescripcionProducto(producto.getDescripcionProducto());
        response.setPrecioProducto(producto.getPrecioProducto());
        response.setStockProducto(producto.getStockProducto());
        response.setImagenProducto(producto.getImagenProducto());
        response.setFechaPublicacion(producto.getFechaPublicacion());
        response.setEstado(producto.getEstado());
        response.setUnidad(producto.getUnidad());
        response.setActivo(producto.getActivo());
        response.setFechaDesactivacion(producto.getFechaDesactivacion());
        response.setMotivoDesactivacion(producto.getMotivoDesactivacion());

        // SUBCATEGORÍA + CATEGORÍA
        if (producto.getSubcategoria() != null) {
            response.setIdSubcategoria(producto.getSubcategoria().getIdSubcategoria());
            response.setNombreSubcategoria(producto.getSubcategoria().getNombreSubcategoria());

            if (producto.getSubcategoria().getCategoria() != null){
                response.setIdCategoria(producto.getSubcategoria().getCategoria().getIdCategoria());
                response.setNombreCategoria(producto.getSubcategoria().getCategoria().getNombreCategoria());
            }
        }

        // VENDEDOR
        if (producto.getVendedor() != null){
            response.setIdVendedor(producto.getVendedor().getIdVendedor());
            response.setNombreEmpresa(producto.getVendedor().getNombreEmpresa());

            if(producto.getVendedor().getUsuario() != null){
                response.setNombreVendedor(
                        producto.getVendedor().getUsuario().getNombre() + " " +
                        producto.getVendedor().getUsuario().getApellido()
                );
            }
        }

        // VALORACIONES
        if(producto.getValoraciones() != null && !producto.getValoraciones().isEmpty()){
            response.setPromedioValoracion(
                    producto.getValoraciones().stream()
                            .mapToDouble(v -> v.getCalificacion())
                            .average().orElse(0.0));

            response.setTotalValoraciones(producto.getValoraciones().size());

            response.setValoraciones(
                    producto.getValoraciones().stream().map(v -> {
                        ValoracionResponse vr = new ValoracionResponse();
                        vr.setIdValoracion(v.getIdValoracion());
                        vr.setCalificacion(v.getCalificacion());
                        vr.setComentario(v.getComentario());
                        vr.setFechaValoracion(v.getFechaValoracion());

                        if(v.getConsumidor() != null && v.getConsumidor().getUsuario() != null){
                            vr.setIdConsumidor(v.getConsumidor().getIdConsumidor());
                            vr.setNombreConsumidor(v.getConsumidor().getUsuario().getNombre() + " " +
                                                  v.getConsumidor().getUsuario().getApellido());
                        }
                        return vr;
                    }).toList()
            );
        } else {
            response.setPromedioValoracion(0.0);
            response.setTotalValoraciones(0);
            response.setValoraciones(List.of());
        }

        System.out.println("✅ Detalle obtenido exitosamente");
        
        return response;
    }

    // ===================== CONVERTIR A RESPONSE =====================
    private ProductoResponse convertir(Producto producto) {
        ProductoResponse response = new ProductoResponse();

        response.setIdProducto(producto.getIdProducto());
        response.setNombreProducto(producto.getNombreProducto());
        response.setDescripcionProducto(producto.getDescripcionProducto());
        response.setPrecioProducto(producto.getPrecioProducto());
        response.setStockProducto(producto.getStockProducto());
        response.setUnidad(producto.getUnidad());
        response.setImagenProducto(producto.getImagenProducto());
        response.setFechaPublicacion(producto.getFechaPublicacion());
        response.setEstado(producto.getEstado());
        
        // ✅ CAMPOS DE BORRADO LÓGICO
        response.setActivo(producto.getActivo());
        response.setFechaDesactivacion(producto.getFechaDesactivacion());
        response.setMotivoDesactivacion(producto.getMotivoDesactivacion());
        response.setUltimaActualizacion(producto.getUltimaActualizacion());

        // SUBCATEGORÍA
        if (producto.getSubcategoria() != null) {
            response.setIdSubcategoria(producto.getSubcategoria().getIdSubcategoria());
            response.setNombreSubcategoria(producto.getSubcategoria().getNombreSubcategoria());

            if (producto.getSubcategoria().getCategoria() != null) {
                response.setIdCategoria(producto.getSubcategoria().getCategoria().getIdCategoria());
                response.setNombreCategoria(producto.getSubcategoria().getCategoria().getNombreCategoria());
            }
        }

        // VENDEDOR
        if (producto.getVendedor() != null) {
            response.setIdVendedor(producto.getVendedor().getIdVendedor());
            response.setNombreEmpresa(producto.getVendedor().getNombreEmpresa());
        }

        // VALORACIONES (PROMEDIO + TOTAL)
        if (producto.getValoraciones() != null && !producto.getValoraciones().isEmpty()) {
            double promedio = producto.getValoraciones()
                    .stream()
                    .mapToDouble(v -> v.getCalificacion())
                    .average()
                    .orElse(0.0);

            response.setPromedioValoracion(promedio);
            response.setTotalValoraciones(producto.getValoraciones().size());
        } else {
            response.setPromedioValoracion(0.0);
            response.setTotalValoraciones(0);
        }

        return response;
    }

    // ===================== TOP 20 MEJORES PRODUCTOS =====================
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTop20Mejores() {
        System.out.println("🏆 Obteniendo top 20 mejores productos");
        
        PageRequest pageable = PageRequest.of(0, 20);

        return productoRepository.findTop20Mejores(pageable)
                .stream()
                .filter(Producto::estaActivo) // Solo activos
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    // ===================== LISTAR PARA ADMIN =====================
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarTodosParaAdmin() {
        System.out.println("📋 Listando todos los productos para admin");
        
        return productoRepository.findAll().stream().map(producto -> {
            Map<String, Object> map = new HashMap<>();
            
            map.put("idProducto", producto.getIdProducto());
            map.put("nombreProducto", producto.getNombreProducto());
            map.put("descripcionProducto", producto.getDescripcionProducto());
            map.put("precioProducto", producto.getPrecioProducto());
            map.put("stockProducto", producto.getStockProducto());
            map.put("unidad", producto.getUnidad());
            map.put("imagenProducto", producto.getImagenProducto());
            map.put("estado", producto.getEstado());
            map.put("activo", producto.getActivo());
            map.put("fechaDesactivacion", producto.getFechaDesactivacion());
            map.put("motivoDesactivacion", producto.getMotivoDesactivacion());
            map.put("ultimaActualizacion", producto.getUltimaActualizacion());
            map.put("fechaPublicacion", producto.getFechaPublicacion());
            
            // Datos de subcategoría
            if (producto.getSubcategoria() != null) {
                map.put("idSubcategoria", producto.getSubcategoria().getIdSubcategoria());
                map.put("nombreSubcategoria", producto.getSubcategoria().getNombreSubcategoria());
                
                // Datos de categoría
                if (producto.getSubcategoria().getCategoria() != null) {
                    map.put("idCategoria", producto.getSubcategoria().getCategoria().getIdCategoria());
                    map.put("nombreCategoria", producto.getSubcategoria().getCategoria().getNombreCategoria());
                }
            }
            
            // Datos de vendedor
            if (producto.getVendedor() != null) {
                map.put("idVendedor", producto.getVendedor().getIdVendedor());
                map.put("nombreEmpresa", producto.getVendedor().getNombreEmpresa());
            }
            
            // URL de imagen (ya es URL completa de Cloudinary)
            map.put("imagenUrl", producto.getImagenProducto());
            
            return map;
        }).collect(Collectors.toList());
    }
}