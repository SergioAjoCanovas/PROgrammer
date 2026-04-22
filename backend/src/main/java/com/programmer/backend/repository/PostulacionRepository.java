package com.programmer.backend.repository;

import com.programmer.backend.domain.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {
    
    // Este es el método mágico que usa el Controlador para buscar los candidatos de una oferta
    List<Postulacion> findByOfertaId(Long ofertaId);
}