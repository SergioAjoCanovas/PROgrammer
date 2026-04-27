package com.programmer.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.programmer.backend.domain.PerfilDesarrollador;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ViewController {

    @Autowired
    private SearchService searchService;

    // =========================
    // HOME
    // =========================
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/main")
    public String main() {
        return "UI/main";
    }

    // =========================
    // AUTH
    // =========================
    @GetMapping("/login")
    public String login() {
        return "UI/loginPage/loginPage";
    }

    @GetMapping("/signUp")
    public String signup() {
        return "UI/signUpPage/signUpPage";
    }

    // =========================
    // PERFIL
    // =========================
    @GetMapping("/profileView")
    public String profileView() {
        return "UI/profileView/profileView";
    }

    // =========================
    // PROYECTOS
    // =========================
    @GetMapping("/createProject")
    public String createProject() {
        return "UI/createProject/createProject";
    }

    @GetMapping("/editProject")
    public String editProject() {
        return "UI/editProject/editProject";
    }

    @GetMapping("/projectlist")
    public String projectlist() {
        return "UI/projectlist/projectlist";
    }

    @GetMapping("/projectview")
    public String projectview() {
        return "UI/projectview/projectview";
    }
    
    @GetMapping("/newprojectreview")
    public String newprojectreview() {
        return "UI/newprojectreview/newprojectreview";
    }

    @GetMapping("/viewprojectreviews")
    public String viewprojectreviews() {
        return "UI/viewprojectreviews/viewprojectreviews";
    }

    // =========================
    // EMPLEO
    // =========================
    @GetMapping("/jobview")
    public String jobview() {
        return "UI/jobview/jobview";
    }

    // =========================
    // RED / BÚSQUEDA (CORREGIDO)
    // =========================
    @GetMapping("/searchProgrammer")
    public String searchProgrammer(HttpSession session, Model model) {
        // Obtenemos el ID del usuario logueado para no mostrarse a sí mismo en destacados
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        Long currentUserId = (usuario != null) ? usuario.getId() : null;

        model.addAttribute("developers", searchService.getFeaturedDevelopers(currentUserId));
        model.addAttribute("allTechnologies", searchService.getAllTechnologies());
        
        // Inicializamos lista vacía para que el HTML no de error al buscar "selectedTechIds"
        model.addAttribute("selectedTechIds", new ArrayList<Long>());
        
        return "UI/searchHome/searchProgrammer";
    }

    @GetMapping("/searchProgrammer/search")
    public String searchProgrammer(@RequestParam(value = "query", required = false) String query, 
                                   @RequestParam(value = "tech", required = false) List<Long> techIds, 
                                   HttpSession session,
                                   Model model) {
        
        // Obtenemos el ID del usuario logueado para pasárselo al servicio
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        Long currentUserId = (usuario != null) ? usuario.getId() : null;

        // LLAMADA CORREGIDA: Ahora enviamos los 3 parámetros que pide el SearchService
        List<PerfilDesarrollador> results = searchService.searchDevelopers(query, techIds, currentUserId);
        
        model.addAttribute("developers", results);
        model.addAttribute("searchQuery", query);
        model.addAttribute("resultCount", results.size());
        model.addAttribute("allTechnologies", searchService.getAllTechnologies());
        
        // PERSISTENCIA DE FILTROS:
        // Si techIds es null (no se marcó nada), enviamos lista vacía para evitar errores en el HTML
        model.addAttribute("selectedTechIds", techIds != null ? techIds : new ArrayList<Long>());
        
        return "UI/searchHome/searchProgrammer";
    }

    @GetMapping("/serachCompanies") 
    public String serachCompanies() {
        return "UI/searchHome/serachCompanies";
    }

    // =========================
    // MENÚ / GENERAL
    // =========================
    @GetMapping("/menu")
    public String menu() {
        return "UI/menu/menu";
    }

    @GetMapping("/chats")
    public String chats() {
        return "UI/chats/chats";
    }

    // =========================
    // PUBLICAR OFERTA
    // =========================
    @GetMapping("/publishOffer")
    public String publishOffer() {
        return "UI/company/publishOffer";
    }

    @GetMapping("/loginPage")
    public String loginPage() {
        return "UI/loginPage/loginPage";
    }

    // =======================================
    // TERMINOS, PRIVACIDAD, SOBRE NOSOTROS
    // =======================================
    @GetMapping("/terminos")
    public String mostrarTerminos() {
        return "UI/terminos/terminos"; 
    }

    @GetMapping("/privacidad")
    public String mostrarPrivacidad() {
        return "UI/privacidad/privacidad"; 
    }

    @GetMapping("/nosotros")
    public String mostrarNosotros(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        model.addAttribute("usuarioLogueado", usuario);
        return "UI/nosotros/nosotros"; 
    }
}