package com.programmer.backend.controller;

import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/adminPanel")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String adminPanel(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || (!"ADMIN".equals(usuario.getRol().getNombre()) && !"1".equals(usuario.getRol().getNombre()))) {
            return "redirect:/main";
        }

        List<Usuario> todos = usuarioRepository.findAll();

        List<Usuario> logueados = todos.stream()
                .filter(u -> "LOGUEADO".equals(u.getEstado()) || u.getEstado() == null)
                .filter(u -> !"ADMIN".equals(u.getRol().getNombre()) && !"1".equals(u.getRol().getNombre()))
                .collect(Collectors.toList());

        List<Usuario> verificados = todos.stream()
                .filter(u -> "VERIFICADO".equals(u.getEstado()) || 
                            (("ADMIN".equals(u.getRol().getNombre()) || "1".equals(u.getRol().getNombre())) && !"ELIMINADO".equals(u.getEstado())))
                .collect(Collectors.toList());

        model.addAttribute("logueados", logueados);
        model.addAttribute("verificados", verificados);

        return "UI/adminPanel/adminPanel";
    }

    @PostMapping("/verificar/{id}")
    public String verificarUsuario(@PathVariable Long id, HttpSession session) {
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        if (sessionUser != null && ("ADMIN".equals(sessionUser.getRol().getNombre()) || "1".equals(sessionUser.getRol().getNombre()))) {
            Usuario usuario = usuarioRepository.findById(id).orElse(null);
            if (usuario != null) {
                usuario.setEstado("VERIFICADO");
                usuarioRepository.save(usuario);
            }
        }
        return "redirect:/adminPanel";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, HttpSession session) {
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        if (sessionUser != null && ("ADMIN".equals(sessionUser.getRol().getNombre()) || "1".equals(sessionUser.getRol().getNombre()))) {
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
}
