package com.programmer.backend.dto;

public class ChatPreviewDTO {

    private Long userId;
    private String username;
    private String fotoPerfil;

    public ChatPreviewDTO(Long userId, String username, String fotoPerfil) {
        this.userId = userId;
        this.username = username;
        this.fotoPerfil = fotoPerfil;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFotoPerfil() { return fotoPerfil; }
}