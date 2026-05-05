package com.programmer.backend.repository;

import com.programmer.backend.domain.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
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
        WHERE (m.emisor.id = :user1 AND m.receptor.id = :user2)
           OR (m.emisor.id = :user2 AND m.receptor.id = :user1)
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
}