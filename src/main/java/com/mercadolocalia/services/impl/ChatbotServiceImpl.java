package com.mercadolocalia.services.impl;

import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.repositories.PedidoRepository;
import com.mercadolocalia.repositories.SubcategoriaRepository;
import com.mercadolocalia.services.ChatbotService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private SubcategoriaRepository subcategoriaRepository;

    @Override
    public String responder(
            String mensaje,
            String rol,
            Integer idConsumidor,
            Integer idVendedor
    ) {

        if (mensaje == null || mensaje.isBlank()) {
            return "🤖 ¿En qué puedo ayudarte?";
        }

        mensaje = mensaje.toLowerCase();

        // =========================
        // 📅 GLOBAL (TODOS)
        // =========================
        if (mensaje.contains("fecha") || mensaje.contains("hoy")) {
            return "📅 Hoy es " + LocalDate.now();
        }

        // =========================
        // 🧠 BÚSQUEDA DE PRODUCTOS
        // =========================
        if (contieneIntencionBusqueda(mensaje)) {

            String palabraClave = extraerPalabraClave(mensaje);

            // ❌ FUERA DEL DOMINIO
            if (!existeEnDominio(palabraClave)) {
                return "❌ Actualmente MercadoLocal no ofrece productos de \""
                        + palabraClave
                        + "\".\n🌱 Nos enfocamos en productos locales y artesanales.";
            }

            // ✅ DENTRO DEL DOMINIO
            List<Producto> productos =
                    productoRepository
                            .findBySubcategoria_NombreSubcategoriaContainingIgnoreCase(palabraClave);

            if (productos.isEmpty()) {
                return "🔍 No hay productos disponibles de \""
                        + palabraClave
                        + "\" en este momento.";
            }

            StringBuilder r = new StringBuilder("🛍️ Productos disponibles:\n");

            productos.stream().limit(5).forEach(p ->
                    r.append("• ")
                     .append(p.getNombreProducto())
                     .append(" - $")
                     .append(p.getPrecioProducto())
                     .append(" / ")
                     .append(p.getUnidad())
                     .append("\n")
            );

            return r.toString();
        }

        // =========================
        // 🛒 CONSUMIDOR
        // =========================
        if ("CONSUMIDOR".equalsIgnoreCase(rol)) {

            if (mensaje.contains("pedido") && idConsumidor != null) {
                int total =
                        pedidoRepository.countByConsumidor_IdConsumidor(idConsumidor);

                return "📦 Tienes " + total + " pedidos registrados.";
            }
        }

        // =========================
        // 🧑‍🌾 VENDEDOR
        // =========================
        if ("VENDEDOR".equalsIgnoreCase(rol)) {

            if (mensaje.contains("producto")) {
                return "📦 Gestiona tus productos desde el Panel de Vendedor.";
            }

            if (mensaje.contains("pedido")) {
                return "📦 Revisa los pedidos recibidos en tu panel.";
            }

            if (mensaje.contains("venta")) {
                return "📊 Consulta tus ventas en Análisis de Ventas.";
            }
        }

        // =========================
        // 🛡️ ADMIN
        // =========================
        if ("ADMIN".equalsIgnoreCase(rol)) {

            if (mensaje.contains("reporte")) {
                return "📊 Los reportes están en el módulo de administración.";
            }

            if (mensaje.contains("usuario")) {
                return "👥 Puedes gestionar usuarios desde Admin.";
            }

            if (mensaje.contains("stock")) {
                return "📦 El stock se revisa desde Reportes Admin.";
            }
        }

        // =========================
        // ❓ DEFAULT
        // =========================
        return "🤖 Puedo ayudarte con productos locales, pedidos, ventas o reportes.";
    }

    // =========================
    // 🧠 MÉTODOS AUXILIARES
    // =========================

    private boolean contieneIntencionBusqueda(String mensaje) {
        return mensaje.contains("quiero")
                || mensaje.contains("busco")
                || mensaje.contains("necesito")
                || mensaje.contains("productos");
    }

    private String extraerPalabraClave(String mensaje) {
        return mensaje
                .replace("quiero", "")
                .replace("busco", "")
                .replace("necesito", "")
                .replace("productos", "")
                .replace("de", "")
                .trim();
    }

    private boolean existeEnDominio(String palabraClave) {
        return subcategoriaRepository
                .existsByNombreSubcategoriaContainingIgnoreCase(palabraClave);
    }
}
