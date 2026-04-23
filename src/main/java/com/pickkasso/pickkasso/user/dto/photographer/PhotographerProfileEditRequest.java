package com.pickkasso.pickkasso.user.dto.photographer;

import com.pickkasso.pickkasso.user.entity.ResponseTime;

import java.util.List;

public record PhotographerProfileEditRequest(
        String imgUrl,
        String nickname,
        String intro,
        String link,
        Integer contactableStartTime,
        Integer contactableEndTime,
        ResponseTime responseTime,
        List<String> tools,
        List<CareerDto> careers,
        List<EducationDto> educations
) {
}
