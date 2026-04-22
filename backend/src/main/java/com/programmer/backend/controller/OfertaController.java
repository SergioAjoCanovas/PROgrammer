package com.programmer.backend.controller;

import com.programmer.backend.domain.OfertaEmpleo;
import com.programmer.backend.domain.Postulacion;
import com.programmer.backend.domain.Tecnologia;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.OfertaRepository;
import com.programmer.backend.repository.PostulacionRepository;
import com.programmer.backend.repository.TecnologiaRepository;
import com.programmer.backend.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller 
public class OfertaController {

    @Autowired
    private OfertaRepository ofertaRepository;

    @Autowired
    private TecnologiaRepository tecnologiaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PostulacionRepository postulacionRepository;

    // 1. VER TODAS LAS OFERTAS
    @GetMapping("/jobsearching")
    public String verOfertas(Model model) {
        try {
            List<OfertaEmpleo> listaOfertas = ofertaRepository.findAll();
            model.addAttribute("ofertas", listaOfertas != null ? listaOfertas : new java.util.ArrayList<>());
            return "UI/jobsearching/jobsearching"; 
        } catch (Exception e) {
            System.out.println("Error en jobsearching: " + e.getMessage());
            return "UI/main"; 
        }
    }

    // 2. VER SOLO LAS OFERTAS DE UNA EMPRESA
    @GetMapping("/mis-ofertas")
    public String verMisOfertas(@RequestParam("empresa") String usernameEmpresa, Model model) {
        try {
            List<OfertaEmpleo> todasLasOfertas = ofertaRepository.findAll();
            List<OfertaEmpleo> misOfertas = todasLasOfertas.stream()
                .filter(oferta -> oferta.getEmpresa() != null && usernameEmpresa.equals(oferta.getEmpresa().getUsername()))
                .collect(Collectors.toList());
            
            model.addAttribute("ofertas", misOfertas);
            return "UI/misofertas/misofertas"; 
        } catch (Exception e) {
            System.out.println("Error al cargar mis ofertas: " + e.getMessage());
            return "redirect:/jobsearching";
        }
    }

    // 3. VER POSTULACIONES DE UNA OFERTA
    @GetMapping("/ver-postulaciones/{id}")
    public String verPostulaciones(@PathVariable("id") Long ofertaId, Model model) {
        try {
            Optional<OfertaEmpleo> ofertaOpt = ofertaRepository.findById(ofertaId);
            if (ofertaOpt.isPresent()) {
                model.addAttribute("oferta", ofertaOpt.get());
                List<Postulacion> postulaciones = postulacionRepository.findByOfertaId(ofertaId);
                model.addAttribute("postulaciones", postulaciones);
                
                // NOTA: Recuerda que si tu carpeta no está dentro de UI, tienes que quitar el "UI/"
                return "UI/ver_postulaciones/ver_postulaciones"; 
            }
            return "redirect:/jobsearching";
        } catch (Exception e) {
            System.out.println("Error al cargar postulaciones: " + e.getMessage());
            return "redirect:/jobsearching";
        }
    }

    // 4. CREAR UNA NUEVA OFERTA
    @PostMapping("/api/ofertas/crear")
    public String crearOferta(
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("requisitos") String requisitos,
            @RequestParam("ofrecemos") String ofrecemos,
            @RequestParam("rango_salarial") String rangoSalarial,
            @RequestParam(value = "tecnologias_ids", required = false) List<Long> tecnologiasIds,
            @RequestParam("username_empresa") String usernameEmpresa) {
        
        OfertaEmpleo nuevaOferta = new OfertaEmpleo();
        nuevaOferta.setTitulo(titulo);
        nuevaOferta.setDescripcion(descripcion);
        nuevaOferta.setRequisitos(requisitos);
        nuevaOferta.setOfrecemos(ofrecemos);
        nuevaOferta.setRangoSalarial(rangoSalarial);
        nuevaOferta.setActiva(true);

        Optional<Usuario> empresaOpt = usuarioRepository.findByUsername(usernameEmpresa);
        if (empresaOpt.isPresent()) {
            nuevaOferta.setEmpresa(empresaOpt.get());
        }

        if (tecnologiasIds != null && !tecnologiasIds.isEmpty()) {
            List<Tecnologia> seleccionadas = tecnologiaRepository.findAllById(tecnologiasIds);
            nuevaOferta.setTecnologias(seleccionadas);
        }

        ofertaRepository.save(nuevaOferta);
        return "redirect:/jobsearching"; 
    }

    // 5. CAMBIAR ESTADO ACTIVA/CERRADA (TOGGLE)
    @PostMapping("/api/ofertas/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleEstadoOferta(@PathVariable("id") Long id) {
        try {
            Optional<OfertaEmpleo> ofertaOpt = ofertaRepository.findById(id);
            if (ofertaOpt.isPresent()) {
                OfertaEmpleo oferta = ofertaOpt.get();
                oferta.setActiva(!oferta.getActiva()); 
                ofertaRepository.save(oferta);
                
                return ResponseEntity.ok().body("{\"success\": true, \"activa\": " + oferta.getActiva() + "}");
            }
            return ResponseEntity.badRequest().body("{\"success\": false}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // 6. ELIMINAR OFERTA COMPLETAMENTE
    @DeleteMapping("/api/ofertas/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> borrarOferta(@PathVariable("id") Long id) {
        try {
            Optional<OfertaEmpleo> ofertaOpt = ofertaRepository.findById(id);
            if (ofertaOpt.isPresent()) {
                // Primero borramos las postulaciones de esa oferta para que la base de datos no dé error
                List<Postulacion> postulaciones = postulacionRepository.findByOfertaId(id);
                if (!postulaciones.isEmpty()) {
                    postulacionRepository.deleteAll(postulaciones);
                }
                
                // Después borramos la oferta
                ofertaRepository.deleteById(id);
                
                return ResponseEntity.ok().body("{\"success\": true}");
            }
            return ResponseEntity.badRequest().body("{\"success\": false}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }
}