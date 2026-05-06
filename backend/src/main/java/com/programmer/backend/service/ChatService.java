package com.programmer.backend.service;

import com.programmer.backend.domain.Mensaje;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.dto.MensajeDTO;
import com.programmer.backend.dto.ChatPreviewDTO;
import com.programmer.backend.repository.MensajeRepository;
import com.programmer.backend.repository.UsuarioRepository;
import com.programmer.backend.repository.UsuariosSeguimientoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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
            throw new RuntimeException("No puedes enviar mensajes si no sois amigos");
        }

        Mensaje mensaje = guardarMensaje(emisorId, receptorId, contenido);

        MensajeDTO dto = new MensajeDTO(
                mensaje.getEmisor().getId(),
                mensaje.getReceptor().getId(),
                mensaje.getContenido(),
                mensaje.getFechaEnvio()
        );

        messagingTemplate.convertAndSend(
                "/topic/messages/" + receptorId,
                dto
        );

        messagingTemplate.convertAndSend(
                "/topic/messages/" + emisorId,
                dto
        );
    }

    @Transactional
    public void marcarChatComoLeido(Long userId, Long otroId) {
        mensajeRepository.marcarComoLeidos(userId, otroId);
    }

    @Transactional
    public void vaciarChat(Long userId, Long otroId) {
        mensajeRepository.vaciarChat(userId, otroId);
    }

    private Mensaje guardarMensaje(Long emisorId, Long receptorId, String contenido) {

        Usuario emisor = usuarioRepository.findById(emisorId)
                .orElseThrow(() -> new RuntimeException("Emisor no encontrado"));

        Usuario receptor = usuarioRepository.findById(receptorId)
                .orElseThrow(() -> new RuntimeException("Receptor no encontrado"));

        Mensaje mensaje = new Mensaje();
        mensaje.setEmisor(emisor);
        mensaje.setReceptor(receptor);
        mensaje.setContenido(contenido);
        mensaje.setFechaEnvio(java.time.LocalDateTime.now());

        return mensajeRepository.save(mensaje);
    }

    public boolean sonAmigos(Long a, Long b) {

        return seguimientoRepository.existsBySeguidor_IdAndSeguido_Id(a, b)
                && seguimientoRepository.existsBySeguidor_IdAndSeguido_Id(b, a);
    }
}