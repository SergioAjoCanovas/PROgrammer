package com.programmer.backend.repository;

import com.programmer.backend.domain.UsuarioSeguimiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuariosSeguimientoRepository extends JpaRepository<UsuarioSeguimiento, Long> {

    boolean existsBySeguidor_IdAndSeguido_Id(Long seguidorId, Long seguidoId);
}