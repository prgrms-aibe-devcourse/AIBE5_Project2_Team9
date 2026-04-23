package com.pickkasso.pickkasso.global.dto;

import java.util.List;

public record AiPickResultDto(
    AiPickQueryDto parsedQuery,
    List<ChatMessageDto> messages,
    List<AiRecommendationDto> recommendations,
    boolean needsNearbyExpansionConfirm,
    String suggestedNearbyLabel
) {
}
