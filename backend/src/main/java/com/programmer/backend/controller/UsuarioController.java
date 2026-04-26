package com.programmer.backend.controller;

import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.UsuarioRepository;
import com.programmer.backend.service.RegistroService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class UsuarioController {

    private final RegistroService registroService;
    private final UsuarioRepository usuarioRepository;

    public UsuarioController(RegistroService registroService,
                             UsuarioRepository usuarioRepository) {
        this.registroService = registroService;
        this.usuarioRepository = usuarioRepository;
    }

    // =========================
    // FOTO PERFIL
    // =========================
    @PostMapping("/usuario/uploadFoto")
    public String subirFoto(@RequestParam("foto") MultipartFile foto,
                            HttpSession session) throws IOException {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/login";

        String rutaImagen = registroService.guardarFoto(foto);

        usuario.setFotoPerfil(rutaImagen);
        usuarioRepository.save(usuario);

        session.setAttribute("usuarioLogueado", usuario);

        return "redirect:/ownProfile";
    }

    // =========================
    // USERNAME
    // =========================
    @PostMapping("/usuario/updateUsername")
    @ResponseBody
    public String updateUsername(@RequestParam("username") String username,
                                 HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) return "NOT_LOGGED";
        if (username == null || username.trim().isEmpty()) return "EMPTY";

        if (usuarioRepository.existsByUsername(username)) return "EXISTS";

        usuario.setUsername(username);
        usuarioRepository.save(usuario);

        session.setAttribute("usuarioLogueado", usuario);

        return "OK";
    }

    // =========================
    // REDES
    // =========================
    @PostMapping("/usuario/updateRed")
    @ResponseBody
    public String updateRed(@RequestParam String tipo,
                            @RequestParam String valor,
                            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "NOT_LOGGED";

        String finalValue = (valor == null || valor.isEmpty()) ? null : valor;

        if ("github".equals(tipo)) {
            usuario.setGithub(finalValue);
        }

        if ("linkedin".equals(tipo)) {
            usuario.setLinkedin(finalValue);
        }

        usuarioRepository.save(usuario);
        session.setAttribute("usuarioLogueado", usuario);

        return "OK";
    }

    // =========================
    // CV
    // =========================
    @PostMapping("/usuario/uploadCV")
    @ResponseBody
    public String subirCV(@RequestParam("cv") MultipartFile cv,
                          HttpSession session) throws IOException {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "NOT_LOGGED";
        if (cv.isEmpty()) return "EMPTY";

        String contentType = cv.getContentType();
        String fileName = cv.getOriginalFilename();

        if (contentType == null ||
            fileName == null ||
            !contentType.contains("pdf") ||
            !fileName.toLowerCase().endsWith(".pdf")) {
            return "INVALID_TYPE";
        }

        if (cv.getSize() > 2 * 1024 * 1024) {
            return "TOO_LARGE";
        }

        String rutaCV = registroService.guardarCV(cv);

        usuario.setCurriculum(rutaCV);
        usuarioRepository.save(usuario);

        session.setAttribute("usuarioLogueado", usuario);

        return "OK|" + rutaCV;
    }

    // =========================
    // BORRAR CV
    // =========================
    @PostMapping("/usuario/deleteCV")
    @ResponseBody
    public String eliminarCV(HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "NOT_LOGGED";

        usuario.setCurriculum(null);
        usuarioRepository.save(usuario);

        session.setAttribute("usuarioLogueado", usuario);

        return "OK";
    }

    // =========================
    // BIO
    // =========================
    @PostMapping("/usuario/updateBio")
    @ResponseBody
    public String updateBio(@RequestParam String biografia,
                            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "NOT_LOGGED";

        if (biografia != null) {
            biografia = biografia.trim();
        }

        if (biografia != null && biografia.length() > 500) {
            return "TOO_LONG";
        }

        usuario.setBiografia(biografia);
        usuarioRepository.save(usuario);

        session.setAttribute("usuarioLogueado", usuario);

        return "OK";
    }
}