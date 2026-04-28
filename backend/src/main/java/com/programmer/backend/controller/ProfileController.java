package com.programmer.backend.controller;

import java.util.List;
import java.util.Comparator;
import com.programmer.backend.domain.*;
import com.programmer.backend.repository.PerfilDesarrolladorRepository;
import com.programmer.backend.repository.PerfilEmpresaRepository;
import com.programmer.backend.repository.UsuarioRepository; // NUEVO
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // NUEVO

@Controller
public class ProfileController {

    @Autowired
    private PerfilDesarrolladorRepository devRepo;

    @Autowired
    private PerfilEmpresaRepository empresaRepo;

    @Autowired
    private UsuarioRepository usuarioRepo; // NUEVO: Repositorio de Usuarios

    @GetMapping("/ownProfile")
    public String ownProfile(HttpSession session, Model model) {
    
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
    
        if (usuario == null) {
            return "redirect:/login";
        }
    
        model.addAttribute("usuarioHeader", usuario);
    
        String cvUrl = usuario.getCurriculum();
        model.addAttribute("cvUrl", cvUrl);
        model.addAttribute("cvNombre", limpiarNombreCV(extraerNombreCV(cvUrl)));
    
        model.addAttribute("tecnologiasUsuario", List.of());
    
        String rol = usuario.getRol().getNombre();
    
        if ("ADMIN".equals(rol) || "ROLE_ADMIN".equals(rol)) {
            return "UI/ownProfile/ownProfile";
        }
    
        if ("USER".equals(rol)) {
            return "UI/profile/visitorProfile";
        }
    
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

    // ==========================================
    // NUEVO MÉTODO: VER PERFIL PÚBLICO DE TERCEROS
    // ==========================================
    @GetMapping("/profileView/{id}")
    public String verPerfilPublico(@PathVariable Long id, Model model, HttpSession session) {
        
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado != null) {
            model.addAttribute("usuarioHeader", usuarioLogueado);
        }

        // Buscar al usuario destino en la Base de Datos
        Usuario usuarioDestino = usuarioRepo.findById(id).orElse(null);

        if (usuarioDestino == null) {
            return "redirect:/searchProgrammer"; // Si no existe, vuelve al buscador
        }

        model.addAttribute("usuario", usuarioDestino);

        // Si es desarrollador, pasamos también sus tecnologías al modelo
        devRepo.findByUsuarioId(id).ifPresent(perfilDev -> {
            model.addAttribute("perfilDesarrollador", perfilDev);
        });

        return "UI/profileView/profileView"; 
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