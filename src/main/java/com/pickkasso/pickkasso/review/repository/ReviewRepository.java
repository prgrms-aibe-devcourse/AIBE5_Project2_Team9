package com.pickkasso.pickkasso.review.repository;

import com.pickkasso.pickkasso.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByReservationId(Long reservationId);
    Optional<Review> findByReservationId(Long reservationId);
    long countByReservation_Item_Id(Long itemId);
    long countByPhotographer_Id(Long photographerId);

    @EntityGraph(attributePaths = {"member", "photographer", "reservation", "reservation.item"})
    Page<Review> findByReservation_Item_Id(Long itemId, Pageable pageable);

    @EntityGraph(attributePaths = {"member", "photographer", "reservation", "reservation.item"})
    Page<Review> findByPhotographer_Id(Long photographerId, Pageable pageable);

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
