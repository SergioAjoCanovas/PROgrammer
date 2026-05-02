package com.programmer.backend.controller;

import com.programmer.backend.domain.NewReview;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.NewReviewRepository;
import com.programmer.backend.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    // El método verPerfil se mantiene por compatibilidad, 
    // pero ahora el flujo principal irá por ProfileController
    @GetMapping("/{id}")
    public String verPerfil(@PathVariable Long id, Model model) {
        return "redirect:/profileView/" + id;
    }

    @PostMapping("/crear")
    public String crearReview(@ModelAttribute NewReview newReview,
                              @RequestParam Long receptorId,
                              HttpSession session) {

        // 1. Comprobar login
        Usuario autor = (Usuario) session.getAttribute("usuarioLogueado");

        if (autor == null) {
            return "redirect:/profileView/" + receptorId + "?error=login";
        }

        Usuario receptor = usuarioRepository.findById(receptorId)
                .orElseThrow();

        // 2. Evitar autoreview
        if (autor.getId().equals(receptor.getId())) {
            return "redirect:/profileView/" + receptorId + "?error=autoreview";
        }

        // 3. Evitar duplicadas
        Optional<NewReview> existente =
                reviewRepository.findByAutorAndReceptor(autor, receptor);

        if (existente.isPresent()) {
            return "redirect:/profileView/" + receptorId + "?error=duplicated";
        }

        // 4. Validar datos mínimos
        if (newReview.getRating() <= 0 || newReview.getComentario() == null
                || newReview.getComentario().trim().isEmpty()) {
            return "redirect:/profileView/" + receptorId + "?error=invalid";
        }

        // 5. Asignar y guardar
        newReview.setAutor(autor);
        newReview.setReceptor(receptor);
        reviewRepository.save(newReview);

        return "redirect:/profileView/" + receptorId + "?success=true";
    }
}