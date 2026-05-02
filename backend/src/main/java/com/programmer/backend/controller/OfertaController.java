package com.programmer.backend.controller;

import com.programmer.backend.domain.*;
import com.programmer.backend.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // <-- NUEVA IMPORTACIÓN

import java.io.IOException; // <-- NUEVA IMPORTACIÓN
import java.nio.file.Files; // <-- NUEVA IMPORTACIÓN
import java.nio.file.Path; // <-- NUEVA IMPORTACIÓN
import java.nio.file.Paths; // <-- NUEVA IMPORTACIÓN
import java.nio.file.StandardCopyOption; // <-- NUEVA IMPORTACIÓN
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
        List<OfertaEmpleo> listaOfertas = ofertaRepository.findAll();
        model.addAttribute("ofertas", listaOfertas != null ? listaOfertas : List.of());
        return "UI/jobsearching/jobsearching"; 
    }

    @GetMapping("/mis-ofertas")
    public String verMisOfertas(@RequestParam("empresa") String usernameEmpresa, Model model) {
        List<OfertaEmpleo> misOfertas = ofertaRepository.findAll().stream()
            .filter(o -> o.getEmpresa() != null && usernameEmpresa.equals(o.getEmpresa().getUsername()))
            .collect(Collectors.toList());
        model.addAttribute("ofertas", misOfertas);
        return "UI/misofertas/misofertas";
    }

    @GetMapping("/ver-postulaciones/{id}")
    public String verPostulaciones(@PathVariable("id") Long ofertaId, Model model) {
        Optional<OfertaEmpleo> ofertaOpt = ofertaRepository.findById(ofertaId);
        if (ofertaOpt.isPresent()) {
            model.addAttribute("oferta", ofertaOpt.get());
            List<Postulacion> postulaciones = postulacionRepository.findByOfertaId(ofertaId);
            model.addAttribute("postulaciones", postulaciones);
            return "UI/ver_postulaciones/ver_postulaciones"; 
        }
        return "redirect:/jobsearching";
    }

    // CARGAR VISTA DE EDICIÓN CON DATOS PREVIOS
    @GetMapping("/editOffer/{id}")
    public String editarOferta(@PathVariable("id") Long id, Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/login";

        Optional<OfertaEmpleo> ofertaOpt = ofertaRepository.findById(id);
        if (ofertaOpt.isPresent()) {
            OfertaEmpleo oferta = ofertaOpt.get();
            if (!oferta.getEmpresa().getId().equals(usuario.getId())) return "redirect:/jobsearching";
            
            model.addAttribute("oferta", oferta);
            // Extraemos los IDs de las tecnologías para pre-marcarlas en el formulario
            List<Long> techIds = oferta.getTecnologias().stream().map(Tecnologia::getId).collect(Collectors.toList());
            model.addAttribute("techIdsActuales", techIds);
            return "UI/company/publishOffer"; 
        }
        return "redirect:/jobsearching";
    }

    // CREAR O ACTUALIZAR OFERTA
    @PostMapping("/api/ofertas/crear")
    public String guardarOferta(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("requisitos") String requisitos,
            @RequestParam("ofrecemos") String ofrecemos,
            @RequestParam("rango_salarial") String rangoSalarial,
            @RequestParam(value = "tecnologias_ids", required = false) List<Long> tecnologiasIds,
            @RequestParam("username_empresa") String usernameEmpresa) {
        
        boolean esEdicion = (id != null);
        OfertaEmpleo oferta = esEdicion ? ofertaRepository.findById(id).orElse(new OfertaEmpleo()) : new OfertaEmpleo();

        oferta.setTitulo(titulo);
        oferta.setDescripcion(descripcion);
        oferta.setRequisitos(requisitos);
        oferta.setOfrecemos(ofrecemos);
        oferta.setRangoSalarial(rangoSalarial);
        
        if (!esEdicion) oferta.setActiva(true);

        usuarioRepository.findByUsername(usernameEmpresa).ifPresent(oferta::setEmpresa);

        if (tecnologiasIds != null && !tecnologiasIds.isEmpty()) {
            oferta.setTecnologias(tecnologiaRepository.findAllById(tecnologiasIds));
        } else if (oferta.getTecnologias() != null) {
            oferta.getTecnologias().clear();
        }

        ofertaRepository.save(oferta);
        return "redirect:/jobsearching?" + (esEdicion ? "ofertaEditada=true" : "ofertaPublicada=true"); 
    }

    @PostMapping("/api/ofertas/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleEstadoOferta(@PathVariable("id") Long id) {
        try {
            Optional<OfertaEmpleo> o = ofertaRepository.findById(id);
            if (o.isPresent()) {
                o.get().setActiva(!o.get().getActiva()); 
                ofertaRepository.save(o.get());
                return ResponseEntity.ok().body("{\"success\": true, \"activa\": " + o.get().getActiva() + "}");
            }
            return ResponseEntity.badRequest().body("{\"success\": false}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"success\": false}");
        }
    }

    @DeleteMapping("/api/ofertas/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> borrarOferta(@PathVariable("id") Long id) {
        try {
            Optional<OfertaEmpleo> o = ofertaRepository.findById(id);
            if (o.isPresent()) {
                postulacionRepository.deleteAll(postulacionRepository.findByOfertaId(id));
                ofertaRepository.deleteById(id);
                return ResponseEntity.ok().body("{\"success\": true}");
            }
            return ResponseEntity.badRequest().body("{\"success\": false}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"success\": false}");
        }
    }

    @PostMapping("/jobview/{id}") 
    public String verDetalleOferta(@PathVariable("id") Long id, @RequestParam(value = "username", required = false) String username, Model model) {
        Optional<OfertaEmpleo> o = ofertaRepository.findById(id);
        if (o.isPresent()) {
            model.addAttribute("oferta", o.get());
            if (username != null && !username.isEmpty()) {
                usuarioRepository.findByUsername(username).ifPresent(usr -> {
                    model.addAttribute("usuarioLogueado", usr);
                    model.addAttribute("proyectos", proyectoRepository.findByAutorId(usr.getId()));
                    postulacionRepository.findByDesarrolladorId(usr.getId()).stream()
                        .filter(p -> p.getOferta().getId().equals(id)).findFirst()
                        .ifPresent(p -> model.addAttribute("postulacionExistente", p));
                });
            }
            return "UI/jobview/jobview"; 
        }
        return "redirect:/jobsearching";
    }

    // --- AQUÍ ESTÁ LA MAGIA PARA EL CV ---
    @PostMapping("/api/postular")
    public String enviarPostulacion(@RequestParam("ofertaId") Long ofertaId, 
                                    @RequestParam("username") String username,
                                    @RequestParam(value = "proyectoId", required = false) Long proyectoId,
                                    @RequestParam(value = "mensaje", required = false) String mensaje,
                                    @RequestParam(value = "opcion_cv", required = false) String opcionCv,
                                    @RequestParam(value = "archivo_cv", required = false) MultipartFile archivoCv) {
        
        boolean esActualizacion = false;
        Optional<OfertaEmpleo> o = ofertaRepository.findById(ofertaId);
        Optional<Usuario> u = usuarioRepository.findByUsername(username);

        if (o.isPresent() && u.isPresent()) {
            Usuario usuario = u.get();
            Postulacion p = postulacionRepository.findByDesarrolladorId(usuario.getId()).stream()
                .filter(post -> post.getOferta().getId().equals(ofertaId)).findFirst().orElse(new Postulacion());
            
            esActualizacion = (p.getId() != null);
            p.setOferta(o.get()); 
            p.setDesarrollador(usuario); 
            p.setMensajeAdjunto(mensaje);
            
            if (proyectoId != null) {
                proyectoRepository.findById(proyectoId).ifPresent(p::setProyectoVinculado);
            }

            // GESTIÓN DEL ARCHIVO CV SUBIDO
            if ("nuevo".equals(opcionCv) && archivoCv != null && !archivoCv.isEmpty()) {
                try {
                    // Crea la carpeta si no existe (ajusta la ruta según tu estructura)
                    String uploadDir = "uploads/cvs/"; 
                    Path uploadPath = Paths.get(uploadDir);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    
                    // Nombra el archivo de forma única
                    String fileName = System.currentTimeMillis() + "_" + archivoCv.getOriginalFilename().replaceAll("\\s+", "_");
                    Path filePath = uploadPath.resolve(fileName);
                    
                    // Guarda el archivo
                    Files.copy(archivoCv.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    String rutaCV = "/" + uploadDir + fileName;
                    
                    // 1. Guardamos el CV adjunto específicamente en la postulación
                    p.setCvAdjunto(rutaCV);
                    
                    // 2. Si el usuario NO tenía un CV en su perfil, le guardamos este como predeterminado
                    if (usuario.getCurriculum() == null || usuario.getCurriculum().isEmpty()) {
                        usuario.setCurriculum(rutaCV);
                        usuarioRepository.save(usuario);
                    }
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    // Aquí podrías manejar el error de subida si quisieras
                }
            }

            postulacionRepository.save(p);
        }
        return "redirect:/jobsearching?" + (esActualizacion ? "actualizado=true" : "postulado=true");
    }

    @GetMapping("/api/postulaciones/mis-ofertas-ids")
    @ResponseBody
    public ResponseEntity<List<Long>> obtenerMisOfertasPostuladas(@RequestParam("username") String username) {
        return usuarioRepository.findByUsername(username)
            .map(u -> ResponseEntity.ok(postulacionRepository.findByDesarrolladorId(u.getId()).stream()
                .map(p -> p.getOferta().getId()).collect(Collectors.toList())))
            .orElse(ResponseEntity.ok(List.of()));
    }
}