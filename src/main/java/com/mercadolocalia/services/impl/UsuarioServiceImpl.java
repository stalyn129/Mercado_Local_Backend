package com.mercadolocalia.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.ActualizarPerfilRequest;
import com.mercadolocalia.dto.UsuarioPerfilDTO;
import com.mercadolocalia.dto.UsuarioRequest;
import com.mercadolocalia.dto.UsuarioResponse;
import com.mercadolocalia.entities.Consumidor;
import com.mercadolocalia.entities.Rol;
import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.repositories.ConsumidorRepository;
import com.mercadolocalia.repositories.RolRepository;
import com.mercadolocalia.repositories.UsuarioRepository;
import com.mercadolocalia.repositories.VendedorRepository;
import com.mercadolocalia.services.UsuarioService;

import jakarta.transaction.Transactional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ConsumidorRepository consumidorRepository;

    @Autowired
    private VendedorRepository vendedorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ====================================================
    // OBTENER PERFIL (Usuario logueado)
    // ====================================================
    @Override
    public UsuarioPerfilDTO obtenerPerfilDTO(String correo) {

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Integer idConsumidor = null;
        String direccionConsumidor = null;
        String telefonoConsumidor = null;
        String cedulaConsumidor = null;

        Integer idVendedor = null;
        String nombreEmpresa = null;
        String direccionEmpresa = null;
        String telefonoEmpresa = null;
        String rucEmpresa = null;
        Double calificacionPromedio = null;

        String rol = usuario.getRol().getNombreRol();

        if ("CONSUMIDOR".equals(rol)) {
            Consumidor consumidor = consumidorRepository.findByUsuario(usuario);
            if (consumidor == null) {
                throw new RuntimeException("Consumidor no encontrado");
            }

            idConsumidor = consumidor.getIdConsumidor();
            direccionConsumidor = consumidor.getDireccionConsumidor();
            telefonoConsumidor = consumidor.getTelefonoConsumidor();
            cedulaConsumidor = consumidor.getCedulaConsumidor();
        }

        if ("VENDEDOR".equals(rol)) {
            Vendedor vendedor = vendedorRepository.findByUsuario(usuario)
                    .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

            idVendedor = vendedor.getIdVendedor();
            nombreEmpresa = vendedor.getNombreEmpresa();
            direccionEmpresa = vendedor.getDireccionEmpresa();
            telefonoEmpresa = vendedor.getTelefonoEmpresa();
            rucEmpresa = vendedor.getRucEmpresa();
            calificacionPromedio = vendedor.getCalificacionPromedio();
        }

        return new UsuarioPerfilDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getFechaNacimiento(),
                rol,
                usuario.getEsAdministrador(),
                usuario.getFechaRegistro(),
                usuario.getEstado(),

                idConsumidor,
                direccionConsumidor,
                telefonoConsumidor,
                cedulaConsumidor,

                idVendedor,
                nombreEmpresa,
                direccionEmpresa,
                telefonoEmpresa,
                rucEmpresa,
                calificacionPromedio
        );
    }

    // ====================================================
    // LISTAR USUARIOS (ADMIN)
    // ====================================================
    @Override
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(u -> new UsuarioResponse(
                        u.getIdUsuario(),
                        u.getNombre(),
                        u.getApellido(),
                        u.getCorreo(),
                        "********",
                        u.getFechaNacimiento(),
                        u.getRol().getNombreRol(),
                        u.getEsAdministrador(),
                        u.getFechaRegistro(),
                        u.getEstado()
                ))
                .toList();
    }

    // ====================================================
    // CAMBIAR ESTADO
    // ====================================================
    @Override
    public String cambiarEstado(Integer id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getEstado() == null || usuario.getEstado().equalsIgnoreCase("Suspendido")) {
            usuario.setEstado("Activo");
        } else {
            usuario.setEstado("Suspendido");
        }

        usuarioRepository.save(usuario);
        return usuario.getEstado();
    }

    // ====================================================
    // ACTUALIZAR USUARIO (ADMIN)
    // ====================================================
    @Override
    public UsuarioResponse actualizarUsuario(Integer id, UsuarioRequest req) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombre(req.nombre());
        usuario.setApellido(req.apellido());
        usuario.setCorreo(req.correo());
        usuario.setFechaNacimiento(req.fechaNacimiento());
        usuario.setEstado(req.estado());

        if (req.contrasena() != null && !req.contrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(req.contrasena()));
        }

        Rol rol = rolRepository.findByNombreRol(req.rol())
                .orElseThrow(() -> new RuntimeException("Rol no válido"));

        usuario.setRol(rol);

        usuarioRepository.save(usuario);

        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                "********",
                usuario.getFechaNacimiento(),
                usuario.getRol().getNombreRol(),
                usuario.getEsAdministrador(),
                usuario.getFechaRegistro(),
                usuario.getEstado()
        );
    }

    // ====================================================
    // ELIMINAR USUARIO
    // ====================================================
    @Override
    public void eliminarUsuario(Integer id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuarioRepository.delete(usuario);
    }

 // ====================================================
 // ACTUALIZAR PERFIL (Usuario logueado)
 // ====================================================
 @Override
 @Transactional
 public UsuarioPerfilDTO actualizarPerfil(String correo, ActualizarPerfilRequest request) {

     Usuario usuario = usuarioRepository.findByCorreo(correo)
             .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

     // -------- DATOS BASE USUARIO --------
     if (request.getNombre() != null) {
         usuario.setNombre(request.getNombre());
     }
     if (request.getApellido() != null) {
         usuario.setApellido(request.getApellido());
     }
     if (request.getFechaNacimiento() != null) {
         usuario.setFechaNacimiento(request.getFechaNacimiento());
     }

     usuarioRepository.save(usuario);

     String rol = usuario.getRol().getNombreRol();

     // ================= CONSUMIDOR =================
     if ("CONSUMIDOR".equalsIgnoreCase(rol)) {

         Consumidor consumidor = consumidorRepository.findByUsuario(usuario);

         if (consumidor == null) {
             throw new RuntimeException("Consumidor no encontrado");
         }

         if (request.getDireccionConsumidor() != null) {
             consumidor.setDireccionConsumidor(request.getDireccionConsumidor());
         }
         if (request.getTelefonoConsumidor() != null) {
             consumidor.setTelefonoConsumidor(request.getTelefonoConsumidor());
         }

         consumidorRepository.save(consumidor);
     }

     // ================= VENDEDOR =================
     if ("VENDEDOR".equalsIgnoreCase(rol)) {

         Vendedor vendedor = vendedorRepository.findByUsuario(usuario)
                 .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

         if (request.getDireccionEmpresa() != null) {
             vendedor.setDireccionEmpresa(request.getDireccionEmpresa());
         }
         if (request.getTelefonoEmpresa() != null) {
             vendedor.setTelefonoEmpresa(request.getTelefonoEmpresa());
         }

         vendedorRepository.save(vendedor);
     }

     return obtenerPerfilDTO(correo);
 }


}
