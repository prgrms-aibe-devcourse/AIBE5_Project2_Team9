package com.pickkasso.pickkasso.user.dto.photographer;

import java.util.List;

public record PhotographerProfileResponse(
        Long photographerId,
        Long profileId,
        String imgUrl,
        String nickname,
        String intro,
        String link,
        List<String> tools,
        List<CareerDto> careers,
        List<EducationDto> educations
) {
}
