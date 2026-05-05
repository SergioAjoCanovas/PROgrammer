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
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/project-reviews")
@Transactional
public class ProjectReviewController {

    private final ProjectReviewRepository reviewRepository;
    private final ProyectoRepository proyectoRepository;
    private final ProjectReviewService projectReviewService;
    private final com.programmer.backend.service.NotificacionService notificacionService;

    public ProjectReviewController(ProjectReviewRepository reviewRepository,
                                   ProyectoRepository proyectoRepository,
                                   ProjectReviewService projectReviewService,
                                   com.programmer.backend.service.NotificacionService notificacionService) {
        this.reviewRepository = reviewRepository;
        this.proyectoRepository = proyectoRepository;
        this.projectReviewService = projectReviewService;
        this.notificacionService = notificacionService;
    }

    // =========================
    // VER FORMULARIO
    // =========================
    @GetMapping("/crear/{proyectoId}")
    public String verFormulario(@PathVariable Long proyectoId, 
                               @RequestParam(required = false) String edit,
                               Model model, HttpSession session) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElseThrow();
        Usuario autor = (Usuario) session.getAttribute("usuarioLogueado");
        
        ProjectReview reviewExistente = null;
        boolean editMode = "true".equals(edit);
        boolean isDuplicate = false;

        if (autor != null) {
            boolean isAdmin = autor.getRol() != null &&
                    ("ADMIN".equalsIgnoreCase(autor.getRol().getNombre()) || autor.getRol().getId() == 1L);

            if (proyecto.getAutor().getId().equals(autor.getId()) && !isAdmin) {
                model.addAttribute("error", "autorreview");
            }
            
            reviewExistente = reviewRepository.findByAutorAndProyecto(autor, proyecto).orElse(null);
            
            if (reviewExistente != null && !editMode) {
                isDuplicate = true;
                model.addAttribute("error", "duplicado");
            }
        }

        // Si es duplicado y NO estamos editando, mandamos una review vacía para que el textarea esté limpio
        model.addAttribute("proyecto", proyecto);
        model.addAttribute("review", (reviewExistente != null && editMode) ? reviewExistente : new ProjectReview());
        model.addAttribute("editMode", editMode);
        model.addAttribute("isDuplicate", isDuplicate);
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

        review.setAutor(autor);
        review.setProyecto(proyecto);

        // Si ya existe una review del mismo autor para el mismo proyecto, la actualizamos
        ProjectReview reviewExistente = reviewRepository.findByAutorAndProyecto(autor, proyecto).orElse(null);
        if (reviewExistente != null) {
            reviewExistente.setComentario(review.getComentario());
            reviewExistente.setArquitectura(review.getArquitectura());
            reviewExistente.setLimpieza(review.getLimpieza());
            reviewExistente.setDocumentacion(review.getDocumentacion());
            reviewRepository.save(reviewExistente);
            redirectAttributes.addAttribute("success", "updated");
        } else {
            reviewRepository.save(review);
            redirectAttributes.addAttribute("success", "created");
        }

        // Enviar notificación al autor del proyecto
        String mensajeNotif = autor.getUsername() + " ha valorado tu proyecto '" + proyecto.getTitulo() + "': " + review.getComentario();
        notificacionService.enviarNotificacion(proyecto.getAutor(), mensajeNotif, "NUEVA_RESEÑA_PROYECTO", "/project-reviews/" + proyectoId);

        return "redirect:/project-reviews/" + proyectoId;
    }

    // =========================
    // ELIMINAR REVIEW
    // =========================
    @PostMapping("/eliminar")
    public String eliminarReview(@RequestParam Long reviewId, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null) return "redirect:/login";

        ProjectReview review = reviewRepository.findById(reviewId).orElse(null);
        if (review != null) {
            boolean isAdmin = usuarioLogueado.getRol() != null && 
                             ("ADMIN".equalsIgnoreCase(usuarioLogueado.getRol().getNombre()) || usuarioLogueado.getRol().getId() == 1L);
            
            if (review.getAutor().getId().equals(usuarioLogueado.getId()) || isAdmin) {
                Long proyectoId = review.getProyecto().getId();
                reviewRepository.delete(review);
                redirectAttributes.addAttribute("success", "deleted");
                return "redirect:/project-reviews/" + proyectoId;
            }
        }
        return "redirect:/proyectos";
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