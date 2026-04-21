package com.programmer.backend.controller;

import com.programmer.backend.domain.Proyecto;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.ProyectoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/proyectos")
public class ProyectoController {

    private final ProyectoRepository proyectoRepository;

    public ProyectoController(ProyectoRepository proyectoRepository) {
        this.proyectoRepository = proyectoRepository;
    }

    @PostMapping("/crear")
    public String crearProyecto(@ModelAttribute Proyecto proyecto,
                                HttpSession session) {

        System.out.println("ENTRA AL CONTROLLER");

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            System.out.println("NO HAY USUARIO");
            return "redirect:/login";
        }

        System.out.println("USUARIO LOGUEADO: " + usuario.getUsername());

        // 🔥 IMPORTANTE: campo correcto es autor
        proyecto.setAutor(usuario);

        proyectoRepository.save(proyecto);

        System.out.println("PROYECTO GUARDADO");

        return "redirect:/ownProfile";
    }
}