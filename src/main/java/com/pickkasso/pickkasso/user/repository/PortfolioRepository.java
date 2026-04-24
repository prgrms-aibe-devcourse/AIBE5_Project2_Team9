package com.pickkasso.pickkasso.user.repository;

import com.pickkasso.pickkasso.user.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio,Long> {
    Optional<Portfolio> findByIdAndPhotographerId(Long portfolioId, Long photographerId);
    List<Portfolio> findByPhotographerIdOrderByIdDesc(Long photographerId);
    boolean existsByPhotographerId(Long photographerId);
}
