package com.programmer.backend.config;

import com.programmer.backend.domain.PerfilDesarrollador;
import com.programmer.backend.domain.Rol;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.PerfilDesarrolladorRepository;
import com.programmer.backend.repository.RolRepository;
import com.programmer.backend.repository.UsuarioRepository;
import com.programmer.backend.service.RegistroService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PerfilDesarrolladorRepository perfilDesarrolladorRepository;

    @Autowired
    private RegistroService registroService;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = token.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        if (name != null) {
            name = name.replace(" ", "").toLowerCase();
        } else {
            name = email.split("@")[0];
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        Usuario usuario;

        if (usuarioOpt.isEmpty()) {
            // Si el usuario no existe en la base de datos, no lo registramos automáticamente.
            // Invalidamos la sesión de Spring Security y redirigimos al login con error.
            request.getSession().invalidate();
            response.sendRedirect("/login?error=not_registered");
            return;
        } else {
            usuario = usuarioOpt.get();
            // Actualizar foto de perfil si no tiene una
            if ((usuario.getFotoPerfil() == null || usuario.getFotoPerfil().isEmpty()) && picture != null) {
                usuario.setFotoPerfil(picture);
                usuarioRepository.save(usuario);
            }
        }

        // Crear la sesión exactamente como lo hace AuthController
        HttpSession session = request.getSession();
        session.setAttribute("usuarioLogueado", usuario);
        session.setAttribute("rolUsuario", usuario.getRol().getNombre());

        response.sendRedirect("/main?user=" + java.net.URLEncoder.encode(usuario.getUsername(), "UTF-8") + "&rol=" + java.net.URLEncoder.encode(usuario.getRol().getNombre(), "UTF-8"));
    }
}