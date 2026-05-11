package com.programmer.backend.service;

import com.programmer.backend.domain.Notificacion;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.NotificacionRepository;
import com.programmer.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacionService(NotificacionRepository notificacionRepository, UsuarioRepository usuarioRepository) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void enviarNotificacion(Usuario receptor, String mensaje, String tipo, String enlace) {
        
        Usuario u = usuarioRepository.findById(receptor.getId()).orElse(receptor);
        
        
        if (u.isSilenciarNotificaciones() && !"NUEVO_PARCHE".equals(tipo)) {
            return;
        }

        Notificacion n = new Notificacion();
        n.setUsuario(u);
        n.setMensaje(mensaje);
        n.setTipo(tipo);
        n.setEnlace(enlace);
        n.setFechaCreacion(new Date());
        
        notificacionRepository.save(n);
    }
}
