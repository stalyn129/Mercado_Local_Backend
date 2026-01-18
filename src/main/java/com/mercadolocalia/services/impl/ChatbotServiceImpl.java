package com.mercadolocalia.services.impl;

import com.mercadolocalia.entities.ChatHistorial;
import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.repositories.ChatHistorialRepository;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.repositories.PedidoRepository;
import com.mercadolocalia.services.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private ChatHistorialRepository chatRepo;

    private final String BASE_URL_FRONTEND = "http://localhost:5173/producto/";

    @Override
    @Transactional
    public String responder(String mensaje, String rol, Integer idConsumidor, Integer idVendedor) {
        if (mensaje == null || mensaje.isBlank()) return "🤖 ¿En qué puedo ayudarte?";

        String msg = mensaje.toLowerCase().trim();
        String respuesta = "";
        
        // 1. IDENTIFICACIÓN DE IDENTIDAD
        Integer idUsuarioActivo = (idConsumidor != null) ? idConsumidor : idVendedor;
        boolean esInvitado = (idUsuarioActivo == null);

        // 2. RESTRICCIONES DE ROL (SEGURIDAD DE INTENCIÓN)
        
        // Caso: Un VENDEDOR intentando comprar o buscar productos para comprar
        if ("VENDEDOR".equalsIgnoreCase(rol) && (msg.contains("comprar") || msg.contains("quiero") || msg.contains("busco"))) {
            return "👨‍🌾 Como vendedor, tu cuenta está configurada para gestionar productos y ventas. Si deseas comprar, por favor ingresa con una cuenta de Consumidor.";
        }

        // Caso: Un CONSUMIDOR intentando funciones de gestión (vender/subir stock)
        if ("CONSUMIDOR".equalsIgnoreCase(rol) && (msg.contains("vender") || msg.contains("subir producto") || msg.contains("mis ventas"))) {
            return "🛒 Esta cuenta es de Consumidor. Si eres productor y quieres vender, por favor regístrate como Vendedor.";
        }

        // 3. GENERACIÓN DE RESPUESTA BASADA EN ROL
        if (msg.matches("^(hola|buenas|buenos dias|saludos)$")) {
            respuesta = "👋 ¡Hola! Soy el asistente de MercadoLocal. ¿En qué puedo ayudarte hoy?";
        } 
        else if (msg.contains("recomienda") || msg.contains("sugieres") || msg.contains("qué hay")) {
            // Pasamos el rol para saber si mandamos el link o no
            respuesta = obtenerRecomendacionDiferenciada(rol);
        } 
        else if (contieneIntencionBusqueda(msg)) {
            // Solo permitimos búsqueda real si NO es vendedor (por la regla anterior)
            respuesta = ejecutarBusquedaInteligente(msg, rol);
        } 
        else if (msg.contains("pedido")) {
            respuesta = consultarPedidosPorRol(rol, idUsuarioActivo);
        } 
        else {
            respuesta = "🤖 No estoy seguro de entenderte. ¿Podrías ser más específico con tu consulta?";
        }

        // 4. PERSISTENCIA CONDICIONAL (Solo si NO es invitado)
        if (!esInvitado) {
            // IMPORTANTE: Ahora pasamos el rol al constructor
            chatRepo.save(new ChatHistorial(idUsuarioActivo, rol, "user", mensaje));
            chatRepo.save(new ChatHistorial(idUsuarioActivo, rol, "bot", respuesta));
        }

        return respuesta;
    }

    /**
     * Recomendación que quita el link si es Vendedor o Invitado
     */
    private String obtenerRecomendacionDiferenciada(String rol) {
        List<Producto> productos = productoRepository.findAll();
        if (productos.isEmpty()) return "🤖 Aún no hay productos disponibles.";
        
        Producto p = productos.get((int) (Math.random() * productos.size()));
        
        // Si es CONSUMIDOR, mandamos link completo
        if ("CONSUMIDOR".equalsIgnoreCase(rol)) {
            return "🌟 Te sugiero el *" + p.getNombreProducto() + "*.\n" +
                   "💰 Precio: $" + String.format("%.2f", p.getPrecioProducto()) + "\n" +
                   "🔗 [Ver producto](" + BASE_URL_FRONTEND + p.getIdProducto() + ")";
        }
        
        // Para VENDEDOR o INVITADO, solo damos la información sin link de compra
        return "🍎 Un producto muy popular ahora es: *" + p.getNombreProducto() + "*. Su precio de mercado actual es de $" + String.format("%.2f", p.getPrecioProducto()) + ".";
    }
    /**
     * Recupera el historial para el Frontend
     */
    public List<ChatHistorial> obtenerHistorial(Integer idUsuario, String rol) {
        if (idUsuario == null || rol == null) return List.of();
        return chatRepo.findByIdUsuarioAndRolOrderByFechaAsc(idUsuario, rol);
    }

    /**
     * Borra el historial (para el botón de la papelera)
     */
    @Transactional
    public void limpiarHistorial(Integer idUsuario) {
        if (idUsuario != null) {
            chatRepo.deleteByIdUsuarioAndRol(idUsuario, BASE_URL_FRONTEND);
        }
    }

    private String ejecutarBusquedaInteligente(String mensaje, String rol) {
        String palabraClave = extraerPalabraClave(mensaje);
        
        // Validación de longitud mínima con excepciones para productos de nombre corto
        if (palabraClave.length() < 3 && !palabraClave.matches("(ajo|uva|pan|sal|cal|té)")) {
             return "🤖 ¿Qué producto buscas? Prueba escribiendo el nombre completo (ej: 'tomates').";
        }

        List<Producto> catalogo = productoRepository.findAll();

        // Filtro por similitud en nombre y subcategoría
        List<Producto> resultados = catalogo.stream()
                .filter(p -> esSimilar(palabraClave, p.getNombreProducto()) || 
                             esSimilar(palabraClave, p.getSubcategoria().getNombreSubcategoria()))
                .limit(4)
                .collect(Collectors.toList());

        if (resultados.isEmpty()) {
            return "🔍 No encontré productos relacionados con \"" + palabraClave + "\".\n\n" +
                   "💡 Prueba con categorías como 'frutas', 'verduras' o 'lácteos'.";
        }

        StringBuilder r = new StringBuilder("🛍️ ¡Mira lo que encontré!\n\n");
        
        for (Producto p : resultados) {
            r.append("✅ *").append(p.getNombreProducto().toUpperCase()).append("*\n")
             .append("💰 Precio: $").append(String.format("%.2f", p.getPrecioProducto()))
             .append(" / ").append(p.getUnidad()).append("\n");

            // REGLA DE ROL: Solo el CONSUMIDOR recibe el link de compra/detalles
            if ("CONSUMIDOR".equalsIgnoreCase(rol)) {
                r.append("🔗 [Ver detalles del producto](").append(BASE_URL_FRONTEND).append(p.getIdProducto()).append(")\n");
            } 
        }

        return r.toString();
    }

    private String obtenerRecomendacion() {
        List<Producto> productos = productoRepository.findAll();
        if (productos.isEmpty()) return "🤖 Por ahora no tenemos productos, ¡vuelve pronto!";
        
        Producto p = productos.get((int) (Math.random() * productos.size()));
        return "🌟 Te sugiero probar el *" + p.getNombreProducto() + "*.\n" +
               "💰 Solo $" + String.format("%.2f", p.getPrecioProducto()) + "\n" +
               "🔗 [Ver producto](" + BASE_URL_FRONTEND + p.getIdProducto() + ")";
    }

    private String consultarPedidosPorRol(String rol, Integer id) {
        if ("CONSUMIDOR".equalsIgnoreCase(rol) && id != null) {
            int total = pedidoRepository.countByConsumidor_IdConsumidor(id);
            return "📦 Tienes *" + total + "* pedidos registrados. Revísalos en tu perfil.";
        }
        return "🧑‍🌾 Revisa tus pedidos o ventas en tu panel de control.";
    }

    private boolean esSimilar(String usuario, String baseDeDatos) {
        if (usuario.isBlank() || baseDeDatos == null) return false;
        String u = usuario.toLowerCase();
        String db = baseDeDatos.toLowerCase();

        if (u.length() <= 4) return db.contains(u);

        String uNorm = (u.endsWith("s")) ? u.substring(0, u.length() - 1) : u;
        String dbNorm = (db.endsWith("s")) ? db.substring(0, db.length() - 1) : db;

        if (dbNorm.contains(uNorm)) return true;

        int maxDistancia = (uNorm.length() > 6) ? 2 : 1;
        return calcularDistancia(uNorm, dbNorm) <= maxDistancia; 
    }

    private int calcularDistancia(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];
        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) dp[i][j] = j;
                else if (j == 0) dp[i][j] = i;
                else {
                    int costo = (x.charAt(i - 1) == y.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), 
                               dp[i - 1][j - 1] + costo);
                }
            }
        }
        return dp[x.length()][y.length()];
    }

    private boolean contieneIntencionBusqueda(String mensaje) {
        return mensaje.matches(".*(quiero|busco|necesito|productos|hay|vendes|vende|tienen|dame|comprar).*") || 
               mensaje.split(" ").length <= 2;
    }

    private String extraerPalabraClave(String mensaje) {
        return mensaje.replaceAll("(?i)^(hola|buenos dias|tardes|noches|quiero|busco|necesito|productos|vendes|vende|hay|dame|comprar|tienen|de|un|una|los|las|el|la)\\s*", "").trim();
    }
}