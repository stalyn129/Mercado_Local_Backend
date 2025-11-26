package com.mercadolocalia.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.ValoracionRequest;
import com.mercadolocalia.dto.ValoracionResponse;
import com.mercadolocalia.entities.Consumidor;
import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.entities.Valoracion;
import com.mercadolocalia.repositories.ConsumidorRepository;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.repositories.ValoracionRepository;
import com.mercadolocalia.services.ValoracionService;

@Service
public class ValoracionServiceImpl implements ValoracionService {

    @Autowired
    private ValoracionRepository valoracionRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ConsumidorRepository consumidorRepository;

    @Override
    public ValoracionResponse crearValoracion(ValoracionRequest request) {

        Producto producto = productoRepository.findById(request.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Consumidor consumidor = consumidorRepository.findById(request.getIdConsumidor())
                .orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));

        // Evitar que el mismo consumidor valore dos veces el mismo producto
        if (valoracionRepository.existsByProductoAndConsumidor(producto, consumidor)) {
            throw new RuntimeException("El consumidor ya valoró este producto");
        }

        Valoracion val = new Valoracion();
        val.setProducto(producto);
        val.setConsumidor(consumidor);
        val.setCalificacion(request.getCalificacion());
        val.setComentario(request.getComentario());
        val.setFechaValoracion(LocalDateTime.now());

        valoracionRepository.save(val);

        return convertir(val);
    }

    @Override
    public List<ValoracionResponse> listarPorProducto(Integer idProducto) {

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        return valoracionRepository.findByProducto(producto)
                .stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    @Override
    public List<ValoracionResponse> listarPorConsumidor(Integer idConsumidor) {

        Consumidor consumidor = consumidorRepository.findById(idConsumidor)
                .orElseThrow(() -> new RuntimeException("Consumidor no encontrado"));

        return valoracionRepository.findByConsumidor(consumidor)
                .stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    private ValoracionResponse convertir(Valoracion val) {
        ValoracionResponse res = new ValoracionResponse();

        res.setIdValoracion(val.getIdValoracion());
        res.setCalificacion(val.getCalificacion());
        res.setComentario(val.getComentario());
        res.setFechaValoracion(val.getFechaValoracion());

        // Producto
        res.setIdProducto(val.getProducto().getIdProducto());
        res.setNombreProducto(val.getProducto().getNombreProducto());

        // Consumidor
        res.setIdConsumidor(val.getConsumidor().getIdConsumidor());
        res.setNombreConsumidor(
                val.getConsumidor().getUsuario().getNombre() + " " +
                val.getConsumidor().getUsuario().getApellido()
        );

        return res;
    }
}
