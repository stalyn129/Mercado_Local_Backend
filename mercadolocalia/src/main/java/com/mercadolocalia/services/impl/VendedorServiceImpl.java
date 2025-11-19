package com.mercadolocalia.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.VendedorRequest;
import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.repositories.UsuarioRepository;
import com.mercadolocalia.repositories.VendedorRepository;
import com.mercadolocalia.services.VendedorService;

@Service
public class VendedorServiceImpl implements VendedorService {

    @Autowired
    private VendedorRepository vendedorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

    @Override
    public Vendedor obtenerVendedorPorUsuario(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return vendedorRepository.findByUsuario(usuario);
    }

    @Override
    public Vendedor obtenerVendedorPorId(Integer idVendedor) {
        return vendedorRepository.findById(idVendedor)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
    }
}
