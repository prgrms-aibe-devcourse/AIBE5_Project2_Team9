package com.pickkasso.pickkasso.item.repository;

import com.pickkasso.pickkasso.item.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    @Query("SELECT p FROM Plan p JOIN FETCH p.item i JOIN FETCH i.photographer ph WHERE p.id = :id")
    Optional<Plan> findByIdWithItemAndPhotographer(@Param("id") Long id);
}
