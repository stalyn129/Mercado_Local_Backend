// src/main/java/com/mercadolocalia/repositories/ConfigRepository.java
package com.mercadolocalia.repositories;

import com.mercadolocalia.entities.Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ConfigRepository extends JpaRepository<Config, Long> {
    Optional<Config> findBySistema(String sistema);
    
    // Opcional: si quieres mantener el método delete
    // void deleteBySistema(String sistema);
}