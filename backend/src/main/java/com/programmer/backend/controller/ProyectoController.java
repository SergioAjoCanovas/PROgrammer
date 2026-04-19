package com.programmer.backend.controller;

import com.programmer.backend.domain.Proyecto;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.ProyectoRepository;
import com.programmer.backend.repository.UsuarioRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/proyectos")
public class ProyectoController {

    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;

    public ProyectoController(ProyectoRepository proyectoRepository,
                              UsuarioRepository usuarioRepository) {
        this.proyectoRepository = proyectoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // =========================
    // VER FORMULARIO CREAR
    // =========================
    @GetMapping("/crear")
    public String verFormulario(Model model) {

        model.addAttribute("proyecto", new Proyecto());

        return "UI/createProject";
    }

    // =========================
    // CREAR PROYECTO
    // =========================
    @PostMapping("/crear")
    public String crearProyecto(@ModelAttribute Proyecto proyecto,
                                Principal principal) {

        // Usuario logeado
        if (principal == null) {
            return "redirect:/login";
        }

        Usuario autor = usuarioRepository.findByUsername(principal.getName())
                .orElseThrow();

        // Seteos obligatorios
        proyecto.setAutor(autor);
        proyecto.setEstaValidado(false);

        // Guardar
        proyectoRepository.save(proyecto);

        return "redirect:/proyectos";
    }

    // =========================
    // LISTAR PROYECTOS
    // =========================
    @GetMapping
    public String listarProyectos(Model model) {

        model.addAttribute("proyectos", proyectoRepository.findAll());

        return "UI/projectlist";
    }

    // =========================
    // VER DETALLE PROYECTO
    // =========================
    @GetMapping("/{id}")
    public String verProyecto(@PathVariable Long id, Model model) {

        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow();

        model.addAttribute("proyecto", proyecto);

        return "UI/projectview";
    }
}
