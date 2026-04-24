package com.pickkasso.pickkasso.review.repository;

import com.pickkasso.pickkasso.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByReservationId(Long reservationId);
    Optional<Review> findByReservationId(Long reservationId);
}
