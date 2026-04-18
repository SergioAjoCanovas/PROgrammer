package com.programmer.backend.repository;
import com.programmer.backend.domain.ProjectReview;
import com.programmer.backend.domain.Proyecto;
import com.programmer.backend.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectReviewRepository extends JpaRepository<ProjectReview, Long> {

    List<ProjectReview> findByProyecto(Proyecto proyecto);

    // 🔒 Para evitar duplicados
    Optional<ProjectReview> findByAutorAndProyecto(Usuario autor, Proyecto proyecto);
}