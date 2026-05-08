package com.programmer.backend.service;

import com.programmer.backend.domain.ProjectReview;
import com.programmer.backend.repository.ProjectReviewRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectReviewService {

    private final ProjectReviewRepository reviewRepository;

    public ProjectReviewService(ProjectReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // =========================================================
    // REVIEWS POR PROYECTO
    // =========================================================
    public List<ProjectReview> findByProjectId(Long projectId) {
        return reviewRepository.findByProyectoId(projectId);
    }

    // =========================================================
    // MEDIAS
    // =========================================================
    public double mediaArquitectura(Long projectId) {
        Double val = reviewRepository.mediaArquitectura(projectId);
        return val != null ? val : 0;
    }
    
    public double mediaLimpieza(Long projectId) {
        Double val = reviewRepository.mediaLimpieza(projectId);
        return val != null ? val : 0;
    }
    
    public double mediaDocumentacion(Long projectId) {
        Double val = reviewRepository.mediaDocumentacion(projectId);
        return val != null ? val : 0;
    }

    // =========================================================
    // (OPCIONAL PERO ÚTIL A FUTURO) MEDIA TOTAL
    // =========================================================
    public double mediaTotal(Long projectId) {
        double arq = mediaArquitectura(projectId);
        double lim = mediaLimpieza(projectId);
        double doc = mediaDocumentacion(projectId);
    
        return (arq + lim + doc) / 3;
    }
}