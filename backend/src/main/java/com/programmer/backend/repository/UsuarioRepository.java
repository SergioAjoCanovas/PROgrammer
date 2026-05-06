package com.programmer.backend.repository;

import com.programmer.backend.domain.Mensaje;
import com.programmer.backend.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<Usuario> findByRolNombre(String nombreRol);

    List<Usuario> findByRolNombreIn(List<String> roles);

    @Query("""
    SELECT u
    FROM Usuario u
    WHERE u.id IN (
        SELECT us.seguido.id
        FROM UsuarioSeguimiento us
        WHERE us.seguidor.id = :userId
    )
    AND u.id IN (
        SELECT us2.seguidor.id
        FROM UsuarioSeguimiento us2
        WHERE us2.seguido.id = :userId
    )
    """)
    List<Usuario> findAmigos(@Param("userId") Long userId);
}