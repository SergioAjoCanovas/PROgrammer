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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    // --- 1. REGISTRO ---
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
        
        if (!password.equals(confirmPassword) || usuarioRepository.existsByUsername(username)) {
            response.sendRedirect("http://127.0.0.1:5500/UI/signUpPage/signUpPage.html?error=true");
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

        // Lógica para mostrar "User" en lugar de "ADMIN"
        String nombreRolAMostrar = rolElegido.getNombre();
        if ("ADMIN".equalsIgnoreCase(nombreRolAMostrar)) {
            nombreRolAMostrar = "User";
        }

        String redirectUrl = String.format(
            "http://127.0.0.1:5500/backend/src/main/resources/templates/UI/main.html?user=%s&rol=%s",
            nuevoUsuario.getUsername(), 
            nombreRolAMostrar
        );
        response.sendRedirect(redirectUrl);
    }

    // --- 2. LOGIN ---
    @PostMapping("/login")
    public void iniciarSesion(
            @RequestParam("username") String usuarioOEmail,
            @RequestParam("password") String password,
            HttpSession session,
            HttpServletResponse response
    ) throws IOException {
        
        var usuarioOpcional = usuarioRepository.findByUsername(usuarioOEmail);
        if (usuarioOpcional.isEmpty()) {
            usuarioOpcional = usuarioRepository.findByEmail(usuarioOEmail);
        }

        if (usuarioOpcional.isEmpty() || !passwordEncoder.matches(password, usuarioOpcional.get().getPassword())) {
            response.sendRedirect("http://127.0.0.1:5500/UI/loginPage/loginPage.html?error=auth"); 
            return;
        }

        Usuario usuario = usuarioOpcional.get();
        
        // Lógica para mostrar "User" en lugar de "ADMIN"
        String nombreRolAMostrar = usuario.getRol().getNombre();
        if ("ADMIN".equalsIgnoreCase(nombreRolAMostrar)) {
            nombreRolAMostrar = "User";
        }

        String redirectUrl = String.format(
            "http://127.0.0.1:5500/backend/src/main/resources/templates/UI/main.html?user=%s&rol=%s",
            usuario.getUsername(), 
            nombreRolAMostrar
        );
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/session")
    public Map<String, String> getSession(HttpSession session) {
        String username = (String) session.getAttribute("usuarioLogueado");
        String rol = (String) session.getAttribute("rolUsuario");
        
        if (username == null) {
            return Collections.singletonMap("auth", "false");
        }
        
        Map<String, String> data = new HashMap<>();
        data.put("auth", "true");
        data.put("username", username);
        data.put("rol", rol);
        return data;
    }
}