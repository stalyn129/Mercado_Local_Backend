package com.mercadolocalia.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.EstadisticasDTO;
import com.mercadolocalia.dto.PedidoDTO;
import com.mercadolocalia.dto.VendedorRequest;
import com.mercadolocalia.entities.Pedido;
import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.repositories.PedidoRepository;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.repositories.UsuarioRepository;
import com.mercadolocalia.repositories.VendedorRepository;
import com.mercadolocalia.services.VendedorService;

@Service
public class VendedorServiceImpl implements VendedorService {

    @Autowired
    private VendedorRepository vendedorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // ============================================================
    // REGISTRAR VENDEDOR
    // ============================================================
    @Override
    public Vendedor registrarVendedor(VendedorRequest request) {

        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (vendedorRepository.existsByUsuario(usuario)) {
            throw new RuntimeException("El usuario ya tiene un perfil de vendedor");
        }

        Vendedor vendedor = new Vendedor();
        vendedor.setUsuario(usuario);
        vendedor.setNombreEmpresa(request.getNombreEmpresa());
        vendedor.setRucEmpresa(request.getRucEmpresa());
        vendedor.setDireccionEmpresa(request.getDireccionEmpresa());
        vendedor.setTelefonoEmpresa(request.getTelefonoEmpresa());
        vendedor.setCalificacionPromedio(0.0);

        return vendedorRepository.save(vendedor);
    }

    // ============================================================
    // OBTENER POR USUARIO
    // ============================================================
    @Override
    public Vendedor obtenerVendedorPorUsuario(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return vendedorRepository.findByUsuario(usuario);
    }

    // ============================================================
    // OBTENER POR ID
    // ============================================================
    @Override
    public Vendedor obtenerVendedorPorId(Integer idVendedor) {
        return vendedorRepository.findById(idVendedor)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
    }

    // ============================================================
    // ESTADÍSTICAS DEL DASHBOARD
    // ============================================================
    @Override
    public EstadisticasDTO obtenerEstadisticas(Integer vendedorId) {

        Vendedor vendedor = vendedorRepository.findById(vendedorId)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        // INGRESOS
        Double ingresos = pedidoRepository.sumarIngresosPorVendedor(vendedorId);
        if (ingresos == null) ingresos = 0.0;

        // TOTAL PEDIDOS
        Integer totalPedidos = pedidoRepository.countByVendedor(vendedor);

        // TOTAL PRODUCTOS
        Integer productos = productoRepository.countByVendedor(vendedor);

        EstadisticasDTO dto = new EstadisticasDTO();
        dto.setIngresosTotales(ingresos);
        dto.setTotalPedidos(totalPedidos);
        dto.setProductosDisponibles(productos);

        return dto;
    }

    // ============================================================
    // PEDIDOS RECIENTES
    // ============================================================
    @Override
    public List<PedidoDTO> obtenerPedidosRecientes(Integer vendedorId) {

        Vendedor vendedor = vendedorRepository.findById(vendedorId)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        List<Pedido> pedidos = pedidoRepository.findTop10ByVendedorOrderByFechaPedidoDesc(vendedor);

        return pedidos.stream()
                .map(p -> {
                    PedidoDTO dto = new PedidoDTO();

                    dto.setId(p.getIdPedido());
                    dto.setNumero(String.valueOf(p.getIdPedido()));
                    dto.setEstado(p.getEstadoPedido());
                    dto.setTotal(p.getTotal());
                    dto.setFecha(p.getFechaPedido());

                    // Cliente REAL
                    if (p.getConsumidor() != null && p.getConsumidor().getUsuario() != null) {
                        dto.setClienteNombre(
                                p.getConsumidor().getUsuario().getNombre() + " " +
                                p.getConsumidor().getUsuario().getApellido()
                        );
                    } else {
                        dto.setClienteNombre("Cliente");
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
}
