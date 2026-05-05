package com.programmer.backend.controller;

import com.programmer.backend.dto.ChatPreviewDTO;
import com.programmer.backend.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat/amigos")
public class ChatAmigosController {

    private final ChatService chatService;

    public ChatAmigosController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{userId}")
    public List<ChatPreviewDTO> getAmigos(@PathVariable Long userId) {
        return chatService.obtenerAmigos(userId);
    }
}