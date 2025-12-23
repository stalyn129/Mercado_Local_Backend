package com.mercadolocalia.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.mercadolocalia.dto.ProductoPublicDTO;
import com.mercadolocalia.dto.VendedorPublicDTO;
import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.entities.Usuario;
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.repositories.VendedorRepository;

@RestController
@RequestMapping("/api/public/vendedores")
public class VendedorPublicController {

    private final VendedorRepository vendedorRepository;
    private final ProductoRepository productoRepository;

    public VendedorPublicController(
            VendedorRepository vendedorRepository,
            ProductoRepository productoRepository
    ) {
        this.vendedorRepository = vendedorRepository;
        this.productoRepository = productoRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendedorPublicDTO> obtenerVendedorPublico(
            @PathVariable Integer id
    ) {
        Vendedor vendedor = vendedorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vendedor no encontrado"
                ));

        List<ProductoPublicDTO> productos =
                productoRepository.obtenerProductosPublicosPorVendedor(id);

        Double calificacionPromedio = productos.isEmpty()
                ? null
                : productos.stream()
                    .mapToDouble(ProductoPublicDTO::getPromedioValoracion)
                    .average()
                    .orElse(0);

        // 👇 USUARIO PROPIETARIO
        Usuario usuario = vendedor.getUsuario();

        VendedorPublicDTO response = new VendedorPublicDTO(
                vendedor.getIdVendedor(),
                vendedor.getNombreEmpresa(),
                usuario.getNombre(),        
                usuario.getApellido(),      
                vendedor.getDireccionEmpresa(),
                vendedor.getTelefonoEmpresa(),
                calificacionPromedio,
                productos
        );

        return ResponseEntity.ok(response);
    }

}
