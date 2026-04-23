package com.pickkasso.pickkasso.global.dto.aipick;

import java.util.List;

public record AiPickQueryDto(
    String rawQuery,
    String requestedDate,
    String location,
    Integer maxPrice,
    String category,
    List<String> styleTags
) {
}
