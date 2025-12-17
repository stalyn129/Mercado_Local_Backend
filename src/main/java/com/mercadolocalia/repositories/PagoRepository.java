package com.mercadolocalia.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mercadolocalia.entities.Pago;

public interface PagoRepository extends JpaRepository<Pago, Integer> {
}