package com.pickkasso.pickkasso.user.repository;

import com.pickkasso.pickkasso.user.entity.PortfolioImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PortfolioImgRepository extends JpaRepository<PortfolioImg, Long> {
    @Query("SELECT pi FROM PortfolioImg pi " +
           "WHERE pi.portfolio.photographer.id = :photographerId " +
               "AND pi.imgOrder = 0")
    List<PortfolioImg> findRepresentativeImgsByPhotographerId(@Param("photographerId") Long photographerId);
}
