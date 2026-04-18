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
    // CREAR REVIEW
    // =========================
    //Pendiente de asignar las reseñas a logeos, no poder hacer autoreseñas e impedir reseñas duplicadas de usuario.
    //Todo ello va asociado al login.
    //PARA QUE FUNCIONE ACTUALMENTE, LA BBDD DEBE TENER UN USUARIO LLAMADO ANONIMO
    @PostMapping("/crear")
    public String crearReview(@ModelAttribute NewReview newReview,
                            @RequestParam Long receptorId) {

        Usuario receptor = usuarioRepository.findById(receptorId)
                .orElseThrow();

        // 👤 AUTOR ANÓNIMO (SIN LOGIN)
        Usuario autor = usuarioRepository.findByUsername("anonimo")
                .orElseThrow();

        newReview.setAutor(autor);
        newReview.setReceptor(receptor);

        reviewRepository.save(newReview);

        return "redirect:/reviews/" + receptorId;
    }
}