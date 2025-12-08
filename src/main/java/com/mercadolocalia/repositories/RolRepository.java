package com.mercadolocalia.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadolocalia.entities.Rol;

public interface RolRepository extends JpaRepository<Rol, Integer> {
	
	Optional<Rol> findByNombreRol(String nombreRol);

}
