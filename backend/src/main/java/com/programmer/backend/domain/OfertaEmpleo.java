package com.programmer.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "ofertas_empleo")
public class OfertaEmpleo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Aquí iría el empresa_id. De momento lo dejamos preparado.
    // private Long empresa_id;

    private String titulo;
    
    private String descripcion;
    
    private Boolean activa = true;

    // ¡MAGIA! Esta etiqueta rellena tu tabla intermedia 'oferta_tecnologia'
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

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }

    public List<Tecnologia> getTecnologias() { return tecnologias; }
    public void setTecnologias(List<Tecnologia> tecnologias) { this.tecnologias = tecnologias; }
}