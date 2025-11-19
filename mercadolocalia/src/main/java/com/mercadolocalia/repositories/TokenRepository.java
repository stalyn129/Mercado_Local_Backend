package com.mercadolocalia.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadolocalia.entities.Token;
import com.mercadolocalia.entities.Usuario;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    List<Token> findByUsuario(Usuario usuario);
}
