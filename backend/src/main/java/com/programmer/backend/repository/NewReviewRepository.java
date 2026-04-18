package com.programmer.backend.repository;

import com.programmer.backend.domain.NewReview;
import com.programmer.backend.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NewReviewRepository extends JpaRepository<NewReview, Long> {

    List<NewReview> findByReceptor(Usuario receptor);

    Optional<NewReview> findByAutorAndReceptor(Usuario autor, Usuario receptor);

    @Query("SELECT AVG(r.rating) FROM NewReview r WHERE r.receptor = :receptor")
    Double getAverageRating(Usuario receptor);
}