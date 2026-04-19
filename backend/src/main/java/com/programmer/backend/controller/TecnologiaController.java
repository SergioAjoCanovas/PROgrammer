package com.programmer.backend.controller;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.programmer.backend.domain.Tecnologia;
import com.programmer.backend.repository.TecnologiaRepository;

@RestController
@RequestMapping("/api/tecnologias")
@CrossOrigin(origins = "*")
public class TecnologiaController {

    @Autowired
    private TecnologiaRepository tecnologiaRepository;

    // Esta ruta devuelve TODAS las tecnologías en formato JSON
    @GetMapping("/todas")
    public List<Tecnologia> obtenerTodas() {
        return tecnologiaRepository.findAll();
    }
}