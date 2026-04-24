package com.programmer.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.programmer.backend.domain.Usuario;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

@Controller
public class ViewController {

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
    @GetMapping("/ownProfile")
    public String ownProfile(HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuarioHeader", usuario);

        return "UI/ownProfile/ownProfile";
    }

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
    // RED / BÚSQUEDA
    // =========================
    @GetMapping("/searchProgrammer")
    public String searchProgrammer() {
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
    // PUBLICAR OFERTA (CASO ESPECIAL)
    // =========================
    @GetMapping("/publishOffer")
    public String publishOffer() {
        return "UI/company/publishOffer";
    }

    @GetMapping("/loginPage")
    public String loginPage() {
        return "UI/loginPage/loginPage";
    }

    // =========================
    // TECNOLOGÍAS
    // =========================
    @GetMapping("/addTechnology")
    public String addTechnology(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuarioHeader", usuario);
        return "UI/addTechnology/addTechnology"; 
    }



    // =======================================
    // TERMINOS,PRIVACIDAD,SOBRE NOSOTROS
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