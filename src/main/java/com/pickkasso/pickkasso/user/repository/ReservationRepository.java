package com.pickkasso.pickkasso.user.repository;

import com.pickkasso.pickkasso.user.entity.Reservation;
import com.pickkasso.pickkasso.user.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            select r from Reservation r
              join fetch r.member m
              join fetch r.item i
            where r.photographer.id = :photographerId
              and r.status = :status
            order by r.scheduledAt asc
            """)
    List<Reservation> findPendingByPhotographer(
            @Param("photographerId") Long photographerId,
            @Param("status") ReservationStatus status);

    @Query("""
            select r from Reservation r
              join fetch r.member m
              join fetch r.item i
            where r.photographer.id = :photographerId
              and r.scheduledAt >= :from
              and r.scheduledAt < :to
              and r.status in :statuses
            order by r.scheduledAt asc
            """)
    List<Reservation> findInRange(
            @Param("photographerId") Long photographerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("statuses") Collection<ReservationStatus> statuses);

    @Query("""
            select r from Reservation r
              join fetch r.member m
              join fetch r.item i
            where r.photographer.id = :photographerId
            order by r.scheduledAt desc
            """)
    List<Reservation> findAllByPhotographerIdForManagement(@Param("photographerId") Long photographerId);

    long countByPhotographerIdAndStatusAndScheduledAtBetween(
            Long photographerId, ReservationStatus status,
            LocalDateTime from, LocalDateTime to);

    long countByPhotographerIdAndStatus(Long photographerId, ReservationStatus status);

    long countByPhotographerId(Long photographerId);

    Optional<Reservation> findByIdAndPhotographerId(Long id, Long photographerId);

    @Query("""
            select r from Reservation r
              join fetch r.member m
              join fetch r.item i
              join fetch i.tag t
              join fetch i.photographer p
            where r.id = :id
            """)
    Optional<Reservation> findByIdWithDetails(@Param("id") Long id);

    List<Reservation> findByStatusAndScheduledAtBefore(ReservationStatus status, LocalDateTime before);

    @Query("""
            select r from Reservation r
              join fetch r.item i
              join fetch i.photographer p
            where r.member.id = :memberId
            order by r.scheduledAt desc
            """)
    List<Reservation> findByMemberIdWithDetails(@Param("memberId") Long memberId);
}
