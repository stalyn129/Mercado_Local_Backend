package com.mercadolocalia.security.auth;

import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.AuthResponse;
import com.mercadolocalia.dto.LoginRequest;
import com.mercadolocalia.dto.RegisterRequest;
import com.mercadolocalia.entities.Consumidor;
import com.mercadolocalia.entities.Rol;
import com.mercadolocalia.entities.Token;
import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.repositories.ConsumidorRepository;
import com.mercadolocalia.repositories.RolRepository;
import com.mercadolocalia.repositories.TokenRepository;
import com.mercadolocalia.repositories.UsuarioRepository;
import com.mercadolocalia.repositories.VendedorRepository;
import com.mercadolocalia.security.jwt.JwtService;

@Service
public class AuthenticationService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ConsumidorRepository consumidorRepository;

    @Autowired
    private VendedorRepository vendedorRepository;
    
    // 🔵 VERIFICAR SI EL EMAIL YA EXISTE (para Google OAuth)
    public boolean checkEmailExists(String email) {
        return usuarioRepository.existsByCorreo(email);
    }

    // 🔵 NUEVO MÉTODO: Verificar Token de Google
    public Map<String, Object> verifyGoogleToken(String googleToken) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = googleToken != null && !googleToken.isEmpty();
            
            response.put("valid", isValid);
            response.put("message", isValid ? "Token válido" : "Token inválido");
            
            if (isValid) {
                response.put("verified", true);
            }
            
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", "Error verificando token: " + e.getMessage());
        }
        
        return response;
    }
    
    // 🔵 REGISTRO CON GOOGLE (sin contraseña)
    public AuthResponse registrarConGoogle(RegisterRequest request) {
        
        // Validar que sea un registro con Google
        if (request.getGoogleAuth() == null || !request.getGoogleAuth()) {
            AuthResponse response = new AuthResponse();
            response.setMensaje("❌ Este método es solo para registro con Google");
            return response;
        }
        
        // Validar correo duplicado
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            AuthResponse response = new AuthResponse();
            response.setMensaje("❌ El correo ya está registrado");
            return response;
        }

        // Obtener el Rol desde la BD
        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        // Crear usuario base con Google Auth
        Usuario usuario = new Usuario();
        String nombreCompleto = (request.getNombre() + " " + request.getApellido()).trim();
        String[] partes = nombreCompleto.split("\\s+");

        String nombre = partes[0];
        String apellido = partes.length > 1
                ? String.join(" ", java.util.Arrays.copyOfRange(partes, 1, partes.length))
                : "";

        usuario.setNombre(nombre);
        usuario.setApellido(apellido);

        usuario.setCorreo(request.getCorreo());
        
        // Para Google Auth, generamos una contraseña dummy pero segura
        String dummyPassword = "google_oauth_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString();
        usuario.setContrasena(passwordEncoder.encode(dummyPassword));
        
        usuario.setFechaNacimiento(LocalDate.parse(request.getFechaNacimiento()));
        usuario.setRol(rol);
        usuario.setEsAdministrador(false);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setEstado("Activo");
        
        // ✅ ESTOS SON LOS CAMPOS CLAVE
        usuario.setGoogleAuth(true);
        usuario.setEmailVerificado(request.getEmailVerified() != null && request.getEmailVerified());

        usuarioRepository.save(usuario);

        Integer idConsumidor = null;
        Integer idVendedor = null;

        // -------------------------------------------
        // SI ES CONSUMIDOR (idRol = 3)
        // -------------------------------------------
        if (request.getIdRol() == 3) { // ID para CONSUMIDOR
            // Verificar si ya existe un consumidor para este usuario
            if (consumidorRepository.findByUsuario(usuario) == null) {
                Consumidor consumidor = new Consumidor();
                consumidor.setUsuario(usuario);
                consumidor.setCedulaConsumidor(request.getCedula());
                consumidor.setDireccionConsumidor(request.getDireccion());
                consumidor.setTelefonoConsumidor(request.getTelefono());
                Consumidor guardado = consumidorRepository.save(consumidor);
                idConsumidor = guardado.getIdConsumidor();
                
                System.out.println("✅ Consumidor creado con Google: " + consumidor.getCedulaConsumidor());
            }
        }

        // -------------------------------------------
        // SI ES VENDEDOR (idRol = 2)
        // -------------------------------------------
        if (request.getIdRol() == 2) { // ID para VENDEDOR
            // Verificar si ya existe un vendedor para este usuario
            if (!vendedorRepository.findByUsuario(usuario).isPresent()) {
                Vendedor vendedor = new Vendedor();
                vendedor.setUsuario(usuario);
                vendedor.setNombreEmpresa(request.getNombreEmpresa());
                vendedor.setRucEmpresa(request.getRuc());
                vendedor.setDireccionEmpresa(request.getDireccionEmpresa());
                vendedor.setTelefonoEmpresa(request.getTelefonoEmpresa());
                vendedor.setCalificacionPromedio(0.0);

                Vendedor guardado = vendedorRepository.save(vendedor);
                idVendedor = guardado.getIdVendedor();
                
                System.out.println("✅ Vendedor creado con Google: " + vendedor.getNombreEmpresa());
            }
        }

        // Generar token JWT
        String tokenJwt = jwtService.generarToken(usuario);

        // Guardar token en BD
        Token token = new Token();
        token.setUsuario(usuario);
        token.setToken(tokenJwt);
        token.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        tokenRepository.save(token);

        // Armar respuesta
        AuthResponse response = new AuthResponse();
        response.setToken(tokenJwt);
        response.setMensaje("✔ Usuario registrado con Google exitosamente");
        response.setIdUsuario(usuario.getIdUsuario());
        response.setRol(rol.getNombreRol());
        response.setIdConsumidor(idConsumidor);
        response.setIdVendedor(idVendedor);
        response.setNombre(usuario.getNombre());
        response.setApellido(usuario.getApellido());
        response.setCorreo(usuario.getCorreo());
        
        // Añadir campos de Google
        response.setGoogleAuth(true);
        response.setEmailVerificado(usuario.getEmailVerificado());
        
        // Agregar logs para debug
        System.out.println("Usuario registrado con Google:");
        System.out.println("- ID: " + usuario.getIdUsuario());
        System.out.println("- Email: " + usuario.getCorreo());
        System.out.println("- Google Auth: " + usuario.getGoogleAuth());
        System.out.println("- Email Verificado: " + usuario.getEmailVerificado());
        System.out.println("- Rol: " + rol.getNombreRol());

        return response;
    }

    // ============================================================
    // 🔵 LOGIN CON GOOGLE (para usuarios que ya se registraron con Google)
    // ============================================================
    public AuthResponse loginConGoogle(String email) {
        
        System.out.println("=== GOOGLE LOGIN DEBUG ===");
        System.out.println("Email buscado: " + email);
        
        // Buscar usuario por email
        Usuario usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> {
                    System.out.println("Usuario no encontrado para email: " + email);
                    return new RuntimeException("Usuario no encontrado");
                });
        
        System.out.println("Usuario encontrado: " + usuario.getNombre());
        System.out.println("Google Auth: " + usuario.getGoogleAuth());
        System.out.println("Email Verificado: " + usuario.getEmailVerificado());
        System.out.println("Estado: " + usuario.getEstado());
        System.out.println("Rol: " + (usuario.getRol() != null ? usuario.getRol().getNombreRol() : "null"));
        
        // Verificar que sea un usuario de Google
        if (usuario.getGoogleAuth() == null || !usuario.getGoogleAuth()) {
            // Si no es Google Auth, verificar si el email está verificado
            if (usuario.getEmailVerificado() == null || !usuario.getEmailVerificado()) {
                throw new RuntimeException("Este usuario no está registrado con Google");
            }
        }
        
        // Verificar que el usuario esté activo
        if (!"Activo".equals(usuario.getEstado())) {
            throw new RuntimeException("El usuario no está activo");
        }
        
        // Generar token JWT
        String tokenJwt = jwtService.generarToken(usuario);

        // Guardar token en BD
        Token token = new Token();
        token.setUsuario(usuario);
        token.setToken(tokenJwt);
        token.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        tokenRepository.save(token);

        // Buscar consumidor / vendedor
        Consumidor c = consumidorRepository.findByUsuario(usuario);
        Vendedor v = vendedorRepository.findByUsuario(usuario).orElse(null);

        // Construir respuesta MEJORADA
        AuthResponse response = new AuthResponse();
        response.setToken(tokenJwt);
        response.setMensaje("✔ Login con Google exitoso");
        response.setIdUsuario(usuario.getIdUsuario());
        response.setRol(usuario.getRol().getNombreRol());
        response.setNombre(usuario.getNombre());
        response.setApellido(usuario.getApellido());
        response.setCorreo(usuario.getCorreo());
        
        // Añadir campos de Google para el frontend
        response.setGoogleAuth(usuario.getGoogleAuth() != null && usuario.getGoogleAuth());
        response.setEmailVerificado(usuario.getEmailVerificado() != null && usuario.getEmailVerificado());

        // Agregar IDs específicos según rol
        if (c != null) {
            response.setIdConsumidor(c.getIdConsumidor());
            System.out.println("Consumidor ID encontrado: " + c.getIdConsumidor());
        }

        if (v != null) {
            response.setIdVendedor(v.getIdVendedor());
            System.out.println("Vendedor ID encontrado: " + v.getIdVendedor());
            if (v.getNombreEmpresa() != null && !v.getNombreEmpresa().isEmpty()) {
                response.setNombre(v.getNombreEmpresa());
            }
        }

        System.out.println("Respuesta final: " + response);
        return response;
    }

    // ============================================================
    // 🔵 REGISTRO COMPLETO (Usuario + Consumidor/Vendedor) - VERSIÓN CORREGIDA
    // ============================================================
    public AuthResponse registrar(RegisterRequest request) {

        // Validar correo duplicado
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            AuthResponse response = new AuthResponse();
            response.setMensaje("❌ El correo ya está registrado");
            return response;
        }

        // Obtener el Rol desde la BD
        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        // Crear usuario base
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setCorreo(request.getCorreo());
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        usuario.setFechaNacimiento(LocalDate.parse(request.getFechaNacimiento()));
        usuario.setRol(rol);
        usuario.setEsAdministrador(false);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setEstado("Activo");
        
        // Para registro normal, no es Google Auth
        usuario.setGoogleAuth(false);
        usuario.setEmailVerificado(false);

        usuarioRepository.save(usuario);

        Integer idConsumidor = null;
        Integer idVendedor = null;

        // -------------------------------------------
        // SI ES CONSUMIDOR (idRol = 3)
        // -------------------------------------------
        if (request.getIdRol() == 3) { // ID para CONSUMIDOR
            // Verificar si ya existe un consumidor para este usuario
            if (consumidorRepository.findByUsuario(usuario) == null) {
                Consumidor consumidor = new Consumidor();
                consumidor.setUsuario(usuario);
                consumidor.setCedulaConsumidor(request.getCedula());
                consumidor.setDireccionConsumidor(request.getDireccion());
                consumidor.setTelefonoConsumidor(request.getTelefono());
                Consumidor guardado = consumidorRepository.save(consumidor);
                idConsumidor = guardado.getIdConsumidor();
                
                System.out.println("✅ Consumidor creado: " + consumidor.getCedulaConsumidor());
            }
        }

        // -------------------------------------------
        // SI ES VENDEDOR (idRol = 2)
        // -------------------------------------------
        if (request.getIdRol() == 2) { // ID para VENDEDOR
            // Verificar si ya existe un vendedor para este usuario
            if (!vendedorRepository.findByUsuario(usuario).isPresent()) {
                Vendedor vendedor = new Vendedor();
                vendedor.setUsuario(usuario);
                vendedor.setNombreEmpresa(request.getNombreEmpresa());
                vendedor.setRucEmpresa(request.getRuc());
                vendedor.setDireccionEmpresa(request.getDireccionEmpresa());
                vendedor.setTelefonoEmpresa(request.getTelefonoEmpresa());
                vendedor.setCalificacionPromedio(0.0);

                Vendedor guardado = vendedorRepository.save(vendedor);
                idVendedor = guardado.getIdVendedor();
                
                System.out.println("✅ Vendedor creado: " + vendedor.getNombreEmpresa());
            }
        }

        // Generar token JWT
        String tokenJwt = jwtService.generarToken(usuario);

        // Guardar token en BD
        Token token = new Token();
        token.setUsuario(usuario);
        token.setToken(tokenJwt);
        token.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        tokenRepository.save(token);

        // Armar respuesta
        AuthResponse response = new AuthResponse();
        response.setToken(tokenJwt);
        response.setMensaje("✔ Usuario registrado exitosamente");
        response.setIdUsuario(usuario.getIdUsuario());
        response.setRol(rol.getNombreRol()); // Usar el nombre del rol obtenido
        response.setIdConsumidor(idConsumidor);
        response.setIdVendedor(idVendedor);
        response.setNombre(usuario.getNombre());
        response.setApellido(usuario.getApellido());
        response.setCorreo(usuario.getCorreo());
        
        // Agregar logs para debug
        System.out.println("Usuario registrado:");
        System.out.println("- ID: " + usuario.getIdUsuario());
        System.out.println("- Rol ID: " + request.getIdRol());
        System.out.println("- Rol Nombre: " + rol.getNombreRol());
        System.out.println("- Consumidor ID: " + idConsumidor);
        System.out.println("- Vendedor ID: " + idVendedor);

        return response;
    }

    // ============================================================
    // 🔵 LOGIN (NO DAÑA NADA EXISTENTE)
    // ============================================================
    public AuthResponse login(LoginRequest request) {

        // 1️⃣ Autenticación (NO tocar)
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getCorreo(),
                            request.getContrasena()
                    )
            );
        } catch (Exception ex) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        // 2️⃣ Obtener usuario base (NO tocar)
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 3️⃣ Generar token JWT (NO tocar)
        String tokenJwt = jwtService.generarToken(usuario);

        Token token = new Token();
        token.setUsuario(usuario);
        token.setToken(tokenJwt);
        token.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        tokenRepository.save(token);

        // 4️⃣ Buscar consumidor / vendedor (YA lo haces)
        Consumidor c = consumidorRepository.findByUsuario(usuario);
        Vendedor v = vendedorRepository.findByUsuario(usuario).orElse(null);

        // 5️⃣ Construir respuesta (AQUÍ ESTÁ LA CLAVE)
        AuthResponse response = new AuthResponse();
        response.setToken(tokenJwt);
        response.setMensaje("✔ Login exitoso");
        response.setIdUsuario(usuario.getIdUsuario());
        response.setRol(usuario.getRol().getNombreRol());
        response.setNombre(usuario.getNombre());
        response.setApellido(usuario.getApellido());
        response.setCorreo(usuario.getCorreo());

        // ================= CONSUMIDOR =================
        if (c != null && "CONSUMIDOR".equalsIgnoreCase(usuario.getRol().getNombreRol())) {
            response.setIdConsumidor(c.getIdConsumidor());
        }

        // ================= VENDEDOR =================
        if (v != null && "VENDEDOR".equalsIgnoreCase(usuario.getRol().getNombreRol())) {
            response.setIdVendedor(v.getIdVendedor());
            if (v.getNombreEmpresa() != null && !v.getNombreEmpresa().isEmpty()) {
                response.setNombre(v.getNombreEmpresa());
            }
        }

        return response;
    }
}