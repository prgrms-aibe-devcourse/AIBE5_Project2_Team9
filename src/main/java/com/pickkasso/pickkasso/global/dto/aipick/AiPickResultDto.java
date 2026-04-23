package com.pickkasso.pickkasso.global.dto.aipick;

import java.util.List;

public record AiPickResultDto(
    AiPickQueryDto parsedQuery,
    List<ChatMessageDto> messages,
    List<AiRecommendationDto> recommendations,
    boolean needsNearbyExpansionConfirm,
    String suggestedNearbyLabel
) {
}
