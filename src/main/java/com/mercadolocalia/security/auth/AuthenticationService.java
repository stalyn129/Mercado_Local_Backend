package com.mercadolocalia.security.auth;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    // ============================================================
    // 🔵 REGISTRO COMPLETO (Usuario + Consumidor/Vendedor)
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

        usuarioRepository.save(usuario);

        Integer idConsumidor = null;
        Integer idVendedor = null;

        // -------------------------------------------
        // SI ES CONSUMIDOR (idRol = 3)
        // -------------------------------------------
        if (rol.getNombreRol().equalsIgnoreCase("CONSUMIDOR")) {
            Consumidor consumidor = new Consumidor();
            consumidor.setUsuario(usuario);
            consumidor.setCedulaConsumidor(request.getCedula());
            consumidor.setDireccionConsumidor(request.getDireccion());
            consumidor.setTelefonoConsumidor(request.getTelefono());
            Consumidor guardado = consumidorRepository.save(consumidor);
            idConsumidor = guardado.getIdConsumidor();
        }

        // -------------------------------------------
        // SI ES VENDEDOR (idRol = 2)
        // -------------------------------------------
        if (rol.getNombreRol().equalsIgnoreCase("VENDEDOR")) {
            Vendedor vendedor = new Vendedor();
            vendedor.setUsuario(usuario);
            vendedor.setNombreEmpresa(request.getNombreEmpresa());
            vendedor.setRucEmpresa(request.getRuc());
            vendedor.setDireccionEmpresa(request.getDireccionEmpresa());
            vendedor.setTelefonoEmpresa(request.getTelefonoEmpresa());
            vendedor.setCalificacionPromedio(0.0);

            Vendedor guardado = vendedorRepository.save(vendedor);
            idVendedor = guardado.getIdVendedor();
        }

        // Generar token JWT
        String tokenJwt = jwtService.generarToken(usuario);

        // Guardar token en BD (como ya tienes)
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
        response.setRol(usuario.getRol().getNombreRol());
        response.setIdConsumidor(idConsumidor);
        response.setIdVendedor(idVendedor);

        return response;
    }

    // ============================================================
    // 🔵 LOGIN
    // ============================================================
    public AuthResponse login(LoginRequest request) {

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

        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String tokenJwt = jwtService.generarToken(usuario);

        Token token = new Token();
        token.setUsuario(usuario);
        token.setToken(tokenJwt);
        token.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        tokenRepository.save(token);

        // BUSCAR CONSUMIDOR O VENDEDOR RELACIONADO
        Consumidor c = consumidorRepository.findByUsuario(usuario);
        Integer idConsumidor = (c != null) ? c.getIdConsumidor() : null;

        Vendedor v = vendedorRepository.findByUsuario(usuario);
        Integer idVendedor = (v != null) ? v.getIdVendedor() : null;


        // Construir respuesta
        AuthResponse response = new AuthResponse();
        response.setToken(tokenJwt);
        response.setMensaje("✔ Login exitoso");
        response.setIdUsuario(usuario.getIdUsuario());
        response.setRol(usuario.getRol().getNombreRol());
        response.setIdConsumidor(idConsumidor);
        response.setIdVendedor(idVendedor);

        return response;
    }

}
