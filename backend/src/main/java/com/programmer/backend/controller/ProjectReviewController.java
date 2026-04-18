package com.programmer.backend.controller;

import com.programmer.backend.domain.ProjectReview;
import com.programmer.backend.domain.Proyecto;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.ProjectReviewRepository;
import com.programmer.backend.repository.ProyectoRepository;
import com.programmer.backend.repository.UsuarioRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/project-reviews")
public class ProjectReviewController {

    private final ProjectReviewRepository reviewRepository;
    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;

    public ProjectReviewController(ProjectReviewRepository reviewRepository,
                                   ProyectoRepository proyectoRepository,
                                   UsuarioRepository usuarioRepository) {
        this.reviewRepository = reviewRepository;
        this.proyectoRepository = proyectoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // =========================
    // VER FORMULARIO
    // =========================
    @GetMapping("/crear/{proyectoId}")
    public String verFormulario(@PathVariable Long proyectoId, Model model) {

        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow();

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("review", new ProjectReview());

        return "UI/newprojectreview";
    }

    // =========================
    // CREAR REVIEW
    // =========================
    @PostMapping("/crear")
    public String crearReview(@ModelAttribute ProjectReview review,
                              @RequestParam Long proyectoId,
                              Principal principal) {

        //1. Usuario logeado
        if (principal == null) {
            return "redirect:/login";
        }

        Usuario autor = usuarioRepository.findByUsername(principal.getName())
                .orElseThrow();

        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow();

        //2. Autoreseña
        if (proyecto.getAutor().getId().equals(autor.getId())) {
            return "redirect:/project-reviews/" + proyectoId + "?error=autorreview";
        }

        //3. Duplicado
        if (reviewRepository.findByAutorAndProyecto(autor, proyecto).isPresent()) {
            return "redirect:/project-reviews/" + proyectoId + "?error=duplicado";
        }

        //Guardar
        review.setAutor(autor);
        review.setProyecto(proyecto);

        reviewRepository.save(review);

        return "redirect:/project-reviews/" + proyectoId;
    }

    // =========================
    // VER REVIEWS
    // =========================
    @GetMapping("/{proyectoId}")
    public String verReviews(@PathVariable Long proyectoId, Model model) {

        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow();

        List<ProjectReview> reviews = reviewRepository.findByProyecto(proyecto);

        // =========================
        // MEDIAS
        // =========================
        double mediaArq = reviews.stream()
                .mapToInt(ProjectReview::getArquitectura)
                .average().orElse(0);

        double mediaLim = reviews.stream()
                .mapToInt(ProjectReview::getLimpieza)
                .average().orElse(0);

        double mediaDoc = reviews.stream()
                .mapToInt(ProjectReview::getDocumentacion)
                .average().orElse(0);

        double mediaTotal = (mediaArq + mediaLim + mediaDoc) / 3;

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("reviews", reviews);
        model.addAttribute("mediaArq", mediaArq);
        model.addAttribute("mediaLim", mediaLim);
        model.addAttribute("mediaDoc", mediaDoc);
        model.addAttribute("mediaTotal", mediaTotal);

        return "UI/viewprojectreviews";
    }
}