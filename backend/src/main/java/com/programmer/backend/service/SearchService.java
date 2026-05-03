package com.programmer.backend.service;

import com.programmer.backend.domain.PerfilDesarrollador;
import com.programmer.backend.domain.PerfilEmpresa;
import com.programmer.backend.domain.Tecnologia;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.PerfilDesarrolladorRepository;
import com.programmer.backend.repository.PerfilEmpresaRepository;
import com.programmer.backend.repository.TecnologiaRepository;
import com.programmer.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final List<String> TECHNOLOGY_PRIORITY = Arrays.asList(
            "JavaScript", "TypeScript", "React", "Angular", "Vue.js", "Svelte", "Next.js", "Nuxt.js",
            "HTML5", "CSS3", "Tailwind CSS", "Bootstrap",
            "Java", "Spring Boot", "Python", "Node.js", "C#", "PHP", "Kotlin", "Django", "Laravel", ".NET Core",
            "MySQL", "PostgreSQL", "MongoDB", "Redis", "SQLite", "Docker", "Kubernetes", "AWS", "Azure", "Google Cloud"
    );

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilDesarrolladorRepository perfilDesarrolladorRepository;

    @Autowired
    private TecnologiaRepository tecnologiaRepository;

    public List<PerfilDesarrollador> getAllDevelopers() {
        List<Usuario> developers = usuarioRepository.findByRolNombreIn(Arrays.asList("DEVELOPER", "ADMIN"));
        return developers.stream()
                .map(user -> perfilDesarrolladorRepository.findByUsuarioId(user.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(this::sortTecnologiasByPriority)
                .collect(Collectors.toList());
    }

        public List<PerfilDesarrollador> getFeaturedDevelopers(Long currentUserId) {
        List<Usuario> developers = usuarioRepository.findByRolNombreIn(Arrays.asList("DEVELOPER", "ADMIN"));
        return developers.stream()
                .filter(user -> currentUserId == null || !user.getId().equals(currentUserId))
                .map(user -> perfilDesarrolladorRepository.findByUsuarioId(user.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(perfil -> perfil.getTecnologias() != null && !perfil.getTecnologias().isEmpty())
                .sorted((p1, p2) -> Integer.compare(
                        p2.getUsuario().getFollowersCount(),
                        p1.getUsuario().getFollowersCount()))
                .limit(10)
                .peek(this::sortTecnologiasByPriority)
                .collect(Collectors.toList());
    }

    public List<PerfilDesarrollador> searchDevelopers(String query, List<Long> techIds, Long currentUserId) {
        List<Usuario> developers = usuarioRepository.findByRolNombreIn(Arrays.asList("DEVELOPER", "ADMIN"));
        return developers.stream()
                .filter(user -> currentUserId == null || !user.getId().equals(currentUserId))
                .map(user -> perfilDesarrolladorRepository.findByUsuarioId(user.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(perfil -> {
                    // Filtrar si no tiene tecnologías asignadas
                    if (perfil.getTecnologias() == null || perfil.getTecnologias().isEmpty()) {
                        return false;
                    }

                    // Filtrar por nombre si existe query
                    if (query != null && !query.trim().isEmpty()) {
                        if (!perfil.getUsuario().getUsername().toLowerCase().startsWith(query.toLowerCase().trim())) {
                            return false;
                        }
                    }
                    
                    // Filtrar por tecnologías: debe tener AL MENOS UNA de las seleccionadas
                    if (techIds != null && !techIds.isEmpty()) {
                        Set<Long> perfilTechIds = perfil.getTecnologias().stream()
                                .map(Tecnologia::getId)
                                .collect(Collectors.toSet());
                        if (!perfilTechIds.stream().anyMatch(techIds::contains)) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted((p1, p2) -> {
                    if (techIds == null || techIds.isEmpty()) return 0;

                    long matches1 = p1.getTecnologias().stream()
                            .filter(t -> techIds.contains(t.getId()))
                            .count();
                    long matches2 = p2.getTecnologias().stream()
                            .filter(t -> techIds.contains(t.getId()))
                            .count();

                    // 1. Primero por número de coincidencias con el filtro (Descendente)
                    if (matches1 != matches2) {
                        return Long.compare(matches2, matches1);
                    }

                    // 2. Si hay empate, el que tenga más tecnologías en total va primero (Descendente)
                    int total1 = p1.getTecnologias().size();
                    int total2 = p2.getTecnologias().size();
                    return Integer.compare(total2, total1);
                })
                .peek(this::sortTecnologiasByPriority)
                .collect(Collectors.toList());
    }

    private void sortTecnologiasByPriority(PerfilDesarrollador perfil) {
        Set<Tecnologia> ordenadas = perfil.getTecnologias().stream()
                .sorted(Comparator.comparingInt(this::getTechPriority))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        perfil.setTecnologias(ordenadas);
    }

    private int getTechPriority(Tecnologia tecnologia) {
        int index = TECHNOLOGY_PRIORITY.indexOf(tecnologia.getNombre());
        return index >= 0 ? index : TECHNOLOGY_PRIORITY.size();
    }

    public List<Tecnologia> getAllTechnologies() {
        return tecnologiaRepository.findAll();
    }


        
    @Autowired
    private PerfilEmpresaRepository perfilEmpresaRepository;

    // Método para las empresas destacadas (el carrusel inicial)
    public List<PerfilEmpresa> getFeaturedCompanies(Long currentUserId) {
        List<Usuario> companies = usuarioRepository.findByRolNombre("COMPANY");
        return companies.stream()
                .filter(user -> currentUserId == null || !user.getId().equals(currentUserId))
                .map(user -> perfilEmpresaRepository.findByUsuarioId(user.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted((p1, p2) -> Integer.compare(
                        p2.getUsuario().getFollowersCount(),
                        p1.getUsuario().getFollowersCount()))
                .limit(10)
                .collect(Collectors.toList());
    }

    // Método para la búsqueda filtrada
    public List<PerfilEmpresa> searchCompanies(String query, List<Long> techIds, Long currentUserId) {
        List<Usuario> companies = usuarioRepository.findByRolNombre("COMPANY");
        return companies.stream()
                .filter(user -> currentUserId == null || !user.getId().equals(currentUserId))
                .map(user -> perfilEmpresaRepository.findByUsuarioId(user.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(perfil -> {
                    if (query != null && !query.trim().isEmpty()) {
                        String q = query.toLowerCase().trim();
                        if (!(perfil.getUsuario().getUsername().toLowerCase().contains(q) ||
                             (perfil.getSector() != null && perfil.getSector().toLowerCase().contains(q)))) {
                             return false;
                        }
                    }

                    if (techIds != null && !techIds.isEmpty()) {
                        Set<Long> perfilTechIds = perfil.getTecnologias().stream()
                                .map(Tecnologia::getId)
                                .collect(Collectors.toSet());
                        if (!perfilTechIds.stream().anyMatch(techIds::contains)) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted((p1, p2) -> {
                    if (techIds == null || techIds.isEmpty()) return 0;

                    long matches1 = p1.getTecnologias().stream()
                            .filter(t -> techIds.contains(t.getId()))
                            .count();
                    long matches2 = p2.getTecnologias().stream()
                            .filter(t -> techIds.contains(t.getId()))
                            .count();

                    if (matches1 != matches2) {
                        return Long.compare(matches2, matches1);
                    }

                    int total1 = p1.getTecnologias().size();
                    int total2 = p2.getTecnologias().size();
                    return Integer.compare(total2, total1);
                })
                .collect(Collectors.toList());
    }
}