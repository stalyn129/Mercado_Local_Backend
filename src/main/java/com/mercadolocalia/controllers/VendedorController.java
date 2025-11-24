package com.mercadolocalia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mercadolocalia.dto.VendedorRequest;
import com.mercadolocalia.entities.Vendedor;
import com.mercadolocalia.services.VendedorService;

@RestController
@RequestMapping("/vendedor")
@CrossOrigin(origins = "*")
public class VendedorController {

    @Autowired
    private VendedorService vendedorService;

    @PostMapping("/registrar")
    public Vendedor registrar(@RequestBody VendedorRequest request) {
        return vendedorService.registrarVendedor(request);
    }

    @GetMapping("/usuario/{idUsuario}")
    public Vendedor obtenerPorUsuario(@PathVariable Integer idUsuario) {
        return vendedorService.obtenerVendedorPorUsuario(idUsuario);
    }

    @GetMapping("/{id}")
    public Vendedor obtenerPorId(@PathVariable Integer id) {
        return vendedorService.obtenerVendedorPorId(id);
    }
}
