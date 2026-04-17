package com.pickkasso.pickkasso.user.dto.portfolio;

import com.pickkasso.pickkasso.user.entity.PortfolioProjectType;

import java.time.LocalDate;
import java.util.List;

public record PortfolioDto(
        String name,
        String description,
        LocalDate startTime,
        LocalDate endTime,
        List<PortfolioImgDto> portfolioImgDtoList,
        List<Long> tagIdList,
        PortfolioProjectType projectType
) {
}
