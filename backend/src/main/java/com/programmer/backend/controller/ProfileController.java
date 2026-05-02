package com.programmer.backend.controller;

import java.util.*;
import java.util.stream.Collectors;

import com.programmer.backend.domain.*;
import com.programmer.backend.repository.*;
import com.programmer.backend.service.ProyectoService;
import com.programmer.backend.service.PerfilDesarrolladorService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private PerfilDesarrolladorService perfilService;

    // =========================
    // OWN PROFILE
    // =========================
    @GetMapping("/ownProfile")
    public String ownProfile(HttpSession session, Model model) {

        Usuario usuarioSession = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSession == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioRepo.findById(usuarioSession.getId()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuarioHeader", usuario);

        // REVIEWS
        List<NewReview> reviews = reviewRepository.findByReceptor(usuario);
        Double media = reviewRepository.getAverageRating(usuario);

        model.addAttribute("reviews", reviews);
        model.addAttribute("media", media != null ? media : 0);

        // CV
        String cvUrl = usuario.getCurriculum();
        model.addAttribute("cvUrl", cvUrl);
        model.addAttribute("cvNombre", limpiarNombreCV(extraerNombreCV(cvUrl)));

        // PROYECTOS
        List<ProyectoService.ProyectoDTO> ultimosProyectos =
                proyectoService.obtenerUltimosProyectos(usuario);

        model.addAttribute("ultimosProyectos", ultimosProyectos);

        // SEGUIMIENTO
        Set<Usuario> seguidores = usuario.getSeguidores() != null ? usuario.getSeguidores() : new HashSet<>();
        Set<Usuario> siguiendo = usuario.getSiguiendo() != null ? usuario.getSiguiendo() : new HashSet<>();
        
        Set<Usuario> amigos = new HashSet<>(seguidores);
        amigos.retainAll(siguiendo);

        model.addAttribute("seguidores", seguidores);
        model.addAttribute("siguiendo", siguiendo);
        model.addAttribute("amigos", amigos);

        String rol = usuario.getRol().getNombre();

        // ADMIN + DEVELOPER
        if ("ADMIN".equals(rol) || "ROLE_ADMIN".equals(rol) || "DEVELOPER".equals(rol)) {
            return buildDeveloperLikeProfile(usuario, model);
        }

        // COMPANY
        if ("COMPANY".equals(rol)) {

            PerfilEmpresa perfil = empresaRepo.findByUsuarioId(usuario.getId())
                    .orElseGet(() -> {
                        PerfilEmpresa nuevo = new PerfilEmpresa();
                        nuevo.setUsuario(usuario);
                        return empresaRepo.save(nuevo);
                    });

            List<Tecnologia> tecnologiasOrdenadas = perfil.getTecnologias().stream()
                    .sorted(Comparator.comparing(Tecnologia::getNombre))
                    .toList();

            model.addAttribute("perfil", perfil);
            model.addAttribute("tecnologiasUsuario", tecnologiasOrdenadas);

            return "UI/ownProfile/ownProfile";
        }

        // USER
        if ("USER".equals(rol)) {
            return "UI/profile/visitorProfile";
        }

        return "redirect:/login";
    }

    // =========================
    // PUBLIC PROFILE
    // =========================
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

        List<ProyectoService.ProyectoDTO> ultimosProyectos =
                proyectoService.obtenerUltimosProyectos(usuarioDestino);

        model.addAttribute("ultimosProyectos", ultimosProyectos);

        devRepo.findByUsuarioIdWithTecnologias(id).ifPresent(perfilDev -> {

            model.addAttribute("perfilDesarrollador", perfilDev);

            Map<String, List<Tecnologia>> techsAgrupadas =
                    perfilDev.getTecnologias().stream()
                            .collect(Collectors.groupingBy(t ->
                                    t.getCategoria() != null ?
                                            t.getCategoria().getNombre() :
                                            "Otras"
                            ));

            model.addAttribute("techsPorCategoria", techsAgrupadas);
        });

        return "UI/profileView/profileView";
    }

    // =========================
    // CORE LOGIC
    // =========================
    private String buildDeveloperLikeProfile(Usuario usuario, Model model) {

        PerfilDesarrollador perfil = perfilService.getOrCreateProfile(usuario);

        List<Tecnologia> tecnologiasOrdenadas =
                perfilService.getTecnologiasOrdenadas(perfil);

        model.addAttribute("perfil", perfil);
        model.addAttribute("tecnologiasUsuario", tecnologiasOrdenadas);

        return "UI/ownProfile/ownProfile";
    }

    // =========================
    // HELPERS
    // =========================
    private String limpiarNombreCV(String nombre) {
        if (nombre == null) return null;
        return nombre.replaceFirst("^\\d+_", "");
    }

    private String extraerNombreCV(String url) {
        if (url == null || url.isBlank()) return null;
        String nombre = url.substring(url.lastIndexOf("/") + 1);
        return nombre.replaceFirst("^[0-9]+[-_]*", "");
    }
}