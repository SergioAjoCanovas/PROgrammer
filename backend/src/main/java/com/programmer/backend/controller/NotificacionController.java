package com.programmer.backend.controller;

import com.programmer.backend.domain.Notificacion;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.NotificacionRepository;
import com.programmer.backend.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<?> getNotificaciones(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return ResponseEntity.status(401).body("No logueado");
        }
        
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioOrderByFechaCreacionDesc(usuario);
        long noLeidas = notificacionRepository.countByUsuarioAndLeidaFalse(usuario);
        
        return ResponseEntity.ok(Map.of(
            "notificaciones", notificaciones,
            "noLeidas", noLeidas
        ));
    }

    @PostMapping("/marcar-leidas")
    @Transactional
    public ResponseEntity<?> marcarComoLeidas(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return ResponseEntity.status(401).build();

        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioOrderByFechaCreacionDesc(usuario);
        for (Notificacion n : notificaciones) {
            if (!n.isLeida()) {
                n.setLeida(true);
            }
        }
        notificacionRepository.saveAll(notificaciones);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<?> borrarTodas(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return ResponseEntity.status(401).build();

        notificacionRepository.deleteByUsuario(usuario);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> borrarUna(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return ResponseEntity.status(401).build();

        notificacionRepository.findById(id).ifPresent(notificacion -> {
            // Verificar que la notificacion pertenece al usuario logueado
            if (notificacion.getUsuario().getId().equals(usuario.getId())) {
                notificacionRepository.delete(notificacion);
            }
        });
        return ResponseEntity.ok().build();
    }

    @PostMapping("/toggle-silence")
    @Transactional
    public ResponseEntity<?> toggleSilence(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return ResponseEntity.status(401).build();

        Usuario u = usuarioRepository.findById(usuario.getId()).orElse(null);
        if (u != null) {
            u.setSilenciarNotificaciones(!u.isSilenciarNotificaciones());
            usuarioRepository.save(u);
            session.setAttribute("usuarioLogueado", u); // actualizamos sesion
            return ResponseEntity.ok(Map.of("silenciado", u.isSilenciarNotificaciones()));
        }
        return ResponseEntity.badRequest().build();
    }
}
