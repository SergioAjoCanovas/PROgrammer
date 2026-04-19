package com.programmer.backend.repository;

import com.programmer.backend.domain.OfertaEmpleo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfertaRepository extends JpaRepository<OfertaEmpleo, Long> {
}