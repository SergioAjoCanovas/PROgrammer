package com.programmer.backend.service;

import com.programmer.backend.domain.Mensaje;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.dto.ChatPreviewDTO;
import com.programmer.backend.repository.MensajeRepository;
import com.programmer.backend.repository.UsuarioRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatListService {

    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;

    public ChatListService(MensajeRepository mensajeRepository,
                           UsuarioRepository usuarioRepository) {
        this.mensajeRepository = mensajeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ChatPreviewDTO> getChats(Long userId) {

        return usuarioRepository.findAmigos(userId)
                .stream()
                .map(otro -> {

                    List<Mensaje> mensajes =
                            mensajeRepository.findUltimoMensajeConversacion(userId, otro.getId());

                    Mensaje ultimo = mensajes.isEmpty() ? null : mensajes.get(0);

                    int noLeidos = mensajeRepository.countNoLeidos(userId, otro.getId());

                    return new ChatPreviewDTO(
                            otro.getId(),
                            otro.getUsername(),
                            otro.getFotoPerfil(),
                            noLeidos,
                            ultimo != null ? ultimo.getContenido() : "",
                            ultimo != null ? ultimo.getFechaEnvio() : null
                    );
                })
                .sorted((a, b) -> {

                    if (a.getFechaUltimoMensaje() == null) return 1;
                    if (b.getFechaUltimoMensaje() == null) return -1;

                    return b.getFechaUltimoMensaje()
                            .compareTo(a.getFechaUltimoMensaje());
                })
                .toList();
    }
}