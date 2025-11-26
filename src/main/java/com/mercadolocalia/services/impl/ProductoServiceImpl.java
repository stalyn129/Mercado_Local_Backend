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

    // ===================== ACTUALIZAR 🔥 =====================
    @Override
    public ProductoResponse actualizarProducto(Integer id, ProductoRequest request) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id " + id));

        asignarDatos(p, request);
        guardarImagen(request, p); // 🔥 cambia imagen solo si se envía

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

        // Obtener vendedor
        if (r.getIdVendedor()!=null) {
            Vendedor v = vendedorRepository.findById(r.getIdVendedor())
                    .orElseThrow(()-> new RuntimeException("Vendedor no existe"));
            p.setVendedor(v);
        } else if(r.getIdUsuario()!=null) {
            Usuario u = usuarioRepository.findById(r.getIdUsuario())
                    .orElseThrow(()-> new RuntimeException("Usuario no existe"));
            p.setVendedor(vendedorRepository.findByUsuario(u));
        }
    }

    private void guardarImagen(ProductoRequest r, Producto p) {
        try {
            if (r.getImagen()!=null && !r.getImagen().isEmpty()) {
                String carpeta="uploads/";
                File dir = new File(carpeta);
                if(!dir.exists()) dir.mkdirs();

                String nombre = System.currentTimeMillis()+"_"+r.getImagen().getOriginalFilename();
                Files.write(Paths.get(carpeta+nombre), r.getImagen().getBytes());

                p.setImagenProducto("http://localhost:8080/"+carpeta+nombre);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error subiendo imagen");
        }
    }

    // ===================== OTROS =====================
    @Override public void eliminarProducto(Integer id) { productoRepository.deleteById(id); }
    @Override public ProductoResponse obtenerPorId(Integer id) { return convertir(productoRepository.findById(id).orElseThrow()); }
    @Override public List<ProductoResponse> listarPorVendedor(Integer id) { return productoRepository.findByVendedor(vendedorRepository.findById(id).orElseThrow()).stream().map(this::convertir).collect(Collectors.toList()); }
    @Override public List<ProductoResponse> listarPorSubcategoria(Integer id) { return productoRepository.findBySubcategoria(subcategoriaRepository.findById(id).orElseThrow()).stream().map(this::convertir).collect(Collectors.toList()); }
    @Override public List<ProductoResponse> listarTodos() { return productoRepository.findAll().stream().map(this::convertir).collect(Collectors.toList()); }
    @Override public ProductoResponse cambiarEstado(Integer id, String estado) { Producto p=productoRepository.findById(id).orElseThrow(); p.setEstado(estado); productoRepository.save(p); return convertir(p); }

    private ProductoResponse convertir(Producto p) {
        ProductoResponse r = new ProductoResponse();
        r.setIdProducto(p.getIdProducto());
        r.setNombreProducto(p.getNombreProducto());
        r.setDescripcionProducto(p.getDescripcionProducto());
        r.setPrecioProducto(p.getPrecioProducto());
        r.setStockProducto(p.getStockProducto());
        r.setImagenProducto(p.getImagenProducto());
        r.setFechaPublicacion(p.getFechaPublicacion());
        r.setEstado(p.getEstado());
        if(p.getVendedor()!=null) r.setIdVendedor(p.getVendedor().getIdVendedor());
        if(p.getSubcategoria()!=null) r.setIdSubcategoria(p.getSubcategoria().getIdSubcategoria());
        return r;
    }
}
