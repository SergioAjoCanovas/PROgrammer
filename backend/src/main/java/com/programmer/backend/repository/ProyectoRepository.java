package com.programmer.backend.repository;

import com.programmer.backend.domain.Proyecto;
import com.programmer.backend.domain.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    
    // Añade esta línea: Spring Boot generará automáticamente la consulta SQL
    List<Proyecto> findByAutorId(Long autorId);
    List<Proyecto> findTop2ByAutorOrderByIdDesc(Usuario autor);
    
}