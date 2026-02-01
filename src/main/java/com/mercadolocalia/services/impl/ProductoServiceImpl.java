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

    @Autowired private ProductoRepository productoRepository;
    @Autowired private VendedorRepository vendedorRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SubcategoriaRepository subcategoriaRepository;

    // ===================== CREAR =====================
    @Override
    @Transactional
    public ProductoResponse crearProducto(ProductoRequest request) {
        Producto p = new Producto();
        asignarDatos(p, request);
        p.setFechaPublicacion(LocalDateTime.now());
        p.setEstado("Disponible");
        p.setActivo(true); // ✅ Por defecto activo
        p.setUltimaActualizacion(LocalDateTime.now());
        guardarImagen(request, p);
        productoRepository.save(p);
        return convertir(p);
    }

    // ===================== ACTUALIZAR (SOLO NOMBRE Y PRECIO) =====================
    @Override
    @Transactional
    public ProductoResponse actualizarProducto(Integer id, ProductoRequest request) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id " + id));
        
        // Verificar que esté activo
        if (!p.estaActivo()) {
            throw new RuntimeException("No se puede actualizar un producto inactivo");
        }
        
        // ✅ SOLO ACTUALIZAR NOMBRE Y PRECIO
        if (request.getNombreProducto() != null && !request.getNombreProducto().trim().isEmpty()) {
            p.setNombreProducto(request.getNombreProducto());
        }
        
        if (request.getPrecioProducto() != null && request.getPrecioProducto() > 0) {
            p.setPrecioProducto(request.getPrecioProducto());
        }
        
        // ❌ NO permitir cambiar stock, unidad, etc.
        
        p.setUltimaActualizacion(LocalDateTime.now());
        
        if (request.getImagenProducto() != null && !request.getImagenProducto().isEmpty()) {
            p.setImagenProducto(request.getImagenProducto());
        }
        
        productoRepository.save(p);
        return convertir(p);
    }

    // ===================== BORRADO LÓGICO - ELIMINAR =====================
    @Override
    @Transactional
    public void eliminarProducto(Integer id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        // BORRADO LÓGICO - Desactivar en lugar de eliminar
        p.desactivar("Eliminado por administrador");
        productoRepository.save(p);
    }
    
    // ===================== DESACTIVAR PRODUCTO =====================
    @Override
    @Transactional
    public ProductoResponse desactivarProducto(Integer id, String motivo) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        p.desactivar(motivo);
        productoRepository.save(p);
        return convertir(p);
    }
    
    // ===================== REACTIVAR PRODUCTO =====================
    @Override
    @Transactional
    public ProductoResponse reactivarProducto(Integer id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        p.reactivar();
        productoRepository.save(p);
        return convertir(p);
    }

    private void asignarDatos(Producto p, ProductoRequest r) {
        Subcategoria sub = subcategoriaRepository.findById(r.getIdSubcategoria())
                .orElseThrow(() -> new RuntimeException("Subcategoría no existe"));

        p.setSubcategoria(sub);
        p.setNombreProducto(r.getNombreProducto());
        p.setDescripcionProducto(r.getDescripcionProducto());
        p.setPrecioProducto(r.getPrecioProducto());
        p.setStockProducto(r.getStockProducto());
        p.setUnidad(r.getUnidad());
        p.setUltimaActualizacion(LocalDateTime.now());

        if (r.getIdVendedor() != null) {
            Vendedor v = vendedorRepository.findById(r.getIdVendedor())
                    .orElseThrow(() -> new RuntimeException("Vendedor no existe"));
            p.setVendedor(v);
        } else if (r.getIdUsuario() != null) {
            Usuario u = usuarioRepository.findById(r.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no existe"));
            Vendedor v = vendedorRepository.findByUsuario(u)
                    .orElseThrow(() -> new RuntimeException("El usuario no es vendedor"));
            p.setVendedor(v);
        }
    }

    // ================== GUARDAR IMAGEN ==================
    private void guardarImagen(ProductoRequest r, Producto p) {
        if (r.getImagenProducto() != null && !r.getImagenProducto().isEmpty()) {
            p.setImagenProducto(r.getImagenProducto());
        }
    }

    // ================= CONSULTAS =================
    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Integer id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Producto no encontrado con id " + id));
        
        // Para frontend público, solo devolver si está activo
        if (!p.estaActivo()) {
            throw new RuntimeException("Producto no disponible");
        }
        
        return convertir(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorVendedor(Integer id) {
        Vendedor vendedor = vendedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
        
        return productoRepository.findByVendedor(vendedor).stream()
                .filter(Producto::estaActivo) // Solo activos
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorSubcategoria(Integer id) {
        Subcategoria subcategoria = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategoría no encontrada"));
        
        return productoRepository.findBySubcategoria(subcategoria).stream()
                .filter(Producto::estaActivo) // Solo activos
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    // ================= LISTAR TODOS (PARA ADMIN) =================
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodos() {
        return productoRepository.findAll().stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }
    
    // ================= LISTAR ACTIVOS (PARA EXPLORAR) =================
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarActivos() {
        return productoRepository.findAll().stream()
                .filter(Producto::estaActivo)
                .map(this::convertir)
                .collect(Collectors.toList());
    }
    
    // ================= LISTAR INACTIVOS =================
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarInactivos() {
        return productoRepository.findAll().stream()
                .filter(p -> !p.estaActivo())
                .map(this::convertir)
                .collect(Collectors.toList());
    }
    
    // ================= LISTAR PARA ADMIN (CON DATOS COMPLETOS) =================
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarTodosParaAdmin() {
        return productoRepository.findAll().stream().map(p -> {
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
            map.put("fechaPublicacion", p.getFechaPublicacion());
            
            // Datos de subcategoría
            if (p.getSubcategoria() != null) {
                map.put("idSubcategoria", p.getSubcategoria().getIdSubcategoria());
                map.put("nombreSubcategoria", p.getSubcategoria().getNombreSubcategoria());
                
                // Datos de categoría
                if (p.getSubcategoria().getCategoria() != null) {
                    map.put("idCategoria", p.getSubcategoria().getCategoria().getIdCategoria());
                    map.put("nombreCategoria", p.getSubcategoria().getCategoria().getNombreCategoria());
                }
            }
            
            // Datos de vendedor
            if (p.getVendedor() != null) {
                map.put("idVendedor", p.getVendedor().getIdVendedor());
                map.put("nombreEmpresa", p.getVendedor().getNombreEmpresa());
            }
            
            // URL completa de imagen
            if (p.getImagenProducto() != null && !p.getImagenProducto().isEmpty()) {
                if (p.getImagenProducto().startsWith("http")) {
                    map.put("imagenUrl", p.getImagenProducto());
                } else {
                    map.put("imagenUrl", "http://localhost:8080/uploads/" + p.getImagenProducto());
                }
            } else {
                map.put("imagenUrl", null);
            }
            
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductoResponse cambiarEstado(Integer id, String estado) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        // Solo permitir cambiar estado si está activo
        if (!p.estaActivo()) {
            throw new RuntimeException("No se puede cambiar estado de producto inactivo");
        }
        
        p.setEstado(estado);
        p.setUltimaActualizacion(LocalDateTime.now());
        productoRepository.save(p);
        return convertir(p);
    }

    // ================= DETALLE COMPLETO =================
    @Override
    @Transactional(readOnly = true)
    public ProductoDetalleResponse obtenerDetalleProducto(Integer idProducto) {
        Producto p = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductoDetalleResponse r = new ProductoDetalleResponse();

        // BÁSICO
        r.setIdProducto(p.getIdProducto());
        r.setNombreProducto(p.getNombreProducto());
        r.setDescripcionProducto(p.getDescripcionProducto());
        r.setPrecioProducto(p.getPrecioProducto());
        r.setStockProducto(p.getStockProducto());
        r.setImagenProducto(p.getImagenProducto());
        r.setFechaPublicacion(p.getFechaPublicacion());
        r.setEstado(p.getEstado());
        r.setUnidad(p.getUnidad());
        r.setActivo(p.getActivo()); // ✅ Nuevo campo
        r.setFechaDesactivacion(p.getFechaDesactivacion()); // ✅ Nuevo campo
        r.setMotivoDesactivacion(p.getMotivoDesactivacion()); // ✅ Nuevo campo

        // SUBCATEGORÍA + CATEGORÍA
        if (p.getSubcategoria() != null) {
            r.setIdSubcategoria(p.getSubcategoria().getIdSubcategoria());
            r.setNombreSubcategoria(p.getSubcategoria().getNombreSubcategoria());

            if (p.getSubcategoria().getCategoria() != null){
                r.setIdCategoria(p.getSubcategoria().getCategoria().getIdCategoria());
                r.setNombreCategoria(p.getSubcategoria().getCategoria().getNombreCategoria());
            }
        }

        // VENDEDOR
        if (p.getVendedor() != null){
            r.setIdVendedor(p.getVendedor().getIdVendedor());
            r.setNombreEmpresa(p.getVendedor().getNombreEmpresa());

            if(p.getVendedor().getUsuario() != null){
                r.setNombreVendedor(
                        p.getVendedor().getUsuario().getNombre() + " " +
                        p.getVendedor().getUsuario().getApellido()
                );
            }
        }

        // VALORACIONES
        if(p.getValoraciones() != null && !p.getValoraciones().isEmpty()){
            r.setPromedioValoracion(
                    p.getValoraciones().stream()
                            .mapToDouble(v -> v.getCalificacion())
                            .average().orElse(0.0));

            r.setTotalValoraciones(p.getValoraciones().size());

            r.setValoraciones(
                    p.getValoraciones().stream().map(v -> {
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
            r.setPromedioValoracion(0.0);
            r.setTotalValoraciones(0);
            r.setValoraciones(List.of());
        }

        return r;
    }

    // ================= CONVERTIR RESPONSE GENERAL =================
    private ProductoResponse convertir(Producto p) {
        ProductoResponse r = new ProductoResponse();

        r.setIdProducto(p.getIdProducto());
        r.setNombreProducto(p.getNombreProducto());
        r.setDescripcionProducto(p.getDescripcionProducto());
        r.setPrecioProducto(p.getPrecioProducto());
        r.setStockProducto(p.getStockProducto());
        r.setUnidad(p.getUnidad());
        r.setImagenProducto(p.getImagenProducto());
        r.setFechaPublicacion(p.getFechaPublicacion());
        r.setEstado(p.getEstado());
        
        // ✅ CAMPOS DE BORRADO LÓGICO
        r.setActivo(p.getActivo());
        r.setFechaDesactivacion(p.getFechaDesactivacion());
        r.setMotivoDesactivacion(p.getMotivoDesactivacion());
        r.setUltimaActualizacion(p.getUltimaActualizacion());

        // SUBCATEGORÍA
        if (p.getSubcategoria() != null) {
            r.setIdSubcategoria(p.getSubcategoria().getIdSubcategoria());
            r.setNombreSubcategoria(p.getSubcategoria().getNombreSubcategoria());

            if (p.getSubcategoria().getCategoria() != null) {
                r.setIdCategoria(p.getSubcategoria().getCategoria().getIdCategoria());
                r.setNombreCategoria(p.getSubcategoria().getCategoria().getNombreCategoria());
            }
        }

        // VENDEDOR
        if (p.getVendedor() != null) {
            r.setIdVendedor(p.getVendedor().getIdVendedor());
            r.setNombreEmpresa(p.getVendedor().getNombreEmpresa());
        }

        // VALORACIONES (PROMEDIO + TOTAL)
        if (p.getValoraciones() != null && !p.getValoraciones().isEmpty()) {
            double promedio = p.getValoraciones()
                    .stream()
                    .mapToDouble(v -> v.getCalificacion())
                    .average()
                    .orElse(0.0);

            r.setPromedioValoracion(promedio);
            r.setTotalValoraciones(p.getValoraciones().size());
        } else {
            r.setPromedioValoracion(0.0);
            r.setTotalValoraciones(0);
        }

        return r;
    }

    // ================= OBTENER LOS 20 MEJORES TOP =================
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTop20Mejores() {
        PageRequest pageable = PageRequest.of(0, 20);

        return productoRepository.findTop20Mejores(pageable)
                .stream()
                .filter(Producto::estaActivo) // Solo activos
                .map(this::convertir)
                .collect(Collectors.toList());
    }
}