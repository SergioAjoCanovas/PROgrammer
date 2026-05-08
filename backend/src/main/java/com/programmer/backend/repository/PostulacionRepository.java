package com.programmer.backend.repository;

import com.programmer.backend.domain.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {
    
    // Método que ya tenías
    List<Postulacion> findByOfertaId(Long ofertaId);
    
    // NUEVO: Método para buscar a qué ofertas se ha apuntado un desarrollador
    List<Postulacion> findByDesarrolladorId(Long desarrolladorId);
}