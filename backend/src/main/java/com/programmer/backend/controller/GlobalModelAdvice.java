package com.programmer.backend.controller;

import com.programmer.backend.domain.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("usuarioHeader")
    public Usuario usuarioHeader(HttpSession session) {
        return (Usuario) session.getAttribute("usuarioLogueado");
    }
}