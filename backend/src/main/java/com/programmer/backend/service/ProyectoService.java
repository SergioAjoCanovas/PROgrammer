package com.programmer.backend.service;

import com.programmer.backend.domain.Proyecto;
import com.programmer.backend.domain.Tecnologia;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;

    public ProyectoService(ProyectoRepository proyectoRepository) {
        this.proyectoRepository = proyectoRepository;
    }

    // ==========================================
    // BORRADO REAL (FIX DEFINITIVO)
    // ==========================================
    @Transactional
    public void eliminarProyecto(Proyecto proyecto) {
    
        System.out.println("TX ACTIVE: " + 
            org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()
        );
    
        Long id = proyecto.getId();
    
        System.out.println("INICIO BORRADO PROYECTO ID: " + id);
    
        int deleted = proyectoRepository.deleteTecnologiasByProyectoId(id);
        System.out.println("RELACIONES BORRADAS: " + deleted);
    
        try {
            proyectoRepository.deleteById(id);
            System.out.println("PROYECTO BORRADO");
        } catch (Exception e) {
            System.out.println("ERROR BORRANDO PROYECTO:");
            e.printStackTrace();
        }
    }

    // ==========================================
    // DTO
    // ==========================================
    public static class ProyectoDTO {

        private Long id;
        private String nombre;
        private String descripcion;
        private List<String> imagenes;
        private List<Tecnologia> tecnologias;
        private Boolean estaValidado;

        public ProyectoDTO(Long id,
                           String nombre,
                           String descripcion,
                           List<String> imagenes,
                           List<Tecnologia> tecnologias,
                           Boolean estaValidado) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.imagenes = imagenes;
            this.tecnologias = tecnologias;
            this.estaValidado = estaValidado;
        }

        public Long getId() { return id; }
        public String getNombre() { return nombre; }
        public String getDescripcion() { return descripcion; }
        public List<String> getImagenes() { return imagenes; }
        public List<Tecnologia> getTecnologias() { return tecnologias; }
        public Boolean getEstaValidado() { return estaValidado; }
    }

    public List<ProyectoDTO> obtenerUltimosProyectos(Usuario usuario) {
        return proyectoRepository
                .findTop2ByAutorOrderByIdDesc(usuario)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private ProyectoDTO mapToDTO(Proyecto p) {
        if (p == null) return null;

        return new ProyectoDTO(
                p.getId(),
                p.getTitulo(),
                p.getDescripcion(),
                mapImagenes(p),
                p.getTecnologias() != null ? p.getTecnologias() : List.of(),
                p.getEstaValidado()
        );
    }

    private List<String> mapImagenes(Proyecto p) {
        if (p == null) return List.of();

        return Stream.of(
                p.getFoto1(),
                p.getFoto2(),
                p.getFoto3(),
                p.getFoto4()
        )
        .filter(Objects::nonNull)
        .filter(f -> !f.isBlank())
        .toList();
    }
}