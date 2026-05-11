package com.programmer.backend.service;

import com.programmer.backend.domain.*;
import com.programmer.backend.repository.PerfilDesarrolladorRepository;
import com.programmer.backend.repository.TecnologiaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PerfilDesarrolladorService {

    private final PerfilDesarrolladorRepository devRepo;
    private final TecnologiaRepository tecnologiaRepository;

    public PerfilDesarrolladorService(PerfilDesarrolladorRepository devRepo,
                                      TecnologiaRepository tecnologiaRepository) {
        this.devRepo = devRepo;
        this.tecnologiaRepository = tecnologiaRepository;
    }

   
    public PerfilDesarrollador getOrCreateProfile(Usuario usuario) {

        return devRepo.findByUsuarioIdWithTecnologias(usuario.getId())
                .orElseGet(() -> {
                    PerfilDesarrollador nuevo = new PerfilDesarrollador();
                    nuevo.setUsuario(usuario);
                    return devRepo.save(nuevo);
                });
    }

   
    public List<Tecnologia> getTecnologiasOrdenadas(PerfilDesarrollador perfil) {

        return perfil.getTecnologias().stream()
                .sorted(Comparator.comparing(Tecnologia::getNombre))
                .toList();
    }

   
    @Transactional
    public void asignarTecnologias(Long perfilId, List<Long> techIds) {

        PerfilDesarrollador perfil = devRepo.findById(perfilId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        Set<Tecnologia> tecnos = new HashSet<>(
                tecnologiaRepository.findAllById(techIds)
        );

        perfil.getTecnologias().clear();
        perfil.getTecnologias().addAll(tecnos);

        devRepo.save(perfil);
    }
}