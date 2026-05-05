package com.programmer.backend.service;

import com.programmer.backend.domain.Mensaje;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.dto.MensajeDTO;
import com.programmer.backend.dto.ChatPreviewDTO;
import com.programmer.backend.repository.MensajeRepository;
import com.programmer.backend.repository.UsuarioRepository;
import com.programmer.backend.repository.UsuariosSeguimientoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuariosSeguimientoRepository seguimientoRepository;

    public ChatService(MensajeRepository mensajeRepository,
                       UsuarioRepository usuarioRepository,
                       UsuariosSeguimientoRepository seguimientoRepository) {
        this.mensajeRepository = mensajeRepository;
        this.usuarioRepository = usuarioRepository;
        this.seguimientoRepository = seguimientoRepository;
    }

    public List<MensajeDTO> obtenerConversacion(Long userId1, Long userId2) {

        if (!sonAmigos(userId1, userId2)) return List.of();

        return mensajeRepository.findConversacion(userId1, userId2)
                .stream()
                .map(m -> new MensajeDTO(
                        m.getEmisor().getId(),
                        m.getReceptor().getId(),
                        m.getContenido(),
                        m.getFechaEnvio()
                ))
                .toList();
    }

    public void enviarMensaje(Long emisorId, Long receptorId, String contenido) {

        if (!sonAmigos(emisorId, receptorId)) {
            throw new RuntimeException("No sois amigos");
        }

        Usuario emisor = usuarioRepository.findById(emisorId).orElseThrow();
        Usuario receptor = usuarioRepository.findById(receptorId).orElseThrow();

        Mensaje m = new Mensaje();
        m.setEmisor(emisor);
        m.setReceptor(receptor);
        m.setContenido(contenido);

        mensajeRepository.save(m);
    }

    public boolean sonAmigos(Long a, Long b) {

        return seguimientoRepository.existsBySeguidor_IdAndSeguido_Id(a, b)
            && seguimientoRepository.existsBySeguidor_IdAndSeguido_Id(b, a);
    }

    public List<ChatPreviewDTO> obtenerAmigos(Long userId) {

        return usuarioRepository.findAmigos(userId)
                .stream()
                .map(u -> new ChatPreviewDTO(
                        u.getId(),
                        u.getUsername(),
                        u.getFotoPerfil()
                ))
                .toList();
    }
}