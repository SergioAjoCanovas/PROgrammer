package com.programmer.backend.controller;

import com.programmer.backend.domain.Proyecto;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.ProyectoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.io.File;

@Controller
@RequestMapping("/proyectos")
public class ProyectoController {

    private final ProyectoRepository proyectoRepository;

    public ProyectoController(ProyectoRepository proyectoRepository) {
        this.proyectoRepository = proyectoRepository;
    }

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
    
                if (!file.isEmpty()) {
    
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

    @GetMapping("/proyecto/{id}")
    public String verProyecto(@PathVariable Long id, Model model, HttpSession session) {

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

    @ControllerAdvice
    public class GlobalExceptionHandler {

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public String handleMaxSizeException(RedirectAttributes redirectAttributes) {

            redirectAttributes.addFlashAttribute("error", "FILE_TOO_LARGE");
            return "redirect:/proyectos/crear";
        }
    }
}