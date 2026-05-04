package com.programmer.backend.controller;

import java.util.List;
import com.programmer.backend.domain.ProjectReview;
import com.programmer.backend.domain.Proyecto;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.ProjectReviewRepository;
import com.programmer.backend.repository.ProyectoRepository;
import com.programmer.backend.service.ProjectReviewService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/project-reviews")
public class ProjectReviewController {

    private final ProjectReviewRepository reviewRepository;
    private final ProyectoRepository proyectoRepository;
    private final ProjectReviewService projectReviewService;

    public ProjectReviewController(ProjectReviewRepository reviewRepository,
                                   ProyectoRepository proyectoRepository,
                                   ProjectReviewService projectReviewService) {
        this.reviewRepository = reviewRepository;
        this.proyectoRepository = proyectoRepository;
        this.projectReviewService = projectReviewService; // 🔥 ESTO FALTABA
    }

    // =========================
    // VER FORMULARIO
    // =========================
    @GetMapping("/crear/{proyectoId}")
    public String verFormulario(@PathVariable Long proyectoId, Model model) {

        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElseThrow();

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("review", new ProjectReview());
        model.addAttribute("proyectoId", proyectoId);

        return "UI/newprojectreview/newprojectreview";
    }

    // =========================
    // CREAR REVIEW
    // =========================
    @PostMapping("/crear")
    public String crearReview(@ModelAttribute ProjectReview review,
                             @RequestParam Long proyectoId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        Usuario autor = (Usuario) session.getAttribute("usuarioLogueado");

        if (autor == null) return "redirect:/login";

        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
        if (proyecto == null) return "redirect:/proyectos";

        boolean isAdmin = autor.getRol() != null &&
                ("ADMIN".equalsIgnoreCase(autor.getRol().getNombre())
                        || autor.getRol().getId() == 1L);

        // VISITOR no puede reseñar
        if (autor.getRol() != null &&
                "VISITOR".equalsIgnoreCase(autor.getRol().getNombre())) {

            redirectAttributes.addFlashAttribute("error", "no_permitido");
            return "redirect:/proyectos/proyecto/" + proyectoId;
        }

        // Autoreseña
        if (proyecto.getAutor().getId().equals(autor.getId()) && !isAdmin) {
            redirectAttributes.addFlashAttribute("error", "autorreview");
            return "redirect:/project-reviews/" + proyectoId;
        }

        // Duplicado
        if (reviewRepository.findByAutorAndProyecto(autor, proyecto).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "duplicado");
            return "redirect:/project-reviews/" + proyectoId;
        }

        review.setAutor(autor);
        review.setProyecto(proyecto);

        reviewRepository.save(review);

        redirectAttributes.addFlashAttribute("success", "review_creada");

        return "redirect:/project-reviews/" + proyectoId;
    }

    // =========================
    // VER REVIEWS (🔥 ESTE ES EL ÚNICO)
    // =========================
    @GetMapping("/{proyectoId}")
    public String verReviews(@PathVariable Long proyectoId, Model model) {

        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);

        if (proyecto == null) {
            return "redirect:/proyectos";
        }

        Usuario autor = proyecto.getAutor();

        List<ProjectReview> reviews =
                reviewRepository.findByProyectoOrderByFechaDesc(proyecto);

        // ⚠️ evitar null en medias
        Double mediaArq = projectReviewService.mediaArquitectura(proyectoId);
        Double mediaLim = projectReviewService.mediaLimpieza(proyectoId);
        Double mediaDoc = projectReviewService.mediaDocumentacion(proyectoId);

        double total = 0;
        if (mediaArq != null && mediaLim != null && mediaDoc != null) {
            total = (mediaArq + mediaLim + mediaDoc) / 3;
        }

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("autor", autor);
        model.addAttribute("reviews", reviews);

        model.addAttribute("mediaArq", mediaArq != null ? mediaArq : 0);
        model.addAttribute("mediaLim", mediaLim != null ? mediaLim : 0);
        model.addAttribute("mediaDoc", mediaDoc != null ? mediaDoc : 0);
        model.addAttribute("mediaTotal", total);

        return "UI/viewprojectreviews/viewprojectreviews";
    }
}