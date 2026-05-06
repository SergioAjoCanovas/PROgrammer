package com.programmer.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.programmer.backend.domain.UsuarioSeguimiento;

public interface FollowRepository extends JpaRepository<UsuarioSeguimiento, Long> {

    boolean existsBySeguidorIdAndSeguidoId(Long seguidorId, Long seguidoId);
}
