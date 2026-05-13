package com.programmer.backend.controller;

import com.programmer.backend.domain.*;
import com.programmer.backend.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    @Autowired
    private NotificacionRepository notificacionRepository;

    @GetMapping("/jobsearching")
    public String verOfertas(Model model) {
        List<OfertaEmpleo> listaOfertas = ofertaRepository.findAllByOrderByIdDesc();
        model.addAttribute("ofertas", listaOfertas != null ? listaOfertas : List.of());
        return "UI/jobsearching/jobsearching"; 
    }

    @GetMapping("/mis-ofertas")
    public String verMisOfertas(@RequestParam("empresa") String usernameEmpresa, Model model, HttpSession session) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        boolean esAdmin = usuarioLogueado != null && usuarioLogueado.getRol() != null && 
                          ("ADMIN".equalsIgnoreCase(usuarioLogueado.getRol().getNombre()) || "1".equals(usuarioLogueado.getRol().getNombre()));

        List<OfertaEmpleo> allOfertas = ofertaRepository.findAllByOrderByIdDesc();
        
        if (esAdmin) {
            // Mis Ofertas (las creadas por el admin)
            List<OfertaEmpleo> misOfertas = allOfertas.stream()
                .filter(o -> o.getEmpresa() != null && usernameEmpresa.equals(o.getEmpresa().getUsername()))
                .collect(Collectors.toList());
            
            // Ofertas de otras empresas (Agrupadas por Empresa)
            java.util.Map<Usuario, List<OfertaEmpleo>> ofertasPorEmpresa = allOfertas.stream()
                .filter(o -> o.getEmpresa() != null && !usernameEmpresa.equals(o.getEmpresa().getUsername()))
                .collect(Collectors.groupingBy(OfertaEmpleo::getEmpresa));

            model.addAttribute("misOfertas", misOfertas);
            model.addAttribute("ofertasPorEmpresa", ofertasPorEmpresa);
        } else {
            List<OfertaEmpleo> misOfertas = allOfertas.stream()
                .filter(o -> o.getEmpresa() != null && usernameEmpresa.equals(o.getEmpresa().getUsername()))
                .collect(Collectors.toList());
            model.addAttribute("ofertas", misOfertas);
        }

        model.addAttribute("esAdmin", esAdmin);
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
            
            // Comprobamos si es el dueño O si es administrador
            boolean esAdmin = usuario.getRol() != null && 
                               ("ADMIN".equalsIgnoreCase(usuario.getRol().getNombre()) || "1".equals(usuario.getRol().getNombre()));
            
            if (!oferta.getEmpresa().getId().equals(usuario.getId()) && !esAdmin) {
                return "redirect:/jobsearching"; // Si no es ni el dueño ni Admin, lo echamos
            }
            
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
        
        // CORRECCIÓN AQUÍ: Solo establecemos la empresa creadora y la activamos si NO es una edición
        if (!esEdicion) {
            oferta.setActiva(true);
            usuarioRepository.findByUsername(usernameEmpresa).ifPresent(oferta::setEmpresa);
        }
        // Si es edición, la oferta ya tiene su empresa original cargada de la base de datos, así que no la sobreescribimos.

        if (tecnologiasIds != null && !tecnologiasIds.isEmpty()) {
            oferta.setTecnologias(tecnologiaRepository.findAllById(tecnologiasIds));
        } else if (oferta.getTecnologias() != null) {
            oferta.getTecnologias().clear();
        }

        ofertaRepository.save(oferta);

        if (!esEdicion && oferta.getEmpresa() != null) {
            for (Usuario seguidor : oferta.getEmpresa().getSeguidores()) {
                if (!seguidor.isSilenciarNotificaciones()) {
                    Notificacion n = new Notificacion();
                    n.setUsuario(seguidor);
                    n.setTipo("NUEVA_OFERTA");
                    n.setMensaje("La empresa " + oferta.getEmpresa().getUsername() + " ha publicado una nueva oferta: " + oferta.getTitulo());
                    n.setEnlace("/profileView/" + oferta.getEmpresa().getId());
                    notificacionRepository.save(n);
                }
            }
        }

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
                    String uploadDir = "uploads/cvs/"; 
                    Path uploadPath = Paths.get(uploadDir);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    
                    String fileName = System.currentTimeMillis() + "_" + archivoCv.getOriginalFilename().replaceAll("\\s+", "_");
                    Path filePath = uploadPath.resolve(fileName);
                    
                    Files.copy(archivoCv.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    String rutaCV = "/" + uploadDir + fileName;
                    
                    p.setCvAdjunto(rutaCV);
                    
                    if (usuario.getCurriculum() == null || usuario.getCurriculum().isEmpty()) {
                        usuario.setCurriculum(rutaCV);
                        usuarioRepository.save(usuario);
                    }
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            postulacionRepository.save(p);

            // Notificar a la empresa si no es una actualización
            if (!esActualizacion && o.get().getEmpresa() != null) {
                Usuario empresa = o.get().getEmpresa();
                if (!empresa.isSilenciarNotificaciones()) {
                    Notificacion n = new Notificacion();
                    n.setUsuario(empresa);
                    n.setTipo("NUEVA_POSTULACION");
                    n.setMensaje("El usuario " + usuario.getUsername() + " se ha postulado a tu oferta: " + o.get().getTitulo());
                    n.setEnlace("/ver-postulaciones/" + ofertaId);
                    notificacionRepository.save(n);
                }
            }
        }
        return "redirect:/jobsearching?" + (esActualizacion ? "actualizado=true" : "postulado=true");
    }

    @GetMapping("/api/postulaciones/mis-ofertas-ids")
    @ResponseBody
    public ResponseEntity<List<java.util.Map<String, Object>>> obtenerMisOfertasPostuladas(@RequestParam("username") String username) {
        return usuarioRepository.findByUsername(username)
            .map(u -> ResponseEntity.ok(postulacionRepository.findByDesarrolladorId(u.getId()).stream()
                .map(p -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("ofertaId", p.getOferta().getId());
                    map.put("estado", p.getEstado());
                    return map;
                }).collect(Collectors.toList())))
            .orElse(ResponseEntity.ok(List.of()));
    }

    @PostMapping("/api/postulaciones/{id}/update-status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable("id") Long id, @RequestParam("estado") String estado) {
        try {
            Optional<Postulacion> postOpt = postulacionRepository.findById(id);
            if (postOpt.isPresent()) {
                Postulacion p = postOpt.get();
                p.setEstado(estado);
                postulacionRepository.save(p);

                // Notificar al desarrollador
                Notificacion n = new Notificacion();
                n.setUsuario(p.getDesarrollador());
                boolean aceptada = "ACEPTADA".equals(estado);
                n.setTipo(aceptada ? "POSTULACION_ACEPTADA" : "POSTULACION_RECHAZADA");
                String msg = aceptada ? "¡Felicidades! Tu postulación para '" + p.getOferta().getTitulo() + "' ha sido ACEPTADA." 
                                      : "Tu postulación para '" + p.getOferta().getTitulo() + "' ha sido rechazada.";
                n.setMensaje(msg);
                n.setEnlace("/jobsearching");
                notificacionRepository.save(n);

                return ResponseEntity.ok().body("{\"success\": true}");
            }
            return ResponseEntity.badRequest().body("{\"success\": false}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"success\": false}");
        }
    }

    @PostMapping("/api/postulaciones/eliminar")
    @ResponseBody
    public ResponseEntity<?> eliminarPostulacion(@RequestParam("ofertaId") Long ofertaId, @RequestParam("username") String username) {
        try {
            Optional<Usuario> userOpt = usuarioRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                Optional<Postulacion> postOpt = postulacionRepository.findByDesarrolladorId(userOpt.get().getId()).stream()
                    .filter(p -> p.getOferta().getId().equals(ofertaId)).findFirst();
                if (postOpt.isPresent()) {
                    postulacionRepository.delete(postOpt.get());
                    return ResponseEntity.ok().body("{\"success\": true}");
                }
            }
            return ResponseEntity.badRequest().body("{\"success\": false}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"success\": false}");
        }
    }
}