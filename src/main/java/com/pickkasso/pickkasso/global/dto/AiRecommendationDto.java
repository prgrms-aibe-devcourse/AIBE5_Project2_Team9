package com.pickkasso.pickkasso.global.dto;

public record AiRecommendationDto(
    Long itemId,
    Long photographerId,
    String category,
    String title,
    String photographerName,
    Double rating,
    Integer reviewCount,
    String region,
    Integer priceFrom,
    String reason,
    String thumbUrl
) {
}
