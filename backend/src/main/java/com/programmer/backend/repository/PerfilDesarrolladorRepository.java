package com.programmer.backend.repository;

import com.programmer.backend.domain.PerfilDesarrollador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PerfilDesarrolladorRepository extends JpaRepository<PerfilDesarrollador, Long> {

    Optional<PerfilDesarrollador> findByUsuarioId(Long usuarioId);

    @Query("""
        SELECT p FROM PerfilDesarrollador p
        LEFT JOIN FETCH p.tecnologias
        WHERE p.usuario.id = :id
    """)
    Optional<PerfilDesarrollador> findByUsuarioIdWithTecnologias(Long id);
}