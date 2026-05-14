package com.programmer.backend.controller;

import com.programmer.backend.dto.MensajeDTO;
import com.programmer.backend.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // =========================================
    // CHAT ENTRE 2 USUARIOS
    // =========================================
    @GetMapping("/{user1}/{user2}")
    public List<MensajeDTO> getChat(@PathVariable Long user1,
                                    @PathVariable Long user2) {
        return chatService.obtenerConversacion(user1, user2);
    }

    // =========================================
    // ENVIAR MENSAJE
    // =========================================
    @PostMapping("/send")
    public void sendMessage(@RequestParam Long emisorId,
                            @RequestParam Long receptorId,
                            @RequestParam String contenido) {

        chatService.enviarMensaje(emisorId, receptorId, contenido);
    }

    // =========================================
    // MARCAR COMO LEÍDOS
    // =========================================
    @PostMapping("/marcar-leidos")
    public void marcarLeidos(@RequestParam Long userId,
                            @RequestParam Long otroId) {
        chatService.marcarChatComoLeido(userId, otroId);
    }

    // =========================================
    // VACIAR CHAT
    // =========================================
    @PostMapping("/vaciar")
    public void vaciarChat(@RequestParam Long userId,
                        @RequestParam Long otroId) {
        chatService.vaciarChat(userId, otroId);
    }

    @GetMapping("/unread-count")
    public int getUnreadCount(@RequestParam Long userId) {
        return chatService.contarTodosNoLeidos(userId);
    }
}