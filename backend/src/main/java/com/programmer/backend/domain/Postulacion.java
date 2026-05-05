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

    // --- NUEVA COLUMNA AÑADIDA PARA EL CV ---
    @Column(name = "cv_adjunto")
    private String cvAdjunto;

    @Column(name = "fecha_postulacion", insertable = false, updatable = false)
    private Date fechaPostulacion;

    @Column(name = "estado", length = 20)
    private String estado = "PENDIENTE";

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

    public String getCvAdjunto() {
        return cvAdjunto;
    }

    public void setCvAdjunto(String cvAdjunto) {
        this.cvAdjunto = cvAdjunto;
    }

    public Date getFechaPostulacion() {
        return fechaPostulacion;
    }

    public void setFechaPostulacion(Date fechaPostulacion) {
        this.fechaPostulacion = fechaPostulacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}