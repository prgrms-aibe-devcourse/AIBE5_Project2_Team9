package com.pickkasso.pickkasso.user.dto.photographer;

import com.pickkasso.pickkasso.user.entity.ResponseTime;

import java.util.List;

public record PhotographerProfileResponse(
        Long photographerId,
        Long profileId,
        String imgUrl,
        String nickname,
        String intro,
        String link,
        Integer purchaseCount,
        Long reviewScore,
        Integer reviewCount,
        Integer contactableStartTime,
        Integer contactableEndTime,
        ResponseTime responseTime,
        List<String> tools,
        List<CareerDto> careers,
        List<EducationDto> educations,
        List<PhotographerPortfolioSummaryDto> portfolios
) {
}
