package com.programmer.backend.controller;

import com.programmer.backend.domain.*;
import com.programmer.backend.repository.PerfilDesarrolladorRepository;
import com.programmer.backend.repository.PerfilEmpresaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @Autowired
    private PerfilDesarrolladorRepository devRepo;

    @Autowired
    private PerfilEmpresaRepository empresaRepo;

    @GetMapping("/ownProfile")
    public String ownProfile(HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuarioHeader", usuario);

        String rol = usuario.getRol().getNombre();

        // =========================
        // ADMIN (IMPORTANTE)
        // =========================
        if ("ADMIN".equals(rol) || "ROLE_ADMIN".equals(rol)) {
            return "UI/ownProfile/ownProfile"; // o la vista que quieras
        }

        // =========================
        // USER
        // =========================
        if ("USER".equals(rol)) {
            return "UI/profile/visitorProfile";
        }

        // =========================
        // DEVELOPER
        // =========================
        if ("DEVELOPER".equals(rol)) {
            PerfilDesarrollador perfil = devRepo.findByUsuarioId(usuario.getId())
                    .orElseGet(() -> {
                        PerfilDesarrollador nuevo = new PerfilDesarrollador();
                        nuevo.setUsuario(usuario);
                        return devRepo.save(nuevo);
                    });

            model.addAttribute("perfil", perfil);
            model.addAttribute("usuario", usuario);

            return "UI/ownProfile/ownProfile";
        }

        // =========================
        // COMPANY
        // =========================
        if ("COMPANY".equals(rol)) {

            PerfilEmpresa perfil = empresaRepo.findByUsuarioId(usuario.getId())
                    .orElseGet(() -> {
                        PerfilEmpresa nuevo = new PerfilEmpresa();
                        nuevo.setUsuario(usuario);
                        return empresaRepo.save(nuevo);
                    });

            model.addAttribute("perfil", perfil);
            model.addAttribute("usuario", usuario);

            return "UI/ownProfile/ownProfile";
        }

        return "redirect:/login";
    }
}