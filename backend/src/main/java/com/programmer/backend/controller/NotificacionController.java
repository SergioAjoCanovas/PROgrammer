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

    @Autowired
    private com.programmer.backend.service.ChatService chatService;

    @GetMapping
    public ResponseEntity<?> getNotificaciones(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return ResponseEntity.status(401).body("No logueado");
        }
        
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioOrderByFechaCreacionDesc(usuario);
        long noLeidas = notificacionRepository.countByUsuarioAndLeidaFalse(usuario);
        int noLeidasChat = chatService.contarTodosNoLeidos(usuario.getId());
        
        return ResponseEntity.ok(Map.of(
            "notificaciones", notificaciones,
            "noLeidas", noLeidas,
            "noLeidasChat", noLeidasChat
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

    @PostMapping("/{id}/leer")
    @Transactional
    public ResponseEntity<?> marcarLeida(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return ResponseEntity.status(401).build();

        notificacionRepository.findById(id).ifPresent(n -> {
            if (n.getUsuario().getId().equals(usuario.getId())) {
                n.setLeida(true);
                notificacionRepository.save(n);
            }
        });
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tipo/{tipo}")
    @Transactional
    public ResponseEntity<?> borrarPorTipo(@PathVariable String tipo, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return ResponseEntity.status(401).build();

        if ("RESENAS".equals(tipo)) {
            notificacionRepository.deleteByUsuarioAndTipoIn(usuario, java.util.List.of("NUEVA_RESEÑA_PERFIL", "NUEVA_RESEÑA_PROYECTO"));
        } else if ("OTRAS".equals(tipo)) {
            notificacionRepository.deleteByUsuarioAndTipoNotIn(usuario, java.util.List.of("NUEVO_PARCHE", "NUEVA_RESEÑA_PERFIL", "NUEVA_RESEÑA_PROYECTO"));
        } else {
            notificacionRepository.deleteByUsuarioAndTipo(usuario, tipo);
        }
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
    @PostMapping("/broadcast-patch")
    @Transactional
    public ResponseEntity<?> broadcastPatch(@RequestParam("mensaje") String mensaje, HttpSession session) {
        Usuario sessionUser = (Usuario) session.getAttribute("usuarioLogueado");
        if (sessionUser == null) {
            return ResponseEntity.status(401).body("No logueado");
        }

        Usuario admin = usuarioRepository.findById(sessionUser.getId()).orElse(null);

        if (admin == null || admin.getRol() == null || !("ADMIN".equalsIgnoreCase(admin.getRol().getNombre()) || "1".equals(admin.getRol().getNombre()))) {
            return ResponseEntity.status(403).body("Acceso denegado");
        }

        if (mensaje == null || mensaje.trim().isEmpty() || mensaje.length() > 2000) {
            return ResponseEntity.badRequest().body("Mensaje inválido o demasiado largo.");
        }

        List<Usuario> todosLosUsuarios = usuarioRepository.findAll();
        for (Usuario u : todosLosUsuarios) {
            // Se envía a TODOS los usuarios, sin importar si tienen las notificaciones silenciadas
            Notificacion n = new Notificacion();
            n.setUsuario(u);
            n.setMensaje(mensaje);
            n.setTipo("NUEVO_PARCHE");
            n.setEnlace("#"); // El frontend manejará el clic para mostrar el modal
            n.setFechaCreacion(new java.util.Date()); // ¡CRUCIAL PARA QUE THYMELEAF NO PETE AL FORMATEAR LA FECHA!
            notificacionRepository.save(n);
        }

        return ResponseEntity.ok("OK");
    }
}

