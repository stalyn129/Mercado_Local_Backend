package com.mercadolocalia.services;

import java.util.List;
import com.mercadolocalia.entities.LogSistema;

public interface LogService {
    void guardar(String accion, String usuario);
    List<LogSistema> listar();
}
