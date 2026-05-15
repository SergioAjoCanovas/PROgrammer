package com.programmer.backend.repository;

import com.programmer.backend.domain.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {
    
    List<Postulacion> findByOfertaId(Long ofertaId);
   
    List<Postulacion> findByDesarrolladorId(Long desarrolladorId);
}