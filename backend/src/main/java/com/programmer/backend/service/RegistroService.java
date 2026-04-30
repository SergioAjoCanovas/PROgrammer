package com.programmer.backend.service;

import com.programmer.backend.domain.*;
import com.programmer.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilDesarrolladorRepository perfilDesarrolladorRepository;
    private final PerfilEmpresaRepository perfilEmpresaRepository;

    public RegistroService(UsuarioRepository usuarioRepository,
                           PerfilDesarrolladorRepository perfilDesarrolladorRepository,
                           PerfilEmpresaRepository perfilEmpresaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.perfilDesarrolladorRepository = perfilDesarrolladorRepository;
        this.perfilEmpresaRepository = perfilEmpresaRepository;
    }

    // --------------------------------------------------------
    // 1. REGISTRO + CREACIÓN DE PERFIL SEGÚN ROL
    // --------------------------------------------------------
    @Transactional
    public Usuario registrarUsuario(Usuario usuarioNuevo) {

        Usuario usuarioGuardado = usuarioRepository.save(usuarioNuevo);

        Rol rol = usuarioGuardado.getRol();

        if (rol != null && rol.getNombre() != null) {

            String nombreRol = rol.getNombre().toLowerCase();

            // =========================
            // DESARROLLADOR
            // =========================
            if (nombreRol.equals("desarrollador")) {

                PerfilDesarrollador perfil = new PerfilDesarrollador();
                perfil.setUsuario(usuarioGuardado);

                perfilDesarrolladorRepository.save(perfil);
            }

            // =========================
            // EMPRESA
            // =========================
            if (nombreRol.equals("empresa")) {

                PerfilEmpresa perfil = new PerfilEmpresa();
                perfil.setUsuario(usuarioGuardado);

                perfilEmpresaRepository.save(perfil);
            }
        }

        return usuarioGuardado;
    }

    // --------------------------------------------------------
    // 2. FOTO PERFIL
    // --------------------------------------------------------
    public String guardarFoto(MultipartFile archivo) throws IOException {

        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        String nombreArchivo = UUID.randomUUID() + "_" + archivo.getOriginalFilename();

        Path directorioPath = Paths.get(System.getProperty("user.dir"), "uploads", "perfiles");

        if (!Files.exists(directorioPath)) {
            Files.createDirectories(directorioPath);
        }

        Path rutaFinal = directorioPath.resolve(nombreArchivo);

        Files.copy(archivo.getInputStream(), rutaFinal);

        return "/uploads/perfiles/" + nombreArchivo;
    }

    // --------------------------------------------------------
    // 3. CV
    // --------------------------------------------------------
    public String guardarCV(MultipartFile file) throws IOException {

        String carpeta = "uploads/cv/";

        File directorio = new File(carpeta);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        String nombreArchivo = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Path ruta = Paths.get(carpeta + nombreArchivo);

        Files.write(ruta, file.getBytes());

        return "/" + carpeta + nombreArchivo;
    }
}