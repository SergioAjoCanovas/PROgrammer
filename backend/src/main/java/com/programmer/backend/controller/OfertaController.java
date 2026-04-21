package com.programmer.backend.controller;

import com.programmer.backend.domain.OfertaEmpleo;
import com.programmer.backend.domain.Tecnologia;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.OfertaRepository;
import com.programmer.backend.repository.TecnologiaRepository;
import com.programmer.backend.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller 
public class OfertaController {

    @Autowired
    private OfertaRepository ofertaRepository;

    @Autowired
    private TecnologiaRepository tecnologiaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/jobsearching")
    public String verOfertas(Model model) {
        try {
            List<OfertaEmpleo> listaOfertas = ofertaRepository.findAll();
            // Aseguramos que 'ofertas' nunca sea null para que el HTML no explote
            model.addAttribute("ofertas", listaOfertas != null ? listaOfertas : new java.util.ArrayList<>());
            
            // Si tu archivo se llama jobsearching.html y está en templates/UI/
            return "UI/jobsearching/jobsearching"; 
        } catch (Exception e) {
            System.out.println("Error en jobsearching: " + e.getMessage());
            return "UI/main"; // Redirigir a inicio si falla
        }
    }

    @PostMapping("/api/ofertas/crear")
    public String crearOferta(
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("requisitos") String requisitos,
            @RequestParam("ofrecemos") String ofrecemos,
            @RequestParam("rango_salarial") String rangoSalarial,
            @RequestParam(value = "tecnologias_ids", required = false) List<Long> tecnologiasIds,
            @RequestParam("username_empresa") String usernameEmpresa) {
        
        OfertaEmpleo nuevaOferta = new OfertaEmpleo();
        nuevaOferta.setTitulo(titulo);
        nuevaOferta.setDescripcion(descripcion);
        nuevaOferta.setRequisitos(requisitos);
        nuevaOferta.setOfrecemos(ofrecemos);
        nuevaOferta.setRangoSalarial(rangoSalarial);
        nuevaOferta.setActiva(true);

        // BUSCAR Y ASIGNAR LA EMPRESA
        Optional<Usuario> empresaOpt = usuarioRepository.findByUsername(usernameEmpresa);
        if (empresaOpt.isPresent()) {
            nuevaOferta.setEmpresa(empresaOpt.get());
        }

        if (tecnologiasIds != null && !tecnologiasIds.isEmpty()) {
            List<Tecnologia> seleccionadas = tecnologiaRepository.findAllById(tecnologiasIds);
            nuevaOferta.setTecnologias(seleccionadas);
        }

        ofertaRepository.save(nuevaOferta);
        return "redirect:/jobsearching"; 
    }
}