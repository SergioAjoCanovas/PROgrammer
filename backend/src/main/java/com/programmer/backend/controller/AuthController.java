package com.programmer.backend.controller;

import com.programmer.backend.domain.Rol;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.domain.PerfilDesarrollador;
import com.programmer.backend.domain.PerfilEmpresa;

import com.programmer.backend.repository.RolRepository;
import com.programmer.backend.repository.UsuarioRepository;
import com.programmer.backend.repository.PerfilDesarrolladorRepository;
import com.programmer.backend.repository.PerfilEmpresaRepository;

import com.programmer.backend.service.RegistroService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
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
    private PerfilDesarrolladorRepository perfilDesarrolladorRepository;

    @Autowired
    private PerfilEmpresaRepository perfilEmpresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RegistroService registroService;

    // =========================
    // REGISTRO
    // =========================
    @PostMapping("/signup")
    public RedirectView registrar(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirm_password") String confirmPassword,
            @RequestParam("id_rol") long idRol,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            HttpSession session
    ) throws IOException {

        // Validaciones
        if (!password.equals(confirmPassword)
                || usuarioRepository.existsByUsername(username)
                || usuarioRepository.existsByEmail(email)) {

            return new RedirectView("/signUp?error=true");
        }

        // Rol
        Rol rolElegido = rolRepository.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        // Foto (por defecto usamos el icono estándar)
        String rutaFoto = "/Img/stock/default-profile.svg";
        if (foto != null && !foto.isEmpty()) {
            rutaFoto = registroService.guardarFoto(foto);
        }

        // Usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setPassword(passwordEncoder.encode(password));
        nuevoUsuario.setRol(rolElegido);
        nuevoUsuario.setFotoPerfil(rutaFoto);

        // Guardar usuario
        Usuario usuarioGuardado = registroService.registrarUsuario(nuevoUsuario);

        // =========================
        // 🔥 CREACIÓN DE PERFIL AUTOMÁTICA
        // =========================

        String rolNombre = rolElegido.getNombre();

        if (rolNombre.equals("DESARROLLADOR")) {
            PerfilDesarrollador perfil = new PerfilDesarrollador();
            perfil.setUsuario(usuarioGuardado);
            perfilDesarrolladorRepository.save(perfil);
        }

        if (rolNombre.equals("EMPRESA")) {
            PerfilEmpresa perfil = new PerfilEmpresa();
            perfil.setUsuario(usuarioGuardado);
            perfilEmpresaRepository.save(perfil);
        }

        // 🔥 REGISTRAR EN SPRING SECURITY
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                usuarioGuardado.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rolNombre))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Sesión manual
        session.setAttribute("usuarioLogueado", usuarioGuardado);
        session.setAttribute("rolUsuario", rolNombre);

        return new RedirectView("/main?user=" + java.net.URLEncoder.encode(username, "UTF-8") + "&rol=" + java.net.URLEncoder.encode(rolNombre, "UTF-8"));
    }

    // =========================
    // LOGIN
    // =========================
    @PostMapping("/login")
    public RedirectView iniciarSesion(
            @RequestParam("username") String usuarioOEmail,
            @RequestParam("password") String password,
            HttpSession session
    ) throws IOException {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(usuarioOEmail);

        if (usuarioOpt.isEmpty()) {
            usuarioOpt = usuarioRepository.findByEmail(usuarioOEmail);
        }

        if (usuarioOpt.isEmpty()
                || !passwordEncoder.matches(password, usuarioOpt.get().getPassword())) {

            return new RedirectView("/login?error=auth");
        }

        Usuario usuario = usuarioOpt.get();
        String rolNombre = usuario.getRol().getNombre();

        // 🔥 REGISTRAR EN SPRING SECURITY (Evita que bloqueen tu redirección)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                usuario.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rolNombre))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Sesión manual
        session.setAttribute("usuarioLogueado", usuario);
        session.setAttribute("rolUsuario", rolNombre);

        return new RedirectView("/main?user=" + java.net.URLEncoder.encode(usuario.getUsername(), "UTF-8") + "&rol=" + java.net.URLEncoder.encode(rolNombre, "UTF-8"));
    }
}