package com.programmer.backend.controller;

import com.programmer.backend.domain.Proyecto;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.ProyectoRepository;
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
import java.util.stream.Stream;

@Controller
@RequestMapping("/proyectos")
public class ProyectoController {

    private final ProyectoRepository proyectoRepository;
    private final ProyectoService proyectoService;

    public ProyectoController(ProyectoRepository proyectoRepository,
                              ProyectoService proyectoService) {
        this.proyectoRepository = proyectoRepository;
        this.proyectoService = proyectoService;
    }

    // =========================================================
    // CREAR PROYECTO
    // =========================================================
    @PostMapping("/crear")
    public String crearProyecto(@ModelAttribute Proyecto proyecto,
                                @RequestParam("imagenes") MultipartFile[] imagenes,
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
    public String projectList(HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return "redirect:/login";
        }

        List<Proyecto> proyectos =
                proyectoRepository.findByAutorId(usuario.getId());

        model.addAttribute("usuario", usuario);
        model.addAttribute("proyectos", proyectos);

        return "UI/projectlist/projectlist";
    }

    // =========================================================
    // BORRAR PROYECTO (POST + FETCH FRIENDLY)
    // =========================================================
    @PostMapping("/delete/{id}")
    @ResponseBody
    public String eliminarProyecto(@PathVariable Long id, HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return "NO_LOGIN";
        }

        Proyecto proyecto = proyectoRepository.findById(id).orElse(null);

        if (proyecto == null) {
            return "NOT_FOUND";
        }

        if (!proyecto.getAutor().getId().equals(usuario.getId())) {
            return "UNAUTHORIZED";
        }

        proyectoService.eliminarProyecto(proyecto);

        return "OK";
    }

    // =========================================================
    // HANDLER
    // =========================================================
    @ControllerAdvice
    public static class GlobalExceptionHandler {

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public String handleMaxSizeException(RedirectAttributes redirectAttributes) {
            redirectAttributes.addFlashAttribute("error", "FILE_TOO_LARGE");
            return "redirect:/proyectos/crear";
        }
    }
}