package com.programmer.backend.controller;

import com.programmer.backend.domain.NewReview;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.NewReviewRepository;
import com.programmer.backend.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class NewReviewController {

    private final NewReviewRepository reviewRepository;
    private final UsuarioRepository usuarioRepository;
    private final com.programmer.backend.service.NotificacionService notificacionService;

    public NewReviewController(NewReviewRepository reviewRepository,
                                UsuarioRepository usuarioRepository,
                                com.programmer.backend.service.NotificacionService notificacionService) {
        this.reviewRepository = reviewRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionService = notificacionService;
    }

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

        // 4. Validar datos mínimos (Permitimos 0 estrellas, pero el comentario es obligatorio)
        if (newReview.getComentario() == null || newReview.getComentario().trim().isEmpty()) {
            return "redirect:/profileView/" + receptorId + "?error=invalid";
        }

        // 5. Asignar y guardar
        newReview.setAutor(autor);
        newReview.setReceptor(receptor);
        reviewRepository.save(newReview);

        // 6. Enviar notificación
        String mensajeNotif = autor.getUsername() + " te ha dejado una reseña";
        if (newReview.getRating() > 0) {
            mensajeNotif += " de " + newReview.getRating() + " estrellas";
        }
        mensajeNotif += ": " + newReview.getComentario();
        
        notificacionService.enviarNotificacion(receptor, mensajeNotif, "NUEVA_RESEÑA_PERFIL", "/profileView/" + receptor.getId());

        return "redirect:/profileView/" + receptorId + "?success=created";
    }

    @PostMapping("/editar")
    public String editarReview(@RequestParam Long reviewId,
                               @RequestParam int rating,
                               @RequestParam String comentario,
                               HttpSession session) {
        Usuario autor = (Usuario) session.getAttribute("usuarioLogueado");
        if (autor == null) return "redirect:/";

        // Buscar la reseña original
        NewReview review = reviewRepository.findById(reviewId).orElse(null);
        
        if (review != null && review.getAutor().getId().equals(autor.getId())) {
            // Validar y actualizar campos
            if (rating >= 0 && rating <= 5 && comentario != null && !comentario.trim().isEmpty()) {
                
                review.setRating(rating);
                review.setComentario(comentario);
                reviewRepository.save(review);
                
                return "redirect:/profileView/" + review.getReceptor().getId() + "?success=updated";
            }
        }
        
        if (review != null) {
            return "redirect:/profileView/" + review.getReceptor().getId();
        }
        return "redirect:/";
    }

    @PostMapping("/eliminar")
    public String eliminarReview(@RequestParam Long reviewId, HttpSession session) {
        Usuario autor = (Usuario) session.getAttribute("usuarioLogueado");
        if (autor == null) return "redirect:/";
        
        NewReview review = reviewRepository.findById(reviewId).orElse(null);
        if (review != null) {
            boolean isAdmin = autor.getRol() != null && "ADMIN".equalsIgnoreCase(autor.getRol().getNombre());
            if (review.getAutor().getId().equals(autor.getId()) || isAdmin) {
                Long receptorId = review.getReceptor().getId();
                reviewRepository.delete(review);
                return "redirect:/profileView/" + receptorId + "?success=deleted";
            }
        }
        return "redirect:/";
    }
}
