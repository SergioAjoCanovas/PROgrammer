package com.programmer.backend.repository;

import com.programmer.backend.domain.NewReview;
import com.programmer.backend.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NewReviewRepository extends JpaRepository<NewReview, Long> {

    List<NewReview> findByReceptor(Usuario receptor);

    // MEDIA GLOBAL
    @Query("SELECT AVG(r.rating) FROM NewReview r WHERE r.receptor = :receptor")
    Double getAverageRating(Usuario receptor);
}