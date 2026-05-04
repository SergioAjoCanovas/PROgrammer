package com.programmer.backend.repository;

import com.programmer.backend.domain.ProjectReview;
import com.programmer.backend.domain.Proyecto;
import com.programmer.backend.domain.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectReviewRepository extends JpaRepository<ProjectReview, Long> {


    List<ProjectReview> findByProyecto(Proyecto proyecto);

    Optional<ProjectReview> findByAutorAndProyecto(Usuario autor, Proyecto proyecto);

    List<ProjectReview> findByProyectoOrderByFechaDesc(Proyecto proyecto);


    List<ProjectReview> findByProyectoId(Long proyectoId);

    @Query("SELECT AVG(r.arquitectura) FROM ProjectReview r WHERE r.proyecto.id = :id")
    Double mediaArquitectura(@Param("id") Long id);

    @Query("SELECT AVG(r.limpieza) FROM ProjectReview r WHERE r.proyecto.id = :id")
    Double mediaLimpieza(@Param("id") Long id);

    @Query("SELECT AVG(r.documentacion) FROM ProjectReview r WHERE r.proyecto.id = :id")
    Double mediaDocumentacion(@Param("id") Long id);
}