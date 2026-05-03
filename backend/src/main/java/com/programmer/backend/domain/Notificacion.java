package com.programmer.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Usuario usuario; // El usuario que recibe la notificacion

    private String mensaje;
    private String tipo; // "NUEVO_SEGUIDOR", "NUEVA_OFERTA"
    private boolean leida = false;
    private Date fechaCreacion;
    private String enlace; // Opcional, para redirigir al perfil o a la oferta

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = new Date();
    }
}
