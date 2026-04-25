package com.pickkasso.pickkasso.item.repository;

import com.pickkasso.pickkasso.item.entity.Plan;
import com.pickkasso.pickkasso.item.entity.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    @Query("SELECT p FROM Plan p " +
        "WHERE p.item.id = :itemId " +
        "AND p.planType = :planType ")
    Optional<Plan> findByItemIdAndPlanType(@Param("itemId") Long itemId, @Param("planType") PlanType planType);

    @Query("SELECT p FROM Plan p " +
        "WHERE p.item.id = :itemId " +
        "AND p.enabled = true")
    List<Plan> findByItemIdAndEnabled(@Param("itemId") Long itemId);

    @Query("SELECT p FROM Plan p " +
        "JOIN FETCH p.item i " +
        "JOIN FETCH i.photographer ph " +
        "WHERE p.id = :id")
    Optional<Plan> findByIdWithItemAndPhotographer(@Param("id") Long id);
}
