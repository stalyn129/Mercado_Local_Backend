package com.mercadolocalia.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadolocalia.entities.CarritoItem;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Integer> {

    List<CarritoItem> findByCarritoIdCarrito(Integer idCarrito);

}
