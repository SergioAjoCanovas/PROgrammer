package com.programmer.backend.domain; // Mantengo tu paquete actual

import com.fasterxml.jackson.annotation.JsonIgnore; // <-- Evita bucles al enviar el JSON al HTML
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tecnologias")
public class Tecnologia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    @JsonIgnore // ¡Muy importante para que funcione el fetch en JavaScript!
    private CategoriaTecnologia categoria;

    // --- GETTERS Y SETTERS ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public CategoriaTecnologia getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaTecnologia categoria) {
        this.categoria = categoria;
    }
}