package com.pickkasso.pickkasso.user.dto.photographer;

public record PhotographerPortfolioSummaryDto(
        Long portfolioId,
        String name,
        String description,
        String imgUrl
) {
}
