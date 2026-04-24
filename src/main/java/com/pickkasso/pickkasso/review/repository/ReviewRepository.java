package com.pickkasso.pickkasso.review.repository;

import com.pickkasso.pickkasso.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByReservationId(Long reservationId);
    Optional<Review> findByReservationId(Long reservationId);

    @Query("""
            select r from Review r
              join fetch r.member m
              join fetch r.reservation res
              join fetch res.item i
            where r.photographer.id = :photographerId
            order by r.createdAt desc
            """)
    List<Review> findByPhotographerIdWithDetails(@Param("photographerId") Long photographerId);

    @Query("""
            select r from Review r
              join fetch r.member m
              join fetch r.reservation res
              join fetch res.item i
            where res.item.id = :itemId
            order by r.createdAt desc
            """)
    List<Review> findByItemIdWithDetails(@Param("itemId") Long itemId);
}
