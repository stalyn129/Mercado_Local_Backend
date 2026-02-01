package com.mercadolocalia.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mercadolocalia.entities.DetalleFactura;
import com.mercadolocalia.entities.Factura;

import java.util.List;

public interface DetalleFacturaRepository extends JpaRepository<DetalleFactura, Integer> {
    
    List<DetalleFactura> findByFactura(Factura factura);
    
    // Si necesitas otras consultas
    List<DetalleFactura> findByFacturaIdFactura(Integer idFactura);
}