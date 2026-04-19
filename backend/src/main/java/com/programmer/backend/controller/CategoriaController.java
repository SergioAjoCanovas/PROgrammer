package com.programmer.backend.controller;

import com.programmer.backend.domain.CategoriaTecnologia;
import com.programmer.backend.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*") // Para que no te dé el error de conexión de antes
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping("/todas")
    public List<CategoriaTecnologia> obtenerTodas() {
        // Esto devuelve las categorías y, gracias al EAGER de la entidad, 
        // también incluye las tecnologías de cada una automáticamente.
        return categoriaRepository.findAll();
    }
}