package com.programmer.backend.controller;

import com.programmer.backend.domain.OfertaEmpleo;
import com.programmer.backend.domain.Postulacion;
import com.programmer.backend.domain.Proyecto;
import com.programmer.backend.domain.Tecnologia;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.OfertaRepository;
import com.programmer.backend.repository.PostulacionRepository;
import com.programmer.backend.repository.ProyectoRepository;
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

import java.security.Principal;
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

    @Autowired
    private ProyectoRepository proyectoRepository; 

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

    @GetMapping("/ver-postulaciones/{id}")
    public String verPostulaciones(@PathVariable("id") Long ofertaId, Model model) {
        try {
            Optional<OfertaEmpleo> ofertaOpt = ofertaRepository.findById(ofertaId);
            if (ofertaOpt.isPresent()) {
                model.addAttribute("oferta", ofertaOpt.get());
                List<Postulacion> postulaciones = postulacionRepository.findByOfertaId(ofertaId);
                model.addAttribute("postulaciones", postulaciones);
                
                return "UI/ver_postulaciones/ver_postulaciones"; 
            }
            return "redirect:/jobsearching";
        } catch (Exception e) {
            System.out.println("Error al cargar postulaciones: " + e.getMessage());
            return "redirect:/jobsearching";
        }
    }

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

    @DeleteMapping("/api/ofertas/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> borrarOferta(@PathVariable("id") Long id) {
        try {
            Optional<OfertaEmpleo> ofertaOpt = ofertaRepository.findById(id);
            if (ofertaOpt.isPresent()) {
                List<Postulacion> postulaciones = postulacionRepository.findByOfertaId(id);
                if (!postulaciones.isEmpty()) {
                    postulacionRepository.deleteAll(postulaciones);
                }
                
                ofertaRepository.deleteById(id);
                
                return ResponseEntity.ok().body("{\"success\": true}");
            }
            return ResponseEntity.badRequest().body("{\"success\": false}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // 7. VER DETALLE DE UNA OFERTA (Añadido el usuario al modelo)
    @PostMapping("/jobview/{id}") // <-- CAMBIO AQUÍ: Pasa de @GetMapping a @PostMapping
    public String verDetalleOferta(
            @PathVariable("id") Long id, 
            @RequestParam(value = "username", required = false) String username, 
            Model model) {
        
        try {
            Optional<OfertaEmpleo> ofertaOpt = ofertaRepository.findById(id);
            if (ofertaOpt.isPresent()) {
                model.addAttribute("oferta", ofertaOpt.get());

                List<Proyecto> misProyectos = new java.util.ArrayList<>();
                
                if (username != null && !username.isEmpty()) {
                    Optional<Usuario> usuarioActual = usuarioRepository.findByUsername(username);
                    if (usuarioActual.isPresent()) {
                        Usuario usr = usuarioActual.get();
                        model.addAttribute("usuarioLogueado", usr);
                        misProyectos = proyectoRepository.findByAutorId(usr.getId());
                    }
                }
                
                model.addAttribute("proyectos", misProyectos);
                
                return "UI/jobview/jobview"; 
            }
            return "redirect:/jobsearching";
        } catch (Exception e) {
            System.out.println("Error al cargar la oferta en jobview: " + e.getMessage());
            return "redirect:/jobsearching";
        }
    }

// 8. ENVIAR LA POSTULACIÓN Y REDIRIGIR
    @PostMapping("/api/postular")
    public String enviarPostulacion(
            @RequestParam("ofertaId") Long ofertaId,
            @RequestParam("username") String username,
            @RequestParam(value = "proyectoId", required = false) Long proyectoId,
            @RequestParam(value = "mensaje", required = false) String mensaje
            // @RequestParam(value = "archivo_cv", required = false) MultipartFile archivoCv 
            // (El archivo viaja, pero lo ignoramos de momento hasta implementar un servicio de almacenamiento)
    ) {
        try {
            // Buscamos la oferta y el usuario
            Optional<OfertaEmpleo> ofertaOpt = ofertaRepository.findById(ofertaId);
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

            if (ofertaOpt.isPresent() && usuarioOpt.isPresent()) {
                Postulacion nuevaPostulacion = new Postulacion();
                nuevaPostulacion.setOferta(ofertaOpt.get());
                nuevaPostulacion.setDesarrollador(usuarioOpt.get());
                nuevaPostulacion.setMensajeAdjunto(mensaje);

                // Si seleccionó un proyecto, lo vinculamos
                if (proyectoId != null) {
                    Optional<Proyecto> proyectoOpt = proyectoRepository.findById(proyectoId);
                    proyectoOpt.ifPresent(nuevaPostulacion::setProyectoVinculado);
                }

                // Guardamos en la base de datos
                postulacionRepository.save(nuevaPostulacion);
            }
        } catch (Exception e) {
            System.out.println("Error al guardar la postulación: " + e.getMessage());
        }

        // Redirigimos a la página principal de ofertas
        return "redirect:/jobsearching";
    }

    // 9. OBTENER LAS OFERTAS A LAS QUE SE HA POSTULADO UN USUARIO
        @GetMapping("/api/postulaciones/mis-ofertas-ids")
        @ResponseBody
        public ResponseEntity<List<Long>> obtenerMisOfertasPostuladas(@RequestParam("username") String username) {
            try {
                Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
                if (usuarioOpt.isPresent()) {
                    // Buscamos sus postulaciones
                    List<Postulacion> postulaciones = postulacionRepository.findByDesarrolladorId(usuarioOpt.get().getId());
                    
                    // Extraemos solo los IDs de las ofertas
                    List<Long> ofertaIds = postulaciones.stream()
                            .map(p -> p.getOferta().getId())
                            .collect(Collectors.toList());
                            
                    return ResponseEntity.ok(ofertaIds);
                }
                return ResponseEntity.ok(new java.util.ArrayList<>());
            } catch (Exception e) {
                return ResponseEntity.status(500).body(new java.util.ArrayList<>());
            }
        }
}