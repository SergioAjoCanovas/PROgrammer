package com.programmer.backend.domain;

import java.util.List;

public class ProyectoDTO {

    private String nombre;
    private String descripcion;
    private List<String> imagenes;
    private List<Tecnologia> tecnologias;

    public ProyectoDTO() {}

    public ProyectoDTO(String nombre, String descripcion,
                       List<String> imagenes,
                       List<Tecnologia> tecnologias) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagenes = imagenes;
        this.tecnologias = tecnologias;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }

    public List<Tecnologia> getTecnologias() {
        return tecnologias;
    }

    public void setTecnologias(List<Tecnologia> tecnologias) {
        this.tecnologias = tecnologias;
    }
}