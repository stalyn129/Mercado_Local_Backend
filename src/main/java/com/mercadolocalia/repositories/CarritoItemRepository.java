package com.mercadolocalia.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.mercadolocalia.entities.CarritoItem;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Integer> {

    @Transactional
    void deleteAllByCarrito_IdCarrito(Integer idCarrito);
}
