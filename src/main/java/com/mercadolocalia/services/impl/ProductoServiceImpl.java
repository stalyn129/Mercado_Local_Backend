package com.mercadolocalia.services.impl;

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

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VendedorRepository vendedorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SubcategoriaRepository subcategoriaRepository;

    // =========================================================
    // CREAR PRODUCTO
    // =========================================================
    @Override
    public ProductoResponse crearProducto(ProductoRequest request) {

        validarRequestBasico(request);

        Vendedor vendedor = obtenerVendedorDesdeRequest(request);

        Subcategoria subcategoria = subcategoriaRepository.findById(request.getIdSubcategoria())
                .orElseThrow(() -> new RuntimeException(
                        "Subcategoría no encontrada con id " + request.getIdSubcategoria()));

        Producto p = new Producto();
        p.setVendedor(vendedor);
        p.setSubcategoria(subcategoria);
        p.setNombreProducto(request.getNombreProducto());
        p.setDescripcionProducto(request.getDescripcionProducto());
        p.setPrecioProducto(request.getPrecioProducto());
        p.setStockProducto(request.getStockProducto());
        p.setImagenProducto(request.getImagenProducto());
        p.setFechaPublicacion(LocalDateTime.now());
        p.setEstado("Disponible");

        productoRepository.save(p);

        return convertir(p);
    }

    // =========================================================
    // ACTUALIZAR PRODUCTO
    // =========================================================
    @Override
    public ProductoResponse actualizarProducto(Integer id, ProductoRequest request) {

        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Producto no encontrado con id " + id));

        Vendedor vendedor = obtenerVendedorDesdeRequest(request);

        Subcategoria subcategoria = subcategoriaRepository.findById(request.getIdSubcategoria())
                .orElseThrow(() -> new RuntimeException(
                        "Subcategoría no encontrada con id " + request.getIdSubcategoria()));

        p.setVendedor(vendedor);
        p.setSubcategoria(subcategoria);
        p.setNombreProducto(request.getNombreProducto());
        p.setDescripcionProducto(request.getDescripcionProducto());
        p.setPrecioProducto(request.getPrecioProducto());
        p.setStockProducto(request.getStockProducto());
        p.setImagenProducto(request.getImagenProducto());

        productoRepository.save(p);

        return convertir(p);
    }

    // =========================================================
    // ELIMINAR PRODUCTO
    // =========================================================
    @Override
    public void eliminarProducto(Integer id) {
        productoRepository.deleteById(id);
    }

    // =========================================================
    // OBTENER POR ID
    // =========================================================
    @Override
    public ProductoResponse obtenerPorId(Integer id) {

        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Producto no encontrado con id " + id));

        return convertir(p);
    }

    // =========================================================
    // LISTAR POR VENDEDOR
    // =========================================================
    @Override
    public List<ProductoResponse> listarPorVendedor(Integer idVendedor) {

        Vendedor vendedor = vendedorRepository.findById(idVendedor)
                .orElseThrow(() -> new RuntimeException(
                        "Vendedor no encontrado con id " + idVendedor));

        return productoRepository.findByVendedor(vendedor)
                .stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    // =========================================================
    // LISTAR POR SUBCATEGORÍA
    // =========================================================
    @Override
    public List<ProductoResponse> listarPorSubcategoria(Integer idSubcategoria) {

        Subcategoria sub = subcategoriaRepository.findById(idSubcategoria)
                .orElseThrow(() -> new RuntimeException(
                        "Subcategoría no encontrada con id " + idSubcategoria));

        return productoRepository.findBySubcategoria(sub)
                .stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    // =========================================================
    // LISTAR TODOS
    // =========================================================
    @Override
    public List<ProductoResponse> listarTodos() {

        return productoRepository.findAll()
                .stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    // =========================================================
    // CAMBIAR ESTADO
    // =========================================================
    @Override
    public ProductoResponse cambiarEstado(Integer idProducto, String estado) {

        Producto p = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException(
                        "Producto no encontrado con id " + idProducto));

        p.setEstado(estado);
        productoRepository.save(p);

        return convertir(p);
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================
    private void validarRequestBasico(ProductoRequest r) {

        if (r.getNombreProducto() == null || r.getNombreProducto().isBlank())
            throw new IllegalArgumentException("El nombre del producto es obligatorio");

        if (r.getPrecioProducto() == null)
            throw new IllegalArgumentException("Debe enviar el precio");

        if (r.getStockProducto() == null)
            throw new IllegalArgumentException("Debe enviar stockProducto");

        if (r.getIdSubcategoria() == null)
            throw new IllegalArgumentException("Debe enviar idSubcategoria");

        if (r.getIdVendedor() == null && r.getIdUsuario() == null)
            throw new IllegalArgumentException("Debe enviarse idVendedor o idUsuario");
    }

    // =========================================================
    // OBTENER VENDEDOR DESDE REQUEST
    // =========================================================
    private Vendedor obtenerVendedorDesdeRequest(ProductoRequest request) {

        // Caso A → idVendedor
        if (request.getIdVendedor() != null) {
            return vendedorRepository.findById(request.getIdVendedor())
                    .orElseThrow(() -> new RuntimeException(
                            "Vendedor no encontrado con id " + request.getIdVendedor()));
        }

        // Caso B → idUsuario
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado con id " + request.getIdUsuario()));

        Vendedor vendedor = vendedorRepository.findByUsuario(usuario);

        if (vendedor == null)
            throw new RuntimeException("Ese usuario no tiene perfil de vendedor");

        return vendedor;
    }

    // =========================================================
    // CONVERTIR A DTO RESPONSE
    // =========================================================
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

        if (p.getVendedor() != null) {
            r.setIdVendedor(p.getVendedor().getIdVendedor());
            r.setNombreEmpresa(p.getVendedor().getNombreEmpresa());
        }

        if (p.getSubcategoria() != null) {
            r.setIdSubcategoria(p.getSubcategoria().getIdSubcategoria());
            r.setNombreSubcategoria(p.getSubcategoria().getNombreSubcategoria());
        }

        return r;
    }
}
