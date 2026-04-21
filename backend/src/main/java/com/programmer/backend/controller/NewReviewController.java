package com.programmer.backend.controller;
import com.programmer.backend.domain.NewReview;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.NewReviewRepository;
import com.programmer.backend.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class NewReviewController {

    private final NewReviewRepository reviewRepository;
    private final UsuarioRepository usuarioRepository;

    public NewReviewController(NewReviewRepository reviewRepository,
                                UsuarioRepository usuarioRepository) {
        this.reviewRepository = reviewRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // =========================
    // VER PERFIL + REVIEWS
    // =========================
    @GetMapping("/{id}")
    public String verPerfil(@PathVariable Long id, Model model) {

        Usuario receptor = usuarioRepository.findById(id)
                .orElseThrow();

        List<NewReview> reviews = reviewRepository.findByReceptor(receptor);

        Double media = reviewRepository.getAverageRating(receptor);

        model.addAttribute("usuario", receptor);
        model.addAttribute("reviews", reviews);
        model.addAttribute("media", media != null ? media : 0);

        model.addAttribute("newReview", new NewReview());

        return "UI/newreview";
    }

    // =========================
    // CREAR REVIEW (BACKEND REAL)
    // =========================
    @PostMapping("/crear")
    public String crearReview(@ModelAttribute NewReview newReview,
                              @RequestParam Long receptorId,
                              HttpSession session,
                              Model model) {

        // 1. comprobar login
        String username = (String) session.getAttribute("usuarioLogueado");

        if (username == null) {
            return "redirect:/reviews/" + receptorId + "?error=login";
        }

        Usuario autor = usuarioRepository.findByUsername(username)
                .orElseThrow();

        Usuario receptor = usuarioRepository.findById(receptorId)
                .orElseThrow();

        // 2. evitar autoreview
        if (autor.getId().equals(receptor.getId())) {
            return "redirect:/reviews/" + receptorId + "?error=autoreview";
        }

        // 3. evitar duplicadas (1 review por usuario hacia receptor)
        Optional<NewReview> existente =
                reviewRepository.findByAutorAndReceptor(autor, receptor);

        if (existente.isPresent()) {
            return "redirect:/reviews/" + receptorId + "?error=duplicated";
        }

        // 4. validar datos mínimos
        if (newReview.getRating() <= 0 || newReview.getComentario() == null
                || newReview.getComentario().trim().isEmpty()) {
            return "redirect:/reviews/" + receptorId + "?error=invalid";
        }

        // 5. asignar
        newReview.setAutor(autor);
        newReview.setReceptor(receptor);

        reviewRepository.save(newReview);

        return "redirect:/reviews/" + receptorId;
    }
}