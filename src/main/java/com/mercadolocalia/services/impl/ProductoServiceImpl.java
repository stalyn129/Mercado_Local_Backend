package com.mercadolocalia.services.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public ProductoResponse crearProducto(ProductoRequest request) {
        Producto p = new Producto();
        asignarDatos(p, request);
        p.setFechaPublicacion(LocalDateTime.now());
        p.setEstado("Disponible");
        guardarImagen(request, p);
        productoRepository.save(p);
        return convertir(p);
    }

    // ===================== ACTUALIZAR =====================
    @Override
    public ProductoResponse actualizarProducto(Integer id, ProductoRequest request) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id " + id));

        asignarDatos(p, request);
        guardarImagen(request, p);
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

        // ========== 🚨 NUEVO: UNIDAD ==========
        p.setUnidad(r.getUnidad()); // kg - unidad - litro - libra - caja

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

 // ================== 🔥 OPCIÓN A — IMAGEN COMO URL ==================
    private void guardarImagen(ProductoRequest r, Producto p) {

        // Si la imagen llega como URL desde Postman/React
        if (r.getImagenProducto() != null && !r.getImagenProducto().isEmpty()) {
            p.setImagenProducto(r.getImagenProducto());
        }
    }


    // ================= CONSULTAS =================
    @Override
    public ProductoResponse obtenerPorId(Integer id) {
        return productoRepository.findById(id)
                .map(this::convertir)
                .orElseThrow(() -> new RuntimeException("❌ Producto no encontrado con id " + id));
    }

    @Override public void eliminarProducto(Integer id) { productoRepository.deleteById(id); }
    @Override public List<ProductoResponse> listarPorVendedor(Integer id) {
        return productoRepository.findByVendedor(vendedorRepository.findById(id).orElseThrow())
               .stream().map(this::convertir).collect(Collectors.toList());
    }
    @Override public List<ProductoResponse> listarPorSubcategoria(Integer id) {
        return productoRepository.findBySubcategoria(subcategoriaRepository.findById(id).orElseThrow())
               .stream().map(this::convertir).collect(Collectors.toList());
    }
    @Override public List<ProductoResponse> listarTodos() {
        return productoRepository.findAll().stream().map(this::convertir).collect(Collectors.toList());
    }
    @Override public ProductoResponse cambiarEstado(Integer id, String estado) {
        Producto p=productoRepository.findById(id).orElseThrow();
        p.setEstado(estado); productoRepository.save(p);
        return convertir(p);
    }

    // ================= DETALLE COMPLETO =================
    @Override
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
        r.setUnidad(p.getUnidad()); // ⚠ envío a frontend

        // SUBCATEGORÍA + CATEGORÍA
        if (p.getSubcategoria()!=null) {
            r.setIdSubcategoria(p.getSubcategoria().getIdSubcategoria());
            r.setNombreSubcategoria(p.getSubcategoria().getNombreSubcategoria());

            if (p.getSubcategoria().getCategoria()!=null){
                r.setIdCategoria(p.getSubcategoria().getCategoria().getIdCategoria());
                r.setNombreCategoria(p.getSubcategoria().getCategoria().getNombreCategoria());
            }
        }

        // VENDEDOR
        if (p.getVendedor()!=null){
            r.setIdVendedor(p.getVendedor().getIdVendedor());
            r.setNombreEmpresa(p.getVendedor().getNombreEmpresa());

            if(p.getVendedor().getUsuario()!=null){
                r.setNombreVendedor(
                        p.getVendedor().getUsuario().getNombre()+" "+
                        p.getVendedor().getUsuario().getApellido()
                );
            }
        }

        // VALORACIONES
        if(p.getValoraciones()!=null && !p.getValoraciones().isEmpty()){

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

                        if(v.getConsumidor()!=null && v.getConsumidor().getUsuario()!=null){
                            vr.setIdConsumidor(v.getConsumidor().getIdConsumidor());
                            vr.setNombreConsumidor(v.getConsumidor().getUsuario().getNombre()+" "+
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

        // ⭐ VALORACIONES (PROMEDIO + TOTAL)
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
    public List<ProductoResponse> listarTop20Mejores() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 20);

        return productoRepository.findTop20Mejores(pageable)
                .stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

}
