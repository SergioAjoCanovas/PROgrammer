package com.programmer.backend.controller;

import com.programmer.backend.domain.Proyecto;
import com.programmer.backend.domain.Tecnologia;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.ProyectoRepository;
import com.programmer.backend.repository.TecnologiaRepository;
import com.programmer.backend.repository.UsuarioRepository;
import com.programmer.backend.service.ProyectoService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/proyectos")
public class ProyectoController {

    private final ProyectoRepository proyectoRepository;
    private final ProyectoService proyectoService;
    private final UsuarioRepository usuarioRepository;
    private final TecnologiaRepository tecnologiaRepository;

    public ProyectoController(
            ProyectoRepository proyectoRepository,
            ProyectoService proyectoService,
            UsuarioRepository usuarioRepository,
            TecnologiaRepository tecnologiaRepository
    ) {
        this.proyectoRepository = proyectoRepository;
        this.proyectoService = proyectoService;
        this.usuarioRepository = usuarioRepository;
        this.tecnologiaRepository = tecnologiaRepository;
    }

    // =========================================================
    // CREAR PROYECTO
    // =========================================================
    @PostMapping("/crear")
    public String crearProyecto(@ModelAttribute Proyecto proyecto,
                                @RequestParam(value = "tecnologias", required = false) List<Long> tecnologias,
                                @RequestParam(value = "imagenes", required = false) MultipartFile[] imagenes,
                                HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return "redirect:/login";
        }

        proyecto.setAutor(usuario);

        String uploadDir = System.getProperty("user.dir") + "/uploads/projects/";

