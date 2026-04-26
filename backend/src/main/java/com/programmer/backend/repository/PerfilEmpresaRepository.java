package com.programmer.backend.repository;

import com.programmer.backend.domain.PerfilEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilEmpresaRepository extends JpaRepository<PerfilEmpresa, Long> {

    Optional<PerfilEmpresa> findByUsuarioId(Long usuarioId);
}