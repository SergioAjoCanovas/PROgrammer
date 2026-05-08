package com.programmer.backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        try {
            // Forzar que la columna 'mensaje' soporte 2500 caracteres, ya que Hibernate no hace el ALTER COLUMN por sí solo
            jdbcTemplate.execute("ALTER TABLE notificaciones MODIFY COLUMN mensaje VARCHAR(2500)");
            
            // Arreglar notificaciones corruptas que no tenían fecha de creación
            jdbcTemplate.execute("UPDATE notificaciones SET fecha_creacion = CURRENT_TIMESTAMP WHERE fecha_creacion IS NULL");
            
            System.out.println("==========================================================");
            System.out.println("ÉXITO: Columna 'mensaje' de notificaciones ampliada a 2500");
            System.out.println("ÉXITO: Notificaciones corruptas sin fecha reparadas.");
            System.out.println("==========================================================");
        } catch (Exception e) {
            System.out.println("Nota: No se pudo alterar la tabla notificaciones (puede que ya esté modificada o no exista aún).");
        }
    }
}
