package com.programmer.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;

import com.programmer.backend.domain.*;
import com.programmer.backend.repository.NewReviewRepository;
import com.programmer.backend.repository.PerfilDesarrolladorRepository;
import com.programmer.backend.repository.PerfilEmpresaRepository;
import com.programmer.backend.repository.UsuarioRepository;
import com.programmer.backend.service.ProyectoService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProfileController {

    @Autowired
    private PerfilDesarrolladorRepository devRepo;

    @Autowired
    private PerfilEmpresaRepository empresaRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private NewReviewRepository reviewRepository;

    @Autowired
    private ProyectoService proyectoService;

    @GetMapping("/ownProfile")
    public String ownProfile(HttpSession session, Model model) {
    
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
    
        if (usuario == null) {
            return "redirect:/login";
        }
    
        model.addAttribute("usuarioHeader", usuario);
    
        // RESEÑAS
        List<NewReview> reviews = reviewRepository.findByReceptor(usuario);
        Double media = reviewRepository.getAverageRating(usuario);
    
        model.addAttribute("reviews", reviews);
        model.addAttribute("media", media != null ? media : 0);
    
        // CV
        String cvUrl = usuario.getCurriculum();
        model.addAttribute("cvUrl", cvUrl);
        model.addAttribute("cvNombre", limpiarNombreCV(extraerNombreCV(cvUrl)));
    
        // TECNOLOGÍAS (por defecto vacío)
        model.addAttribute("tecnologiasUsuario", List.of());
    
        // 🔥 PROYECTOS GLOBAL (AQUÍ ESTÁ LA CLAVE)
        List<ProyectoService.ProyectoDTO> ultimosProyectos =
                proyectoService.obtenerUltimosProyectos(usuario);
    
        model.addAttribute("ultimosProyectos", ultimosProyectos);
    
        String rol = usuario.getRol().getNombre();
    
        if ("ADMIN".equals(rol) || "ROLE_ADMIN".equals(rol)) {
            return "UI/ownProfile/ownProfile";
        }
    
        if ("USER".equals(rol)) {
            return "UI/profile/visitorProfile";
        }
    
        List<Tecnologia> tecnologiasOrdenadas = List.of();

        if ("DEVELOPER".equals(rol)) {
        
            PerfilDesarrollador perfil = devRepo.findByUsuarioId(usuario.getId())
                    .orElse(null);
        
            if (perfil != null) {
                tecnologiasOrdenadas = perfil.getTecnologias().stream()
                        .sorted(Comparator.comparing(Tecnologia::getNombre))
                        .toList();
            }
        
        } else if ("COMPANY".equals(rol)) {
        
            PerfilEmpresa perfil = empresaRepo.findByUsuarioId(usuario.getId())
                    .orElse(null);
        
            if (perfil != null) {
                tecnologiasOrdenadas = perfil.getTecnologias().stream()
                        .sorted(Comparator.comparing(Tecnologia::getNombre))
                        .toList();
            }
        }
        
        model.addAttribute("tecnologiasUsuario", tecnologiasOrdenadas);
    
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

    @GetMapping("/profileView/{id}")
    public String verPerfilPublico(@PathVariable Long id, Model model, HttpSession session) {

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado != null) {
            model.addAttribute("usuarioHeader", usuarioLogueado);
        }

        Usuario usuarioDestino = usuarioRepo.findById(id).orElse(null);

        if (usuarioDestino == null) {
            return "redirect:/searchProgrammer";
        }

        model.addAttribute("usuario", usuarioDestino);

        List<NewReview> reviews = reviewRepository.findByReceptor(usuarioDestino);
        Double media = reviewRepository.getAverageRating(usuarioDestino);

        model.addAttribute("reviews", reviews);
        model.addAttribute("media", media != null ? media : 0);
        model.addAttribute("newReview", new NewReview());

        devRepo.findByUsuarioId(id).ifPresent(perfilDev -> {
            model.addAttribute("perfilDesarrollador", perfilDev);

            try {
                Map<String, List<Tecnologia>> techsAgrupadas = perfilDev.getTecnologias().stream()
                        .collect(Collectors.groupingBy(t -> {
                            if (t.getCategoria() != null && t.getCategoria().getNombre() != null) {
                                return t.getCategoria().getNombre();
                            }
                            return "Otras";
                        }));
                model.addAttribute("techsPorCategoria", techsAgrupadas);
            } catch (Exception e) {
                model.addAttribute("techsPorCategoria", null);
            }
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