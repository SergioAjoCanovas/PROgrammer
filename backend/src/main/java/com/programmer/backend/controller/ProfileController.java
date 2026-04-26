package com.programmer.backend.controller;
import java.util.List;
import java.util.Comparator;
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
    
        // =========================
        // SIEMPRE DISPONIBLE (IMPORTANTE)
        // =========================
        model.addAttribute("usuarioHeader", usuario);
    
        String cvUrl = usuario.getCurriculum();
        model.addAttribute("cvUrl", cvUrl);
        model.addAttribute("cvNombre", limpiarNombreCV(extraerNombreCV(cvUrl)));
    
        model.addAttribute("tecnologiasUsuario", List.of());
    
        String rol = usuario.getRol().getNombre();
    
        // =========================
        // ADMIN
        // =========================
        if ("ADMIN".equals(rol) || "ROLE_ADMIN".equals(rol)) {
            return "UI/ownProfile/ownProfile";
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
    
            List<Tecnologia> tecnologiasOrdenadas = perfil.getTecnologias()
                    .stream()
                    .sorted(Comparator.comparing(Tecnologia::getNombre))
                    .toList();
    
            model.addAttribute("tecnologiasUsuario", tecnologiasOrdenadas);
    
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
    
            return "UI/ownProfile/ownProfile";
        }
    
        return "redirect:/login";
    }

    private String limpiarNombreCV(String nombre) {
        if (nombre == null) return null;
    
        return nombre.replaceFirst("^\\d+_", "");
    }
    
    private String extraerNombreCV(String url) {
        if (url == null || url.isBlank()) return null;
    
        String nombre = url.substring(url.lastIndexOf("/") + 1);

        nombre = nombre.replaceFirst("^[0-9]+[-_]*", "");
    
        return nombre;
    }
}