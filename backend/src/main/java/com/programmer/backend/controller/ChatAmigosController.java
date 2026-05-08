package com.programmer.backend.controller;

import com.programmer.backend.dto.ChatPreviewDTO;
import com.programmer.backend.service.ChatListService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat/amigos")
public class ChatAmigosController {

    private final ChatListService chatListService;

    public ChatAmigosController(ChatListService chatListService) {
        this.chatListService = chatListService;
    }

    @GetMapping("/{userId}")
    public List<ChatPreviewDTO> getAmigos(@PathVariable Long userId) {
        return chatListService.getChats(userId);
    }
}