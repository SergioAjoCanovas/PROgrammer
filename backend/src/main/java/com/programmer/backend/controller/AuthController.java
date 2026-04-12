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

    // --- 1. REGISTRO + LOGIN AUTOMÁTICO ---
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
        
        // Si las contraseñas no coinciden o el usuario ya existe, lo devolvemos al formulario de registro en el Live Server
        if (!password.equals(confirmPassword) || usuarioRepository.existsByUsername(username)) {
            response.sendRedirect("http://127.0.0.1:5500/frontend/UI/signUpPage/signUpPage.html?error=true");
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

        // Creamos la sesión
        session.setAttribute("usuarioLogueado", username);
        session.setAttribute("rolUsuario", rolElegido.getNombre());

        // ¡ÉXITO! Lo mandamos a la página principal de tu Live Server
        // OJO: Asegúrate de que esta ruta es la correcta para tu index.html principal
        response.sendRedirect("http://127.0.0.1:5500/frontend/UI/main.html");
    }

    // --- 2. LOGIN TRADICIONAL ---
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

        // Si los datos están mal, lo devolvemos a la página de login de tu Live Server
        if (usuarioOpcional.isEmpty() || !passwordEncoder.matches(password, usuarioOpcional.get().getPassword())) {
            response.sendRedirect("http://127.0.0.1:5500/frontend/UI/loginPage/loginPage.html?error=auth"); 
            return;
        }

        // ¡LOGIN CORRECTO! Creamos la sesión
        Usuario usuario = usuarioOpcional.get();
        session.setAttribute("usuarioLogueado", usuario.getUsername());
        session.setAttribute("rolUsuario", usuario.getRol().getNombre());

        // ¡ÉXITO! Lo mandamos a la página principal de tu Live Server
        response.sendRedirect("http://127.0.0.1:5500/frontend/UI/main.html"); 
    }
}