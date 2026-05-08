package com.programmer.backend.domain;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "ofertas_empleo")
public class OfertaEmpleo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empresa_id")
    private Usuario empresa;

    private String titulo;
    private String descripcion;
    
    // --- NUEVOS CAMPOS ---
    @Column(columnDefinition = "TEXT")
    private String requisitos;

    @Column(columnDefinition = "TEXT")
    private String ofrecemos;

    @Column(name = "rango_salarial")
    private String rangoSalarial;

    private Boolean activa = true;

    @ManyToMany
    @JoinTable(
        name = "oferta_tecnologia",
        joinColumns = @JoinColumn(name = "oferta_id"),
        inverseJoinColumns = @JoinColumn(name = "tecnologia_id")
    )
    private List<Tecnologia> tecnologias;

    // --- GETTERS Y SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getEmpresa() { return empresa; }
    public void setEmpresa(Usuario empresa) { this.empresa = empresa; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getRequisitos() { return requisitos; }
    public void setRequisitos(String requisitos) { this.requisitos = requisitos; }

    public String getOfrecemos() { return ofrecemos; }
    public void setOfrecemos(String ofrecemos) { this.ofrecemos = ofrecemos; }

    public String getRangoSalarial() { return rangoSalarial; }
    public void setRangoSalarial(String rangoSalarial) { this.rangoSalarial = rangoSalarial; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }

    public List<Tecnologia> getTecnologias() { return tecnologias; }
    public void setTecnologias(List<Tecnologia> tecnologias) { this.tecnologias = tecnologias; }
}