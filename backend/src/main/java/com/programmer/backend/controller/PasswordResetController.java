package com.programmer.backend.controller;

import com.programmer.backend.domain.PasswordResetToken;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.PasswordResetTokenRepository;
import com.programmer.backend.repository.UsuarioRepository;
import com.programmer.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.UUID;

@Controller
public class PasswordResetController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Vista de "Olvidé mi contraseña"
    @GetMapping("/forgotPassword")
    public String showForgotPassword() {
        return "UI/loginPage/forgotPassword";
    }

    // Procesar la solicitud de reseteo
    @PostMapping("/forgotPassword")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No hemos encontrado ninguna cuenta con ese correo.");
            return "redirect:/forgotPassword";
        }

        Usuario usuario = usuarioOpt.get();

        
        Optional<PasswordResetToken> existingToken = tokenRepository.findByUsuario(usuario);
        existingToken.ifPresent(token -> tokenRepository.delete(token));

        
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, usuario);
        tokenRepository.save(resetToken);

        // Enviamos el email (en consola si no hay SMTP configurado)
        String resetUrl = "http://localhost:8080/resetPassword?token=" + token;
        String subject = "Restablecer tu contraseña en PROgrammer";
        String message = "Hola " + usuario.getUsername() + ",\n\n" +
                         "Has solicitado restablecer tu contraseña. Haz clic en el siguiente enlace para crear una nueva:\n\n" +
                         resetUrl + "\n\n" +
                         "Si no has sido tú, ignora este mensaje.\n\n" +
                         "Saludos,\nEl equipo de PROgrammer";

        emailService.sendSimpleMessage(usuario.getEmail(), subject, message);

        redirectAttributes.addFlashAttribute("success", "Te hemos enviado un correo con las instrucciones.");
        return "redirect:/forgotPassword";
    }

    // Vista para poner nueva contraseña
    @GetMapping("/resetPassword")
    public String showResetPassword(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            redirectAttributes.addFlashAttribute("error", "El enlace de recuperación es inválido o ha caducado.");
            return "redirect:/login";
        }

        model.addAttribute("token", token);
        return "UI/loginPage/resetPassword";
    }

    // Procesar el cambio de contraseña
    @PostMapping("/resetPassword")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       RedirectAttributes redirectAttributes) {
        
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            redirectAttributes.addFlashAttribute("error", "El enlace de recuperación es inválido o ha caducado.");
            return "redirect:/login";
        }

        Usuario usuario = tokenOpt.get().getUsuario();
        usuario.setPassword(passwordEncoder.encode(password));
        usuarioRepository.save(usuario);

        // Borramos el token para que no se pueda volver a usar
        tokenRepository.delete(tokenOpt.get());

        redirectAttributes.addFlashAttribute("success", "Tu contraseña ha sido cambiada exitosamente. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }
}
