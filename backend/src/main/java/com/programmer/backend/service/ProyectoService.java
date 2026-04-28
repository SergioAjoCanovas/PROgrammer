package com.programmer.backend.service;

import com.programmer.backend.domain.Proyecto;
import com.programmer.backend.domain.Tecnologia;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.ProyectoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;

    public ProyectoService(ProyectoRepository proyectoRepository) {
        this.proyectoRepository = proyectoRepository;
    }

    /**
     * DTO interno (NO es tabla, solo objeto para la vista)
     */
    public static class ProyectoDTO {

        private Long id;
        private String nombre;
        private String descripcion;
        private List<String> imagenes;
        private List<Tecnologia> tecnologias;

        public ProyectoDTO(Long id,
                           String nombre,
                           String descripcion,
                           List<String> imagenes,
                           List<Tecnologia> tecnologias) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.imagenes = imagenes;
            this.tecnologias = tecnologias;
        }

        public Long getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public List<String> getImagenes() {
            return imagenes;
        }

        public List<Tecnologia> getTecnologias() {
            return tecnologias;
        }
    }

    /**
     * Devuelve los 2 proyectos más recientes YA adaptados para la vista
     */
    public List<ProyectoDTO> obtenerUltimosProyectos(Usuario usuario) {

        return proyectoRepository
                .findTop2ByAutorOrderByIdDesc(usuario)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Convierte entidad → DTO
     */
    private ProyectoDTO mapToDTO(Proyecto p) {
        return new ProyectoDTO(
                p.getId(),              // 🔥 AÑADIDO
                p.getTitulo(),
                p.getDescripcion(),
                mapImagenes(p),
                p.getTecnologias()
        );
    }

    /**
     * Convierte foto_1..foto_4 → List<String>
     */
    private List<String> mapImagenes(Proyecto p) {
        return List.of(
                        p.getFoto1(),
                        p.getFoto2(),
                        p.getFoto3(),
                        p.getFoto4()
                )
                .stream()
                .filter(java.util.Objects::nonNull)
                .filter(f -> !f.isBlank())
                .toList();
    }
}