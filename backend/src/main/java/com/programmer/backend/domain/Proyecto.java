package com.programmer.backend.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "proyectos")
@Data
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // AUTOR
    @ManyToOne
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    // CATEGORÍA (sin entidad)
    @Column(name = "categoria_id")
    private Long categoriaId;

    // INFO
    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "repo_url")
    private String repoUrl;

    @Column(name = "foto_1")
    private String foto1;
    
    @Column(name = "foto_2")
    private String foto2;
    
    @Column(name = "foto_3")
    private String foto3;
    
    @Column(name = "foto_4")
    private String foto4;

    @Column(name = "esta_validado")
    private Boolean estaValidado;

    
    @ManyToMany
    @JoinTable(
        name = "proyecto_tecnologias", 
        joinColumns = @JoinColumn(name = "proyecto_id"),
        inverseJoinColumns = @JoinColumn(name = "tecnologia_id")
    )
    private List<Tecnologia> tecnologias;

    // BORRADO
    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectReview> reviews;
}