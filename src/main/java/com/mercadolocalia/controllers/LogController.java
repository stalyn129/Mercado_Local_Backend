package com.mercadolocalia.controllers;

import com.mercadolocalia.entities.LogSistema;
import com.mercadolocalia.services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/logs")
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping
    public List<LogSistema> obtenerLogs() {
        return logService.listar();
    }
}
