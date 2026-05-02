package com.programmer.backend.controller;

import com.programmer.backend.domain.PerfilDesarrollador;
import com.programmer.backend.domain.PerfilEmpresa;
import com.programmer.backend.domain.Tecnologia;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.PerfilDesarrolladorRepository;
import com.programmer.backend.repository.PerfilEmpresaRepository;
import com.programmer.backend.repository.TecnologiaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TechController {

    @Autowired
    private TecnologiaRepository tecnologiaRepository;

    @Autowired
    private PerfilDesarrolladorRepository perfilRepo;

    @Autowired
    private PerfilEmpresaRepository empresaRepo;

    // =========================
    // VISTA AÑADIR TECNOLOGÍAS
    // =========================
    @GetMapping("/addTechnology")
    public String addTechnology(Model model, HttpSession session) {
    
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
    
        if (usuario == null) {
            return "redirect:/login";
        }
    
        String rol = usuario.getRol().getNombre();
    
        if ("USUARIO".equals(rol)) {
            return "redirect:/ownProfile";
        }
    
        List<Tecnologia> tecnologias = tecnologiaRepository.findAll();
    
        Map<String, List<Tecnologia>> tecnologiasPorCategoria =
                tecnologias.stream()
                        .filter(t -> t.getCategoria() != null)
                        .collect(Collectors.groupingBy(
                                t -> t.getCategoria().getNombre(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));
    
        model.addAttribute("tecnologiasPorCategoria", tecnologiasPorCategoria);
    
        Set<Long> techSeleccionadas = new HashSet<>();
    
        if ("COMPANY".equals(rol)) {
            PerfilEmpresa empresa = empresaRepo.findByUsuarioId(usuario.getId()).orElse(null);
            if (empresa != null && empresa.getTecnologias() != null) {
                techSeleccionadas = empresa.getTecnologias()
                        .stream()
                        .map(Tecnologia::getId)
                        .collect(Collectors.toSet());
            }
        } else {
            PerfilDesarrollador perfil = perfilRepo.findByUsuarioId(usuario.getId()).orElse(null);
            if (perfil != null && perfil.getTecnologias() != null) {
                techSeleccionadas = perfil.getTecnologias()
                        .stream()
                        .map(Tecnologia::getId)
                        .collect(Collectors.toSet());
            }
        }
    
        model.addAttribute("techSeleccionadas", techSeleccionadas);
    
        return "UI/addTechnology/addTechnology";
    }

    // =========================
    // GUARDAR TECNOLOGÍAS
    // =========================
    @PostMapping("/usuario/saveTechnologies")
    public String saveTechnologies(@RequestParam(required = false) List<Long> techIds,
                                HttpSession session) {

        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");

        if (user == null) return "redirect:/login";

        Set<Tecnologia> nuevas = new HashSet<>();

        if (techIds != null && !techIds.isEmpty()) {
            nuevas = new HashSet<>(tecnologiaRepository.findAllById(techIds));
        }

        String rol = user.getRol().getNombre();

        if ("DEVELOPER".equals(rol) || "ADMIN".equals(rol) || "ROLE_ADMIN".equals(rol)) {

            PerfilDesarrollador perfil = perfilRepo.findByUsuarioId(user.getId())
                    .orElseGet(() -> {
                        PerfilDesarrollador p = new PerfilDesarrollador();
                        p.setUsuario(user);
                        return perfilRepo.save(p);
                    });
        
            perfil.setTecnologias(nuevas);
            perfilRepo.save(perfil);
    
        } else if ("COMPANY".equals(rol)) {

            PerfilEmpresa perfil = empresaRepo.findByUsuarioId(user.getId())
                    .orElseGet(() -> {
                        PerfilEmpresa p = new PerfilEmpresa();
                        p.setUsuario(user);
                        return empresaRepo.save(p);
                    });

            perfil.setTecnologias(nuevas);
            empresaRepo.save(perfil);
        }

        return "redirect:/ownProfile";
    }
}