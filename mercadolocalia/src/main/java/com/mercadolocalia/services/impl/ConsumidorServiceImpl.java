package com.mercadolocalia.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.ConsumidorRequest;
import com.mercadolocalia.entities.Consumidor;
import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.repositories.ConsumidorRepository;
import com.mercadolocalia.repositories.UsuarioRepository;
import com.mercadolocalia.services.ConsumidorService;

@Service
public class ConsumidorServiceImpl implements ConsumidorService {

    @Autowired
    private ConsumidorRepository consumidorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public Consumidor registrarConsumidor(ConsumidorRequest request) {

        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (consumidorRepository.existsByUsuario(usuario)) {
            throw new RuntimeException("El usuario ya tiene un perfil de consumidor");
        }

        Consumidor consumidor = new Consumidor();
        consumidor.setUsuario(usuario);
        consumidor.setCedulaConsumidor(request.getCedulaConsumidor());
        consumidor.setDireccionConsumidor(request.getDireccionConsumidor());
        consumidor.setTelefonoConsumidor(request.getTelefonoConsumidor());

        return consumidorRepository.save(consumidor);
    }

    @Override
    public Consumidor obtenerConsumidorPorUsuario(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return consumidorRepository.findByUsuario(usuario);
    }

    @Override
    public Consumidor obtenerConsumidorPorId(Integer idConsumidor) {
        return consumidorRepository.findById(idConsumidor)
                .orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));
    }
}
