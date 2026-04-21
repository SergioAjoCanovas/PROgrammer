package com.programmer.backend.service;

import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class RegistroService {

    // ÚNICAMENTE llamamos al repositorio de Usuario
    private final UsuarioRepository usuarioRepository;

    public RegistroService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // --------------------------------------------------------
    // MÉTODO 1: GUARDAR EL USUARIO EN LA BASE DE DATOS
    // --------------------------------------------------------
    @Transactional
    public Usuario registrarUsuario(Usuario usuarioNuevo) {
        // Guarda el usuario en tu única tabla y punto final
        return usuarioRepository.save(usuarioNuevo);
    }

    // --------------------------------------------------------
    // MÉTODO 2: GUARDAR LA FOTO EN LA CARPETA STATIC
    // --------------------------------------------------------
    public String guardarFoto(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        // 1. Generamos el nombre único para la imagen
        String nombreArchivo = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();
        
        // 2. Ruta RELATIVA (Quitamos el System.getProperty)
        // Java buscará esta ruta dentro de la carpeta donde se está ejecutando el proyecto
        Path directorioPath = Paths.get("backend", "src", "main", "resources", "static", "Img", "perfiles");
        
        // Creamos la carpeta si no existe (esto evita errores de "ruta no encontrada")
        if (!Files.exists(directorioPath)) {
            Files.createDirectories(directorioPath);
        }
        
        // Unimos la carpeta con el nombre del archivo
        Path rutaFinal = directorioPath.resolve(nombreArchivo);
        
        // 3. Guardar físicamente el archivo
        Files.copy(archivo.getInputStream(), rutaFinal);
        
        // 4. Retornamos la ruta que se guardará en el String de la Base de Datos
        return "/Img/perfiles/" + nombreArchivo;
    }
}