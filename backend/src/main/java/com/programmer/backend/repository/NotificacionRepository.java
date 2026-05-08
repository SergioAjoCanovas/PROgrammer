package com.programmer.backend.repository;

import com.programmer.backend.domain.Notificacion;
import com.programmer.backend.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);
    long countByUsuarioAndLeidaFalse(Usuario usuario);
    void deleteByUsuario(Usuario usuario);
    void deleteByUsuarioAndTipo(Usuario usuario, String tipo);
    void deleteByUsuarioAndTipoIn(Usuario usuario, List<String> tipos);
    void deleteByUsuarioAndTipoNotIn(Usuario usuario, List<String> tipos);
}
