package com.mercadolocalia.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.entities.Valoracion;
import com.mercadolocalia.repositories.PedidoRepository;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.repositories.ValoracionRepository;

@RestController
@RequestMapping("/reportes") 
public class ReportesController {

    @Autowired
    private PedidoRepository pedidoRepo;

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private ValoracionRepository valoracionRepo;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ======================== ENDPOINTS DE ADMIN (VIEJOS) ========================
    
    // 1. VENTAS POR CATEGORÍA (para admin dashboard)
    @GetMapping("/ventas-por-categoria")
    public ResponseEntity<List<Map<String, Object>>> ventasPorCategoria() {
        try {
            List<Map<String, Object>> ventas = pedidoRepo.obtenerVentasPorCategoria();
            return ResponseEntity.ok(ventas != null ? ventas : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    // 2. STOCK POR PRODUCTOS (para admin dashboard)
    @GetMapping("/stock-productos")
    public ResponseEntity<List<Map<String, Object>>> stockProductos() {
        try {
            List<Map<String, Object>> stock = productoRepo.obtenerStockProductos();
            return ResponseEntity.ok(stock != null ? stock : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // ======================== ENDPOINTS PRINCIPALES DEL DASHBOARD (NUEVOS) ========================

    // 3. ESTADÍSTICAS GENERALES DEL VENDEDOR
    @GetMapping("/dashboard/{vendedorId}")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable Integer vendedorId) {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // Estadísticas básicas
            Map<String, Object> estadisticas = pedidoRepo.obtenerEstadisticasVendedor(vendedorId);
            dashboard.put("estadisticas", estadisticas != null ? estadisticas : Map.of(
                "totalPedidos", 0,
                "ingresosTotales", 0.0,
                "promedioVenta", 0.0,
                "clientesUnicos", 0
            ));
            
            // Ventas mensuales
            List<Object[]> ventasMensuales = pedidoRepo.obtenerVentasMensualesPorVendedor(vendedorId);
            dashboard.put("ventasMensuales", convertirVentasMensuales(ventasMensuales));
            
            // Top productos
            List<Map<String, Object>> productosTop = pedidoRepo.obtenerProductosTopPorVendedor(vendedorId);
            dashboard.put("productosTop", productosTop != null ? productosTop : new ArrayList<>());
            
            // Clientes recurrentes
            List<Map<String, Object>> clientesRecurrentes = pedidoRepo.obtenerClientesRecurrentesPorVendedor(vendedorId);
            dashboard.put("clientesRecurrentes", clientesRecurrentes != null ? clientesRecurrentes : new ArrayList<>());
            
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Error al cargar dashboard: " + e.getMessage()));
        }
    }

    // 4. VENTAS MENSUALES DETALLADAS
    @GetMapping("/ventas-mensuales/{vendedorId}")
    public ResponseEntity<List<Map<String, Object>>> getVentasMensuales(@PathVariable Integer vendedorId) {
        try {
            List<Object[]> resultados = pedidoRepo.obtenerVentasMensualesPorVendedor(vendedorId);
            return ResponseEntity.ok(convertirVentasMensuales(resultados));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
    }

    // 5. TOP PRODUCTOS
    @GetMapping("/productos-top/{vendedorId}")
    public ResponseEntity<List<Map<String, Object>>> getProductosTop(@PathVariable Integer vendedorId) {
        try {
            List<Map<String, Object>> productos = pedidoRepo.obtenerProductosTopPorVendedor(vendedorId);
            return ResponseEntity.ok(productos != null ? productos : new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 6. CLIENTES RECURRENTES
    @GetMapping("/clientes-recurrentes/{vendedorId}")
    public ResponseEntity<List<Map<String, Object>>> getClientesRecurrentes(@PathVariable Integer vendedorId) {
        try {
            List<Map<String, Object>> clientes = pedidoRepo.obtenerClientesRecurrentesPorVendedor(vendedorId);
            return ResponseEntity.ok(clientes != null ? clientes : new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 7. ESTADO DE PEDIDOS
    @GetMapping("/estados-pedidos/{vendedorId}")
    public ResponseEntity<List<Map<String, Object>>> getEstadosPedidos(@PathVariable Integer vendedorId) {
        try {
            List<Object[]> resultados = pedidoRepo.obtenerPedidosPorEstado(vendedorId);
            List<Map<String, Object>> estados = new ArrayList<>();
            
            if (resultados != null) {
                for (Object[] row : resultados) {
                    Map<String, Object> estado = new HashMap<>();
                    estado.put("estado", row[0]);
                    estado.put("cantidad", row[1]);
                    estados.add(estado);
                }
            }
            
            return ResponseEntity.ok(estados);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 8. TENDENCIA DE VENTAS (ÚLTIMOS 30 DÍAS) - CORREGIDO
    @GetMapping("/tendencia-ventas/{vendedorId}")
    public ResponseEntity<List<Map<String, Object>>> getTendenciaVentas(@PathVariable Integer vendedorId) {
        try {
            // AGREGAR ESTA LÍNEA - Fecha de inicio para los últimos 30 días
            LocalDateTime fechaInicio = LocalDateTime.now().minusDays(30);
            
            List<Object[]> resultados = pedidoRepo.obtenerTendenciaVentas(vendedorId, fechaInicio);
            List<Map<String, Object>> tendencia = new ArrayList<>();
            
            if (resultados != null) {
                for (Object[] row : resultados) {
                    Map<String, Object> dia = new HashMap<>();
                    dia.put("fecha", row[0] != null ? row[0].toString() : "");
                    dia.put("pedidos", row[1] != null ? row[1] : 0);
                    dia.put("ventas", row[2] != null ? row[2] : 0.0);
                    tendencia.add(dia);
                }
            }
            
            return ResponseEntity.ok(tendencia);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // ======================== REPORTES DE PRODUCTOS ========================

    // 9. PRODUCTOS MEJOR VALORADOS
    @GetMapping("/productos-valorados/{vendedorId}")
    public ResponseEntity<List<Map<String, Object>>> getProductosValorados(@PathVariable Integer vendedorId) {
        try {
            List<Map<String, Object>> productos = valoracionRepo.obtenerProductosMejorValorados(vendedorId);
            return ResponseEntity.ok(productos != null ? productos : new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 10. STOCK BAJO
    @GetMapping("/stock-bajo/{vendedorId}")
    public ResponseEntity<List<Map<String, Object>>> getStockBajo(@PathVariable Integer vendedorId) {
        try {
            List<Map<String, Object>> productos = productoRepo.obtenerProductosStockBajo(vendedorId);
            return ResponseEntity.ok(productos != null ? productos : new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 11. PRODUCTOS POR CATEGORÍA
    @GetMapping("/productos-categoria/{vendedorId}")
    public ResponseEntity<List<Map<String, Object>>> getProductosPorCategoria(@PathVariable Integer vendedorId) {
        try {
            List<Map<String, Object>> categorias = productoRepo.obtenerProductosPorCategoria(vendedorId);
            return ResponseEntity.ok(categorias != null ? categorias : new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 12. PRODUCTOS SIN VENTAS
    @GetMapping("/productos-sin-ventas/{vendedorId}")
    public ResponseEntity<List<Map<String, Object>>> getProductosSinVentas(@PathVariable Integer vendedorId) {
        try {
            List<Map<String, Object>> productos = productoRepo.obtenerProductosSinVentas(vendedorId);
            return ResponseEntity.ok(productos != null ? productos : new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // ======================== REPORTES DE VALORACIONES ========================

    // 13. DISTRIBUCIÓN DE CALIFICACIONES
    @GetMapping("/distribucion-calificaciones/{vendedorId}")
    public ResponseEntity<List<Map<String, Object>>> getDistribucionCalificaciones(@PathVariable Integer vendedorId) {
        try {
            List<Object[]> resultados = valoracionRepo.obtenerDistribucionCalificaciones(vendedorId);
            List<Map<String, Object>> distribucion = new ArrayList<>();
            
            if (resultados != null) {
                for (Object[] row : resultados) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("estrellas", row[0] != null ? row[0] : 0);
                    item.put("cantidad", row[1] != null ? row[1] : 0);
                    distribucion.add(item);
                }
            }
            
            return ResponseEntity.ok(distribucion);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 14. RESEÑAS RECIENTES
    @GetMapping("/resenas-recientes/{vendedorId}")
    public ResponseEntity<List<Valoracion>> getResenasRecientes(@PathVariable Integer vendedorId) {
        try {
            List<Valoracion> resenas = valoracionRepo.findTop10ByVendedorOrderByFechaValoracionDesc(vendedorId);
            return ResponseEntity.ok(resenas != null ? resenas : new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // ======================== REPORTES POR FECHA ========================

    // 15. VENTAS POR FECHA ESPECÍFICA - CORREGIDO
    @GetMapping("/ventas-por-fecha/{vendedorId}")
    public ResponseEntity<List<Map<String, Object>>> getVentasPorFecha(
            @PathVariable Integer vendedorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            LocalDateTime inicio = fecha.atStartOfDay();
            LocalDateTime fin = fecha.plusDays(1).atStartOfDay();
            
            List<Map<String, Object>> ventas = pedidoRepo.findWithFilters(
                null, vendedorId, null, null, null, inicio, fin
            ).stream()
            .map(pedido -> {
                Map<String, Object> venta = new HashMap<>();
                venta.put("id", pedido.getIdPedido());
                
                // Obtener nombre completo del consumidor
                String nombreCompleto = pedido.getConsumidor().getUsuario().getNombre() + " " +
                                       pedido.getConsumidor().getUsuario().getApellido();
                venta.put("cliente", nombreCompleto);
                
                venta.put("total", pedido.getTotal());
                venta.put("estado", pedido.getEstadoPedido());
                venta.put("fecha", pedido.getFechaPedido());
                return venta;
            })
            .collect(Collectors.toList());
            
            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // 16. VENTAS POR RANGO DE FECHAS - CORREGIDO
    @GetMapping("/ventas-por-rango/{vendedorId}")
    public ResponseEntity<Map<String, Object>> getVentasPorRango(
            @PathVariable Integer vendedorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        try {
            LocalDateTime inicioDateTime = inicio.atStartOfDay();
            LocalDateTime finDateTime = fin.plusDays(1).atStartOfDay();
            
            List<Map<String, Object>> ventas = pedidoRepo.findWithFilters(
                null, vendedorId, null, null, null, inicioDateTime, finDateTime
            ).stream()
            .map(pedido -> {
                Map<String, Object> venta = new HashMap<>();
                venta.put("id", pedido.getIdPedido());
                
                // Obtener nombre completo del consumidor
                String nombreCompleto = pedido.getConsumidor().getUsuario().getNombre() + " " +
                                       pedido.getConsumidor().getUsuario().getApellido();
                venta.put("cliente", nombreCompleto);
                
                venta.put("total", pedido.getTotal());
                venta.put("estado", pedido.getEstadoPedido());
                venta.put("fecha", pedido.getFechaPedido());
                return venta;
            })
            .collect(Collectors.toList());
            
            double totalVentas = ventas.stream()
                .mapToDouble(v -> {
                    Object totalObj = v.get("total");
                    if (totalObj instanceof Number) {
                        return ((Number) totalObj).doubleValue();
                    }
                    return 0.0;
                })
                .sum();
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("ventas", ventas);
            resultado.put("totalVentas", totalVentas);
            resultado.put("totalPedidos", ventas.size());
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "ventas", new ArrayList<>(),
                "totalVentas", 0.0,
                "totalPedidos", 0
            ));
        }
    }

    // ======================== ENDPOINTS MIXTOS ========================

    // 17. RESUMEN COMPLETO DEL VENDEDOR
    @GetMapping("/resumen/{vendedorId}")
    public ResponseEntity<Map<String, Object>> getResumenCompleto(@PathVariable Integer vendedorId) {
        try {
            Map<String, Object> resumen = new HashMap<>();
            
            // Estadísticas de pedidos
            Map<String, Object> estadisticas = pedidoRepo.obtenerEstadisticasVendedor(vendedorId);
            resumen.put("estadisticas", estadisticas != null ? estadisticas : new HashMap<>());
            
            // Productos mejor valorados
            List<Map<String, Object>> productosValorados = valoracionRepo.obtenerProductosMejorValorados(vendedorId);
            resumen.put("productosValorados", productosValorados != null ? productosValorados : new ArrayList<>());
            
            // Stock bajo
            List<Map<String, Object>> stockBajo = productoRepo.obtenerProductosStockBajo(vendedorId);
            resumen.put("stockBajo", stockBajo != null ? stockBajo : new ArrayList<>());
            
            // Clientes recurrentes
            List<Map<String, Object>> clientes = pedidoRepo.obtenerClientesRecurrentesPorVendedor(vendedorId);
            resumen.put("clientesRecurrentes", clientes != null ? clientes : new ArrayList<>());
            
            // Distribución de calificaciones
            List<Object[]> distribucion = valoracionRepo.obtenerDistribucionCalificaciones(vendedorId);
            resumen.put("distribucionCalificaciones", convertirDistribucion(distribucion));
            
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 18. ESTADÍSTICAS GENERALES DEL SISTEMA (PARA ADMIN)
    @GetMapping("/estadisticas-generales")
    public ResponseEntity<Map<String, Object>> getEstadisticasGenerales() {
        try {
            Map<String, Object> estadisticas = new HashMap<>();
            
            // Ventas
            estadisticas.put("totalVentas", pedidoRepo.sumTotalVentas() != null ? pedidoRepo.sumTotalVentas() : 0.0);
            estadisticas.put("ventasMesActual", pedidoRepo.sumVentasMesActual() != null ? pedidoRepo.sumVentasMesActual() : 0.0);
            
            // Productos - usar el método que ya tienes funcionando
            // Si tienes countByEstado, úsalo, sino usa findByEstado
            try {
                List<com.mercadolocalia.entities.Producto> productosDisponibles = productoRepo.findByEstado("Disponible");
                estadisticas.put("productosDisponibles", productosDisponibles != null ? productosDisponibles.size() : 0);
            } catch (Exception e) {
                estadisticas.put("productosDisponibles", 0);
            }
            
            estadisticas.put("totalProductos", productoRepo.count());
            
            // Pedidos
            estadisticas.put("pedidosTotales", pedidoRepo.count());
            
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "totalVentas", 0.0,
                "ventasMesActual", 0.0,
                "totalProductos", 0,
                "productosDisponibles", 0,
                "pedidosTotales", 0
            ));
        }
    }

    // ======================== ENDPOINTS DE PRUEBA Y SALUD ========================

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        return ResponseEntity.ok(Map.of("status", "OK", "message", "ReportesController funcionando"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "ReportesController");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }

    // ======================== MÉTODOS AUXILIARES ========================

    private List<Map<String, Object>> convertirVentasMensuales(List<Object[]> resultados) {
        List<Map<String, Object>> ventas = new ArrayList<>();
        
        if (resultados != null) {
            for (Object[] row : resultados) {
                if (row[0] != null) {
                    Map<String, Object> venta = new HashMap<>();
                    venta.put("mes", convertirNumeroMes((Integer) row[0]));
                    venta.put("total", row[1] != null ? row[1] : 0.0);
                    ventas.add(venta);
                }
            }
        }
        
        return ventas;
    }

    private String convertirNumeroMes(int numeroMes) {
        String[] meses = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        return (numeroMes >= 1 && numeroMes <= 12) ? meses[numeroMes - 1] : "Mes " + numeroMes;
    }

    private List<Map<String, Object>> convertirDistribucion(List<Object[]> distribucion) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        if (distribucion != null) {
            for (Object[] row : distribucion) {
                if (row[0] != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("estrellas", row[0]);
                    item.put("cantidad", row[1] != null ? row[1] : 0);
                    resultado.add(item);
                }
            }
        }
        
        return resultado;
    }
}