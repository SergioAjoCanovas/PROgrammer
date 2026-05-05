package com.programmer.backend.controller;

import com.programmer.backend.dto.ChatPreviewDTO;
import com.programmer.backend.service.ChatListService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat/list")
public class ChatListController {

    private final ChatListService chatListService;

    public ChatListController(ChatListService chatListService) {
        this.chatListService = chatListService;
    }

    @GetMapping("/{userId}")
    public List<ChatPreviewDTO> getChats(@PathVariable Long userId) {
        return chatListService.getChats(userId);
    }
}