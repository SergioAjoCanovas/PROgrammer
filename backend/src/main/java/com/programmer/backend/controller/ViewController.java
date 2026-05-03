package com.programmer.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.programmer.backend.domain.PerfilDesarrollador;
import com.programmer.backend.domain.PerfilEmpresa;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ViewController {

    @Autowired
    private SearchService searchService;

    // =========================
    // HOME / AUTH
    // =========================
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/main")
    public String main() {
        return "UI/main";
    }

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
    // BÚSQUEDA PROGRAMADORES
    // =========================
    @GetMapping("/searchProgrammer")
    public String searchProgrammer(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        Long currentUserId = (usuario != null) ? usuario.getId() : null;

        model.addAttribute("developers", searchService.getFeaturedDevelopers(currentUserId));
        model.addAttribute("allTechnologies", searchService.getAllTechnologies());
        model.addAttribute("selectedTechIds", new ArrayList<Long>());
        
        return "UI/searchHome/searchProgrammer";
    }

    @GetMapping("/searchProgrammer/search")
    public String searchProgrammer(@RequestParam(value = "query", required = false) String query, 
                                   @RequestParam(value = "tech", required = false) List<Long> techIds, 
                                   HttpSession session,
                                   Model model) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        Long currentUserId = (usuario != null) ? usuario.getId() : null;

        List<PerfilDesarrollador> results = searchService.searchDevelopers(query, techIds, currentUserId);
        
        model.addAttribute("developers", results);
        model.addAttribute("searchQuery", query);
        model.addAttribute("allTechnologies", searchService.getAllTechnologies());
        model.addAttribute("selectedTechIds", techIds != null ? techIds : new ArrayList<Long>());
        
        return "UI/searchHome/searchProgrammer";
    }

    // =========================
    // BÚSQUEDA EMPRESAS (ACTUALIZADO)
    // =========================
   @GetMapping("/searchCompanies") 
    public String searchCompanies(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        Long currentUserId = (usuario != null) ? usuario.getId() : null;

        model.addAttribute("companies", searchService.getFeaturedCompanies(currentUserId)); 
        model.addAttribute("searchQuery", "");
        model.addAttribute("allTechnologies", searchService.getAllTechnologies());
        model.addAttribute("selectedTechIds", new ArrayList<Long>());
        
        return "UI/searchHome/searchCompanies";
    }

    @GetMapping("/searchCompanies/search")
    public String searchCompaniesSearch(@RequestParam(value = "query", required = false) String query,
                                        @RequestParam(value = "tech", required = false) List<Long> techIds,
                                        HttpSession session,
                                        Model model) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        Long currentUserId = (usuario != null) ? usuario.getId() : null;

        model.addAttribute("companies", searchService.searchCompanies(query, techIds, currentUserId)); 
        model.addAttribute("searchQuery", query);
        model.addAttribute("allTechnologies", searchService.getAllTechnologies()); 
        model.addAttribute("selectedTechIds", techIds != null ? techIds : new ArrayList<Long>());
        
        return "UI/searchHome/searchCompanies";
    }

    // =========================
    // MENÚ / CHATS / EMPLEO
    // =========================
    @GetMapping("/menu")
    public String menu() {
        return "UI/menu/menu";
    }

    @GetMapping("/chats")
    public String chats() {
        return "UI/chats/chats";
    }

    @GetMapping("/jobview")
    public String jobview() {
        return "UI/jobview/jobview";
    }

    @GetMapping("/publishOffer")
    public String publishOffer() {
        return "UI/company/publishOffer";
    }

    // =======================================
    // LEGAL Y OTROS
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


    // =======================================
    // PERFIL LIMITADO PARA USUARIO
    // =======================================
    @GetMapping("/limitedProfile")
    public String limitedProfile(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        
        if (usuario == null || !"VISITOR".equals(usuario.getRol().getNombre())) {
            return "redirect:/login";
        }

        model.addAttribute("usuarioHeader", usuario);
        return "UI/ownProfile/ownProfileLimited";
    }
}