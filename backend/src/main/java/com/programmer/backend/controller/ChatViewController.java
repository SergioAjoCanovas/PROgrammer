package com.programmer.backend.controller;

import com.programmer.backend.domain.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/chats")
public class ChatViewController {

    @org.springframework.beans.factory.annotation.Autowired
    private com.programmer.backend.repository.UsuarioRepository usuarioRepo;

    @GetMapping
    public String chats(Model model, HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("user1", usuario.getId());
        model.addAttribute("user2", null);

        return "UI/chats/chats";
    }

    @GetMapping("/{user1}/{user2}")
    public String chat(@PathVariable Long user1,
                       @PathVariable Long user2,
                       Model model,
                       HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return "redirect:/login";
        }

        // evita que alguien fuerce otro user1 en URL
        if (!usuario.getId().equals(user1)) {
            return "redirect:/chats";
        }

        model.addAttribute("user1", usuario.getId());
        model.addAttribute("user2", user2);

        // Añadir info del receptor para que el chat cargue su cabecera directamente
        com.programmer.backend.domain.Usuario receptor = usuarioRepo.findById(user2).orElse(null);
        if (receptor != null) {
            model.addAttribute("receptorNombre", receptor.getUsername());
            model.addAttribute("receptorAvatar", receptor.getFotoPerfil());
        }

        return "UI/chats/chats";
    }
}