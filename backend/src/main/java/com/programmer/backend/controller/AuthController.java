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
    // REGISTRO
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

        // Validaciones
        if (!password.equals(confirmPassword)
                || usuarioRepository.existsByUsername(username)
                || usuarioRepository.existsByEmail(email)) {

            response.sendRedirect("/signup?error=true");
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

        // 🔥 CORRECCIÓN CLAVE: guardar el objeto completo
        session.setAttribute("usuarioLogueado", nuevoUsuario);
        session.setAttribute("rolUsuario", rolElegido.getNombre());

        response.sendRedirect("/ownProfile?user=" + username + "&rol=" + rolElegido.getNombre());
    }

    // =========================
    // LOGIN
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

        if (usuarioOpt.isEmpty()
                || !passwordEncoder.matches(password, usuarioOpt.get().getPassword())) {

            response.sendRedirect("/login?error=auth");
            return;
        }

        Usuario usuario = usuarioOpt.get();

        // 🔥 CORRECCIÓN CLAVE: guardar el objeto completo
        session.setAttribute("usuarioLogueado", usuario);
        session.setAttribute("rolUsuario", usuario.getRol().getNombre());

        response.sendRedirect("/ownProfile?user=" + usuario.getUsername() + "&rol=" + usuario.getRol().getNombre());
    }
}