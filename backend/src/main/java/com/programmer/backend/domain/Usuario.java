package com.programmer.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @Column(name = "github")
    private String github;

    @Column(name = "linkedin")
    private String linkedin;

    @Column(name = "curriculum")
    private String curriculum;
    
    @Lob
    @Column(name = "biografia", columnDefinition = "TEXT")
    private String biografia;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "estado", length = 20)
    private String estado = "LOGUEADO";

    @Column(name = "silenciar_notificaciones", columnDefinition = "boolean default false")
    private Boolean silenciarNotificaciones = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol")
    private Rol rol;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuario_seguidores",
        joinColumns = @JoinColumn(name = "seguido_id"),
        inverseJoinColumns = @JoinColumn(name = "seguidor_id")
    )
    private java.util.Set<Usuario> seguidores = new java.util.HashSet<>();

    @ManyToMany(mappedBy = "seguidores", fetch = FetchType.LAZY)
    private java.util.Set<Usuario> siguiendo = new java.util.HashSet<>();

    public boolean isSilenciarNotificaciones() {
        return this.silenciarNotificaciones != null ? this.silenciarNotificaciones : false;
    }

    public void setSilenciarNotificaciones(Boolean silenciarNotificaciones) {
        this.silenciarNotificaciones = silenciarNotificaciones;
    }

    public int getFollowersCount() {
        return this.seguidores != null ? this.seguidores.size() : 0;
    }

    public String getFollowersCountFormatted() {
        int count = this.seguidores != null ? this.seguidores.size() : 0;
        if (count >= 1000000) {
            return String.format(java.util.Locale.US, "%.1f Mill", count / 1000000.0);
        } else if (count >= 1000) {
            return String.format(java.util.Locale.US, "%.1f K", count / 1000.0);
        }
        return String.valueOf(count);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id != null && id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}