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

    // ===================== CREAR PRODUCTO =====================
    @Override
    @Transactional
    public ProductoResponse crearProducto(ProductoRequest request) {
        System.out.println("🛍️ Creando nuevo producto: " + request.getNombreProducto());
        
        // Validar datos básicos
        validarDatosCreacion(request);
        
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
            if (request.getNombreProducto().trim().length() < 2) {
                throw new RuntimeException("El nombre del producto debe tener al menos 2 caracteres");
            }
            producto.setNombreProducto(request.getNombreProducto().trim());
        }
        
        if (request.getPrecioProducto() != null) {
            if (request.getPrecioProducto() <= 0) {
                throw new RuntimeException("El precio debe ser mayor a 0");
            }
            producto.setPrecioProducto(request.getPrecioProducto());
        }
        
        if (request.getDescripcionProducto() != null) {
            producto.setDescripcionProducto(request.getDescripcionProducto().trim());
        }
        
        if (request.getStockProducto() != null) {
            if (request.getStockProducto() < 0) {
                throw new RuntimeException("El stock no puede ser negativo");
            }
            producto.setStockProducto(request.getStockProducto());
        }
        
        if (request.getUnidad() != null) {
            producto.setUnidad(request.getUnidad().trim());
        }
        
        // ✅ ACTUALIZAR SUBCATEGORÍA SI SE PROPORCIONA
        if (request.getIdSubcategoria() != null) {
            Subcategoria subcategoria = subcategoriaRepository.findById(request.getIdSubcategoria())
                    .orElseThrow(() -> new RuntimeException("Subcategoría no existe"));
            producto.setSubcategoria(subcategoria);
        }
        
        // ✅ ACTUALIZAR IMAGEN SI SE PROPORCIONA NUEVA URL
        if (request.getImagenProducto() != null) {
            validarYGuardarImagen(request, producto);
        }
        
        producto.setUltimaActualizacion(LocalDateTime.now());
        productoRepository.save(producto);
        
        System.out.println("✅ Producto actualizado exitosamente");
        
        return convertir(producto);
    }

    // ===================== VALIDACIÓN DE DATOS PARA CREACIÓN =====================
    private void validarDatosCreacion(ProductoRequest request) {
        if (request.getNombreProducto() == null || request.getNombreProducto().trim().isEmpty()) {
            throw new RuntimeException("El nombre del producto es obligatorio");
        }
        
        if (request.getNombreProducto().trim().length() < 2) {
            throw new RuntimeException("El nombre del producto debe tener al menos 2 caracteres");
        }
        
        if (request.getPrecioProducto() == null || request.getPrecioProducto() <= 0) {
            throw new RuntimeException("El precio debe ser mayor a 0");
        }
        
        if (request.getStockProducto() == null || request.getStockProducto() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }
        
        if (request.getIdSubcategoria() == null) {
            throw new RuntimeException("La subcategoría es obligatoria");
        }
        
        if (request.getUnidad() == null || request.getUnidad().trim().isEmpty()) {
            throw new RuntimeException("La unidad de medida es obligatoria");
        }
        
        // Validar que se proporcione algún identificador de vendedor
        if (request.getIdVendedor() == null && request.getIdUsuario() == null) {
            throw new RuntimeException("Debe proporcionar idVendedor o idUsuario");
        }
    }

    // ===================== VALIDAR Y GUARDAR IMAGEN =====================
    private void validarYGuardarImagen(ProductoRequest request, Producto producto) {
        String imagenUrl = request.getImagenProducto();
        
        // Si la imagen es nula o vacía, establecer como null
        if (imagenUrl == null || imagenUrl.trim().isEmpty()) {
            System.out.println("⚠️ Producto sin imagen");
            producto.setImagenProducto(null);
            return;
        }
        
        System.out.println("📷 Procesando imagen del producto...");
        System.out.println("   URL recibida: " + (imagenUrl.length() > 100 ? imagenUrl.substring(0, 100) + "..." : imagenUrl));
        
        // ✅ VALIDAR QUE SEA URL VÁLIDA
        if (!esUrlValida(imagenUrl)) {
            System.err.println("❌ URL de imagen no válida");
            throw new RuntimeException("URL de imagen no válida. Debe ser una URL HTTP/HTTPS válida.");
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
        
        // Aceptar URLs HTTP/HTTPS
        return url.startsWith("http://") || url.startsWith("https://");
    }
    
    private boolean esUrlDeImagen(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        String urlLower = url.toLowerCase();
        return urlLower.contains(".jpg") || 
               urlLower.contains(".jpeg") || 
               urlLower.contains(".png") || 
               urlLower.contains(".gif") || 
               urlLower.contains(".webp") ||
               urlLower.contains(".bmp") ||
               urlLower.contains("image/");
    }

    // ===================== ASIGNAR DATOS DEL PRODUCTO =====================
    private void asignarDatos(Producto producto, ProductoRequest request) {
        // ✅ VALIDAR Y OBTENER SUBCATEGORÍA
        Subcategoria subcategoria = subcategoriaRepository.findById(request.getIdSubcategoria())
                .orElseThrow(() -> new RuntimeException("Subcategoría no existe"));
        
        producto.setSubcategoria(subcategoria);
        producto.setNombreProducto(request.getNombreProducto().trim());
        producto.setDescripcionProducto(request.getDescripcionProducto() != null ? 
                                      request.getDescripcionProducto().trim() : null);
        producto.setPrecioProducto(request.getPrecioProducto());
        producto.setStockProducto(request.getStockProducto());
        producto.setUnidad(request.getUnidad().trim());

        // ✅ OBTENER VENDEDOR
        Vendedor vendedor = obtenerVendedor(request);
        producto.setVendedor(vendedor);
    }
    
    private Vendedor obtenerVendedor(ProductoRequest request) {
        if (request.getIdVendedor() != null) {
            return vendedorRepository.findById(request.getIdVendedor())
                    .orElseThrow(() -> new RuntimeException("Vendedor no existe"));
        } else if (request.getIdUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no existe"));
            return vendedorRepository.findByUsuario(usuario)
                    .orElseThrow(() -> new RuntimeException("El usuario no es vendedor"));
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
        
        if (!producto.estaActivo()) {
            throw new RuntimeException("El producto ya está desactivado");
        }
        
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
        
        if (!producto.estaActivo()) {
            throw new RuntimeException("El producto ya está desactivado");
        }
        
        String motivoFinal = motivo != null && !motivo.trim().isEmpty() ? 
                           motivo.trim() : "Desactivado por administrador";
        
        producto.desactivar(motivoFinal);
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
        
        if (producto.estaActivo()) {
            throw new RuntimeException("El producto ya está activo");
        }
        
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
    public List<ProductoResponse> listarPorVendedor(Integer idVendedor) {
        System.out.println("📋 Listando productos del vendedor ID: " + idVendedor);
        
        Vendedor vendedor = vendedorRepository.findById(idVendedor)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
        
        return productoRepository.findByVendedor(vendedor).stream()
                .filter(Producto::estaActivo) // Solo activos
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    // ===================== LISTAR POR SUBCATEGORÍA =====================
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorSubcategoria(Integer idSubcategoria) {
        System.out.println("📋 Listando productos de subcategoría ID: " + idSubcategoria);
        
        Subcategoria subcategoria = subcategoriaRepository.findById(idSubcategoria)
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
    public ProductoResponse cambiarEstado(Integer id, String nuevoEstado) {
        System.out.println("🔄 Cambiando estado del producto ID: " + id + " a: " + nuevoEstado);
        
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        // Solo permitir cambiar estado si está activo
        if (!producto.estaActivo()) {
            throw new RuntimeException("No se puede cambiar estado de producto inactivo");
        }
        
        // Validar estado
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            throw new RuntimeException("El estado no puede estar vacío");
        }
        
        List<String> estadosValidos = List.of("Disponible", "Agotado", "Reservado", "Descontinuado");
        if (!estadosValidos.contains(nuevoEstado.trim())) {
            throw new RuntimeException("Estado inválido. Use: " + String.join(", ", estadosValidos));
        }
        
        producto.setEstado(nuevoEstado.trim());
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
        response.setUltimaActualizacion(producto.getUltimaActualizacion());

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

    // ===================== MÉTODOS AUXILIARES =====================
    
    /**
     * Método para listar todos los productos en formato Map para admin
     */
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
                
                if (producto.getVendedor().getUsuario() != null) {
                    map.put("nombreVendedor", 
                           producto.getVendedor().getUsuario().getNombre() + " " +
                           producto.getVendedor().getUsuario().getApellido());
                }
            }
            
            // URL de imagen
            map.put("imagenUrl", producto.getImagenProducto());
            
            return map;
        }).collect(Collectors.toList());
    }
    
    /**
     * Método para buscar productos por término
     */
    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarProductos(String termino) {
        System.out.println("🔍 Buscando productos con término: " + termino);
        
        if (termino == null || termino.trim().isEmpty()) {
            return listarActivos();
        }
        
        // Usar el método que tienes en el repositorio
        List<Producto> productos = productoRepository
                .findByNombreProductoContainingIgnoreCaseOrSubcategoria_NombreSubcategoriaContainingIgnoreCaseOrSubcategoria_Categoria_NombreCategoriaContainingIgnoreCase(
                    termino.trim(), termino.trim(), termino.trim());
        
        return productos.stream()
                .filter(Producto::estaActivo)
                .map(this::convertir)
                .collect(Collectors.toList());
    }
    
    /**
     * Método para listar productos por categoría
     */
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorCategoria(Integer idCategoria) {
        System.out.println("📋 Listando productos de categoría ID: " + idCategoria);
        
        // Usar el método que tienes en el repositorio
        List<Producto> productos = productoRepository.findByCategoriaIdCategoria(idCategoria);
        
        return productos.stream()
                .filter(Producto::estaActivo)
                .map(this::convertir)
                .collect(Collectors.toList());
    }
    
    /**
     * Verificar si existen productos asociados a una categoría
     */
    @Transactional(readOnly = true)
    public boolean existeProductosConCategoria(Integer idCategoria) {
        return productoRepository.existsByCategoriaIdCategoria(idCategoria);
    }
    
    /**
     * Verificar si existen productos asociados a una subcategoría
     */
    @Transactional(readOnly = true)
    public boolean existeProductosConSubcategoria(Integer idSubcategoria) {
        return productoRepository.existsBySubcategoriaIdSubcategoria(idSubcategoria);
    }
}