package com.programmer.backend.service;

import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String foto = oAuth2User.getAttribute("picture");

        if (!usuarioRepository.existsByEmail(email)) {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setFotoPerfil(foto);

            String tempUsername = email.split("@")[0];
            nuevoUsuario.setUsername(tempUsername);
            
            // Contraseña aleatoria para que tu BBDD no se queje
            nuevoUsuario.setPassword(UUID.randomUUID().toString());

            // LO DEJAMOS EN NULL. Esto le dirá al sistema que es nuevo y tiene que elegir.
            nuevoUsuario.setRol(null); 

            usuarioRepository.save(nuevoUsuario);
        }

        return oAuth2User;
    }
}