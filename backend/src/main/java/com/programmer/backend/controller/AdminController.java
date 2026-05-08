package com.programmer.backend.controller;

import com.programmer.backend.domain.Proyecto;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.ProyectoRepository;
import com.programmer.backend.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/adminPanel")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    // Helper method to safely check if a user is an admin without throwing NullPointerExceptions
    private boolean isUserAdmin(Usuario usuario) {
        if (usuario == null || usuario.getRol() == null || usuario.getRol().getNombre() == null) {
            return false;
        }
        String nombreRol = usuario.getRol().getNombre();
        return "ADMIN".equals(nombreRol) || "1".equals(nombreRol);
    }

    @GetMapping
    public String adminPanel(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        
        // Safely check if logged-in user is admin
        if (!isUserAdmin(usuario)) {
            return "redirect:/main";
        }

        List<Usuario> todos = usuarioRepository.findAll();

        List<Usuario> logueados = todos.stream()
                .filter(u -> "LOGUEADO".equals(u.getEstado()) || u.getEstado() == null)
                .filter(u -> !isUserAdmin(u)) // Safe check
                .collect(Collectors.toList());

        List<Usuario> verificados = todos.stream()
                .filter(u -> "VERIFICADO".equals(u.getEstado()) || 
                            (isUserAdmin(u) && !"ELIMINADO".equals(u.getEstado()))) // Safe check
                .collect(Collectors.toList());

        model.addAttribute("logueados", logueados);
        model.addAttribute("verificados", verificados);

        List<Proyecto> todosProyectos = proyectoRepository.findAll();

        Map<Usuario, List<Proyecto>> proyectosNoValidados = todosProyectos.stream()
                .filter(p -> p.getEstaValidado() == null || !p.getEstaValidado())
                .collect(Collectors.groupingBy(Proyecto::getAutor));

        Map<Usuario, List<Proyecto>> proyectosValidados = todosProyectos.stream()
                .filter(p -> p.getEstaValidado() != null && p.getEstaValidado())
                .collect(Collectors.groupingBy(Proyecto::getAutor));

        model.addAttribute("proyectosNoValidados", proyectosNoValidados);
        model.addAttribute("proyectosValidados", proyectosValidados);

        return "UI/adminPanel/adminPanel";
    }

    @PostMapping("/verificar/{id}")
    public String verificarUsuario(@PathVariable Long id, HttpSession session) {
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        if (isUserAdmin(sessionUser)) {
            Usuario usuario = usuarioRepository.findById(id).orElse(null);
            if (usuario != null) {
                usuario.setEstado("VERIFICADO");
                usuarioRepository.save(usuario);
            }
        }
        return "redirect:/adminPanel";
    }

    @PostMapping("/desverificar/{id}")
    public String desverificarUsuario(@PathVariable Long id, HttpSession session) {
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        if (isUserAdmin(sessionUser)) {
            Usuario usuario = usuarioRepository.findById(id).orElse(null);
            if (usuario != null) {
                usuario.setEstado("LOGUEADO"); 
                usuarioRepository.save(usuario);
            }
        }
        return "redirect:/adminPanel";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, HttpSession session) {
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        if (isUserAdmin(sessionUser)) {
            Usuario usuario = usuarioRepository.findById(id).orElse(null);
            if (usuario != null) {
                try {
                    usuarioRepository.delete(usuario);
                } catch (Exception e) {
                    usuario.setEstado("ELIMINADO");
                    usuarioRepository.save(usuario);
                    System.err.println("No se pudo borrar completamente por dependencias en DB, se ha hecho soft delete: " + e.getMessage());
                }
            }
        }
        return "redirect:/adminPanel";
    }

    @PostMapping("/verificarProyecto/{id}")
    public String verificarProyecto(@PathVariable Long id, HttpSession session) {
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        if (isUserAdmin(sessionUser)) {
            Proyecto proyecto = proyectoRepository.findById(id).orElse(null);
            if (proyecto != null) {
                proyecto.setEstaValidado(true);
                proyectoRepository.save(proyecto);
            }
        }
        return "redirect:/adminPanel";
    }

    @PostMapping("/desverificarProyecto/{id}")
    public String desverificarProyecto(@PathVariable Long id, HttpSession session) {
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        if (isUserAdmin(sessionUser)) {
            Proyecto proyecto = proyectoRepository.findById(id).orElse(null);
            if (proyecto != null) {
                proyecto.setEstaValidado(false);
                proyectoRepository.save(proyecto);
            }
        }
        return "redirect:/adminPanel";
    }

    @PostMapping("/eliminarProyecto/{id}")
    public String eliminarProyecto(@PathVariable Long id, HttpSession session) {
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        if (isUserAdmin(sessionUser)) {
            Proyecto proyecto = proyectoRepository.findById(id).orElse(null);
            if (proyecto != null) {
                proyectoRepository.delete(proyecto);
            }
        }
        return "redirect:/adminPanel";
    }
}