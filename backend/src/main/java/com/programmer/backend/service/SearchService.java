package com.programmer.backend.service;

import com.programmer.backend.domain.PerfilDesarrollador;
import com.programmer.backend.domain.Tecnologia;
import com.programmer.backend.domain.Usuario;
import com.programmer.backend.repository.PerfilDesarrolladorRepository;
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

    public List<PerfilDesarrollador> getAllDevelopers() {
        List<Usuario> developers = usuarioRepository.findByRolNombre("DEVELOPER");
        return developers.stream()
                .map(user -> perfilDesarrolladorRepository.findByUsuarioId(user.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(this::sortTecnologiasByPriority)
                .collect(Collectors.toList());
    }

    public List<PerfilDesarrollador> getFeaturedDevelopers() {
        List<Usuario> developers = usuarioRepository.findByRolNombre("DEVELOPER");
        return developers.stream()
                .limit(10)
                .map(user -> perfilDesarrolladorRepository.findByUsuarioId(user.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(this::sortTecnologiasByPriority)
                .collect(Collectors.toList());
    }

    public List<PerfilDesarrollador> searchDevelopersByUsernameStartingWith(String prefix) {
        List<Usuario> developers = usuarioRepository.findByRolNombre("DEVELOPER");
        return developers.stream()
                .filter(user -> user.getUsername().toLowerCase().startsWith(prefix.toLowerCase()))
                .map(user -> perfilDesarrolladorRepository.findByUsuarioId(user.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
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
}