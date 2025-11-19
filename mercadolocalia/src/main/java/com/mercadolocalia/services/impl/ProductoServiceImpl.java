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
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.repositories.SubcategoriaRepository;
import com.mercadolocalia.repositories.VendedorRepository;
import com.mercadolocalia.services.ProductoService;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VendedorRepository vendedorRepository;

    @Autowired
    private SubcategoriaRepository subcategoriaRepository;

    // ===============================================================
    // CREAR PRODUCTO
    // ===============================================================
    @Override
    public ProductoResponse crearProducto(ProductoRequest request) {

        Vendedor vendedor = vendedorRepository.findById(request.getIdVendedor())
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        Subcategoria subcategoria = null;
        if (request.getIdSubcategoria() != null) {
            subcategoria = subcategoriaRepository.findById(request.getIdSubcategoria())
                    .orElseThrow(() -> new RuntimeException("Subcategoría no encontrada"));
        }

        Producto producto = new Producto();
        producto.setVendedor(vendedor);
        producto.setSubcategoria(subcategoria);
        producto.setNombreProducto(request.getNombreProducto());
        producto.setDescripcionProducto(request.getDescripcionProducto());
        producto.setPrecioProducto(request.getPrecioProducto());
        producto.setStockProducto(request.getStockProducto());
        producto.setImagenProducto(request.getImagenProducto());
        producto.setFechaPublicacion(LocalDateTime.now());
        producto.setEstado("Disponible");

        productoRepository.save(producto);

        return convertir(producto);
    }

    // ===============================================================
    // ACTUALIZAR PRODUCTO
    // ===============================================================
    @Override
    public ProductoResponse actualizarProducto(Integer id, ProductoRequest request) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Vendedor vendedor = vendedorRepository.findById(request.getIdVendedor())
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        Subcategoria subcategoria = null;
        if (request.getIdSubcategoria() != null) {
            subcategoria = subcategoriaRepository.findById(request.getIdSubcategoria())
                    .orElseThrow(() -> new RuntimeException("Subcategoría no encontrada"));
        }

        producto.setVendedor(vendedor);
        producto.setSubcategoria(subcategoria);
        producto.setNombreProducto(request.getNombreProducto());
        producto.setDescripcionProducto(request.getDescripcionProducto());
        producto.setPrecioProducto(request.getPrecioProducto());
        producto.setStockProducto(request.getStockProducto());
        producto.setImagenProducto(request.getImagenProducto());
        // Mantener estado anterior
        producto.setEstado(producto.getEstado());

        productoRepository.save(producto);

        return convertir(producto);
    }

    // ===============================================================
    // ELIMINAR PRODUCTO
    // ===============================================================
    @Override
    public void eliminarProducto(Integer id) {
        productoRepository.deleteById(id);
    }

    // ===============================================================
    // OBTENER POR ID
    // ===============================================================
    @Override
    public ProductoResponse obtenerPorId(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        return convertir(producto);
    }

    // ===============================================================
    // LISTAR TODOS LOS PRODUCTOS
    // ===============================================================
    @Override
    public List<ProductoResponse> listarTodos() {
        return productoRepository.findAll()
                .stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    // ===============================================================
    // LISTAR POR VENDEDOR
    // ===============================================================
    @Override
    public List<ProductoResponse> listarPorVendedor(Integer idVendedor) {

        Vendedor vendedor = vendedorRepository.findById(idVendedor)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        List<Producto> productos = productoRepository.findByVendedor(vendedor);

        return productos.stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    // ===============================================================
    // LISTAR POR SUBCATEGORÍA
    // ===============================================================
    @Override
    public List<ProductoResponse> listarPorSubcategoria(Integer idSubcategoria) {

        Subcategoria subcategoria = subcategoriaRepository.findById(idSubcategoria)
                .orElseThrow(() -> new RuntimeException("Subcategoría no encontrada"));

        List<Producto> productos = productoRepository.findBySubcategoria(subcategoria);

        return productos.stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    // ===============================================================
    // CONVERTIR ENTIDAD → DTO
    // ===============================================================
    private ProductoResponse convertir(Producto producto) {

        ProductoResponse res = new ProductoResponse();

        res.setIdProducto(producto.getIdProducto());
        res.setNombreProducto(producto.getNombreProducto());
        res.setDescripcionProducto(producto.getDescripcionProducto());
        res.setPrecioProducto(producto.getPrecioProducto());
        res.setStockProducto(producto.getStockProducto());
        res.setImagenProducto(producto.getImagenProducto());
        res.setFechaPublicacion(producto.getFechaPublicacion());
        res.setEstado(producto.getEstado());

        // Vendedor
        if (producto.getVendedor() != null) {
            res.setIdVendedor(producto.getVendedor().getIdVendedor());
            res.setNombreEmpresa(producto.getVendedor().getNombreEmpresa());
        }

        // Subcategoría
        if (producto.getSubcategoria() != null) {
            res.setIdSubcategoria(producto.getSubcategoria().getIdSubcategoria());
            res.setNombreSubcategoria(producto.getSubcategoria().getNombreSubcategoria());
        }

        return res;
    }
}
