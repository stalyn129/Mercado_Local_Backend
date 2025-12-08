package com.mercadolocalia.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mercadolocalia.entities.LogSistema;
import com.mercadolocalia.repositories.LogRepository;
import com.mercadolocalia.services.LogService;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogRepository repo;

    @Override
    public void guardar(String accion, String usuario) {
        LogSistema log = new LogSistema();
        log.setAccion(accion);
        log.setUsuario(usuario);
        repo.save(log);
    }

    @Override
    public List<LogSistema> listar() {
        return repo.findAllByOrderByFechaDesc();
    }
}
