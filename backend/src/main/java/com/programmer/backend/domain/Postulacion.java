package com.programmer.backend.domain;

import jakarta.persistence.*; // Si usas Spring Boot 2, cambia 'jakarta' por 'javax'
import java.util.Date;

@Entity
@Table(name = "postulaciones")
public class Postulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "oferta_id")
    private OfertaEmpleo oferta;

    @ManyToOne
    @JoinColumn(name = "desarrollador_id")
    private Usuario desarrollador;

    // Supongo que tienes una clase Proyecto creada. Si no, avísame.
    @ManyToOne
    @JoinColumn(name = "proyecto_vinculado_id")
    private Proyecto proyectoVinculado;

    @Column(name = "mensaje_adjunto", length = 500)
    private String mensajeAdjunto;

    @Column(name = "fecha_postulacion", insertable = false, updatable = false)
    private Date fechaPostulacion;

    // --- GETTERS Y SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OfertaEmpleo getOferta() {
        return oferta;
    }

    public void setOferta(OfertaEmpleo oferta) {
        this.oferta = oferta;
    }

    public Usuario getDesarrollador() {
        return desarrollador;
    }

    public void setDesarrollador(Usuario desarrollador) {
        this.desarrollador = desarrollador;
    }

    public Proyecto getProyectoVinculado() {
        return proyectoVinculado;
    }

    public void setProyectoVinculado(Proyecto proyectoVinculado) {
        this.proyectoVinculado = proyectoVinculado;
    }

    public String getMensajeAdjunto() {
        return mensajeAdjunto;
    }

    public void setMensajeAdjunto(String mensajeAdjunto) {
        this.mensajeAdjunto = mensajeAdjunto;
    }

    public Date getFechaPostulacion() {
        return fechaPostulacion;
    }

    public void setFechaPostulacion(Date fechaPostulacion) {
        this.fechaPostulacion = fechaPostulacion;
    }
}