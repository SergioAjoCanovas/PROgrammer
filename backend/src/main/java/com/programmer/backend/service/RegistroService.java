package com.programmer.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.programmer.backend.domain.*;
import com.programmer.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilDesarrolladorRepository perfilDesarrolladorRepository;
    private final PerfilEmpresaRepository perfilEmpresaRepository;
    private final Cloudinary cloudinary;

    public RegistroService(UsuarioRepository usuarioRepository,
                           PerfilDesarrolladorRepository perfilDesarrolladorRepository,
                           PerfilEmpresaRepository perfilEmpresaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.perfilDesarrolladorRepository = perfilDesarrolladorRepository;
        this.perfilEmpresaRepository = perfilEmpresaRepository;

        // Configuración de Cloudinary (Introduce aquí tus credenciales reales)
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dmfsfky9r",
                "api_key", "856664266132363",
                "api_secret", "RLm0bg2FlZW5-Vy3IpVx6_icR3Q",
                "secure", true
        ));
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
    // 2. FOTO PERFIL (Alojado en Cloudinary)
    // --------------------------------------------------------
    public String guardarFoto(MultipartFile archivo) throws IOException {

        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        // Subir a Cloudinary
        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(archivo.getBytes(), ObjectUtils.emptyMap());

        // Devolver la URL segura (https) generada por Cloudinary
        return uploadResult.get("secure_url").toString();
    }

    // --------------------------------------------------------
    // 3. CV (Alojado en Cloudinary)
    // --------------------------------------------------------
    public String guardarCV(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        // Subir a Cloudinary (resource_type "auto" es importante para aceptar PDFs)
        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", "auto"
        ));

        // Devolver la URL segura del PDF generado por Cloudinary
        return uploadResult.get("secure_url").toString();
    }
}