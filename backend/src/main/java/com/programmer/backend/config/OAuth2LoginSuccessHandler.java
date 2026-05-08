package com.programmer.backend.config;

import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.UsuarioRepository;
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

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    @Lazy 
    private PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = token.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String picture = oAuth2User.getAttribute("picture");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            if ((usuario.getFotoPerfil() == null || usuario.getFotoPerfil().isEmpty()) && picture != null) {
                usuario.setFotoPerfil(picture);
                usuarioRepository.save(usuario);
            }

            HttpSession session = request.getSession();
            session.setAttribute("usuarioLogueado", usuario);

            // SI EL ROL ES NULL -> Es nuevo y no ha elegido
            if (usuario.getRol() == null) {
                session.setAttribute("rolUsuario", "PENDIENTE");
                response.sendRedirect("/elegir-rol");
                return;
            }

            // SI YA TIENE ROL -> Directo para adentro
            session.setAttribute("rolUsuario", usuario.getRol().getNombre());
            response.sendRedirect("/main?user=" + java.net.URLEncoder.encode(usuario.getUsername(), "UTF-8") 
                    + "&rol=" + java.net.URLEncoder.encode(usuario.getRol().getNombre(), "UTF-8"));
        } else {
            request.getSession().invalidate();
            response.sendRedirect("/login?error=not_registered");
        }
    }
}