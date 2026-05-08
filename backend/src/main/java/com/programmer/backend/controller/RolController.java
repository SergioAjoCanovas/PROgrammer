package com.programmer.backend.controller;

import com.programmer.backend.domain.Usuario;
import com.programmer.backend.domain.Rol;
import com.programmer.backend.domain.PerfilDesarrollador;
import com.programmer.backend.domain.PerfilEmpresa;
import com.programmer.backend.repository.UsuarioRepository;
import com.programmer.backend.repository.RolRepository;
import com.programmer.backend.repository.PerfilDesarrolladorRepository;
import com.programmer.backend.repository.PerfilEmpresaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class RolController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PerfilDesarrolladorRepository perfilDesarrolladorRepository;

    @Autowired
    private PerfilEmpresaRepository perfilEmpresaRepository;

    @GetMapping("/elegir-rol")
    public String elegirRol() {
        return "UI/elegir-rol/elegir-rol";
    }

    @PostMapping("/seleccionar-rol")
    public String seleccionarRol(@RequestParam("tipo") String tipo, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        
        if (usuario != null) {
            Rol nuevoRol = rolRepository.findByNombre(tipo).orElseThrow();
            usuario.setRol(nuevoRol);
            usuarioRepository.save(usuario);

            // Si elige desarrollador, le creamos el perfil en la BBDD
            if (tipo.equals("DEVELOPER")) {
                PerfilDesarrollador perfil = new PerfilDesarrollador();
                perfil.setUsuario(usuario);
                perfilDesarrolladorRepository.save(perfil);
            } 
            // Si elige empresa, le creamos el perfil en la BBDD
            else if (tipo.equals("COMPANY")) {
                PerfilEmpresa perfil = new PerfilEmpresa();
                perfil.setUsuario(usuario);
                perfilEmpresaRepository.save(perfil);
            }

            session.setAttribute("rolUsuario", nuevoRol.getNombre());
            
            return "redirect:/main?user=" + URLEncoder.encode(usuario.getUsername(), StandardCharsets.UTF_8)
                    + "&rol=" + URLEncoder.encode(nuevoRol.getNombre(), StandardCharsets.UTF_8);
        }
        
        return "redirect:/login";
    }
}