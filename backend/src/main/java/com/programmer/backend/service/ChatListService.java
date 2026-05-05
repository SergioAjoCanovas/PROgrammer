package com.programmer.backend.service;

import com.programmer.backend.domain.Usuario;
import com.programmer.backend.dto.ChatPreviewDTO;
import com.programmer.backend.repository.MensajeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatListService {

    private final MensajeRepository mensajeRepository;

    public ChatListService(MensajeRepository mensajeRepository) {
        this.mensajeRepository = mensajeRepository;
    }

    public List<ChatPreviewDTO> getChats(Long userId) {

        return mensajeRepository.findUltimosChats(userId)
                .stream()
                .map(m -> {
                    Usuario otro = m.getEmisor().getId().equals(userId)
                            ? m.getReceptor()
                            : m.getEmisor();

                    return new ChatPreviewDTO(
                            otro.getId(),
                            otro.getUsername(),
                            otro.getFotoPerfil()
                    );
                })
                .toList();
    }
}