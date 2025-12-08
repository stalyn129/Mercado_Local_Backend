package com.mercadolocalia.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.repositories.PedidoRepository;
import com.mercadolocalia.repositories.ProductoRepository;

@RestController
@RequestMapping("/reportes")
public class ReportesController {

    @Autowired
    private PedidoRepository pedidoRepo;

    // ======================== VENTAS POR CATEGORÍA ========================
    @GetMapping("/ventas-por-categoria")
    public List<Map<String, Object>> ventasPorCategoria() {
        return pedidoRepo.obtenerVentasPorCategoria();
    }
    
    @Autowired
    private ProductoRepository productoRepo;

    // ======================== STOCK POR PRODUCTOS ========================
    @GetMapping("/stock-productos")
    public List<Map<String, Object>> stockProductos() {
        return productoRepo.obtenerStockProductos();
    }
}
