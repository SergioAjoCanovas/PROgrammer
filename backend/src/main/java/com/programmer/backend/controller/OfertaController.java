package com.programmer.backend.controller;

import com.programmer.backend.domain.OfertaEmpleo;
import com.programmer.backend.domain.Tecnologia;
import com.programmer.backend.repository.OfertaRepository;
import com.programmer.backend.repository.TecnologiaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller // Usamos @Controller para poder redirigir al usuario después de guardar
public class OfertaController {

    @Autowired
    private OfertaRepository ofertaRepository;

    @Autowired
    private TecnologiaRepository tecnologiaRepository;

    @PostMapping("/api/ofertas/crear")
    public String crearOferta(
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            // Recogemos la lista de IDs de las tecnologías que ha marcado
            @RequestParam(value = "tecnologias_ids", required = false) List<Long> tecnologiasIds) {
        
        // 1. Creamos la oferta en blanco
        OfertaEmpleo nuevaOferta = new OfertaEmpleo();
        nuevaOferta.setTitulo(titulo);
        nuevaOferta.setDescripcion(descripcion);
        nuevaOferta.setActiva(true);

        // 2. Si ha marcado tecnologías, las buscamos en la BD y se las enganchamos
        if (tecnologiasIds != null && !tecnologiasIds.isEmpty()) {
            List<Tecnologia> tecnologiasSeleccionadas = tecnologiaRepository.findAllById(tecnologiasIds);
            nuevaOferta.setTecnologias(tecnologiasSeleccionadas);
        }

        // 3. Guardamos en la Base de Datos
        ofertaRepository.save(nuevaOferta);

        // 4. Redirigimos a la página principal tras el éxito
        return "redirect:http://127.0.0.1:5500/backend/src/main/resources/templates/UI/jobsearching/jobsearching.html"; 
    }
}