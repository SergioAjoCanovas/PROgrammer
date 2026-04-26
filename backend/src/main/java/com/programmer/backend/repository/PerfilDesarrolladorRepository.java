package com.programmer.backend.repository;

import com.programmer.backend.domain.PerfilDesarrollador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilDesarrolladorRepository extends JpaRepository<PerfilDesarrollador, Long> {

    Optional<PerfilDesarrollador> findByUsuarioId(Long usuarioId);
}