        try {
            for (int i = 0; i < imagenes.length; i++) {

                MultipartFile file = imagenes[i];

                if (file != null && !file.isEmpty()) {

                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    File dest = new File(uploadDir + fileName);

                    dest.getParentFile().mkdirs();
                    file.transferTo(dest);

                    String ruta = "/uploads/projects/" + fileName;

                    switch (i) {
                        case 0 -> proyecto.setFoto1(ruta);
                        case 1 -> proyecto.setFoto2(ruta);
                        case 2 -> proyecto.setFoto3(ruta);
                        case 3 -> proyecto.setFoto4(ruta);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Vincular tecnologías
        if (tecnologias != null && !tecnologias.isEmpty()) {
            List<Tecnologia> listaTechs = tecnologias.stream()
                    .map(id -> tecnologiaRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            proyecto.setTecnologias(listaTechs);
        }

        proyectoRepository.save(proyecto);

        return "redirect:/ownProfile";
    }

    // =========================================================
    // VER PROYECTO
    // =========================================================
    @GetMapping("/proyecto/{id}")
    public String verProyecto(@PathVariable Long id, Model model) {

        Proyecto proyecto = proyectoRepository.findById(id).orElse(null);

        if (proyecto == null) {
            return "redirect:/ownProfile";
        }

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("autor", proyecto.getAutor());

        List<String> imagenes = Stream.of(
                proyecto.getFoto1(),
                proyecto.getFoto2(),
                proyecto.getFoto3(),
                proyecto.getFoto4()
        )
        .filter(Objects::nonNull)
        .filter(f -> !f.isBlank())
        .toList();

        model.addAttribute("imagenes", imagenes);

        return "UI/projectView/projectView";
    }

    // =========================================================
    // LISTA PROYECTOS
    // =========================================================
    @GetMapping("/projectList")
    public String projectList(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        return "redirect:/proyectos/projectList/" + usuario.getId();
    }

    @GetMapping("/projectList/{id}")
    public String projectListUser(@PathVariable Long id,
                                  HttpSession session,
                                  Model model) {

        Usuario targetUser = usuarioRepository.findById(id).orElse(null);

        if (targetUser == null) {
            return "redirect:/login";
        }

        List<Proyecto> proyectos = proyectoRepository.findByAutorId(targetUser.getId());

        model.addAttribute("usuario", targetUser);
        model.addAttribute("proyectos", proyectos);

        model.addAttribute("usuarioHeader",
                session.getAttribute("usuarioLogueado") != null
                        ? session.getAttribute("usuarioLogueado")
                        : targetUser);

        String cvUrl = targetUser.getCurriculum();
        model.addAttribute("cvUrl", cvUrl);
        model.addAttribute("cvNombre", extraerNombreCV(cvUrl));

        return "UI/projectlist/projectlist";
    }

    private String extraerNombreCV(String ruta) {
        if (ruta == null || ruta.isEmpty()) return null;

        String nombre = ruta.substring(ruta.lastIndexOf("/") + 1);

        if (nombre.contains("_")) {
            nombre = nombre.substring(nombre.indexOf("_") + 1);
        }

        return nombre;
    }

    // =========================================================
    // BORRAR PROYECTO
    // =========================================================
    @PostMapping("/delete/{id}")
    @ResponseBody
    public String eliminarProyecto(@PathVariable Long id, HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) return "NO_LOGIN";

        Proyecto proyecto = proyectoRepository.findById(id).orElse(null);

        if (proyecto == null) return "NOT_FOUND";

        boolean isOwner = proyecto.getAutor().getId().equals(usuario.getId());

        boolean isAdmin = usuario.getRol() != null &&
                ("ADMIN".equalsIgnoreCase(usuario.getRol().getNombre())
                        || usuario.getRol().getId() == 1L);

        boolean isProjectOwnerAdmin = proyecto.getAutor().getRol() != null &&
                ("ADMIN".equalsIgnoreCase(proyecto.getAutor().getRol().getNombre())
                        || proyecto.getAutor().getRol().getId() == 1L);

        if (!isOwner) {
            if (isAdmin && !isProjectOwnerAdmin) {
                // permitido
            } else {
                return "UNAUTHORIZED";
            }
        }

        proyectoService.eliminarProyecto(proyecto);

        return "OK";
    }

    // =========================================================
    // EDITAR PROYECTO
    // =========================================================
    @GetMapping("/editar/{id}")
    public String editarProyecto(@PathVariable Long id, Model model) {

        Proyecto proyecto = proyectoRepository.findById(id).orElse(null);

        if (proyecto == null) {
            return "redirect:/proyectos";
        }

        List<Tecnologia> tecnologias = tecnologiaRepository.findAll();

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("tecnologias", tecnologias);

        model.addAttribute("tecnologiasSeleccionadas",
                proyecto.getTecnologias()
                        .stream()
                        .map(Tecnologia::getId)
                        .toList()
        );

        return "UI/createProject/createProject";
    }

    @PostMapping("/editar")
    public String guardarProyecto(@ModelAttribute Proyecto proyecto,
                                  @RequestParam(value = "tecnologias", required = false) List<Long> tecnologias,
                                  @RequestParam(value = "imagenes", required = false) MultipartFile[] imagenes) {

        Proyecto proyectoExistente = proyectoRepository.findById(proyecto.getId()).orElse(null);

        if (proyectoExistente == null) {
            return "redirect:/proyectos";
        }

        proyectoExistente.setTitulo(proyecto.getTitulo());
        proyectoExistente.setDescripcion(proyecto.getDescripcion());
        proyectoExistente.setRepoUrl(proyecto.getRepoUrl());

        if (tecnologias != null) {
            List<Tecnologia> tecnologiasActualizadas = tecnologias.stream()
                    .map(id -> tecnologiaRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            proyectoExistente.setTecnologias(tecnologiasActualizadas);
        }

        String uploadDir = System.getProperty("user.dir") + "/uploads/projects/";

        try {
            if (imagenes != null) {
                for (int i = 0; i < imagenes.length; i++) {

                    MultipartFile file = imagenes[i];

                    if (file != null && !file.isEmpty()) {

                        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                        File dest = new File(uploadDir + fileName);

                        dest.getParentFile().mkdirs();
                        file.transferTo(dest);

                        String ruta = "/uploads/projects/" + fileName;

                        switch (i) {
                            case 0 -> proyectoExistente.setFoto1(ruta);
                            case 1 -> proyectoExistente.setFoto2(ruta);
                            case 2 -> proyectoExistente.setFoto3(ruta);
                            case 3 -> proyectoExistente.setFoto4(ruta);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        proyectoRepository.save(proyectoExistente);

        return "redirect:/proyectos/proyecto/" + proyectoExistente.getId() + "?success=updated";
    }

    // =========================================================
    // HANDLER GLOBAL
    // =========================================================
    @ControllerAdvice
    public static class GlobalExceptionHandler {

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public String handleMaxSizeException(RedirectAttributes redirectAttributes) {
            redirectAttributes.addFlashAttribute("error", "FILE_TOO_LARGE");
            return "redirect:/createProject";
        }
    }
}