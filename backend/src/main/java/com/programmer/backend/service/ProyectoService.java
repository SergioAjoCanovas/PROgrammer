package com.programmer.backend.service;

import com.programmer.backend.domain.Proyecto;
import com.programmer.backend.domain.Tecnologia;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.ProyectoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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

        if (p == null) {
            return null; // protección extra (no debería pasar, pero evita crashes)
        }

        return new ProyectoDTO(
                p.getId(),
                p.getTitulo(),
                p.getDescripcion(),
                mapImagenes(p),
                p.getTecnologias() != null ? p.getTecnologias() : List.of()
        );
    }

    /**
     * Convierte foto_1..foto_4 → List<String> (sin nulls)
     */
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