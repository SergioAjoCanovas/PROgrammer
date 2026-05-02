package com.programmer.backend.repository;

import com.programmer.backend.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<Usuario> findByRolNombre(String nombreRol);
    List<Usuario> findByRolNombreIn(List<String> roles);
}