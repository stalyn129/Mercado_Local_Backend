package com.mercadolocalia.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.mercadolocalia.repositories.UsuarioRepository;
import com.mercadolocalia.repositories.VendedorRepository;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.repositories.PedidoRepository;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private VendedorRepository vendedorRepo;
    @Autowired private ProductoRepository productoRepo;
    @Autowired private PedidoRepository pedidoRepo;

    // 📌 Estadísticas
    @GetMapping("/stats")
    public Map<String, Object> stats() {

        long totalUsuarios     = usuarioRepo.count();
        long totalVendedores   = vendedorRepo.count();
        long totalProductos    = productoRepo.count();
        long totalPedidos      = pedidoRepo.count();

        // 🔥 Ventas reales por monto total
        Double montoTotalVentas = pedidoRepo.sumTotalVentas();  // te explico cómo crearlo abajo

        // 🔥 Ventas del mes actual
        Double ventasMes = pedidoRepo.sumVentasMesActual();

        Map<String, Object> data = new HashMap<>();
        data.put("totalUsuarios", totalUsuarios);
        data.put("totalVendedores", totalVendedores);
        data.put("totalProductos", totalProductos);
        data.put("totalVentas", montoTotalVentas);
        data.put("ventasMes", ventasMes);
        data.put("crecimiento", (ventasMes / (montoTotalVentas == 0 ? 1 : montoTotalVentas)) * 100);

        return data;
    }


    @GetMapping("/activities")
    public List<Map<String, Object>> activities() {

        List<Map<String, Object>> data = new ArrayList<>();

        // 🟢 Últimos usuarios registrados
        usuarioRepo.findTop5ByOrderByFechaRegistroDesc().forEach(u -> {
            data.add(Map.of(
                    "tipo","usuario",
                    "nombre", u.getNombre()+" "+u.getApellido(),
                    "fecha", u.getFechaRegistro()
            ));
        });

        // 🟢 Últimos vendedores registrados
        vendedorRepo.findTop5ByOrderByIdVendedorDesc().forEach(v -> {
            data.add(Map.of(
                    "tipo","vendedor",
                    "empresa", v.getNombreEmpresa(),
                    "fecha", v.getUsuario().getFechaRegistro()
            ));
        });

        // 🟢 Últimos pedidos realizados
        pedidoRepo.findTop5ByOrderByIdPedidoDesc().forEach(p -> {
            data.add(Map.of(
                    "tipo","pedido",
                    "monto", p.getTotal(),
                    "estado", p.getEstadoPedido(),
                    "fecha", p.getFechaPedido()
            ));
        });

        return data;
    }

}