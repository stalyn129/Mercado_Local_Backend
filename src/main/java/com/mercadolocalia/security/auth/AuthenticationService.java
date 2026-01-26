package com.mercadolocalia.security.auth;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

  // ================= CONSUMIDOR =================
     if (c != null && "CONSUMIDOR".equalsIgnoreCase(usuario.getRol().getNombreRol())) {
         response.setIdConsumidor(c.getIdConsumidor());
         response.setNombre(usuario.getNombre());      // 👈 CORRECTO
         response.setApellido(usuario.getApellido());  // 👈 CORRECTO
         response.setCorreo(usuario.getCorreo());      // 👈 CORRECTO
     }

  // ================= VENDEDOR =================
     if (v != null && "VENDEDOR".equalsIgnoreCase(usuario.getRol().getNombreRol())) {
         response.setIdVendedor(v.getIdVendedor());
         response.setNombre(
                 v.getNombreEmpresa() != null
                         ? v.getNombreEmpresa()
                         : usuario.getNombre()
         );
         response.setApellido(usuario.getApellido());
         response.setCorreo(usuario.getCorreo());
     }

     return response;
 }


}
