package com.mercadolocalia.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mercadolocalia.entities.LogSistema;
import java.util.List;

public interface LogRepository extends JpaRepository<LogSistema, Integer> {

    List<LogSistema> findAllByOrderByFechaDesc();

}
