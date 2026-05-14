package com.programmer.backend.repository;

import com.programmer.backend.domain.Mensaje;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    // =========================================
    // CONVERSACIÓN ENTRE 2 USUARIOS
    // =========================================
    @Query("""
        SELECT m
        FROM Mensaje m
        JOIN FETCH m.emisor
        JOIN FETCH m.receptor
        WHERE (
            (m.emisor.id = :user1 AND m.receptor.id = :user2 AND m.borradoPorEmisor = false)
         OR (m.emisor.id = :user2 AND m.receptor.id = :user1 AND m.borradoPorReceptor = false)
        )
        ORDER BY m.fechaEnvio ASC
    """)
    List<Mensaje> findConversacion(@Param("user1") Long user1,
                                   @Param("user2") Long user2);

    // =========================================
    // ÚLTIMOS CHATS (CORRECTO PARA UI)
    // =========================================
    @Query("""
        SELECT m
        FROM Mensaje m
        JOIN FETCH m.emisor
        JOIN FETCH m.receptor
        WHERE m.id IN (
            SELECT MAX(m2.id)
            FROM Mensaje m2
            WHERE m2.emisor.id = :userId OR m2.receptor.id = :userId
            GROUP BY CASE
                WHEN m2.emisor.id = :userId THEN m2.receptor.id
                ELSE m2.emisor.id
            END
        )
        ORDER BY m.fechaEnvio DESC
    """)
    List<Mensaje> findUltimosChats(@Param("userId") Long userId);

    // =========================================
    // MENSAJES NO LEIDOS
    // =========================================

    @Query("""
        SELECT COUNT(m)
        FROM Mensaje m
        WHERE m.receptor.id = :userId
        AND m.emisor.id = :otroId
        AND m.leido = false
        AND m.borradoPorReceptor = false
        """)
        int countNoLeidos(Long userId, Long otroId);
    
    @Query("""
        SELECT COUNT(m)
        FROM Mensaje m
        WHERE m.receptor.id = :userId
        AND m.leido = false
        AND m.borradoPorReceptor = false
        """)
    int countAllNoLeidos(Long userId);

    // =========================================
    // MARCAR MENSAJES COMO LEIDOS
    // =========================================

    @Modifying
        @Query("""
        UPDATE Mensaje m
        SET m.leido = true
        WHERE m.receptor.id = :userId
        AND m.emisor.id = :otroId
        AND m.leido = false
        AND m.borradoPorReceptor = false
        """)
        void marcarComoLeidos(Long userId, Long otroId);

    // =========================================
    // ULTIMOS MENSAJES POR CONVERSACION
    // =========================================
    @Query("""
        SELECT m
        FROM Mensaje m
        WHERE (m.emisor.id = :userId AND m.receptor.id = :otroId)
        OR (m.emisor.id = :otroId AND m.receptor.id = :userId)
        ORDER BY m.fechaEnvio DESC
        """)
        List<Mensaje> findUltimoMensajeConversacion(Long userId, Long otroId);

    // =========================================
    // VACIAR CHATS
    // =========================================ç
    @Modifying
    @Transactional
    @Query("""
    UPDATE Mensaje m
    SET 
        m.borradoPorEmisor = CASE 
            WHEN m.emisor.id = :userId THEN true 
            ELSE m.borradoPorEmisor 
        END,
        m.borradoPorReceptor = CASE 
            WHEN m.receptor.id = :userId THEN true 
            ELSE m.borradoPorReceptor 
        END
    WHERE 
        (m.emisor.id = :userId AND m.receptor.id = :otroId)
    OR (m.emisor.id = :otroId AND m.receptor.id = :userId)
    """)
    void vaciarChat(Long userId, Long otroId);

    
}