package com.programmer.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.programmer.backend.domain.Tecnologia;
import com.programmer.backend.repository.TecnologiaRepository;

@Controller
public class StackController {

    @Autowired
    private TecnologiaRepository tecnologiaRepository;

    @GetMapping("/stack")
    public String mostrarStack(Model model) {

        List<Tecnologia> tecnologias = tecnologiaRepository.findAll()
            .stream()
            .filter(t -> t.getCategoria() != null)
            .toList();

        Map<String, List<Tecnologia>> tecnologiasPorCategoria = tecnologias.stream()
            .sorted(Comparator.comparing(t -> t.getCategoria().getId()))
            .collect(Collectors.groupingBy(
                t -> t.getCategoria().getNombre(),
                LinkedHashMap::new,
                Collectors.toList()
            ));

        model.addAttribute("tecnologiasPorCategoria", tecnologiasPorCategoria);

        return "UI/addTechnology/addTechnology";
    }
}