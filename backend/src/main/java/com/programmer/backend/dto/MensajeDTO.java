package com.programmer.backend.dto;

import java.time.LocalDateTime;

public class MensajeDTO {
    private Long emisorId;
    private Long receptorId;
    private String contenido;
    private LocalDateTime fechaEnvio;

    public MensajeDTO(Long emisorId, Long receptorId, String contenido, LocalDateTime fechaEnvio) {
        this.emisorId = emisorId;
        this.receptorId = receptorId;
        this.contenido = contenido;
        this.fechaEnvio = fechaEnvio;
    }

    public Long getEmisorId() { return emisorId; }
    public Long getReceptorId() { return receptorId; }
    public String getContenido() { return contenido; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
}