package com.programmer.backend.controller;

import com.programmer.backend.domain.Rol;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.RolRepository;
import com.programmer.backend.repository.UsuarioRepository;
import com.programmer.backend.service.RegistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private RegistroService registroService;

    // =========================
    // REGISTRO (Fusionado y Único)
    // =========================
    @PostMapping("/signup")
    public void registrar(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirm_password") String confirmPassword,
            @RequestParam("id_rol") long idRol,
            @RequestParam(value = "foto", required = false) MultipartFile foto, // Foto opcional
            HttpSession session,
            HttpServletResponse response
    ) throws IOException {

        // Validaciones previas
        if (!password.equals(confirmPassword)
                || usuarioRepository.existsByUsername(username)
                || usuarioRepository.existsByEmail(email)) {

            response.sendRedirect("/signup?error=true");
            return;
        }

        // Buscar el rol
        Rol rolElegido = rolRepository.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        // Lógica de la foto
        String rutaFoto = null;
        if (foto != null && !foto.isEmpty()) {
            rutaFoto = registroService.guardarFoto(foto);
        }

        // Crear el usuario y rellenarlo
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setPassword(passwordEncoder.encode(password));
        nuevoUsuario.setRol(rolElegido);
        nuevoUsuario.setFotoPerfil(rutaFoto);

        // Guardar el usuario en BD
        usuarioRepository.save(nuevoUsuario);

        // Guardar en sesión
        session.setAttribute("usuarioLogueado", nuevoUsuario);
        session.setAttribute("rolUsuario", rolElegido.getNombre());

        response.sendRedirect("/main?user=" + username + "&rol=" + rolElegido.getNombre());
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

        session.setAttribute("usuarioLogueado", usuario);
        session.setAttribute("rolUsuario", usuario.getRol().getNombre());

        response.sendRedirect("/main?user=" + usuario.getUsername() + "&rol=" + usuario.getRol().getNombre());
    }
}