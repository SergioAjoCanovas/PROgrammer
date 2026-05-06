package com.programmer.backend.dto;

import java.time.LocalDateTime;

public class ChatPreviewDTO {

    private Long userId;
    private String username;
    private String fotoPerfil;

    private int noLeidos;
    private String ultimoMensaje;
    private LocalDateTime fechaUltimoMensaje;

    public ChatPreviewDTO(Long userId,
                          String username,
                          String fotoPerfil,
                          int noLeidos,
                          String ultimoMensaje,
                          LocalDateTime fechaUltimoMensaje) {

        this.userId = userId;
        this.username = username;
        this.fotoPerfil = fotoPerfil;
        this.noLeidos = noLeidos;
        this.ultimoMensaje = ultimoMensaje;
        this.fechaUltimoMensaje = fechaUltimoMensaje;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFotoPerfil() { return fotoPerfil; }

    public int getNoLeidos() { return noLeidos; }
    public String getUltimoMensaje() { return ultimoMensaje; }
    public LocalDateTime getFechaUltimoMensaje() { return fechaUltimoMensaje; }
}