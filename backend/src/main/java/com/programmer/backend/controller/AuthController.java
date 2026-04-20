package com.programmer.backend.controller;

import com.programmer.backend.domain.Rol;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.RolRepository;
import com.programmer.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =========================
    // REGISTRO (CORREGIDO)
    // =========================
    @PostMapping("/signup")
    public void registrar(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirm_password") String confirmPassword,
            @RequestParam("id_rol") long idRol,
            HttpSession session,
            HttpServletResponse response
    ) throws IOException {

        // 1. Validaciones: Si falla, redirigimos a la ruta de Spring "/signUp"
        if (!password.equals(confirmPassword)
                || usuarioRepository.existsByUsername(username)
                || usuarioRepository.existsByEmail(email)) {

            // CORRECCIÓN: Usar la ruta del ViewController, no la de Live Server
            response.sendRedirect("/signUp?error=true");
            return;
        }

        Rol rolElegido = rolRepository.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setPassword(passwordEncoder.encode(password));
        nuevoUsuario.setRol(rolElegido);

        usuarioRepository.save(nuevoUsuario);

        // 2. Guardamos en sesión de servidor
        session.setAttribute("usuarioLogueado", nuevoUsuario.getUsername());
        session.setAttribute("rolUsuario", rolElegido.getNombre());

        // 3. Redirección con parámetros para que el JS guarde en LocalStorage
        // CORRECCIÓN: Redirigir a "/ownProfile" (ruta relativa)
        response.sendRedirect("/ownProfile?user=" + username + "&rol=" + rolElegido.getNombre());
    }

    // =========================
    // LOGIN (ESTABA BIEN, SÓLO REPASO)
    // =========================
    @PostMapping("/login")
    public void iniciarSesion(
            @RequestParam("username") String usuarioOEmail,
            @RequestParam("password") String password,
            HttpSession session,
            HttpServletResponse response
    ) throws IOException {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(usuarioOEmail);
        if (usuarioOpt.isEmpty()) {
            usuarioOpt = usuarioRepository.findByEmail(usuarioOEmail);
        }

        if (usuarioOpt.isEmpty() || !passwordEncoder.matches(password, usuarioOpt.get().getPassword())) {
            // CORRECCIÓN: Ruta relativa al ViewController
            response.sendRedirect("/login?error=auth");
            return;
        }

        Usuario usuario = usuarioOpt.get();
        
        session.setAttribute("usuarioLogueado", usuario.getUsername());
        session.setAttribute("rolUsuario", usuario.getRol().getNombre());

        // Redirección con parámetros
        response.sendRedirect("/ownProfile?user=" + usuario.getUsername() + "&rol=" + usuario.getRol().getNombre());
    }
}